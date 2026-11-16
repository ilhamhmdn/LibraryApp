package com.libraryapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryapp.dto.request.RegisterBorrowerRequest;
import com.libraryapp.repository.ActiveLoanRepository;
import com.libraryapp.repository.BookCopyRepository;
import com.libraryapp.repository.BorrowerRepository;
import com.libraryapp.repository.IsbnCatalogRepository;
import com.libraryapp.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActiveLoanRepository activeLoanRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private IsbnCatalogRepository isbnCatalogRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @BeforeEach
    void cleanDatabase() {
        activeLoanRepository.deleteAll();
        loanRepository.deleteAll();
        bookCopyRepository.deleteAll();
        isbnCatalogRepository.deleteAll();
        borrowerRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/borrowers should create borrower and return 201 Created with Location header")
    void shouldRegisterBorrowerSuccessfully() throws Exception {
        RegisterBorrowerRequest request = new RegisterBorrowerRequest("John Doe", "john.doe@example.com");

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/borrowers should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestOnValidationFailure() throws Exception {
        RegisterBorrowerRequest request = new RegisterBorrowerRequest("", "invalid-email");

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/v1/borrowers should return 409 Conflict when duplicate email is registered")
    void shouldReturnConflictOnDuplicateEmail() throws Exception {
        RegisterBorrowerRequest first = new RegisterBorrowerRequest("User One", "shared@example.com");
        RegisterBorrowerRequest duplicate = new RegisterBorrowerRequest("User Two", "SHARED@example.com");

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }
}
