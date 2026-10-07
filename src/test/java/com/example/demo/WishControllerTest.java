package com.example.demo;


import com.example.demo.entity.Category;
import com.example.demo.entity.Priority;
import com.example.demo.entity.Wish;
import com.example.demo.entity.WishStatus;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @ParameterizedTest
    @CsvSource({
            "ACTIVE, PURCHASED",
            "PURCHASED, ACTIVE",
            "ACTIVE, ACTIVE",
            "PURCHASED, PURCHASED"
    })
    void updateWishStatus_whenStatusIsValid_persistsStatusAndPreservesDetails(
            WishStatus initialStatus,
            WishStatus requestedStatus
    ) throws Exception {
        Wish wish = createWish(initialStatus);
        String requestBody = "{\"status\":\"%s\"}".formatted(requestedStatus);

        mockMvc.perform(patch("/api/wishes/{id}/status", wish.getWishId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(wish.getWishId()))
                .andExpect(jsonPath("$.status").value(requestedStatus.name()))
                .andExpect(jsonPath("$.wishName").value("Kindle"))
                .andExpect(jsonPath("$.wishPrice").value(120.0))
                .andExpect(jsonPath("$.url").value("https://example.com/kindle"))
                .andExpect(jsonPath("$.categoryId").value(wish.getCategory().getId()))
                .andExpect(jsonPath("$.categoryName").value("Books"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        Wish persisted = wishRepository.findById(wish.getWishId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(requestedStatus);
        assertThat(persisted.getWishName()).isEqualTo(wish.getWishName());
        assertThat(persisted.getWishPrice()).isEqualTo(wish.getWishPrice());
        assertThat(persisted.getUrl()).isEqualTo(wish.getUrl());
        assertThat(persisted.getPriority()).isEqualTo(wish.getPriority());
        assertThat(persisted.getCategory().getId()).isEqualTo(wish.getCategory().getId());

        mockMvc.perform(get("/api/wishes/{id}", wish.getWishId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(requestedStatus.name()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"status\":null}"})
    void updateWishStatus_whenStatusIsMissing_returnsValidationError(String requestBody) throws Exception {
        Wish wish = createWish(WishStatus.PURCHASED);

        mockMvc.perform(patch("/api/wishes/{id}/status", wish.getWishId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Wish status is required"));

        assertThat(wishRepository.findById(wish.getWishId()).orElseThrow().getStatus())
                .isEqualTo(WishStatus.PURCHASED);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"status\":\"UNKNOWN\"}",
            "{\"status\":\"active\"}",
            "{\"status\":0}",
            "{\"status\":1}",
            "{\"status\":\"0\"}",
            "{\"status\":true}",
            "{\"status\":{}}",
            "{\"status\":[]}",
            "{\"status\":",
            "[]",
            "null"
    })
    void updateWishStatus_whenBodyIsInvalid_returnsGenericErrorAndDoesNotMutateWish(
            String requestBody
    ) throws Exception {
        Wish wish = createWish(WishStatus.PURCHASED);

        mockMvc.perform(patch("/api/wishes/{id}/status", wish.getWishId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());

        assertThat(wishRepository.findById(wish.getWishId()).orElseThrow().getStatus())
                .isEqualTo(WishStatus.PURCHASED);
    }

    @Test
    void updateWishStatus_whenBodyIsAbsent_returnsBadRequest() throws Exception {
        Wish wish = createWish(WishStatus.ACTIVE);

        mockMvc.perform(patch("/api/wishes/{id}/status", wish.getWishId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        assertThat(wishRepository.findById(wish.getWishId()).orElseThrow().getStatus())
                .isEqualTo(WishStatus.ACTIVE);
    }

    @Test
    void updateWishStatus_whenWishDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/wishes/{id}/status", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PURCHASED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wish with id " + Long.MAX_VALUE + " was not found"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        assertThat(wishRepository.count()).isZero();
    }

    @Test
    void updateWish_whenWishIsPurchased_preservesStatus() throws Exception {
        Wish wish = createWish(WishStatus.PURCHASED);
        String requestBody = """
                {
                  "wishName": "Kindle Paperwhite",
                  "wishPrice": 150.0,
                  "url": "https://example.com/paperwhite",
                  "categoryId": %d,
                  "priority": "LOW"
                }
                """.formatted(wish.getCategory().getId());

        mockMvc.perform(put("/api/wishes/{id}", wish.getWishId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishName").value("Kindle Paperwhite"))
                .andExpect(jsonPath("$.wishPrice").value(150.0))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.status").value("PURCHASED"));

        Wish persisted = wishRepository.findById(wish.getWishId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(WishStatus.PURCHASED);
        assertThat(persisted.getWishName()).isEqualTo("Kindle Paperwhite");
    }

    @Test
    void wishRoutes_whenWishExists_supportReadListAndDelete() throws Exception {
        Wish wish = createWish(WishStatus.PURCHASED);

        mockMvc.perform(get("/api/wishes/{id}", wish.getWishId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(wish.getWishId()))
                .andExpect(jsonPath("$.status").value("PURCHASED"));

        mockMvc.perform(get("/api/wishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(wish.getWishId()))
                .andExpect(jsonPath("$[0].status").value("PURCHASED"));

        mockMvc.perform(delete("/api/wishes/{id}", wish.getWishId()))
                .andExpect(status().isNoContent());

        assertThat(wishRepository.existsById(wish.getWishId())).isFalse();
        mockMvc.perform(get("/api/wishes/{id}", wish.getWishId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWishStatus_whenOriginIsAllowed_passesCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/wishes/1/status")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")));
    }

    @Test
    void updateWishStatus_whenOriginIsNotAllowed_rejectsCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/wishes/1/status")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void createWish_whenPriorityIsNumeric_returnsGenericBadRequest() throws Exception {
        Category category = createCategory();
        String requestBody = """
                {
                  "wishName": "Kindle",
                  "wishPrice": 120.0,
                  "categoryId": %d,
                  "priority": 0
                }
                """.formatted(category.getId());

        mockMvc.perform(post("/api/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        assertThat(wishRepository.count()).isZero();
    }

    private Wish createWish(WishStatus status) {
        Wish wish = new Wish();
        wish.setWishName("Kindle");
        wish.setWishPrice(120.0);
        wish.setUrl("https://example.com/kindle");
        wish.setCategory(createCategory());
        wish.setPriority(Priority.HIGH);
        wish.setStatus(status);

        return wishRepository.saveAndFlush(wish);
    }
}
