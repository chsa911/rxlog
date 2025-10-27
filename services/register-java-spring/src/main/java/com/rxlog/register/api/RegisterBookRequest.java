package com.rxlog.register.api;

import jakarta.validation.constraints.NotBlank;

public record RegisterBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String publisher,
        String barcode,
        Integer sizeRuleId
) {}