## 创建知识库主表
CREATE TABLE `mw_knowledgebase_table` (
  `id` varchar(255) NOT NULL COMMENT 'id',
  `title` varchar(128) NOT NULL COMMENT '知识标题',
  `trigger_cause` varchar(255) NOT NULL COMMENT '触发原因',
  `attachment_url` varchar(128) DEFAULT NULL COMMENT '附件地址',
  `solution` longtext NOT NULL COMMENT '解决方案',
  `type_id` int(11) NOT NULL COMMENT '知识分类',
  `creator` varchar(128) NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `modifier` varchar(128) NOT NULL COMMENT '修改人',
  `modification_date` datetime NOT NULL COMMENT '修改时间',
  `activiti_status` int(11) DEFAULT NULL COMMENT '工作流状态：-1-未发布；1-执行中；2-撤销；3-被驳回；4-已完成',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标识符',
  `version` int(128) DEFAULT NULL COMMENT '版本号',
  `process_id` varchar(128) DEFAULT NULL COMMENT '流程实例id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

##创建知识和用户关系表
CREATE TABLE `mw_knowledge_user_mapper` (
  `id` int(50) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` int(50) NOT NULL COMMENT '用户id',
  `knowledge_id` varchar(255) NOT NULL COMMENT '知识Id',
  `status` int(11) NOT NULL COMMENT '状态：-1取消点赞 ，1点赞；-2取消踩，2踩',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;

##创建记录知识被踩或被赞总量表
CREATE TABLE `mw_knowledge_likeorhate_record` (
  `id` int(50) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `knowledge_id` varchar(255) NOT NULL COMMENT '知识Id',
  `status` int(11) NOT NULL COMMENT '状态：0点赞 ，1被踩',
  `times` int(11) NOT NULL COMMENT '点赞或者被踩总次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
