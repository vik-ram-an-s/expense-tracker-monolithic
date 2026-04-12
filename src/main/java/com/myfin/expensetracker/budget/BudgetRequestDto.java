package com.myfin.expensetracker.budget;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BudgetRequestDto {

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotNull
    private Integer year;

    private List<CategoryBudgetDto> categoryBudgets;

    @NotNull
    @Positive
    private BigDecimal totalBudget;
}
