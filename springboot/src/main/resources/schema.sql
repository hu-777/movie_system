DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS screening;
DROP TABLE IF EXISTS upcoming_movies;
DROP TABLE IF EXISTS released_movies;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS user;

CREATE TABLE admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30),
    avatar VARCHAR(500)
);

CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30),
    avatar VARCHAR(500),
    sex VARCHAR(20),
    phone VARCHAR(50),
    email VARCHAR(150),
    security_question VARCHAR(255),
    security_answer VARCHAR(255)
);

CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE notice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    time VARCHAR(50)
);

CREATE TABLE released_movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(100),
    actors VARCHAR(500),
    score VARCHAR(50),
    box_office VARCHAR(100),
    showtime VARCHAR(100),
    area VARCHAR(100),
    length VARCHAR(100),
    unit VARCHAR(50),
    store INT DEFAULT 0,
    price INT DEFAULT 0,
    category_id INT
);

CREATE TABLE upcoming_movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    comment TEXT,
    showtime VARCHAR(100),
    tickets INT DEFAULT 0,
    goods_id INT,
    img VARCHAR(500),
    price INT DEFAULT 0,
    type VARCHAR(100),
    position VARCHAR(100)
);

CREATE TABLE screening (
    id INT AUTO_INCREMENT PRIMARY KEY,
    goods_id INT,
    goods_name VARCHAR(200),
    hall_name VARCHAR(100),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    price INT DEFAULT 0,
    total_seats INT DEFAULT 0,
    available_seats INT DEFAULT 0,
    screen_type VARCHAR(50)
);

CREATE TABLE seat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    screening_id INT NOT NULL,
    seat_row VARCHAR(10),
    seat_column INT,
    status VARCHAR(30),
    seat_name VARCHAR(20)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(100),
    goods_id INT,
    num INT,
    user_id INT,
    status VARCHAR(50),
    time VARCHAR(50),
    total_price INT DEFAULT 0,
    screening_id INT,
    seat_info VARCHAR(500),
    hall_name VARCHAR(100),
    show_time VARCHAR(100)
);

CREATE TABLE invoice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(100),
    order_no VARCHAR(100),
    goods_id INT,
    num INT,
    price INT,
    user_id INT,
    time VARCHAR(50),
    status VARCHAR(50)
);
