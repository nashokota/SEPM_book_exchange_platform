package com.team.book_exchange.repository;

import com.team.book_exchange.entity.SellerApplication;
import com.team.book_exchange.enums.SellerApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {

    boolean existsByUserIdAndStatus(Long userId, SellerApplicationStatus status);

    @EntityGraph(attributePaths = "user")
    List<SellerApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "user")
    List<SellerApplication> findByStatusOrderByCreatedAtDesc(SellerApplicationStatus status);
}
