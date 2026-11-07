package com.umc.movie.repository;

import com.umc.movie.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByNicknameAndMemberIdNot(String nickname, Long memberId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
