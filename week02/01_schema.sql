-- Run once in MySQL Workbench to create the practice database and schema.
CREATE DATABASE IF NOT EXISTS week02_library;
USE week02_library;

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
  FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE rental (
  rental_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  rented_at DATETIME NOT NULL,
  due_at DATETIME NOT NULL,
  returned_at DATETIME NULL,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id)
);

CREATE TABLE tag (
  tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL
);

CREATE TABLE book_tag (
  book_id BIGINT,
  tag_id BIGINT,
  PRIMARY KEY (book_id, tag_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id),
  FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
);

CREATE TABLE book_like (
  user_id BIGINT,
  book_id BIGINT,
  PRIMARY KEY (user_id, book_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id)
);

CREATE TABLE notification (
  notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(30) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);
