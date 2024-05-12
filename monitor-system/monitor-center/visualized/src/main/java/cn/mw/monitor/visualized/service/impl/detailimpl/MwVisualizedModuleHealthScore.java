package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.enums.VisualizedScoreItemEnum;
import cn.mw.monitor.visualized.enums.VisualizedScoreTypeEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 健康评分
 * @Author gengjb
 * @Date 2023/4/18 9:56
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleHealthScore implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    private final String GROUP_NODE = ",5,";

    private final String TYPE_NAME = "虚拟化";


    @Override
    public int[] getType() {
        return new int[]{51};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            for (MwTangibleassetsDTO tangibleassetsDTO : tangibleassetsDTOS) {
                if(tangibleassetsDTO.getGroupNodes().contains(GROUP_NODE)){
                    tangibleassetsDTO.setAssetsTypeName(TYPE_NAME);
                }
            }
            List<MwVisualizedScoreProportionDto> visualizedScoreProportion = visualizedManageDao.getVisualizedScoreProportion();
            log.info("MwVisualizedModuleHealthScore{} getData() modelInstanceId::"+moduleParam.getModelInstanceId()+"::visualizedScoreProportion::"+visualizedScoreProportion);
            List<String> itemNames = new ArrayList<>();
            List<String> assetsIds = new ArrayList<>();
            for (MwVisualizedScoreProportionDto mwVisualizedScoreProportionDto : visualizedScoreProportion) {
                filterAssets(tangibleassetsDTOS,mwVisualizedScoreProportionDto);
                String itemName = mwVisualizedScoreProportionDto.getItemName();
                itemNames.addAll(Arrays.asList(itemName.split(",")));
                List<MwTangibleassetsDTO> assetsDtos = mwVisualizedScoreProportionDto.getAssetsDtos();
                if(CollectionUtils.isEmpty(assetsDtos)){continue;}
                assetsIds.addAll(assetsDtos.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList()));
            }
            //获取一天内的数据
            List<MwVisualizedCacheHistoryDto> cacheHistoryDtos = visualizedManageDao.selectVisualizedCacheHistoryBatch(assetsIds, itemNames);
            log.info("MwVisualizedModuleHealthScore{} getData() modelInstanceId::"+moduleParam.getModelInstanceId()+"::cacheHistoryDtos::"+cacheHistoryDtos);
            if(CollectionUtils.isEmpty(cacheHistoryDtos)){return null;}
            setItemNameInfo(cacheHistoryDtos);
            List<MwVisualizedScoreStatusDto> scoreStatusDtos = new ArrayList<>();
            for (MwVisualizedScoreProportionDto proportionDto : visualizedScoreProportion) {
                log.info("MwVisualizedModuleHealthScore{} getData() modelInstanceId::"+moduleParam.getModelInstanceId()+"::proportionDto::"+proportionDto);
                List<MwTangibleassetsDTO> assetsDtos = proportionDto.getAssetsDtos();
                if(CollectionUtils.isEmpty(assetsDtos)){continue;}
                List<String> ids = assetsDtos.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
                List<String> items = Arrays.asList(proportionDto.getItemName().split(","));
                List<MwVisualizedCacheHistoryDto> historyDtos = cacheHistoryDtos.stream().
                        filter(item -> ids.contains(item.getAssetsId()) && items.contains(item.getItemName())).collect(Collectors.toList());
                log.info("MwVisualizedModuleHealthScore{} getData() modelInstanceId::"+moduleParam.getModelInstanceId()+"::historyDtos::"+historyDtos);
                //按照时间分组
                Map<String, List<MwVisualizedCacheHistoryDto>> timeDataMap = historyDtos.stream().collect(Collectors.groupingBy(item -> item.getClock()));
                log.info("MwVisualizedModuleHealthScore{} getData() modelInstanceId::"+moduleParam.getModelInstanceId()+"::timeDataMap::"+timeDataMap);
                List<MwVisualizedScoreStatusDto> statusDtos = handlerData(timeDataMap);
                log.info("MwVisualizedModuleHealthScore{} getData() modelInstanceId::"+moduleParam.getModelInstanceId()+"::statusDtos::"+statusDtos);
                //根据占比计算每个时间段的分数
                computeHealthScorce(statusDtos,proportionDto.getProportion());
                scoreStatusDtos.addAll(statusDtos);
            }
            //数据分组
            MwVisualizedModuleHealthScoreDto realDatas = dataGroupHandler(scoreStatusDtos);
            return realDatas;
        }catch (Throwable e){
            log.error("可视化组件区查询健康评分失败",e);
            return null;
        }
    }

    /**
     * 数据分组处理
     * @param scoreStatusDtos
     */
    private MwVisualizedModuleHealthScoreDto dataGroupHandler(List<MwVisualizedScoreStatusDto> scoreStatusDtos){
        MwVisualizedModuleHealthScoreDto healthScoreDto = new MwVisualizedModuleHealthScoreDto();
        if(CollectionUtils.isEmpty(scoreStatusDtos)){return healthScoreDto;}
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        //按照时间分组
        Map<String, List<MwVisualizedScoreStatusDto>> timeMap = scoreStatusDtos.stream().collect(Collectors.groupingBy(item -> item.getClock()));
        log.info("MwVisualizedModuleHealthScore{} dataGroupHandler() timeMap::"+timeMap);
        List<HealthScoreDetailedDto> detailedDtos = new ArrayList<>();
        //计算每个时间点的总分数
        for (Map.Entry<String, List<MwVisualizedScoreStatusDto>> entry : timeMap.entrySet()) {
            String clock = entry.getKey();
            if(!DateUtils.formatDate(new Date()).equals(DateUtils.formatDate(new Date(Long.parseLong(clock)*1000)))){
                continue;
            }
            List<MwVisualizedScoreStatusDto> dtos = entry.getValue();
            HealthScoreDetailedDto scoreDetailedDto = new HealthScoreDetailedDto();
            Date date = new Date();
            date.setTime(Long.parseLong(clock)*1000);
            if(CollectionUtils.isEmpty(dtos)){
                scoreDetailedDto.setClock(clock);
                scoreDetailedDto.setScore(100);
                scoreDetailedDto.setTime(format.format(date));
                detailedDtos.add(scoreDetailedDto);
                continue;
            }
            //取集合总和，保留两位小数
            double sum = dtos.stream().mapToDouble(MwVisualizedScoreStatusDto::getScorce).sum();
            scoreDetailedDto.setClock(clock);
            scoreDetailedDto.setScore(new BigDecimal(sum).setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            scoreDetailedDto.setTime(format.format(date));
            detailedDtos.add(scoreDetailedDto);
        }
        //去除最后一条数据，避免数据采集时间不一致
        if(CollectionUtils.isNotEmpty(detailedDtos)){
            detailedDtos = detailedDtos.subList(0,detailedDtos.size()-1);
        }
        //计算所有时间的平均分
        double avgValue = detailedDtos.stream().mapToDouble(HealthScoreDetailedDto::getScore).average().getAsDouble();
        healthScoreDto.setCountScore(new BigDecimal(avgValue).setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        //数据按时间排序
        detailedSort(detailedDtos);
        healthScoreDto.setScoreDetailedDtos(detailedDtos);
        return healthScoreDto;
    }

    /**
     * 计算健康分数
     */
    private void computeHealthScorce(List<MwVisualizedScoreStatusDto> scoreStatusDtos,Integer proportion){
        if(CollectionUtils.isEmpty(scoreStatusDtos)){return;}
        for (MwVisualizedScoreStatusDto scoreStatusDto : scoreStatusDtos) {
            if(scoreStatusDto.getAbNormalCount() == 0){
                scoreStatusDto.setScorce(Double.parseDouble(String.valueOf(proportion)));
                continue;
            }
            double scorce = new BigDecimal((scoreStatusDto.getNormalCount() * 1.0 / (scoreStatusDto.getAbNormalCount() + scoreStatusDto.getNormalCount())) * proportion).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            scoreStatusDto.setScorce(scorce);
        }
    }

    /**
     * 数据处理
     * @param timeDataMap
     */
    private List<MwVisualizedScoreStatusDto> handlerData(Map<String, List<MwVisualizedCacheHistoryDto>> timeDataMap){
        List<MwVisualizedScoreStatusDto> scoreStatusDtos = new ArrayList<>();
        for (Map.Entry<String, List<MwVisualizedCacheHistoryDto>> entry : timeDataMap.entrySet()) {
            List<MwVisualizedCacheHistoryDto> value = entry.getValue();
            MwVisualizedScoreStatusDto scoreStatusDto = new MwVisualizedScoreStatusDto();
            for (MwVisualizedCacheHistoryDto cacheHistoryDto : value) {
                scoreStatusDto.setClock(cacheHistoryDto.getClock());
                checkItemStatus(cacheHistoryDto.getItemName(),cacheHistoryDto.getAvgValue(),scoreStatusDto);
            }
            scoreStatusDtos.add(scoreStatusDto);
        }
        return scoreStatusDtos;
    }


    private void checkItemStatus(String itemName,String vlaue, MwVisualizedScoreStatusDto scoreStatusDto){
        VisualizedScoreItemEnum name = VisualizedScoreItemEnum.getByItemName(itemName);
        log.info("MwVisualizedModuleHealthScore{} filterAssets() typnameeEnum::"+name+":::itemName::"+itemName);
        switch (name){
            case PROCESS_HEALTH:
                if(StringUtils.isNotBlank(vlaue) &&  Double.parseDouble(vlaue) == new Double(1)){
                    scoreStatusDto.setNormalCount(scoreStatusDto.getNormalCount()+1);
                }else{
                    scoreStatusDto.setAbNormalCount(scoreStatusDto.getAbNormalCount()+1);
                }
                break;
            case MW_ORACLE_PYTHON_GET_VERSION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) == new Double(1)){
                    scoreStatusDto.setNormalCount(scoreStatusDto.getNormalCount()+1);
                }else{
                    scoreStatusDto.setAbNormalCount(scoreStatusDto.getAbNormalCount()+1);
                }
                break;
            case CPU_UTILIZATION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) < 90){
                    scoreStatusDto.setNormalCount(scoreStatusDto.getNormalCount()+1);
                }else{
                    scoreStatusDto.setAbNormalCount(scoreStatusDto.getAbNormalCount()+1);
                }
                break;
            case MEMORY_UTILIZATION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) < 70){
                    scoreStatusDto.setNormalCount(scoreStatusDto.getNormalCount()+1);
                }else{
                    scoreStatusDto.setAbNormalCount(scoreStatusDto.getAbNormalCount()+1);
                }
                break;
            case MW_DISK_UTILIZATION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) < 90){
                    scoreStatusDto.setNormalCount(scoreStatusDto.getNormalCount()+1);
                }else{
                    scoreStatusDto.setAbNormalCount(scoreStatusDto.getAbNormalCount()+1);
                }
                break;
            case ICMP_PING:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) == new Double(1)){
                    scoreStatusDto.setNormalCount(scoreStatusDto.getNormalCount()+1);
                }else{
                    scoreStatusDto.setAbNormalCount(scoreStatusDto.getAbNormalCount()+1);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 将分区名称与监控项拆分
     * @param cacheHistoryDtos
     */
    private void setItemNameInfo(List<MwVisualizedCacheHistoryDto> cacheHistoryDtos){
        for (MwVisualizedCacheHistoryDto cacheHistoryDto : cacheHistoryDtos) {
            String itemName = cacheHistoryDto.getItemName();
            if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
            cacheHistoryDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
            cacheHistoryDto.setItemName(itemName.split("]")[1]);
        }
    }

    /**
     * 过滤资产数据
     * @param tangibleassetsDTOS
     * @param proportionDto
     */
    private void filterAssets(List<MwTangibleassetsDTO> tangibleassetsDTOS,MwVisualizedScoreProportionDto proportionDto){
        Integer type = proportionDto.getType();
        log.info("MwVisualizedModuleHealthScore{} filterAssets() type::"+type);
        if(type == null){return;}
        String classifyName = proportionDto.getClassifyName();
        log.info("MwVisualizedModuleHealthScore{} filterAssets() classifyName::"+classifyName);
        VisualizedScoreTypeEnum typeEnum = VisualizedScoreTypeEnum.getByType(type);
        log.info("MwVisualizedModuleHealthScore{} filterAssets() typeEnum::"+typeEnum);
        List<String> names = Arrays.asList(classifyName.split(","));
        switch (typeEnum){
            case BUSINESS_CLASSIFY:
                proportionDto.setAssetsDtos(tangibleassetsDTOS.stream().filter(item -> names.contains(item.getModelClassify())).collect(Collectors.toList()));
                break;
            case ASSETS_TYPE:
                proportionDto.setAssetsDtos(tangibleassetsDTOS.stream().filter(item -> names.contains(item.getAssetsTypeName())).collect(Collectors.toList()));
                break;
            case ASSETS_NAME:
                proportionDto.setAssetsDtos(tangibleassetsDTOS.stream().filter(item -> item.getInstanceName().contains(classifyName)).collect(Collectors.toList()));
        }
    }

    private void detailedSort(List<HealthScoreDetailedDto> scoreDetailedDtos){
        Collections.sort(scoreDetailedDtos, new Comparator<HealthScoreDetailedDto>() {
            @Override
            public int compare(HealthScoreDetailedDto o1, HealthScoreDetailedDto o2) {
                if(Long.parseLong(o1.getClock()) < Long.parseLong(o2.getClock())){
                    return -1;
                }
                if(Long.parseLong(o1.getClock()) > Long.parseLong(o2.getClock())){
                    return 1;
                }
                return 0;
            }
        });
    }

}
