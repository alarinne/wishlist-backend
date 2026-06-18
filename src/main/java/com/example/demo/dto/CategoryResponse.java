package com.example.demo.dto;

public record CategoryResponse(
        Long id,
        String name,
        String code,
        String label
) {
}
