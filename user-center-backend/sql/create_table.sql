# 数据库初始化

-- 创建库
create database if not exists partner_matching;

-- 切换库
use partner_matching;

# 导入示例用户
INSERT INTO user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, isDelete, userRole, planetCode) VALUES ('江喜原', 'jxy', 'https://himg.bdimg.com/sys/portraitn/item/public.1.e137c1ac.yS1WqOXfSWEasOYJ2-0pvQ', null, '3bde3d5b0fabb4c368527b83393f8ff7', null, null, 0, '2023-08-06 14:14:22', '2023-08-06 14:39:37', 0, 1, '1');

CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `tagName` varchar(256) NOT NULL COMMENT '标签名称',
  `userId` bigint(20) NOT NULL COMMENT '用户 id',
  `parentId` bigint(20) DEFAULT NULL COMMENT '父标签 id',
  `isParent` tinyint(4) DEFAULT NULL COMMENT '0 - 不是父标签，1 - 是',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='标签'


CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(256) NOT NULL COMMENT '队伍名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `maxNum` int(11) NOT NULL DEFAULT '1' COMMENT '最大人数',
  `expireTime` datetime DEFAULT NULL COMMENT '过期时间',
  `userId` bigint(20) DEFAULT NULL COMMENT '用户id',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '0 - 公开，1 - 私有，2 - 加密',
  `password` varchar(512) DEFAULT NULL COMMENT '密码',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COMMENT='队伍'


CREATE TABLE `user` (
  `username` varchar(256) DEFAULT NULL COMMENT '用户昵称',
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) DEFAULT NULL COMMENT '账号',
  `avatarUrl` varchar(1024) DEFAULT NULL COMMENT '用户头像',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别',
  `userPassword` varchar(512) NOT NULL COMMENT '密码',
  `phone` varchar(128) DEFAULT NULL COMMENT '电话',
  `email` varchar(512) DEFAULT NULL COMMENT '邮箱',
  `userStatus` int(11) NOT NULL DEFAULT '0' COMMENT '状态 0 - 正常',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `userRole` int(11) NOT NULL DEFAULT '0' COMMENT '用户角色 0 - 普通用户 1 - 管理员',
  `planetCode` varchar(512) DEFAULT NULL COMMENT '星球编号',
  `tags` varchar(1024) DEFAULT NULL COMMENT '标签 Json 列表',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=910306 DEFAULT CHARSET=utf8 COMMENT='用户'


CREATE TABLE `user_team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) DEFAULT NULL COMMENT '用户id',
  `teamId` bigint(20) DEFAULT NULL COMMENT '队伍id',
  `joinTime` datetime DEFAULT NULL COMMENT '加入时间',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='用户队伍关系'