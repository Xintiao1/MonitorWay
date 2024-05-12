package cn.mw.monitor.alert.dao;

import cn.mw.monitor.alert.dto.*;
import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.action.param.Label;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/8/27 14:45
 */
public interface MwAlertActionDao {

    int insertAction(AddAndUpdateAlertActionParam param);

    int updateAction(AddAndUpdateAlertActionParam param);

    int updateActionEnable(AlertAndRuleEnableParam param);

    int deleteAction(List<String> actionId);

    int insertSeverityMapper(List<ActionSeverityMapper> actionSeverityMappers);

    int insertAssetsMapper(List<ActionAssetsMapper> actionAssetsMappers);

    int insertUserTypeMapper(List<ActionUserTypeMapper> actionUserTypeMappers);

    List<Integer> getDefaultUserIds(String assetsId);

    int insertActionUsersMapper(List<ActionUserMapper> actionUserMappers);

    int insertActionGroupsMapper(List<ActionGroupMapper> actionGroupMappers);

    int insertActionTypeMapper(List<ActionTypeMapper> actionTypeMappers);

    int insertActionLeveRule(ActionLevelRule actionLevelRule);

    int insertActionLeveUserMapper(List<ActionLevelRule> actionLevelRule);

    int insertActionLeveEmailMapper(List<ActionLevelRule> actionLevelRule);

    int insertActionRuleMapper(List<ActionRuleMapper> actionRuleMappers);

    int insertActionLabelMapper(Label label);

    int deleteActionAssetsMapper(String actionId);

    int deleteActionRulesMapper(String actionId);

    int deleteActionUsersMapper(String actionId);

    int deleteActionGroupsMapper(String actionId);

    int deleteActionTypesMapper(String actionId);

    int deleteActionServerityMapper(String actionId);

    int deleteActionLabelMapper(String actionIds);

    List<String> getAssetsByActionId(String actionId);

    List<String> getAssetsByActionIds(List<String> actionIds);

    int deleteActionAssetsMappers(List<String> actionId);

    int deleteActionRulesMappers(List<String> actionId);

    int deleteActionUsersMappers(List<String> actionId);

    int deleteActionTypesMappers(List<String> actionId);

    int deleteActionServerityMappers(List<String> actionId);

    int deleteActionLabelMappers(List<String> actionIds);


    List<AlertActionTable> selectPriActionList(Map priCriteria);

    List<AlertActionTable> selectPubActionList(Map pubCriteria);

    List<AlertActionTable> selectActionList(Map pubCriteria);

    AddAndUpdateAlertActionParam selectPopupAction(String actionId);

    Label selectLabel(String actionId);

    List<String> selectSeverity(String actionId);

    List<String> selectRules(String actionId);

    List<Integer> selectActionTypes(String actionId);

    List<Integer> selectActionUsers(String actionId);

    HashSet<Integer> selectActionUsersMapper(String actionId);

    List<Integer> selectActionGroupsMapper(String actionId);

    List<String> selectUserType(String actionId);

    List<ActionLevelRuleParam> selectActionLevelRule();

    List<Integer> selectActionLevelUserId(ActionLevelRuleParam actionLevelRule);

    int addActionLevelEventMapper(ActionLevelRuleParam actionLevelRule);

    List<Integer> selectActionLevelEventMapper(ActionLevelRuleParam actionLevelRule);

    int deleteActionLevelRules(List<String> actionIds);

    int deleteActionLevelEventMappers(List<String> actionIds);

    int deleteActionLevelUserMappers(List<String> actionIds);

    ActionLevelRuleParam selectActionLevelUserIdByActionId(String actionId);

    int deleteActionLevelRule(String actionId);

    int deleteActionLevelEventMapper(String actionId);

    int deleteActionLevelUserMapper(String actionId);

    Integer selectWebmonitorId(WebMonitorParam webMonitorParam);

    HashSet<Integer> selectUserMapper(String typeid);

    List<Integer> selectGroupMapper(String typeid);

    List<Integer> selectOrgMapper(String typeid);

    List<String> selectNetworkLinkId(NetworkLinkParam networkLinkParam);

    List<AssetsFielidParam> getAssetsFielid();

    int addAction(AddAndUpdateAlertActionDto param);

    int upAction(AddAndUpdateAlertActionDto param);

    MwRuleSelectListParam selectAction(String actionId);

    int insertActionAssetsclumn(List<AssetsFielidParam> actionLevelRule);

    int deleteActionAssetsclumnMapper(String actionId);

    int deleteActionAssetsclumnMappers(List<String> actionId);

    List<AssetsFielidParam> selectActionAssetsclumn(String actionId);

    int insertActionLeveRuleMapper(List<ActionLevelRule> actionLevelRule);

    int deleteActionLeveRuleMapper(String actionId);

    int deleteActionLeveRuleMappers(List<String> actionId);

    int deleteActionLevelEmailMappers(List<String> actionId);

    int deleteActionUserTypeMappers(List<String> actionId);

    List<String> selecActionLeveRuleMapper(ActionLevelRuleParam actionLevelRule);

    List<ActionLevelParam> getLevelInfo(ActionLevelRuleParam actionLevelRule);

    Integer selecActionLeveTimeUnit(ActionLevelRuleParam actionLevelRule);

    List<ActionLevelRuleParam> selecActionLeveEmail(String actionId);

    MwTangibleassetsParam slecltTangibleassetsByfielids(@Param("list") List<AssetsFielidParam> result,@Param("assetsId") String assetsId);

    Integer insertMwAlertRuleSelect(List<MwRuleSelectParam> param);

    int deleteMwAlertRuleSelect(String uuid);

    int deleteActionLevelEmailMapper(String actionId);

    int deleteActionUserTypeMapper(String actionId);

    List<MwRuleSelectParam> selectMwAlertRuleSelect(String uuid);

    List<MwRuleSelectListParam> selectMwAlertAction(String actionId);

    List<MwRuleSelectEventParam> selectMwAlertRuleSelectEvent(String uuid);

    Integer insertMwAlertRuleSelectEvent(MwRuleSelectEventParam param);

    Integer deleteMwAlertRuleSelectEvent(MwRuleSelectEventParam param);

    MwRuleSelectEventParam selectMwAlertRuleSelectEventBytitle(@Param("title") String title, @Param("ip") String ip,@Param("isAlarm") Boolean isAlarm,@Param("actionId") String actionId);

    int upMwAlertRuleSelectEvent(MwRuleSelectEventParam param);

    int deleteMwAlertRuleSelects(@Param("uuids") List<String> uuids);

    int upMwAlertAction(MwRuleSelectListParam selectListParam);

    List<String> selectTypeIdMapper(@Param("hostid") String hostid);

    HashSet<Integer> selectFuzzUserMapper(String hostid);

    List<Integer>  selectFuzzGroupMapper(String hostid);

    List<Integer>  selectFuzzOrgMapper(String hostid);

    List<String> selectActionIds();

}
