package com.example.demo;


import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createCategory_whenRequestIsValid_returnsCreated() throws Exception {
        String requestBody = """
            {
              "name": "Books",
              "code": "books",
              "label": "Books"
            }
            """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void createCategory_whenCodeIsBlank_returnsBadRequest() throws Exception {
        String requestBody = """
                    {
                      "name": "Books",
                      "code": "",
                      "label": "Books"
                      }
                """;

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Category code is required"));
    }

    @Test
    void createCategory_whenCodeAlreadyExists_returnsConflict() throws Exception {
        String requestBody = """
            {
              "name": "Games",
              "code": "games-duplicate-test",
              "label": "Games"
            }
            """;

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }
}
