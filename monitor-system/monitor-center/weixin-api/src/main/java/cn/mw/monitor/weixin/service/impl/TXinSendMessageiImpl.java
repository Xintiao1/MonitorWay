package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.param.ActionLevelRuleParam;
import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.MD5EncryptUtil;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.entity.HuaXingRuleParam;
import cn.mw.monitor.weixin.entity.UserInfo;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class TXinSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    String toUser;

    private static HashMap<String,Object> contentmap = new HashMap<>();

    //发送方
    private HuaXingRuleParam qyEntity;

    private ActionLevelRuleParam alr;

    private AlertRuleTableCommons alertRuleTable;

    public TXinSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId, ActionLevelRuleParam alr) {

        log.info("T信 map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        this.alr = alr;

    }

    @Override
    public void sendMessage(String sendMessage) {
        Integer errcode = -1;
        String erroMessage = "";
        try {
            Map<String, String> header = new HashMap<>();
            header.put("appId", qyEntity.getAppId());
            String timestamp = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();
            timestamp = sdf.format(date);
            header.put("timestamp",timestamp);
            String sign = MD5EncryptUtil.sign(qyEntity.getAppSecret() + timestamp);
            header.put("sign",sign);
            header.put("Content-type", "application/json");
            String result = LoadUtil.doPost(qyEntity.getUrl(), contentmap, header,alertRuleTable);
            JSONObject jsonObject = JSONObject.parseObject(result);
            log.info("T信发送结果：" + jsonObject);
            if(jsonObject.getString("code").equals("0")){
                errcode = 0;
            }else{
                erroMessage = jsonObject.getString("message");
            }
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message T信:{}",e);
        }finally {
            saveHis("T信",sendMessage,errcode,map.get("事件ID"),erroMessage,title,map.get("IP地址"),isAlarm,userIds, map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {
        String getUserName = getUserName();
        StringBuffer sb = new StringBuffer();
        String title = null;
        sb.append(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                .append(AlertEnum.MODELSYSTEM.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.MODELSYSTEM.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        String topic = isAlarm ? map.get(AlertEnum.ALERTTITLE.toString()) : map.get(AlertEnum.RECOVERYTITLE.toString());
        String alertInfo = isAlarm ? (map.get(AlertEnum.ALERTINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.PROBLEMDETAILS.toString())) : (map.get(AlertEnum.RECOVERYINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.RECOVERYDETAILS.toString()));
        String alertLevel = isAlarm ? map.get(AlertEnum.ALERTLEVEL.toString()) : map.get(AlertEnum.RECOVERYLEVEL.toString());
        String nowState = isAlarm ? map.get(AlertEnum.NOWSTATE.toString()) : map.get(AlertEnum.RENEWSTATUS.toString());
        sb.append(AlertEnum.TOPIC.toString() + AlertAssetsEnum.COLON.toString()).append(topic).append(AlertAssetsEnum.Comma.toString()).append('\n')
                .append(AlertEnum.ALERTINFO.toString() + AlertAssetsEnum.COLON.toString()).append(alertInfo).append(AlertAssetsEnum.Comma.toString()).append('\n')
                .append(AlertEnum.APPCATIONINFO.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Specifications.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                .append(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Domain.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                .append(AlertEnum.ALERTLEVEL.toString() + AlertAssetsEnum.COLON.toString()).append(MWAlertLevelParam.actionAlertLevelMap.get(alertLevel)).append(AlertAssetsEnum.Comma.toString()).append('\n');
        sb.append(AlertEnum.PERSON.toString() + AlertAssetsEnum.COLON.toString()).append(getUserName).append('\n');
        if(isAlarm){
            title = "告警事件-通知";
            if(alr != null && alr.getIsActionLevel()!=null && alr.getIsActionLevel()){
                title = "告警事件-升级";
                sb.append(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
            }else {
                sb.append(AlertEnum.ALERTSTARTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.SENDTIEM.toString() + AlertAssetsEnum.COLON.toString()).append(df.format(date)).append(AlertAssetsEnum.Comma.toString()).append('\n');
            }
        }else {
            title = "告警事件-恢复";
            sb.append(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.FAILURETIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                    .append(AlertEnum.CLOSETIME.toString()+ AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
            long clock = 0;
            try {
                clock = df.parse(map.get(AlertEnum.RECOVERYTIME.toString())).getTime() - df.parse(map.get(AlertEnum.FAILURETIME.toString())).getTime();
                clock = clock/1000;
            } catch (Exception e) {
                log.error("时间转换失败:{}", e);
            }
            sb.append(AlertEnum.LONGTIMEZH.toString() + AlertAssetsEnum.COLON.toString()).append(SeverityUtils.getLastTime(clock)).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        sb.append(AlertEnum.NOWSTATE.toString() + AlertAssetsEnum.COLON.toString()).append(nowState).append(AlertAssetsEnum.Comma.toString()).append('\n');
        sb.append(AlertEnum.EVENTID.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.EVENTID.toString()));
        String msg = sb.toString();

        HashMap<String, Object> sendDataMap = new HashMap<>();
        sendDataMap.put("appId",qyEntity.getAppId());
        sendDataMap.put("sender",qyEntity.getSender());
        sendDataMap.put("content",msg);
        sendDataMap.put("contentType",0);
        sendDataMap.put("sessionType",qyEntity.getSessionType());
        contentmap = sendDataMap;
        return sendDataMap.toString();
    }

    @Override
    public Object selectFrom(){
        HuaXingRuleParam qyEntity = mwWeixinTemplateDao.findHuaXingMessage(ruleId);
        this.qyEntity = qyEntity;
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        return qyEntity;
    }

    public String getUserName(){
        List<UserInfo> userName = mwWeixinTemplateDao.selectUserName(userIds);
        StringBuffer sb = new StringBuffer();
        for(UserInfo temp : userName){
            sb.append(temp.getUserName()).append("-").append(temp.getPhoneNumber()).append(",");
        }
        System.out.println(sb.toString().substring(0,sb.length()-1));
        return sb.toString();
    }

    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        try {
            HashSet<Integer> groupAndOrgUserIds = new HashSet<>();
            groupAndOrgUserIds.addAll(userIds);
            if(alr != null){
                if(CollectionUtils.isNotEmpty(alr.getEmailCC())) groupAndOrgUserIds.addAll(alr.getEmailCC());
                if(CollectionUtils.isNotEmpty(alr.getGroupUserIds())) groupAndOrgUserIds.addAll(alr.getGroupUserIds());
                if(CollectionUtils.isNotEmpty(alr.getOrgUserIds())) groupAndOrgUserIds.addAll(alr.getOrgUserIds());
            }
            List<String> list = mwWeixinTemplateDao.selectLoginName(groupAndOrgUserIds);
            toUser = getSendTouer(list);
            return null;
        }catch (Exception e){
            log.error("perform select accept T信:", e);
            return null;
        }

    }

    public String getSendTouer(List<String> userIds) {
        StringBuffer touser = new StringBuffer();
        HashSet<String> userIds1 = (HashSet<String>) userIds.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        if (userIds1 != null && userIds1.size() > 0 && userIds1.size() <= 1000) {
            Iterator iterator = userIds1.iterator();
            while (iterator.hasNext()) {
                String temp = iterator.next().toString();
                temp = temp.substring(0,temp.indexOf("@"));
                touser.append(",").append(temp);
            }
        }else {
            return "";
        }
        touser.replace(0, 1, "");
        log.info("T信接收人:" + touser);
        return touser.toString();
    }

    @Override
    public Object call() throws Exception {
        synchronized (contentmap){
            try{
                //1判断级别是否符合
                if(!outPut()){
                    return null;
                }
                log.info("the alert information level is satisfied");

                //2:查询发送方
                selectFrom();
                log.info("perform select send T信 finish");

                //3:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("T信 userIds" + userIds);

                log.info("perform select accept T信 finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("T信 perform deal message:{}", "*****");

                //4发送企业微信消息
                sendMessage(sendMessage);
                log.info("T信 message send finish");
                return null;
            }catch (Exception e){
                log.error("T信 message send appear unknown error:",e);
                throw new Exception(e.getMessage());
            }
        }
    }
}
