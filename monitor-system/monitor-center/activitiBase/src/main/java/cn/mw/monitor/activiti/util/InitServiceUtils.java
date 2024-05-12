package cn.mw.monitor.activiti.util;

import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author syt
 * @Date 2020/10/20 9:50
 * @Version 1.0
 */
@Component
public class InitServiceUtils {
    @Autowired
    private ActivitiService activitiDemoService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    public InitServiceUtils() {
    }

    public static class SingletonHolder {
        private static final InitServiceUtils INSTANCE = new InitServiceUtils();
    }

    public static final InitServiceUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ActivitiService getActivitiDemoService() {
        return InitServiceUtils.getInstance().activitiDemoService;
    }

    public ILoginCacheInfo getILoginCacheInfo() {
        return InitServiceUtils.getInstance().iLoginCacheInfo;
    }

    @PostConstruct
    public void init() {
        InitServiceUtils.getInstance().activitiDemoService = this.activitiDemoService;
        InitServiceUtils.getInstance().iLoginCacheInfo = this.iLoginCacheInfo;
    }

}
