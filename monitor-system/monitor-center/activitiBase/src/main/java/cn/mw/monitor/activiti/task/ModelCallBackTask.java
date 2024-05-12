package cn.mw.monitor.activiti.task;

import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class ModelCallBackTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Map map = delegateExecution.getVariables();
            String bindBean = (String)map.get(ActivitiService.BIND_BEAN);
            Object obj = SpringUtils.getBean(bindBean);

            String method = (String)map.get(ActivitiService.BIND_METHOD);
            Method beanMethod = obj.getClass().getMethod(method);

            String paramClassStr = (String)map.get(ActivitiService.BIND_PARAM_CLASS);
            Class paramClass = this.getClass().getClassLoader().loadClass(paramClassStr);

            String param = (String)map.get(ActivitiService.BIND_PARAM);

            Object paramObject = JSONObject.parseObject(param, paramClass);

            //设置拦截参数, 对应aop类才能直接回调原对象
            if(paramObject instanceof BaseProcessParam){
                BaseProcessParam baseProcessParam = (BaseProcessParam) paramObject;
                baseProcessParam.setIntercepted(false);
            }
            beanMethod.invoke(obj, paramObject);
        }catch (Exception e){
            log.error("execute", e);
        }
    }
}
