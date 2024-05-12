package cn.mw.monitor.shiro;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.api.listener.LoginEventProcesser;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.util.RedisUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author baocb
 * 日期:2020-05-22
 * 说明:自定义并发登录拦截器
 */
public class KickoutSessionControlFilter extends AccessControlFilter {

    private static final Logger logger = LoggerFactory.getLogger(KickoutSessionControlFilter.class.getName());
    /**
     * 被踢出后重定向的地址  后台接口路径
     */
    private String kicOutUrl;

    /**
     * 最大登录人数 默认为1
     */
    private int maxNum = 1;

    /**
     * 踢出前者还是后者 为true踢出后者 默认踢出前者
     */
    private boolean kicoutAfter = false;

    private static final String DEFAULT_KICKOUT_CACHE_KEY_PREFIX = "shiro:cache:kickout:";

    private static final String redisGroup = LoginEventProcesser.class.getSimpleName();

    private String keyPrefix = DEFAULT_KICKOUT_CACHE_KEY_PREFIX;

    private String getRedisKickoutKey(String username) {
        return this.keyPrefix + username;
    }

    private static final String KICOUT_PROPERTY_NAME = "kickout";

    /**
     * 是否允许访问，true表示允许
     * @param servletRequest
     * @param servletResponse
     * @param o
     * @return
     * @throws Exception
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    /**
     * 表示访问拒绝时是否自己处理，如果返回true表示自己不处理且继续拦截器链执行，返回false表示自己已经处理了（比如重定向到另一个页面）。
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        ShiroSessionManager sessionManager = SpringUtils.getBean(ShiroSessionManager.class);
        Subject subject = SecurityUtils.getSubject();
        //如果用户没有登录且没有配置『记住我』,则跳过
        if(!subject.isAuthenticated() && !subject.isRemembered()){
            return true;
        }

        UserDTO user = (UserDTO)subject.getPrincipal();
        Session session = subject.getSession();

        Serializable sessionId = session.getId();

        //初始化用户队列 放进缓存
        Deque<Serializable> deque = (Deque<Serializable>)redisUtils.get(getRedisKickoutKey(user.getLoginName()));
        if(deque == null || deque.size() == 0){
            deque = new LinkedList<>();
        }

        //如果用户队列里没有此sessionId,且没有被踢出 则放入队列 并放入缓存
        if(!deque.contains(sessionId) && session.getAttribute(KICOUT_PROPERTY_NAME) == null){
            deque.push(sessionId);
            redisUtils.set(getRedisKickoutKey(user.getLoginName()), deque, -1);
        }

        //如果队列里的用户数量超过最大值 开始踢人
        while (deque.size() > maxNum){
            Serializable kicoutSessionId;
            //为true踢出前者  kicoutAfter默认是false
            if(!kicoutAfter){
                kicoutSessionId = deque.removeLast();
            } else {
                //否则踢出后者
                kicoutSessionId = deque.removeFirst();
            }
            //刪除缓存中的token信息
            redisUtils.del(redisGroup+":"+kicoutSessionId);
            redisUtils.set(getRedisKickoutKey(user.getLoginName()), deque, -1);
            try {
                Session kicoutSession = sessionManager.getSession(new DefaultSessionKey(kicoutSessionId));
                if(kicoutSession != null){
                    //设置此属性为true表示这个会话被踢出了
                    kicoutSession.setAttribute(KICOUT_PROPERTY_NAME, true);
                }
            } catch (SessionException e) {
                logger.info(kicoutSessionId+"已过期！");
            }

        }

        if(session.getAttribute(KICOUT_PROPERTY_NAME) != null){
            //会话被踢出了
            try {
                //从shiro退出登录
                subject.logout();
                //给前端返回信息
                HttpServletResponse response = WebUtils.toHttp(servletResponse);
                Reply reply = Reply.fail(ErrorConstant.USER_100135, ErrorConstant.USER_MSG_100135);
                ResponseBase result = new  ResponseBase(cn.mw.monitor.api.common.Constants.HTTP_RES_CODE_303, Constants.HTTP_RES_CODE_303_VALUE, reply);
                HttpUtils.responseOutWithJson(response, result);
                return false;
            } catch (Exception e) {
                logger.error("会话被踢出失败",e);
            }

        }
        return true;
    }

    public String getKicOutUrl() {
        return kicOutUrl;
    }

    public void setKicOutUrl(String kicOutUrl) {
        this.kicOutUrl = kicOutUrl;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public boolean isKicoutAfter() {
        return kicoutAfter;
    }

    public void setKicoutAfter(boolean kicoutAfter) {
        this.kicoutAfter = kicoutAfter;
    }

    public static String getKicoutPrefix(){return DEFAULT_KICKOUT_CACHE_KEY_PREFIX;}
}

