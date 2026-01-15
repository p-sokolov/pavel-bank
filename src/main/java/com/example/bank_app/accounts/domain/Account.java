package com.example.bank_app.accounts.domain;

import com.example.bank_app.users.domain.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal balance;

    /**
     * Debt amount in the same currency as the account. Used when the account is blocked with reason DEBT.
     */
    @Column(nullable = false)
    private BigDecimal debtAmount;

    @Column(nullable = false)
    private boolean blocked;

    @Enumerated(EnumType.STRING)
    private BlockReason blockReason;

    // только для вкладов
    private LocalDate depositEndDate;

    protected Account() {
    }

    public Account(User owner, AccountType type, Currency currency, LocalDate depositEndDate) {
        if (owner == null) throw new IllegalArgumentException("Owner required");
        if (type == null) throw new IllegalArgumentException("Type required");
        if (currency == null) throw new IllegalArgumentException("Currency required");

        if (type == AccountType.DEPOSIT) {
            if (depositEndDate == null)
                throw new IllegalArgumentException("Deposit end date required for DEPOSIT");
        } else {
            if (depositEndDate != null)
                throw new IllegalArgumentException("Only DEPOSIT may have end date");
        }

        this.owner = owner;
        this.type = type;
        this.currency = currency;
        this.depositEndDate = depositEndDate;
        this.balance = BigDecimal.ZERO;
        this.debtAmount = BigDecimal.ZERO;
        this.blocked = false;
        this.blockReason = null;
    }

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public AccountType getType() {
        return type;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getDebtAmount() {
        return debtAmount;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public BlockReason getBlockReason() {
        return blockReason;
    }

    public User getOwner() {
        return owner;
    }

    public LocalDate getDepositEndDate() {
        return depositEndDate;
    }

    public void debit(BigDecimal amount) {
        ensureCanDebit();
        validateAmount(amount);

        if (type == AccountType.DEPOSIT && !isDepositExpired()) {
            throw new IllegalStateException("Deposit not expired");
        }

        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        ensureCanCredit();
        validateAmount(amount);
        balance = balance.add(amount);
    }

    public void block(BlockReason reason) {
        if (reason == null)
            throw new IllegalArgumentException("Block reason required");

        if (reason == BlockReason.DEBT) {
            throw new IllegalArgumentException("Use markDebt(amount) to apply DEBT block");
        }

        this.blocked = true;
        this.blockReason = reason;
    }

    /**
     * Marks debt on this account and blocks it with reason DEBT.
     * Incoming money (credit) is still allowed while blocked for DEBT, so the user can repay.
     */
    public void markDebt(BigDecimal amount) {
        validateAmount(amount);
        if (type == AccountType.DEPOSIT) {
            throw new IllegalStateException("Deposits cannot have debt");
        }
        debtAmount = debtAmount.add(amount);
        blocked = true;
        blockReason = BlockReason.DEBT;
    }

    /**
     * Repays debt from the account balance.
     */
    public void repayDebt(BigDecimal amount) {
        validateAmount(amount);
        if (!blocked || blockReason != BlockReason.DEBT) {
            throw new IllegalStateException("Account is not blocked for debt");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        balance = balance.subtract(amount);
        debtAmount = debtAmount.subtract(amount);

        if (debtAmount.signum() < 0) {
            // don't allow overpay: return the extra money back to balance
            balance = balance.add(debtAmount.abs());
            debtAmount = BigDecimal.ZERO;
        }

        if (debtAmount.signum() == 0) {
            blocked = false;
            blockReason = null;
        }
    }

    public void unblock() {
        if (!blocked) {
            return;
        }

        if (blockReason == BlockReason.DEBT && debtAmount.signum() > 0) {
            throw new IllegalStateException("Account has outstanding debt");
        }

        this.blocked = false;
        this.blockReason = null;
    }

    private void ensureCanDebit() {
        if (blocked) {
            throw new IllegalStateException("Account blocked");
        }
    }

    private void ensureCanCredit() {
        // allow incoming money for debt repayment
        if (blocked && blockReason != BlockReason.DEBT) {
            throw new IllegalStateException("Account blocked");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Amount must be positive");
    }

    private boolean isDepositExpired() {
        return type == AccountType.DEPOSIT &&
               depositEndDate != null &&
               !LocalDate.now().isBefore(depositEndDate);
    }
}
