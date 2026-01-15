package com.example.bank_app.e2e;

import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.accounts.infrastructure.AccountJpaRepository;
import com.example.bank_app.transactions.infrastructure.TransactionJpaRepository;
import com.example.bank_app.users.infrastructure.UserJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests: controller -> application -> JPA -> H2 -> exception mapping.
 * Small number of "golden flows" gives maximum confidence.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiE2ETest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired UserJpaRepository userRepo;
    @Autowired AccountJpaRepository accountRepo;
    @Autowired TransactionJpaRepository txRepo;

    @BeforeEach
    void cleanDb() {
        // order matters because of FK relationships
        txRepo.deleteAll();
        accountRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void health_isOk() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void e2e_createUser_createAccount_deposit_withdraw_and_getBalance() throws Exception {
        UUID userId = createUser("Pavel");
        UUID accountId = createAccount(userId, AccountType.CARD, Currency.RUB, null);

        deposit(accountId, bd("150.00"));
        withdraw(accountId, bd("40"));

        JsonNode acc = getAccount(accountId);
        assertBdEquals("110.00", acc.get("balance"));
        assertThat(acc.get("blocked").asBoolean()).isFalse();
        assertThat(acc.get("currency").asText()).isEqualTo("RUB");
    }

    @Test
    void e2e_transfer_createsTransaction_and_updatesBothBalances_and_listByAccountWorks() throws Exception {
        UUID u1 = createUser("Alice");
        UUID u2 = createUser("Bob");

        UUID a1 = createAccount(u1, AccountType.CARD, Currency.RUB, null);
        UUID a2 = createAccount(u2, AccountType.SAVINGS, Currency.RUB, null);

        deposit(a1, bd("200"));

        UUID txId = transfer(a1, a2, bd("70.50"));

        JsonNode fromAcc = getAccount(a1);
        JsonNode toAcc = getAccount(a2);

        assertBdEquals("129.50", fromAcc.get("balance"));
        assertBdEquals("70.50", toAcc.get("balance"));

        // Get transaction by id
        JsonNode tx = getTransaction(txId);
        assertThat(UUID.fromString(tx.get("fromAccountId").asText())).isEqualTo(a1);
        assertThat(UUID.fromString(tx.get("toAccountId").asText())).isEqualTo(a2);
        assertBdEquals("70.50", tx.get("amount"));

        // List by account should include it (your repo sorts by createdAt desc)
        mvc.perform(get("/api/transactions").param("accountId", a1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].id", is(txId.toString())));
    }

    @Test
    void e2e_debt_flow_blocksOutgoing_allowsRepay_handlesOverpay_and_unblocks() throws Exception {
        UUID user = createUser("Debtor");
        UUID accId = createAccount(user, AccountType.CARD, Currency.RUB, null);

        deposit(accId, bd("100"));

        // Mark debt 50 => account blocked(DEBT), debtAmount=50
        mvc.perform(post("/api/accounts/{id}/debt", accId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Amount("50"))))
                .andExpect(status().isOk());

        JsonNode accAfterDebt = getAccount(accId);
        assertThat(accAfterDebt.get("blocked").asBoolean()).isTrue();
        assertThat(accAfterDebt.get("blockReason").asText()).isEqualTo("DEBT");
        assertBdEquals("50", accAfterDebt.get("debtAmount"));

        // Outgoing operation must fail: blocked
        UUID otherUser = createUser("Receiver");
        UUID otherAcc = createAccount(otherUser, AccountType.CARD, Currency.RUB, null);

        mvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Transfer(accId, otherAcc, bd("10")))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("Account blocked")));

        // Repay with overpay 60:
        // balance 100 - 50 debt = 50; extra 10 returns to balance => still 50; debt=0; unblock
        mvc.perform(post("/api/accounts/{id}/repay-debt", accId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Amount("60"))))
                .andExpect(status().isOk());

        JsonNode accAfterRepay = getAccount(accId);
        assertThat(accAfterRepay.get("blocked").asBoolean()).isFalse();
        assertThat(accAfterRepay.get("blockReason").isNull()).isTrue();
        assertBdEquals("0", accAfterRepay.get("debtAmount"));
        assertBdEquals("50", accAfterRepay.get("balance"));
    }

    @Test
    void e2e_errorMapping_notFound_and_badRequest() throws Exception {
        // 404 by unknown user
        mvc.perform(get("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.path", is(startsWith("/api/users/"))));

        // 400: negative amount on deposit
        UUID u = createUser("X");
        UUID acc = createAccount(u, AccountType.CARD, Currency.RUB, null);

        mvc.perform(post("/api/accounts/{id}/deposit", acc)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Amount("-1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("Amount must be positive")));
    }

    @Test
    void e2e_depositAccount_cannotWithdraw_beforeEndDate_conflict() throws Exception {
        UUID u = createUser("Depositor");
        UUID dep = createAccount(u, AccountType.DEPOSIT, Currency.RUB, LocalDate.now().plusDays(5));

        deposit(dep, bd("100"));

        mvc.perform(post("/api/accounts/{id}/withdraw", dep)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Amount("10"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("Deposit not expired")));
    }

    // ---------------- helpers ----------------

    private UUID createUser(String name) throws Exception {
        MvcResult res = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new CreateUser(name))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        JsonNode body = om.readTree(res.getResponse().getContentAsString());
        return UUID.fromString(body.get("id").asText());
    }

    private UUID createAccount(UUID userId, AccountType type, Currency currency, LocalDate depositEndDate) throws Exception {
        CreateAccount req = new CreateAccount(userId, type.name(), currency.name(), depositEndDate);

        MvcResult res = mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.ownerId", is(userId.toString())))
                .andExpect(jsonPath("$.type", is(type.name())))
                .andExpect(jsonPath("$.currency", is(currency.name())))
                .andReturn();

        JsonNode body = om.readTree(res.getResponse().getContentAsString());
        return UUID.fromString(body.get("id").asText());
    }

    private void deposit(UUID accountId, BigDecimal amount) throws Exception {
        mvc.perform(post("/api/accounts/{id}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Amount(amount.toPlainString()))))
                .andExpect(status().isOk());
    }

    private void withdraw(UUID accountId, BigDecimal amount) throws Exception {
        mvc.perform(post("/api/accounts/{id}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Amount(amount.toPlainString()))))
                .andExpect(status().isOk());
    }

    private UUID transfer(UUID from, UUID to, BigDecimal amount) throws Exception {
        MvcResult res = mvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new Transfer(from, to, amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        JsonNode body = om.readTree(res.getResponse().getContentAsString());
        return UUID.fromString(body.get("id").asText());
    }

    private JsonNode getAccount(UUID id) throws Exception {
        MvcResult res = mvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isOk())
                .andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    private JsonNode getTransaction(UUID id) throws Exception {
        MvcResult res = mvc.perform(get("/api/transactions/{id}", id))
                .andExpect(status().isOk())
                .andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    /**
     * JSON number can come as 100 or 100.0 depending on BigDecimal scale.
     * Compare via BigDecimal compareTo to ignore scale differences.
     */
    private static void assertBdEquals(String expected, JsonNode node) {
        BigDecimal exp = new BigDecimal(expected);
        BigDecimal act = node.isNumber()
                ? node.decimalValue()
                : new BigDecimal(node.asText());

        if (exp.compareTo(act) != 0) {
            throw new AssertionError("Expected " + exp.toPlainString() + " but was " + act.toPlainString());
        }
    }

    // simple DTOs for requests (so test does not depend on your request classes visibility)
    private record CreateUser(String name) {}
    private record CreateAccount(UUID userId, String type, String currency, LocalDate depositEndDate) {}
    private record Amount(String amount) {}
    private record Transfer(UUID from, UUID to, BigDecimal amount) {}
}
