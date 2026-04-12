package com.myfin.expensetracker.budget;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryBudgetDto {
    private Long categoryId;

    @Positive
    private BigDecimal limitAmount;
}
