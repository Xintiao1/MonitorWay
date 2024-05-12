package cn.mw.monitor.shiro;

import cn.mw.monitor.user.advcontrol.RequestInfo;
import cn.mw.monitor.user.service.IUserControlService;
import cn.mw.monitor.util.IpUtil;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MwUserControlFilter extends AuthorizationFilter {

    private static final Logger logger = LoggerFactory.getLogger(MwUserControlFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object o) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String ip = IpUtil.getIpAddr(httpServletRequest);
        String time = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm").format(LocalDateTime.now());
        RequestInfo requestInfo = new RequestInfo(ip, "",time);
        Subject subject = this.getSubject(request, response);
        subject.getSession().setAttribute(IUserControlService.REQINFO, requestInfo);

        return true;
    }
}
