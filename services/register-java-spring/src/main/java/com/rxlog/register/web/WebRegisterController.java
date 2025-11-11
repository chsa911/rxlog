package com.rxlog.register.web;

import com.rxlog.register.api.RegisterBookRequest;
import com.rxlog.register.api.RegisterBookResponse;
import com.rxlog.register.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register") // keep /api so it matches the gateway route
@RequiredArgsConstructor
public class WebRegisterController {

    private final RegisterService service;

    // accept both /book and /books to be flexible
    @PostMapping({"/book", "/books"})
    public ResponseEntity<RegisterBookResponse> register(@RequestBody RegisterBookRequest req) {
        return ResponseEntity.ok(service.register(req));
    }
}