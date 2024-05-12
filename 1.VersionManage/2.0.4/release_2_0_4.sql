alter table mw_tangibleassets_table add monitor_server_id int null comment '监控服务器id';
alter table mw_scanresultsuccess_table add monitor_server_id int null comment '监控服务器id';
alter table mw_scanrule_table add monitor_server_id int null comment '监控服务器id';


CREATE TABLE mw_ncm_vendor_table (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  brand varchar(128) DEFAULT NULL COMMENT '厂商',
  specification varchar(128) DEFAULT NULL COMMENT '规格型号',
  PRIMARY KEY (id)
);
alter table mw_tangibleassets_table modify polling_engine varchar(128);

create table mw_alert_weixin_rule
(
    rule_id    varchar(32)  not null comment '规则id' primary key,
    agent_id   varchar(128) not null,
    app_secret varchar(128) not null,
    token      varchar(128) not null,
    openid     varchar(128) null comment '微信用户openid'
);

CREATE TABLE `mw_ncm_timetask_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `taskname` varchar(128) DEFAULT NULL COMMENT '任务名称',
  `timetype` varchar(255) DEFAULT NULL COMMENT '时间类型',
  `plan` varchar(255) DEFAULT NULL COMMENT '任务计划',
  `type` varchar(255) DEFAULT NULL COMMENT '任务类型',
  `last_result` varchar(255) DEFAULT NULL COMMENT '上一次执行结果',
  `after_time` datetime DEFAULT NULL COMMENT '下一次执行时间',
  `last_time` datetime DEFAULT NULL COMMENT '上一次执行时间',
  `class_name` varchar(255) DEFAULT NULL COMMENT '所在类全名',
  `method` varchar(255) DEFAULT NULL COMMENT '方法名',
  `cron` varchar(255) DEFAULT NULL COMMENT '时间设置',
  `status` int(11) DEFAULT NULL COMMENT '是否启动',
  `config_type` varchar(255) DEFAULT NULL COMMENT '下载配置类型',
  `cmds` varchar(255) DEFAULT NULL COMMENT '执行脚本内容',
  PRIMARY KEY (`id`) USING BTREE
);


insert into mw_notcheck_url( id,url )
values
(107,'/mwapi/solarReport/export'),
(111,'/mwapi/alert/getLuceneByTitle'),
(112,'/mwapi/alert/getItemByTriggerId'),
(113,'/mwapi/alert/getHistoryByItemId'),
(114,'/mwapi/link/enable');

update mw_module set url='/mwapi/indexmessage'  where id=115;
update mw_alert_action_type set action_type='企业微信' where id=5;
insert into mw_alert_action_type( `id`,`action_type` ) values(6,'其他');

##syt
alter table mw_thirdparty_server_table add main_server tinyint(1)  not null comment '主从标识，（只能有一个主）';
INSERT INTO mw_pagefield_table (page_id, prop, label, visible) VALUES (132, 'mainServer', '主/从', 1);
##其中 mw_module这张表需要全部导入 对应上pageId






