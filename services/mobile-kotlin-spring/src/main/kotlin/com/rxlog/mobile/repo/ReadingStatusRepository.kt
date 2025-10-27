package com.rxlog.mobile.repo
import com.rxlog.mobile.domain.ReadingStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
interface ReadingStatusRepository : JpaRepository<ReadingStatus, UUID>
