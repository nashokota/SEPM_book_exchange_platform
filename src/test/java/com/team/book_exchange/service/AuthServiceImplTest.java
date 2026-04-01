package com.team.book_exchange.service;

import com.team.book_exchange.dto.auth.RegisterRequest;
import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.exception.EmailAlreadyExistsException;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.UserRepository;
import com.team.book_exchange.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

   @Mock
   private UserRepository userRepository;

   @Mock
   private RoleRepository roleRepository;

   @Mock
   private PasswordEncoder passwordEncoder;

   @InjectMocks
   private AuthServiceImpl authService;

   @Test
   void registerBuyer_shouldSaveUserWithBuyerRole_whenEmailIsNew() {
       RegisterRequest request = new RegisterRequest();
       request.setFullName("Buyer One");
       request.setEmail("Buyer@example.com");
       request.setPassword("password123");
       request.setConfirmPassword("password123");
       request.setPhoneNumber("01700000000");
       request.setAddress("Khulna");

       Role buyerRole = Role.builder()
               .id(2L)
               .name(RoleName.ROLE_BUYER)
               .build();

       when(userRepository.existsByEmail("buyer@example.com")).thenReturn(false);
       when(roleRepository.findByName(RoleName.ROLE_BUYER)).thenReturn(Optional.of(buyerRole));
       when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
       when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

       User savedUser = authService.registerBuyer(request);

       assertEquals("Buyer One", savedUser.getFullName());
       assertEquals("buyer@example.com", savedUser.getEmail());
       assertEquals("encodedPassword", savedUser.getPassword());
       assertTrue(savedUser.getRoles().contains(buyerRole));
       verify(userRepository).save(any(User.class));
   }

   @Test
   void registerBuyer_shouldThrowException_whenEmailAlreadyExists() {
       RegisterRequest request = new RegisterRequest();
       request.setFullName("Buyer One");
       request.setEmail("buyer@example.com");
       request.setPassword("password123");
       request.setConfirmPassword("password123");

       when(userRepository.existsByEmail("buyer@example.com")).thenReturn(true);

       assertThrows(EmailAlreadyExistsException.class, () -> authService.registerBuyer(request));
       verify(userRepository, never()).save(any(User.class));
   }
}
