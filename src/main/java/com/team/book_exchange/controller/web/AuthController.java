package com.team.book_exchange.controller.web;

import com.team.book_exchange.dto.auth.RegisterRequest;
import com.team.book_exchange.exception.EmailAlreadyExistsException;
import com.team.book_exchange.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

   private final AuthService authService;

   @ModelAttribute("registerRequest")
   public RegisterRequest registerRequest() {
       return new RegisterRequest();
   }

   @GetMapping("/login")
   public String showLoginPage(
           @RequestParam(value = "error", required = false) String error,
           @RequestParam(value = "logout", required = false) String logout,
           Authentication authentication,
           Model model
   ) {
       if (isAuthenticated(authentication)) {
           return "redirect:/dashboard";
       }

       if (error != null) {
           model.addAttribute("errorMessage", "Invalid email or password.");
       }

       if (logout != null) {
           model.addAttribute("successMessage", "You have been logged out successfully.");
       }

       return "auth/login";
   }

   @GetMapping("/register")
   public String showRegisterPage(Authentication authentication) {
       if (isAuthenticated(authentication)) {
           return "redirect:/dashboard";
       }

       return "auth/register";
   }

   @PostMapping("/register")
   public String registerBuyer(
           @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
           BindingResult bindingResult,
           RedirectAttributes redirectAttributes,
           Authentication authentication
   ) {
       if (isAuthenticated(authentication)) {
           return "redirect:/dashboard";
       }

       if (registerRequest.getPassword() != null
               && registerRequest.getConfirmPassword() != null
               && !registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
           bindingResult.rejectValue("confirmPassword", "mismatch", "Password and confirm password must match.");
       }

       if (bindingResult.hasErrors()) {
           return "auth/register";
       }

       try {
           authService.registerBuyer(registerRequest);
       } catch (EmailAlreadyExistsException ex) {
           bindingResult.rejectValue("email", "duplicate", ex.getMessage());
           return "auth/register";
       }

       redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please log in.");
       return "redirect:/login";
   }

   private boolean isAuthenticated(Authentication authentication) {
       return authentication != null
               && authentication.isAuthenticated()
               && !(authentication instanceof AnonymousAuthenticationToken);
   }
}
