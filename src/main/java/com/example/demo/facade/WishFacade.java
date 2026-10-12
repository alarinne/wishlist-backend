package com.example.demo.facade;

import com.example.demo.dto.WishRequest;
import com.example.demo.dto.WishResponse;
import com.example.demo.dto.WishStatusUpdateRequest;
import com.example.demo.service.WishService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WishFacade {

    private final WishService wishService;

    public WishFacade(WishService wishService) {
        this.wishService = wishService;
    }

    public WishResponse createWish(WishRequest request) {
        return wishService.createWish(request);
    }

    public List<WishResponse> getAllWishes() {
        return wishService.getAllWishes();
    }

    public WishResponse getWishById(Long id) {
        return wishService.getWishById(id);
    }

    public WishResponse updateWish(Long id, WishRequest request) {
        return wishService.updateWish(id, request);
    }

    public WishResponse updateWishStatus(Long id, WishStatusUpdateRequest request) {
        return wishService.updateWishStatus(id, request.status());
    }

    public void deleteWish(Long id) {
        wishService.deleteWish(id);
    }
}
