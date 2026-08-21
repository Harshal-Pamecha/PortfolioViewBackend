package com.portfolio.dto;

import com.portfolio.entity.Account;
import com.portfolio.entity.AccountType;
import com.portfolio.entity.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDto(
    Integer id,
    String name,
    AccountType type,
    BigDecimal balance,
    Currency currency,
    AccountFamilyMemberDto familyMember,
    LocalDateTime createdAt
) {
    public static AccountDto from(Account account) {
        if (account == null) return null;
        return new AccountDto(
            account.getId(),
            account.getName(),
            account.getType(),
            account.getBalance(),
            account.getCurrency(),
            AccountFamilyMemberDto.from(account.getFamilyMember()),
            account.getCreatedAt()
        );
    }
}
