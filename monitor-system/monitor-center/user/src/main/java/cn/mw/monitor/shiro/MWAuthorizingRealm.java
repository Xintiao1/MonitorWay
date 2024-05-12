package cn.mw.monitor.shiro;

import cn.mw.monitor.event.RefreshPermEvent;
import cn.mw.monitor.service.user.exception.UserLockedException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.IUserControllerLogin;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;
import cn.mw.monitor.state.UserExpireState;
import cn.mw.monitor.user.service.IMWNotCheckUrlService;
import cn.mw.monitor.user.service.MwModuleService;
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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by dev on 2020/2/16.
 */
@Component
public class MWAuthorizingRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(MWAuthorizingRealm.class);

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    @Autowired
    IUserControllerLogin iUserControllerLogin;

    @Autowired
    IMWNotCheckUrlService mwNotCheckUrlService;

    @Autowired
    MwModuleService mwModuleService;

    @Autowired
    StringRedisTemplate redisTemplate;

    final static String SHIRO_IS_LOCK = "SHIRO_IS_LOCK--";

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
        String nowUserName = loginCacheInfo.getLoginName();
        if(permission instanceof MwPermission){
            MwPermission mwPermission = (MwPermission) permission;
            isNeecCheck = mwNotCheckUrlService.isNeedCheck(mwPermission.getUri());

            if(!isNeecCheck){
                logger.info(nowUserName+"operating now ,"+"MWAuthorizingRealm uri: " + mwPermission.getUri() +
                        " is notcheckurl , pass!");
                return !isNeecCheck;
            }

            String modulePermKey = mwModuleService.getModulePermKey(mwPermission.getUri());
            String loginName = loginCacheInfo.getLoginName();
            Integer roleId = loginCacheInfo.getRoleInfo().getId();
            StringBuffer key = new StringBuffer();
            key.append(roleId).append("-").append(modulePermKey);
            MwRoleModulePermMapper mwRoleModulePermMapper = loginCacheInfo.getMwRoleModulePermMapper(key.toString(), loginName);

            if(null != mwRoleModulePermMapper && mwRoleModulePermMapper.getEnable()){
                logger.info(nowUserName+": have this url {"+mwPermission.getUri()+"} permission , pass!");
                return true;
            }else{
                logger.info("MwRoleModulePermMapper:" + mwRoleModulePermMapper);
            }

            //由于redis缓存策略原因,导致权限数据意外删除,此时需要重新刷新权限缓存,并再次检查
            LoginContext loginContext = loginCacheInfo.getCacheInfo(loginName);
            if(null != loginContext) {
                Integer userId = loginContext.getUserId();
                RefreshPermEvent refreshPermEvent = new RefreshPermEvent(loginContext, loginName, userId);
                try {
                    iUserControllerLogin.handleEvent(refreshPermEvent);
                } catch (Throwable throwable) {
                    logger.error("MWAuthorizingRealm", throwable);
                }
                mwRoleModulePermMapper = loginCacheInfo.getMwRoleModulePermMapper(key.toString(), loginName);
                if (null != mwRoleModulePermMapper && mwRoleModulePermMapper.getEnable()) {
                    logger.info(nowUserName + ": recheck permission have this url {" + mwPermission.getUri() + "} permission , pass!");
                    return true;
                }
            }else{
                logger.info("loginContext is null");
            }

            StringBuffer sb = new StringBuffer("loginName:").append(loginName)
                    .append(";roleId:").append(roleId)
                    .append(";key:").append(key)
                    .append(";uri:").append(mwPermission.getUri())
                    ;
            logger.info("url validate fail, no permission------>"+sb.toString());
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

        LoginContext loginContext = loginCacheInfo.getThreadLocalInfo();
        LoginInfo loginInfo = loginContext.getLoginInfo();
        UserDTO user = loginInfo.getUser();

        MWPasswordPlanDTO mwpasswordPlanDTO = loginContext.getMwpasswordPlanDTO();
        user.setHashTypeId(mwpasswordPlanDTO.getHashTypeId());
        user.setSalt(mwpasswordPlanDTO.getSalt());
        String passwd = loginContext.getDbpasswd();

        //登陆失败后拒绝访问
        if (mwpasswordPlanDTO.getIsRefuseAcc()) {
            String key = SHIRO_IS_LOCK+loginName;
            ValueOperations<String,String> operations = redisTemplate.opsForValue();
            //访问一次 计数一次
            if (!UserExpireState.LOCK.getName().equals(operations.get(key))) {
                operations.increment(key,1);
                //超过重试次数，用户锁定
                if (Integer.parseInt(Objects.requireNonNull(operations.get(key))) >= mwpasswordPlanDTO.getRetryNum()) {
                    operations.set(key, UserExpireState.LOCK.getName());
                    redisTemplate.expire(key,mwpasswordPlanDTO.getRetrySec(), TimeUnit.SECONDS);
                }
            }else {
                throw new UserLockedException(loginName,mwpasswordPlanDTO.getRetrySec());
            }
        }


        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user,
                passwd,  //密码
                ByteSource.Util.bytes(mwpasswordPlanDTO.getSalt()),
                getName()  //realm name
        );
        return authenticationInfo;
    }

}
