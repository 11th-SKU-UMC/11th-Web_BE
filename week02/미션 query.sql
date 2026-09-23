-- 미션 1 --
SELECT b.title, b.description, c.name
FROM book b
JOIN category c ON b.category_id = c.category_id
WHERE c.name = '문학' AND b.is_available = TRUE
ORDER BY b.book_id DESC
LIMIT 10;

/* 기준 테이블은 book이고, 결과 한 행이 책 한 권을 의미하기 때문입니다.
   book은 category_id만 가지고 있어 카테고리 이름을 얻으려면 category를 외래키로 JOIN해야 합니다.
   WHERE로 '문학' 카테고리와 대여 가능 상태를 거르고, 최신순인 book_id 내림차순으로 10개만 조회했습니다. */
   
-- 미션 2 --
SELECT b.title, r.rented_at, r.due_at
FROM book b
JOIN rental r ON b.book_id = r.book_id
WHERE r.returned_at IS NULL
ORDER BY r.due_at DESC;

/* 기준 테이블은 rental입니다. 결과 한 행이 대여 한 건이고 조건과 정렬 기준이 모두 rental 컬럼이기 때문입니다.
   rental은 book_id만 가지고 있어 책 제목을 얻기 위해 book을 JOIN했습니다.
   WHERE로 조회 대상 사용자와 미반납(returned_at IS NULL)을 거르고, 반납 임박 순인 due_at 오름차순으로 정렬했습니다. */
   
-- 미션 3 --
-- 특정 사용자 id = 1, 특정 책 id = 1 --
SELECT b.title, t.name, (bl.user_id IS NOT NULL) AS is_liked
FROM book b
JOIN book_tag bt ON b.book_id = bt.book_id
JOIN tag t ON bt.tag_id = t.tag_id
LEFT JOIN book_like bl ON bl.book_id = b.book_id AND bl.user_id = 1
WHERE b.book_id = 1;

/* 기준 테이블은 book이며, book과 tag는 다대다 관계라 중간 테이블 book_tag를 거쳐 JOIN했습니다.
   book_like는 좋아요를 누르지 않으면 행이 없어 INNER JOIN 시 책까지 사라지므로 LEFT JOIN을 썼고,
   사용자 조건을 WHERE에 두면 LEFT JOIN이 무력화되기 때문에 ON 절에 넣었습니다.
   WHERE로 조회할 책을 지정했고, 좋아요 여부는 (bl.user_id IS NOT NULL)로 1/0을 반환합니다. */

-- ------------------------------------------------------------------
-- ERD 확장 쿼리
-- 특정 사용자가 도전 중이면서 아직 성공하지 않은 미션을 마감 임박 순으로 조회합니다.
-- 결과에는 가게명, 미션 조건, 성공 포인트, 미션 기한, 시작일시를 포함합니다.
-- ------------------------------------------------------------------
SELECT s.store_name, m.mission_condition, m.point, m.end_date, um.start_at
FROM user_mission um
JOIN mission m ON m.mission_id = um.mission_id
JOIN store s ON s.store_id = m.store_id
WHERE um.user_id = 1 AND um.success = FALSE AND m.end_date >= CURDATE()
ORDER BY m.end_date
LIMIT 10;