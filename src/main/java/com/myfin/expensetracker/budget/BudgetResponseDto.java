package com.myfin.expensetracker.budget;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BudgetResponseDto {
    private Long id;


    private Integer month;

    private Integer year;


    private BigDecimal totalBudget;


    private List<CategoryBudgetResponseDto> categoryBudgets;

    private LocalDateTime createdDateTime;


    private LocalDateTime updatedDateTime;
}
