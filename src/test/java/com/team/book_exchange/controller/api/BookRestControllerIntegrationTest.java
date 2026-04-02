package com.team.book_exchange.controller.api;

import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.repository.BookRepository;
import com.team.book_exchange.repository.CategoryRepository;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookRestControllerIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private CategoryRepository categoryRepository;

   @Autowired
   private BookRepository bookRepository;

   @Autowired
   private RoleRepository roleRepository;

   @Test
   void getPublicBooks_shouldReturnOnlyAvailableBooks() throws Exception {
       Role sellerRole = getOrCreateRole(RoleName.ROLE_SELLER);

       User seller = userRepository.save(User.builder()
               .fullName("Seller One")
               .email("seller1@example.com")
               .password("encodedPassword")
               .enabled(true)
               .roles(new HashSet<>(Set.of(sellerRole)))
               .build());

       Category category = categoryRepository.save(Category.builder()
               .name("Programming")
               .description("Tech books")
               .build());

       bookRepository.save(Book.builder()
               .seller(seller)
               .category(category)
               .title("Clean Code")
               .author("Robert C. Martin")
               .condition(BookCondition.GOOD)
               .listingMode(ListingMode.SELL_ONLY)
               .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
               .price(new BigDecimal("500.00"))
               .description("Available book")
               .build());

       bookRepository.save(Book.builder()
               .seller(seller)
               .category(category)
               .title("Hidden Book")
               .author("Unknown Author")
               .condition(BookCondition.GOOD)
               .listingMode(ListingMode.SELL_ONLY)
               .availabilityStatus(BookAvailabilityStatus.UNAVAILABLE)
               .price(new BigDecimal("300.00"))
               .description("Unavailable book")
               .build());

       mockMvc.perform(get("/api/books"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.length()").value(1))
               .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
               .andExpect(jsonPath("$.content[0].availabilityStatus").value("AVAILABLE"))
               .andExpect(jsonPath("$.totalElements").value(1));
   }

   private Role getOrCreateRole(RoleName roleName) {
       return roleRepository.findByName(roleName)
               .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
   }
}
