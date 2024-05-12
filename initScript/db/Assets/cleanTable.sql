## 资产模块
truncate table mw_assetssnmpv1_table;
truncate table mw_assetssnmpv3_table;
truncate table mw_agentassets_table;
truncate table mw_portassets_table;
truncate table mw_iotassets_table;
truncate table mw_tangibleassets_table;
## 带外资产
truncate table mw_outbandassets_table;
## 无形资产
truncate table mw_intangibleassets_table;

## 资产扫描模块
truncate table mw_iprang_table;
truncate table mw_ipaddresses_table;
truncate table mw_ipaddresslist_table;
truncate table mw_agentrule_table;
truncate table mw_rulesnmpv1_table;
truncate table mw_rulesnmpv3_table;
truncate table mw_portrule_table;
truncate table mw_scanrule_table;
truncate table mw_scanresultfail_table;
truncate table mw_scanresultsuccess_table;
truncate table mw_intangibleassets_table;
truncate table mw_tangible_outband_mapper;
truncate table mw_assetsgroup_server_mapper;

##资产维护计划模块
truncate table mw_assets_mainten;
truncate table mw_assets_mainten_host;
truncate table mw_assets_mainten_hostgroup;
truncate table mw_assets_mainten_timesolt;
truncate table mw_assets_mainten_tag;
truncate table mw_device_info;
##配置管理
truncate table mw_accountmanage_table;
truncate table mw_ncm_timetask_table;
truncate table mw_templatemanage_table;

##资产接口信息
truncate table mw_cmdbmd_assets_interface;

truncate table mw_assets_newfield;