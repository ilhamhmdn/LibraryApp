package com.libraryapp.repository;

import com.libraryapp.entity.IsbnCatalog;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IsbnCatalogRepository extends JpaRepository<IsbnCatalog, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM IsbnCatalog c WHERE c.isbn = :isbn")
    Optional<IsbnCatalog> findByIdForUpdate(@Param("isbn") String isbn);
}
