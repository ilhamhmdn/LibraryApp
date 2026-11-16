package com.libraryapp.util;

import com.libraryapp.exception.InvalidIsbnException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IsbnUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "0-13-468599-7",
            "0134685997",
            "0-321-35668-3",
            "0321356683",
            "0-471-95869-7",
            "0-9752298-0-X"
    })
    @DisplayName("Should accept valid ISBN-10 values and normalize hyphens/spaces")
    void shouldAcceptValidIsbn10(String rawIsbn) {
        String normalized = IsbnUtils.normalizeAndValidate(rawIsbn);
        assertThat(normalized).hasSize(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "978-0-13-468599-1",
            "9780134685991",
            "978-0-321-35668-0",
            "9780321356680",
            "978-3-16-148410-0"
    })
    @DisplayName("Should accept valid ISBN-13 values and normalize hyphens/spaces")
    void shouldAcceptValidIsbn13(String rawIsbn) {
        String normalized = IsbnUtils.normalizeAndValidate(rawIsbn);
        assertThat(normalized).hasSize(13);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "12345",
            "978-0-13-468599-9", // bad checksum
            "0-13-468599-9",     // bad checksum
            "INVALID-ISBN-XX",
            "9780134685991234"
    })
    @DisplayName("Should reject invalid ISBN strings and checksums")
    void shouldRejectInvalidIsbns(String rawIsbn) {
        assertThatThrownBy(() -> IsbnUtils.normalizeAndValidate(rawIsbn))
                .isInstanceOf(InvalidIsbnException.class);
    }

    @Test
    @DisplayName("Should throw exception for null ISBN")
    void shouldThrowExceptionForNullIsbn() {
        assertThatThrownBy(() -> IsbnUtils.normalizeAndValidate(null))
                .isInstanceOf(InvalidIsbnException.class);
    }
}
