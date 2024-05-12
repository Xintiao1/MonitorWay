package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.weixin.service.SendMessageBase;
import gzcb.query.SmsSender;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信发送实现类
 */
public class DxSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方手机号
    private HashSet<String> sendPhones;

    private String title;

    public DxSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                             HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
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
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        List<String> phones = mwWeixinTemplateDao.selectPhones(userIds);
        HashSet<String> sendPhones = (HashSet<String>) phones.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        this.sendPhones = sendPhones;
        return sendPhones;
    }

    @Override
    public void sendMessage(String sendMessage) {
        //发送信息
        StringBuffer sb = new StringBuffer();
        String error = "";
        int isSuccess = 0;
        if(sendPhones != null && sendPhones.size()>0 ){
            for (String sendPhone : sendPhones) {
                try{
                    //短信jar优化 1：群发短信 2:单个短发发送添加超时时间20s
                    /*SmsSenderService service = new SmsSenderService();
                    SmsSenderDelegate port = service.getSmsSenderPort();
                    String res = port.queryReport("Ivan03738", sendPhone, sendMessage);
                    sb.append("res").append(",");*/
                    SmsSender.sender(sendPhone,sendMessage);
                    sb.append("0").append(",");
                }catch (Exception e){
                    isSuccess = 1;
                    error = e.getMessage();
                }
            }
        }

        //保存记录
        saveHis("SMS_SEND_NEW短信",sendMessage,isSuccess,map.get("事件ID"),error,title,map.get("IP地址"),this.isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
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
            if(sendPhones == null || sendPhones.size()==0){
                log.info("perform select phones:{}", sendPhones.size());
                return null;
            }
            log.info("perform select phones:{}", sendPhones.size());

            //3拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "****");

            //4发送短信
            sendMessage(sendMessage);
            log.info("DX message send finish");
            return null;
        }catch (Exception e){
            log.error("DX message send appear unknown error:{}",e);
            throw new Exception(e);
        }
    }
}
