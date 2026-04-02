package com.team.book_exchange.repository;

import com.team.book_exchange.entity.ExchangeRequest;
import com.team.book_exchange.enums.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    boolean existsByBuyerIdAndBookIdAndStatus(Long buyerId, Long bookId, RequestStatus status);

    boolean existsByBookIdAndStatus(Long bookId, RequestStatus status);

    @EntityGraph(attributePaths = {"book", "book.category", "buyer", "seller"})
    List<ExchangeRequest> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    @EntityGraph(attributePaths = {"book", "book.category", "buyer", "seller"})
    List<ExchangeRequest> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @EntityGraph(attributePaths = {"book", "book.category", "buyer", "seller"})
    Optional<ExchangeRequest> findByIdAndSellerId(Long id, Long sellerId);
}
