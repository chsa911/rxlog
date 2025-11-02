package com.rxlog.register.api;

public record RegisterBookResponse(
        String bookId,
        String status,
        String readingStatus,
        String barcode
) {}