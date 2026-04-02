package com.team.book_exchange.dto.book;

import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookResponse {

   private Long id;
   private String title;
   private String author;
   private String isbn;
   private BookCondition condition;
   private ListingMode listingMode;
   private BookAvailabilityStatus availabilityStatus;
   private BigDecimal price;
   private String exchangePreference;
   private String imageUrl;
   private String description;

   private Long categoryId;
   private String categoryName;

   private Long sellerId;
   private String sellerName;

   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
}
