package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.*;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import cn.mw.monitor.util.entity.TCP_UDPFrom;
import cn.mw.monitor.weixin.entity.MwShenZhenSMSFromEntity;
import cn.mw.monitor.weixin.mypackage.SMSServiceSoap;
import cn.mw.monitor.weixin.mypackage.SendSMSRet;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业微信发送实现类
 */
public class SysLogMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private TCP_UDPFrom from;

    public SysLogMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
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
        log.info("通讯协议发送");
        int isSuccess = -1;
        String error = "";
        try {
            if(from.getAgreementType() == 1){
                isSuccess = TCP_UDPSendUtil.TCPSend(from.getHost(), from.getPort(), sendMessage);
            }
            if(from.getAgreementType() == 2){
                isSuccess = TCP_UDPSendUtil.UDPSend(from.getHost(), from.getPort(), sendMessage);
            }
            if(from.getAgreementType() == 3){
                isSuccess = TCP_UDPSendUtil.TCPSendByTLS(from, sendMessage);
            }

        }catch (Exception e){
            error = e.getMessage();
            log.error("error perform send message SYSLog发送错误：" , e);
        }finally {
            //保存记录
            saveHis("SYSLog",sendMessage,isSuccess,map.get("事件ID"),error,this.title,map.get("IP地址"),isAlarm, null,map.get(AlertEnum.HOSTID.toString()));
        }

    }


    @Override
    public String dealMessage() {
        return super.dealMessage();
    }

    @Override
    public Object selectFrom(){
        TCP_UDPFrom from = mwWeixinTemplateDao.findTCPFrom(ruleId);
        this.from = from;
        return from;
    }
    /*public  void decrypt(GeneralMessageEntity applyWeiXin) {
        if(applyWeiXin != null){
            try {
                if (applyWeiXin.getSecret() != null) {
                    applyWeiXin.setSecret(EncryptsUtil.decrypt(applyWeiXin.getSecret()));
                    applyWeiXin.setAgentId(EncryptsUtil.decrypt(applyWeiXin.getAgentId()));
                    applyWeiXin.setId(EncryptsUtil.decrypt(applyWeiXin.getId()));
                }
            } catch (Exception e) {

            }
        }
    }*/


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            return null;
        }catch (Exception e){
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
            //3:查询发送方
            selectFrom();
            log.info("perform select send 系统推送 finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送消息
            log.info("系统推送 message send star");
            sendMessage(sendMessage);
            log.info("系统推送 message send finish");
            return null;
        }catch (Exception e){
            log.error("系统推送 message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }
}
