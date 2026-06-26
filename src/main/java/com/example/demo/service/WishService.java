package com.example.demo.service;

import com.example.demo.dto.WishRequest;
import com.example.demo.dto.WishResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.Wish;
import com.example.demo.exception.CategoryNotFoundException;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.WishRepository;
import org.springframework.stereotype.Service;
import com.example.demo.exception.WishNotFoundException;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class WishService {

    private final WishRepository wishRepository;
    private final CategoryRepository categoryRepository;

    public WishService(WishRepository wishRepository, CategoryRepository categoryRepository) {
        this.wishRepository = wishRepository;
        this.categoryRepository = categoryRepository;
    }

    public WishResponse createWish(WishRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Wish wish = new Wish();
        wish.setWishName(request.wishName());
        wish.setWishPrice(request.wishPrice());
        wish.setUrl(request.url());
        wish.setCategory(category);
        wish.setPriority(request.priority());
        wish.setStatus("ACTIVE");

        Wish savedWish = wishRepository.save(wish);

        return toResponse(savedWish);
    }

    public List<WishResponse> getAllWishes() {
        return wishRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WishResponse getWishById(Long id) {
        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new WishNotFoundException(id));

        return toResponse(wish);
    }

    public WishResponse updateWish(Long id, WishRequest request) {
        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new WishNotFoundException(id));

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        wish.setWishName(request.wishName());
        wish.setWishPrice(request.wishPrice());
        wish.setUrl(request.url());
        wish.setCategory(category);
        wish.setPriority(request.priority());

        Wish updatedWish = wishRepository.save(wish);

        return toResponse(updatedWish);
    }

    private WishResponse toResponse(Wish wish) {
        return new WishResponse(
                wish.getWishId(),
                wish.getWishName(),
                wish.getWishPrice(),
                wish.getUrl(),
                wish.getStatus(),
                wish.getCategory().getId(),
                wish.getCategory().getName(),
                wish.getPriority()
        );
    }

    public void deleteWish(Long id) {
        if (!wishRepository.existsById(id)) {
            throw new WishNotFoundException(id);
        }

        wishRepository.deleteById(id);
    }
}
