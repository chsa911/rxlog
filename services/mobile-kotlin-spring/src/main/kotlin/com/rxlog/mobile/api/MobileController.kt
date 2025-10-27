package com.rxlog.mobile.api
import com.rxlog.mobile.domain.ReadingStatus
import com.rxlog.mobile.repo.ReadingStatusRepository
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*
import java.util.*
data class ReadingStatusRequest(@field:NotBlank val barcode: String, val pages: Int? = null, @field:NotBlank val status: String)
@RestController @RequestMapping("/api/mobile")
class MobileController(private val repo: ReadingStatusRepository) {
  @PostMapping("/reading-status")
  fun record(@RequestBody req: ReadingStatusRequest): ReadingStatus {
    val rs = ReadingStatus(bookId = try { UUID.fromString(req.barcode) } catch (_: Exception) { null }, status = req.status, pages = req.pages)
    return repo.save(rs)
  }
  @GetMapping("/health") fun health() = "ok"
}
