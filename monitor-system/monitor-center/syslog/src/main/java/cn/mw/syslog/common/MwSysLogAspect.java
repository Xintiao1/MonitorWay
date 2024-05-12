package cn.mw.syslog.common;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MwSysLogAspect {

//    @Autowired
//    private MwSysLogService mwSysLogService;
//
//    @Autowired
//    private ILoginCacheInfo iLoginCacheInfo;
//
//    private static final String CURRENT_LOGIN_NAME_KEY = "CURRENT_LOGIN_NAME_KEY";
//
//
//    @Pointcut("@annotation(cn.mw.monitor.annotation.MwSysLog)")
//    public void logPointCut() {
//
//    }
//
//
//    @Around("logPointCut()")
//    public Object around(ProceedingJoinPoint point) throws Throwable {
//        MethodSignature signature = (MethodSignature) point.getSignature();
//        Method method = signature.getMethod();
//        MwSysLog sysLog = method.getAnnotation(MwSysLog.class);
//        if(!sysLog.value().equals("用户登出")&&!sysLog.value().equals("用户登录")){
//            if (iLoginCacheInfo.getLoginName()!=null){
//                testLog(point,iLoginCacheInfo.getLoginName());
//            }
//        }
//      return point.proceed();
//    }
//
//    @Before("logPointCut()")
//    public void befofore(JoinPoint point) throws Throwable {
//        MethodSignature signature = (MethodSignature) point.getSignature();
//        Method method = signature.getMethod();
//        MwSysLog sysLog = method.getAnnotation(MwSysLog.class);
//           if (sysLog.value().equals("用户登出")){
//               testLog(point,iLoginCacheInfo.getLoginName());
//
//           }
//    }
//
//    @After("logPointCut()")
//    public void after(JoinPoint point) throws Throwable {
//        MethodSignature signature = (MethodSignature) point.getSignature();
//        Method method = signature.getMethod();
//        MwSysLog sysLog = method.getAnnotation(MwSysLog.class);
//        if (sysLog.value().equals("用户登录")){
//            testLog(point,iLoginCacheInfo.getLoginName());
//        }
//  }
//
//    private void testLog(JoinPoint point,String name) throws Throwable {
//        HttpServletRequest httpServletRequest =
//                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String ip = IpUtil.getIpAddr(httpServletRequest);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String createDate = sdf.format(new Date());
//        long beginTime = System.currentTimeMillis();
//        long time = System.currentTimeMillis() - beginTime;
//        saveLog(point, time, ip, createDate,name);
//    }
//
//    private void saveLog(JoinPoint point, long time, String ip, String createDate,String name) {
//        MethodSignature signature = (MethodSignature) point.getSignature();
//        Method method = signature.getMethod();
//        MwSysLogEntity sysLogEntity = new MwSysLogEntity();
//        sysLogEntity.setUserIp(ip);
//        sysLogEntity.setExeuTime(time);
//        sysLogEntity.setCreateDate(createDate);
//        MwSysLog sysLog = method.getAnnotation(MwSysLog.class);
//        if (sysLog != null) {
//            sysLogEntity.setRemark(sysLog.value());
//            sysLogEntity.setType(sysLog.type());
//        }
//        String className = point.getTarget().getClass().getName();
//        String methodName = signature.getName();
//        sysLogEntity.setClassName(className);
//        sysLogEntity.setMothodName(methodName);
//        sysLogEntity.setUserName(name);
//        if (name==null||name.equals("null")||name.equals("")){
//            if (sysLogEntity.getRemark().equals("用户登录")){
//                sysLogEntity.setRemark("账号密码错误");
//                sysLogEntity.setUserName("未知用户");
//                mwSysLogService.save(sysLogEntity);
//            }
//        } else {
//            mwSysLogService.save(sysLogEntity);
//        }
//        }


}
