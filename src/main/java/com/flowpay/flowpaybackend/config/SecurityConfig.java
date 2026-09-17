package com.flowpay.flowpaybackend.config;

import com.flowpay.flowpaybackend.service.SellerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private SellerUserDetailsService sellerUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .userDetailsService(sellerUserDetailsService)

            .authorizeHttpRequests(auth -> auth

                // Public pages
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html"
                ).permitAll()

                // Public customer payment APIs
                .requestMatchers(
                    "/order/payment/**",
                    "/order/qr/**"
                ).permitAll()

                // Seller pages and APIs
                .requestMatchers(
                    "/dashboard.html",
                    "/create-order.html",
                    "/order-details.html",
                    "/order/**"
                ).authenticated()

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard.html", true)
                .failureUrl("/login.html?error=true")
                .permitAll()
            )

            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}