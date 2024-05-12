package cn.mw.monitor.alert.service.manager;

import cn.mw.monitor.service.alert.dto.AlertLevelENEnum;
import cn.mw.monitor.service.alert.dto.AlertLevelEnum;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import org.springframework.stereotype.Service;

@Service
public class DefaultLevel implements  ObtainAlertLevel{

    @Override
    public void getAlertLevel() {
        for (AlertLevelEnum levelEnum : AlertLevelEnum.values()){
            MWAlertLevelParam.alertLevelMap.put(levelEnum.getCode(),levelEnum.getName());
        }
        for(AlertLevelENEnum levelENEnum : AlertLevelENEnum.values()){
            MWAlertLevelParam.actionAlertLevelMap.put(levelENEnum.getNameEN(),levelENEnum.getName());
        }
    }

}
