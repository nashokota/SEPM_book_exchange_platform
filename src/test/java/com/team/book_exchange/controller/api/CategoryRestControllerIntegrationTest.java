package com.team.book_exchange.controller.api;

import com.team.book_exchange.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryRestControllerIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private CategoryRepository categoryRepository;

   @Test
   @WithMockUser(username = "admin@bookexchange.local", roles = {"ADMIN"})
   void createCategory_shouldReturnCreatedAndPersistCategory() throws Exception {
       String requestBody = """
               {
                 "name": "History",
                 "description": "History related books"
               }
               """;

       mockMvc.perform(post("/api/categories")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value("History"))
               .andExpect(jsonPath("$.description").value("History related books"));

       assertTrue(categoryRepository.existsByNameIgnoreCase("History"));
   }
}
