package com.team.book_exchange.controller.web;

import com.team.book_exchange.dto.seller.SellerApplicationRequest;
import com.team.book_exchange.exception.SellerApplicationNotAllowedException;
import com.team.book_exchange.service.SellerApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller-applications")
@RequiredArgsConstructor
public class SellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    @ModelAttribute("sellerApplicationRequest")
    public SellerApplicationRequest sellerApplicationRequest() {
        return new SellerApplicationRequest();
    }

    @GetMapping("/apply")
    public String showApplyPage(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!sellerApplicationService.canUserApply(authentication.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You cannot submit a seller application right now.");
            return "redirect:/seller-applications/mine";
        }

        return "seller-applications/apply";
    }

    @PostMapping("/apply")
    public String submitApplication(
            @Valid @ModelAttribute("sellerApplicationRequest") SellerApplicationRequest sellerApplicationRequest,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "seller-applications/apply";
        }

        try {
            sellerApplicationService.submitApplication(authentication.getName(), sellerApplicationRequest);
        } catch (SellerApplicationNotAllowedException ex) {
            bindingResult.reject("notAllowed", ex.getMessage());
            return "seller-applications/apply";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Seller application submitted successfully.");
        return "redirect:/seller-applications/mine";
    }

    @GetMapping("/mine")
    public String myApplications(Authentication authentication, Model model) {
        model.addAttribute("applications",
                sellerApplicationService.getApplicationsForUser(authentication.getName()));
        model.addAttribute("canApply",
                sellerApplicationService.canUserApply(authentication.getName()));

        return "seller-applications/mine";
    }
}
