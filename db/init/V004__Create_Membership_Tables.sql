-- 会员等级表
CREATE TABLE IF NOT EXISTS membership_levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_name VARCHAR(50) NOT NULL,
    level_code VARCHAR(20) NOT NULL UNIQUE,
    display_order INT,
    description TEXT,
    storage_limit BIGINT NOT NULL,
    max_file_size BIGINT NOT NULL,
    max_file_count INT NOT NULL,
    download_speed_limit BIGINT,
    upload_speed_limit BIGINT,
    daily_download_limit INT,
    daily_upload_limit INT,
    can_share_files BOOLEAN NOT NULL DEFAULT FALSE,
    can_create_public_links BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_level_code (level_code),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户会员关系表
CREATE TABLE IF NOT EXISTS user_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    level_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    storage_used BIGINT NOT NULL DEFAULT 0,
    file_count INT NOT NULL DEFAULT 0,
    points_earned INT DEFAULT 0,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (level_id) REFERENCES membership_levels(id),
    INDEX idx_user_id (user_id),
    INDEX idx_level_id (level_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 会员操作日志表
CREATE TABLE IF NOT EXISTS membership_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_detail TEXT,
    old_level_id BIGINT,
    new_level_id BIGINT,
    operator_id BIGINT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (old_level_id) REFERENCES membership_levels(id),
    FOREIGN KEY (new_level_id) REFERENCES membership_levels(id),
    FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化会员等级数据
INSERT INTO membership_levels (level_name, level_code, display_order, description, storage_limit, max_file_size, max_file_count, download_speed_limit, upload_speed_limit, daily_download_limit, daily_upload_limit, can_share_files, can_create_public_links, priority, is_active) VALUES
('普通用户', 'free', 1, '免费用户，基础文件存储', 1073741824, 52428800, 100, 0, 0, 0, 0, TRUE, FALSE, 1, TRUE),
('白银会员', 'silver', 2, '白银会员，支持文件分享', 5368709120, 104857600, 500, 0, 0, 0, 0, TRUE, FALSE, 2, TRUE),
('黄金会员', 'gold', 3, '黄金会员，支持公开链接', 10737418240, 209715200, 1000, 0, 0, 100, 0, TRUE, TRUE, 3, TRUE),
('钻石会员', 'diamond', 4, '钻石会员，最高权限', 53687091200, 1073741824, 10000, 0, 0, 1000, 500, TRUE, TRUE, 4, TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
