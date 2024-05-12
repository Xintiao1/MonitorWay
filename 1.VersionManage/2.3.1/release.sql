CREATE TABLE `mw_mymonitor_label` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `navigation_bar_name` varchar(255) NOT NULL COMMENT '详情页导航栏名称',
  `template_id` varchar(20) NOT NULL COMMENT '相关zabbix模板id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table mw_component_layout_table add navigation_bar_id int(11)  not null comment '导航栏标签id';


CREATE TABLE `mw_ad_info_mapper` (
  `id` int(50) NOT NULL AUTO_INCREMENT,
  `ad_info` varchar(255) DEFAULT NULL,
  `local_info` varchar(255) DEFAULT NULL COMMENT '机构',
  `ad_ip_add` varchar(255) DEFAULT NULL,
  `ad_port` varchar(255) DEFAULT NULL,
  `group_info` varchar(255) DEFAULT NULL COMMENT '用户组',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8;

INSERT INTO mw_alert_action_type VALUES(8,'阿里短信');

alter table mw_settings_info add http_header varchar (255) default null comment 'http请求头';

CREATE TABLE `mw_alert_aliyunsms_rule`  (
  `rule_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '规则id',
  `signName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '阿里短信签名',
  `templateCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '阿里短线模板ID',
  `accessKeyId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '阿里短信accessKeyId',
  `accessKeySecret` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '阿里短信accessKeySecret',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8;

ALTER TABLE `monitor`.`mw_sys_user` MODIFY COLUMN `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱';

alter table mw_settings_info add icon varchar (255) default null comment '浏览器标签图标';
alter table mw_settings_info add user_icon varchar (255) default null comment '用户个人信息logo';

alter table mw_base_vendor_icon add custom_flag int (2) default 0 comment '图标类型： 0-系统定义，1-用户上传';

CREATE TABLE `mw_adconfig_user_mapper` (
  `id` int(50) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_id` int(50) NOT NULL COMMENT 'AD配置id',
  `login_name` varchar(255) DEFAULT NULL COMMENT '登录名',
  `user_name` varchar(255) DEFAULT NULL COMMENT '用户名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `monitor`.`mw_adAuthentic_mapper` ADD COLUMN `domain_name` varchar(255) NULL COMMENT '域名';

CREATE TABLE `mw_alerthistory_7days`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `acknowledged` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `alertType` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `alertid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `assetsId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `clock` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `eventid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hostid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `longTime` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `message` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `monitorServerId` int(11) NULL DEFAULT NULL,
  `monitorServerName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `objectName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `objectid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `r_eventid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `rclock` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `recoverTime` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `severity` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `subject` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 439 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER table mw_topo_editor_node add width double(10,2) default 50 comment '宽';
ALTER table mw_topo_editor_node add height double(10,2) default 50 comment '长';

alter table mw_alert_rule add state tinyint (1) default 1 comment '启用标识：0未启用，1启用';
alter table mw_alert_action add state tinyint (1) default 1 comment '启用标识：0未启用，1启用';

CREATE TABLE `mw_snmp_port_credential` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `snmp_version` varchar(255) DEFAULT NULL COMMENT 'SNMP版本',
  `port` varchar(255)  DEFAULT NULL COMMENT '端口',
  `module` varchar(255) DEFAULT NULL COMMENT '模块',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mw_snmp_credential` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `snmp_version` varchar(255) DEFAULT NULL COMMENT 'SNMP版本',
  `comm_name` varchar(255) DEFAULT NULL COMMENT '团体名',
  `module` varchar(255) DEFAULT NULL COMMENT '模块',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mw_sys_credential` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mw_account` varchar(255) DEFAULT NULL COMMENT '账号',
  `mw_passwd` longtext DEFAULT NULL COMMENT '密码',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `module` varchar(255) DEFAULT NULL COMMENT '模块',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into mw_layout_base(id,layout_type,count) values (6,'7宮格',7);

CREATE TABLE `mw_sy_ad_info` (
  `id` int(50) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ip_server_name` varchar(255) DEFAULT NULL COMMENT '服务器名称',
  `ip_address` varchar(255) DEFAULT NULL COMMENT '服务器IP地址',
  `port` varchar(255) DEFAULT NULL COMMENT '端口',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `monitor`.`mw_sys_credential` ADD COLUMN `module_id` int(50) NULL COMMENT '模块id';
ALTER TABLE `monitor`.`mw_snmp_credential` ADD COLUMN `module_id` int(50) NULL COMMENT '模块id';
ALTER TABLE `monitor`.`mw_snmp_port_credential` ADD COLUMN `module_id` int(50) NULL COMMENT '模块id';



DROP TABLE IF EXISTS `mw_ip_connection`;
CREATE TABLE `mw_ip_connection`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ip地址',
  `org_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属机构',
  `oper_type` int(11) NULL DEFAULT 0 COMMENT '操作类型  0.开始断网 1.断网结束',
  `oper_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `oper_platform` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作平台',
  `oper` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作人',
  `oper_status` int(11) NULL DEFAULT 0 COMMENT '断网状态 0.未结束 1.已结束',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE `monitor`.`mw_sys_credential` ADD COLUMN `cred_desc` varchar(255) NULL COMMENT '凭据描述';
ALTER TABLE `monitor`.`mw_sys_credential` MODIFY COLUMN `module_id` varchar(255) NULL DEFAULT NULL COMMENT '模块id';
ALTER TABLE `monitor`.`mw_snmp_credential` MODIFY COLUMN `module_id` varchar(255) NULL DEFAULT NULL COMMENT '模块id';
ALTER TABLE `monitor`.`mw_snmp_port_credential` MODIFY COLUMN `module_id` varchar(255) NULL DEFAULT NULL COMMENT '模块id';


CREATE TABLE `mw_index_base`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bulk_id` int(10) NOT NULL COMMENT '首页模块id',
  `bulk_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '首页模块名称',
  `delete_flag` tinyint(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (1,"告警概括");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (2,"CPU");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (3,"节点丢包率");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (4,"活动告警");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (5,"内存");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (6,"节点延时");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (7,"消息统计");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (8,"磁盘");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (9,"接口流量");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (10,"日志量统计");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (11,"资产统计");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (12,"当日数据时间分布");
INSERT INTO mw_index_base (bulk_id,bulk_name) VALUES (13,"系统运行状态");

ALTER TABLE `monitor`.`mw_sys_credential` ADD COLUMN `cred_details` varchar(255) NULL COMMENT '下拉框展示';


#拓扑图
ALTER TABLE `monitor`.`mw_topo_editor_link` ADD COLUMN `up_if_name` varchar(255) NULL COMMENT '上传名称';
ALTER TABLE `monitor`.`mw_topo_editor_link` ADD COLUMN `down_if_name` varchar(255) NULL COMMENT '下载名称';

ALTER TABLE `mw_webmonitor_table` MODIFY COLUMN `host_id` VARCHAR(128) NOT NULL;