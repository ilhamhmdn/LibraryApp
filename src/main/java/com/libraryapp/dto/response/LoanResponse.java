package com.libraryapp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Details of a book loan")
public class LoanResponse {

    @Schema(description = "Unique identifier of the loan record", example = "10")
    private Long id;

    @Schema(description = "ID of the borrower", example = "1")
    private Long borrowerId;

    @Schema(description = "ID of the borrowed physical book copy", example = "5")
    private Long bookCopyId;

    @Schema(description = "Timestamp when the copy was borrowed", example = "2026-11-15T15:30:00Z")
    private Instant borrowedAt;

    @Schema(description = "Timestamp when the copy was returned (null if active)", example = "2026-11-15T16:00:00Z")
    private Instant returnedAt;

    public LoanResponse() {
    }

    public LoanResponse(Long id, Long borrowerId, Long bookCopyId, Instant borrowedAt, Instant returnedAt) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.bookCopyId = bookCopyId;
        this.borrowedAt = borrowedAt;
        this.returnedAt = returnedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Long getBookCopyId() {
        return bookCopyId;
    }

    public void setBookCopyId(Long bookCopyId) {
        this.bookCopyId = bookCopyId;
    }

    public Instant getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(Instant borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Instant returnedAt) {
        this.returnedAt = returnedAt;
    }
}
