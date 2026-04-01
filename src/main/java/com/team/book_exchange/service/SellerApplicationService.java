package com.team.book_exchange.service;

import com.team.book_exchange.dto.seller.SellerApplicationRequest;
import com.team.book_exchange.entity.SellerApplication;

import java.util.List;

public interface SellerApplicationService {

    SellerApplication submitApplication(String userEmail, SellerApplicationRequest request);

    List<SellerApplication> getApplicationsForUser(String userEmail);

    List<SellerApplication> getPendingApplications();

    SellerApplication approveApplication(Long applicationId, String adminRemarks);

    SellerApplication rejectApplication(Long applicationId, String adminRemarks);

    boolean canUserApply(String userEmail);
}
