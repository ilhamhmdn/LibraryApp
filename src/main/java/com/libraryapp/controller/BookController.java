package com.libraryapp.controller;

import com.libraryapp.dto.request.RegisterBookRequest;
import com.libraryapp.dto.response.BookCopyResponse;
import com.libraryapp.dto.response.ErrorResponse;
import com.libraryapp.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Operations for registering and retrieving physical book copies")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(summary = "Register a physical book copy", description = "Registers a physical book copy in the inventory. If the ISBN already exists, title and author must match the catalogue. Each registration creates an individual physical copy with its own book ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book copy registered successfully",
                    content = @Content(schema = @Schema(implementation = BookCopyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failure or invalid ISBN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Metadata mismatch for existing ISBN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookCopyResponse> registerBook(@Valid @RequestBody RegisterBookRequest request) {
        BookCopyResponse response = bookService.registerBook(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "List all physical book copies", description = "Retrieves all physical book copies in the inventory including their ISBN, title, author, and current availability status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of physical book copies retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookCopyResponse.class))))
    })
    public ResponseEntity<List<BookCopyResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get physical book copy by ID", description = "Retrieves details and availability of an individual physical book copy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book copy retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookCopyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book copy not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookCopyResponse> getBookById(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID of the physical book copy", example = "1")
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}
