package com.libraryapp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for registering a physical book copy")
public class RegisterBookRequest {

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN input must not exceed 20 characters")
    @Schema(description = "ISBN-10 or ISBN-13 identifier", example = "978-0-13-468599-1")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the book", example = "Effective Java")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    @Schema(description = "Author of the book", example = "Joshua Bloch")
    private String author;

    public RegisterBookRequest() {
    }

    public RegisterBookRequest(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
