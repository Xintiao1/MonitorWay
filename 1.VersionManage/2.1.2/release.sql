alter table mw_tangibleassets_table add column  `timing` varchar(255) default  NULL COMMENT '定时间隔';
CREATE TABLE `mw_report_link_allday` (
  `interface_id` varchar(32) NOT NULL COMMENT '线路id',
  `caption` varchar(32) NOT NULL COMMENT '线路名称',
  `date_time` datetime NOT NULL COMMENT '存入时间',
  `in_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `out_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `band_unit` varchar(32) NOT NULL COMMENT '带宽单位',
  `in_max_bps` decimal(15,2) NOT NULL COMMENT '流入流量最大值',
  `in_average_bps` decimal(15,2) NOT NULL COMMENT '流入流量平均值',
  `in_min_bps` decimal(15,2) NOT NULL COMMENT '流入流量最小值',
  `in_max_use` decimal(15,2) NOT NULL COMMENT '流入量最大利用率',
  `in_avg_use` decimal(15,2) NOT NULL COMMENT '流入量平均利用率',
  `in_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)<10%',
  `in_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)10%-50%',
  `in_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)50%-80%',
  `in_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)>80%',
  `out_max_bps` decimal(15,2) NOT NULL COMMENT '流出流量最大值',
  `out_average_bps` decimal(15,2) NOT NULL COMMENT '流出流量平均值',
  `out_min_bps` decimal(15,2) NOT NULL COMMENT '流出流量最小值',
  `out_max_use` decimal(15,2) NOT NULL COMMENT '流出量最大利用率',
  `out_avg_use` decimal(15,2) NOT NULL COMMENT '流出量平均利用率',
  `out_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)<10%',
  `out_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)10%-50%',
  `out_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)50%-80%',
  `out_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)>80%'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `mw_report_link_allday_worktime` (
  `interface_id` varchar(32) NOT NULL COMMENT '线路id',
  `caption` varchar(32) NOT NULL COMMENT '线路名称',
  `date_time` datetime NOT NULL COMMENT '存入时间',
  `in_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `out_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `band_unit` varchar(32) NOT NULL COMMENT '带宽单位',
  `in_max_bps` decimal(15,2) NOT NULL COMMENT '流入流量最大值',
  `in_average_bps` decimal(15,2) NOT NULL COMMENT '流入流量平均值',
  `in_min_bps` decimal(15,2) NOT NULL COMMENT '流入流量最小值',
  `in_max_use` decimal(15,2) NOT NULL COMMENT '流入量最大利用率',
  `in_avg_use` decimal(15,2) NOT NULL COMMENT '流入量平均利用率',
  `in_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)<10%',
  `in_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)10%-50%',
  `in_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)50%-80%',
  `in_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)>80%',
  `out_max_bps` decimal(15,2) NOT NULL COMMENT '流出流量最大值',
  `out_average_bps` decimal(15,2) NOT NULL COMMENT '流出流量平均值',
  `out_min_bps` decimal(15,2) NOT NULL COMMENT '流出流量最小值',
  `out_max_use` decimal(15,2) NOT NULL COMMENT '流出量最大利用率',
  `out_avg_use` decimal(15,2) NOT NULL COMMENT '流出量平均利用率',
  `out_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)<10%',
  `out_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)10%-50%',
  `out_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)50%-80%',
  `out_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)>80%'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `mw_report_link_workday` (
  `interface_id` varchar(32) NOT NULL COMMENT '线路id',
  `caption` varchar(32) NOT NULL COMMENT '线路名称',
  `date_time` datetime NOT NULL COMMENT '存入时间',
  `in_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `out_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `band_unit` varchar(32) NOT NULL COMMENT '带宽单位',
  `in_max_bps` decimal(15,2) NOT NULL COMMENT '流入流量最大值',
  `in_average_bps` decimal(15,2) NOT NULL COMMENT '流入流量平均值',
  `in_min_bps` decimal(15,2) NOT NULL COMMENT '流入流量最小值',
  `in_max_use` decimal(15,2) NOT NULL COMMENT '流入量最大利用率',
  `in_avg_use` decimal(15,2) NOT NULL COMMENT '流入量平均利用率',
  `in_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)<10%',
  `in_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)10%-50%',
  `in_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)50%-80%',
  `in_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)>80%',
  `out_max_bps` decimal(15,2) NOT NULL COMMENT '流出流量最大值',
  `out_average_bps` decimal(15,2) NOT NULL COMMENT '流出流量平均值',
  `out_min_bps` decimal(15,2) NOT NULL COMMENT '流出流量最小值',
  `out_max_use` decimal(15,2) NOT NULL COMMENT '流出量最大利用率',
  `out_avg_use` decimal(15,2) NOT NULL COMMENT '流出量平均利用率',
  `out_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)<10%',
  `out_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)10%-50%',
  `out_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)50%-80%',
  `out_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)>80%'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `mw_report_link_workday_worktime` (
  `interface_id` varchar(32) NOT NULL COMMENT '线路id',
  `caption` varchar(32) NOT NULL COMMENT '线路名称',
  `date_time` datetime NOT NULL COMMENT '存入时间',
  `in_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `out_bandwidth` decimal(15,2) NOT NULL COMMENT '上行带宽',
  `band_unit` varchar(32) NOT NULL COMMENT '带宽单位',
  `in_max_bps` decimal(15,2) NOT NULL COMMENT '流入流量最大值',
  `in_average_bps` decimal(15,2) NOT NULL COMMENT '流入流量平均值',
  `in_min_bps` decimal(15,2) NOT NULL COMMENT '流入流量最小值',
  `in_max_use` decimal(15,2) NOT NULL COMMENT '流入量最大利用率',
  `in_avg_use` decimal(15,2) NOT NULL COMMENT '流入量平均利用率',
  `in_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)<10%',
  `in_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)10%-50%',
  `in_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)50%-80%',
  `in_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(入向)>80%',
  `out_max_bps` decimal(15,2) NOT NULL COMMENT '流出流量最大值',
  `out_average_bps` decimal(15,2) NOT NULL COMMENT '流出流量平均值',
  `out_min_bps` decimal(15,2) NOT NULL COMMENT '流出流量最小值',
  `out_max_use` decimal(15,2) NOT NULL COMMENT '流出量最大利用率',
  `out_avg_use` decimal(15,2) NOT NULL COMMENT '流出量平均利用率',
  `out_proportion_ten` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)<10%',
  `out_proportion_fifty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)10%-50%',
  `out_proportion_eighty` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)50%-80%',
  `out_proportion_hundred` decimal(15,2) NOT NULL COMMENT '接口流量时间占比(出向)>80%'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table mw_tangibleassets_table add column  `timing` varchar(255) default  NULL COMMENT '定时间隔';

alter table mw_ncm_timetask_download_history add column  `path` varchar(255) default  NULL COMMENT '结果汇总文件地址';
alter table mw_ncm_timetask_download_history add column  `name` varchar(255) default  NULL COMMENT '汇总文件名称';

alter table mw_ipaddresses_table  modify column ip_type tinyint(1) NOT NULL DEFAULT 0;

