package cn.mw.monitor.weixin.service.impl;

import cn.mw.module.security.dto.EsSysLogTagDTO;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.BussinessAlarmInfoParam;
import cn.mw.monitor.util.EmailSendUtil;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.entity.HtmlTemplateEmailParam;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 邮件发送实现类
 */
public class EmailSendHuaXingImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方email
    private String[] sendEmails;

    private String[] emailToCC;

    //发送方
    private EmailFrom from = new EmailFrom();

    private HashMap<String, String> map;

    private EmailSendUtil emailSendUtil;

    private Boolean isHtml = false;

    List<Integer> emailUserIds;

    List<Integer> emailCCUserIds;

    private String eventId="-1";
    public EmailSendHuaXingImpl(HashMap<String, String> map, HashSet<Integer> userIds, List<Integer> emailUserIds, List<Integer> emailCCUserIds) {
        this.map = map;
        this.userIds = userIds;
        EmailSendUtil emailSendUtil = ApplicationContextProvider.getBean(EmailSendUtil.class);
        this.emailSendUtil = emailSendUtil;
        this.emailUserIds = emailUserIds;
        this.emailCCUserIds = emailCCUserIds;
    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("监控平台emails sendMessage start!");
        Integer isSuccess = -1;
        String erroMessage = "";
        try{
            erroMessage = emailSendUtil.sendTextEmail(sendEmails, from,  map.get("TITLE"),map.get("emailContent"),isHtml);
            log.info("监控平台邮箱发送结果接口：" + erroMessage);
            isSuccess = "success".equals(erroMessage)? 0 : 1;
        }catch(Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message 监控平台email:{}",e.getMessage());
        }finally {
            saveHis("监控平台邮箱发送",map.get("emailContent"),isSuccess,eventId,erroMessage,map.get(AlertEnum.TITLE.toString().toUpperCase()),"-1",true, userIds,"-1");
        }



    }

    @Override
    public String dealMessage() {
        if(map.containsKey("ishtml")){
            isHtml = true;
        }
        if(map.containsKey(AlertEnum.EVENTIDEN.toString())){
            eventId = map.get(AlertEnum.EVENTIDEN.toString());
        }
        return null;
    }

    @Override
    public Object selectFrom(){
        log.info("监控平台emails selectFrom start! ruleId: " + ruleId);
        try{
            log.info("监控平台emails selectFrom start! ruleId: " + ruleId);
            from = mwWeixinTemplateDao.selectEmailFrom(null);
            from.setEmailToCC(emailToCC);
            log.info("监控平台emails selectFrom end! from: " + from);
        }catch (Exception e){
            log.error("监控平台emails 異常:" + e);
        }
        log.info("监控平台emails selectFrom start!");
        return from;
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        if(CollectionUtils.isNotEmpty(emailUserIds)){
            List<String> emails = mwWeixinTemplateDao.selectEmailBy(emailUserIds);
            List<String> sendEmails = emails.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
            String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
            this.sendEmails = tos;
        }else if(CollectionUtils.isNotEmpty(userIds)){
            List<String> emails = mwWeixinTemplateDao.selectEmail(userIds);
            List<String> sendEmails = emails.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
            String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
            this.sendEmails = tos;
        }else{
            String email = map.get("email");
            String[] tos = email.split(",");
            this.sendEmails = tos;
        }
        if(CollectionUtils.isNotEmpty(emailCCUserIds)){
            List<String> emailsCC = mwWeixinTemplateDao.selectEmailBy(emailCCUserIds);
            List<String> sendEmails = emailsCC.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
            String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
            this.emailToCC = tos;
        }
        log.info("监控平台用户邮箱：" + sendEmails);
        return sendEmails;
    }

    @Override
    public Object call() throws Exception {
        try{

            //2:根据系统用户id,查询接收人email
            selectAccepts(userIds);
            if(sendEmails == null || sendEmails.length==0){
                log.info("perform select accept emails:{}", sendEmails.length);
                return null;
            }

            //3:查询发送方
            selectFrom();
            if(from == null || from.getUsername().equals("")){
                log.info("perform select send emails is null");
                return null;
            }
            log.info("perform select send emails finish");
            log.info("perform select send selectFrom" + from);
            //4:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "***");

            //4发送邮件
            sendMessage(sendMessage);
            log.info("email message send finish");
            return null;
        }catch (Exception e){
            log.info("监控平台email message send appear unknown error:{}",e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
