package com.team.book_exchange.service;

import com.team.book_exchange.dto.seller.SellerApplicationRequest;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.SellerApplication;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.enums.SellerApplicationStatus;
import com.team.book_exchange.exception.SellerApplicationNotAllowedException;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.SellerApplicationRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.impl.SellerApplicationServiceImpl;
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
class SellerApplicationServiceImplTest {

    @Mock
    private SellerApplicationRepository sellerApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private SellerApplicationServiceImpl sellerApplicationService;

    @Test
    void submitApplication_shouldSavePendingApplication_whenUserIsEligible() {
        Role buyerRole = Role.builder().id(2L).name(RoleName.ROLE_BUYER).build();

        User user = User.builder()
                .id(10L)
                .fullName("Buyer One")
                .email("buyer@example.com")
                .password("encoded")
                .enabled(true)
                .roles(new HashSet<>(Set.of(buyerRole)))
                .build();

        SellerApplicationRequest request = new SellerApplicationRequest();
        request.setReason("I want to exchange and sell books.");
        request.setPickupLocation("Khulna");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));
        when(sellerApplicationRepository.existsByUserIdAndStatus(10L, SellerApplicationStatus.PENDING)).thenReturn(false);
        when(sellerApplicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SellerApplication saved = sellerApplicationService.submitApplication("buyer@example.com", request);

        assertNotNull(saved);
        assertEquals(SellerApplicationStatus.PENDING, saved.getStatus());
        assertEquals("I want to exchange and sell books.", saved.getReason());
        assertEquals("Khulna", saved.getPickupLocation());
        assertEquals(user, saved.getUser());
        verify(sellerApplicationRepository).save(any(SellerApplication.class));
    }

    @Test
    void submitApplication_shouldThrowException_whenUserAlreadyHasSellerRole() {
        Role buyerRole = Role.builder().id(2L).name(RoleName.ROLE_BUYER).build();
        Role sellerRole = Role.builder().id(3L).name(RoleName.ROLE_SELLER).build();

        User user = User.builder()
                .id(10L)
                .fullName("Seller User")
                .email("seller@example.com")
                .password("encoded")
                .enabled(true)
                .roles(new HashSet<>(Set.of(buyerRole, sellerRole)))
                .build();

        SellerApplicationRequest request = new SellerApplicationRequest();
        request.setReason("I still want to apply again.");
        request.setPickupLocation("Khulna");

        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(user));

        assertThrows(SellerApplicationNotAllowedException.class,
                () -> sellerApplicationService.submitApplication("seller@example.com", request));

        verify(sellerApplicationRepository, never()).save(any(SellerApplication.class));
    }

    @Test
    void submitApplication_shouldThrowException_whenPendingApplicationAlreadyExists() {
        Role buyerRole = Role.builder().id(2L).name(RoleName.ROLE_BUYER).build();

        User user = User.builder()
                .id(11L)
                .fullName("Buyer Two")
                .email("buyer2@example.com")
                .password("encoded")
                .enabled(true)
                .roles(new HashSet<>(Set.of(buyerRole)))
                .build();

        SellerApplicationRequest request = new SellerApplicationRequest();
        request.setReason("Please approve me.");
        request.setPickupLocation("Dhaka");

        when(userRepository.findByEmail("buyer2@example.com")).thenReturn(Optional.of(user));
        when(sellerApplicationRepository.existsByUserIdAndStatus(11L, SellerApplicationStatus.PENDING)).thenReturn(true);

        assertThrows(SellerApplicationNotAllowedException.class,
                () -> sellerApplicationService.submitApplication("buyer2@example.com", request));

        verify(sellerApplicationRepository, never()).save(any(SellerApplication.class));
    }

    @Test
    void approveApplication_shouldMarkApprovedAndAddSellerRole() {
        Role buyerRole = Role.builder().id(2L).name(RoleName.ROLE_BUYER).build();
        Role sellerRole = Role.builder().id(3L).name(RoleName.ROLE_SELLER).build();

        User user = User.builder()
                .id(20L)
                .fullName("Buyer Three")
                .email("buyer3@example.com")
                .password("encoded")
                .enabled(true)
                .roles(new HashSet<>(Set.of(buyerRole)))
                .build();

        SellerApplication application = SellerApplication.builder()
                .id(100L)
                .user(user)
                .reason("I want to list my books.")
                .pickupLocation("Khulna")
                .status(SellerApplicationStatus.PENDING)
                .build();

        when(sellerApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(roleRepository.findByName(RoleName.ROLE_SELLER)).thenReturn(Optional.of(sellerRole));
        when(sellerApplicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SellerApplication approved = sellerApplicationService.approveApplication(100L, "Approved by admin.");

        assertEquals(SellerApplicationStatus.APPROVED, approved.getStatus());
        assertEquals("Approved by admin.", approved.getAdminRemarks());
        verify(userRepository).save(user);
        verify(sellerApplicationRepository).save(application);
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void rejectApplication_shouldMarkRejectedWithoutAddingSellerRole() {
        Role buyerRole = Role.builder().id(2L).name(RoleName.ROLE_BUYER).build();

        User user = User.builder()
                .id(21L)
                .fullName("Buyer Four")
                .email("buyer4@example.com")
                .password("encoded")
                .enabled(true)
                .roles(new HashSet<>(Set.of(buyerRole)))
                .build();

        SellerApplication application = SellerApplication.builder()
                .id(101L)
                .user(user)
                .reason("I want to list my books.")
                .pickupLocation("Dhaka")
                .status(SellerApplicationStatus.PENDING)
                .build();

        when(sellerApplicationRepository.findById(101L)).thenReturn(Optional.of(application));
        when(sellerApplicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SellerApplication rejected = sellerApplicationService.rejectApplication(101L, "Incomplete information.");

        assertEquals(SellerApplicationStatus.REJECTED, rejected.getStatus());
        assertEquals("Incomplete information.", rejected.getAdminRemarks());
        verify(userRepository, never()).save(any(User.class));
        verify(sellerApplicationRepository).save(application);
        assertEquals(1, user.getRoles().size());
    }
}
