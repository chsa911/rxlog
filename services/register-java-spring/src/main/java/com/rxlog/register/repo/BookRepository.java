// services/register-java-spring/src/main/java/com/rxlog/register/repo/BookRepository.java
package com.rxlog.register.repo;

import com.rxlog.register.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {}