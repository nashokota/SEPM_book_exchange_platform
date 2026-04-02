package com.team.book_exchange.controller.web;

import com.team.book_exchange.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PublicBookController {

   private final BookService bookService;

   @GetMapping("/books")
   public String listAvailableBooks(Model model) {
       model.addAttribute("books", bookService.getPublicAvailableBooks());
       return "books/index";
   }

   @GetMapping("/books/{bookId}")
   public String showBookDetails(@PathVariable Long bookId, Model model) {
       model.addAttribute("book", bookService.getPublicAvailableBook(bookId));
       return "books/details";
   }
}
