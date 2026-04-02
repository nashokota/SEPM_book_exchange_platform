package com.team.book_exchange.config;

import com.team.book_exchange.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

   private final CustomUserDetailsService customUserDetailsService;

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
               .authorizeHttpRequests(auth -> auth
                       .requestMatchers("/", "/login", "/register", "/access-denied", "/books", "/books/**", "/css/**", "/js/**", "/images/**").permitAll()
                       .requestMatchers("/admin/**").hasRole("ADMIN")
                       .requestMatchers("/seller/**").hasRole("SELLER")
                       .requestMatchers("/dashboard/**").authenticated()
                       .anyRequest().authenticated()
               )
               .formLogin(form -> form
                       .loginPage("/login")
                       .defaultSuccessUrl("/dashboard", true)
                       .permitAll()
               )
               .logout(logout -> logout
                       .logoutSuccessUrl("/login?logout")
                       .permitAll()
               )
               .exceptionHandling(exception -> exception
                       .accessDeniedPage("/access-denied")
               )
               .userDetailsService(customUserDetailsService);

       return http.build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }
}
