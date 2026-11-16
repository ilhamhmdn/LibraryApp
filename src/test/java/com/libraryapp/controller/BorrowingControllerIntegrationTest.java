package com.libraryapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryapp.dto.request.RegisterBookRequest;
import com.libraryapp.dto.request.RegisterBorrowerRequest;
import com.libraryapp.dto.response.BookCopyResponse;
import com.libraryapp.dto.response.BorrowerResponse;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowingControllerIntegrationTest {

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

    private Long borrower1Id;
    private Long borrower2Id;
    private Long bookId;

    @BeforeEach
    void setUp() throws Exception {
        activeLoanRepository.deleteAll();
        loanRepository.deleteAll();
        bookCopyRepository.deleteAll();
        isbnCatalogRepository.deleteAll();
        borrowerRepository.deleteAll();

        RegisterBorrowerRequest borrower1 = new RegisterBorrowerRequest("Borrower One", "b1@example.com");
        RegisterBorrowerRequest borrower2 = new RegisterBorrowerRequest("Borrower Two", "b2@example.com");

        MvcResult resB1 = mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrower1)))
                .andExpect(status().isCreated())
                .andReturn();
        borrower1Id = objectMapper.readValue(resB1.getResponse().getContentAsString(), BorrowerResponse.class).getId();

        MvcResult resB2 = mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrower2)))
                .andExpect(status().isCreated())
                .andReturn();
        borrower2Id = objectMapper.readValue(resB2.getResponse().getContentAsString(), BorrowerResponse.class).getId();

        RegisterBookRequest book1 = new RegisterBookRequest("978-0-13-468599-1", "Domain Modeling Primer", "Jane Miller");
        MvcResult resBook = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book1)))
                .andExpect(status().isCreated())
                .andReturn();
        bookId = objectMapper.readValue(resBook.getResponse().getContentAsString(), BookCopyResponse.class).getId();
    }

    @Test
    @DisplayName("POST /api/v1/borrowers/{borrowerId}/borrowings/{bookId} should borrow book successfully")
    void shouldBorrowBookSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/borrowings/" + bookId))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.borrowerId").value(borrower1Id))
                .andExpect(jsonPath("$.bookCopyId").value(bookId))
                .andExpect(jsonPath("$.borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.returnedAt").doesNotExist());
    }

    @Test
    @DisplayName("Borrow should return 404 Not Found when borrower does not exist")
    void shouldReturnNotFoundWhenBorrowerDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/999999/borrowings/" + bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Borrower not found")));
    }

    @Test
    @DisplayName("Borrow should return 404 Not Found when book copy does not exist")
    void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/borrowings/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Physical book copy not found")));
    }

    @Test
    @DisplayName("Borrow should return 409 Conflict when book copy is already borrowed")
    void shouldReturnConflictWhenBookAlreadyBorrowed() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/borrowings/" + bookId))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/borrowers/" + borrower2Id + "/borrowings/" + bookId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("currently borrowed by another borrower")));
    }

    @Test
    @DisplayName("POST /api/v1/borrowers/{borrowerId}/returns/{bookId} should return book successfully")
    void shouldReturnBookSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/borrowings/" + bookId))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/returns/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.borrowerId").value(borrower1Id))
                .andExpect(jsonPath("$.bookCopyId").value(bookId))
                .andExpect(jsonPath("$.borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.returnedAt", notNullValue()));
    }

    @Test
    @DisplayName("Return should return 409 Conflict when book is not currently borrowed")
    void shouldReturnConflictWhenBookNotBorrowed() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/returns/" + bookId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("not currently borrowed")));
    }

    @Test
    @DisplayName("Return should return 409 Conflict when borrower does not hold the loan")
    void shouldReturnConflictWhenWrongBorrowerAttemptsReturn() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers/" + borrower1Id + "/borrowings/" + bookId))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/borrowers/" + borrower2Id + "/returns/" + bookId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("is not borrowed by borrower")));
    }
}
