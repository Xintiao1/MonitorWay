alter table mw_ipaddressmanagelist_table add `assets_type` varchar(10) DEFAULT NULL COMMENT '资产类型';
UPDATE mw_assets_logo  set normal_logo =REPLACE(normal_logo,'/upload/assets-logo','/mwapi/basics/assets-logo');
UPDATE mw_assets_logo  set alert_logo =REPLACE(alert_logo,'/upload/assets-logo','/mwapi/basics/assets-logo');
UPDATE mw_assets_logo  set severity_logo =REPLACE(severity_logo,'/upload/assets-logo','/mwapi/basics/assets-logo');
UPDATE mw_assets_logo  set urgency_logo =REPLACE(urgency_logo,'/upload/assets-logo','/mwapi/basics/assets-logo');
UPDATE mw_topo_graph  set topo_graph =REPLACE(topo_graph,'/upload/assets-logo','/mwapi/basics/assets-logo');

UPDATE mw_pageselect_table set prop = 'loginState' where id = 9 and page_Id = 41;

CREATE TABLE `mw_alert_overdue_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `context` text DEFAULT NULL COMMENT '消息内容',
  `con_time` datetime DEFAULT NULL COMMENT '消息产生时间',
  `start_time` datetime DEFAULT NULL COMMENT '项目启动时间',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modification_date` datetime DEFAULT NULL COMMENT '修改时间',
  `modifier` varchar(128) DEFAULT NULL COMMENT '修改人 ',
  `is_send` bigint(11) DEFAULT NULL COMMENT '是否已发送',
  `delete_flag` bigint(11) DEFAULT NULL COMMENT '伪删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table mw_assetstemplate_table modify description varchar(1024);

CREATE TABLE `mw_ipv6managelist_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `link_id` int(11) NOT NULL COMMENT 'ip地址管理表主键',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'ip地址',
  `ip_type` int(11) DEFAULT NULL COMMENT 'ip类型',
  `ip_state` int(11) DEFAULT NULL COMMENT '使用状态',
  `remarks` varchar(128) DEFAULT NULL COMMENT '备注',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `interval` int(11) DEFAULT NULL COMMENT '同步数据时间间隔',
  `online` int(11) DEFAULT NULL COMMENT '在线状态：0：离线 1：在线',
  `mac` varchar(255) DEFAULT NULL COMMENT 'MAC地址',
  `vendor` varchar(255) DEFAULT NULL COMMENT '厂商',
  `access_equip` varchar(255) DEFAULT NULL COMMENT '接入设备',
  `access_port` varchar(255) DEFAULT NULL COMMENT '接入端口',
  `last_date` datetime DEFAULT NULL COMMENT '最后一次在线时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  `assets_id` varchar(255) DEFAULT NULL COMMENT '资产表主键',
  `assets_name` varchar(255) DEFAULT NULL COMMENT '资产表名称',
  `access_port_name` varchar(200) DEFAULT NULL COMMENT '接入端口名称',
  `assets_type` varchar(10) DEFAULT NULL COMMENT '资产类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=513 DEFAULT CHARSET=utf8;

alter table `mw_pagefield_table` add column  type varchar(32) comment '字段显示类型 img,txt,ope';

alter table mw_user_mapper modify column type_id varchar(32) NOT NULL comment '模块主键id';
alter table mw_user_mapper modify column user_id int(11) comment '用户id';
alter table mw_user_mapper modify column type varchar(128) comment '模块类型';

alter table mw_group_mapper modify column type_id varchar(32) NOT NULL comment '模块主键id';
alter table mw_group_mapper modify column group_id int(11) comment '用户组id';
alter table mw_group_mapper modify column type varchar(128) comment '模块类型';

alter table mw_org_mapper modify column type_id varchar(32) NOT NULL comment '模块主键id';
alter table mw_org_mapper modify column org_id int(11) comment '机构id';
alter table mw_org_mapper modify column type varchar(128) comment '模块类型';

alter table mw_org_mapper comment '机构模块映射表';
alter table mw_user_mapper comment '用户模块映射表';
alter table mw_group_mapper comment'用户组模块映射表';