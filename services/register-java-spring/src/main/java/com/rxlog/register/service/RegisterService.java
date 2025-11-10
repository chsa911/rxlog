package com.rxlog.register.service;

import com.rxlog.register.api.RegisterBookRequest;
import com.rxlog.register.api.RegisterBookResponse;
import com.rxlog.register.api.ReadingStatusRequest;
import com.rxlog.register.api.ReadingStatusResponse;
import com.rxlog.register.domain.Book;
import com.rxlog.register.repo.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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

    /**
     * Base URL of the barcode service (or the gateway that routes to it).
     * For local dev via gateway you might want: http://localhost:8080
     * In your current setup it defaults to the barcodes-go service in Docker.
     */
    @Value("${barcode.service.base:http://barcodes-go:8082}")
    String barcodeBase;

    public RegisterService(BookRepository books) {
        this.books = books;
        this.web = WebClient.builder().build();
    }

    /* =======================================================================
       NEW: Helpers + Exceptions + API to allocate by dimensions (cm)
       ======================================================================= */

    // Accepts "10,5", "10.5" or numeric; throws if null/invalid
    private static BigDecimal parseCm(Object v, String fieldName) {
        if (v == null) throw new IllegalArgumentException(fieldName + " is required");
        String s = v.toString().trim().replace(',', '.');
        try { return new BigDecimal(s); }
        catch (Exception e) { throw new IllegalArgumentException(fieldName + " must be a number, got: " + v); }
    }

    // Exceptions you can map to HTTP 422/409/503 in your controller advice
    public static class NoRuleAppliesException extends RuntimeException {
        public NoRuleAppliesException(String msg) { super(msg); }
    }
    public static class NoStockAvailableException extends RuntimeException {
        public NoStockAvailableException(String msg) { super(msg); }
    }
    public static class UpstreamDbUnavailableException extends RuntimeException {
        public UpstreamDbUnavailableException(String msg) { super(msg); }
    }

    /**
     * Ask the barcode service to allocate a barcode by dimensions (in centimeters).
     * Accepts comma decimals (e.g., "10,5") and dot decimals ("10.5").
     *
     * Returns the allocated barcode string (e.g., "ogk012").
     *
     * Throws:
     *  - NoRuleAppliesException (422)
     *  - NoStockAvailableException (409)
     *  - UpstreamDbUnavailableException (503)
     *  - RuntimeException for other 5xx or malformed response
     */
    public String assignBarcodeForDimensionsCm(Object widthCm, Object heightCm) {
        BigDecimal w = parseCm(widthCm,  "widthCm");
        BigDecimal h = parseCm(heightCm, "heightCm");

        Map<?,?> response = web.post()
                .uri(barcodeBase + "/api/barcodes/assignForDimensions") // gateway route or direct service
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("widthCm", w, "heightCm", h))
                .retrieve()
                // Map well-known statuses to clear exceptions
                .onStatus(s -> s.value() == 422,
                        r -> r.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(msg -> Mono.error(new NoRuleAppliesException(
                                        msg.isBlank() ? "No size rule applies for " + w + "×" + h + " cm" : msg))))
                .onStatus(s -> s.value() == 409,
                        r -> r.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(msg -> Mono.error(new NoStockAvailableException(
                                        msg.isBlank() ? "No stock available for " + w + "×" + h + " cm" : msg))))
                .onStatus(s -> s.value() == 503,
                        r -> r.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(msg -> Mono.error(new UpstreamDbUnavailableException(
                                        msg.isBlank() ? "Barcode DB unavailable" : msg))))
                // Let other 5xx bubble as a generic runtime
                .onStatus(HttpStatusCode::is5xxServerError,
                        r -> r.createException().flatMap(Mono::error))
                // Success => expect {"barcode":"..."}
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("barcode")) {
            throw new RuntimeException("Barcode service returned no 'barcode' field");
        }
        return String.valueOf(response.get("barcode"));
    }

    /* =======================================================================
       Existing flows (unchanged)
       ======================================================================= */

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

        // width/height are expected in mm in your current DTO
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