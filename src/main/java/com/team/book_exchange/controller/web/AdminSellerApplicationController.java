package com.team.book_exchange.controller.web;

import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.exception.SellerApplicationNotAllowedException;
import com.team.book_exchange.service.SellerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/seller-applications")
@RequiredArgsConstructor
public class AdminSellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    @GetMapping
    public String listPendingApplications(Model model) {
        model.addAttribute("applications", sellerApplicationService.getPendingApplications());
        return "admin/seller-applications";
    }

    @PostMapping("/{applicationId}/approve")
    public String approveApplication(
            @PathVariable Long applicationId,
            @RequestParam(value = "adminRemarks", required = false) String adminRemarks,
            RedirectAttributes redirectAttributes
    ) {
        try {
            sellerApplicationService.approveApplication(applicationId, adminRemarks);
            redirectAttributes.addFlashAttribute("successMessage", "Seller application approved successfully.");
        } catch (ResourceNotFoundException | SellerApplicationNotAllowedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/seller-applications";
    }

    @PostMapping("/{applicationId}/reject")
    public String rejectApplication(
            @PathVariable Long applicationId,
            @RequestParam(value = "adminRemarks", required = false) String adminRemarks,
            RedirectAttributes redirectAttributes
    ) {
        try {
            sellerApplicationService.rejectApplication(applicationId, adminRemarks);
            redirectAttributes.addFlashAttribute("successMessage", "Seller application rejected successfully.");
        } catch (ResourceNotFoundException | SellerApplicationNotAllowedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/seller-applications";
    }
}
