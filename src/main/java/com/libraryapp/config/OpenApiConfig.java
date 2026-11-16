package com.libraryapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .version("1.0.0")
                        .description("RESTful API for the Library Management System. "
                                + "Manages borrowers, physical book copy inventory, ISBN catalogue metadata, "
                                + "and transaction-safe book borrowing and returns. "
                                + "\n\n**Key Concepts:**"
                                + "\n- **ISBN vs. Book ID:** An ISBN identifies a book title/author catalogue entry. "
                                + "The `bookId` uniquely identifies an individual physical copy. Multiple physical copies can share the same ISBN."
                                + "\n- **Single Active Borrower Invariant:** A physical copy can only be borrowed by one borrower at any given time. "
                                + "Concurrent borrow attempts are protected via database-level pessimistic locking and relational constraints."
                                + "\n- **Borrow & Return Flow:** A borrower borrows a physical copy using their borrower ID and the book ID. "
                                + "The same borrower returns the copy, restoring its availability for other borrowers.")
                        .contact(new Contact().name("Library Management Team"))
                        .license(new License().name("Apache 2.0")));
    }
}
