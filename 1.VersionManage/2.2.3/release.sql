CREATE TABLE `mw_topo_editor_link` (
  `topo_id` varchar(255) NOT NULL COMMENT '拓扑id',
  `element_type` varchar(11) DEFAULT 'link' COMMENT '元素类型',
  `node_aid` varchar(255) DEFAULT NULL COMMENT '节点起始id',
  `node_zid` varchar(255) DEFAULT NULL COMMENT '节点结束id',
  `text` varchar(255) DEFAULT NULL COMMENT '描述',
  `font_color` varchar(255) DEFAULT NULL COMMENT '字体颜色'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mw_topo_editor_node` (
  `topo_id` varchar(255) NOT NULL COMMENT '拓扑id',
  `element_type` varchar(11) DEFAULT 'node' COMMENT '元素类型',
  `x` varchar(255) DEFAULT NULL COMMENT '坐标-横',
  `y` varchar(255) DEFAULT NULL COMMENT '坐标-竖',
	`id` varchar(255) DEFAULT NULL COMMENT '节点id',
	`image` varchar(255) DEFAULT NULL COMMENT '图片信息',
  `text` varchar(255) DEFAULT NULL COMMENT '描述',
  `text_position` varchar(255) DEFAULT NULL COMMENT'文本位置信息',
	`larm` varchar(10) DEFAULT NULL COMMENT'告警',
	`level` varchar(10) DEFAULT NULL COMMENT'等级',
	`category` int(10) DEFAULT NULL COMMENT'是否根节点'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;