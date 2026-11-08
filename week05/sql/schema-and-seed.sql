CREATE DATABASE IF NOT EXISTS umc_movie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE umc_movie;
CREATE TABLE IF NOT EXISTS member (
 member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 email VARCHAR(254) NOT NULL UNIQUE,
 password VARCHAR(60) NOT NULL,
 nickname VARCHAR(50) NOT NULL UNIQUE,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS rating (
 rating_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 member_id BIGINT NOT NULL,
 movie_id BIGINT NOT NULL,
 score TINYINT NOT NULL,
 comment TEXT,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_rating_member FOREIGN KEY(member_id) REFERENCES member(member_id),
 CONSTRAINT uk_rating_member_movie UNIQUE(member_id,movie_id),
 CONSTRAINT ck_rating_score CHECK(score BETWEEN 1 AND 5)
);
CREATE TABLE IF NOT EXISTS favorite (
 favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 member_id BIGINT NOT NULL,
 movie_id BIGINT NOT NULL,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_favorite_member FOREIGN KEY(member_id) REFERENCES member(member_id),
 CONSTRAINT uk_favorite_member_movie UNIQUE(member_id,movie_id)
);
-- 반복 실행해도 동일 이메일/닉네임을 중복 생성하지 않음
INSERT INTO member(email,password,nickname) VALUES
('noco@umc.com','$2a$10$abcdefghijklmnopqrstuu6urxX0ESe5EFTsKuQ1tP8cUmXMx0A8y','수현'),
('minseo@umc.com','$2a$10$abcdefghijklmnopqrstuu6urxX0ESe5EFTsKuQ1tP8cUmXMx0A8y','민서')
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname);
INSERT INTO rating(member_id,movie_id,score,comment)
SELECT member_id,550,5,'최고의 영화!' FROM member WHERE email='noco@umc.com'
ON DUPLICATE KEY UPDATE score=VALUES(score),comment=VALUES(comment);
INSERT INTO rating(member_id,movie_id,score,comment)
SELECT member_id,155,4,'다시 봐도 명작' FROM member WHERE email='noco@umc.com'
ON DUPLICATE KEY UPDATE score=VALUES(score),comment=VALUES(comment);
INSERT INTO rating(member_id,movie_id,score,comment)
SELECT member_id,550,3,'호불호가 갈릴 것 같아요' FROM member WHERE email='minseo@umc.com'
ON DUPLICATE KEY UPDATE score=VALUES(score),comment=VALUES(comment);
