package com.gynaid.backend.config;

import com.gynaid.backend.security.JwtAuthenticationFilter;
import com.gynaid.backend.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, RateLimitingFilter rateLimitingFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔒 SECURITY: Enable CSRF protection (was disabled - CRITICAL VULNERABILITY)
            .csrf(csrf -> csrf
                .csrfTokenRepository(createCsrfTokenRepository())
                .ignoringRequestMatchers("/api/webhooks/**", "/api/auth/login", "/api/auth/register", "/h2-console/**")
            )
            
            // 🔒 CORS Configuration - Secure allowed origins
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 🔒 Session Management - Stateless with secure session handling
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 🔒 Authorization Rules - Default to authentication required
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - authentication not required
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/verify-email",
                    "/api/auth/reset-password",
                    "/api/health",
                    "/api/placeholder/**",
                    "/api/webhooks/**",
                    "/actuator/health",
                    "/error",
                    "/h2-console/**"
                ).permitAll()
                
                // 🔒 Protected endpoints - authentication required
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/provider/**").hasAnyRole("PROVIDER_INDIVIDUAL", "PROVIDER_INSTITUTION")
                .requestMatchers("/api/client/**").hasRole("CLIENT")
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/consultations/**").authenticated()
                .requestMatchers("/api/payments/**").authenticated()
                .requestMatchers("/api/health-profile/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // 🔒 Security Headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny()) // Prevent clickjacking
                .contentTypeOptions(contentType -> contentType.disable())
                .httpStrictTransportSecurity(hstsConfig ->
                    hstsConfig.maxAgeInSeconds(31536000))
                .referrerPolicy(referrer -> referrer.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            
            // 🔒 Authentication Provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    private CookieCsrfTokenRepository createCsrfTokenRepository() {
        // FIXED: Enable CSRF protection with secure configuration
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieMaxAge(-1); // Session-based (deleted when browser closes)
        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 🔒 ENVIRONMENT-AWARE: Allow appropriate origins based on environment
        List<String> allowedOrigins;
        String environment = System.getProperty("spring.profiles.active", "development");
        
        if ("production".equals(environment)) {
            // Production: Only production domains
            allowedOrigins = Arrays.asList(
                "https://gynaid.com",
                "https://app.gynaid.com"
            );
        } else {
            // Development/Staging: Include localhost for development
            allowedOrigins = Arrays.asList(
                "https://gynaid.com",
                "https://app.gynaid.com",
                "http://localhost:3000",  // React dev server
                "http://localhost:5173",  // Vite dev server
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
            );
        }
        
        configuration.setAllowedOrigins(allowedOrigins);
        
        // 🔒 SECURE: Restrict allowed methods to essential ones
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 🔒 SECURE: Restrict allowed headers (was "*" - CRITICAL VULNERABILITY)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-CSRF-Token",
            "X-Request-ID",
            "Accept",
            "Origin",
            "Cache-Control"
        ));
        
        // 🔒 SECURE: Expose only necessary headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Request-ID",
            "X-CSRF-Token"
        ));
        
        // 🔒 SECURE: Allow credentials only from allowed origins
        configuration.setAllowCredentials(true);
        
        // 🔒 SECURE: Limit preflight cache time
        configuration.setMaxAge(1800L); // 30 minutes
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
