##创建TPServer主表
create table mw_thirdparty_server_table
(
    id                         int(50) auto_increment comment '主键' primary key,
    monitoring_server_name     varchar(255)         not null comment '名称',
    monitoring_server_ip       varchar(255)         not null comment 'IP地址',
    monitoring_server_url      varchar(50)          not null comment '路径',
    monitoring_server_user     varchar(30)          not null comment '登录名',
    monitoring_server_password varchar(256)         not null comment '密码',
    monitoring_server_version  varchar(100)         not null comment '版本',
    monitoring_server_type     varchar(100)         not null comment '类型',
    main_server                tinyint(1)           not null comment '主从标识，（只能有一个主）',
    creator                    varchar(255)         not null comment '创建人',
    create_date                datetime             not null comment '创建时间',
    modifier                   varchar(255)         not null comment '修改人',
    modification_date          datetime             not null comment '修改时间',
    delete_flag                tinyint(1) default 0 not null comment '删除标识'
) comment '第三方服务器表';

create table mw_assetstemplate_server_mapper
(
    id                 int auto_increment primary key,
    assetstemplate_id  int null comment '资产扫描模版id',
    server_id          int null comment '第三方服务器id',
    template_id varchar(128) null comment 'zabbix服务器中的模版id'
) comment '资产扫描模版和第三方服务器关联表';

##新增有关TPServer 类型和版本的数据 到mw_dropdown_table  其中类型 的drop_code是server_type  其dropValue 对应以下版本的drop_code = drop_value + "_version"
INSERT INTO `mw_dropdown_table` (drop_code, drop_key, drop_value, update_time, delete_flag) VALUES ('server_type', 1, 'Zabbix', '2020-10-30 09:15:06', 0);
INSERT INTO `mw_dropdown_table` (drop_code, drop_key, drop_value, update_time, delete_flag) VALUES ('zabbix_version', 1, '3.0', '2020-10-30 09:16:20', 0);
INSERT INTO `mw_dropdown_table` (drop_code, drop_key, drop_value, update_time, delete_flag) VALUES ('zabbix_version', 2, '4.0', '2020-10-30 09:16:46', 0);
INSERT INTO `mw_dropdown_table` (drop_code, drop_key, drop_value, update_time, delete_flag) VALUES ('zabbix_version', 3, '5.0', '2020-10-30 09:17:13', 0);
INSERT INTO `mw_dropdown_table` (drop_code, drop_key, drop_value, update_time, delete_flag) VALUES ('zabbix_version', 4, '6.0', '2023-02-08 16:39:38', 0);


##新增表头字段
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerName', '监控服务器名称', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerIp', 'IP地址', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerUrl', '路径', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerUser', '登录账号', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerPassword', '登录密码', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerType', '监控服务器类型', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'monitoringServerVersion', '版本', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'mainServer', '主/从', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'department', '机构', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'group', '用户组', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'principal', '负责人', 1);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'creator', '创建人', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'creationDate', '创建时间', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'modifier', '修改人', 0);
INSERT INTO `mw_pagefield_table` (page_id, prop, label, visible) VALUES (132, 'modificationDate', '修改时间', 0);

##新增查询下拉字段
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'monitoringServerName', '监控服务器名称', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'monitoringServerIp', 'IP地址', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'monitoringServerUrl', '路径', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'monitoringServerType', '监控服务器类型', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'monitoringServerVersion', '版本', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'department', '机构', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'group', '用户组', 3, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'principal', '负责人', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'creator', '创建人', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'creationDate', '创建时间', 2, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'modifier', '修改人', 1, NULL, NULL);
INSERT INTO `mw_pageselect_table` (page_id, prop, label, input_format, url, typeof) VALUES (132, 'modificationDate', '修改时间', 2, NULL, NULL);
