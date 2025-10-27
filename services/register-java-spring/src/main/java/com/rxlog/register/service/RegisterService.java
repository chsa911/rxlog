package com.rxlog.register.service;

import com.rxlog.register.api.RegisterBookRequest;
import com.rxlog.register.api.RegisterBookResponse;
import com.rxlog.register.domain.Author;
import com.rxlog.register.domain.Book;
import com.rxlog.register.domain.Publisher;
import com.rxlog.register.repo.AuthorRepository;
import com.rxlog.register.repo.BookRepository;
import com.rxlog.register.repo.PublisherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class RegisterService {

    private final AuthorRepository authors;
    private final PublisherRepository publishers;
    private final BookRepository books;
    private final WebClient web;

    @Value("${barcode.service.base:http://barcodes-go:8082}")
    private String barcodeBase;

    public RegisterService(AuthorRepository authors,
                           PublisherRepository publishers,
                           BookRepository books) {
        this.authors = authors;
        this.publishers = publishers;
        this.books = books;
        this.web = WebClient.builder().build();
    }

    @Transactional
    public RegisterBookResponse register(RegisterBookRequest req) {
        // Upsert Author
        Author author = authors.findByNameIgnoreCase(req.author())
                .orElseGet(() -> {
                    Author a = new Author();
                    a.setName(req.author());
                    return authors.save(a);
                });

        // Upsert Publisher
        Publisher publisher = publishers.findByNameIgnoreCase(req.publisher())
                .orElseGet(() -> {
                    Publisher p = new Publisher();
                    p.setName(req.publisher());
                    return publishers.save(p);
                });

        // Create Book
        Book book = new Book();
        book.setTitle(req.title());
        book.setAuthor(author);
        book.setPublisher(publisher);
        if (req.barcode() != null && !req.barcode().isBlank()) {
            book.setBarcodes(List.of(req.barcode()));
        }
        book = books.save(book);

        // Optionally call Barcode service to reserve + assign
        String barcodeStatus = "SKIPPED";
        if (req.barcode() != null && !req.barcode().isBlank()) {
            int rule = (req.sizeRuleId() == null) ? 1 : req.sizeRuleId();
            try {
                // reserve
                web.post()
                        .uri(barcodeBase + "/api/barcodes/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "code", req.barcode(),
                                "sizeRuleId", rule
                        ))
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                // assign
                web.post()
                        .uri(barcodeBase + "/api/barcodes/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "code", req.barcode(),
                                "bookId", book.getId().toString()
                        ))
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                barcodeStatus = "ASSIGNED";
            } catch (Exception e) {
                // You could add logging here if desired
                barcodeStatus = "ERROR";
            }
        }

        return new RegisterBookResponse(
                book.getId().toString(),
                "CREATED",
                barcodeStatus
        );
    }
}