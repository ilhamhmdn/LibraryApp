package com.libraryapp.exception;

public class BookNotBorrowedException extends ConflictException {
    public BookNotBorrowedException(Long bookId) {
        super("Physical book copy with ID " + bookId + " is not currently borrowed.");
    }
}
