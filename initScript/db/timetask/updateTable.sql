#初始化数据
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (1, 1, '数据收敛', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskTwo', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (2, 1, '数据输出', 'cn.mw.monitor.report.timer.MwReportTimeSendEmail', 'reportSendEmail', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (3, 1, '数据缓存', 'cn.mw.monitor.report.service.impl.MwReportTerraceManageServiceImpl', 'manualRunTimeTaskThere', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (4, 2, '数据缓存', 'cn.mw.monitor.virtualization.service.impl.MwVirtualServiceImpl', 'saveVirtualTree', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (5, 3, '数据缓存', 'cn.mw.time.MWZabbixConnetStateTime', 'zabbixConnectCheck', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (6, 4, '配置备份', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'downloadConfig', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (7, 4, '配置执行', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'execConfigScript', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (8, 4, '合规检测', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'execReport', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (9, 5, '数据扫描', 'cn.mw.monitor.ipaddressmanage.service.impl.MwIpAddressManageServiceScanImpl', 'cronBatchScanIp', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (10, 6, '数据扫描', 'cn.mw.monitor.screen.timer.MWNewScreenTime', 'censusAssetsAmount', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (11, 4, '配置比对', 'cn.mw.monitor.configmanage.service.impl.MwConfigManageServiceImpl', 'compareConfigContent', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (13, 8, '数据清理', 'cn.mw.monitor.netflow.service.impl.MWNetflowServiceImpl', 'cleanData', NULL);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (12, 7, '基线', 'cn.mw.monitor.timetask.timer.MwBaseLineTime', 'baseLineHealthValue', 0);
INSERT INTO `mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (36, 15, '自动化执行作业', 'cn.mw.monitor.script.service.impl.ScriptManageServiceImpl', 'downloadConfig', 0);


INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (1, '报表任务', 1);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (2, '虚拟化任务', 1);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (3, '监控服务器', 1);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (4, '配置管理', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (5, 'IP地址管理', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (6, '资产管理', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (7, '基线配置', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (8, '流量管理任务', 1);
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (15, '自动化', 0);


INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (1, 1, 1);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (2, 1, 2);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (3, 1, 3);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (4, 2, 4);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (5, 3, 5);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (6, 4, 6);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (7, 4, 7);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (8, 4, 8);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (9, 5, 9);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (10, 6, 10);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (11, 4, 11);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (12, 7, 12);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (13, 8, 13);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (36, 15, 36);




INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('165189394831283fd5aea8e6341be9f9', 1, 1, '报表数据收敛定时任务', '缓存报表数据', 1, NULL, '2022-08-04 02:00:00', '2022-08-05 02:00:00', 38);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('1647840798624701ccd56d9954c899ba', 1, 2, '报表定时发送邮件定时', NULL, 0, 'day', '2022-07-13 11:10:13', '2022-07-13 11:20:00', 55);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('16511340007069e54caa0e31648428db', 1, 2, '报表邮件发送每周', NULL, 0, 'week', '2022-05-17 16:25:49', '2022-05-18 16:25:49', 427);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('1651134042719bf6e6888ceb946ec806', 1, 2, '报表邮件每月', NULL, 0, 'month', '2022-05-17 16:32:15', '2022-05-18 16:30:35', 72);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('16522397520215ea8d85b2d534dd8819', 1, 3, '流量报表数据缓存', NULL, 1, NULL, '2022-08-04 16:00:00', '2022-08-04 17:00:00', 9);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('16522410406091abc7f20f9464fa09fa', 1, 3, '运行状态缓存', NULL, 1, NULL, '2022-08-04 16:10:00', '2022-08-04 16:15:00', 3);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('1650522759505ddbb167645b84f3287a', 7, 12, '基线健康取值', NULL, 1, NULL, '2022-08-04 11:00:00', '2022-08-05 11:00:00', 11);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('1643704713426ff953cb91dc54c81ba4', 6, 10, '首页数据扫描', '测试定时', 1, '测试定时', NULL, NULL, NULL);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('16596837116114eb1be769bd14768b35', 8, 13, 'es数据清理', 'es数据清理', 0, NULL, NULL, NULL, NULL);



INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (511, '165189394831283fd5aea8e6341be9f9', '12');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (512, '165189394831283fd5aea8e6341be9f9', '13');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (513, '165189394831283fd5aea8e6341be9f9', '22');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (514, '165189394831283fd5aea8e6341be9f9', '7');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (515, '165189394831283fd5aea8e6341be9f9', '9');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (494, '1647840798624701ccd56d9954c899ba', '23');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (495, '1647840798624701ccd56d9954c899ba', '24');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (497, '16511340007069e54caa0e31648428db', '23');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (498, '16511340007069e54caa0e31648428db', '24');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (499, '1651134042719bf6e6888ceb946ec806', '23');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (500, '1651134042719bf6e6888ceb946ec806', '24');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (508, '16522410406091abc7f20f9464fa09fa', '12');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (509, '16522410406091abc7f20f9464fa09fa', '7');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_object`(`id`, `newtimetask_id`, `object_id`) VALUES (510, '16522397520215ea8d85b2d534dd8819', '13');




INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (38, '报表数据收敛时间', '0 0 2 * * ?', '2点执行一次', 'H', NULL, '02:00:00');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (30, '10分钟一次', '0 */10  1-23 * * ?', '每月第1-23天点从第*分开始,每10分执行一次', 'S', '', '');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (36, '16点25分49秒执行一次', '49 25 16 * * ?', '16点25分49秒执行一次', 'H', NULL, '16:25:49');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (37, '16点30分35秒执行一次', '35 30 16 * * ?', '16点30分35秒执行一次', 'H', NULL, '16:30:35');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (27, '平均一小时执行一次', '0 0 */1 * * ?', '从*点开始,每1小时执行一次', 'S', NULL, NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (40, '平均五分钟执行一次', '0 */5 * * * ?', '从第*分开始,每5分执行一次', 'S', NULL, NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (49, '11点执行一次', '0 0 11 * * ?', '11点执行一次', 'H', '3', '11:00:00');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (43, '每天1点', '0 0 1 * * ?', '1点执行一次', 'H', NULL, '01:00:00');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (50, '首页任务定时', '0 50 23 * * ?', '23点50分执行一次', 'H', NULL, '23:50:00');
INSERT INTO `monitor`.`mw_ncm_timetask_time_plan`(`id`, `time_name`, `time_cron`, `time_cron_chinese`, `time_type`, `time_choice`, `time_hms`) VALUES (51, '每天3点', '0 0 3 * * ?', '3点执行一次', 'H', NULL, '03:00:00');

INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (238, 38, '165189394831283fd5aea8e6341be9f9');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (221, 30, '1647840798624701ccd56d9954c899ba');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (223, 36, '16511340007069e54caa0e31648428db');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (224, 37, '1651134042719bf6e6888ceb946ec806');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (236, 40, '16522410406091abc7f20f9464fa09fa');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (237, 27, '16522397520215ea8d85b2d534dd8819');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (233, 49, '1650522759505ddbb167645b84f3287a');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (239, 50, '1643704713426ff953cb91dc54c81ba4');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (243, 51, '16596837116114eb1be769bd14768b35');
##首页定时任务初始化
INSERT INTO `monitor`.`mw_ncm_timetask_model`(`id`, `model_name`, `model_type`) VALUES (9, '首页大屏', 0);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (14, 9, '流量错误包缓存', 'cn.mw.monitor.screen.timer.MwFlowErrorCountTime', 'getFlowErrorCount', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (14, 9, 14);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (20, 9, '首页TopN缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheTopNData', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (21, 9, '首页流量缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheInterfaceFlowData', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_action`(`id`, `model_id`, `action_name`, `action_impl`, `action_method`, `action_model`) VALUES (22, 9, '首页带宽缓存', 'cn.mw.monitor.screen.timer.MwNewScreenCacheTime', 'cacheFlowBandWidthData', NULL);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (20, 9, 20);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (21, 9, 21);
INSERT INTO `monitor`.`mw_ncm_timetask_tree`(`id`, `model_id`, `action_id`) VALUES (22, 9, 22);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('1683858673548a5de81452481430a918', 9, 20, '首页topN数据缓存', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('16838588328119a7ab8ec7068447da76', 9, 21, '首页流量缓存', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO `monitor`.`mw_ncm_newtimetask`(`id`, `model_id`, `action_id`, `time_name`, `time_description`, `time_button`, `time_object`, `time_start_time`, `time_end_time`, `time_count`) VALUES ('16838588648786dea65535e5b45a38dd', 9, 22, '首页带宽数据缓存', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (461, 30, '1683858673548a5de81452481430a918');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (462, 30, '16838588328119a7ab8ec7068447da76');
INSERT INTO `monitor`.`mw_ncm_newtimetask_mapper_time`(`id`, `time_id`, `newtimetask_id`) VALUES (463, 30, '16838588648786dea65535e5b45a38dd');


