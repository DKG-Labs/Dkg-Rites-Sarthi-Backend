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
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/vendor/poData",
                                "/api/vendor/po-assigned",
                                "/api/vendor/proxy-pdf",
                                "/sarthi-backend/api/auth",
                                "/sarthi-backend/api/auth/login",
                                "/sarthi-backend/api/auth",
                                "/sarthi-backend/api/region-cluster/regions",
                                "/sarthi-backend/api/region-cluster/clusters/{regionName}",
                                "/sarthi-backend/api/ie-users/{clusterName}",
                                "/sarthi-backend/initiateWorkflow",
                                "/sarthi-backend/performTransitionAction",
                                "/sarthi-backend/allPendingWorkflowTransition",
                                "/sarthi-backend/allPendingQtyEditTransitions",
                                "/sarthi-backend/workflowTransitionHistory",
                                "/api/ibs/sarthi/authenticate",
                                // Raw Material APIs - temporarily public for testing
                                "/api/raw-material/**",
                                // Process Material APIs - temporarily public for testing
                                "/api/process-material/**",
                                // Final Material APIs - temporarily public for testing
                                "/api/final-material/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/ui",
                                "/configuration/security"
                        ).permitAll()

                        .requestMatchers(
                                "/api/ibs/callData",
                                "/api/ibs/acknowledge"
                        ).authenticated()
                        // All other requests - permit for now (can be changed to authenticated() later)
                        .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
