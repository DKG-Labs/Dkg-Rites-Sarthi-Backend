package com.sarthi.config;

import com.sarthi.util.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(java.util.List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.ritesqasarthi.com",
                "https://*.azurewebsites.net",
                "https://*.vercel.app"
        ));
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addExposedHeader("*");
        
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(contentType -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' http://localhost:* https://*.ritesqasarthi.com https://*.azurewebsites.net https://*.vercel.app")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Allow all preflight CORS OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Public Authentication & Token Exchange endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/login/**",
                                "/api/auth/loginBasedOnType",
                                "/api/auth/verifyOtp",
                                "/api/auth/forgot-password",
                                "/sarthi-backend/api/auth/login",
                                "/api/sarthi/authenticate",
                                "/api/ibs/sarthi/authenticate"
                        ).permitAll()

                        // 3. API Documentation (Swagger / OpenAPI)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/actuator/health"
                        ).permitAll()

                        // 4. Public File & Certificate Viewing, Vendor PO and Plant Lookups
                        .requestMatchers(
                                "/api/vendor/poData",
                                "/api/vendor/po-data",
                                "/api/vendor/po-assigned",
                                "/api/railpad-vendor-plant/**",
                                "/api/vendor-plant/**",
                                "/api/filters/**",
                                "/api/vendor/proxy-pdf",
                                "/vendor/proxy-pdf",
                                "/api/certificate-storage/view",
                                "/api/certificate-storage/view/**",
                                "/api/correction-slip/view-pdf/**",
                                "/api/correction-slip/download-pdf/**",
                                "/api/ic-annexures/list",
                                "/api/ic-annexures/download/**",
                                "/api/case-letter/**",
                                "/api/images/**",
                                "/dashboard/images/**",
                                "/api/sleeper-dashboard/**",
                                "/api/reports/**",
                                "/api/ibs/**",
                                "/api/Vendorsync/**"
                        ).permitAll()

                        // 5. ALL OTHER APIS REQUIRE STRICT TOKEN AUTHENTICATION
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"responseStatus\":{\"statusCode\":401,\"message\":\"Unauthorized: Authentication token is missing, invalid, or expired\"}}");
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
