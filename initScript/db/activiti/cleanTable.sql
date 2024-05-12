##清activiti
SET foreign_key_checks = 0;
truncate table ACT_HI_ATTACHMENT;
truncate table ACT_HI_COMMENT;
truncate table ACT_HI_DETAIL;
truncate table ACT_HI_ACTINST;
truncate table ACT_HI_IDENTITYLINK;

truncate table ACT_HI_PROCINST;
truncate table ACT_HI_TASKINST;
truncate table ACT_HI_VARINST;
truncate table ACT_RU_EXECUTION;
truncate table ACT_RU_TASK;
truncate table ACT_RU_IDENTITYLINK;

truncate table ACT_RU_VARIABLE;
truncate table ACT_RU_EXECUTION;
truncate table ACT_GE_BYTEARRAY;
##业务流程定义表
truncate table ACT_RE_PROCDEF;
truncate table ACT_RE_DEPLOYMENT;

truncate table mw_process_def;
truncate table mw_process_module_bind;
truncate table mw_process_my_task;
truncate table mw_process_start_activiti;

truncate table my_unfinish_process;
truncate table mw_process_def_q;
truncate table mw_process_task_list;
SET foreign_key_checks = 1;
truncate table mw_process_model_tree_bind;
truncate table mw_process_task_module_bind;