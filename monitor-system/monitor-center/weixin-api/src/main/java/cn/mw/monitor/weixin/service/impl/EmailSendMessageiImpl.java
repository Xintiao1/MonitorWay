package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.param.ActionLevelRuleParam;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.EmailSendUtil;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.entity.HtmlTemplateEmailParam;
import cn.mw.monitor.weixin.entity.UserInfo;
import cn.mw.monitor.weixin.service.EmailService;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 邮件发送实现类
 */
public class EmailSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //接收方email
    private String[] sendEmails;

    //发送方
    private EmailFrom from = new EmailFrom();

    private String title;

    private String actionId;

    private ActionLevelRuleParam alr;

    //发送信息
    private HtmlTemplateEmailParam param;

    //邮件发送功能实现类
    private EmailService emailService;


    private EmailSendUtil emailSendUtil;

    private String ruleEmail;
    //正则表达式的模式 编译正则表达式
    private Pattern emailPattern;

    private String tempType = env.getProperty("alert.level");

    private String emailTxt = null;

    private String[] emailToCC;

    public EmailSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                 HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId,String actionId,ActionLevelRuleParam alr) {
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        if(null != map){
            this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;
        }
        this.actionId = actionId;
        this.alr = alr;
        EmailService emailService = ApplicationContextProvider.getBean(EmailService.class);
        this.emailService = emailService;
        EmailSendUtil emailSendUtil = ApplicationContextProvider.getBean(EmailSendUtil.class);
        this.emailSendUtil = emailSendUtil;
        ruleEmail = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        emailPattern = Pattern.compile(ruleEmail);
    }

    @Override
    public void sendMessage(String sendMessage) {
        log.info("emails sendMessage start!");
        log.info("邮箱参数param：" + param);
        Integer isSuccess = -1;
        String erroMessage = "";
        String title = "";
        String ip = "";
        String emailTitle ="";
        try{
            if(isCommon()) {
                String template = getMessageContext().getKey(EmailSendUtil.TEMPLATE_NAME).toString();
                title = getMessageContext().getKey(EmailSendUtil.TEMPLATE_TITLE).toString();
                Object param = getMessageContext().getKey(EmailSendUtil.TEMPLATE_PARAM);
                erroMessage = emailSendUtil.sendCommonHtmlTemplate(sendEmails, from, title, param,template);
            }else if(tempType.equals(AlertEnum.WANGKE.toString())){
                erroMessage = emailSendUtil.sendTextEmail(sendEmails, from, from.getEmailHeaderTitle(), emailTxt, false);
            } else {
                ip = map.get("IP地址");
                String alertLevel = env.getProperty("alert.level");
                if (isAlarm) {
                    String sbTitle = "";
                    if(!map.get(AlertEnum.MODELSYSTEM.toString()).contains("null")){
                        sbTitle = map.get(AlertEnum.MODELSYSTEM.toString()) + AlertAssetsEnum.UNDERLINE.toString();
                    }
                    sbTitle = sbTitle + map.get(AlertEnum.HostNameZH.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.IPAddress.toString()) + AlertAssetsEnum.UNDERLINE.toString() + param.getTitle();
                    log.info("业务系统名称获取：" + map.get(AlertEnum.MODELSYSTEM.toString()));
                    log.info("邮件标题：" + sbTitle);
                    emailTitle = StringUtils.isBlank(from.getEmailHeaderTitle()) ? sbTitle :(from.getEmailHeaderTitle() + AlertEnum.ALERTNOTICE.toString());
                    log.info("邮件标题emailTitle：" + emailTitle);
                    if(alertLevel.equals(AlertEnum.SHANYING.toString())){
                        emailTitle = "【OPEN】【" + map.get(AlertEnum.HostNameZH.toString()) + "】【" +  map.get(AlertEnum.ALERTTITLE.toString()) + "】【" + AlertEnum.UNUSUAL.toString() + "】";
                    }
                    String template = AlertEnum.ALARM.toString();
                    if(tempType.equals(AlertEnum.HUAXING.toString())){
                        template = AlertEnum.ALARM_HUAXING.toString();
                        if(alr!=null && alr.getIsActionLevel()!=null && alr.getIsActionLevel()){
                            template = AlertEnum.UPGARDE_HUAXING.toString();
                        }
                    }
                    erroMessage = emailSendUtil.sendAlertHtmlTemplate(sendEmails, from, emailTitle , param, template);
                    log.info("emails res:" + erroMessage);
                } else {
                    String sbTitle = "";
                    if(!map.get(AlertEnum.MODELSYSTEM.toString()).contains("null")){
                        sbTitle = map.get(AlertEnum.MODELSYSTEM.toString()) + AlertAssetsEnum.UNDERLINE.toString();
                    }
                    sbTitle = sbTitle + map.get(AlertEnum.HostNameZH.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.IPAddress.toString()) + AlertAssetsEnum.UNDERLINE.toString() + param.getTitle();
                    log.info("业务系统名称获取：" + map.get(AlertEnum.MODELSYSTEM.toString()));
                    log.info("邮件标题：" + sbTitle);
                    emailTitle = StringUtils.isBlank(from.getEmailHeaderTitle()) ? sbTitle :(from.getEmailHeaderTitle() + AlertEnum.RECOVERYNOTICE.toString());
                    log.info("邮件标题emailTitle：" + emailTitle);
                    if(alertLevel.equals(AlertEnum.SHANYING.toString())){
                        emailTitle = "【CLOSED】【" + map.get(AlertEnum.HostNameZH.toString()) + "】【" +  map.get(AlertEnum.RECOVERYTITLE.toString()) + "】【" +  AlertEnum.NORMAL.toString() + "】";
                    }
                    String template = AlertEnum.RESTORE.toString();
                    if(tempType.equals(AlertEnum.HUAXING.toString())){
                        template = AlertEnum.RESTORE_HUAXING.toString();
                        if(alr!=null && alr.getIsActionLevel()!=null && alr.getIsActionLevel()){
                            template = AlertEnum.UPGARDE_HUAXING.toString();
                        }
                    }
                    erroMessage = emailSendUtil.sendAlertHtmlTemplate(sendEmails, from, emailTitle, param, template);
                    log.info("emails res:" + erroMessage);
                }
            }
            isSuccess = "success".equals(erroMessage)? 0 : 1;
        }catch(Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message email:{}",e);
        }finally {
            //保存记录
            saveHis("邮件",sendMessage,isSuccess,map.get("事件ID"),erroMessage,emailTitle,ip,isAlarm, userIds,sendEmails,map.get(AlertEnum.HOSTID.toString()));
        }

    }

    @Override
    public String dealMessage() {
        if(isCommon() && null != getMessageContext()){
            Object param = getMessageContext().getKey(EmailSendUtil.TEMPLATE_PARAM);
            return param.toString();
        }

        HtmlTemplateEmailParam param = new HtmlTemplateEmailParam();
        String getUserName = null;
        if(CollectionUtils.isNotEmpty(userIds)){
            getUserName = getUserName(alr.getEmailUserIds());
        }
        StringBuffer sb = new StringBuffer();
        if(isAlarm){
            title = map.get("告警标题");
            /*param.setOption(assets.getHostName());
            param.setAddress(assets.getInBandIp());*/
            param.setContext(map.get("告警信息"));
            param.setLevel(map.get("告警等级"));
            param.setDate(map.get("告警时间"));
            param.setTitle(map.get("告警标题"));
            param.setMessage(map.get(AlertEnum.ALERTINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.PROBLEMDETAILS.toString()));
            param.setIp(map.get(AlertEnum.IPAddress.toString()));
            param.setDetail(map.get("问题详情"));
            param.setState(map.get("当前状态"));
            param.setId(map.get("事件ID"));
            param.setName(map.get("主机名称"));
            param.setDomain(map.get("区域"));
            param.setAssetsMonitor(map.get("关联模块"));
            param.setSpecifications(map.get(AlertEnum.Specifications.toString()));
            if(tempType.equals(AlertEnum.HUAXING.toString())) {
                param.setLevel(MWAlertLevelParam.actionAlertLevelMap.get(map.get(AlertEnum.ALERTLEVEL.toString())));
                param.setLongTime(SeverityUtils.CalculateTime(datePare(map.get("告警时间"))));
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
                param.setSendDate(df.format(date));
                param.setPerson(getUserName);
                param.setApplicationSystem(map.get(AlertEnum.MODELSYSTEM.toString()));
                log.info("华星邮箱业务系统参数参数：" + map.get(AlertEnum.MODELSYSTEM.toString()));
            }else if(tempType.equals(AlertEnum.WANGKE.toString())) {
                sb.append(param.getDate()).append(param.getName()).append(param.getIp()).append("【告警】" + param.getTitle()).append(param.getDetail());
                emailTxt = sb.toString();
                return emailTxt;
            }
        }else {
            title = map.get("恢复信息");
            /*param.setOption(assets.getHostName());*/
            param.setContext(map.get("恢复信息"));
            param.setDate1(map.get("故障时间"));
            param.setDate2(map.get("恢复时间"));
            param.setTitle(map.get("恢复标题"));
            param.setMessage(map.get(AlertEnum.RECOVERYINFO.toString()) + AlertAssetsEnum.UNDERLINE.toString() + map.get(AlertEnum.RECOVERYDETAILS.toString()));
            param.setLevel(map.get("恢复等级"));
            param.setIp(map.get(AlertEnum.IPAddress.toString()));
            param.setDate(map.get("故障时间"));
            param.setHfdate(map.get("恢复时间"));
            param.setDetail(map.get("恢复详情"));
            param.setState(map.get("恢复状态"));
            param.setId(map.get("事件ID"));
            param.setName(map.get("主机名称"));
            param.setDomain(map.get("区域"));
            param.setAssetsMonitor(map.get("关联模块"));
            param.setSpecifications(map.get("系统信息"));
            if(tempType.equals(AlertEnum.HUAXING.toString())) {
                param.setLevel(MWAlertLevelParam.actionAlertLevelMap.get(map.get(AlertEnum.RECOVERYLEVEL.toString())));

                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
                try {
                    long clock = df.parse(map.get(AlertEnum.RECOVERYTIME.toString())).getTime() - df.parse(map.get(AlertEnum.FAILURETIME.toString())).getTime();
                    clock = clock/1000;
                    param.setLongTime(SeverityUtils.getLastTime(clock));
                } catch (Exception e) {
                    log.error("时间转换失败:{}", e);
                }
                param.setSendDate(df.format(date));
                param.setPerson(getUserName);
                param.setApplicationSystem(map.get(AlertEnum.MODELSYSTEM.toString()));
                log.info("华星邮箱业务系统参数参数：" + map.get(AlertEnum.MODELSYSTEM.toString()));
            }else if(tempType.equals(AlertEnum.WANGKE.toString())) {
                sb.append(param.getHfdate()).append(param.getName()).append(param.getIp()).append("【恢复】" + param.getTitle()).append(param.getDetail());
                emailTxt = sb.toString();
                return emailTxt;
            }
        }
        if(map.get("系统信息")==null || map.get("系统信息").equals("")){
            param.setSystemHidden("display:none");
        }
        this.param = param;
        return param.toString();
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

    @Override
    public Object selectFrom(){
        log.info("emails selectFrom start! ruleId: " + ruleId);
        try{
            log.info("emails selectFrom start! ruleId: " + ruleId);
            from = mwWeixinTemplateDao.selectEmailFrom(ruleId);
            log.info("emails selectFrom end! ruleId: " + ruleId);
            from.setEmailToCC(emailToCC);
        }catch (Exception e){
            log.error("emails 異常:" + e);
        }
        log.info("emails selectFrom start!");
        return from;
    }

    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        log.info("邮件接收人star");
        List<String> emails = new ArrayList<>();
        List<String> emailsCC = new ArrayList<>();
        if(alr != null && alr.getActionId() != null && alr.getActionId() != "" && alr.getLevel() != null){
            log.info("邮件接收人 alr :" + alr);
            ActionLevelRuleParam temp = mwWeixinTemplateDao.selectLevelRuleEmailMapper(alr);
            if(temp != null){
                if(temp.getIsAllUser() == null || temp.getIsAllUser() == 1){
                    emails = mwWeixinTemplateDao.selectEmail(userIds);
                }else{
                    emails.add(temp.getEmail());
                }
            }
        }else {
            log.info("邮件接收人selectPopupAction:" + actionId);
            AddAndUpdateAlertActionParam param = mwWeixinTemplateDao.selectPopupAction(actionId);
            log.info("IsAllUser:" + param.getIsAllUser());
            if(param.getIsAllUser() == 2){
                String[] s = param.getEmail().split(";");
                emails.addAll(Arrays.asList(s));
            }else{
                log.info("查询邮箱人:" + alr.getEmailUserIds());
                emails = mwWeixinTemplateDao.selectEmailBy(alr.getEmailUserIds());
            }
            if(alr != null){
                HashSet<Integer> emailCCUserIds = new HashSet<>();
                if(CollectionUtils.isNotEmpty(alr.getGroupUserIds())) emailCCUserIds.addAll(alr.getGroupUserIds());
                if(CollectionUtils.isNotEmpty(alr.getEmailCC())) emailCCUserIds.addAll(alr.getEmailCC());
                if(CollectionUtils.isNotEmpty(alr.getOrgUserIds())) emailCCUserIds.addAll(alr.getOrgUserIds());
                if(CollectionUtils.isNotEmpty(emailCCUserIds)){
                    emailsCC = emailPattern(mwWeixinTemplateDao.selectEmail(emailCCUserIds));
                }
            }
        }

        List<String> sendEmails = emailPattern(emails);
        String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
        emailToCC = emailsCC.toArray(new String[emailsCC.size()]);
        this.sendEmails = tos;
        log.info("用户邮箱:" + sendEmails);
        log.info("用户邮箱alr.setEmailToCC:" + emailsCC);
        return tos;
    }

    private String getUserName(List<Integer> userIds){
        StringBuffer sb = new StringBuffer();
        if(CollectionUtils.isNotEmpty(userIds)){
            List<UserInfo> userName = mwWeixinTemplateDao.selectUserNameBy(userIds);
            for(UserInfo temp : userName){
                sb.append(temp.getUserName()).append("-").append(temp.getPhoneNumber()).append(",");
            }
            return sb.toString().substring(0,sb.length()-1);
        }
        return sb.toString();
    }

    public List<String> emailPattern(List<String> email) {
        List<String> result = new ArrayList();
        if(CollectionUtils.isNotEmpty(email)){
            for(String mail : email){
                if(StringUtils.isEmpty(mail)){
                    continue;
                }
                Matcher m = emailPattern.matcher(mail);
                if(!m.matches()){
                    log.warn("error mail:" + mail);
                    continue;
                }
                result.add(mail);
            }
        }
        return result;

    }

    @Override
    public Object call() throws Exception {
        try{
            //1判断级别是否符合
            if(!isCommon() && !outPut()){
                return null;
            }
            log.info("the alert information level is satisfied");

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
            log.error("email message send appear unknown error:{}",e);
            throw new Exception(e);
        }
    }
}
