package com.service.sector.aggregator.filter;

import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.service.UserService;
import com.service.sector.aggregator.service.external.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshingJwtFilter extends OncePerRequestFilter {

    private static final Duration REFRESH_THRESHOLD = Duration.ofHours(24);

    private final JwtService jwtService;
    private final AppUserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String authHdr = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHdr == null || !authHdr.startsWith("Bearer ")) {
            chain.doFilter(req, resp);
            return;
        }

        Jws<Claims> jws = jwtService.getClaims(authHdr);
        Claims claims = jws.getPayload();

        // Check if token expires soon
        Instant exp = claims.getExpiration().toInstant();
        if (Duration.between(Instant.now(), exp).compareTo(REFRESH_THRESHOLD) < 0) {
            Long userId = claims.get("uid", Long.class);
            AppUser appUser = userRepo.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            String newToken = jwtService.generateToken(appUser);
            resp.setHeader("X-New-Token", newToken);      // or overwrite “Authorization”
            resp.setHeader("Access-Control-Expose-Headers", "X-New-Token");
        }

        chain.doFilter(req, resp);
    }
}
