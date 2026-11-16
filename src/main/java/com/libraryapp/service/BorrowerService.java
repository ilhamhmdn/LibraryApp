package com.libraryapp.service;

import com.libraryapp.dto.request.RegisterBorrowerRequest;
import com.libraryapp.dto.response.BorrowerResponse;
import com.libraryapp.entity.Borrower;
import com.libraryapp.exception.BorrowerNotFoundException;
import com.libraryapp.exception.DuplicateEmailException;
import com.libraryapp.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    public BorrowerService(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    @Transactional
    public BorrowerResponse registerBorrower(RegisterBorrowerRequest request) {
        String trimmedName = request.getName().trim();
        String rawEmail = request.getEmail().trim();
        String normalizedEmail = rawEmail.toLowerCase(Locale.ROOT);

        if (borrowerRepository.existsByEmailNormalized(normalizedEmail)) {
            throw new DuplicateEmailException(rawEmail);
        }

        Borrower borrower = new Borrower(trimmedName, rawEmail, normalizedEmail, Instant.now());
        Borrower saved = borrowerRepository.save(borrower);

        return new BorrowerResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Borrower getBorrowerEntity(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new BorrowerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public BorrowerResponse getBorrowerById(Long id) {
        Borrower borrower = getBorrowerEntity(id);
        return new BorrowerResponse(borrower.getId(), borrower.getName(), borrower.getEmail(), borrower.getCreatedAt());
    }
}
