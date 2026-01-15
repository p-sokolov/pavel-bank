package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.BlockReason;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account create(CreateAccountCommand cmd) {
        User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Account account = new Account(
                user,
                cmd.type(),
                cmd.currency(),
                cmd.depositEndDate()
        );

        user.addAccount(account);

        return accountRepository.save(account);
    }

    @Transactional
    public void block(UUID accountId, BlockReason reason) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.block(reason);
    }

    @Transactional
    public void unblock(UUID accountId) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.unblock();
    }
}
