package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.weixin.entity.UserInfo;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 企业微信发送实现类
 */
public class LeShanBankMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    private Map<String, Object> param = new HashMap<>();

    public LeShanBankMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                  HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        log.info("乐山银行接收人：" + userIds);

    }

    @Override
    public void sendMessage(String sendMessage){
        log.info("乐山市银行发送信息");
        int isSuccess = -1;
        String error = "";
        try {
            String url = env.getProperty("leshanbank.url");
            log.info("乐山市url：" + url);
            error = DingdingQunSendUtil.doPost(url,param,null);
            log.info("乐山市银行发送param：" + param);
            log.info("乐山市银行发送结果：" + error);
            if(error.contains("success")){
                isSuccess = 0;
            }
        }catch (Exception e){
            error = e.getMessage();
            log.error("error perform send message 乐山市银行:",e);
        }finally {
            //保存记录
            log.info("乐山银行保存记录接收人：" + userIds);
            saveHis("乐山市银行",sendMessage,isSuccess,map.get("事件ID"),error,this.title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }

    }


    @Override
    public String dealMessage() {
        Map<String, Object> param = new HashMap<>();
        String alertTitle = map.get(AlertEnum.ALERTTITLE.toString());
        String alertInfo = map.get(AlertEnum.ALERTINFO.toString());
        String alertLevel = map.get(AlertEnum.ALERTLEVEL.toString());
        String lastOccurrence = map.get(AlertEnum.ALERTTIME.toString());
        String problemDetails = map.get(AlertEnum.PROBLEMDETAILS.toString());
        String status = map.get(AlertEnum.NOWSTATE.toString());
        if(!isAlarm){
            alertTitle = map.get(AlertEnum.RECOVERYTITLE.toString());
            alertInfo = map.get(AlertEnum.RECOVERYINFO.toString());
            alertLevel = map.get(AlertEnum.RECOVERYLEVEL.toString());
            lastOccurrence = map.get(AlertEnum.RECOVERYTIME.toString());
            problemDetails = map.get(AlertEnum.RECOVERYDETAILS.toString());
            status = "RESOLVED";
        }
        String msg = super.dealMessage();
        //msg = msg.replaceAll("\n","|");
        param.put("SourceEventID",map.get(AlertEnum.EVENTID.toString()));
        param.put("SourceCIName",alertInfo);
        param.put("SourceAlertKey",problemDetails);
        param.put("SourceSeverity",alertLevel);

        param.put("LastOccurrence",datePare(lastOccurrence));
        param.put("Summary",alertTitle);
        List<Integer> userIdList = new ArrayList<>(userIds);
        param.put("User_a",getUserName(userIdList.get(0)));
        if(userIdList.size() == 1){
            param.put("User_b",getUserName(userIdList.get(0)));
        }
        if(userIdList.size()>1){
            param.put("User_b",getUserName(userIdList.get(1)));
        }
        param.put("Item",problemDetails);
        param.put("Status",status);
        param.put("value",msg);
        this.param = param;
        return JSON.toJSONString(param);
    }

    private String datePare(String inputStr){
        String inputFormat = "yyyy.MM.dd-HH:mm:ss";
        String outputFormat = "yyyy.MM.dd HH:mm:ss";
        String outputStr = null;
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
            Date inputDate = inputDateFormat.parse(inputStr);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
            outputStr = outputDateFormat.format(inputDate);

        }  catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return outputStr;
    }

    public String getUserName(Integer userId){
        HashSet<Integer> userIds = new HashSet<>();
        userIds.add(userId);
        StringBuffer sb = new StringBuffer();
        if(CollectionUtils.isNotEmpty(userIds)){
            List<UserInfo> userName = mwWeixinTemplateDao.selectUserName(userIds);
            for(UserInfo temp : userName){
                sb.append(temp.getUserName()).append(temp.getPhoneNumber()).append(",");
            }
            return sb.toString().substring(0,sb.length()-1);
        }
        return sb.toString();
    }

    @Override
    public Object selectFrom(){
        return null;
    }


    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        try {
            return null;
        }catch (Exception e){
            log.error("perform select accept 乐山市银行:", e);
            return null;
        }

    }

    @Override
    public Object call() throws Exception {
        synchronized (param){
            try{
                //1判断级别是否符合
                if(!outPut()){
                    return null;
                }
                log.info("the alert information level is satisfied");

                //4:拼接发送信息
                String sendMessage = dealMessage();
                log.info("perform deal message:{}", param);

                //4发送企业微信消息
                log.info("乐山市银行 message send star");
                sendMessage(sendMessage);
                log.info("乐山市银行 message send finish");
                return null;
            }catch (Exception e){
                log.error("乐山市银行 message send appear unknown error:",e);
                throw new Exception(e.getMessage());
            }
        }
    }
}
