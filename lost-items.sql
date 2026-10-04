/*
 Navicat Premium Data Transfer

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3306
 Source Schema         : lost-items

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 31/07/2026 11:07:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分类图标',
  `category_sort` int NOT NULL DEFAULT 0 COMMENT '显示排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`category_id`) USING BTREE,
  UNIQUE INDEX `category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '物品分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '电子产品', '/profile/upload/4eb56dc066e041cf99fc2081f94f4ab6_电子产品.png.png', 1, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (2, '证件卡片', '/profile/upload/ef356c60c57e4a8fbe164d2490987c55_证件卡片.png.png', 2, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (3, '书籍资料', '/profile/upload/83d25e3102df44e58cff249dd2e0ee28_书籍资料.png.png', 3, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (4, '衣物饰品', '/profile/upload/eea1b1b36e7b49aba20e43d149065e1d_衣物饰品.png.png', 4, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (5, '钱包现金', '/profile/upload/1da2c503f7de4658b6a2aa3eba7038c3_钱包现金.png.png', 5, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (6, '钥匙门卡', '/profile/upload/2eafe82c26024dfebd399b7a7f21e990_钥匙门卡.png.png', 6, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (7, '水杯餐具', '/profile/upload/2f0be756f11f4204864311a3cecb86c5_水杯餐具.png.png', 7, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (8, '运动器材', '/profile/upload/67937a149bd54455abfb9a09be4b7669_运动器材.png.png', 8, '2026-07-29 15:00:00');
INSERT INTO `category` VALUES (9, '其他物品', '/profile/upload/c682db8f8ef14b4d9ea3e866f6374bba_其他物品.png.png', 99, '2026-07-29 15:00:00');

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item`  (
  `item_id` int NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '信息类型(lost寻物启事 found失物招领)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '详细描述',
  `category_id` int NOT NULL COMMENT '分类ID',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图片(多张用逗号分隔)',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '丢失/拾取地点',
  `happen_time` date NULL DEFAULT NULL COMMENT '丢失/拾取日期',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '联系方式',
  `urgent` int NOT NULL DEFAULT 0 COMMENT '是否急寻(0否 1是)',
  `reward` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '悬赏说明',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'open' COMMENT '状态(open进行中 done已完成)',
  `views` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `user_id` int NOT NULL COMMENT '发布人用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  UNIQUE INDEX `item_id`(`item_id` ASC) USING BTREE,
  INDEX `fk_item_category`(`category_id` ASC) USING BTREE,
  INDEX `fk_item_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '失物/寻物信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of item
-- ----------------------------
INSERT INTO `item` VALUES (1, 'found', 'AirPods Pro 耳机（白色充电盒）', '在图书馆三楼自习区靠窗座位捡到，盒子上有轻微划痕，可描述特征认领。', 1, 'https://picsum.photos/seed/airpods/700/500,https://picsum.photos/seed/airpods2/700/500', '图书馆三楼自习区', '2026-07-28', '微信：chenxi_2026', 0, '', 'open', 56, 1, '2026-07-30 09:20:00');
INSERT INTO `item` VALUES (2, 'lost', '校园一卡通（张同学）', '中午在第二食堂二楼吃饭时丢失，卡套是蓝色的，里面还有一张照片，很着急！', 2, 'https://picsum.photos/seed/card/700/500', '第二食堂二楼', '2026-07-29', '电话：13800006621', 1, '', 'open', 89, 1, '2026-07-30 12:40:00');
INSERT INTO `item` VALUES (3, 'found', '黑色短款钱包', '篮球场边长椅上捡到一个黑色钱包，内有现金和几张卡，请失主描述内部物品认领。', 5, 'https://picsum.photos/seed/wallet/700/500', '东区篮球场', '2026-07-29', 'QQ：81234567', 0, '', 'open', 134, 1, '2026-07-30 15:10:00');
INSERT INTO `item` VALUES (4, 'lost', 'iPad（灰色保护套）+ Apple Pencil', '在自习室复习时短暂离开，回来发现 iPad 不见了，里面有重要的复习资料，急寻！', 1, 'https://picsum.photos/seed/ipad/700/500,https://picsum.photos/seed/ipad2/700/500', '第四教学楼自习室', '2026-07-28', '电话：15000003308', 1, '50元', 'open', 312, 1, '2026-07-29 21:40:00');
INSERT INTO `item` VALUES (5, 'found', '一串钥匙（带小熊挂件）', '晚上跑步时在操场东侧跑道捡到，共4把钥匙，挂着一只棕色小熊玩偶。', 6, 'https://picsum.photos/seed/keys/700/500', '操场东侧跑道', '2026-07-28', '微信：runner_night', 0, '', 'open', 73, 1, '2026-07-29 20:05:00');
INSERT INTO `item` VALUES (6, 'found', '高等数学课本（下册）', '3教101捡到一本高数下册，扉页写了名字但看不清，书里夹着不少笔记。', 3, 'https://picsum.photos/seed/book/700/500', '第三教学楼 101', '2026-07-27', 'QQ：66889900', 0, '', 'open', 28, 1, '2026-07-28 15:20:00');
INSERT INTO `item` VALUES (7, 'lost', '银色手链', '在西门公交站丢失的手链已经找回，感谢捡到并联系我的好心同学！', 4, 'https://picsum.photos/seed/chain/700/500', '西门公交站', '2026-07-24', '微信：taotao_ss', 0, '奶茶一杯', 'done', 156, 1, '2026-07-25 10:30:00');
INSERT INTO `item` VALUES (8, 'found', '蓝白格子雨伞', '教学楼门口伞架上多出来的一把伞，已由失主成功认领。', 9, 'https://picsum.photos/seed/umbrella/700/500', '第一教学楼门口', '2026-07-23', 'QQ：10203040', 0, '', 'done', 98, 1, '2026-07-24 08:50:00');

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `menu_id` int NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '菜单名称',
  `parent_id` int NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_sort` int NOT NULL COMMENT '菜单排序',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '组件路径',
  `menu_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '菜单类型(M目录 C菜单)',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单图标',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`menu_id`) USING BTREE,
  UNIQUE INDEX `menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (1, '系统管理', 0, 99, 'system', NULL, 'M', '系统管理', '2026-01-25 17:48:15');
INSERT INTO `menu` VALUES (2, '用户管理', 1, 1, 'users', 'system/user/index', 'C', '用户管理', '2026-01-25 17:48:16');
INSERT INTO `menu` VALUES (3, '角色管理', 1, 2, 'role', 'system/role/index', 'C', '角色管理', '2026-01-26 17:23:54');
INSERT INTO `menu` VALUES (4, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', '菜单管理', '2026-01-26 17:25:07');
INSERT INTO `menu` VALUES (5, '首页', 0, 1, 'index', 'system/index', 'C', '首页', '2026-01-28 20:01:23');
INSERT INTO `menu` VALUES (6, '内容管理', 0, 2, 'content', NULL, 'M', '内容管理', '2026-07-29 14:04:47');
INSERT INTO `menu` VALUES (7, '公告管理', 6, 1, 'notice', 'content/notice/index', 'C', '公告管理', '2026-07-29 12:00:00');
INSERT INTO `menu` VALUES (8, '分类管理', 6, 2, 'category', 'content/category/index', 'C', '分类管理', '2026-07-29 15:00:00');
INSERT INTO `menu` VALUES (9, '业务管理', 0, 3, 'item', NULL, 'M', '业务管理', '2026-07-29 15:50:04');
INSERT INTO `menu` VALUES (10, '物品管理', 9, 1, 'list', 'item/index', 'C', '业务管理', '2026-07-31 11:10:00');
INSERT INTO `menu` VALUES (11, '消息管理', 9, 2, 'message', 'message/index', 'C', '公告管理', '2026-07-31 15:00:00');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `item_id` int NOT NULL COMMENT '关联物品ID',
  `from_user_id` int NOT NULL COMMENT '发送人用户ID',
  `to_user_id` int NOT NULL COMMENT '接收人用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '消息内容',
  `is_read` int NOT NULL DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  UNIQUE INDEX `message_id`(`message_id` ASC) USING BTREE,
  INDEX `fk_message_item`(`item_id` ASC) USING BTREE,
  INDEX `fk_message_from_user`(`from_user_id` ASC) USING BTREE,
  INDEX `fk_message_to_user`(`to_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户留言消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '公告内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notice_id`) USING BTREE,
  UNIQUE INDEX `notice_id`(`notice_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '公告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '平台上线啦，捡到物品请及时发布招领信息', '<p>各位同学：</p><p>校园失物招领平台正式上线啦！今后大家丢失或捡到物品，都可以在平台上发布信息，让失物更快回到主人身边。</p><p>捡到物品的同学，请尽量在第一时间发布招领信息，并填写清楚拾取地点、时间和物品特征，方便失主辨认。</p><p>感谢每一位拾金不昧的同学，让我们一起共建温暖校园！</p>', '2026-07-29 09:00:00');
INSERT INTO `notice` VALUES (2, '认领物品时请核对物品特征，谨防冒领', '<p>为保障失主权益，认领物品时请注意以下几点：</p><p>1. 认领前请失主准确描述物品的外观特征、丢失时间和地点；</p><p>2. 贵重物品（手机、电脑、证件等）建议当面核对后再交接，必要时可查验相关凭证；</p><p>3. 发布招领信息时，建议隐去物品的关键特征（如卡号后几位、挂件细节等），留作认领时核对；</p><p>4. 如遇冒领行为，请及时联系学生服务中心处理。</p>', '2026-07-29 10:30:00');
INSERT INTO `notice` VALUES (3, '贵重物品建议移交至学生服务中心保管', '<p>同学们捡到现金、证件、电子产品等贵重物品时，若短时间内无人认领，建议移交至学生服务中心统一保管。</p><p>学生服务中心地址：大学生活动中心一楼大厅（西侧窗口）。</p><p>工作时间：周一至周五 8:30-17:30，周末 9:00-16:00。</p><p>移交后平台会同步更新物品去向，失主可凭有效证件前往认领。</p>', '2026-07-29 14:00:00');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `role_id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称',
  `role_sort` int NOT NULL COMMENT '显示排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'admin', 1, '超级管理员', '2026-01-22 17:41:30');
INSERT INTO `role` VALUES (2, 'user', 2, '普通用户', '2026-01-22 17:41:49');

-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu`  (
  `role_id` int NOT NULL COMMENT '角色ID',
  `menu_id` int NOT NULL COMMENT '菜单ID',
  INDEX `fk_role_menu_role`(`role_id` ASC) USING BTREE,
  INDEX `fk_role_menu_menu`(`menu_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menu_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色菜单关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `sex` int NULL DEFAULT NULL COMMENT '用户性别(0男 1女)',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '密码',
  `role_id` int NOT NULL DEFAULT 2 COMMENT '角色ID',
  `wx_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信小程序openId',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `idx_wx_openid`(`wx_openid` ASC) USING BTREE,
  INDEX `fk_user_role`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 0, NULL, 'admin', 1, NULL, '2026-01-14 11:29:43');
INSERT INTO `user` VALUES (2, '花菜菜', 0, '/profile/avatar/1f7dd28e45ff4f7191103155f774a608.jpeg', '2ba5d1b9-3e21-47ac-b32a-0783fef1f244', 2, 'oTPgI6DF-6ZX5bbUJH0DCuGdheG4', '2026-07-29 12:08:12');

SET FOREIGN_KEY_CHECKS = 1;
