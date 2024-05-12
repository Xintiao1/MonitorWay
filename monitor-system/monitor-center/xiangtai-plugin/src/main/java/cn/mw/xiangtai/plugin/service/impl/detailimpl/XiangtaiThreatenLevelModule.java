package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.AlertDto;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.enums.XiangtaiAlertLevelEnum;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author gengjb
 * @description 威胁等级分析
 * @date 2023/10/19 14:47
 */

@Service
@Slf4j
public class XiangtaiThreatenLevelModule implements XiangtaiVisualizedModule {

    @Autowired
    private SyslogAlertService syslogAlertService;

    private final String ALERT_LEVEL = "alertLevel";

    private final String ALERT_COUNT = "count";

    @Override
    public int[] getType() {
        return new int[]{115};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<AlertDto> alertDtos = new ArrayList<>();
            List<Map<String, Integer>> threatLevel = syslogAlertService.getThreatLevel();
            if(CollectionUtils.isEmpty(threatLevel)){return alertDtos;}
            for (Map<String, Integer> map : threatLevel) {
                AlertDto alertDto = new AlertDto();
                Integer level = map.get(ALERT_LEVEL);
                Integer alertCount = map.get(ALERT_COUNT);
                String alertLevelName = XiangtaiAlertLevelEnum.getByAlertLevel(level).getAlertLevelName();
                alertDto.setAlertLevel(level);
                alertDto.setAlertLevelName(alertLevelName);
                alertDto.setAlertCount(alertCount);
                alertDtos.add(alertDto);
            }
            //数据排序
            alertLevelAscSort(alertDtos);
            return alertDtos;
        }catch (Throwable e){
            log.error("XiangTaiAttackSituation{} getData() ERROR::",e);
            return null;
        }
    }


    private void alertLevelAscSort( List<AlertDto> alertDtos){
        Collections.sort(alertDtos, new Comparator<AlertDto>() {
            @Override
            public int compare(AlertDto o1, AlertDto o2) {
                if(o1.getAlertLevel() > o2.getAlertLevel()){
                    return 1;
                }
                if(o1.getAlertLevel() < o2.getAlertLevel()){
                    return -1;
                }
                return 0;
            }
        });
    }
}
