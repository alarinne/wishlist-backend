package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,

        @NotBlank(message = "Category code is required")
        String code,

        @NotBlank(message = "Category label is required")
        String label
) {
}
