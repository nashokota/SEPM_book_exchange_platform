package com.team.book_exchange.controller.web;

import com.team.book_exchange.service.BookService;
import com.team.book_exchange.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

   private final BookService bookService;
   private final CategoryService categoryService;

   @GetMapping("/")
   public String home(Model model) {
       var featuredBooks = bookService.getPublicAvailableBooks(null, null, 0, 4);

       model.addAttribute("appName", "Book Exchange Platform");
       model.addAttribute("featuredBooks", featuredBooks.getContent());
       model.addAttribute("availableBooksCount", featuredBooks.getTotalElements());
       model.addAttribute("categoryCount", categoryService.getAllCategories().size());
       model.addAttribute("categories", categoryService.getAllCategories());
       return "home";
   }

   @GetMapping("/access-denied")
   public String accessDenied() {
       return "error/access-denied";
   }
}
