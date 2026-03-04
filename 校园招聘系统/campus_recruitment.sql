/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : campus_recruitment

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 24/07/2025 10:29:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for application
-- ----------------------------
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投递ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `position_id` bigint NOT NULL COMMENT '职位ID',
  `resume_id` bigint NOT NULL COMMENT '简历ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '投递状态(0待查看/1已查看/2通过筛选/3面试邀请/4不合适/5已录用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_position_id`(`position_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_resume_id`(`resume_id` ASC) USING BTREE,
  CONSTRAINT `fk_application_position_id` FOREIGN KEY (`position_id`) REFERENCES `position` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_application_student_id` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投递记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of application
-- ----------------------------
INSERT INTO `application` VALUES (3, 2, 6, 2, 3, '2025-07-02 22:27:36', NULL);
INSERT INTO `application` VALUES (6, 5, 5, 5, 1, '2025-07-02 22:27:36', NULL);
INSERT INTO `application` VALUES (7, 6, 1, 10, 3, '2025-07-06 23:01:00', '2025-07-24 10:25:24');

-- ----------------------------
-- Table structure for company
-- ----------------------------
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
  `industry` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属行业',
  `company_size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业规模',
  `company_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业地址',
  `company_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业Logo',
  `company_intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '企业简介',
  `verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否认证(0未认证/1已认证)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_company_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of company
-- ----------------------------
INSERT INTO `company` VALUES (1, 11, '阿里巴巴集团', '互联网/IT', '10000人以上', '杭州市余杭区文一西路969号', '/img/1751878520586.jpg', '阿里巴巴集团创立于1999年，是一家提供电子商务在线交易平台的公司，业务包括B2B贸易、网上零售、购物搜索引擎、第三方支付和云计算服务。', 1, '2025-07-02 22:26:01', '2025-07-06 23:04:32');
INSERT INTO `company` VALUES (2, 12, '腾讯', '互联网/IT', '10000人以上', '深圳市南山区高新科技园北区', '/img/1753267667676.jpg', '腾讯是中国最大的互联网综合服务提供商之一，也是中国服务用户最多的互联网企业之一。成立于1998年11月，是中国最大的互联网综合服务提供商之一，也是中国服务用户最多的互联网企业之一。', 1, '2025-07-02 22:26:01', '2025-07-06 23:04:34');
INSERT INTO `company` VALUES (3, 13, '百度', '互联网/IT', '10000人以上', '北京市海淀区上地十街10号百度大厦', '/img/1753267722749.jpg', '百度是拥有强大互联网基础的领先AI公司。百度愿景是：成为最懂用户，并能帮助人们成长的全球顶级高科技公司。', 1, '2025-07-02 22:26:01', '2025-07-06 23:04:35');
INSERT INTO `company` VALUES (4, 14, '华为技术有限公司', '通信/电子', '10000人以上', '深圳市龙岗区坂田华为基地', '/api/files/img/huawei_logo.png', '华为创立于1987年，是全球领先的ICT（信息与通信）基础设施和智能终端提供商，致力于把数字世界带入每个人、每个家庭、每个组织，构建万物互联的智能世界。', 0, '2025-07-02 22:26:01', '2025-07-02 22:26:06');
INSERT INTO `company` VALUES (5, 15, '小米科技', '互联网/电子', '5000-10000人', '北京市海淀区清河中街68号华润五彩城', '/api/files/img/xiaomi_logo.png', '小米公司正式成立于2010年4月，是一家以手机、智能硬件和IoT平台为核心的互联网公司。小米的使命是，始终坚持做\"感动人心、价格厚道\"的好产品，让全球每个人都能享受科技带来的美好生活。', 0, '2025-07-02 22:26:01', NULL);

-- ----------------------------
-- Table structure for interview
-- ----------------------------
DROP TABLE IF EXISTS `interview`;
CREATE TABLE `interview`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  `application_id` bigint NOT NULL COMMENT '投递ID',
  `interview_time` datetime NOT NULL COMMENT '面试时间',
  `interview_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面试地点',
  `interview_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面试类型(现场/视频/电话)',
  `interview_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '面试备注',
  `interview_status` int NOT NULL DEFAULT 0 COMMENT '面试状态(0待确认/1已确认/2已完成/3已取消)',
  `interview_result` int NOT NULL DEFAULT 0 COMMENT '面试结果(0未知/1通过/2未通过)',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '面试反馈',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_application_id`(`application_id` ASC) USING BTREE,
  INDEX `idx_interview_time`(`interview_time` ASC) USING BTREE,
  INDEX `idx_interview_status`(`interview_status` ASC) USING BTREE,
  CONSTRAINT `fk_interview_application_id` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '面试表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of interview
-- ----------------------------
INSERT INTO `interview` VALUES (1, 3, '2025-07-05 22:27:52', '深圳市龙岗区坂田华为基地A3栋', '现场', '请提前30分钟到达面试地点，并携带身份证、学生证等证件', 1, 0, NULL, '2025-07-02 22:27:52', NULL);
INSERT INTO `interview` VALUES (3, 7, '2025-07-17 00:00:00', '公司', '现场', '', 2, 1, 'OK', '2025-07-07 10:21:10', '2025-07-07 10:22:33');
INSERT INTO `interview` VALUES (4, 7, '2025-07-31 00:00:00', '大厅', '现场', '', 0, 0, NULL, '2025-07-24 10:25:24', NULL);

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读(0未读/1已读)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 11, 6, 'INTERVIEW_NOTICE', 'Java开发工程师面试邀请', '尊敬的张三同学，恭喜您通过我们的简历筛选，诚邀您参加Java开发工程师岗位的面试。请尽快回复确认面试时间。', 1, '2025-07-02 22:27:52', '2025-07-24 09:59:15');
INSERT INTO `message` VALUES (2, 12, 6, 'RESUME_STATUS', '简历已查看通知', '您投递的前端开发工程师岗位简历已被查看，请耐心等待后续通知。', 1, '2025-07-02 22:27:52', '2025-07-24 09:59:28');
INSERT INTO `message` VALUES (3, 14, 7, 'INTERVIEW_NOTICE', '通信工程师面试邀请', '尊敬的李四同学，恭喜您通过我们的简历筛选，诚邀您参加通信工程师岗位的面试。面试时间已安排在3天后，请查看面试详情。', 1, '2025-07-02 22:27:52', '2025-07-24 09:59:13');
INSERT INTO `message` VALUES (4, 13, 9, 'APPLICATION_STATUS', '算法工程师简历筛选通过', '尊敬的赵六同学，恭喜您的简历通过我们的初步筛选，我们将尽快安排面试，请保持手机畅通。', 0, '2025-07-02 22:27:52', '2025-07-24 09:58:48');
INSERT INTO `message` VALUES (5, 1, 14, 'VERIFICATION_NOTICE', '企业认证申请通知', '您的企业认证申请已收到，我们将在3个工作日内完成审核，请耐心等待。', 0, '2025-07-02 22:27:52', '2025-07-24 09:58:44');
INSERT INTO `message` VALUES (6, 1, 15, 'VERIFICATION_NOTICE', '企业认证申请通知', '您的企业认证申请已收到，我们将在3个工作日内完成审核，请耐心等待。', 0, '2025-07-02 22:27:52', '2025-07-24 09:58:45');
INSERT INTO `message` VALUES (8, 11, 6, 'APPLICATION_STATUS', '简历投递状态更新', '您投递的 Java开发工程师 职位简历状态已更新为 已查看', 1, '2025-07-10 10:28:59', '2025-07-24 10:24:37');
INSERT INTO `message` VALUES (9, 11, 6, 'APPLICATION_STATUS', '简历投递状态更新', '您投递的 Java开发工程师 职位简历状态已更新为 已录用', 1, '2025-07-10 10:29:02', '2025-07-24 10:24:29');
INSERT INTO `message` VALUES (10, 11, 6, 'APPLICATION_STATUS', '简历投递状态更新', '您投递的 Java开发工程师 职位简历状态已更新为 已查看', 1, '2025-07-10 10:57:01', '2025-07-24 10:24:30');
INSERT INTO `message` VALUES (11, 11, 6, 'APPLICATION_STATUS', '简历投递状态更新', '您投递的 Java开发工程师 职位简历状态已更新为 已录用', 1, '2025-07-10 10:57:06', '2025-07-24 10:24:30');
INSERT INTO `message` VALUES (12, 11, 6, 'APPLICATION_STATUS', '简历投递状态更新', '您投递的 Java开发工程师 职位简历状态已更新为 通过筛选', 1, '2025-07-10 10:57:10', '2025-07-19 11:00:19');
INSERT INTO `message` VALUES (13, 1, 6, 'VERIFICATION_NOTICE', '测试 ', '测试', 0, '2025-07-24 09:33:33', NULL);
INSERT INTO `message` VALUES (15, 1, 7, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (16, 1, 8, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (17, 1, 9, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (18, 1, 10, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (19, 1, 11, 'SYSTEM_NOTICE', '111', '11', 1, '2025-07-24 10:08:08', '2025-07-24 10:11:21');
INSERT INTO `message` VALUES (20, 1, 12, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (21, 1, 13, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (22, 1, 14, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (23, 1, 15, 'SYSTEM_NOTICE', '111', '11', 0, '2025-07-24 10:08:08', NULL);
INSERT INTO `message` VALUES (24, 11, 6, 'INTERVIEW_NOTICE', 'Java开发工程师面试邀请', '您好，您申请的Java开发工程师职位已通过简历筛选，阿里巴巴集团诚邀您参加现场面试。\n面试时间：2025-07-31 00:00\n面试地点：大厅\n', 0, '2025-07-24 10:25:24', NULL);

-- ----------------------------
-- Table structure for position
-- ----------------------------
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '职位ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `position_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职位名称',
  `position_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位类型',
  `position_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位类别',
  `salary_min` int NULL DEFAULT NULL COMMENT '最低薪资',
  `salary_max` int NULL DEFAULT NULL COMMENT '最高薪资',
  `education_requirement` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历要求',
  `experience_requirement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '经验要求',
  `work_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作地点',
  `position_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '职位描述',
  `position_requirement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '职位要求',
  `position_count` int NULL DEFAULT NULL COMMENT '招聘人数',
  `status` int NOT NULL DEFAULT 2 COMMENT '职位状态(0下线/1上线/2审核中)',
  `deadline` date NULL DEFAULT NULL COMMENT '截止日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_position_name`(`position_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_position_company_id` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '职位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of position
-- ----------------------------
INSERT INTO `position` VALUES (1, 1, 'Java开发工程师', '全职', '软件开发', 15000, 25000, '本科', '应届毕业生', '杭州', '负责阿里巴巴电商平台的后端服务开发和维护，参与系统架构设计和优化。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉Java编程语言和常用框架(Spring、MyBatis等)；\n3. 了解分布式系统设计和微服务架构；\n4. 良好的团队协作能力和沟通能力。', 10, 1, '2025-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:41');
INSERT INTO `position` VALUES (2, 1, '前端开发工程师', '全职', '软件开发', 12000, 20000, '本科', '应届毕业生', '杭州', '负责阿里巴巴电商平台的前端界面开发和优化，提升用户体验。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉HTML、CSS、JavaScript等前端技术；\n3. 熟悉Vue、React等前端框架；\n4. 有良好的设计感和用户体验意识。', 8, 1, '2025-12-30', '2025-07-02 22:26:42', '2025-07-19 15:05:39');
INSERT INTO `position` VALUES (3, 2, '游戏开发工程师', '全职', '游戏开发', 18000, 30000, '本科', '应届毕业生', '深圳', '参与腾讯游戏的开发和维护，负责游戏核心功能实现和性能优化。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉C++或Unity开发；\n3. 有游戏开发相关经验或项目经历；\n4. 热爱游戏，有创新精神和团队合作意识。', 5, 1, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:43');
INSERT INTO `position` VALUES (4, 2, '产品经理', '全职', '产品管理', 15000, 25000, '本科', '应届毕业生', '深圳', '负责腾讯产品的需求分析、功能设计和产品规划，推动产品落地和迭代优化。', '1. 本科及以上学历，专业不限；\n2. 良好的产品思维和用户体验意识；\n3. 优秀的沟通能力和项目管理能力；\n4. 对互联网产品有浓厚兴趣和独到见解。', 3, 0, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:45');
INSERT INTO `position` VALUES (5, 3, '算法工程师', '全职', '人工智能', 20000, 35000, '硕士', '应届毕业生', '北京', '参与百度AI相关算法研究和开发，推动AI技术在各业务场景的应用。', '1. 计算机、数学等相关专业硕士及以上学历；\n2. 扎实的机器学习和深度学习理论基础；\n3. 熟练掌握Python、TensorFlow或PyTorch；\n4. 有相关研究经历或发表过论文者优先。', 6, 1, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:46');
INSERT INTO `position` VALUES (6, 4, '通信工程师', '校招', '通信工程', 18000, 28000, '本科', '应届毕业生', '深圳', '参与华为通信设备的研发和测试，负责网络架构设计和优化。', '1. 通信工程、电子工程等相关专业本科及以上学历；\n2. 熟悉通信原理和网络协议；\n3. 具备较强的问题分析和解决能力；\n4. 良好的团队合作精神和抗压能力。', 10, 2, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:51');
INSERT INTO `position` VALUES (7, 4, '硬件工程师', '校招', '硬件研发', 16000, 26000, '本科', '应届毕业生', '深圳', '参与华为终端产品的硬件设计和开发，负责电路设计和优化。', '1. 电子工程、自动化等相关专业本科及以上学历；\n2. 熟悉电子电路设计和PCB布局；\n3. 了解硬件产品开发流程；\n4. 具备较强的动手能力和创新精神。', 8, 1, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:52');
INSERT INTO `position` VALUES (8, 5, '软件测试工程师', '校招', '软件测试', 12000, 18000, '本科', '应届毕业生', '北京', '负责小米产品的软件测试，包括功能测试、性能测试和自动化测试。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉软件测试理论和方法；\n3. 了解自动化测试工具和框架；\n4. 细心耐心，有较强的责任心和团队合作精神。', 5, 2, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:53');
INSERT INTO `position` VALUES (9, 5, '市场营销专员', '校招', '市场营销', 10000, 15000, '本科', '应届毕业生', '北京', '参与小米产品的市场推广和营销活动策划，负责内容创作和社交媒体运营。', '1. 市场营销、新闻传播等相关专业本科及以上学历；\n2. 良好的文案功底和创意能力；\n3. 熟悉各类社交媒体平台和营销工具；\n4. 对数码产品有浓厚兴趣，了解行业动态。', 3, 1, '2024-12-31', '2025-07-02 22:26:42', '2025-07-06 23:03:55');
INSERT INTO `position` VALUES (10, 1, '数据分析师', '全职', '数据分析', 14000, 22000, '本科', '应届毕业生', '杭州', '负责阿里巴巴数据的分析和挖掘，提供业务决策支持。', '1. 统计、计算机相关专业本科及以上学历；\n2. 熟练使用SQL、Python等工具；\n3. 良好的数据敏感度和分析能力。', 5, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (11, 2, 'UI设计师', '全职', '设计', 13000, 21000, '本科', '应届毕业生', '深圳', '负责腾讯产品的界面设计和用户体验优化。', '1. 设计相关专业本科及以上学历；\n2. 熟练使用Photoshop、Sketch等工具；\n3. 良好的审美和创新能力。', 4, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (12, 3, '机器学习工程师', '全职', '人工智能', 22000, 38000, '硕士', '应届毕业生', '北京', '参与百度机器学习模型的开发和优化。', '1. 计算机相关专业硕士及以上学历；\n2. 扎实的机器学习基础；\n3. 熟练使用TensorFlow或PyTorch。', 7, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (13, 4, '网络工程师', '校招', '网络工程', 17000, 27000, '本科', '应届毕业生', '深圳', '负责华为网络设备的配置和维护。', '1. 网络工程相关专业本科及以上学历；\n2. 熟悉网络协议和设备；\n3. CCNA或同等证书优先。', 6, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (14, 5, '产品运营专员', '校招', '产品运营', 11000, 16000, '本科', '应届毕业生', '北京', '负责小米产品的运营和用户增长。', '1. 市场营销相关专业本科及以上学历；\n2. 熟悉数据分析工具；\n3. 良好的沟通能力。', 4, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (15, 1, 'DevOps工程师', '全职', '运维', 16000, 26000, '本科', '应届毕业生', '杭州', '负责阿里巴巴系统的部署和监控。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉Docker、Kubernetes；\n3. 了解CI/CD流程。', 5, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (16, 2, '后端开发工程师', '全职', '软件开发', 15000, 25000, '本科', '应届毕业生', '深圳', '负责腾讯后端服务的开发和维护。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉Java或Go语言；\n3. 了解微服务架构。', 6, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (17, 3, '数据科学家', '全职', '数据科学', 25000, 40000, '博士', '应届毕业生', '北京', '负责百度数据科学研究和模型构建。', '1. 数据科学相关专业博士学历；\n2. 精通统计学和机器学习；\n3. 有发表论文经验。', 3, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (18, 4, '嵌入式工程师', '校招', '嵌入式开发', 18000, 28000, '本科', '应届毕业生', '深圳', '负责华为嵌入式系统的开发。', '1. 电子工程相关专业本科及以上学历；\n2. 熟悉C语言和RTOS；\n3. 有硬件调试经验。', 5, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (19, 5, '内容运营专员', '校招', '内容运营', 10000, 15000, '本科', '应届毕业生', '北京', '负责小米内容平台的运营和编辑。', '1. 新闻传播相关专业本科及以上学历；\n2. 良好的写作能力；\n3. 熟悉社交媒体。', 4, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (20, 1, '安全工程师', '全职', '网络安全', 18000, 30000, '本科', '应届毕业生', '杭州', '负责阿里巴巴系统的安全防护和漏洞修复。', '1. 信息安全相关专业本科及以上学历；\n2. 熟悉网络安全工具；\n3. 有渗透测试经验。', 4, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (21, 2, '移动开发工程师', '全职', '移动开发', 16000, 26000, '本科', '应届毕业生', '深圳', '负责腾讯移动应用的开发。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉Android或iOS开发；\n3. 有App发布经验。', 5, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (22, 3, '前端工程师', '全职', '软件开发', 14000, 24000, '本科', '应届毕业生', '北京', '负责百度前端界面的开发和优化。', '1. 计算机相关专业本科及以上学历；\n2. 熟练使用React或Vue；\n3. 了解性能优化。', 6, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (23, 4, '项目经理', '校招', '项目管理', 20000, 30000, '本科', '应届毕业生', '深圳', '负责华为项目的规划和执行。', '1. 项目管理相关专业本科及以上学历；\n2. 熟悉PMP知识；\n3. 优秀的领导力。', 3, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (24, 5, '人力资源专员', '校招', '人力资源', 12000, 18000, '本科', '应届毕业生', '北京', '负责小米招聘和员工关系管理。', '1. 人力资源相关专业本科及以上学历；\n2. 熟悉劳动法；\n3. 良好的沟通技能。', 4, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);
INSERT INTO `position` VALUES (25, 1, '云计算工程师', '全职', '云计算', 17000, 28000, '本科', '应届毕业生', '杭州', '负责阿里巴巴云平台的开发和维护。', '1. 计算机相关专业本科及以上学历；\n2. 熟悉AWS或阿里云；\n3. 了解容器技术。', 5, 1, '2027-12-31', '2025-07-02 22:26:42', NULL);

-- ----------------------------
-- Table structure for resume
-- ----------------------------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `resume_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '简历名称',
  `resume_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简历内容(JSON格式)',
  `resume_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简历文件路径',
  `pdf_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'PDF文件路径',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认简历(0否/1是)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_resume_student_id` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '简历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resume
-- ----------------------------
INSERT INTO `resume` VALUES (5, 6, '张三的简历', '{\"personalInfo\":{\"name\":\"张三\",\"gender\":\"男\",\"birthDate\":\"2001-07-04\",\"phone\":\"15252311111\",\"email\":\"179614511@qq.com\"},\"education\":[{\"school\":\"北京大学\",\"major\":\"无\",\"degree\":\"本科\",\"timeRange\":[\"2020-07\",\"2020-08\"],\"description\":\"\",\"startTime\":\"2020-07\",\"endTime\":\"2020-08\"}],\"experience\":[{\"type\":\"工作经历\",\"name\":\"小米\",\"role\":\"测试\",\"timeRange\":[\"2024-07\",\"2024-08\"],\"description\":\"\",\"startTime\":\"2024-07\",\"endTime\":\"2024-08\"}],\"skills\":[{\"type\":\"证书\",\"name\":\"CET4\",\"level\":\"555\",\"date\":\"2020-07-17\",\"description\":\"高分！\"},{\"type\":\"证书\",\"name\":\"CET6\",\"level\":\"580\",\"date\":\"2021-01-21\",\"description\":\"高分\"}],\"jobIntention\":{},\"selfEvaluation\":\"喜欢编程！\"}', '/resume/1751595588407.pdf', NULL, 0, '2025-07-02 22:27:36', '2025-07-05 10:06:49');
INSERT INTO `resume` VALUES (10, 6, '小明的简历', '{\"basicInfo\":{\"name\":\"蒋xx\",\"gender\":\"男\",\"birthDate\":\"2025-07-24\",\"email\":\"1796145611@qq.com\",\"phone\":\"13151515151\",\"address\":\"中国台湾省\",\"photo\":\"\"},\"education\":[{\"school\":\"北京大学\",\"major\":\"计算机科学与技术\",\"degree\":\"本科\",\"startDate\":\"2020-09\",\"endDate\":\"2024-07\",\"gpa\":\"4.0\",\"description\":\"嘿！\"}],\"workExperience\":[{\"company\":\"小米集团\",\"position\":\"研发人员\",\"startDate\":\"2024-06\",\"endDate\":\"2025-06\",\"description\":\"主导基于STM32的智能机械臂项目开发，负责硬件接口设计与软件开发。采用HAL库实现微控制器编程，优化系统响应速度20%。与5人团队协作，成功将项目从概念转化为原型产品，获得公司创新奖。\"},{\"company\":\"华为\",\"position\":\"测试人员\",\"startDate\":\"2025-06\",\"endDate\":\"2025-07\",\"description\":\"负责Java自动化测试框架开发与维护，编写测试用例覆盖率达90%以上。使用Selenium和TestNG实现Web应用自动化测试，提升测试效率35%。\"}],\"projectExperience\":[{\"name\":\"基于STM32的智能机械臂\",\"role\":\"研发人员\",\"startDate\":\"2023-06\",\"endDate\":\"2023-07\",\"description\":\"基于STM32的智能机械臂项目，使用STM32微控制器和HAL库进行硬件接口设计和软件开发。\",\"techStack\":\"STM32, HAL\"}],\"skills\":[{\"name\":\"C++\",\"proficiency\":5,\"description\":\"精通C++语言，具有5年以上嵌入式系统开发经验，主导开发基于STM32的智能机械臂项目，优化系统性能20%。擅长解决复杂算法问题。\"},{\"name\":\"JAVA\",\"proficiency\":3,\"description\":\"熟悉Java全栈开发，熟练使用Spring Boot、MyBatis等主流框架。具备自动化测试框架开发经验，测试用例覆盖率达90%以上。\"}],\"selfEvaluation\":\"3年Java全栈开发经验，精通Spring Boot、MyBatis等主流框架，具备自动化测试框架开发能力，测试用例覆盖率达90%以上。主导开发基于STM32的智能机械臂项目，通过优化系统架构提升20%性能。熟悉微服务架构和分布式系统设计，具有良好的代码规范和团队协作能力。\",\"jobIntention\":{\"position\":\"JAVA工程师\",\"industry\":\"计算机\",\"location\":\"南京\",\"salaryExpectation\":\"12000\"},\"certificates\":[{\"name\":\"CET4\",\"issueDate\":\"2020-02\",\"issuingOrganization\":\"CET\",\"description\":\"1\"}],\"languages\":[{\"name\":\"英语\",\"proficiency\":\"高级\",\"description\":\"2\"}]}', NULL, NULL, 1, '2025-07-05 09:50:18', '2025-07-24 10:26:48');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `student_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号',
  `college` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院',
  `major` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  `education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历',
  `graduation_year` int NULL DEFAULT NULL COMMENT '毕业年份',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no` ASC) USING BTREE,
  CONSTRAINT `fk_student_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (2, 7, '2020001002', '信息工程学院', '通信工程', '本科', 2024, '2025-07-02 22:26:01', NULL);
INSERT INTO `student` VALUES (3, 8, '2020001003', '经济管理学院', '市场营销', '本科', 2024, '2025-07-02 22:26:01', NULL);
INSERT INTO `student` VALUES (4, 9, '2021002001', '计算机学院', '计算机科学与技术', '硕士', 2024, '2025-07-02 22:26:01', NULL);
INSERT INTO `student` VALUES (5, 10, '2021002002', '人工智能学院', '人工智能', '硕士', 2024, '2025-07-02 22:26:01', NULL);
INSERT INTO `student` VALUES (6, 6, '2006051039', '计算机工程学院', '计算机科学与技术', '硕士', 2024, '2025-07-03 23:31:34', '2025-07-07 16:21:18');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `sex` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `role_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码(STUDENT/COMPANY/ADMIN)',
  `status` int NOT NULL DEFAULT 1 COMMENT '账号状态(0禁用/1启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$ns4ez.ij619yAiRaIrMhd.bhT4jorD5FX/h3hM565qlDQ7v3p06J.', '系统管理员', '男', '/img/1753322980853.png', 'admin@qq.com', '13123456789', 'ADMIN', 1, '2025-07-02 17:11:44', '2025-07-24 10:09:54');
INSERT INTO `user` VALUES (6, 'student1', '$2a$10$kXvoIsySk56ZUH5NFp/Gzuke/Tw8K7FdtudLkck6kjPCgHbqEUwqa', '张三', '男', '/img/1753267191676.png', 'student1@example.com', '13800138001', 'STUDENT', 1, '2025-07-02 22:26:01', '2025-07-23 18:39:51');
INSERT INTO `user` VALUES (7, 'student2', '$2a$10$pHJQFZ5mA3YYwx.pbJQdO.7I5OmFYBYA9td2I7Nb7r9vNG1VXxq2C', '李四', NULL, '/api/files/img/default_avatar.jpg', 'student2@example.com', '13800138002', 'STUDENT', 1, '2025-07-02 22:26:01', NULL);
INSERT INTO `user` VALUES (8, 'student3', '$2a$10$pHJQFZ5mA3YYwx.pbJQdO.7I5OmFYBYA9td2I7Nb7r9vNG1VXxq2C', '王五', NULL, '/api/files/img/default_avatar.jpg', 'student3@example.com', '13800138003', 'STUDENT', 1, '2025-07-02 22:26:01', NULL);
INSERT INTO `user` VALUES (9, 'student4', '$2a$10$pHJQFZ5mA3YYwx.pbJQdO.7I5OmFYBYA9td2I7Nb7r9vNG1VXxq2C', '赵六', NULL, '/api/files/img/default_avatar.jpg', 'student4@example.com', '13800138004', 'STUDENT', 1, '2025-07-02 22:26:01', NULL);
INSERT INTO `user` VALUES (10, 'student5', '$2a$10$pHJQFZ5mA3YYwx.pbJQdO.7I5OmFYBYA9td2I7Nb7r9vNG1VXxq2C', '钱七', NULL, '/api/files/img/default_avatar.jpg', 'student5@example.com', '13800138005', 'STUDENT', 1, '2025-07-02 22:26:01', NULL);
INSERT INTO `user` VALUES (11, 'company1', '$2a$10$kXvoIsySk56ZUH5NFp/Gzuke/Tw8K7FdtudLkck6kjPCgHbqEUwqa', '阿里巴巴招聘专员', NULL, '/api/files/img/default_avatar.jpg', 'hr@alibaba.com', '13900139001', 'COMPANY', 1, '2025-07-02 22:26:01', '2025-07-03 23:18:40');
INSERT INTO `user` VALUES (12, 'company2', '$2a$10$kXvoIsySk56ZUH5NFp/Gzuke/Tw8K7FdtudLkck6kjPCgHbqEUwqa', '腾讯招聘专员', NULL, '/api/files/img/default_avatar.jpg', 'hr@tencent.com', '13900139002', 'COMPANY', 1, '2025-07-02 22:26:01', '2025-07-23 18:40:42');
INSERT INTO `user` VALUES (13, 'company3', '$2a$10$kXvoIsySk56ZUH5NFp/Gzuke/Tw8K7FdtudLkck6kjPCgHbqEUwqa', '百度招聘专员', NULL, '/api/files/img/default_avatar.jpg', 'hr@baidu.com', '13900139003', 'COMPANY', 1, '2025-07-02 22:26:01', '2025-07-23 18:40:42');
INSERT INTO `user` VALUES (14, 'company4', '$2a$10$kXvoIsySk56ZUH5NFp/Gzuke/Tw8K7FdtudLkck6kjPCgHbqEUwqa', '华为招聘专员', NULL, '/api/files/img/default_avatar.jpg', 'hr@huawei.com', '13900139004', 'COMPANY', 1, '2025-07-02 22:26:01', '2025-07-23 18:40:43');
INSERT INTO `user` VALUES (15, 'company5', '$2a$10$kXvoIsySk56ZUH5NFp/Gzuke/Tw8K7FdtudLkck6kjPCgHbqEUwqa', '小米招聘专员', NULL, '/api/files/img/default_avatar.jpg', 'hr@xiaomi.com', '13900139005', 'COMPANY', 1, '2025-07-02 22:26:01', '2025-07-23 18:40:44');

SET FOREIGN_KEY_CHECKS = 1;
