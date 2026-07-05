package com.portfolio.event;

import com.portfolio.entity.Transaction;
import java.util.List;

public record PortfolioChangedEvent(Integer userId, List<Transaction> transactionsToCreate) {
    public PortfolioChangedEvent(Integer userId) {
        this(userId, List.of());
    }
    
    public PortfolioChangedEvent(Integer userId, Transaction singleTransaction) {
        this(userId, List.of(singleTransaction));
    }
}
