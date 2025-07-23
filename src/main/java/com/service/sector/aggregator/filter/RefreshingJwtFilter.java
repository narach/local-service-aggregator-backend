package com.service.sector.aggregator.filter;

import com.service.sector.aggregator.config.JwtProperties;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.service.external.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;


@Component
@RequiredArgsConstructor
public class RefreshingJwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository userRepo;
    private final JwtProperties props;

    @Override
    protected void doFilterInternal(HttpServletRequest req
            , HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {

        // Extract token header
        String bearer = extractBearer(req);

        // Bypass if request doesn't have a header
        if (bearer == null) {
            chain.doFilter(req, resp);
            return;
        }

        try {
            Jws<Claims> jws = jwtService.getClaims(bearer);
            Claims claims = jws.getPayload();

            Instant exp = claims.getExpiration().toInstant();

            // If token already expired - throw 401 Unauthorized exception
            if (Duration.between(Instant.now(), exp).compareTo(Duration.ZERO) < 0) {
                resp.sendError(HttpStatus.UNAUTHORIZED.value(), "Auth token expired");
                return;
            }

            // Check if token expires soon
            if (Duration.between(Instant.now(), exp).compareTo(props.getRefreshThreshold()) < 0) {
                Long userId = claims.get("uid", Long.class);
                AppUser appUser = userRepo.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                String newToken = jwtService.generateToken(appUser);
                resp.setHeader("X-New-Token", newToken);      // or overwrite “Authorization”
                resp.setHeader("Access-Control-Expose-Headers", "X-New-Token");
            }
        } catch (Exception e) {
            resp.sendError(HttpStatus.UNAUTHORIZED.value(), "Auth token expired");
            return;
        }

        chain.doFilter(req, resp);
    }

    /**
     * Returns the complete <code>"Bearer &lt;jwt&gt;"</code> string or {@code null}
     * when no token is present.
     */
    private String extractBearer(HttpServletRequest req) throws IOException {

        /* 1) HEADER -------------------------------------------------------- */
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(header) && header.startsWith("Bearer ")) {
            return header;
        }
        return null;
    }

}
