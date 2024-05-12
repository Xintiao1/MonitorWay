package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.alert.dto.BussinessAlarmInfoParam;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.QyWeixinSendUtil;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业微信发送实现类
 */
public class QyWxSendHuaXingAlertImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收人（多）
    private String touser;

    //发送方（企业微信）
    private GeneralMessageEntity qyEntity;

    private AlertRuleTableCommons alertRuleTable;

    private String eventId="-1";

    public QyWxSendHuaXingAlertImpl(HashMap<String, String> map, HashSet<Integer> userIds) {
        this.map = map;
        this.userIds = userIds;
    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("监控平台企业微信sendMessage：" + sendMessage);
        Integer errcode = -1;
        String erroMessage ="";
        try {
            erroMessage = QyWeixinSendUtil.sendQyWeixinMessage(sendMessage,qyEntity,alertRuleTable);
            log.info("监控平台企业微信发送结果：" + erroMessage);
            errcode = Integer.parseInt(JSON.parseObject(erroMessage).get("errcode").toString());
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("监控平台error perform send message qiyeweixin:",e);
        }finally {
            saveHis("监控平台企业微信发送",sendMessage,errcode,eventId,erroMessage,map.get(AlertEnum.TITLE.toString().toUpperCase()),"-1",true, userIds,"-1");
        }

    }


    @Override
    public String dealMessage() {
        HashMap<String, Object> sendDataMap = new HashMap<>();
        sendDataMap.put("touser", touser);
        sendDataMap.put("msgtype", "textcard");
        sendDataMap.put("agentid", qyEntity.getAgentId());
        HashMap<String, String> firstdata = new HashMap<>();
        firstdata.put("title", map.get(AlertEnum.TITLE.toString().toUpperCase()));
        firstdata.put("btntxt", "详情");
        firstdata.put("description", map.get(AlertEnum.QYWECHATCONTENT.toString()));
        firstdata.put("url", "url");
        if(map.containsKey(AlertEnum.URL.toString())){
            firstdata.put("url", map.get(AlertEnum.URL.toString()));
        }
        sendDataMap.put("textcard", firstdata);
        String sendStr = JSON.toJSONString(sendDataMap);
        if(map.containsKey(AlertEnum.EVENTIDEN.toString())){
            eventId = map.get(AlertEnum.EVENTIDEN.toString());
        }
        return sendStr;
    }

    @Override
    public Object selectFrom(){
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleByIdAndType(null,"5");
        GeneralMessageEntity qyEntity = mwWeixinTemplateDao.findWeiXinMessage(alertRuleTable.getRuleId());
        decrypt(qyEntity);
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
                log.error("监控平台解密失败:",e);
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
        log.info("企业微信接收人:" + touser);
        return touser.toString();
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            if(CollectionUtils.isNotEmpty(userIds)){
                //根据系统用户id,查询微信userId
                List<String> list = mwWeixinTemplateDao.selectQyWeixinUserId(userIds);

                //处理需要userIds,转换格式
                String touser = getSendTouer(list);
                this.touser = touser;
            }else {
                this.touser = map.get("qyWeChatUser");
            }
            log.info("企业微信接收人：" + touser);
            return null;
        }catch (Exception e){
            log.error("perform select accept 监控平台qiyeweixin:", e);
            return null;
        }

    }

    @Override
    public Object call() throws Exception {
        try{

            //2:根据系统用户id,查询接收人
            selectAccepts(userIds);
            if(touser == null || touser.equals("")){
                log.info("perform select accept 监控平台qiyeweixin is null");
                return null;
            }
            //3:查询发送方
            selectFrom();
            log.info("perform select send 监控平台qiyeweixin finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal 监控平台message:{}", "*****");

            //4发送企业微信消息
            sendMessage(sendMessage);
            log.info("qiyeweixin 监控平台message send finish");
            return null;
        }catch (Exception e){
            log.error("qiyeweixin 监控平台message send appear unknown error:",e);
            throw new Exception(e);
        }
    }
}
