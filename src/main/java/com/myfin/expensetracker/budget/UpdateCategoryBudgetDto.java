package com.myfin.expensetracker.budget;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UpdateCategoryBudgetDto {

    @Positive
    private BigDecimal limitAmount;
}
