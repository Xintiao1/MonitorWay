package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.AliyunDxSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.TengxunDxSendUtil;
import cn.mw.monitor.util.entity.TengXunSmsFromEntity;
import cn.mw.monitor.weixin.entity.AliyunSmsFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 腾讯短信发送实现类
 */
public class TengxunAndHuaWeiDxSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //发送方（腾讯云配置）
    private TengXunSmsFromEntity qyEntity;

    private String[] msg;

    //腾讯接收人
    private String[] phoneNumber;

    //发送方（阿里云配置）
    private AliyunSmsFromEntity aliEntity;

    //接收方手机号
    private String phone;

    //接收方手机号
    private HashSet<String> sendPhones;

    private AlertRuleTableCommons alertRuleTable;


    public TengxunAndHuaWeiDxSendMessageiImpl(HashMap<String, String> Hashmap, HashSet<Integer> userIds,
                                              HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) throws Exception {
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
    public String dealMessage() {
        JSONObject jsonObject = new JSONObject();
        if(isAlarm){
            msg = new String[]{map.get("告警标题"), map.get("主机名称"),map.get("IP地址"), map.get("告警时间"), map.get("告警等级"), map.get("问题详情")};
            title = map.get("告警标题");
            String title = subString(map.get("告警标题"));
            String Address = subString(map.get("IP地址"));
            String renewInfo = subString(map.get("问题详情"));
            String devicename = subString(map.get("主机名称"));
            String level = subString(map.get("告警等级"));
            jsonObject.put("title",title);
            jsonObject.put("devicename",devicename);
            jsonObject.put("level", level);
            jsonObject.put("Address",Address);
            jsonObject.put("faultTIme", map.get("告警时间"));
            jsonObject.put("renewTime", "");
            jsonObject.put("renewInfo", renewInfo);
            jsonObject.put("renwStatus", "PROBLEM");

        }else {
            qyEntity.setTemplateId(qyEntity.getRecoveryTemplateId());
            msg = new String[]{map.get("恢复标题"), map.get("主机名称"),map.get("IP地址"), map.get("恢复时间"), map.get("恢复等级"), map.get("恢复详情")};
            title = map.get("恢复标题");
            String title = subString(map.get("恢复标题"));
            String Address = subString(map.get("IP地址"));
            String renewStatus = subString(map.get("恢复状态"));
            String renewInfo = subString(map.get("恢复信息"));
            String devicename = subString(map.get("主机名称"));
            String level = subString(map.get("恢复等级"));
            jsonObject.put("title", title);
            jsonObject.put("devicename",devicename);
            jsonObject.put("level", level);
            jsonObject.put("Address", Address);
            jsonObject.put("faultTIme", map.get("故障时间"));
            jsonObject.put("renewTime", map.get("恢复时间"));
            jsonObject.put("renewInfo", renewInfo);
            jsonObject.put("renwStatus", renewStatus);
        }
        return jsonObject.toString();
    }

    @Override
    public Object selectFrom(){
        TengXunSmsFromEntity qyEntity = mwWeixinTemplateDao.findTengxunSMSMessage(ruleId);
        this.qyEntity = qyEntity;
        AliyunSmsFromEntity aliEntity = mwWeixinTemplateDao.findAliyunSMSMessage(ruleId);
        decrypt(aliEntity);
        this.aliEntity = aliEntity;
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        return qyEntity;
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
        this.sendPhones = sendPhones;
        phoneNumber  = sendPhones.toArray(new String[sendPhones.size()]);
        phone = sendPhones.toString().replaceAll(" ","").replaceAll("]","").replaceAll("\\[","");
        return sendPhones;
    }



    @Override
    public void sendMessage(String sendMessage) {
        //发送信息
        String error = "";
        int isSuccess = -1;
        String title = null;
        try{
            Random random = new Random();
            int num = random.nextInt(2);
            log.info("随机数为：" + num);
            if(num == 0){
                title = "腾讯短信";
                error = TengxunDxSendUtil.send(qyEntity,msg,phoneNumber,alertRuleTable);
                log.info("腾讯1短信发送结果:" + error);
                if(!error.equals("Ok")){
                    title = "阿里短信";
                    error = AliyunDxSendUtil.send(aliEntity.getSignName(), aliEntity.getTemplateCode(), phone, sendMessage, aliEntity.getAccessKeyId(), aliEntity.getAccessKeySecret(), alertRuleTable);
                    log.info("阿里1短信发送结果:" + error);
                    JSONObject json = JSON.parseObject(error);
                    if(json.get("Code").equals("OK")){
                        isSuccess = 0;
                    }
                }else{
                    isSuccess = 0;
                }
            }else{
                title = "阿里短信";
                error = AliyunDxSendUtil.send(aliEntity.getSignName(), aliEntity.getTemplateCode(), phone, sendMessage, aliEntity.getAccessKeyId(), aliEntity.getAccessKeySecret(),alertRuleTable);
                log.info("阿里2短信发送结果:" + error);
                if(error == null || !JSON.parseObject(error).get("Code").equals("OK")){
                    title = "腾讯短信";
                    error = TengxunDxSendUtil.send(qyEntity,msg,phoneNumber,alertRuleTable);
                    log.info("腾讯2短信发送结果:" + error);
                    if(error.equals("Ok")){
                        isSuccess = 0;
                    }
                }else{
                    isSuccess = 0;
                }
            }

            log.info("短信发送结果:" + error);
        }catch(Exception e){
            error = e.getMessage();
            log.error("error perform send message TengxunDX:",e);
        }finally {
            //保存记录
            saveHis(title,sendMessage,isSuccess,map.get("事件ID"),error,title,map.get("IP地址"),isAlarm, userIds, map.get(AlertEnum.HOSTID.toString()));
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
            log.info("TengxunDX sendPhones" + sendPhones);

            if(sendPhones == null || sendPhones.size()==0){
                log.info("perform select phones:{}", sendPhones.size());
                return null;
            }
            log.info("perform select phones:{}", sendPhones.size());
            //3获取发送发
            selectFrom();
            log.info("perform deal selectFrom:{}", qyEntity);
            if(qyEntity == null || qyEntity.getSecretKey().equals("")){
                log.info("perform select send TengxunSMS is null");
                return null;
            }
            //4拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", sendMessage);
            //4发送短信
            sendMessage(sendMessage);
            log.info("TengxunDX message send finish");
            return null;
        }catch (Exception e){
            log.error("TengxunDX message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }
}
