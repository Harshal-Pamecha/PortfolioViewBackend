package com.portfolio.dto.request;

import com.portfolio.entity.AccountType;
import java.math.BigDecimal;

public record AccountRequestDto(
    String name,
    AccountType type,
    BigDecimal balance,
    Integer familyMemberId
) {}
