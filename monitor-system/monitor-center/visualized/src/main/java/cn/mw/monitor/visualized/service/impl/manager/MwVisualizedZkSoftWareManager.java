package cn.mw.monitor.visualized.service.impl.manager;

import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName
 * @Description MwVisualizedZkSoftWareManager
 * @Author gengjb
 * @Date 2023/3/15 16:17
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwVisualizedZkSoftWareManager {

    Map<Integer, MwVisualizedZkSoftWare> zkSoftWare = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init(){
        Map<String, MwVisualizedZkSoftWare> beans = applicationContext.getBeansOfType(MwVisualizedZkSoftWare.class);
        for (MwVisualizedZkSoftWare bean : beans.values()) {
            int[] type = bean.getType();
            for (int i : type) {
                zkSoftWare.put(i,bean);
            }
        }
    }

    public Object getDataByType(Integer type){
        return zkSoftWare.get(type).getData();
    }
}
