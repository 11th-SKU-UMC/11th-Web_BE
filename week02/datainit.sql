CREATE DATABASE umc2;

USE umc2;

-- 테이블 생성
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nickname VARCHAR(30) NOT NULL
);

CREATE TABLE category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE book (
    book_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (category_id)
        REFERENCES category(category_id)
);

CREATE TABLE rental (
    rental_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    rented_at DATETIME NOT NULL,
    due_at DATETIME NOT NULL,
    returned_at DATETIME NULL,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id),
    FOREIGN KEY (book_id)
        REFERENCES book(book_id)
);

CREATE TABLE tag (
    tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE book_tag (
    book_id BIGINT,
    tag_id BIGINT,
    PRIMARY KEY (book_id, tag_id),
    FOREIGN KEY (book_id)
        REFERENCES book(book_id),
    FOREIGN KEY (tag_id)
        REFERENCES tag(tag_id)
);

CREATE TABLE book_like (
    user_id BIGINT,
    book_id BIGINT,
    PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id)
        REFERENCES users(user_id),
    FOREIGN KEY (book_id)
        REFERENCES book(book_id)
);

CREATE TABLE notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
);

-- 테이블 확인
SHOW TABLES;

-- 더미 데이터 전체 삽입
INSERT INTO users (nickname)
VALUES
    ('민서'),
    ('수현');

INSERT INTO category (name)
VALUES
    ('문학'),
    ('과학');

INSERT INTO book (
    category_id,
    title,
    description,
    is_available
)
VALUES
    (1, '달빛 도서관', '소설', TRUE),
    (1, '겨울의 편지', '에세이', FALSE),
    (2, '우주를 읽는 법', '과학 교양', TRUE);

INSERT INTO rental (
    user_id,
    book_id,
    rented_at,
    due_at,
    returned_at
)
VALUES
    (
        1,
        2,
        '2026-08-10 10:00:00',
        '2026-08-17 10:00:00',
        NULL
    ),
    (
        2,
        1,
        '2026-08-01 10:00:00',
        '2026-08-08 10:00:00',
        '2026-08-07 15:00:00'
    );

INSERT INTO tag (name)
VALUES
    ('소설'),
    ('추천'),
    ('과학');

INSERT INTO book_tag (book_id, tag_id)
VALUES
    (1, 1),
    (1, 2),
    (3, 3);

INSERT INTO book_like (user_id, book_id)
VALUES
    (1, 1),
    (1, 3);
    
-- 데이터 확인
SELECT * FROM users;
SELECT * FROM category;
SELECT * FROM book;
SELECT * FROM rental;
SELECT * FROM tag;
SELECT * FROM book_tag;
SELECT * FROM book_like;
SELECT * FROM notification;