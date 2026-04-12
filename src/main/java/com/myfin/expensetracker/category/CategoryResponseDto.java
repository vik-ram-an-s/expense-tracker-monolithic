package com.myfin.expensetracker.category;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponseDto {

    private Long id;

    private String name;

    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDateTime;
}