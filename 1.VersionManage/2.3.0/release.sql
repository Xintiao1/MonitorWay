DROP TABLE IF EXISTS `mw_settings_info`;
CREATE TABLE `mw_settings_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `logo_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片url',
  `logo_basecode` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '图片base64码',
  `modify_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `sideheader_color` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '侧边栏头背景颜色',
  `sidemenu_color` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '侧边栏菜单颜色',
  `sidemenu_textcolor` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '侧边栏文本颜色',
  `sidemenu_text_selectcolor` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '侧边栏选中颜色',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '系统名称',
  `local_language` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语言',
  `title_color` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'logo文字颜色',
  `logo_descrition` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'logo描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

alter table mw_assetstemplate_table add interfaces_type int(11)  not null comment 'zabbix interfaceType 1:zabbix agent; 2:SNMP; 3:IPMI; 4:JMX';
insert into `monitor`.`mw_ncm_timetask_type_mapper` (typename,typemethod,typeclass)
VALUES
('虚拟化树数据定时更新','saveVirtualTree','cn.mw.monitor.virtualization.service.impl.MwVirtualServiceImpl');

CREATE TABLE `mw_adAuthentic_mapper` (
  `id` int(50) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ad_ip_add` varchar(255) DEFAULT NULL COMMENT '服务器IP',
  `ad_port` varchar(255) DEFAULT NULL COMMENT '端口号',
  `ad_apartment_name` varchar(255) DEFAULT NULL COMMENT '部门名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8;

insert into mw_pagefield_table (page_id,prop,label,visible,order_number) VALUES (41,'userType','用户类型',1,8);
insert into mw_pageselect_table (page_id,prop,label,input_format) VALUES (41,'userType','用户类型',1);

insert into mw_pagefield_table (page_id,prop,label,visible,order_number) VALUES (160,'adInfo','组织机构',1,1);
insert into mw_pagefield_table (page_id,prop,label,visible,order_number) VALUES (160,'localInfo','本地机构',1,2);
insert into mw_pagefield_table (page_id,prop,label,visible,order_number) VALUES (160,'operation','操作',1,3);

ALTER TABLE `monitor`.`mw_sys_user` MODIFY COLUMN `login_name` varchar(120) NOT NULL COMMENT '登录名' ;

ALTER TABLE `monitor`.`mw_sys_user` ADD COLUMN `user_type` varchar(255) NULL COMMENT '用户类型';
