package cn.mw.monitor.alert.service.manager;

import cn.mw.monitor.service.alert.dto.AlertHuaXingLevelENEnum;
import cn.mw.monitor.service.alert.dto.AlertHuaXingLevelEnum;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import org.springframework.stereotype.Service;

@Service
public class HuaXingLevel implements  ObtainAlertLevel{

    @Override
    public void getAlertLevel() {
        for (AlertHuaXingLevelEnum levelEnum : AlertHuaXingLevelEnum.values()){
            MWAlertLevelParam.alertLevelMap.put(levelEnum.getCode(),levelEnum.getName());
            MWAlertLevelParam.severities.add(Integer.parseInt(levelEnum.getCode()));
        }
        for(AlertHuaXingLevelENEnum levelENEnum : AlertHuaXingLevelENEnum.values()){
            MWAlertLevelParam.actionAlertLevelMap.put(levelENEnum.getNameEN(),levelENEnum.getName());
        }
    }

}
