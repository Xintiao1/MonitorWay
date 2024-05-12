ALTER TABLE mw_passwd_inform ADD COLUMN `modify_type` int(11) NULL COMMENT '修改类型标识';

ALTER TABLE `monitor`.`mw_passwd_inform` ADD COLUMN `delete_flag` tinyint(1) NOT NULL COMMENT '删除标识';