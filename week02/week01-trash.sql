USE week01_mission;

INSERT INTO member (nickname, email, social_id) VALUES
  ('민서', 'minseo@example.com', 'social-minseo'),
  ('수현', 'suhyeon@example.com', 'social-suhyeon');

INSERT INTO region (name) VALUES
  ('강남구'),
  ('마포구');

INSERT INTO food_category (name) VALUES
  ('한식'),
  ('분식'),
  ('카페');

INSERT INTO store (region_id, food_category_id, name, address, description) VALUES
  (1, 1, '오늘의 밥상', '서울특별시 강남구 테헤란로 1', '집밥 메뉴를 제공하는 식당'),
  (2, 2, '골목 떡볶이', '서울특별시 마포구 월드컵로 2', '매콤한 떡볶이 전문점');

INSERT INTO mission (store_id, title, description, reward_point) VALUES
  (1, '대표 메뉴 먹기', '오늘의 대표 한식 메뉴를 먹고 인증하기', 100),
  (1, '리뷰 작성하기', '식사 후 솔직한 리뷰 남기기', 50),
  (2, '떡볶이 맛보기', '대표 떡볶이 메뉴를 먹고 인증하기', 100);

INSERT INTO member_mission (member_id, mission_id, status, started_at, completed_at) VALUES
  (1, 1, 'completed', '2026-09-20 12:00:00', '2026-09-20 13:00:00'),
  (1, 2, 'completed', '2026-09-21 12:00:00', '2026-09-21 12:30:00'),
  (1, 3, 'in_progress', '2026-09-22 18:00:00', NULL),
  (2, 3, 'completed', '2026-09-23 18:00:00', '2026-09-23 19:00:00');

INSERT INTO review (member_mission_id, rating, content, created_at) VALUES
  (1, 5, '반찬이 맛있고 친절했어요.', '2026-09-20 13:10:00'),
  (2, 4, '다음에도 방문하고 싶어요.', '2026-09-21 12:40:00'),
  (4, 5, '양념이 맛있고 또 먹고 싶어요.', '2026-09-23 19:10:00');

INSERT INTO member_preference (member_id, food_category_id) VALUES
  (1, 1),
  (1, 3),
  (2, 2);
