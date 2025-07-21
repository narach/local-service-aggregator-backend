package com.service.sector.aggregator.config;

import com.service.sector.aggregator.filter.AdminJwtFilter;
import com.service.sector.aggregator.filter.RefreshingJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain apiChain(HttpSecurity http, RefreshingJwtFilter refreshingJwtFilter, AdminJwtFilter adminFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(refreshingJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(adminFilter, RefreshingJwtFilter.class);
        return http.build();
    }
}
