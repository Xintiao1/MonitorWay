package cn.mw.monitor.shiro;

import cn.mw.monitor.api.param.aduser.ADAuthenticParam;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.user.common.ADUtils;
import cn.mw.monitor.user.common.UsernameToken;
import cn.mw.monitor.user.dao.MWADUserDao;
import cn.mw.monitor.user.model.ADConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

@Component
@Slf4j
public class MWdCredentialsMatcher implements CredentialsMatcher {

    @Autowired
    PasswordManage passwordManage;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Resource
    private MWADUserDao mwadUserDao;

    //重置登录次数
     final String resetNum = "0";

    @Override
    public boolean doCredentialsMatch(AuthenticationToken authenticationToken, AuthenticationInfo authenticationInfo){
        boolean match = false;
       /* boolean result = authenticationToken instanceof UsernameToken;*/
        //mw-login  ldap-login
        CustomUserNamePasswdToken customToken = (CustomUserNamePasswdToken)authenticationToken;
        //微信 免密登录
        if(customToken.getUnpass()){

                String key = MWAuthorizingRealm.SHIRO_IS_LOCK+customToken.getUsername();
                redisTemplate.delete(key);
                return true;

        }

        if (LoginType.Common.getType().equals(customToken.getLoginType())) {
            log.info("zy--  mw login match rule at present ");

            SimpleAuthenticationInfo authInfo = (SimpleAuthenticationInfo)authenticationInfo;

            PrincipalCollection principalCollection = authenticationInfo.getPrincipals();
            UserDTO userdto = (UserDTO)principalCollection.getPrimaryPrincipal();
            String passwd = authInfo.getCredentials().toString();
            log.info("zy--  this is cryptographic db passwd "+passwd);

            //界面输入密码信息
            String loginName = customToken.getUsername();
            String loginPassword = String.valueOf(customToken.getPassword());
            log.info("zy--  this is cryptographic login passwd "+passwd);
            String hashTypeId = userdto.getHashTypeId();
            String salt = userdto.getSalt();

            Credential credential = new Credential(loginName, loginPassword, salt, hashTypeId);
            loginPassword = passwordManage.encryptPassword(credential);

            match = loginPassword.equals(passwd);
            log.info("zy--  db passwd equals login passwd "+match);
            ValueOperations<String,String> operations = redisTemplate.opsForValue();
            //清空登录计数
            if (match) {
                String key = MWAuthorizingRealm.SHIRO_IS_LOCK+loginName;
                operations.set(key,resetNum);
                redisTemplate.delete(key);
            }
        }else if (LoginType.LDAP_LOGIN.getType().equals(customToken.getLoginType())) {

            String loginName = customToken.getUsername();
            String loginPassword = String.valueOf(customToken.getPassword());
            ADConfigDTO ad = mwadUserDao.select();
            String domainName = ad.getDomainName();
            ADAuthenticParam param = ADAuthenticParam.builder()
                    .adAdminAccount(loginName+"@"+domainName)
                    .adAdminPasswd(loginPassword)
                    .adServerIpAdd(ad.getLdapIpAdd())
                    .adPort(ad.getLdapPort())
                    .adServerName("matchRule").build();

            Hashtable<String, String> env = ADUtils.getEnv(param);
            try {
                LdapContext ctx = ADUtils.getContext(env);
                log.info("ldap login success ");
                match = true;
            } catch (NamingException e) {
                match = false;
                log.error("ldap login failed :",e);
            }
        }
        return match;
    }
}
