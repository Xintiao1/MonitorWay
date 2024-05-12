package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.weixin.entity.DingdingqunFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/**
 * 飞书群机器人发送实现类
 */
public class FeiShuQunSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //发送方（飞书群机器人）
    private DingdingqunFromEntity qyEntity;

    private  Map<String, Object> sendMsgMap = new HashMap<>();

    private static final String AT_ALL_USER = "<at user_id=\"all\">所有人</at>";



    public FeiShuQunSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                     HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) throws Exception {
        log.info("飞书群map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;

    }



    @Override
    public void sendMessage(String sendMessage) {
        Integer errcode = -1;
        String erroMessage = "";
        try {
            String response = DingdingQunSendUtil.sendPostByMap(qyEntity.getWebHook(),sendMsgMap);
            errcode = 0;
            log.info("飞书群发送结果：" + response);
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message feishuqun:{}",e);
        }finally {
            saveHis("飞书",sendMessage,errcode,map.get("事件ID"),erroMessage,this.title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }
    }


    @Override
    public String dealMessage() {
        try{
            String msg = super.dealMessage() + AT_ALL_USER;
            int timestamp = (int) (System.currentTimeMillis() / 1000);
            String sign = GenSign(timestamp);
            sendMsgMap.put("timestamp", timestamp);
            sendMsgMap.put("sign", sign);
            sendMsgMap.put("msg_type", "text");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("text", msg);
            sendMsgMap.put("content", jsonObject);
        }catch (Exception e){
            log.error("加密失败:{}",e);
        }
        return sendMsgMap.toString();

    }

    private String GenSign(int timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + qyEntity.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(new byte[]{});
        return new String(Base64.encodeBase64(signData));
    }


    @Override
    public Object selectFrom(){
        DingdingqunFromEntity qyEntity = mwWeixinTemplateDao.findDingdingQunMessage(ruleId);
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        return qyEntity;
    }
    public  void decrypt(DingdingqunFromEntity dingDingQun) {
        if(dingDingQun != null){
            try {
                if (dingDingQun.getWebHook() != null) {
                    dingDingQun.setKeyWord(EncryptsUtil.decrypt(dingDingQun.getKeyWord()));
                }
            } catch (Exception e) {
                log.error("失败:{}",e);
            }
        }
    }
    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        return null;
    }


    @Override
    public Object call() throws Exception {
        try{
            //1判断级别是否符合
            if(!outPut()){
                return null;
            }
            log.info("the alert information level is satisfied");

            //2:查询发送方
            selectFrom();
            log.info("perform select send feishuqun finish");

            //3:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送飞书群消息
            sendMessage(sendMessage);
            log.info("feishuqun message send finish");
            return null;
        }catch (Exception e){
            log.error("feishuqun message send appear unknown error:{}",e);
            throw new Exception(e);
        }
    }

}
