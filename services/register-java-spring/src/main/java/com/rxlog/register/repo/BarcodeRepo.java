package com.rxlog.register.repo;

// We use JdbcTemplate for barcode operations, not JPA.
// This placeholder avoids compilation errors when the old repo is still referenced.
public interface BarcodeRepo {
    // intentionally empty
}