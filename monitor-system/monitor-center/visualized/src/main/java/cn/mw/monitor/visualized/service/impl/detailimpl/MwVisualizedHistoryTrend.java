package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheHistoryDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 获取历史数据趋势信息
 * @Author gengjb
 * @Date 2023/5/21 0:37
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedHistoryTrend implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;
    
    private final String  POOL_CAPACITY_USED = "POOL_Capacity used";

    @Override
    public int[] getType() {
        return new int[]{78};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                moduleParam.setAssetsId(moduleParam.getAssetsIds().get(0));
            }
            //根据资产ID与监控项名称查询历史缓存数据
            List<MwVisualizedCacheHistoryDto> cacheHistoryDtos = visualizedManageDao.selectVisualizedCacheHistoryMonitorInfo(moduleParam.getAssetsId(), moduleParam.getItemNames());
            if(CollectionUtils.isEmpty(cacheHistoryDtos)){return null;}
            //按照itemName分组
            Map<String, List<MwVisualizedCacheHistoryDto>> historyDtoMap = cacheHistoryDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            Map<String, List<MwVisualizedCacheHistoryDto>> listMap = new HashMap<>();
            //做数据排序
            for (String itemName : historyDtoMap.keySet()) {
                List<MwVisualizedCacheHistoryDto> historyDtos = historyDtoMap.get(itemName);
                if(CollectionUtils.isEmpty(historyDtos)){continue;}
                dateFilter(historyDtos);
                convertdate(historyDtos);
                //排序
                dataSort(historyDtos);
                if(itemName.contains("[") || itemName.contains("]")){
                    itemName = itemName.substring(itemName.indexOf("[") + 1, itemName.indexOf("]"));
                }
                listMap.put(itemName,historyDtos);
            }
            return listMap;
        }catch (Throwable e){
            log.error("MwVisualizedExternalAssetsInfo{} getData::",e);
            return null;
        }
    }

    /**
     * 转换时间
     * @param historyDtos
     */
    private void convertdate(List<MwVisualizedCacheHistoryDto> historyDtos){
        SimpleDateFormat format = new SimpleDateFormat("HH");
        for (MwVisualizedCacheHistoryDto historyDto : historyDtos) {
            String clock = historyDto.getClock();
            if(StringUtils.isBlank(clock)){continue;}
            Date date = new Date();
            date.setTime(Long.parseLong(clock) * 1000);
            historyDto.setValueDate(date);
            historyDto.setTime(format.format(date));
        }
    }

    /**
     * 日期过滤
     * @param historyDtos
     */
    private void dateFilter(List<MwVisualizedCacheHistoryDto> historyDtos){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Iterator<MwVisualizedCacheHistoryDto> iterator = historyDtos.iterator();
        while (iterator.hasNext()){
            MwVisualizedCacheHistoryDto historyDto = iterator.next();
            String clock = historyDto.getClock();
            if(StringUtils.isBlank(clock)){continue;}
            Date date = new Date();
            date.setTime(Long.parseLong(clock) * 1000);
            if(!format.format(new Date()).contains(format.format(date))){
                iterator.remove();
            }
        }
    }

    /**
     * 告警按时间排序
     */
    private void dataSort(List<MwVisualizedCacheHistoryDto> cacheHistoryDtos){
        Collections.sort(cacheHistoryDtos, new Comparator<MwVisualizedCacheHistoryDto>() {
            @Override
            public int compare(MwVisualizedCacheHistoryDto o1, MwVisualizedCacheHistoryDto o2) {
                if(o1.getValueDate().compareTo(o2.getValueDate()) > 0){
                    return 1;
                }
                if(o1.getValueDate().compareTo(o2.getValueDate()) < 0){
                    return -1;
                }
                return 0;
            }
        });
    }
}
