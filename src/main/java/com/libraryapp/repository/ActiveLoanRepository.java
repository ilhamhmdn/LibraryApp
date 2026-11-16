package com.libraryapp.repository;

import com.libraryapp.entity.ActiveLoan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActiveLoanRepository extends JpaRepository<ActiveLoan, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ActiveLoan a WHERE a.bookCopyId = :bookCopyId")
    Optional<ActiveLoan> findByBookCopyIdForUpdate(@Param("bookCopyId") Long bookCopyId);

    Optional<ActiveLoan> findByBookCopyId(Long bookCopyId);

    boolean existsByBookCopyId(Long bookCopyId);

    Optional<ActiveLoan> findByLoanId(Long loanId);
}
