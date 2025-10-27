package com.rxlog.register.repo;
import com.rxlog.register.domain.Author; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; public interface AuthorRepository extends JpaRepository<Author, UUID> { Optional<Author> findByNameIgnoreCase(String name); }
