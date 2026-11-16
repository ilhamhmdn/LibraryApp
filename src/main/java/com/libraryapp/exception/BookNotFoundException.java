package com.libraryapp.exception;

public class BookNotFoundException extends ResourceNotFoundException {
    public BookNotFoundException(Long id) {
        super("Physical book copy not found with ID: " + id);
    }
}
