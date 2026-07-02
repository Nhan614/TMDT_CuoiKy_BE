-- Thêm các trường mới vào bảng products
ALTER TABLE products ADD COLUMN cloudinary_public_id VARCHAR(255) DEFAULT NULL;
ALTER TABLE products ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE products ADD COLUMN artisan_id BIGINT DEFAULT NULL;

-- Thêm khoá ngoại artisan_id tham chiếu tới bảng artisans
ALTER TABLE products ADD CONSTRAINT fk_product_artisan FOREIGN KEY (artisan_id) REFERENCES artisans(id);

-- Thêm các trường xác thực email khi đăng ký
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(255) DEFAULT NULL;
ALTER TABLE users ADD COLUMN verification_token_expiry DATETIME DEFAULT NULL;

-- Các tài khoản cũ đã tồn tại trước khi có tính năng này coi như đã xác thực
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE;

