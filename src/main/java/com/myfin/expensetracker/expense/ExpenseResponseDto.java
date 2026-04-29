package com.myfin.expensetracker.expense;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseResponseDto {

    private Long id;

    private Long userId;

    private BigDecimal amount;

    private ExpenseCategoryResponse category;

    private PaymentMethod paymentMethod;

    private String description;

    private LocalDateTime expenseDate;

}