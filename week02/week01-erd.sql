-- 1주차 회원 미션 ERD 조회 확장 실습
USE week01_mission;

SET @current_member_id = 1;

SELECT
  member.nickname,
  store.name AS store_name,
  mission.title AS mission_title,
  review.rating,
  review.content,
  review.created_at
FROM review
JOIN member_mission
  ON review.member_mission_id = member_mission.id
JOIN member
  ON member_mission.member_id = member.id
JOIN mission
  ON member_mission.mission_id = mission.id
JOIN store
  ON mission.store_id = store.id
WHERE member.id = @current_member_id
  AND member_mission.status = 'completed'
ORDER BY review.created_at DESC, review.id DESC
LIMIT 10;
