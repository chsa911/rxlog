package com.rxlog.register.api;

import jakarta.validation.constraints.NotBlank;

public record ReadingStatusRequest(
        @NotBlank String bookId,
        @NotBlank String status,   // "in_progress" | "finished" | "abandoned"
        Boolean topBook            // optional toggle
) {}