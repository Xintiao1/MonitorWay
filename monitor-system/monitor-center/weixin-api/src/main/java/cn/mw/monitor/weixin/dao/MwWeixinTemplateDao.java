package cn.mw.monitor.weixin.dao;

import cn.mw.monitor.alert.param.ActionLevelRuleParam;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.alert.dto.WeLinkRuleParam;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.util.entity.*;
import cn.mw.monitor.weixin.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface MwWeixinTemplateDao {

    int insertOverdue(List<MwOverdueTable> data);

    int insert(MwWeixinTemplateTable templateTable);

    int delete(String templateId);

    MwWeixinTemplateTable selectOne(String templateId);

    MwWeixinTemplateTable selectOneByTemplateName(String templateName);

    List<String> selectGxActionId(@Param("hostId") String hostId);

    List<ActionRule> selectRuleMapper(@Param("actionId") String actionId);

    HashSet<Integer> selectUserId(@Param("actionId") String actionId);

    HashSet<String> selectLevel(@Param("actionId") String actionId);

    int insertRecord(AlertRecordTable alertRecordTable);

    int insertRecordUserMapper(@Param("id") Integer id,@Param("userIds") HashSet<Integer> userIds);

    int insertRecordEmailMapper(@Param("id") Integer id,@Param("emails")  List<String> emails);

    String selectRecord(AlertRecordTable alertRecordTable);

    List<String> selectEmail(@Param("list") HashSet<Integer> list);

    List<String> selectEmailBy(@Param("list") List<Integer> list);

    List<UserInfo> selectUserName(@Param("list") HashSet<Integer> list);

    List<UserInfo> selectUserNameBy(@Param("list") List<Integer> list);

    List<String> selectLoginName(@Param("list") HashSet<Integer> list);

    List<String> selectLoginNameBywechatId(@Param("list") String[] wechatIdLits);

    List<MWUser> selectByUserId(@Param("list") HashSet<Integer> list);

    List<String> selectGroupName(@Param("list") HashSet<Integer> list);

    AddAndUpdateAlertActionParam selectPopupAction(String actionId);

    ActionLevelRuleParam selectLevelRuleEmailMapper(ActionLevelRuleParam actionLevelRule);

    List<String> selectQyWeixinUserId(@Param("list") HashSet<Integer> list);

    List<String> selectWeixinUserId(@Param("list") HashSet<Integer> list);

    List<String> selectPhones(@Param("list") HashSet<Integer> list);

    List<String> selectMorePhones(@Param("list") HashSet<Integer> list);

    EmailFrom selectEmailFrom(@Param("id") String id);

    EmailFrom selectEmailFromCommon(@Param("id") String id);

    EmailFrom selectEmailByNameCommon(@Param("ruleName") String ruleName);

    GeneralMessageEntity findWeiXinMessage(@Param("ruleId") String ruleId);

    MwShenZhenSMSFromEntity findShenZhenSmsFrom(@Param("ruleId") String ruleId);

    MwCaiZhengTingSMSFromEntity findCaiZhengTingSmsFrom(@Param("ruleId") String ruleId);

    WeixinFromEntity findWeiXinFrom(@Param("ruleId") String ruleId);

    HuaXingRuleParam findHuaXingMessage(@Param("ruleId") String ruleId);

    HuaXingYuYinRuleParam findHuaXingYuyinMessage(@Param("ruleId") String ruleId);

    WeLinkRuleParam findWelinkFrom(@Param("ruleId") String ruleId);

    AlertRuleTableCommons selectRuleById(String ruleId);

    AlertRuleTableCommons selectRuleByIdAndType(String ruleId, String actionType);

    DingdingqunFromEntity findDingdingQunMessage(@Param("ruleId") String ruleId);

    AliyunSmsFromEntity findAliyunSMSMessage(@Param("ruleId") String ruleId);

    AliYunYuYinlParam findAliyunYuYinMessage(@Param("ruleId") String ruleId);

    TengXunSmsFromEntity findTengxunSMSMessage(@Param("ruleId") String ruleId);

    HuaWeiSmsFromEntity findHuaWeiSMSMessage(@Param("ruleId") String ruleId);

    List<MwOverdueTable> selectList(Map pubCriteria);

    void deleteBatch(@Param("idList") List<MwOverdueTable> list);

    List<MwOverdueTable> selectOverdue(@Param("ids") List<Integer> ids);

    void batUpdate(@Param("list") List<MwOverdueTable> lists);

    List<ActionRule> selectLevelRuleMapper(@Param("actionId") String actionId, @Param("level") Integer level);

    String selectassetsType(String hostId);

    TCP_UDPFrom findTCPFrom(@Param("ruleId") String ruleId);

    void insertHuaxingAlertTable(HuaXingAlertParam param);

    void updateHuaxingAlertTable(HuaXingAlertParam param);
}
