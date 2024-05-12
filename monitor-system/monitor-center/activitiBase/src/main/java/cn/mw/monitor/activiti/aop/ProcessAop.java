package cn.mw.monitor.activiti.aop;

import cn.mw.monitor.activiti.dao.ProcessDao;
import cn.mw.monitor.activiti.dto.ProcessModuleBindDTO;
import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.annotation.MwProcess;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@Slf4j
public class ProcessAop extends BaseApiService {

    @Resource
    private ProcessDao processDao;

    @Autowired
    ActivitiService activitiService;

    @Pointcut("@annotation(cn.mw.monitor.annotation.MwProcess)")
    public void pointcut(){
    }





    private Object invokeOriginalMethod(String instanceId) throws Exception{

        Map map = activitiService.getInstanceVariable(instanceId);
        String bindBean = (String)map.get(ActivitiService.BIND_BEAN);
        Object obj = SpringUtils.getBean(bindBean);

        String method = (String)map.get(ActivitiService.BIND_METHOD);
        Method beanMethod = obj.getClass().getMethod(method);

        String paramClassStr = (String)map.get(ActivitiService.BIND_PARAM_CLASS);
        Class paramClass = this.getClass().getClassLoader().loadClass(paramClassStr);

        String param = (String)map.get(ActivitiService.BIND_PARAM);

        Object paramObject = JSONObject.parseObject(param, paramClass);

        return beanMethod.invoke(obj, paramObject);
    }
}
