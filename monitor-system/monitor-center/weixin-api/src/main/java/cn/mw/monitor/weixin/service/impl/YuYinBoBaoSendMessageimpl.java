package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.util.EmailSendUtil;
import cn.mw.monitor.weixin.service.SendMessageBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 企业微信发送实现类
 */
public class YuYinBoBaoSendMessageimpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private HashSet<Integer> userIds;
    private List<String> userName;
    private String alertLevel = env.getProperty("alert.level");

    private WebSocket webSocket;

    private static String MODULE = "alertwebscoket";

    public YuYinBoBaoSendMessageimpl(HashMap<String, String> map, HashSet<Integer> userIds, HashSet<String> severity) {
        this.map = map;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        this.userIds = userIds;
        this.severity = severity;
        WebSocket webSocket = ApplicationContextProvider.getBean(WebSocket.class);
        this.webSocket = webSocket;
    }

    @Override
    public void sendMessage(String sendMessage){
        try {
            String nowState = map.containsKey(AlertEnum.NOWSTATE.toString()) ? AlertEnum.UNUSUAL.toString() : AlertEnum.NORMAL.toString();
            webSocket.setModelDataId(MODULE);
            if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                webSocket.sendTextMessage(MODULE,map.get(AlertEnum.HOSTNAME.toString()) + "," + AlertEnum.HOSTID.toString() + map.get(AlertEnum.HOSTIP.toString()) + "," + nowState);
            }else {
                log.info("资产负责人id：" + userIds);
                for(Integer userId : userIds){
                    log.info("userId：" + userId);
                    webSocket.setUserId(userId.toString());
                    webSocket.yuyinBobao( userId, MODULE,map.get(AlertEnum.HOSTNAME.toString()) + "," + AlertEnum.HOSTID.toString() + map.get(AlertEnum.HOSTIP.toString()) + "," + nowState);
                }
            }


        }catch (Exception e){
            log.error("语音播报错误:{}",e);
        }

    }

    @Override
    public String dealMessage() {
        return super.dealMessage();
    }

    @Override
    public Object selectFrom(){
        return null;
    }


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        return null;
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

            selectAccepts(userIds);
            log.info("perform deal selectAccepts");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            log.info("语音播报 message send star");
            sendMessage(sendMessage);
            log.info("语音播报 message send finish");
            return null;
        }catch (Exception e){
            log.error("语音播报 message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }

}
