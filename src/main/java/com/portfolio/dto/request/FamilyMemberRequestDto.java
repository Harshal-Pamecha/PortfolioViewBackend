package com.portfolio.dto.request;

import com.portfolio.entity.Currency;
import java.math.BigDecimal;

public record FamilyMemberRequestDto(
    String name,
    BigDecimal balance,
    Currency currency
) {}
