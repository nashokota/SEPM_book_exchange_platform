package com.team.book_exchange.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

   @GetMapping("/")
   public String home(Model model) {
       model.addAttribute("appName", "Book Exchange Platform");
       return "home";
   }

   @GetMapping("/access-denied")
   public String accessDenied() {
       return "error/access-denied";
   }
}
