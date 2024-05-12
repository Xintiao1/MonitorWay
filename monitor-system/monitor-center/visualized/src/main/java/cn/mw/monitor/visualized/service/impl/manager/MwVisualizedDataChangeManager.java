package cn.mw.monitor.visualized.service.impl.manager;

import cn.mw.monitor.visualized.service.MwVisualizedDataChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MwVisualizedDataChangeService
 * @Author gengjb
 * @Date 2022/4/26 10:08
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwVisualizedDataChangeManager {

    Map<Integer, MwVisualizedDataChange> datastarties = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init(){
        Map<String, MwVisualizedDataChange> beans = applicationContext.getBeansOfType(MwVisualizedDataChange.class);
        for (MwVisualizedDataChange bean : beans.values()) {
            int[] type = bean.getType();
            for (int i : type) {
                datastarties.put(i,bean);
            }
        }
    }

    public Object getDataByType(Integer type,Object data){
        return datastarties.get(type).getData(data);
    }
}
