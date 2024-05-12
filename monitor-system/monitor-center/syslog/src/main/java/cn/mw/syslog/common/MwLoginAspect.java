package cn.mw.syslog.common;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.systemLog.api.MwSysLogService;
import cn.mw.monitor.service.systemLog.dto.LoginLogDTO;
import cn.mw.monitor.service.user.dto.LoginDTO;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.util.IpUtil;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Aspect
@Component
public class MwLoginAspect {

    @Autowired
    private MwSysLogService mwSysLogService;

    @Pointcut("@annotation(cn.mw.monitor.annotation.MwLoginLog)")
    public void loginPointCut(){

    }

    @AfterReturning(returning = "ret",pointcut = "loginPointCut()")
    public void afterLogin(Object ret) throws Throwable {
        ResponseBase ll=(ResponseBase) ret;
        String isSuccess=null;
        String username=null;
        String loginway=null;
        String failType=null;
        if(ll.getRtnCode()== Constants.HTTP_RES_CODE_200){
            LoginDTO loginDTO=(LoginDTO)ll.getData();
            LoginParam loginParam = loginDTO.getLoginParam();
            isSuccess="成功";
            username=loginParam.getLoginName();
            failType="登录成功";
            if("mw_login".equals(loginParam.getLoginType())){
                loginway="本地";
            }else if("ldap_login".equals(loginParam.getLoginType())){
                loginway="LDAP";
            }else{
                //postman测试时
                loginway="本地";
            }
        }else{
            LoginParam loginParam=(LoginParam)ll.getData();
            isSuccess="失败";
            username=loginParam.getLoginName();
            failType=ll.getMsg();
            if("mw_login".equals(loginParam.getLoginType())){
                loginway="本地";
            }else if("ldap_login".equals(loginParam.getLoginType())){
                loginway="LDAP";
            }else{
                loginway="本地";
            }

        }
        testLog(username,isSuccess,failType,loginway);
    }

    private void testLog(String username,String isSuccess,String failType,String loginWay) throws Throwable {
        HttpServletRequest httpServletRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = IpUtil.getIpAddr(httpServletRequest);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createDate = sdf.format(new Date());
        saveLog(createDate,username,ip,loginWay,failType,isSuccess);
    }

    private void saveLog(String createDate,String name,  String ip,String loginWay,String failType,String isSuccess) {
        LoginLogDTO loginLogDTO = new LoginLogDTO();
        loginLogDTO.setUserIp(ip);
        loginLogDTO.setCreateDate(createDate);
        loginLogDTO.setUserName(name);
        loginLogDTO.setLoginWay(loginWay);
        loginLogDTO.setIsSuccess(isSuccess);
        loginLogDTO.setFailType(failType);
        mwSysLogService.saveLoginLog(loginLogDTO);
    }
}
