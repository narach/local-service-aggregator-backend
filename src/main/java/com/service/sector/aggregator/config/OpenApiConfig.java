package com.service.sector.aggregator.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title       = "Local Service Aggregator API",
                version     = "v1",
                description = "CRUD for masters, bookings, etc.",
                contact     = @Contact(name = "API Support", email = "support@example.com")
        ),
        servers = {
                @Server(
                        url         = "http://localhost:8080",
                        description = "Local host"
                ),
                @Server(
                        url         = "https://aggregator.duckdns.org",
                        description = "EC2 instance"
                )
        }
)
public class OpenApiConfig {}
