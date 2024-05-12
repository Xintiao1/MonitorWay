alter table monitor.mw_cmdbmd_relations_group add defaut_group_flag boolean default false null comment 'true: 系统创建的默认分组,表示为未分组, false: 由用户手动创建';
alter table monitor.mw_cmdbmd_instance add topo_info text null comment '实例拓扑信息';
alter table monitor.mw_scanresultsuccess_table add template_name varchar(256) null comment '模版名';
alter table monitor.mw_scanrule_table add engine_id varchar(256) null comment '引擎id';

#lijubo 20230310 增加
INSERT INTO `monitor`.`mw_topo_line_setting` (`id`, `line_setting`) VALUES ('global', '{"lineRuleList":[{"color":"255,255,255","type":"","value":0.0}],"refreshInterval":120}');

#lijubo 20230319 增加
alter table monitor.mw_cmdbmd_instance add notify_info varchar(125) null comment '实例通知信息';
#qzg 20130321 增加资产视图树结构分组
INSERT INTO `monitor`.`mw_select_url_base`(`id`, `drop_number_value`, `drop_char_value`, `drop_label`, `drop_type`, `drop_value_type`, `deep`, `pid`, `nodes`, `is_node`) VALUES (70, 9, '模型', '模型', 'assetsType', 0, 1, 0, ',70,', 0);

#lijubo 20230321 增加
alter table monitor.mw_cmdbmd_instance add proxy_id varchar(128) null comment '扫描代理引擎';

#lijubo 20230401 增加
alter table monitor.mw_scanresultsuccess_table add device_code varchar(255) null comment '设备指纹信息';

#guiqw mysql环境,增加告警模块内容

DELETE from  mw_module where id in (271,272,273);
INSERT INTO `mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`)
VALUES (271, 24, 'alarm_now', '当前告警查询', '', 1, 3, '23,24,271', 1, NULL, 0, NULL);
INSERT INTO `mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`)
VALUES (272, 24, 'alarm_history', '历史告警', '', 1, 3, '23,24,272', 1, NULL, 0, NULL);
INSERT INTO `mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`)
VALUES (273, 24, 'send_message', '消息发送', '', 1, 3, '23,24,273', 1, NULL, 0, NULL);

# 增加增删改的权限
DELETE from  mw_module_perm_mapper where module_id in (271,272,273);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 271, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 271, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 271, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 271, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 271, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 271, 6);

DELETE from  mw_role_module_perm_mapper where module_id in (271,272,273);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-271-browse', 0, 271, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-271-create', 0, 271, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-271-delete', 0, 271, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-271-editor', 0, 271, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-271-perform', 0, 271, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-271-secopassword', 0, 271, 'secopassword', 1);

# 增加增删改的权限
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 272, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 272, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 272, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 272, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 272, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 272, 6);

INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-272-browse', 0, 272, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-272-create', 0, 272, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-272-delete', 0, 272, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-272-editor', 0, 272, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-272-perform', 0, 272, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-272-secopassword', 0, 272, 'secopassword', 1);

# 增加增删改的权限
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 273, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 273, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 273, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 273, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 273, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 273, 6);

INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-273-browse', 0, 273, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-273-create', 0, 273, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-273-delete', 0, 273, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-273-editor', 0, 273, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-273-perform', 0, 273, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-273-secopassword', 0, 273, 'secopassword', 1);
#qzg 2023/04/09 设置业务系统模型为内置模型
update mw_cmdbmd_manage set model_level = 0 where model_name in ('业务系统','业务分类')
#lbq 23/4/12
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (863, '/mwapi/action/getAlertLevel');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (862, '/mwapi/alert/getAlertLevel');
#qzg 2023/04/12 新增模型属性分类
INSERT INTO `mw_cmdbmd_properties_type`(`properties_type_id`, `properties_type_name`) VALUES (4, '外部关联(多选)');
UPDATE `mw_cmdbmd_properties_type` SET `properties_type_name` = '外部关联(单选)' WHERE `properties_type_id` = 5;

#gengjb 2023/04/15 资产扫成功表增加snmp版本字段
ALTER TABLE `monitor`.`mw_scanresultsuccess_table` ADD COLUMN `snmp_version` int(11) NULL COMMENT 'snmp扫描版本';

#qzg 20230418 增加
alter table mw_cmdbmd_macro_value_authname add model_id int(11) null comment '模型Id';
#qzg 20230418 业务系统数据修改
update mw_cmdbmd_manage set model_id ='1002'
where model_id = (SELECT model_id FROM `mw_cmdbmd_manage`
where model_name = '告警标签父模型');
update mw_cmdbmd_manage set pids = REPLACE(pids,(SELECT model_id FROM `mw_cmdbmd_manage`
where model_name = '告警标签父模型'),'1002')
where pids like concat('%,',(SELECT model_id FROM `mw_cmdbmd_manage`
where model_name = '告警标签父模型'),',%');
UPDATE mw_cmdbmd_manage SET model_id = '1004'  WHERE model_name = '业务系统' and delete_flag = 0;
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (884, '/mwapi/modelView/marcoInfoByModel/delete');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (885, '/mwapi/modelRancher/login/browse');

#lijubo 20230418 扫描成功表轮训引擎字段定义
alter table mw_scanresultsuccess_table modify polling_engine varchar(128) null comment '轮训引擎';

#lijubo  20230419 新增实例视图表
create table mw_cmdbmd_instance_view
(
    id          bigint(64)   not null primary key,
    view_name   varchar(255) null comment '视图名称',
    instance_id int          null comment '实例id',
    creator     varchar(128) null comment '创建人',
    create_time timestamp    null comment '创建时间',
    modifier    varchar(128) null comment '修改人',
    upd_time    timestamp    null comment '修改时间'
)

#过期提醒定时任务sql
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (19, 11, '定时任务触发过期提醒', 'cn.mw.monitor.model.service.impl.MwModelInstanceServiceImplV1', 'getTimeOutInfoByTimeTask', 0);
INSERT INTO `mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (19, 11, 19);


#gengjb 2023-04-23 增加可视化组件
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (47, 33, '告警分类状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'admin', '2023-04-18 10:24:05', NULL, NULL, 0, NULL, 'alertClassify');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (48, 33, '告警趋势', 'alertTrend_icon.svg', 'alertTrend_drag.png', 'admin', '2023-04-18 10:25:42', NULL, NULL, 0, NULL, 'alertTrend');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (49, 33, '告警列表', 'alertList_icon.svg', 'alertList_drag.png', 'admin', '2023-04-18 15:37:15', NULL, NULL, 0, NULL, 'alertList');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (50, 33, '业务可用性', 'usability_icon.svg', 'usability_drag.png', 'admin', '2023-04-18 15:38:12', NULL, NULL, 0, NULL, 'businessUsab');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (51, 33, '健康数据', 'healthInfo_icon.svg', 'healthInfo_drag.png', 'admin', '2023-04-19 10:28:28', NULL, NULL, 0, NULL, 'healthInfo');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (52, 33, '健康状态', 'healthStatus_icon.svg', 'healthStatus_drag.png', 'admin', '2023-04-19 10:30:08', NULL, NULL, 0, NULL, 'healthStatus');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (53, 33, '拓扑', 'topology_icon.svg', 'topology_drag.png', 'admin', '2023-04-19 14:27:18', NULL, NULL, 0, NULL, 'topology');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (54, 33, '业务分区', 'businessSys_icon.svg', 'businessSys_drag.png', 'admin', '2023-04-19 14:53:38', NULL, NULL, 0, NULL, 'businessPartition');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (55, 33, '内存排行', 'memory_icon.svg', 'memory_drag.png', 'admin', '2023-04-20 08:56:34', NULL, NULL, 0, NULL, 'memoryTopN');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (56, 33, 'CPU排行', 'cpu_icon.svg', 'cpu_drag.png', 'admin', '2023-04-20 08:57:29', NULL, NULL, 0, NULL, 'cpuTopN');
INSERT INTO `monitor`.`mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (57, 33, '表空间排行', 'dbSpace_icon.svg', 'dbSpace_drag.png', 'admin', '2023-04-20 08:58:17', NULL, NULL, 0, NULL, 'tbsSpace');

#gengjb 2023-04-26 首页定时任务增加到定时任务功能执行
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (20, 9, '首页TopN缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheTopNData', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (21, 9, '首页流量缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheInterfaceFlowData', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (22, 9, '首页带宽缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheFlowBandWidthData', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (20, 9, 20);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (21, 9, 21);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (22, 9, 22);

#lbq 23/4/26 告警动作定时任务
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (13, '告警动作', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (23, 13, '告警压缩', 'cn.mw.time.MWZbxTime', 'alarmCompression', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (24, 13, '系统日志发送', 'cn.mw.time.MWZbxTime', 'sendSyslog', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (25, 13, 'zabbix服务监控', 'cn.mw.time.MWZbxTime', 'sendZabbixService', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (27, 13, '当前告警', 'cn.mw.time.MWZbxAlertNowTime', 'saveAlertGetNow', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (26, 13, '分级告警', 'cn.mw.time.MWZbxAlertLevelTime', 'levelAlert', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (28, 13, '历史告警', 'cn.mw.time.MWZbxAlertNowTime', 'saveAlertGetHist', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (23, 13, 23);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (24, 13, 24);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (25, 13, 25);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (26, 13, 26);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (27, 13, 27);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (28, 13, 28);
#qzg 0427
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (905, '/mwapi/modelView/editorModelFieldToEs/editor');

#qzg 0428 资源中心维护计划
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (10497, 274, 'name', '名称', 1, 1, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (10498, 274, 'maintenanceType', '类型', 1, 2, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (10499, 274, 'activeSince', '启用自从', 1, 3, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (10500, 274, 'activeTill', '启用直到', 1, 4, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (10501, 274, 'status', '状态', 1, 5, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (10502, 274, 'description', '描述', 1, 6, NULL, NULL);
#qzg 0428 资源中心维护计划
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (906, '/mwapi/modelAssets/mainTain/groupdropdown/browse');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (907, '/mwapi/modelAssets/mainTain/hostdropdown/browse');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (908, '/mwapi/modelAssets/mainTain/getAssetsDifficulty');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (909, '/mwapi/modelAssets/mainTain/fuzzQuery');
#Lumingmig 新增权限
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (910, '/mwapi/script-manage/accountBase/account/create');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (911, '/mwapi/script-manage/accountBase/account/editor');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (912, '/mwapi/script-manage/accountManage/alert/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (913, '/mwapi/script-manage/accountManage/alert/create');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (914, '/mwapi/script-manage/accountManage/alert/editor');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (915, '/mwapi/script-manage/accountManage/alert/delete');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (916, '/mwapi/script-manage/accountManage/alert/homework');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (917, '/mwapi/configManage/getVariableByid');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (918, '/mwapi/esDataRefresh/create');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (919, '/mwapi/adServer/syncOrg');

CREATE TABLE "mw_script_varibale" (
  "id" NUMBER(11,0) VISIBLE NOT NULL,
  "name" NVARCHAR2(255) VISIBLE,
  "type" NUMBER(11,0) VISIBLE,
  "value" NCLOB VISIBLE,
  "change_type" NUMBER(11,0) VISIBLE,
  "run_ture" NUMBER(11,0) VISIBLE,
  "varible_type" NUMBER(11,0) VISIBLE,
  "conrrelation_id" NUMBER(11,0) VISIBLE,
  "varible_desc" NVARCHAR2(1000) VISIBLE
);
COMMENT ON COLUMN "mw_script_varibale"."name" IS '变量名称';
COMMENT ON COLUMN "mw_script_varibale"."type" IS '变量种类 0.字符串 1.主机';
COMMENT ON COLUMN "mw_script_varibale"."value" IS '值';
COMMENT ON COLUMN "mw_script_varibale"."change_type" IS '赋值可变';
COMMENT ON COLUMN "mw_script_varibale"."run_ture" IS '执行前确定值';
COMMENT ON COLUMN "mw_script_varibale"."varible_type" IS '变量类型 0.作业变量 1.执行方案变量';
COMMENT ON COLUMN "mw_script_varibale"."conrrelation_id" IS '关联id';
COMMENT ON COLUMN "mw_script_varibale"."varible_desc" IS '变量描述';

-- ----------------------------
-- Primary Key structure for table mw_script_varibale
-- ----------------------------
ALTER TABLE "mw_script_varibale" ADD CONSTRAINT "SYS_C0015814" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table mw_script_varibale
-- ----------------------------
ALTER TABLE "mw_script_varibale" ADD CONSTRAINT "SYS_C0015813" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

ALTER TABLE "mw_script_exe_history_log"
ADD ("is_varible" NUMBER(11,0));

COMMENT ON COLUMN "mw_script_exe_history_log"."is_varible" IS '是否用变量';

CREATE TABLE "mw_homework_alert" (
  "id" NUMBER(11,0) VISIBLE NOT NULL,
  "alert_plan_name" NVARCHAR2(255) VISIBLE,
  "alert_title" NVARCHAR2(255) VISIBLE,
  "alert_level" NUMBER(11,0) VISIBLE,
  "alert_exe_homework" NVARCHAR2(255) VISIBLE
);
COMMENT ON COLUMN "mw_homework_alert"."alert_plan_name" IS '告警触发的名称';
COMMENT ON COLUMN "mw_homework_alert"."alert_title" IS '告警标题';
COMMENT ON COLUMN "mw_homework_alert"."alert_level" IS '告警等级';
COMMENT ON COLUMN "mw_homework_alert"."alert_exe_homework" IS '告警触发作业';

-- ----------------------------
-- Primary Key structure for table mw_homework_alert
-- ----------------------------
ALTER TABLE "mw_homework_alert" ADD CONSTRAINT "SYS_C0015816" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table mw_homework_alert
-- ----------------------------
ALTER TABLE "mw_homework_alert" ADD CONSTRAINT "SYS_C0015815" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

ALTER TABLE "mw_homework_manage_table"
ADD ("variable_ids" VARCHAR2(255));

#gqw 20230428 增加资源中心维护计划模块
INSERT INTO`mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`) VALUES
 (274, 216, 'asset-maintenance', '维护计划', '/mwapi/modelAssets/mainTain', 1, 2, '216,274', 1, NULL, 0, NULL);

# 增加对应的增删改的权限
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 274, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 274, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 274, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 274, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 274, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 274, 6);

INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-274-browse', 0, 274, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-274-create', 0, 274, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-274-delete', 0, 274, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-274-editor', 0, 274, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-274-perform', 0, 274, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-274-secopassword', 0, 274, 'secopassword', 1);

#20230508 lijubo 新增拓扑信息表
create table monitor.mw_cmdbmd_instance_topo_info
(
    id               varchar(32) null comment 'ID',
    instance_id      int         null comment '实例id',
    instance_view_id bigint(64)  null comment '实例拓扑视图id',
    topo_info        text        null comment '实例拓扑信息'
);

alter table mw_cmdbmd_instance_topo_info drop column topo_info;

#20230510 gengjb 可视化组件区增加播报组件
INSERT INTO `mw_visualized_chart_table`(`id`, `parent_id`, `partition_name`, `icon_url`, `drag_url`, `creator`, `create_date`, `modifier`, `modification_date`, `delete_flag`, `sign`, `partition_eng`) VALUES (63, 33, '测试播报', 'broadCast.svg', 'broadCast.svg', 'gengjb', '2023-05-10 11:30:19', NULL, NULL, 0, NULL, 'testBroadCast');

#23/5/11 告警定时任务
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (13, '告警动作', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (23, 13, '告警压缩', 'cn.mw.time.MWZbxTime', 'alarmCompression', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (24, 13, '系统日志发送', 'cn.mw.time.MWZbxTime', 'sendSyslog', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (25, 13, 'zabbix服务监控', 'cn.mw.time.MWZbxTime', 'sendZabbixService', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (26, 13, '分级告警', 'cn.mw.time.MWZbxAlertLevelTime', 'levelAlert', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (27, 13, '当前告警', 'cn.mw.time.MWZbxAlertNowTime', 'saveAlertGetNow', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (28, 13, '历史告警', 'cn.mw.time.MWZbxAlertNowTime', 'saveAlertGetHist', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (29, 13, '华星SQL告警', 'cn.mw.time.MWZbxHuaXingAlertTime', 'sendHuaXingAlert', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (23, 13, 23);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (24, 13, 24);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (25, 13, 25);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (26, 13, 26);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (27, 13, 27);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (28, 13, 28);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (29, 13, 29);

#20230515 gengjb 维护计划增加资源中心支持字段
ALTER TABLE `mw_assets_mainten_host`
ADD COLUMN `model_instance_id` varchar(255) NULL COMMENT '模型实例ID';


#gqw 20230516 增加拓扑管理根据资产标签创建拓扑
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (930, '/mwapi/topology/getSimplifyLabel');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (931, '/mwapi/topology/getAssetsLabelEnable');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (933, '/mwapi/topology/assetsLabel/browse');

#gqw 20230522 删除拓扑标签的脏数据
delete from mw_label_mapper where type_id not in (
    SELECT  id  from mw_topo_graph)  and module_type = 'TOPO_GRAPH';
delete from mw_label_drop_mapper where type_id not in (
    SELECT  id  from mw_topo_graph)  and module_type = 'TOPO_GRAPH';
delete from mw_label_date_mapper where type_id not in (
    SELECT  id  from mw_topo_graph)  and module_type = 'TOPO_GRAPH';

#20230523 gengjb 可视化定时任务
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (14, '可视化', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (30, 14, '可视化存储历史趋势数据', 'cn.mw.monitor.visualized.time.MwVisualizedModuleHistoryTime', 'getHistoryInfo', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (31, 14, '可视化获取监控项最新数据', 'cn.mw.monitor.visualized.time.MwVisualizedModuleTime', 'visualizedCacheInfo', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (32, 14, '可视化获取外部资产状态', 'cn.mw.monitor.visualized.time.MwVisualizedObtainDataTime', 'getZabbixHostInfoCache', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (30, 14, 30);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (31, 14, 31);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (32, 14, 32);


#20230601 qzg 资产接口信息表新增字段
ALTER TABLE `mw_cmdbmd_assets_interface` ADD COLUMN `is_show`  tinyint(1) NULL COMMENT '页面是否显示(默认显示全部)';
#20230602 qzg notcheck数据
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (949, '/mwapi/modelMonitor/getInterface/isOpen');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (950, '/mwapi/modelMonitor/refreshInterfaceInfo/perform');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (951, '/mwapi/modelMonitor/getNetDataList/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (952, '/mwapi/modelMonitor/getInterfaceInfo/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (953, '/mwapi/modelMonitor/getNetDetail/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (954, '/mwapi/modelMonitor/updateInterfaceDesc/editor');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (955, '/mwapi/modelMonitor/batchUpdateShow/editor');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (956, '/mwapi/modelMonitor/geInterfaceTypeInfo/browse');

#gengjb 23/6/6 可视化增加告警数据记录定时任务
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (33, 12, '可视化获取每天告警记录', 'cn.mw.monitor.visualized.time.MwVisualizedAlertRecordTime', 'getCurrAlertRecord', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (33, 14, 33);


#20230606 qzg 资产接口信息表新增字段
ALTER TABLE `mw_cmdbmd_assets_interface` ADD COLUMN `alert_tag`  tinyint(1) NULL COMMENT '是否接口告警';
ALTER TABLE `mw_cmdbmd_assets_interface` ADD COLUMN `host_id`  varchar(255) NULL COMMENT '主机HostId';
ALTER TABLE `mw_cmdbmd_assets_interface` ADD COLUMN `host_ip`  varchar(255) NULL COMMENT '主机Ip';

#20230609 gengjb 增加报表安全门限设置表
CREATE TABLE `mw_report_safevalue_table` (
  `id` varchar(128) NOT NULL COMMENT '主键ID',
  `name` varchar(128) DEFAULT NULL COMMENT '名称',
  `cpu_safevalue` int(10) DEFAULT NULL COMMENT 'CPU安全值',
  `memory_safevalue` int(10) DEFAULT NULL COMMENT '内存安全值',
  `interface_safevalue` int(10) DEFAULT NULL COMMENT '接口安全值',
  `type` int(10) DEFAULT NULL COMMENT '类型 1:区域标签类型 2：其他',
  `creator` varchar(128) DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报表安全门限设置表';

#20230613 添加字段
ALTER TABLE `monitor`.`mw_report_safevalue_table`
ADD COLUMN `interface_desc` varchar(255) NULL COMMENT '接口描述';

#23230614 gengjb 首页资产分组缓存任务
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (35, 9, '首页资产分组缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheNewScreenAssetsGroup', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (35, 9, 35);

ALTER TABLE `mw_cmdbmd_assets_interface` ADD COLUMN `editor_desc` tinyint(1) NULL COMMENT '是否修改过描述信息';


#20230614 gqw 增加拓扑根据机构绘制拓扑数据
CREATE TABLE `mw_topo_graph_assets_orgids` (
   `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
   `topo_graph_id` varchar(255) NOT NULL,
   `assets_org_ids` tinytext NOT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#20230615 gqw 增加拓扑根据机构绘制拓扑数据判断条件
INSERT INTO mw_notcheck_url(id, url) VALUES (972, '/mwapi/topology/getAssetsOrgEnable');

#23/06/20 qzg 资产凭证凭证表数据插入
INSERT INTO `monitor`.`mw_cmdbmd_model_macro`(`id`, `model_id`, `macro_id`) VALUES (32, 69, 1);
INSERT INTO `monitor`.`mw_cmdbmd_model_macro`(`id`, `model_id`, `macro_id`) VALUES (33, 69, 2);
INSERT INTO `monitor`.`mw_cmdbmd_model_macro`(`id`, `model_id`, `macro_id`) VALUES (34, 69, 3);

#23/06/21 qzg 数据源查询状态下拉框
INSERT INTO `monitor`.`mw_select_url_base`(`id`, `drop_number_value`, `drop_char_value`, `drop_label`, `drop_type`, `drop_value_type`, `deep`, `pid`, `nodes`, `is_node`) VALUES (72, 1, '状态', '正常', 'status', 0, 1, 0, ',72,', 0);
INSERT INTO `monitor`.`mw_select_url_base`(`id`, `drop_number_value`, `drop_char_value`, `drop_label`, `drop_type`, `drop_value_type`, `deep`, `pid`, `nodes`, `is_node`) VALUES (73, 0, '状态', '异常', 'status', 0, 1, 0, ',73,', 0);
UPDATE `monitor`.`mw_pageselect_table` SET  `input_format` = 3, `url` = '/selectNum/browse?type=source_status' WHERE `page_Id` = 224 and  `prop` = 'status';

#23/06/27 qzg
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (988, '/mwapi/modelView/getDeploymentType/browse');

#2023/6/30 gengjb 可视化增加资产可用性缓存
INSERT INTO `monitor`.`mw_visualized_cache_item_table`(`id`, `item_name`, `type`) VALUES ('30', 'MW_HOST_AVAILABLE', 1);

#2023/7/2 guiqw 四川项目增加ARP视图查看
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (991, '/mwapi/tableView/getEnable');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (990, '/mwapi/tableView/result/browse');
INSERT INTO `mw_notcheck_url`(`id`, `url`) VALUES (992, '/mwapi/tableView/result/exportExcel');
#ARP视图
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 276, 'macAddress', 'MAC地址', 1, 1, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 276, 'ipAddress', 'IP地址', 1, 2, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 276, 'type', '类别', 1, 3, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 276, 'interfaceName', '接口名称', 1, 4, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 276, 'vlanName', 'vlan名称', 1, 5, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 276, 'macAddress', 'MAC地址', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 276, 'interfaceName', '接口名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 276, 'vlanName', 'vlan名称', 1, NULL, NULL);
#MAC视图
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 277, 'macAddress', 'MAC地址', 1, 1, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 277, 'interfaceName', '接口名称', 1, 2, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 277, 'status', '状态', 1, 3, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 277, 'vlanName', 'vlan名称', 1, 4, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 277, 'macAddress', 'MAC地址', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 277, 'interfaceName', '接口名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 277, 'vlanName', 'vlan名称', 1, NULL, NULL);
#IP视图
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 278, 'macAddress', 'MAC地址', 1, 1, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 278, 'interfaceName', '接口名称', 1, 2, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 278, 'status', '状态', 1, 3, NULL, NULL);
INSERT INTO `mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 278, 'vlanName', 'vlan名称', 1, 5, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 278, 'macAddress', 'MAC地址', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 278, 'interfaceName', '接口名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 278, 'vlanName', 'vlan名称', 1, NULL, NULL);

#2023/7/3 gengjb  可视化增加表
CREATE TABLE `mw_visualized_businstatuse_table` (
  `id` varchar(128) NOT NULL COMMENT 'ID',
  `model_system_name` varchar(128) DEFAULT NULL COMMENT '业务系统名称',
  `title_name` varchar(128) DEFAULT NULL COMMENT '标题',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='可视化业务状态表';

#20230705 gqw 增加日志管理以及日志审计权限数据
#增加日志管理主菜单
INSERT INTO `monitor`.`mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`)
 VALUES (291, 0, '日志管理', '日志管理', '', 1, 1, '291', 1, 0, 0, NULL);
#更新原有逻辑，将每个菜单下移一位
update  mw_module set deep = 4 , nodes = CONCAT('291,',nodes)  where id in (267,268,269);
update  mw_module set deep = 3, nodes = CONCAT('291,',nodes) where id in (225,227,228);
update  mw_module set deep = 2, nodes = CONCAT('291,',nodes) ,pid = 291 where id in (224);
#增加逻辑权限
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 291, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 291, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 291, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 291, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 291, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 291, 6);
#增加admin对应的权限
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-291-browse', 0, 291, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-291-create', 0, 291, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-291-delete', 0, 291, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-291-editor', 0, 291, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-291-perform', 0, 291, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-291-secopassword', 0, 291, 'secopassword', 1);
#增加日志审计菜单
INSERT INTO `monitor`.`mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`)
 VALUES (292, 291, '日志审计', '日志审计', '', 1, 2, '291,292', 1, 0, 0, NULL);
#增加逻辑权限
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 292, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 292, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 292, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 292, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 292, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 292, 6);
#增加admin对应的权限
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-292-browse', 0, 292, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-292-create', 0, 292, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-292-delete', 0, 292, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-292-editor', 0, 292, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-292-perform', 0, 292, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-292-secopassword', 0, 292, 'secopassword', 1);

INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (994, '/mwapi/logManage/getTables');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (995, '/mwapi/logManage/getColumnByTable');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (996, '/mwapi/logManage/list');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (997, '/mwapi/logManage/saveSelectedColumns');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (998, '/mwapi/logManage/getLogAnalysisCacheInfo');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (999, '/mwapi/logManage/analysisChar');

#0711 qzg
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (989, '/mwapi/modelMacroAuth/getList/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (990, '/mwapi/modelMacroAuth/getAllMacroField/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (991, '/mwapi/modelMacroAuth/queryMacroField/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (992, '/mwapi/modelMacroAuth/addMacroAuthInfo/insert');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (993, '/mwapi/modelMacroAuth/updateMacroAuthInfo/editor');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (994, '/mwapi/modelMacroAuth/deleteMarcoInfo/delete');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1000, '/mwapi/modelMacroAuth/selectInfoPopup/brows');
#0714 qzg
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1002, '/mwapi/modelVirtual/queryAssetsInfo/browse');
#0721 qzg新增模型图标
ALTER TABLE `mw_cmdbmd_manage` ADD COLUMN `icon_type` tinyint(1) DEFAULT NULL COMMENT '模型图标类型(0内置，1用户自定义)';
#0725 qzg
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1010, '/mwapi/instanceName/browse');

#lbq 23/7/28
    INSERT INTO `monitor`.`mw_alert_action_type`(`id`, `action_type`) VALUES (25, '乐山银行');

#0801 接口url
#通道管理
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1006, '/mwapi/logManage/vectorChannel/add');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1007, '/mwapi/logManage/vectorChannel/update');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1008, '/mwapi/logManage/vectorChannel/list');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1009, '/mwapi/logManage/vectorChannel/delete');

INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11758, 295, 'mdificationDate', '更新时间', 1, 8, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11759, 295, 'mdificationUser', '更新人', 1, 9, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11751, 295, 'channelName', '通道名称', 1, 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11752, 295, 'channelIp', 'IP地址', 1, 2, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11753, 295, 'channelPort', '端口', 1, 3, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11754, 295, 'status', '通道状态', 1, 4, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11755, 295, 'type', '类型', 1, 5, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11756, 295, 'createDate', '创建时间', 1, 6, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11757, 295, 'createUser', '创建人', 1, 7, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11760, 295, 'relevanceRule', '规则名', 1, 10, NULL, NULL);

INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 295, 'channelName', '通道名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 295, 'channelIp', 'IP地址', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 295, 'status', '通道状态', 1, NULL, NULL);


#转发配置
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1011, '/mwapi/logManage/forward/delete');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1012, '/mwapi/logManage/forward/list');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1013, '/mwapi/logManage/forward/update');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1014, '/mwapi/logManage/forward/add');
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1015, '/mwapi/logManage/forward/getEnumValue');

INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'forwardingName', '转发名称', 1, 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'forwardingIp', 'IP地址', 1, 2, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'forwardingPort', '端口', 1, 3, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'status', '状态', 1, 4, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'forwardingKeyPath', 'KEY地址', 1, 5, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'relevanceRule', '规则名', 1, 6, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'createTime', '创建时间', 1, 7, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'createUser', '创建人', 1, 8, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'updateTime', '更新时间', 1, 9, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table` (`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (null, 296, 'updateUser', '更新人', 1, 10, NULL, NULL);

INSERT INTO `mw_pageselect_table` (`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 296, 'forwardingName', '转发名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 296, 'forwardingIp', 'IP地址', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (null, 296, 'status', '转发状态', 1, NULL, NULL);

#lijubo 2023/08/01
create table mw_cmdbmd_assets_snmp_info
(
	id varchar(128) not null comment '主键ID',
	mac_info longtext null comment 'mac表信息',
	arp_info longtext null comment 'arp表信息',
	interface_info longtext null comment '接口表信息',
	create_time timestamp null comment '创建时间',
	constraint mw_assets_snmp_info_pk primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资产snmp信息表';

INSERT INTO monitor.mw_ncm_newtimetask (id, model_id, action_id, time_name, time_description, time_button, time_object, time_start_time, time_end_time, time_count) VALUES ('16908903059985b65ab31367841efb3d', 6, 40, '同步资产详情snmp信息', null, 1, null, '2023-08-01 23:44:00', '2023-08-01 23:46:00', 11);
INSERT INTO monitor.mw_ncm_timetask_action (id, model_id, action_name, action_impl, action_method, action_model) VALUES (40, 6, '同步资产snmp信息', 'cn.mw.monitor.model.service.impl.MwModelTableViewServiceImpl', 'timeTask', 0);
INSERT INTO monitor.mw_ncm_timetask_tree (id, model_id, action_id) VALUES (40, 6, 40);

#gengjb 增加线路状态的下拉值
INSERT INTO `monitor`.`mw_dropdown_table`(`drop_id`, `drop_code`, `drop_key`, `drop_value`, `update_time`, `delete_flag`, `drop_val`) VALUES (7909, 'link_status', 1, '正常', '2023-08-07 15:02:00', 1, NULL);
INSERT INTO `monitor`.`mw_dropdown_table`(`drop_id`, `drop_code`, `drop_key`, `drop_value`, `update_time`, `delete_flag`, `drop_val`) VALUES (7910, 'link_status', 0, '异常', '2023-08-07 15:02:00', 1, NULL);

#lijubo 增加拓扑连线显示
alter table `monitor`.`mw_topo_graph` add column show_line boolean default true null comment '显示连线';

#shenwenyi 2023/8/9 导出用户在线时长接口
INSERT INTO `mw_notcheck_url` (`id`, `url`) VALUES (1016, '/mwapi/user/session/exportUserOnline');

# 创建用户登录时长表
CREATE TABLE `mw_user_session` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`user_id` int(11) DEFAULT NULL,
`user_name` varchar(255) DEFAULT NULL,
`org_id` varchar(255) DEFAULT NULL,
`org_name` varchar(255) DEFAULT NULL,
`login_time` datetime DEFAULT NULL,
`logout_time` datetime DEFAULT NULL,
`create_time` date DEFAULT NULL,
`online_time` bigint(20) DEFAULT NULL COMMENT '(毫秒)',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

#shenwenyi 2023/8/10  查询用户在线时长分页接口
INSERT INTO `monitor`.`mw_notcheck_url` (`id`, `url`) VALUES (1017, '/mwapi/user/session/browse');
INSERT INTO `monitor`.`mw_report_table` (`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`, `send_time`) VALUES ('15', '用户在线时长报表', '用户在线时长报表', 1, 1, '2023-08-10 14:36:51', 'swy', '2023-08-10 14:36:58', 'swy', 0, NULL);
alter table `monitor`.`mw_user_session` add column `login_name` varchar(30) character set utf8mb4 default null;
#lbq 2023/8/21 新增模块
    INSERT INTO `monitor`.`mw_module`(`id`, `pid`, `module_name`, `module_desc`, `url`, `is_node`, `deep`, `nodes`, `enable`, `version`, `delete_flag`, `node_protocol`) VALUES (295, 0, '事件流', '事件流', NULL, 1, 1, '295', 1, 0, 0, NULL);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 295, 1);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 295, 2);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 295, 3);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 295, 4);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 295, 5);
INSERT INTO `mw_module_perm_mapper`(`id`, `module_id`, `perm_id`) VALUES (null, 295, 6);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-295-browse', 0, 295, 'browse', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-295-create', 0, 295, 'create', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-295-delete', 0, 295, 'delete', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-295-editor', 0, 295, 'editor', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-295-perform', 0, 295, 'perform', 1);
INSERT INTO `mw_role_module_perm_mapper`(`id`, `role_id`, `module_id`, `perm_name`, `enable`)
VALUES ('0-295-secopassword', 0, 295, 'secopassword', 1);
#qzg 2023/08/21
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1052, '/mwapi/modelMonitor/updateStatus/editor');

#gengjb 2023/08/29 增加资产流量统计报表
INSERT INTO `monitor`.`mw_report_table`(`id`, `report_name`, `report_desc`, `report_type_id`, `report_time_id`, `create_date`, `creator`, `modification_date`, `modifier`, `delete_flag`, `send_time`) VALUES ('29', '资产流量统计报表', '资产流量统计报表', 2, 1, '2023-08-28 14:59:55', 'admin', '2023-08-28 15:00:00', 'admin', 0, NULL);

#qzg 2023/09/04 创建资产实例评价表
CREATE TABLE `mw_cmdbmd_judge` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `judge_message` varchar(1024) DEFAULT NULL COMMENT '评价内容',
  `judge_score` int(4) DEFAULT NULL COMMENT '评价分值',
  `judge_time` datetime DEFAULT NULL COMMENT '评价时间',
  `user_id` int(11) DEFAULT NULL COMMENT '评价人Id',
  `user_name` varchar(255) DEFAULT NULL COMMENT '评价人',
  `instance_id` int(11) DEFAULT NULL COMMENT '资产实例Id(项目Id，供应商Id)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1070, '/mwapi/judgeMessage/selectInfoById/browse');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1071, '/mwapi/judgeMessage/insertJudgeInfo/create');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1072, '/mwapi/judgeMessage/checkJudgeCycle/browse');

#lbq 23/9/8
CREATE TABLE `mw_alert_trigger_close_table`  (
                                                 `server_id` int(255) NULL DEFAULT NULL,
                                                 `trigger_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                                 `close_date` datetime(0) NULL DEFAULT NULL,
                                                 `operator` int(255) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE `monitor`.`mw_alert_record_table`
    ADD COLUMN `eventid` varchar(255);

#gengjb 2023/09/19 容器告警缓存
CREATE TABLE `mw_visualized_containeralert_table` (
      `id` varchar(128) NOT NULL COMMENT '主键ID',
      `alert_level` varchar(128) DEFAULT NULL COMMENT '告警等级',
      `alert_count` varchar(128) DEFAULT NULL COMMENT '告警数量',
      `alert_date` varchar(128) DEFAULT NULL COMMENT '告警时间',
      `partition_name` varchar(128) DEFAULT NULL COMMENT '分区名称',
      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='可视化容器告警信息';


#lmm 23/9/19
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (42, 15, 42);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (42, 15, '自动化处理超时任务', 'cn.mw.monitor.script.service.impl.ScriptManageServiceImpl', 'gameOverTask', 0);

#gengjb 2023/10/07 可视化健康分数占比情况表
CREATE TABLE `mw_visualized_scoreproportion_table` (
   `id` varchar(128) NOT NULL COMMENT '主键ID',
   `proportion` int(10) DEFAULT NULL COMMENT '占比',
   `type` int(10) DEFAULT NULL COMMENT '类型，1；业务分类，2：资产类型，3：资产名称',
   `classify_name` varchar(255) DEFAULT NULL COMMENT '分类名称，多个分类以,分割',
   `item_name` varchar(255) DEFAULT NULL COMMENT '监控项',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='可视化评分占比';

#qzg 1017规格型号表新增设备高度字段
ALTER TABLE `monitor`.`mw_cmdbmd_vendor_specification` ADD COLUMN `device_height` int(11) NULL COMMENT '设备高度';
#qzg 1017规格型号新增表头字段
INSERT INTO `monitor`.`mw_pagefield_table`(`page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (245, 'deviceHeight', '设备高度', 1, 2, NULL, NULL);
INSERT INTO `monitor`.`mw_pageselect_table`(`page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (245, 'deviceHeight', '设备高度', 1, NULL, NULL);
#qzg 1020 创建可视化页面查询参数保存表
CREATE TABLE `mw_visualized_query_value` (
                                             `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                                             `value_json` text DEFAULT NULL,
                                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#lbq 23/10/27
ALTER TABLE `monitor`.`mw_alert_action`
    MODIFY COLUMN `start_time` varchar(32) NULL DEFAULT NULL COMMENT '开始时间' AFTER `effect_time_select`,
    MODIFY COLUMN `end_time` varchar(32) NULL DEFAULT NULL COMMENT '结束时间' AFTER `start_time`;

#lbq 23/11/13
    INSERT INTO `monitor`.`mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (10384, 298, 'monitorServerName', '监控服务器', 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (10385, 298, 'userName', '操作用户', 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (10386, 298, 'ignoreDate', '操作日期', 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pageselect_table`(`id`, `page_Id`, `prop`, `label`, `input_format`, `url`, `typeof`) VALUES (10387, 298, 'name', '告警标题', 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11789, 298, 'monitorServerName', '监控服务器', 4, 1, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11790, 298, 'userName', '操作用户', 1, 2, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11791, 298, 'ignoreDate', '操作日期', 1, 3, NULL, NULL);
INSERT INTO `monitor`.`mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11792, 298, 'name', '告警标题', 1, 1, NULL, NULL);

CREATE TABLE `mw_alert_ignore_table`  (
                                          `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                                          `server_id` int(255) NULL DEFAULT NULL,
                                          `event_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                          `ignore_date` datetime(0) NULL DEFAULT NULL,
                                          `operator` int(255) NULL DEFAULT NULL,
                                          `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

#20231114
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (989, '/mwapi/alert/ignore/alert');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (990, '/mwapi/alert/getignore/alert');
#23/11/15 lbq
ALTER TABLE `monitor`.`mw_license_list_table`
    MODIFY COLUMN `expire_date` varchar(32) NULL DEFAULT NULL COMMENT '到期时间' AFTER `module_name`;

#lbq 23/11/17
    INSERT INTO `monitor`.`mw_alert_action_type`(`id`, `action_type`) VALUES (26, '云之讯');

#qzg 1116模板表新增转态字段
INSERT INTO `monitor`.`mw_pagefield_table`(`id`, `page_id`, `prop`, `label`, `visible`, `order_number`, `type`, `is_tree`) VALUES (11793, 244, 'status', '启用状态', 1, 5, NULL, NULL);
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1099, '/mwapi/modelTemplate/templateStatus/editor');
update mw_cmdbmd_template_table set status = 1;
ALTER TABLE `monitor`.`mw_cmdbmd_template_table` ADD COLUMN `status` tinyint(1)  NULL COMMENT '启用状态 0禁用，1启用';


ALTER TABLE `monitor`.`mw_cmdbmd_manage` ADD COLUMN `model_sort` int(4)  NULL COMMENT '模型排序数字';


#20231122
INSERT INTO `monitor`.`mw_notcheck_url` (`id`, `url`) VALUES (1102, '/mwapi/userGroup/sort');
#20231124 qzg
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1104, '/mwapi/judgeMessage/JudgeInfo/delete');

#lbq 23/11/30
    INSERT INTO `monitor`.`mw_alert_action_type`(`id`, `action_type`) VALUES (27, '天畅云');
#lbq 23/12/1
UPDATE `monitor`.`mw_ncm_timetask_action` SET `model_id` = 13, `action_name` = '采集器服务监控', `action_impl` = 'cn.mw.time.MWZbxTime', `action_method` = 'sendZabbixService', `action_model` = 0 WHERE `id` = 25;

#lbq 23/12/20
    INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (989, '/mwapi/alert/triggerexport/perform');

#lbq 23/1/3
ALTER TABLE `monitor`.`mw_alert_confirm_user_table`
    ADD COLUMN `type` varchar(255) NULL AFTER `user_id`;
#lbq 23/1/4
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1130, '/mwapi/alert/getAlertCount');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1131, '/mwapi/alert/getAlert');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1132, '/mwapi/alert/getEventFlowByEventId');
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1133, '/mwapi/alert/getAlertMessage');

#gengjb 20240118 首页增加接口丢包率组件
INSERT INTO `monitor`.`mw_notcheck_url`(`id`, `url`) VALUES (1132, '/mwapi/new/screen/getInterfaceRate');
INSERT INTO `monitor`.`mw_newhomepage_init`(`bulk_id`, `bulk_name`, `delete_flag`, `module_url`, `display_time`, `init_type`) VALUES (16, '接口丢包率', 0, '/new/screen/getInterfaceRate', 0, 1);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (60, 9, '首页获取接口丢包率数据', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheInterfaceRateData', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (60, 9, 60);
#lbq 23/1/18
INSERT INTO mw_ncm_timetask_tree(id, model_id, action_id) VALUES ('63', '13', '63');
INSERT INTO mw_ncm_timetask_action(id, model_id, action_name, action_impl, action_method, action_model) VALUES ('63', '13', '消息记录写入TXT文档', 'cn.mw.time.MWZbxAlertLevelTime', 'sendInfoWriteTxt', NULL);
#gengjb 24/01/30 增加磁盘报表分机构进行邮件发送定时任务语句
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (65, 16, '磁盘报表定时邮件发送', 'cn.mw.monitor.report.timer.MwDiskReportSendEmailTime', 'sendDiskReport', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (65, 16, 65);
#lmm 24/03/1
ALTER TABLE `monitor`.`mw_sys_user`
ADD COLUMN `oa` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '绑定其他系统用户登录名' AFTER `more_phones`;
#lbq 23/3/7
    INSERT INTO `monitor`.`mw_alert_action_type`(`id`, `action_type`) VALUES (28, '飞书');
