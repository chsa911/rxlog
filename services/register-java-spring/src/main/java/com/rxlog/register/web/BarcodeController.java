package com.rxlog.register.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/barcodes")
@RequiredArgsConstructor
public class BarcodeController {

    private final JdbcTemplate jdbc;

    private static BigDecimal parseCm(Object v, String name) {
        if (v == null) throw new IllegalArgumentException(name + " is required");
        String s = v.toString().trim().replace(',', '.');
        try { return new BigDecimal(s); }
        catch (Exception e) { throw new IllegalArgumentException(name + " must be a number, got: " + v); }
    }

    /**
     * Assign a barcode by dimensions (in centimeters). No position param is needed:
     * the DB derives e/l/o from height bands (auto allocator).
     *
     * Body JSON: { "widthCm": 10.5, "heightCm": 19 }   // "10,5" is also accepted
     */
    @PostMapping("/assignForDimensions")
    public ResponseEntity<?> assignForDimensions(@RequestBody Map<String,Object> body) {
        BigDecimal wcm = parseCm(body.get("widthCm"),  "widthCm");
        BigDecimal hcm = parseCm(body.get("heightCm"), "heightCm");

        try {
            // 1) See if any rule applies at all (try e/l/o; if all null -> 422)
            String ruleE = jdbc.queryForObject(
                    "select match_color_code_with_stock_cm(?, ?, 'e')", String.class, wcm, hcm);
            String ruleL = jdbc.queryForObject(
                    "select match_color_code_with_stock_cm(?, ?, 'l')", String.class, wcm, hcm);
            String ruleO = jdbc.queryForObject(
                    "select match_color_code_with_stock_cm(?, ?, 'o')", String.class, wcm, hcm);

            if (ruleE == null && ruleL == null && ruleO == null) {
                return ResponseEntity.unprocessableEntity()
                        .body(Map.of("type","NO_RULE_APPLIES","widthCm",wcm,"heightCm",hcm));
            }

            // 2) Let the DB auto-derive the position (e/l/o) and assign
            String code = jdbc.queryForObject(
                    "select assign_barcode_for_dimensions_auto_cm(?, ?)",
                    String.class, wcm, hcm);

            if (code == null) {
                // a rule applied but no stock for the derived position
                String rule = ruleE != null ? ruleE : (ruleL != null ? ruleL : ruleO);
                return ResponseEntity.status(409).body(Map.of("type","NO_STOCK","rule",rule));
            }

            return ResponseEntity.ok(Map.of("barcode", code));
        } catch (org.springframework.dao.DataAccessResourceFailureException e) {
            // DB not reachable
            return ResponseEntity.status(503).body(Map.of("type","DB_UNAVAILABLE"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("type","BAD_REQUEST","message",e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("type","INTERNAL","message",e.getMessage()));
        }
    }

    /** Optional: release an unsaved barcode. Body: { "code": "eik012" } */
    @PostMapping("/release")
    public ResponseEntity<?> release(@RequestBody Map<String,Object> body) {
        String code = String.valueOf(body.get("code"));
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("type","BAD_REQUEST","message","code is required"));
        }
        try {
            int upd = jdbc.update("update public.barcodes set is_available=true where code=? and is_available=false", code);
            // optionally close history here if you track it
            return ResponseEntity.ok(Map.of("released", upd > 0));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("type","INTERNAL","message",e.getMessage()));
        }
    }
}