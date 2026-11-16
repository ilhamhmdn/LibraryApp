package com.libraryapp.service;

import com.libraryapp.dto.request.RegisterBookRequest;
import com.libraryapp.dto.response.BookCopyResponse;
import com.libraryapp.entity.BookCopy;
import com.libraryapp.entity.IsbnCatalog;
import com.libraryapp.exception.BookNotFoundException;
import com.libraryapp.exception.IsbnMetadataMismatchException;
import com.libraryapp.repository.BookCopyRepository;
import com.libraryapp.repository.BookCopySummary;
import com.libraryapp.repository.IsbnCatalogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private IsbnCatalogRepository isbnCatalogRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private com.libraryapp.repository.ActiveLoanRepository activeLoanRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("Should register new catalog entry and copy when ISBN does not exist")
    void shouldRegisterFirstCopyForNewIsbn() {
        RegisterBookRequest request = new RegisterBookRequest("978-0-13-468599-1", "Domain Modeling Primer", "Jane Miller");

        when(isbnCatalogRepository.findByIdForUpdate("9780134685991")).thenReturn(Optional.empty());
        when(isbnCatalogRepository.save(any(IsbnCatalog.class))).thenAnswer(i -> i.getArgument(0));
        when(bookCopyRepository.save(any(BookCopy.class))).thenAnswer(i -> {
            BookCopy copy = i.getArgument(0);
            copy.setId(101L);
            return copy;
        });

        BookCopyResponse response = bookService.registerBook(request);

        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getIsbn()).isEqualTo("9780134685991");
        assertThat(response.getTitle()).isEqualTo("Domain Modeling Primer");
        assertThat(response.getAuthor()).isEqualTo("Jane Miller");
        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Should register additional copy when existing ISBN has matching title and author case-insensitively")
    void shouldRegisterAdditionalCopyForMatchingIsbn() {
        RegisterBookRequest request = new RegisterBookRequest("9780134685991", "domain modeling primer", "jane miller");
        IsbnCatalog existing = new IsbnCatalog("9780134685991", "Domain Modeling Primer", "Jane Miller", Instant.now());

        when(isbnCatalogRepository.findByIdForUpdate("9780134685991")).thenReturn(Optional.of(existing));
        when(bookCopyRepository.save(any(BookCopy.class))).thenAnswer(i -> {
            BookCopy copy = i.getArgument(0);
            copy.setId(102L);
            return copy;
        });

        BookCopyResponse response = bookService.registerBook(request);

        assertThat(response.getId()).isEqualTo(102L);
        assertThat(response.getIsbn()).isEqualTo("9780134685991");
        verify(isbnCatalogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration when title differs for existing ISBN")
    void shouldRejectWhenTitleDiffersForExistingIsbn() {
        RegisterBookRequest request = new RegisterBookRequest("9780134685991", "Different Title", "Jane Miller");
        IsbnCatalog existing = new IsbnCatalog("9780134685991", "Domain Modeling Primer", "Jane Miller", Instant.now());

        when(isbnCatalogRepository.findByIdForUpdate("9780134685991")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookService.registerBook(request))
                .isInstanceOf(IsbnMetadataMismatchException.class);

        verify(bookCopyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration when author differs for existing ISBN")
    void shouldRejectWhenAuthorDiffersForExistingIsbn() {
        RegisterBookRequest request = new RegisterBookRequest("9780134685991", "Domain Modeling Primer", "Different Author");
        IsbnCatalog existing = new IsbnCatalog("9780134685991", "Domain Modeling Primer", "Jane Miller", Instant.now());

        when(isbnCatalogRepository.findByIdForUpdate("9780134685991")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookService.registerBook(request))
                .isInstanceOf(IsbnMetadataMismatchException.class);

        verify(bookCopyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should list all books mapped from summaries")
    void shouldListAllBooks() {
        BookCopySummary summary1 = createSummary(1L, "9780134685991", "Title 1", "Author 1", true);
        BookCopySummary summary2 = createSummary(2L, "9780134685991", "Title 1", "Author 1", false);

        when(bookCopyRepository.findAllSummaries()).thenReturn(List.of(summary1, summary2));

        List<BookCopyResponse> books = bookService.getAllBooks();

        assertThat(books).hasSize(2);
        assertThat(books.get(0).getId()).isEqualTo(1L);
        assertThat(books.get(0).isAvailable()).isTrue();
        assertThat(books.get(1).getId()).isEqualTo(2L);
        assertThat(books.get(1).isAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book copy does not exist")
    void shouldThrowWhenBookNotFound() {
        when(bookCopyRepository.findByIdWithCatalog(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookCopyEntity(999L))
                .isInstanceOf(BookNotFoundException.class);
    }

    private BookCopySummary createSummary(Long id, String isbn, String title, String author, boolean available) {
        return new BookCopySummary() {
            @Override
            public Long getId() { return id; }
            @Override
            public String getIsbn() { return isbn; }
            @Override
            public String getTitle() { return title; }
            @Override
            public String getAuthor() { return author; }
            @Override
            public boolean isAvailable() { return available; }
        };
    }
}
