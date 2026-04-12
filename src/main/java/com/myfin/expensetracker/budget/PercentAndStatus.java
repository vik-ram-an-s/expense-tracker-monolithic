package com.myfin.expensetracker.budget;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PercentAndStatus {
    private BigDecimal percent;
    private BudgetStatus status;

}
