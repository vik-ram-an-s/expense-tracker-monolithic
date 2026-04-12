package com.myfin.expensetracker.budget;


import com.myfin.expensetracker.category.CategoryResponseDto;

import lombok.Data;


import java.math.BigDecimal;


@Data
public class CategoryBudgetResponseDto {
    private Long id;

    private CategoryResponse categoryResponse;

    private BigDecimal limitAmount;

}
