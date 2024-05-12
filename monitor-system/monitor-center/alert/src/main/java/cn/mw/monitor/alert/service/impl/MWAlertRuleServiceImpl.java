package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.alert.dto.WeLinkRuleParam;
import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.*;
import cn.mw.monitor.util.entity.*;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.alert.dao.MwAlertRuleDao;
import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.alert.service.MWAlertRuleService;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.state.DataType;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/4/1 9:55
 */
@Service
@Slf4j
public class MWAlertRuleServiceImpl implements MWAlertRuleService {

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Value("spring.huaxingdatasource.url")
    private String DB_URL;

    @Autowired
    private EmailSendUtil emailSendUtil;
    @Resource
    private MwAlertRuleDao mwAlertRuleDao;
    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWUserService mwUserService;



    @Override
    @Transactional
    public Reply insertRule(AddAndUpdateAlertRuleParam param) {
        //   try {
        String loginName = iLoginCacheInfo.getLoginName();
        param.setCreator(loginName);
        param.setModifier(loginName);
        String ruleId = UuidUtil.getUid();
        param.setRuleId(ruleId);
        mwAlertRuleDao.insertRule(param);
        Integer actionType = param.getActionType();

        //    @ApiModelProperty("通知类型 1微信 2钉钉 3邮件 4短信 5企业微信 6其他")
        switch (actionType) {
            case 1:
                WeiXinParam weiXinParam = param.getWeiXinParam();
                weiXinParam.setRuleId(ruleId);
                encrypt(weiXinParam);
                mwAlertRuleDao.insertWeiXinRule(weiXinParam);
                break;
            case 2:
                DingDingParam dingDingParam = param.getDingDingParam();
                dingDingParam.setRuleId(ruleId);
                encrypt(dingDingParam);
                mwAlertRuleDao.insertDingDingRule(dingDingParam);
                break;
            case 3:
                EmailParam emailParam = param.getEmailParam();
                emailParam.setRuleId(ruleId);
                if(emailParam.getLogo() != null){
                    emailParam.setUrl(filePath + "//file-upload//");
                }
                //加密密码相关字段
                encrypt(emailParam);

                mwAlertRuleDao.insertEmailRule(emailParam);
                break;
            case 5:
                ApplyWeiXinParam applyWeiXinParam = param.getApplyWeiXinParam();
                applyWeiXinParam.setRuleId(ruleId);
                encrypt(applyWeiXinParam);
                mwAlertRuleDao.insertApplyWeiXin(applyWeiXinParam);
                break;
            case 7:
            case 28:
                DingDingQunParam dingDingQunParam = param.getDingDingGroupParam();
                dingDingQunParam.setRuleId(ruleId);
                encrypt(dingDingQunParam);
                mwAlertRuleDao.insertDingDingQunRule(dingDingQunParam);
                break;
            case 8:
                AliyunSmsParam aliyunSmsParam = param.getAliyunSmsParam();
                aliyunSmsParam.setRuleId(ruleId);
                encrypt(aliyunSmsParam);
                mwAlertRuleDao.insertAliyunSMSRule(aliyunSmsParam);
                break;
            case 9:
                ShenzhenSmsParam shenzhenSmsParam = param.getShenzhenSmsparam();
                shenzhenSmsParam.setRuleId(ruleId);
                mwAlertRuleDao.insertShenzhenSMSRule(shenzhenSmsParam);
                break;
            case 10:
                SYSLogParam sysLogParam = param.getSysLogParam();
                sysLogParam.setRuleId(ruleId);
                mwAlertRuleDao.insertSYSLogRule(sysLogParam);
                break;
            case 11:
            case 12:
            case 19:
            case 21:
            case 26:
            case 27:
                MwCaiZhengTingSMSParam caiZhengTingSMSParam = param.getCaiZhengTingSMSParam();
                caiZhengTingSMSParam.setRuleId(ruleId);
                mwAlertRuleDao.insertCaiZhengTingSMsRule(caiZhengTingSMSParam);
                break;
            case 13:
                SYSLogParam guangZhouBank = param.getGuangZhouBankParam();
                guangZhouBank.setRuleId(ruleId);
                mwAlertRuleDao.insertSYSLogRule(guangZhouBank);
                break;
            case 14:
                AliYunYuYinlParam aliYunYuYinlParam = param.getAliYunYuYinParam();
                aliYunYuYinlParam.setRuleId(ruleId);
                mwAlertRuleDao.insertAliyunYuYinRule(aliYunYuYinlParam);
                break;
            case 15:
                TengXunSmsFromEntity tengXunSmsFromEntity = param.getTengXunSmsFromEntity();
                AliyunSmsParam aliSmsParam = param.getAliyunSmsParam();
                if(tengXunSmsFromEntity != null){
                    tengXunSmsFromEntity.setRuleId(ruleId);
                    mwAlertRuleDao.insertTengxunsmsRule(tengXunSmsFromEntity);
                }
                if(aliSmsParam != null){
                    aliSmsParam.setRuleId(ruleId);
                    encrypt(aliSmsParam);
                    mwAlertRuleDao.insertAliyunSMSRule(aliSmsParam);
                }
                break;
            case 16:
                WeLinkRuleParam weLinkRuleParam = param.getWeLinkRuleParam();
                weLinkRuleParam.setRuleId(ruleId);
                mwAlertRuleDao.insertWeLinkRule(weLinkRuleParam);
                break;
            case 17:
            case 18:
                HuaXingRuleParam huaXingRuleParam = param.getHuaXingRuleParam();
                huaXingRuleParam.setRuleId(ruleId);
                mwAlertRuleDao.insertHuaXingRule(huaXingRuleParam);
                break;
            case 24:
                HuaXingYuYinRuleParam huaXingYuYinRuleParam = param.getHuaXingYuYinRuleParam();
                huaXingYuYinRuleParam.setRuleId(ruleId);
                mwAlertRuleDao.insertHuaXingYuyinRule(huaXingYuYinRuleParam);
                break;
            default:
                break;
        }
        param.setPrincipal(param.getUserIds());
        mwCommonService.addMapperAndPerm(param);
        log.info("ACTION_LOG[]告警规则[]新增告警规则[]{}insertRule[]param{}", param);
        return Reply.ok();
//        } catch (Exception e) {
//            log.error("fail to insertRule with param={}, cause:{}", param, e);
//            return Reply.fail(ErrorConstant.ALERT_RULE_INSERT_CODE_300006, ErrorConstant.ALERT_RULE_INSERT_MAG_300006);
//        }
    }

    @Override
    @Transactional
    public Reply editorRule(AddAndUpdateAlertRuleParam param) {
        String loginName = iLoginCacheInfo.getLoginName();
        param.setModifier(loginName);
        mwAlertRuleDao.updateRule(param);
        String ruleId = param.getRuleId();
        int beforeActionType = mwAlertRuleDao.getActionTypeByRuleId(ruleId);

        switch (beforeActionType) {
            case 1:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_weixin_rule");
                break;
            case 2:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_dingding_rule");
                break;
            case 3:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_email_rule");
                break;
            case 4:
                break;
            case 5:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_apply_weixin_rule");
                break;
            case 7:
            case 28:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_dingdingqun_rule");
                break;
            case 8:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_aliyunsms_rule");
                break;
            case 9:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_shenzhenSMS_rule");
                break;
            case 10:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_tcp_udp_rule");
                break;
            case 11:
            case 12:
            case 19:
            case 21:
            case 26:
            case 27:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_caizhengju_sms_rule");
                break;
            case 13:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_tcp_udp_rule");
                break;
            case 14:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_aliyun_yuyin_rule");
                break;
            case 15:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_aliyunsms_rule");
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_tengxunsms_rule");
                break;
            case 16:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_welink_rule");
                break;
            case 17:
            case 18:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_huaxing_rule");
                break;
            case 24:
                mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_huaxing_yuyin_table");
                break;
            default:
                break;
        }

        Integer actionType = param.getActionType();
        switch (actionType) {
            case 1:
                WeiXinParam weiXinParam = param.getWeiXinParam();
                weiXinParam.setRuleId(ruleId);
                encrypt(weiXinParam);
                mwAlertRuleDao.insertWeiXinRule(weiXinParam);
                break;
            case 2:
                DingDingParam dingDingParam = param.getDingDingParam();
                dingDingParam.setRuleId(ruleId);
                encrypt(dingDingParam);
                mwAlertRuleDao.insertDingDingRule(dingDingParam);
                break;
            case 3:
                EmailParam emailParam = param.getEmailParam();
                emailParam.setRuleId(ruleId);
                if(emailParam.getLogo() != null){
                    emailParam.setUrl(filePath + "//file-upload//");
                }
                //加密密码相关字段
                encrypt(emailParam);

                mwAlertRuleDao.insertEmailRule(emailParam);
                break;
            case 4:
                break;
            case 5:
                ApplyWeiXinParam applyWeiXinParam = param.getApplyWeiXinParam();
                applyWeiXinParam.setRuleId(ruleId);
                encrypt(applyWeiXinParam);
                mwAlertRuleDao.insertApplyWeiXin(applyWeiXinParam);
                break;
            case 7:
            case 28:
                DingDingQunParam dingDingQunParam = param.getDingDingGroupParam();
                dingDingQunParam.setRuleId(ruleId);
                encrypt(dingDingQunParam);
                mwAlertRuleDao.insertDingDingQunRule(dingDingQunParam);
                break;
            case 8:
                AliyunSmsParam aliyunSmsParam = param.getAliyunSmsParam();
                aliyunSmsParam.setRuleId(ruleId);
                encrypt(aliyunSmsParam);
                mwAlertRuleDao.insertAliyunSMSRule(aliyunSmsParam);
                break;
            case 9:
                ShenzhenSmsParam shenzhenSmsParam = param.getShenzhenSmsparam();
                shenzhenSmsParam.setRuleId(ruleId);
                mwAlertRuleDao.insertShenzhenSMSRule(shenzhenSmsParam);
                break;
            case 10:
                SYSLogParam sysLogParam = param.getSysLogParam();
                sysLogParam.setRuleId(ruleId);
                mwAlertRuleDao.insertSYSLogRule(sysLogParam);
                break;
            case 11:
            case 12:
            case 19:
            case 21:
            case 26:
            case 27:
                MwCaiZhengTingSMSParam caiZhengTingSMSParam = param.getCaiZhengTingSMSParam();
                caiZhengTingSMSParam.setRuleId(ruleId);
                mwAlertRuleDao.insertCaiZhengTingSMsRule(caiZhengTingSMSParam);
                break;
            case 13:
                SYSLogParam guangZhouBank = param.getGuangZhouBankParam();
                guangZhouBank.setRuleId(ruleId);
                mwAlertRuleDao.insertSYSLogRule(guangZhouBank);
                break;
            case 14:
                AliYunYuYinlParam aliYunYuYinlParam = param.getAliYunYuYinParam();
                aliYunYuYinlParam.setRuleId(ruleId);
                mwAlertRuleDao.insertAliyunYuYinRule(aliYunYuYinlParam);
                break;
            case 15:
                TengXunSmsFromEntity tengXunSmsFromEntity = param.getTengXunSmsFromEntity();
                AliyunSmsParam aliSmsParam = param.getAliyunSmsParam();
                if(tengXunSmsFromEntity != null){
                    tengXunSmsFromEntity.setRuleId(ruleId);
                    mwAlertRuleDao.insertTengxunsmsRule(tengXunSmsFromEntity);
                }
                if(aliSmsParam != null){
                    aliSmsParam.setRuleId(ruleId);
                    encrypt(aliSmsParam);
                    mwAlertRuleDao.insertAliyunSMSRule(aliSmsParam);
                }
                break;
            case 16:
                WeLinkRuleParam weLinkRuleParam = param.getWeLinkRuleParam();
                weLinkRuleParam.setRuleId(ruleId);
                mwAlertRuleDao.insertWeLinkRule(weLinkRuleParam);
                break;
            case 17:
            case 18:
                HuaXingRuleParam huaXingRuleParam = param.getHuaXingRuleParam();
                huaXingRuleParam.setRuleId(ruleId);
                mwAlertRuleDao.insertHuaXingRule(huaXingRuleParam);
                break;
            case 24:
                HuaXingYuYinRuleParam huaXingYuYinRuleParam = param.getHuaXingYuYinRuleParam();
                huaXingYuYinRuleParam.setRuleId(ruleId);
                mwAlertRuleDao.insertHuaXingYuyinRule(huaXingYuYinRuleParam);
                break;
            default:
                break;
        }
        param.setPrincipal(param.getUserIds());
        mwCommonService.updateMapperAndPerm(param);
        log.info("ACTION_LOG[]告警规则[]修改告警规则[]{}editorRule-param[]{}", param);
        return Reply.ok();
    }

    public <T> void encrypt(T t) {
        //加密邮件密码相关字段
        if(t instanceof EmailParam) {
            EmailParam e = ((EmailParam) t);
            e.setEmailSendPassword(EncryptsUtil.encrypt(e.getEmailSendPassword()));
        }
        if(t instanceof WeiXinParam) {
            WeiXinParam e = ((WeiXinParam) t);
            e.setAppSecret(EncryptsUtil.encrypt(e.getAppSecret()));
            e.setAgentId(EncryptsUtil.encrypt(e.getAgentId()));
        }
        if(t instanceof DingDingParam) {
            DingDingParam e = ((DingDingParam) t);
            e.setAppSecret(EncryptsUtil.encrypt(e.getAppSecret()));
        }
        if(t instanceof ApplyWeiXinParam) {
            ApplyWeiXinParam e = ((ApplyWeiXinParam) t);
            e.setSecret(EncryptsUtil.encrypt(e.getSecret()));
            e.setApplyId(EncryptsUtil.encrypt(e.getApplyId()));
            e.setAgentId(EncryptsUtil.encrypt(e.getAgentId()));
        }
        if(t instanceof AliyunSmsParam) {
            AliyunSmsParam e = ((AliyunSmsParam) t);
            e.setAccessKeySecret(EncryptsUtil.encrypt(e.getAccessKeySecret()));
            e.setAccessKeyId(EncryptsUtil.encrypt(e.getAccessKeyId()));
            e.setTemplateCode(EncryptsUtil.encrypt(e.getTemplateCode()));
        }
        if(t instanceof DingDingQunParam) {
            DingDingQunParam e = ((DingDingQunParam) t);
            e.setKeyWord(EncryptsUtil.encrypt(e.getKeyWord()));
        }
    }

    public  void decrypt(AddAndUpdateAlertRuleParam data) {
        EmailParam email = data.getEmailParam();
        if(email !=null){
            try {
                if (email.getEmailSendPassword() != null) {
                    email.setEmailSendPassword(EncryptsUtil.decrypt(email.getEmailSendPassword()));
                }
            } catch (Exception e) {
                log.error("解密失败:{}",e);
            }
        }
        WeiXinParam weiXin = data.getWeiXinParam();
        if(weiXin != null){
            try {
                if (weiXin.getAppSecret() != null) {
                    weiXin.setAppSecret(EncryptsUtil.decrypt(weiXin.getAppSecret()));
                    weiXin.setAgentId(EncryptsUtil.decrypt(weiXin.getAgentId()));
                }
            } catch (Exception e) {
                log.error("解密失败:{}",e);
            }
        }
        DingDingParam dingDing = data.getDingDingParam();
        if(dingDing != null){
            try {
                if (dingDing.getAppSecret() != null) {
                    dingDing.setAppSecret(EncryptsUtil.decrypt(dingDing.getAppSecret()));
                }
            } catch (Exception e) {
                log.error("解密失败:{}",e);
            }
        }
        ApplyWeiXinParam applyWeiXin = data.getApplyWeiXinParam();
        if(applyWeiXin != null){
            try {
                if (applyWeiXin.getSecret() != null) {
                    applyWeiXin.setSecret(EncryptsUtil.decrypt(applyWeiXin.getSecret()));
                    applyWeiXin.setAgentId(EncryptsUtil.decrypt(applyWeiXin.getAgentId()));
                    applyWeiXin.setApplyId(EncryptsUtil.decrypt(applyWeiXin.getApplyId()));
                }
            } catch (Exception e) {
                log.error("解密失败:{}",e);
            }
        }
        AliyunSmsParam aliYun = data.getAliyunSmsParam();
        if(aliYun != null){
            try {
                if (aliYun.getAccessKeySecret() != null) {
                    aliYun.setAccessKeySecret(EncryptsUtil.decrypt(aliYun.getAccessKeySecret()));
                    aliYun.setAccessKeyId(EncryptsUtil.decrypt(aliYun.getAccessKeyId()));
                    aliYun.setTemplateCode(EncryptsUtil.decrypt(aliYun.getTemplateCode()));
                }
            } catch (Exception e) {
                log.error("解密失败:{}",e);
            }
        }
        DingDingQunParam dingDingQunParam = data.getDingDingGroupParam();
        if(dingDingQunParam != null){
            try {
                if (dingDingQunParam.getWebHook() != null) {
                    dingDingQunParam.setKeyWord(EncryptsUtil.decrypt(dingDingQunParam.getKeyWord()));
                }
            } catch (Exception e) {
                log.error("解密失败:{}",e);
            }
        }
    }

    @Override
    @Transactional
    public Reply deleteRule(List<MwAlertRuleParam> params) {

        List<String> ruleIds = new ArrayList<>();
        if (params.size() > 0) {
            for (MwAlertRuleParam mwAlertRuleParam : params) {
                String ruleId = mwAlertRuleParam.getRuleId();
                ruleIds.add(ruleId);
            }
        }
        int count = mwAlertRuleDao.selectRuleActionCount(ruleIds);
            if(count>0){
                throw  new RuntimeException("规则关联了告警动作无法删除");
            }
        if (params.size() > 0) {
            for (MwAlertRuleParam mwAlertRuleParam : params) {
                String ruleId = mwAlertRuleParam.getRuleId();
                ruleIds.add(ruleId);
                Integer actionType = mwAlertRuleParam.getActionType();
                switch (actionType) {
                    case 1:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_weixin_rule");
                        break;
                    case 2:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_dingding_rule");
                        break;
                    case 3:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_email_rule");
                        break;
                    case 4:
                        break;
                    case 5:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_apply_weixin_rule");
                        break;
                    case 7:
                    case 28:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_dingdingqun_rule");
                        break;
                    case 8:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_aliyunsms_rule");
                        break;
                    case 9:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_shenzhenSMS_rule");
                        break;
                    case 10:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_tcp_udp_rule");
                        break;
                    case 11:
                    case 12:
                    case 19:
                    case 21:
                    case 26:
                    case 27:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_caizhengju_sms_rule");
                        break;
                    case 13:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_tcp_udp_rule");
                        break;
                    case 14:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_aliyun_yuyin_rule");
                        break;
                    case 15:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_aliyunsms_rule");
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_tengxunsms_rule");
                        break;
                    case 16:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_welink_rule");
                        break;
                    case 17:
                    case 18:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_huaxing_rule");
                        break;
                    case 24:
                        mwAlertRuleDao.deleteTypeRule(ruleId, "mw_alert_huaxing_yuyin_table");
                        break;
                    default:
                        break;
                }
            }
        }
        mwAlertRuleDao.deleteRule(ruleIds);
        mwAlertRuleDao.deleteRuleActionMapper(ruleIds);
        //删除对应报表的规则mapper
        mwAlertRuleDao.deleteRuleReportMapper(ruleIds);
        DeleteDto deleteDto = DeleteDto.builder().typeIds(ruleIds).type(DataType.RULE.getName()).build();
        mwCommonService.deleteMapperAndPerms(deleteDto);

        log.info("ACTION_LOG[]告警规则[]删除告警规则[]{}deleteRule-ruleId[]{}", params);
        return Reply.ok();
    }

    @Override
    public Reply getActionType() {
        try {
            List<Map> list = mwAlertRuleDao.getActionType();
//            Map<Integer, String> map = new HashMap<>();
//            map.put(1, "微信");
//            map.put(2, "钉钉");
//            map.put(3, "邮箱");
            log.info("ACTION_LOG[]告警规则[]查询告警通知类型[]");
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getActionType with param={}, cause:{}", e);
            return Reply.fail(ErrorConstant.ALERT_ACTION_TYPE_CODE_300009, ErrorConstant.ALERT_ACTION_TYPE_MAG_300009);
        }
    }

    @Override
    public Reply getRuleListByActionTypeIds(List<Integer> actionIds) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> idList = mwUserService.getAllTypeIdList(userInfo,DataType.RULE);
            Map<String, Object> pubCriteria = new HashMap<String, Object>();
            pubCriteria.put("actionIds",actionIds);
            pubCriteria.put("isSystem",userInfo.isSystemUser());
            if(CollectionUtils.isNotEmpty(idList)){
                if(idList.get(0) != null){
                    pubCriteria.put("listSet", Joiner.on(",").join(idList));
                }
            }
            List<Map> list = mwAlertRuleDao.getRuleListByActionTypeIds(pubCriteria);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getRuleListByActionTypeIds with param={}, cause:{}", e);
            return Reply.fail(ErrorConstant.ALERT_ACTION_RULE_BROWSE_CODE_300015, ErrorConstant.ALERT_ACTION_RULE_BROWSE_MAG_300015);
        }
    }

    @Override
    public Reply selectRuleById(String ruleId) {
        try {
            AddAndUpdateAlertRuleParam alertRuleTable = mwAlertRuleDao.selectRuleById(ruleId);
            switch (alertRuleTable.getActionType()){
                case 1:
                    WeiXinParam weiXinParam = mwAlertRuleDao.selectWeixin(ruleId);
                    alertRuleTable.setWeiXinParam(weiXinParam);
                    break;
                case 3:
                    EmailParam emailParam = mwAlertRuleDao.selectEmail(ruleId);
                    alertRuleTable.setEmailParam(emailParam);
                    break;
                case 5:
                    ApplyWeiXinParam applyWeiXinParam = mwAlertRuleDao.selectApplyWeixin(ruleId);
                    alertRuleTable.setApplyWeiXinParam(applyWeiXinParam);
                    break;
                case 7:
                case 28:
                    DingDingQunParam dingDingQunParam = mwAlertRuleDao.selectDingDingQun(ruleId);
                    alertRuleTable.setDingDingGroupParam(dingDingQunParam);
                    break;
                case 8:
                    AliyunSmsParam aliyunSmsParam = mwAlertRuleDao.selectAliyunSms(ruleId);
                    alertRuleTable.setAliyunSmsParam(aliyunSmsParam);
                    break;
                case 9:
                    ShenzhenSmsParam shenzhenSmsParam = mwAlertRuleDao.selectShenzhenSms(ruleId);
                    alertRuleTable.setShenzhenSmsparam(shenzhenSmsParam);
                    break;
                case 10:
                    SYSLogParam sysLogParam = mwAlertRuleDao.selectSysLog(ruleId);
                    alertRuleTable.setSysLogParam(sysLogParam);
                    break;
                case 11:
                case 12:
                case 19:
                case 21:
                case 26:
                case 27:
                    MwCaiZhengTingSMSParam caiZhengTingSMSParam = mwAlertRuleDao.selectCaiZhengTingSMS(ruleId);
                    alertRuleTable.setCaiZhengTingSMSParam(caiZhengTingSMSParam);
                    break;
                case 13:
                    SYSLogParam guangZhouBank = mwAlertRuleDao.selectSysLog(ruleId);
                    alertRuleTable.setGuangZhouBankParam(guangZhouBank);
                    break;
                case 14:
                    AliYunYuYinlParam aliYunYuYinlParam = mwAlertRuleDao.findAliyunYuYinMessage(ruleId);
                    alertRuleTable.setAliYunYuYinParam(aliYunYuYinlParam);
                    break;
                case 15:
                    TengXunSmsFromEntity tengXunSmsFromEntity = mwAlertRuleDao.findTengxunsmsMessage(ruleId);
                    alertRuleTable.setTengXunSmsFromEntity(tengXunSmsFromEntity);
                    /*HuaWeiSmsFromEntity huaWeiSmsFromEntity = mwAlertRuleDao.findHuaweismsMessage(ruleId);
                    alertRuleTable.setHuaWeiSmsFromEntity(huaWeiSmsFromEntity);*/
                    AliyunSmsParam aliSmsParam = mwAlertRuleDao.selectAliyunSms(ruleId);
                    alertRuleTable.setAliyunSmsParam(aliSmsParam);
                    break;
                case 16:
                    WeLinkRuleParam weLinkRuleParam = mwAlertRuleDao.findWeLinkMessage(ruleId);
                    alertRuleTable.setWeLinkRuleParam(weLinkRuleParam);
                    break;
                case 17:
                case 18:
                    HuaXingRuleParam huaXingRuleParam = mwAlertRuleDao.findHuaXingMessage(ruleId);
                    alertRuleTable.setHuaXingRuleParam(huaXingRuleParam);
                    break;
                case 24:
                    HuaXingYuYinRuleParam huaXingYuYinRuleParam = mwAlertRuleDao.findHuaXingYuyinMessage(ruleId);
                    alertRuleTable.setHuaXingYuYinRuleParam(huaXingYuYinRuleParam);
                    break;
                default:
                    break;
            }
            //密码字段相关还原
            decrypt(alertRuleTable);
            DataPermission dataPermission = mwCommonService.getDataPermissionDetail(DataType.RULE,ruleId);
            alertRuleTable.setGroupIds(dataPermission.getGroupIds());
            alertRuleTable.setUserIds(dataPermission.getUserIds());
            alertRuleTable.setOrgIds(dataPermission.getOrgNodes());
           /* // usergroup重新赋值使页面可以显示
            if (null != alertRuleTable.getGroups() && alertRuleTable.getGroups().size() > 0) {
                List<Integer> groupIds = new ArrayList<>();
                alertRuleTable.getGroups().forEach(
                        groupDTO -> groupIds.add(groupDTO.getGroupId())
                );
                alertRuleTable.setGroupIds(groupIds);
            }
            // user重新赋值
            if (null != alertRuleTable.getPrincipal() && alertRuleTable.getPrincipal().size() > 0) {
                List<Integer> userIds = new ArrayList<>();
                alertRuleTable.getPrincipal().forEach(
                        userDTO -> userIds.add(userDTO.getUserId())
                );
                alertRuleTable.setUserIds(userIds);
            }
            // 机构重新赋值使页面可以显示
            if (null != alertRuleTable.getDepartment() && alertRuleTable.getDepartment().size() > 0) {
                List<List<Integer>> orgNodes = new ArrayList<>();
                if (null != alertRuleTable.getDepartment() && alertRuleTable.getDepartment().size() > 0) {
                    alertRuleTable.getDepartment().forEach(department -> {
                                List<Integer> orgIds = new ArrayList<>();
                                List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                                nodes.forEach(node -> {
                                    if (!"".equals(node))
                                        orgIds.add(Integer.valueOf(node));
                                });
                                orgNodes.add(orgIds);
                            }
                    );
                    alertRuleTable.setOrgIds(orgNodes);
                }
            }*/
            return Reply.ok(alertRuleTable);
        } catch (Exception e) {
            log.error("fail to selectRuleById with param={}, cause:{}", ruleId, e);
            return Reply.fail(ErrorConstant.ALERT_ACTION_RULE_BROWSE_CODE_300015, ErrorConstant.ALERT_ACTION_RULE_BROWSE_MAG_300015);
        }
    }

    @Override
    public Reply selectRuleList(MwAlertRuleParam mwAlertRuleParam) {
        try {
            List<AlertRuleTable> list = getRuleList(mwAlertRuleParam);
            PageList pageList = new PageList();
            List newList = pageList.getList(list, mwAlertRuleParam.getPageNumber(), mwAlertRuleParam.getPageSize());
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(mwAlertRuleParam.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setPageSize(mwAlertRuleParam.getPageSize());
            pageInfo.setList(newList);
            log.info("ACCESS_LOG[]zbx_action[]告警管理[]分页查询告警规则[]{}[]", mwAlertRuleParam);
            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to selectRuleList with param={}, cause:{}", mwAlertRuleParam, e);
            return Reply.fail(ErrorConstant.ALERT_RULE_SELECT_CODE_300005, ErrorConstant.ALERT_RULE_SELECT_MAG_300005);

        }
    }

    public List<AlertRuleTable> getRuleList(MwAlertRuleParam mwAlertRuleParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        List<String> idList = mwUserService.getAllTypeIdList(userInfo,DataType.RULE);
        Map pubCriteria = PropertyUtils.describe(mwAlertRuleParam);
        pubCriteria.put("isSystem",userInfo.isSystemUser());
        List<AlertRuleTable> list = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(idList)){
            if(idList.get(0) != null){
                pubCriteria.put("listSet", Joiner.on(",").join(idList));
            }
        }
        list = mwAlertRuleDao.selectRuleList(pubCriteria);
        if(CollectionUtils.isNotEmpty(list)){
            for(AlertRuleTable art : list){
                DataPermission dataPermission = mwCommonService.getDataPermissionDetail(DataType.RULE,art.getRuleId());
                art.setUserDTO(dataPermission.getPrincipal());
                art.setGroups(dataPermission.getGroups());
                art.setDepartment(dataPermission.getDepartment());
                if(CollectionUtils.isNotEmpty(dataPermission.getDepartment())){
                    StringBuilder departmentString = new StringBuilder();
                    for(OrgDTO org : dataPermission.getDepartment()){
                        departmentString.append(org.getOrgName()).append(",");
                    }
                    art.setDepartmentString(departmentString.deleteCharAt(departmentString.length()-1).toString());

                }
                if(CollectionUtils.isNotEmpty(dataPermission.getGroups())){
                    StringBuilder groupsString = new StringBuilder();
                    for(GroupDTO group : dataPermission.getGroups()){
                        groupsString.append(group.getGroupName()).append(",");
                    }
                    art.setGroupsString(groupsString.deleteCharAt(groupsString.length()-1).toString());
                }
                if(CollectionUtils.isNotEmpty(dataPermission.getPrincipal())){
                    StringBuilder userDTOString = new StringBuilder();
                    for(UserDTO user : dataPermission.getPrincipal()){
                        userDTOString.append(user.getUserName()).append(",");
                    }
                    art.setGroupsString(userDTOString.deleteCharAt(userDTOString.length()-1).toString());
                }

            }
        }
        return list;
    }

    @Override
    public Reply fuzzSeach(MwAlertRuleParam mwAlertRuleParam) {
        try {
            List<AlertRuleTable> list = getRuleList(mwAlertRuleParam);
            HashSet<String> ruleNameFuzzyQuery = new HashSet<String>();
            HashSet<String> ruleDescFuzzyQuery = new HashSet<String>();
            HashSet<String> creatorFuzzyQuery = new HashSet<String>();
            HashSet<String> modifierFuzzyQuery = new HashSet<String>();
            HashSet<String> result = new HashSet<String>();
            if(list.size() > 0 && list != null){
                for(AlertRuleTable s : list){
                    if(StringUtils.isNotEmpty(s.getRuleName()) && s.getRuleName().toUpperCase().contains(mwAlertRuleParam.getFuzzyQuery())){
                        ruleNameFuzzyQuery.add(s.getRuleName());
                        result.add(s.getRuleName());
                    }
                    if(StringUtils.isNotEmpty(s.getRuleDesc()) && s.getRuleDesc().toUpperCase().contains(mwAlertRuleParam.getFuzzyQuery())){
                        ruleDescFuzzyQuery.add(s.getRuleDesc());
                        result.add(s.getRuleDesc());
                    }
                    if(StringUtils.isNotEmpty(s.getCreator()) && s.getCreator().toUpperCase().contains(mwAlertRuleParam.getFuzzyQuery())){
                        creatorFuzzyQuery.add(s.getCreator());
                        result.add(s.getCreator());
                    }
                    if(StringUtils.isNotEmpty(s.getModifier()) && s.getModifier().toUpperCase().contains(mwAlertRuleParam.getFuzzyQuery())){
                        modifierFuzzyQuery.add(s.getModifier());
                        result.add(s.getModifier());
                    }
                }
            }
            Map<String,List<String>> fuzzyQuery = new HashMap<>();
            fuzzyQuery.put("ruleName",sort(ruleNameFuzzyQuery));
            fuzzyQuery.put("ruleDesc",sort(ruleDescFuzzyQuery));
            fuzzyQuery.put("creator",sort(creatorFuzzyQuery));
            fuzzyQuery.put("modifier",sort(modifierFuzzyQuery));
            fuzzyQuery.put("fuzzyQuery",sort(result));
            return Reply.ok(fuzzyQuery);
        } catch (Exception e) {
            log.error("fail to selectRuleList with param={}, cause:{}", mwAlertRuleParam, e);
            return Reply.fail(ErrorConstant.ALERT_RULE_SELECT_CODE_300005, ErrorConstant.ALERT_RULE_SELECT_MAG_300005);

        }
    }

    //排序不分区英文大小写
    public List<String> sort(HashSet<String> result){
        List<String> ts = new ArrayList<>(result);
        Collections.sort(ts, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (o1.compareToIgnoreCase(o2) == 0 ? o1.compareTo(o2) : o1.compareToIgnoreCase(o2));
            }
        });
        return ts;
    }


    @Override
    public Reply sendTest(AddAndUpdateAlertRuleParam param){
        String response = "发送失败！";
        Integer actionType = param.getActionType();
        AlertRuleTableCommons alertRuleTable = new AlertRuleTableCommons();
        alertRuleTable.setProxyAccount(param.getProxyAccount());
        alertRuleTable.setProxyIp(param.getProxyIp());
        alertRuleTable.setProxyPassword(param.getProxyPassword());
        alertRuleTable.setProxyPort(param.getProxyPort());
        alertRuleTable.setProxyState(param.getProxyState());
        switch (actionType) {
            case 1:
                //获取token
                WeiXinParam weiXinParam = param.getWeiXinParam();
                //获取用户openid
                List<String> tousers =  mwAlertRuleDao.selectWeixinUserId(param.getTestUserId());
                HashMap<String, String> wxmap = new HashMap<>();
                wxmap.put("appid",weiXinParam.getAgentId());
                wxmap.put("secret",weiXinParam.getAppSecret());
                wxmap.put("templateid",weiXinParam.getAlertTempleate());
                wxmap.put("keyword1",param.getTestMessage());
                try{
                    for (String touser : tousers){
                        String token = WeiXinSendUtil.getAccessToken(weiXinParam.getAgentId(),weiXinParam.getAppSecret(),alertRuleTable);
                        WeiXinSendUtil.send(touser, wxmap,token,alertRuleTable);
                    }
                    response = "发送成功！";
                } catch (IOException e) {
                    response = e.getMessage();
                    log.error("微信测试发送失败：" + e);
                }
                break;
            case 2:
                /*DingDingParam dingDingParam = param.getDingDingParam();
                dingDingParam.setRuleId(ruleId);
                encrypt(dingDingParam);
                mwAlertRuleDao.insertDingDingRule(dingDingParam);*/
                break;
            case 3:
                EmailParam emailParam = param.getEmailParam();
                List<String> emails = mwAlertRuleDao.selectEmailByUser(param.getTestUserId());
                List<String> sendEmails = emails.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
                String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
                EmailFrom emailFrom = new EmailFrom();
                emailFrom.setIsSsl(emailParam.getIsSsl());
                emailFrom.setPassword(emailParam.getEmailSendPassword());
                emailFrom.setPersonal(emailParam.getPersonal());
                emailFrom.setPort(Integer.parseInt(emailParam.getEmailServerPort()));
                emailFrom.setUsername(emailParam.getEmailSendUserName());
                emailFrom.setHostName(emailParam.getEmailServerAddress());
                emailFrom.setUrl(filePath + "//file-upload//");
                emailFrom.setLogo(emailParam.getLogo());
                emailFrom.setIsLogo(emailParam.getIsLogo());
                emailFrom.setIsDelsuffix(emailParam.getIsDelsuffix());
                try{
                    Boolean isHtml = false;
                    if(param.getIsHtml() != null){
                        isHtml = param.getIsHtml();
                    }
                    response = emailSendUtil.sendTextEmail(tos, emailFrom, emailParam.getEmailHeaderTitle() + "告警通知", param.getTestMessage(),isHtml);
                }catch (Exception e){
                    response = "邮件测试发送失败：" + e.getMessage();
                    log.error("邮件测试发送失败：" + e);
                }

                /*EmailParam emailParam = param.getEmailParam();
                emailParam.setRuleId(ruleId);

                //加密密码相关字段
                encrypt(emailParam);

                mwAlertRuleDao.insertEmailRule(emailParam);*/
                break;
            case 4:
                break;
            case 5:
                ApplyWeiXinParam applyWeiXinParam = param.getApplyWeiXinParam();
                GeneralMessageEntity qiEntity = new GeneralMessageEntity();
                qiEntity.setAgentId(applyWeiXinParam.getAgentId());
                qiEntity.setId(applyWeiXinParam.getApplyId());
                qiEntity.setSecret(applyWeiXinParam.getSecret());
                List<String> list = mwAlertRuleDao.selectQyWeixinUserId(param.getTestUserId());
                //处理需要userIds,转换格式
                String touser = getSendTouer(list);
                HashMap<String, Object> sendDataMap = new HashMap<>();
                sendDataMap.put("touser", touser);
                sendDataMap.put("msgtype", "textcard");
                sendDataMap.put("agentid", qiEntity.getAgentId());
                HashMap<String, String> firstdata = new HashMap<>();
                firstdata.put("title", "测试\n ");
                firstdata.put("btntxt", "详情");
                firstdata.put("description", param.getTestMessage());
                firstdata.put("url", "URL");
                sendDataMap.put("textcard", firstdata);
                String sendStr = JSON.toJSONString(sendDataMap);
                try{
                    response = QyWeixinSendUtil.sendQyWeixinMessage(sendStr, qiEntity,alertRuleTable);
                }catch (Exception e) {
                    response = e.getMessage();
                    log.error("企业微信测试发送失败：" + e);
                }

                break;
            case 7:
            case 28:
                DingDingQunParam dingDingQunParam = param.getDingDingGroupParam();
                response = DingdingQunSendUtil.sendMessage(param.getTestMessage(),dingDingQunParam.getWebHook(),dingDingQunParam.getSecret());
                break;
            case 8:
                AliyunSmsParam aliyunSmsParam = param.getAliyunSmsParam();
                List<String> phones = mwAlertRuleDao.selectPhones(param.getTestUserId());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title",param.getTestMessage());
                jsonObject.put("devicename",param.getTestMessage());
                jsonObject.put("domain",param.getTestMessage());
                jsonObject.put("Address",param.getTestMessage());
                jsonObject.put("faultTime","");
                jsonObject.put("renewTime","");
                jsonObject.put("renewInfo",param.getTestMessage());
                jsonObject.put("renewStatus",param.getTestMessage());
                try{
                    String phone = phones.toString().replaceAll(" ","").replaceAll("]","").replaceAll("\\[","");
                    response = AliyunDxSendUtil.send(aliyunSmsParam.getSignName(), aliyunSmsParam.getTemplateCode(), phone, jsonObject.toJSONString(), aliyunSmsParam.getAccessKeyId(), aliyunSmsParam.getAccessKeySecret(),alertRuleTable);
                    JSONObject json = JSON.parseObject(response);
                    if(json.get("Code").equals("OK")){
                        response = "发送成功！";
                    }
                }catch (Exception e) {
                    response = e.getMessage();
                    log.error("阿里短信测试发送失败：" + e);
                }
                break;
            case 9:
                /*ShenzhenSmsParam shenzhenSmsParam = param.getShenzhenSmsparam();
                shenzhenSmsParam.setRuleId(ruleId);
                mwAlertRuleDao.insertShenzhenSMSRule(shenzhenSmsParam);*/
                break;
            case 13:
                try{
                    SYSLogParam guangZhouBankParam = param.getGuangZhouBankParam();
                    List<String> phonenums = mwAlertRuleDao.selectPhones(param.getTestUserId());
                    for(String s : phonenums){
                        String msg = s + param.getTestMessage();
                        Integer isSuccess = TCP_UDPSendUtil.UDPSend(guangZhouBankParam.getHost(), guangZhouBankParam.getPort(), msg);
                        log.info("广州银行接收人：" + msg);
                        log.info("广州银行发送结果：" + isSuccess);
                    }
                    response = "发送成功！";
                }catch (Exception e){
                    response = e.getMessage();
                    log.error("广州银行测试发送失败：" + e);
                }
                break;
            case 14:
                try{
                    AliYunYuYinlParam aliYunYuYinParam = param.getAliYunYuYinParam();
                    List<String> phone = mwAlertRuleDao.selectPhones(param.getTestUserId());
                    String info = param.getTestMessage();
                    String hostname = param.getTestMessage();
                    JSONObject json = new JSONObject();
                    json.put("info",info);
                    json.put("hostname", hostname);
                    for(String s : phone){
                        aliYunYuYinParam.setCalledNumber(s);
                        String isSuccess = AliyunYuYinSendUtil.sendQyWeixinMessage(aliYunYuYinParam,json.toString());
                        log.info("阿里云语音：" + s);
                        log.info("阿里云语音发送结果：" + isSuccess);
                    }
                    response = "发送成功！";
                }catch (Exception e){
                    response = e.getMessage();
                    log.error("阿里云语音测试发送失败：" + e);
                }
                break;
            case 22:
                log.info("华星光电接口开始请求");
                String url = DB_URL;
                HashMap<String,String> params = new HashMap<>();
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(new Date());
                String userName = "测试";
                String telephone = "122313322";
                String userEmail = "92899@qq.com";
                String department = "1";
                String itsmContacts = "2";
                params.put("curHandlers", "zoucaifeng");//描述
                params.put("secondHandler", "zoucaifeng");//267:公司268:生产厂269:产线270:个人
                params.put("secondHanderName", "邹彩凤");//274:一般275:低
                params.put("occurTime", format);//发生时间
                params.put("contactName", "程碧凡");//联系人
                params.put("contactEmail", "chengbifan@tcl.com");//联系人邮箱
                params.put("contactDept", "中小尺寸客户服务科");//申请人部门
                params.put("contactCompany", "GC00f");//申请人部门
                params.put("contactCode", "20960");//申请人部门
                params.put("status", "已分配");//申请人部门
                params.put("statusCode", "INCIDENT_ASSIGN");//申请人部门
                params.put("curNodes", "N1");//申请人部门
                params.put("companyCode", "1000");//申请人部门
                params.put("factoryName", "t1");//申请人部门
                params.put("factoryCode", "t1");//申请人部门
                params.put("subWorkScop", "领域名称0614001");//申请人部门
                params.put("subWorkScopCode", "0614001");//申请人部门
                params.put("subWorkScopOwner", "zoucaifeng");//申请人部门
                params.put("systemLevel", "C");//申请人部门
                params.put("workScop", "ITSM");//申请人部门
                params.put("workScopCode", "1032-50001");//申请人部门
                params.put("workScopPick", "ITSM > 领域名称0614001");//申请人部门
                params.put("caseSource", "邮件");//申请人部门
                params.put("incidentTypeCode", "incident");//申请人部门
                params.put("incidentTypeName", "突发事件");//申请人部门
                params.put("caseDesc", "测试");//申请人部门
                params.put("effectScopCode", "user");//申请人部门
                params.put("effectScop", "个人");//申请人部门
                params.put("emergencyCode", "normal");//申请人部门
                params.put("emergency", "一般");//申请人部门
                params.put("priorityCode", "INCIDENT_PRIORITY_III");//申请人部门

                params.put("priorityName", "三级");//申请人部门
                params.put("serviceName", "OA系统");//申请人部门
                params.put("serviceCode", "378");//申请人部门
                params.put("caseLocation", "test");//申请人部门
                params.put("caseLocationCode", "333");//申请人部门
                params.put("totalInterruptFlag", "false");//申请人部门

                response = LoadUtil.post(DB_URL, JSON.toJSONString(params),null);
                log.info("华星光电接口返回：" + response);
                break;
            default:
                break;
        }
        return Reply.ok(response);
    }

    public String getSendTouer(List<String> userIds) {
        StringBuffer touser = new StringBuffer();
        HashSet<String> userIds1 = (HashSet<String>) userIds.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        if (userIds1 != null && userIds1.size() > 0 && userIds1.size() <= 1000) {
            Iterator<String> iterator = userIds1.iterator();
            while (iterator.hasNext()) {
                touser.append("|").append(iterator.next());
            }
        }else {
            return "";
        }
        touser.replace(0, 1, "");
        return touser.toString();
    }

}
