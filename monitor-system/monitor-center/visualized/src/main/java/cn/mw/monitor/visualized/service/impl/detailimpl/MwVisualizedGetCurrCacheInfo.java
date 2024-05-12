package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedFoldLineDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 获取当前缓存信息
 * @Author gengjb
 * @Date 2023/5/21 1:01
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedGetCurrCacheInfo implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Value("${visualized.isTopN}")
    private boolean isTopN;

    @Value("${deployment.environment}")
    private String deploymentType;

    private final String SHANYING = "shanying";

    @Override
    public int[] getType() {
        return new int[]{77,79,80,81,82,83,85,87,86};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            log.info("可视化查询互联网"+moduleParam.getAssetsIds());
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            log.info("可视化查询互联网"+assetsIds);
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
            List<MwVisualizedCacheDto> cacheDtoList = new ArrayList<>();
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            for (List<String> ids : partition) {
                cacheDtoList.addAll(visualizedManageDao.selectvisualizedCacheInfos(ids, moduleParam.getItemNames()));
            }
            if(CollectionUtils.isEmpty(cacheDtoList)){return null;}
            for (MwVisualizedCacheDto cacheDto : cacheDtoList) {
                String itemName = cacheDto.getItemName();
                if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
                cacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
                cacheDto.setItemName(itemName.split("]")[1]);
            }
            List<MwVisualizedCacheDto> cacheDtos = groupSetValue(cacheDtoList,moduleParam.getChartType(),moduleParam.getIsLinkFlow());
            if(CollectionUtils.isEmpty(cacheDtos)){return null;}
            for (MwVisualizedCacheDto cacheDto : cacheDtos) {
                if (StringUtils.isNotBlank(deploymentType) && SHANYING.equals(deploymentType) && StringUtils.isNotBlank(cacheDto.getValue()) && MwVisualizedUtil.checkStrIsNumber(cacheDto.getValue())) {
                    //数据取整
                    cacheDto.setValue(new BigDecimal(cacheDto.getValue()).setScale(0,BigDecimal.ROUND_HALF_UP).toString());
                }
            }
            //按照value数据排序
            dataSort(cacheDtos);
            if(moduleParam.getIsLinkFlow() != null && moduleParam.getIsLinkFlow()){
                //流量数据处理
                return linkHandler(cacheDtos);
            }
            if(moduleParam.getIsMultiLine() != null && moduleParam.getIsMultiLine()){
                return handleFoldLineInfo(cacheDtos);
            }
            if(moduleParam.getChartType() == 86){
                if(CollectionUtils.isEmpty(cacheDtos) || cacheDtos.size() <= 5){return cacheDtos;}
                return cacheDtos.subList(0, 5);
            }
            if(CollectionUtils.isEmpty(cacheDtos) || cacheDtos.size() <= 10){return cacheDtos;}
            return cacheDtos.subList(0, 10);
        }catch (Throwable e){
            log.error("MwVisualizedGetCurrCacheInfo{} getData::",e);
            return null;
        }
    }

    /**
     * 处理折线图数据
     * @param cacheDtos
     */
    private List<MwVisualizedFoldLineDto> handleFoldLineInfo(List<MwVisualizedCacheDto> cacheDtos){
        List<MwVisualizedFoldLineDto> foldLineDtos = new ArrayList<>();
        Map<String, List<MwVisualizedCacheDto>> listMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
        if(listMap == null || listMap.isEmpty()){return foldLineDtos;}
        for (String name : listMap.keySet()) {
            List<MwVisualizedCacheDto> value = listMap.get(name);
            MwVisualizedFoldLineDto lineDto = new MwVisualizedFoldLineDto();
            //判断是否取值topN
            if(isTopN && value.size() > 10){
                lineDto.setValues(value.subList(0,10));
            }else{
                lineDto.setValues(value);
            }
            lineDto.setName(name);
            foldLineDtos.add(lineDto);
        }
        return foldLineDtos;
    }

    /**
     * 分组设置值
     * @param cacheDtoList
     */
    private List<MwVisualizedCacheDto> groupSetValue(List<MwVisualizedCacheDto> cacheDtoList,Integer charType,Boolean isLinkFlow){
        if(charType == 77 || charType == 86 || (isLinkFlow != null && isLinkFlow)){return cacheDtoList;}
        List<MwVisualizedCacheDto> cacheDtos = new ArrayList<>();
        Map<String, List<MwVisualizedCacheDto>> dtoMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getItemName()+item.getAssetsId()));
        for (String idAndName : dtoMap.keySet()) {
            List<MwVisualizedCacheDto> dtos = dtoMap.get(idAndName);
            if(CollectionUtils.isEmpty(dtos)){continue;}
            //求value的平均值
            double value = dtos.stream().map(MwVisualizedCacheDto::getValue).collect(Collectors.toList()).stream().mapToDouble(item -> Double.parseDouble(item)).reduce((a, b) -> a + b).getAsDouble();
            MwVisualizedCacheDto cacheDto = dtos.get(0);
            cacheDto.setValue(new BigDecimal(String.valueOf(value/dtos.size())).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
            cacheDtos.add(cacheDto);
        }
        return cacheDtos;
    }

    private void dataSort(List<MwVisualizedCacheDto> cacheDtoList){
        Collections.sort(cacheDtoList, new Comparator<MwVisualizedCacheDto>() {
            @Override
            public int compare(MwVisualizedCacheDto o1, MwVisualizedCacheDto o2) {
                if(Double.parseDouble(o1.getValue()) >Double.parseDouble(o2.getValue())){
                    return -1;
                }
                if(Double.parseDouble(o1.getValue()) < Double.parseDouble(o2.getValue())){
                    return 1;
                }
                return 0;
            }
        });
    }

    private static final String linkUnits = "bps";

    /**
     * 流量需要统计
     * @param cacheDtos
     */
    private Map<String,List<MwVisualizedCacheDto>> linkHandler(List<MwVisualizedCacheDto> cacheDtos){
        Map<String,List<MwVisualizedCacheDto>> cacheDtoMap = new HashMap<>();
        //按照资产分组
        Map<String, List<MwVisualizedCacheDto>> assetsMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId()));
        for (String assetsId : assetsMap.keySet()) {
            List<MwVisualizedCacheDto> dtos = assetsMap.get(assetsId);
            if(CollectionUtils.isEmpty(dtos)){continue;}
            //按监控项名称分组
            Map<String, List<MwVisualizedCacheDto>> listMap = dtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            for (String itemName : listMap.keySet()) {
                List<MwVisualizedCacheDto> visualizedCacheDtos = listMap.get(itemName);
                if(CollectionUtils.isEmpty(visualizedCacheDtos)){continue;}
                //获取所有value的总数
                visualizedCacheDtos.forEach(item->{
                    Map<String, String> valueMap = UnitsUtil.getValueMap(item.getValue(), linkUnits, item.getUnits());
                    item.setValue(valueMap.get("value"));
                });
                //计算总数
                double sum = visualizedCacheDtos.stream().map(MwVisualizedCacheDto::getValue).collect(Collectors.toList()).stream().mapToDouble(item -> Double.parseDouble(item)).reduce((a, b) -> a + b).getAsDouble();
                MwVisualizedCacheDto cacheDto = visualizedCacheDtos.get(0);
                //进行单位转换
                Map<String, String> valueMap = UnitsUtil.getValueMap(new BigDecimal(sum).toString(), "Mbps", linkUnits);
                if(valueMap == null || valueMap.isEmpty()){
                    cacheDto.setValue(String.valueOf(sum));
                    cacheDto.setUnits(linkUnits);
                }else{
                    cacheDto.setValue(valueMap.get("value"));
                    cacheDto.setUnits(valueMap.get("units"));
                }
                List<MwVisualizedCacheDto> mapDtos = cacheDtoMap.get(itemName);
                if(mapDtos == null){
                    mapDtos = new ArrayList<>();
                    mapDtos.add(cacheDto);
                    cacheDtoMap.put(itemName,mapDtos);
                    continue;
                }
                mapDtos.add(cacheDto);
            }
        }
        return cacheDtoMap;
    }
}
