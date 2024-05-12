/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2021/2/5 9:56:49                             */
/*==============================================================*/


drop table if exists mw_cmdbmd_assets_mapper;

drop table if exists mw_cmdbmd_group;

drop table if exists mw_cmdbmd_manage;

drop table if exists mw_cmdbmd_mapper;

drop table if exists mw_cmdbmd_properties;

drop table if exists mw_cmdbmd_properties_mapper;

drop table if exists mw_cmdbmd_properties_type;

drop table if exists mw_cmdbmd_relations;

drop table if exists mw_cmdbmd_relations_group;

drop table if exists mw_cmdbmd_type_base;

drop table if exists mw_cmdbmd_assets_mapper;

/*==============================================================*/
/* Table: mw_cmdbmd_assets_mapper                               */
/*==============================================================*/
create table mw_cmdbmd_assets_mapper
(
   left_tangible_id     varchar(32),
   right_tangible_id    varchar(32)
);


/*==============================================================*/
/* Table: mw_cmdbmd_group                                       */
/*==============================================================*/
create table mw_cmdbmd_group
(
   model_group_id       int(11) not null auto_increment comment '模型分类id',
   model_group_name     varchar(32) comment '模型分类名称',
   is_show              boolean comment '是否显示',
   deep                 int(11) comment '深度',
   nodes                varchar(256) comment '节点ID',
   pid                  varchar(32) comment '上级id',
   is_node              boolean comment '是否根节点',
   creator              varchar(128) comment '创建人',
   create_date          datetime comment '创建时间',
   modifier             varchar(128) comment '修改人',
   modification_date    datetime comment '修改时间',
   primary key (model_group_id)
);

/*==============================================================*/
/* Table: mw_cmdbmd_manage                                      */
/*==============================================================*/
create table mw_cmdbmd_manage
(
   model_id             int(11) not null auto_increment comment '模型id',
   model_name           varchar(32) comment '模型名称',
   model_desc           varchar(128) comment '模型描述',
   model_index          varchar(32) comment '模型标识符索引',
   model_type_id        varchar(128) comment '模型类型id',
   model_group_id       varchar(128) comment '模型分类id',
   url           varchar(128) comment '模型icon',
   is_show              boolean comment '是否显示',
   deep                 int(11) comment '深度',
   nodes                varchar(256) comment '节点ID',
   pid                  varchar(32) comment '上级id',
   is_node              boolean comment '是否根节点',
   creator              varchar(128) comment '创建人',
   create_date          datetime comment '创建时间',
   modifier             varchar(128) comment '修改人',
   modification_date    datetime comment '修改时间',
   delete_flag          boolean default false comment '删除标识',
   primary key (model_id)
);



drop table if exists mw_cmdbmd_mapper;

/*==============================================================*/
/* Table: mw_cmdbmd_mapper                                      */
/*==============================================================*/
create table mw_cmdbmd_mapper
(
   tangible_id          varchar(32) comment '资产主键',
   model_instance_id    int(11) comment '模型实例主键'
);




drop table if exists mw_cmdbmd_properties;

/*==============================================================*/
/* Table: mw_cmdbmd_properties                                  */
/*==============================================================*/
create table mw_cmdbmd_properties
(
   properties_id        int(11) not null auto_increment comment 'id',
   `index_id`                varchar(32) comment 'es存的索引的id',
   properties_name      varchar(32) comment '属性名称',
   default_value        varchar(32) comment '默认值',
   properties_type_id   int(11) comment '属性类型id',
   properties_type      varchar(32) comment '属性分类（基本，默认）',
   model_id             int(11) comment '模型id',
   is_read              boolean comment '是否只读',
   is_must              boolean comment '是否必填',
   is_only              boolean comment '是否唯一',
   is_show              boolean comment '是否显示',
   primary key (properties_id)
);


/*==============================================================*/
/* Table: mw_cmdbmd_properties_mapper                           */
/*==============================================================*/
create table mw_cmdbmd_properties_mapper
(
   assets_id            varchar(32) comment '资产主键',
   properties_id        int(11) comment '模型主键'
);

drop table if exists mw_cmdbmd_properties_type;

/*==============================================================*/
/* Table: mw_cmdbmd_properties_type                             */
/*==============================================================*/
create table mw_cmdbmd_properties_type
(
   properties_type_id                   int(11) not null auto_increment,
   properties_type_name      varchar(32),
   primary key (properties_type_id)
);



drop table if exists mw_cmdbmd_relations;

/*==============================================================*/
/* Table: mw_cmdbmd_relations                                   */
/*==============================================================*/
create table mw_cmdbmd_relations
(
   relation_id          int(11) not null auto_increment comment 'id',
   relation_name        varchar(32) comment '关系名称',
   left_model_id        int(11) comment '左端模型id',
   right_model_id       int(11) comment '右端模型id',
   left_relation_group_id int(11) comment '左关系分组id',
   right_relation_group_id int(11) comment '右关系分组id',
   left_relation        varchar(32) comment '左关联个数(左-右(0-1或者0-n))',
   right_relation       varchar(32) comment '右关联关个数左-右(0-1或者0-n))',
   creator              varchar(128) comment '创建人',
   create_date          datetime comment '创建时间',
   modifier             varchar(128) comment '修改人',
   modification_date    datetime comment '修改时间',
   delete_flag          boolean default false comment '删除标识',
   primary key (relation_id)
);



drop table if exists mw_cmdbmd_relations_group;

/*==============================================================*/
/* Table: mw_cmdbmd_relations_group                             */
/*==============================================================*/
create table mw_cmdbmd_relations_group
(
   relation_group_id    int(11) not null auto_increment,
   model_id             int(11) comment '模型id',
   relation_group_name  varchar(32) comment '分组名称',
   relation_group_desc  varchar(128) comment '分组描述',
   creator              varchar(128) comment '创建人',
   create_date          datetime comment '创建时间',
   modifier             varchar(128) comment '修改人',
   modification_date    datetime comment '修改时间',
   delete_flag          boolean default false comment '删除标识',
   primary key (relation_group_id)
);






drop table if exists mw_cmdbmd_manage;

/*==============================================================*/
/* Table: mw_cmdbmd_manage                                      */
/*==============================================================*/
create table mw_cmdbmd_manage
(
   model_id             int(11) not null auto_increment comment '模型id',
   model_name           varchar(32) comment '模型名称',
   model_desc           varchar(128) comment '模型描述',
   model_index          varchar(32) comment '模型标识符索引',
   model_type_id        int(11) comment '模型类型id',
   model_group_id       int(11) comment '模型分类id',
   model_group_sub_id   int(11) comment '模型分类子id',
   url           varchar(128) comment '模型icon',
   is_show              boolean comment '是否显示',
   deep                 int(11) comment '深度',
   nodes                varchar(256) comment '节点ID',
   pid                  int(11) comment '上级id',
   is_node              boolean comment '是否根节点',
   creator              varchar(128) comment '创建人',
   create_date          datetime comment '创建时间',
   modifier             varchar(128) comment '修改人',
   modification_date    datetime comment '修改时间',
   delete_flag          boolean default false comment '删除标识',
   primary key (model_id)
);


drop table if exists mw_cmdbmd_type_base;

/*==============================================================*/
/* Table: mw_cmdbmd_type_base                                   */
/*==============================================================*/
create table mw_cmdbmd_type_base
(
   model_type_id        int(11) comment '模型id',
   model_type_name      varchar(32) comment '模型名称'
);

drop table if exists mw_cmdbmd_instance;

/*==============================================================*/
/* Table: mw_cmdbmd_instance                                    */
/*==============================================================*/
create table mw_cmdbmd_instance
(
   instance_id          int not null auto_increment comment '模型实例id',
   instance_name        varchar(32) comment '实例名称',
   model_id             int comment '对应的模型id',
   primary key (instance_id)
);

drop table if exists mw_cmdbmd_instance_mapper;

/*==============================================================*/
/* Table: mw_cmdbmd_instance_mapper                             */
/*==============================================================*/
create table mw_cmdbmd_instance_mapper
(
   instance_relations_id int(11) not null auto_increment comment '实例关系id',
   left_instance_id     int(11) comment '左实例id',
   right_instance_id    int(11) comment '右实例id',
   type                 varchar(32) comment '实例关系类型(1父子关系,2 1-n/1-1关系)',
   primary key (instance_relations_id)
);

drop table if exists mw_cmdbmd_instance_chart;

/*==============================================================*/
/* Table: mw_cmdbmd_instance_chart                              */
/*==============================================================*/
create table mw_cmdbmd_instance_chart
(
   chart_id             int(11) not null auto_increment,
   chart_name           varchar(32),
   primary key (chart_id)
);

drop table if exists mw_cmdbmd_instance_mapper;

/*==============================================================*/
/* Table: mw_cmdbmd_instance_mapper                             */
/*==============================================================*/
create table mw_cmdbmd_instance_mapper
(
   instance_relations_id int(11) not null auto_increment comment '实例关系id',
   left_instance_id     int(11) comment '左实例id',
   right_instance_id    int(11) comment '右实例id',
   type                 varchar(32) comment '实例关系类型(1父子关系,2 1-n/1-1关系)',
   chart_id             int(11),
   primary key (instance_relations_id)
);
drop table if exists mw_cmdbmd_instance;

/*==============================================================*/
/* Table: mw_cmdbmd_instance                                    */
/*==============================================================*/
create table mw_cmdbmd_instance
(
   instance_id          int not null auto_increment comment '模型实例id',
   instance_name        varchar(32) comment '实例名称',
   model_id             int comment '对应的模型id'
   primary key (instance_id)
);

alter table mw_cmdbmd_properties modify column is_read tinyint(1) default 0;
alter table mw_cmdbmd_properties modify column is_must tinyint(1) default 0;
alter table mw_cmdbmd_properties modify column is_only tinyint(1) default 0;
alter table mw_cmdbmd_properties modify column is_show tinyint(1) default 0;

alter table mw_cmdbmd_manage modify column is_show tinyint(1) default 0;
alter table mw_cmdbmd_group modify column is_show tinyint(1) default 0;alter table mw_enginemanage_table add `proxy_address` varchar(128) DEFAULT NULL comment '活动代理地址';
alter table mw_tangibleassets_table modify column `assets_id` varchar(128) DEFAULT NULL COMMENT '资产id';
alter table mw_outbandassets_table modify column `assets_id` varchar(128) DEFAULT NULL COMMENT '资产id';
ALTER table mw_ipaddressmanage_table add address_desc VARCHAR(200);
ALTER table mw_ipaddressmanage_table add longitude VARCHAR(50);
ALTER table mw_ipaddressmanage_table add latitude VARCHAR(50);

/*==============================================================*/
/* Table: 新增部分                                    */
/*==============================================================*/
ALTER table mw_ipaddressmanage_table add country VARCHAR(50);
ALTER table mw_ipaddressmanage_table add state VARCHAR(50);
ALTER table mw_ipaddressmanage_table add city VARCHAR(50);
ALTER table mw_ipaddressmanage_table add region VARCHAR(50);

ALTER table mw_ipv6manage_table add address_desc VARCHAR(200);
ALTER table mw_ipv6manage_table add longitude VARCHAR(50);
ALTER table mw_ipv6manage_table add latitude VARCHAR(50);
ALTER table mw_ipv6manage_table add country VARCHAR(50);
ALTER table mw_ipv6manage_table add state VARCHAR(50);
ALTER table mw_ipv6manage_table add city VARCHAR(50);
ALTER table mw_ipv6manage_table add region VARCHAR(50);

CREATE TABLE `mw_license_list_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `module_id` varchar(128) NOT NULL COMMENT '模块id',
  `module_name` varchar(128) NOT NULL COMMENT '模块名称',
  `expire_date` datetime DEFAULT NULL COMMENT '到期时间',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `module_type` varchar(20) DEFAULT NULL COMMENT '激活状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
