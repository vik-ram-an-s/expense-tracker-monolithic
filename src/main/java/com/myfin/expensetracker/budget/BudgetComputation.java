package com.myfin.expensetracker.budget;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetComputation {
    private BigDecimal spent;
    private BigDecimal limit;
    private BigDecimal remaining;
    private BigDecimal overspent;
    private BigDecimal usedPercentage;
    private BudgetStatus status;
}
