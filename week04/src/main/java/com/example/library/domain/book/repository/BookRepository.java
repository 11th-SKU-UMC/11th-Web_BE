package com.example.library.domain.book.repository;

import com.example.library.domain.book.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @EntityGraph(attributePaths = "category")
    List<Book> findAllByOrderByIdDesc();
}
