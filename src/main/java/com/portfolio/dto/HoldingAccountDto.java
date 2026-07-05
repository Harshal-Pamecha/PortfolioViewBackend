package com.portfolio.dto;

import com.portfolio.entity.Account;

public record HoldingAccountDto(
    Integer id,
    String name,
    HoldingFamilyMemberDto familyMember
) {
    public static HoldingAccountDto from(Account account) {
        if (account == null) return null;
        return new HoldingAccountDto(
            account.getId(),
            account.getName(),
            HoldingFamilyMemberDto.from(account.getFamilyMember())
        );
    }
}
