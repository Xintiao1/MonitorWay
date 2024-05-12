package cn.mw.monitor.shiro;


import cn.mw.monitor.common.constant.Constants;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.*;

/**
 * Created by yeshengqi on 2019/5/9.
 */
@Configuration
public class ShiroConfig implements ApplicationContextAware {

    @Value("${mwapi.open}")
    private boolean isOpen;

    @Value("${visualized.open}")
    private boolean isVisualizedOpen;

    private ApplicationContext applicationContext;

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(WebSecurityManager securityManager) throws Exception {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        //以下是过滤链，按顺序过滤，所以/**需要放最后
        //开放的静态资源

//        shiroFilterFactoryBean.setLoginUrl("/mwapi/user/login");//自定义登录页面地址
        shiroFilterFactoryBean.setSuccessUrl("/index");//登录成功
        filterChainDefinitionMap.put("/logout", "logout"); //退出登录

        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/mwapi/user/login", "anon");
        filterChainDefinitionMap.put("/error", "anon");
        filterChainDefinitionMap.put("/login/captcha", "anon");
        filterChainDefinitionMap.put("/mwapi/login/logo", "anon");
        filterChainDefinitionMap.put("/mwapi/test", "anon");
        filterChainDefinitionMap.put("/mwapi/login/captcha", "anon");
        filterChainDefinitionMap.put("/mwapi/user/loginInfo", "anon");
        filterChainDefinitionMap.put("/mwapi/loginPage", "anon");
        filterChainDefinitionMap.put("/mwapi/wxAuth/callBack", "anon");
        filterChainDefinitionMap.put("/mwapi/bind/wechat", "anon");
        filterChainDefinitionMap.put("/mwapi/bind/login", "anon");
        filterChainDefinitionMap.put("/mwapi/TPServer/refresh", "anon");
        filterChainDefinitionMap.put("/mwapi/ipAddressManageList/export", "anon");
        filterChainDefinitionMap.put("/mwapi/TimeServer/refresh", "anon");
        filterChainDefinitionMap.put("/mwapi/timeAllTask/runNewTime", "anon");
        filterChainDefinitionMap.put("/mwapi/mwVersion/browse", "anon");
        filterChainDefinitionMap.put("/mwapi/delBind", "anon");
        filterChainDefinitionMap.put("/mwapi/qyWXLogin", "anon");
        filterChainDefinitionMap.put("/mwapi/qywxAuth/callBack", "anon");
        filterChainDefinitionMap.put("/mwapi/logLevel", "anon");
        /**
         * @describe /mwapi/weixin/wx     get
         * 微信端访问我们系统当请求是get请求时：
         * 1：根据微信传递的参数和我们系统从配置文件里面读取服务号的相关配置进行加密算法，
         * 用加密算法的结果和预期的结果比较，正常的话我们认为该请求是微信正常的请求，同时返回约定的值回去
         * 2：微信端接收我们的结果去判断校验，正常的话，微信就会认为我们的系统是服务号的后端，验证成功。
         * 这个过程就是企业服务号的接入。
         *
         *  @describe /mwapi/weixin/wx     post
         * 微信端访问我们系统当请求是post请求时：
         * 这个请求主要是微信段给我们传递数据
         * （传递那些数据？用户在服务号里面输入的文本，用户在服务号里面的操作等）
         */
        filterChainDefinitionMap.put("/mwapi/weixin/wx", "anon");
        filterChainDefinitionMap.put("/mwapi/ws/screen/popup/browse/**", "anon");
        filterChainDefinitionMap.put("/mwapi/ws/myWaitingToDo/count/browse/**", "anon");
        filterChainDefinitionMap.put("/druid/**", "anon");
        /**
         * @describe /mwapi/weixin/getUserInfoAndRedirect     get
         * 1微信用户想在服务号登录我们的微端不会直接让他登录，先让它跳转到这个地址。
         * 2在这个地址的方法里面我们可以获取当前用户的基本信息，然后做出一系列操作。
         * 3如果这个用户可以登录我们的微端，就返回一个 重定向到我们的微端的地址，
         * 不可以的话返回一个错误页面或者返回空等操作。
         */
        filterChainDefinitionMap.put("/mwapi/weixin/getUserInfoAndRedicet", "anon");


        filterChainDefinitionMap.put("/mwapi/user/login", "anon,mwusercontrol");
        //filterChainDefinitionMap.put("/*", "anon");
        filterChainDefinitionMap.put(Constants.APP_RESOURCE, "anon");

        filterChainDefinitionMap.put(Constants.PLUGINS_RESOURCE, "anon");

        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger/**", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/mwapi/upload/**", "anon");
        filterChainDefinitionMap.put("/mwapi/basics/**", "anon");
        filterChainDefinitionMap.put("/mwapi/common/**", "anon");

        filterChainDefinitionMap.put("/v2/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");

        if(isOpen){
            filterChainDefinitionMap.put("/mwapi/script-manage/open/**", "anon");
            filterChainDefinitionMap.put("/mwapi/modelView/open/**", "anon");
            filterChainDefinitionMap.put("/mwapi/server/open/**", "anon");
            filterChainDefinitionMap.put("/mwapi/weixin/open/sendMessage", "anon");
            filterChainDefinitionMap.put("/mwapi/weixin/open/getHtml", "anon");
            filterChainDefinitionMap.put("/mwapi/open/getIpAddressListDes/browse", "anon");
            filterChainDefinitionMap.put("/mwapi/ModelDigitalTwin/**", "anon");
            filterChainDefinitionMap.put("/mwapi/alert/open/getToken", "anon");
            filterChainDefinitionMap.put("/mwapi/alert/open/saveToken", "anon");
        }
        //是否开放可视化大屏的免登录功能
        if(isVisualizedOpen){
            filterChainDefinitionMap.put("/mwapi/visualized/**", "anon");
            filterChainDefinitionMap.put("/mwapi/topology/listNotifyEvent","anon");
            filterChainDefinitionMap.put("/mwapi/topology/editor","anon");
            filterChainDefinitionMap.put("/mwapi/topology/group/browse","anon");
        }

        filterChainDefinitionMap.put("/**", "kickout,authc,mwperm,mwusercontrol");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        Map<String, Filter> mwFilters = new HashMap<>();
        //限制同一帐号同时在线的个数
        mwFilters.put("kickout",new KickoutSessionControlFilter());
        mwFilters.put("authc",new ShiroLoginFilter());
        mwFilters.put("mwperm", new MwPermsFilter());
        mwFilters.put("mwusercontrol", new MwUserControlFilter());
        shiroFilterFactoryBean.setFilters(mwFilters);

        return shiroFilterFactoryBean;
    }

    @Bean
    public FilterRegistrationBean delegatingFilterProxy(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();

        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        filterRegistrationBean.setUrlPatterns(urlPatterns);
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName("shiroFilter");
        filterRegistrationBean.setFilter(proxy);
        return filterRegistrationBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        /*String[] beannames = applicationContext.getBeanDefinitionNames();
        for(String name: beannames){
            ////System.out.println(name);
        }*/
    }
}
