package cn.mw.monitor.shiro;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.user.advcontrol.RequestInfo;
import cn.mw.monitor.util.IpUtil;
import cn.mw.monitor.user.service.IUserControlService;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MwPermsFilter extends AuthorizationFilter {

    private static final Logger logger = LoggerFactory.getLogger(MwPermsFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object o) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String ip = IpUtil.getIpAddr(httpServletRequest);
        String time = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        RequestInfo requestInfo = new RequestInfo(ip, "" ,time);

        String uri = httpServletRequest.getRequestURI();
        Subject subject = this.getSubject(request, response);
        subject.getSession().setAttribute(IUserControlService.REQINFO, requestInfo);
        
        boolean ret = subject.isPermitted(new MwPermission(uri));
        return ret;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {

        Reply reply = Reply.fail(ErrorConstant.USER_100129, ErrorConstant.USER_MSG_100129);
        ResponseBase result = new  ResponseBase(Constants.HTTP_RES_CODE_301, Constants.HTTP_RES_CODE_301_VALUE, reply);
        HttpUtils.responseOutWithJson((HttpServletResponse)response, result);
        return false;
    }
}
