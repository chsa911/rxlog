package com.rxlog.register.repo;
import com.rxlog.register.domain.Book; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface BookRepository extends JpaRepository<Book, UUID> {}
