package com.portfolio.dto;

import com.portfolio.entity.Account;
import com.portfolio.entity.AccountType;

public record TransactionAccountDto(
    Integer id,
    String name,
    AccountType type,
    TransactionFamilyMemberDto familyMember
) {
    public static TransactionAccountDto from(Account account) {
        if (account == null) return null;
        return new TransactionAccountDto(
            account.getId(),
            account.getName(),
            account.getType(),
            TransactionFamilyMemberDto.from(account.getFamilyMember())
        );
    }
}
