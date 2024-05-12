package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.WeLinkRuleParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.WeLinkSendUtil;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class WelinkSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    String[] to;

    private static HashMap<String,Object> contentmap = new HashMap<>();

    //发送方
    private WeLinkRuleParam qyEntity;

    public WelinkSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                  HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {

        log.info("Welink map：" + map);
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
            String result = WeLinkSendUtil.send(contentmap,qyEntity);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(jsonObject.getString("code").equals("0")){
                errcode = 0;
            }else{
                erroMessage = jsonObject.getString("message");
            }
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message WeLink:{}",e);
        }finally {
            saveHis("WeLink",sendMessage,errcode,map.get("事件ID"),erroMessage,title,map.get("IP地址"),isAlarm,userIds, map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {
        String msg = super.dealMessage();
        String title = null;
        if(isAlarm){
            title = map.get("告警标题");
        }else {
            title = map.get("恢复标题");
        }
        contentmap.put("toUserList",to);
        contentmap.put("msgRange",0);
        contentmap.put("publicAccID", qyEntity.getPublicAccId());
        contentmap.put("msgTitle",title);
        contentmap.put("msgContent",msg);
        contentmap.put("urlType","");
        contentmap.put("urlPath","");
        contentmap.put("msgOwner","");
        return super.dealMessage();
    }

    @Override
    public Object selectFrom(){
        WeLinkRuleParam qyEntity = mwWeixinTemplateDao.findWelinkFrom(ruleId);
        this.qyEntity = qyEntity;
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
            List<String> weLinkUserIds = WeLinkSendUtil.getUserId(sendPhones,qyEntity);
            String[] to = weLinkUserIds.toArray(new String[weLinkUserIds.size()]);
            this.to = to;
            return sendPhones;
        }catch (Exception e){
            log.error("perform select accept welink:", e);
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

                //2:查询发送方
                selectFrom();
                log.info("perform select send welink finish");

                //3:根据系统用户id,查询接收人
                selectAccepts(userIds);
                log.info("welink userIds" + userIds);

                log.info("perform select accept welink finish");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("welink perform deal message:{}", "*****");

                //4发送企业微信消息
                sendMessage(sendMessage);
                log.info("welink message send finish");
                return null;
            }catch (Exception e){
                log.error("welink message send appear unknown error:",e);
                throw new Exception(e.getMessage());
            }
        }
    }
}
