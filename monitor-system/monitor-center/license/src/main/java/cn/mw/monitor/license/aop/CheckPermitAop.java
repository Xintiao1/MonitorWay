package cn.mw.monitor.license.aop;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ModuleDesc;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.license.util.ScheduleGetPermit;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@Slf4j
public class CheckPermitAop {
    @Autowired
    ScheduleGetPermit scheduleGetPermit;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Pointcut("@annotation(cn.mw.monitor.annotation.MwPermit)")
    public void check(){
    }

    @Around("check()")
    public  Object checkPermit(ProceedingJoinPoint joinPoint) throws Throwable{
        long start = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MwPermit annotation = method.getAnnotation(MwPermit.class);
        String module = annotation.moduleName();
        int permit = scheduleGetPermit.isPermit(module);
        log.info("消耗时间：" + (System.currentTimeMillis() - start));
        //permit = 0;
        if(permit==0){
            return joinPoint.proceed(args);
        }
        String msg = null;
        String loginName = iLoginCacheInfo.getLoginName();
        switch (permit) {
            case 1:
                msg = "许可文件加载失败！";
                break;
            case 2:
                msg = ModuleDesc.getModuleDescEnum(module) + "module not licensed,please reapply";
                break;
            case 3:
                msg="The SN is incorrect,please upload the correct license file";
                break;
            case 4:
                msg=  "您的许可已到期，为了不影响使用，请及时更新!";
                break;
            case 6:
                msg= ModuleDesc.getModuleDescEnum(module)+ "is stop,please go to configuration";
                break;
            case 7:
                msg = module;
                if(ModuleDesc.getModuleDescEnum(module) != null){
                    msg = ModuleDesc.getModuleDescEnum(module);
                }
                Object obj = joinPoint.proceed(args);
                if(obj instanceof ResponseBase){
                    ResponseBase rb = (ResponseBase) obj;
                    if(!redisTemplate.hasKey(loginName + "_operation")){
                        rb.setLicMsg(msg + "使用已过期,请重新激活！");
                    }
                    return rb;
                }
                break;
            case 8:
                ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
                LicenseXmlParam param = propMap.get(module);
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                Date parse1 = sf.parse(param.getExpireDate());
                Date date = new Date();
                long time = parse1.getTime()- date.getTime();
                long d=time/(3600*24*1000);
                Object ob = joinPoint.proceed(args);
                if(ob instanceof ResponseBase){
                    ResponseBase rb = (ResponseBase) ob;
                    if(!redisTemplate.hasKey(loginName + "_operation")){
                        rb.setLicMsg("许可还剩余" + d + "天！");
                    }
                    return rb;
                }
                break;
            default:
                msg="unkonw erro";
        }
        return new ResponseBase(Constants.HTTP_RES_CODE_500,msg, null);
    }
}
