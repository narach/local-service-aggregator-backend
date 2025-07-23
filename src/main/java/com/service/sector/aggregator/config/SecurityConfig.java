package com.service.sector.aggregator.config;

import com.service.sector.aggregator.filter.AdminJwtFilter;
import com.service.sector.aggregator.filter.RefreshingJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Chain for admin endpoints – runs both filters.
     */
    @Bean
    @Order(1)
    SecurityFilterChain adminChain(HttpSecurity http,
                                   RefreshingJwtFilter refreshingJwtFilter,
                                   AdminJwtFilter adminFilter) throws Exception {
        http
                .securityMatcher("/api/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .addFilterBefore(refreshingJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminFilter, RefreshingJwtFilter.class);

        return http.build();
    }


    /**
     * Chain for all other requests – runs only the RefreshingJwtFilter.
     */
    @Bean
    @Order(2)
    SecurityFilterChain defaultChain(HttpSecurity http,
                                     RefreshingJwtFilter refreshingJwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .addFilterBefore(refreshingJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
