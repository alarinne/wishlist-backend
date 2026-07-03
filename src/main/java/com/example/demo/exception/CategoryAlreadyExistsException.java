package com.example.demo.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
  public CategoryAlreadyExistsException(String code) {
    super("Category with code " + code + " already exists");
  }
}
