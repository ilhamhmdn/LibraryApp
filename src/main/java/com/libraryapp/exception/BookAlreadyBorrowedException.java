package com.libraryapp.exception;

public class BookAlreadyBorrowedException extends ConflictException {
    public BookAlreadyBorrowedException(Long bookId) {
        super("Physical book copy with ID " + bookId + " is currently borrowed by another borrower.");
    }
}
