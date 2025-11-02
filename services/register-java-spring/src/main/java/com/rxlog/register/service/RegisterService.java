package com.rxlog.register.service;

import com.rxlog.register.api.RegisterBookRequest;
import com.rxlog.register.api.RegisterBookResponse;
import com.rxlog.register.api.ReadingStatusRequest;
import com.rxlog.register.api.ReadingStatusResponse;
import com.rxlog.register.domain.Book;
import com.rxlog.register.repo.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RegisterService {

    private final BookRepository books;
    private final WebClient web;

    @Value("${barcode.service.base:http://barcodes-go:8082}")
    String barcodeBase;

    public RegisterService(BookRepository books) {
        this.books = books;
        this.web = WebClient.builder().build();
    }

    /**
     * Saves a new book using the barcode already assigned by the barcode service.
     * No re-checks, no commit.
     * On save failure, the barcode is released back to the pool.
     * If the initial status is finished/abandoned, release immediately and detach from the book.
     */
    @Transactional
    public RegisterBookResponse register(RegisterBookRequest req) {
        // Basic guard to avoid saving without a barcode (UI should prevent this)
        if (req.barcode() == null || req.barcode().isBlank()) {
            throw new IllegalArgumentException("Barcode is required.");
        }

        // Build the entity
        Book b = new Book();
        b.setAuthor(req.author());
        b.setPublisher(req.publisher());
        b.setPages(req.pages());

        b.setTitleKeyword(req.titleKeyword());
        b.setTitleKeywordPosition(req.titleKeywordPosition());
        b.setTitleKeyword2(req.titleKeyword2());
        b.setTitleKeyword2Position(req.titleKeyword2Position());
        b.setTitleKeyword3(req.titleKeyword3());
        b.setTitleKeyword3Position(req.titleKeyword3Position());

        b.setWidthMm(req.width());
        b.setHeightMm(req.height());

        b.setBarcodes(List.of(req.barcode()));
        b.setReadingStatus(req.readingStatus());
        b.setTopBook(Boolean.TRUE.equals(req.topBook()));
        b.setReadingStatusUpdatedAt(OffsetDateTime.now());

        // Save; on failure, release the consumed barcode
        try {
            b = books.save(b);
        } catch (Exception saveEx) {
            try {
                web.post().uri(barcodeBase + "/api/barcodes/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("code", req.barcode()))
                        .retrieve().toBodilessEntity().block();
            } catch (Exception ignored) {}
            throw saveEx;
        }

        // If created directly as finished/abandoned → release barcode and detach
        boolean done = "finished".equalsIgnoreCase(req.readingStatus())
                || "abandoned".equalsIgnoreCase(req.readingStatus());
        if (done) {
            try {
                web.post().uri(barcodeBase + "/api/barcodes/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("code", req.barcode()))
                        .retrieve().toBodilessEntity().block();
                b.setBarcodes(Collections.emptyList());
                books.save(b);
            } catch (Exception ignored) {}
        }

        return new RegisterBookResponse(b.getId().toString(), "CREATED", req.readingStatus(), req.barcode());
    }

    /**
     * Updates reading status; when moving to finished/abandoned, release and detach the barcode.
     */
    @Transactional
    public ReadingStatusResponse updateReadingStatus(ReadingStatusRequest req) {
        UUID id = UUID.fromString(req.bookId());
        Book b = books.findById(id).orElseThrow();

        String status = req.status().toLowerCase(Locale.ROOT);
        if (!List.of("in_progress", "finished", "abandoned").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        if (req.topBook() != null) b.setTopBook(req.topBook());
        b.setReadingStatus(status);
        b.setReadingStatusUpdatedAt(OffsetDateTime.now());

        String released = null;
        boolean done = status.equals("finished") || status.equals("abandoned");
        if (done && b.getBarcodes() != null && !b.getBarcodes().isEmpty()) {
            String code = b.getBarcodes().get(0);
            b.setBarcodes(Collections.emptyList()); // detach
            try {
                web.post().uri(barcodeBase + "/api/barcodes/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("code", code))
                        .retrieve().toBodilessEntity().block();
                released = code;
            } catch (Exception ignored) {}
        }

        books.save(b);
        return new ReadingStatusResponse(b.getId().toString(), status, b.isTopBook(), released);
    }
}