package com.example.bank_app;

import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.BlockReason;
import com.example.bank_app.accounts.domain.Currency;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper om;

        private UUID createUser(String name) throws Exception {
                String body = om.writeValueAsString(java.util.Map.of("name", name));
                String json = mvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andReturn().getResponse().getContentAsString();
                return UUID.fromString(om.readTree(json).get("id").asText());
        }

        private UUID createAccount(UUID userId, AccountType type, Currency currency, String endDateOrNull)
                        throws Exception {
                java.util.Map<String, Object> req = new java.util.HashMap<>();
                req.put("userId", userId.toString());
                req.put("type", type.name());
                req.put("currency", currency.name());
                if (endDateOrNull != null)
                        req.put("depositEndDate", endDateOrNull);

                String json = mvc.perform(post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andReturn().getResponse().getContentAsString();
                return UUID.fromString(om.readTree(json).get("id").asText());
        }

        @Test
        void health_isOk() throws Exception {
                mvc.perform(get("/health"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("OK"));
        }

        @Test
        void endToEnd_apiFlow_createUsersAccounts_deposit_transfer_debt_and_errorMapping() throws Exception {
                UUID u1 = createUser("Alice");
                UUID u2 = createUser("Bob");

                UUID a1 = createAccount(u1, AccountType.CARD, Currency.RUB, null);
                UUID a2 = createAccount(u2, AccountType.CARD, Currency.RUB, null);

                // deposit 100 into a1
                mvc.perform(post("/api/accounts/{id}/deposit", a1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of("amount", 100))))
                                .andExpect(status().isOk());

                mvc.perform(get("/api/accounts/{id}", a1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance", is(100.0)));

                // transfer 10 from a1 to a2
                String txJson = mvc.perform(post("/api/transactions/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of(
                                                "from", a1.toString(),
                                                "to", a2.toString(),
                                                "amount", 10.0))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.currency", is("RUB")))
                                .andReturn().getResponse().getContentAsString();

                UUID txId = UUID.fromString(om.readTree(txJson).get("id").asText());

                mvc.perform(get("/api/transactions/{id}", txId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.fromAccountId", is(a1.toString())))
                                .andExpect(jsonPath("$.toAccountId", is(a2.toString())))
                                .andExpect(jsonPath("$.amount", is(10.0)));

                mvc.perform(get("/api/transactions")
                                .param("accountId", a1.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

                mvc.perform(get("/api/accounts/{id}", a1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance", is(90.0)));
                mvc.perform(get("/api/accounts/{id}", a2))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance", is(10.0)));

                // debt flow: mark debt 50 on a1
                mvc.perform(post("/api/accounts/{id}/debt", a1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of("amount", 50))))
                                .andExpect(status().isOk());

                mvc.perform(get("/api/accounts/{id}", a1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.blocked", is(true)))
                                .andExpect(jsonPath("$.blockReason", is("DEBT")))
                                .andExpect(jsonPath("$.debtAmount", is(50.0)));

                // while DEBT-blocked: incoming money allowed
                mvc.perform(post("/api/accounts/{id}/deposit", a1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of("amount", 20))))
                                .andExpect(status().isOk());

                // but withdraw is forbidden -> 409 CONFLICT
                mvc.perform(post("/api/accounts/{id}/withdraw", a1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of("amount", 1))))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code", is("CONFLICT")));

                // repay debt fully
                mvc.perform(post("/api/accounts/{id}/repay-debt", a1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of("amount", 50))))
                                .andExpect(status().isOk());

                mvc.perform(get("/api/accounts/{id}", a1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.blocked", is(false)))
                                .andExpect(jsonPath("$.blockReason", is(nullValue())))
                                .andExpect(jsonPath("$.debtAmount", is(0.0)));

                // explicit block/unblock
                mvc.perform(post("/api/accounts/{id}/block", a2)
                                .param("reason", BlockReason.FRAUD.name()))
                                .andExpect(status().isOk());

                mvc.perform(get("/api/accounts/{id}", a2))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.blocked", is(true)))
                                .andExpect(jsonPath("$.blockReason", is("FRAUD")));

                mvc.perform(post("/api/accounts/{id}/unblock", a2))
                                .andExpect(status().isOk());

                // error mapping examples
                mvc.perform(get("/api/accounts/{id}", UUID.randomUUID()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code", is("NOT_FOUND")));

                mvc.perform(post("/api/accounts/{id}/deposit", a1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of("amount", -1))))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));

                mvc.perform(post("/api/transactions/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(java.util.Map.of(
                                                "from", a1.toString(),
                                                "to", a1.toString(),
                                                "amount", 1))))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
        }
}
