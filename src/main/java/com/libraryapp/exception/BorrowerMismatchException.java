package com.libraryapp.exception;

public class BorrowerMismatchException extends ConflictException {
    public BorrowerMismatchException(Long borrowerId, Long bookId) {
        super("Physical book copy with ID " + bookId + " is not borrowed by borrower with ID " + borrowerId + ".");
    }
}
