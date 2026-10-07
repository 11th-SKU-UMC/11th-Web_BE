package com.umc.study.repository;

import com.umc.study.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    // 전체 도서 최신순 조회
    List<Book> findAllByOrderByBookIdDesc();

    // 선택 미션 2 - 제목에 keyword가 포함된 도서 검색
    List<Book> findByTitleContaining(String keyword);
}