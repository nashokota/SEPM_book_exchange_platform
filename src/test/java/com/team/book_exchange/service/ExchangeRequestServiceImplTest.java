package com.team.book_exchange.service;

import com.team.book_exchange.dto.request.ExchangeRequestRequest;
import com.team.book_exchange.entity.Book;
import com.team.book_exchange.entity.ExchangeRequest;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.RequestStatus;
import com.team.book_exchange.enums.RequestType;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.exception.RequestNotAllowedException;
import com.team.book_exchange.repository.BookRepository;
import com.team.book_exchange.repository.ExchangeRequestRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.impl.ExchangeRequestServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRequestServiceImplTest {

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ExchangeRequestServiceImpl exchangeRequestService;

    @Test
    void submitRequest_shouldSavePendingBuyRequest_whenValid() {
        Role buyerRole = Role.builder().name(RoleName.ROLE_BUYER).build();
        Role sellerRole = Role.builder().name(RoleName.ROLE_SELLER).build();

        User buyer = User.builder()
            .id(1L)
            .email("buyer@example.com")
            .roles(new HashSet<>(Set.of(buyerRole)))
            .build();

        User seller = User.builder()
            .id(2L)
            .email("seller@example.com")
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build();

        Book book = Book.builder()
            .id(10L)
            .title("Clean Code")
            .seller(seller)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .build();

        ExchangeRequestRequest request = new ExchangeRequestRequest();
        request.setBookId(10L);
        request.setRequestType(RequestType.BUY);
        request.setMessage("I want to buy this.");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(bookRepository.findByIdAndAvailabilityStatus(10L, BookAvailabilityStatus.AVAILABLE)).thenReturn(Optional.of(book));
        when(exchangeRequestRepository.existsByBuyerIdAndBookIdAndStatus(1L, 10L, RequestStatus.PENDING)).thenReturn(false);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExchangeRequest saved = exchangeRequestService.submitRequest("buyer@example.com", request);

        assertNotNull(saved);
        assertEquals(RequestStatus.PENDING, saved.getStatus());
        assertEquals(RequestType.BUY, saved.getRequestType());
        assertEquals(book, saved.getBook());
        assertEquals(buyer, saved.getBuyer());
        assertEquals(seller, saved.getSeller());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }

    @Test
    void submitRequest_shouldThrowException_whenBuyerRequestsOwnBook() {
        Role sellerRole = Role.builder().name(RoleName.ROLE_SELLER).build();

        User sameUser = User.builder()
            .id(5L)
            .email("same@example.com")
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build();

        Book book = Book.builder()
            .id(11L)
            .seller(sameUser)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .build();

        ExchangeRequestRequest request = new ExchangeRequestRequest();
        request.setBookId(11L);
        request.setRequestType(RequestType.BUY);

        when(userRepository.findByEmail("same@example.com")).thenReturn(Optional.of(sameUser));
        when(bookRepository.findByIdAndAvailabilityStatus(11L, BookAvailabilityStatus.AVAILABLE)).thenReturn(Optional.of(book));

        assertThrows(RequestNotAllowedException.class,
            () -> exchangeRequestService.submitRequest("same@example.com", request));

        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    void submitRequest_shouldThrowException_whenDuplicatePendingRequestExists() {
        Role buyerRole = Role.builder().name(RoleName.ROLE_BUYER).build();
        Role sellerRole = Role.builder().name(RoleName.ROLE_SELLER).build();

        User buyer = User.builder()
            .id(1L)
            .email("buyer@example.com")
            .roles(new HashSet<>(Set.of(buyerRole)))
            .build();

        User seller = User.builder()
            .id(2L)
            .email("seller@example.com")
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build();

        Book book = Book.builder()
            .id(10L)
            .seller(seller)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .build();

        ExchangeRequestRequest request = new ExchangeRequestRequest();
        request.setBookId(10L);
        request.setRequestType(RequestType.BUY);

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(bookRepository.findByIdAndAvailabilityStatus(10L, BookAvailabilityStatus.AVAILABLE)).thenReturn(Optional.of(book));
        when(exchangeRequestRepository.existsByBuyerIdAndBookIdAndStatus(1L, 10L, RequestStatus.PENDING)).thenReturn(true);

        assertThrows(RequestNotAllowedException.class,
            () -> exchangeRequestService.submitRequest("buyer@example.com", request));

        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    void submitRequest_shouldThrowException_whenExchangeRequestHasNoOfferedBookTitle() {
        Role buyerRole = Role.builder().name(RoleName.ROLE_BUYER).build();
        Role sellerRole = Role.builder().name(RoleName.ROLE_SELLER).build();

        User buyer = User.builder()
            .id(1L)
            .email("buyer@example.com")
            .roles(new HashSet<>(Set.of(buyerRole)))
            .build();

        User seller = User.builder()
            .id(2L)
            .email("seller@example.com")
            .roles(new HashSet<>(Set.of(sellerRole)))
            .build();

        Book book = Book.builder()
            .id(10L)
            .seller(seller)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .build();

        ExchangeRequestRequest request = new ExchangeRequestRequest();
        request.setBookId(10L);
        request.setRequestType(RequestType.EXCHANGE);

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(bookRepository.findByIdAndAvailabilityStatus(10L, BookAvailabilityStatus.AVAILABLE)).thenReturn(Optional.of(book));
        when(exchangeRequestRepository.existsByBuyerIdAndBookIdAndStatus(1L, 10L, RequestStatus.PENDING)).thenReturn(false);

        assertThrows(RequestNotAllowedException.class,
            () -> exchangeRequestService.submitRequest("buyer@example.com", request));
    }

    @Test
    void approveRequest_shouldApproveAndMarkBookUnavailable() {
        User seller = User.builder()
            .id(2L)
            .email("seller@example.com")
            .build();

        Book book = Book.builder()
            .id(10L)
            .availabilityStatus(BookAvailabilityStatus.AVAILABLE)
            .seller(seller)
            .build();

        ExchangeRequest exchangeRequest = ExchangeRequest.builder()
            .id(100L)
            .book(book)
            .seller(seller)
            .status(RequestStatus.PENDING)
            .build();

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(exchangeRequestRepository.findByIdAndSellerId(100L, 2L)).thenReturn(Optional.of(exchangeRequest));
        when(exchangeRequestRepository.existsByBookIdAndStatus(10L, RequestStatus.APPROVED)).thenReturn(false);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExchangeRequest approved = exchangeRequestService.approveRequest(100L, "seller@example.com", "Approved.");

        assertEquals(RequestStatus.APPROVED, approved.getStatus());
        assertEquals(BookAvailabilityStatus.UNAVAILABLE, book.getAvailabilityStatus());
        verify(bookRepository).save(book);
        verify(exchangeRequestRepository).save(exchangeRequest);
    }

    @Test
    void rejectRequest_shouldMarkRejected() {
        User seller = User.builder()
            .id(2L)
            .email("seller@example.com")
            .build();

        ExchangeRequest exchangeRequest = ExchangeRequest.builder()
            .id(101L)
            .seller(seller)
            .status(RequestStatus.PENDING)
            .build();

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(seller));
        when(exchangeRequestRepository.findByIdAndSellerId(101L, 2L)).thenReturn(Optional.of(exchangeRequest));
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExchangeRequest rejected = exchangeRequestService.rejectRequest(101L, "seller@example.com", "Not interested.");

        assertEquals(RequestStatus.REJECTED, rejected.getStatus());
        assertEquals("Not interested.", rejected.getSellerResponseRemarks());
        verify(exchangeRequestRepository).save(exchangeRequest);
    }
}
