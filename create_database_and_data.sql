-- 创建数据库


-- 创建用户信息表
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    employ_id VARCHAR(20) NOT NULL UNIQUE COMMENT '工号',
    user_eng_name VARCHAR(50) COMMENT '英文名',
    group_type VARCHAR(20) NOT NULL COMMENT '组类型',
    sub_group VARCHAR(20) NOT NULL COMMENT '子组',
    is_deleted CHAR(1) DEFAULT 'N' COMMENT '是否删除',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 创建比赛表
DROP TABLE IF EXISTS competitions;
CREATE TABLE competitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    total_cases INT DEFAULT 20 COMMENT '总用例数',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛表';

-- 创建提交信息表
DROP TABLE IF EXISTS submissions;
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    branch VARCHAR(100) NOT NULL COMMENT '分支名称',
    passed INT DEFAULT 0 COMMENT '通过用例数',
    completion_time BIGINT DEFAULT 0 COMMENT '完成时间(秒)',
    submit_time TIMESTAMP NOT NULL COMMENT '提交时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交信息表';

-- 插入比赛数据
INSERT INTO competitions (id, start_time, total_cases) VALUES 
(1, '2025-10-22 10:00:00', 25);

-- 插入用户数据 (60个用户)
INSERT INTO user_info (id, user_name, employ_id, user_eng_name, group_type, sub_group, is_deleted) VALUES
-- AI组 - AI-1小组 (10人)
(4, '李华', 'AI002', 'ai_user_02', 'AI组', 'AI-1小组', 'N'),
(5, '王强', 'AI003', 'ai_user_03', 'AI组', 'AI-1小组', 'N'),
(6, '陈静', 'AI004', 'ai_user_04', 'AI组', 'AI-1小组', 'N'),
(7, '刘芳', 'AI005', 'ai_user_05', 'AI组', 'AI-1小组', 'N'),
(8, '赵伟', 'AI006', 'ai_user_06', 'AI组', 'AI-1小组', 'N'),
(9, '孙颖', 'AI007', 'ai_user_07', 'AI组', 'AI-1小组', 'N'),
(10, '周杰', 'AI008', 'ai_user_08', 'AI组', 'AI-1小组', 'N'),
(11, '吴敏', 'AI009', 'ai_user_09', 'AI组', 'AI-1小组', 'N'),
(12, '郑亮', 'AI010', 'ai_user_10', 'AI组', 'AI-1小组', 'N'),
(13, '马涛', 'AI011', 'ai_user_11', 'AI组', 'AI-1小组', 'N'),

-- AI组 - AI-2小组 (10人)
(14, '朱琳', 'AI012', 'ai_user_12', 'AI组', 'AI-2小组', 'N'),
(15, '胡佳', 'AI013', 'ai_user_13', 'AI组', 'AI-2小组', 'N'),
(16, '林达', 'AI014', 'ai_user_14', 'AI组', 'AI-2小组', 'N'),
(17, '郭阳', 'AI015', 'ai_user_15', 'AI组', 'AI-2小组', 'N'),
(18, '何丽', 'AI016', 'ai_user_16', 'AI组', 'AI-2小组', 'N'),
(19, '高鹏', 'AI017', 'ai_user_17', 'AI组', 'AI-2小组', 'N'),
(20, '罗娜', 'AI018', 'ai_user_18', 'AI组', 'AI-2小组', 'N'),
(21, '梁超', 'AI019', 'ai_user_19', 'AI组', 'AI-2小组', 'N'),
(22, '宋佳', 'AI020', 'ai_user_20', 'AI组', 'AI-2小组', 'N'),
(23, '黄勇', 'AI021', 'ai_user_21', 'AI组', 'AI-2小组', 'N'),

-- AI组 - AI-3小组 (10人)
(24, '杨婷', 'AI022', 'ai_user_22', 'AI组', 'AI-3小组', 'N'),
(25, '吴迪', 'AI023', 'ai_user_23', 'AI组', 'AI-3小组', 'N'),
(26, '郑敏', 'AI024', 'ai_user_24', 'AI组', 'AI-3小组', 'N'),
(27, '徐亮', 'AI025', 'ai_user_25', 'AI组', 'AI-3小组', 'N'),
(28, '马琳', 'AI026', 'ai_user_26', 'AI组', 'AI-3小组', 'N'),
(29, '曾强', 'AI027', 'ai_user_27', 'AI组', 'AI-3小组', 'N'),
(30, '刘佳', 'AI028', 'ai_user_28', 'AI组', 'AI-3小组', 'N'),
(31, '唐辉', 'AI029', 'ai_user_29', 'AI组', 'AI-3小组', 'N'),
(32, '陈明', 'AI030', 'ai_user_30', 'AI组', 'AI-3小组', 'N'),
(33, '张强', 'AI031', 'ai_user_31', 'AI组', 'AI-3小组', 'N'),

-- AI组 - AI-4小组 (10人)
(34, '王丽', 'AI032', 'ai_user_32', 'AI组', 'AI-4小组', 'N'),
(35, '刘伟', 'AI033', 'ai_user_33', 'AI组', 'AI-4小组', 'N'),
(36, '赵敏', 'AI034', 'ai_user_34', 'AI组', 'AI-4小组', 'N'),
(37, '孙磊', 'AI035', 'ai_user_35', 'AI组', 'AI-4小组', 'N'),
(38, '周敏', 'AI036', 'ai_user_36', 'AI组', 'AI-4小组', 'N'),
(39, '吴芳', 'AI037', 'ai_user_37', 'AI组', 'AI-4小组', 'N'),
(40, '郑华', 'AI038', 'ai_user_38', 'AI组', 'AI-4小组', 'N'),
(41, '马强', 'AI039', 'ai_user_39', 'AI组', 'AI-4小组', 'N'),
(42, '朱颖', 'AI040', 'ai_user_40', 'AI组', 'AI-4小组', 'N'),
(43, '胡敏', 'AI041', 'ai_user_41', 'AI组', 'AI-4小组', 'N'),

-- 非AI组 - 非AI-1小组 (10人)
(44, '林华', 'NAI001', 'non_ai_user_01', '非AI组', '非AI-1小组', 'N'),
(45, '郭强', 'NAI002', 'non_ai_user_02', '非AI组', '非AI-1小组', 'N'),
(46, '何静', 'NAI003', 'non_ai_user_03', '非AI组', '非AI-1小组', 'N'),
(47, '高芳', 'NAI004', 'non_ai_user_04', '非AI组', '非AI-1小组', 'N'),
(48, '罗伟', 'NAI005', 'non_ai_user_05', '非AI组', '非AI-1小组', 'N'),
(49, '梁颖', 'NAI006', 'non_ai_user_06', '非AI组', '非AI-1小组', 'N'),
(50, '宋杰', 'NAI007', 'non_ai_user_07', '非AI组', '非AI-1小组', 'N'),
(51, '黄敏', 'NAI008', 'non_ai_user_08', '非AI组', '非AI-1小组', 'N'),
(52, '杨亮', 'NAI009', 'non_ai_user_09', '非AI组', '非AI-1小组', 'N'),
(53, '吴涛', 'NAI010', 'non_ai_user_10', '非AI组', '非AI-1小组', 'N'),

-- 非AI组 - 非AI-2小组 (10人)
(54, '郑琳', 'NAI011', 'non_ai_user_11', '非AI组', '非AI-2小组', 'N'),
(55, '马佳', 'NAI012', 'non_ai_user_12', '非AI组', '非AI-2小组', 'N'),
(56, '朱达', 'NAI013', 'non_ai_user_13', '非AI组', '非AI-2小组', 'N'),
(57, '胡阳', 'NAI014', 'non_ai_user_14', '非AI组', '非AI-2小组', 'N'),
(58, '林丽', 'NAI015', 'non_ai_user_15', '非AI组', '非AI-2小组', 'N'),
(59, '郭鹏', 'NAI016', 'non_ai_user_16', '非AI组', '非AI-2小组', 'N'),
(60, '何娜', 'NAI017', 'non_ai_user_17', '非AI组', '非AI-2小组', 'N'),
(61, '高超', 'NAI018', 'non_ai_user_18', '非AI组', '非AI-2小组', 'N'),
(62, '罗佳', 'NAI019', 'non_ai_user_19', '非AI组', '非AI-2小组', 'N'),
(63, '梁辉', 'NAI020', 'non_ai_user_20', '非AI组', '非AI-2小组', 'N');

-- 插入提交数据 (60条记录，覆盖各个组，通过用例数最大为25)
INSERT INTO submissions (user_id, branch, passed, completion_time, submit_time) VALUES
-- AI-1小组提交记录
(4, 'feature-branch-001', 23, 1800, '2025-10-22 10:30:00'),
(5, 'feature-branch-002', 18, 2100, '2025-10-22 10:35:00'),
(6, 'feature-branch-003', 25, 2400, '2025-10-22 10:40:00'),
(7, 'feature-branch-004', 15, 2700, '2025-10-22 10:45:00'),
(8, 'feature-branch-005', 22, 3000, '2025-10-22 10:50:00'),
(9, 'feature-branch-006', 19, 3300, '2025-10-22 10:55:00'),
(10, 'feature-branch-007', 24, 3600, '2025-10-22 11:00:00'),
(11, 'feature-branch-008', 16, 3900, '2025-10-22 11:05:00'),
(12, 'feature-branch-009', 21, 4200, '2025-10-22 11:10:00'),
(13, 'feature-branch-010', 17, 4500, '2025-10-22 11:15:00'),

-- AI-2小组提交记录
(14, 'feature-branch-011', 20, 1800, '2025-10-22 10:30:00'),
(15, 'feature-branch-012', 25, 2100, '2025-10-22 10:35:00'),
(16, 'feature-branch-013', 14, 2400, '2025-10-22 10:40:00'),
(17, 'feature-branch-014', 23, 2700, '2025-10-22 10:45:00'),
(18, 'feature-branch-015', 18, 3000, '2025-10-22 10:50:00'),
(19, 'feature-branch-016', 22, 3300, '2025-10-22 10:55:00'),
(20, 'feature-branch-017', 16, 3600, '2025-10-22 11:00:00'),
(21, 'feature-branch-018', 24, 3900, '2025-10-22 11:05:00'),
(22, 'feature-branch-019', 19, 4200, '2025-10-22 11:10:00'),
(23, 'feature-branch-020', 21, 4500, '2025-10-22 11:15:00'),

-- AI-3小组提交记录
(24, 'feature-branch-021', 17, 1800, '2025-10-22 10:30:00'),
(25, 'feature-branch-022', 25, 2100, '2025-10-22 10:35:00'),
(26, 'feature-branch-023', 13, 2400, '2025-10-22 10:40:00'),
(27, 'feature-branch-024', 22, 2700, '2025-10-22 10:45:00'),
(28, 'feature-branch-025', 20, 3000, '2025-10-22 10:50:00'),
(29, 'feature-branch-026', 18, 3300, '2025-10-22 10:55:00'),
(30, 'feature-branch-027', 24, 3600, '2025-10-22 11:00:00'),
(31, 'feature-branch-028', 15, 3900, '2025-10-22 11:05:00'),
(32, 'feature-branch-029', 23, 4200, '2025-10-22 11:10:00'),
(33, 'feature-branch-030', 19, 4500, '2025-10-22 11:15:00'),

-- AI-4小组提交记录
(34, 'feature-branch-031', 16, 1800, '2025-10-22 10:30:00'),
(35, 'feature-branch-032', 21, 2100, '2025-10-22 10:35:00'),
(36, 'feature-branch-033', 25, 2400, '2025-10-22 10:40:00'),
(37, 'feature-branch-034', 14, 2700, '2025-10-22 10:45:00'),
(38, 'feature-branch-035', 22, 3000, '2025-10-22 10:50:00'),
(39, 'feature-branch-036', 18, 3300, '2025-10-22 10:55:00'),
(40, 'feature-branch-037', 20, 3600, '2025-10-22 11:00:00'),
(41, 'feature-branch-038', 17, 3900, '2025-10-22 11:05:00'),
(42, 'feature-branch-039', 24, 4200, '2025-10-22 11:10:00'),
(43, 'feature-branch-040', 19, 4500, '2025-10-22 11:15:00'),

-- 非AI-1小组提交记录
(44, 'feature-branch-041', 15, 1800, '2025-10-22 10:30:00'),
(45, 'feature-branch-042', 23, 2100, '2025-10-22 10:35:00'),
(46, 'feature-branch-043', 12, 2400, '2025-10-22 10:40:00'),
(47, 'feature-branch-044', 20, 2700, '2025-10-22 10:45:00'),
(48, 'feature-branch-045', 18, 3000, '2025-10-22 10:50:00'),
(49, 'feature-branch-046', 25, 3300, '2025-10-22 10:55:00'),
(50, 'feature-branch-047', 16, 3600, '2025-10-22 11:00:00'),
(51, 'feature-branch-048', 21, 3900, '2025-10-22 11:05:00'),
(52, 'feature-branch-049', 14, 4200, '2025-10-22 11:10:00'),
(53, 'feature-branch-050', 22, 4500, '2025-10-22 11:15:00'),

-- 非AI-2小组提交记录
(54, 'feature-branch-051', 17, 1800, '2025-10-22 10:30:00'),
(55, 'feature-branch-052', 19, 2100, '2025-10-22 10:35:00'),
(56, 'feature-branch-053', 24, 2400, '2025-10-22 10:40:00'),
(57, 'feature-branch-054', 13, 2700, '2025-10-22 10:45:00'),
(58, 'feature-branch-055', 21, 3000, '2025-10-22 10:50:00'),
(59, 'feature-branch-056', 18, 3300, '2025-10-22 10:55:00'),
(60, 'feature-branch-057', 25, 3600, '2025-10-22 11:00:00'),
(61, 'feature-branch-058', 15, 3900, '2025-10-22 11:05:00'),
(62, 'feature-branch-059', 23, 4200, '2025-10-22 11:10:00'),
(63, 'feature-branch-060', 20, 4500, '2025-10-22 11:15:00');

-- 创建索引
CREATE INDEX idx_user_info_employ_id ON user_info(employ_id);
CREATE INDEX idx_user_info_group_type ON user_info(group_type);
CREATE INDEX idx_user_info_sub_group ON user_info(sub_group);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_submit_time ON submissions(submit_time);
CREATE INDEX idx_submissions_passed ON submissions(passed);

-- 查询验证数据
SELECT '用户统计' as info, COUNT(*) as count FROM user_info WHERE is_deleted = 'N'
UNION ALL
SELECT '提交统计', COUNT(*) FROM submissions
UNION ALL
SELECT '比赛统计', COUNT(*) FROM competitions;

-- 各组提交统计
SELECT 
    u.sub_group as '小组名称',
    COUNT(s.id) as '提交数量',
    ROUND(AVG(s.passed), 1) as '平均通过数',
    MAX(s.passed) as '最大通过数',
    MIN(s.passed) as '最小通过数'
FROM submissions s
INNER JOIN user_info u ON s.user_id = u.id
GROUP BY u.sub_group
ORDER BY u.sub_group;
