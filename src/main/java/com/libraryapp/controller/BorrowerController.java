package com.libraryapp.controller;

import com.libraryapp.dto.request.RegisterBorrowerRequest;
import com.libraryapp.dto.response.BorrowerResponse;
import com.libraryapp.dto.response.ErrorResponse;
import com.libraryapp.service.BorrowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/borrowers")
@Tag(name = "Borrowers", description = "Operations for managing library borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    @Operation(summary = "Register a new borrower", description = "Registers a borrower with a name and email address. Email must be unique across all borrowers.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Borrower registered successfully",
                    content = @Content(schema = @Schema(implementation = BorrowerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failure or malformed payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate email address",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BorrowerResponse> registerBorrower(@Valid @RequestBody RegisterBorrowerRequest request) {
        BorrowerResponse response = borrowerService.registerBorrower(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    @Operation(summary = "Get borrower by ID", description = "Retrieves borrower details by their unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrower retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BorrowerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Borrower not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BorrowerResponse> getBorrowerById(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID of the borrower", example = "1")
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok(borrowerService.getBorrowerById(id));
    }
}
