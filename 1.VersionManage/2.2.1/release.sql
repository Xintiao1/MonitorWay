CREATE TABLE `mw_pubipaddress_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `link_id` int(11) NOT NULL COMMENT '对应ip地址段',
  `ip_address` varchar(128) NOT NULL COMMENT 'ip地址',
  `ip_type` varchar(12) NOT NULL COMMENT 'ip类型',
  `country` varchar(128) DEFAULT NULL COMMENT '国家',
  `state` varchar(128) DEFAULT NULL COMMENT '省',
  `city` varchar(128) DEFAULT NULL COMMENT '市',
  `region` varchar(128) DEFAULT NULL COMMENT '县',
	`isp` varchar(128) DEFAULT NULL COMMENT '供应商-联通/电信/移动/',
  `longitude` varchar(128) DEFAULT NULL COMMENT '经度',
  `latitude` varchar(128) DEFAULT NULL COMMENT '纬度',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

drop table if exists mw_cmdbmd_properties_mapper;