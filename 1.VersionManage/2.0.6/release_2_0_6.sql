drop view view_user_org;
create view view_user_org as
select a.user_id    AS user_id,
       a.login_name AS login_name,
       c.org_id     AS org_id,
       c.org_name   AS org_name,
       c.deep       AS deep,
       c.nodes      AS nodes
from ((mw_sys_user a join mw_user_org_mapper b)
         join mw_sys_org c)
where a.user_id = b.user_id
  and b.org_id = c.org_id
  and a.delete_flag = 0
  and b.delete_flag = 0
  and c.delete_flag = 0;

drop table mw_report_rule_mapper;
CREATE TABLE `mw_report_rule_mapper` (
  `report_id` varchar(32) DEFAULT NULL COMMENT '报表id',
  `rule_id` varchar(32) DEFAULT NULL COMMENT '规则id',
    `rule_type` int(11)  DEFAULT NULL COMMENT '1推送 2微信 3邮件'
);
##syt
insert into mw_notcheck_url values (115, "/mwapi/redisItems/update")
insert into mw_notcheck_url values (116, "/mwapi/activiti/getActivitiView/browse")
