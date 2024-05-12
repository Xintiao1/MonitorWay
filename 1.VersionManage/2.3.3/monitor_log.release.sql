# 2021.9.3 monitor.log库
CREATE TABLE `mw_login_log_2021_q3rd` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `user_ip` varchar(50) DEFAULT NULL COMMENT '登录者IP',
  `user_name` varchar(50) DEFAULT NULL COMMENT '系统登录用户名',
  `create_date` datetime DEFAULT NULL COMMENT '登录时间',
  `is_success` varchar(50) DEFAULT NULL COMMENT '是否成功',
  `login_way` varchar(50) DEFAULT NULL COMMENT '登录方式',
  `fail_type` varchar(50) DEFAULT NULL COMMENT '失败原因',
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 2021.9.3 monitor.log库
CREATE TABLE `mw_system_log_2021_q3rd` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `log_time` datetime(6) NOT NULL COMMENT '创建时间',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `model_name` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `obj_name` varchar(3000) DEFAULT NULL COMMENT '操作对象',
  `operate_des` varchar(3000) DEFAULT NULL COMMENT '操作描述',
  `type` varchar(125) DEFAULT NULL COMMENT '模块类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#2021.9.3 monitor.log库
drop table mw_login_log_record;
drop table mw_sys_log_record;