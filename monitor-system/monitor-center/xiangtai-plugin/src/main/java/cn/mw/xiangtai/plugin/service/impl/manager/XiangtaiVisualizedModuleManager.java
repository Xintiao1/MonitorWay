package cn.mw.xiangtai.plugin.service.impl.manager;

import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gengjb
 * @description 祥泰可视化组件管理类
 * @date 2023/10/17 9:34
 */
@Component
@Slf4j
public class XiangtaiVisualizedModuleManager {

    Map<Integer, XiangtaiVisualizedModule> xiangtaiModuleMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init(){
        Map<String, XiangtaiVisualizedModule> beans = applicationContext.getBeansOfType(XiangtaiVisualizedModule.class);
        for (XiangtaiVisualizedModule bean : beans.values()) {
            int[] type = bean.getType();
            for (int i : type) {
                xiangtaiModuleMap.put(i,bean);
            }
        }
    }

    public Object getDataByType(Integer type, XiangtaiVisualizedParam visualizedParam){
        return xiangtaiModuleMap.get(type).getData(visualizedParam);
    }
}
