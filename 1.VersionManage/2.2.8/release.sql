drop table if exists mw_component_layout_table;
CREATE TABLE `mw_component_layout_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `assets_type_sub_id` int(11) NOT NULL COMMENT '资产子类型',
  `component_layout` longtext NOT NULL COMMENT '组件布局',
  `default_flag` tinyint(1) DEFAULT NULL COMMENT '是否为默认布局',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `monitor_server_id` int(11) NOT NULL COMMENT '监控服务器id',
  `template_id` varchar(128) NOT NULL COMMENT '第三方监控服务器中关联模板id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
