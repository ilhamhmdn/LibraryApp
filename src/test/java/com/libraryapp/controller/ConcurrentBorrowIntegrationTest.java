package com.libraryapp.controller;

import com.libraryapp.dto.request.RegisterBookRequest;
import com.libraryapp.dto.request.RegisterBorrowerRequest;
import com.libraryapp.dto.response.BookCopyResponse;
import com.libraryapp.dto.response.BorrowerResponse;
import com.libraryapp.exception.ConflictException;
import com.libraryapp.repository.ActiveLoanRepository;
import com.libraryapp.service.BookService;
import com.libraryapp.service.BorrowerService;
import com.libraryapp.service.BorrowingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConcurrentBorrowIntegrationTest {

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private ActiveLoanRepository activeLoanRepository;

    @Autowired
    private com.libraryapp.repository.LoanRepository loanRepository;

    @Autowired
    private com.libraryapp.repository.BookCopyRepository bookCopyRepository;

    @Autowired
    private com.libraryapp.repository.IsbnCatalogRepository isbnCatalogRepository;

    @Autowired
    private com.libraryapp.repository.BorrowerRepository borrowerRepository;

    @org.junit.jupiter.api.BeforeEach
    void cleanDatabase() {
        activeLoanRepository.deleteAll();
        loanRepository.deleteAll();
        bookCopyRepository.deleteAll();
        isbnCatalogRepository.deleteAll();
        borrowerRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrently borrowing the same physical copy allows exactly one success and rejects all competing threads")
    void shouldPermitOnlyOneSuccessfulBorrowUnderHighConcurrency() throws InterruptedException {
        int numberOfThreads = 10;
        List<Long> borrowerIds = new ArrayList<>();

        for (int i = 1; i <= numberOfThreads; i++) {
            BorrowerResponse borrower = borrowerService.registerBorrower(
                    new RegisterBorrowerRequest("Borrower " + i, "user" + i + "@example.com")
            );
            borrowerIds.add(borrower.getId());
        }

        BookCopyResponse book = bookService.registerBook(
                new RegisterBookRequest("978-0-13-468599-1", "Domain Modeling Primer", "Jane Miller")
        );
        Long bookCopyId = book.getId();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final Long borrowerId = borrowerIds.get(i);
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    borrowingService.borrowBook(borrowerId, bookCopyId);
                    successCount.incrementAndGet();
                } catch (ConflictException | DataIntegrityViolationException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        startLatch.countDown();
        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(numberOfThreads - 1);
        assertThat(unexpectedErrorCount.get()).isZero();
        assertThat(activeLoanRepository.existsByBookCopyId(bookCopyId)).isTrue();
    }
}
