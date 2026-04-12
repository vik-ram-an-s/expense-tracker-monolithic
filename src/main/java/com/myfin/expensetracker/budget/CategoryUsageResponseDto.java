package com.myfin.expensetracker.budget;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryUsageResponseDto {

    private String categoryName;

    private BigDecimal limit;

    private BigDecimal spent;

    private BigDecimal remaining;

    private BigDecimal overspent;

    private BigDecimal usedPercentage;

    private BudgetStatus status;
}
