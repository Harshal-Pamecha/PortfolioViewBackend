package com.portfolio.dto;

import com.portfolio.entity.FamilyMember;
import com.portfolio.entity.Currency;
import java.math.BigDecimal;

public record FamilyMemberDto(
    Integer id,
    String name,
    BigDecimal balance,
    Boolean self,
    String type,
    Currency currency
) {
    public static FamilyMemberDto from(FamilyMember familyMember) {
        if (familyMember == null) return null;
        boolean selfVal = familyMember.getName() != null && familyMember.getName().equalsIgnoreCase("self");
        return new FamilyMemberDto(
            familyMember.getId(),
            familyMember.getName(),
            familyMember.getBalance(),
            selfVal,
            selfVal ? "SELF" : "FAMILY",
            familyMember.getCurrency()
        );
    }
}
