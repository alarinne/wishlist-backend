package com.example.demo.controller;

import com.example.demo.dto.WishRequest;
import com.example.demo.dto.WishResponse;
import com.example.demo.service.WishService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @PostMapping
    public WishResponse createWish(@RequestBody WishRequest request) {
        return wishService.createWish(request);
    }

    @GetMapping
    public List<WishResponse> getAllWishes() {
        return wishService.getAllWishes();
    }

    @GetMapping("/{id}")
    public WishResponse getWishById(@PathVariable Long id) {
        return wishService.getWishById(id);
    }

    @PutMapping("/{id}")
    public WishResponse updateWish(
            @PathVariable Long id,
            @RequestBody WishRequest request
    ) {
        return wishService.updateWish(id, request);
    }
}
