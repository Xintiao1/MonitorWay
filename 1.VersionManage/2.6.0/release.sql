#2022/01/07 gengjb 报表增加定时发送字段
ALTER TABLE `mw_report_table`
ADD COLUMN `send_time` tinyint(1) NULL COMMENT '定时发送开关';

#20220108 发送消息
DROP TABLE IF EXISTS `mw_system_message`;
CREATE TABLE `mw_system_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_text` varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '消息创建时间',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `own_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息所属人',
  `module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属哪个模块',
  `read_status` int(11) NULL DEFAULT 0 COMMENT '读取状态 1.已读 0.未读',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3712 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

#20220109 ljb 更新最新的拓扑图标
delete from mw_assets_logo;
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (19, 3, 20, '防火墙', '/mwapi/basics/assets-logo/anquanshebei-normal.png', '/mwapi/basics/assets-logo/anquanshebei-alert.png', '/mwapi/basics/assets-logo/anquanshebei-severity.png', '/mwapi/basics/assets-logo/anquanshebei-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (154, 5, 0, '虚拟化', '/mwapi/basics/assets-logo/xunihua-normal.png', '/mwapi/basics/assets-logo/xunihua-alert.png', '/mwapi/basics/assets-logo/xunihua-severity.png', '/mwapi/basics/assets-logo/xunihua-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (156, 4, 0, '存储设备', '/mwapi/basics/assets-logo/cunchushebei-normal.png', '/mwapi/basics/assets-logo/cunchushebei-alert.png', '/mwapi/basics/assets-logo/cunchushebei-severity.png', '/mwapi/basics/assets-logo/cunchushebei-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (158, 10, 0, '混合云', '/mwapi/basics/assets-logo/hunheyun-normal.png', '/mwapi/basics/assets-logo/hunheyun-alert.png', '/mwapi/basics/assets-logo/hunheyun-severity.png', '/mwapi/basics/assets-logo/hunheyun-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (159, 7, 0, '中间件', '/mwapi/basics/assets-logo/zhongjiianjian-normal.png', '/mwapi/basics/assets-logo/zhongjiianjian-alert.png', '/mwapi/basics/assets-logo/zhongjiianjian-severity.png', '/mwapi/basics/assets-logo/zhongjiianjian-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (160, 8, 0, '数据库', '/mwapi/basics/assets-logo/shujuku-normal.png', '/mwapi/basics/assets-logo/shujuku-alert.png', '/mwapi/basics/assets-logo/shujuku-severity.png', '/mwapi/basics/assets-logo/shujuku-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (161, 6, 0, '应用', '/mwapi/basics/assets-logo/yingyong-normal.png', '/mwapi/basics/assets-logo/zhongjiianjian-alert.png', '/mwapi/basics/assets-logo/zhongjiianjian-severity.png', '/mwapi/basics/assets-logo/zhongjiianjian-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (183, 1, 0, '服务器', '/mwapi/basics/assets-logo/fuwuqi-normal.png', '/mwapi/basics/assets-logo/hunheyun-alert.png', '/mwapi/basics/assets-logo/fuwuqi-severity.png', '/mwapi/basics/assets-logo/fuwuqi-urgency.png');
INSERT INTO mw_assets_logo (id, asset_type, asset_sub_type, logo_desc, normal_logo, alert_logo, severity_logo, urgency_logo) VALUES (184, 2, 0, '网络设备', '/mwapi/basics/assets-logo/wangluoshebei-normal.png', '/mwapi/basics/assets-logo/wangluoshebei-alert.png', '/mwapi/basics/assets-logo/wangluoshebei-severity.png', '/mwapi/basics/assets-logo/wangluoshebei-urgency.png');

#2022/1/9 lbq 邮件新增邮件标题
ALTER TABLE `monitor`.`mw_alert_email_rule`
ADD COLUMN `title` varchar(255) NULL COMMENT '邮件标题' AFTER `personal`;

#2022/1/9 lbq
ALTER TABLE `monitor`.`mw_alert_action`
ADD COLUMN `effect_time_select` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `state`,
ADD COLUMN `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间' AFTER `effect_time_select`,
ADD COLUMN `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间' AFTER `start_time`,
ADD COLUMN `alarm_compression_select` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `end_time`,
ADD COLUMN `custom_time` int(32) NULL DEFAULT NULL COMMENT '自定义时间' AFTER `alarm_compression_select`,
ADD COLUMN `time_unit` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间单位' AFTER `custom_time`,
ADD COLUMN `custom_num` int(32) NULL DEFAULT NULL COMMENT '自定义次数' AFTER `time_unit`,
ADD COLUMN `num_unit` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '自定义次数单位' AFTER `custom_num`,
ADD COLUMN `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `num_unit`,
ADD COLUMN `success_num` int(11) NULL DEFAULT 0 AFTER `name`,
ADD COLUMN `fail_num` int(11) NULL DEFAULT 0 AFTER `success_num`,
ADD COLUMN `area` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '区域数据选择' AFTER `fail_num`;

# 合规检测功能 gui
-- ----------------------------
-- Table structure for mw_config_manage_detect_exec_log
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_detect_exec_log`;
CREATE TABLE `mw_config_manage_detect_exec_log`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_uuid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告的UUID',
  `assets_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资产ID',
  `rule_id` int(11) NULL DEFAULT NULL COMMENT '规则ID',
  `policy_id` int(11) NULL DEFAULT NULL COMMENT '策略ID',
  `report_id` int(11) NULL DEFAULT NULL COMMENT '报告ID',
  `handle_state` tinyint(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态（0：未处理，1：处理中  2：处理结束  3：处理失败）',
  `rule_level` tinyint(2) NULL DEFAULT NULL COMMENT '匹配等级（0.一般 1.警告 2.严重 3：匹配失败）',
  `create_date` datetime(0) NOT NULL COMMENT '创建时间',
  `modification_date` datetime(0) NOT NULL COMMENT '修改时间',
  `creator` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '创建人',
  `modifier` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '修改人',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '合约检测——执行记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mw_config_manage_detect_relation
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_detect_relation`;
CREATE TABLE `mw_config_manage_detect_relation`  (
  `id` int(11) NOT NULL COMMENT '合规检测各表主键ID',
  `type_id` int(11) NOT NULL COMMENT '所属文件夹ID',
  `type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点属性-POLICY 政策 -REPORT 报表 -RULE 规则'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '配置管理的检测报告，策略和规则的对应关系\r\n当type为REPORT时，id是mw_config_manage_report_manage的主键ID，type_id为mw_config_manage_policy_manage的主键ID\r\n当type为POLICY时，id是mw_config_manage_policy_manage的主键ID，type_id为mw_config_manage_rule_manage的主键ID\r\n' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mw_config_manage_policy_manage
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_policy_manage`;
CREATE TABLE `mw_config_manage_policy_manage`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `policy_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '策略名称',
  `policy_tree_group` int(11) NULL DEFAULT NULL COMMENT '策略所在树状图哪个组',
  `policy_describe` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '策略描述',
  `config_type` int(11) UNSIGNED NULL DEFAULT 0 COMMENT '配置类别(0：全部  1：Running-config  2:StartUp-Config)',
  `detect_assets_type` tinyint(4) NULL DEFAULT NULL COMMENT '检测资产类别（0：厂商  1：自定义）',
  `detect_condition` tinyint(4) NULL DEFAULT NULL COMMENT '判断条件（0：等于  1：不等于），仅在厂商ID不为0时生效',
  `vendor_id` int(11) NULL DEFAULT NULL COMMENT '关联厂商ID，如果不为0，则表示关联了厂商信息',
  `create_date` datetime(0) NOT NULL COMMENT '创建时间',
  `modification_date` datetime(0) NOT NULL COMMENT '修改时间',
  `creator` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `modifier` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '合规检测--检测报告信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mw_config_manage_policy_relation
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_policy_relation`;
CREATE TABLE `mw_config_manage_policy_relation`  (
  `policy_id` int(11) NOT NULL COMMENT '策略主键ID',
  `assets_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资产ID',
  INDEX `idx_policy_id`(`policy_id`) USING BTREE COMMENT '策略ID索引',
  INDEX `idx_assets_id`(`assets_id`) USING BTREE COMMENT '资产ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '策略和资产的对应关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mw_config_manage_report_manage
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_report_manage`;
CREATE TABLE `mw_config_manage_report_manage`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告名称',
  `report_tree_group` int(11) NULL DEFAULT NULL COMMENT '报告所在树状图哪个组',
  `report_describe` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告描述',
  `state` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态（0：失效，1：生效）',
  `report_uuid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最新的报告UUID（未生成则为null）',
  `create_date` datetime(0) NOT NULL COMMENT '创建时间',
  `modification_date` datetime(0) NOT NULL COMMENT '修改时间',
  `creator` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '创建人',
  `modifier` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '修改人',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '合规检测--检测报告信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mw_config_manage_rule_manage
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_rule_manage`;
CREATE TABLE `mw_config_manage_rule_manage`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '规则名称',
  `rule_tree_group` int(11) NULL DEFAULT NULL COMMENT '规则所在树状图哪个组',
  `rule_describe` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '规则描述',
  `rule_match_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '匹配内容',
  `rule_content_type` tinyint(2) NOT NULL COMMENT '规则匹配类型（0：字符串  1：正则表达式）',
  `rule_match_type` tinyint(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '匹配方式 0：匹配1：不匹配',
  `rule_senior_type` tinyint(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0：为普通检索  1：高级检索',
  `rule_level` int(11) NULL DEFAULT NULL COMMENT '0.一般 1.警告 2.严重',
  `rule_repair_type` int(11) NULL DEFAULT NULL COMMENT '修复脚本的类型',
  `rule_repair_string` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修复脚本命令',
  `create_date` datetime(0) NOT NULL COMMENT '创建时间',
  `modification_date` datetime(0) NOT NULL COMMENT '修改时间',
  `creator` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '创建人',
  `modifier` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '修改人',
  `delete_flag` tinyint(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mw_config_manage_tree_group
-- ----------------------------
DROP TABLE IF EXISTS `mw_config_manage_tree_group`;
CREATE TABLE `mw_config_manage_tree_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'treeid',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点名称',
  `parent_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '父节点名称',
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点属性-POLICY 政策 -REPORT 报表 -RULE 规则',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

# gui 系统管理员权限分配
DELETE  from  mw_role_module_perm_mapper   where module_id = 221 and role_id = 0;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-browse', 0, 221, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-cn_mw_sign', 0, 221, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-create', 0, 221, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-delete', 0, 221, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-editor', 0, 221, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-perform', 0, 221, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-221-secopassword', 0, 221, 'secopassword', 1);


#2022/1/9 lbq 默认为0
ALTER TABLE `monitor`.`mw_alert_action`
MODIFY COLUMN `success_num` int(11) NULL DEFAULT 0 AFTER `num_unit`,
MODIFY COLUMN `fail_num` int(11) NULL DEFAULT 0 AFTER `success_num`;

#2022/1/11 邮件表新增logo字段
ALTER TABLE `monitor`.`mw_alert_email_rule`
ADD COLUMN `logo` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮件图像' AFTER `title`,
ADD COLUMN `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图像路径' AFTER `logo`;

#2022/1/16 ljb
alter table mw_topo_graph add scene longtext null comment '前端控件参数';
alter table mw_topo_graph add stages longtext null comment '前端控件参数';


#2022/1/16 lmm
DROP TABLE IF EXISTS `mw_process_def`;
CREATE TABLE `mw_process_def`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `activiti_process_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '流程定义id',
  `process_instance_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'ProcessInstanceByKey',
  `process_data` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '存贮当时前端参数',
  `status` int(255) NULL DEFAULT 0 COMMENT '流程是否激活 0.已激活 1.未激活 2.删除',
  `version` int(11) NULL DEFAULT NULL COMMENT '当前部署的流程的版本',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `delete_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '流程定义表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `mw_process_module_bind`;
CREATE TABLE `mw_process_module_bind`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `activiti_process_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '流程定义id',
  `process_instance_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '流程创建id',
  `model_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '模型id',
  `action` int(11) NULL DEFAULT NULL COMMENT '模型操作',
  `status` int(255) NULL DEFAULT 0 COMMENT '流程是否修改了',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '流程与模块绑定表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `mw_process_my_task`;
CREATE TABLE `mw_process_my_task`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `process_instance_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '我提交的任务id审批',
  `login_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '提交人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `mw_process_start_activiti`;
CREATE TABLE `mw_process_start_activiti`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `module_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '流程关联的模块id',
  `process_instance_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '启动的工做流id',
  `process_def_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '启动流程的配置模块id',
  `action` int(255) NULL DEFAULT NULL COMMENT '操作数 ',
  `status` int(11) NULL DEFAULT NULL COMMENT '是否启用了（0.启用失败 1.启用成功）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 110 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `mw_system_message`;
CREATE TABLE `mw_system_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_text` varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '消息创建时间',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `own_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息所属人',
  `module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属哪个模块',
  `read_status` int(11) NULL DEFAULT 0 COMMENT '读取状态 1.已读 0.未读',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3950 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `my_unfinish_process`;
CREATE TABLE `my_unfinish_process`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `login_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户登录名',
  `un_finish_process` int(11) NULL DEFAULT NULL COMMENT '代办审批',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#qzg 2022/01/18 模型分组表新增分组类型字段
alter table mw_cmdbmd_group add column group_level int(1) COMMENT '是否内置（1内置，不可删除；0自定义，可删除）';
#qzg 2022/01/18 日志数据源连接表
CREATE TABLE `mw_sys_log_datasource_connection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `connection_type` int(6) DEFAULT NULL,
  `connection_name` varchar(255) DEFAULT NULL COMMENT '数据源连接类型名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
#qzg 2022/01/18 日志数据源设置表
CREATE TABLE `mw_sys_log_datasource_setting` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `datasource_name` varchar(255) DEFAULT NULL COMMENT '数据源名称',
  `datasource_type` varchar(255) DEFAULT NULL COMMENT '数据源类型',
  `ip` varchar(255) DEFAULT NULL COMMENT '地址',
  `port` int(11) DEFAULT NULL COMMENT '端口',
  `connection_type` tinyint(2) DEFAULT NULL COMMENT '连接类型 1：http  2：https',
  `is_pass` tinyint(2) DEFAULT NULL COMMENT '是否认证 true:需要账号认证 false:不需要认证',
  `username` varchar(255) DEFAULT NULL COMMENT '账号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `state` tinyint(2) DEFAULT NULL COMMENT '是否启用  1启用  0禁用',
  `query_es_index` varchar(255) DEFAULT NULL COMMENT '日志查询的索引',
  `delete_flag` tinyint(2) DEFAULT NULL COMMENT '删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
#qzg 2022/01/18 日志数据源类型表
CREATE TABLE `mw_sys_log_datasource_type` (
  `id` int(11) NOT NULL,
  `type` int(6) DEFAULT NULL COMMENT '数据源类型Id',
  `name` varchar(255) DEFAULT NULL COMMENT '数据源类型名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统日志数据源类型表';
#qzg 2022/01/18 日志数据规则设置表
CREATE TABLE `mw_sys_log_rule_mapper` (
  `id` int(12) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(255) DEFAULT NULL COMMENT '规则名称',
  `rule_id` varchar(64) DEFAULT NULL COMMENT '规则id',
  `state` tinyint(1) DEFAULT NULL COMMENT '启用状态',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(255) DEFAULT NULL COMMENT '修改人',
  `update_date` datetime DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint(1) DEFAULT NULL COMMENT '删除标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
#qzg 2022/01/18 日志数据规则表
CREATE TABLE `mw_sys_log_rule_select` (
  `id` varchar(255) NOT NULL,
  `deep` int(32) DEFAULT NULL,
  `condition_unit` varchar(32) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `relation` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `parent_id` varchar(32) DEFAULT NULL,
  `rule_id` varchar(32) DEFAULT NULL,
  KEY `idx_uuid` (`rule_id`) USING BTREE COMMENT 'uuid索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
#qzg 2022/01/18 日志数据标签表
CREATE TABLE `mw_sys_log_tag` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '标签名称',
  `color` varchar(255) DEFAULT NULL COMMENT '标签颜色',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='系统日志标签表';
#qzg 2022/01/18 日志数据标签关联表
CREATE TABLE `mw_sys_log_tag_mapper` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_mapper_id` int(12) DEFAULT NULL COMMENT '规则映射id',
  `tag_id` int(12) DEFAULT NULL COMMENT '标签id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8 COMMENT='系统日志标签表';

# gengjb 新首页增删改查功能
#资产过滤table
CREATE TABLE `mw_newscreen_filter_assets` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `model_data_id` varchar(128) DEFAULT NULL COMMENT '大屏组件数据id',
  `model_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `in_band_ip` varchar(128) DEFAULT NULL COMMENT 'ip地址',
  `assets_type_id` int(11) DEFAULT NULL COMMENT '资产类型',
  `assets_type_sub_id` int(11) DEFAULT NULL COMMENT '资产子类型',
  `monitor_mode` int(11) DEFAULT NULL COMMENT '监控方式',
  `polling_engine` varchar(32) DEFAULT NULL COMMENT '轮训引擎',
  `manufacturer` varchar(128) DEFAULT NULL COMMENT '厂商',
  `specifications` varchar(128) DEFAULT NULL COMMENT '规格型号',
  `time_lag` int(11) DEFAULT NULL COMMENT '刷新间隔',
  `linkInterfaces` varchar(3000) DEFAULT NULL COMMENT '大屏线路组件线路集合',
  `filter_label_id` varchar(128) DEFAULT NULL COMMENT '标签id',
  `filter_org_id` varchar(128) DEFAULT NULL COMMENT '机构id',
  `name` varchar(128) DEFAULT NULL COMMENT '卡片名称存储(方便查询)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1503 DEFAULT CHARSET=utf8;

#模块初始化table
CREATE TABLE `mw_newhomepage_init` (
  `bulk_id` int(11) DEFAULT NULL COMMENT '首页模块类型',
  `bulk_name` varchar(50) DEFAULT NULL COMMENT '首页模块名称',
  `delete_flag` tinyint(2) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#用户模块table
CREATE TABLE `mw_newindex_bulk` (
  `model_data_id` varchar(50) DEFAULT NULL COMMENT '模块唯一id',
  `bulk_id` int(11) DEFAULT NULL COMMENT '模块类型id',
  `bulk_name` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `delete_flag` tinyint(1) unsigned zerofill DEFAULT 0 COMMENT '删除标识：1-删除，0-未删除',
  `create_date` datetime DEFAULT NULL COMMENT '添加数据时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#初始化模块新增语句
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (1, '告警概括', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (2, 'CPU利用率', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (3, '节点丢包率', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (5, '内存', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (6, '节点延时', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (8, '磁盘', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (9, '接口流量', 0);
INSERT INTO `mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`) VALUES (7, '消息统计', 0);

#qzg 2022/01/21 模型字段结构果表新增字段
alter table mw_cmdbmd_properties_value add column expire_remind tinyint(1) DEFAULT NULL COMMENT '时间类型：是否到期提醒';
alter table mw_cmdbmd_properties_value add column before_expiretime int(11) DEFAULT NULL COMMENT '时间类型：到期前时间';
alter table mw_cmdbmd_properties_value add column time_unit varchar(10) DEFAULT NULL COMMENT '时间类型：到期时间类型:秒，分，时，天，月';
#qzg 2022/01/24 消息表新增字段
alter table mw_system_message add column node longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '跳转信息数据';
alter table mw_system_message add column isRedirect tinyint(1) DEFAULT NULL COMMENT '是否跳转';

#gengjb 2022/01/26 报表推送用户表
CREATE TABLE `mw_report_senduser` (
  `report_id` varchar(64) NOT NULL COMMENT '模块主键id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
   group_id int(11) DEFAULT NULL COMMENT '用户组ID',
  `type` varchar(128) DEFAULT NULL COMMENT '类型'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报表发送邮件用户表';

#gengjb 2022/02/07 大屏资产数量表
CREATE TABLE `mw_assetsamount_time` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_amount` int(10) DEFAULT NULL COMMENT '资产数量',
  `census_date` varchar(256) DEFAULT NULL COMMENT '统计日期',
  `update_date` date DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;


#lumingming 2022/02/16 系统定时任务
DROP TABLE IF EXISTS `mw_ncm_timetask_type_mapper`;
CREATE TABLE `mw_ncm_timetask_type_mapper`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `typename` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '类型名称(任务类型下拉框展示名称)',
  `typemethod` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '执行方法全称',
  `typeclass` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '执行方法所在类的全称',
  `selecturl` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联配置查询方法url',
  `configname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联配置查询字段值',
  `configid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联配置查询字段名称',
  `type` int(11) NULL DEFAULT 0 COMMENT '0.表示自定义定时任务 1.表示系统定时任务',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#数据
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (1, '配置备份', 'runMethod', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', '/configManage/browse', 'assetsName', 'id', 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (2, '执行脚本', 'runScript', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', '/configManage/browse', 'assetsName', 'id', 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (3, '测试数据', 'bkctest', 'cn.mw.monitor.timetask.service.impl.MwTimeTaskServiceImpl', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (4, '监控项数据存储15min', 'saveItemHistory', 'cn.mw.time.MWItemHistoryTime', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (5, '监控项数据存储60min', 'saveItemHistoryHour', 'cn.mw.time.MWItemHistoryTime', NULL, NULL, '', 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (6, '知识库点赞存储60min', 'saveKnowledgeLikedHistory', 'cn.mw.time.MWKnowledgeLikedTime', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (7, 'IP地址管理间隔扫描', 'cronBatchScanIp', 'cn.mw.monitor.ipaddressmanage.service.impl.MwIpAddressManageServiceScanImpl', NULL, NULL, 'timing', 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (10, '虚拟化树数据定时更新', 'saveVirtualTree', 'cn.mw.monitor.virtualization.service.impl.MwVirtualServiceImpl', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (11, '获取当前告警', 'saveAlertGetNow', 'cn.mw.time.MWZbxTime', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (12, '获取zabbix监控服务器状态', 'zabbixConnectCheck', 'cn.mw.time.MWZabbixConnetStateTime', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (13, '报表定时发送邮件信息', 'reportSendEmail', 'cn.mw.monitor.report.timer.MwReportTimeSendEmail', NULL, NULL, NULL, 0);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (14, '线路流量统计数据', 'linkReportDailyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (15, '线路流量统计上周数据', 'linkReportWeeklyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (16, '线路流量统计上月数据', 'linkReportMonthlyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (17, '缓存CPU与内存报表天级数据', 'cpuAndMemoryReportDailyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (18, '判断是否需要存储CPU与内存上周数据', 'cpuAndMemoryReportWeeklyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (19, '判断是否需要存储CPU与内存上月数据', 'cpuAndMemoryReportMonthlyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (20, '缓存磁盘使用情况报表天级数据', 'diskUseReportDailyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (21, '判断是否需要存储磁盘使用情况上周数据', 'diskUseReportWeeklyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (22, '判断是否需要存储磁盘使用情况上月数据', 'diskUseReportMonthlyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (23, '缓存资产可用性报表天数据', 'assetUsabilityReportDailyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (24, '判断是否需要资产可用性上周数据', 'assetUsabilityReportWeeklyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (25, '判断是否需要存储资产可用性上月数据', 'assetUsabilityReportMonthlyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (26, '缓存运行状态报表天数据', 'runStateReportDailyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (27, '判断是否需要存储运行状态报表上周数据', 'runStateReportWeeklyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (28, '判断是否需要存储运行状态报表上月数据', 'runStateReportMonthlyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (29, '缓存MPLS报告天数据', 'mplsHistoryReportDailyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (30, '判断是否需要存储MPLS报表上周数据', 'mplsHistoryReportWeeklyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (31, '判断是否需要存储MPLS报表上月数据', 'mplsHistoryReportMonthlyDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);
INSERT INTO `mw_ncm_timetask_type_mapper` VALUES (32, '缓存线路流量报表 1小时执行一次', 'getToDayLInkFlowReportDataCache', 'cn.mw.monitor.report.timer.MwReportManageTimeToo', NULL, NULL, NULL, 1);


#系统定时任务
ALTER TABLE `monitor`.`mw_ncm_timetask_table`
ADD COLUMN `task_type` int(11) NULL DEFAULT 0 COMMENT '0.表示自定义定时任务 1.表示系统定时任务' AFTER `time_custom`;
#数据
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (68, 'gjb', '2022-02-16 17:27:27', 'gjb', '2022-02-16 17:27:27', '线路流量报表每天数据存储', 'S', '1点15分执行一次', '线路流量统计数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'linkReportDailyDataCache', '0 15 1 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (69, 'gjb', '2022-02-16 17:28:15', 'gjb', '2022-02-16 17:28:18', '线路流量报表上周数据存储', 'S', '3点执行一次', '线路流量统计上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'linkReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (70, 'gjb', '2022-02-16 17:28:59', 'gjb', '2022-02-16 17:31:22', '线路流量报表上月数据存储', 'S', '3点执行一次', '线路流量统计上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'linkReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (71, 'gjb', '2022-02-16 17:30:03', 'gjb', '2022-02-16 17:30:03', 'CPU与内存报表每天数据存储', 'S', '4点10分执行一次', '缓存CPU与内存报表天级数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'cpuAndMemoryReportDailyDataCache', '0 10 4 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (72, 'gjb', '2022-02-16 17:30:43', 'gjb', '2022-02-16 17:32:10', 'CPU与内存数据上周数据存储', 'S', '3点执行一次', '判断是否需要存储CPU与内存上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'cpuAndMemoryReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (73, 'gjb', '2022-02-16 17:31:04', 'gjb', '2022-02-16 17:32:08', 'CPU与内存数据上月数据存储', 'S', '3点执行一次', '判断是否需要存储CPU与内存上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'cpuAndMemoryReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (74, 'gjb', '2022-02-16 17:32:04', 'gjb', '2022-02-16 17:32:04', '磁盘使用情况每天数据存储', 'S', '2点10分执行一次', '缓存磁盘使用情况报表天级数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'diskUseReportDailyDataCache', '0 10 2 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (75, 'gjb', '2022-02-16 17:32:46', 'gjb', '2022-02-16 17:32:46', '磁盘使用情况上周数据存储', 'S', '3点执行一次', '判断是否需要存储磁盘使用情况上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'diskUseReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (76, 'gjb', '2022-02-16 17:33:06', 'gjb', '2022-02-16 17:33:59', '磁盘使用情况上月数据存储', 'S', '3点执行一次', '判断是否需要存储磁盘使用情况上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'diskUseReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (77, 'gjb', '2022-02-16 17:33:56', 'gjb', '2022-02-16 17:34:01', '资产可用性报表每天数据存储', 'S', '3点30分执行一次', '缓存资产可用性报表天数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'assetUsabilityReportDailyDataCache', '0 30 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (78, 'gjb', '2022-02-16 17:34:58', 'gjb', '2022-02-16 17:34:58', '资产可用性报表上周数据存储', 'S', '3点执行一次', '判断是否需要资产可用性上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'assetUsabilityReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (79, 'gjb', '2022-02-16 17:35:28', 'gjb', '2022-02-16 17:35:28', '资产可用性报表上月数据存储', 'S', '3点执行一次', '判断是否需要存储资产可用性上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'assetUsabilityReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (80, 'gjb', '2022-02-16 17:36:17', 'gjb', '2022-02-16 17:36:19', '运行状态报表每天数据存储', 'S', '2点01分执行一次', '缓存运行状态报表天数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'runStateReportDailyDataCache', '* 01 2 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (81, 'gjb', '2022-02-16 17:37:01', 'gjb', '2022-02-16 17:37:04', '运行状态报表上周数据存储', 'S', '3点执行一次', '判断是否需要存储运行状态报表上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'runStateReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (82, 'gjb', '2022-02-16 17:37:34', 'gjb', '2022-02-16 17:37:34', '运行状态报表上月数据存储', 'S', '3点执行一次', '判断是否需要存储运行状态报表上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'runStateReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (83, 'gjb', '2022-02-16 17:38:12', 'gjb', '2022-02-16 17:38:12', 'MPLS报表每天数据存储', 'S', '5点30分执行一次', '缓存MPLS报告天数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'mplsHistoryReportDailyDataCache', '0 30 5 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (84, 'gjb', '2022-02-16 17:38:54', 'gjb', '2022-02-16 17:38:54', 'MPLS报表上周数据存储', 'S', '3点执行一次', '判断是否需要存储MPLS报表上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'mplsHistoryReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (85, 'gjb', '2022-02-16 17:39:17', 'gjb', '2022-02-16 17:39:17', 'MPLS报表上月数据存储', 'S', '3点执行一次', '判断是否需要存储MPLS报表上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'mplsHistoryReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (86, 'gjb', '2022-02-16 17:39:58', 'gjb', '2022-02-16 17:40:02', '线路流量报表每小时数据更新', 'S', '从第*分开始,每60分执行一次', '缓存线路流量报表 1小时执行一次', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'getToDayLInkFlowReportDataCache', '0 */60 * * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);

INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (68, 'gjb', '2022-02-16 17:27:27', 'gengjb', '2022-02-21 10:52:08', '线路流量报表每天数据存储', 'S', '10点50分21秒执行一次', '线路流量统计数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'linkReportDailyDataCache', '21 50 10 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '10:50:21', 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (69, 'gjb', '2022-02-16 17:28:15', 'gjb', '2022-02-16 17:28:18', '线路流量报表上周数据存储', 'S', '3点执行一次', '线路流量统计上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'linkReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (70, 'gjb', '2022-02-16 17:28:59', 'gjb', '2022-02-16 17:31:22', '线路流量报表上月数据存储', 'S', '3点执行一次', '线路流量统计上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'linkReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (71, 'gjb', '2022-02-16 17:30:03', 'gjb', '2022-02-16 17:30:03', 'CPU与内存报表每天数据存储', 'S', '4点10分执行一次', '缓存CPU与内存报表天级数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'cpuAndMemoryReportDailyDataCache', '0 10 4 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (72, 'gjb', '2022-02-16 17:30:43', 'gjb', '2022-02-16 17:32:10', 'CPU与内存数据上周数据存储', 'S', '3点执行一次', '判断是否需要存储CPU与内存上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'cpuAndMemoryReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (73, 'gjb', '2022-02-16 17:31:04', 'gjb', '2022-02-16 17:32:08', 'CPU与内存数据上月数据存储', 'S', '3点执行一次', '判断是否需要存储CPU与内存上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'cpuAndMemoryReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (74, 'gjb', '2022-02-16 17:32:04', 'gjb', '2022-02-16 17:32:04', '磁盘使用情况每天数据存储', 'S', '2点10分执行一次', '缓存磁盘使用情况报表天级数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'diskUseReportDailyDataCache', '0 10 2 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (75, 'gjb', '2022-02-16 17:32:46', 'gjb', '2022-02-16 17:32:46', '磁盘使用情况上周数据存储', 'S', '3点执行一次', '判断是否需要存储磁盘使用情况上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'diskUseReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (76, 'gjb', '2022-02-16 17:33:06', 'gjb', '2022-02-16 17:33:59', '磁盘使用情况上月数据存储', 'S', '3点执行一次', '判断是否需要存储磁盘使用情况上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'diskUseReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (77, 'gjb', '2022-02-16 17:33:56', 'gjb', '2022-02-16 17:34:01', '资产可用性报表每天数据存储', 'S', '3点30分执行一次', '缓存资产可用性报表天数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'assetUsabilityReportDailyDataCache', '0 30 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (78, 'gjb', '2022-02-16 17:34:58', 'gjb', '2022-02-16 17:34:58', '资产可用性报表上周数据存储', 'S', '3点执行一次', '判断是否需要资产可用性上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'assetUsabilityReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (79, 'gjb', '2022-02-16 17:35:28', 'gjb', '2022-02-16 17:35:28', '资产可用性报表上月数据存储', 'S', '3点执行一次', '判断是否需要存储资产可用性上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'assetUsabilityReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (80, 'gjb', '2022-02-16 17:36:17', 'gjb', '2022-02-16 17:36:19', '运行状态报表每天数据存储', 'S', '2点01分执行一次', '缓存运行状态报表天数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'runStateReportDailyDataCache', '* 01 2 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (81, 'gjb', '2022-02-16 17:37:01', 'gjb', '2022-02-16 17:37:04', '运行状态报表上周数据存储', 'S', '3点执行一次', '判断是否需要存储运行状态报表上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'runStateReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (82, 'gjb', '2022-02-16 17:37:34', 'gjb', '2022-02-16 17:37:34', '运行状态报表上月数据存储', 'S', '3点执行一次', '判断是否需要存储运行状态报表上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'runStateReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (83, 'gjb', '2022-02-16 17:38:12', 'gjb', '2022-02-16 17:38:12', 'MPLS报表每天数据存储', 'S', '5点30分执行一次', '缓存MPLS报告天数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'mplsHistoryReportDailyDataCache', '0 30 5 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (84, 'gjb', '2022-02-16 17:38:54', 'gjb', '2022-02-16 17:38:54', 'MPLS报表上周数据存储', 'S', '3点执行一次', '判断是否需要存储MPLS报表上周数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'mplsHistoryReportWeeklyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (85, 'gjb', '2022-02-16 17:39:17', 'gjb', '2022-02-16 17:39:17', 'MPLS报表上月数据存储', 'S', '3点执行一次', '判断是否需要存储MPLS报表上月数据', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'mplsHistoryReportMonthlyDataCache', '0 0 3 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (86, 'gjb', '2022-02-16 17:39:58', 'gjb', '2022-02-16 17:40:02', '线路流量报表每小时数据更新', 'S', '从第*分开始,每60分执行一次', '缓存线路流量报表 1小时执行一次', NULL, NULL, NULL, 'cn.mw.monitor.report.timer.MwReportManageTimeToo', 'getToDayLInkFlowReportDataCache', '0 */60 * * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (87, 'gjb', '2022-02-17 09:52:36', 'gjb', '2022-02-17 09:52:40', '首页统计每日资产数量', 'S', '23点58分执行一次', '首页资产统计数据定时执行', NULL, NULL, NULL, 'cn.mw.monitor.screen.timer.MWNewScreenTime', 'censusAssetsAmount', '0 58 23 * * ?', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (88, 'gengjb', '2022-02-17 10:10:06', 'gengjb', '2022-02-17 10:10:06', '资产可用性报表5分钟存储redis', 'S', '5-22点从第*分开始,每5分执行一次', '资产可用性报表5分钟存储', '无异常', '2022-02-25 12:50:00', '2022-02-25 12:45:00', 'cn.mw.monitor.report.timer.MwReportManageTime', 'getAssetsAbblie', '0 */5 5-22 * * ?', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `mw_ncm_timetask_table`(`id`, `creator`, `create_date`, `modifier`, `modification_date`, `taskname`, `timetype`, `plan`, `type`, `last_result`, `after_time`, `last_time`, `class_name`, `method`, `cron`, `status`, `config_type`, `cmds`, `timing`, `select_url`, `select_id`, `month`, `week`, `day`, `hms`, `time_custom`, `task_type`) VALUES (89, 'gengjb', '2022-02-17 10:10:40', 'gengjb', '2022-02-17 10:10:40', '运行状态报表5分钟存储redis', 'S', '5-22点从第*分开始,每5分执行一次', '运行状态报表5分钟缓存', '无异常', '2022-02-25 12:50:00', '2022-02-25 12:45:00', 'cn.mw.monitor.report.timer.MwReportManageTime', 'getRedisRunTime', '0 */5 5-22 * * ?', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1);

#队列表
DROP TABLE IF EXISTS `mw_ipaddress_scan_queue`;
CREATE TABLE `mw_ipaddress_scan_queue`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `link_id` int(11) NULL DEFAULT NULL,
  `param` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '实体集',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '关联用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#权限表
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-231-browse', 0, 231, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-231-create', 0, 231, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-231-delete', 0, 231, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-231-perform', 0, 231, 'perform', 1);

#模型关系表 2022/02/21
DROP TABLE IF EXISTS `mw_cmdbmd_relations`;
CREATE TABLE `mw_cmdbmd_relations` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `own_model_id` int(11) DEFAULT NULL COMMENT '当前模型id',
  `relation_group_id` int(11) DEFAULT NULL COMMENT '关系组id',
  `opposite_model_id` int(11) DEFAULT NULL COMMENT '关联模型id',
  `own_relation_name` varchar(255) DEFAULT NULL COMMENT '当前模型描述关系名称',
  `own_relation_id` varchar(64) DEFAULT NULL COMMENT '当前模型描述关系Id',
  `own_relation_num` tinyint(1) DEFAULT NULL COMMENT '当前模型关联个数(1:0-1或者2:0-n)',
  `opposite_relation_name` varchar(255) DEFAULT NULL COMMENT '关联模型描述关系名称',
  `opposite_relation_id` varchar(64) DEFAULT NULL COMMENT '关联模型描述关系Id',
  `opposite_relation_num` tinyint(1) DEFAULT NULL COMMENT '关联模型关联个数(1:0-1或者2:0-n)',
  `creator` varchar(128) DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
#新增关系分组表 qzg
DROP TABLE IF EXISTS `mw_cmdbmd_relations_group`;
CREATE TABLE `mw_cmdbmd_relations_group` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '分组id',
  `own_model_id` int(11) DEFAULT NULL,
  `relation_group_name` varchar(255) DEFAULT NULL,
  `relation_group_desc` varchar(255) DEFAULT NULL,
  `creator` varchar(128) DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模型关系分组表';
CREATE TABLE `mw_cmdbmd_relations_group_mapper` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `own_model_id` int(11) DEFAULT NULL COMMENT '当前模型id',
  `opposite_model_id` int(32) DEFAULT NULL COMMENT '关联模型id',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
  `relation_group_id` int(11) DEFAULT NULL COMMENT '分组id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模型关系与关系分组映射表';

#资产详情布局表新增字段
ALTER TABLE `monitor`.`mw_component_layout_table`
ADD COLUMN `assets_id` varchar(64) DEFAULT NULL COMMENT '资产id';

CREATE TABLE `mw_mymonitor_custom_label` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `navigation_bar_name` varchar(255) DEFAULT NULL COMMENT '详情页导航栏名称',
  `assets_id` int(11) DEFAULT NULL COMMENT '资产hostId',
  `type` int(1) DEFAULT NULL COMMENT '类型：0新增，1删除',
  `navigation_bar_id` int(11) DEFAULT NULL COMMENT '详情页导航栏Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8 COMMENT='资产详情页导航栏自定义表';


#gengjb 新增流量报表与cpu报表SQL
ALTER TABLE `mw_linkflow_census_report`
ADD COLUMN `acceptFlow_min` varchar(128) NULL COMMENT '接收最小流量',
ADD COLUMN `acceptFlow_total` varchar(128) NULL COMMENT '接收总流量' ,
ADD COLUMN `sendingFlow_min` varchar(128) NULL COMMENT '发送最小流量',
ADD COLUMN `sendingFlow_total` varchar(128) NULL COMMENT '发送总流量';
ALTER TABLE `mw_linkflow_report_daily`
ADD COLUMN `acceptFlow_min` varchar(128) NULL COMMENT '接收最小流量',
ADD COLUMN `acceptFlow_total` varchar(128) NULL COMMENT '接收总流量' ,
ADD COLUMN `sendingFlow_min` varchar(128) NULL COMMENT '发送最小流量',
ADD COLUMN `sendingFlow_total` varchar(128) NULL COMMENT '发送总流量';
ALTER TABLE `mw_linkflow_report_weekly`
ADD COLUMN `acceptFlow_min` varchar(128) NULL COMMENT '接收最小流量',
ADD COLUMN `acceptFlow_total` varchar(128) NULL COMMENT '接收总流量' ,
ADD COLUMN `sendingFlow_min` varchar(128) NULL COMMENT '发送最小流量',
ADD COLUMN `sendingFlow_total` varchar(128) NULL COMMENT '发送总流量';
ALTER TABLE `mw_linkflow_report_monthly`
ADD COLUMN `acceptFlow_min` varchar(128) NULL COMMENT '接收最小流量',
ADD COLUMN `acceptFlow_total` varchar(128) NULL COMMENT '接收总流量' ,
ADD COLUMN `sendingFlow_min` varchar(128) NULL COMMENT '发送最小流量',
ADD COLUMN `sendingFlow_total` varchar(128) NULL COMMENT '发送总流量';
ALTER TABLE `mw_cpuandmemory_daily`
ADD COLUMN `icmp_response_time` varchar(128) NULL COMMENT 'ICMP响应时间';
ALTER TABLE `mw_cpuandmemory_weekly`
ADD COLUMN `icmp_response_time` varchar(128) NULL COMMENT 'ICMP响应时间';
ALTER TABLE `mw_cpuandmemory_monthly`
ADD COLUMN `icmp_response_time` varchar(128) NULL COMMENT 'ICMP响应时间';

#模型表新增模型视图字段
ALTER TABLE `mw_cmdbmd_manage`
ADD COLUMN `model_view` int(1) DEFAULT NULL COMMENT '模型视图（1：机房视图，2机柜视图）';

#qzg 2022/03/07 模型字段结构果表新增字段
alter table mw_cmdbmd_properties_value add column `relation_model_index` varchar(64) DEFAULT NULL COMMENT '数据关联模型index';
alter table mw_cmdbmd_properties_value add column `relation_properties_index` varchar(64) DEFAULT NULL COMMENT '数据管理属性index';
alter table mw_cmdbmd_properties_value add column `is_relation` tinyint(1) DEFAULT NULL COMMENT '是否模型关联数据';


#gqw 每次更新系统后,将所有用户登录状态置为离线
UPDATE mw_sys_user SET login_state = 'OFFLINE';

#gengjb 线路树结构新建表
CREATE TABLE `mw_link_tree` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '线路树结构目录ID',
  `prent_id` int(10) DEFAULT NULL COMMENT '父目录ID',
  `contents_name` varchar(256) DEFAULT NULL COMMENT '目录名称',
  `sort` int(10) DEFAULT NULL COMMENT '目录顺序',
  `org_id` varchar(256) DEFAULT NULL COMMENT '机构ID',
  `user_group_id` varchar(256) DEFAULT NULL COMMENT '用户组ID',
  `user_id` varchar(256) DEFAULT NULL COMMENT '用户ID',
  `describe` varchar(256) DEFAULT NULL COMMENT '描述',
  `link_id` varchar(256) DEFAULT NULL COMMENT '线路id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

CREATE TABLE `mw_linkid_treeid` (
  `link_id` varchar(256) DEFAULT NULL COMMENT '线路ID',
  `tree_id` int(10) DEFAULT NULL COMMENT '树结构id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#lmm 定时任务系统整体更新-1 20220309
#新定时任务表
DROP TABLE IF EXISTS `mw_ncm_newtimetask`;
CREATE TABLE `mw_ncm_newtimetask`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `model_id` int(11) NULL DEFAULT NULL,
  `action_id` int(11) NULL DEFAULT NULL,
  `time_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '定时组名称',
  `time_description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '定时计划的描述',
  `time_button` int(11) NULL DEFAULT 0 COMMENT '定时计划是否开启 0.开启 1.关闭',
  `time_object` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '非时间计划脚本',
  `time_start_time` datetime(0) NULL DEFAULT NULL COMMENT '上次时间',
  `time_end_time` datetime(0) NULL DEFAULT NULL COMMENT '下次时间',
  `time_count` int(11) NULL DEFAULT NULL COMMENT '完成执行的时间 单位：s',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


#新历史记录表
DROP TABLE IF EXISTS `mw_ncm_newtimetask_history`;
CREATE TABLE `mw_ncm_newtimetask_history`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `result_type` int(11) NULL DEFAULT 0 COMMENT '0.String 1.url',
  `result_context` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '返回结果参数',
  `new_timetask_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联定时计划组',
  `object_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否关联模块',
  `run_time` int(11) NULL DEFAULT NULL COMMENT '当前对象任务执行时长',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `is_success` int(11) NULL DEFAULT 0 COMMENT '当前模块定时是否成功',
  `fail_reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#定时任务对应对象
DROP TABLE IF EXISTS `mw_ncm_newtimetask_mapper_object`;
CREATE TABLE `mw_ncm_newtimetask_mapper_object`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `newtimetask_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `object_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#定时任务定义时间
DROP TABLE IF EXISTS `mw_ncm_newtimetask_mapper_time`;
CREATE TABLE `mw_ncm_newtimetask_mapper_time`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `time_id` int(11) NULL DEFAULT NULL,
  `newtimetask_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#定时任务指定动作
DROP TABLE IF EXISTS `mw_ncm_timetask_action`;
CREATE TABLE `mw_ncm_timetask_action`  (
  `id` int(11) NOT NULL,
  `model_id` int(11) NULL DEFAULT NULL,
  `action_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '动作名称',
  `action_impl` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '实现的位置',
  `action_method` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '动作实现方法',
  `action_model` int(11) NULL DEFAULT 0 COMMENT '动作实现存在指定对象',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#定时任务模块化
DROP TABLE IF EXISTS `mw_ncm_timetask_model`;
CREATE TABLE `mw_ncm_timetask_model`  (
  `id` int(11) NOT NULL,
  `model_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '模块名称',
  `model_type` int(11) NULL DEFAULT NULL COMMENT '0.表示自定义定时任务 1.表示系统定时任务',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#定时任务初始化时间
DROP TABLE IF EXISTS `mw_ncm_timetask_time_plan`;
CREATE TABLE `mw_ncm_timetask_time_plan`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `time_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间计划',
  `time_cron` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间函数',
  `time_cron_chinese` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间表达时翻译',
  `time_type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'H.小时 W.周 M.月 S.自定义',
  `time_choice` int(11) NULL DEFAULT NULL COMMENT '选择小时周对应选项',
  `time_hms` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '具体到具体24小时内时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


#定时树状图
DROP TABLE IF EXISTS `mw_ncm_timetask_tree`;
CREATE TABLE `mw_ncm_timetask_tree`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id` int(11) NULL DEFAULT NULL COMMENT '模型id',
  `action_id` int(11) NULL DEFAULT NULL COMMENT '动作id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;



#初始化数据
INSERT INTO `mw_ncm_timetask_action` VALUES (1, 1, '数据收敛', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskTwo', NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (2, 1, '数据输出', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskThere', NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (3, 1, '数据缓存', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskThere', NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (4, 2, '数据缓存', 'cn.mw.monitor.virtualization.service.impl.MwVirtualServiceImpl', 'saveVirtualTree', NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (5, 3, '数据缓存', 'cn.mw.time.MWZabbixConnetStateTime', 'zabbixConnectCheck', NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (6, 4, '配置备份', NULL, NULL, NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (7, 4, '配置执行', NULL, NULL, NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (8, 4, '合规检测', NULL, NULL, NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (9, 5, '数据扫描', 'cn.mw.monitor.ipaddressmanage.service.impl.MwIpAddressManageServiceScanImpl', 'cronBatchScanIp', NULL);
INSERT INTO `mw_ncm_timetask_action` VALUES (10, 6, '数据扫描', NULL, NULL, NULL);

INSERT INTO `mw_ncm_timetask_model` VALUES (1, '报表任务', 1);
INSERT INTO `mw_ncm_timetask_model` VALUES (2, '虚拟化任务', 1);
INSERT INTO `mw_ncm_timetask_model` VALUES (3, '监控服务器', 1);
INSERT INTO `mw_ncm_timetask_model` VALUES (4, '配置管理', 0);
INSERT INTO `mw_ncm_timetask_model` VALUES (5, 'IP地址管理', 0);
INSERT INTO `mw_ncm_timetask_model` VALUES (6, '资产管理', 0);

INSERT INTO `mw_ncm_timetask_tree` VALUES (1, 1, 1);
INSERT INTO `mw_ncm_timetask_tree` VALUES (2, 1, 2);
INSERT INTO `mw_ncm_timetask_tree` VALUES (3, 1, 3);
INSERT INTO `mw_ncm_timetask_tree` VALUES (4, 2, 4);
INSERT INTO `mw_ncm_timetask_tree` VALUES (5, 3, 5);
INSERT INTO `mw_ncm_timetask_tree` VALUES (6, 4, 6);
INSERT INTO `mw_ncm_timetask_tree` VALUES (7, 4, 7);
INSERT INTO `mw_ncm_timetask_tree` VALUES (8, 4, 8);
INSERT INTO `mw_ncm_timetask_tree` VALUES (9, 5, 9);
INSERT INTO `mw_ncm_timetask_tree` VALUES (10, 6, 10);
#到此结束

#ljb 20220311 ip地址冲突管理
create table monitor.mw_ipconfict_his
(
    id          varchar(33) not null primary key,
    ip          varchar(15) null,
    mac         varchar(17) null comment 'mac地址',
    deviceName  varchar(50) null comment '上联设备名称',
    portName    varchar(50) null comment '上联接口名称',
    create_time timestamp   null comment '创建日期'
) comment 'ip冲突历史表';

create table monitor.mw_ipconfict_his_detail
(
    id          varchar(33) not null primary key,
    conflict_id varchar(33) null comment 'ip冲突历史表关联id',
    ip          varchar(15) null,
    mac         varchar(17) null comment 'mac地址',
    device_name varchar(50) null comment '上联设备名称',
    port_name   varchar(50) null comment '上联接口名称',
    creat_time  timestamp   null comment '创建时间'
) comment '发生冲突ip表';

alter table monitor.mw_ipaddressmanagelist_table add is_conflict boolean default false null comment 'ip是否冲突';

#qzg 20220315 新增资产详情布局版本控制表
CREATE TABLE `mw_component_layout_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `component_layout_id` int(11) NOT NULL COMMENT '布局id',
  `version` int(11) NOT NULL COMMENT '版本控制',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `assets_type_sub_id` int(11) NOT NULL COMMENT '资产子类型',
  `component_layout` longtext NOT NULL COMMENT '组件布局',
  `default_flag` tinyint(1) DEFAULT NULL COMMENT '是否为默认布局',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `monitor_server_id` int(11) NOT NULL COMMENT '监控服务器id',
  `template_id` varchar(128) NOT NULL COMMENT '第三方监控服务器中关联模板id',
  `navigation_bar_id` int(11) NOT NULL COMMENT '导航栏标签id',
  `assets_id` varchar(64) DEFAULT NULL COMMENT '资产id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#gengjb 监控大屏新增线路折线图组件
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES
(32, '线路折线图', '线路接口发送与接收流量明细', NULL, NULL, NULL, '线路', 1, 'cn.mw.monitor.screen.service.modelImpl.MWLagerScreenLinkLineChartModel');
INSERT INTO `monitor`.`mw_layout_base`(`id`, `layout_type`, `count`) VALUES (8, '类型二9宫格', 9);
INSERT INTO `monitor`.`mw_layout_base`(`id`, `layout_type`, `count`) VALUES (9, '类型二4宫格', 8);

#20220318 ljb 拓扑流量显示
alter table monitor.mw_topo_graph add netflow_info longtext null comment '流量信息';


## gqw 2022.03.17
DROP TABLE IF EXISTS `mw_config_manage_changes`;
CREATE TABLE `mw_config_manage_changes` (
  `assets_id` varchar(32) NOT NULL COMMENT '资产的主键ID',
  `new_file_path` varchar(255) NOT NULL COMMENT '新数据',
  `old_file_path` varchar(255) NOT NULL COMMENT '旧数据',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `delete_flag` tinyint(2) unsigned NOT NULL DEFAULT 0 COMMENT '删除标识位'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置管理——资产配置比对差异记录表';
#20220321 gengjb 修改定时任务执行报表邮件发送方法
update mw_ncm_timetask_action set action_impl = 'cn.mw.monitor.report.timer.MwReportTimeSendEmail',action_method = 'reportSendEmail' where id = 2

#20220323 ljb
alter table monitor.mw_db_version add vendor varchar(50) null comment '@猫维智能运维平台';

#20220324 ljb
alter table monitor.mw_topo_graph add graph_index int null comment '拓扑图索引';

#20220329 gengjb 首页模块增加接口路径地址及时间组件显示字段
ALTER TABLE `monitor`.`mw_newhomepage_init`
ADD COLUMN `module_url` varchar(255) NULL COMMENT '模块访问接口';
ALTER TABLE `monitor`.`mw_newhomepage_init`
ADD COLUMN `display_time` tinyint(2) NULL COMMENT '是否显示时间';
delete from mw_newhomepage_init;
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (1, '告警概括', 0, NULL, 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (2, 'CPU利用率', 0, 'new/screen/getAssetsTopN', 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (3, '节点丢包率', 0, 'new/screen/getAssetsTopN', 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (5, '内存', 0, 'new/screen/getAssetsTopN', 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (6, '节点延时', 0, 'new/screen/getAssetsTopN', 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (8, '磁盘', 0, 'new/screen/getAssetsTopN', 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (9, '接口流量', 0, 'new/screen/getLinkTopN', 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (7, '消息统计', 0, NULL, 0);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (10, '告警标题分类统计', 0, 'new/screen/getAlertCount', 1);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (11, '告警资产分类统计', 0, 'new/screen/getAlertCount', 1);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (12, '告警级别分类统计', 0, 'new/screen/getAlertCount', 1);
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`) VALUES (13, 'ip冲突统计', 0, 'ipAddressManage/ipconflict/browse', 0);




## gqw 2022.03.29 补充参数
ALTER TABLE `mw_ncm_configmanage_path`
ADD COLUMN `max_count` int(11) NULL COMMENT '配置的最大数量' AFTER `perfrom_path`,
ADD COLUMN `max_time` int(11) NULL COMMENT '配置保留的最长时间' AFTER `max_count`;