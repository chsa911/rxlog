package com.rxlog.register.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "books")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {

    @Id @GeneratedValue
    private UUID id;

    // Inline basics (string author/publisher), pages
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

    // Dimensions in mm
    @Column(name = "width_mm")
    private Integer widthMm;

    @Column(name = "height_mm")
    private Integer heightMm;

    // Reading status + flags
    @Column(name = "reading_status")
    private String readingStatus; // "in_progress" | "finished" | "abandoned"

    @Builder.Default
    @Column(name = "top_book", nullable = false)
    private boolean topBook = false;

    @Column(name = "reading_status_updated_at")
    private OffsetDateTime readingStatusUpdatedAt;

    // Barcodes stored as element collection
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "book_barcodes", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "barcode")
    private List<String> barcodes = new ArrayList<>();
}