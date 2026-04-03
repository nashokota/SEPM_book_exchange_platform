package com.team.book_exchange.dto.request;

import com.team.book_exchange.enums.RequestStatus;
import com.team.book_exchange.enums.RequestType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExchangeRequestResponse {

   private Long id;

   private Long bookId;
   private String bookTitle;

   private Long buyerId;
   private String buyerName;

   private Long sellerId;
   private String sellerName;

   private RequestType requestType;
   private RequestStatus status;

   private String offeredBookTitle;
   private String offeredBookDetails;
   private String message;
   private String sellerResponseRemarks;

   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
}
