package com.team.book_exchange.controller.api;

import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.entity.ExchangeRequest;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import com.team.book_exchange.enums.RequestStatus;
import com.team.book_exchange.enums.RequestType;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.repository.BookRepository;
import com.team.book_exchange.repository.CategoryRepository;
import com.team.book_exchange.repository.ExchangeRequestRepository;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExchangeRequestRestControllerIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private RoleRepository roleRepository;

   @Autowired
   private CategoryRepository categoryRepository;

   @Autowired
   private BookRepository bookRepository;

   @Autowired
   private ExchangeRequestRepository exchangeRequestRepository;

   @Test
   @WithMockUser(username = "buyer@example.com", roles = {"BUYER"})
   void submitRequest_shouldReturnCreatedAndPersistRequest() throws Exception {
       Role buyerRole = getOrCreateRole(RoleName.ROLE_BUYER);
       Role sellerRole = getOrCreateRole(RoleName.ROLE_SELLER);

       User buyer = userRepository.save(User.builder()
               .fullName("Buyer One")
               .email("buyer@example.com")
               .password("encodedPassword")
               .enabled(true)
               .roles(new HashSet<>(Set.of(buyerRole)))
               .build());

       User seller = userRepository.save(User.builder()
               .fullName("Seller One")
               .email("seller@example.com")
               .password("encodedPassword")
               .enabled(true)
               .roles(new HashSet<>(Set.of(sellerRole)))
               .build());

       Category category = categoryRepository.save(Category.builder()
               .name("Programming")
               .description("Tech books")
               .build());

       Book book = bookRepository.save(Book.builder()
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

       String requestBody = """
               {
                 "bookId": %d,
                 "requestType": "BUY",
                 "message": "I want to buy this book."
               }
               """.formatted(book.getId());

       mockMvc.perform(post("/api/requests")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.status").value("PENDING"))
               .andExpect(jsonPath("$.requestType").value("BUY"))
               .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
               .andExpect(jsonPath("$.buyerName").value("Buyer One"));

       assertEquals(1, exchangeRequestRepository.findAll().size());
       ExchangeRequest savedRequest = exchangeRequestRepository.findAll().get(0);
       assertEquals(buyer.getId(), savedRequest.getBuyer().getId());
       assertEquals(seller.getId(), savedRequest.getSeller().getId());
   }

   @Test
   @WithMockUser(username = "seller@example.com", roles = {"SELLER"})
   void approveRequest_shouldReturnOkAndMarkBookUnavailable() throws Exception {
       Role buyerRole = getOrCreateRole(RoleName.ROLE_BUYER);
       Role sellerRole = getOrCreateRole(RoleName.ROLE_SELLER);

       User buyer = userRepository.save(User.builder()
               .fullName("Buyer Two")
               .email("buyer2@example.com")
               .password("encodedPassword")
               .enabled(true)
               .roles(new HashSet<>(Set.of(buyerRole)))
               .build());

       User seller = userRepository.save(User.builder()
               .fullName("Seller One")
               .email("seller@example.com")
               .password("encodedPassword")
               .enabled(true)
               .roles(new HashSet<>(Set.of(sellerRole)))
               .build());

       Category category = categoryRepository.save(Category.builder()
               .name("Programming")
               .description("Tech books")
               .build());

       Book book = bookRepository.save(Book.builder()
               .seller(seller)
               .category(category)
               .title("Operating Systems")
               .author("Silberschatz")
               .condition(BookCondition.GOOD)
               .listingMode(ListingMode.SELL_ONLY)
               .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
               .price(new BigDecimal("600.00"))
               .description("Available book")
               .build());

       ExchangeRequest exchangeRequest = exchangeRequestRepository.save(ExchangeRequest.builder()
               .book(book)
               .buyer(buyer)
               .seller(seller)
               .requestType(RequestType.BUY)
               .status(RequestStatus.PENDING)
               .message("Please approve this.")
               .build());

       String requestBody = """
               {
                 "remarks": "Approved by seller."
               }
               """;

       mockMvc.perform(patch("/api/seller/requests/{requestId}/approve", exchangeRequest.getId())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("APPROVED"))
               .andExpect(jsonPath("$.sellerResponseRemarks").value("Approved by seller."));

       Optional<Book> updatedBook = bookRepository.findById(book.getId());
       assertTrue(updatedBook.isPresent());
       assertEquals(BookAvailabilityStatus.UNAVAILABLE, updatedBook.get().getAvailabilityStatus());
   }

   private Role getOrCreateRole(RoleName roleName) {
       return roleRepository.findByName(roleName)
               .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
   }
}
