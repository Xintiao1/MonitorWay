package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.WeiXinSendUtil;
import cn.mw.monitor.weixin.entity.WeixinFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微信发送实现类
 */
public class WxSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收人（多）
    private HashSet<String> touser;

    private static HashMap<String,String> contentmap = new HashMap<>();

    private String title;

    //发送方（微信）
    private WeixinFromEntity qyEntity;

    private AlertRuleTableCommons alertRuleTable;

    public WxSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                              HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        log.info("WxSendmessage start!");
        log.info("微信 map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;


    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("weixin satr sendMessage：" + sendMessage);
        Integer errcode = -1;
        String erroMessage = "";
        try {
            for (String touser : this.touser) {
                //获取token
                String token = WeiXinSendUtil.getAccessToken(qyEntity.getAgentId(),qyEntity.getSecret(),alertRuleTable);
                WeiXinSendUtil.send(touser, contentmap,token,alertRuleTable);
            }
            errcode = 0;
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message weixin:",e);
        }finally {
            saveHis("微信",sendMessage,errcode,map.get("事件ID"),erroMessage,title,map.get("IP地址"),isAlarm,userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {

        //获取模板信息
        String hostid = map.get("HOSTID");
        log.info("微信 hostid：" + hostid);
        if (map.get("告警标题") != null) {
            log.info("微信告警标题开始");
            title = map.get("告警标题");
            contentmap.put("templateid",qyEntity.getAlertTempleate());
            contentmap.put("keyword1",map.get("主机名称") + "-" + map.get("IP地址"));
            contentmap.put("keyword2",map.get("告警时间"));
            contentmap.put("keyword3", MWAlertLevelParam.actionAlertLevelMap.get(map.get(AlertEnum.ALERTLEVEL.toString())));
            contentmap.put("keyword4",map.get("告警信息"));
            contentmap.put("keyword5",map.get("当前状态"));
            contentmap.put("remark","请运维人员相互告知!");
            contentmap.put("first",map.get("告警标题"));
            log.info("微信告警标题结束");
            /*template_id = qyEntity.getAlertTempleate();
            keyword1 = map.get("主机名称");
            keyword2 = map.get("告警时间");
            keyword3 = map.get("告警等级");
            keyword4 = map.get("告警信息");
            keyword5 = map.get("当前状态");
            remark = "请运维人员相互告知！";
            first = map.get("告警标题");*/
        } else if (map.get("恢复标题") != null) {
            log.info("恢复标题题开始");
            title = map.get("恢复标题");
            contentmap.put("templateid",qyEntity.getRecoveryTempleate());
            contentmap.put("keyword1",map.get("主机名称") + "-" + map.get("IP地址"));
            contentmap.put("keyword2",map.get("恢复信息"));
            contentmap.put("keyword3",map.get("故障时间"));
            contentmap.put("keyword4",map.get("恢复时间"));
            contentmap.put("keyword5",MWAlertLevelParam.actionAlertLevelMap.get(map.get(AlertEnum.RECOVERYLEVEL.toString())));
            contentmap.put("remark","告警已恢复!");
            contentmap.put("first",map.get("恢复标题"));
            log.info("恢复标题题结束");
            /*template_id = qyEntity.getRecoveryTempleate();
            keyword1 = map.get("主机名称");
            keyword2 = map.get("恢复信息");
            keyword3 = map.get("故障时间");
            keyword4 = map.get("恢复时间");
            keyword5 = map.get("恢复等级");
            first = map.get("恢复标题");
            remark = "告警已恢复！";*/
        }
        return super.dealMessage();
    }

    @Override
    public Object selectFrom(){
        WeixinFromEntity qyEntity = mwWeixinTemplateDao.findWeiXinFrom(ruleId);
        AlertRuleTableCommons alertRuleTable = mwWeixinTemplateDao.selectRuleById(ruleId);
        this.alertRuleTable = alertRuleTable;
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        if(StringUtils.isNotEmpty(qyEntity.getAgentId())){
            contentmap.put("appid",qyEntity.getAgentId());
            contentmap.put("secret",qyEntity.getSecret());
        }
        return qyEntity;
    }

    public  void decrypt(WeixinFromEntity weiXin) {
        log.info("WeixinFromEntity:" + weiXin.getSecret());
        if(weiXin != null){
            try {
                if (weiXin.getSecret() != null) {
                    weiXin.setSecret(EncryptsUtil.decrypt(weiXin.getSecret()));
                    weiXin.setAgentId(EncryptsUtil.decrypt(weiXin.getAgentId()));
                }
                log.info("解密完成");
            } catch (Exception e) {
                log.error("微信解密错误：", e);
            }
        }
    }

    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        try {
            //根据系统用户id,查询微信userId
            List<String> list = mwWeixinTemplateDao.selectWeixinUserId(userIds);
            log.info("wexin list" + list);
            HashSet<String> touser = (HashSet<String>) list.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
            //处理需要userIds,转换格式
            log.info("wexin touser" + touser);
            this.touser = touser;
            return touser;
        }catch (Exception e){
            log.error("perform select accept weixin:", e);
            return null;
        }

    }

    @Override
    public Object call() throws Exception {
        synchronized (contentmap){
            try{
                //1判断级别是否符合
                if(!outPut()){
                    return null;
                }
                log.info("the alert information level is satisfied");

                //2:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("weixin userIds" + userIds);
                if(touser == null || touser.equals("")){
                    log.info("perform select accept weixin is null");
                    return null;
                }
                log.info("perform select accept weixin finish");

                //3:查询发送方
                selectFrom();
                log.info("perform select send weixin finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("微信 perform deal message:{}", "*****");

                //4发送企业微信消息
                sendMessage(sendMessage);
                log.info("weixin message send finish");
                return null;
            }catch (Exception e){
                log.error("weixin message send appear unknown error:",e);
                throw new Exception(e.getMessage());
            }
        }
    }
}
