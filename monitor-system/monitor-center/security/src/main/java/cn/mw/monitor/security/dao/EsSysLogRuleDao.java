package cn.mw.monitor.security.dao;

import cn.mw.module.security.dto.*;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/1/6
 */
public interface EsSysLogRuleDao {

    Integer insertMwAlertRuleSelect(List<MwRuleSelectParam> param);

    void insertSysLogRule(EsSysLogRuleDTO param);

    void insertTagInfo(EsSysLogTagDTO param);

    void insertSysLogTagMapper(EsSysLogRuleTagMapperDTO param);

    void createTagInfo(EsSysLogTagDTO param);

    void deleteSysLogTagInfo(List<Integer> ids);

    List<EsSysLogTagDTO> getSysLogTagInfo();

    List<Map<String, Object>> fuzzSearchAllFiled();

    List<EsSysLogRuleDTO> getSystemLogRulesInfos(EsSysLogRuleDTO param);

    List<EsSysLogRuleDTO> getRulesInfosBySysLogAudit();

    EsSysLogRuleDTO getSystemLogRulesInfoById(EsSysLogRuleDTO param);

    List<EsSysLogTagDTO> getRuleTags(Integer id);

    List<MwRuleSelectParam> getAlertRules(String ruleId);

    void deleteMwAlertRuleSelect(String ruleId);

    void deleteMwAlertRuleSelectByMore(List<String> ruleIds);

    void deleteTagInfo(Integer id);

    void deleteTagInfoByMore(List<Integer> ids);

    void updateSysLogRulesState(EsSysLogRuleDTO param);

    void updateSysLogRulesInfo(EsSysLogRuleDTO param);

    void deleteSysLogRulesInfo(List<Integer> ids);

    void insertActionRuleMapper(List<SysLogRuleMapper> actionRuleMappers);

    void insertActionUsersMapper(List<SysLogUserMapper> actionUserMappers);

    int insertActionGroupsMapper(List<SysLogGroupMapper> actionGroupMappers);

    List<String> selectRules(String actionId);

    List<Integer> selectActionUsersMapper(String actionId);

    List<Integer> selectActionGroupsMapper(String actionId);

    int deleteActionRulesMapper(String actionId);

    int deleteActionUsersMapper(String actionId);

    int deleteActionGroupsMapper(String actionId);

    List<EsSysLogRuleDTO> getRulesInfosByAction();

}
