package com.team.book_exchange.config;

import com.team.book_exchange.entity.User;
import com.team.book_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Locale;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalViewModelAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @ModelAttribute("currentUserRoles")
    public List<String> currentUserRoles(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return List.of();
        }

        return authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .sorted()
            .toList();
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(Authentication authentication) {
        return isAuthenticated(authentication) ? authentication.getName() : null;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        String normalizedEmail = authentication.getName().trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .map(User::getFullName)
            .orElse(authentication.getName());
    }

    @ModelAttribute("isSeller")
    public boolean isSeller(Authentication authentication) {
        return currentUserRoles(authentication).contains("ROLE_SELLER");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return currentUserRoles(authentication).contains("ROLE_ADMIN");
    }
}
