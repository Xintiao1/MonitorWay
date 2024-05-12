/*
package cn.mw.monitor.weixin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

*/
/**
 * 实现多账号，轮询发送----->实现指定发送人
 *//*

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class CustomJavaMailSenderImpl extends JavaMailSenderImpl implements JavaMailSender {

    //设置一个默认发件人邮箱
    private String usernameFirst;
    private String passwordFirst;
    private String hostFirst;
    private int portFirst;
    private Boolean isSSL;


    private final MailProperties properties;

    public MailProperties getProperties() {
        return properties;
    }


    */
/*private UserService userService;
    //构造器注入Spring对象
    @Autowired
    public CustomJavaMailSenderImpl(MailProperties properties,UserService userService) {
        this.properties = properties;
        this.userService = userService;
        List<UserEntity> lists = userService.getList();
        // 初始化账号
        if (usernameList == null){
            usernameList = new ArrayList<String>();
            usernameList.add(lists.get(0).getOpenid());
            usernameList.add(lists.get(1).getOpenid());
        }
        // 初始化密码
        if (passwordList == null){
            passwordList = new ArrayList<String>();
            passwordList.add(lists.get(0).getNickname());
            passwordList.add(lists.get(1).getNickname());
        }
    }*//*


    @Autowired
    public CustomJavaMailSenderImpl(MailProperties properties) {
        this.properties = properties;

        // 初始化账号
        if (usernameFirst == null){
            usernameFirst = "1q.com";
        }
        // 初始化密码passwordFirst
        if (passwordFirst == null){
            passwordFirst = "abc";
        }
    }

    @Override
    protected void doSend(MimeMessage[] mimeMessage, Object[] object) throws MailException {

        super.setUsername(usernameFirst);
        super.setPassword(passwordFirst);

        // 设置编码和各种参数
        super.setHost(hostFirst);
        super.setPort(portFirst);
        super.setDefaultEncoding(this.properties.getDefaultEncoding().name());
        if(isSSL){
            super.setJavaMailProperties(asProperties(this.properties.getProperties()));
        }
        super.doSend(mimeMessage, object);
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }

    @Override
    public String getUsername() {
        return usernameFirst;
    }

    public String getUsernameFirst() {
        return usernameFirst;
    }

    public void setUsernameFirst(String usernameFirst) {
        this.usernameFirst = usernameFirst;
    }

    public String getPasswordFirst() {
        return passwordFirst;
    }

    public void setPasswordFirst(String passwordFirst) {
        this.passwordFirst = passwordFirst;
    }

    public String getHostFirst() {
        return hostFirst;
    }

    public void setHostFirst(String hostFirst) {
        this.hostFirst = hostFirst;
    }

    public int getPortFirst() {
        return portFirst;
    }

    public void setPortFirst(int portFirst) {
        this.portFirst = portFirst;
    }

    public Boolean getSSL() {
        return isSSL;
    }

    public void setSSL(Boolean SSL) {
        isSSL = SSL;
    }
}
*/
