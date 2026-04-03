package com.team.book_exchange.controller.web;

import com.team.book_exchange.dto.request.ExchangeRequestRequest;
import com.team.book_exchange.exception.RequestNotAllowedException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.service.BookService;
import com.team.book_exchange.service.ExchangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;
    private final BookService bookService;

    @ModelAttribute("exchangeRequestRequest")
    public ExchangeRequestRequest exchangeRequestRequest() {
        return new ExchangeRequestRequest();
    }

    @GetMapping("/create")
    public String showCreatePage(
        @RequestParam("bookId") Long bookId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("book", bookService.getPublicAvailableBook(bookId));
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/books";
        }

        ExchangeRequestRequest request = new ExchangeRequestRequest();
        request.setBookId(bookId);
        model.addAttribute("exchangeRequestRequest", request);

        return "requests/create";
    }

    @PostMapping("/create")
    public String submitRequest(
        @Valid @ModelAttribute("exchangeRequestRequest") ExchangeRequestRequest exchangeRequestRequest,
        BindingResult bindingResult,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("book", bookService.getPublicAvailableBook(exchangeRequestRequest.getBookId()));
        } catch (ResourceNotFoundException ex) {
            bindingResult.reject("bookNotFound", ex.getMessage());
            return "requests/create";
        }

        if (bindingResult.hasErrors()) {
            return "requests/create";
        }

        try {
            exchangeRequestService.submitRequest(authentication.getName(), exchangeRequestRequest);
        } catch (RequestNotAllowedException | ResourceNotFoundException ex) {
            bindingResult.reject("requestNotAllowed", ex.getMessage());
            return "requests/create";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Your request was submitted successfully.");
        return "redirect:/requests/my";
    }

    @GetMapping("/my")
    public String myRequests(Authentication authentication, Model model) {
        model.addAttribute("requests", exchangeRequestService.getRequestsForBuyer(authentication.getName()));
        return "requests/my";
    }
}
