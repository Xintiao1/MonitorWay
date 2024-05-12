DROP TABLE IF EXISTS `mw_alert_dingdingqun_rule`;
CREATE TABLE `mw_alert_dingdingqun_rule`  (
  `rule_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '规则id',
  `webhook` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '群机器人webhook地址',
  `keyword` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息推送关键字',
  `secret` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密钥',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE mw_sys_user ADD COLUMN open_id VARCHAR(255) NULL DEFAULT NULL COMMENT '用户绑定微信openId';


update mw_pageselect_table set input_format = 4 where page_id = 10 and id = 600;

INSERT INTO mw_alert_action_type VALUES(7,'钉钉群');