package com.libraryapp.exception;

public class IsbnMetadataMismatchException extends ConflictException {
    public IsbnMetadataMismatchException(String isbn, String expectedTitle, String expectedAuthor, String providedTitle, String providedAuthor) {
        super(String.format("ISBN '%s' is already registered with title '%s' and author '%s', but received title '%s' and author '%s'. All copies of an ISBN must share identical metadata.",
                isbn, expectedTitle, expectedAuthor, providedTitle, providedAuthor));
    }
}
