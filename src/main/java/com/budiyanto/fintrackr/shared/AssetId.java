package com.budiyanto.fintrackr.shared;

import java.util.Objects;
import org.apache.commons.validator.routines.checkdigit.ISINCheckDigit;

public record AssetId(String value) {

    public AssetId {

        Objects.requireNonNull(value, "Isin value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Isin value cannot be blank");
        }

        if (!ISINCheckDigit.ISIN_CHECK_DIGIT.isValid(value)) {
             throw new IllegalArgumentException("ISIN check digit is invalid");
        }
    }

    public static AssetId of(String value) {
        return new AssetId(value);
    }

}
