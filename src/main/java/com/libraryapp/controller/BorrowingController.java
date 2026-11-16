package com.libraryapp.controller;

import com.libraryapp.dto.response.ErrorResponse;
import com.libraryapp.dto.response.LoanResponse;
import com.libraryapp.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/borrowers/{borrowerId}")
@Tag(name = "Borrowing & Returns", description = "Operations for borrowing and returning physical book copies")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping("/borrowings/{bookId}")
    @Operation(summary = "Borrow a physical book copy", description = "Borrows a specific physical book copy on behalf of the identified borrower. The physical copy must currently be available.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book copy borrowed successfully",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "404", description = "Borrower or book copy not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Book copy is already borrowed by another borrower",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanResponse> borrowBook(
            @Parameter(description = "ID of the borrower", example = "1")
            @PathVariable Long borrowerId,
            @Parameter(description = "ID of the physical book copy", example = "1")
            @PathVariable Long bookId) {

        LoanResponse response = borrowingService.borrowBook(borrowerId, bookId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/borrowers/{borrowerId}/borrowings/{bookId}")
                .buildAndExpand(borrowerId, bookId)
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @org.springframework.web.bind.annotation.GetMapping("/borrowings/{bookId}")
    @Operation(summary = "Get active loan details", description = "Retrieves current active loan details for a borrowed book copy by borrower ID and book ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active loan details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "404", description = "Borrower or book copy not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Book copy is not borrowed, or is borrowed by another borrower",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanResponse> getActiveLoan(
            @Parameter(description = "ID of the borrower", example = "1")
            @PathVariable Long borrowerId,
            @Parameter(description = "ID of the physical book copy", example = "1")
            @PathVariable Long bookId) {

        return ResponseEntity.ok(borrowingService.getActiveLoan(borrowerId, bookId));
    }

    @PostMapping("/returns/{bookId}")
    @Operation(summary = "Return a borrowed physical book copy", description = "Returns a previously borrowed physical book copy on behalf of the borrower. Verifies that the borrower holds the active loan on the copy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book copy returned successfully",
                    content = @Content(schema = @Schema(implementation = LoanResponse.class))),
            @ApiResponse(responseCode = "404", description = "Borrower or book copy not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Book copy is not borrowed, or is borrowed by another borrower",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanResponse> returnBook(
            @Parameter(description = "ID of the borrower", example = "1")
            @PathVariable Long borrowerId,
            @Parameter(description = "ID of the physical book copy", example = "1")
            @PathVariable Long bookId) {

        LoanResponse response = borrowingService.returnBook(borrowerId, bookId);
        return ResponseEntity.ok(response);
    }
}
