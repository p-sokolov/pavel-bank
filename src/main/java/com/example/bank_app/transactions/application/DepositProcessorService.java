package com.example.bank_app.transactions.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.application.AccountRepository;
import com.example.bank_app.transactions.domain.DepositTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DepositProcessorService {

    private final DepositTaskRepository depositTaskRepository;
    private final AccountRepository accountRepository;

    public DepositProcessorService(DepositTaskRepository depositTaskRepository,
                                   AccountRepository accountRepository) {
        this.depositTaskRepository = depositTaskRepository;
        this.accountRepository = accountRepository;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void processPendingDeposits() {
        List<DepositTask> pendingTasks = depositTaskRepository.findTop1000ByStatusOrderByCreatedAtAsc(DepositTask.TaskStatus.PENDING);

        if (pendingTasks.isEmpty()) {
            return;
        }

        Map<UUID, List<DepositTask>> tasksByAccount = pendingTasks.stream()
                .collect(Collectors.groupingBy(DepositTask::getAccountId));

        for (Map.Entry<UUID, List<DepositTask>> entry : tasksByAccount.entrySet()) {
            UUID accountId = entry.getKey();
            List<DepositTask> tasks = entry.getValue();

            BigDecimal totalAmount = tasks.stream()
                    .map(DepositTask::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Account account = accountRepository.findByIdForUpdate(accountId).orElse(null);

            if (account != null) {
                try {
                    account.credit(totalAmount);
                    tasks.forEach(task -> task.setStatus(DepositTask.TaskStatus.PROCESSED));
                } catch (Exception e) {
                    tasks.forEach(task -> task.setStatus(DepositTask.TaskStatus.FAILED));
                }
            } else {
                tasks.forEach(task -> task.setStatus(DepositTask.TaskStatus.FAILED));
            }

            depositTaskRepository.saveAll(tasks);
        }
    }
}