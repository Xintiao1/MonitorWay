package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.util.MD5EncryptUtil;
import cn.mw.monitor.util.entity.HuaXingRuleParam;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import cn.mw.monitor.plugin.alert.AlertPlugin;

/**
 *
 */
public class TXinSendRancherMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    String toUser;

    private static HashMap<String,Object> contentmap = new HashMap<>();

    //发送方
    private HuaXingRuleParam qyEntity;

    private AlertRuleTableCommons alertRuleTable;

    private AlertPlugin alertPlugin;

    private String eventId="-1";

    public TXinSendRancherMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds) {

        log.info("监控平台T信 map：" + map);
        this.map = map;
        AlertPlugin alertPlugin = ApplicationContextProvider.getBean(AlertPlugin.class);
        this.userIds = userIds;
        this.alertPlugin = alertPlugin;
    }

    @Override
    public void sendMessage(String sendMessage) {
        Integer errcode = -1;
        String erroMessage = "";
        try {
            Map<String, String> header = new HashMap<>();
            header.put("appId", qyEntity.getAppId());
            String timestamp = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();
            timestamp = sdf.format(date);
            header.put("timestamp",timestamp);
            String sign = MD5EncryptUtil.sign(qyEntity.getAppSecret() + timestamp);
            header.put("sign",sign);
            String result = LoadUtil.doPost(qyEntity.getUrl(), contentmap, header,alertRuleTable);
            log.info("监控平台T信Url：" + qyEntity.getUrl());
            log.info("监控平台T信contentmap：" + contentmap);
            log.info("监控平台T信header：" + header);
            log.info("监控平台T信发送结果：" + result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(jsonObject.getString("code").equals("0")){
                errcode = 0;
            }else{
                erroMessage = jsonObject.getString("message");
            }
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message 监控平台T信:{}",e);
        }finally {
            saveHis("监控平台T信",sendMessage,errcode,eventId,erroMessage,title,map.get("IP地址"),isAlarm,userIds, map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {

        HashMap<String, Object> sendDataMap = new HashMap<>();
        String cid = UUIDUtils.getUUID();
        sendDataMap.put("cid",cid);
        sendDataMap.put("pluginId",qyEntity.getPluginId());
        if(StringUtils.isNotBlank(qyEntity.getToken())){
            sendDataMap.put("serToken",qyEntity.getToken());
        }
        sendDataMap.put("summary",map.get("TITLE"));
        List<HashMap<String,String>> itemsMapValue = new ArrayList<>();
        HashMap<String,Object> itemsMap = new HashMap<>();
        List<HashMap<String,Object>> items = new ArrayList<>();
        for(int i=0;i<=2;i++){
            if(i<2){
                HashMap<String,String> itemsMapValueMap = new HashMap<>();
                itemsMapValueMap.put("str",map.get("TITLE"));
                itemsMapValue.add(itemsMapValueMap);
            }else if(i == 1){
                HashMap<String,String> itemsMapValueMap = new HashMap<>();
                itemsMapValueMap.put("str","");
                itemsMapValue.add(itemsMapValueMap);
            }else {
                HashMap<String,String> itemsMapValueMap = new HashMap<>();
                itemsMapValueMap.put("str",map.get("TxinContent"));
                itemsMapValue.add(itemsMapValueMap);
            }

        }
        itemsMap.put("value",itemsMapValue);
        itemsMap.put("type",2);
        items.add(itemsMap);
        sendDataMap.put("items",items);
        if(map.containsKey(AlertEnum.URL.toString())){
            HashMap<String,Object> btnValueMap = new HashMap<>();
            List<HashMap<String,Object>> btnValueList = new ArrayList<>();
            HashMap<String,Object> btnValue = new HashMap<>();
            btnValue.put("str","详情");
            btnValue.put("targetUrl",map.get(AlertEnum.URL.toString()));
            btnValueList.add(btnValue);
            btnValueMap.put("value",btnValueList);
            sendDataMap.put("btn",btnValueMap);
        }
        HashMap<String, Object> filter = new HashMap<>();
        filter.put("toUser", toUser);
        sendDataMap.put("filter",filter);
        if(map.containsKey(AlertEnum.EVENTIDEN.toString())){
            eventId = map.get(AlertEnum.EVENTIDEN.toString());
        }
        contentmap = sendDataMap;
        return sendDataMap.toString();
    }

    @Override
    public Object selectFrom(){
        HuaXingRuleParam qyEntity = mwWeixinTemplateDao.findHuaXingMessage(null);
        this.qyEntity = qyEntity;
        log.info("监控平台监控平台T信 selectFrom end! from: " + qyEntity);
        this.alertRuleTable = mwWeixinTemplateDao.selectRuleById(qyEntity.getRuleId());
        return qyEntity;
    }

    @Override
    public HashSet<String> selectAccepts(HashSet<Integer> userIds) {
        try {
            List<String> list = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(userIds)){
                list = mwWeixinTemplateDao.selectLoginName(userIds);
            }else {
                String wechatId = map.get("qyWeChatUser");
                String[] wechatIdLits = wechatId.split("\\|");
                log.info("监控平台T信wechatIdLits：" + wechatIdLits.length);
                log.info("监控平台T信wechatIdLits tostring：" + Arrays.toString(wechatIdLits));
                list = alertPlugin.getUserNameByQyWechatId(wechatIdLits);
                log.info("监控平台T信alertPlugin结果返回：" + list);
            }
            toUser = getSendTouer(list);
            return null;
        }catch (Exception e){
            log.error("perform select accept 监控平台T信:", e);
            return null;
        }

    }

    public String getSendTouer(List<String> userIds) {
        StringBuffer touser = new StringBuffer();
        HashSet<String> userIds1 = (HashSet<String>) userIds.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        if (userIds1 != null && userIds1.size() > 0 && userIds1.size() <= 1000) {
            Iterator iterator = userIds1.iterator();
            while (iterator.hasNext()) {
                String temp = iterator.next().toString();
                if(temp.contains("@")){
                    temp = temp.substring(0,temp.indexOf("@"));
                }
                touser.append(",").append(temp);
            }
        }else {
            return "";
        }
        touser.replace(0, 1, "");
        log.info("监控平台T信接收人:" + touser);
        return touser.toString();
    }

    @Override
    public Object call() throws Exception {
        try{
            //1判断级别是否符合
            //2:查询发送方
            selectFrom();
            log.info("perform select send 监控平台T信 finish");

            //3:根据系统用户id,查询接收人
            selectAccepts(userIds);
            log.info("监控平台T信 userIds" + userIds);

            log.info("perform select accept 监控平台T信 finish");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("监控平台T信 perform deal message:{}", "*****");

            //4发送企业微信消息
            sendMessage(sendMessage);
            log.info("监控平台T信 message send finish");
            return null;
        }catch (Exception e){
            log.error("监控平台T信 message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }
}
