package com.libraryapp.repository;

import com.libraryapp.entity.BookCopy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BookCopy b JOIN FETCH b.isbnCatalog WHERE b.id = :id")
    Optional<BookCopy> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT b.id AS id, c.isbn AS isbn, c.title AS title, c.author AS author, " +
            "(CASE WHEN a.bookCopyId IS NULL THEN true ELSE false END) AS available " +
            "FROM BookCopy b " +
            "JOIN b.isbnCatalog c " +
            "LEFT JOIN ActiveLoan a ON b.id = a.bookCopyId " +
            "ORDER BY b.id ASC")
    List<BookCopySummary> findAllSummaries();

    @Query("SELECT b FROM BookCopy b JOIN FETCH b.isbnCatalog WHERE b.id = :id")
    Optional<BookCopy> findByIdWithCatalog(@Param("id") Long id);
}
