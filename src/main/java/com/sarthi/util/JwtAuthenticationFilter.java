package com.sarthi.util;

import com.sarthi.service.Impl.UserServiceImpl;
import com.sarthi.service.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private com.sarthi.repository.UserMasterRepository userMasterRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // CASE 1: No Authorization header → allow request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // CASE 2: Authorization exists → extract token
        String token = authHeader.substring(7).trim();

        // CASE 2A: Support frontend mock development tokens (CM, CallDesk, Finance, SMS, Railpad-IE, Railwayboard)
        if (token != null && (token.startsWith("cm-mock-token") ||
                              token.startsWith("calldesk-mock-token") ||
                              token.startsWith("sms-mock-token") ||
                              token.startsWith("finance-mock-token") ||
                              token.startsWith("railpad-mock-token") ||
                              token.startsWith("sleeper-vendor-token") ||
                              token.startsWith("railwayboard-mock-token"))) {

            String role = "CM";
            String username = "Cm";
            if (token.startsWith("calldesk-")) { role = "CALL_DESK"; username = "CallDesk"; }
            else if (token.startsWith("sms-")) { role = "SMS"; username = "Rail SMS"; }
            else if (token.startsWith("finance-")) { role = "FINANCE"; username = "Finance"; }
            else if (token.startsWith("railpad-")) { role = "RAILPAD_IE"; username = "Railpad-IE"; }
            else if (token.startsWith("railwayboard-")) { role = "RAILWAY_BOARD"; username = "Railwayboard"; }
            else if (token.startsWith("sleeper-vendor-")) { role = "SLEEPER_VENDOR"; username = "Sleeper Vendor"; }

            org.springframework.security.core.userdetails.User mockUser =
                    new org.springframework.security.core.userdetails.User(
                            username,
                            "",
                            java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
            return;
        }

        String subject = null;

        try {
            subject = jwtService.extractUserId(token);
        } catch (Exception e) {
            // Invalid token → do NOT block → allow request (same as Rites project)
            filterChain.doFilter(request, response);
            return;
        }

        // CASE 3: Token valid → authenticate user
        if (subject != null && !subject.trim().isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                com.sarthi.entity.UserMaster user = null;

                // 1. Try finding by numeric userId
                try {
                    Integer uId = Integer.valueOf(subject);
                    user = userMasterRepository.findByUserId(uId).orElse(null);
                } catch (NumberFormatException ignored) {}

                // 2. Try finding by userName or employeeCode
                if (user == null) {
                    user = userMasterRepository.findFirstByUserName(subject).orElse(null);
                    if (user == null) {
                        user = userMasterRepository.findFirstByEmployeeCode(subject).orElse(null);
                    }
                }

                // 3. Try colon prefix variants for vendor codes (:1007406 vs 1007406)
                if (user == null) {
                    if (subject.startsWith(":")) {
                        String stripped = subject.substring(1);
                        user = userMasterRepository.findFirstByUserName(stripped).orElse(null);
                        if (user == null) {
                            user = userMasterRepository.findFirstByEmployeeCode(stripped).orElse(null);
                        }
                    } else {
                        String coloed = ":" + subject;
                        user = userMasterRepository.findFirstByUserName(coloed).orElse(null);
                        if (user == null) {
                            user = userMasterRepository.findFirstByEmployeeCode(coloed).orElse(null);
                        }
                    }
                }

                if (user != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Valid signed JWT token from Sarthi backend: create authenticated principal
                    org.springframework.security.core.userdetails.User principal =
                            new org.springframework.security.core.userdetails.User(
                                    subject,
                                    "",
                                    java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                            );
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.warn("Authentication error for token subject " + subject + ": " + e.getMessage());
            }
        }

        // Continue request
        filterChain.doFilter(request, response);
    }



  /*  @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        Integer userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                userId = Integer.valueOf(jwtService.extractUserId(token));
            } catch (Exception e) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userServiceImpl.loadUserByUsername(userId);

            if (jwtService.isValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }*/

    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.equals("/login") ||
                requestPath.startsWith("/v3/api-docs") ||
                requestPath.startsWith("/swagger-ui") ||
                requestPath.startsWith("/dashboard/images") ||
                requestPath.startsWith("/api/images") ||
                requestPath.startsWith("/public");
    }
}
