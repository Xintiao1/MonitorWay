package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedContainerDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 获取容器告警信息
 * @date 2023/9/19 9:57
 */
@Service
@Slf4j
public class MwVisualizedContainerAlert implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Override
    public int[] getType() {
        return new int[]{120};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            String serverName = moduleParam.getServerName();
            List<String> days = MwVisualizedDateUtil.getDays(moduleParam.getAlertTrendDays());
            //根据日期和名称获取缓存数据
            List<MwVisualizedContainerDto> containerDtos = visualizedManageDao.getVisualizedCacheContaineAlertInfo(serverName, days);
            if(CollectionUtils.isEmpty(containerDtos)){return null;}
            Map<String, List<MwVisualizedContainerDto>> listMap = containerDtos.stream().collect(Collectors.groupingBy(item -> item.getAlertLevel()));
            ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
            Map<String, List<MwVisualizedContainerDto>> levelMap = new HashMap<>();
            for (Map.Entry<String, List<MwVisualizedContainerDto>> entry : listMap.entrySet()) {
                String key = entry.getKey();
                List<MwVisualizedContainerDto> value = entry.getValue();
                //按日期排序
                alertSort(containerDtos);
                for (String code : alertLevelMap.keySet()) {
                    String name = alertLevelMap.get(code);
                    if(name.equals(key)){
                        levelMap.put(code+"_"+key,value);
                        break;
                    }
                }
            }
            Map<String, List<MwVisualizedContainerDto>> result = new LinkedHashMap<>();
            levelMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
            return result;
        }catch (Throwable e){
            log.error("MwVisualizedContainerAlert{} getData() ERROR",e);
            return null;
        }
    }



    private void alertSort( List<MwVisualizedContainerDto> containerDtos){
        Collections.sort(containerDtos, new Comparator<MwVisualizedContainerDto>() {
            @Override
            public int compare(MwVisualizedContainerDto o1, MwVisualizedContainerDto o2) {
                if(DateUtils.parse(o1.getAlertDate()).compareTo(DateUtils.parse(o2.getAlertDate())) > 0){
                    return 1;
                }
                if(DateUtils.parse(o1.getAlertDate()).compareTo(DateUtils.parse(o2.getAlertDate())) < 0){
                    return -1;
                }
                return 0;
            }
        });
    }
}
