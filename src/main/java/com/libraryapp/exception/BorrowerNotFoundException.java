package com.libraryapp.exception;

public class BorrowerNotFoundException extends ResourceNotFoundException {
    public BorrowerNotFoundException(Long id) {
        super("Borrower not found with ID: " + id);
    }
}
