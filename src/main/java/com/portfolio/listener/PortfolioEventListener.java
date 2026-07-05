package com.portfolio.listener;

import com.portfolio.event.PortfolioChangedEvent;
import com.portfolio.repository.PortfolioSnapshotRepository;
import com.portfolio.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PortfolioEventListener {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final TransactionService transactionService;

    @Async
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPortfolioChanged(PortfolioChangedEvent event) {
        if (event.transactionsToCreate() != null) {
            for (var tx : event.transactionsToCreate()) {
                transactionService.create(tx);
            }
        }
        snapshotRepository.deleteByUserId(event.userId());
    }
}
