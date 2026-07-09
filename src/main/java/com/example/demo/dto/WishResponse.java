package com.example.demo.dto;

import com.example.demo.entity.Priority;
import com.example.demo.entity.WishStatus;

public record WishResponse(
        Long id,
        String wishName,
        Double wishPrice,
        String url,
        WishStatus status,
        Long categoryId,
        String categoryName,
        Priority priority
) {
}
