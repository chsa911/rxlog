package com.rxlog.mobile.domain
import jakarta.persistence.*
import java.util.*
@Entity @Table(name="reading_status")
data class ReadingStatus(
  @Id @GeneratedValue var id: UUID? = null,
  var bookId: UUID? = null,
  @Column(nullable=false) var status: String = "finished",
  var pages: Int? = null,
  @Column(nullable=false) var recordedAt: java.time.OffsetDateTime = java.time.OffsetDateTime.now()
)
