package com.rxlog.register.api;
import com.rxlog.register.service.RegisterService; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/register")
public class RegisterController {
  private final RegisterService s; public RegisterController(RegisterService s){ this.s=s; }
  @PostMapping("/book") public RegisterBookResponse register(@RequestBody @Valid RegisterBookRequest req){ return s.register(req); }
  @GetMapping("/health") public String health(){ return "ok"; }
}
