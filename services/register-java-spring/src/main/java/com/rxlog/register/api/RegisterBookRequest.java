package com.rxlog.register.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request for creating/registering a book.
 * Server expects width/height in millimeters (mm).
 */
public record RegisterBookRequest(
        // core
        @NotBlank String author,
        @NotBlank String publisher,
        @NotNull @Min(1) Integer pages,

        // title keywords & positions
        @NotBlank String titleKeyword,
        @NotNull @Min(1) Integer titleKeywordPosition,
        String  titleKeyword2,
        Integer titleKeyword2Position,
        String  titleKeyword3,
        Integer titleKeyword3Position,

        // barcode + status
        @NotBlank String barcode,
        @NotBlank String readingStatus,         // "in_progress" | "finished" | "abandoned"
        Boolean topBook,

        // book dimensions (mm)
        @NotNull @Min(1) Integer width,
        @NotNull @Min(1) Integer height
) {}