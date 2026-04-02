package com.team.book_exchange.repository;

import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import com.team.book_exchange.enums.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("findBySellerIdOrderByCreatedAtDesc should return books for one seller only")
    void findBySellerIdOrderByCreatedAtDesc_shouldReturnOnlySellerBooks() {
        Role sellerRole = getOrCreateRole(RoleName.ROLE_SELLER);

        User sellerOne = userRepository.save(User.builder()
            .fullName("Seller One")
            .email("seller1@example.com")
            .password("encoded")
            .enabled(true)
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build());

        User sellerTwo = userRepository.save(User.builder()
            .fullName("Seller Two")
            .email("seller2@example.com")
            .password("encoded")
            .enabled(true)
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build());

        Category category = categoryRepository.save(Category.builder()
            .name("Programming")
            .description("Tech books")
            .build());

        bookRepository.save(Book.builder()
            .seller(sellerOne)
            .category(category)
            .title("Book A")
            .author("Author A")
            .condition(BookCondition.GOOD)
            .listingMode(ListingMode.SELL_ONLY)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .price(new BigDecimal("400.00"))
            .build());

        bookRepository.save(Book.builder()
            .seller(sellerTwo)
            .category(category)
            .title("Book B")
            .author("Author B")
            .condition(BookCondition.GOOD)
            .listingMode(ListingMode.SELL_ONLY)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .price(new BigDecimal("500.00"))
            .build());

        var sellerOneBooks = bookRepository.findBySellerIdOrderByCreatedAtDesc(sellerOne.getId());

        assertEquals(1, sellerOneBooks.size());
        assertEquals("Book A", sellerOneBooks.get(0).getTitle());
    }

    @Test
    @DisplayName("findByIdAndAvailabilityStatus should return only matching available book")
    void findByIdAndAvailabilityStatus_shouldReturnOnlyAvailableBook() {
        Role sellerRole = getOrCreateRole(RoleName.ROLE_SELLER);

        User seller = userRepository.save(User.builder()
            .fullName("Seller One")
            .email("seller@example.com")
            .password("encoded")
            .enabled(true)
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build());

        Category category = categoryRepository.save(Category.builder()
            .name("Programming")
            .description("Tech books")
            .build());

        Book availableBook = bookRepository.save(Book.builder()
            .seller(seller)
            .category(category)
            .title("Available Book")
            .author("Author A")
            .condition(BookCondition.GOOD)
            .listingMode(ListingMode.SELL_ONLY)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .price(new BigDecimal("400.00"))
            .build());

        Book unavailableBook = bookRepository.save(Book.builder()
            .seller(seller)
            .category(category)
            .title("Unavailable Book")
            .author("Author B")
            .condition(BookCondition.GOOD)
            .listingMode(ListingMode.SELL_ONLY)
            .availabilityStatus(BookAvailabilityStatus.UNAVAILABLE)
            .price(new BigDecimal("500.00"))
            .build());

        Optional<Book> foundAvailable = bookRepository.findByIdAndAvailabilityStatus(
            availableBook.getId(),
            BookAvailabilityStatus.AVAILABLE
        );

        Optional<Book> hiddenUnavailable = bookRepository.findByIdAndAvailabilityStatus(
            unavailableBook.getId(),
            BookAvailabilityStatus.AVAILABLE
        );

        assertTrue(foundAvailable.isPresent());
        assertEquals("Available Book", foundAvailable.get().getTitle());
        assertTrue(hiddenUnavailable.isEmpty());
    }

    @Test
    @DisplayName("findByAvailabilityStatus should return only public available books")
    void findByAvailabilityStatus_shouldReturnOnlyAvailableBooks() {
        Role sellerRole = getOrCreateRole(RoleName.ROLE_SELLER);

        User seller = userRepository.save(User.builder()
            .fullName("Seller One")
            .email("seller@example.com")
            .password("encoded")
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
            .title("Visible Book")
            .author("Author A")
            .condition(BookCondition.GOOD)
            .listingMode(ListingMode.SELL_ONLY)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .price(new BigDecimal("400.00"))
            .build());

        bookRepository.save(Book.builder()
            .seller(seller)
            .category(category)
            .title("Hidden Book")
            .author("Author B")
            .condition(BookCondition.GOOD)
            .listingMode(ListingMode.SELL_ONLY)
            .availabilityStatus(BookAvailabilityStatus.UNAVAILABLE)
            .price(new BigDecimal("500.00"))
            .build());

        var page = bookRepository.findByAvailabilityStatus(
            BookAvailabilityStatus.AVAILABLE,
            PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        assertEquals("Visible Book", page.getContent().get(0).getTitle());
    }

    private Role getOrCreateRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
            .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }
}
