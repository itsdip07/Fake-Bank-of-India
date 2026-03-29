package com.example.bankapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.bankapp.service.AccountService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        AccountService accountService;

        @Bean
        static PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/register", "/login", "/otp/verify",
                                                                "/otp/resend", "/privacy-policy",
                                                                "/termsofservice", "/access-denied",
                                                                "/ex_products_page.html", "/support_page.html",
                                                                "/ai_banking.html", "/current_acc.html",
                                                                "/savings_acc.html",
                                                                "/demat.html", "/fixed_deposit.html",
                                                                "/credit_cards.html",
                                                                "/personal_loans.html", "/home_loans.html",
                                                                "/vehicle_loans.html",
                                                                "/investments.html", "/groww.html", "debit.html")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedPage("/access-denied"))
                                .headers(header -> header.frameOptions(frameoptions -> frameoptions.sameOrigin()));

                return http.build();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(accountService).passwordEncoder(passwordEncoder());
        }
}