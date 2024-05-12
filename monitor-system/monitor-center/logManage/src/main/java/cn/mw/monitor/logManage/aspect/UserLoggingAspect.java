package cn.mw.monitor.logManage.aspect;

import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class UserLoggingAspect {

    @Autowired
    private MWUserService mwUserService;

    @Autowired
    private RedisUtils redisUtils;

    @Pointcut("execution(* cn.mw.monitor.logManage.controller.*.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object entry(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("更新当前用户信息到缓存");
        cacheUserName();
        return joinPoint.proceed();
    }


    public static final String REDIS_USER_KEY = "logManager:user:globalUser";

    public String getUserName() {
        try {
            GlobalUserInfo user = mwUserService.getGlobalUser();
            if (ObjectUtils.isNotEmpty(user) && StringUtils.isNotEmpty(user.getUserName())) {
                return user.getUserName();
            }
            return "Anonymous User";
        } catch (Exception e) {
            return "Anonymous User";
        }
    }

    public void cacheUserName() {
        String userName = getUserName();
        if (StringUtils.isNotBlank(userName)) {
            // 不设置过期时间
            Object userNameObj = redisUtils.get(REDIS_USER_KEY);
            if (userNameObj == null) {
                redisUtils.set(REDIS_USER_KEY, userName);
            } else {
                if (!String.valueOf(userNameObj).equals(userName)) {
                    redisUtils.set(REDIS_USER_KEY, userName);
                }
            }
        }
    }
}
