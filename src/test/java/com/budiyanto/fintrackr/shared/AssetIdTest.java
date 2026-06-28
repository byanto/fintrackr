package com.budiyanto.fintrackr.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AssetId Tests")
class AssetIdTest {

    // 1. Happy path
    @Test
    @DisplayName("Given a valid ISIN string, when constructed, then return a valid AssetId")
    void should_returnValidAssetId_when_constructed() {
        // Given
        String isin = "IDN000053402";  // ISIN of ABF Fund

        // When
        AssetId result = AssetId.of(isin);

        // Then
        assertThat(result.value()).isEqualTo(isin);
    }

    // 2. Null -> rejected
    @ParameterizedTest
    @NullSource
    @DisplayName("Given a null String, when constructed, then throws an NullPointerException")
    void should_throwException_when_nullStringIsGiven(String invalidIsin) {
        // Then
        assertThatThrownBy(() -> AssetId.of(invalidIsin))
                .isInstanceOf(NullPointerException.class);
    }

    // 3. Empty / Blank -> rejected
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Given empty or blank String, when constructed, then throws an IllegalArgumentException")
    void should_throwException_when_emptyOrBlankStringIsGiven(String invalidIsin) {
        // Then
        assertThatThrownBy(() -> AssetId.of(invalidIsin))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // 4. wrong length (not 12) -> rejected
    @ParameterizedTest
    @ValueSource(strings = {"abcdefghijk", "abcdefghijklm"})
    @DisplayName("Given a String with a length other than 12, when constructed, then throws an IllegalArgumentException")
    void should_throwException_when_isinStringLengthNot12(String invalidIsin) {
        assertThatThrownBy(() -> AssetId.of(invalidIsin))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // 5. fails Luhn -> rejected
    @Test
    @DisplayName("Given an ISIN string that fails the Luhn checksum validation, when constructed, then throws an IllegalArgumentException")
    void should_throwException_when_badLuhn() {
        // Given
        String invalidIsin = "IDN000053409";

        // When & Then
        assertThatThrownBy(() -> AssetId.of(invalidIsin))
                .isInstanceOf(IllegalArgumentException.class);

    }
}
