package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleRankingDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
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
 * @ClassName MwVisualizedModuleRanking
 * @Description 数据排行
 * @Author gengjb
 * @Date 2023/4/17 21:08
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleRanking implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Value("${deployment.environment}")
    private String deploymentType;

    private final String SHANYING = "shanying";

    @Override
    public int[] getType() {
        return new int[]{55,56,60,61,62,69,90,91};
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
            //获取监控信息并返回
            List<MwVisualizedModuleRankingDto> rankingDtos = getMonitorNews(assetsIds, moduleParam.getItemName());
            if(moduleParam.getTopCount() != null){
                if(CollectionUtils.isEmpty(rankingDtos) || rankingDtos.size() <= moduleParam.getTopCount()){return rankingDtos;}
                return rankingDtos.subList(0, moduleParam.getTopCount());
            }
            if(CollectionUtils.isEmpty(rankingDtos) || rankingDtos.size() <= 10){return rankingDtos;}
            return rankingDtos.subList(0, 10);
        }catch (Throwable e){
            log.error("可视化组件区查询排行失败",e);
            return null;
        }

    }

    /**
     * 获取监控信息
     */
    private List<MwVisualizedModuleRankingDto> getMonitorNews(List<String> assetsIds,String itemName){
        List<MwVisualizedModuleRankingDto> moduleRankingDtos = new ArrayList<>();
        //查询缓存数据
        List<MwVisualizedCacheDto> cacheDtos = groupGetInfo(assetsIds,itemName);
        if(CollectionUtils.isEmpty(cacheDtos)){return moduleRankingDtos;}
        for (MwVisualizedCacheDto cacheDto : cacheDtos) {
            String name = cacheDto.getItemName();
            if(StringUtils.isBlank(name) || !name.contains("]")){continue;}
            cacheDto.setName(name.substring(name.indexOf("[")+1,name.indexOf("]")));
            cacheDto.setItemName(name.split("]")[1]);
        }
        Map<String, List<MwVisualizedCacheDto>> dtoMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()+item.getAssetsId()));
        for (String idAndName : dtoMap.keySet()) {
            List<MwVisualizedCacheDto> dtos = dtoMap.get(idAndName);
            if(CollectionUtils.isEmpty(dtos)){continue;}
            //求value的平均值
            double sumValue = dtos.stream().map(MwVisualizedCacheDto::getValue).collect(Collectors.toList()).stream().mapToDouble(item -> Double.parseDouble(item)).reduce((a, b) -> a + b).getAsDouble();
            MwVisualizedCacheDto cacheDto = dtos.get(0);
            double dvalue = new BigDecimal(String.valueOf(sumValue / dtos.size())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            MwVisualizedModuleRankingDto rankingDto = new MwVisualizedModuleRankingDto();
            if (StringUtils.isNotBlank(deploymentType) && SHANYING.equals(deploymentType)) {
                //数据取整
                rankingDto.extractFrom(cacheDto.getAssetsName(),new BigDecimal(dvalue).setScale(0,BigDecimal.ROUND_HALF_UP).toString()+ cacheDto.getUnits(),new BigDecimal(dvalue).setScale(0,BigDecimal.ROUND_HALF_UP).doubleValue());
            }else{
                rankingDto.extractFrom(cacheDto.getAssetsName(),dvalue+ cacheDto.getUnits(),dvalue);
            }
            moduleRankingDtos.add(rankingDto);
        }
        if(CollectionUtils.isEmpty(moduleRankingDtos)){return moduleRankingDtos;}
        //数据排序
        itemSort(moduleRankingDtos);
        return moduleRankingDtos;
    }


    private  List<MwVisualizedCacheDto> groupGetInfo(List<String> assetsIds,String itemName){
        if(CollectionUtils.isEmpty(assetsIds) || assetsIds.size() < 500){
            return visualizedManageDao.selectvisualizedCacheInfo(assetsIds, itemName);
        }
        List<List<String>> lists = Lists.partition(assetsIds, 500);
        List<MwVisualizedCacheDto> cacheDtos = new ArrayList<>();
        for (List<String> list : lists) {
            List<MwVisualizedCacheDto> dtos = visualizedManageDao.selectvisualizedCacheInfo(list, itemName);
            if(CollectionUtils.isEmpty(dtos)){continue;}
            cacheDtos.addAll(dtos);
        }
        return cacheDtos;
    }

    private void itemSort(List<MwVisualizedModuleRankingDto> moduleRankingDtos){
        Collections.sort(moduleRankingDtos, new Comparator<MwVisualizedModuleRankingDto>() {
            @Override
            public int compare(MwVisualizedModuleRankingDto o1, MwVisualizedModuleRankingDto o2) {
                if(o1.getSortValue() > o2.getSortValue()){
                    return -1;
                }
                if(o1.getSortValue() < o2.getSortValue()){
                    return 1;
                }
                return 0;
            }
        });
    }
}
