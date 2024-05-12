DROP VIEW IF EXISTS view_mw_module_perm;
create view view_mw_module_perm as
select b.id AS id, a.id AS moduleId, a.url AS url, c.id AS permId, c.perm_name AS permName
from ((mw_module a join monitor.mw_module_perm_mapper b)
         join monitor.mw_permission c)
where a.id = b.module_id
  and b.perm_id = c.id and  a.delete_flag = 0;

DROP VIEW IF EXISTS view_user_control;
create view view_user_control as
select a.user_id AS user_id, a.login_name AS login_name, b.rule AS rule, c.name AS control_name
from ((mw_sys_user a join mw_user_control b)
         join mw_user_control_type c)
where a.user_id = b.user_id
  and b.control_type_id = c.id;

DROP VIEW IF EXISTS view_user_control_action;
create view view_user_control_action as
select a.user_id    AS user_id,
       a.login_name AS login_name,
       b.cond       AS cond,
       b.operation  AS operation
from (mw_sys_user a
         join mw_user_control_cond b)
where b.user_id = a.user_id;

DROP VIEW IF EXISTS view_user_group;
create view view_user_group as
select u.user_id    AS user_id,
       u.login_name AS login_name,
       g.group_id   AS group_id,
       g.group_name AS group_name,
       g.enable     AS enable
from ((mw_sys_user u join mw_user_group_mapper mapper)
         join mw_group_table g)
where u.user_id = mapper.user_id
  and mapper.group_id = g.group_id
  and u.delete_flag = 0
  and mapper.delete_flag = 0
  and g.delete_flag = 0;

DROP VIEW IF EXISTS view_user_org;
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

INSERT INTO mw_settings_info(id, logo_url, logo_basecode, modify_time, sideheader_color, sidemenu_color, sidemenu_textcolor, sidemenu_text_selectcolor, title, local_language, title_color, logo_descrition, http_header, icon, user_icon) VALUES (null, '', NULL, now(), NULL, 'rgb(4, 21, 40)', 'rgb(191, 203, 217)', 'rgb(64, 158, 255)', '猫维', NULL, '#002140', '智能运维平台系统', '/opt/app/monitorweb/upload', '', NULL);


#将新首页，GIS地图，自动化发现，日志审计，合规检测设置为不可见
update mw_module set delete_flag = true WHERE id in (117,174,178,221,224,229);
#将对应关系也删除
update mw_role_module_perm_mapper set `enable` = false WHERE module_id in (117,174,178,221,224,229);
