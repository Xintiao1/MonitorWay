package cn.mw.monitor.shiro;

import cn.mw.monitor.api.common.SpringUtils;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("securityManager")
public class DelegatingSecurityManager extends AbstractFactoryBean<WebSecurityManager> implements InvocationHandler, BeanClassLoaderAware, ApplicationContextAware {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.timeout}")
    private Integer redisTimeout;

    @Value("${userExpireTime}")
    private long expireTime;

    private DefaultWebSecurityManager defaultWebSecurityManager;

    private ClassLoader classLoader;

    private ApplicationContext applicationContext;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getDeclaringClass() == Object.class){
            return method.invoke(proxy, args);
        }
        if(null == this.defaultWebSecurityManager){
            init();
        }
        try{
            return method.invoke(this.defaultWebSecurityManager, args);
        }catch(InvocationTargetException e){
            //代理调用，要剥掉最外层的InvocationTargetException
            throw e.getTargetException();
        }
    }

    @Override
    public Class<WebSecurityManager> getObjectType() {
        return WebSecurityManager.class;
    }

    @Override
    protected WebSecurityManager createInstance() throws Exception {
        return (WebSecurityManager) Proxy.newProxyInstance(this.classLoader, new Class<?>[]{WebSecurityManager.class}, this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void init(){
        MWAuthorizingRealm authenticatingRealm = (MWAuthorizingRealm)applicationContext.getBean("MWAuthorizingRealm");
        ADAuthorizingRealm adAuthorizingRealm = (ADAuthorizingRealm)applicationContext.getBean("ADAuthorizingRealm");
        MWdCredentialsMatcher matcher = (MWdCredentialsMatcher)applicationContext.getBean("MWdCredentialsMatcher");
        ShiroSessionManager sessionManager = SpringUtils.getBean(ShiroSessionManager.class);
        ShiroSessionListener listener = SpringUtils.getBean(ShiroSessionListener.class);
        Collection<SessionListener> listeners = new ArrayList<SessionListener>();
        //配置监听
        listeners.add(listener);
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionDAO(redisSessionDAO());
        sessionManager.setGlobalSessionTimeout(expireTime * 1000);
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionIdCookie(sessionIdCookie());
        //是否开启删除无效的session对象  默认为true
        sessionManager.setDeleteInvalidSessions(true);
        //是否开启定时调度器进行检测过期session 默认为true
        sessionManager.setSessionValidationSchedulerEnabled(true);
        //设置session失效的扫描时间, 清理用户直接关闭浏览器造成的孤立会话 默认为 1个小时
        //设置该属性 就不需要设置 ExecutorServiceSessionValidationScheduler 底层也是默认自动调用ExecutorServiceSessionValidationScheduler
        sessionManager.setSessionValidationInterval(3600000);
        //取消url 后面的 JSESSIONID
        sessionManager.setSessionIdUrlRewritingEnabled(false);

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        MyModularRealmAuthenticator customAuthenticator = new MyModularRealmAuthenticator();
        //设置realm判断条件
        customAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        securityManager.setAuthenticator(customAuthenticator);

        List<Realm> realms = new ArrayList<>();
        authenticatingRealm.setName(LoginType.Common.getType());
        adAuthorizingRealm.setName(LoginType.LDAP_LOGIN.getType());
        authenticatingRealm.setCredentialsMatcher(matcher);
        adAuthorizingRealm.setCredentialsMatcher(matcher);
        realms.add(authenticatingRealm);
        realms.add(adAuthorizingRealm);
        securityManager.setRealms(realms);
        securityManager.setSessionManager(sessionManager);
        this.defaultWebSecurityManager = securityManager;
    }

    /**
     * 自定义的realm管理，主要针对多realm
     */
 /*   public MyModularRealmAuthenticator myModularRealmAuthenticator() {
        MyModularRealmAuthenticator customAuthenticator = new MyModularRealmAuthenticator();
        //设置realm判断条件
        customAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());

        return customAuthenticator;
    }*/

/*    public MWAuthorizingRealm authenticatingRealm () {
        MWAuthorizingRealm realm = new MWAuthorizingRealm();
        realm.setName(LoginType.Common.getType());
//        realm.setCredentialsMatcher(mWdCredentialsMatcher());
        return realm;
    }*/

    /**
     * ldap
     */
/*    public ADAuthorizingRealm adAuthorizingRealm () {
        ADAuthorizingRealm realm = new ADAuthorizingRealm();
        realm.setName(LoginType.LDAP_LOGIN.getType());
        //自定义密码校验器
//        realm.setCredentialsMatcher(adDCredentialsMatcher());
        return realm;
    }*/

    /**
     * 配置redisManager
     */
    public RedisManager getRedisManager() {

        RedisManager redisManager = new RedisManager();
        redisManager.setHost(redisHost+":"+redisPort);
        redisManager.setPassword(redisPassword);
        redisManager.setTimeout(redisTimeout);
        return redisManager;
    }

    /**
     * 自定义session持久化
     *
     * @return
     */
    public RedisSessionDAO redisSessionDAO() {

        /*
          为啥session也要持久化？
                重启应用，用户无感知，可以继续以原先的状态继续访问
          注意点：
                DO对象需要实现序列化接口 Serializable
                logout接口和以前一样调用，请求logout后会删除redis里面的对应的key,即删除对应的token
         */
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(getRedisManager());

        //配置自定义sessionId,shiro自动生成色sessionId不满足条件时可以使用
        redisSessionDAO.setSessionIdGenerator(new ShiroSessionIdGenerator());
        return redisSessionDAO;
    }

    public SimpleCookie sessionIdCookie(){
        //这个参数是cookie的名称
        SimpleCookie simpleCookie = new SimpleCookie("sid");
        //setcookie的httponly属性如果设为true的话，会增加对xss防护的安全系数。它有以下特点：

        //setcookie()的第七个参数
        //设为true后，只能通过http访问，javascript无法访问
        //防止xss读取cookie
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        //maxAge=-1表示浏览器关闭时失效此Cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }
}

