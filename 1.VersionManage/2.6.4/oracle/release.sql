create sequence SEQ_MW_ASSETSSNMPV1_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_ASSETSSNMPV3_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_AGENTASSETS_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_PORTASSETS_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_IOTASSETS_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_DEVICE_INFO minvalue 1 nomaxvalue start with 1 increment by 1;




#lmm
ALTER TABLE "MWDEVSQL"."mw_ipaddressmanage_table"
MODIFY ("longitude"  NULL)

create sequence SEQ_IPADDRESSMANAGE_LIST
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_IPADDRESS_INSERT
before insert on "mw_ipaddressmanagelist_table"
for each row
begin
select SEQ_IPADDRESSMANAGE_LIST.nextval into :new."id" from dual;
end T_IPADDRESS_INSERT;


create sequence SEQ_IP_DISTRIBUTION
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_IP_DISTRIBUTION
before insert on "mw_ip_distribution"
for each row
begin
select SEQ_IP_DISTRIBUTION.nextval into :new."id" from dual;
end T_IP_DISTRIBUTIONT;


create sequence SEQ_IPADDRESS_STATUS_HIS
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_IPADDRESS_STATUS_HIS
before insert on "mw_ipaddress_status_his"
for each row
begin
select SEQ_IPADDRESS_STATUS_HIS.nextval into :new."id" from dual;
end T_IPADDRESS_STATUS_HIS;


create sequence SEQ_NEWTIMETASK_MAPPER_OBJECT
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_NEWTIMETASK_MAPPER_OBJECT
before insert on "mw_ncm_newtimetask_mapper_object"
for each row
begin
select SEQ_NEWTIMETASK_MAPPER_OBJECT.nextval into :new."id" from dual;
end T_NEWTIMETASK_MAPPER_OBJECT;


create sequence SEQ_NEWTIMETASK_MAPPER_TIME
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_NEWTIMETASK_MAPPER_TIME
before insert on "mw_ncm_newtimetask_mapper_time"
for each row
begin
select SEQ_NEWTIMETASK_MAPPER_TIME.nextval into :new."id" from dual;
end T_NEWTIMETASK_MAPPER_TIME;

create sequence SEQ_UNFINISH_PROCESS
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_UNFINISH_PROCESS
before insert on "my_unfinish_process"
for each row
begin
select T_UNFINISH_PROCESS.nextval into :new."id" from dual;
end SEQ_UNFINISH_PROCESS;

create sequence SEQ_PUBIPADDRESS
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;


create or replace trigger T_PUBIPADDRESS
before insert on "mw_pubipaddress_table"
for each row
begin
select T_PUBIPADDRESS.nextval into :new."id" from dual;
end SEQ_PUBIPADDRESS;


ALTER TABLE "mw_ipv6manage_table"
MODIFY ("index_sort"  DEFAULT 0)


ALTER TABLE "MWDEVSQL"."mw_ipaddressmanage_table"
MODIFY ("index_sort"  DEFAULT 0)



ALTER TABLE "MWDEVSQL"."mw_ncm_newtimetask"
MODIFY ("time_button"  DEFAULT 0)


ALTER TABLE "MWDEVSQL"."mw_ncm_newtimetask"
MODIFY ("action_id" NVARCHAR2(11))


create sequence SEQ_MW_SOLAR_TIME minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_ALERT_RECORD_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_ALERT_OVERDUE_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_CMDBMD_ASSETS_INTERFACE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_ASSETS_MAINTEN minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_ASSETS_MAINTEN_HOST minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_LIUCENSE_EXPIRE_TABLE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_IP_SCAN_ASSETS minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_TOPO_GRAPH_SNMPV1V2 minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_TOPO_GRAPH_SNMPV3 minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_TOPO_GROUP minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_TOPO_GROUP_INFO minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_TOPO_SUBLINE_SETTING minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_TOPO_TREE minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_SYS_LOG_DATASOURCE_SETTING minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_SYS_LOG_RULE_MAPPER minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_mw_sys_log_tag_mapper minvalue 1 nomaxvalue start with 1 increment by 1;
create sequence SEQ_MW_SYS_LOG_TAG minvalue 1 nomaxvalue start with 1 increment by 1;



ALTER TABLE "mw_knowledgebase_table"
MODIFY ("delete_flag"  DEFAULT 0)

ALTER TABLE "MWDEVSQL"."mw_ipaddressmanage_table"
MODIFY ("parent_id"  DEFAULT 0)

#lijubo add 20230318
ALTER TABLE "MWDEVSQL"."mw_scanrule_table" ADD ("engine_id" varchar2(256));
COMMENT ON COLUMN "MWDEVSQL"."mw_scanrule_table"."engine_id" IS '引擎id';

ALTER TABLE "MWDEVSQL"."mw_scanresultsuccess_table" ADD ("template_name" varchar2(256));
COMMENT ON COLUMN "MWDEVSQL"."mw_scanresultsuccess_table"."template_name" IS '模版名';

#lijubo add 20230319
ALTER TABLE "MWDEVSQL"."mw_cmdbmd_instance" ADD ("notify_info" varchar2(128));
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance"."notify_info" IS '实例通知信息';

create sequence SEQ_WEBMONITOR minvalue 1 nomaxvalue start with 1 increment by 1;
#qzg 20230321 增加资产视图树结构分组
INSERT INTO "MWDEVSQL"."mw_select_url_base"("id", "drop_number_value", "drop_char_value", "drop_label", "drop_type", "drop_value_type", "deep", "pid", "nodes", "is_node") VALUES ('70', '9', '模型', '模型', 'assetsType', '0', '1', '0', ',70,', '0');

#lijubo add 20230322
ALTER TABLE "MWDEVSQL"."mw_cmdbmd_instance" ADD ("proxy_id" varchar2(128));
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance"."proxy_id" IS '实例通知信息';

ALTER TABLE "MWDEVSQL"."mw_cmdbmd_instance" ADD ("topo_info" CLOB);
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance"."topo_info" IS '实例拓扑信息';



#gengjb 20230322 修改监控大屏表默认值
ALTER TABLE "MWDEVSQL"."mw_large_screen_table" MODIFY ("delete_flag"  DEFAULT 0);
ALTER TABLE "MWDEVSQL"."mw_layout_data" MODIFY ("delete_flag"  DEFAULT 0);

#lbq 2023/3/28 修改alert表默认值
ALTER TABLE "MWDEVSQL"."mw_alert_action"
MODIFY ("success_num"  DEFAULT 0)
MODIFY ("fail_num"  DEFAULT 0);

#lbq 2023/3/28 notcheck表数据更新
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('862', '/mwapi/alert/getAlertLevel');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('863', '/mwapi/action/getAlertLevel');

#lijubo add 20230401
ALTER TABLE "MWDEVSQL"."mw_scanresultsuccess_table" ADD ("device_code" varchar2(255));
COMMENT ON COLUMN "MWDEVSQL"."mw_scanresultsuccess_table"."device_code" IS '设备指纹信息';

#lbq 2023/4/3 新字段
ALTER TABLE "MWDEVSQL"."mw_alert_action_level_rule_mapper"
ADD ("is_send_person" NUMBER(4,0));
ALTER TABLE "MWDEVSQL"."mw_alert_action_level_rule_mapper"
ADD ("is_send_person" NUMBER(4,0) DEFAULT 0);

#lbq 2023/4/4 更新pageid字段
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'eventid', "label" = '事件ID', "visible" = '0', "order_number" = '0', "type" = NULL, "is_tree" = NULL WHERE "id" = '89';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'objectid', "label" = '对象ID', "visible" = '0', "order_number" = '0', "type" = NULL, "is_tree" = NULL WHERE "id" = '90';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'severity', "label" = '告警级别', "visible" = '1', "order_number" = '1', "type" = NULL, "is_tree" = NULL WHERE "id" = '91';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'name', "label" = '告警标题', "visible" = '1', "order_number" = '2', "type" = NULL, "is_tree" = NULL WHERE "id" = '92';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'objectName', "label" = '告警对象', "visible" = '1', "order_number" = '3', "type" = NULL, "is_tree" = NULL WHERE "id" = '93';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'alertType', "label" = '告警类型', "visible" = '1', "order_number" = '6', "type" = NULL, "is_tree" = NULL WHERE "id" = '94';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'ip', "label" = 'IP地址', "visible" = '1', "order_number" = '4', "type" = NULL, "is_tree" = NULL WHERE "id" = '95';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'clock', "label" = '告警时间', "visible" = '1', "order_number" = '7', "type" = NULL, "is_tree" = NULL WHERE "id" = '96';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'longTime', "label" = '持续时间', "visible" = '1', "order_number" = '8', "type" = NULL, "is_tree" = NULL WHERE "id" = '97';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'acknowledged', "label" = '处理状态', "visible" = '1', "order_number" = '5', "type" = NULL, "is_tree" = NULL WHERE "id" = '98';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'rclock', "label" = '恢复时间', "visible" = '1', "order_number" = '9', "type" = NULL, "is_tree" = NULL WHERE "id" = '99';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'monitorServerName', "label" = '监控服务器名称', "visible" = '1', "order_number" = '10', "type" = NULL, "is_tree" = NULL WHERE "id" = '100';

INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1682', '272', 'eventid', '事件ID', '0', '0', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1671', '272', 'objectid', '对象ID', '0', '0', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1672', '272', 'severity', '告警级别', '1', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1673', '272', 'name', '告警标题', '1', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1674', '272', 'objectName', '告警对象', '1', '3', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1675', '272', 'alertType', '告警类型', '1', '6', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1676', '272', 'ip', 'IP地址', '1', '4', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1677', '272', 'clock', '告警时间', '1', '7', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1678', '272', 'longTime', '持续时间', '1', '8', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1679', '272', 'acknowledged', '处理状态', '1', '5', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1680', '272', 'rclock', '恢复时间', '1', '9', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1681', '272', 'monitorServerName', '监控服务器名称', '1', '10', NULL, NULL);

UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '273', "prop" = 'date', "label" = '通知日期', "visible" = '1', "order_number" = '1', "type" = NULL, "is_tree" = NULL WHERE "id" = '10278';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '273', "prop" = 'method', "label" = '通知方式', "visible" = '1', "order_number" = '2', "type" = NULL, "is_tree" = NULL WHERE "id" = '10279';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '273', "prop" = 'text', "label" = '消息内容', "visible" = '1', "order_number" = '4', "type" = NULL, "is_tree" = NULL WHERE "id" = '10280';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '273', "prop" = 'userName', "label" = '接收人', "visible" = '1', "order_number" = '3', "type" = NULL, "is_tree" = NULL WHERE "id" = '10281';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '273', "prop" = 'resultState', "label" = '通知结果', "visible" = '1', "order_number" = '5', "type" = NULL, "is_tree" = NULL WHERE "id" = '10282';

UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'rclock', "label" = '恢复时间', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '710';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'fuzzyQuery', "label" = '模糊查询', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '732';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'eventid', "label" = '事件ID', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '68';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'name', "label" = '告警标题', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '69';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'objectName', "label" = '告警对象', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '70';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'alertType', "label" = '告警类型', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '71';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'ip', "label" = 'IP地址', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '72';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'clock', "label" = '告警时间', "input_format" = '2', "url" = NULL, "typeof" = NULL WHERE "id" = '73';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'longTime', "label" = '持续时间', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '74';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'acknowledged', "label" = '处理状态', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '75';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '271', "prop" = 'monitorServerName', "label" = '监控服务器名称', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '77';

INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('11710', '272', 'rclock', '恢复时间', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('11732', '272', 'fuzzyQuery', '模糊查询', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1168', '272', 'eventid', '事件ID', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1169', '272', 'name', '告警标题', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1170', '272', 'objectName', '告警对象', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1171', '272', 'alertType', '告警类型', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1172', '272', 'ip', 'IP地址', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1173', '272', 'clock', '告警时间', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1174', '272', 'longTime', '持续时间', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1175', '272', 'acknowledged', '处理状态', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('1177', '272', 'monitorServerName', '监控服务器名称', '1', NULL, NULL);

UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '273', "prop" = 'method', "label" = '通知方式', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '10282';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '273', "prop" = 'text', "label" = '消息内容', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '10283';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '273', "prop" = 'userName', "label" = '接收人', "input_format" = '1', "url" = NULL, "typeof" = NULL WHERE "id" = '10284';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '273', "prop" = 'resultState', "label" = '通知结果', "input_format" = '3', "url" = NULL, "typeof" = NULL WHERE "id" = '10285';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '273', "prop" = 'date', "label" = '通知日期', "input_format" = '2', "url" = NULL, "typeof" = NULL WHERE "id" = '10286';


#guiqw 新增模块数据(告警相关)
INSERT INTO "mw_module"("id", "pid", "module_name", "module_desc", "url", "is_node", "deep", "nodes", "enable", "version", "delete_flag", "node_protocol") VALUES ('271', '24', 'alarm_now', '当前告警查询', NULL, '1', '3', '23,24,271', '1', '0', '0', NULL);
INSERT INTO "mw_module"("id", "pid", "module_name", "module_desc", "url", "is_node", "deep", "nodes", "enable", "version", "delete_flag", "node_protocol") VALUES ('272', '24', 'alarm_history', '历史告警', NULL, '1', '3', '23,24,272', '1', '0', '0', NULL);
INSERT INTO "mw_module"("id", "pid", "module_name", "module_desc", "url", "is_node", "deep", "nodes", "enable", "version", "delete_flag", "node_protocol") VALUES ('273', '24', 'send_message', '消息发送', NULL, '1', '3', '23,24,273', '1', '0', '0', NULL);

# 增加增删改的权限
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 271, 1);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 271, 2);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 271, 3);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 271, 4);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 271, 5);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 271, 6);

INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-271-browse', 0, 271, 'browse', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-271-create', 0, 271, 'create', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-271-delete', 0, 271, 'delete', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-271-editor', 0, 271, 'editor', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-271-perform', 0, 271, 'perform', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-271-secopassword', 0, 271, 'secopassword', 1);


# 增加增删改的权限
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 272, 1);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 272, 2);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 272, 3);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 272, 4);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 272, 5);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 272, 6);

INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-272-browse', 0, 272, 'browse', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-272-create', 0, 272, 'create', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-272-delete', 0, 272, 'delete', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-272-editor', 0, 272, 'editor', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-272-perform', 0, 272, 'perform', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-272-secopassword', 0, 272, 'secopassword', 1);
# 增加增删改的权限
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 273, 1);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 273, 2);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 273, 3);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 273, 4);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 273, 5);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 273, 6);

INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-273-browse', 0, 273, 'browse', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-273-create', 0, 273, 'create', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-273-delete', 0, 273, 'delete', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-273-editor', 0, 273, 'editor', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-273-perform', 0, 273, 'perform', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-273-secopassword', 0, 273, 'secopassword', 1);


#lmm
DROP TABLE "mw_script_out_asssets";
CREATE TABLE "mw_script_out_asssets" (
  "id" NUMBER(11,0) VISIBLE NOT NULL,
  "hostname" NVARCHAR2(255) VISIBLE,
  "ip" NVARCHAR2(255) VISIBLE,
  "account_id" NUMBER VISIBLE
)
TABLESPACE "MONITOR_DAT"
LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;

-- ----------------------------
-- Primary Key structure for table mw_script_out_asssets
-- ----------------------------
ALTER TABLE "mw_script_out_asssets" ADD CONSTRAINT "SYS_C0015589" PRIMARY KEY ("id");


#gengjb 2023/04/10 增加接口地址
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('875', '/mwapi/modelView/open/getModel/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('876', '/mwapi/server/open/getMonitoringItems/browse');
#qzg 2023/04/12 新增模型属性分类
INSERT INTO "mw_cmdbmd_properties_type"("properties_type_id", "properties_type_name") VALUES ('4', '外部关联(多选)');
UPDATE "mw_cmdbmd_properties_type" SET "properties_type_name" = '外部关联(单选)' WHERE "properties_type_id" = '5';

#lbq 23/4/13 华星光电sql解析
CREATE TABLE "MWDEVSQL"."BUSSINESS_ALARM_INFO" (
  "DBID" VARCHAR2(32 BYTE) VISIBLE NOT NULL,
  "IP" VARCHAR2(20 BYTE) VISIBLE NOT NULL,
  "BUSSINESS_NAME" VARCHAR2(20 BYTE) VISIBLE,
  "CONTENT" CLOB VISIBLE,
  "ALARM_LEVEL" NUMBER(8,0) VISIBLE NOT NULL,
  "CREATE_TIME" TIMESTAMP(6) VISIBLE,
  "UPDAT_TIME" TIMESTAMP(6) VISIBLE,
  "STATUS" VARCHAR2(6 BYTE) VISIBLE NOT NULL,
  "TABLE_CONTENT" VARCHAR2(1024 BYTE) VISIBLE,
  "ALARM_EVENT_NAME" VARCHAR2(255 BYTE) VISIBLE
)
TABLESPACE "MONITOR_DAT"



-- ----------------------------
-- Checks structure for table mw_script_out_asssets
-- ----------------------------
ALTER TABLE "mw_script_out_asssets" ADD CONSTRAINT "SYS_C0015588" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

#gengjb 2023/04/15 资产扫成功表增加snmp版本字段
ALTER TABLE "MWDEVSQL"."mw_scanresultsuccess_table"
ADD ("snmp_version" NUMBER(11,0));
COMMENT ON COLUMN "MWDEVSQL"."mw_scanresultsuccess_table"."snmp_version" IS 'snmp扫描版本';
#qzg 2023/04/18 同步凭证增加字段modelId
ALTER TABLE "mw_cmdbmd_macro_value_authname" ADD ("model_id" NUMBER(11,0));
#qzg 20230418 业务系统数据修改
update "mw_cmdbmd_manage" set "pids" = REPLACE("pids",(SELECT "model_id" FROM "mw_cmdbmd_manage"
where "model_name" = '告警标签父模型'),'1002')
where "pids" like ('%,'||(SELECT "model_id" FROM "mw_cmdbmd_manage"
where "model_name" = '告警标签父模型')||',%');
update "mw_cmdbmd_manage" set "model_id" ='1002'
where "model_id" = (SELECT "model_id" FROM "mw_cmdbmd_manage"
where "model_name" = '告警标签父模型');
UPDATE "mw_cmdbmd_manage" SET "model_id" = '1004'  WHERE "model_name" = '业务系统'  and "delete_flag" = '0';
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('868', '/mwapi/modelView/marcoInfoByModel/delete');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('883', '/mwapi/modelRancher/login/browse');

#lijub 20230418 修改扫描结果字段
alter table "MWDEVSQL"."mw_scanresultsuccess_table" modify "polling_engine" NVARCHAR2(128)

#lijubo  20230419 新增实例视图表
create table "MWDEVSQL"."mw_cmdbmd_instance_view"
(
    "id"          NUMBER(24) not null constraint mw_cmdbmd_instance_view_pk primary key,
    "view_name"   NVARCHAR2(255) null,
    "instance_id" NUMBER(11,0)   null,
    "creator"     NVARCHAR2(255) null,
    "create_time" TIMESTAMP      null,
    "modifier"    NVARCHAR2(255) null,
    "upd_time"    TIMESTAMP      null
);
comment on table "MWDEVSQL"."mw_cmdbmd_instance_view" is '实例视图';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."id" is 'ID';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."view_name" is '视图名称';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."instance_id" is '实例id';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."creator" is '创建人';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."create_time" is '创建时间';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."modifier" is '修改人';
comment on column "MWDEVSQL"."mw_cmdbmd_instance_view"."upd_time" is '修改时间';

INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (867, '/mwapi/modelRelation/instanceView/create');
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (868, '/mwapi/modelRelation/instanceView/editor');
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (869, '/mwapi/modelRelation/instanceView/delete');
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (870, '/mwapi/modelRelation/instanceView/select');
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (871, '/mwapi/modelRelation/instanceView/browse');

#lbq 23/4/20 通知规则类型
INSERT INTO "MWDEVSQL"."mw_alert_action_type"("id", "action_type") VALUES ('17', '华星光电-移动平台');
INSERT INTO "MWDEVSQL"."mw_alert_action_type"("id", "action_type") VALUES ('18', 'T信');
CREATE TABLE "MWDEVSQL"."mw_alert_huaxing_rule" (
  "rule_id" VARCHAR2(32 BYTE) VISIBLE NOT NULL,
  "app_id" VARCHAR2(255 BYTE) VISIBLE NOT NULL,
  "app_key" VARCHAR2(255 BYTE) VISIBLE NOT NULL,
  "plugin_id" VARCHAR2(255 BYTE) VISIBLE,
  "token" VARCHAR2(255 BYTE) VISIBLE,
  "url" VARCHAR2(255 BYTE) VISIBLE NOT NULL,
  "sender" VARCHAR2(255 BYTE) VISIBLE,
  "session_type" VARCHAR2(255 BYTE) VISIBLE
)
TABLESPACE "MONITOR_DAT"

#zah 2023/4/18 mw_sys_log_datasource_setting表新增认证方式id字段并更改id类型,新增数据源运行状态字段
ALTER TABLE "MWDEVSQL"."mw_sys_log_datasource_setting"
    ADD ("auth_type" NUMBER(6,0));
COMMENT ON COLUMN "MWDEVSQL"."mw_sys_log_datasource_setting"."auth_type" IS '认证方式id';
ALTER TABLE "MWDEVSQL"."mw_sys_log_datasource_setting"
    ADD ("status" NUMBER(2,0));
COMMENT ON COLUMN "MWDEVSQL"."mw_sys_log_datasource_setting"."status" IS '数据源运行状态 1启动 0 关闭';
ALTER TABLE "MWDEVSQL"."mw_sys_log_datasource_setting"
    MODIFY ("id" NUMBER(20,0));

#zah 2023/4/18 mw_sys_log_datasource_connection表新增数据源类型id字段并更改数据
ALTER TABLE "MWDEVSQL"."mw_sys_log_datasource_connection"
    ADD ("datasource_type_id" NUMBER(6,0));
COMMENT ON COLUMN "MWDEVSQL"."mw_sys_log_datasource_connection"."datasource_type_id" IS '数据源类型id';
UPDATE "MWDEVSQL"."mw_sys_log_datasource_connection" SET "datasource_type_id" = 1;
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_connection" ("id", "connection_type", "connection_name", "datasource_type_id") VALUES (3, 3,"Receiver", 2);
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_connection" ("id", "connection_type", "connection_name", "datasource_type_id") VALUES (4, 4, "Driect", 2);

#zah 2023/4/18	新建认证方式码表mw_sys_log_datasource_auth并插入数据
CREATE TABLE "MWDEVSQL"."mw_sys_log_datasource_auth"
(	"id" NUMBER(11,0) NOT NULL ENABLE,
     "auth_type" NUMBER(11,0),
     "auth_name" NVARCHAR2(255),
     "datasource_type_id" NUMBER(11,0),
     PRIMARY KEY ("id")
         USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
         STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
         PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
         BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
         TABLESPACE "MONITOR_DAT"  ENABLE
) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_auth" ("id", "auth_type", "auth_name", "datasource_type_id") VALUES ('1', '1', 'SSL', '2');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_auth" ("id", "auth_type", "auth_name", "datasource_type_id") VALUES ('2', '2', 'SASL/GSAPI', '2');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_auth" ("id", "auth_type", "auth_name", "datasource_type_id") VALUES ('3', '3', 'SASL/PLAIN', '2');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_auth" ("id", "auth_type", "auth_name", "datasource_type_id") VALUES ('4', '4', 'SASL/SCRAM', '2');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_auth" ("id", "auth_type", "auth_name", "datasource_type_id") VALUES ('5', '5', 'SASL/OAUTHBEARER', '2');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_auth" ("id", "auth_type", "auth_name", "datasource_type_id") VALUES ('6', '6', 'Delegation Token', '2');

#zah 2023/4/18	新建topic字段码表mw_alert_field_code并插入数据
CREATE TABLE "MWDEVSQL"."mw_alert_field_code"
(	"id" NUMBER(11,0) NOT NULL ENABLE,
     "code" NVARCHAR2(255),
     "name" NVARCHAR2(255),
     "is_march_model" NUMBER(4,0),
     "model_table_name" NVARCHAR2(255),
     "input_type" NVARCHAR2(255),
     "is_used" NUMBER(4,0),
     PRIMARY KEY ("id")
         USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
         STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
         PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
         BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
         TABLESPACE "MONITOR_DAT"  ENABLE
) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('1', 'eventTitle', '事件标题', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('2', 'hostId', '主机ID', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('3', 'hostName', '主机名称', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('4', 'hostIp', '主机IP', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('5', 'alarmLevel', '严重级别', '1', 'mw_alert_level_code', 'tag', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('6', 'eventContent', '事件内容', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('7', 'eventTime', '事件时间', '1', 'mw_event_time_moule_code', 'select', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('8', 'eventId', '事件ID', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('9', 'currentState', '当前状态', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('10', 'business', '业务', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('11', 'targetType', '指标类型', '0', NULL, 'input', '1');
INSERT INTO "MWDEVSQL"."mw_alert_field_code" ("id", "code", "name", "is_march_model", "model_table_name", "input_type", "is_used") VALUES ('12', 'targetName', '指标名', '0', NULL, 'input', '1');

#zah 2023/4/18	新建时间模式码表mw_event_time_moule_code并插入数据
CREATE TABLE "MWDEVSQL"."mw_event_time_moule_code"
(	"id" NUMBER(11,0) NOT NULL ENABLE,
     "code" NUMBER(11,0),
     "name" NVARCHAR2(255),
     PRIMARY KEY ("id")
         USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
         STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
         PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
         BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
         TABLESPACE "MONITOR_DAT"  ENABLE
) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_event_time_moule_code" ("id", "code", "name") VALUES ('1', '1', 'yyyy-MM-dd HH:mm:ss');

#zah 2023/4/18	新建告警等级码表mw_alert_level_code并插入数据
CREATE TABLE "MWDEVSQL"."mw_alert_level_code"
(	"id" NUMBER(11,0) NOT NULL ENABLE,
     "code" NVARCHAR2(255),
     "name" NVARCHAR2(255),
     PRIMARY KEY ("id")
         USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
         STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
         PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
         BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
         TABLESPACE "MONITOR_DAT"  ENABLE
) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_alert_level_code" ("id", "code", "name") VALUES ('1', 'info', '信息');
INSERT INTO "MWDEVSQL"."mw_alert_level_code" ("id", "code", "name") VALUES ('2', 'warn', '警告');
INSERT INTO "MWDEVSQL"."mw_alert_level_code" ("id", "code", "name") VALUES ('3', 'general', '一般');
INSERT INTO "MWDEVSQL"."mw_alert_level_code" ("id", "code", "name") VALUES ('4', 'serious', '严重');
INSERT INTO "MWDEVSQL"."mw_alert_level_code" ("id", "code", "name") VALUES ('5', 'urgent', '紧急');

#zah 2023/4/18	新建topic信息表mw_kafka_topic_info
CREATE TABLE "MWDEVSQL"."mw_kafka_topic_info"
(	"id" NUMBER(20,0) NOT NULL ENABLE,
     "topic_code" NVARCHAR2(255),
     "topic_name" NVARCHAR2(255),
     "field_code" NVARCHAR2(255),
     "field_name" NVARCHAR2(255),
     "kafka_field_mapping" NVARCHAR2(255),
     "mapping_rule" NVARCHAR2(255),
     "kafka_id" NUMBER(20,0),
     "is_delete" NUMBER(4,0),
     PRIMARY KEY ("id")
         USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
         STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
         PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
         BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
         TABLESPACE "MONITOR_DAT"  ENABLE
) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "MONITOR_DAT";





#过期提醒定时任务sql
INSERT INTO "mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES (19, 11, '定时任务触发过期提醒', 'cn.mw.monitor.model.service.impl.MwModelInstanceServiceImplV1', 'getTimeOutInfoByTimeTask', 0);
INSERT INTO "mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES (19, 11, 19);


#gengjb 2023-04-23 增加可视化组件区查询接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (886, '/mwapi/visualized/module/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (893, '/mwapi/visualized/module/business/browse');

#gengjb 2023-04-23 增加可视化组件
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('33', NULL, '组件区', NULL, NULL, 'admin', TO_DATE('2023-02-15 14:15:44', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'assembly');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('37', '33', '中控机柜模型', 'serverRoom_icon.svg', 'serverRoom_drag.png', 'admin', TO_DATE('2023-03-07 11:59:41', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'serverRoom');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('38', '33', '中控运行时间', 'runTime_icon.svg', 'zkRunTime_drag.png', 'admin', TO_DATE('2023-03-07 12:00:27', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'zkyxsj');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('39', '33', '中控统计信息', 'census_icon.svg', 'zkCensus_drag.png', 'admin', TO_DATE('2023-03-07 12:01:10', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'zktj');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('40', '33', '中控机柜监测', 'rockMonitor_icon.svg', 'rockMonitor_drag.png', 'admin', TO_DATE('2023-03-07 12:02:38', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'zkjc');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('41', '33', '中控告警', 'zkAlert_icon.svg', 'zkAlert_drag.png', 'admin', TO_DATE('2023-03-07 12:03:18', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'alert');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('42', '33', '中控运行数据', 'runData_icon.svg', 'zkRundata_drag.png', 'admin', TO_DATE('2023-03-09 09:19:43', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'runData');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('44', '33', '中控时间', 'time_icon.svg', 'zkTime_drag.png', 'admin', TO_DATE('2023-03-10 22:41:52', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'zktime');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('45', '33', '中控配电柜', 'serverRoom_icon.svg', 'zkDistribution_drag.png', 'admin', TO_DATE('2023-03-13 14:38:13', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'zkpdg');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('46', '33', '告警播报', 'speak.svg', 'speak.svg', 'admin', TO_DATE('2023-03-28 10:02:04', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'speak');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('47', '33', '告警分类状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'admin', TO_DATE('2023-04-18 10:24:05', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'alertClassify');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('48', '33', '告警趋势', 'alertTrend_icon.svg', 'alertTrend_drag.png', 'admin', TO_DATE('2023-04-18 10:25:42', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'alertTrend');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('49', '33', '告警列表', 'alertList_icon.svg', 'alertList_drag.png', 'admin', TO_DATE('2023-04-18 15:37:15', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'alertList');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('50', '33', '业务可用性', 'usability_icon.svg', 'usability_drag.png', 'admin', TO_DATE('2023-04-18 15:38:12', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'businessUsab');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('51', '33', '健康数据', 'healthInfo_icon.svg', 'healthInfo_drag.png', 'admin', TO_DATE('2023-04-19 10:28:28', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'healthInfo');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('52', '33', '健康状态', 'healthStatus_icon.svg', 'healthStatus_drag.png', 'admin', TO_DATE('2023-04-19 10:30:08', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'healthStatus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('53', '33', '拓扑', 'topology_icon.svg', 'topology_drag.png', 'admin', TO_DATE('2023-04-19 14:27:18', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'topology');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('58', '33', 'VDI资源负载', 'VDI_icon.svg', 'VDI_drag.png', 'admin', TO_DATE('2023-04-24 15:27:24', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'vdiLoad');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('59', '33', 'Session统计', 'sessionStat_icon.svg', 'sessionStat_drag.png', 'admin', TO_DATE('2023-04-24 15:29:36', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'sessionCensus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('54', '33', '业务分区', 'businessSys_icon.svg', 'businessSys_drag.png', 'admin', TO_DATE('2023-04-19 14:53:38', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'businessPartition');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('55', '33', '内存排行', 'memory_icon.svg', 'memory_drag.png', 'admin', TO_DATE('2023-04-20 08:56:34', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'memoryTopN');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('56', '33', 'CPU排行', 'cpu_icon.svg', 'cpu_drag.png', 'admin', TO_DATE('2023-04-20 08:57:29', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'cpuTopN');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('57', '33', '表空间排行', 'dbSpace_icon.svg', 'dbSpace_drag.png', 'admin', TO_DATE('2023-04-20 08:58:17', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'tbsSpace');

#gqw 创建LDAP用户经理信息表
CREATE TABLE "mw_ldap_user_manager_rel" (
  "user_id" NUMBER(11,0) NOT NULL,
  "manager_content" VARCHAR2(1024) NOT NULL,
  PRIMARY KEY ("user_id")
)
#gengjb 2023-04-26 首页定时任务增加到定时任务页面执行
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('20', '9', '首页TopN缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheTopNData', NULL);
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('21', '9', '首页流量缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheInterfaceFlowData', NULL);
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('22', '9', '首页带宽缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheFlowBandWidthData', NULL);
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('20', '9', '20');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('21', '9', '21');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('22', '9', '22');

#lbq 23/4/26 告警动作定时任务
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('23', '13', '23');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('24', '13', '24');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('25', '13', '25');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('26', '13', '26');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('27', '13', '27');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('28', '13', '28');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('29', '13', '29');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_model"("id", "model_name", "model_type") VALUES ('13', '告警动作', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('23', '13', '告警压缩', 'cn.mw.time.MWZbxTime', 'alarmCompression', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('24', '13', '系统日志发送', 'cn.mw.time.MWZbxTime', 'sendSyslog', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('25', '13', 'zabbix服务监控', 'cn.mw.time.MWZbxTime', 'sendZabbixService', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('26', '13', '分级告警', 'cn.mw.time.MWZbxAlertLevelTime', 'levelAlert', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('27', '13', '当前告警', 'cn.mw.time.MWZbxAlertNowTime', 'saveAlertGetNow', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('28', '13', '历史告警', 'cn.mw.time.MWZbxAlertNowTime', 'saveAlertGetHist', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('29', '13', '华星SQL告警', 'cn.mw.time.MWZbxHuaXingAlertTime', 'sendHuaXingAlert', '0');

#qzg 0427
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (905, '/mwapi/modelView/editorModelFieldToEs/editor');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (918, '/mwapi/esDataRefresh/create');


#qzg 0428 资源中心维护计划
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (10497, 274, 'name', '名称', 1, 1, NULL, NULL);
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (10498, 274, 'maintenanceType', '类型', 1, 2, NULL, NULL);
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (10499, 274, 'activeSince', '启用自从', 1, 3, NULL, NULL);
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (10500, 274, 'activeTill', '启用直到', 1, 4, NULL, NULL);
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (10501, 274, 'status', '状态', 1, 5, NULL, NULL);
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (10502, 274, 'description', '描述', 1, 6, NULL, NULL);

#gqw 20230428 增加资源中心维护计划模块
INSERT INTO "mw_module"("id", "pid", "module_name", "module_desc", "url", "is_node", "deep", "nodes", "enable", "version", "delete_flag", "node_protocol") VALUES
 (274, 216, 'asset-maintenance', '维护计划', '/mwapi/modelAssets/mainTain', 1, 2, '216,274', 1, NULL, 0, NULL);

# 增加增删改的权限
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 274, 1);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 274, 2);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 274, 3);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 274, 4);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 274, 5);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 274, 6);

INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-274-browse', 0, 274, 'browse', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-274-create', 0, 274, 'create', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-274-delete', 0, 274, 'delete', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-274-editor', 0, 274, 'editor', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-274-perform', 0, 274, 'perform', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-274-secopassword', 0, 274, 'secopassword', 1);
#qzg 0428 资源中心维护计划
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (906, '/mwapi/modelAssets/mainTain/groupdropdown/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (907, '/mwapi/modelAssets/mainTain/hostdropdown/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (908, '/mwapi/modelAssets/mainTain/getAssetsDifficulty');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (909, '/mwapi/modelAssets/mainTain/fuzzQuery');


#lumingming
CREATE TABLE "MWDEVSQL"."mw_homework_alert" (
  "id" NUMBER(11,0) VISIBLE NOT NULL,
  "alert_plan_name" NVARCHAR2(255) VISIBLE,
  "alert_title" NVARCHAR2(255) VISIBLE,
  "alert_level" NUMBER(11,0) VISIBLE,
  "alert_exe_homework" NVARCHAR2(255) VISIBLE
);
COMMENT ON COLUMN "MWDEVSQL"."mw_homework_alert"."alert_plan_name" IS '告警触发的名称';
COMMENT ON COLUMN "MWDEVSQL"."mw_homework_alert"."alert_title" IS '告警标题';
COMMENT ON COLUMN "MWDEVSQL"."mw_homework_alert"."alert_level" IS '告警等级';
COMMENT ON COLUMN "MWDEVSQL"."mw_homework_alert"."alert_exe_homework" IS '告警触发作业';

-- ----------------------------
-- Primary Key structure for table mw_homework_alert
-- ----------------------------
ALTER TABLE "MWDEVSQL"."mw_homework_alert" ADD CONSTRAINT "SYS_C0015816" PRIMARY KEY ("id");

-- ----------------------------
-- Checks structure for table mw_homework_alert
-- ----------------------------
ALTER TABLE "MWDEVSQL"."mw_homework_alert" ADD CONSTRAINT "SYS_C0015815" CHECK ("id" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;

#20230508 lijubo 新增拓扑信息表
create table "MWDEVSQL"."mw_cmdbmd_instance_topo_info"
(
    "id"               NVARCHAR2(255),
    "instance_id"      NUMBER(11),
    "instance_view_id" NUMBER(24),
    "topo_info"        CLOB
);
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance_topo_info"."id" IS 'ID';
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance_topo_info"."instance_id" IS '实例id';
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance_topo_info"."instance_view_id" IS '实例拓扑视图id';
COMMENT ON COLUMN "MWDEVSQL"."mw_cmdbmd_instance_topo_info"."topo_info" IS '实例拓扑信息';

#lumingming
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (895, '/mwapi/modelRancher/getRancherDeviceTree/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (896, '/mwapi/modelRancher/getRancherList/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (897, '/mwapi/user/getAuthUserList');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (898, '/mwapi/user/changeUserAuth');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (899, '/mwapi/modelRancher/getRancherPerUser/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (900, '/mwapi/modelRancher/setRancherPerUser/create');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (901, '/mwapi/configManage/createVariable');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (902, '/mwapi/configManage/getVariable');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (903, '/mwapi/modelView/open/citrixList/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (904, '/mwapi/script-manage/account-manage/removeAssets');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (905, '/mwapi/modelView/editorModelFieldToEs/editor');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (906, '/mwapi/modelAssets/mainTain/groupdropdown/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (907, '/mwapi/modelAssets/mainTain/hostdropdown/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (908, '/mwapi/modelAssets/mainTain/getAssetsDifficulty');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (909, '/mwapi/modelAssets/mainTain/fuzzQuery');
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
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (920, '/mwapi/configManage/browselistAssets');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (921, '/mwapi/setModelAreaDataToEs/editor');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (922, '/mwapi/script-manage/alert/homeworkList');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (923, '/mwapi/getInstanceIdByLinkRelation/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (924, '/mwapi/script-manage/getAnsibleIP');

#20230510 gengjb 可视化组件区增加播报组件
INSERT INTO "mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('63', '33', '测试播报', 'broadCast.svg', 'broadCast.svg', 'gengjb', TO_DATE('2023-05-10 11:30:19', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'testBroadCast');

#2023510 gengjb 增加接口Url
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('925', '/mwapi/server/getAssetsStatus/browse');

#23/5/11
CREATE TABLE "MWDEVSQL"."mw_huaxing_alert_table" (
  "starts_at" VARCHAR2(255 BYTE) VISIBLE,
  "ends_at" VARCHAR2(255 BYTE) VISIBLE,
  "status" VARCHAR2(255 BYTE) VISIBLE,
  "severity" VARCHAR2(255 BYTE) VISIBLE,
  "alert_name" VARCHAR2(255 BYTE) VISIBLE,
  "duration" VARCHAR2(255 BYTE) VISIBLE,
  "project_name" VARCHAR2(255 BYTE) VISIBLE,
  "ip" VARCHAR2(255 BYTE) VISIBLE
)
TABLESPACE "MONITOR_DAT";

#23/5/11 lbq 表头更新
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('10503', '273', 'error', '错误原因', '1', '7', NULL, NULL);

#20230511 gengjb 资产发现成功列表增加OID信息展示
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('10510', '75', 'sysObjId', 'OID信息', '1', '7', NULL, NULL);

#zah 20230511 更改字段属性
ALTER TABLE "MWDEVSQL"."mw_sys_log_datasource_setting"
    MODIFY ("id" NVARCHAR2(20));
ALTER TABLE "MWDEVSQL"."mw_kafka_topic_info"
    MODIFY ("id" NVARCHAR2(20))
    MODIFY ("kafka_id" NVARCHAR2(20))
    ADD ("is_consume_last" NUMBER(4,0));

COMMENT ON COLUMN "MWDEVSQL"."mw_kafka_topic_info"."is_consume_last" IS '是否从队列最新处消费（1是0否（从队列开始处消费））';

#20230515 gengjb 维护计划增加资源中心支持字段
ALTER TABLE "mw_assets_mainten_host"
ADD ("model_instance_id" NVARCHAR2(255));
COMMENT ON COLUMN "mw_assets_mainten_host"."model_instance_id" IS '模型实例ID';
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('926', '/mwapi/assets/mainTain/getHostInfo');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('927', '/mwapi/assets/mainTain/getModelListInfo');

#20230517 qzg 新增web监测模型
INSERT INTO "mw_cmdbmd_manage"("model_id", "model_name", "model_desc", "model_index", "model_type_id", "model_level", "group_nodes", "model_group_id", "model_group_sub_id", "model_view", "model_icon", "nodes", "is_show","deep", "pid", "is_node", "creator", "create_date", "modifier", "modification_date", "delete_flag", "pids", "is_show_menu", "is_hide_model", "is_full_text_search", "is_ignore_change_log", "prop_info") VALUES (72, 'WEB监测', 'WEB监测', 'mw_2d4ce194957945b29b7e79ed327d147f', 1, 0, ',0,11,', 11, NULL, 0, 'mw', NULL, 1, 1, 0, NULL, 'qxx', '2023-05-17 14:46:45', 'qxx', '2023-05-17 14:46:45', 0, '', NULL, NULL, NULL, NULL, '[{\"indexId\":\"instanceName\",\"isEditorShow\":true,\"isInsertShow\":true,\"isListShow\":true,\"isLookShow\":true,\"isMust\":true,\"isOnly\":false,\"isRead\":false,\"isShow\":true,\"propertiesLevel\":0,\"propertiesName\":\"名称\",\"propertiesType\":\"默认属性\",\"propertiesTypeId\":1,\"sort\":0},{\"indexId\":\"operationMonitor\",\"isEditorShow\":true,\"isInsertShow\":true,\"isListShow\":false,\"isLookShow\":true,\"isMust\":false,\"isOnly\":false,\"isRead\":false,\"isShow\":false,\"propertiesLevel\":0,\"propertiesName\":\"运维监控\",\"propertiesType\":\"默认属性\",\"propertiesTypeId\":17,\"sort\":1},{\"indexId\":\"autoManage\",\"isEditorShow\":true,\"isInsertShow\":true,\"isListShow\":false,\"isLookShow\":true,\"isMust\":false,\"isOnly\":false,\"isRead\":false,\"isShow\":false,\"propertiesLevel\":0,\"propertiesName\":\"自动化\",\"propertiesType\":\"默认属性\",\"propertiesTypeId\":17,\"sort\":2},{\"indexId\":\"logManage\",\"isEditorShow\":true,\"isInsertShow\":true,\"isListShow\":false,\"isLookShow\":true,\"isMust\":false,\"isOnly\":false,\"isRead\":false,\"isShow\":false,\"propertiesLevel\":0,\"propertiesName\":\"日志管理\",\"propertiesType\":\"默认属性\",\"propertiesTypeId\":17,\"sort\":3},{\"indexId\":\"propManage\",\"isEditorShow\":true,\"isInsertShow\":true,\"isListShow\":false,\"isLookShow\":true,\"isMust\":false,\"isOnly\":false,\"isRead\":false,\"isShow\":false,\"propertiesLevel\":0,\"propertiesName\":\"配置管理\",\"propertiesType\":\"默认属性\",\"propertiesTypeId\":17,\"sort\":4}]');
INSERT INTO "mw_cmdbmd_pagefield_table"("id", "model_id", "prop", "label", "visible", "order_number", "type", "is_tree", "model_properties_id") VALUES (547, 72, 'instanceName', '名称', 1, 0, 1, NULL, 1);
INSERT INTO "mw_cmdbmd_pagefield_table"("id", "model_id", "prop", "label", "visible", "order_number", "type", "is_tree", "model_properties_id") VALUES (548, 72, 'operationMonitor', '运维监控', 0, 1, 17, NULL, 2);
INSERT INTO "mw_cmdbmd_pagefield_table"("id", "model_id", "prop", "label", "visible", "order_number", "type", "is_tree", "model_properties_id") VALUES (549, 72, 'autoManage', '自动化', 0, 2, 17, NULL, 3);
INSERT INTO "mw_cmdbmd_pagefield_table"("id", "model_id", "prop", "label", "visible", "order_number", "type", "is_tree", "model_properties_id") VALUES (550, 72, 'logManage', '日志管理', 0, 3, 17, NULL, 4);
INSERT INTO "mw_cmdbmd_pagefield_table"("id", "model_id", "prop", "label", "visible", "order_number", "type", "is_tree", "model_properties_id") VALUES (551, 72, 'propManage', '配置管理', 0, 4, 17, NULL, 5);

#23/5/19 lbq T5BC
INSERT INTO "MWDEVSQL"."mw_alert_action_type"("id", "action_type") VALUES ('20', '华星光电T5BC');
UPDATE "MWDEVSQL"."mw_alert_action_type" SET "id" = '18', "action_type" = 'T信群机器人' WHERE "id" = '18';


#gqw 20230516 增加拓扑管理根据资产标签创建拓扑
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (930, '/mwapi/topology/getSimplifyLabel');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (931, '/mwapi/topology/getAssetsLabelEnable');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (933, '/mwapi/topology/assetsLabel/browse');

#gqw 20230522 删除拓扑标签的脏数据
delete from "mw_label_mapper" where "type_id" not in (
    SELECT  "id"  from "mw_topo_graph")  and "module_type" = 'TOPO_GRAPH';
delete from "mw_label_drop_mapper" where "type_id" not in (
    SELECT  "id"  from "mw_topo_graph")  and "module_type" = 'TOPO_GRAPH';
delete from "mw_label_date_mapper" where "type_id" not in (
    SELECT  "id"  from "mw_topo_graph")  and "module_type" = 'TOPO_GRAPH';


#2023/05/22 gengjb 可视化新增缓存表
CREATE TABLE "MWDEVSQL"."mw_visualized_cache_assets_table" (
  "id" NVARCHAR2(128) VISIBLE,
  "host_name" NVARCHAR2(128) VISIBLE,
  "host_id" NVARCHAR2(128) VISIBLE,
  "host_group_id" NVARCHAR2(128) VISIBLE,
  "host_group_name" NVARCHAR2(128) VISIBLE,
  "host_status" NVARCHAR2(128) VISIBLE,
  "creator" NVARCHAR2(128) VISIBLE,
  "create_date" DATE VISIBLE
)
TABLESPACE "MONITOR_DAT";

CREATE TABLE "MWDEVSQL"."mw_visualized_cache_history_table" (
  "id" NVARCHAR2(128) VISIBLE,
  "assets_id" NVARCHAR2(128) VISIBLE,
  "assets_name" NVARCHAR2(128) VISIBLE,
  "host_id" NVARCHAR2(128) VISIBLE,
  "item_name" NVARCHAR2(128) VISIBLE,
  "avg_value" NVARCHAR2(128) VISIBLE,
  "max_value" NVARCHAR2(128) VISIBLE,
  "min_value" NVARCHAR2(128) VISIBLE,
  "units" NVARCHAR2(128) VISIBLE,
  "clock" NVARCHAR2(128) VISIBLE,
  "creator" NVARCHAR2(128) VISIBLE,
  "create_date" DATE VISIBLE
)
TABLESPACE "MONITOR_DAT";

CREATE TABLE "MWDEVSQL"."mw_visualized_cache_table" (
  "cache_id" NVARCHAR2(128) VISIBLE NOT NULL,
  "assets_id" NVARCHAR2(128) VISIBLE,
  "assets_name" NVARCHAR2(128) VISIBLE,
  "host_id" NVARCHAR2(128) VISIBLE,
  "item_name" NVARCHAR2(128) VISIBLE,
  "value" NVARCHAR2(128) VISIBLE,
  "units" NVARCHAR2(128) VISIBLE,
  "clock" NVARCHAR2(128) VISIBLE,
  "creator" NVARCHAR2(128) VISIBLE,
  "create_date" DATE VISIBLE,
  "modifier" NVARCHAR2(128) VISIBLE,
  "modification_date" DATE VISIBLE
)
TABLESPACE "MONITOR_DAT";

CREATE TABLE "MWDEVSQL"."mw_visualized_drop_down_table" (
  "id" NVARCHAR2(128) VISIBLE NOT NULL,
  "drop_value" NVARCHAR2(128) VISIBLE,
  "type" NUMBER(11,0) VISIBLE,
  "item_name" NVARCHAR2(255) VISIBLE
)
TABLESPACE "MONITOR_DAT";
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('1', '集群内存使用率', 1, '');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('10', '磁盘平均使用率', 3, 'MW_DISK_UTILIZATION');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('11', 'HDFS使用率', 3, 'NameNode: Percent capacity remaining');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('12', '系统处理状态', 5, 'CPU_UTILIZATION');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('13', '文件处理状态', 5, 'CPU_UTILIZATION');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('14', '文件处理时长', 5, 'CPU_UTILIZATION');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('15', 'APP活跃数', 4, 'ResourceManager: App runnings');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('16', 'HDFS读写IO', 4, 'NameNode: Total load');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('17', 'RPC队列和处理时间', 4, 'ResourceManager: RPC queue & processing time');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('18', '集群流量', 4, 'ResourceManager ReceivedBytes,ResourceManager SentBytes');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('19', '内存使用情况', 4, 'ResourceManager MemoryPool Used');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('2', '集群CPU使用率', 1, NULL);
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('20', '文件输入错误总数', 4, 'TotalFileIoErrors');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('21', '活动源数量', 4, 'NumActiveSources');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('22', '节点内存排行', 6, 'Used Memory UTILIZATION');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('23', 'JVM堆使用', 6, 'JVM Heap usage');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('24', '数据节点CPU负载', 6, 'SystemCpuLoad');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('3', '集群PODS使用率', 1, NULL);
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('4', '在线机器', 2, 'ResourceManager: Active NMs');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('5', '离线机器', 2, 'ResourceManager: Unhealthy NMs');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('6', 'HDFS总量', 2, 'NameNode: Capacity remaining,NameNode: Percent capacity remaining');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('7', 'HDFS使用量', 2, 'NameNode: Capacity remaining');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('8', 'CPU平均使用率', 3, 'CPU_UTILIZATION');
INSERT INTO `monitor`.`mw_visualized_drop_down_table`(`id`, `drop_value`, `type`, `item_name`) VALUES ('9', '内存平均使用率', 3, 'MEMORY_UTILIZATION');

INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('64', '33', '类型统计', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-18 10:27:43', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'typeStatistics');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('65', '33', '运营状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-19 14:07:35', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'operateStatus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('66', '33', '资产趋势', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-19 14:08:27', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'assetsTrend');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('67', '33', 'agent状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-19 14:09:12', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'agentStatus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('68', '33', '资产一览', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-19 14:10:10', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'assetsOverview');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('69', '33', '主机资源', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-19 14:11:23', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'hostResources');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('70', '33', '资产分布', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-19 14:12:31', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'assetsSpread');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('71', '33', '业务状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-20 11:54:37', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'businessStatus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('72', '33', '集群概览', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 11:32:17', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'colonyOverview');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('73', '33', '集群使用', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 15:59:33', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'colonyUse');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('74', '33', '集群排行', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 16:01:02', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'colonyRanking');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('75', '33', '机器统计', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 16:03:20', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'machineStatis');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('76', '33', '机器使用情况', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 16:04:36', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'machineUse');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('77', '33', '机器排行', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 16:05:43', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'machineRanking');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('78', '33', '机器运行状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-21 16:07:08', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'machineRunStatus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('79', '33', '处理状态', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-22 15:26:50', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'dealWithStatus');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('80', '33', '处理时长', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-22 15:28:14', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'dealWithDuration');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('81', '33', '收发包统计', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:01:28', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'wrapStatis');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('82', '33', 'total连接数', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:03:36', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'totalConnectNumber');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('84', '33', '广域网', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:48:26', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'wideAreaNetWorl');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('83', '33', '虚拟存储', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:04:45', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'virtuallySorage');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('85', '33', '互联网', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:49:32', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'internet');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('86', '33', '存储访问量排行', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:50:39', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'storageAccess');
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('87', '33', '防火墙负载', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-05-23 10:51:44', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'fireWall');


#20230523 gengjb 可视化定时任务
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_model"("id", "model_name", "model_type") VALUES ('14', '可视化', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('30', '14', '可视化存储历史趋势数据', 'cn.mw.monitor.visualized.time.MwVisualizedModuleHistoryTime', 'getHistoryInfo', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('31', '14', '可视化获取监控项最新数据', 'cn.mw.monitor.visualized.time.MwVisualizedModuleTime', 'visualizedCacheInfo', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('32', '14', '可视化获取外部资产状态', 'cn.mw.monitor.visualized.time.MwVisualizedObtainDataTime', 'getZabbixHostInfoCache', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('30', '14', '30');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('31', '14', '31');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('32', '14', '32');

#20230523 gengjb 可视化缓存数据监控项
CREATE TABLE "MWDEVSQL"."mw_visualized_cache_item_table" (
  "id" NVARCHAR2(128) VISIBLE NOT NULL,
  "item_name" NVARCHAR2(128) VISIBLE,
  "type" NUMBER(11,0) VISIBLE
)
TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('1', 'CPU_UTILIZATION', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('10', 'NameNode: Percent capacity remaining', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('11', 'NameNode: Capacity remaining', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('12', 'ResourceManager: Active NMs', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('13', 'ResourceManager: Unhealthy NMs', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('14', 'Used Memory UTILIZATION', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('15', 'JVM Heap usage', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('16', 'SystemLoadAverage', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('17', 'TotalFileIoErrors', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('18', 'NumActiveSources', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('19', 'ResourceManager: App runnings', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('2', 'MEMORY_UTILIZATION', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('20', 'NameNode: Total load', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('21', 'ResourceManager: RPC queue & processing time', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('22', 'ResourceManager ReceivedBytes', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('23', 'ResourceManager SentBytes', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('24', 'ResourceManager MemoryPool Used', '2');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('3', 'MW_DISK_UTILIZATION', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('4', 'AGENT_PING_STATUS', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('5', 'ICMP_PING', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('6', 'MW_INTERFACE_OUT_TRAFFIC', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('7', 'MW_INTERFACE_IN_TRAFFIC', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('8', 'ICMP_RESPONSE_TIME', '1');
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table" VALUES ('9', 'ICMP_LOSS', '1');

#gengjb 2023/05/25 可视化外部资产缓存表增加字段
ALTER TABLE "MWDEVSQL"."mw_visualized_cache_assets_table"
ADD ("server_id" NUMBER(10,0))
ADD ("server_name" NVARCHAR2(255));
COMMENT ON COLUMN "MWDEVSQL"."mw_visualized_cache_assets_table"."server_id" IS '外部资产服务器ID';
COMMENT ON COLUMN "MWDEVSQL"."mw_visualized_cache_assets_table"."server_name" IS '外部资产服务器名称';


#20230601 qzg 资产接口信息表新增字段
ALTER TABLE "mw_cmdbmd_assets_interface"
ADD ("is_show" NUMBER(4,0));
COMMENT ON COLUMN "mw_cmdbmd_assets_interface"."is_show" IS '页面是否显示(默认显示全部)';

#20230602 qzg notcheck新增接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('949', '/mwapi/modelMonitor/getInterface/isOpen');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('950', '/mwapi/modelMonitor/refreshInterfaceInfo/perform');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('951', '/mwapi/modelMonitor/getNetDataList/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('952', '/mwapi/modelMonitor/getInterfaceInfo/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('953', '/mwapi/modelMonitor/getNetDetail/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('954', '/mwapi/modelMonitor/updateInterfaceDesc/editor');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('955', '/mwapi/modelMonitor/batchUpdateShow/editor');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('956', '/mwapi/modelMonitor/geInterfaceTypeInfo/browse');



#lbq 23/6/2 notcheck表更新
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('957', '/mwapi/alert/close/trigger');

#gengjb 23/6/6 可视化增加告警数据记录缓存
CREATE TABLE "MWDEVSQL"."mw_visualized_alert_cache_table" (
  "cache_id" NVARCHAR2(128) VISIBLE NOT NULL,
  "assets_id" NVARCHAR2(128) VISIBLE,
  "host_id" NVARCHAR2(128) VISIBLE,
  "alert_severity" NVARCHAR2(128) VISIBLE,
  "time" NVARCHAR2(128) VISIBLE
)
TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('33', '12', '可视化获取每天告警记录', 'cn.mw.monitor.visualized.time.MwVisualizedAlertRecordTime', 'getCurrAlertRecord', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('33', '14', '33');



#20230601 qzg 资产接口信息表新增字段
ALTER TABLE "mw_cmdbmd_assets_interface"
ADD ("alert_tag" NUMBER(4,0));
COMMENT ON COLUMN "mw_cmdbmd_assets_interface"."alert_tag" IS '是否接口告警';
ALTER TABLE "mw_cmdbmd_assets_interface"
ADD ("host_id" NVARCHAR2(255));
COMMENT ON COLUMN "mw_cmdbmd_assets_interface"."host_id" IS '主机HostId';
ALTER TABLE "mw_cmdbmd_assets_interface"
ADD ("host_ip" NVARCHAR2(255));
COMMENT ON COLUMN "mw_cmdbmd_assets_interface"."host_ip" IS '主机Ip';

#20230607 gengjb 可视化容器大屏数据存储
INSERT INTO "MWDEVSQL"."mw_visualized_chart_table"("id", "parent_id", "partition_name", "icon_url", "drag_url", "creator", "create_date", "modifier", "modification_date", "delete_flag", "sign", "partition_eng") VALUES ('88', '33', '容器分区', 'assetsNumber_icon.svg', 'assetsNumber_drag.png', 'gengjb', TO_DATE('2023-06-07 18:56:20', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, '0', NULL, 'containe');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('928', '/mwapi/visualized/containe/dropdown/browse');

CREATE TABLE "MWDEVSQL"."mw_visualized_container_item_table" (
  "id" NVARCHAR2(128) VISIBLE NOT NULL,
  "desc" NVARCHAR2(128) VISIBLE,
  "url" NVARCHAR2(128) VISIBLE,
  "partition_name" NVARCHAR2(128) VISIBLE,
  "param" NVARCHAR2(256) VISIBLE,
  "units" NVARCHAR2(128) VISIBLE,
  "item_name" NVARCHAR2(255) VISIBLE
)
TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('1', '节点总数', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=sum(kube_node_info{instance="10.42.220.123:8080"})', '个', 'kube_node_info');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('10', 'Pod入网流量', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=sum by(pod) (rate(container_network_receive_bytes_total{instance=~"172.27.*"}[5m])) /1024 /1024', 'Mbps', 'container_network_receive_bytes_total');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('11', 'Pod出网流量', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=sum by(pod) (rate(container_network_transmit_bytes_total{instance=~"172.27.*"}[5m])) /1024 /1024', 'Mbps', 'container_network_transmit_bytes_total');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('12', '节点总数', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=sum(kube_node_info{instance="10.42.6.202:8080"})', '个', 'kube_node_info');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('13', '不可用节点数', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=count(max((1 - (node_memory_MemAvailable_bytes{instance=~"10.109.*"} / (node_memory_MemTotal_bytes{instance=~"10.109.*"}))) * 100 > 85 or(100 - avg(irate(node_cpu_seconds_total{mode="idle",instance=~"10.109.*"}[5m])) by (node) * 100) > 85) by (node))', '%', 'kube_node_spec_unschedulable');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('14', '节点内存使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=(1 - (node_memory_MemAvailable_bytes{instance=~"10.109.*"} / (node_memory_MemTotal_bytes{instance=~"10.109.*"}))) * 100', '%', 'node_memory_MemAvailable_bytes');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('15', '节点内存平均使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=avg(1 - (node_memory_MemAvailable_bytes{instance=~"10.109.*"} / (node_memory_MemTotal_bytes{instance=~"10.109.*"}))) * 100', '%', 'USAGERATE');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('16', 'Pod总数', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=sum(kube_pod_status_phase{instance=''10.42.6.202:8080'',phase="Running"})', '个', 'kube_pod_status_phase');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('17', '节点cpu平均使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=100 - (avg(irate(node_cpu_seconds_total{mode="idle",instance=~"10.109.*"}[5m])) * 100)', '%', 'USAGERATE');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('18', '节点文件使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=avg(100 * (1 - (node_filesystem_avail_bytes{mountpoint="/",instance=~"10.109.*"} / node_filesystem_size_bytes{mountpoint="/",instance=~"10.109.*"})))', '%', 'USAGERATE');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('19', 'Pod各自cpu使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=100 * sum(rate(container_cpu_usage_seconds_total{instance=~"10.109.*"}[5m])) by (pod) / sum(container_spec_cpu_quota{instance=~"10.109.*"}) by (pod)', '%', 'container_cpu_usage_seconds_total');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('2', '不可用节点数', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=count(max((1 - (node_memory_MemAvailable_bytes{instance=~"172.27.*"} / (node_memory_MemTotal_bytes{instance=~"172.27.*"}))) * 100 > 85 or(100 - avg(irate(node_cpu_seconds_total{mode="idle",instance=~"172.27.*"}[5m])) by (node) * 100) > 85) by (node))', '个', 'kube_node_spec_unschedulable');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('20', 'Pod各自内存使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=100 * sum(container_memory_usage_bytes{instance=~"10.109.*"}) by(pod) / sum(container_spec_memory_limit_bytes{instance=~"10.109.*"}) by(pod)', '%', 'container_memory_usage_bytes');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('21', 'Pod入网流量', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=sum by(pod) (rate(container_network_receive_bytes_total{instance=~"10.109.*"}[5m])) /1024 /1024', 'Mbps', 'container_network_receive_bytes_total');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('22', 'Pod出网流量', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=sum by(pod) (rate(container_network_transmit_bytes_total{instance=~"10.109.*"}[5m])) /1024 /1024', 'Mbps', 'container_network_transmit_bytes_total');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('23', '各自文件使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=100 * (1 - (node_filesystem_avail_bytes{mountpoint="/",instance=~"172.27.*"} / node_filesystem_size_bytes{mountpoint="/",instance=~"172.27.*"}))', '%', 'node_filesystem_avail_bytes');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('24', '各自文件使用率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=100 * (1 - (node_filesystem_avail_bytes{mountpoint="/",instance=~"10.109.*"} / node_filesystem_size_bytes{mountpoint="/",instance=~"10.109.*"})) ', '%', 'node_filesystem_avail_bytes');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('25', 'CPU预留率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=avg(sum(rate(node_cpu_seconds_total{mode="idle",instance=~"172.27.*"}[5m])) by (instance) / count(node_cpu_seconds_total{mode="system",instance=~"172.27.*"}) by(instance))', '%', 'RESERVED');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('26', 'CPU预留率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=avg(sum(rate(node_cpu_seconds_total{mode="idle",instance=~"10.109.*"}[5m])) by (instance) / count(node_cpu_seconds_total{mode="system",instance=~"10.109.*"}) by(instance)) ', '%', 'RESERVED');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('27', '内存预留率', 'http://10.225.14.65:9090/api/v1/query', 'OA', 'query=100 - (avg(1 - (node_memory_MemAvailable_bytes{instance=~"10.109.*"} / (node_memory_MemTotal_bytes{instance=~"10.109.*"}))) * 100)', '%', 'RESERVED');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('28', '内存预留率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=100 - (avg(1 - (node_memory_MemAvailable_bytes{instance=~"172.27.*"} / (node_memory_MemTotal_bytes{instance=~"172.27.*"}))) * 100)', '%', 'RESERVED');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('3', '节点内存使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=(1 - (node_memory_MemAvailable_bytes{instance=~"172.27.*"} / (node_memory_MemTotal_bytes{instance=~"172.27.*"}))) * 100', '%', 'node_memory_MemAvailable_bytes');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('4', '节点内存平均使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=avg(1 - (node_memory_MemAvailable_bytes{instance=~"172.27.*"} / (node_memory_MemTotal_bytes{instance=~"172.27.*"}))) * 100', '%', 'USAGERATE');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('5', 'Pod总数', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=sum(kube_pod_status_phase{instance=''10.42.220.123:8080'',phase="Running"})', '个', 'kube_pod_status_phase');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('6', '节点cpu平均使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=100 - (avg(irate(node_cpu_seconds_total{mode="idle",instance=~"172.27.*"}[5m])) * 100)', '%', 'USAGERATE');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('7', '节点文件使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=avg(100 * (1 - (node_filesystem_avail_bytes{mountpoint="/",instance=~"172.27.*"} / node_filesystem_size_bytes{mountpoint="/",instance=~"172.27.*"})))', '%', 'USAGERATE');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('8', 'Pod各自cpu使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=100 * sum(rate(container_cpu_usage_seconds_total{instance=~"172.27.*"}[5m])) by (pod) / sum(container_spec_cpu_quota{instance=~"172.27.*"}) by (pod)', '%', 'container_cpu_usage_seconds_total');
INSERT INTO "MWDEVSQL"."mw_visualized_container_item_table" VALUES ('9', 'Pod各自内存使用率', 'http://10.225.14.65:9090/api/v1/query', 'FAB', 'query=100 * sum(container_memory_usage_bytes{instance=~"172.27.*"}) by(pod) / sum(container_spec_memory_limit_bytes{instance=~"172.27.*"}) by(pod)', '%', 'container_memory_usage_bytes');

#lbq 23/6/9 发送告警记录表字段更新
ALTER TABLE "MWDEVSQL"."mw_alert_record_table"
    ADD ("eventid" VARCHAR2(255))

#lbq 23/6/9 厂别领域信息
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1683', '271', 'modelClassifyName', '领域', '1', '12', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('1684', '271', 'modelSystemName', '厂别', '1', '13', NULL, NULL);

#lbq 23/6/10 华星恢复告警
ALTER TABLE "MWDEVSQL"."BUSSINESS_ALARM_INFO"
    ADD ("IS_SEND" VARCHAR2(255 BYTE) VISIBLE)

#gengjb 23/06/12 华星增加每日资产记录
CREATE TABLE "MWDEVSQL"."mw_visualized_assets_count_table" (
  "id" NVARCHAR2(128) VISIBLE NOT NULL,
  "partition_name" NVARCHAR2(128) VISIBLE,
  "partition_number" NUMBER(11,0) VISIBLE,
  "time" NVARCHAR2(128) VISIBLE
)
TABLESPACE "MONITOR_DAT";

INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('35', '14', '可视化统计分区资产计数', 'cn.mw.monitor.visualized.time.MwVisualizedAeestsCountTime', 'getPartitionAssetsInfo', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('35', '14', '35');

#lbq 23/6/13
    INSERT INTO "MWDEVSQL"."mw_alert_action_type"("id", "action_type") VALUES ('22', '华星光电工单系统');

#23230614 gengjb 首页资产分组缓存任务
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('35', '9', '首页资产分组缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheNewScreenAssetsGroup', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('35', '9', '35');
#20230601 qzg 资产接口信息表新增字段
ALTER TABLE "mw_cmdbmd_assets_interface"
ADD ("editor_desc" NUMBER(4,0));
COMMENT ON COLUMN "mw_cmdbmd_assets_interface"."editor_desc" IS '是否修改过描述信息';

#lbq notchekc 23/6/16
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('976', '/mwapi/alert/custom/time');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('977', '/mwapi/alert/get/time');

#20230615 gqw 增加拓扑根据机构绘制拓扑数据判断条件
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (972, '/mwapi/topology/getAssetsOrgEnable');

#23/6/20 lbq
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'modelSystem', "label" = '厂别', "visible" = '1', "order_number" = '13', "type" = NULL, "is_tree" = NULL WHERE "id" = '1684';
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '271', "prop" = 'modelClassify', "label" = '领域', "visible" = '1', "order_number" = '12', "type" = NULL, "is_tree" = NULL WHERE "id" = '1683';

#23/6/20 lbq notcheck表
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('928', '/mwapi/alert/get/project');

#20230620 gengjb 维护计划增加表头字段 华星已执行
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11733', '275', 'instanceName', '资产名称', '1', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11734', '275', 'inBandIp', 'IP地址', '1', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11735', '275', 'modelSystem', '业务系统', '1', '3', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11736', '275', 'modelClassify', '业务分类', '1', '4', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11737', '275', 'assetsTypeSubName', '子类型', '1', '5', NULL, NULL);
#23/06/20 qzg 资产凭证凭证表数据插入
#23/06/20 qzg 资产凭证凭证表数据插入
INSERT INTO "MWDEVSQL"."mw_cmdbmd_model_macro"("id", "model_id", "macro_id") VALUES ('32', '69', '1');
INSERT INTO "MWDEVSQL"."mw_cmdbmd_model_macro"("id", "model_id", "macro_id") VALUES ('33', '69', '2');
INSERT INTO "MWDEVSQL"."mw_cmdbmd_model_macro"("id", "model_id", "macro_id") VALUES ('34', '69', '3');

#23/06/21 qzg 数据源查询状态下拉框
INSERT INTO "mw_select_url_base"("id", "drop_number_value", "drop_char_value", "drop_label", "drop_type", "drop_value_type", "deep", "pid", "nodes", "is_node") VALUES (72, 1, '状态', '正常', 'status', 0, 1, 0, ',72,', 0);
INSERT INTO "mw_select_url_base"("id", "drop_number_value", "drop_char_value", "drop_label", "drop_type", "drop_value_type", "deep", "pid", "nodes", "is_node") VALUES (73, 0, '状态', '异常', 'status', 0, 1, 0, ',73,', 0);
UPDATE "mw_pageselect_table" SET  "input_format" = 3, "url" = '/selectNum/browse?type=source_status' WHERE "page_Id" = 224 and  "prop" = 'status';

#23/06/27 qzg
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (988, '/mwapi/modelView/getDeploymentType/browse');

#23/6/27 lbq
CREATE TABLE "MWDEVSQL"."mw_alert_trigger_close_table" (
       "server_id" NUMBER(32,0) VISIBLE,
       "trigger_id" NVARCHAR2(255) VISIBLE,
       "close_date" DATE VISIBLE,
       "operator" NVARCHAR2(255) VISIBLE
)
    TABLESPACE "MONITOR_DAT";

#2023/6/30 gengjb 可视化增加资产可用性缓存
INSERT INTO "MWDEVSQL"."mw_visualized_cache_item_table"("id", "item_name", "type") VALUES ('30', 'MW_HOST_AVAILABLE', '1');

#2023/7/2 guiqw 四川项目增加ARP视图查看(oracle环境只需要执行notcheck即可)
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (991, '/mwapi/tableView/getEnable');

#2023/7/3 gengjb  可视化增加接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('985', '/mwapi/visualized/businstatus/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('986', '/mwapi/visualized/businstatus/dropDown');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('987', '/mwapi/visualized/businstatus/create');

#2023/7/3 gengjb  可视化增加表
CREATE TABLE "MWDEVSQL"."mw_visualized_businstatuse_table" (
  "id" NVARCHAR2(128) VISIBLE NOT NULL,
  "model_system_name" NVARCHAR2(128) VISIBLE,
  "title_name" NVARCHAR2(128) VISIBLE
)
TABLESPACE "MONITOR_DAT";

#20230705 gqw 增加日志管理以及日志审计权限数据
#增加日志管理主菜单
INSERT INTO "mw_module"("id", "pid", "module_name", "module_desc", "url", "is_node", "deep", "nodes", "enable", "version", "delete_flag", "node_protocol") VALUES
 (291, 0, '日志管理', '日志管理', '', 1, 1, '291', 1, 0, 0, NULL);
#更新原有逻辑将每个菜单下移一位
update  "mw_module" set "deep" = 4 , "nodes" = CONCAT('291,',"nodes")  where "id" in (267,268,269);
update  "mw_module" set "deep" = 3, "nodes" = CONCAT('291,',"nodes") where "id" in (225,227,228);
update  "mw_module" set "deep" = 2, "nodes" = CONCAT('291,',"nodes") ,"pid" = 291 where "id" in (224);
#增加逻辑权限
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 291, 1);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 291, 2);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 291, 3);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 291, 4);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 291, 5);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 291, 6);
#增加admin对应的权限
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-291-browse', 0, 291, 'browse', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-291-create', 0, 291, 'create', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-291-delete', 0, 291, 'delete', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-291-editor', 0, 291, 'editor', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-291-perform', 0, 291, 'perform', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-291-secopassword', 0, 291, 'secopassword', 1);
#增加日志审计菜单
INSERT INTO "mw_module"("id", "pid", "module_name", "module_desc", "url", "is_node", "deep", "nodes", "enable", "version", "delete_flag", "node_protocol") VALUES
 (292, 291, '日志审计', '日志审计', '', 1, 2, '291,292', 1, 0, 0, NULL);
#增加逻辑权限
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 292, 1);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 292, 2);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 292, 3);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 292, 4);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 292, 5);
INSERT INTO "mw_module_perm_mapper"("id", "module_id", "perm_id") VALUES (SEQ_MODULE_PERM_MAPPER.NEXTVAL, 292, 6);
#增加admin对应的权限
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-292-browse', 0, 292, 'browse', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-292-create', 0, 292, 'create', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-292-delete', 0, 292, 'delete', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-292-editor', 0, 292, 'editor', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-292-perform', 0, 292, 'perform', 1);
INSERT INTO "mw_role_module_perm_mapper"("id", "role_id", "module_id", "perm_name", "enable")
VALUES ('0-292-secopassword', 0, 292, 'secopassword', 1);
#日志审计搜索功能相关接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (994, '/mwapi/logManage/getTables');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (995, '/mwapi/logManage/getColumnByTable');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (996, '/mwapi/logManage/list');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (997, '/mwapi/logManage/saveSelectedColumns');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (998, '/mwapi/logManage/getLogAnalysisCacheInfo');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (999, '/mwapi/logManage/analysisChar');

#lbq 23/7/7 历史告警新增字段
    INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11738', '272', 'modelSystem', '业务系统', '1', '12', NULL, NULL);

#qzg 0711
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('989', '/mwapi/modelMacroAuth/selectInfoPopup/brows');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('990', '/mwapi/modelMacroAuth/getAllMacroField/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('991', '/mwapi/modelMacroAuth/queryMacroField/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('992', '/mwapi/modelMacroAuth/addMacroAuthInfo/insert');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('993', '/mwapi/modelMacroAuth/updateMacroAuthInfo/editor');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('994', '/mwapi/modelMacroAuth/deleteMarcoInfo/delete');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('995', '/mwapi/modelMacroAuth/getList/browse');
#qzg 0714
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1002', '/mwapi/modelVirtual/queryAssetsInfo/browse');

#zah 20230713 数据源配置功能更新
INSERT INTO "MWDEVSQL"."mw_event_time_moule_code" ("id", "code", "name") VALUES ('2', '2', 'yyyy.MM.dd-HH:mm:ss');
CREATE TABLE "MWDEVSQL"."mw_topic_field_alert_level"
(	"field_id" NVARCHAR2(20),
     "topic_code" NVARCHAR2(20),
     "level" NVARCHAR2(50),
     "source_field" NVARCHAR2(255),
     "mapp_value" NVARCHAR2(255),
     "is_delete" NUMBER(4,0)
)
    TABLESPACE "MONITOR_DAT";
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '224', "prop" = 'status', "label" = '状态', "visible" = '1', "order_number" = '7', "type" = NULL, "is_tree" = NULL WHERE "id" = '10241';
UPDATE "MWDEVSQL"."mw_pageselect_table" SET "page_Id" = '224', "prop" = 'status', "label" = '状态', "input_format" = '3', "url" = '/selectNum/browse?type=source_status', "typeof" = NULL WHERE "id" = '10241';

# 23/7/16 lbq
CREATE TABLE "MWDEVSQL"."mw_alert_huaxing_yuyin_table" (
                                                           "url" VARCHAR2(255 BYTE) VISIBLE,
                                                           "app_key" VARCHAR2(255 BYTE) VISIBLE,
                                                           "tpl_id" VARCHAR2(255 BYTE) VISIBLE,
                                                           "rule_id" VARCHAR2(255 BYTE) VISIBLE
);
INSERT INTO "MWDEVSQL"."mw_alert_action_type"("id", "action_type") VALUES ('24', '华星语音通知');


#gengjb 华星增加资产详情进程top10页签接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('998', '/mwapi/server/getAssetsDetailsProcess/browse');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('999', '/mwapi/server/assetsDetailsProcess/download');

#lbq 23/7/19
UPDATE "MWDEVSQL"."mw_pagefield_table" SET "page_id" = '272', "prop" = 'modelSystem', "label" = '厂别', "visible" = '1', "order_number" = '12', "type" = NULL, "is_tree" = NULL WHERE "id" = '11738';
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11741', '272', 'modelClassify', '领域', '1', '12', NULL, NULL);

#20230721 qzg 模型表新增图标类型
ALTER TABLE "mw_cmdbmd_manage"
ADD ("icon_type" NUMBER(4,0));
COMMENT ON COLUMN "mw_cmdbmd_manage"."icon_type" IS '模型图标类型(0内置，1用户自定义)';
#20230725 qzg
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1010', '/mwapi/instanceName/browse');

#lbq 23/7/27
CREATE TABLE "MWDEVSQL"."mw_alert_user_subscribe_rule_table" (
                                                                 "user_id" NUMBER VISIBLE,
                                                                 "rule_id" VARCHAR2(255 BYTE) VISIBLE
)
    TABLESPACE "MONITOR_DAT";
CREATE TABLE "MWDEVSQL"."mw_alert_user_subscribe_modelsystem_table" (
                                                                        "user_id" NUMBER VISIBLE,
                                                                        "model_system" VARCHAR2(255 BYTE) VISIBLE
)
    TABLESPACE "MONITOR_DAT";

#通道管理
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1006', '/mwapi/logManage/vectorChannel/add');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1007', '/mwapi/logManage/vectorChannel/update');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1008', '/mwapi/logManage/vectorChannel/list');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1009', '/mwapi/logManage/vectorChannel/delete');

INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11758', '295', 'mdificationDate', '更新时间', '1', '8', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11759', '295', 'mdificationUser', '更新人', '1', '9', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11751', '295', 'channelName', '通道名称', '1', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11752', '295', 'channelIp', 'IP地址', '1', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11753', '295', 'channelPort', '端口', '1', '3', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11754', '295', 'status', '通道状态', '1', '4', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11755', '295', 'type', '类型', '1', '5', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11756', '295', 'createDate', '创建时间', '1', '6', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11757', '295', 'createUser', '创建人', '1', '7', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11760', '295', 'relevanceRule', '规则名', '1', '10', NULL, NULL);

INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10377', '295', 'channelName', '通道名称', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10378', '295', 'channelIp', 'IP地址', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10379', '295', 'status', '通道状态', '1', NULL, NULL);


#转发配置
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1011', '/mwapi/logManage/forward/delete');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1012', '/mwapi/logManage/forward/list');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1013', '/mwapi/logManage/forward/update');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1014', '/mwapi/logManage/forward/add');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('1015', '/mwapi/logManage/forward/getEnumValue');

INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11761', '296', 'forwardingName', '转发名称', '1', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11762', '296', 'forwardingIp', 'IP地址', '1', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11763', '296', 'forwardingPort', '端口', '1', '3', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11764', '296', 'status', '状态', '1', '4', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11765', '296', 'forwardingKeyPath', 'KEY地址', '1', '5', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11766', '296', 'relevanceRule', '规则名', '1', '6', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11767', '296', 'createTime', '创建时间', '1', '7', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11768', '296', 'createUser', '创建人', '1', '8', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11769', '296', 'updateTime', '更新时间', '1', '9', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11770', '296', 'updateUser', '更新人', '1', '10', NULL, NULL);

INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10381', '296', 'forwardingIp', 'IP地址', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10382', '296', 'status', '转发状态', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10383', '296', 'forwardingName', '转发名称', '1', NULL, NULL);

#20230804 zah clickhouse数据源配置
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_connection" ("id", "connection_type", "connection_name", "datasource_type_id") VALUES ('5', '5', 'http', '3');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_connection" ("id", "connection_type", "connection_name", "datasource_type_id") VALUES ('6', '6', 'tcp', '3');
INSERT INTO "MWDEVSQL"."mw_sys_log_datasource_type" ("id", "type", "name") VALUES ('3', '3', 'ClickHouse');

#20230808 lijubo 增减连线显示
ALTER TABLE "MWDEVSQL"."mw_topo_graph" ADD ("show_line" NUMBER(1)  DEFAULT  1);

#2023/8/11 lbq 华星告警表新增字段
ALTER TABLE "MWDEVSQL"."mw_huaxing_alert_table"
    ADD ("alert_type" VARCHAR2(255));
ALTER TABLE "MWDEVSQL"."mw_huaxing_alert_table"
    ADD ("model_classify" VARCHAR2(255));
ALTER TABLE "MWDEVSQL"."mw_huaxing_alert_table"
    ADD ("model_system" VARCHAR2(255));
ALTER TABLE "MWDEVSQL"."BUSSINESS_ALARM_INFO"
    ADD ("SEVERITY" VARCHAR2(255));
ALTER TABLE "MWDEVSQL"."BUSSINESS_ALARM_INFO"
    ADD ("MODEL_SYSTEM" VARCHAR2(255))
ADD ("MODEL_CLASSIFY" VARCHAR2(255));
ALTER TABLE "MWDEVSQL"."BUSSINESS_ALARM_INFO"
    ADD ("OBJECT_NAME" VARCHAR2(255))
ADD ("ALERT_TYPE" VARCHAR2(255));

#shenwenyi 2023/8/9 导出用户在线时长接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES (1016, '/mwapi/user/session/exportUserOnline');

# 创建用户登录时长表
CREATE TABLE "MWDEVSQL"."mw_user_session" (
                                   "id" NUMBER not null constraint mw_user_session_pk primary key,
                                   "user_id" NUMBER,
                                   "user_name" VARCHAR(255 BYTE),
                                   "org_id" VARCHAR(255 BYTE),
                                   "org_name" VARCHAR(255 BYTE),
                                   "login_time" DATE ,
                                   "logout_time" DATE ,
                                   "create_time" DATE ,
                                   "online_time" NUMBER
);

#shenwenyi 2023/8/10  查询用户在线时长分页接口
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (1017, '/mwapi/user/session/browse');
INSERT INTO "MWDEVSQL"."mw_report_table" ("id", "report_name", "report_desc", "report_type_id", "report_time_id", "create_date", "creator", "modification_date", "modifier", "delete_flag", "send_time") VALUES ('15', '用户在线时长报表', '用户在线时长报表', 1, 1, '2023-08-10 14:36:51', 'swy', '2023-08-10 14:36:58', 'swy', 0, NULL);
ALTER TABLE "MWDEVSQL"."mw_user_session" ADD ("login_name" VARCHAR(255 BYTE));

#qzg 2023/08/21
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES (1052, '/mwapi/modelMonitor/updateStatus/editor');

#gqw 2023/8/31 添加同步LDAP用户数据功能
DROP TABLE "MWDEVSQL"."MW_HUAXING_LDAP_USER";
CREATE TABLE "MWDEVSQL"."MW_HUAXING_LDAP_USER" (
                                                   "USER_ID" NUMBER ( 24 ) NOT NULL PRIMARY KEY,
                                                   "USER_NAME" NVARCHAR2 ( 255 ) NULL,
                                                   "LOGIN_NAME" NVARCHAR2 ( 255 ) NULL,
                                                   "PHONE_NUMBER" NVARCHAR2 ( 255 ) NULL,
                                                   "WECHAT_ID" NVARCHAR2 ( 255 ) NULL,
                                                   "EMAIL" NVARCHAR2 ( 255 ) NULL,
                                                   "CREATE_TIME" TIMESTAMP NULL,
                                                   "UPDATE_TIME" TIMESTAMP NULL,
                                                   "DELETE_FLAG" NUMBER ( 5 ) NULL
);
#lbq 23/8/31
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10361', '290', 'description', '触发器名称', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10362', '290', 'closeDate', '关闭时间', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10363', '290', 'userName', '处理人', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10364', '290', 'monitorServerName', '监控服务器', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11742', '290', 'description', '触发器名称', '0', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11743', '290', 'closeDate', '关闭时间', '0', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11744', '290', 'userName', '处理人', '0', '3', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11745', '290', 'monitorServerName', '监控服务器', '0', '4', NULL, NULL);
#lbq 23/9/1
ALTER TABLE "MWDEVSQL"."mw_huaxing_alert_table"
    ADD ("event_id" VARCHAR2(255));

#qzg 23/9/4 新增资产实例评价表
CREATE TABLE "MWDEVSQL"."mw_cmdbmd_judge" (
	"id" NUMBER ( 11, 0 ) NOT NULL,
	"judge_message" NVARCHAR2 ( 128 ),
	"judge_score" NVARCHAR2 ( 128 ),
	"judge_time" DATE,
	"user_id" NUMBER ( 11, 0 ),
	"user_name" NVARCHAR2 ( 128 ),
	"instance_id" NUMBER ( 11, 0 )
);
#qzg 23/9/4
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (1070, '/mwapi/judgeMessage/selectInfoById/browse');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (1071, '/mwapi/judgeMessage/insertJudgeInfo/create');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (1072, '/mwapi/judgeMessage/checkJudgeCycle/browse');

#guiqw 20230906 增加用户模块的定时任务
INSERT INTO "mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES (37, 12, '定时同步用户数据', 'cn.mw.monitor.plugin.user.UserPlugin', 'saveLdapUser', 0);
INSERT INTO "mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES (37, 12, 37);

#gengjb 2023/09/06 可视化BC增加告警信息
ALTER TABLE "mw_visualized_cache_assets_table" ADD("alert_title" VARCHAR2(255));

#lbq 23/9/7
ALTER TABLE "MWDEVSQL"."mw_huaxing_alert_table"
    ADD ("create_date" DATE);

#gengjb 2023/09/13 华星增加定时任务缓存
INSERT INTO "mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('60', '14', '华星数据库数据缓存', 'cn.huaxing.time.HuaxingVisualizedTime', 'getHuaxingDataInfo', '0');
INSERT INTO "mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('60', '14', '60');

#gengjb 2023/09/19 容器告警缓存
CREATE TABLE "MWDEVSQL"."mw_visualized_containeralert_table" (
     "id" NVARCHAR2 (128) NOT NULL,
     "alert_level" NVARCHAR2 ( 128 ),
     "alert_count" NUMBER ( 11, 0 ),
     "alert_date" NVARCHAR2 ( 128 ),
     "partition_name" NVARCHAR2 ( 128 )
);
INSERT INTO "mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('62', '14', '容器告警缓存', 'cn.mw.monitor.visualized.time.MwVisualizedContainerAlertTime', 'getContainerAlertRecord', '0');
INSERT INTO "mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('62', '14', '62');

#gengjb 2023/10/07 可视化健康分数占比情况表
CREATE TABLE "MWDEVSQL"."mw_visualized_scoreproportion_table" (
    "id" NVARCHAR2(128) VISIBLE NOT NULL,
    "proportion" NUMBER(11,0) VISIBLE,
    "type" NUMBER(11,0) VISIBLE,
    "classify_name" NVARCHAR2(255) VISIBLE,
    "item_name" VARCHAR2(255 BYTE) VISIBLE
)
TABLESPACE "MONITOR_DAT";
INSERT INTO "MWDEVSQL"."mw_visualized_scoreproportion_table" VALUES ('1', '20', '1', 'MODMES,FABMES', 'PROCESS_HEALTH');
INSERT INTO "MWDEVSQL"."mw_visualized_scoreproportion_table" VALUES ('2', '20', '1', 'RVD系统', 'PROCESS_HEALTH');
INSERT INTO "MWDEVSQL"."mw_visualized_scoreproportion_table" VALUES ('3', '20', '2', '数据库', 'MW_ORACLE_PYTHON_GET_VERSION');
INSERT INTO "MWDEVSQL"."mw_visualized_scoreproportion_table" VALUES ('4', '20', '2', '操作系统', 'CPU_UTILIZATION,MEMORY_UTILIZATION,MW_DISK_UTILIZATION');
INSERT INTO "MWDEVSQL"."mw_visualized_scoreproportion_table" VALUES ('5', '10', '2', '虚拟化', 'ICMP_PING');
INSERT INTO "MWDEVSQL"."mw_visualized_scoreproportion_table" VALUES ('6', '10', '3', 'mes', 'ICMP_PING');

#gengjb 华星可视化新增定时任务
INSERT INTO "mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('70', '14', '可视化VDI数据缓存', 'cn.mw.monitor.visualized.time.MwVisualizedModuleTime', 'visualizedVdiCache', '0');
INSERT INTO "mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('70', '14', '70');

#20231017 qzg 规格型号新增设备高度
ALTER TABLE "mw_cmdbmd_vendor_specification"
    ADD ("device_height" NUMBER(11,0));
COMMENT ON COLUMN "mw_cmdbmd_vendor_specification"."device_height" IS '设备高度';
#qzg 1017规格型号新增表头字段
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (12788, 245, 'deviceHeight', '设备高度', 1, 2, NULL, NULL);
INSERT INTO "monitor"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES (11383, 245, 'deviceHeight', '设备高度', 1, NULL, NULL);
#qzg 1020 创建可视化页面查询参数保存表
CREATE TABLE "MWDEVSQL"."mw_visualized_query_value"
(	"id" NUMBER(11,0) NOT NULL ENABLE,
     "value_json" NCLOB,
     PRIMARY KEY ("id")
         USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
         TABLESPACE "MONITOR_DAT"  ENABLE
);
CREATE SEQUENCE  "MWDEVSQL"."SEQ_MW_VISUALIZED_QUERY_VALUE"  MINVALUE 1 MAXVALUE 9999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE  NOKEEP  NOSCALE  GLOBAL

#lbq 23/10/30
ALTER TABLE "MWDEVSQL"."mw_alert_action"
    MODIFY ("start_time" NVARCHAR2(32))
    MODIFY ("end_time" NVARCHAR2(32))

#gengjb 231106 报表管理建表
CREATE TABLE "MWDEVSQL"."mw_report_cache_history_table"
(	"id" NVARCHAR2(128) NOT NULL ENABLE,
     "assets_name" NVARCHAR2(128),
     "assets_ip" NVARCHAR2(128),
     "assets_id" NVARCHAR2(128),
     "server_id" NUMBER(11,0),
     "host_id" NVARCHAR2(128),
     "value_avg" NVARCHAR2(128),
     "value_max" NVARCHAR2(128),
     "value_min" NVARCHAR2(128),
     "units" NVARCHAR2(128),
     "item_name" NVARCHAR2(128),
     "partition_name" NVARCHAR2(128),
     "clock" NVARCHAR2(128),
     "date" DATE,
     "save_time" DATE,
     PRIMARY KEY ("id")
     USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
     STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
     PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
     BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
     TABLESPACE "MONITOR_DAT"  ENABLE
);
CREATE TABLE "MWDEVSQL"."mw_report_cache_latest_table"
(	"id" NVARCHAR2(128) NOT NULL ENABLE,
     "assets_name" NVARCHAR2(128),
     "assets_ip" NVARCHAR2(128),
     "assets_id" NVARCHAR2(128),
     "server_id" NUMBER(11,0),
     "host_id" NVARCHAR2(128),
     "last_value" NVARCHAR2(128),
     "units" NVARCHAR2(128),
     "item_name" NVARCHAR2(128),
     "partition_name" NVARCHAR2(128),
     "clock" NVARCHAR2(128),
     "date" DATE,
     "save_time" DATE,
     PRIMARY KEY ("id")
     USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
     STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
     PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
     BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
     TABLESPACE "MONITOR_DAT"  ENABLE
);
CREATE TABLE "MWDEVSQL"."mw_report_index_table"
(	"id" NVARCHAR2(128) NOT NULL ENABLE,
     "item_name" NVARCHAR2(128),
     "chn_name" NVARCHAR2(128),
     PRIMARY KEY ("id")
     USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
     STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
     PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
     BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
     TABLESPACE "MONITOR_DAT"  ENABLE
);
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_model"("id", "model_name", "model_type") VALUES ('16', '自定义报表任务', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('72', '16', '历史报表任务', 'cn.mw.monitor.report.timer.MwReportMonitorIndexTime', 'reportIndexCacheHistory', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('71', '16', '实时报表任务', 'cn.mw.monitor.report.timer.MwReportMonitorIndexTime', 'reportIndexCacheLatest', '0');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('71', '16', '71');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('72', '16', '72');

INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('1094', '/mwapi/report/custom/realTime/export');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('1093', '/mwapi/report/custom/history/export');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('1092', '/mwapi/report/custom/index/dropDown');
INSERT INTO "mw_notcheck_url"("id", "url") VALUES ('1091', '/mwapi/report/custom/browse');

#可视化增加图片路径关联表
CREATE TABLE "MWDEVSQL"."mw_visualized_image_table"
(	"visualized_id" NUMBER(11,0) NOT NULL ENABLE,
     "image_url" NVARCHAR2(255),
     "node_id" NVARCHAR2(255)
);
ALTER TABLE "MWDEVSQL"."mw_visualized_table"
    ADD ("is_template" NUMBER(11,0));
COMMENT ON COLUMN "MWDEVSQL"."mw_visualized_table"."is_template" IS '是否是模板';


#20231116 1116模板表新增转态字段
INSERT INTO "mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES (11793, 244, 'status', '启用状态', 1, 5, NULL, NULL);
INSERT INTO "mw_notcheck_url"("id", "url") VALUES (1099, '/mwapi/modelTemplate/templateStatus/editor');
ALTER TABLE "mw_cmdbmd_template_table"
    ADD ("status" NUMBER(4,0));
COMMENT ON COLUMN "mw_cmdbmd_template_table"."status" IS '启用状态 0禁用，1启用';
update "MWDEVSQL"."mw_cmdbmd_template_table" set "status" = 1;

#20231116 1120模型表新增排序字段
ALTER TABLE "mw_cmdbmd_manage"
    ADD ("model_sort" NUMBER(4,0));
COMMENT ON COLUMN "mw_cmdbmd_manage"."model_sort" IS '模型排序数字';

#23/11/22 lbq
CREATE TABLE "MWDEVSQL"."mw_alert_ignore_table" (
       "id" VARCHAR2(255 BYTE) VISIBLE NOT NULL,
       "server_id" NUMBER(32,0) VISIBLE,
       "event_id" VARCHAR2(32 BYTE) VISIBLE,
       "ignore_date" DATE VISIBLE,
       "operator" NUMBER(32,0) VISIBLE,
       "name" VARCHAR2(255 BYTE) VISIBLE
)
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('856', '/mwapi/alert/ignore/alert');
INSERT INTO "MWDEVSQL"."mw_notcheck_url"("id", "url") VALUES ('857', '/mwapi/alert/getignore/alert');
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10384', '298', 'monitorServerName', '监控服务器', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10385', '298', 'userName', '操作用户', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10386', '298', 'ignoreDate', '操作日期', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pageselect_table"("id", "page_Id", "prop", "label", "input_format", "url", "typeof") VALUES ('10387', '298', 'name', '告警标题', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11789', '298', 'monitorServerName', '监控服务器', '1', '1', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11790', '298', 'userName', '操作用户', '1', '2', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11791', '298', 'ignoreDate', '操作日期', '1', '3', NULL, NULL);
INSERT INTO "MWDEVSQL"."mw_pagefield_table"("id", "page_id", "prop", "label", "visible", "order_number", "type", "is_tree") VALUES ('11792', '298', 'name', '告警标题', '1', '4', NULL, NULL);

#23/11/23 lbq
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_tree"("id", "model_id", "action_id") VALUES ('63', '13', '63');
INSERT INTO "MWDEVSQL"."mw_ncm_timetask_action"("id", "model_id", "action_name", "action_impl", "action_method", "action_model") VALUES ('63', '13', '消息记录写入TXT文档', 'cn.mw.time.MWZbxAlertLevelTime', 'sendInfoWriteTxt', NULL);

#20231122
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES ('1095', '/mwapi/userGroup/sort');
#20231124 qzg
INSERT INTO "MWDEVSQL"."mw_notcheck_url" ("id", "url") VALUES ('1104', '/mwapi/judgeMessage/JudgeInfo/delete');

