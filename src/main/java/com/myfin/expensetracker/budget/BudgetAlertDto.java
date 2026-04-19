package com.myfin.expensetracker.budget;

import lombok.Data;

import java.util.List;

@Data
public class BudgetAlertDto {
    private BudgetStatus overAllStatus;

    private String message;

    private List<CategoryAlertDto> categoryAlertDtoList;


}
