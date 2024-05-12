drop table mw_report_user_mapper;
drop table mw_report_org_mapper;
drop table mw_group_assets_mapper;
drop table mw_group_webmonitor_mapper;
drop table mw_label_link_mapper;
drop table mw_inbandip_table;
drop table mw_role_module_mapper;
drop table mw_sequence;
alter table mw_network_link add column  `band_unit` varchar(32) default 'bps' NOT NULL COMMENT '带宽单位';
alter table mw_thirdparty_server_table  modify column monitoring_server_user varchar(30);
alter table mw_assetstemplate_table drop column group_id;
delete from mw_pagefield_table where prop='operation' and page_id=12;
alter table mw_report_type_table add column  `icon` varchar(128) default ''  COMMENT '图标地址';
update mw_report_type_table  set icon='@/assets/image/png/history.png' where id=1;
update mw_report_type_table  set icon='@/assets/image/png/qushi.png' where id=2;
update mw_report_type_table  set icon='@/assets/image/png/paihang.png' where id=3;
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (132, '/mwapi/report/getReportCount/browse')
alter table mw_bulk_data modify column delete_flag tinyint(1) default 0;
CREATE TABLE mw_mac_oui (
  mac varchar(255) DEFAULT NULL COMMENT 'mac地址前六位',
  vendor varchar(255) DEFAULT NULL COMMENT '品牌/厂商',
  address varchar(255) DEFAULT NULL COMMENT '厂商地址相关信息',
  short_name varchar(255) DEFAULT NULL COMMENT '简称'
);

CREATE TABLE mw_ipaddressmanagelist_his_table (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  link_id int(11) NOT NULL COMMENT 'ip地址管理清单表主键',
  ip_address varchar(128) DEFAULT NULL COMMENT 'ip地址',
  mac varchar(255) DEFAULT NULL COMMENT 'MAC地址',
  vendor varchar(255) DEFAULT NULL COMMENT '厂商',
  access_equip varchar(255) DEFAULT NULL COMMENT '接入设备',
  access_port varchar(255) DEFAULT NULL COMMENT '接入端口',
  update_date datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id) USING BTREE
);
##syt_2_0_9
##没起作用
-- INSERT INTO `mw_module` VALUES (136, 104, 'outbandAssets_popup', '带外资产修改弹窗', '/mwapi/assets/outband/popup', 1, 3, '31,104,136', 1, 0);
insert into mw_notcheck_url ( id,url ) values (127,'/mwapi/assets/outband/popup/browse');
##带外资产查看标签
insert into mw_notcheck_url ( id,url ) values (128,'/mwapi/assets/outband/getLabels');
##无形资产查看标签
insert into mw_notcheck_url ( id,url ) values (129,'/mwapi/assets/intangible/getLabels');
##数据库详情页查看INNODB数据接口
insert into mw_notcheck_url ( id,url ) values (131,'/mwapi/server/getRelevantInfoByItemName/browse');
##给引擎表添加一个字段记录创建zabbix中proxy的名称
alter table mw_enginemanage_table add proxy_name varchar(128) not null comment '代理名称';
