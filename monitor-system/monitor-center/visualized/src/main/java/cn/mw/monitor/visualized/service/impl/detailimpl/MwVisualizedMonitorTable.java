package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheHistoryDto;
import cn.mw.monitor.visualized.dto.VisualizedItemTableDto;
import cn.mw.monitor.visualized.enums.VisualizedDateTypeEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.VisualizedDateTypeUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 监控表格
 * @date 2023/12/25 10:44
 */
@Service
@Slf4j
public class MwVisualizedMonitorTable implements MwVisualizedModule {


    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;
    private final String OUT = "OUT";

    private final String IN = "IN";


    @Override
    public int[] getType() {
        return new int[]{130};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
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
            //获取日期
            VisualizedDateTypeEnum dateTypeEnumByType = VisualizedDateTypeEnum.getDateTypeEnumByType(moduleParam.getDateType());
            List<String> times = VisualizedDateTypeUtils.getTime(dateTypeEnumByType);
            List<MwVisualizedCacheHistoryDto> cacheDtoList = new ArrayList<>();
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            for (List<String> ids : partition) {
                cacheDtoList.addAll(visualizedManageDao.selectVisualizedDayData(ids, moduleParam.getItemNames(),times.get(0),times.get(1)));
            }
            if(CollectionUtils.isEmpty(cacheDtoList)){return null;}
            for (MwVisualizedCacheHistoryDto cacheDto : cacheDtoList) {
                String itemName = cacheDto.getItemName();
                if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
                cacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
                cacheDto.setItemName(itemName.split("]")[1]);
            }
            Map<String, MwTangibleassetsDTO> assetsMap = tangibleassetsDTOS.stream()
                    .collect(Collectors.toMap(MwTangibleassetsDTO::getId, obj -> obj));
            //数据处理
            List<VisualizedItemTableDto> itemTableDtos = handlerData(cacheDtoList, moduleParam.getItemNames(), moduleParam.getFilterMaxValue(), assetsMap);
            return itemTableDtos;
        }catch (Throwable e){
            log.error("MwVisualizedGetCurrCacheInfo{} getData::",e);
            return null;
        }
    }

    /**
     * 数据处理
     * @param cacheDtoList
     * @param itemNames
     * @param filterMaxValue
     * @param assetsMap
     * @return
     */
    private List<VisualizedItemTableDto> handlerData(List<MwVisualizedCacheHistoryDto> cacheDtoList,List<String> itemNames,String filterMaxValue,
                             Map<String, MwTangibleassetsDTO> assetsMap){
        if(itemNames.get(0).contains(VisualizedConstant.INTERFACE)){
            return handlerInterfaceData(cacheDtoList,filterMaxValue,assetsMap);
        }
        Map<String, List<MwVisualizedCacheHistoryDto>> listMap = new HashMap<>();
        List<VisualizedItemTableDto> itemTableDtos = new ArrayList<>();
        if(!itemNames.get(0).contains(VisualizedConstant.DISK)){
            //按照资产+监控项进行数据分组
            listMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getAssetsId() + item.getItemName()));
        }else{
            listMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getAssetsId() + item.getName()+item.getItemName()));
        }
        for (Map.Entry<String, List<MwVisualizedCacheHistoryDto>> entry : listMap.entrySet()) {
            List<MwVisualizedCacheHistoryDto> cacheDtos = entry.getValue();
            //取value最大的值
            MwVisualizedCacheHistoryDto cacheDto = cacheDtos.stream().max(Comparator.comparing(obj -> Double.parseDouble(obj.getAvgValue()))).get();
            //判断该对象是否符合条件
            if(Double.parseDouble(cacheDto.getAvgValue()) >= Double.parseDouble(filterMaxValue)){
                VisualizedItemTableDto itemTableDto = new VisualizedItemTableDto();
                MwTangibleassetsDTO mwTangibleassetsDTO = assetsMap.get(cacheDto.getAssetsId());
                itemTableDto.extractFrom(cacheDto,mwTangibleassetsDTO);
                itemTableDtos.add(itemTableDto);
            }
        }
        return itemTableDtos;
    }



    /**
     * 接口数据处理
     */
    private List<VisualizedItemTableDto> handlerInterfaceData(List<MwVisualizedCacheHistoryDto> cacheDtoList,String filterMaxValue,
                                      Map<String, MwTangibleassetsDTO> assetsMap){
        List<VisualizedItemTableDto> itemTableDtos = new ArrayList<>();
        Map<String, List<MwVisualizedCacheHistoryDto>> listMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getAssetsId() + item.getName()));
        for (Map.Entry<String, List<MwVisualizedCacheHistoryDto>> entry : listMap.entrySet()) {
            VisualizedItemTableDto itemTableDto = new VisualizedItemTableDto();
            List<MwVisualizedCacheHistoryDto> value = entry.getValue();
            Map<String, List<MwVisualizedCacheHistoryDto>> itemMap = value.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            for (Map.Entry<String, List<MwVisualizedCacheHistoryDto>> itemEntry : itemMap.entrySet()) {
                List<MwVisualizedCacheHistoryDto> dtos = itemEntry.getValue();
                MwVisualizedCacheHistoryDto cacheDto = dtos.stream().max(Comparator.comparing(obj -> Double.parseDouble(obj.getAvgValue()))).get();
                Date date = new Date();
                date.setTime(Long.parseLong(cacheDto.getClock())*1000);
                if(Double.parseDouble(cacheDto.getAvgValue()) >= Double.parseDouble(filterMaxValue) && itemEntry.getKey().contains(IN)){
                    itemTableDto.setInValue(cacheDto.getAvgValue()+cacheDto.getUnits());
                    itemTableDto.setInTime(DateUtils.formatDateTime(date));
                }
                if(Double.parseDouble(cacheDto.getAvgValue()) >= Double.parseDouble(filterMaxValue) && itemEntry.getKey().contains(OUT)){
                    itemTableDto.setOutValue(cacheDto.getAvgValue()+cacheDto.getUnits());
                    itemTableDto.setOutTime(DateUtils.formatDateTime(date));
                }
            }
            if(StringUtils.isNotBlank(itemTableDto.getInValue()) && StringUtils.isNotBlank(itemTableDto.getOutValue())){
                itemTableDto.extractFrom(value.get(0),assetsMap.get(value.get(0).getAssetsId()));
                itemTableDtos.add(itemTableDto);
            }
        }
        return itemTableDtos;
    }
}
