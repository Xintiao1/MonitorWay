INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (119, '/mwapi/labelManage/getDropLabelList/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (120, '/mwapi/labelManage/moduleType/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (123, '/mwapi/getAssetsListByAssetsTypeId');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (125, '/mwapi/assets/template/getList');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (124, '/mwapi/getDropLabelListByAssetsTypeList');

alter table mw_network_link add column  `root_server_id` int(11) default 1 NOT NULL COMMENT '源设备zabbix服务器id';
alter table mw_network_link add column  `target_server_id` int(11) default 1 NOT NULL COMMENT '目标zabbix服务器id';

DROP TABLE t_ip_address;
DROP TABLE t_ip_segment;
DROP TABLE t_sys_dept;
DROP TABLE t_sys_log;
DROP TABLE t_sys_menu;
DROP TABLE t_sys_org;
DROP TABLE t_sys_role;
DROP TABLE t_sys_role_menu;
DROP TABLE t_sys_setting;
DROP TABLE t_sys_user;
DROP TABLE t_sys_user_role;
DROP TABLE t_top;
DROP TABLE t_type_item;
DROP TABLE t_user_item_graph;
DROP TABLE t_zbx_action;
DROP TABLE t_zbx_action2;
DROP TABLE t_zbx_activ;
DROP TABLE t_zbx_asset;
DROP TABLE t_zbx_assets_oid;
DROP TABLE t_zbx_assets_other;
DROP TABLE t_zbx_assets_scan;
DROP TABLE t_zbx_asstes_alarm;
DROP TABLE t_zbx_cloud;
DROP TABLE t_zbx_conditions_alert;
DROP TABLE t_zbx_conditions_object;
DROP TABLE t_zbx_config;
DROP TABLE t_zbx_config_job;
DROP TABLE t_zbx_config_manage;
DROP TABLE t_zbx_config_set;
DROP TABLE t_zbx_config_template;
DROP TABLE t_zbx_config_template_command;
DROP TABLE t_zbx_config_user;
DROP TABLE t_zbx_group;
DROP TABLE t_zbx_host;
DROP TABLE t_zbx_img_alert;
DROP TABLE t_zbx_img_assist;
DROP TABLE t_zbx_knowledge;
DROP TABLE t_zbx_license;
DROP TABLE t_zbx_log;
DROP TABLE t_zbx_macro;
DROP TABLE t_zbx_maps;
DROP TABLE t_zbx_module;
DROP TABLE t_zbx_optlog;
DROP TABLE t_zbx_screen;
DROP TABLE t_zbx_screen_layout;
DROP TABLE t_zbx_screen_temp_com;
DROP TABLE t_zbx_template;
DROP TABLE t_zbx_topo;
DROP TABLE t_zbx_topo_element;
DROP TABLE t_zbx_trigger_expression;
DROP TABLE t_zbx_trigger_item_condition;
DROP TABLE t_zbx_trigger_itemname;
DROP TABLE t_zbx_type;
DROP TABLE t_zbx_graph;
alter table mw_ipaddressmanage_table add is_include int null comment '是否为可用地址';
alter table mw_assetstemplate_table drop column template_id;

CREATE TABLE mw_assetsgroup_server_mapper (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  group_id varchar(128) DEFAULT NULL COMMENT 'Zabbix群租对应groupId',
  monitor_server_id varchar(128) DEFAULT NULL COMMENT '监控服务器id',
  assets_subtype_id varchar(128) DEFAULT NULL COMMENT '类型管理表对应主键',
  PRIMARY KEY (id) USING BTREE
);

##syt_2_0_8
##带外资产的表结构修改
alter table mw_outbandassets_table add monitor_server_id int null comment '监控服务器id';
alter table mw_outbandassets_table drop column principal_flag;
alter table mw_outbandassets_table drop column group_flag;
alter table mw_outbandassets_table modify polling_engine varchar(128);
alter table mw_outbandassets_table modify enable varchar(20);
##将带外资产的下拉框数据放在nocheck
insert into mw_notcheck_url values (121, "/mwapi/assets/outband/dropdown/browse")
##添加一个资产类型为带外资产
INSERT INTO `mw_assetssubtype_table` (type_name,pid,type_desc,enable,nodes,creator,create_date,modifier,modification_Date,classify)
VALUES ('带外资产', 0, '用于创建带外资产模板', '1', ',0,69,', 'syt', '2020-12-1 11:37:30', 'syt', '2020-12-1 11:37:30',1);
##清activiti
DELETE FROM ACT_HI_ATTACHMENT;
DELETE FROM ACT_HI_COMMENT;
DELETE FROM ACT_HI_DETAIL;
DELETE FROM ACT_HI_ACTINST;
DELETE FROM ACT_HI_IDENTITYLINK;

DELETE FROM ACT_HI_PROCINST;
DELETE FROM ACT_HI_TASKINST;
DELETE FROM ACT_HI_VARINST;
DELETE FROM ACT_RU_IDENTITYLINK;

DELETE FROM ACT_RU_VARIABLE;
DELETE FROM ACT_RU_EXECUTION;

DELETE FROM ACT_RU_TASK;
DELETE FROM ACT_GE_BYTEARRAY;
DELETE FROM ACT_RE_PROCDEF;
DELETE FROM ACT_RE_DEPLOYMENT;

DELETE FROM ACT_RE_PROCDEF;
##删除无形资产表头无用字段 操作
DELETE FROM mw_pagefield_table WHERE page_id = 82 AND prop = 'operation' AND label = '操作';
##修改资产模板表头描述改成特征信息
UPDATE mw_pagefield_table SET label= '特征信息' WHERE page_id = 37 and label= '描述';
##修改资产模板查询的 文本改成下拉框
UPDATE mw_pageselect_table SET input_format= 3 WHERE page_id = 37 and label= '子类型';
UPDATE mw_pageselect_table SET input_format= 3 WHERE page_id = 37 and label= '资产类型';
UPDATE mw_pageselect_table SET input_format= 3 WHERE page_id = 37 and label= '规格型号';
UPDATE mw_pageselect_table SET input_format= 3 WHERE page_id = 37 and label= '厂商';
