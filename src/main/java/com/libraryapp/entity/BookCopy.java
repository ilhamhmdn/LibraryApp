package com.libraryapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "isbn", nullable = false)
    private IsbnCatalog isbnCatalog;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public BookCopy() {
    }

    public BookCopy(IsbnCatalog isbnCatalog, Instant createdAt) {
        this.isbnCatalog = isbnCatalog;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IsbnCatalog getIsbnCatalog() {
        return isbnCatalog;
    }

    public void setIsbnCatalog(IsbnCatalog isbnCatalog) {
        this.isbnCatalog = isbnCatalog;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
