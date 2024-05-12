package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.action.param.UserIdsType;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.util.Sha256Encryption;
import cn.mw.monitor.weixin.entity.MwCaiZhengTingSMSFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 企业微信发送实现类
 */
public class WorkSystemSendMessageimpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private HashMap<String, String> map;

    private UserIdsType userIdsType;

    private MwCaiZhengTingSMSFromEntity qyEntity;

    private String ruleId;

    private List<String> userName;

    private List<String> groupName;

    private Map<String, Object> msgMap;

    public WorkSystemSendMessageimpl(HashMap<String, String> map, UserIdsType userIdsType, String ruleId) {
        this.map = map;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        this.userIdsType = userIdsType;
        this.ruleId = ruleId;
    }

    @Override
    public void sendMessage(String sendMessage){
        String result = null;
        String erroMessage = "";
        Integer errcode = -1;
        try {
            String workFlow = qyEntity.getUrl() + "/api/v1/workflow/process.do";
            Map<String, String> headParam = new HashMap<>();
            headParam.put("token", getToken());
            headParam.put("Content-type", "application/json;charset=UTF-8");
            log.info("工单系统msgMap：" + msgMap);
            result = LoadUtil.httpPost(workFlow, msgMap, null);
            log.info("工单系统获取结果:" + result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            Boolean success = (Boolean) jsonObject.get("success");
            if(success){
                errcode = 0;
            }
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("工单系统错误:{}",e);
        }finally {
            saveHis("工单系统",JSON.toJSONString(msgMap),errcode,map.get("事件ID"),erroMessage,map.get(AlertEnum.ALERTTITLE.toString()),map.get("IP地址"),isAlarm,userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }


    private String getToken() {
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        boolean isHas = redisUtils.hasKey(qyEntity.getAccount() + "Token");
        redisUtils.del(qyEntity.getAccount() + "Token");
        if (!isHas) {
            Map<String, String> headParam = new HashMap<>();
            headParam.put("Content-type", "application/json;charset=UTF-8");
            String loginUrl = qyEntity.getUrl() + "/signin.do?acc=ACC&pd=PD&vcode=&rememberMe=0";
            String acc = qyEntity.getAccount();
            String pd = Sha256Encryption.getSha256Str(qyEntity.getPassword());
            loginUrl = loginUrl.replaceAll("ACC", acc)
                    .replaceAll("PD", pd);
            String tokenStr = LoadUtil.httpPost(loginUrl,null,headParam);
            log.info("工单系统获取token结果:" + tokenStr);
            JSONObject jsonObject = JSONObject.parseObject(tokenStr);
            String token = jsonObject.getString("token");
            redisUtils.set(qyEntity.getAccount() + "Token", token, 86400);
        }
        String token = (String) redisUtils.get(qyEntity.getAccount() + "Token");
        return token;
    }

    @Override
    public String dealMessage() {
        if(isAlarm){
            Map<String, Object> msgMap = new HashMap<>();
            Map<String, Object> jsonData = new HashMap<>();
            List<Object> bosList = new ArrayList<>();
            Map<String, Object> bos = new HashMap<>();
            bos.put("boDefId", "20000003381563");
            bos.put("formKey", "HPD");
            bos.put("readOnly", false);
            Map<String, Object> data = new HashMap<>();

            data.put("bo_Def_Id_", "20000003381563");
            data.put("Title", map.get(AlertEnum.ALERTTITLE.toString()));
            data.put("Description", map.get(AlertEnum.PROBLEMDETAILS.toString()));
            String date = map.get(AlertEnum.ALERTTIME.toString()).replaceAll("-"," ").replaceAll("\\.","-");
            data.put("FSTime", date);
            data.put("Priority", map.get(AlertEnum.ALERTLEVEL.toString()));
            data.put("Priority_name", map.get(AlertEnum.ALERTLEVEL.toString()));
            if(CollectionUtils.isNotEmpty(userName)){
                StringBuffer sb = new StringBuffer();
                for (String user : userName){
                    sb.append(user).append(",");
                }
                String user = sb.toString().substring(0,sb.length()-1);
                data.put("Assignee", user);
                data.put("Assignee_name", user);
            }
            if(CollectionUtils.isNotEmpty(groupName)){
                StringBuffer sb = new StringBuffer();
                for (String group : groupName){
                    sb.append(group).append(",");
                }
                String group = sb.toString().substring(0,sb.length()-1);
                data.put("AssignedGroup_name", group);
                data.put("AssignedGroup", group);
            }
            bos.put("data", data);
            bosList.add(bos);
            jsonData.put("bos", bosList);
            msgMap.put("jsonData", JSON.toJSONString(jsonData));
            msgMap.put("solId", "20000006355281");
            msgMap.put("userAccount", qyEntity.getAccount());
            this.msgMap = msgMap;
        }
        return null;
    }

    @Override
    public Object selectFrom(){
        MwCaiZhengTingSMSFromEntity qyEntity = mwWeixinTemplateDao.findCaiZhengTingSmsFrom(ruleId);
        this.qyEntity = qyEntity;
        return qyEntity;
    }


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        if(CollectionUtils.isNotEmpty(userIdsType.getPersonUserIds())){
            userName = mwWeixinTemplateDao.selectLoginName(userIdsType.getPersonUserIds());
        }
        if(CollectionUtils.isNotEmpty(userIdsType.getGroupIds())){
            groupName = mwWeixinTemplateDao.selectGroupName(userIdsType.getGroupIds());
        }
        return null;
    }

    @Override
    public Object call() throws Exception {
        try{
            if(!isAlarm){
                return null;
            }

            selectAccepts(userIds);
            log.info("perform deal selectAccepts");

            selectFrom();
            log.info("perform deal selectFrom");

            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送企业微信消息
            log.info("工单平台 message send star");
            sendMessage(sendMessage);
            log.info("工单平台 message send finish");
            return null;
        }catch (Exception e){
            log.error("工单平台 message send appear unknown error:",e);
            throw new Exception(e.getMessage());
        }
    }
}
