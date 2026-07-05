package com.portfolio.dto;

import com.portfolio.entity.FamilyMember;
import com.portfolio.entity.Currency;

public record AccountFamilyMemberDto(
    Integer id,
    String name,
    Currency currency
) {
    public static AccountFamilyMemberDto from(FamilyMember familyMember) {
        if (familyMember == null) return null;
        return new AccountFamilyMemberDto(
            familyMember.getId(),
            familyMember.getName(),
            familyMember.getCurrency()
        );
    }
}
