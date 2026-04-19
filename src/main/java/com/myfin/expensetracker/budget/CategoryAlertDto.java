package com.myfin.expensetracker.budget;

import lombok.Data;

@Data
public class CategoryAlertDto {
    private BudgetStatus status;

    private String categoryName;

    private String message;
}
