package com.portfolio.dto.request;

import com.portfolio.entity.AccountType;
import com.portfolio.entity.Currency;
import java.math.BigDecimal;

public record AccountRequestDto(
    String name,
    AccountType type,
    BigDecimal balance,
    Currency currency,
    Integer familyMemberId
) {}
