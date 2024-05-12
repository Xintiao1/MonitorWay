#2021.08.23 设备指纹信息表
create table mw_device_info
(
	id int auto_increment,
	asset_id varchar(255) null comment '资产id',
	device_code VARCHAR(255) null comment '设备idcode',
	constraint mw_device_info_pk primary key (id)
);

#维护计划需求变更表结构变更语句
alter table mw_assets_mainten drop column maintenanceid;
alter table mw_assets_mainten_host add column serverId int(10) COMMENT 'zabbix服务ID';
alter table mw_assets_mainten_host add column type_id int(10) COMMENT '资产类型的ID';
alter table mw_assets_mainten_host add column maintenanceid int(10) COMMENT 'zabbix中维护的主键ID';

#2021.08.26修改
alter table mw_topo_graph add column filter_enable tinyint(1) default 0 COMMENT '是否过滤资产';
alter table mw_topo_graph add column edited tinyint(1) default 0 comment '是否编辑过';
alter table mw_topo_graph add column asset_type VARCHAR(255) comment '资产类型列表';
#20210827Lmm新加
alter table mw_ipaddressmanagelist_table add distribution_status int(11) DEFAULT 0 not null comment '清单地址分配状态';
alter table mw_ipv6managelist_table add distribution_status int(11) DEFAULT 0 not null comment '清单地址分配状态';

#维护计划新增周期频率字段
ALTER TABLE mw_assets_mainten ADD COLUMN cyclic_frequency int(10) NULL COMMENT '周期频率';

#告警历史表添加字段20210902
ALTER TABLE mw_alerthistory_7days ADD COLUMN userId int(11) NULL;


#20210902 15:38  有形资产列表描述信息字段接受512
alter table mw_tangibleassets_table modify description varchar(512);



#ldap

ALTER TABLE mw_ad_info_mapper ADD COLUMN search_nodes varchar(255) NULL COMMENT '节点' ;
ALTER TABLE mw_ad_info_mapper ADD COLUMN ad_type varchar(255) NULL COMMENT 'ad类型' ;
ALTER TABLE mw_ad_info_mapper ADD COLUMN role_id varchar(50) NULL COMMENT '角色id' ,
ADD COLUMN org_id varchar(50) NULL COMMENT '机构id' ,
ADD COLUMN group_id varchar(50) NULL COMMENT '用户组id' ;

CREATE TABLE `monitor`.`mw_ad_config_temp_mapper`  (
  `id` int(50) NOT NULL AUTO_INCREMENT,
  `config_id` int(50) NULL COMMENT '配置id',
  `creator` varchar(255) NULL COMMENT '创建人',
  `type` varchar(255) NULL COMMENT '类型',
  PRIMARY KEY (`id`)
);

CREATE TABLE `monitor`.`mw_ad_improtUser_temp_mapper`  (
  `id` int(50) NOT NULL AUTO_INCREMENT,
  `user_id` int(50) NULL COMMENT '用户id',
  `creator` varchar(255) NULL COMMENT '创建人',
  `type` varchar(255) NULL COMMENT '类型',
  PRIMARY KEY (`id`)
);

ALTER TABLE `monitor`.`mw_adAuthentic_mapper`
ADD COLUMN `ad_account` varchar(255) NULL COMMENT '账号',
ADD COLUMN `ad_passwd` text NULL COMMENT '密码' ;

# 2021.9.7机构添加机构经纬度
alter table mw_sys_org add column coordinate varchar (255) DEFAULT NULL COMMENT '经纬度';

#资产列表自定义树状结构建表语句
CREATE TABLE `mw_treetructure_customclassify` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `custom_name` varchar(255) DEFAULT NULL COMMENT '自定义名称',
  `one_level_classify_id` int(11) DEFAULT NULL COMMENT '一级分类ID',
  `one_level_classify_name` varchar(255) DEFAULT NULL COMMENT '一级分类名称',
  `two_level_classify_id` int(11) DEFAULT NULL COMMENT '二级分类ID',
  `two_level_classify_name` varchar(255) DEFAULT NULL COMMENT '二级分类名称',
  `three_level_classify_id` int(11) DEFAULT NULL COMMENT '三级分类ID',
  `three_level_classify_name` varchar(255) DEFAULT NULL COMMENT '三级分类名称',
  `classify_type` varchar(255) DEFAULT NULL COMMENT '分类类型',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modifier` varchar(255) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8 COMMENT='树状结构自定义分类表';



#20210915Lmm新加
alter table mw_ipaddressmanagelist_table add is_rewrite int(11) DEFAULT 0 not null comment '覆盖方式 0.不覆盖 1.覆盖';
alter table mw_ipaddressmanagelist_table add is_update int(11) DEFAULT 0 not null comment '是否修改';
#20210916Lmm新加
alter table mw_ip_distribution add orgtext varchar(1000) comment '对应机构名称选项值';
alter table mw_ip_distribution add orgIds varchar(255)  comment '对应机构名称';
alter table mw_ip_distribution add oa int(11)  comment 'oa流程';
alter table mw_ip_distribution add oaurl int(11)  comment 'oaurl流程';
alter table mw_ip_distribution add orgIds varchar(255)  comment 'oa流程选项值';
alter table mw_ip_distribution add oaurltext varchar(255) comment 'oaurl流程选项值';
#20210917 ljb新加
create table mw_ipscan_asset
(
    id                int auto_increment comment 'id',
    tangible_asset_id varchar(32) null comment '有型资产id',
    subnet            varchar(20) null comment 'ip子网',
    subnet_binary     varchar(32) null comment '子网二进制表示',
    constraint mw_ipscan_asset_id_uindex unique (id)
);

#20210920 ljb新加
create table mw_topo_tree
(
    id                int auto_increment,
    tangible_asset_id varchar(32)  null comment '资产id',
    device_ip         varchar(20)  null comment '设备ip',
    node_id           int          null comment '节点id',
    node_path         varchar(255) null comment '节点路径',
    constraint mw_topo_tree_id_uindex unique (id)
);



#20211014 lmm
alter table mw_ipaddressmanage_table add index_sort int(11)  comment '排序优先级';
alter table mw_ipv6manage_table add index_sort int(11)  comment '排序优先级';
ALTER TABLE `monitor`.`mw_ipaddressmanage_table`
MODIFY COLUMN `index_sort` int(11) NULL DEFAULT 0 COMMENT '排序优先级' AFTER `radio_status`;
ALTER TABLE `monitor`.`mw_ipv6manage_table`
MODIFY COLUMN `index_sort` int(11) NULL DEFAULT 0 COMMENT '排序优先级' AFTER `ip_rand_end`;


## 2021-10-11
## 在角色表增加允许登录字段
ALTER TABLE `monitor`.`mw_role`
ADD COLUMN `allow_login_flag` tinyint(4) UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否允许登录，0：不允许，1：允许' AFTER `delete_flag`;

#20211015 gengjb山鹰报表
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('8', '资产信息报表(山鹰)', '资产信息报表(山鹰)', 2, 2, '2021-10-15 09:25:37', 'gjb', '2021-10-15 09:25:37', 'gjb', 0);
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('9', 'CPU信息报表(山鹰)', 'CPU信息报表(山鹰)', 2, 2, '2021-10-15 09:25:37', 'gjb', '2021-10-15 09:25:37', 'gjb', 0);
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('10', '磁盘使用率报表(山鹰)', '磁盘使用率(山鹰)', 2, 2, '2021-10-15 09:25:37', 'gjb', '2021-10-15 09:25:37', 'gjb', 0);
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('11', '磁盘可用率报表(山鹰)', '磁盘可用率(山鹰)', 2, 2, '2021-10-15 09:25:37', 'gjb', '2021-10-15 09:25:37', 'gjb', 0);
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('12', '资产可用性报表(山鹰)', '资产可用性报表(山鹰)', 2, 2, '2021-10-15 09:25:37', 'gjb', '2021-10-15 09:25:37', 'gjb', 0);


#20211015 增加角色类别字段及更新字段
ALTER TABLE `monitor`.`mw_role`
ADD COLUMN `role_type` tinyint(4) UNSIGNED NOT NULL DEFAULT 2 COMMENT '1:系统角色；2：自定义角色' AFTER `delete_flag`;

update `monitor`.`mw_role` set role_type = 1 where id  = 0 ;
#20211011qzg新增
alter table mw_cmdbmd_properties modify index_id varchar(129);
alter table mw_cmdbmd_properties add sort int(11) comment '属性字段排序';
alter table mw_cmdbmd_manage modify model_index varchar(128);
alter table mw_cmdbmd_manage add group_nodes varchar(255) COMMENT '模型分类节点Id';
alter table mw_cmdbmd_manage add pids varchar(255) COMMENT '父级模型Ids';
alter table mw_cmdbmd_manage add is_show_menu tinyint(1) COMMENT '是否在菜单上显示 0：不显示，1：显示';
alter table mw_cmdbmd_manage add is_hide_model tinyint(1) COMMENT '是否隐藏模型 0：不隐藏，1：隐藏';
alter table mw_cmdbmd_manage add is_full_text_search tinyint(1) COMMENT '是否全文搜索屏蔽 0：不屏蔽，1：屏蔽';
alter table mw_cmdbmd_manage add is_ignore_change_log tinyint(1) COMMENT '是否忽略变更日志 0：不忽略，1：忽略';
#20211011qzg新增属性结构体关联表
CREATE TABLE `mw_cmdbmd_properties_struct` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `struct_name` varchar(255) DEFAULT NULL COMMENT '结构项名称',
  `struct_id` varchar(128) DEFAULT NULL COMMENT '结构项Id',
  `struct_type` tinyint(1) DEFAULT NULL COMMENT '结构项类型',
  `enumerate_value` varchar(255) DEFAULT NULL COMMENT '枚举值',
  `model_id` int(11) DEFAULT NULL COMMENT '模型Id',
  `properties_index_id` varchar(128) DEFAULT NULL COMMENT '属性Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模型属性结构体表';

#20211019 ljb
create table mw_ipaddressmanagelist_port
(
    id                      int auto_increment primary key,
    access_equip            varchar(255) null comment '接入设备',
    access_port             varchar(255) null comment '接入端口',
    access_port_name        varchar(255) null comment '接入端口名',
    ip_addressmanagelist_id int          null comment 'mw_ipaddressmanagelist_table关联主键'
) comment 'ip上联接口表';

create table mw_ipaddressmanagelist_port_his
(
    id                      int auto_increment primary key,
    access_equip            varchar(255) null comment '接入设备',
    access_port             varchar(255) null comment '接入端口',
    access_port_name        varchar(255) null comment '接入端口名',
    ip_addressmanagelist_his_id int          null comment 'mw_ipaddressmanagelist_his_table关联主键'
) comment 'ip上联接口历史表';

alter table mw_ipaddressmanagelist_his_table add batch_id varchar(255) not null;

#lmm
ALTER TABLE `monitor`.`mw_ipam_oper_history` ADD COLUMN `descript` varchar(255) NULL COMMENT '描述信息' AFTER `rlist_id`;

ALTER TABLE `monitor`.`mw_ipaddressmanagelist_table` ADD COLUMN `is_include` int(11) NULL DEFAULT 1 COMMENT '是否为有用地址' AFTER `is_update`;
alter table mw_ipv6managelist_table add is_update int(11) DEFAULT 0 not null comment '是否修改';
ALTER TABLE `monitor`.`mw_ipv6managelist_table` ADD COLUMN `is_include` int(11) NULL DEFAULT 1 COMMENT '是否为有用地址' AFTER `is_update`;

#lmm 初始化ip分配关系
UPDATE mw_ipaddressmanagelist_table set is_include = 1,distribution_status=0  where  1=1;
UPDATE mw_ipv6managelist_table set is_include = 1,distribution_status=0  where  1=1;
DELETE FROM mw_ip_distribution WHERE 1=1;
DELETE FROM mw_ipam_oper_history WHERE 1=1;
DELETE FROM mw_ipam_process_history WHERE 1=1;
 UPDATE mw_ipaddressmanagelist_table set is_include = 1,distribution_status=0  where  1=1;
 UPDATE mw_ipv6managelist_table set is_include = 1,distribution_status=0  where  1=1;
 DELETE FROM mw_ip_distribution WHERE 1=1;
 DELETE FROM mw_ipam_oper_history WHERE 1=1;
 DELETE FROM mw_ipam_process_history WHERE 1=1;

 #lmm 20211021
 ALTER TABLE `monitor`.`mw_ipaddressmanagelist_table` MODIFY COLUMN `is_include` int(11) NULL DEFAULT 0 COMMENT '是否为有用地址' AFTER `is_update`;

#20211022 磁盘使用率报表改名
update mw_report_table set report_name = '磁盘使用情况报表',report_desc = '磁盘使用情况报表' where id = 10

#2021/10/22 qzg 模型管理个性化设置字段表
CREATE TABLE `mw_cmdbmd_customcol_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `col_id` int(11) NOT NULL COMMENT '列ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `sortable` tinyint(1) NOT NULL COMMENT '是否排序',
  `width` int(11) DEFAULT NULL COMMENT '宽度',
  `visible` tinyint(1) NOT NULL COMMENT '是否显示',
  `order_number` int(11) NOT NULL COMMENT '顺序数',
  `model_properties_id` int(11) DEFAULT NULL COMMENT '模型属性Id',
  `delete_flag` int(1) DEFAULT NULL COMMENT '还原标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=561 DEFAULT CHARSET=utf8;

#2021/10/22 qzg 模型管理属性字段表
CREATE TABLE `mw_cmdbmd_pagefield_table` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `model_id` int(11) NOT NULL COMMENT '模型ID',
  `prop` varchar(128) NOT NULL COMMENT '字段代码',
  `label` varchar(128) NOT NULL COMMENT '字段名称',
  `visible` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否显示',
  `order_number` int(11) NOT NULL COMMENT '顺序数',
  `type` int(6) DEFAULT NULL COMMENT '1字符串,2整形数字,3浮点型数据,4布尔型,5日期类型,6结构体，7:Ip地址',
  `is_tree` int(11) DEFAULT NULL COMMENT '是否可以展开',
  `model_properties_id` int(11) DEFAULT NULL COMMENT '模型属性Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

#2021/10/25 gengjb 线路流量报表增加语句
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('13', '平均与最大流量统计报表', '平均与最大流量统计报表', 2, 2, '2021-10-25 10:03:15', 'gjb', '2021-10-25 10:03:22', 'gjb', 0);

#2021/10/26 ljb 流程定义表
create table mw_process_def
(
    id                  int auto_increment primary key,
    activiti_process_id varchar(255) not null comment 'activiti流程id',
    process_data        longtext     null
) comment '流程定义表';

alter table mw_group_mapper modify type_id varchar(64) not null comment '模块主键id';
alter table mw_user_mapper modify type_id varchar(64) not null comment '模块主键id';
alter table mw_org_mapper modify type_id varchar(64) not null comment '模块主键id';

#2021/10/27 gengjb 山鹰MPLS报告报表新增语句
INSERT INTO `mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`) VALUES ('21', 'MPLS报告报表', 'MPLS报告报表', 2, 2, '2021-10-15 09:25:37', 'gjb', '2021-10-18 09:57:44', 'gjb', 0);

#2021/10/28 许可数量表
ALTER TABLE mw_license_list_table ADD COLUMN used_count int(11) NULL;
ALTER TABLE mw_license_list_table ADD COLUMN module_count int(11) NULL;
ALTER TABLE mw_license_list_table ADD COLUMN img_url varchar(255) NULL;
ALTER TABLE mw_license_list_table ADD COLUMN module_describe varchar(255) NULL;

#2021/10/29
alter table mw_thirdparty_server_table add encrypted_flag boolean default true null comment '密码是否加密标识';


#2021/11/01 #lmm 消息提示
DROP TABLE IF EXISTS `mw_system_message`;
CREATE TABLE `mw_system_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_text` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '消息创建时间',
  `own_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息所属人',
  `module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属哪个模块',
  `read_status` int(11) NULL DEFAULT 0 COMMENT '读取状态 1.已读 0.未读',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 170 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#2021/11/03 qzg 模型属性定义值表
CREATE TABLE `mw_cmdbmd_properties_value` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `properties_value_type` int(1) DEFAULT NULL COMMENT '属性类型id,1字符串,2整形数字,3浮点型数据,4布尔型,5日期类型,6结构体，7:Ip地址',
  `show_type` int(2) DEFAULT NULL COMMENT '显示类型（0默认，1多行字符串，2URL，3Markdown）',
  `default_type` int(2) DEFAULT NULL COMMENT '属性默认值类型（0固定值，1内置函数，2自增Id，3流水号）',
  `default_value` varchar(1024) DEFAULT NULL COMMENT '属性默认值',
  `regex` varchar(255) DEFAULT NULL COMMENT '正则表达式',
  `drop_op` varchar(512) DEFAULT NULL COMMENT '下拉框数组数据，以“,”分割',
  `default_value_list` varchar(512) DEFAULT NULL COMMENT '下拉框数组数据默认值，以“,”分割',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;


#2021/11/09 流量统计报表缓存表
CREATE TABLE `mw_linkflow_census_report` (
	`id` INT ( 10 ) UNSIGNED NOT NULL AUTO_INCREMENT,
	`time` VARCHAR ( 128 ) DEFAULT NULL COMMENT '时间区间',
	`assets_name` VARCHAR ( 128 ) DEFAULT NULL COMMENT '资产名称',
	`interface_name` VARCHAR ( 128 ) DEFAULT NULL COMMENT '接口名称',
	`acceptFlow_max` VARCHAR ( 128 ) DEFAULT NULL COMMENT '接收最大',
	`acceptFlow_avg` VARCHAR ( 128 ) DEFAULT NULL COMMENT '接收平均',
	`sendingFlow_max` VARCHAR ( 128 ) DEFAULT NULL COMMENT '发送最大',
	`sendingFlow_avg` VARCHAR ( 128 ) DEFAULT NULL COMMENT '发送平均',
	`assetsId` VARCHAR ( 128 ) DEFAULT NULL COMMENT '资产ID',
	`type` INT ( 10 ) DEFAULT NULL COMMENT '时间类型',
	`modification_date` datetime NOT NULL COMMENT '修改时间',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='流量统计报表数据缓存';

#2021/11/09 磁盘使用情况缓存表
CREATE TABLE `mw_diskuse_condition_report` (
	`id` INT ( 10 ) UNSIGNED NOT NULL AUTO_INCREMENT,
	`assets_name` VARCHAR ( 128 ) DEFAULT NULL COMMENT '资产名称',
	`ip_address` VARCHAR ( 128 ) DEFAULT NULL COMMENT 'IP地址',
	`type_name` VARCHAR ( 128 ) DEFAULT NULL COMMENT '分区名称',
	`disk_total` VARCHAR ( 128 ) DEFAULT NULL COMMENT '磁盘总容量',
	`disk_free` VARCHAR ( 128 ) DEFAULT NULL COMMENT '剩余磁盘容量',
	`disk_max_value` VARCHAR ( 128 ) DEFAULT NULL COMMENT '分区使用率最大',
	`disk_min_value` VARCHAR ( 128 ) DEFAULT NULL COMMENT '分区使用率最小',
	`disk_avg_value` VARCHAR ( 128 ) DEFAULT NULL COMMENT '分区使用率平均',
	`disk_use` VARCHAR ( 128 ) DEFAULT NULL COMMENT '已使用磁盘容量',
	`disk_usable` VARCHAR ( 128 ) DEFAULT NULL COMMENT '磁盘可用率',
	`assetsId` VARCHAR ( 128 ) DEFAULT NULL COMMENT '资产ID',
	`type` INT ( 10 ) DEFAULT NULL COMMENT '时间类型',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='磁盘使用情况报表数据缓存';

#2021/11/09  CPU与内存报表缓存表
CREATE TABLE `mw_cpuandmemory_report` (
	`id` INT ( 10 ) UNSIGNED NOT NULL AUTO_INCREMENT,
	`assets_name` VARCHAR ( 128 ) DEFAULT NULL COMMENT '资产名称',
	`ip` VARCHAR ( 128 ) DEFAULT NULL COMMENT 'IP地址',
	`max_value` VARCHAR ( 128 ) DEFAULT NULL COMMENT '最大利用率',
	`min_value` VARCHAR ( 128 ) DEFAULT NULL COMMENT '最小利用率',
	`avg_value` VARCHAR ( 128 ) DEFAULT NULL COMMENT '平均利用率',
	`disk_total` VARCHAR ( 128 ) DEFAULT NULL COMMENT '内存总容量',
	`disk_uesr` VARCHAR ( 128 ) DEFAULT NULL COMMENT '内存已使用容量',
	`disk_user_rate` VARCHAR ( 128 ) DEFAULT NULL COMMENT '内存利用率',
	`assetsId` VARCHAR ( 128 ) DEFAULT NULL COMMENT '资产ID',
	`type` INT ( 10 ) DEFAULT NULL COMMENT '时间类型',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='CPU与内存报表';

#2021/11/09 资产可用性报表缓存表
CREATE TABLE `mw_assetsusability_report` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `assets_usability` varchar(128) DEFAULT NULL COMMENT '资产可用性',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='资产可用性报表';

#20210915Lmm新加
alter table mw_ipaddressmanagelist_his_table add change_ip_status int(11) DEFAULT 0 not null comment '修改使用状态';

#2021/11/08 qzg 模型实例表新增实例图片、实例二维码字段
alter table mw_cmdbmd_instance add `instance_image` varchar(255) DEFAULT NULL COMMENT '资产实例图片';
alter table mw_cmdbmd_instance add  `instance_QRCode` varchar(255) DEFAULT NULL COMMENT '资产二维码图片';

#2021/11/13 ljb
alter table mw_diskuse_condition_report add update_time datetime null comment '更新时间';

alter table mw_diskuse_condition_report add update_success boolean null comment '更新结果';


#2021/11/15 lmm
CREATE TABLE `monitor`.`mw_report_run_time_status`  (
  `host_id` varchar(255) NULL,
  `asset_name` varchar(255) NULL,
  `ip` varchar(255) NULL,
  `item_name` varchar(255) NULL,
  `max_value` varchar(255) NULL,
  `min_value` varchar(255) NULL,
  `avg_value` varchar(255) NULL,
  `out_interface_avg_value` varchar(255) NULL,
	`sort_last_avg_value` float(11, 5) NULL,
  `date_type` int(12) NULL COMMENT '0.今天 1.昨天 2.上周'
);

#2021/11/17 qzg
alter table mw_cmdbmd_properties add `is_insert` tinyint(1)  COMMENT '结构体是否新增（0：导入，1：新增）';

#2021/11/18 lmm
truncate table mw_assetsusability_report;
ALTER TABLE `monitor`.`mw_assetsusability_report` ADD COLUMN `belong_time` date NULL COMMENT '所属时间' AFTER `type`;
ALTER TABLE `monitor`.`mw_report_run_time_status` ADD COLUMN `belong_time` date NULL COMMENT '所属时间' AFTER `date_type`;

#2021/11/25 lmm
truncate table mw_report_run_time_status;
ALTER TABLE `monitor`.`mw_report_run_time_status`
ADD COLUMN `interface_name` varchar(255) NULL COMMENT '接口名称' AFTER `sort_last_avg_value`,
ADD COLUMN `disk_name` varchar(255) NULL COMMENT '磁盘名称' AFTER `interface_name`,
ADD COLUMN `assetsId` varchar(128) NULL COMMENT '资产id' AFTER `disk_name`;

ALTER TABLE `monitor`.`mw_report_run_time_status`
ADD COLUMN `id` int NOT NULL AUTO_INCREMENT FIRST,
ADD PRIMARY KEY (`id`);


#2021/11/26 gengjb
CREATE TABLE `mw_mpls_history_report` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `mpls_data` mediumtext DEFAULT NULL,
  `link_name` varchar(255) DEFAULT NULL COMMENT '线路名称',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44131 DEFAULT CHARSET=utf8 COMMENT='mpls报表历史数据数据缓存';



#2021/12/02 gengjia

ALTER TABLE `mw_mpls_history_report`
ADD COLUMN `send_data` longtext NULL COMMENT '发送数据' AFTER `save_time`,
ADD COLUMN `accept_data` longtext NULL COMMENT '接收数据' AFTER `send_data`;

ALTER TABLE `mw_mpls_history_report`
ADD COLUMN `sort_send_data` longtext NULL COMMENT '平均小时发送数据',
ADD COLUMN `sort_accept_data` longtext NULL COMMENT '平均小时接收数据';

#2021/12/02 lmm
ALTER TABLE `monitor`.`mw_report_run_time_status`
MODIFY COLUMN `sort_last_avg_value` float(11, 2) NULL DEFAULT NULL AFTER `out_interface_avg_value`;

#2021/12/16 lmm
ALTER TABLE `monitor`.`mw_system_message`
MODIFY COLUMN `message_text` varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息内容' AFTER `id`;
#2021/12/22 lbq
CREATE TABLE `mw_alert_rule_select_event`  (
  `text` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '告警内容',
  `hostid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资产',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '告警标题',
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主机IP',
  `is_alarm` tinyint(1) NULL DEFAULT NULL,
  `date` datetime(0) NULL DEFAULT NULL COMMENT '日期',
  `size` int(32) NULL DEFAULT NULL COMMENT '条数',
  `uuid` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#2021/12/22 lbq
CREATE TABLE `mw_alert_rule_select`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `deep` int(32) NULL DEFAULT NULL,
  `condition_unit` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `relation` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `value` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `parent_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `uuid` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#2021/12/22 lbq
CREATE TABLE `mw_alert_select_effect_mapper`  (
  `effect_time_select` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `state` int(1) NULL DEFAULT NULL COMMENT '告警状态0：告警，1：不告警',
  `alarm_compression_select` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `custom_time` int(32) NULL DEFAULT NULL COMMENT '自定义时间',
  `time_unit` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间单位',
  `custom_num` int(32) NULL DEFAULT NULL COMMENT '自定义次数',
  `num_unit` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '自定义次数单位',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `uuid` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `success_num` int(11) UNSIGNED NULL DEFAULT 0,
  `fail_num` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#2021/12/22 lbq
ALTER TABLE `monitor`.`mw_alert_action_level_rule`
ADD COLUMN `date_two` float(11, 1) NULL AFTER `date`,
ADD COLUMN `date_three` float(11, 1) NULL AFTER `date_two`,
ADD COLUMN `level` int(11) NULL COMMENT '页面选择的级别' AFTER `date_three`;

#2021/12/22 lbq
CREATE TABLE `mw_alert_action_assetsclumn_mapper`  (
  `action_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `clumn_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `clumn_coment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#2021/12/24 lbq
CREATE TABLE `mw_alert_action_level_rule_mapper`  (
  `action_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `rule_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `level` int(1) NULL DEFAULT NULL,
  `time_unit` int(1) NULL DEFAULT NULL COMMENT '时间单位'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


#20211224 gengjb 报表逻辑优化建表语句 --start
CREATE TABLE `mw_linkflow_report_daily` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `time` varchar(128) DEFAULT NULL COMMENT '时间区间',
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `interface_name` varchar(128) DEFAULT NULL COMMENT '接口名称',
  `acceptFlow_max` varchar(128) DEFAULT NULL COMMENT '接收最大',
  `acceptFlow_avg` varchar(128) DEFAULT NULL COMMENT '接收平均',
  `sendingFlow_max` varchar(128) DEFAULT NULL COMMENT '发送最大',
  `sendingFlow_avg` varchar(128) DEFAULT NULL COMMENT '发送平均',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50530 DEFAULT CHARSET=utf8 COMMENT='流量统计报表天数据缓存';

CREATE TABLE `mw_linkflow_report_weekly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `time` varchar(128) DEFAULT NULL COMMENT '时间区间',
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `interface_name` varchar(128) DEFAULT NULL COMMENT '接口名称',
  `acceptFlow_max` varchar(128) DEFAULT NULL COMMENT '接收最大',
  `acceptFlow_avg` varchar(128) DEFAULT NULL COMMENT '接收平均',
  `sendingFlow_max` varchar(128) DEFAULT NULL COMMENT '发送最大',
  `sendingFlow_avg` varchar(128) DEFAULT NULL COMMENT '发送平均',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `week_date` varchar(255) DEFAULT NULL COMMENT '周时间区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44259 DEFAULT CHARSET=utf8 COMMENT='流量统计报表上周数据缓存';

CREATE TABLE `mw_linkflow_report_monthly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `time` varchar(128) DEFAULT NULL COMMENT '时间区间',
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `interface_name` varchar(128) DEFAULT NULL COMMENT '接口名称',
  `acceptFlow_max` varchar(128) DEFAULT NULL COMMENT '接收最大',
  `acceptFlow_avg` varchar(128) DEFAULT NULL COMMENT '接收平均',
  `sendingFlow_max` varchar(128) DEFAULT NULL COMMENT '发送最大',
  `sendingFlow_avg` varchar(128) DEFAULT NULL COMMENT '发送平均',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `month_date` varchar(255) DEFAULT NULL COMMENT '月时间区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44016 DEFAULT CHARSET=utf8 COMMENT='流量统计报表上月数据缓存';

CREATE TABLE `mw_assetsusability_daily` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `assets_usability` varchar(128) DEFAULT NULL COMMENT '资产可用性',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  `save_time` date DEFAULT NULL COMMENT '所属时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18058 DEFAULT CHARSET=utf8 COMMENT='资产可用性报表-按天存储';

CREATE TABLE `mw_assetsusability_weekly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `assets_usability` varchar(128) DEFAULT NULL COMMENT '资产可用性',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  `save_time` date DEFAULT NULL COMMENT '所属时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  `week_date` varchar(255) DEFAULT NULL COMMENT '周时间区间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17228 DEFAULT CHARSET=utf8 COMMENT='资产可用性报表-按周存储';


CREATE TABLE `mw_assetsusability_monthly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `assets_usability` varchar(128) DEFAULT NULL COMMENT '资产可用性',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `type` int(10) DEFAULT NULL COMMENT '时间类型',
  `save_time` date DEFAULT NULL COMMENT '所属时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  `month_date` varchar(255) DEFAULT NULL COMMENT '周时间区间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17193 DEFAULT CHARSET=utf8 COMMENT='资产可用性报表-按月存储';



CREATE TABLE `mw_mplshistory_daily` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `link_name` varchar(255) DEFAULT NULL COMMENT '线路名称',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `send_data` longtext DEFAULT NULL COMMENT '发送数据',
  `accept_data` longtext DEFAULT NULL COMMENT '接收数据',
  `sort_send_data` longtext DEFAULT NULL COMMENT '平均小时发送数据',
  `sort_accept_data` longtext DEFAULT NULL COMMENT '平均小时接收数据',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2816 DEFAULT CHARSET=utf8 COMMENT='mpls报表历史数据数据缓存-按天存储';

CREATE TABLE `mw_mplshistory_weekly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `link_name` varchar(255) DEFAULT NULL COMMENT '线路名称',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `send_data` longtext DEFAULT NULL COMMENT '发送数据',
  `accept_data` longtext DEFAULT NULL COMMENT '接收数据',
  `week_date` varchar(255) DEFAULT NULL COMMENT '周日期区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4906 DEFAULT CHARSET=utf8 COMMENT='mpls报表历史数据数据缓存-按周存储';

CREATE TABLE `mw_mplshistory_monthly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `link_name` varchar(255) DEFAULT NULL COMMENT '线路名称',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `send_data` longtext DEFAULT NULL COMMENT '发送数据',
  `accept_data` longtext DEFAULT NULL COMMENT '接收数据',
  `month_date` varchar(255) DEFAULT NULL COMMENT '月日期区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6922 DEFAULT CHARSET=utf8 COMMENT='mpls报表历史数据数据缓存-按月存储';

CREATE TABLE `mw_diskuse_daily` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `type_name` varchar(128) DEFAULT NULL COMMENT '分区名称',
  `disk_total` varchar(128) DEFAULT NULL COMMENT '磁盘总容量',
  `disk_free` varchar(128) DEFAULT NULL COMMENT '剩余磁盘容量',
  `disk_max_value` varchar(128) DEFAULT NULL COMMENT '分区使用率最大',
  `disk_min_value` varchar(128) DEFAULT NULL COMMENT '分区使用率最小',
  `disk_avg_value` varchar(128) DEFAULT NULL COMMENT '分区使用率平均',
  `disk_use` varchar(128) DEFAULT NULL COMMENT '已使用磁盘容量',
  `disk_usable` varchar(128) DEFAULT NULL COMMENT '磁盘可用率',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2712 DEFAULT CHARSET=utf8 COMMENT='磁盘使用情况报表数据缓存-按天存储';

CREATE TABLE `mw_diskuse_weekly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `type_name` varchar(128) DEFAULT NULL COMMENT '分区名称',
  `disk_total` varchar(128) DEFAULT NULL COMMENT '磁盘总容量',
  `disk_free` varchar(128) DEFAULT NULL COMMENT '剩余磁盘容量',
  `disk_max_value` varchar(128) DEFAULT NULL COMMENT '分区使用率最大',
  `disk_min_value` varchar(128) DEFAULT NULL COMMENT '分区使用率最小',
  `disk_avg_value` varchar(128) DEFAULT NULL COMMENT '分区使用率平均',
  `disk_use` varchar(128) DEFAULT NULL COMMENT '已使用磁盘容量',
  `disk_usable` varchar(128) DEFAULT NULL COMMENT '磁盘可用率',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `week_date` varchar(255) DEFAULT NULL COMMENT '周时间区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1783 DEFAULT CHARSET=utf8 COMMENT='磁盘使用情况报表数据缓存-按周存储';

CREATE TABLE `mw_diskuse_monthly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `type_name` varchar(128) DEFAULT NULL COMMENT '分区名称',
  `disk_total` varchar(128) DEFAULT NULL COMMENT '磁盘总容量',
  `disk_free` varchar(128) DEFAULT NULL COMMENT '剩余磁盘容量',
  `disk_max_value` varchar(128) DEFAULT NULL COMMENT '分区使用率最大',
  `disk_min_value` varchar(128) DEFAULT NULL COMMENT '分区使用率最小',
  `disk_avg_value` varchar(128) DEFAULT NULL COMMENT '分区使用率平均',
  `disk_use` varchar(128) DEFAULT NULL COMMENT '已使用磁盘容量',
  `disk_usable` varchar(128) DEFAULT NULL COMMENT '磁盘可用率',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `save_time` date DEFAULT NULL COMMENT '添加时间',
  `month_date` varchar(255) DEFAULT NULL COMMENT '月时间区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1657 DEFAULT CHARSET=utf8 COMMENT='磁盘使用情况报表数据缓存-按月存储';


CREATE TABLE `mw_runstate_daily` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产id',
  `host_id` varchar(255) DEFAULT NULL COMMENT '主机ID',
  `asset_name` varchar(255) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(255) DEFAULT NULL COMMENT 'IP地址',
  `item_name` varchar(255) DEFAULT NULL COMMENT '分类名称',
  `max_value` varchar(255) DEFAULT NULL,
  `min_value` varchar(255) DEFAULT NULL,
  `avg_value` varchar(255) DEFAULT NULL,
  `out_interface_avg_value` varchar(255) DEFAULT NULL,
  `sort_last_avg_value` float(11,5) DEFAULT NULL,
  `interface_name` varchar(255) DEFAULT NULL COMMENT '接口名称',
  `disk_name` varchar(255) DEFAULT NULL COMMENT '磁盘名称',
  `save_time` date DEFAULT NULL COMMENT '所属储存时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10153 DEFAULT CHARSET=utf8 COMMENT='运行状态报表-按天存储';

CREATE TABLE `mw_runstate_weekly` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产id',
  `host_id` varchar(255) DEFAULT NULL COMMENT '主机ID',
  `asset_name` varchar(255) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(255) DEFAULT NULL COMMENT 'IP地址',
  `item_name` varchar(255) DEFAULT NULL COMMENT '分类名称',
  `max_value` varchar(255) DEFAULT NULL,
  `min_value` varchar(255) DEFAULT NULL,
  `avg_value` varchar(255) DEFAULT NULL,
  `out_interface_avg_value` varchar(255) DEFAULT NULL,
  `sort_last_avg_value` float(11,5) DEFAULT NULL,
  `interface_name` varchar(255) DEFAULT NULL COMMENT '接口名称',
  `disk_name` varchar(255) DEFAULT NULL COMMENT '磁盘名称',
  `save_time` date DEFAULT NULL COMMENT '所属储存时间',
  `week_date` varchar(255) DEFAULT NULL COMMENT '周日期区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=609 DEFAULT CHARSET=utf8 COMMENT='运行状态报表-按周存储';

CREATE TABLE `mw_runstate_monthly` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产id',
  `host_id` varchar(255) DEFAULT NULL COMMENT '主机ID',
  `asset_name` varchar(255) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(255) DEFAULT NULL COMMENT 'IP地址',
  `item_name` varchar(255) DEFAULT NULL COMMENT '分类名称',
  `max_value` varchar(255) DEFAULT NULL,
  `min_value` varchar(255) DEFAULT NULL,
  `avg_value` varchar(255) DEFAULT NULL,
  `out_interface_avg_value` varchar(255) DEFAULT NULL,
  `sort_last_avg_value` float(11,5) DEFAULT NULL,
  `interface_name` varchar(255) DEFAULT NULL COMMENT '接口名称',
  `disk_name` varchar(255) DEFAULT NULL COMMENT '磁盘名称',
  `save_time` date DEFAULT NULL COMMENT '所属储存时间',
  `month_date` varchar(255) DEFAULT NULL COMMENT '月日期区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=174 DEFAULT CHARSET=utf8 COMMENT='运行状态报表-按月存储';

CREATE TABLE `mw_cpuandmemory_daily` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `max_value` varchar(128) DEFAULT NULL COMMENT '最大利用率',
  `min_value` varchar(128) DEFAULT NULL COMMENT '最小利用率',
  `avg_value` varchar(128) DEFAULT NULL COMMENT '平均利用率',
  `disk_total` varchar(128) DEFAULT NULL COMMENT '内存总容量',
  `disk_uesr` varchar(128) DEFAULT NULL COMMENT '内存已使用容量',
  `disk_user_rate` varchar(128) DEFAULT NULL COMMENT '内存利用率',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `save_time` date DEFAULT NULL COMMENT '数据来源时间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10368 DEFAULT CHARSET=utf8 COMMENT='CPU与内存报表-按天存储';

CREATE TABLE `mw_cpuandmemory_weekly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `max_value` varchar(128) DEFAULT NULL COMMENT '最大利用率',
  `min_value` varchar(128) DEFAULT NULL COMMENT '最小利用率',
  `avg_value` varchar(128) DEFAULT NULL COMMENT '平均利用率',
  `disk_total` varchar(128) DEFAULT NULL COMMENT '内存总容量',
  `disk_uesr` varchar(128) DEFAULT NULL COMMENT '内存已使用容量',
  `disk_user_rate` varchar(128) DEFAULT NULL COMMENT '内存利用率',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `save_time` date DEFAULT NULL COMMENT '数据来源时间',
  `week_date` varchar(255) DEFAULT NULL COMMENT '周日期区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9719 DEFAULT CHARSET=utf8 COMMENT='CPU与内存报表-按周存储';

CREATE TABLE `mw_cpuandmemory_monthly` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assets_name` varchar(128) DEFAULT NULL COMMENT '资产名称',
  `ip` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  `max_value` varchar(128) DEFAULT NULL COMMENT '最大利用率',
  `min_value` varchar(128) DEFAULT NULL COMMENT '最小利用率',
  `avg_value` varchar(128) DEFAULT NULL COMMENT '平均利用率',
  `disk_total` varchar(128) DEFAULT NULL COMMENT '内存总容量',
  `disk_uesr` varchar(128) DEFAULT NULL COMMENT '内存已使用容量',
  `disk_user_rate` varchar(128) DEFAULT NULL COMMENT '内存利用率',
  `assetsId` varchar(128) DEFAULT NULL COMMENT '资产ID',
  `save_time` date DEFAULT NULL COMMENT '数据来源时间',
  `month_date` varchar(255) DEFAULT NULL COMMENT '月日期区间',
  `update_success` tinyint(1) DEFAULT NULL COMMENT '更新结果',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9712 DEFAULT CHARSET=utf8 COMMENT='CPU与内存报表-按月存储';
#20211224 gengjb 报表逻辑优化建表语句 --end

#2021/12/28 lbq
CREATE TABLE `mw_alert_record_user_mapper`  (
  `id` int(11) NULL DEFAULT NULL,
  `user_id` int(11) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#2022/01/04 gengjb 手动执行定时任务参数表
CREATE TABLE `mw_report_down` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `report_id` int(10) DEFAULT NULL COMMENT '报表ID',
  `report_name` varchar(256) DEFAULT NULL COMMENT '报表定时任务名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;



