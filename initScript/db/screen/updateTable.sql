##用户首页布局信息表
drop table mw_index_bulk;
CREATE TABLE `mw_index_bulk` (
  `model_data_id` varchar(50) DEFAULT NULL COMMENT '模块唯一id',
  `bulk_id` int(11) DEFAULT NULL COMMENT '模块类型id',
  `bulk_name` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `delete_flag` tinyint(1) unsigned zerofill DEFAULT 0 COMMENT '删除标识：1-删除，0-未删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
##首页初始化模块表
drop table mw_index_init_bulk;
CREATE TABLE `mw_index_init_bulk` (
  `bulk_id` int(11) DEFAULT NULL COMMENT '首页模块类型',
  `bulk_name` varchar(50) DEFAULT NULL COMMENT '首页模块名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
##首页初始化模块数据
truncate table mw_index_init_bulk;
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (1,"告警概括");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (2,"CPU");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (3,"节点丢包率");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (4,"活动告警");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (5,"内存");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (6,"节点延时");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (7,"消息统计");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (8,"磁盘");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (9,"接口流量");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (10,"日志量统计");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (11,"资产统计");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (12,"当日数据时间分布");
INSERT INTO `mw_index_init_bulk` (bulk_id,bulk_name) VALUES (13,"系统");