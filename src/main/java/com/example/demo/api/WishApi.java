package com.example.demo.api;

import com.example.demo.dto.WishRequest;
import com.example.demo.dto.WishResponse;
import com.example.demo.dto.WishStatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/wishes")
public interface WishApi {

    @PostMapping
    ResponseEntity<WishResponse> createWish(@Valid @RequestBody WishRequest request);

    @GetMapping
    List<WishResponse> getAllWishes();

    @GetMapping("/{id}")
    WishResponse getWishById(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    WishResponse updateWish(@PathVariable("id") Long id, @Valid @RequestBody WishRequest request);

    @PatchMapping("/{id}/status")
    WishResponse updateWishStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody WishStatusUpdateRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteWish(@PathVariable("id") Long id);
}
