package com.umc.domain.book.repository;

import com.umc.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 1. 카테고리 ID로 도서 목록 조회 (기존 findByCategoryId 대체)
    List<Book> findByCategory_Id(Long categoryId);

    // 2. 전체 목록 최신순 조회 (4주차 필수 미션 요구사항)
    List<Book> findAllByOrderByBookIdDesc();
}