package com.rxlog.register.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {

    @Id
    private String id;

    // Inline basics
    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String publisher;

    @Column(nullable = false)
    private Integer pages;

    // Title keywords + positions
    @Column(name = "title_keyword", nullable = false)
    private String titleKeyword;

    @Column(name = "title_keyword_position", nullable = false)
    private Integer titleKeywordPosition;

    @Column(name = "title_keyword2")
    private String titleKeyword2;

    @Column(name = "title_keyword2_position")
    private Integer titleKeyword2Position;

    @Column(name = "title_keyword3")
    private String titleKeyword3;

    @Column(name = "title_keyword3_position")
    private Integer titleKeyword3Position;

    /**
     * Dimensions in millimeters, stored as columns 'width' and 'height'
     * (no _mm suffix).
     */
    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    // Reading status + flags
    @Column(name = "reading_status")
    private String readingStatus;  // "in_progress" | "finished" | "abandoned"

    @Builder.Default
    @Column(name = "top_book", nullable = false)
    private boolean topBook = false;

    @Column(name = "reading_status_updated_at")
    private OffsetDateTime readingStatusUpdatedAt;

    /** New audit columns */
    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;

    @Column(name = "top_book_set_at")
    private OffsetDateTime topBookSetAt;

    // Barcodes stored as element collection
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "book_barcodes", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "barcode")
    private List<String> barcodes = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (id == null) id = java.util.UUID.randomUUID().toString();
        if (registeredAt == null) registeredAt = OffsetDateTime.now();
        if (readingStatusUpdatedAt == null) readingStatusUpdatedAt = registeredAt;
        if (topBook && topBookSetAt == null) topBookSetAt = registeredAt;
    }
}