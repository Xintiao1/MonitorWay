package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.param.AliyunSmsParam;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.AliyunDxSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.weixin.entity.AliyunSmsFromEntity;
import cn.mw.monitor.weixin.entity.DingdingqunFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aliyuncs.profile.DefaultProfile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import com.aliyuncs.http.MethodType;

/**y
 * 阿里短信发送实现类
 */
public class AliyunDxSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //发送方（阿里云配置）
    private AliyunSmsFromEntity qyEntity;

    //发送的信息
    protected HashMap<String, String> map_a;

    //接收方手机号
    private String phone;

    private String title;

    private AlertRuleTableCommons alertRuleTable;

    public AliyunDxSendMessageiImpl(HashMap<String, String> Hashmap, HashSet<Integer> userIds,
                                    HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) throws Exception {
        map_a = Hashmap;
        this.map = Hashmap;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = Hashmap.get("告警标题")==null? Hashmap.get("恢复标题")==null? null:false : true;
    }

    //判断参数是否过长
    public String subString(String s){
        if(s != null){
            if(s.length() > 24){
                return s.substring(0,24).replace("\"","");
            }
            return s.replace("\"","");
        }
        return null;
    }

    @Override
    public String dealMessage() {
        log.info("AliyunSms dealMessage star");
        log.info("AliyunSms map" + map_a);
        JSONObject jsonObject = new JSONObject();
        if(isAlarm){
            title = map_a.get("告警标题");
            String title = subString(map_a.get("告警标题"));
            String Address = subString(map_a.get("IP地址"));
            String renewInfo = subString(map_a.get("问题详情"));
            String devicename = subString(map_a.get("主机名称"));
            String domain = subString(map_a.get("区域"));
            jsonObject.put("title",title);
            jsonObject.put("devicename",devicename);
            jsonObject.put("domain", domain);
            jsonObject.put("Address",Address);
            jsonObject.put("faultTime", map_a.get("告警时间"));
            jsonObject.put("renewTime", "");
            jsonObject.put("renewInfo", renewInfo);
            jsonObject.put("renewStatus", "PROBLEM" + "_" + map_a.get("IP地址"));
        }else {
            title = map_a.get("恢复标题");
            String title = subString(map_a.get("恢复标题"));
            String Address = subString(map_a.get("IP地址"));
            String renewInfo = subString(map_a.get("恢复详情"));
            String devicename = subString(map_a.get("主机名称"));
            String domain = subString(map_a.get("区域"));
            jsonObject.put("title", title);
            jsonObject.put("devicename",devicename);
            jsonObject.put("domain", domain);
            jsonObject.put("Address", Address);
            jsonObject.put("faultTime", map_a.get("故障时间"));
            jsonObject.put("renewTime", map_a.get("恢复时间"));
            jsonObject.put("renewInfo", renewInfo);
            jsonObject.put("renewStatus", renewInfo + "_" + map_a.get("IP地址"));
        }
        log.info("AliyunSms dealMessage jsonObject.toJSONString()" + jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    @Override
    public Object selectFrom(){
        AliyunSmsFromEntity qyEntity = mwWeixinTemplateDao.findAliyunSMSMessage(ruleId);
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        return qyEntity;
    }
    public  void decrypt(AliyunSmsFromEntity aliYun) {
        if(aliYun != null){
            try {
                if (aliYun.getAccessKeySecret() != null) {
                    aliYun.setAccessKeySecret(EncryptsUtil.decrypt(aliYun.getAccessKeySecret()));
                    aliYun.setAccessKeyId(EncryptsUtil.decrypt(aliYun.getAccessKeyId()));
                    aliYun.setTemplateCode(EncryptsUtil.decrypt(aliYun.getTemplateCode()));
                }
            } catch (Exception e) {
                log.error("失败:{}",e);
            }
        }
    }

    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        List<String> phones = mwWeixinTemplateDao.selectPhones(userIds);
        List<String> morePhones = mwWeixinTemplateDao.selectMorePhones(userIds);
        if(CollectionUtils.isNotEmpty(morePhones)){
            for(String s : morePhones){
                String[] strs = s.split(",");
                phones.addAll(Arrays.asList(strs));
            }
        }
        HashSet<String> sendPhones = (HashSet<String>) phones.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        phone = sendPhones.toString().replaceAll(" ","").replaceAll("]","").replaceAll("\\[","");
        return sendPhones;
    }


    public JSONObject send(String signName, String templateCode, String phone, String content) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-qingdao", qyEntity.getAccessKeyId(), qyEntity.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", content);
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("AliyunDX response:" + response.getData());
        } catch (Exception e) {
            log.error("error perform send message AliyunDX:{}",e);
        }
        return null;
    }

    @Override
    public void sendMessage(String sendMessage) {
        //发送信息
        StringBuffer sb = new StringBuffer();
        String error = "";
        int isSuccess = -1;
        try{
            error = AliyunDxSendUtil.send(qyEntity.getSignName(), qyEntity.getTemplateCode(), phone, sendMessage, qyEntity.getAccessKeyId(), qyEntity.getAccessKeySecret(),alertRuleTable);
            JSONObject json = JSON.parseObject(error);
            if(json.get("Code").equals("OK")){
                isSuccess = 0;
            }
            log.info("短信发送结果：" + error);
        }catch(Exception e){
            error = e.getMessage();
            log.error("error perform send message AliyunDX:{}",e);
        }finally {
            //保存记录
            saveHis("阿里云短信",sendMessage,isSuccess,map_a.get("事件ID"),error,title,map_a.get("IP地址"),isAlarm, userIds,map_a.get(AlertEnum.HOSTID.toString()));
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

            //2根据系统用户id,查询接收人手机号
            selectAccepts(userIds);

            //3获取发送发
            selectFrom();
            log.info("perform deal selectFrom:{}", qyEntity);
            if(qyEntity == null || qyEntity.getAccessKeyId().equals("")){
                log.info("perform select send aliyunSMS is null");
                return null;
            }
            //4拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", sendMessage);
            //4发送短信
            sendMessage(sendMessage);
            log.info("AliyunDX message send finish");
            return null;
        }catch (Exception e){
            log.error("AliyunDX message send appear unknown error:{}",e);
            throw new Exception(e.getMessage());
        }
    }
}
