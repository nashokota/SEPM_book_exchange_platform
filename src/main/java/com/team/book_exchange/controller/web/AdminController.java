package com.team.book_exchange.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

   @GetMapping("/admin")
   public String adminHome(Authentication authentication, Model model) {
       model.addAttribute("currentUserEmail", authentication.getName());
       return "admin/index";
   }
}
