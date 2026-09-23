use umc2;

## mission 1
-- 문학 카테고리의 대여 가능한 도서를 최신순으로 10개 조회
SELECT
    b.title,
    b.description,
    c.name AS category_name
FROM book b
JOIN category c
    ON b.category_id = c.category_id
WHERE c.name = '문학'
    AND b.is_available = TRUE
ORDER BY b.book_id DESC
LIMIT 10;

# mission 2
-- 특정 사용자가 아직 반납하지 않은 책을 반납 예정일 순으로 조회
SELECT
    b.title,
    r.rented_at,
    r.due_at
FROM rental r
JOIN book b
    ON r.book_id = b.book_id
WHERE r.user_id = 1
  AND r.returned_at IS NULL
ORDER BY r.due_at ASC;


# mission 3
-- 특정 책의 태그 목록과 특정 사용자의 좋아요 여부 조회
SELECT
    b.title,
    t.name AS tag_name,
    CASE
        WHEN bl.user_id IS NOT NULL THEN TRUE
        ELSE FALSE
    END AS is_liked
FROM book b
JOIN book_tag bt
    ON b.book_id = bt.book_id
JOIN tag t
    ON bt.tag_id = t.tag_id
LEFT JOIN book_like bl
    ON b.book_id = bl.book_id
    AND bl.user_id = 1
WHERE b.book_id = 1
ORDER BY t.tag_id ASC;


# 확장 mission
-- 내 ERD 기준 특정 회원이 참여한 미션의 미션 제목, 가게 이름, 지역 이름, 진행 상태를 최근 시작 순으로 조회
SELECT
    m.title AS mission_title,
    s.name AS store_name,
    r.name AS region_name,
    mm.status,
    mm.started_at,
    mm.completed_at
FROM member_mission mm
JOIN mission m
    ON mm.mission_id = m.id
JOIN store s
    ON m.store_id = s.id
JOIN region r
    ON s.region_id = r.id
WHERE mm.member_id = 1
ORDER BY mm.started_at DESC, mm.id DESC
LIMIT 10;