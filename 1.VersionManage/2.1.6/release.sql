insert into `monitor`.`mw_ncm_timetask_type_mapper` (typename,typemethod,typeclass,configid)
VALUES
('IP地址管理间隔扫描','cronBatchScanIp','cn.mw.monitor.ipaddressmanage.service.impl.MwIpAddressManageServiceScanImpl','timing');
alter table mw_ncm_vendor_table add description varchar(255) DEFAULT NULL COMMENT '描述';
alter table mw_ncm_vendor_table add vendor_id int(11) NOT NULL COMMENT '厂商/品牌id';

alter table mw_ncm_vendor_table add `creator` varchar(255) NOT NULL COMMENT '创建人';
alter table mw_ncm_vendor_table add  `create_date` datetime NOT NULL COMMENT '创建时间';
alter table mw_ncm_vendor_table add  `modifier` varchar(255) NOT NULL COMMENT '修改人';
alter table mw_ncm_vendor_table add  `modification_Date` datetime NOT NULL COMMENT '修改时间';

CREATE TABLE `mw_base_vendor_icon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vendor` varchar(255) DEFAULT NULL COMMENT '品牌/厂商',
  `vendor_small_icon` varchar(255) DEFAULT NULL COMMENT '小图标',
  `vendor_large_icon` varchar(255) DEFAULT NULL COMMENT '大图标',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mw_select_url_base` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列id',
  `drop_number_value` int(11) DEFAULT NULL COMMENT '下拉实际传递的值',
  `drop_char_value` varchar(50) DEFAULT NULL COMMENT '下拉实际传递的值',
  `drop_label` varchar(50) DEFAULT NULL COMMENT '下拉显示的值',
  `drop_type` varchar(10) NOT NULL COMMENT '下拉的类型',
  `drop_value_type` int(11) NOT NULL COMMENT '下拉的传值类型0是数字类型 1是字符串类型',
  `deep` int(11) DEFAULT 1 COMMENT '深度',
  `pid` int(11) NOT NULL COMMENT '上级id',
  `nodes` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '节点ID',
  `is_node` tinyint(1) NOT NULL COMMENT 'true 表示叶子结点',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update mw_pageselect_table set input_format = 4 where id = 16;

update mw_pageselect_table set input_format = 4 where id = 24;

update mw_pageselect_table set input_format = 4 where id = 31;

insert into mw_notcheck_url(id,url) VALUES(170,'/mwapi/module/create');

insert into mw_notcheck_url(id,url) VALUES(171,'/mwapi/module/delete');

insert into mw_notcheck_url(id,url) VALUES(172,'/mwapi/module/editor');
