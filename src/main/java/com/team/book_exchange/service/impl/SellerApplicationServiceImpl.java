package com.team.book_exchange.service.impl;

import com.team.book_exchange.dto.seller.SellerApplicationRequest;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.SellerApplication;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.enums.SellerApplicationStatus;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.exception.SellerApplicationNotAllowedException;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.SellerApplicationRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.SellerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerApplicationServiceImpl implements SellerApplicationService {

    private final SellerApplicationRepository sellerApplicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public SellerApplication submitApplication(String userEmail, SellerApplicationRequest request) {
        User user = findUserByEmail(userEmail);
        ensureUserCanApply(user);

        SellerApplication application = SellerApplication.builder()
                .user(user)
                .reason(normalizeRequired(request.getReason()))
                .pickupLocation(normalizeRequired(request.getPickupLocation()))
                .status(SellerApplicationStatus.PENDING)
                .build();

        return sellerApplicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerApplication> getApplicationsForUser(String userEmail) {
        User user = findUserByEmail(userEmail);
        return sellerApplicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerApplication> getPendingApplications() {
        return sellerApplicationRepository.findByStatusOrderByCreatedAtDesc(SellerApplicationStatus.PENDING);
    }

    @Override
    public SellerApplication approveApplication(Long applicationId, String adminRemarks) {
        SellerApplication application = findApplicationById(applicationId);

        if (application.getStatus() != SellerApplicationStatus.PENDING) {
            throw new SellerApplicationNotAllowedException("Only pending seller applications can be approved.");
        }

        User user = application.getUser();
        Role sellerRole = roleRepository.findByName(RoleName.ROLE_SELLER)
                .orElseThrow(() -> new IllegalStateException("ROLE_SELLER was not found."));

        boolean alreadySeller = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_SELLER);

        if (!alreadySeller) {
            user.getRoles().add(sellerRole);
            userRepository.save(user);
        }

        application.setStatus(SellerApplicationStatus.APPROVED);
        application.setAdminRemarks(normalizeOptional(adminRemarks));

        return sellerApplicationRepository.save(application);
    }

    @Override
    public SellerApplication rejectApplication(Long applicationId, String adminRemarks) {
        SellerApplication application = findApplicationById(applicationId);

        if (application.getStatus() != SellerApplicationStatus.PENDING) {
            throw new SellerApplicationNotAllowedException("Only pending seller applications can be rejected.");
        }

        application.setStatus(SellerApplicationStatus.REJECTED);
        application.setAdminRemarks(normalizeOptional(adminRemarks));

        return sellerApplicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserApply(String userEmail) {
        User user = findUserByEmail(userEmail);

        boolean isSeller = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_SELLER);

        if (isSeller) {
            return false;
        }

        return !sellerApplicationRepository.existsByUserIdAndStatus(user.getId(), SellerApplicationStatus.PENDING);
    }

    private void ensureUserCanApply(User user) {
        boolean isSeller = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_SELLER);

        if (isSeller) {
            throw new SellerApplicationNotAllowedException("You are already an approved seller.");
        }

        boolean hasPendingApplication =
                sellerApplicationRepository.existsByUserIdAndStatus(user.getId(), SellerApplicationStatus.PENDING);

        if (hasPendingApplication) {
            throw new SellerApplicationNotAllowedException("You already have a pending seller application.");
        }
    }

    private User findUserByEmail(String userEmail) {
        String normalizedEmail = userEmail.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));
    }

    private SellerApplication findApplicationById(Long applicationId) {
        return sellerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller application not found with id: " + applicationId));
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
