package com.budiyanto.fintrackr.shared;

import org.apache.commons.validator.routines.checkdigit.ISINCheckDigit;

import java.util.Objects;

public record AssetId(String value) {

    public AssetId {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Isin value cannot be blank");
        }

        if (!ISINCheckDigit.ISIN_CHECK_DIGIT.isValid(value)) {
             throw new IllegalArgumentException("Isin value should be Luhn check digit");
        }
    }

    public static AssetId of(String value) {
        return new AssetId(value);
    }

}
