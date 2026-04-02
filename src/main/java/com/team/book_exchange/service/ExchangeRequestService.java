package com.team.book_exchange.service;

import com.team.book_exchange.dto.request.ExchangeRequestRequest;
import com.team.book_exchange.entity.ExchangeRequest;

import java.util.List;

public interface ExchangeRequestService {

    ExchangeRequest submitRequest(String buyerEmail, ExchangeRequestRequest request);

    List<ExchangeRequest> getRequestsForBuyer(String buyerEmail);

    List<ExchangeRequest> getRequestsForSeller(String sellerEmail);

    ExchangeRequest approveRequest(Long requestId, String sellerEmail, String remarks);

    ExchangeRequest rejectRequest(Long requestId, String sellerEmail, String remarks);
}
