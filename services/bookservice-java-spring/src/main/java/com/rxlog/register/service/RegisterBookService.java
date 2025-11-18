// services/bookservice-java-spring/src/main/java/com/rxlog/register/service/RegisterBookService.java
package com.rxlog.register.service;

import com.rxlog.register.api.CreateDraftRequest;
import com.rxlog.register.api.CreateDraftResponse;
import com.rxlog.register.api.RegisterBookRequest;
import com.rxlog.register.api.RegisterBookResponse;
import com.rxlog.register.web.BookDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fachlogik für das Registrieren von Büchern.
 */
@Service
public class RegisterBookService {

    private final BookDao bookDao;

    public RegisterBookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    /**
     * Erzeugt einen „Draft“ (Entwurf) für ein neues Buch.
     * In dieser Demo speichern wir den Draft nicht, sondern geben nur die Maße zurück.
     */
    public CreateDraftResponse createDraft(CreateDraftRequest req) {
        return new CreateDraftResponse(
                null,          // kein persistierter Draft in der Demo
                req.width(),
                req.height()
        );
    }

    /**
     * Schreibt ein neues Buch in die Datenbank und gibt die zugehörige bookId + Barcode + Status zurück.
     */
    @Transactional
    public RegisterBookResponse registerBook(RegisterBookRequest req) {
        String bookId = bookDao.insert(req);

        // Debug‐Ausgabe hilft, wenn später noch etwas hakt
        System.out.println(">>> registerBook: id=" + bookId + ", barcode=" + req.barcode());

        return new RegisterBookResponse(
                bookId,
                req.barcode(),
                req.readingStatus()
        );
    }
}