package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.util.WeiXinSendUtil;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import cn.mw.monitor.weixin.entity.MwShenZhenSMSFromEntity;
import cn.mw.monitor.weixin.mypackage.SMSServiceSoap;
import cn.mw.monitor.weixin.mypackage.SendSMSRet;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mw.monitor.weixin.service.WxPortalService;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业微信发送实现类
 */
public class ShenZhenSMSMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方手机号
    private HashSet<String> sendPhones;

    //发送方（深圳短信）
    private MwShenZhenSMSFromEntity qyEntity;

    String token = null;

    String appId = null;


    public ShenZhenSMSMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                   HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;

    }

    @Override
    public void sendMessage(String sendMessage) throws MalformedURLException {
        log.info("深圳广电局发送信息");
        int isSuccess = -1;
        String error = "";
        try {
            URL url = new URL("http://10.150.33.106/NS_SMS_Service/smsservice.asmx?WSDL");
            QName qname = new QName("http://tempuri.org/","SMSService");
            Service service = Service.create(url,qname);
            SMSServiceSoap send = service.getPort(SMSServiceSoap.class);
            for (String phoneNo : sendPhones) {
                SendSMSRet result = send.sendSMS(qyEntity.getAppID(),qyEntity.getAppPWD(),qyEntity.getBizClassID(),qyEntity.getBizTypeID(),qyEntity.getBizSubTypeID(),qyEntity.getExtNo(),phoneNo,sendMessage,qyEntity.getIsNeedReport(),BigDecimal.valueOf(qyEntity.getCustID()),qyEntity.getAppSMSCode());
                log.info("深圳广电局：" + result);
            }
            if(StringUtils.isNotEmpty(qyEntity.getApiUrl())){
                sendSZCloud(sendMessage,"2");
            }
            isSuccess = 0;
        }catch (Exception e){
            error = e.getMessage();
            log.error("error perform send message 深圳短信:",e);
        }finally {
            //保存记录
            sendSZCloud(sendMessage,"0");
            saveHis("深圳短信",sendMessage,isSuccess,map.get("事件ID"),error,this.title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }
    private void sendSZCloud(String sendMessage,String grade){
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        if(redisUtils.hasKey(qyEntity.getApiRoleName())){
            if(redisUtils.hasKey("ShenZhenAccessToken")){
                token = (String) redisUtils.get("ShenZhenAccessToken");
            }else {
                token = getToken();
            }
        }else{
            getAppId();
        }
        StringBuffer phone = new StringBuffer();
        for (String phoneNo : sendPhones) {
            phone.append(phoneNo).append("|");
        }
        if(isAlarm){
            String severityMp = map.get("告警等级");
            String severity = null;
            if(severityMp.equals("信息")) {
                severity = "3";
            }
            if(severityMp.equals("警告") || severityMp.equals("一般严重")){
                severity = "4";
            }
            if(severityMp.equals("严重") || severityMp.equals("灾难")){
                severity = "5";
            }
            szAlertCloud(token,grade,phone.toString(),severity,sendMessage);
        }else{
            szCancelCloud(token,grade,phone.toString());
        }
    }

    private void szCancelCloud(String token, String grade, String phone){
        try {
            String url = qyEntity.getApiUrl() + "/szCloud/cancelAlarm";
            Map<String,Object> json=new HashMap();
            json.put("ori_serial",map.get("事件ID"));
            json.put("source",1);
            json.put("n_changtime",map.get("恢复时间"));
            json.put("grade",grade);
            json.put("n_mobile",phone);
            Map<String, String> headParam = new HashMap();
            headParam.put("token", token);
            String result = DingdingQunSendUtil.doPost(url,json,headParam);
            log.info("深圳短信发送恢复告警结果：" + result);
        }catch (Exception e){
            log.error("深圳短信发送恢复告警结果错误：", e);
        }

    }

    private void szAlertCloud(String token, String grade, String phone, String severity, String sendMessage){
        try{
            String url = qyEntity.getApiUrl() + "/szCloud/insertAlarm";
            Map<String,Object> json=new HashMap();
            json.put("firstoccurrence",map.get("告警时间"));
            json.put("grade",grade);
            json.put("lastoccurrence",map.get("告警时间"));
            json.put("manager","配套系统");
            json.put("n_dev_name",map.get("主机名称"));
            json.put("n_mobile",phone);
            json.put("node",map.get("主机名称"));
            json.put("nodealias",map.get("主机名称"));
            json.put("nodeip",map.get("IP地址"));
            json.put("ori_serial",map.get("事件ID"));
            json.put("source",5);
            json.put("severity",severity);
            json.put("summary",sendMessage);
            json.put("tally",1);
            json.put("ya_flag",mwWeixinTemplateDao.selectassetsType(map.get("HOSTID").toString()));
            Map<String, String> headParam = new HashMap();
            headParam.put("token", token);
            String result = DingdingQunSendUtil.doPost(url,json,headParam);
            log.info("深圳短信发送告警结果：" + result);
        }catch (Exception e){
            log.error("深圳短信发送告警结果错误：", e);
        }

    }

    private void getAppId(){
        String url = qyEntity.getApiUrl() + "/auth/generateTokenAndAppId?roleName=" + qyEntity.getApiRoleName();
        Map<String,Object> paramMap = new HashMap<>();
        String result = WeiXinSendUtil.doGetPost(url,"GET",paramMap);
        JSONObject json = JSONObject.parseObject(result);
        if((Boolean) json.get("success")){
            JSONObject data = json.getJSONObject("data");
            token = data.get("token").toString();
            appId = data.get("appId").toString();
            //将token放入redis中保存
            RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
            redisUtils.set("ShenZhenAccessToken", token, (Long) data.get("exp"));
            redisUtils.set(qyEntity.getApiRoleName(), appId);
        }
        log.info("深圳短信获取appId:" + result);
    }

    private String getToken(){
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        String appId = redisUtils.get(qyEntity.getApiRoleName()).toString();
        String url = qyEntity.getApiUrl() + "/auth/generateTokenByAppId?appId=" + appId;
        Map<String,Object> paramMap = new HashMap<>();
        String result = WeiXinSendUtil.doGetPost(url,"GET",paramMap);
        JSONObject json = JSONObject.parseObject(result);
        if((Boolean) json.get("success")){
            JSONObject data = json.getJSONObject("data");
            token = data.get("token").toString();
            //将token放入redis中保存
            redisUtils.set("ShenZhenAccessToken", token, (Long) data.get("exp"));
        }
        log.info("深圳短信获取token:" + result);
        return token;
    }

    @Override
    public String dealMessage() {
        JSONObject jsonObject = new JSONObject();
        String devicename = map.get("主机名称");
        String domain = map.get("区域");
        String Address = map.get("IP地址");
        String level = null;
        String renewInfo = null;
        if(isAlarm){
            title = map.get("告警标题");
            renewInfo = map.get("问题详情");
            level = map.get("告警等级");
            jsonObject.put("faultTime", map.get("告警时间"));
            jsonObject.put("renewTime", "");
            jsonObject.put("renewStatus", "PROBLEM" + "_" + map.get("IP地址"));
        }else {
            title = map.get("恢复标题");
            renewInfo = map.get("恢复详情");
            level = map.get("恢复等级");
            jsonObject.put("faultTime", map.get("故障时间"));
            jsonObject.put("renewTime", map.get("恢复时间"));
            jsonObject.put("renewStatus", renewInfo + "_" + map.get("IP地址"));
        }
        jsonObject.put("level",level);
        jsonObject.put("title",title);
        jsonObject.put("devicename",devicename);
        jsonObject.put("domain", domain);
        jsonObject.put("Address",Address);
        jsonObject.put("renewInfo", renewInfo);
        log.info("深圳短信 jsonObject.toJSONString()" + jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    @Override
    public Object selectFrom(){
        MwShenZhenSMSFromEntity qyEntity = mwWeixinTemplateDao.findShenZhenSmsFrom(ruleId);
        //decrypt(qyEntity);
        this.qyEntity = qyEntity;
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
                log.error("失败:",e);
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
        return touser.toString();
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            //根据系统用户id,查询用户手机号
            List<String> phones = mwWeixinTemplateDao.selectPhones(userIds);
            List<String> morePhones = mwWeixinTemplateDao.selectMorePhones(userIds);
            if(CollectionUtils.isNotEmpty(morePhones)){
                for(String s : morePhones){
                    String[] strs = s.split(",");
                    phones.addAll(Arrays.asList(strs));
                }
            }
            HashSet<String> sendPhones = (HashSet<String>) phones.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
            this.sendPhones = sendPhones;
            return sendPhones;
        }catch (Exception e){
            log.error("perform select accept 深圳:", e);
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
            if(sendPhones == null || sendPhones.size()==0){
                log.info("perform select phones:{}", sendPhones.size());
                return null;
            }
            log.info("perform select phones:{}", sendPhones.size());

            //3:查询发送方
            selectFrom();
            log.info("perform select send 深圳 finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送企业微信消息
            log.info("深圳 message send star");
            sendMessage(sendMessage);
            log.info("深圳 message send finish");
            return null;
        }catch (Exception e){
            log.error("深圳 message send appear unknown error:",e);
            throw new Exception(e);
        }
    }
}
