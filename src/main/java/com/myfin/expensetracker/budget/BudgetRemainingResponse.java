package com.myfin.expensetracker.budget;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRemainingResponse {
    private BigDecimal remainingBudget;
}
