alter table mw_ncm_timetask_config_mapper add column  `name` varchar(255) default  NULL COMMENT '显示名称';
alter table mw_ncm_timetask_table add column  `timing` varchar(255) default  NULL COMMENT '定时间隔';
alter table mw_ncm_timetask_table add column  `select_url` varchar(255) default  NULL COMMENT '查看关联配置';
alter table mw_ncm_timetask_table add column  `select_id` varchar(255) default  NULL COMMENT '查看关联配置参数名称';
alter table mw_ncm_timetask_table add column  `month` varchar(255) default  NULL COMMENT '月';
alter table mw_ncm_timetask_table add column  `week` varchar(255) default  NULL COMMENT '周';
alter table mw_ncm_timetask_table add column  `day` varchar(255) default  NULL COMMENT '日';
alter table mw_ncm_timetask_table add column  `hms` varchar(255) default  NULL COMMENT '时分秒';
alter table mw_ncm_timetask_table add column  `time_custom` int(11) default  NULL COMMENT '是否自定义cron设置';




