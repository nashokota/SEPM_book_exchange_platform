package com.team.book_exchange.service.impl;

import com.team.book_exchange.dto.book.BookRequest;
import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.ListingMode;
import com.team.book_exchange.exception.InvalidBookListingException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.repository.BookRepository;
import com.team.book_exchange.repository.CategoryRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksForSeller(String sellerEmail) {
        User seller = findUserByEmail(sellerEmail);
        return bookRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookForSeller(Long bookId, String sellerEmail) {
        User seller = findUserByEmail(sellerEmail);
        return findBookByIdAndSeller(bookId, seller.getId());
    }

    @Override
    public Book createBook(String sellerEmail, BookRequest request) {
        User seller = findUserByEmail(sellerEmail);
        Category category = findCategoryById(request.getCategoryId());

        validateListingRules(request);

        Book book = Book.builder()
            .seller(seller)
            .category(category)
            .title(normalizeRequired(request.getTitle()))
            .author(normalizeRequired(request.getAuthor()))
            .isbn(normalizeOptional(request.getIsbn()))
            .condition(request.getCondition())
            .listingMode(request.getListingMode())
            .availabilityStatus(request.getAvailabilityStatus())
            .price(normalizePriceForMode(request.getListingMode(), request.getPrice()))
            .exchangePreference(normalizeOptional(request.getExchangePreference()))
            .imageUrl(normalizeOptional(request.getImageUrl()))
            .description(normalizeOptional(request.getDescription()))
            .build();

        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long bookId, String sellerEmail, BookRequest request) {
        User seller = findUserByEmail(sellerEmail);
        Book book = findBookByIdAndSeller(bookId, seller.getId());
        Category category = findCategoryById(request.getCategoryId());

        validateListingRules(request);

        book.setCategory(category);
        book.setTitle(normalizeRequired(request.getTitle()));
        book.setAuthor(normalizeRequired(request.getAuthor()));
        book.setIsbn(normalizeOptional(request.getIsbn()));
        book.setCondition(request.getCondition());
        book.setListingMode(request.getListingMode());
        book.setAvailabilityStatus(request.getAvailabilityStatus());
        book.setPrice(normalizePriceForMode(request.getListingMode(), request.getPrice()));
        book.setExchangePreference(normalizeOptional(request.getExchangePreference()));
        book.setImageUrl(normalizeOptional(request.getImageUrl()));
        book.setDescription(normalizeOptional(request.getDescription()));

        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Long bookId, String sellerEmail) {
        User seller = findUserByEmail(sellerEmail);
        Book book = findBookByIdAndSeller(bookId, seller.getId());
        bookRepository.delete(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getPublicAvailableBooks(String keyword, Long categoryId, int page, int size) {
        String normalizedKeyword = normalizeOptional(keyword);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (normalizedKeyword != null && categoryId != null) {
            return bookRepository
                .findByAvailabilityStatusAndCategoryIdAndTitleContainingIgnoreCaseOrAvailabilityStatusAndCategoryIdAndAuthorContainingIgnoreCase(
                    BookAvailabilityStatus.AVAILABLE,
                    categoryId,
                    normalizedKeyword,
                    BookAvailabilityStatus.AVAILABLE,
                    categoryId,
                    normalizedKeyword,
                    pageable
                );
        }

        if (normalizedKeyword != null) {
            return bookRepository
                .findByAvailabilityStatusAndTitleContainingIgnoreCaseOrAvailabilityStatusAndAuthorContainingIgnoreCase(
                    BookAvailabilityStatus.AVAILABLE,
                    normalizedKeyword,
                    BookAvailabilityStatus.AVAILABLE,
                    normalizedKeyword,
                    pageable
                );
        }

        if (categoryId != null) {
            return bookRepository.findByAvailabilityStatusAndCategoryId(
                BookAvailabilityStatus.AVAILABLE,
                categoryId,
                pageable
            );
        }

        return bookRepository.findByAvailabilityStatus(BookAvailabilityStatus.AVAILABLE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Book getPublicAvailableBook(Long bookId) {
        return bookRepository.findByIdAndAvailabilityStatus(bookId, BookAvailabilityStatus.AVAILABLE)
            .orElseThrow(() -> new ResourceNotFoundException("Available book not found with id: " + bookId));
    }

    private void validateListingRules(BookRequest request) {
        if ((request.getListingMode() == ListingMode.SELL_ONLY || request.getListingMode() == ListingMode.BOTH)
            && request.getPrice() == null) {
            throw new InvalidBookListingException("Price is required for sell-only and both-mode listings.");
        }
    }

    private BigDecimal normalizePriceForMode(ListingMode listingMode, BigDecimal price) {
        if (listingMode == ListingMode.EXCHANGE_ONLY) {
            return null;
        }
        return price;
    }

    private User findUserByEmail(String sellerEmail) {
        String normalizedEmail = sellerEmail.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private Book findBookByIdAndSeller(Long bookId, Long sellerId) {
        return bookRepository.findByIdAndSellerId(bookId, sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found for this seller with id: " + bookId));
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
