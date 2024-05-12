CREATE TABLE `mw_alert_action_level_event_mapper`  (
  `action_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `event_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `level` int(1) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


CREATE TABLE `mw_alert_action_level_rule`  (
  `action_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `state` int(1) NULL DEFAULT NULL COMMENT '1启用；0未启用',
  `date` float(11, 1) NULL DEFAULT NULL COMMENT '分钟',
  PRIMARY KEY (`action_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `mw_alert_action_level_user_mapper`  (
  `action_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_id` int(11) NULL DEFAULT NULL,
  `level` int(1) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE mw_alert_action_level_rule MODIFY date FLOAT(11,1) default NULL;

alter table mw_alert_weixin_rule add alert_templeate varchar(255)  default null comment '告警通知模板';

alter table mw_alert_weixin_rule add recovery_templeate varchar(255)  default null comment '恢复通知模板';

#预留状态导出
INSERT INTO `monitor`.`mw_dictionary_table`(`id`, `key`, `value`, `typeof`, `descri`, `creator`, `create_date`, `modifier`, `modification_date`) VALUES (11, 2, '预留', 2, 'ip地址使用状态', NULL, NULL, NULL, NULL);

CREATE TABLE `mw_alert_action_group_mapper`  (
  `action_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `group_id` int(11) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

alter table mw_alert_record_table add title varchar(255)  default null comment '告警标题';

alter table mw_alert_record_table add ip varchar(255)  default null comment '主机IP';

alter table mw_alert_record_table add is_alarm tinyint (1)  default null;

#删除带外IP
DELETE FROM `mw_pagefield_table` where page_id = 7  and prop  = 'outBandIp';

#增加指纹库信息表
DROP TABLE IF EXISTS `mw_fingerprint_version_table`;
CREATE TABLE `mw_fingerprint_version_table` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fingerprint_version` varchar(255) NOT NULL DEFAULT '0' COMMENT '版本信息（值为距离1970-01-01的毫秒数）',
  `finger_file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '指纹库文件名称',
  `fingerprint_file_path` varchar(512) NOT NULL DEFAULT '' COMMENT '指纹库文件路径（绝对路径）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

#增加数据
INSERT INTO `monitor`.`mw_fingerprint_version_table`(`id`, `fingerprint_version`, `finger_file_name`, `fingerprint_file_path`) VALUES (1, '1', 'test.txt', '/opt/test.txt');

#首页布局表
CREATE TABLE `mw_index_bulk` (
  `model_data_id` varchar(50) DEFAULT NULL COMMENT '模块唯一id',
  `bulk_id` int(11) DEFAULT NULL COMMENT '模块类型id',
  `bulk_name` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `delete_flag` tinyint(1) unsigned zerofill DEFAULT 0 COMMENT '删除标识：1-删除，0-未删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

##首页初始化模块表
CREATE TABLE `mw_index_init_bulk` (
  `bulk_id` int(11) DEFAULT NULL COMMENT '首页模块类型',
  `bulk_name` varchar(50) DEFAULT NULL COMMENT '首页模块名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

##首页初始化模块数据
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (1,"告警概括");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (2,"CPU");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (3,"节点丢包率");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (4,"活动告警");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (5,"内存");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (6,"节点延时");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (7,"消息统计");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (8,"磁盘");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (9,"接口流量");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (10,"日志量统计");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (11,"资产统计");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (12,"当日数据时间分布");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (13,"系统运行状态");


#资产管理下维护的基础信息表
CREATE TABLE mw_assets_mainten (
    id int(10) unsigned NOT NULL AUTO_INCREMENT,
    maintenanceid varchar(255) NOT NULL COMMENT '维护ID',
    name varchar(255) DEFAULT NULL COMMENT '维护名称',
    delete_flag tinyint(4) DEFAULT 0 COMMENT '删除标记，0未删除，1：已删除',
    creator varchar(255) DEFAULT NULL COMMENT '创建人',
    create_date datetime DEFAULT NULL COMMENT '创建时间',
    modifier varchar(255) DEFAULT NULL COMMENT '修改人',
    modification_date datetime DEFAULT NULL COMMENT '修改时间',
    active_since datetime DEFAULT NULL COMMENT '启用自从',
    active_till datetime DEFAULT NULL COMMENT '启用直到',
    description varchar(1000) DEFAULT NULL COMMENT '描述',
    maintenanceType int(10) DEFAULT  NULL COMMENT '维护类型,0:有数据收集，1:无数据收集',
    groupids varchar(2000) DEFAULT  NULL COMMENT '主机组',
    hostids varchar(2000) DEFAULT  NULL COMMENT '主机',
    PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='资产管理维护基础数据';

alter table mw_assets_mainten add column tagseval_type int(10) COMMENT '标记 0：与/或者，2：或者';

alter table mw_assets_mainten add column formdata varchar(4000) COMMENT '存储表单提交数据';

alter table mw_assets_mainten add column serverId int(10) COMMENT 'zabbix服务ID';

alter table mw_assets_mainten add column typeId int(10) COMMENT '选择主机群组ID';

#资产管理下维护的时间段表
CREATE TABLE mw_assets_mainten_timesolt (
    id int(10) unsigned NOT NULL AUTO_INCREMENT,
    delete_flag tinyint(4) DEFAULT 0 COMMENT '删除标记，0未删除，1：已删除',
    creator varchar(255) DEFAULT NULL COMMENT '创建人',
    create_date datetime DEFAULT NULL COMMENT '创建时间',
    modifier varchar(255) DEFAULT NULL COMMENT '修改人',
    modification_date datetime DEFAULT NULL COMMENT '修改时间',
	maintenid int(10) DEFAULT NULL COMMENT '资产管理维护基础数据表主键',
	day int(10) DEFAULT NULL COMMENT '天数',
	dayOfWeek varchar(255) DEFAULT NULL COMMENT '星期',
	every int(10) DEFAULT NULL COMMENT '每一个',
	month varchar(255) DEFAULT NULL COMMENT '月份',
	period int(10) DEFAULT NULL COMMENT '时期',
	start_date datetime DEFAULT NULL COMMENT '开始时间',
	start_time int(10) DEFAULT NULL COMMENT '开始时间',
    timePeriod_type int(10) DEFAULT NULL COMMENT '时间段类型，0：默认，仅一次； 2：每天； 3：每周； 4：每月一次',
    PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='资产管理维护时间段数据';

#资产管理下维护的主机表
CREATE TABLE mw_assets_mainten_host (
    id int(10) unsigned NOT NULL AUTO_INCREMENT,
    delete_flag tinyint(4) DEFAULT 0 COMMENT '删除标记，0未删除，1：已删除',
    creator varchar(255) DEFAULT NULL COMMENT '创建人',
    create_date datetime DEFAULT NULL COMMENT '创建时间',
    modifier varchar(255) DEFAULT NULL COMMENT '修改人',
    modification_date datetime DEFAULT NULL COMMENT '修改时间',
    maintenid int(10) DEFAULT NULL COMMENT '资产管理维护基础数据表主键',
    hostid varchar(255) DEFAULT NULL COMMENT '主机',
    PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='资产管理维护主机数据';

#资产管理下维护的主机群组表
CREATE TABLE mw_assets_mainten_hostgroup (
    id int(10) unsigned NOT NULL AUTO_INCREMENT,
    delete_flag tinyint(4) DEFAULT 0 COMMENT '删除标记，0未删除，1：已删除',
    creator varchar(255) DEFAULT NULL COMMENT '创建人',
    create_date datetime DEFAULT NULL COMMENT '创建时间',
    modifier varchar(255) DEFAULT NULL COMMENT '修改人',
    modification_date datetime DEFAULT NULL COMMENT '修改时间',
    maintenid int(10) DEFAULT NULL COMMENT '资产管理维护基础数据表主键',
    groupid varchar(255) DEFAULT NULL COMMENT '主机组',
    PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='资产管理维护主机组数据';

#资产管理下维护的标记信息表
CREATE TABLE mw_assets_mainten_tag (
	id int(10) unsigned NOT NULL AUTO_INCREMENT,
	delete_flag tinyint(4) DEFAULT 0 COMMENT '删除标记，0未删除，1：已删除',
	creator varchar(255) DEFAULT NULL COMMENT '创建人',
	create_date datetime DEFAULT NULL COMMENT '创建时间',
	modifier varchar(255) DEFAULT NULL COMMENT '修改人',
	modification_date datetime DEFAULT NULL COMMENT '修改时间',
	maintenid int(10) DEFAULT NULL COMMENT '资产管理维护基础数据表主键',
	tag varchar(255) DEFAULT NULL COMMENT '标记名称',
	operator int(10) DEFAULT NULL COMMENT '运算符，0：等于，2：包含',
	value varchar(255) DEFAULT NULL COMMENT '主机',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='资产管理维护标记数据';

## 资产类型新增图标字段
alter table mw_assetssubtype_table add column  `type_icon` varchar(255) default  NULL COMMENT '类型图标';


## ----------------------------------------ip地址加强版------------------------------------------------
alter table mw_ipaddressmanage_table add radio_status int(11) DEFAULT 0 not null comment '是否做选项分组 0.不作为分组 1.作为分组';
alter table mw_ipv6manage_table add radio_status int(11) DEFAULT 0 not null comment '是否做选项分组 0.不作为分组 1.作为分组';
alter table mw_ipv6manage_table add ip_rand_start varchar(255)  not null comment 'ipv6的起始范围';
alter table mw_ipv6manage_table add ip_rand_end varchar(255)  not null comment 'ipv6的结束范围';

## ip地址分配表
DROP TABLE IF EXISTS `mw_ip_distribution`;
CREATE TABLE `mw_ip_distribution`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `primary_ip` int(11) NOT NULL COMMENT '分配地址\r\n',
  `primary_type` int(11) NULL DEFAULT 0 COMMENT 'IP地址分配类型 0.ipv4,1.ipv6',
  `iplist_type` int(255) NULL DEFAULT 0 COMMENT 'IP分配类型 0.ipv4,1.ipv6',
  `iplist_id` int(11) NULL DEFAULT NULL COMMENT '选择的可用地址id',
  `ipgroup_id` int(11) NULL DEFAULT NULL COMMENT 'IP分组的id',
  `ipgroup_type` int(255) NULL DEFAULT 0 COMMENT 'IP分组的id类型 0.ipv4,1.ipv6',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 98 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


## ip地址下拉选择表
DROP TABLE IF EXISTS `mw_ipaddress_drop_mapper`;
CREATE TABLE `mw_ipaddress_drop_mapper`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label_ip_id` int(11) NULL DEFAULT NULL COMMENT 'ip地址的ip',
  `label_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '对映的选项label',
  `label_drop_id` int(11) NULL DEFAULT NULL COMMENT '选项值',
  `label_ip_type` int(11) NULL DEFAULT NULL COMMENT 'ip种类 0.4 1.6',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


## ip地址属性表 及初始化数据
DROP TABLE IF EXISTS `mw_ipaddress_label`;
CREATE TABLE `mw_ipaddress_label`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标签名称',
  `label_drop` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下拉选项值',
  `label_level` int(11) NULL DEFAULT 0 COMMENT '0.标识基础属性 1.标识高级属性',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mw_ipaddress_label
-- ----------------------------
INSERT INTO `mw_ipaddress_label` VALUES (1, '区域', '16279540952932fb59ea03da74fb48ff', 0);
INSERT INTO `mw_ipaddress_label` VALUES (2, '类型', '16279540952932fb59ea03da74fb48ki', 0);
INSERT INTO `mw_ipaddress_label` VALUES (3, '用途', '16279540952932fb59ea03da74fb48ac', 0);
INSERT INTO `mw_ipaddress_label` VALUES (4, '位置', '16279540952932fb59ea03da74fb48ab', 0);
INSERT INTO `mw_ipaddress_label` VALUES (5, 'OA', '16279540952932fb59ea03da74fb48aq', 1);
INSERT INTO `mw_ipaddress_label` VALUES (6, 'OAurl', '16279540952932fb59ea03da74fb48ao', 1);

## ip地址操作历史表
DROP TABLE IF EXISTS `mw_ipam_oper_history`;
CREATE TABLE `mw_ipam_oper_history`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) NULL DEFAULT 0 COMMENT '操作类型 0.分配 1.回收',
  `creator` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '分配人',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '分配时间',
  `applicant` int(11) NULL DEFAULT NULL COMMENT '审批人id',
  `ip_type` int(11) NULL DEFAULT NULL COMMENT '0.ipv4 1.ipv6',
  `rlist_id` int(11) NULL DEFAULT NULL COMMENT 'id清单表id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 170 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


## ip地址分配历史表
DROP TABLE IF EXISTS `mw_ipam_process_history`;
CREATE TABLE `mw_ipam_process_history`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `applicant` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审批申请人',
  `applicant_date` datetime(0) NULL DEFAULT NULL COMMENT '申请时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


#资产维护计划主机表增加主机名称字段
alter table mw_assets_mainten_host add column host_name varchar(255) COMMENT '主机名称';

INSERT INTO mw_alert_action_type VALUES(9,'深圳短信');

#资产过滤表添加大屏组件过滤时字段
alter table mw_filter_assets add column linkInterfaces varchar (3000) comment '大屏线路组件线路集合';

#mw_model_base表新增三种大屏组件类型数据
insert into mw_model_base(id,model_name,model_desc,model_type,is_show,class) values
(29,"资产概况","机构下资产统计","资产概况",1,"cn.mw.monitor.screen.service.modelImpl.AssestAlertCountModel");

insert into mw_model_base(id,model_name,model_desc,model_type,is_show,class) values
(30,"应用类型","资产分类为应用的资产统计","资产概况",1,"cn.mw.monitor.screen.service.modelImpl.AssestAlertCountModel");

insert into mw_model_base(id,model_name,model_desc,model_type,is_show,class) values
(31,"关键线路","资产分类为应用的资产统计","线路",1,"cn.mw.monitor.screen.service.modelImpl.LinkRankModel");

## 线路源端口目标设备不必填数据库表修改
alter table mw_network_link modify column root_server_id int(11) DEFAULT 0 COMMENT '源设备zabbix服务器id';
alter table mw_network_link modify column target_server_id int(11) DEFAULT 0 COMMENT '目标zabbix服务器id';


update mw_model_base set model_desc="线路接口数据与状态" where id =31;


##首页及大屏根据机构,标签过滤资产字段添加
alter table mw_filter_assets add column filter_label_id varchar (128) comment '标签id';
alter table mw_filter_assets add column filter_org_id varchar (128) comment '机构id';

##新增深圳数据库表2021/8/20
CREATE TABLE `mw_alert_shenzhenSMS_rule`  (
  `rule_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '规则id',
  `app_id` int(11) NULL DEFAULT NULL COMMENT '应用ID',
  `app_pwd` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用密码',
  `biz_class_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务大类编号',
  `biz_type_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务类型编号',
  `biz_sub_type_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务子类型编号',
  `ext_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '扩展编号',
  `is_need_report` int(11) NULL DEFAULT NULL COMMENT '是否需要状态报告',
  `cust_id` int(11) NULL DEFAULT NULL COMMENT '合同账号',
  `app_sms_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用系统标识',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;