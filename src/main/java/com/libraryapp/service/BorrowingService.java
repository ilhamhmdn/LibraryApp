package com.libraryapp.service;

import com.libraryapp.dto.response.LoanResponse;
import com.libraryapp.entity.ActiveLoan;
import com.libraryapp.entity.BookCopy;
import com.libraryapp.entity.Borrower;
import com.libraryapp.entity.Loan;
import com.libraryapp.exception.BookAlreadyBorrowedException;
import com.libraryapp.exception.BookNotBorrowedException;
import com.libraryapp.exception.BookNotFoundException;
import com.libraryapp.exception.BorrowerMismatchException;
import com.libraryapp.exception.BorrowerNotFoundException;
import com.libraryapp.repository.ActiveLoanRepository;
import com.libraryapp.repository.BookCopyRepository;
import com.libraryapp.repository.BorrowerRepository;
import com.libraryapp.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BorrowingService {

    private final BorrowerRepository borrowerRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LoanRepository loanRepository;
    private final ActiveLoanRepository activeLoanRepository;

    public BorrowingService(BorrowerRepository borrowerRepository,
                            BookCopyRepository bookCopyRepository,
                            LoanRepository loanRepository,
                            ActiveLoanRepository activeLoanRepository) {
        this.borrowerRepository = borrowerRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.loanRepository = loanRepository;
        this.activeLoanRepository = activeLoanRepository;
    }

    @Transactional
    public LoanResponse borrowBook(Long borrowerId, Long bookId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new BorrowerNotFoundException(borrowerId));

        BookCopy bookCopy = bookCopyRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (activeLoanRepository.findByBookCopyIdForUpdate(bookId).isPresent()) {
            throw new BookAlreadyBorrowedException(bookId);
        }

        Loan loan = new Loan(bookCopy, borrower, Instant.now());
        Loan savedLoan = loanRepository.save(loan);

        activeLoanRepository.save(new ActiveLoan(bookCopy.getId(), savedLoan.getId()));

        return new LoanResponse(
                savedLoan.getId(),
                borrower.getId(),
                bookCopy.getId(),
                savedLoan.getBorrowedAt(),
                savedLoan.getReturnedAt()
        );
    }

    @Transactional
    public LoanResponse returnBook(Long borrowerId, Long bookId) {
        if (!borrowerRepository.existsById(borrowerId)) {
            throw new BorrowerNotFoundException(borrowerId);
        }

        if (!bookCopyRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        ActiveLoan activeLoan = activeLoanRepository.findByBookCopyIdForUpdate(bookId)
                .orElseThrow(() -> new BookNotBorrowedException(bookId));

        Loan loan = loanRepository.findById(activeLoan.getLoanId())
                .orElseThrow(() -> new IllegalStateException("Active loan references non-existent loan record: " + activeLoan.getLoanId()));

        if (!loan.getBorrower().getId().equals(borrowerId)) {
            throw new BorrowerMismatchException(borrowerId, bookId);
        }

        loan.setReturnedAt(Instant.now());
        activeLoanRepository.delete(activeLoan);

        return new LoanResponse(
                loan.getId(),
                borrowerId,
                bookId,
                loan.getBorrowedAt(),
                loan.getReturnedAt()
        );
    }

    @Transactional(readOnly = true)
    public LoanResponse getActiveLoan(Long borrowerId, Long bookId) {
        if (!borrowerRepository.existsById(borrowerId)) {
            throw new BorrowerNotFoundException(borrowerId);
        }
        if (!bookCopyRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        ActiveLoan activeLoan = activeLoanRepository.findByBookCopyId(bookId)
                .orElseThrow(() -> new BookNotBorrowedException(bookId));

        Loan loan = loanRepository.findById(activeLoan.getLoanId())
                .orElseThrow(() -> new IllegalStateException("Active loan references non-existent loan record: " + activeLoan.getLoanId()));

        if (!loan.getBorrower().getId().equals(borrowerId)) {
            throw new BorrowerMismatchException(borrowerId, bookId);
        }

        return new LoanResponse(
                loan.getId(),
                borrowerId,
                bookId,
                loan.getBorrowedAt(),
                loan.getReturnedAt()
        );
    }
}
