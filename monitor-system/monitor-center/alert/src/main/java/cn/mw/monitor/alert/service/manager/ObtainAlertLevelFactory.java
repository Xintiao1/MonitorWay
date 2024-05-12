package cn.mw.monitor.alert.service.manager;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ObtainAlertLevelFactory {

    private static Map<String, ObtainAlertLevel> map = new HashMap<>();

    static {
        map.put(AlertEnum.Default.toString(), new DefaultLevel());
        map.put(AlertEnum.HUAXING.toString(), new HuaXingLevel());
        map.put(AlertEnum.XIZANGDIANWANG.toString(), new XiZangDianWangLevel());
        map.put(AlertEnum.GuangZhouBank.toString(), new DefaultLevel());
        map.put(AlertEnum.SHANYING.toString(), new DefaultLevel());
        map.put(AlertEnum.WANGKE.toString(), new DefaultLevel());
        map.put(AlertEnum.CHENGDUGUANWEI.toString(), new DefaultLevel());
        map.put(AlertEnum.SHENGRENYI.toString(), new DefaultLevel());
    }

    public static ObtainAlertLevel getObtainAlertLevel(String projectName){
        return map.get(projectName);
    }

}
