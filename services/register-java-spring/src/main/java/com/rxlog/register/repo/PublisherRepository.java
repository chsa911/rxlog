package com.rxlog.register.repo;
import com.rxlog.register.domain.Publisher; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; public interface PublisherRepository extends JpaRepository<Publisher, UUID> { Optional<Publisher> findByNameIgnoreCase(String name); }
