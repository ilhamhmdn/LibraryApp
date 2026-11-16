package com.libraryapp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation failure details for a specific field")
public class FieldErrorDetail {

    @Schema(description = "Field name", example = "email")
    private String field;

    @Schema(description = "Failure message", example = "Borrower email must be a valid email address")
    private String message;

    public FieldErrorDetail() {
    }

    public FieldErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
