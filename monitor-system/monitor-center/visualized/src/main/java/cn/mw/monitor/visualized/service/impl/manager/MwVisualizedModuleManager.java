package cn.mw.monitor.visualized.service.impl.manager;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParamList;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedModuleManager
 * @Description 组件区管理
 * @Author gengjb
 * @Date 2023/4/17 10:05
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwVisualizedModuleManager {


    Map<Integer, MwVisualizedModule> zkSoftWare = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init(){
        Map<String, MwVisualizedModule> beans = applicationContext.getBeansOfType(MwVisualizedModule.class);
        for (MwVisualizedModule bean : beans.values()) {
            int[] type = bean.getType();
            for (int i : type) {
                zkSoftWare.put(i,bean);
            }
        }
    }

    public Object getDataByType(Integer type,Object data){
        return zkSoftWare.get(type).getData(data);
    }
}
