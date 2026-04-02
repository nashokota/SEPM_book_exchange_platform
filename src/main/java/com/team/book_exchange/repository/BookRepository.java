package com.team.book_exchange.repository;

import com.team.book_exchange.entity.Book;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<Book> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @EntityGraph(attributePaths = {"category"})
    Optional<Book> findByIdAndSellerId(Long id, Long sellerId);

    @EntityGraph(attributePaths = {"category", "seller"})
    Optional<Book> findByIdAndAvailabilityStatus(Long id, BookAvailabilityStatus availabilityStatus);

    @EntityGraph(attributePaths = {"category", "seller"})
    Page<Book> findByAvailabilityStatusAndCategoryIdAndTitleContainingIgnoreCaseOrAvailabilityStatusAndCategoryIdAndAuthorContainingIgnoreCase(
        BookAvailabilityStatus availabilityStatusForTitle,
        Long categoryIdForTitle,
        String titleKeyword,
        BookAvailabilityStatus availabilityStatusForAuthor,
        Long categoryIdForAuthor,
        String authorKeyword,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "seller"})
    Page<Book> findByAvailabilityStatusAndTitleContainingIgnoreCaseOrAvailabilityStatusAndAuthorContainingIgnoreCase(
        BookAvailabilityStatus availabilityStatusForTitle,
        String titleKeyword,
        BookAvailabilityStatus availabilityStatusForAuthor,
        String authorKeyword,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "seller"})
    Page<Book> findByAvailabilityStatusAndCategoryId(
        BookAvailabilityStatus availabilityStatus,
        Long categoryId,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "seller"})
    Page<Book> findByAvailabilityStatus(
        BookAvailabilityStatus availabilityStatus,
        Pageable pageable
    );
}
