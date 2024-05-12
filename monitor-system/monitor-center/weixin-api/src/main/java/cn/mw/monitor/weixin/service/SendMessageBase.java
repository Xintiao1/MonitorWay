package cn.mw.monitor.weixin.service;

import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.websocket.Message;
import cn.mw.monitor.websocket.WebSocketGetCount;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.entity.AlertRecordTable;
import cn.mw.monitor.weixin.entity.UserInfo;
import cn.mw.monitor.weixin.service.impl.EmailSendHuaXingImpl;
import cn.mw.monitor.weixin.service.impl.TXinSendRancherMessageiImpl;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * 发送信息的基类
 * 1：业务信息相关字段通过子类构造器注入
 * 2：需要的spring注入类对象使用工具类ApplicationContextProvider获取
 * 3：事务相关操作没有（只有一个保存历史记录）
 */
@Slf4j
public abstract class SendMessageBase implements Callable<Object> {

    protected MwWeixinTemplateDao mwWeixinTemplateDao;

    public Environment env;

    private MWMessageService mwMessageService;

    public SendMessageBase() {
        MwWeixinTemplateDao mwWeixinTemplateDao = ApplicationContextProvider.getBean(MwWeixinTemplateDao.class);
        this.mwWeixinTemplateDao = mwWeixinTemplateDao;
        Environment env = ApplicationContextProvider.getBean(Environment.class);
        this.env = env;
        MWMessageService mwMessageService = ApplicationContextProvider.getBean(MWMessageService.class);
        this.mwMessageService = mwMessageService;
    }


    //发送的信息
    protected HashMap<String, String> map = new HashMap<>();
    //true:告警标题  false:恢复标题  null:未传入标题
    protected Boolean isAlarm;

    protected String title;

    //接收的用户
    protected HashSet<Integer> userIds;

    //可以发送的告警级别
    protected HashSet<String> severity;

    //这个发送的信息代表的资产
    protected MwTangibleassetsDTO assets;

    //发送方标识（通过这个查询发送方信息）
    protected String ruleId;

    //通用发送方式,非告警发送
    private boolean isCommon = false;

    public boolean isCommon() {
        return isCommon;
    }

    public void setCommon(boolean common) {
        isCommon = common;
    }

    //消息发送上下文
    private MessageContext messageContext;

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    //发送消息
    public abstract void sendMessage(String sendMessage) throws MalformedURLException;

    //查询发送方
    public abstract Object selectFrom();

    //查询接收方
    public abstract Object selectAccepts(HashSet<Integer> userIds);

    //处理消息
    public  String dealMessage(){
        StringBuffer content = new StringBuffer();
        String alertLevel = env.getProperty("alert.level");
        if(isAlarm){
            if(map.get(AlertEnum.DefaultSelection.toString()).equals(AlertEnum.Default.toString())){
                title = map.get(AlertEnum.ALERTTITLE.toString());
                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
                content.append(AlertAssetsEnum.LeftBracketZH.toString() + AlertEnum.ALERT.toString() + AlertAssetsEnum.RightBracketZH.toString())
                        .append(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Domain.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.ALERTTITLE.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTITLE.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.HostNameZH.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.HostNameZH.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.SystemInfo.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.SystemInfo.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.ALERTINFO.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTINFO.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.ALERTLEVEL.toString() + AlertAssetsEnum.COLON.toString()).append(MWAlertLevelParam.actionAlertLevelMap.get(map.get(AlertEnum.ALERTLEVEL.toString()))).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.ALERTSTARTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.ALERTTIME.toString() + AlertAssetsEnum.COLON.toString()).append(simpleDateFormat.format(date)).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.LONGTIMECH.toString() + AlertAssetsEnum.COLON.toString()).append(SeverityUtils.CalculateTime(datePare(map.get("告警时间")))).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.PROBLEMDETAILS.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.PROBLEMDETAILS.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.NOWSTATE.toString() + AlertAssetsEnum.COLON.toString()).append(AlertEnum.UNUSUAL.toString()).append(AlertAssetsEnum.Comma.toString()).append('\n');
            }
        }else {
            title = map.get(AlertEnum.RECOVERYTITLE.toString());
            if(map.get(AlertEnum.DefaultSelection.toString()).equals(AlertEnum.Default.toString())){
                content.append(AlertAssetsEnum.LeftBracketZH.toString() + AlertEnum.RECOVERY.toString() + AlertAssetsEnum.RightBracketZH.toString())
                        .append(AlertEnum.Domain.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Domain.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYTITLE.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTITLE.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.HostNameZH.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.HostNameZH.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.SystemInfo.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.SystemInfo.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYINFO.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYINFO.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYLEVEL.toString() + AlertAssetsEnum.COLON.toString()).append(MWAlertLevelParam.actionAlertLevelMap.get(map.get(AlertEnum.RECOVERYLEVEL.toString()))).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.IPAddress.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.FAILURETIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.FAILURETIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYTIME.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTIME.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RECOVERYDETAILS.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYDETAILS.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n')
                        .append(AlertEnum.RENEWSTATUS.toString() + AlertAssetsEnum.COLON.toString()).append(AlertEnum.NORMAL.toString()).append(AlertAssetsEnum.Comma.toString()).append('\n');

            }
        }
        if(map.get(AlertEnum.AssociatedModule.toString())!=null){
            content.append(AlertEnum.AssociatedModule.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssociatedModule.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AutoSequence.toString()) != null){
            content.append(AlertEnum.AutoSequence.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AutoSequence.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AssetsID.toString()) != null){
            content.append(AlertEnum.AssetsID.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssetsID.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AssetsName.toString()) != null){
            content.append(AlertEnum.AssetsName.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssetsName.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.InBandIp.toString()) != null){
            content.append(AlertEnum.InBandIp.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.InBandIp.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.ASSETSTYPE.toString()) != null){
            content.append(AlertEnum.ASSETSTYPE.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ASSETSTYPE.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AssetsTypeSubId.toString()) != null){
            content.append(AlertEnum.AssetsTypeSubId.toString()  + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssetsTypeSubId.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.PollingEngine.toString()) != null){
            content.append(AlertEnum.PollingEngine.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.PollingEngine.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.MonitorMode.toString()) != null){
            content.append(AlertEnum.MonitorMode.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.MonitorMode.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.Manufacturer.toString()) != null){
            content.append(AlertEnum.Manufacturer.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Manufacturer.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.Specifications.toString()) != null){
            content.append(AlertEnum.Specifications.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Specifications.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.Description.toString()) != null){
            content.append(AlertEnum.Description.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Description.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AssetsStatus.toString()) != null){
            content.append(AlertEnum.AssetsStatus.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssetsStatus.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.DeleteFlag.toString()) != null){
            content.append(AlertEnum.DeleteFlag.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.DeleteFlag.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.MonitorFlag.toString()) != null){
            content.append(AlertEnum.MonitorFlag.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.MonitorFlag.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.SettingFlag.toString()) != null){
            content.append(AlertEnum.SettingFlag.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.SettingFlag.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.Creator.toString()) != null){
            content.append(AlertEnum.Creator.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Creator.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.CreateDate.toString()) != null){
            content.append(AlertEnum.CreateDate.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.CreateDate.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.Modifier.toString()) != null){
            content.append(AlertEnum.Modifier.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Modifier.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.ModificationDate.toString()) != null){
            content.append(AlertEnum.ModificationDate.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ModificationDate.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.ScanSuccessId.toString()) != null){
            content.append(AlertEnum.ScanSuccessId.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ScanSuccessId.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.MonitorServerId.toString()) != null){
            content.append(AlertEnum.MonitorServerId.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.MonitorServerId.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.Timing.toString()) != null){
            content.append(AlertEnum.Timing.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.Timing.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.TpServerHostName.toString()) != null){
            content.append(AlertEnum.TpServerHostName.toString()  + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.TpServerHostName.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.TemplateId.toString()) != null){
            content.append(AlertEnum.TemplateId.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.TemplateId.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AssetsUuid.toString()) != null){
            content.append(AlertEnum.AssetsUuid.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssetsUuid.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.AssetsSerialnum.toString()) != null){
            content.append(AlertEnum.AssetsSerialnum.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.AssetsSerialnum.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        if(map.get(AlertEnum.CompressNumber.toString())!=null){
            content.append(AlertEnum.CompressNumber.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.CompressNumber.toString())).append(AlertAssetsEnum.Comma.toString()).append('\n');
        }
        content.append(AlertEnum.EVENTID.toString() + AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.EVENTID.toString()));
        return content.toString();
    }

    private String datePare(String inputStr){
        String inputFormat = "yyyy.MM.dd-HH:mm:ss";
        String outputFormat = "yyyy-MM-dd HH:mm:ss";
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

    //保存记录
    public void saveHis(String method, String text, Integer isSuccess, String eventId, String error, String title, String ip, Boolean isAlarm, HashSet<Integer> userIds, String hostId) {
        saveHis(method,text,isSuccess,eventId,error,title,ip,isAlarm,userIds,null, hostId);
    }
    //保存记录
    public void saveHis(String method, String text, Integer isSuccess, String eventId, String error, String title, String ip, Boolean isAlarm, HashSet<Integer> userIds,String[] email, String hostId) {
        try {
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod(method);
            recored.setText(text);
            recored.setIsSuccess(isSuccess);
            recored.setHostid(hostId);
            recored.setEventid(eventId);
            recored.setError(error);
            recored.setTitle(title);
            recored.setIp(ip);
            recored.setIsAlarm(isAlarm);
            recored.setUserIds(userIds);
            mwWeixinTemplateDao.insertRecord(recored);
            if(CollectionUtils.isNotEmpty(userIds)){
                mwWeixinTemplateDao.insertRecordUserMapper(recored.getId(),userIds);
                if(isSuccess != 0){
                    List<MWUser> mwUsers = mwWeixinTemplateDao.selectByUserId(userIds);
                    mwMessageService.sendFailAlertMessage(title + "发送失败！",mwUsers,"告警通知失败提醒",false,null);
                }
            }
            String alertLevel = env.getProperty("alert.level");
            if(isSuccess != 0 && alertLevel.equals(AlertEnum.HUAXING.toString())){
                sendEmali(text);
            }
            if(email != null){
                List<String> emails = Arrays.asList(email);
                mwWeixinTemplateDao.insertRecordEmailMapper(recored.getId(),emails);
            }
            log.info("告警消息保存历史记录表成功:{}",recored);
        }catch (Exception e) {
            log.error("告警消息保存历史记录表失败:",e);
        }
    }

    private void sendEmali(String text){
        HashSet<Integer> list = new HashSet<>();
        list.add(106);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        HashMap<String, String> mapError = new HashMap<>();
        mapError.put("TITLE","告警通知失败通知！");
        mapError.put("emailContent",text);
        pool.submit(new EmailSendHuaXingImpl(mapError, list, null ,null));
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程错误：" + e);
        }
    }


    //判断该消息的告警/恢复级别是否满足
    public Boolean conformLevel() {
        if(isAlarm == null){
            return null;
        }
        if(isAlarm){
            String level = map.get(AlertEnum.ALERTLEVEL.toString());
            if(level == null){
                return null;
            }else {
                return severity.contains(level) ? true:false;
            }
        }else {
            String level = map.get(AlertEnum.RECOVERYLEVEL.toString());
            if(level == null){
                return null;
            }else {
                return severity.contains(level) ? true:false;
            }
        }
    }
    public Boolean outPut(){
        if(conformLevel() == null ){
            log.info("the alert information field is incomplete:{}", map);
            return false;
        }else {
            if (conformLevel() == false){
                log.info("the alert information level is not satisfied：" + map);
                return false;
            }
        }
        return true;
    }

    public String getUserName(HashSet<Integer> userIds){
        StringBuffer sb = new StringBuffer();
        if(CollectionUtils.isNotEmpty(userIds)){
            List<UserInfo> userName = mwWeixinTemplateDao.selectUserName(userIds);
            for(UserInfo temp : userName){
                sb.append(temp.getUserName()).append("-").append(temp.getPhoneNumber()).append(",");
            }
            return sb.toString().substring(0,sb.length()-1);
        }
        return sb.toString();
    }


}
