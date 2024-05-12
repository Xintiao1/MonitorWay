package cn.mw.monitor.visualized.service.impl.manager;

import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import cn.mw.monitor.visualized.service.MwVisualizedDataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedDataSourceManager
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/26 11:09
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwVisualizedDataSourceManager {

    Map<Integer, MwVisualizedDataSourceService> dataSourceMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init(){
        Map<String, MwVisualizedDataSourceService> beans = applicationContext.getBeansOfType(MwVisualizedDataSourceService.class);
        for (MwVisualizedDataSourceService bean : beans.values()) {
            dataSourceMap.put(bean.getDataSource(),bean);
        }
    }

    public Object getDataByType(Integer type, MwVisualizedIndexQueryParam data){
        return dataSourceMap.get(type).getData(data);
    }
}
