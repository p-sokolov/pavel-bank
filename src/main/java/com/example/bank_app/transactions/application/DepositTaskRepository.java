package com.example.bank_app.transactions.application;

import com.example.bank_app.transactions.domain.DepositTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DepositTaskRepository extends JpaRepository<DepositTask, UUID> {
    
    List<DepositTask> findTop1000ByStatusOrderByCreatedAtAsc(DepositTask.TaskStatus status);
    
}