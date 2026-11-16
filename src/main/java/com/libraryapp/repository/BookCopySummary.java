package com.libraryapp.repository;

public interface BookCopySummary {
    Long getId();
    String getIsbn();
    String getTitle();
    String getAuthor();
    boolean isAvailable();
}
