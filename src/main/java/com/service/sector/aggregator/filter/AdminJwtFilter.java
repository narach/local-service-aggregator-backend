package com.service.sector.aggregator.filter;

import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.service.external.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class AdminJwtFilter extends OncePerRequestFilter {

    private final AppUserRepository userRepo;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Apply filter only to "/api/admin/*" endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Bearer token required");
            return;
        }

        Long uid = jwtService.extractUserId(auth);
        AppUser user = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!user.isAdmin()) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Admin role required");
            return;
        }

        // Mark request as authenticated so later security rules pass
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
