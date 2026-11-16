package com.libraryapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryapp.dto.request.RegisterBookRequest;
import com.libraryapp.dto.response.BookCopyResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIntegrationTest {

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
    @DisplayName("POST /api/v1/books should register copy and return 201 with Location header")
    void shouldRegisterBookCopySuccessfully() throws Exception {
        RegisterBookRequest request = new RegisterBookRequest("978-0-13-468599-1", "Domain Modeling Primer", "Jane Miller");

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.isbn").value("9780134685991"))
                .andExpect(jsonPath("$.title").value("Domain Modeling Primer"))
                .andExpect(jsonPath("$.author").value("Jane Miller"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/books should allow registering multiple copies for same ISBN with distinct IDs")
    void shouldAllowMultipleCopiesForSameIsbn() throws Exception {
        RegisterBookRequest copy1 = new RegisterBookRequest("9780134685991", "Domain Modeling Primer", "Jane Miller");
        RegisterBookRequest copy2 = new RegisterBookRequest("978-0-13-468599-1", "domain modeling primer", "jane miller");

        MvcResult result1 = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(copy1)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(copy2)))
                .andExpect(status().isCreated())
                .andReturn();

        BookCopyResponse res1 = objectMapper.readValue(result1.getResponse().getContentAsString(), BookCopyResponse.class);
        BookCopyResponse res2 = objectMapper.readValue(result2.getResponse().getContentAsString(), BookCopyResponse.class);

        assertThat(res1.getId()).isNotNull();
        assertThat(res2.getId()).isNotNull();
        assertThat(res1.getId()).isNotEqualTo(res2.getId());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isbn").value("9780134685991"))
                .andExpect(jsonPath("$[1].isbn").value("9780134685991"));
    }

    @Test
    @DisplayName("POST /api/v1/books should reject registration when title differs for existing ISBN")
    void shouldRejectConflictingTitleForExistingIsbn() throws Exception {
        RegisterBookRequest original = new RegisterBookRequest("9780134685991", "Domain Modeling Primer", "Jane Miller");
        RegisterBookRequest conflicting = new RegisterBookRequest("9780134685991", "Conflicting Title", "Jane Miller");

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(original)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflicting)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already registered with title")));
    }

    @Test
    @DisplayName("POST /api/v1/books should return 400 Bad Request on invalid ISBN checksum")
    void shouldRejectInvalidIsbnChecksum() throws Exception {
        RegisterBookRequest request = new RegisterBookRequest("9780134685999", "Some Book", "Author");

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid ISBN")));
    }

    @Test
    @DisplayName("GET /api/v1/books should return empty list when no books registered")
    void shouldReturnEmptyListWhenNoBooks() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
