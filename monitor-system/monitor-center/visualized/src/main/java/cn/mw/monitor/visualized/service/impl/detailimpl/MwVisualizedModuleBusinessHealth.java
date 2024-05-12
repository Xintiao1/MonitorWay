package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleBusinessHealthDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleModelTypeDto;
import cn.mw.monitor.visualized.dto.MwVisualizedScoreProportionDto;
import cn.mw.monitor.visualized.enums.VisualizedScoreItemEnum;
import cn.mw.monitor.visualized.enums.VisualizedScoreTypeEnum;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedModuleBusinessHealth
 * @Description 业务健康状态
 * @Author gengjb
 * @Date 2023/4/17 22:38
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleBusinessHealth implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    private final String GROUP_NODE = ",5,";

    private final String TYPE_NAME = "虚拟化";


    @Override
    public int[] getType() {
        return new int[]{52};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            log.info("MwVisualizedModuleBusinessHealth{} getData()  moduleParam::"+moduleParam);
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            MwVisualizedModuleBusinessHealthDto moduleBusinessHealthDto = new MwVisualizedModuleBusinessHealthDto();
            moduleBusinessHealthDto.setTypeClassifys(getModelType( tangibleassetsDTOS));
            //根据业务分类获取各个主机状态信息
            moduleBusinessHealthDto.setBusinessClassify(getHostProcessInfo(tangibleassetsDTOS));
            return moduleBusinessHealthDto;
        }catch (Throwable e){
            log.error("可视化组件区查询模型分区失败",e);
            return null;
        }
    }

    /**
     * 获取主机进程信息
     * @param tangibleassetsDTOS
     */
    private Map<String,List<MwVisualizedModuleModelTypeDto>> getHostProcessInfo(List<MwTangibleassetsDTO> tangibleassetsDTOS){
        Map<String,List<MwVisualizedModuleModelTypeDto>> modelTypeDtoMap = new HashMap<>();
        List<ItemApplication> itemApplicationList = new ArrayList<>();
        List<String> assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
        List<MwVisualizedCacheDto> cacheDtoList = new ArrayList<>();
        List<List<String>> partition = Lists.partition(assetsIds, 500);
        for (List<String> ids : partition) {
            cacheDtoList.addAll(visualizedManageDao.selectvisualizedCacheInfo(ids, RackZabbixItemConstant.PROCESS_HEALTH));
        }
        if(CollectionUtils.isNotEmpty(cacheDtoList)){
            for (MwVisualizedCacheDto cacheDto : cacheDtoList) {
                ItemApplication application = new ItemApplication();
                application.setHostid(cacheDto.getHostId());
                application.setLastvalue(cacheDto.getValue());
                application.setUnits(cacheDto.getUnits());
                application.setName(cacheDto.getItemName());
                itemApplicationList.add(application);
            }
        }
        log.info("可视化健康状态查询进程信息"+itemApplicationList);
        if(CollectionUtils.isEmpty(itemApplicationList)){return modelTypeDtoMap;}
        Map<String,MwTangibleassetsDTO> assetsMap = new HashMap<>();
        tangibleassetsDTOS.forEach(item->{
            assetsMap.put(item.getAssetsId(),item);
        });
        for (ItemApplication itemApplication : itemApplicationList) {
            String hostid = itemApplication.getHostid();
            String lastvalue = itemApplication.getLastvalue();
            if(StringUtils.isBlank(hostid) || StringUtils.isBlank(lastvalue)){continue;}
            MwTangibleassetsDTO tangibleassetsDTO = assetsMap.get(hostid);
            MwVisualizedModuleModelTypeDto modelTypeDto = classifyHandler(lastvalue, itemApplication.getName());
            String modelClassify = tangibleassetsDTO.getModelClassify();
            if(StringUtils.isBlank(modelClassify)){continue;}
            if(modelTypeDtoMap.containsKey(modelClassify)){
                List<MwVisualizedModuleModelTypeDto> modelTypeDtos = modelTypeDtoMap.get(modelClassify);
                modelTypeDtos.add(modelTypeDto);
                modelTypeDtoMap.put(modelClassify,modelTypeDtos);
            }else{
                List<MwVisualizedModuleModelTypeDto> modelTypeDtos = new ArrayList<>();
                modelTypeDtos.add(modelTypeDto);
                modelTypeDtoMap.put(modelClassify,modelTypeDtos);
            }
        }
        log.info("可视化健康状态查询进程信息明细"+modelTypeDtoMap);
        return modelTypeDtoMap;
    }

    /**
     * 处理分类信息
     * @param lastvalue
     * @param name
     */
    private MwVisualizedModuleModelTypeDto classifyHandler(String lastvalue,String name){
        MwVisualizedModuleModelTypeDto modelTypeDto = new MwVisualizedModuleModelTypeDto();
        if(StringUtils.isBlank(name) || !name.contains("[") || !name.contains("]")){return modelTypeDto;}
        String processName = name.substring(name.indexOf("[") + 1, name.indexOf("]"));//进程名称
        modelTypeDto.setName(processName);
        String status = Double.parseDouble(lastvalue) == 0 ? VisualizedConstant.ABNORMAL : VisualizedConstant.NORMAL;
        modelTypeDto.setStatus(status);
        return modelTypeDto;
    }

    /**
     * 获取模型类型分类信息
     * @param tangibleassetsDTOS
     */
    private List<MwVisualizedScoreProportionDto> getModelType( List<MwTangibleassetsDTO> tangibleassetsDTOS){
        List<MwVisualizedScoreProportionDto> visualizedScoreProportion = visualizedManageDao.getVisualizedScoreProportion();
        if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return visualizedScoreProportion;}
        for (MwTangibleassetsDTO tangibleassetsDTO : tangibleassetsDTOS) {
            if(tangibleassetsDTO.getGroupNodes().contains(GROUP_NODE)){
                tangibleassetsDTO.setAssetsTypeName(TYPE_NAME);
            }
        }
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
        log.info("MwVisualizedModuleBusinessHealth{} getModelType() visualizedScoreProportion::"+visualizedScoreProportion);
        List<MwVisualizedCacheDto> mwVisualizedCacheDtos = visualizedManageDao.selectvisualizedCacheInfos(assetsIds, itemNames);
        setItemNameInfo(mwVisualizedCacheDtos);
        for (MwVisualizedScoreProportionDto proportionDto : visualizedScoreProportion) {
            List<MwTangibleassetsDTO> assetsDtos = proportionDto.getAssetsDtos();
            if(CollectionUtils.isEmpty(assetsDtos)){
                proportionDto.setAssetsCount(0);
                proportionDto.setStatus(VisualizedZkSoftWareEnum.NORMAL.getName());
                continue;
            }
            proportionDto.setAssetsCount(assetsDtos.size());
            List<String> ids = assetsDtos.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            List<String> items = Arrays.asList(proportionDto.getItemName().split(","));
            List<MwVisualizedCacheDto> visualizedCacheDtos = mwVisualizedCacheDtos.stream().
                    filter(item -> ids.contains(item.getAssetsId()) && items.contains(item.getItemName())).collect(Collectors.toList());
            log.info("MwVisualizedModuleBusinessHealth{} getModelType() visualizedCacheDtos::"+visualizedCacheDtos);
            log.info("MwVisualizedModuleBusinessHealth{} getModelType() className::"+proportionDto.getClassifyName()+">>>"+proportionDto.getItemName());
            StringBuffer buffer = new StringBuffer();
            //按照时间分组
            for (MwVisualizedCacheDto dto : visualizedCacheDtos) {
                boolean flag = checkItemStatus(dto.getItemName(), dto.getValue());
                log.info("MwVisualizedModuleBusinessHealth{} getModelType() flag::"+flag);
                if(!flag){
                    buffer.append(dto.getAssetsName()+">>>");
                }
            }
            if(buffer != null && buffer.length() > 0){
                proportionDto.setStatus(VisualizedZkSoftWareEnum.ABNORMAL.getName());
                proportionDto.setErrorAssets(buffer.toString());
            }else{
                proportionDto.setStatus(VisualizedZkSoftWareEnum.NORMAL.getName());
            }
        }
        return visualizedScoreProportion;
    }


    /**
     * 将分区名称与监控项拆分
     * @param cacheDtos
     */
    private void setItemNameInfo(List<MwVisualizedCacheDto> cacheDtos){
        for (MwVisualizedCacheDto cacheHistoryDto : cacheDtos) {
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
        if(type == null){return;}
        String classifyName = proportionDto.getClassifyName();
        VisualizedScoreTypeEnum typeEnum = VisualizedScoreTypeEnum.getByType(type);
        List<String> names = Arrays.asList(classifyName.split(","));
        switch (typeEnum){
            case BUSINESS_CLASSIFY:
                proportionDto.setAssetsDtos(tangibleassetsDTOS.stream().filter(item -> names.contains(item.getModelClassify())).collect(Collectors.toList()));
                break;
            case ASSETS_TYPE:
                proportionDto.setAssetsDtos(tangibleassetsDTOS.stream().filter(item -> names.contains(item.getAssetsTypeName())).collect(Collectors.toList()));
                break;
            case ASSETS_NAME:
                proportionDto.setClassifyName(tangibleassetsDTOS.get(0).getAssetsTypeName());
                proportionDto.setAssetsDtos(tangibleassetsDTOS.stream().filter(item -> item.getInstanceName().contains(classifyName)).collect(Collectors.toList()));
        }
    }

    private boolean checkItemStatus(String itemName,String vlaue){
        VisualizedScoreItemEnum name = VisualizedScoreItemEnum.getByItemName(itemName);
        switch (name){
            case PROCESS_HEALTH:
                if(StringUtils.isNotBlank(vlaue) &&  Double.parseDouble(vlaue) == new Double(1)){
                    return true;
                }else{
                    return false;
                }
            case MW_ORACLE_PYTHON_GET_VERSION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) == new Double(1)){
                    return true;
                }else{
                    return false;
                }
            case CPU_UTILIZATION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) < 90){
                    return true;
                }else{
                    return false;
                }
            case MEMORY_UTILIZATION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) < 70){
                    return true;
                }else{
                    return false;
                }
            case MW_DISK_UTILIZATION:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) < 90){
                    return true;
                }else{
                    return false;
                }
            case ICMP_PING:
                if(StringUtils.isNotBlank(vlaue) && Double.parseDouble(vlaue) == new Double(1)){
                    return true;
                }else{
                    return false;
                }
            default:
                return true;
        }
    }
}
