package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.param.ActionLevelRuleParam;
import cn.mw.monitor.alert.param.ApplyWeiXinParam;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.QyWeixinSendUtil;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import cn.mw.monitor.weixin.entity.UserInfo;
import cn.mw.monitor.weixin.entity.WeixinFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mw.monitor.weixin.service.WxPortalService;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业微信发送实现类
 */
public class QyWxSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收人（多）
    private String touser;

    //发送方（企业微信）
    private GeneralMessageEntity qyEntity;

    private WxPortalService wxPortalService;

    private String title;

    private AlertRuleTableCommons alertRuleTable;

    private String tempType = env.getProperty("alert.level");

    private String qyUrl = env.getProperty("qyweixin.url");

    private ActionLevelRuleParam alr;

    public QyWxSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId, ActionLevelRuleParam alr) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        this.alr = alr;
        WxPortalService wxPortalService = ApplicationContextProvider.getBean(WxPortalService.class);
        this.wxPortalService = wxPortalService;

    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("企业微信sendMessage：" + sendMessage);
        Integer errcode = -1;
        String erroMessage ="";
        try {
            erroMessage = QyWeixinSendUtil.sendQyWeixinMessage(sendMessage,qyEntity,alertRuleTable);
            log.info("企业微信发送结果：" + erroMessage);
            errcode = Integer.parseInt(JSON.parseObject(erroMessage).get("errcode").toString());
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message qiyeweixin:",e);
        }finally {
            saveHis("企业微信",sendMessage,errcode,map.get("事件ID"),erroMessage,title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {
        String context = super.dealMessage();
        StringBuffer url = new StringBuffer(qyUrl);
        String topic = isAlarm ? map.get(AlertEnum.ALERTTITLE.toString()) : map.get(AlertEnum.RECOVERYTITLE.toString());
        String alertInfo = isAlarm ? (map.get(AlertEnum.ALERTINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.PROBLEMDETAILS.toString())) : (map.get(AlertEnum.RECOVERYINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.RECOVERYDETAILS.toString()));
        String alertLevel = isAlarm ? map.get(AlertEnum.ALERTLEVEL.toString()) : map.get(AlertEnum.RECOVERYLEVEL.toString());
        String getUserName = getUserName(userIds);
        String nowState = isAlarm ? map.get(AlertEnum.NOWSTATE.toString()) : map.get(AlertEnum.RENEWSTATUS.toString());
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        title = isAlarm ? "告警事件-通知" : "告警事件-恢复";
        String time = isAlarm ? map.get(AlertEnum.ALERTTIME.toString()) : map.get(AlertEnum.RECOVERYTIME.toString());
        if(tempType.equals(AlertEnum.HUAXING.toString())) {
            StringBuffer sb = new StringBuffer();
            sb.append(AlertEnum.MODELSYSTEM.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.MODELSYSTEM.toString())).append('\n')
                    .append(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append('\n');
            sb.append(AlertEnum.TOPIC.toString() + AlertAssetsEnum.COLON.toString()).append(topic).append('\n')
                    .append(AlertEnum.ALERTINFO.toString() + AlertAssetsEnum.COLON.toString()).append(alertInfo).append('\n')
                    .append(AlertEnum.APPCATIONINFO.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Specifications.toString())).append('\n')
                    .append(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Domain.toString())).append('\n')
                    .append(AlertEnum.ALERTLEVEL.toString() + AlertAssetsEnum.COLON.toString()).append(MWAlertLevelParam.actionAlertLevelMap.get(alertLevel)).append('\n');

            sb.append(AlertEnum.PERSON.toString() + AlertAssetsEnum.COLON.toString()).append(getUserName).append('\n');
            if(isAlarm){
                if(alr != null && alr.getIsActionLevel()!=null && alr.getIsActionLevel()){
                    title = "告警事件-升级";
                    sb.append(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append('\n');
                }else {
                    sb.append(AlertEnum.ALERTSTARTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append('\n')
                            .append(AlertEnum.SENDTIEM.toString() + AlertAssetsEnum.COLON.toString()).append(df.format(date)).append('\n');
                }
            }else {
                sb.append(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.FAILURETIME.toString())).append('\n')
                        .append(AlertEnum.CLOSETIME.toString()+ AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTIME.toString())).append('\n');
                long clock = 0;
                try {
                    clock = df.parse(map.get(AlertEnum.RECOVERYTIME.toString())).getTime() - df.parse(map.get(AlertEnum.FAILURETIME.toString())).getTime();
                    clock = clock/1000;
                } catch (Exception e) {
                    log.error("时间转换失败:{}", e);
                }
                sb.append(AlertEnum.LONGTIMEZH.toString() + AlertAssetsEnum.COLON.toString()).append(SeverityUtils.getLastTime(clock)).append('\n');
            }
            sb.append(AlertEnum.NOWSTATE.toString() + AlertAssetsEnum.COLON.toString()).append(nowState).append('\n');
            sb.append(AlertEnum.EVENTID.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.EVENTID.toString()));
            context = sb.toString();
        }
        if(tempType.equals(AlertEnum.SHENGRENYI.toString())) {
            String suffix = isAlarm ? "对象故障" : "恢复正常,撤销告警";

            HashMap<String, Object> sendDataMap = new HashMap<>();
            StringBuffer sb = new StringBuffer();
            sb.append(time).append(" ").append(map.get(AlertEnum.IPAddress.toString())).append(" ").append(topic).append(" ").append(suffix);
            context = sb.toString();
            sendDataMap.put("touser", touser);
            sendDataMap.put("msgtype", "text");
            sendDataMap.put("agentid", qyEntity.getAgentId());
            HashMap<String, String> contentMap = new HashMap<>();
            contentMap.put("content",context);
            sendDataMap.put("text",contentMap);
            return JSON.toJSONString(sendDataMap);
        }
        try{
            url.append(AlertAssetsEnum.QUESTION.toString())
                    .append(AlertEnum.TITLE.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(title,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.HOSTNAME.toString().toLowerCase()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.HostNameZH.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.IP.toString().toLowerCase()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.IPAddress.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.TOPICEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(topic,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.ALERTINFOEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(alertInfo,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.ALERTLEVELEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(MWAlertLevelParam.actionAlertLevelMap.get(alertLevel),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.NOWSTATEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(nowState,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.EVENTIDEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.EVENTID.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString());
            if(map.containsKey(AlertEnum.MODELSYSTEM.toString()) && map.get(AlertEnum.MODELSYSTEM.toString()) != null){
                url.append(AlertEnum.MODELSYSTEMEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(map.get(AlertEnum.MODELSYSTEM.toString())).append(AlertAssetsEnum.AND.toString());
            }
            if(map.containsKey(AlertEnum.Specifications.toString()) && map.get(AlertEnum.Specifications.toString()) != null){
                url.append(AlertEnum.APPCATIONINFOEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.Specifications.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString());
            }
            if(map.containsKey(AlertEnum.Domain.toString()) && map.get(AlertEnum.Domain.toString()) != null){
                url.append(AlertEnum.DOMAINEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.Domain.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString());
            }
            if(getUserName != null){
                url.append(AlertEnum.PERSONEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(getUserName,"UTF-8")).append(AlertAssetsEnum.AND.toString());
            }
            log.info("企业微信url:" + url);
            if(isAlarm){
                if(alr != null && alr.getIsActionLevel() != null && alr.getIsActionLevel()){
                    url.append(AlertEnum.ALERTTIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.ALERTTIME.toString()),"UTF-8"));
                }else{
                    url.append(AlertEnum.ALERTSTARTIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.ALERTTIME.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.SENDTIEMEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(df.format(date),"UTF-8"));
                }
            }else{
                url.append(AlertEnum.ALERTTIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.FAILURETIME.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                        .append(AlertEnum.CLOSETIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.RECOVERYTIME.toString()),"UTF-8"));
            }
        }catch (Exception e){
            log.error("url 拼接失败:{}",e);
        }
        log.info("企业微信url:" + url);
        HashMap<String, Object> sendDataMap = new HashMap<>();
        sendDataMap.put("touser", touser);
        sendDataMap.put("msgtype", "textcard");
        sendDataMap.put("agentid", qyEntity.getAgentId());
        HashMap<String, String> firstdata = new HashMap<>();
        if(isAlarm){
            title = map.get("告警标题");
            if(alr != null && alr.getIsActionLevel()!=null && alr.getIsActionLevel()){
                firstdata.put("title", "[告警升级]\n主机名称:" + map.get("主机名称") + "\n ");
            }else {
                firstdata.put("title", "[系统告警]\n主机名称:" + map.get("主机名称") + "\n ");
            }
        }else {
            title = map.get("恢复信息");
            firstdata.put("title", "[恢复通知]\n主机名称:" + map.get("主机名称") + "\n ");
        }
        firstdata.put("btntxt", "详情");
        firstdata.put("description", context.toString());
        firstdata.put("url", url.toString());
        sendDataMap.put("textcard", firstdata);
        String sendStr = JSON.toJSONString(sendDataMap);
        return sendStr;
    }

    @Override
    public Object selectFrom(){
        GeneralMessageEntity qyEntity = mwWeixinTemplateDao.findWeiXinMessage(ruleId);
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        log.info("企业微信发送方：" + qyEntity);
        return qyEntity;
    }
    public  void decrypt(GeneralMessageEntity applyWeiXin) {
        if(applyWeiXin != null){
            try {
                if (applyWeiXin.getSecret() != null) {
                    applyWeiXin.setSecret(EncryptsUtil.decrypt(applyWeiXin.getSecret()));
                    applyWeiXin.setAgentId(EncryptsUtil.decrypt(applyWeiXin.getAgentId()));
                    applyWeiXin.setId(EncryptsUtil.decrypt(applyWeiXin.getId()));
                }
            } catch (Exception e) {
                log.error("解密失败:",e);
            }
        }
    }

    public String getSendTouer(List<String> userIds) {
        StringBuffer touser = new StringBuffer();
        HashSet<String> userIds1 = (HashSet<String>) userIds.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        if (userIds1 != null && userIds1.size() > 0 && userIds1.size() <= 1000) {
            Iterator iterator = userIds1.iterator();
            while (iterator.hasNext()) {
                touser.append("|").append(iterator.next());
            }
        }else {
            return "";
        }
        touser.replace(0, 1, "");
        log.info("企业微信接收人:" + touser);
        return touser.toString();
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            //根据系统用户id,查询微信userId
            HashSet<Integer> groupAndOrgUserIds = new HashSet<>();
            groupAndOrgUserIds.addAll(userIds);
            if(alr != null){
                if(CollectionUtils.isNotEmpty(alr.getEmailCC())) groupAndOrgUserIds.addAll(alr.getEmailCC());
                if(CollectionUtils.isNotEmpty(alr.getGroupUserIds())) groupAndOrgUserIds.addAll(alr.getGroupUserIds());
                if(CollectionUtils.isNotEmpty(alr.getOrgUserIds())) groupAndOrgUserIds.addAll(alr.getOrgUserIds());
            }
            List<String> list = mwWeixinTemplateDao.selectQyWeixinUserId(groupAndOrgUserIds);
            log.info("企业微信接收人ids:" + groupAndOrgUserIds);
            //处理需要userIds,转换格式
            String touser = getSendTouer(list);
            this.touser = touser;
            return null;
        }catch (Exception e){
            log.error("perform select accept qiyeweixin:", e);
            return null;
        }

    }

    @Override
    public Object call() throws Exception {
        try{
            //1判断级别是否符合
            if(!outPut()){
                return null;
            }
            log.info("the alert information level is satisfied");

            //2:根据系统用户id,查询接收人
            selectAccepts(userIds);
            if(touser == null || touser.equals("")){
                log.info("perform select accept qiyeweixin is null");
                return null;
            }
            /*log.info("perform select accept qiyeweixin finish");*/

            //3:查询发送方
            selectFrom();
            log.info("perform select send qiyeweixin finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送企业微信消息
            sendMessage(sendMessage);
            log.info("qiyeweixin message send finish");
            return null;
        }catch (Exception e){
            log.error("qiyeweixin message send appear unknown error:",e);
            throw new Exception(e);
        }
    }
}
