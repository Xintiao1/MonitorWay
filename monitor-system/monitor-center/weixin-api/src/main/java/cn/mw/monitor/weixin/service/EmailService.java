package cn.mw.monitor.weixin.service;

import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.monitor.user.dao.MwUserOrgMapperDao;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.entity.CustomJavaMailSenderImpl;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.weixin.entity.HtmlTemplateEmailParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;


/**
 * @author bkc
 * @create 2020-08-14 11:21
 */
@Service
@Transactional
@Slf4j
public class EmailService {
    @Value("${file.url}")
    private String imgUrl;
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private CustomJavaMailSenderImpl customJavaMailSender;

    @Autowired
    private MwUserOrgMapperDao mwUserOrgMapperDao;
    //报表邮件发送（附件邮件）
    public String sendReportEmail(String to, String subject, EmailFrom emailFrom, String pathName){
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
            log.info("报表邮件发送（附件邮件）失败:{}",e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * html模板邮件
     * @param to 邮件接收者
     * @param emailFrom 邮件发送者信息
     * @param subject 邮件标题
     * @param emailParam 邮件内容
     *
     */
    public String sendAlertHtmlTemplate(String[] to, EmailFrom emailFrom,String subject, HtmlTemplateEmailParam emailParam)  {
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
            String template = "alarm";
            String dirroot = System.getProperty("user.dir");
            //String imgPath = dirroot + "\\monitor-system\\monitor-center\\weixin-api\\target\\classes\\static\\logo.svg";
            log.info("告警邮件user.dir dirroot：" + dirroot);
            log.info("告警邮件user.home dirroot：" + System.getProperty("user.home"));
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
            //System.err.println("emailText"+emailText);
            mimeMessageHelper.setText(emailText, true);
           //添加图片
            SettingDTO logoDto = mwUserOrgMapperDao.selectSettingsInfo();
            if(logoDto != null && !logoDto.getLogoUrl().equals("")){
                String imgPath = logoDto.getHttpHeader()  + "//file-upload//";
                imgPath = imgPath + logoDto.getLogoUrl();
                log.info("告警邮件imgPath：" + imgPath);
                FileSystemResource logoImage = new FileSystemResource(new File(imgPath));
                log.info("告警邮件logoImage：" + logoImage);
                mimeMessageHelper.addInline("logoImage", logoImage);
            }
            customJavaMailSender.send(mimeMessage);
            return "success";
        }catch (Exception e){
            log.info("告警邮件发送失败:{}",e.getMessage());
            return e.getMessage();
        }
    }

    public String sendRestoreHtmlTemplate(String[] to, EmailFrom emailFrom,String subject, HtmlTemplateEmailParam emailParam)  {
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
            String template = "restore";

            String dirroot = System.getProperty("user.dir");
            //String imgPath = dirroot + "\\monitor-system\\monitor-center\\weixin-api\\target\\classes\\static\\logo.svg";


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
            mimeMessageHelper.setText(emailText, true);


            //添加图片
            SettingDTO logoDto = mwUserOrgMapperDao.selectSettingsInfo();
            if(logoDto != null && !logoDto.getLogoUrl().equals("")){
                String imgPath = logoDto.getHttpHeader()  + "//file-upload//";
                imgPath = imgPath + logoDto.getLogoUrl();
                log.info("告警邮件imgPath：" + imgPath);
                FileSystemResource logoImage = new FileSystemResource(new File(imgPath));
                log.info("告警邮件logoImage：" + logoImage);
                mimeMessageHelper.addInline("logoImage", logoImage);
            }
            customJavaMailSender.send(mimeMessage);

            return "success";
        }catch (Exception e){
            log.info("告警恢复邮件发送失败:{}",e.getMessage());
            return e.getMessage();
        }
    }

    public  void decrypt(EmailFrom  email) {
        if(email !=null){
            try {
                email.setPassword(EncryptsUtil.decrypt(email.getPassword()));
            } catch (Exception e) {
                log.error("解密失败:{}",e.getMessage());
            }
        }
    }


    /*@Async
    @GetMapping(value="email/string")
    public void sendSimpleMail(*//*String to, String subject, String content*//*) {
        String to = "997449455@qq.com";
        String subject = "MyTestEmail";
        String content = "你好啊！bbbbbbb" ;
        SimpleMailMessage message = new SimpleMailMessage();
        //收信人
        message.setTo(to);
        //主题
        message.setSubject(subject);
        //内容
        message.setText(content);
        //发信人
        message.setFrom(from);
        mailSender.send(message);
    }

    @Async
    public void sendHtmlMail(String to, String subject, String content) {
        //log.info("发送HTML邮件开始：{},{},{}", to, subject, content);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            //true代表支持html
            helper.setText(content, true);
            mailSender.send(message);
            //log.info("发送HTML邮件成功");
        } catch (MessagingException e) {
            //log.error("发送HTML邮件失败：", e);
        }
    }

    @Async
    public void sendAttachmentMail(String to, String subject, String content, String filePath) {
        //log.info("发送带附件邮件开始：{},{},{},{}", to, subject, content, filePath);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = file.getFilename();
            //添加附件，可多次调用该方法添加多个附件
            helper.addAttachment(fileName, file);
            mailSender.send(message);
//            log.info("发送带附件邮件成功");
        } catch (MessagingException e) {
//            log.error("发送带附件邮件失败", e);
        }
    }

    @Async
    public void sendInlineResourceMail(String to, String subject, String content, String rscPath, String rscId) {
//        log.info("发送带图片邮件开始：{},{},{},{},{}", to, subject, content, rscPath, rscId);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            FileSystemResource res = new FileSystemResource(new File(rscPath));
            //重复使用添加多个图片
            helper.addInline(rscId, res);
            mailSender.send(message);
//            log.info("发送带图片邮件成功");
        } catch (MessagingException e) {
//            log.error("发送带图片邮件失败", e);
        }
    }

    *//**
     * html模板邮件
     * @param from 发件人
     * @param to 收件人
     * @param subject 邮件主题
     * @param emailParam 给模板的参数
     * @param template html模板路径(相对路径)  Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
     * @param imgPath 图片/文件路径(绝对路径)
     * @throws MessagingException
     *//*
    @GetMapping(value="email/mode")
    public void thymeleafEmail(*//*String from, String[] to, String subject, EmailParam emailParam,
                               String template, String imgPath*//*) throws MessagingException {
        String from = wangxJavaMailSender.getUsername();//this.from;
        String[] to = new String[]{"18325533640@163.com"};
        String subject = "这是一封测试邮件";
        EmailParam  emailParam = new EmailParam();
        emailParam.setOption("quatest");
        emailParam.setAddress("10.0.0.0");
        emailParam.setContext("服务器异常");
        emailParam.setLevel("warm");
        emailParam.setDate("2019-9-99");
        String template = "email/template";
        String imgPath = "C:\\Users\\86183\\Desktop\\logoImage.jpg";


        MimeMessage mimeMessage =mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(from);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        // 利用 Thymeleaf 模板构建 html 文本
        Context ctx = new Context();
        // 给模板的参数的上下文
        ctx.setVariable("emailParam", emailParam);
        // 执行模板引擎，执行模板引擎需要传入模板名、上下文对象
        // Thymeleaf的默认配置期望所有HTML文件都放在 **resources/templates ** 目录下，以.html扩展名结尾。
        // String emailText = templateEngine.process("email/templates", ctx);
        String emailText = templateEngine.process(template, ctx);
        mimeMessageHelper.setText(emailText, true);
        // FileSystemResource logoImage= new FileSystemResource("D:\\image\\logo.jpg");
        //绝对路径
        FileSystemResource logoImage = new FileSystemResource(imgPath);
        //相对路径，项目的resources路径下
        //ClassPathResource logoImage = new ClassPathResource("static/image/logonew.png");
        // 添加附件,第一个参数表示添加到 Email 中附件的名称，第二个参数是图片资源
        //一般图片调用这个方法
        mimeMessageHelper.addInline("logoImage", logoImage);
        //一般文件附件调用这个方法
        //mimeMessageHelper.addAttachment("logoImage", resource);
        mailSender.send(mimeMessage);

    }*/

}
