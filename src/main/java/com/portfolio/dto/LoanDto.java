package com.portfolio.dto;

import com.portfolio.entity.Loan;
import com.portfolio.entity.Currency;
import java.math.BigDecimal;

public record LoanDto(
    Integer id,
    String loanName,
    String type,
    String provider,
    BigDecimal principal,
    BigDecimal outstanding,
    BigDecimal interestRate,
    BigDecimal emi,
    Currency currency
) {
    public static LoanDto from(Loan loan) {
        if (loan == null) return null;
        return new LoanDto(
            loan.getId(),
            loan.getName(),
            loan.getType(),
            loan.getProvider(),
            loan.getPrincipleAmount(),
            loan.getOutstanding(),
            loan.getInterest(),
            loan.getEmi(),
            loan.getCurrency()
        );
    }
}
