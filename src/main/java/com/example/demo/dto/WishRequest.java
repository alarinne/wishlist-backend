package com.example.demo.dto;

import com.example.demo.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WishRequest(
        @NotBlank(message = "Wish name is required")
        String wishName,

        @NotNull(message = "Wish price is required")
        @PositiveOrZero(message = "Wish price must be zero or positive")
        Double wishPrice,

        String url,

        @NotNull(message = "Category id is required")
        Long categoryId,

        @NotNull(message = "Priority is required")
        Priority priority
) {
}
