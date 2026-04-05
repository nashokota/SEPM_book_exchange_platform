package com.team.book_exchange.config;

import com.team.book_exchange.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
               .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
               .authorizeHttpRequests(auth -> auth
                       .requestMatchers(
                               "/",
                               "/login",
                               "/register",
                               "/access-denied",
                               "/books",
                               "/books/**",
                               "/milestones",
                               "/api/milestones",
                               "/css/**",
                               "/js/**",
                               "/images/**"
                       ).permitAll()

                       .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                       .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()

                       .requestMatchers(HttpMethod.GET, "/api/seller/books").hasRole("SELLER")
                       .requestMatchers(HttpMethod.POST, "/api/books").hasRole("SELLER")
                       .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("SELLER")
                       .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("SELLER")

                       .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                       .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                       .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                       .requestMatchers(HttpMethod.POST, "/api/requests").authenticated()
                       .requestMatchers(HttpMethod.GET, "/api/requests/my").authenticated()
                       .requestMatchers("/api/seller/requests/**").hasRole("SELLER")

                       .requestMatchers("/admin/**").hasRole("ADMIN")
                       .requestMatchers("/seller/**").hasRole("SELLER")
                       .requestMatchers("/requests/**").authenticated()
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
