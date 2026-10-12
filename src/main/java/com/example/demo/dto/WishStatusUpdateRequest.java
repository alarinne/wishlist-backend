package com.example.demo.dto;

import com.example.demo.entity.WishStatus;
import jakarta.validation.constraints.NotNull;

public record WishStatusUpdateRequest(
        @NotNull(message = "Wish status is required")
        WishStatus status
) {
}
