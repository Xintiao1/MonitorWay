package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedBatchHistoryDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheHistoryDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 查询历史记录，批量
 * @date 2023/8/18 10:36
 */
@Service
@Slf4j
public class MwVisualizedBatchHistoryTrend implements MwVisualizedModule {


    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Override
    public int[] getType() {
        return new int[]{84};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            log.info("MwVisualizedFileMount{} moduleParam:"+moduleParam.getAssetsIds());
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            log.info("MwVisualizedFileMount{} assetsIds:"+assetsIds);
            if(CollectionUtils.isNotEmpty(tangibleassetsDTOS)){
                List<String> ids = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
                Iterator<String> iterator = assetsIds.iterator();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    if(!ids.contains(next)){
                        iterator.remove();
                    }
                }
            }
            if(StringUtils.isBlank(moduleParam.getAssetsId()) && CollectionUtils.isEmpty(moduleParam.getAssetsIds())){
                assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            }
            if((StringUtils.isNotBlank(moduleParam.getAssetsId()) || CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())) && CollectionUtils.isEmpty(assetsIds)){
                return null;
            }
            List<MwVisualizedCacheHistoryDto> cacheHistoryDtos = new ArrayList<>();
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            //查询数据库数据
            for (List<String> ids : partition) {
                cacheHistoryDtos.addAll(visualizedManageDao.selectVisualizedCacheHistoryBatch(ids, moduleParam.getItemNames()));
            }
            if(CollectionUtils.isEmpty(cacheHistoryDtos)){return null;}
            for (MwVisualizedCacheHistoryDto cacheHistoryDto : cacheHistoryDtos) {
                String itemName = cacheHistoryDto.getItemName();
                if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
                cacheHistoryDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
                cacheHistoryDto.setItemName(itemName.split("]")[1]);
            }
            return handlerTrendHistoryInfo(cacheHistoryDtos,moduleParam.getUnits());
        }catch (Throwable e){
            log.error("MwVisualizedExternalAssetsInfo{} getData::",e);
            return null;
        }
    }


    private List<MwVisualizedBatchHistoryDto> handlerTrendHistoryInfo(List<MwVisualizedCacheHistoryDto> cacheHistoryDtos,String units){
        //将单位换成统一单位
        for (MwVisualizedCacheHistoryDto cacheHistoryDto : cacheHistoryDtos) {
            Map<String, String> convertedValue = UnitsUtil.getValueMap(cacheHistoryDto.getAvgValue(),units,cacheHistoryDto.getUnits());
            cacheHistoryDto.setAvgValue(convertedValue.get("value"));
            cacheHistoryDto.setUnits(convertedValue.get("units"));
        }
        List<MwVisualizedBatchHistoryDto> batchHistoryDtos = new ArrayList<>();
        Map<String, List<MwVisualizedCacheHistoryDto>> historyMap = cacheHistoryDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId() + item.getItemName()));
        if(historyMap == null || historyMap.isEmpty()){return batchHistoryDtos;}
        for (String name : historyMap.keySet()) {
            List<MwVisualizedCacheHistoryDto> historyDtos = historyMap.get(name);
            //按照时间分组
            Map<String, List<MwVisualizedCacheHistoryDto>> clockMap = historyDtos.stream().collect(Collectors.groupingBy(item -> item.getClock()));
            if(clockMap == null || clockMap.isEmpty()){continue;}
            MwVisualizedBatchHistoryDto batchHistoryDto = new MwVisualizedBatchHistoryDto();
            List<MwVisualizedCacheHistoryDto> newCacheHistoryDtos = new ArrayList<>();
            for (String clock : clockMap.keySet()) {
                MwVisualizedCacheHistoryDto newHistoryDto = new MwVisualizedCacheHistoryDto();
                List<MwVisualizedCacheHistoryDto> cacheHistoryDtoList = clockMap.get(clock);
                //数据汇总
                newHistoryDto = cacheHistoryDtoList.get(0);
                double sum = cacheHistoryDtoList.stream().mapToDouble(cacheDto -> Double.valueOf(cacheDto.getAvgValue())).sum();
                newHistoryDto.setAvgValue(new BigDecimal(sum).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                newCacheHistoryDtos.add(newHistoryDto);
            }
            batchHistoryDto.setName(historyDtos.get(0).getAssetsName()+historyDtos.get(0).getItemName());
            dateFilter(newCacheHistoryDtos);
            //按照时间排序
            convertdate(newCacheHistoryDtos);
            dataSort(newCacheHistoryDtos);
            batchHistoryDto.setHistoryDtos(newCacheHistoryDtos);
            batchHistoryDtos.add(batchHistoryDto);
        }
        return batchHistoryDtos;
    }

    /**
     * 转换时间
     * @param historyDtos
     */
    private void convertdate(List<MwVisualizedCacheHistoryDto> historyDtos){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
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
