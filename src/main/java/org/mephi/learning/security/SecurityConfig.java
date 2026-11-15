package org.mephi.learning.security;

import org.mephi.learning.entity.User;
import org.mephi.learning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return email -> {
            User user =
                    users.findByEmail(email.trim().toLowerCase())
                            .orElseThrow(() -> new UsernameNotFoundException("Unknown email"));
            if (user.getPasswordHash() == null) {
                throw new UsernameNotFoundException("Password is not configured");
            }
            return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth -> {
                            if (!securityEnabled) {
                                auth.anyRequest().permitAll();
                                return;
                            }
                            auth.requestMatchers("/api/auth/**", "/error")
                                    .permitAll()
                                    .requestMatchers(
                                            HttpMethod.GET,
                                            "/api/courses/**",
                                            "/api/categories",
                                            "/api/tags")
                                    .permitAll()
                                    .requestMatchers(
                                            HttpMethod.POST,
                                            "/api/courses/**",
                                            "/api/modules/**",
                                            "/api/lessons/**",
                                            "/api/categories",
                                            "/api/tags",
                                            "/api/quizzes/**")
                                    .hasAnyRole("TEACHER", "ADMIN")
                                    .requestMatchers(
                                            HttpMethod.PUT,
                                            "/api/courses/**",
                                            "/api/modules/**",
                                            "/api/lessons/**",
                                            "/api/assignments/**",
                                            "/api/materials/**",
                                            "/api/quizzes/**",
                                            "/api/categories/**",
                                            "/api/tags/**")
                                    .hasAnyRole("TEACHER", "ADMIN")
                                    .requestMatchers(HttpMethod.DELETE, "/api/**")
                                    .hasRole("ADMIN")
                                    .requestMatchers(HttpMethod.POST, "/api/users")
                                    .hasRole("ADMIN")
                                    .requestMatchers("/api/users/**")
                                    .authenticated()
                                    .requestMatchers("/api/**")
                                    .authenticated()
                                    .anyRequest()
                                    .permitAll();
                        })
                .exceptionHandling(
                        exceptions ->
                                exceptions.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .logout(
                        logout ->
                                logout.logoutUrl("/api/auth/logout")
                                        .invalidateHttpSession(true)
                                        .deleteCookies("JSESSIONID"));
        return http.build();
    }
}
