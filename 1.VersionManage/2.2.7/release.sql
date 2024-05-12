CREATE TABLE `mw_interfacefilter_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `tangible_assets_id` varchar(35) NOT NULL COMMENT '资产主键',
  `name_type` varchar(255) NOT NULL COMMENT '类型',
  `show_data` longtext NOT NULL COMMENT '存储展示信息（json格式）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
