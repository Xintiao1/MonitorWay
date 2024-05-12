package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.param.ActionLevelRuleParam;
import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.MD5EncryptUtil;
import cn.mw.monitor.util.entity.HuaXingYuYinRuleParam;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class HuaXingYuYinSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //发送方
    private HuaXingYuYinRuleParam qyEntity;

    private ActionLevelRuleParam alr;

    private AlertRuleTableCommons alertRuleTable;

    private HashSet<String> sendPhones;

    private Map<String,Object> sendMap = new TreeMap<>();

    public HuaXingYuYinSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                        HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId, ActionLevelRuleParam alr) {

        log.info("华星光电语音通知 map：" + map);
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
            String url = qyEntity.getUrl() + "/voice/api/callNotify";
            for(String phone : sendPhones){
                sendMap.put("mobile",phone);
                String str = Ksort(sendMap) + "|" + qyEntity.getAppKey();
                log.info("华星语音拼接消息：" + str);
                String sign = MD5EncryptUtil.encrypt(str);
                log.info("华星语音AppKey：" + qyEntity.getAppKey());
                log.info("华星语音sign：" + sign);
                sendMap.put("accessToken",sign);
                //String body =  sendMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
                Map<String, String> header = new HashMap<>();
                header.put("content-type","application/json");
                erroMessage = LoadUtil.doPost(url, sendMap, header,alertRuleTable);
                log.info("华星光电Url：" + url);
                log.info("华星光电contentmap：" + sendMap);
                log.info("华星光电发送结果：" + erroMessage);
                sendMap.remove("accessToken");
                if(!erroMessage.contains("error")){
                    errcode = 0;
                }

            }

        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message 华星光电语音通知:{}",e);
        }finally {
            saveHis("华星光电语音通知",sendMessage,errcode,map.get("事件ID"),erroMessage,title,map.get("IP地址"),isAlarm,userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    public static String Ksort(Map<String, Object> map){
        String sb = "";
        String[] key = new String[map.size()];
        int index = 0;
        for (String k : map.keySet()) {
            key[index] = k;
            index++;
        }
        Arrays.sort(key);
        for (String s : key) {
            sb += s + "|" + map.get(s);
        }
// 将得到的字符串进⾏处理得到⽬标格式的字符串
        try {
            sb = URLEncoder.encode(sb, "UTF-8");
        } catch (UnsupportedEncodingException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }// 使⽤常⻅的UTF-8编码

        sb = sb.replace("%7C", "|");
//System.out.println(sb);
        return sb.toString();
    }

    @Override
    public String dealMessage() {
        Map<String,Object> sendMap = new TreeMap<>();
        sendMap.put("tplId",qyEntity.getTplId());
        sendMap.put("action","callNotify");
        JSONObject jsonObject = new JSONObject();
        String alertTitle = map.get(AlertEnum.ALERTTITLE.toString());
        if(!isAlarm){
            alertTitle = map.get(AlertEnum.RECOVERYTITLE.toString());
        }
        String message = "很抱歉通知您！您当前有一级告警通知" + map.get(AlertEnum.MODELSYSTEM.toString()) + alertTitle;
        jsonObject.put("message",message);
        sendMap.put("tplParams",jsonObject.toJSONString());
        sendMap.put("service","voice");
        String timestamp = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = new Date();
        timestamp = sdf.format(date);
        sendMap.put("timestamp",timestamp);
        sendMap.put("version","2021-06-01");
        sendMap.put("caller","21828000");
        sendMap.put("responsor",getUserName(userIds));
        sendMap.put("area",map.get(AlertEnum.MODELSYSTEMZH.toString()));
        sendMap.put("system",map.get(AlertEnum.MODELCLASSIFY.toString()));
        sendMap.put("department",map.get(AlertEnum.ORG.toString()));
        this.sendMap = sendMap;
        return null;
    }

    @Override
    public Object selectFrom(){
        HuaXingYuYinRuleParam qyEntity = mwWeixinTemplateDao.findHuaXingYuyinMessage(ruleId);
        this.qyEntity = qyEntity;
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        return qyEntity;
    }


    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        try {
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
            log.error("perform select accept 华星光电语音通知:", e);
            return null;
        }

    }


    @Override
    public Object call() throws Exception {

            try{
                if(!isAlarm){
                    return null;
                }
                //1判断级别是否符合
                if(!outPut()){
                    return null;
                }
                log.info("the alert information level is satisfied");

                //2:查询发送方
                selectFrom();
                log.info("perform select send 华星光电语音通知 finish");

                //3:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("华星光电语音通知 userIds" + userIds);

                log.info("perform select accept 华星光电语音通知 finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("华星光电语音通知 perform deal message:{}", sendMessage);

                //4发送企业微信消息
                sendMessage(sendMessage);
                log.info("华星光电语音通知 message send finish");
                return null;
            }catch (Exception e){
                log.error("华星光电语音通知 message send appear unknown error:",e);
                throw new Exception(e.getMessage());
            }
        }

}
