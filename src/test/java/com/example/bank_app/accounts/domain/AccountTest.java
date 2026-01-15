package com.example.bank_app.accounts.domain;

import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static User user() {
        return new User("U");
    }

    @Test
    void ctor_validatesRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new Account(null, AccountType.CARD, Currency.RUB, null));
        assertThrows(IllegalArgumentException.class, () -> new Account(user(), null, Currency.RUB, null));
        assertThrows(IllegalArgumentException.class, () -> new Account(user(), AccountType.CARD, null, null));
    }

    @Test
    void ctor_depositRequiresEndDate_andOthersMustNotHaveIt() {
        assertThrows(IllegalArgumentException.class,
                () -> new Account(user(), AccountType.DEPOSIT, Currency.RUB, null));

        assertThrows(IllegalArgumentException.class,
                () -> new Account(user(), AccountType.CARD, Currency.RUB, LocalDate.now().plusDays(1)));
    }

    @Test
    void credit_increasesBalance() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        a.credit(bd("10.00"));
        a.credit(bd("2.50"));
        assertEquals(bd("12.50"), a.getBalance());
    }

    @Test
    void debit_decreasesBalance() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        a.credit(bd("10"));
        a.debit(bd("3"));
        assertEquals(bd("7"), a.getBalance());
    }

    @Test
    void debit_validatesAmountAndFunds() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        assertThrows(IllegalArgumentException.class, () -> a.debit(null));
        assertThrows(IllegalArgumentException.class, () -> a.debit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> a.debit(bd("-1")));

        a.credit(bd("5"));
        assertThrows(IllegalStateException.class, () -> a.debit(bd("6")));
    }

    @Test
    void credit_validatesAmount() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        assertThrows(IllegalArgumentException.class, () -> a.credit(null));
        assertThrows(IllegalArgumentException.class, () -> a.credit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> a.credit(bd("-0.01")));
    }

    @Test
    void depositAccount_cannotDebitBeforeExpiry_butCanAfter() {
        Account depNotExpired = new Account(user(), AccountType.DEPOSIT, Currency.RUB, LocalDate.now().plusDays(10));
        depNotExpired.credit(bd("100"));
        assertThrows(IllegalStateException.class, () -> depNotExpired.debit(bd("1")), "Deposit not expired");

        Account depExpired = new Account(user(), AccountType.DEPOSIT, Currency.RUB, LocalDate.now().minusDays(1));
        depExpired.credit(bd("100"));
        depExpired.debit(bd("1"));
        assertEquals(bd("99"), depExpired.getBalance());
    }

    @Test
    void block_requiresNonNullReason_andDebtRequiresMarkDebt() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        assertThrows(IllegalArgumentException.class, () -> a.block(null));
        assertThrows(IllegalArgumentException.class, () -> a.block(BlockReason.DEBT));
    }

    @Test
    void blockedAccount_disallowsDebit_andDisallowsCreditUnlessDebt() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        a.credit(bd("10"));
        a.block(BlockReason.FRAUD);

        assertTrue(a.isBlocked());
        assertEquals(BlockReason.FRAUD, a.getBlockReason());

        assertThrows(IllegalStateException.class, () -> a.debit(bd("1")));
        assertThrows(IllegalStateException.class, () -> a.credit(bd("1")));
    }

    @Test
    void markDebt_blocksWithDebtReason_andAllowsIncomingMoney() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        a.markDebt(bd("25"));
        assertTrue(a.isBlocked());
        assertEquals(BlockReason.DEBT, a.getBlockReason());
        assertEquals(bd("25"), a.getDebtAmount());

        // credit is allowed when blocked for debt
        a.credit(bd("10"));
        assertEquals(bd("10"), a.getBalance());
        // debit is still forbidden while blocked (even for DEBT)
        assertThrows(IllegalStateException.class, () -> a.debit(bd("1")));
    }

    @Test
    void markDebt_disallowedForDeposits() {
        Account dep = new Account(user(), AccountType.DEPOSIT, Currency.RUB, LocalDate.now().plusDays(1));
        assertThrows(IllegalStateException.class, () -> dep.markDebt(bd("1")));
    }

    @Test
    void repayDebt_requiresDebtBlock_andEnoughFunds_andUnblocksWhenPaid() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        a.credit(bd("100"));

        assertThrows(IllegalStateException.class, () -> a.repayDebt(bd("1")));

        a.markDebt(bd("30"));
        assertThrows(IllegalStateException.class, () -> a.repayDebt(bd("200")));

        a.repayDebt(bd("10"));
        assertEquals(bd("90"), a.getBalance());
        assertEquals(bd("20"), a.getDebtAmount());
        assertTrue(a.isBlocked());

        a.repayDebt(bd("25")); // overpay by 5 => should return extra back
        assertEquals(bd("70"), a.getBalance()); // 90 - 20 (debt) = 70
        assertEquals(BigDecimal.ZERO, a.getDebtAmount());
        assertFalse(a.isBlocked());
        assertNull(a.getBlockReason());
    }

    @Test
    void unblock_noopWhenNotBlocked_andFailsWhenDebtOutstanding() {
        Account a = new Account(user(), AccountType.CARD, Currency.RUB, null);
        a.unblock(); // no exception

        a.markDebt(bd("1"));
        assertThrows(IllegalStateException.class, a::unblock);
    }
}
