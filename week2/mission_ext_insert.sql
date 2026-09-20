USE umc_week2;

INSERT INTO member (email, password, name, nickname, phone, created_at)
VALUES
    ('minseo@example.com', '1234', '김민서', '민서', '010-1111-1111', '2026-09-01 10:00:00'),
    ('suhyeon@example.com', '1234', '이수현', '수현', '010-2222-2222', '2026-09-02 11:00:00'),
    ('jiwoo@example.com', '1234', '박지우', '지우', '010-3333-3333', '2026-09-03 12:00:00');

INSERT INTO region (name)
VALUES
    ('서울'),
    ('경기');

INSERT INTO food_category (name)
VALUES
    ('한식'),
    ('양식'),
    ('카페');

INSERT INTO store (region_id, food_category_id, name, address, description)
VALUES
    (1, 1, '맛있는 식당', '서울특별시 성북구', '한식 전문점'),
    (1, 2, '파스타 하우스', '서울특별시 마포구', '파스타 전문점'),
    (2, 3, '오늘의 카페', '경기도 고양시', '디저트 카페'),
    (2, 1, '행복 식당', '경기도 고양시', '가정식 전문점'),
    (1, 3, '달빛 카페', '서울특별시 종로구', '커피와 디저트 전문점');

INSERT INTO mission
    (store_id, title, description, reward_point, difficulty, start_date, end_date)
VALUES
    (1, '한식 메뉴 먹기', '한식 메뉴를 주문해 보세요.', 500, 'EASY', '2026-09-01', '2026-09-30'),
    (1, '친구와 방문하기', '친구와 함께 방문해 보세요.', 700, 'NORMAL', '2026-09-01', '2026-09-30'),
    (2, '파스타 주문하기', '파스타 메뉴를 주문해 보세요.', 600, 'EASY', '2026-09-01', '2026-10-10'),
    (2, '리뷰 작성하기', '식사 후 리뷰를 작성해 보세요.', 1000, 'HARD', '2026-09-01', '2026-10-10'),
    (3, '음료 주문하기', '원하는 음료를 주문해 보세요.', 300, 'EASY', '2026-09-05', '2026-10-15'),
    (3, '디저트와 음료 주문하기', '디저트와 음료를 함께 주문해 보세요.', 800, 'NORMAL', '2026-09-05', '2026-10-15'),
    (4, '가정식 메뉴 먹기', '가정식 메뉴를 주문해 보세요.', 500, 'EASY', '2026-09-10', '2026-10-20'),
    (5, '커피와 케이크 주문하기', '커피와 케이크를 함께 주문해 보세요.', 900, 'NORMAL', '2026-09-10', '2026-10-20');

INSERT INTO member_mission
    (member_id, mission_id, status, started_at, completed_at)
VALUES
    (1, 1, 'IN_PROGRESS', '2026-09-12 10:00:00', NULL),
    (1, 3, 'IN_PROGRESS', '2026-09-14 14:00:00', NULL),
    (1, 5, 'IN_PROGRESS', '2026-09-16 16:00:00', NULL),
    (1, 7, 'COMPLETED', '2026-09-10 11:00:00', '2026-09-15 13:00:00'),
    (2, 2, 'IN_PROGRESS', '2026-09-13 12:00:00', NULL),
    (2, 6, 'COMPLETED', '2026-09-08 15:00:00', '2026-09-17 17:00:00'),
    (3, 4, 'IN_PROGRESS', '2026-09-15 13:00:00', NULL),
    (3, 8, 'IN_PROGRESS', '2026-09-18 18:00:00', NULL);