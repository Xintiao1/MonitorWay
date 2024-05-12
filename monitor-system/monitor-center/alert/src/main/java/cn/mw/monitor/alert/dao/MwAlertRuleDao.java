package cn.mw.monitor.alert.dao;

import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.service.alert.dto.WeLinkRuleParam;
import cn.mw.monitor.util.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/4/4 15:13
 */
public interface MwAlertRuleDao {

    List<AlertRuleTable>  selectPriRuleList(Map priCriteria);

    List<AlertRuleTable>  selectPubRuleList(Map pubCriteria);

    List<AlertRuleTable>  selectRuleList(Map pubCriteria);

    AddAndUpdateAlertRuleParam selectRuleById(String ruleId);

    WeiXinParam selectWeixin(String ruleId);

    EmailParam selectEmail(String ruleId);

    ApplyWeiXinParam selectApplyWeixin(String ruleId);

    DingDingQunParam selectDingDingQun(String ruleId);

    AliyunSmsParam selectAliyunSms(String ruleId);

    ShenzhenSmsParam selectShenzhenSms(String ruleId);

    SYSLogParam selectSysLog(String ruleId);

    AliYunYuYinlParam findAliyunYuYinMessage(@Param("ruleId") String ruleId);

    TengXunSmsFromEntity findTengxunsmsMessage(@Param("ruleId") String ruleId);

    HuaWeiSmsFromEntity findHuaweismsMessage(@Param("ruleId") String ruleId);

    WeLinkRuleParam findWeLinkMessage(@Param("ruleId") String ruleId);

    HuaXingRuleParam findHuaXingMessage(@Param("ruleId") String ruleId);

    HuaXingYuYinRuleParam findHuaXingYuyinMessage(@Param("ruleId") String ruleId);

    MwCaiZhengTingSMSParam selectCaiZhengTingSMS(String ruleId);

    List<String> selectWeixinUserId(@Param("list") List<Integer> list);

    List<String> selectQyWeixinUserId(@Param("list") List<Integer> list);

    List<String> selectPhones(@Param("list") List<Integer> list);

    int insertRule(AddAndUpdateAlertRuleParam param);

    int insertWeiXinRule(WeiXinParam weiXinParam);

    int insertDingDingRule(DingDingParam dingDingParam);

    int insertEmailRule(EmailParam emailParam);

    int insertApplyWeiXin(ApplyWeiXinParam applyWeiXinParam);

    int insertDingDingQunRule(DingDingQunParam dingDingQunParam);

    int insertAliyunSMSRule(AliyunSmsParam aliyunSmsParam);

    int insertAliyunYuYinRule(AliYunYuYinlParam aliYunYuYinlParam);

    int insertTengxunsmsRule(TengXunSmsFromEntity tengXunSmsFromEntity);

    int insertHuaweismsRule(HuaWeiSmsFromEntity huaWeiSmsFromEntity);

    int insertWeLinkRule(WeLinkRuleParam weLinkRuleParam);

    int insertHuaXingRule(HuaXingRuleParam huaXingRuleParam);

    int insertHuaXingYuyinRule(HuaXingYuYinRuleParam huaXingYuYinRuleParam);

    int insertShenzhenSMSRule(ShenzhenSmsParam shenzhenSmsParam);

    int insertSYSLogRule(SYSLogParam sysLogParam);

    int insertCaiZhengTingSMsRule(MwCaiZhengTingSMSParam sysLogParam);

    int updateRule(AddAndUpdateAlertRuleParam param);

    int updateRuleEnable(AlertAndRuleEnableParam param);

    int deleteRule(List<String> ruleIds);

  //  int deleteWeiXinRule(String ruleId);

    int deleteTypeRule(@Param("ruleId") String ruleId, @Param("tableName") String tableName);

//    int deleteDingDingRule(String ruleId);
//
//    int deleteEmailRule(String ruleId);

    int deleteRuleActionMapper(List<String> ruleIds);

    int selectRuleActionCount(List<String> ruleIds);

    List<Map> getRuleListByActionTypeIds(Map priCriteria);

    List<Map> getActionType();

    int updateWeiXinRule(WeiXinParam weiXinParam);

    int updateDingDingRule(DingDingParam dingDingParam);

    int updateEmailRule(EmailParam emailParam);

    int updateapplyWeiXinRule(ApplyWeiXinParam applyWeiXinParam);

    int deleteRuleReportMapper(List<String> ruleIds);

    int getActionTypeByRuleId(String ruleId);

    List<String> selectEmailByUser(@Param("list") List<Integer> list);

    HashSet<Integer> selectSubUserId(@Param("modelSystem") String modelSystem,@Param("ruleId") String ruleId);
}
