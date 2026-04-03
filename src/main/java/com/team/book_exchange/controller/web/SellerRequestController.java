package com.team.book_exchange.controller.web;

import com.team.book_exchange.exception.RequestNotAllowedException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.service.ExchangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/requests")
@RequiredArgsConstructor
public class SellerRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @GetMapping
    public String incomingRequests(Authentication authentication, Model model) {
        model.addAttribute("requests", exchangeRequestService.getRequestsForSeller(authentication.getName()));
        return "seller/requests";
    }

    @PostMapping("/{requestId}/approve")
    public String approveRequest(
        @PathVariable Long requestId,
        @RequestParam(value = "remarks", required = false) String remarks,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            exchangeRequestService.approveRequest(requestId, authentication.getName(), remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Request approved successfully.");
        } catch (RequestNotAllowedException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/seller/requests";
    }

    @PostMapping("/{requestId}/reject")
    public String rejectRequest(
        @PathVariable Long requestId,
        @RequestParam(value = "remarks", required = false) String remarks,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            exchangeRequestService.rejectRequest(requestId, authentication.getName(), remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Request rejected successfully.");
        } catch (RequestNotAllowedException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/seller/requests";
    }
}
