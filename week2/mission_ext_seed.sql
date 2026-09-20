USE umc_week2;

CREATE TABLE member (
    member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(30) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    phone VARCHAR(20),
    created_at DATETIME NOT NULL
);

CREATE TABLE social_login (
    social_login_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    uid VARCHAR(100) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(member_id)
);

CREATE TABLE region (
    region_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE food_category (
    food_category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE store (
    store_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    region_id BIGINT NOT NULL,
    food_category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    description TEXT,
    FOREIGN KEY (region_id) REFERENCES region(region_id),
    FOREIGN KEY (food_category_id) REFERENCES food_category(food_category_id)
);

CREATE TABLE mission (
    mission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    reward_point INT NOT NULL,
    difficulty VARCHAR(20),
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (store_id) REFERENCES store(store_id)
);

CREATE TABLE member_mission (
    member_mission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    mission_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATETIME,
    completed_at DATETIME,
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (mission_id) REFERENCES mission(mission_id)
);