package com.sam.bankmanagement.Bank.Management.Application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort + contextPath);
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("support@blessonsam.com");
        contact.setName("Blesson Sam Team");
        contact.setUrl("https://sam.com");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Mini Bank Management System API")
                .version("v1.0")
                .contact(contact)
                .description("A comprehensive bank management system with customer management, account management, transactions, and interest calculations. " +
                        "This API provides endpoints for creating and managing customers, opening different types of accounts (Savings and Current), " +
                        "performing transactions (deposits, withdrawals, transfers), and handling interest calculations.")
                .termsOfService("https://edforce.com/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
