package com.myfin.expensetracker.expense;


import com.myfin.expensetracker.category.Category;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseRequestDto {

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount cannot be negative")
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String description;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;
}
