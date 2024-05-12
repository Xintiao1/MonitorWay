package cn.mw.monitor.alert.service.manager;

import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AlertLevelENEnum;
import cn.mw.monitor.service.alert.dto.AlertLevelEnum;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import org.springframework.stereotype.Service;

@Service
public class XiZangDianWangLevel implements  ObtainAlertLevel{

    @Override
    public void getAlertLevel() {
        for (AlertLevelEnum levelEnum : AlertLevelEnum.values()){
            if(levelEnum.getCode().equals("2") || levelEnum.getCode().equals("4")){
                MWAlertLevelParam.alertLevelMap.put(levelEnum.getCode(),levelEnum.getName());
                MWAlertLevelParam.severities.add(Integer.parseInt(levelEnum.getCode()));
            }
        }
        for(AlertLevelENEnum levelENEnum : AlertLevelENEnum.values()){
            if(levelENEnum.getName().equals(AlertEnum.Warning.toString()) || levelENEnum.getName().equals(AlertEnum.ERROR.toString())){
                MWAlertLevelParam.actionAlertLevelMap.put(levelENEnum.getNameEN(),levelENEnum.getName());
            }
        }

    }
}
