package com.team.book_exchange.service;

import com.team.book_exchange.dto.book.BookRequest;
import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.exception.InvalidBookListingException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.repository.BookRepository;
import com.team.book_exchange.repository.CategoryRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void createBook_shouldSaveBook_whenRequestIsValid() {
        Role sellerRole = Role.builder().id(3L).name(RoleName.ROLE_SELLER).build();

        User seller = User.builder()
            .id(10L)
            .fullName("Seller One")
            .email("seller@example.com")
            .password("encoded")
            .enabled(true)
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build();

        Category category = Category.builder()
            .id(5L)
            .name("Programming")
            .description("Tech books")
            .build();

        BookRequest request = new BookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setCategoryId(5L);
        request.setCondition(BookCondition.GOOD);
        request.setListingMode(ListingMode.SELL_ONLY);
        request.setAvailabilityStatus(BookAvailabilityStatus.AVAILABLE);
        request.setPrice(new BigDecimal("500.00"));
        request.setDescription("Well maintained copy.");

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book saved = bookService.createBook("seller@example.com", request);

        assertEquals("Clean Code", saved.getTitle());
        assertEquals("Robert C. Martin", saved.getAuthor());
        assertEquals(category, saved.getCategory());
        assertEquals(new BigDecimal("500.00"), saved.getPrice());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_shouldThrowException_whenSellModeHasNoPrice() {
        User seller = User.builder()
            .id(10L)
            .email("seller@example.com")
            .roles(new HashSet<>())
            .build();

        Category category = Category.builder()
            .id(5L)
            .name("Programming")
            .build();

        BookRequest request = new BookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setCategoryId(5L);
        request.setCondition(BookCondition.GOOD);
        request.setListingMode(ListingMode.SELL_ONLY);
        request.setAvailabilityStatus(BookAvailabilityStatus.AVAILABLE);

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        assertThrows(InvalidBookListingException.class,
            () -> bookService.createBook("seller@example.com", request));
    }

    @Test
    void createBook_shouldSetPriceNull_whenExchangeOnly() {
        User seller = User.builder()
            .id(10L)
            .email("seller@example.com")
            .roles(new HashSet<>())
            .build();

        Category category = Category.builder()
            .id(5L)
            .name("Programming")
            .build();

        BookRequest request = new BookRequest();
        request.setTitle("Operating Systems");
        request.setAuthor("Silberschatz");
        request.setCategoryId(5L);
        request.setCondition(BookCondition.GOOD);
        request.setListingMode(ListingMode.EXCHANGE_ONLY);
        request.setAvailabilityStatus(BookAvailabilityStatus.AVAILABLE);
        request.setPrice(new BigDecimal("700.00"));

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book saved = bookService.createBook("seller@example.com", request);

        assertNull(saved.getPrice());
    }

    @Test
    void updateBook_shouldThrowException_whenBookDoesNotBelongToSeller() {
        User seller = User.builder()
            .id(10L)
            .email("seller@example.com")
            .roles(new HashSet<>())
            .build();

        BookRequest request = new BookRequest();
        request.setTitle("Networks");
        request.setAuthor("Tanenbaum");
        request.setCategoryId(5L);
        request.setCondition(BookCondition.GOOD);
        request.setListingMode(ListingMode.EXCHANGE_ONLY);
        request.setAvailabilityStatus(BookAvailabilityStatus.AVAILABLE);

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(bookRepository.findByIdAndSellerId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> bookService.updateBook(99L, "seller@example.com", request));
    }

    @Test
    void deleteBook_shouldDeleteOwnedBook() {
        User seller = User.builder()
            .id(10L)
            .email("seller@example.com")
            .roles(new HashSet<>())
            .build();

        Book book = Book.builder()
            .id(22L)
            .seller(seller)
            .build();

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(bookRepository.findByIdAndSellerId(22L, 10L)).thenReturn(Optional.of(book));

        bookService.deleteBook(22L, "seller@example.com");

        verify(bookRepository).delete(book);
    }

    @Test
    void getPublicAvailableBooks_shouldReturnAvailableBooks_whenNoSearchOrFilter() {
        Book availableBook = Book.builder()
            .id(1L)
            .title("Clean Architecture")
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .build();

        when(bookRepository.findByAvailabilityStatus(eq(BookAvailabilityStatus.AVAILABLE), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(availableBook)));

        var result = bookService.getPublicAvailableBooks(null, null, 0, 6);

        assertEquals(1, result.getContent().size());
        assertEquals("Clean Architecture", result.getContent().get(0).getTitle());
    }

    @Test
    void getPublicAvailableBooks_shouldSearchByKeyword_whenKeywordProvided() {
        Book availableBook = Book.builder()
            .id(2L)
            .title("Operating Systems")
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .build();

        when(bookRepository.findByAvailabilityStatusAndTitleContainingIgnoreCaseOrAvailabilityStatusAndAuthorContainingIgnoreCase(
            eq(BookAvailabilityStatus.AVAILABLE),
            eq("operating"),
            eq(BookAvailabilityStatus.AVAILABLE),
            eq("operating"),
            any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(availableBook)));

        var result = bookService.getPublicAvailableBooks("operating", null, 0, 6);

        assertEquals(1, result.getContent().size());
        assertEquals("Operating Systems", result.getContent().get(0).getTitle());
    }

    @Test
    void getPublicAvailableBook_shouldThrowException_whenAvailableBookDoesNotExist() {
        when(bookRepository.findByIdAndAvailabilityStatus(999L, BookAvailabilityStatus.AVAILABLE))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> bookService.getPublicAvailableBook(999L));
    }
}
