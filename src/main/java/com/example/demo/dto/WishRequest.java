package com.example.demo.dto;

import com.example.demo.entity.Priority;

public record WishRequest(
        String wishName,
        Double wishPrice,
        String url,
        Long categoryId,
        Priority priority
) {
}
