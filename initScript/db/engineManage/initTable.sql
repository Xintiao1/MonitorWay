##创建引擎主表
CREATE TABLE `mw_enginemanage_table` (
  `id` varchar(128) NOT NULL DEFAULT '',
  `engine_name` varchar(128) NOT NULL COMMENT '引擎名称',
  `server_ip` varchar(128) DEFAULT NULL COMMENT '服务器IP',
  `mode` varchar(11) NOT NULL COMMENT '模式5.主动式6.被动式',
  `description` varchar(128) DEFAULT NULL COMMENT '描述',
  `encryption` varchar(11) DEFAULT NULL COMMENT '加密1.非加密2.共享秘钥PSK4.证书',
  `key_consistency` varchar(128) DEFAULT NULL COMMENT '共享秘钥一致性',
  `shared_key` varchar(128) DEFAULT NULL COMMENT '共享秘钥',
  `publisher` varchar(128) DEFAULT NULL COMMENT '发行者',
  `title` varchar(128) DEFAULT NULL COMMENT '主题',
  `compress` varchar(128) DEFAULT NULL COMMENT '压缩',
  `Monitor_host_number` int(11) DEFAULT 0 COMMENT '监控主机数量',
  `monitoring_items_number` int(11) DEFAULT 0 COMMENT '监控项数量',
  `performance` varchar(128) DEFAULT NULL COMMENT '性能',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `port` varchar(255) DEFAULT NULL COMMENT '端口号',
  `proxy_id` varchar(10) DEFAULT NULL COMMENT '代理id',
  `delete_flag` tinyint(1) NOT NULL COMMENT '删除标识符',
  `monitor_server_id` int(11) NOT NULL COMMENT '监控服务器id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

##新增表头字段
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'engineName', '引擎名称', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'serverIp', '服务器IP', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'monitorServerName', '监控服务器', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'modeName', '模式', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'description', '描述', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'encryptionName', '加密方式', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'keyConsistency', '共享秘钥一致性', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'sharedKey', '共享秘钥', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'publisher', '发行者', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'title', '主题', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'compress', '压缩', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'monitorHostNumber', '监控主机数量', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'monitoringItemsNumber', '监控项数量', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'performance', '性能', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'department', '机构', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'group', '用户组', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'principal', '负责人', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'creator', '创建人', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'creationDate', '创建时间', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'modifier', '修改人', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (39, 'modificationDate', '修改时间', 0);

##新增查询下拉字段
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'engineName', '引擎名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'serverIp', '服务器IP', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'monitorServerName', '监控服务器', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'mode', '引擎模式', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'encryption', '加密方式',  3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'keyConsistency', '共享秘钥一致性',  1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'sharedKey', '共享秘钥',  1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'publisher', '发行者',  1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'title', '主题',  1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'compress', '压缩',  3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'publisher', '发行者',  1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'department', '机构', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'group', '用户组', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'principal', '负责人', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'creator', '创建人', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'creationDate', '创建时间', 2, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'modifier', '修改人', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (39, 'modificationDate', '修改时间', 2, NULL, NULL);
