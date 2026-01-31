-- 船舶管理系统 V2 新功能数据库脚本
-- 包含: 操作日志、消息通知、船舶证书、燃油记录

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `response_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '响应结果',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器信息',
  `execution_time` bigint(20) NULL DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_module`(`module`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 消息通知表
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint(20) NULL DEFAULT 0 COMMENT '发送者ID (0表示系统消息)',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'SYSTEM' COMMENT '消息类型: SYSTEM/MAINTENANCE/VOYAGE/CERTIFICATE/PERSONAL',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'UNREAD' COMMENT '消息状态: UNREAD/READ',
  `related_id` bigint(20) NULL DEFAULT NULL COMMENT '关联业务ID',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联业务类型',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 船舶证书表
-- ----------------------------
DROP TABLE IF EXISTS `ship_certificates`;
CREATE TABLE `ship_certificates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ship_id` bigint(20) NOT NULL COMMENT '船舶ID',
  `certificate_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证书名称',
  `certificate_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证书编号',
  `issuing_authority` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发证机关',
  `issue_date` date NULL DEFAULT NULL COMMENT '发证日期',
  `expiry_date` date NULL DEFAULT NULL COMMENT '到期日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'VALID' COMMENT '状态: VALID/EXPIRING/EXPIRED',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证书附件URL',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ship_id`(`ship_id`) USING BTREE,
  INDEX `idx_expiry_date`(`expiry_date`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '船舶证书表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 燃油记录表
-- ----------------------------
DROP TABLE IF EXISTS `fuel_records`;
CREATE TABLE `fuel_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ship_id` bigint(20) NOT NULL COMMENT '船舶ID',
  `voyage_id` bigint(20) NULL DEFAULT NULL COMMENT '关联航次ID',
  `record_date` date NOT NULL COMMENT '记录日期',
  `fuel_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'HFO' COMMENT '燃油类型: HFO/MDO/MGO/LNG',
  `quantity` decimal(10, 2) NULL DEFAULT NULL COMMENT '消耗量(吨)',
  `unit_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '单价(元/吨)',
  `total_cost` decimal(12, 2) NULL DEFAULT NULL COMMENT '总费用',
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商',
  `port` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加油港口',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ship_id`(`ship_id`) USING BTREE,
  INDEX `idx_voyage_id`(`voyage_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '燃油记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 插入示例数据
-- ----------------------------

-- 船舶证书示例数据
INSERT INTO `ship_certificates` (`ship_id`, `certificate_name`, `certificate_no`, `issuing_authority`, `issue_date`, `expiry_date`, `status`) VALUES
(1, '船舶国籍证书', 'GJ-2024-001', '中华人民共和国海事局', '2024-01-15', '2029-01-14', 'VALID'),
(1, '船舶检验证书', 'JY-2024-001', '中国船级社', '2024-03-20', '2025-03-19', 'VALID'),
(1, '船舶安全设备证书', 'AQ-2024-001', '中国船级社', '2024-02-10', '2025-01-15', 'EXPIRING'),
(2, '船舶国籍证书', 'GJ-2024-002', '中华人民共和国海事局', '2024-02-20', '2029-02-19', 'VALID'),
(2, '国际吨位证书', 'DW-2024-001', '中国船级社', '2024-01-05', '2024-12-20', 'EXPIRED'),
(3, '船舶检验证书', 'JY-2024-003', '中国船级社', '2024-06-15', '2025-06-14', 'VALID');

-- 燃油记录示例数据
INSERT INTO `fuel_records` (`ship_id`, `voyage_id`, `record_date`, `fuel_type`, `quantity`, `unit_price`, `total_cost`, `supplier`, `port`) VALUES
(1, 1, '2025-12-01', 'HFO', 150.50, 4500.00, 677250.00, '中石化', '上海港'),
(1, 1, '2025-12-05', 'MDO', 30.00, 6800.00, 204000.00, '中石油', '宁波港'),
(2, 2, '2025-12-03', 'HFO', 200.00, 4600.00, 920000.00, '中海油', '深圳港'),
(2, NULL, '2025-12-10', 'MGO', 25.50, 7200.00, 183600.00, '壳牌', '香港'),
(3, 3, '2025-12-08', 'HFO', 180.00, 4550.00, 819000.00, '中石化', '青岛港'),
(1, NULL, '2025-12-15', 'LNG', 50.00, 5500.00, 275000.00, '中海油', '天津港');

-- 系统消息示例数据
INSERT INTO `messages` (`sender_id`, `receiver_id`, `title`, `content`, `type`, `status`) VALUES
(0, 1, '欢迎使用船舶管理系统', '您好，欢迎使用船舶管理系统V2.0版本，新增了消息通知、操作日志、证书管理、燃油记录等功能。', 'SYSTEM', 'UNREAD'),
(0, 1, '证书即将到期提醒', '船舶"远洋一号"的船舶安全设备证书将于2025-01-15到期，请及时办理续期。', 'CERTIFICATE', 'UNREAD'),
(0, 1, '维修提醒', '船舶"远洋二号"已完成主机活塞环更换维修，维修费用50000元。', 'MAINTENANCE', 'READ');
