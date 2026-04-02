package com.team.book_exchange.controller.api;

import com.team.book_exchange.dto.request.ExchangeRequestRequest;
import com.team.book_exchange.dto.request.ExchangeRequestResponse;
import com.team.book_exchange.dto.request.RequestDecisionRequest;
import com.team.book_exchange.entity.ExchangeRequest;
import com.team.book_exchange.service.ExchangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeRequestRestController {

   private final ExchangeRequestService exchangeRequestService;

   @PostMapping("/api/requests")
   public ResponseEntity<ExchangeRequestResponse> submitRequest(
           @Valid @RequestBody ExchangeRequestRequest request,
           Authentication authentication
   ) {
       ExchangeRequest created = exchangeRequestService.submitRequest(authentication.getName(), request);
       return ResponseEntity.status(HttpStatus.CREATED).body(mapRequest(created));
   }

   @GetMapping("/api/requests/my")
   public ResponseEntity<List<ExchangeRequestResponse>> getMyRequests(Authentication authentication) {
       List<ExchangeRequestResponse> requests = exchangeRequestService.getRequestsForBuyer(authentication.getName()).stream()
               .map(this::mapRequest)
               .toList();

       return ResponseEntity.ok(requests);
   }

   @GetMapping("/api/seller/requests")
   public ResponseEntity<List<ExchangeRequestResponse>> getIncomingRequests(Authentication authentication) {
       List<ExchangeRequestResponse> requests = exchangeRequestService.getRequestsForSeller(authentication.getName()).stream()
               .map(this::mapRequest)
               .toList();

       return ResponseEntity.ok(requests);
   }

   @PatchMapping("/api/seller/requests/{requestId}/approve")
   public ResponseEntity<ExchangeRequestResponse> approveRequest(
           @PathVariable Long requestId,
           @Valid @RequestBody(required = false) RequestDecisionRequest request,
           Authentication authentication
   ) {
       String remarks = request != null ? request.getRemarks() : null;
       ExchangeRequest approved = exchangeRequestService.approveRequest(requestId, authentication.getName(), remarks);
       return ResponseEntity.ok(mapRequest(approved));
   }

   @PatchMapping("/api/seller/requests/{requestId}/reject")
   public ResponseEntity<ExchangeRequestResponse> rejectRequest(
           @PathVariable Long requestId,
           @Valid @RequestBody(required = false) RequestDecisionRequest request,
           Authentication authentication
   ) {
       String remarks = request != null ? request.getRemarks() : null;
       ExchangeRequest rejected = exchangeRequestService.rejectRequest(requestId, authentication.getName(), remarks);
       return ResponseEntity.ok(mapRequest(rejected));
   }

   private ExchangeRequestResponse mapRequest(ExchangeRequest request) {
       return ExchangeRequestResponse.builder()
               .id(request.getId())
               .bookId(request.getBook() != null ? request.getBook().getId() : null)
               .bookTitle(request.getBook() != null ? request.getBook().getTitle() : null)
               .buyerId(request.getBuyer() != null ? request.getBuyer().getId() : null)
               .buyerName(request.getBuyer() != null ? request.getBuyer().getFullName() : null)
               .sellerId(request.getSeller() != null ? request.getSeller().getId() : null)
               .sellerName(request.getSeller() != null ? request.getSeller().getFullName() : null)
               .requestType(request.getRequestType())
               .status(request.getStatus())
               .offeredBookTitle(request.getOfferedBookTitle())
               .offeredBookDetails(request.getOfferedBookDetails())
               .message(request.getMessage())
               .sellerResponseRemarks(request.getSellerResponseRemarks())
               .createdAt(request.getCreatedAt())
               .updatedAt(request.getUpdatedAt())
               .build();
   }
}
