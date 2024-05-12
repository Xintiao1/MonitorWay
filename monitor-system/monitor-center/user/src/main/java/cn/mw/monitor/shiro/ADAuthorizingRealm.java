package cn.mw.monitor.shiro;

import cn.mw.monitor.api.exception.UnknownUserException;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;
import cn.mw.monitor.user.dao.MWADUserDao;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.user.model.ADConfigDTO;
import cn.mw.monitor.user.model.AdType;
import cn.mw.monitor.user.service.IMWNotCheckUrlService;
import cn.mw.monitor.user.service.MwModuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ldap
 * Created by zy.quaee on 2021/4/29 22:51.
 **/
@Component
@Slf4j
public class ADAuthorizingRealm extends AuthorizingRealm {
    private static final Logger logger = LoggerFactory.getLogger(ADAuthorizingRealm.class);

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    @Autowired
    IMWNotCheckUrlService mwNotCheckUrlService;

    @Autowired
    MwModuleService mwModuleService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    private MWUserDao mwUserDao;

    @Resource
    private MWADUserDao mwadUserDao;

    private final String userType = "AD";
    private final String ldapSalt = "ldap";



    @Override
    public String getName() {
        return LoginType.LDAP_LOGIN.getType();
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        boolean f = LoginType.LDAP_LOGIN.equals(((CustomUserNamePasswdToken) token).getLoginType());
        ////System.out.println("LoginType.LDAP_LOGIN.equals(((CustomUserNamePasswdToken) token).getLoginType())" + f);
        return token instanceof CustomUserNamePasswdToken;
    }

    @Override
    protected void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        boolean isNeecCheck = false;
        if(permission instanceof MwPermission){
            MwPermission mwPermission = (MwPermission) permission;
            isNeecCheck = mwNotCheckUrlService.isNeedCheck(mwPermission.getUri());

            if(!isNeecCheck){
                logger.info("MWAuthorizingRealm uri: " + mwPermission.getUri() + " need to be checked!");
                return !isNeecCheck;
            }

            String modulePermKey = mwModuleService.getModulePermKey(mwPermission.getUri());
            String loginName = loginCacheInfo.getLoginName();
            Integer roleId = loginCacheInfo.getRoleInfo().getId();
            StringBuffer key = new StringBuffer();
            key.append(roleId).append("-").append(modulePermKey);
            MwRoleModulePermMapper mwRoleModulePermMapper = loginCacheInfo.getMwRoleModulePermMapper(key.toString(), loginName);

            if(null != mwRoleModulePermMapper && mwRoleModulePermMapper.getEnable()){
                return true;
            }else{
                logger.info("MwRoleModulePermMapper:" + mwRoleModulePermMapper);
            }
            StringBuffer sb = new StringBuffer("loginName:").append(loginName)
                    .append(";roleId:").append(roleId)
                    .append(";key:").append(key)
                    .append(";uri:").append(mwPermission.getUri())
                    ;
            logger.info(sb.toString());
        }


        return false;
    }

    /**
     * 认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {

        String loginName = (String) authenticationToken.getPrincipal();
        String userType = AdType.AD.name();
        ADConfigDTO ad = mwadUserDao.select();
        String passwd = String.valueOf(authenticationToken.getCredentials());
        String fullName = loginName+"@"+ad.getDomainName();
        log.info("now login user full name ---"+fullName);
        UserDTO user = mwUserDao.selectADUserByType(fullName,userType);

         if (user == null) {
            throw new UnknownUserException();
        }

         return new SimpleAuthenticationInfo(
                user,
                passwd,
                ByteSource.Util.bytes(ldapSalt),
                getName()  //realm name
        );
    }
}
