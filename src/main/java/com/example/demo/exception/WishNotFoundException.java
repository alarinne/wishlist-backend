package com.example.demo.exception;

public class WishNotFoundException extends RuntimeException{
    public WishNotFoundException(Long id) {
        super("Wish with id " + id + " was not found");
    }
}
