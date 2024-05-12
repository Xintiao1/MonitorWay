CREATE TABLE `mw_label_module_base` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `module_type` varchar(128) NOT NULL COMMENT '标签模块类型',
	PRIMARY KEY (`id`) USING BTREE
);


CREATE TABLE `mw_label_mapper` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `label_id` int(11) NOT NULL COMMENT '标签id',
  `module_id` varchar(128) NOT NULL COMMENT '模块id',
  `tagboard` varchar(128) DEFAULT NULL COMMENT '标签值',
  PRIMARY KEY (`id`) USING BTREE
);

CREATE TABLE `mw_label_drop_mapper` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `label_id` int(11) NOT NULL COMMENT '标签id',
  `module_id` varchar(128) NOT NULL COMMENT '模块id',
  `drop_tagboard` int(11) DEFAULT   NULL COMMENT '下拉标签值',
  PRIMARY KEY (`id`) USING BTREE
);

CREATE TABLE `mw_label_date_mapper` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `label_id` int(11) NOT NULL COMMENT '标签id',
  `module_id` varchar(128) NOT NULL COMMENT '模块id',
  `date_tagboard` datetime DEFAULT NULL COMMENT '时间标签值',
  PRIMARY KEY (`id`) USING BTREE
);

CREATE TABLE `mw_label_module_mapper` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `module_id` int(11) NOT NULL COMMENT '模块类型ID',
  `label_id` int(11) NOT NULL COMMENT '标签ID',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
);

INSERT INTO `monitor`.`mw_label_module_base`(`id`, `module_type`) VALUES (1, '资产');
INSERT INTO `monitor`.`mw_label_module_base`(`id`, `module_type`) VALUES (2, '线路');
INSERT INTO `monitor`.`mw_label_module_base`(`id`, `module_type`) VALUES (3, '报表');

##syt
alter table mw_enginemanage_table add monitor_server_id int(11)  not null comment '监控服务器id';
alter table mw_enginemanage_table drop column e;
## mw_item_name_mapper 基础表 手动维护 用户id应该于测试环境相同
update mw_item_name_mapper set descr= 'CPU利用率' where id = 54;
update mw_item_name_mapper set descr= 'CPU利用率(平均5分钟)' where id = 55;
update mw_item_name_mapper set descr= 'CPU利用率(平均1分钟)' where id = 56;
update mw_item_name_mapper set descr= 'CPU核数' where id = 57;
## nocheck中的消息通知url 写成/mwapi/websocket/myWaitingToDo/count/browse报错 改成下面的
update mw_notcheck_url set url= '/mwapi/ws/myWaitingToDo/count/browse' where url = '/mwapi/websocket/myWaitingToDo/count/browse';
## 修改有关引擎的表头字段
DELETE from mw_pagefield_table where page_id = 39;
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
##修改有关引擎的下拉框字段
DELETE from mw_pageselect_table where page_Id = 39;
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
##删除引擎所有脏数据重新测
truncate table mw_enginemanage_table;
##将引擎中监控主机数量字段和监控监控项数量设置默认值
alter table mw_enginemanage_table alter column Monitor_host_number set default 0;
alter table mw_enginemanage_table alter column monitoring_items_number set default 0;

