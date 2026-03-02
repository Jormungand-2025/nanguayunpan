-- 南呱云盘数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS nanguayunpan DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nanguayunpan;

-- 用户信息表
CREATE TABLE IF NOT EXISTS user_info (
                                         user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    nick_name VARCHAR(100) NOT NULL COMMENT '昵称',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    join_time DATETIME NOT NULL COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    use_space BIGINT NOT NULL DEFAULT 0 COMMENT '已使用空间（字节）',
    total_space BIGINT NOT NULL DEFAULT 1073741824 COMMENT '总空间（字节，默认1GB）',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_join_time (join_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 文件信息表
CREATE TABLE IF NOT EXISTS file_info (
                                         file_id VARCHAR(64) NOT NULL COMMENT '文件ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    file_md5 VARCHAR(32) COMMENT '文件MD5（用于秒传）',
    file_pid VARCHAR(64) NOT NULL DEFAULT '0' COMMENT '父目录ID',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    file_name VARCHAR(500) NOT NULL COMMENT '文件名',
    file_cover VARCHAR(500) COMMENT '文件封面',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    last_update_time DATETIME NOT NULL COMMENT '最后更新时间',
    folder_type TINYINT NOT NULL DEFAULT 0 COMMENT '文件夹类型：0-文件，1-文件夹',
    file_category TINYINT COMMENT '文件分类：1-文档，2-图片，3-视频，4-音频，5-压缩包',
    file_type TINYINT COMMENT '文件类型：根据扩展名分类',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    PRIMARY KEY (file_id),
    KEY idx_user_id (user_id),
    KEY idx_file_pid (file_pid),
    KEY idx_folder_type (folder_type),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    KEY idx_file_md5 (file_md5),
    KEY idx_user_file (user_id, file_pid),
    CONSTRAINT fk_file_user FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 分享信息表
CREATE TABLE IF NOT EXISTS share_info (
                                          share_id VARCHAR(64) NOT NULL COMMENT '分享ID',
    file_id VARCHAR(64) NOT NULL COMMENT '文件ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    share_code VARCHAR(20) NOT NULL COMMENT '分享码',
    share_url VARCHAR(500) NOT NULL COMMENT '分享链接',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    expire_time DATETIME COMMENT '过期时间',
    share_type TINYINT NOT NULL DEFAULT 0 COMMENT '分享类型：0-公开分享，1-私密分享',
    view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已失效，1-有效',
    password VARCHAR(100) COMMENT '分享密码',
    PRIMARY KEY (share_id),
    UNIQUE KEY uk_share_code (share_code),
    KEY idx_user_id (user_id),
    KEY idx_file_id (file_id),
    KEY idx_status (status),
    KEY idx_expire_time (expire_time),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_share_user FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_share_file FOREIGN KEY (file_id) REFERENCES file_info (file_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享信息表';

-- 邮箱验证码表（用于注册和重置密码）
CREATE TABLE IF NOT EXISTS email_code (
                                          id BIGINT AUTO_INCREMENT COMMENT '主键ID',
                                          email VARCHAR(100) NOT NULL COMMENT '邮箱',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    type TINYINT NOT NULL COMMENT '类型：0-注册，1-重置密码',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用',
    PRIMARY KEY (id),
    KEY idx_email (email),
    KEY idx_create_time (create_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
                                          config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(1000) NOT NULL COMMENT '配置值',
    config_desc VARCHAR(500) COMMENT '配置描述',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (config_key)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认配置数据
INSERT IGNORE INTO sys_config (config_key, config_value, config_desc, update_time) VALUES
('max_file_size', '104857600', '最大文件大小（100MB）', NOW()),
('allowed_file_types', 'txt,pdf,doc,docx,xls,xlsx,ppt,pptx,jpg,jpeg,png,gif,mp3,mp4,zip,rar', '允许上传的文件类型', NOW()),
('default_user_space', '1073741824', '默认用户空间大小（1GB）', NOW()),
('admin_email', 'admin@nanguayunpan.com', '管理员邮箱', NOW()),
('max_share_days', '30', '最大分享天数', NOW()),
('default_share_days', '7', '默认分享天数', NOW());

-- 创建索引以提高查询性能
CREATE INDEX idx_user_email ON user_info(email);
CREATE INDEX idx_file_user_pid ON file_info(user_id, file_pid);
CREATE INDEX idx_file_name ON file_info(file_name);
CREATE INDEX idx_file_create_time ON file_info(create_time);
CREATE INDEX idx_share_user_time ON share_info(user_id, create_time);
CREATE INDEX idx_share_status_time ON share_info(status, create_time);

-- 创建视图：用户空间使用情况视图
CREATE OR REPLACE VIEW user_space_view AS
SELECT
    u.user_id,
    u.nick_name,
    u.email,
    u.use_space,
    u.total_space,
    (u.total_space - u.use_space) as free_space,
    ROUND((u.use_space * 100.0 / u.total_space), 2) as use_percent
FROM user_info u;

-- 创建视图：文件统计视图
CREATE OR REPLACE VIEW file_stat_view AS
SELECT
    u.user_id,
    u.nick_name,
    COUNT(f.file_id) as total_files,
    SUM(CASE WHEN f.folder_type = 0 THEN 1 ELSE 0 END) as file_count,
    SUM(CASE WHEN f.folder_type = 1 THEN 1 ELSE 0 END) as folder_count,
    COALESCE(SUM(CASE WHEN f.folder_type = 0 THEN f.file_size ELSE 0 END), 0) as total_size
FROM user_info u
         LEFT JOIN file_info f ON u.user_id = f.user_id AND f.status = 1
GROUP BY u.user_id, u.nick_name;

-- 创建视图：分享统计视图
CREATE OR REPLACE VIEW share_stat_view AS
SELECT
    u.user_id,
    u.nick_name,
    COUNT(s.share_id) as total_shares,
    SUM(CASE WHEN s.share_type = 0 THEN 1 ELSE 0 END) as public_shares,
    SUM(CASE WHEN s.share_type = 1 THEN 1 ELSE 0 END) as private_shares,
    SUM(s.view_count) as total_views,
    SUM(s.download_count) as total_downloads
FROM user_info u
         LEFT JOIN share_info s ON u.user_id = s.user_id AND s.status = 1
GROUP BY u.user_id, u.nick_name;