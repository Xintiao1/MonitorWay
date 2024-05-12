package cn.mw.monitor.util;

import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.monitor.util.dao.MwUserMapperDao;
import cn.mw.monitor.util.entity.CustomJavaMailSenderImpl;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.entity.HtmlTemplateEmailParam;
import cn.mw.monitor.util.service.MwEmailManageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;


@Component
public class EmailSendUtil {
    private static final Logger log = LoggerFactory.getLogger("EmailSendUtil");

    public static final String TEMPLATE_NAME = "TEMPLATE_NAME";
    public static final String TEMPLATE_TITLE = "TEMPLATE_TITLE";

    public static final String TEMPLATE_PARAM = "TEMPLATE_PARAM";

    @Autowired
    private CustomJavaMailSenderImpl customJavaMailSender;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private MwUserMapperDao mwUserMapperDao;
    @Autowired
    private MwEmailManageService mwEmailManage;


    public String sendAlertHtmlTemplate(String[] to, String ruleId, String subject, HtmlTemplateEmailParam emailParam,String template)  {
        EmailFrom emailFrom = new EmailFrom();
        if(ruleId == null){
            //调用默认的邮件 系统默认邮件规则
            emailFrom = mwEmailManage.selectEmailFromByName("系统默认邮件规则");
        }else{
            //查询EmailFrom 调用sendAlertHtmlTemplate();
            emailFrom = mwEmailManage.selectEmailFrom(ruleId);
        }
        return sendAlertHtmlTemplate(to, emailFrom, subject, emailParam, template);
    }

    /**
     * html模板邮件
     * @param to 邮件接收者
     * @param emailFrom 邮件发送者信息
     * @param subject 邮件标题
     * @param emailParam 邮件内容
     *
     */
    public String sendAlertHtmlTemplate(String[] to, EmailFrom emailFrom, String subject, HtmlTemplateEmailParam emailParam,String template)  {
        try{
            decrypt(emailFrom);
            String userName = emailFrom.getUsername();
            if(emailFrom.getIsDelsuffix()!=null && emailFrom.getIsDelsuffix()){
                userName = userName.substring(0,userName.lastIndexOf("@"));
            }
            log.info("邮件发送参数测试::"+emailFrom);
            customJavaMailSender.setUsernameFirst(userName);
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());
            /*//outlook邮箱smtp starttls加密
            Properties properties = customJavaMailSender.getJavaMailProperties();
            properties.put("mail.smtp.starttls.enable",true);
            customJavaMailSender.setJavaMailProperties(properties);*/
            InternetAddress from = new InternetAddress();
            from.setAddress(emailFrom.getUsername());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");
            String dirroot = System.getProperty("user.dir");
            //String imgPath = dirroot + "\\monitor-system\\monitor-center\\weixin-api\\target\\classes\\static\\logo.svg";
            log.info("告警邮件user.dir dirroot：" + dirroot);
            log.info("告警邮件user.home dirroot：" + System.getProperty("user.home"));
            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            if(emailFrom.getEmailToCC() != null){
                mimeMessageHelper.setCc(emailFrom.getEmailToCC());
            }
            mimeMessageHelper.setSubject(subject);
            //添加图片
            if(emailFrom.getIsLogo() == null || emailFrom.getIsLogo() ){
                String imgPath = null;
                if(emailFrom.getLogo() == null || emailFrom.getLogo().equals("")){
                    SettingDTO logoDto = mwUserMapperDao.selectSettingsInfo();
                    if(logoDto != null && !logoDto.getLogoUrl().equals("")){
                        imgPath = logoDto.getHttpHeader()  + "//file-upload//";
                        imgPath = imgPath + logoDto.getLogoUrl();
                    }
                }else{
                    imgPath = emailFrom.getUrl() + emailFrom.getLogo();
                }
                if(imgPath != null && !imgPath.equals("")){
                    log.info("告警邮件imgPath：" + imgPath);
                    FileSystemResource logoImage = new FileSystemResource(new File(imgPath));
                    log.info("告警邮件logoImage：" + logoImage);
                    mimeMessageHelper.addInline("logoImage", logoImage);
                }else{
                    emailParam.setHidden("display:none");
                }
            }else{
                emailParam.setHidden("display:none");
            }
            // 利用Thymeleaf 模板构建 html文本
            Context ctx = new Context();
            ctx.setVariable("emailParam", emailParam);
            // 执行模板引擎，执行模板引擎需要传入模板名、上下文对象
            // Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
            String emailText = templateEngine.process(template, ctx);
            log.info("告警邮件emailParam：" + emailParam);
            //log.info("告警邮件模板样式：" + emailText);
            //System.err.println("emailText"+emailText);
            mimeMessageHelper.setText(emailText, true);
            customJavaMailSender.send(mimeMessage);
            return "success";
        }catch (Exception e){
            log.error("告警邮件发送失败:",e);
            log.info("处理无效邮箱地址前：" + Arrays.toString(to));
            if(e instanceof MailSendException){
                Exception[] mailExceptions = ((MailSendException) e).getMessageExceptions();
                for (Exception mailException : mailExceptions) {
                    if(mailException instanceof SendFailedException){
                        Address[] invaliAddress = ((SendFailedException) mailException).getInvalidAddresses();
                        log.info("无效地址2：" + Arrays.toString(invaliAddress));
                        for (Address address : invaliAddress) {
                            to = remove(to, address.toString());
                        }
                        log.info("处理无效邮箱地址后：" + Arrays.toString(to));
                        if(to.length > 0){
                            return sendAlertHtmlTemplate(to, emailFrom, subject, emailParam, template);
                        }
                    }

                }
            }
            return e.getMessage();
        }
    }

    public String sendCommonHtmlTemplate(String[] to, EmailFrom emailFrom, String subject, Object emailParam,String template)  {
        try{
            decrypt(emailFrom);
            String userName = emailFrom.getUsername();
            if(emailFrom.getIsDelsuffix()!=null && emailFrom.getIsDelsuffix()){
                userName = userName.substring(0,userName.lastIndexOf("@"));
            }
            customJavaMailSender.setUsernameFirst(userName);
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());
            /*//outlook邮箱smtp starttls加密
            Properties properties = customJavaMailSender.getJavaMailProperties();
            properties.put("mail.smtp.starttls.enable",true);
            customJavaMailSender.setJavaMailProperties(properties);*/
            InternetAddress from = new InternetAddress();
            from.setAddress(emailFrom.getUsername());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");
            String dirroot = System.getProperty("user.dir");
            //String imgPath = dirroot + "\\monitor-system\\monitor-center\\weixin-api\\target\\classes\\static\\logo.svg";
            log.info("邮件user.dir dirroot：" + dirroot);
            log.info("邮件user.home dirroot：" + System.getProperty("user.home"));
            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);

            // 利用Thymeleaf 模板构建 html文本
            Context ctx = new Context();
            ctx.setVariable("emailParam", emailParam);
            // 执行模板引擎，执行模板引擎需要传入模板名、上下文对象
            // Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
            String emailText = templateEngine.process(template, ctx);
            log.info("告警邮件emailParam：" + emailParam);
            //log.info("告警邮件模板样式：" + emailText);
            //System.err.println("emailText"+emailText);
            mimeMessageHelper.setText(emailText, true);
            customJavaMailSender.send(mimeMessage);
            return "success";
        }catch (Exception e){
            log.error("邮件发送失败:",e);
            log.info("处理无效邮箱地址前：" + Arrays.toString(to));
            return e.getMessage();
        }
    }

    private static String[] remove(String[] arr, String removeElement) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(removeElement)) {
                count++;
            }
        }
        String[] newArr = new String[arr.length - count];
        int index = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].equals(removeElement)) {
                newArr[index] = arr[i];
                index++;
            }
        }
        return newArr;
    }



    public String sendRestoreHtmlTemplate(String[] to, EmailFrom emailFrom,String subject, HtmlTemplateEmailParam emailParam)  {
        try{
            log.info("恢复告警邮件开始");
            decrypt(emailFrom);
            customJavaMailSender.setUsernameFirst(emailFrom.getUsername());
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());
            log.info("恢复告警邮件emailFrom：" + emailFrom.getUsername());
            InternetAddress from = new InternetAddress();
            from.setAddress(emailFrom.getUsername());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");
            String template = "restore";
            log.info("恢复告警邮件from：" + from);
            String dirroot = System.getProperty("user.dir");
            //String imgPath = dirroot + "\\monitor-system\\monitor-center\\weixin-api\\target\\classes\\static\\logo.svg";


            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);

            //添加图片
            if(emailFrom.getIsLogo() || emailFrom.getIsLogo() == null){
                String imgPath = null;
                if(emailFrom.getLogo() == null || emailFrom.getLogo().equals("")){
                    SettingDTO logoDto = mwUserMapperDao.selectSettingsInfo();
                    if(logoDto != null && !logoDto.getLogoUrl().equals("")){
                        imgPath = logoDto.getHttpHeader()  + "//file-upload//";
                        imgPath = imgPath + logoDto.getLogoUrl();
                    }
                }else{
                    imgPath = emailFrom.getUrl() + emailFrom.getLogo();
                }
                if(imgPath != null && !imgPath.equals("")){
                    log.info("告警邮件imgPath：" + imgPath);
                    FileSystemResource logoImage = new FileSystemResource(new File(imgPath));
                    log.info("告警邮件logoImage：" + logoImage);
                    mimeMessageHelper.addInline("logoImage", logoImage);
                }else{
                    emailParam.setHidden("display:none");
                }
            }else{
                emailParam.setHidden("display:none");
            }
            // 利用Thymeleaf 模板构建 html文本
            Context ctx = new Context();
            ctx.setVariable("emailParam", emailParam);
            // 执行模板引擎，执行模板引擎需要传入模板名、上下文对象
            // Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
            String emailText = templateEngine.process(template, ctx);
            log.info("告警邮件emailParam：" + emailParam);
            //log.info("告警邮件模板样式：" + emailText);
            mimeMessageHelper.setText(emailText, true);
            customJavaMailSender.send(mimeMessage);

            return "发送成功！";
        }catch (Exception e){
            log.error("告警恢复邮件发送失败:",e);
            return "告警恢复邮件发送失败:{}"+e.getMessage();
        }
    }
    public  void decrypt(EmailFrom email) {
        if(email !=null){
            try {
                email.setPassword(EncryptsUtil.decrypt(email.getPassword()));
            } catch (Exception e) {
                log.error("解密失败:",e);
            }
        }
    }

    public String sendReportEmail(String to,String subject,EmailFrom emailFrom,String pathName){
        try{
            decrypt(emailFrom);
            log.info("邮件发送参数::"+emailFrom);
            String userName = emailFrom.getUsername();
            if(emailFrom.getIsDelsuffix()!=null && emailFrom.getIsDelsuffix()){
                userName = userName.substring(0,userName.lastIndexOf("@"));
            }
            customJavaMailSender.setUsernameFirst(userName);
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());

            InternetAddress from = new InternetAddress();
            from.setAddress(emailFrom.getUsername());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");

            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText("");
            FileSystemResource file = new FileSystemResource(new File(pathName));
            String fileName = file.getFilename();
            //添加附件，可多次调用该方法添加多个附件
            mimeMessageHelper.addAttachment(fileName, file);
            customJavaMailSender.send(mimeMessage);
            return "成功";
        }catch (Exception e){
            log.error("报表邮件发送（附件邮件）失败:",e);
            return e.getMessage();
        }
    }

    public String sendHtmlTemplate(String[] to, EmailFrom emailFrom, HtmlTemplateEmailParam emailParam,String template)  {
        try{
            decrypt(emailFrom);

            customJavaMailSender.setUsernameFirst(emailFrom.getUsername());
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());

            InternetAddress from = new InternetAddress();
            from.setAddress(customJavaMailSender.getUsernameFirst());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");

            String dirroot = System.getProperty("user.dir");
            //String imgPath = dirroot + "\\monitor-system\\monitor-center\\weixin-api\\target\\classes\\static\\logo.svg";


            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(emailFrom.getEmailHeaderTitle());

            // 利用Thymeleaf 模板构建 html文本
            Context ctx = new Context();
            ctx.setVariable("emailParam", emailParam);
            // 执行模板引擎，执行模板引擎需要传入模板名、上下文对象
            // Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
            String emailText = templateEngine.process(template, ctx);
            log.info("告警邮件emailParam：" + emailParam);
            //log.info("告警邮件模板样式：" + emailText);
            mimeMessageHelper.setText(emailText, true);
            //添加图片
            String imgPath = null;
            if(emailFrom.getLogo() == null || emailFrom.getLogo().equals("")){
                SettingDTO logoDto = mwUserMapperDao.selectSettingsInfo();
                if(logoDto != null && !logoDto.getLogoUrl().equals("")){
                    imgPath = logoDto.getHttpHeader()  + "//file-upload//";
                    imgPath = imgPath + logoDto.getLogoUrl();
                }
            }else{
                imgPath = emailFrom.getUrl() + emailFrom.getLogo();
            }
            if(imgPath != null && !imgPath.equals("")){
                log.info("告警邮件imgPath：" + imgPath);
                FileSystemResource logoImage = new FileSystemResource(new File(imgPath));
                log.info("告警邮件logoImage：" + logoImage);
                mimeMessageHelper.addInline("logoImage", logoImage);
            }else{
                emailParam.setHidden("display:none");
            }
            customJavaMailSender.send(mimeMessage);

            return "success";
        }catch (Exception e){
            log.error("告警恢复邮件发送失败:",e);
            return e.getMessage();
        }
    }

    public String sendTextEmail(String[] to, EmailFrom emailFrom, String title, String text,Boolean isHtml)  {
        try{
            decrypt(emailFrom);
            String userName = emailFrom.getUsername();
            if(emailFrom.getIsDelsuffix()!=null && emailFrom.getIsDelsuffix()){
                userName = userName.substring(0,userName.lastIndexOf("@"));
            }
            customJavaMailSender.setUsernameFirst(userName);
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());

            InternetAddress from = new InternetAddress();
            from.setAddress(emailFrom.getUsername());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");

            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setText(text,isHtml);
            if(emailFrom.getEmailToCC() != null){
                mimeMessageHelper.setCc(emailFrom.getEmailToCC());
            }
            customJavaMailSender.send(mimeMessage);

            return "success";
        }catch (Exception e){
            log.error("告警邮件发送失败:",e);
            log.info("处理无效邮箱地址前：" + Arrays.toString(to));
            if(e instanceof MailSendException){
                Exception[] mailExceptions = ((MailSendException) e).getMessageExceptions();
                for (Exception mailException : mailExceptions) {
                    if(mailException instanceof SendFailedException){
                        Address[] invaliAddress = ((SendFailedException) mailException).getInvalidAddresses();
                        log.info("无效地址2：" + Arrays.toString(invaliAddress));
                        for (Address address : invaliAddress) {
                            to = remove(to, address.toString());
                        }
                        log.info("处理无效邮箱地址后：" + Arrays.toString(to));
                        if(to.length > 0){
                            return sendTextEmail(to, emailFrom, title, text, isHtml);
                        }
                    }

                }
            }
            return e.getMessage();
        }
    }


}
