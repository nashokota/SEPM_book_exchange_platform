package com.team.book_exchange.repository;

import com.team.book_exchange.entity.Book;
import com.team.book_exchange.enums.BookAvailabilityStatus;
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
   List<Book> findByAvailabilityStatusOrderByCreatedAtDesc(BookAvailabilityStatus availabilityStatus);

   @EntityGraph(attributePaths = {"category", "seller"})
   Optional<Book> findByIdAndAvailabilityStatus(Long id, BookAvailabilityStatus availabilityStatus);
}
