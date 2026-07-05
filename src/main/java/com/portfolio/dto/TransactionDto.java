package com.portfolio.dto;

import com.portfolio.entity.Transaction;
import com.portfolio.entity.TransactionType;
import com.portfolio.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(
    Integer id,
    BigDecimal amount,
    Currency currency,
    String category,
    TransactionType type,
    String notes,
    LocalDateTime date,
    TransactionAccountDto sourceAccount,
    TransactionAccountDto destAccount,
    String holdingName,
    String holdingCountryCode
) {
    public static TransactionDto from(Transaction transaction) {
        if (transaction == null) return null;
        
        String cat = transaction.getHolding() != null ? transaction.getHolding().getCategory().name() : null;
        String hName = transaction.getHolding() != null ? transaction.getHolding().getName() : null;
        String hCountryCode = transaction.getHolding() != null ? transaction.getHolding().getCountryCode() : null;
        
        return new TransactionDto(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getCurrency(),
            cat,
            transaction.getType(),
            transaction.getNotes(),
            transaction.getDate(),
            TransactionAccountDto.from(transaction.getSourceAccount()),
            TransactionAccountDto.from(transaction.getDestAccount()),
            hName,
            hCountryCode
        );
    }
}
