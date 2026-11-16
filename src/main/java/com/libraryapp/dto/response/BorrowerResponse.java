package com.libraryapp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Details of a registered borrower")
public class BorrowerResponse {

    @Schema(description = "Unique borrower identifier", example = "1")
    private Long id;

    @Schema(description = "Full name of the borrower", example = "Alice Smith")
    private String name;

    @Schema(description = "Email address of the borrower", example = "alice.smith@example.com")
    private String email;

    @Schema(description = "Registration timestamp in UTC", example = "2026-11-15T15:30:00Z")
    private Instant createdAt;

    public BorrowerResponse() {
    }

    public BorrowerResponse(Long id, String name, String email, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
