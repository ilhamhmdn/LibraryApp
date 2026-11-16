package com.libraryapp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a physical book copy")
public class BookCopyResponse {

    @Schema(description = "Unique identifier of the physical book copy", example = "1")
    private Long id;

    @Schema(description = "Canonical ISBN of the book", example = "9780134685991")
    private String isbn;

    @Schema(description = "Title of the book", example = "Effective Java")
    private String title;

    @Schema(description = "Author of the book", example = "Joshua Bloch")
    private String author;

    @Schema(description = "Current availability to be borrowed", example = "true")
    private boolean available;

    public BookCopyResponse() {
    }

    public BookCopyResponse(Long id, String isbn, String title, String author, boolean available) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
