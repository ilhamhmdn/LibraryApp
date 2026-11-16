package com.libraryapp.exception;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String email) {
        super("A borrower with email '" + email + "' already exists.");
    }
}
