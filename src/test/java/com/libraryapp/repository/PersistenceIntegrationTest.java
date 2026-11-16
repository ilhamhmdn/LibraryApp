package com.libraryapp.repository;

import com.libraryapp.entity.ActiveLoan;
import com.libraryapp.entity.BookCopy;
import com.libraryapp.entity.Borrower;
import com.libraryapp.entity.IsbnCatalog;
import com.libraryapp.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class PersistenceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private IsbnCatalogRepository isbnCatalogRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ActiveLoanRepository activeLoanRepository;

    @Test
    @DisplayName("Should persist borrower and enforce unique normalized email")
    void shouldEnforceUniqueNormalizedEmail() {
        Borrower b1 = new Borrower("Alice Smith", "alice@example.com", "alice@example.com", Instant.now());
        borrowerRepository.saveAndFlush(b1);

        Borrower b2 = new Borrower("Alice Duplicate", "ALICE@example.com", "alice@example.com", Instant.now());
        assertThatThrownBy(() -> borrowerRepository.saveAndFlush(b2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should persist multiple physical copies for the same ISBN with distinct IDs")
    void shouldPersistMultiplePhysicalCopiesForSameIsbn() {
        IsbnCatalog catalog = new IsbnCatalog("9780134685991", "Domain Modeling Primer", "Jane Miller", Instant.now());
        isbnCatalogRepository.saveAndFlush(catalog);

        BookCopy copy1 = new BookCopy(catalog, Instant.now());
        BookCopy copy2 = new BookCopy(catalog, Instant.now());

        bookCopyRepository.saveAndFlush(copy1);
        bookCopyRepository.saveAndFlush(copy2);

        assertThat(copy1.getId()).isNotNull();
        assertThat(copy2.getId()).isNotNull();
        assertThat(copy1.getId()).isNotEqualTo(copy2.getId());
        assertThat(copy1.getIsbnCatalog().getIsbn()).isEqualTo(copy2.getIsbnCatalog().getIsbn());

        List<BookCopySummary> summaries = bookCopyRepository.findAllSummaries();
        assertThat(summaries).hasSize(2);
        assertThat(summaries).allMatch(BookCopySummary::isAvailable);
    }

    @Test
    @DisplayName("Should prevent multiple active loans on the same physical copy at the database level")
    void shouldEnforceSingleActiveLoanPerCopy() {
        Borrower borrower1 = borrowerRepository.saveAndFlush(
                new Borrower("User One", "user1@example.com", "user1@example.com", Instant.now()));
        Borrower borrower2 = borrowerRepository.saveAndFlush(
                new Borrower("User Two", "user2@example.com", "user2@example.com", Instant.now()));

        IsbnCatalog catalog = isbnCatalogRepository.saveAndFlush(
                new IsbnCatalog("9780132350884", "Software Architecture In Depth", "Alex Rivera", Instant.now()));

        BookCopy copy = bookCopyRepository.saveAndFlush(new BookCopy(catalog, Instant.now()));

        Loan loan1 = loanRepository.saveAndFlush(new Loan(copy, borrower1, Instant.now()));
        activeLoanRepository.saveAndFlush(new ActiveLoan(copy.getId(), loan1.getId()));

        entityManager.clear();

        Loan loan2 = loanRepository.saveAndFlush(new Loan(copy, borrower2, Instant.now()));
        ActiveLoan conflictingActiveLoan = new ActiveLoan(copy.getId(), loan2.getId());

        assertThatThrownBy(() -> {
            activeLoanRepository.saveAndFlush(conflictingActiveLoan);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
