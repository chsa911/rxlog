package com.rxlog.register.api;

import com.rxlog.register.service.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register") // Gateway strips /api → /register/...
public class RegisterController {

    private final RegisterService service;

    public RegisterController(RegisterService service) {
        this.service = service;
    }

    // Create/submit a book (expects the DTO you already use)
    @PostMapping("/book")
    public ResponseEntity<?> registerBook(@RequestBody RegisterBookRequest req) {
        var res = service.register(req); // returns RegisterBookResponse
        return ResponseEntity.ok(res);
    }

    // Reading-status change (finished/abandoned → release barcode)
    @PostMapping("/reading-status")
    public ResponseEntity<?> updateReadingStatus(@RequestBody ReadingStatusRequest req) {
        var res = service.updateReadingStatus(req);
        return ResponseEntity.ok(res);
    }
}