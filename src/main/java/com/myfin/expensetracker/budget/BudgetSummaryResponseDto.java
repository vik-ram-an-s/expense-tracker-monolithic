package com.myfin.expensetracker.budget;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetSummaryResponseDto {
    private BigDecimal totalBudget;
    private BigDecimal totalExpense;
    private BigDecimal remainingBudget;

    private BigDecimal overspent;
    private BigDecimal usedPercentage;
    private BudgetStatus status;



}
