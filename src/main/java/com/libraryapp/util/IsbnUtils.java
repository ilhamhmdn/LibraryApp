package com.libraryapp.util;

import com.libraryapp.exception.InvalidIsbnException;

import java.util.Locale;

public final class IsbnUtils {

    private IsbnUtils() {
    }

    public static String normalizeAndValidate(String rawIsbn) {
        if (rawIsbn == null) {
            throw new InvalidIsbnException("ISBN cannot be null.");
        }

        String cleaned = rawIsbn.replaceAll("[\\s-]+", "").toUpperCase(Locale.ROOT);
        if (cleaned.length() == 10) {
            if (!isValidIsbn10(cleaned)) {
                throw new InvalidIsbnException("Invalid ISBN-10 format or checksum: " + rawIsbn);
            }
            return cleaned;
        } else if (cleaned.length() == 13) {
            if (!isValidIsbn13(cleaned)) {
                throw new InvalidIsbnException("Invalid ISBN-13 format or checksum: " + rawIsbn);
            }
            return cleaned;
        } else {
            throw new InvalidIsbnException("ISBN must be 10 or 13 characters (excluding hyphens/spaces): " + rawIsbn);
        }
    }

    private static boolean isValidIsbn10(String isbn) {
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(isbn.charAt(i))) {
                return false;
            }
        }
        char lastChar = isbn.charAt(9);
        if (!Character.isDigit(lastChar) && lastChar != 'X') {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * (isbn.charAt(i) - '0');
        }
        sum += (lastChar == 'X') ? 10 : (lastChar - '0');

        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        for (int i = 0; i < 13; i++) {
            if (!Character.isDigit(isbn.charAt(i))) {
                return false;
            }
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;

        return (isbn.charAt(12) - '0') == checkDigit;
    }
}
