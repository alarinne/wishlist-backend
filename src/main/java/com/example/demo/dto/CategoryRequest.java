package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank
        String name,

        @NotBlank
        String code,

        @NotBlank
        String label
) {
}
