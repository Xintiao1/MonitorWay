package cn.mw.monitor.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * 自定义多realm登陆策略
 * Created by zy.quaee on 2021/4/30 9:33.
 **/
@Slf4j
@Component
public class MyModularRealmAuthenticator extends ModularRealmAuthenticator {

    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        //判断getRealms() 是否返回为空
        assertRealmsConfigured();

        CustomUserNamePasswdToken token = (CustomUserNamePasswdToken) authenticationToken;
        log.info(" custom token ----->"+ token);

        //登录类型
        String loginType = token.getLoginType();

        //所有realm
        Collection<Realm> realms = getRealms();
        //登录类型对应的realm
        HashMap<String,Realm> loginRealms = new HashMap<>(realms.size());
        realms.forEach(r->loginRealms.put(r.getName(),r));

        if (loginRealms.get(loginType) != null) {
            return doSingleRealmAuthentication(loginRealms.get(loginType),token);
        }else {
            return doMultiRealmAuthentication(realms,token);
        }

    }
}

