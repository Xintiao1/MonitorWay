ALTER TABLE `monitor`.`mw_sys_user` ADD COLUMN `validity_type` varchar(20)  NULL COMMENT '有效期至';
alter table mw_tangibleassets_table modify out_band_ip varchar(128);

ALTER TABLE `monitor`.`mw_sys_user` ADD COLUMN `conditions_value` int(11)  NULL DEFAULT NULL COMMENT '用户登录控制条件';

ALTER TABLE `monitor`.`mw_sys_user` ADD COLUMN `action_value` int(11)  NULL DEFAULT NULL COMMENT '用户登录控制动作';
ALTER TABLE `monitor`.`mw_sys_user` ADD COLUMN `validity_type` varchar(20)  NULL COMMENT '有效期至';
alter table mw_tangibleassets_table modify out_band_ip varchar(128);
alter table mw_model_base add column class varchar(128)  COMMENT '组件类的全限定名';
CREATE TABLE `mw_ipv6manage_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `parent_id` int(11) DEFAULT 0 COMMENT '父级节点id',
  `label` varchar(128) DEFAULT NULL COMMENT '名称',
  `type` varchar(128) DEFAULT NULL COMMENT '类型',
  `leaf` varchar(128) DEFAULT NULL COMMENT '是否为叶子节点',
  `descri` varchar(128) DEFAULT NULL COMMENT '描述',
  `ip_addresses` varchar(128) DEFAULT NULL COMMENT 'ip地址段',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `use_count` int(11) DEFAULT 0 COMMENT '已使用ip',
  `notuse_count` int(11) DEFAULT 0 COMMENT '未使用ip',
  `reserved_count` int(11) DEFAULT 0 COMMENT '预留ip',
  `online` int(11) DEFAULT 0 COMMENT '在线ip',
  `offline` int(11) DEFAULT 0 COMMENT '离线ip',
  `mask` varchar(255) DEFAULT '0' COMMENT '子网掩码',
  `is_include` int(11) DEFAULT NULL COMMENT '是否为可用地址',
  `timing` int(11) DEFAULT NULL COMMENT '定时扫描间隔',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
ALTER TABLE `mw_dropdown_table` ADD COLUMN `drop_val` varchar(255)  NULL COMMENT '展示值';

