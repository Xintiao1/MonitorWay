alter table mw_ipaddressmanagelist_table add column  `assets_id` varchar(128) default  NULL COMMENT '资产表主键';
alter table mw_ipaddressmanagelist_table add column  `assets_name` varchar(128) default  NULL COMMENT '资产表名称';
insert into mw_notcheck_url( id,url )
values
(134,'/mwapi/ipAddressManageList/batchScan'),
(135,'/mwapi/ipAddressManageList/getHisList');
alter table mw_ipaddressmanage_table add timing int(11)  default null comment '定时扫描间隔';
INSERT INTO `monitor`.`mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`) VALUES (100, 24, 'monitorServerName', '监控服务器名称', 1, 10);
INSERT INTO `monitor`.`mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (77, 24, 'monitorServerName', '监控服务器名称', 3, NULL, NULL);

CREATE TABLE mw_ncm_timetask_type_mapper (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  typename varchar(128) DEFAULT NULL COMMENT '类型名称(任务类型下拉框展示名称)',
  typemethod varchar(255) DEFAULT NULL COMMENT '执行方法全称',
  typeclass varchar(255) DEFAULT NULL COMMENT '执行方法所在类的全称',
  selecturl varchar(255) DEFAULT NULL COMMENT '关联配置下拉框查询方法url',
  configname varchar(255) DEFAULT NULL COMMENT '关联配置下拉框展示字段',
  configid varchar(255) DEFAULT NULL COMMENT '关联配置下拉框保存字段',
  PRIMARY KEY (id) USING BTREE
);

INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (138, '/mwapi/timetask/getTypeList');
##syt_2_1_0
##将知识表中存附件url的字符长度变成255
alter table mw_knowledgebase_table modify attachment_url varchar(255);
##将无形资产表中资产内容备注的字符长度变成255
alter table mw_intangibleassets_table modify assets_content varchar(255);
alter table mw_intangibleassets_table modify remarks varchar(1024);


