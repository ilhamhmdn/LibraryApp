package com.libraryapp.repository;

import com.libraryapp.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByBookCopyIdOrderByBorrowedAtDesc(Long bookCopyId);

    List<Loan> findByBorrowerIdOrderByBorrowedAtDesc(Long borrowerId);
}
