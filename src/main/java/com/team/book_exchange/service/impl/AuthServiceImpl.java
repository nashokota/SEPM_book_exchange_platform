package com.team.book_exchange.service.impl;

import com.team.book_exchange.dto.auth.RegisterRequest;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.exception.EmailAlreadyExistsException;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final PasswordEncoder passwordEncoder;

   @Override
   public User registerBuyer(RegisterRequest registerRequest) {
       String normalizedEmail = normalizeRequired(registerRequest.getEmail()).toLowerCase(Locale.ROOT);

       if (userRepository.existsByEmail(normalizedEmail)) {
           throw new EmailAlreadyExistsException("An account with this email already exists.");
       }

       Role buyerRole = roleRepository.findByName(RoleName.ROLE_BUYER)
               .orElseThrow(() -> new IllegalStateException("ROLE_BUYER was not found."));

       User user = User.builder()
               .fullName(normalizeRequired(registerRequest.getFullName()))
               .email(normalizedEmail)
               .password(passwordEncoder.encode(registerRequest.getPassword()))
               .phoneNumber(normalizeOptional(registerRequest.getPhoneNumber()))
               .address(normalizeOptional(registerRequest.getAddress()))
               .enabled(true)
               .roles(new HashSet<>())
               .build();

       user.getRoles().add(buyerRole);

       return userRepository.save(user);
   }

   private String normalizeRequired(String value) {
       return value == null ? null : value.trim();
   }

   private String normalizeOptional(String value) {
       if (value == null) {
           return null;
       }

       String trimmed = value.trim();
       return trimmed.isEmpty() ? null : trimmed;
   }
}
