package com.team.book_exchange.controller.api;

import com.team.book_exchange.dto.api.PageResponse;
import com.team.book_exchange.dto.book.BookRequest;
import com.team.book_exchange.dto.book.BookResponse;
import com.team.book_exchange.entity.Book;
import com.team.book_exchange.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookRestController {

   private final BookService bookService;

   @GetMapping("/api/books")
   public ResponseEntity<PageResponse<BookResponse>> getPublicBooks(
           @RequestParam(value = "q", required = false) String keyword,
           @RequestParam(value = "categoryId", required = false) Long categoryId,
           @RequestParam(value = "page", defaultValue = "0") int page,
           @RequestParam(value = "size", defaultValue = "6") int size
   ) {
       Page<BookResponse> bookPage = bookService.getPublicAvailableBooks(keyword, categoryId, page, size)
               .map(this::mapBook);

       return ResponseEntity.ok(PageResponse.from(bookPage));
   }

   @GetMapping("/api/books/{bookId}")
   public ResponseEntity<BookResponse> getPublicBookById(@PathVariable Long bookId) {
       return ResponseEntity.ok(mapBook(bookService.getPublicAvailableBook(bookId)));
   }

   @GetMapping("/api/seller/books")
   public ResponseEntity<List<BookResponse>> getSellerBooks(Authentication authentication) {
       List<BookResponse> books = bookService.getBooksForSeller(authentication.getName()).stream()
               .map(this::mapBook)
               .toList();

       return ResponseEntity.ok(books);
   }

   @PostMapping("/api/books")
   public ResponseEntity<BookResponse> createBook(
           @Valid @RequestBody BookRequest request,
           Authentication authentication
   ) {
       Book created = bookService.createBook(authentication.getName(), request);
       return ResponseEntity.status(HttpStatus.CREATED).body(mapBook(created));
   }

   @PutMapping("/api/books/{bookId}")
   public ResponseEntity<BookResponse> updateBook(
           @PathVariable Long bookId,
           @Valid @RequestBody BookRequest request,
           Authentication authentication
   ) {
       Book updated = bookService.updateBook(bookId, authentication.getName(), request);
       return ResponseEntity.ok(mapBook(updated));
   }

   @DeleteMapping("/api/books/{bookId}")
   public ResponseEntity<Void> deleteBook(
           @PathVariable Long bookId,
           Authentication authentication
   ) {
       bookService.deleteBook(bookId, authentication.getName());
       return ResponseEntity.noContent().build();
   }

   private BookResponse mapBook(Book book) {
       return BookResponse.builder()
               .id(book.getId())
               .title(book.getTitle())
               .author(book.getAuthor())
               .isbn(book.getIsbn())
               .condition(book.getCondition())
               .listingMode(book.getListingMode())
               .availabilityStatus(book.getAvailabilityStatus())
               .price(book.getPrice())
               .exchangePreference(book.getExchangePreference())
               .imageUrl(book.getImageUrl())
               .description(book.getDescription())
               .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
               .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
               .sellerId(book.getSeller() != null ? book.getSeller().getId() : null)
               .sellerName(book.getSeller() != null ? book.getSeller().getFullName() : null)
               .createdAt(book.getCreatedAt())
               .updatedAt(book.getUpdatedAt())
               .build();
   }
}
