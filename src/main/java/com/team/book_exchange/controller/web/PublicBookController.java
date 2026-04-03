package com.team.book_exchange.controller.web;

import com.team.book_exchange.entity.Book;
import com.team.book_exchange.service.BookService;
import com.team.book_exchange.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PublicBookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    @GetMapping("/books")
    public String listAvailableBooks(
        @RequestParam(value = "q", required = false) String keyword,
        @RequestParam(value = "categoryId", required = false) Long categoryId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        Model model
    ) {
        int pageSize = 6;
        Page<Book> bookPage = bookService.getPublicAvailableBooks(keyword, categoryId, page, pageSize);

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        return "books/index";
    }

    @GetMapping("/books/{bookId}")
    public String showBookDetails(@PathVariable Long bookId, Model model) {
        model.addAttribute("book", bookService.getPublicAvailableBook(bookId));
        return "books/details";
    }
}
