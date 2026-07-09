package com.example.demo;


import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WishControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WishRepository wishRepository;

    @BeforeEach
    void cleanDatabase() {
        wishRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category createCategory() {
        Category category = new Category();
        category.setName("Books");
        category.setCode("books-test");
        category.setLabel("Books");

        return categoryRepository.save(category);
    }

    @Test
    void createWish_whenRequestIsValid_returnsCreated() throws Exception {
        Category category = createCategory();

        String requestBody = """
            {
              "wishName": "Kindle",
              "wishPrice": 120.0,
              "url": "https://example.com/kindle",
              "categoryId": %d,
              "priority": "HIGH"
            }
            """.formatted(category.getId());

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createWish_whenWishNameIsBlank_returnsBadRequest() throws Exception {
        Category category = createCategory();

        String requestBody = """
            {
              "wishName": "",
              "wishPrice": 120.0,
              "url": "https://example.com/kindle",
              "categoryId": %d,
              "priority": "HIGH"
            }
            """.formatted(category.getId());

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWish_whenPriceIsNegative_returnsBadRequest() throws Exception {
        Category category = createCategory();

        String requestBody = """
            {
              "wishName": "Kindle",
              "wishPrice": -1,
              "url": "https://example.com/kindle",
              "categoryId": %d,
              "priority": "HIGH"
            }
            """.formatted(category.getId());

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWish_whenCategoryIdIsMissing_returnsBadRequest() throws Exception {
        String requestBody = """
            {
              "wishName": "Kindle",
              "wishPrice": 120.0,
              "url": "https://example.com/kindle",
              "categoryId": null,
              "priority": "HIGH"
            }
            """;

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWish_whenPriorityIsMissing_returnsBadRequest() throws Exception {
        Category category = createCategory();

        String requestBody = """
            {
              "wishName": "Kindle",
              "wishPrice": 120.0,
              "url": "https://example.com/kindle",
              "categoryId": %d,
              "priority": null
            }
            """.formatted(category.getId());

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWish_whenCategoryDoesNotExist_returnsNotFound() throws Exception {
        String requestBody = """
            {
              "wishName": "Kindle",
              "wishPrice": 120.0,
              "url": "https://example.com/kindle",
              "categoryId": 999999,
              "priority": "HIGH"
            }
            """;

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }
}
