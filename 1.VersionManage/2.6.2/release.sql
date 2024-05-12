#ljb 20220614
alter table monitor.mw_topo_graph add if_visible BOOLEAN default true null comment '接口名称是否可见';
# gqw 增加历史执行记录字段
ALTER TABLE `monitor`.`mw_script_exe_history_log`
ADD COLUMN `sql_database` varchar(255) NULL COMMENT '数据库名称（当脚本类别为SQL生效）' AFTER `end_time`,
ADD COLUMN `sql_text` varchar(255) NULL COMMENT 'mysql前置标识（当脚本类别为SQL生效）' AFTER `sql_database`,
ADD COLUMN `sql_order_address` varchar(255) NULL COMMENT '目的地址（当脚本类别为SQL生效）' AFTER `sql_text`;

#gengjb 新增资产发现ICMP规则表
CREATE TABLE `mw_icmprule_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `port` int(11) NOT NULL DEFAULT 10050 COMMENT '端口号',
  `rule_id` int(11) NOT NULL COMMENT '规则ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=811 DEFAULT CHARSET=utf8 COMMENT='资产扫描ICMP类型规则表';
# qzg 接口信息表新增接口管理字段
alter table monitor.mw_cmdbmd_assets_interface add column `manage_state` tinyint(1) DEFAULT NULL COMMENT '接口管理状态(true开启，false关闭)';


#lbq 22/6/27  邮件添加logo启用按钮
ALTER TABLE `monitor`.`mw_alert_email_rule`
ADD COLUMN `is_logo` tinyint(1) NULL COMMENT '是否启用logo' AFTER `url`;

#gengjb 2022/06/28 监控大屏新增四川地图区域划分组件
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES (35, '四川地图区域划分', '四川地图区域划分', NULL, NULL, NULL, '地图', 1, NULL);

#gengjb 2022/07/01 监控大屏新增四川地图边框阴影组件
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES (36, '四川地图边框阴影', '四川地图边框阴影', NULL, NULL, NULL, '地图', 1, NULL);

#2022/07/12 qzg
alter table mw_cmdbmd_properties add `is_manage` tinyint(1) DEFAULT NULL COMMENT '是否纳管字段（资产纳管时是否显示该字段）';

#gengjb 2022/07/14 资产自定义字段表数据修改
ALTER TABLE `monitor`.`mw_assets_newfield` ADD COLUMN `type` int(10) NULL COMMENT '字段类型';
update mw_assets_newfield where type = 0;

#gengjb 2022/07/20 首页新增流量从错误包数据统计组件
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`, `init_type`) VALUES (14, '流量错误包统计', 0, 'new/screen/getFlowErrorCount', 0, 1);

#qzg 2022/07/28 新增接口信息过滤表
CREATE TABLE `mw_cmdbmd_assers_filter` (
  `id` int(11) NOT NULL,
  `filter_field` varchar(2048) DEFAULT NULL COMMENT '资产接口过滤名称',
  `no_start_with` varchar(2048) DEFAULT NULL COMMENT '过滤以该字段开头的接口名称',
  `cable_start_with` varchar(2048) DEFAULT NULL COMMENT '以该字段开头的为光口接口',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
truncate table mw_cmdbmd_assers_filter;
INSERT INTO `monitor`.`mw_cmdbmd_assers_filter`(`id`, `filter_field`, `no_start_with`, `cable_start_with`) VALUES (1, 'null,loopback,console,vlan,combo,bridge,Eth-Trunk,Tunnel,blackhole,bond,vpntun,StruckPort,StruckSub,StackSub,StackPort,vqos,dhcp,drop,aux,cellular,virtual,voice,efxs,huij,vswitch,jvhe,', 'lo,nu,po,vl,gre,br,sit,tif,bri,imq,aux,se,vo,li,', 'ten-g,xg,te,');


#gengjb 2022/08/04 监控大屏增加流量占比流量带宽信息
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES (37, '流量占比排行榜', '1、资产名称2、IP地址3、当前流量占比（高到低排行）', NULL, NULL, NULL, '流量信息', 1, 'cn.mw.monitor.screen.service.modelImpl.ScreenFlowNewsModel');
INSERT INTO `monitor`.`mw_model_base`(`id`, `model_name`, `model_desc`, `model_content`, `assets_type_id`, `item_name`, `model_type`, `is_show`, `class`) VALUES (38, '流量带宽排行榜', '1、资产名称2、IP地址3、当前流量带宽（高到低排行）', NULL, NULL, NULL, '流量信息', 1, 'cn.mw.monitor.screen.service.modelImpl.ScreenFlowNewsModel');

#ljb 20220805 增加流量配置
create table mw_netflow_config
(
    id    int auto_increment primary key,
    name  varchar(255) null,
    value varchar(255) null
);

INSERT INTO monitor.mw_netflow_config (name, value) VALUES ('save_days', '31');

#gengjb 2022/08/09 首页增加流量带宽组件
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`, `init_type`) VALUES (15, '接口流量带宽', 0, 'new/screen/getFlowBandWidth', 0, 1);

#ljb 2022/08/19 增加未知资产子类型
INSERT INTO monitor.mw_assetssubtype_table (id, type_name, pid, type_desc, enable, nodes, creator, create_date, modifier, modification_Date, classify, groupid, type_icon) VALUES (267, '未知', 0, '未知', '1', '0，267', 'ljb', '2022-08-19 12:23:28', 'ljb', '2022-08-19 12:23:37', 1, null, 'f206f21eb9da43b2aae2bb04d17422b2.gif');

#ljb 2022/10/10 增加拓扑绑定neo4j数据库标识
alter table monitor.mw_topo_graph add bind_graph_db boolean default 0 null COMMENT '是否绑定图形数据库';

#ljb 2022/10/10 增加是否显示孤立节点
alter table monitor.mw_topo_graph add show_isolated_node boolean default false null comment '是否显示孤立节点';

#ljb 2022/10/23 增加拓扑连线告警
create table monitor.mw_topo_line_setting
(
    id               varchar(32)     not null primary key,
    line_setting     text            null comment '配置信息',
    refresh_interval int default 120 null comment '更新间隔,秒'
);

#ljb 2022/11/07
create table monitor.mw_topo_subline_setting
(
    id               int auto_increment primary key,
    line_setting     longtext    null comment '配置信息',
    line_id          varchar(32) null comment 'topo_line_setting关联id',
    tangible_id      varchar(32) null comment '资产id',
    up_graph_index   int         null comment '上联拓扑元素id',
    down_graph_index int         null comment '下联拓扑元素id',
    param            longtext    null comment '规则参数'
);

alter table monitor.mw_tangibleassets_table add vxlan_user varchar(125) null comment 'vxlan用户';

alter table monitor.mw_tangibleassets_table add vxlan_passwd char(64) null comment 'vxlan密码';

