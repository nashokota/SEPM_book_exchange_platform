package com.team.book_exchange.controller.web;

import com.team.book_exchange.service.SellerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SellerApplicationService sellerApplicationService;

    @GetMapping("/admin")
    public String adminHome(Authentication authentication, Model model) {
        model.addAttribute("currentUserEmail", authentication.getName());
        model.addAttribute("pendingApplicationCount", sellerApplicationService.getPendingApplications().size());
        return "admin/index";
    }
}
