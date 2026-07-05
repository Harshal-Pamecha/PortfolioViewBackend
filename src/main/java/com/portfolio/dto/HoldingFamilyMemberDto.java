package com.portfolio.dto;

import com.portfolio.entity.FamilyMember;
import com.portfolio.entity.Currency;

public record HoldingFamilyMemberDto(
    Integer id,
    String name,
    Currency currency
) {
    public static HoldingFamilyMemberDto from(FamilyMember familyMember) {
        if (familyMember == null) return null;
        return new HoldingFamilyMemberDto(
            familyMember.getId(),
            familyMember.getName(),
            familyMember.getCurrency()
        );
    }
}
