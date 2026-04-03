package com.team.book_exchange.service.impl;

import com.team.book_exchange.dto.request.ExchangeRequestRequest;
import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.ExchangeRequest;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.RequestStatus;
import com.team.book_exchange.enums.RequestType;
import com.team.book_exchange.exception.RequestNotAllowedException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.repository.BookRepository;
import com.team.book_exchange.repository.ExchangeRequestRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.ExchangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeRequestServiceImpl implements ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public ExchangeRequest submitRequest(String buyerEmail, ExchangeRequestRequest request) {
        User buyer = findUserByEmail(buyerEmail);
        Book book = findAvailableBookById(request.getBookId());
        User seller = book.getSeller();

        if (buyer.getId().equals(seller.getId())) {
            throw new RequestNotAllowedException("You cannot request your own book listing.");
        }

        if (exchangeRequestRepository.existsByBuyerIdAndBookIdAndStatus(
            buyer.getId(),
            book.getId(),
            RequestStatus.PENDING
        )) {
            throw new RequestNotAllowedException("You already have a pending request for this book.");
        }

        validateRequestRules(request);

        ExchangeRequest exchangeRequest = ExchangeRequest.builder()
            .book(book)
            .buyer(buyer)
            .seller(seller)
            .requestType(request.getRequestType())
            .status(RequestStatus.PENDING)
            .offeredBookTitle(normalizeOptional(request.getOfferedBookTitle()))
            .offeredBookDetails(normalizeOptional(request.getOfferedBookDetails()))
            .message(normalizeOptional(request.getMessage()))
            .build();

        if (request.getRequestType() == RequestType.BUY) {
            exchangeRequest.setOfferedBookTitle(null);
            exchangeRequest.setOfferedBookDetails(null);
        }

        return exchangeRequestRepository.save(exchangeRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getRequestsForBuyer(String buyerEmail) {
        User buyer = findUserByEmail(buyerEmail);
        return exchangeRequestRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getRequestsForSeller(String sellerEmail) {
        User seller = findUserByEmail(sellerEmail);
        return exchangeRequestRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId());
    }

    @Override
    public ExchangeRequest approveRequest(Long requestId, String sellerEmail, String remarks) {
        User seller = findUserByEmail(sellerEmail);
        ExchangeRequest exchangeRequest = findRequestByIdAndSeller(requestId, seller.getId());

        if (exchangeRequest.getStatus() != RequestStatus.PENDING) {
            throw new RequestNotAllowedException("Only pending requests can be approved.");
        }

        if (exchangeRequestRepository.existsByBookIdAndStatus(exchangeRequest.getBook().getId(), RequestStatus.APPROVED)) {
            throw new RequestNotAllowedException("This book already has an approved request.");
        }

        exchangeRequest.setStatus(RequestStatus.APPROVED);
        exchangeRequest.setSellerResponseRemarks(normalizeOptional(remarks));

        Book book = exchangeRequest.getBook();
        book.setAvailabilityStatus(BookAvailabilityStatus.UNAVAILABLE);
        bookRepository.save(book);

        return exchangeRequestRepository.save(exchangeRequest);
    }

    @Override
    public ExchangeRequest rejectRequest(Long requestId, String sellerEmail, String remarks) {
        User seller = findUserByEmail(sellerEmail);
        ExchangeRequest exchangeRequest = findRequestByIdAndSeller(requestId, seller.getId());

        if (exchangeRequest.getStatus() != RequestStatus.PENDING) {
            throw new RequestNotAllowedException("Only pending requests can be rejected.");
        }

        exchangeRequest.setStatus(RequestStatus.REJECTED);
        exchangeRequest.setSellerResponseRemarks(normalizeOptional(remarks));

        return exchangeRequestRepository.save(exchangeRequest);
    }

    private void validateRequestRules(ExchangeRequestRequest request) {
        if (request.getRequestType() == RequestType.EXCHANGE) {
            String offeredTitle = normalizeOptional(request.getOfferedBookTitle());
            if (offeredTitle == null) {
                throw new RequestNotAllowedException("Offered book title is required for exchange requests.");
            }
        }
    }

    private User findUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));
    }

    private Book findAvailableBookById(Long bookId) {
        return bookRepository.findByIdAndAvailabilityStatus(bookId, BookAvailabilityStatus.AVAILABLE)
            .orElseThrow(() -> new ResourceNotFoundException("Available book not found with id: " + bookId));
    }

    private ExchangeRequest findRequestByIdAndSeller(Long requestId, Long sellerId) {
        return exchangeRequestRepository.findByIdAndSellerId(requestId, sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found for this seller with id: " + requestId));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
