##syt
##删除notcheck中server模块中的获取标签url
delete from mw_notcheck_url where url= "/mwapi/server/getAssetsLabelByAssetsId/browse";
##添加运维管理页面的权限控制
INSERT INTO `mw_module` VALUES (135, 36, 'system-operation-management', '运维管理', '', 0, 2, '36,135', 1, 0);
