CREATE TABLE borrowers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    email_normalized VARCHAR(254) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_borrowers_email_normalized UNIQUE (email_normalized)
);

CREATE TABLE isbn_catalog (
    isbn VARCHAR(13) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE book_copies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(13) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_book_copies_isbn FOREIGN KEY (isbn) REFERENCES isbn_catalog (isbn)
);

CREATE INDEX idx_book_copies_isbn ON book_copies (isbn);

CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_copy_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    borrowed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    returned_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_loans_id_book_copy UNIQUE (id, book_copy_id),
    CONSTRAINT fk_loans_book_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id),
    CONSTRAINT fk_loans_borrower FOREIGN KEY (borrower_id) REFERENCES borrowers (id)
);

CREATE INDEX idx_loans_book_copy_borrowed_at ON loans (book_copy_id, borrowed_at);
CREATE INDEX idx_loans_borrower_borrowed_at ON loans (borrower_id, borrowed_at);

CREATE TABLE active_loans (
    book_copy_id BIGINT PRIMARY KEY,
    loan_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_active_loans_book_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id),
    CONSTRAINT fk_active_loans_loan FOREIGN KEY (loan_id, book_copy_id) REFERENCES loans (id, book_copy_id)
);
