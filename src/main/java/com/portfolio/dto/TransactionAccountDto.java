package com.portfolio.dto;

import com.portfolio.entity.Account;
import com.portfolio.entity.AccountType;
import com.portfolio.entity.Currency;

public record TransactionAccountDto(
    Integer id,
    String name,
    AccountType type,
    Currency currency,
    TransactionFamilyMemberDto familyMember
) {
    public static TransactionAccountDto from(Account account) {
        if (account == null) return null;
        return new TransactionAccountDto(
            account.getId(),
            account.getName(),
            account.getType(),
            account.getCurrency(),
            TransactionFamilyMemberDto.from(account.getFamilyMember())
        );
    }
}
