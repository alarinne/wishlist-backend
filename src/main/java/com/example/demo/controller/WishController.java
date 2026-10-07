package com.example.demo.controller;

import com.example.demo.api.WishApi;
import com.example.demo.dto.WishRequest;
import com.example.demo.dto.WishResponse;
import com.example.demo.dto.WishStatusUpdateRequest;
import com.example.demo.facade.WishFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class WishController implements WishApi {

    private final WishFacade wishFacade;

    public WishController(WishFacade wishFacade) {
        this.wishFacade = wishFacade;
    }

    @Override
    public ResponseEntity<WishResponse> createWish(WishRequest request) {
        WishResponse createdWish = wishFacade.createWish(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdWish);
    }

    @Override
    public List<WishResponse> getAllWishes() {
        return wishFacade.getAllWishes();
    }

    @Override
    public WishResponse getWishById(Long id) {
        return wishFacade.getWishById(id);
    }

    @Override
    public WishResponse updateWish(Long id, WishRequest request) {
        return wishFacade.updateWish(id, request);
    }

    @Override
    public WishResponse updateWishStatus(Long id, WishStatusUpdateRequest request) {
        return wishFacade.updateWishStatus(id, request);
    }

    @Override
    public ResponseEntity<Void> deleteWish(Long id) {
        wishFacade.deleteWish(id);
        return ResponseEntity.noContent().build();
    }
}
