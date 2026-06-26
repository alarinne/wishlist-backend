package com.example.demo.dto;

import com.example.demo.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WishRequest(
        @NotBlank
        String wishName,

        @NotNull
        @PositiveOrZero
        Double wishPrice,

        String url,

        @NotNull
        Long categoryId,

        @NotNull
        Priority priority
) {
}
