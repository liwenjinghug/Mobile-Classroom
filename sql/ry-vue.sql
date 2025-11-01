/*
 Navicat Premium Dump SQL

 Source Server         : ry-vue
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : ry-vue

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 01/11/2025 15:17:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for class_attendance
-- ----------------------------
DROP TABLE IF EXISTS `class_attendance`;
CREATE TABLE `class_attendance`  (
  `attendance_id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到记录唯一ID',
  `session_id` bigint NOT NULL COMMENT '关联的课堂ID',
  `student_id` bigint NOT NULL COMMENT '关联的学生ID',
  `attendance_time` datetime NULL DEFAULT NULL COMMENT '实际签到时间（NULL表示未签到）',
  `attendance_status` tinyint NOT NULL DEFAULT 0 COMMENT '签到状态: 0-未签到 1-已签到 2-迟到 3-请假 4-早退',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `device_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '签到设备IP地址',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备类型（Web/iOS/Android）',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '签到地理位置',
  PRIMARY KEY (`attendance_id`) USING BTREE,
  UNIQUE INDEX `uq_session_student`(`session_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_session`(`session_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`attendance_status` ASC) USING BTREE,
  INDEX `idx_attendance_time`(`attendance_time` ASC) USING BTREE,
  CONSTRAINT `class_attendance_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `class_session` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `class_attendance_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `class_student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生课堂签到记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class_attendance
-- ----------------------------
INSERT INTO `class_attendance` VALUES (1, 1, 1, '2021-09-29 12:14:27', 1, '2002-10-20 19:19:57', '2025-10-27 19:38:07', '', '', '');
INSERT INTO `class_attendance` VALUES (2, 1, 2, '2008-09-23 17:00:24', 1, '2016-03-30 23:49:38', '2025-10-27 19:38:07', '', '', '');
INSERT INTO `class_attendance` VALUES (3, 1, 3, '2019-12-19 06:16:26', 1, '2007-08-18 19:37:31', '2025-10-27 19:38:09', '', '', '');
INSERT INTO `class_attendance` VALUES (4, 1, 4, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (5, 1, 5, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (6, 1, 6, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (7, 1, 7, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (8, 1, 8, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (9, 1, 9, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (10, 1, 10, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (11, 1, 11, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (12, 1, 12, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (13, 1, 13, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (14, 1, 14, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (15, 1, 15, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (16, 1, 16, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (17, 1, 17, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (18, 1, 18, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (19, 1, 19, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (20, 1, 20, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (21, 1, 21, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (22, 1, 22, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (23, 1, 23, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (24, 1, 24, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (25, 1, 25, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (26, 1, 26, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (27, 1, 27, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (28, 1, 28, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (29, 1, 29, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (30, 1, 30, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (31, 1, 31, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (32, 1, 32, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (33, 1, 33, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (34, 1, 34, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (35, 1, 35, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (36, 1, 36, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (37, 1, 37, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (38, 1, 38, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (39, 1, 39, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);
INSERT INTO `class_attendance` VALUES (40, 1, 40, NULL, 1, '2025-10-27 19:44:56', '2025-10-27 19:44:56', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for class_course
-- ----------------------------
DROP TABLE IF EXISTS `class_course`;
CREATE TABLE `class_course`  (
  `course_id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `course_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程编号',
  `course_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程类型',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属学院',
  `credit` decimal(3, 1) NULL DEFAULT NULL COMMENT '学分',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程简介',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `class_number` int NULL DEFAULT NULL,
  PRIMARY KEY (`course_id`) USING BTREE,
  UNIQUE INDEX `idx_course_code`(`course_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class_course
-- ----------------------------
INSERT INTO `class_course` VALUES (1, '研究与开发实践', 'SJXL230549', '必修', '计算机学院', 2.0, '2024-2025秋季学期开设', '0', 'admin', '2025-10-29 17:50:59', 'admin', '2025-10-29 18:18:46', '1', 1);
INSERT INTO `class_course` VALUES (2, '计算机网络123', 'LZNU135465', '必修', '计算机学院', 3.0, '一门课aaaaaaaa\n', '1', 'admin', '2025-10-29 17:54:33', 'admin', '2025-10-29 18:37:45', '2', 2);
INSERT INTO `class_course` VALUES (3, '计算机金融应用', 'MSNU32546', '选修', '经济学院', 3.5, '锻炼学生实践能力', '0', 'admin', '2025-10-29 18:22:47', 'admin', '2025-10-31 21:39:29', NULL, 3);

-- ----------------------------
-- Table structure for class_homework
-- ----------------------------
DROP TABLE IF EXISTS `class_homework`;
CREATE TABLE `class_homework`  (
  `homework_id` bigint NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `session_id` bigint NOT NULL COMMENT '课堂ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作业内容',
  `requirement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作业要求',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '作业总分',
  `deadline` datetime NOT NULL COMMENT '截止时间',
  `attachments` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件路径',
  `attachment_names` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件原名',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`homework_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_deadline`(`deadline` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_homework
-- ----------------------------
INSERT INTO `class_homework` VALUES (1, 1, 7, '111111111111111111', '12222222222222222222221321', NULL, 100.00, '2025-11-02 11:22:15', '', NULL, '0', 'admin', '2025-11-01 00:49:29', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (2, 1, 7, '111111111111111111', '12222222222222222222221321', NULL, 100.00, '2025-11-02 11:22:15', '', NULL, '0', 'admin', '2025-11-01 00:49:45', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (3, 1, 7, '11111111111', '3333333333333333333333333', NULL, 100.00, '2025-11-07 00:00:00', '', NULL, '0', 'admin', '2025-11-01 00:57:16', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (4, 1, 7, '研开第8周作业', '小程序', NULL, 100.00, '2025-11-11 12:00:00', '/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101013529A007.doc', NULL, '0', 'admin', '2025-11-01 01:35:38', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (5, 1, 7, '99', '99', NULL, 100.00, '2025-11-06 00:00:00', '/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101015431A001.doc', NULL, '0', 'admin', '2025-11-01 01:54:32', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (6, 1, 7, '77', '77', NULL, 100.00, '2025-11-03 07:00:00', '/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101015911A004.doc', NULL, '0', 'admin', '2025-11-01 01:59:12', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (7, 1, 7, '研开第8周', '小程序', NULL, 100.00, '2025-11-07 00:00:00', '/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101151240A001.doc', NULL, '0', 'admin', '2025-11-01 15:12:40', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (8, 1, 7, '研开第8周', '小程序', NULL, 100.00, '2025-11-07 00:00:00', '/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101151240A001.doc', NULL, '0', 'admin', '2025-11-01 15:12:49', '', NULL, NULL);
INSERT INTO `class_homework` VALUES (9, 1, 1, '研开第8周', '小程序', NULL, 100.00, '2025-11-07 00:00:00', '/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101151240A001.doc', NULL, '0', 'admin', '2025-11-01 15:13:28', '', NULL, NULL);

-- ----------------------------
-- Table structure for class_random_pick
-- ----------------------------
DROP TABLE IF EXISTS `class_random_pick`;
CREATE TABLE `class_random_pick`  (
  `rpick_id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '课堂ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `student_id` bigint NOT NULL COMMENT '被抽学生ID',
  `pick_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抽取时间',
  `round_no` int NULL DEFAULT 1 COMMENT '第几次抽人',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`rpick_id`) USING BTREE,
  INDEX `idx_session_picktime`(`session_id` ASC, `pick_time` ASC) USING BTREE,
  INDEX `class_rpick`(`student_id` ASC) USING BTREE,
  CONSTRAINT `class_rpick` FOREIGN KEY (`student_id`) REFERENCES `class_student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `class_rpick1` FOREIGN KEY (`session_id`) REFERENCES `class_session` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class_random_pick
-- ----------------------------
INSERT INTO `class_random_pick` VALUES (5, 1, 0, 4, '2025-10-27 19:45:55', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (6, 1, 0, 27, '2025-10-27 19:46:04', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (9, 1, 0, 18, '2025-10-27 19:57:14', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (10, 1, 0, 21, '2025-10-27 19:57:23', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (11, 1, 0, 23, '2025-10-27 19:59:42', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (12, 1, 0, 36, '2025-10-27 19:59:46', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (13, 1, 0, 24, '2025-10-27 19:59:48', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (14, 1, 0, 6, '2025-10-27 19:59:49', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (15, 1, 0, 37, '2025-10-31 21:58:52', NULL, NULL);
INSERT INTO `class_random_pick` VALUES (16, 1, 0, 16, '2025-11-01 00:42:03', NULL, NULL);

-- ----------------------------
-- Table structure for class_session
-- ----------------------------
DROP TABLE IF EXISTS `class_session`;
CREATE TABLE `class_session`  (
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `teacher_id` bigint NOT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已结束',
  `total_students` int NULL DEFAULT NULL COMMENT '总人数',
  `teacher` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `class_number` int NULL DEFAULT NULL,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`session_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class_session
-- ----------------------------
INSERT INTO `class_session` VALUES (1, '研究与开发实践01', 1, '2025-10-31 20:50:24', '2025-10-31 20:50:28', 1, 50, 'A', 1, '', NULL, '', NULL);
INSERT INTO `class_session` VALUES (3, '计算机金融应用', 3, '2025-10-31 20:57:16', '2025-10-31 20:57:19', 0, 30, 'C', 3, '', NULL, 'admin', '2025-10-31 21:49:01');
INSERT INTO `class_session` VALUES (4, '研究与开发实践02', 2, '2025-10-30 21:55:37', '2025-10-31 21:55:41', 2, 42, 'B', 1, 'admin', '2025-10-31 21:55:44', 'admin', '2025-10-31 21:56:17');
INSERT INTO `class_session` VALUES (7, '研究与开发实践03', 1, '2025-11-07 00:00:00', '2025-11-20 00:00:00', 0, 32, 'C', 1, 'admin', '2025-11-01 00:42:58', 'admin', '2025-11-01 15:12:04');

-- ----------------------------
-- Table structure for class_student
-- ----------------------------
DROP TABLE IF EXISTS `class_student`;
CREATE TABLE `class_student`  (
  `student_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号',
  `student_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别 M/F',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属班级',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint NULL DEFAULT 1 COMMENT '1在读 0退学',
  PRIMARY KEY (`student_id`) USING BTREE,
  UNIQUE INDEX `student_no`(`student_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of class_student
-- ----------------------------
INSERT INTO `class_student` VALUES (1, '2022141460087', '贺英洲', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (2, '2022141460092', '刘成伟', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:19:29', 1);
INSERT INTO `class_student` VALUES (3, '2022141460189', '陈禹岐', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (4, '2022141460306', '王际华', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (5, '2023141460332', '邹易言', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (6, '2023141460333', '白桂源', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (7, '2023141460334', '李卓航', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (8, '2023141460335', '肖翰宇', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (9, '2023141460336', '谢东廷', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (10, '2023141460337', '黄正国', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (11, '2023141460339', '孙清正', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (12, '2023141460341', '毛国屹', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (13, '2023141460342', '徐雷博', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:20:08', 1);
INSERT INTO `class_student` VALUES (14, '2023141460343', '温芃磊', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:20:30', 1);
INSERT INTO `class_student` VALUES (15, '2023141460344', '卜天一', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (16, '2023141460345', '王慧卓', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (17, '2023141460346', '卢昊', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (18, '2023141460347', '朱藩林', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:21:37', 1);
INSERT INTO `class_student` VALUES (19, '2023141460348', '李为', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (20, '2023141460349', '岂皓月', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:23:10', 1);
INSERT INTO `class_student` VALUES (21, '2023141460350', '陈殷琦', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:23:20', 1);
INSERT INTO `class_student` VALUES (22, '2023141460352', '程晨', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (23, '2023141460353', '邹雨航', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (24, '2023141460354', '周雨龙', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (25, '2023141460355', '陈昱都', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (26, '2023141460356', '罗允绩', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:23:38', 1);
INSERT INTO `class_student` VALUES (27, '2023141460357', '张越', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (28, '2023141460358', '吴昱稼', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:23:49', 1);
INSERT INTO `class_student` VALUES (29, '2023141460359', '王远贵', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (30, '2023141460360', '范彭融', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:23:59', 1);
INSERT INTO `class_student` VALUES (31, '2023141460361', '朱传龙', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (32, '2023141460362', '张瑾伦', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (33, '2023141460363', '孙家鹤', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (34, '2023141460364', '孙宏森', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (35, '2023141460365', '官前锦', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:24:14', 1);
INSERT INTO `class_student` VALUES (36, '2023141460366', '方锦阳', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (37, '2023141460367', '熊眭杨', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:24:21', 1);
INSERT INTO `class_student` VALUES (38, '2023141460368', '黎文靖', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);
INSERT INTO `class_student` VALUES (39, '2023141460369', '符桢', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:24:30', 1);
INSERT INTO `class_student` VALUES (40, '2023141460370', '刘伊鸣', NULL, NULL, NULL, NULL, '2025-10-26 14:15:37', '2025-10-26 14:15:37', 1);

-- ----------------------------
-- Table structure for class_student_homework
-- ----------------------------
DROP TABLE IF EXISTS `class_student_homework`;
CREATE TABLE `class_student_homework`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '学生姓名',
  `submit_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提交内容',
  `submission_files` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '提交文件路径',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件原名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `version` int NULL DEFAULT 1 COMMENT '提交版本',
  `grade` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `grade_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '评语',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态（0未提交 1已提交 2已批改 3逾期）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_homework_id`(`homework_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生作业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_student_homework
-- ----------------------------
INSERT INTO `class_student_homework` VALUES (1, 3, 1212121, '', NULL, '改革开放史论文撰写模板.doc', NULL, NULL, NULL, 1, NULL, '', NULL, 'admin', '2025-11-01 01:32:57', '', NULL);
INSERT INTO `class_student_homework` VALUES (2, 5, 121212, '', NULL, '改革开放史论文撰写模板.doc', NULL, NULL, '2025-11-01 01:54:58', 1, NULL, '', '1', 'admin', '2025-11-01 01:54:57', '', NULL);
INSERT INTO `class_student_homework` VALUES (3, 5, 12, '', NULL, '改革开放史论文撰写模板.doc', NULL, NULL, '2025-11-01 01:58:19', 1, NULL, '', '1', 'admin', '2025-11-01 01:58:19', '', NULL);
INSERT INTO `class_student_homework` VALUES (4, 6, 77, '', NULL, '改革开放史论文撰写模板.doc', NULL, NULL, '2025-11-01 01:59:39', 1, NULL, '', '1', 'admin', '2025-11-01 01:59:38', '', NULL);
INSERT INTO `class_student_homework` VALUES (5, 8, 12121111, '', NULL, '改革开放史论文撰写模板.doc', NULL, NULL, '2025-11-01 15:13:53', 1, NULL, '', '1', 'admin', '2025-11-01 15:13:52', '', NULL);

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日历信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '已触发的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务详细信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '暂停的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '调度器状态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int NULL DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint NULL DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint NULL DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  INDEX `sched_name`(`sched_name` ASC, `job_name` ASC, `job_group` ASC) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '触发器详细信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for sys_classroom
-- ----------------------------
DROP TABLE IF EXISTS `sys_classroom`;
CREATE TABLE `sys_classroom`  (
  `classroom_id` bigint NOT NULL AUTO_INCREMENT COMMENT '课堂ID',
  `classroom_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课堂名称',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '未命名课程',
  `start_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上课时间',
  `end_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '下课时间',
  `total_students` int NULL DEFAULT 0 COMMENT '课堂总人数',
  `teacher` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '授课老师',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课堂描述',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0进行中 1停授）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`classroom_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课堂信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_classroom
-- ----------------------------
INSERT INTO `sys_classroom` VALUES (1, 'SJXL230549', '研究与开发实践', '2025.10,29', '2025.10.31', 50, 'A', '2024-2025秋季学期开设', '0', '', NULL, '', NULL);
INSERT INTO `sys_classroom` VALUES (2, '计算机网络123', '计算机网络123', '1', '2', 1, '12', NULL, '1', '', NULL, 'admin', '2025-10-31 20:16:57');
INSERT INTO `sys_classroom` VALUES (3, 'MSNU32546', '计算机金融应用', '2111.1.1', '2112.1.1', 55, NULL, '锻炼学生实践能力', '0', '', NULL, '', NULL);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2025-10-30 17:06:55', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定时任务调度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2025-10-30 17:06:55', '', NULL, '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统访问记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 18:05:38');
INSERT INTO `sys_logininfor` VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-30 20:17:20');
INSERT INTO `sys_logininfor` VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-31 19:10:48');
INSERT INTO `sys_logininfor` VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-31 19:21:07');
INSERT INTO `sys_logininfor` VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-31 21:36:17');
INSERT INTO `sys_logininfor` VALUES (105, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-01 15:11:52');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2025-10-30 17:06:55', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2025-10-30 17:06:55', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2025-10-30 17:06:55', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, '若依官网', 0, 4, 'http://ruoyi.vip', NULL, '', '', 0, 0, 'M', '0', '0', '', 'guide', 'admin', '2025-10-30 17:06:55', '', NULL, '若依官网地址');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2025-10-30 17:06:55', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-10-30 17:06:55', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2025-10-30 17:06:55', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2025-10-30 17:06:55', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2025-10-30 17:06:55', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2025-10-30 17:06:55', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2025-10-30 17:06:55', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2025-10-30 17:06:55', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2025-10-30 17:06:55', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2025-10-30 17:06:55', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2025-10-30 17:06:55', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2025-10-30 17:06:55', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2025-10-30 17:06:55', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2025-10-30 17:06:55', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2025-10-30 17:06:55', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2025-10-30 17:06:55', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2025-10-30 17:06:55', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2025-10-30 17:06:55', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2025-10-30 17:06:55', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-10-30 17:06:55', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2025-10-30 17:06:55', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2025-10-30 17:06:55', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2025-10-30 17:06:55', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 145 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (100, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassroomController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/classroom', '127.0.0.1', '内网IP', '{\"classroomId\":2,\"classroomName\":\"计算机网络123\",\"courseName\":\"计算机网络123\",\"endTime\":\"2\",\"params\":{},\"startTime\":\"1\",\"status\":\"1\",\"teacher\":\"12\",\"totalStudents\":1,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 20:16:57', 8);
INSERT INTO `sys_oper_log` VALUES (101, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践01\",\"classNumber\":1,\"endTime\":\"2025-10-31 20:50:28\",\"params\":{},\"sessionId\":1,\"startTime\":\"2025-10-31 20:50:24\",\"status\":1,\"teacher\":\"A\",\"teacherId\":1,\"totalStudents\":22,\"updateBy\":\"admin\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'total_students\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lw/mapper/ClassSessionMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lw.mapper.ClassSessionMapper.updateSession-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE class_session SET class_name = ?, teacher_id = ?, start_time = ?, end_time = ?, status = ?, total_students = ?, teacher = ?, class_number = ?, update_by = ?, update_time = NOW() WHERE session_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'total_students\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'total_students\' in \'field list\'', '2025-10-31 21:37:03', 66);
INSERT INTO `sys_oper_log` VALUES (102, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践01\",\"classNumber\":1,\"endTime\":\"2025-10-31 20:50:28\",\"params\":{},\"sessionId\":1,\"startTime\":\"2025-10-31 20:50:24\",\"status\":1,\"teacher\":\"AA\",\"teacherId\":1,\"totalStudents\":1,\"updateBy\":\"admin\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'total_students\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lw/mapper/ClassSessionMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lw.mapper.ClassSessionMapper.updateSession-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE class_session SET class_name = ?, teacher_id = ?, start_time = ?, end_time = ?, status = ?, total_students = ?, teacher = ?, class_number = ?, update_by = ?, update_time = NOW() WHERE session_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'total_students\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'total_students\' in \'field list\'', '2025-10-31 21:37:16', 1);
INSERT INTO `sys_oper_log` VALUES (103, '课程管理', 2, 'com.ruoyi.web.controller.proj_lw.CourseController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/course', '127.0.0.1', '内网IP', '{\"classNumber\":3,\"college\":\"经济学院\",\"courseCode\":\"MSNU32546\",\"courseId\":3,\"courseName\":\"计算机金融\",\"courseType\":\"选修\",\"createBy\":\"admin\",\"createTime\":\"2025-10-29 18:22:47\",\"credit\":3.5,\"introduction\":\"锻炼学生实践能力\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:39:18', 11);
INSERT INTO `sys_oper_log` VALUES (104, '课程管理', 2, 'com.ruoyi.web.controller.proj_lw.CourseController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/course', '127.0.0.1', '内网IP', '{\"classNumber\":3,\"college\":\"经济学院\",\"courseCode\":\"MSNU32546\",\"courseId\":3,\"courseName\":\"计算机金融应用\",\"courseType\":\"选修\",\"createBy\":\"admin\",\"createTime\":\"2025-10-29 18:22:47\",\"credit\":3.5,\"introduction\":\"锻炼学生实践能力\",\"params\":{},\"status\":\"0\",\"updateBy\":\"admin\",\"updateTime\":\"2025-10-31 21:39:18\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:39:29', 9);
INSERT INTO `sys_oper_log` VALUES (105, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"计算机金融应用\",\"classNumber\":3,\"endTime\":\"2025-10-31 20:57:19\",\"params\":{},\"sessionId\":3,\"startTime\":\"2025-10-31 20:57:16\",\"status\":0,\"teacher\":\"C\",\"teacherId\":3,\"totalStudents\":10,\"updateBy\":\"admin\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lw/mapper/ClassSessionMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lw.mapper.ClassSessionMapper.updateSession-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE class_session SET class_name = ?, teacher_id = ?, start_time = ?, end_time = ?, status = ?, total_students = ?, teacher = ?, class_number = ?, update_by = ?, update_time = NOW() WHERE session_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'', '2025-10-31 21:43:14', 5);
INSERT INTO `sys_oper_log` VALUES (106, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"计算机金融应用\",\"classNumber\":3,\"endTime\":\"2025-10-31 20:57:19\",\"params\":{},\"sessionId\":3,\"startTime\":\"2025-10-31 20:57:16\",\"status\":0,\"teacher\":\"C\",\"teacherId\":3,\"totalStudents\":10,\"updateBy\":\"admin\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lw/mapper/ClassSessionMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lw.mapper.ClassSessionMapper.updateSession-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE class_session SET class_name = ?, teacher_id = ?, start_time = ?, end_time = ?, status = ?, total_students = ?, teacher = ?, class_number = ?, update_by = ?, update_time = NOW() WHERE session_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'', '2025-10-31 21:43:19', 3);
INSERT INTO `sys_oper_log` VALUES (107, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"计算机金融应用\",\"classNumber\":3,\"endTime\":\"2025-10-31 20:57:19\",\"params\":{},\"sessionId\":3,\"startTime\":\"2025-10-31 20:57:16\",\"status\":0,\"teacher\":\"C\",\"teacherId\":3,\"totalStudents\":10,\"updateBy\":\"admin\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lw/mapper/ClassSessionMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lw.mapper.ClassSessionMapper.updateSession-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE class_session SET class_name = ?, teacher_id = ?, start_time = ?, end_time = ?, status = ?, total_students = ?, teacher = ?, class_number = ?, update_by = ?, update_time = NOW() WHERE session_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'update_by\' in \'field list\'', '2025-10-31 21:43:25', 3);
INSERT INTO `sys_oper_log` VALUES (108, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"计算机金融应用\",\"classNumber\":3,\"createBy\":\"\",\"endTime\":\"2025-10-31 20:57:19\",\"params\":{},\"sessionId\":3,\"startTime\":\"2025-10-31 20:57:16\",\"status\":0,\"teacher\":\"C\",\"teacherId\":3,\"totalStudents\":30,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:49:01', 15);
INSERT INTO `sys_oper_log` VALUES (109, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践02\",\"classNumber\":1,\"createBy\":\"\",\"endTime\":\"2025-10-31 20:56:20\",\"params\":{},\"sessionId\":2,\"startTime\":\"2025-10-31 20:56:09\",\"status\":0,\"teacher\":\"B\",\"teacherId\":2,\"totalStudents\":30,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:50:05', 7);
INSERT INTO `sys_oper_log` VALUES (110, '课堂管理', 3, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/proj_lw/session/2', '127.0.0.1', '内网IP', '[2]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:50:59', 6);
INSERT INTO `sys_oper_log` VALUES (111, '课堂管理', 1, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践02\",\"classNumber\":1,\"createBy\":\"admin\",\"endTime\":\"2025-10-31 21:55:41\",\"params\":{},\"sessionId\":4,\"startTime\":\"2025-10-31 21:55:37\",\"status\":0,\"teacher\":\"B\",\"teacherId\":2,\"totalStudents\":36}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:55:44', 21);
INSERT INTO `sys_oper_log` VALUES (112, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践02\",\"classNumber\":1,\"createBy\":\"admin\",\"createTime\":\"2025-10-31 21:55:44\",\"endTime\":\"2025-10-31 21:55:41\",\"params\":{},\"sessionId\":4,\"startTime\":\"2025-10-30 21:55:37\",\"status\":2,\"teacher\":\"B\",\"teacherId\":2,\"totalStudents\":42,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:56:17', 9);
INSERT INTO `sys_oper_log` VALUES (113, '课堂管理', 1, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"计算机网络\",\"classNumber\":2,\"createBy\":\"admin\",\"endTime\":\"2025-10-31 21:56:43\",\"params\":{},\"sessionId\":5,\"startTime\":\"2025-10-31 00:00:00\",\"status\":1,\"teacher\":\"ASA\",\"teacherId\":1,\"totalStudents\":30}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:56:45', 5);
INSERT INTO `sys_oper_log` VALUES (114, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"计算机网络\",\"classNumber\":2,\"createBy\":\"admin\",\"createTime\":\"2025-10-31 21:56:45\",\"endTime\":\"2025-10-31 21:56:43\",\"params\":{},\"sessionId\":5,\"startTime\":\"2025-10-31 00:00:00\",\"status\":1,\"teacher\":\"ASA\",\"teacherId\":3,\"totalStudents\":36,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:56:55', 9);
INSERT INTO `sys_oper_log` VALUES (115, '课堂管理', 3, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/proj_lw/session/5', '127.0.0.1', '内网IP', '[5]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:57:08', 9);
INSERT INTO `sys_oper_log` VALUES (116, '保存抽取记录', 1, 'com.ruoyi.web.controller.proj_myx.RandomPickController.savePick()', 'POST', 1, 'admin', '研发部门', '/proj_myx/random/pick/save', '127.0.0.1', '内网IP', '{\"pickTime\":\"2025-10-31 21:58:52.006\",\"sessionId\":1,\"studentId\":37,\"teacherId\":0}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 21:58:52', 7);
INSERT INTO `sys_oper_log` VALUES (117, '课堂管理', 1, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践03\",\"classNumber\":1,\"createBy\":\"admin\",\"endTime\":\"2025-10-31 22:01:34\",\"params\":{},\"sessionId\":6,\"startTime\":\"2025-10-31 22:01:31\",\"status\":0,\"teacher\":\"123\",\"teacherId\":2,\"totalStudents\":40}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 22:01:38', 4);
INSERT INTO `sys_oper_log` VALUES (118, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践03\",\"classNumber\":1,\"createBy\":\"admin\",\"createTime\":\"2025-10-31 22:01:38\",\"endTime\":\"2025-10-31 22:01:34\",\"params\":{},\"sessionId\":6,\"startTime\":\"2025-10-31 22:01:31\",\"status\":0,\"teacher\":\"11111\",\"teacherId\":2,\"totalStudents\":40,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 22:01:44', 5);
INSERT INTO `sys_oper_log` VALUES (119, '课堂管理', 3, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.remove()', 'DELETE', 1, 'admin', '研发部门', '/proj_lw/session/6', '127.0.0.1', '内网IP', '[6]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-31 22:01:48', 4);
INSERT INTO `sys_oper_log` VALUES (120, '保存抽取记录', 1, 'com.ruoyi.web.controller.proj_myx.RandomPickController.savePick()', 'POST', 1, 'admin', '研发部门', '/proj_myx/random/pick/save', '127.0.0.1', '内网IP', '{\"pickTime\":\"2025-11-01 00:42:02.917\",\"sessionId\":1,\"studentId\":16,\"teacherId\":0}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 00:42:03', 229);
INSERT INTO `sys_oper_log` VALUES (121, '课堂管理', 1, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践01\",\"classNumber\":1,\"createBy\":\"admin\",\"endTime\":\"2025-11-20 00:00:00\",\"params\":{},\"sessionId\":7,\"startTime\":\"2025-11-07 00:00:00\",\"status\":0,\"teacher\":\"C\",\"teacherId\":1,\"totalStudents\":30}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 00:42:58', 23);
INSERT INTO `sys_oper_log` VALUES (122, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践03\",\"classNumber\":1,\"createBy\":\"admin\",\"createTime\":\"2025-11-01 00:42:58\",\"endTime\":\"2025-11-20 00:00:00\",\"params\":{},\"sessionId\":7,\"startTime\":\"2025-11-07 00:00:00\",\"status\":0,\"teacher\":\"C\",\"teacherId\":1,\"totalStudents\":30,\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 00:43:14', 15);
INSERT INTO `sys_oper_log` VALUES (123, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"\",\"content\":\"1111\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-05 00:00:00\",\"params\":{},\"sessionId\":7,\"title\":\"1111\",\"totalScore\":100}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Table \'ry-vue.class_homework\' doesn\'t exist\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassHomeworkMapper.insertHomework-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_homework (course_id, session_id, title, content, total_score, deadline, attachments, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Table \'ry-vue.class_homework\' doesn\'t exist\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Table \'ry-vue.class_homework\' doesn\'t exist', '2025-11-01 00:45:10', 341);
INSERT INTO `sys_oper_log` VALUES (124, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"\",\"content\":\"1111\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-05 00:00:00\",\"params\":{},\"sessionId\":7,\"title\":\"1111\",\"totalScore\":100}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Table \'ry-vue.class_homework\' doesn\'t exist\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassHomeworkMapper.insertHomework-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_homework (course_id, session_id, title, content, total_score, deadline, attachments, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Table \'ry-vue.class_homework\' doesn\'t exist\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Table \'ry-vue.class_homework\' doesn\'t exist', '2025-11-01 00:45:54', 10);
INSERT INTO `sys_oper_log` VALUES (125, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"\",\"content\":\"12222222222222222222221321\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-02 11:22:15\",\"homeworkId\":1,\"params\":{},\"sessionId\":7,\"title\":\"111111111111111111\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 00:49:29', 20);
INSERT INTO `sys_oper_log` VALUES (126, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"\",\"content\":\"12222222222222222222221321\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-02 11:22:15\",\"homeworkId\":2,\"params\":{},\"sessionId\":7,\"title\":\"111111111111111111\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 00:49:45', 13);
INSERT INTO `sys_oper_log` VALUES (127, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"\",\"content\":\"3333333333333333333333333\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-07 00:00:00\",\"homeworkId\":3,\"params\":{},\"sessionId\":7,\"title\":\"11111111111\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 00:57:16', 234);
INSERT INTO `sys_oper_log` VALUES (128, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":3,\"params\":{},\"remark\":\"\",\"studentId\":121212121,\"submissionFiles\":\"改革开放史论文撰写模板.doc\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassStudentHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassStudentHomeworkMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_student_homework (homework_id, student_id, submission_files, submit_time, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'', '2025-11-01 01:26:34', 404);
INSERT INTO `sys_oper_log` VALUES (129, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":3,\"params\":{},\"remark\":\"\",\"studentId\":121212121,\"submissionFiles\":\"改革开放史论文撰写模板.doc\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassStudentHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassStudentHomeworkMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_student_homework (homework_id, student_id, submission_files, submit_time, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'', '2025-11-01 01:29:50', 7);
INSERT INTO `sys_oper_log` VALUES (130, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":3,\"params\":{},\"remark\":\"\",\"studentId\":12121212121,\"submissionFiles\":\"改革开放史论文撰写模板.doc\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassStudentHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassStudentHomeworkMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_student_homework (homework_id, student_id, submission_files, submit_time, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'', '2025-11-01 01:30:24', 6);
INSERT INTO `sys_oper_log` VALUES (131, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":3,\"params\":{},\"remark\":\"\",\"studentId\":12121212121,\"submissionFiles\":\"改革开放史论文撰写模板.doc\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassStudentHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassStudentHomeworkMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_student_homework (homework_id, student_id, submission_files, submit_time, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'', '2025-11-01 01:31:10', 5);
INSERT INTO `sys_oper_log` VALUES (132, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":2,\"params\":{},\"remark\":\"\",\"studentId\":121212121,\"submissionFiles\":\"改革开放史论文撰写模板.doc\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\r\n### The error may exist in com/ruoyi/proj_lwj/mapper/ClassStudentHomeworkMapper.java (best guess)\r\n### The error may involve com.ruoyi.proj_lwj.mapper.ClassStudentHomeworkMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO class_student_homework (homework_id, student_id, submission_files, submit_time, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'submission_files\' in \'field list\'', '2025-11-01 01:31:35', 5);
INSERT INTO `sys_oper_log` VALUES (133, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":3,\"params\":{},\"remark\":\"\",\"studentHomeworkId\":1,\"studentId\":1212121,\"submissionFiles\":\"改革开放史论文撰写模板.doc\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:32:57', 19);
INSERT INTO `sys_oper_log` VALUES (134, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101013529A007.doc\",\"content\":\"小程序\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-11 12:00:00\",\"homeworkId\":4,\"params\":{},\"sessionId\":7,\"title\":\"研开第8周作业\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:35:38', 19);
INSERT INTO `sys_oper_log` VALUES (135, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101015431A001.doc\",\"content\":\"99\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-06 00:00:00\",\"homeworkId\":5,\"params\":{},\"sessionId\":7,\"title\":\"99\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:54:32', 206);
INSERT INTO `sys_oper_log` VALUES (136, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":5,\"params\":{},\"remark\":\"\",\"status\":1,\"studentHomeworkId\":2,\"studentId\":121212,\"submissionFiles\":\"改革开放史论文撰写模板.doc\",\"submitTime\":\"2025-11-01 01:54:57\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:54:57', 17);
INSERT INTO `sys_oper_log` VALUES (137, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":5,\"params\":{},\"remark\":\"\",\"status\":1,\"studentHomeworkId\":3,\"studentId\":12,\"submissionFiles\":\"改革开放史论文撰写模板.doc\",\"submitTime\":\"2025-11-01 01:58:19\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:58:19', 9);
INSERT INTO `sys_oper_log` VALUES (138, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101015911A004.doc\",\"content\":\"77\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-03 07:00:00\",\"homeworkId\":6,\"params\":{},\"sessionId\":7,\"title\":\"77\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:59:12', 10);
INSERT INTO `sys_oper_log` VALUES (139, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":6,\"params\":{},\"remark\":\"\",\"status\":1,\"studentHomeworkId\":4,\"studentId\":77,\"submissionFiles\":\"改革开放史论文撰写模板.doc\",\"submitTime\":\"2025-11-01 01:59:38\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 01:59:38', 12);
INSERT INTO `sys_oper_log` VALUES (140, '课堂管理', 2, 'com.ruoyi.web.controller.proj_lw.ClassSessionController.edit()', 'PUT', 1, 'admin', '研发部门', '/proj_lw/session', '127.0.0.1', '内网IP', '{\"className\":\"研究与开发实践03\",\"classNumber\":1,\"createBy\":\"admin\",\"createTime\":\"2025-11-01 00:42:58\",\"endTime\":\"2025-11-20 00:00:00\",\"params\":{},\"sessionId\":7,\"startTime\":\"2025-11-07 00:00:00\",\"status\":0,\"teacher\":\"C\",\"teacherId\":1,\"totalStudents\":32,\"updateBy\":\"admin\",\"updateTime\":\"2025-11-01 00:43:14\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 15:12:04', 31);
INSERT INTO `sys_oper_log` VALUES (141, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101151240A001.doc\",\"content\":\"小程序\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-07 00:00:00\",\"homeworkId\":7,\"params\":{},\"sessionId\":7,\"title\":\"研开第8周\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 15:12:41', 19);
INSERT INTO `sys_oper_log` VALUES (142, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101151240A001.doc\",\"content\":\"小程序\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-07 00:00:00\",\"homeworkId\":8,\"params\":{},\"sessionId\":7,\"title\":\"研开第8周\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 15:12:49', 10);
INSERT INTO `sys_oper_log` VALUES (143, '作业管理', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.add()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework', '127.0.0.1', '内网IP', '{\"attachments\":\"/profile/upload/2025/11/01/改革开放史论文撰写模板_20251101151240A001.doc\",\"content\":\"小程序\",\"courseId\":1,\"createBy\":\"admin\",\"deadline\":\"2025-11-07 00:00:00\",\"homeworkId\":9,\"params\":{},\"sessionId\":1,\"title\":\"研开第8周\",\"totalScore\":100}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 15:13:28', 11);
INSERT INTO `sys_oper_log` VALUES (144, '作业提交', 1, 'com.ruoyi.web.controller.proj_lwj.ClassHomeworkController.submit()', 'POST', 1, 'admin', '研发部门', '/proj_lwj/homework/submit', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"homeworkId\":8,\"params\":{},\"remark\":\"\",\"status\":1,\"studentHomeworkId\":5,\"studentId\":12121111,\"submissionFiles\":\"改革开放史论文撰写模板.doc\",\"submitTime\":\"2025-11-01 15:13:52\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-11-01 15:13:52', 20);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '岗位信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2025-10-30 17:06:55', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2025-10-30 17:06:55', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2025-10-30 17:06:55', '', NULL, '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色和部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
INSERT INTO `sys_role_menu` VALUES (2, 110);
INSERT INTO `sys_role_menu` VALUES (2, 111);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
INSERT INTO `sys_role_menu` VALUES (2, 114);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 116);
INSERT INTO `sys_role_menu` VALUES (2, 117);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1000);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
INSERT INTO `sys_role_menu` VALUES (2, 1049);
INSERT INTO `sys_role_menu` VALUES (2, 1050);
INSERT INTO `sys_role_menu` VALUES (2, 1051);
INSERT INTO `sys_role_menu` VALUES (2, 1052);
INSERT INTO `sys_role_menu` VALUES (2, 1053);
INSERT INTO `sys_role_menu` VALUES (2, 1054);
INSERT INTO `sys_role_menu` VALUES (2, 1055);
INSERT INTO `sys_role_menu` VALUES (2, 1056);
INSERT INTO `sys_role_menu` VALUES (2, 1057);
INSERT INTO `sys_role_menu` VALUES (2, 1058);
INSERT INTO `sys_role_menu` VALUES (2, 1059);
INSERT INTO `sys_role_menu` VALUES (2, 1060);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2025-11-01 15:11:53', '2025-10-30 17:06:55', 'admin', '2025-10-30 17:06:55', '', NULL, '管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'ry', '若依', '00', 'ry@qq.com', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2025-10-30 17:06:55', '2025-10-30 17:06:55', 'admin', '2025-10-30 17:06:55', '', NULL, '测试员');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户和角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

SET FOREIGN_KEY_CHECKS = 1;
