package com.myfin.expensetracker.budget;

import lombok.Data;

import java.util.List;

@Data
public class CategoryUsageSummaryResponseDto {

    private List<CategoryUsageResponseDto> categoryUsageResponseDtoList;
}
