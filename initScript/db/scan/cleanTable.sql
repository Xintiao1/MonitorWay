
-- delete from mw_assets_logo where normal_logo like '%/upload/%';
delete from mw_topo_group where parent_id is not null;
truncate table mw_topo_group_userinfo;
truncate table mw_topo_graph;
truncate table mw_topo_graph_ipranges;
truncate table mw_topo_graph_ips;
truncate table mw_topo_graph_snmpv1v2;
truncate table mw_topo_graph_snmpv3;
truncate table mw_topo_graph_subsets;
truncate table mw_topo_line_setting;
truncate table mw_topo_subline_setting;