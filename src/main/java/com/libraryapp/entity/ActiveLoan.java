package com.libraryapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "active_loans")
public class ActiveLoan implements Persistable<Long> {

    @Id
    @Column(name = "book_copy_id", nullable = false)
    private Long bookCopyId;

    @Column(name = "loan_id", nullable = false, unique = true)
    private Long loanId;

    @Transient
    private boolean isNew = true;

    public ActiveLoan() {
    }

    public ActiveLoan(Long bookCopyId, Long loanId) {
        this.bookCopyId = bookCopyId;
        this.loanId = loanId;
        this.isNew = true;
    }

    @Override
    public Long getId() {
        return bookCopyId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    public Long getBookCopyId() {
        return bookCopyId;
    }

    public void setBookCopyId(Long bookCopyId) {
        this.bookCopyId = bookCopyId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
}
