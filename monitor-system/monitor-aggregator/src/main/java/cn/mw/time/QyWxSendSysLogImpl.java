package cn.mw.time;

import cn.mw.module.security.dto.EsSysLogTagDTO;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
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
 * 微信发送实现类
 */
public class QyWxSendSysLogImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private static HashMap<String,String> contentmap = new HashMap<>();

    private GeneralMessageEntity qyEntity;
    private Map<String, Object> map;

    private AlertRuleTableCommons alertRuleTable;

    //接收人（多）
    private String touser;
    public QyWxSendSysLogImpl(Map<String, Object> map, HashSet<Integer> userIds, String ruleId) {
        log.info("企业微信日志 map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.ruleId = ruleId;

    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("企业微信日志 satr sendMessage：" + sendMessage);
        Integer errcode = -1;
        String erroMessage = "";
        try {
            erroMessage = QyWeixinSendUtil.sendQyWeixinMessage(sendMessage,qyEntity,alertRuleTable);
            log.info("企业微信发送系统日志结果：" + erroMessage);
            errcode = Integer.parseInt(JSON.parseObject(erroMessage).get("errcode").toString());
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.info("error perform send message 企业微信日志:{}",e.getMessage());
        }

    }

    @Override
    public String dealMessage() {
        log.info("系统日志企业微信参数开始");
        StringBuffer sb = new StringBuffer();
        List<EsSysLogTagDTO> tagDTOList = (List<EsSysLogTagDTO>) map.get("tagList");
        List<String> tagNames = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(tagDTOList)){
            for(EsSysLogTagDTO s : tagDTOList){
                tagNames.add(s.getTagName());
            }
        }
        sb.append(AlertEnum.ASSETSIP.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("host").toString()).append('\n')
                .append(AlertEnum.AssetsName.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("hostName").toString()).append('\n')
                .append(AlertEnum.LOGLEVEL.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("severity_label").toString()).append('\n')
                .append(AlertEnum.DEVICETYPE.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("facility_label").toString()).append('\n')
                .append(AlertEnum.LOGTIME.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("@timestamp").toString()).append('\n')
                .append(AlertEnum.LOGINFO.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("message").toString()).append('\n')
                .append(AlertEnum.DATASOURCE.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get("dataSourceName").toString()).append('\n')
                .append(AlertEnum.RULETAG.toString()).append(AlertAssetsEnum.COLON.toString()).append(tagNames).append('\n');
        HashMap<String, Object> sendDataMap = new HashMap<>();
        sendDataMap.put("touser", touser);
        sendDataMap.put("msgtype", "textcard");
        sendDataMap.put("agentid", qyEntity.getAgentId());
        HashMap<String, String> firstdata = new HashMap<>();
        firstdata.put("title", "【系统日志通知】" + map.get("hostName").toString());
        firstdata.put("btntxt", "详情");
        firstdata.put("description", sb.toString());
        firstdata.put("url", "url");
        sendDataMap.put("textcard", firstdata);
        String sendStr = JSON.toJSONString(sendDataMap);
        return sendStr;
    }

    @Override
    public Object selectFrom(){
        GeneralMessageEntity qyEntity = mwWeixinTemplateDao.findWeiXinMessage(ruleId);
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        log.info("企业微信发送方：" + qyEntity);
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
                log.error("解密失败:",e);
            }
        }
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            List<String> list = mwWeixinTemplateDao.selectQyWeixinUserId(userIds);
            //处理需要userIds,转换格式
            String touser = getSendTouer(list);
            this.touser = touser;
            return touser;
        }catch (Exception e){
            log.info("perform select accept 企业微信日志:{}", e.getMessage());
            return null;
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
        log.info("企业微信日志接收人:" + touser);
        return touser.toString();
    }

    @Override
    public Object call() throws Exception {
        synchronized (contentmap){
            try{
                //2:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("企业微信日志 userIds" + userIds);
                if(touser == null || touser.equals("")){
                    log.info("perform select accept 企业微信日志 is null");
                    return null;
                }
                log.info("perform select accept 企业微信日志 finish");

                //3:查询发送方
                selectFrom();
                log.info("perform select send 企业微信日志 finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("企业微信日志 perform deal message:{}", "*****");

                //4发送微信消息
                sendMessage(sendMessage);
                log.info("企业微信日志 message send finish");
                return null;
            }catch (Exception e){
                log.info("企业微信日志 message send appear unknown error:{}",e.getMessage());
                throw new Exception(e.getMessage());
            }
        }
    }
}
