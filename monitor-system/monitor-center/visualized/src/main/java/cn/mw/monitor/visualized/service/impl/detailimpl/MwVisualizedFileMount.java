package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleRankingDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
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
 * @author gengjb
 * @description 文件挂载率统计数据
 * @date 2023/8/18 9:27
 */
@Service
@Slf4j
public class MwVisualizedFileMount implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Value("${visualized.isTopN}")
    private boolean isTopN;

    @Override
    public int[] getType() {
        return new int[]{96};
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
            //文件挂载数据统计
            return handlerFileMountInfo(cacheDtoList);
        }catch (Throwable e){
            log.error("MwVisualizedFileMount{} getData::",e);
            return null;
        }
    }

    private List<MwVisualizedModuleRankingDto> handlerFileMountInfo(List<MwVisualizedCacheDto> cacheDtoList){
        List<MwVisualizedModuleRankingDto> rankingDtos = new ArrayList<>();
        //按照资产+分区名称分组
        Map<String, List<MwVisualizedCacheDto>> fileMonutCacheMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getAssetsId() + item.getName()));
        if(fileMonutCacheMap == null && fileMonutCacheMap.isEmpty()){return rankingDtos;}
        for (String name : fileMonutCacheMap.keySet()) {
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = fileMonutCacheMap.get(name);
            MwVisualizedModuleRankingDto rankingDto = new MwVisualizedModuleRankingDto();
            for (MwVisualizedCacheDto mwVisualizedCacheDto : mwVisualizedCacheDtos) {
                rankingDto.setName(mwVisualizedCacheDto.getAssetsName()+"("+mwVisualizedCacheDto.getName()+")");
                rankingDto.setValue(mwVisualizedCacheDto.getValue()+mwVisualizedCacheDto.getUnits());
                rankingDto.setSortValue(Double.parseDouble(mwVisualizedCacheDto.getValue()));
            }
            rankingDtos.add(rankingDto);
        }
        if(CollectionUtils.isEmpty(rankingDtos)){return rankingDtos;}
        //数据排序
        dataSort(rankingDtos);
        if(isTopN && rankingDtos.size() > 10){
            return rankingDtos.subList(0,10);
        }
        return rankingDtos;
    }

    private void dataSort(List<MwVisualizedModuleRankingDto> moduleRankingDtos){
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
