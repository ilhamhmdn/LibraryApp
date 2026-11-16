package com.libraryapp.service;

import com.libraryapp.dto.response.LoanResponse;
import com.libraryapp.entity.ActiveLoan;
import com.libraryapp.entity.BookCopy;
import com.libraryapp.entity.Borrower;
import com.libraryapp.entity.IsbnCatalog;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ActiveLoanRepository activeLoanRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private Borrower borrower1;
    private Borrower borrower2;
    private BookCopy bookCopy;

    @BeforeEach
    void setUp() {
        borrower1 = new Borrower("Alice", "alice@example.com", "alice@example.com", Instant.now());
        borrower1.setId(1L);

        borrower2 = new Borrower("Bob", "bob@example.com", "bob@example.com", Instant.now());
        borrower2.setId(2L);

        IsbnCatalog catalog = new IsbnCatalog("9780134685991", "Domain Modeling Primer", "Jane Miller", Instant.now());
        bookCopy = new BookCopy(catalog, Instant.now());
        bookCopy.setId(10L);
    }

    @Test
    @DisplayName("Should successfully borrow an available physical copy")
    void shouldBorrowAvailableBookSuccessfully() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower1));
        when(bookCopyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(bookCopy));
        when(activeLoanRepository.findByBookCopyIdForUpdate(10L)).thenReturn(Optional.empty());

        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> {
            Loan loan = i.getArgument(0);
            loan.setId(100L);
            return loan;
        });

        LoanResponse response = borrowingService.borrowBook(1L, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBorrowerId()).isEqualTo(1L);
        assertThat(response.getBookCopyId()).isEqualTo(10L);
        assertThat(response.getReturnedAt()).isNull();

        verify(loanRepository).save(any(Loan.class));
        verify(activeLoanRepository).save(any(ActiveLoan.class));
    }

    @Test
    @DisplayName("Should reject borrow when borrower does not exist")
    void shouldRejectBorrowWhenBorrowerNotFound() {
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.borrowBook(99L, 10L))
                .isInstanceOf(BorrowerNotFoundException.class);

        verify(activeLoanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject borrow when book copy does not exist")
    void shouldRejectBorrowWhenBookNotFound() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower1));
        when(bookCopyRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.borrowBook(1L, 99L))
                .isInstanceOf(BookNotFoundException.class);

        verify(activeLoanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject borrow when book is already borrowed by another borrower")
    void shouldRejectBorrowWhenAlreadyBorrowed() {
        when(borrowerRepository.findById(2L)).thenReturn(Optional.of(borrower2));
        when(bookCopyRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(bookCopy));
        when(activeLoanRepository.findByBookCopyIdForUpdate(10L))
                .thenReturn(Optional.of(new ActiveLoan(10L, 50L)));

        assertThatThrownBy(() -> borrowingService.borrowBook(2L, 10L))
                .isInstanceOf(BookAlreadyBorrowedException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully return a borrowed book by the active borrower")
    void shouldReturnBookSuccessfully() {
        when(borrowerRepository.existsById(1L)).thenReturn(true);
        when(bookCopyRepository.existsById(10L)).thenReturn(true);

        ActiveLoan activeLoan = new ActiveLoan(10L, 100L);
        when(activeLoanRepository.findByBookCopyIdForUpdate(10L)).thenReturn(Optional.of(activeLoan));

        Loan loan = new Loan(bookCopy, borrower1, Instant.now().minusSeconds(3600));
        loan.setId(100L);
        when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));

        LoanResponse response = borrowingService.returnBook(1L, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBorrowerId()).isEqualTo(1L);
        assertThat(response.getBookCopyId()).isEqualTo(10L);
        assertThat(response.getReturnedAt()).isNotNull();

        verify(activeLoanRepository).delete(activeLoan);
    }

    @Test
    @DisplayName("Should reject return when book is not currently borrowed")
    void shouldRejectReturnWhenBookNotBorrowed() {
        when(borrowerRepository.existsById(1L)).thenReturn(true);
        when(bookCopyRepository.existsById(10L)).thenReturn(true);
        when(activeLoanRepository.findByBookCopyIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.returnBook(1L, 10L))
                .isInstanceOf(BookNotBorrowedException.class);

        verify(activeLoanRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should reject return when book was borrowed by a different borrower")
    void shouldRejectReturnWhenBorrowerMismatch() {
        when(borrowerRepository.existsById(2L)).thenReturn(true);
        when(bookCopyRepository.existsById(10L)).thenReturn(true);

        ActiveLoan activeLoan = new ActiveLoan(10L, 100L);
        when(activeLoanRepository.findByBookCopyIdForUpdate(10L)).thenReturn(Optional.of(activeLoan));

        Loan loan = new Loan(bookCopy, borrower1, Instant.now().minusSeconds(3600)); // borrowed by borrower1, not 2
        loan.setId(100L);
        when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> borrowingService.returnBook(2L, 10L))
                .isInstanceOf(BorrowerMismatchException.class);

        verify(activeLoanRepository, never()).delete(any());
    }
}
