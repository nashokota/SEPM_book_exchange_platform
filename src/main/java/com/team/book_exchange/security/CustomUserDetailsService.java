package com.team.book_exchange.security;

import com.team.book_exchange.entity.User;
import com.team.book_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

   private final UserRepository userRepository;

   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);

       User user = userRepository.findByEmail(normalizedEmail)
               .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

       List<GrantedAuthority> authorities = user.getRoles().stream()
               .map(role -> new SimpleGrantedAuthority(role.getName().name()))
               .map(authority -> (GrantedAuthority) authority)
               .toList();

       return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
               .password(user.getPassword())
               .disabled(!Boolean.TRUE.equals(user.getEnabled()))
               .authorities(authorities)
               .build();
   }
}
