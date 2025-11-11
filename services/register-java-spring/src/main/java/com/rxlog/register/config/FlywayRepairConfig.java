package com.rxlog.register.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * On startup: run Flyway.repair() once, then migrate().
 * This updates checksums in flyway_schema_history to match the current scripts.
 * After it succeeds, you can keep this or remove it.
 */
@Configuration
public class FlywayRepairConfig {
    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return (Flyway flyway) -> {
            // If history table exists, repair checksums
            try { flyway.repair(); } catch (Exception ignored) {}
            // Then run migrations as usual
            flyway.migrate();
        };
    }
}