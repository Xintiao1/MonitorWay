alter table mw_tangibleassets_table add `tp_server_host_name` varchar(128) NOT NULL COMMENT '第三方监控服务器中主机名称';
update mw_tangibleassets_table set tp_server_host_name = host_name;
alter table mw_outbandassets_table add `tp_server_host_name` varchar(128) NOT NULL COMMENT '第三方监控服务器中主机名称';
update mw_outbandassets_table set tp_server_host_name = host_name;

alter table mw_tangibleassets_table add `template_id` varchar(128) NOT NULL COMMENT '第三方监控服务器中关联模板id';
alter table mw_outbandassets_table add `template_id` varchar(128) NOT NULL COMMENT '第三方监控服务器中关联模板id';

CREATE TABLE `mw_ipv6managelist_his_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `link_id` int(11) NOT NULL COMMENT 'ip地址管理清单表主键',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'ip地址',
  `mac` varchar(255) DEFAULT NULL COMMENT 'MAC地址',
  `vendor` varchar(255) DEFAULT NULL COMMENT '厂商',
  `access_equip` varchar(255) DEFAULT NULL COMMENT '接入设备',
  `access_port` varchar(255) DEFAULT NULL COMMENT '接入端口',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  `access_port_name` varchar(200) DEFAULT NULL COMMENT '接入端口名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13859 DEFAULT CHARSET=utf8;
CREATE TABLE `mw_license_expire_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `module_id` varchar(128) NOT NULL COMMENT '模块id',
  `module_name` varchar(128) NOT NULL COMMENT '模块名称',
  `remain_date` int(11) DEFAULT NULL COMMENT '剩余天数',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


INSERT INTO mw_pagefield_table (page_id, prop, label, visible, order_number)
VALUES(158,'taskName','任务名称',1,1),(158,'taskType','任务类型',1,2),(158,'startEndTime','起止时间',1,3),(158,'detectRange','探测范围',1,4),(158,'portRange','端口范围',1,5),(158,'detectTimes','扫描次数',1,6),(158,'detectSchedule','进度',1,7),(158,'resultCount','扫描结果个数',1,8),(158,'taskDetails','任务详情',1,9),(158,'operation','操作',1,10);

INSERT into mw_pageselect_table (page_Id,prop,label,input_format)
VALUES (158,'taskName','任务名称',1),(158,'taskType','任务类型',1),(158,'startEndTime','起止时间',1),(158,'detectRange','探测范围',1),(158,'portRange','端口范围',1),(158,'detectTimes','扫描次数',1),(158,'detectSchedule','进度',1),(158,'resultCount','扫描结果个数',1),(158,'taskDetails','任务详情',1),(158,'operation','操作',1);

INSERT INTO mw_pagefield_table (page_id, prop, label, visible, order_number)
VALUES(159,'ip','IP地址',1,1),(159,'ipType','IP类型',1,2),(159,'osType','操作系统',1,3),(159,'hostName','主机名',0,4),(159,'serviceName','服务名称',1,5),(159,'port','端口号',1,6),(159,'state','端口状态',1,7),(159,'extraInfo','附加信息',1,8),(159,'reason','reason',1,9),(159,'reasonTTL','reasonTTL',1,10),(159,'product','product',1,11),(159,'agreement','协议',1,12);

INSERT INTO mw_pageselect_table (page_Id,prop,label,input_format)
VALUES(159,'ip','IP地址',1),(159,'ipType','IP类型',1),(159,'osType','操作系统',1),(159,'hostName','主机名',1),(159,'serviceName','服务名称',1),(159,'port','端口号',1),(159,'state','端口状态',1),(159,'extraInfo','附加信息',1),(159,'reason','reason',1),(159,'reasonTTL','reasonTTL',1),(159,'product','product',1),(159,'agreement','协议',1);


INSERT into mw_notcheck_url (id,url)
VALUES (201,'/mwapi/nmapTask/nmapResultBrowse'),(202,'/mwapi/nmapTask/browse'),(203,'/mwapi/nmapTask/nmapTaskDetails'),(204,'/mwapi/nmapTask/editor'),(205,'/mwapi/nmapTask/run');


