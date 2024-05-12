package cn.mw.monitor.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * @Author: bcb
 * @Date: 2020/5/18 22:55
 * @Version 1.0
 * @Discription 自定义SessionMananger 为什么要自定义自定义SessionMananger?
 * 原因:
 * 因为前后端分离的情况下 不是靠session,而是靠token去交互,因此需要自定义这个sessionId的获取
 * 即重写父类的方法(父类是从头中拿sessionId)
 *
 */
@Component
public class ShiroSessionManager extends DefaultWebSessionManager {

    //这个key 放在请求头中,可以自己定义 ,通常是设置为token或者authorization
    private static final String AUTHORIZATION = "X-Token";

    public ShiroSessionManager() {
        super();
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        //将ServletRequest转换成HttpServletRequest
        String sessionId = WebUtils.toHttp(request).getHeader(AUTHORIZATION);
        if (sessionId != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                    ShiroHttpServletRequest.COOKIE_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return sessionId;
        } else {
            return super.getSessionId(request, response);
        }
    }

}
