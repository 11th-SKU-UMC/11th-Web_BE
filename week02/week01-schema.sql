-- 1주차 회원 미션 ERD 실습용 데이터베이스
CREATE DATABASE IF NOT EXISTS week01_mission
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE week01_mission;

CREATE TABLE member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nickname VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  social_id VARCHAR(100),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE region (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE food_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE store (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  region_id BIGINT NOT NULL,
  food_category_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL,
  description TEXT,
  CONSTRAINT fk_store_region FOREIGN KEY (region_id) REFERENCES region(id),
  CONSTRAINT fk_store_food_category FOREIGN KEY (food_category_id) REFERENCES food_category(id)
);

CREATE TABLE mission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  store_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  reward_point INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_mission_store FOREIGN KEY (store_id) REFERENCES store(id)
);

CREATE TABLE member_mission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  mission_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  started_at DATETIME NOT NULL,
  completed_at DATETIME,
  CONSTRAINT fk_member_mission_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_member_mission_mission FOREIGN KEY (mission_id) REFERENCES mission(id)
);

CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_mission_id BIGINT NOT NULL UNIQUE,
  rating INT NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_review_member_mission FOREIGN KEY (member_mission_id) REFERENCES member_mission(id)
);

CREATE TABLE member_preference (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  food_category_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_member_preference_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_member_preference_food_category FOREIGN KEY (food_category_id) REFERENCES food_category(id),
  CONSTRAINT uq_member_preference UNIQUE (member_id, food_category_id)
);
