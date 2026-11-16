package com.libraryapp.service;

import com.libraryapp.dto.request.RegisterBookRequest;
import com.libraryapp.dto.response.BookCopyResponse;
import com.libraryapp.entity.BookCopy;
import com.libraryapp.entity.IsbnCatalog;
import com.libraryapp.exception.BookNotFoundException;
import com.libraryapp.exception.IsbnMetadataMismatchException;
import com.libraryapp.repository.ActiveLoanRepository;
import com.libraryapp.repository.BookCopyRepository;
import com.libraryapp.repository.IsbnCatalogRepository;
import com.libraryapp.util.IsbnUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BookService {

    private final IsbnCatalogRepository isbnCatalogRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ActiveLoanRepository activeLoanRepository;

    public BookService(IsbnCatalogRepository isbnCatalogRepository,
                       BookCopyRepository bookCopyRepository,
                       ActiveLoanRepository activeLoanRepository) {
        this.isbnCatalogRepository = isbnCatalogRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.activeLoanRepository = activeLoanRepository;
    }

    @Transactional
    public BookCopyResponse registerBook(RegisterBookRequest request) {
        String normalizedIsbn = IsbnUtils.normalizeAndValidate(request.getIsbn());
        String trimmedTitle = request.getTitle().trim();
        String trimmedAuthor = request.getAuthor().trim();

        IsbnCatalog catalog = isbnCatalogRepository.findByIdForUpdate(normalizedIsbn)
                .map(existing -> {
                    boolean titleMatches = existing.getTitle().trim().equalsIgnoreCase(trimmedTitle);
                    boolean authorMatches = existing.getAuthor().trim().equalsIgnoreCase(trimmedAuthor);

                    if (!titleMatches || !authorMatches) {
                        throw new IsbnMetadataMismatchException(
                                normalizedIsbn,
                                existing.getTitle(),
                                existing.getAuthor(),
                                trimmedTitle,
                                trimmedAuthor
                        );
                    }
                    return existing;
                })
                .orElseGet(() -> isbnCatalogRepository.save(
                        new IsbnCatalog(normalizedIsbn, trimmedTitle, trimmedAuthor, Instant.now())
                ));

        BookCopy copy = bookCopyRepository.save(new BookCopy(catalog, Instant.now()));

        return new BookCopyResponse(
                copy.getId(),
                catalog.getIsbn(),
                catalog.getTitle(),
                catalog.getAuthor(),
                true
        );
    }

    @Transactional(readOnly = true)
    public List<BookCopyResponse> getAllBooks() {
        return bookCopyRepository.findAllSummaries().stream()
                .map(summary -> new BookCopyResponse(
                        summary.getId(),
                        summary.getIsbn(),
                        summary.getTitle(),
                        summary.getAuthor(),
                        summary.isAvailable()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public BookCopy getBookCopyEntity(Long id) {
        return bookCopyRepository.findByIdWithCatalog(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public BookCopyResponse getBookById(Long id) {
        BookCopy copy = getBookCopyEntity(id);
        boolean available = !activeLoanRepository.existsByBookCopyId(id);
        return new BookCopyResponse(
                copy.getId(),
                copy.getIsbnCatalog().getIsbn(),
                copy.getIsbnCatalog().getTitle(),
                copy.getIsbnCatalog().getAuthor(),
                available
        );
    }
}
