package com.example.demo;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void saveCategory_whenCodeAlreadyExists_throwsDataIntegrityViolationException() {
        Category firstCategory = new Category();
        firstCategory.setName("Books");
        firstCategory.setCode("duplicate-code-db-test");
        firstCategory.setLabel("Books");

        categoryRepository.saveAndFlush(firstCategory);

        Category secondCategory = new Category();
        secondCategory.setName("Other Books");
        secondCategory.setCode("duplicate-code-db-test");
        secondCategory.setLabel("Other Books");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> categoryRepository.saveAndFlush(secondCategory)
        );
    }
}