package com.myfin.expensetracker.expense;

import com.myfin.expensetracker.category.CategoryResponseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseResponseDto {

    private Long id;

    private BigDecimal amount;

    private CategoryResponseDto category;

    private PaymentMethod paymentMethod;

    private String description;

    private LocalDateTime expenseDate;

    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDateTime;
}