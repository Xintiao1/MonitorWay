package cn.mw.monitor.model.param.prometheusContainer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MwPrometheusSelectDropChangeManage {
    private Map<String, MwPrometheusSelectDropChange> map = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, MwPrometheusSelectDropChange> beans = applicationContext.getBeansOfType(MwPrometheusSelectDropChange.class);
        for (MwPrometheusSelectDropChange bean : beans.values()) {
            map.put(bean.getType(), bean);
        }
    }

    public Object getDataByType(String type, Object param) throws Exception {
        Object info = map.get(type).getData(param);
        return info;
    }
}
