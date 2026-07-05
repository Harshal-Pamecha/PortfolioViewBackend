package com.portfolio.dto;

import com.portfolio.entity.FamilyMember;
import com.portfolio.entity.Currency;

public record TransactionFamilyMemberDto(
    String name,
    Currency currency
) {
    public static TransactionFamilyMemberDto from(FamilyMember familyMember) {
        if (familyMember == null) return null;
        return new TransactionFamilyMemberDto(
            familyMember.getName(),
            familyMember.getCurrency()
        );
    }
}
