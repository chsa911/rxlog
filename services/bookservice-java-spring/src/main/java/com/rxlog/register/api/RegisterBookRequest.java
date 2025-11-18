package com.rxlog.register.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request-Payload für das finale Registrieren eines Buchs.
 */
public record RegisterBookRequest(
        // Basisdaten
        @NotBlank String author,
        @NotBlank String publisher,
        @NotNull @Min(1) Integer pages,

        // Titel-Keywords + Positionen
        @NotBlank String titleKeyword,
        @NotNull @Min(1) Integer titleKeywordPosition,
        String  titleKeyword2,
        Integer titleKeyword2Position,
        String  titleKeyword3,
        Integer titleKeyword3Position,

        @NotNull Integer width,
        @NotNull Integer height,

        // Lese-Status
        @NotBlank String readingStatus,   // "in_progress" | "finished" | "abandoned"
        Boolean topBook,

        // zugewiesener Barcode (z.B. "ogk001")
        @NotBlank String barcode
) {}