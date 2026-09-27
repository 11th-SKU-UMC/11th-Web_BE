-- 확장 요구사항:
-- 로그인한 회원이 받은 미션을 매장명, 지역명, 음식 카테고리,
-- 미션 내용과 함께 최신순으로 10개 조회한다.

SELECT
    mm.member_mission_id,
    m.content AS mission_content,
    s.name AS store_name,
    r.name AS region_name,
    fc.name AS food_category_name
FROM member_mission mm
JOIN mission m
    ON mm.mission_id = m.mission_id
JOIN store s
    ON m.store_id = s.store_id
JOIN region r
    ON s.region_id = r.region_id
JOIN food_category fc
    ON s.food_category_id = fc.food_category_id
WHERE mm.member_id = 1
ORDER BY mm.member_mission_id DESC
LIMIT 10;
