-- MariaDB/MySQL Compatible Schema (Updated with member table)
-- Paste into phpMyAdmin or MySQL Workbench

CREATE DATABASE IF NOT EXISTS crud_db;
USE crud_db;

-- Drop and recreate tables to match code
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS kategori;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS students;

CREATE TABLE IF NOT EXISTS kategori (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS menu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    kategori_id INT,
    harga DECIMAL(10,2) NOT NULL,
    stok INT DEFAULT 0,
    FOREIGN KEY (kategori_id) REFERENCES kategori(id)
);

CREATE TABLE IF NOT EXISTS member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    discount DECIMAL(5,4) DEFAULT 0.10
);

-- Sample data
INSERT INTO kategori (nama) VALUES 
('Makanan'),
('Minuman');

INSERT INTO menu (nama, kategori_id, harga, stok) VALUES 
('Nasi Pecel', 1, 15000.00, 50),
('Ayam Goreng', 1, 20000.00, 30),
('Teh Hangat', 2, 5000.00, 40),
('Es Jeruk', 2, 8000.00, 60);

INSERT INTO member (nama, discount) VALUES 
('Silver', 0.10),
('Gold', 0.15),
('Platinum', 0.20);

SELECT 'Kategori:' as Table_Name; SELECT * FROM kategori;
SELECT 'Menu:' as Table_Name; SELECT * FROM menu;
SELECT 'Member:' as Table_Name; SELECT * FROM member;

-- Transaksi tables
DROP TABLE IF EXISTS detail_pesanan;
DROP TABLE IF EXISTS pesanan;

CREATE TABLE pesanan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    wkt DATETIME DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2),
    member VARCHAR(50)
);

CREATE TABLE detail_pesanan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pesanan_id INT,
    menu_id INT,
    qty INT,
    harga_unit DECIMAL(10,2),
    subtotal DECIMAL(10,2),
    FOREIGN KEY (pesanan_id) REFERENCES pesanan(id),
    FOREIGN KEY (menu_id) REFERENCES menu(id)
);

-- Sample transaksi
INSERT INTO pesanan (total, member) VALUES (35000, 'Gold');
INSERT INTO detail_pesanan (pesanan_id, menu_id, qty, harga_unit, subtotal) VALUES (1, 1, 2, 15000, 30000), (1, 3, 1, 5000, 5000);

