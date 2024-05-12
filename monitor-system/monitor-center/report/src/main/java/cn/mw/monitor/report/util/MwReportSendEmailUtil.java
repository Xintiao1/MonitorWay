package cn.mw.monitor.report.util;

import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.entity.CustomJavaMailSenderImpl;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;

/**
 * @author gengjb
 * @description 发送邮件工具类
 * @date 2024/1/29 14:06
 */
@Slf4j
public class MwReportSendEmailUtil {

    /**
     * 进行邮件发送操作
     * @param to 接收邮件邮箱
     * @param subject 报表名称
     * @param emailFrom 发送方信息
     * @param pathNames 附件所在路径
     * @return
     */
    public static String sendReportEmail(String[] to, String subject, EmailFrom emailFrom, List<String> pathNames,CustomJavaMailSenderImpl customJavaMailSender){
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
            System.getProperties().setProperty("mail.mime.splitlongparameters","false");
            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText("");
            if(CollectionUtils.isNotEmpty(pathNames)){
                for (String pathName : pathNames) {
                    FileSystemResource file = new FileSystemResource(new File(pathName));
                    String fileName = file.getFilename();
                    //添加附件，可多次调用该方法添加多个附件
                    mimeMessageHelper.addAttachment(fileName, file);
                }
            }
            customJavaMailSender.send(mimeMessage);
            return "成功";
        }catch (Exception e){
            log.info("报表邮件发送（附件邮件）失败:{}",e.getMessage());
            if(e instanceof MailSendException){
                Exception[] mailExceptions = ((MailSendException) e).getMessageExceptions();
                for (Exception mailException : mailExceptions) {
                    if(mailException instanceof SendFailedException){
                        Address[] invaliAddress = ((SendFailedException) mailException).getInvalidAddresses();
                        for (Address address : invaliAddress) {
                            to = remove(to, address.toString());
                        }
                        if(to.length > 0){
                            return sendReportEmail(to,subject,emailFrom,pathNames,customJavaMailSender);
                        }
                    }

                }
            }
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

    public static void decrypt(EmailFrom  email) {
        if(email !=null){
            try {
                email.setPassword(EncryptsUtil.decrypt(email.getPassword()));
            } catch (Exception e) {
                log.error("报表邮件发送（附件邮件）失败:{}",e.getMessage());
            }
        }
    }
}
