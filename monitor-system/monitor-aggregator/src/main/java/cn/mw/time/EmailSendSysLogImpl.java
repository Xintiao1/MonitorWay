package cn.mw.time;

import cn.mw.module.security.dto.EsSysLogTagDTO;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.EmailSendUtil;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.entity.HtmlTemplateEmailParam;
import cn.mw.monitor.weixin.service.EmailService;
import cn.mw.monitor.weixin.service.SendMessageBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 邮件发送实现类
 */
public class EmailSendSysLogImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方email
    private String[] sendEmails;

    //发送方
    private EmailFrom from = new EmailFrom();

    private String title;

    //发送信息
    private HtmlTemplateEmailParam param;
    private Map<String, Object> map;
    private EmailSendUtil emailSendUtil;

    public EmailSendSysLogImpl(Map<String, Object> map, HashSet<Integer> userIds, String ruleId) {
        this.map = map;
        this.userIds = userIds;
        this.ruleId = ruleId;

        EmailSendUtil emailSendUtil = ApplicationContextProvider.getBean(EmailSendUtil.class);
        this.emailSendUtil = emailSendUtil;
    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("emails sendMessage start!");
        String res = "";
        Integer isSuccess = -1;
        String erroMessage = "";
        try{
            from.setEmailHeaderTitle(map.get("host").toString());
            res = emailSendUtil.sendAlertHtmlTemplate(sendEmails, from,from.getEmailHeaderTitle(),  param,"eslog");
            isSuccess = "success".equals(res)? 0 : 1;
            log.info("邮件发送系统日志结果返回：" + res);
        }catch(Exception e){
            erroMessage = e.getMessage();
            log.info("error perform send message 邮件发送系统:{}",e.getMessage());
        }



    }

    @Override
    public String dealMessage() {
        HtmlTemplateEmailParam param = new HtmlTemplateEmailParam();
        param.setHost(map.get("host").toString());
        param.setHostName(map.get("hostName").toString());
        param.setSeverity_label(map.get("severity_label").toString());
        param.setFacility_label(map.get("facility_label").toString());
        param.setTimestamp(map.get("@timestamp").toString());
        param.setDataSourceName(map.get("dataSourceName").toString());
        param.setMessage(map.get("message").toString());
        List<EsSysLogTagDTO> tagDTOList = (List<EsSysLogTagDTO>) map.get("tagList");
        List<String> tagNames = new ArrayList<>();
        for(EsSysLogTagDTO s : tagDTOList){
            tagNames.add(s.getTagName());
        }
        param.setTagName(tagNames.toString());
        this.param = param;
        return param.toString();
    }

    @Override
    public Object selectFrom(){
        log.info("emails selectFrom start! ruleId: " + ruleId);
        try{
            log.info("emails selectFrom start! ruleId: " + ruleId);
            from = mwWeixinTemplateDao.selectEmailFrom(ruleId);
            log.info("emails selectFrom end! ruleId: " + ruleId);
        }catch (Exception e){
            log.error("emails 異常:" + e);
        }
        log.info("emails selectFrom start!");
        return from;
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        List<String> emails = mwWeixinTemplateDao.selectEmail(userIds);
        List<String> sendEmails = emails.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
        String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
        this.sendEmails = tos;
        log.info("用户邮箱：" + sendEmails);
        return tos;
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
            log.info("emails:{} selectAccepts" + Arrays.toString(sendEmails));
            log.info("perform select accept emails:{}", sendEmails.length);

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
            log.info("email message send appear unknown error:{}",e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
