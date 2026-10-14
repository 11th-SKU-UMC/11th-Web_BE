// src/main/java/com/umc/movie/repository/MemberRepository.java
package com.umc.movie.repository;

import com.umc.movie.entity.Menber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Menber, Long> {
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
}