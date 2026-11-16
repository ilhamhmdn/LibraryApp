package com.libraryapp.service;

import com.libraryapp.dto.request.RegisterBorrowerRequest;
import com.libraryapp.dto.response.BorrowerResponse;
import com.libraryapp.entity.Borrower;
import com.libraryapp.exception.BorrowerNotFoundException;
import com.libraryapp.exception.DuplicateEmailException;
import com.libraryapp.repository.BorrowerRepository;
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
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    @DisplayName("Should successfully register borrower with normalized email")
    void shouldRegisterBorrowerSuccessfully() {
        RegisterBorrowerRequest request = new RegisterBorrowerRequest("  Alice Smith  ", "  Alice.Smith@Example.COM  ");

        when(borrowerRepository.existsByEmailNormalized("alice.smith@example.com")).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenAnswer(invocation -> {
            Borrower b = invocation.getArgument(0);
            b.setId(10L);
            return b;
        });

        BorrowerResponse response = borrowerService.registerBorrower(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Alice Smith");
        assertThat(response.getEmail()).isEqualTo("Alice.Smith@Example.COM");
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    @DisplayName("Should reject duplicate borrower email case-insensitively")
    void shouldRejectDuplicateEmail() {
        RegisterBorrowerRequest request = new RegisterBorrowerRequest("Bob Jones", "bob@example.com");

        when(borrowerRepository.existsByEmailNormalized("bob@example.com")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.registerBorrower(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(borrowerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve borrower entity when found")
    void shouldRetrieveBorrowerEntity() {
        Borrower borrower = new Borrower("Bob", "bob@example.com", "bob@example.com", Instant.now());
        borrower.setId(1L);

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));

        Borrower result = borrowerService.getBorrowerEntity(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when borrower ID does not exist")
    void shouldThrowWhenBorrowerNotFound() {
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowerService.getBorrowerEntity(99L))
                .isInstanceOf(BorrowerNotFoundException.class);
    }
}
