package com.portfolio.dto.request;

import com.portfolio.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequestDto(
    BigDecimal amount,
    TransactionType type,
    LocalDateTime date,
    String notes,
    Integer sourceAccountId,
    Integer destAccountId,
    Integer holdingId
) {}
