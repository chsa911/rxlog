// services/register-java-spring/src/main/java/com/rxlog/register/repo/BarcodeRepo.java
package com.rxlog.register.repo;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

public interface BarcodeRepo extends JpaRepository<com.rxlog.register.domain.Barcode, String> {

    // optional: if you still want to distinguish 422 vs 409
    @Query(value="select match_color_code_with_stock_cm(:w,:h,:p)", nativeQuery = true)
    String matchRuleCm(@Param("w") BigDecimal widthCm,
                       @Param("h") BigDecimal heightCm,
                       @Param("p") String position);

    // auto-position allocator in cm (derives e/l/o from height)
    @Query(value="select assign_barcode_for_dimensions_auto_cm(:w,:h)", nativeQuery = true)
    String assignAutoCm(@Param("w") BigDecimal widthCm,
                        @Param("h") BigDecimal heightCm);
}