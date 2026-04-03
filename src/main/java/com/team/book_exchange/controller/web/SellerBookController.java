package com.team.book_exchange.controller.web;

import com.team.book_exchange.dto.book.BookRequest;
import com.team.book_exchange.entity.Book;
import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import com.team.book_exchange.exception.InvalidBookListingException;
import com.team.book_exchange.service.BookService;
import com.team.book_exchange.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/books")
@RequiredArgsConstructor
public class SellerBookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    @GetMapping
    public String listMyBooks(Authentication authentication, Model model) {
        model.addAttribute("books", bookService.getBooksForSeller(authentication.getName()));
        return "seller/books";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        BookRequest request = new BookRequest();
        request.setCondition(BookCondition.GOOD);
        request.setListingMode(ListingMode.EXCHANGE_ONLY);
        request.setAvailabilityStatus(BookAvailabilityStatus.AVAILABLE);

        model.addAttribute("bookRequest", request);
        addReferenceData(model);
        model.addAttribute("pageTitle", "Create Book Listing");
        model.addAttribute("formAction", "/seller/books/create");
        model.addAttribute("submitLabel", "Create Listing");

        return "seller/book-form";
    }

    @PostMapping("/create")
    public String createBook(
        @Valid @ModelAttribute("bookRequest") BookRequest bookRequest,
        BindingResult bindingResult,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addReferenceData(model);
            model.addAttribute("pageTitle", "Create Book Listing");
            model.addAttribute("formAction", "/seller/books/create");
            model.addAttribute("submitLabel", "Create Listing");
            return "seller/book-form";
        }

        try {
            bookService.createBook(authentication.getName(), bookRequest);
        } catch (InvalidBookListingException ex) {
            bindingResult.reject("invalidListing", ex.getMessage());
            addReferenceData(model);
            model.addAttribute("pageTitle", "Create Book Listing");
            model.addAttribute("formAction", "/seller/books/create");
            model.addAttribute("submitLabel", "Create Listing");
            return "seller/book-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Book listing created successfully.");
        return "redirect:/seller/books";
    }

    @GetMapping("/{bookId}/edit")
    public String showEditPage(@PathVariable Long bookId, Authentication authentication, Model model) {
        Book book = bookService.getBookForSeller(bookId, authentication.getName());

        BookRequest request = new BookRequest();
        request.setTitle(book.getTitle());
        request.setAuthor(book.getAuthor());
        request.setIsbn(book.getIsbn());
        request.setCondition(book.getCondition());
        request.setListingMode(book.getListingMode());
        request.setAvailabilityStatus(book.getAvailabilityStatus());
        request.setPrice(book.getPrice());
        request.setExchangePreference(book.getExchangePreference());
        request.setImageUrl(book.getImageUrl());
        request.setDescription(book.getDescription());
        request.setCategoryId(book.getCategory().getId());

        model.addAttribute("bookRequest", request);
        addReferenceData(model);
        model.addAttribute("pageTitle", "Edit Book Listing");
        model.addAttribute("formAction", "/seller/books/" + bookId + "/edit");
        model.addAttribute("submitLabel", "Update Listing");

        return "seller/book-form";
    }

    @PostMapping("/{bookId}/edit")
    public String updateBook(
        @PathVariable Long bookId,
        @Valid @ModelAttribute("bookRequest") BookRequest bookRequest,
        BindingResult bindingResult,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addReferenceData(model);
            model.addAttribute("pageTitle", "Edit Book Listing");
            model.addAttribute("formAction", "/seller/books/" + bookId + "/edit");
            model.addAttribute("submitLabel", "Update Listing");
            return "seller/book-form";
        }

        try {
            bookService.updateBook(bookId, authentication.getName(), bookRequest);
        } catch (InvalidBookListingException ex) {
            bindingResult.reject("invalidListing", ex.getMessage());
            addReferenceData(model);
            model.addAttribute("pageTitle", "Edit Book Listing");
            model.addAttribute("formAction", "/seller/books/" + bookId + "/edit");
            model.addAttribute("submitLabel", "Update Listing");
            return "seller/book-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Book listing updated successfully.");
        return "redirect:/seller/books";
    }

    @PostMapping("/{bookId}/delete")
    public String deleteBook(@PathVariable Long bookId, Authentication authentication, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(bookId, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Book listing deleted successfully.");
        return "redirect:/seller/books";
    }

    private void addReferenceData(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bookConditions", BookCondition.values());
        model.addAttribute("listingModes", ListingMode.values());
        model.addAttribute("availabilityStatuses", BookAvailabilityStatus.values());
    }
}
