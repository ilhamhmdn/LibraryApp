package com.libraryapp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for registering a new borrower")
public class RegisterBorrowerRequest {

    @NotBlank(message = "Borrower name is required")
    @Size(max = 120, message = "Borrower name must not exceed 120 characters")
    @Schema(description = "Full name of the borrower", example = "Alice Smith")
    private String name;

    @NotBlank(message = "Borrower email is required")
    @Email(message = "Borrower email must be a valid email address")
    @Size(max = 254, message = "Borrower email must not exceed 254 characters")
    @Schema(description = "Email address of the borrower (unique)", example = "alice.smith@example.com")
    private String email;

    public RegisterBorrowerRequest() {
    }

    public RegisterBorrowerRequest(String name, String email) {
        this.name = name;
        this.email = email;
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
}
