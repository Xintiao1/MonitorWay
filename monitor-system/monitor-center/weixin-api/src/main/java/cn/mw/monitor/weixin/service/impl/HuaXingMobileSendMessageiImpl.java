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
import cn.mwpaas.common.utils.StringUtils;
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
public class HuaXingMobileSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    String toUser;

    private static HashMap<String,Object> contentmap = new HashMap<>();

    //发送方
    private HuaXingRuleParam qyEntity;

    private ActionLevelRuleParam alr;

    private AlertRuleTableCommons alertRuleTable;

    public HuaXingMobileSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                         HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId, ActionLevelRuleParam alr) {

        log.info("华星光电 map：" + map);
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
            String result = LoadUtil.doPost(qyEntity.getUrl(), contentmap, header,alertRuleTable);
            log.info("华星光电Url：" + qyEntity.getUrl());
            log.info("华星光电contentmap：" + contentmap);
            log.info("华星光电header：" + header);
            log.info("华星光电发送结果：" + result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(jsonObject.getString("code").equals("0")){
                errcode = 0;
            }else{
                erroMessage = jsonObject.getString("message");
            }
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message 华星光电:{}",e);
        }finally {
            saveHis("华星光电",sendMessage,errcode,map.get("事件ID"),erroMessage,title,map.get("IP地址"),isAlarm,userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {
        String getUserName = getUserName();
        List<String> msgList = new ArrayList<>();
        String title = null;
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        String topic = isAlarm ? map.get(AlertEnum.ALERTTITLE.toString()) : map.get(AlertEnum.RECOVERYTITLE.toString());
        String alertInfo = isAlarm ? (map.get(AlertEnum.ALERTINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.PROBLEMDETAILS.toString())) : (map.get(AlertEnum.RECOVERYINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.RECOVERYDETAILS.toString()));
        String alertLevel = isAlarm ? map.get(AlertEnum.ALERTLEVEL.toString()) : map.get(AlertEnum.RECOVERYLEVEL.toString());
        String nowState = isAlarm ? map.get(AlertEnum.NOWSTATE.toString()) : map.get(AlertEnum.RENEWSTATUS.toString());
        if(isAlarm){
            title = "告警";
            if(alr != null && alr.getIsActionLevel()!=null && alr.getIsActionLevel()){
                title = "升级";
                msgList.add(title + AlertAssetsEnum.COLON.toString() + topic + AlertAssetsEnum.Comma.toString() + '\n');
                msgList.add(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.ALERTTIME.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
            }else {
                msgList.add(title + AlertAssetsEnum.COLON.toString() + topic + AlertAssetsEnum.Comma.toString() + '\n');
                msgList.add(AlertEnum.ALERTSTARTIME.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.ALERTTIME.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
                msgList.add(AlertEnum.SENDTIEM.toString() + AlertAssetsEnum.COLON.toString() + df.format(date) + AlertAssetsEnum.Comma.toString() + '\n');
            }
        }else {
            title = "恢复";
            msgList.add(title + AlertAssetsEnum.COLON.toString() + topic + AlertAssetsEnum.Comma.toString() + '\n');
            msgList.add(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.FAILURETIME.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
            msgList.add(AlertEnum.CLOSETIME.toString()+ AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.RECOVERYTIME.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
            long clock = 0;
            try {
                clock = df.parse(map.get(AlertEnum.RECOVERYTIME.toString())).getTime() - df.parse(map.get(AlertEnum.FAILURETIME.toString())).getTime();
                clock = clock/1000;
            } catch (Exception e) {
                log.error("时间转换失败:{}", e);
            }
            msgList.add(AlertEnum.LONGTIMEZH.toString() + AlertAssetsEnum.COLON.toString() + SeverityUtils.getLastTime(clock) + AlertAssetsEnum.Comma.toString() + '\n');
        }
        msgList.add(AlertEnum.ALERTLEVEL.toString() + AlertAssetsEnum.COLON.toString() + MWAlertLevelParam.actionAlertLevelMap.get(alertLevel) + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.HostNameZH.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.HostNameZH.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.IPAddress.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.MODELSYSTEM.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.MODELSYSTEM.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.ALERTINFO.toString() + AlertAssetsEnum.COLON.toString() + alertInfo + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.APPCATIONINFO.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.Specifications.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.Domain.toString()) + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.PERSON.toString() + AlertAssetsEnum.COLON.toString() + getUserName + '\n');
        msgList.add(AlertEnum.NOWSTATE.toString() + AlertAssetsEnum.COLON.toString() + nowState + AlertAssetsEnum.Comma.toString() + '\n');
        msgList.add(AlertEnum.EVENTID.toString() + AlertAssetsEnum.COLON.toString() + map.get(AlertEnum.EVENTID.toString()));
        String msg = msgList.toString();
        HashMap<String, Object> sendDataMap = new HashMap<>();
        String cid = UUIDUtils.getUUID();
        sendDataMap.put("cid",cid);
        sendDataMap.put("pluginId",qyEntity.getPluginId());
        if(StringUtils.isNotBlank(qyEntity.getToken())){
            sendDataMap.put("serToken",qyEntity.getToken());
        }
        sendDataMap.put("summary",topic);
        List<HashMap<String,Object>> items = new ArrayList<>();
        HashMap<String,Object> itemsMap = new HashMap<>();
        List<HashMap<String,String>> itemsMapValue = new ArrayList<>();
        StringBuffer str = new StringBuffer();
        for(int i=0;i<=msgList.size();i++){
            if(i<2){
                HashMap<String,String> itemsMapValueMap = new HashMap<>();
                itemsMapValueMap.put("str",msgList.get(i));
                itemsMapValue.add(itemsMapValueMap);
            }else if(i >=2 && i < msgList.size()){
                str.append(msgList.get(i));
            }else {
                HashMap<String,String> itemsMapValueMap = new HashMap<>();
                itemsMapValueMap.put("str",str.toString());
                itemsMapValue.add(itemsMapValueMap);
            }

        }
        itemsMap.put("value",itemsMapValue);
        itemsMap.put("type",2);
        items.add(itemsMap);
        sendDataMap.put("items",items);
        HashMap<String, Object> filter = new HashMap<>();
        filter.put("toUser", toUser);
        sendDataMap.put("filter",filter);
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
            log.error("perform select accept 华星光电:", e);
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
                if(temp.contains("@")){
                    temp = temp.substring(0,temp.indexOf("@"));
                    touser.append(",").append(temp);
                }
            }
        }else {
            return "";
        }
        touser.replace(0, 1, "");
        log.info("华星光电接收人:" + touser);
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
                log.info("perform select send 华星光电 finish");

                //3:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("华星光电 userIds" + userIds);

                log.info("perform select accept 华星光电 finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("华星光电 perform deal message:{}", sendMessage);

                //4发送企业微信消息
                sendMessage(sendMessage);
                log.info("华星光电 message send finish");
                return null;
            }catch (Exception e){
                log.error("华星光电 message send appear unknown error:",e);
                throw new Exception(e.getMessage());
            }
        }
    }
}
