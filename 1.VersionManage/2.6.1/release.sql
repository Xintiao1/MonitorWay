#Lmm 20220217 定时任务sql
DELETE FROM `monitor`.`mw_ncm_timetask_action`;
DELETE FROM mw_ncm_timetask_tree;
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (1, 1, '数据收敛', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskTwo', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (2, 1, '数据输出', 'cn.mw.monitor.report.timer.MwReportTimeSendEmail', 'reportSendEmail', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (3, 1, '数据缓存', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskThere', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (4, 2, '数据缓存', 'cn.mw.monitor.virtualization.service.impl.MwVirtualServiceImpl', 'saveVirtualTree', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (5, 3, '数据缓存', 'cn.mw.time.MWZabbixConnetStateTime', 'zabbixConnectCheck', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (6, 4, '配置备份', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'downloadConfig', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (7, 4, '配置执行', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'execConfigScript', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (8, 4, '合规检测', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'execReport', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (9, 5, '数据扫描', 'cn.mw.monitor.ipaddressmanage.service.impl.MwIpAddressManageServiceScanImpl', 'cronBatchScanIp', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (10, 6, '数据扫描', 'cn.mw.monitor.screen.timer.MWNewScreenTime', 'censusAssetsAmount', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (11, 4, '配置比对', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'compareConfigContent', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (1, 1, 1);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (2, 1, 2);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (3, 1, 3);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (4, 2, 4);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (5, 3, 5);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (6, 4, 6);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (7, 4, 7);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (8, 4, 8);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (9, 5, 9);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (10, 6, 10);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (11, 4, 11);

#20220331 ljb
drop table if exists mw_topo_group;
create table mw_topo_group
(
    path_id     int auto_increment primary key,
    id          varchar(32)  not null comment 'id',
    parent_id   varchar(32)  null comment '父id',
    name        varchar(50)  null comment '分组名',
    path        varchar(255) null comment '节点路径',
    modify_time timestamp    null comment '修改时间',
    modifier    varchar(32)  null comment '修改人'
);
INSERT INTO monitor.mw_topo_group (id, parent_id, name, path, modify_time, modifier) VALUES ('1648634443605826452fa136f46e883d', null, '/', '1-', null, null);

alter table monitor.mw_topo_graph add group_id varchar(32) null comment '所属分组id';
update monitor.mw_topo_graph set group_id='1648634443605826452fa136f46e883d';

#gengjb 20220401 首页模块增加初始化字段
ALTER TABLE `monitor`.`mw_newhomepage_init`
ADD COLUMN `init_type` int(10) NULL COMMENT '初始化类型：1.需要初始化  2.不需要初始化';
update mw_newhomepage_init set init_type = 1;

#qzg 20220413 新增实例拓扑关联表
CREATE TABLE `mw_cmdbmd_instance_relation` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `own_model_id` int(11) NOT NULL COMMENT '当前模型id',
  `own_instance_id` int(11) DEFAULT NULL COMMENT '当前实例id',
  `opposite_model_id` int(11) DEFAULT NULL COMMENT '关联模型id',
  `opposite_instance_id` int(11) DEFAULT NULL COMMENT '关联实例id',
  `creator` varchar(128) DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8 COMMENT='模型实例关联表';

#qzg 20220413
ALTER TABLE `monitor`.`mw_cmdbmd_relations`
MODIFY COLUMN `opposite_relation_num` int(1) DEFAULT NULL COMMENT '关联模型关联个数(1:0-1或者2:0-n)';


DROP TABLE IF EXISTS `mw_child_server_table`;
CREATE TABLE `mw_child_server_table` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `server_name` varchar(255) DEFAULT NULL COMMENT '服务名称',
  `server_ip` varchar(255) DEFAULT NULL COMMENT '服务IP',
  `server_port` varchar(255) DEFAULT NULL COMMENT '服务端口',
  `server_version` varchar(255) DEFAULT NULL COMMENT '服务版本',
  `server_enable` varchar(255) DEFAULT NULL COMMENT '是否可以使用',
  `delete_flag` varchar(255) DEFAULT NULL COMMENT '是否删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='子服务表（用于nacos维护）';

ALTER TABLE `mw_accountmanage_table`
ADD COLUMN `system_type` varchar(255) NULL DEFAULT NULL COMMENT '系统类别（1：Linux  2：Windows）' AFTER `enable_password`;

DROP TABLE IF EXISTS `mw_script_manage_table`;
CREATE TABLE `mw_script_manage_table` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `script_name` varchar(255) NOT NULL COMMENT '脚本名称',
  `script_tree_id` int(11) unsigned NOT NULL COMMENT '脚本所在树ID',
  `script_type` varchar(255) DEFAULT NULL COMMENT '脚本类别（sh   batchfile）',
  `script_content` text DEFAULT NULL COMMENT '脚本内容',
  `script_desc` varchar(1024) DEFAULT NULL COMMENT '脚本描述',
  `script_version` varchar(255) DEFAULT NULL COMMENT '脚本版本',
  `account_id` int(11) DEFAULT NULL COMMENT '账户Id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '更新人',
  `delete_flag` tinyint(4) DEFAULT NULL COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='猫维——脚本管理表';

DROP TABLE IF EXISTS `mw_script_exe_history_log`;
CREATE TABLE `mw_script_exe_history_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exec_id` int(11) unsigned DEFAULT NULL COMMENT '执行ID',
  `script_id` int(11) DEFAULT NULL COMMENT '脚本ID',
  `script_name` varchar(255) DEFAULT NULL COMMENT '脚本名称',
  `assets_id` varchar(255) DEFAULT NULL COMMENT '资产ID',
  `assets_ip` varchar(255) DEFAULT NULL COMMENT '资产IP',
  `assets_port` varchar(255) DEFAULT NULL COMMENT '资产端口',
  `account_id` int(11) DEFAULT NULL COMMENT '账户ID',
  `over_time` int(11) DEFAULT NULL COMMENT '最大超时时间',
  `cost_time` int(11) DEFAULT NULL COMMENT '耗时（毫秒）',
  `return_content` text DEFAULT NULL COMMENT '返回内容',
  `script_content` text DEFAULT NULL COMMENT '脚本内容',
  `exec_status` tinyint(4) unsigned DEFAULT 0 COMMENT '执行状态，0：初始化  1：执行中   2：执行结束   9：执行错误',
  `exec_type` int(11) DEFAULT NULL COMMENT '1：页面执行',
  `script_param` varchar(255) DEFAULT NULL COMMENT '脚本参数',
  `is_sensitive` tinyint(4) DEFAULT NULL COMMENT '是否为敏感参数',
  `mission_type` int(11) DEFAULT NULL COMMENT '1:脚本执行  2：文件分发',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `delete_flag` tinyint(4) DEFAULT NULL COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='脚本执行记录表';
#gengjb 20220401 基线配置新增表
CREATE TABLE `mw_baseline_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '基线ID',
  `date_type` int(10) DEFAULT NULL COMMENT '时间类型',
  `item_id` varchar(128) DEFAULT NULL COMMENT '监控项ID',
  `unit` varchar(256) DEFAULT NULL COMMENT '统计单位',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `name` varchar(255) DEFAULT NULL COMMENT '基线名称',
  `enable` int(10) DEFAULT NULL COMMENT '启用状态：1.已启用 2.未启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8 COMMENT='基线配置数据表';

CREATE TABLE `mw_baseline_item` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '基线监控项ID',
  `name` varchar(256) DEFAULT NULL COMMENT '基线监控项名称',
  `item_name` varchar(256) DEFAULT NULL COMMENT '对应数据源监控项名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8 COMMENT='基线配置监控项表';

CREATE TABLE `mw_baseline_health_value` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '健康值ID',
  `assets_id` varchar(256) DEFAULT NULL COMMENT '资产主机ID',
  `item_name` varchar(256) DEFAULT NULL COMMENT '监控项名称',
  `value` varchar(256) DEFAULT NULL COMMENT '健康值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=458 DEFAULT CHARSET=utf8 COMMENT='基线配置健康值存储表';

#2022/4/15 lbq
ALTER TABLE `monitor`.`mw_sys_log_rule_mapper`
ADD COLUMN `action` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '动作' AFTER `delete_flag`;


#lmm 20220414 ipv6
ALTER TABLE `monitor`.`mw_ipv6managelist_table`
ADD COLUMN `is_rewrite` int(11) NULL DEFAULT NULL COMMENT '覆盖' AFTER `is_include`;

#ljb 20220416 拓扑列表排序
create table mw_topo_group_userinfo
(
    id         int auto_increment primary key,
    user       varchar(32) null comment '用户名',
    group_info longtext    null
);

#lmm 20220414 ipv6
ALTER TABLE `monitor`.`mw_ipv6managelist_table`
ADD COLUMN `is_rewrite` int(11) NULL DEFAULT NULL COMMENT '覆盖' AFTER `is_include`;

#ljb 20220418 修改拓扑排序
drop table if exists mw_topo_group_userinfo;
create table mw_topo_group_info
(
    id         int auto_increment primary key,
    group_info longtext    null
);

ALTER TABLE `mw_accountmanage_table`
ADD COLUMN `pid` int(11) NULL COMMENT '父ID，当system_type=mysql的时候，生效' AFTER `system_type`;

UPDATE mw_accountmanage_table set pid = 0;

#gqw 20220420 自动化模块及注册中心权限
DELETE from mw_role_module_perm_mapper  where module_id = 232;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-browse', 0, 232, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-cn_mw_sign', 0, 232, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-create', 0, 232, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-delete', 0, 232, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-editor', 0, 232, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-perform', 0, 232, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-232-secopassword', 0, 232, 'secopassword', 1);

DELETE from mw_role_module_perm_mapper  where module_id = 234;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-browse', 0, 234, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-cn_mw_sign', 0, 234, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-create', 0, 234, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-delete', 0, 234, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-editor', 0, 234, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-perform', 0, 234, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-234-secopassword', 0, 234, 'secopassword', 1);

DELETE from mw_role_module_perm_mapper  where module_id = 236;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-browse', 0, 236, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-cn_mw_sign', 0, 236, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-create', 0, 236, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-delete', 0, 236, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-editor', 0, 236, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-perform', 0, 236, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-236-secopassword', 0, 236, 'secopassword', 1);

DELETE from mw_role_module_perm_mapper  where module_id = 237;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-browse', 0, 237, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-cn_mw_sign', 0, 237, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-create', 0, 237, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-delete', 0, 237, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-editor', 0, 237, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-perform', 0, 237, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-237-secopassword', 0, 237, 'secopassword', 1)

DELETE from mw_role_module_perm_mapper  where module_id = 238;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-browse', 0, 238, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-cn_mw_sign', 0, 238, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-create', 0, 238, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-delete', 0, 238, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-editor', 0, 238, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-perform', 0, 238, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-238-secopassword', 0, 238, 'secopassword', 1);

DELETE from mw_role_module_perm_mapper  where module_id = 239;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-browse', 0, 239, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-cn_mw_sign', 0, 239, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-create', 0, 239, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-delete', 0, 239, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-editor', 0, 239, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-perform', 0, 239, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-239-secopassword', 0, 239, 'secopassword', 1);

DELETE from mw_role_module_perm_mapper  where module_id = 240;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-browse', 0, 240, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-cn_mw_sign', 0, 240, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-create', 0, 240, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-delete', 0, 240, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-editor', 0, 240, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-perform', 0, 240, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-240-secopassword', 0, 240, 'secopassword', 1);

#gengjb 20220420 基线设置权限
DELETE from mw_role_module_perm_mapper  where module_id = 235;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-235-browse', 0, 235, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-235-create', 0, 235, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-235-delete', 0, 235, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-235-editor', 0, 235, 'editor', 1);

#qzg 20220418
CREATE TABLE `mw_cmdbmd_instance_relation_setting` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `own_model_id` int(11) DEFAULT NULL COMMENT '当前模型id',
  `own_instance_id` int(11) DEFAULT NULL COMMENT '当前实例id',
  `hide_model_id` int(11) DEFAULT NULL COMMENT '隐藏的下级模型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


#lmm 20220425
ALTER TABLE `monitor`.`mw_ip_distribution`
ADD COLUMN `bang_distri` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '绑定的关系 最早关系为空' AFTER `orgtext`;
ALTER TABLE `monitor`.`mw_ipam_oper_history`
ADD COLUMN `bang_distri` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '绑定的关系 最早关系为空' AFTER `descript`;

#gqw 创建新的账户表
DROP TABLE IF EXISTS `mw_script_account_manage_table`;
CREATE TABLE `mw_script_account_manage_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `account` varchar(128) DEFAULT NULL COMMENT '账号',
  `account_alias` varchar(128) DEFAULT NULL COMMENT '账户别名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `port` varchar(128) DEFAULT NULL COMMENT '端口',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `account_desc` varchar(255) DEFAULT NULL COMMENT '描述',
  `system_type` varchar(255) DEFAULT NULL COMMENT '系统类别（Linux  Windows  mysql）',
  `pid` int(11) DEFAULT NULL COMMENT '父ID，当system_type=mysql的时候，生效',
  `delete_flag` tinyint(4) DEFAULT NULL COMMENT '是否为删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='自动化——账户管理数据\r\n维护脚本管理的系统账户和数据库账户';

#lmm 20220428
ALTER TABLE `monitor`.`mw_ncm_timetask_time_plan`
MODIFY COLUMN `time_choice` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选择小时周对应选项' AFTER `time_type`;


#lmm 20220503
ALTER TABLE `monitor`.`mw_ipaddressmanagelist_table`
DROP COLUMN `is_tem`,
ADD COLUMN `is_tem` int(11) NULL DEFAULT 0 COMMENT 'IP地址是否临时' AFTER `is_conflict`;
ALTER TABLE `monitor`.`mw_ipv6managelist_table`
ADD COLUMN `is_tem` int(11) NULL DEFAULT 0 COMMENT 'IP地址是否临时' AFTER `is_rewrite`;

#qzg 20220505 资源中心-模板管理表
CREATE TABLE `mw_cmdbmd_template_server_mapper` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `assetstemplate_id` int(11) DEFAULT NULL COMMENT '资产扫描模版id',
  `server_id` int(11) DEFAULT NULL COMMENT '第三方服务器id',
  `template_id` varchar(128) DEFAULT NULL COMMENT 'zabbix服务器中的模版id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='资产扫描模版和第三方服务器关联表';
#qzg 20220505 资源中心-模板管理表
CREATE TABLE `mw_cmdbmd_template_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `template_name` varchar(128) DEFAULT NULL COMMENT '关联模板名',
  `system_objid` varchar(128) DEFAULT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `brand` varchar(128) DEFAULT NULL COMMENT '厂商',
  `specification` varchar(128) DEFAULT NULL COMMENT '型号规格',
  `assets_type_id` int(11) NOT NULL COMMENT '资产类型',
  `sub_assets_type_id` int(11) DEFAULT NULL COMMENT '子类型',
  `monitor_mode` int(11) NOT NULL COMMENT '监控协议',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `interfaces_type` int(11) NOT NULL COMMENT 'zabbix interfaceType 1:zabbix agent; 2:SNMP; 3:IPMI; 4:JMX',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

#qzg 20220505 资源中心-厂商表
CREATE TABLE `mw_cmdbmd_vendor_icon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vendor` varchar(255) DEFAULT NULL COMMENT '品牌/厂商',
  `vendor_small_icon` varchar(255) DEFAULT NULL COMMENT '小图标',
  `vendor_large_icon` varchar(255) DEFAULT NULL COMMENT '大图标',
  `custom_flag` int(2) DEFAULT NULL COMMENT '图标类型： 0-系统定义，1-用户上传',
  `description` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#qzg 20220505 资源中心-规格型号表
CREATE TABLE `mw_cmdbmd_vendor_specification` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `brand` varchar(128) DEFAULT NULL COMMENT '厂商',
  `specification` varchar(128) DEFAULT NULL COMMENT '规格型号',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `vendor_id` int(11) NOT NULL COMMENT '厂商/品牌id',
  `creator` varchar(255) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(255) NOT NULL COMMENT '修改人',
  `modification_Date` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#lbq 2022/5/7
ALTER TABLE `monitor`.`mw_alert_shenzhenSMS_rule`
ADD COLUMN `api_url` varchar(255) NULL COMMENT 'api接口地址' AFTER `app_sms_code`,
ADD COLUMN `role_name` varchar(255) NULL COMMENT '角色名' AFTER `api_url`;


#gengjb 20220507 CPU报表每天每周每月增加内存最大最小字段
ALTER TABLE `monitor`.`mw_cpuandmemory_daily`
ADD COLUMN `memory_max_utilizationRate` varchar(128) NULL COMMENT '内存最大利用率',
ADD COLUMN `memory_min_utilizationRate` varchar(128) NULL COMMENT '内存最小利用率' AFTER `memory_max_utilizationRate`;

ALTER TABLE `monitor`.`mw_cpuandmemory_weekly`
ADD COLUMN `memory_max_utilizationRate` varchar(128) NULL COMMENT '内存最大利用率',
ADD COLUMN `memory_min_utilizationRate` varchar(128) NULL COMMENT '内存最小利用率' AFTER `memory_max_utilizationRate`;

ALTER TABLE `monitor`.`mw_cpuandmemory_monthly`
ADD COLUMN `memory_max_utilizationRate` varchar(128) NULL COMMENT '内存最大利用率',
ADD COLUMN `memory_min_utilizationRate` varchar(128) NULL COMMENT '内存最小利用率' AFTER `memory_max_utilizationRate`;

#qzg 20220505
CREATE TABLE `mw_cmdbmd_modelgroup_server_mapper` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `group_id` varchar(128) DEFAULT NULL COMMENT 'Zabbix群租对应groupId',
  `monitor_server_id` varchar(128) DEFAULT NULL COMMENT '监控服务器id',
  `model_id` varchar(128) DEFAULT NULL COMMENT '模型id;相当于以前的类型管理表对应主键',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

#qzg 20220510 日志管理权限
delete from mw_role_module_perm_mapper where module_id = 224;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-browse', 0, 224, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-cn_mw_sign', 0, 224, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-create', 0, 224, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-delete', 0, 224, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-editor', 0, 224, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-perform', 0, 224, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-224-secopassword', 0, 224, 'secopassword', 1);

#qzg 20220510 资源中心-特征管理权限
delete from mw_role_module_perm_mapper where module_id = 242;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-browse', 0, 242, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-cn_mw_sign', 0, 242, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-create', 0, 242, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-delete', 0, 242, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-editor', 0, 242, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-perform', 0, 242, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-242-secopassword', 0, 242, 'secopassword', 1);


#gengjb 20220512 增加CPU内存实时报表
INSERT INTO `monitor`.`mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`, `send_time`) VALUES ('25', 'CPU内存实时报表', 'CPU内存实时报表', 2, 1, '2022-05-12 14:03:05', 'gjb', '2022-05-12 14:03:10', 'gjb', 0, NULL);

#lbao 2022/5/12 通知规则tcp
CREATE TABLE `mw_alert_tcp_udp_rule`  (
  `rule_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `host` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `port` int(11) NULL DEFAULT NULL,
  `agreement_type` int(11) NULL DEFAULT NULL,
  `algorithm` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '算法',
  `key_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密钥路径',
  `key_password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密钥密码',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#gengjb 2022/05/17 首页ip冲突统计模块改为IP冲突统计
update mw_newhomepage_init set bulk_name = 'IP冲突统计' where bulk_name = 'ip冲突统计';
update mw_newindex_bulk set bulk_name = 'IP冲突统计' where bulk_name = 'ip冲突统计';

#gengjb 2022/05/18 监控大屏添加地图组件
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES (33, '中国地图', '中国地图', NULL, NULL, NULL, '地图', 1, NULL);
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES (34, '四川地图', '四川地图', NULL, NULL, NULL, '地图', 1, NULL);
INSERT INTO `monitor`.`mw_layout_base`(`id`, `layout_type`, `count`) VALUES (10, '类型二6宫格', 6);
update mw_layout_base set count = 9 where id = 6;

#qzg 2022/05/19 资产接口信息表
CREATE TABLE `mw_cmdbmd_assets_interface` (
  `id` int(11) NOT NULL COMMENT 'id',
  `ifIndex` int(11) DEFAULT NULL COMMENT '接口编号',
  `name` varchar(255) DEFAULT NULL COMMENT '接口名称',
  `type` varchar(255) DEFAULT NULL COMMENT '接口类型',
  `state` varchar(255) DEFAULT NULL COMMENT '接口状态',
  `description` varchar(255) DEFAULT NULL COMMENT '接口描述',
  `mac` varchar(255) DEFAULT NULL COMMENT 'Mac地址',
  `mtu` int(11) DEFAULT NULL COMMENT 'MTU',
  `ip` varchar(255) DEFAULT NULL COMMENT 'IP',
  `subnet_mask` varchar(255) DEFAULT NULL COMMENT '子网掩码',
  `vlan_mode` varchar(255) DEFAULT NULL COMMENT '(TRUNK、ACCESS、HYBIRD)',
  `vlan` varchar(255) DEFAULT NULL COMMENT 'access-->vlan tag(数字)；trunk --> trunk',
  `vrf` varchar(255) DEFAULT NULL COMMENT 'vrf',
  `assets_id` int(11) NOT NULL COMMENT '关联设备Id',
  `creator` varchar(255) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(255) NOT NULL COMMENT '修改人',
  `modification_Date` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资产zabbix接口信息表';
#gqw 配置管理,配置存储功能
ALTER TABLE `monitor`.`mw_ncm_configmanage_path`
ADD COLUMN `valid_type` int(4) NULL COMMENT '生效类别(1:天数 2:数量)' AFTER `max_time`;

update `mw_ncm_configmanage_path` set  valid_type = 1 ;

#gqw 自动化需求,文件分发,作业下发
ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `ignore_error` tinyint(4) NULL COMMENT '忽略错误（true:忽略）' AFTER `delete_flag`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `trans_file_param` text NULL COMMENT '文本下发内容' AFTER `ignore_error`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `is_homework` tinyint(4) NULL COMMENT '是否是作业下发' AFTER `trans_file_param`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `homework_sort` int(11) NULL COMMENT '作业下发排序' AFTER `is_homework`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `homework_rel_id` int(11) NULL COMMENT '作业关联ID' AFTER `homework_sort`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `homework_version_id` int(11) NULL COMMENT '作业关联ID' AFTER `homework_sort`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `script_type` varchar(255) NULL COMMENT '脚本类别' AFTER `homework_version_id`;

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `default_account_id` int(11) NULL COMMENT '默认账号' AFTER `script_type`;

UPDATE mw_script_exe_history_log set is_homework = false;

#gqw 增加权限
delete from  `mw_role_module_perm_mapper` where module_id = 246;
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-browse', 0, 246, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-cn_mw_sign', 0, 246, 'cn_mw_sign', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-create', 0, 246, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-delete', 0, 246, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-editor', 0, 246, 'editor', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-perform', 0, 246, 'perform', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-246-secopassword', 0, 246, 'secopassword', 1);

#2022/5/26 lbq 财政厅通知方式
CREATE TABLE `mw_alert_caizhengju_sms_rule`  (
  `rule_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `app_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `app_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sign` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(32) NULL DEFAULT NULL COMMENT '1:翼讯通发送；2：内网',
  `account` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(32) NULL DEFAULT NULL,
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

# 增加作业管理数据
CREATE TABLE `mw_homework_manage_table` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `homework_name` varchar(255) NOT NULL COMMENT '作业名称',
  `homework_tree_id` int(11) DEFAULT NULL COMMENT '作业所在树ID',
  `homework_desc` varchar(1024) DEFAULT NULL COMMENT '作业描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '更新人',
  `delete_flag` tinyint(4) DEFAULT NULL COMMENT '是否删除',
  `version_id` int(11) DEFAULT NULL COMMENT '版本ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='猫维——脚本管理表';

CREATE TABLE `mw_homework_relation_table` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `homework_id` int(11) NOT NULL COMMENT '作业ID',
  `exec_id` int(11) NOT NULL COMMENT '执行ID',
  `homework_version_id` int(11) NOT NULL COMMENT '作业版本ID，当作业编辑后，会有多个版本号',
  `homework_sort` int(11) NOT NULL COMMENT '执行排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='作业ID与执行ID的关联关系表';

ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `end_time` datetime NULL COMMENT '结束时间' AFTER `default_account_id`;

#2022/5/28 ljb
alter table monitor.mw_cmdbmd_assets_interface modify column id int auto_increment comment 'id';
alter table monitor.mw_cmdbmd_assets_interface modify column assets_id varchar(255) null COMMENT '关联设备Id';
alter table monitor.mw_cmdbmd_assets_interface modify column type varchar(32) null comment '接口类型';
alter table monitor.mw_cmdbmd_assets_interface modify column state varchar(24) null comment '接口状态';
alter table monitor.mw_cmdbmd_assets_interface change column vlan_mode if_mode varchar(12) null comment '接口模式(TRUNK、ACCESS、HYBIRD)';
alter table monitor.mw_cmdbmd_assets_interface modify column vlan varchar(32) null comment 'access-->vlan tag(数字)；trunk --> trunk';
alter table monitor.mw_cmdbmd_assets_interface add column port_type varchar(12) null comment '端口类型(暂定为 0：电口；1：光口)';
alter table monitor.mw_cmdbmd_assets_interface add column vlan_flag boolean null comment 'vlan标志' after vlan;

#2022/5/30 lbq sql查询优化
ALTER TABLE `monitor`.`mw_alert_record_user_mapper`
ADD INDEX `id`(`id`) USING BTREE;


#gengjb 2022/05/31 可视化功能建表语句
CREATE TABLE `mw_visualized_chart_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '分区类型ID',
  `parent_id` int(10) DEFAULT NULL COMMENT '父ID',
  `partition_name` varchar(128) DEFAULT NULL COMMENT '分区名称',
  `icon_url` varchar(128) DEFAULT NULL COMMENT '图标URL',
  `drag_url` varchar(256) DEFAULT NULL COMMENT '统计单位',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识符',
  `sign` varchar(128) DEFAULT NULL COMMENT '前端标识-前端需要标识判断组件',
  `partition_eng` varchar(255) DEFAULT NULL COMMENT '分区英文名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8 COMMENT='可视化图形表';

CREATE TABLE `mw_visualized_classify` (
  `classify_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '可视化分类ID',
  `parent_id` int(10) DEFAULT NULL COMMENT '父目录ID',
  `classify_name` varchar(128) DEFAULT NULL COMMENT '可视化分类名称',
  PRIMARY KEY (`classify_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='可视化分类';

CREATE TABLE `mw_visualized_datasource` (
  `datasource_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '数据源ID',
  `datasource_name` varchar(128) DEFAULT NULL COMMENT '数据源名称',
  PRIMARY KEY (`datasource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='可视化数据源表';

CREATE TABLE `mw_visualized_index` (
  `index_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `data_source_id` int(10) DEFAULT NULL COMMENT '数据源ID',
  `index_name` varchar(128) DEFAULT NULL COMMENT '指标名称',
  `index_monitor_item` varchar(256) DEFAULT NULL COMMENT '监控项名称',
  `number_type` varchar(256) DEFAULT NULL COMMENT '数值类型',
  PRIMARY KEY (`index_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2063 DEFAULT CHARSET=utf8 COMMENT='可视化指标信息表';

CREATE TABLE `mw_visualized_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '可视化数据主键',
  `classify_id` int(10) DEFAULT NULL COMMENT '分类ID',
  `visualized_view_name` varchar(128) DEFAULT NULL COMMENT '视图名称',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识符',
  `visualized_data` longtext DEFAULT NULL COMMENT '前端传入数据',
  `visualized_image` longtext DEFAULT NULL COMMENT '可视化图片信息',
  `type` int(10) DEFAULT NULL COMMENT '可视化时间区域类型',
  `date_type` int(10) DEFAULT NULL COMMENT '可视化时间类型',
  `start_time` varchar(255) DEFAULT NULL COMMENT '可视化查询开始时间',
  `end_time` varchar(255) DEFAULT NULL COMMENT '可视化查询结束时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='可视化数据表';

INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-241-browse', 0, 241, 'browse', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-241-create', 0, 241, 'create', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-241-delete', 0, 241, 'delete', 1);
INSERT INTO `monitor`.`mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`) VALUES ('0-241-editor', 0, 241, 'editor', 1);


#gengjb 2022/05/31 资产列表增加用户组筛选
INSERT INTO `monitor`.`mw_select_url_base`(`drop_number_value`, `drop_char_value`, `drop_label`, `drop_type`, `drop_value_type`, `deep`, `pid`, `nodes`, `is_node`) VALUES (8, '用户组', '用户组', 'assetsType', 0, 1, 0, ',63,', 0);
INSERT INTO `monitor`.`mw_select_url_base`(`drop_number_value`, `drop_char_value`, `drop_label`, `drop_type`, `drop_value_type`, `deep`, `pid`, `nodes`, `is_node`) VALUES (8, '用户组', '用户组', 'inAssetsType', 0, 1, 0, ',64,', 0);

#gengjb 2022/06/01 首页选择条件不重置增加字段
ALTER TABLE `monitor`.`mw_newindex_bulk`
ADD COLUMN `count` int(10) NULL  DEFAULT 5 COMMENT '模块显示数据条数',
ADD COLUMN `date_type` int(10) NULL  DEFAULT 1 COMMENT '模块时间类型',
ADD COLUMN `start_time` varchar(255) NULL COMMENT '模块数据开始时间',
ADD COLUMN `end_time` varchar(255) NULL COMMENT '模块数据结束时间';
update mw_newindex_bulk set count = 5,date_type = 1;

#gengjb 2022/06/09 机构新增中国各区域信息表
CREATE TABLE `mw_area` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `parent_id` int(11) unsigned NOT NULL COMMENT '上级区域ID',
  `level` tinyint(1) NOT NULL COMMENT '行政区域等级 1-省 2-市 3-区县 4-街道镇',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `whole_name` varchar(300) DEFAULT '' COMMENT '完整名称',
  `lon` varchar(20) DEFAULT '' COMMENT '本区域经度',
  `lat` varchar(20) DEFAULT '' COMMENT '本区域维度',
  `city_code` varchar(10) DEFAULT '' COMMENT '电话区号',
  `zip_code` char(6) DEFAULT '' COMMENT '邮政编码',
  `area_code` varchar(10) DEFAULT '' COMMENT '行政区划代码',
  `pin_yin` varchar(400) DEFAULT '' COMMENT '名称全拼',
  `simple_py` varchar(200) DEFAULT '' COMMENT '首字母简拼',
  `per_pin_yin` char(1) DEFAULT '' COMMENT '区域名称拼音的第一个字母',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent` (`parent_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=46303 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


