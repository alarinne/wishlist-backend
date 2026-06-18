package com.example.demo.dto;

import com.example.demo.entity.Priority;

public record WishResponse(
        Long id,
        String wishName,
        Double wishPrice,
        String url,
        String status,
        Long categoryId,
        String categoryName,
        Priority priority
) {
}
