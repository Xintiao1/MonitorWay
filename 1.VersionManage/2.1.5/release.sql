ALTER TABLE `monitor`.`mw_customcol_table` ADD COLUMN `delete_flag` int(1) NULL COMMENT '还原标识';


ALTER TABLE `mw_alert_record_table` ADD COLUMN `error` text(0)  NULL COMMENT '出错内容';
ALTER TABLE `monitor`.`mw_ipaddressmanagelist_his_table` ADD COLUMN `access_port_name` varchar(200)  NULL COMMENT '接入端口名称';

ALTER TABLE `monitor`.`mw_ipaddressmanagelist_table` ADD COLUMN `access_port_name` varchar(200)  NULL COMMENT '接入端口名称';
