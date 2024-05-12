package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dto.MwVisuZkRectityModuleDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareItemEnum;
import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 整流模块组件
 * @date 2023/7/17 10:16
 */
@Service
@Slf4j
public class MwVisualizedZkRectityModule  implements MwVisualizedZkSoftWare {

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public int[] getType() {
        return new int[]{93};
    }

    @Override
    public Object getData() {
        try {
            //获取机柜数据
            List<MwTangibleassetsDTO> assetsData = getAssetsData();
            log.info("中控整流模块资产数据"+assetsData);
            if(CollectionUtils.isEmpty(assetsData)){return null;}
            Map<Integer, List<String>> hostIdMap = assetsData.stream().collect(Collectors.groupingBy(MwTangibleassetsDTO::getMonitorServerId, Collectors.mapping(MwTangibleassetsDTO::getAssetsId, Collectors.toList())));
            Map<String, MwTangibleassetsDTO> mwTangibleassetsDTOMap = assetsData.stream().collect(Collectors.toMap(MwTangibleassetsDTO::getAssetsId, Function.identity()));
            //查询监控信息
            return getZabbixItemInfo(hostIdMap,mwTangibleassetsDTOMap);
        }catch (Throwable e){
            log.error("可视化查询整流模块数据失败",e);
            return null;
        }
    }

    /**
     * 查询zabbix中的监控信息
     */
    private Map<String,List<MwVisuZkRectityModuleDto>> getZabbixItemInfo(Map<Integer, List<String>> hostIdMap,Map<String, MwTangibleassetsDTO> mwTangibleassetsDTOMap) throws Exception {
        log.info("MwVisualizedZkRectityModule{} getZabbixItemInfo()  mwTangibleassetsDTOMap:"+mwTangibleassetsDTOMap);
        Map<String,List<MwVisuZkRectityModuleDto>> rectityModuleMap = new HashMap<>();
        for (Integer serverId : hostIdMap.keySet()) {
            List<String> hostIds = hostIdMap.get(serverId);
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, RackZabbixItemConstant.RECTITV_ITEM, hostIds);
            if(result == null || result.isFail()){return rectityModuleMap;}
            List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            //按照hostId进行数据分组
            Map<String, List<ItemApplication>> hostMap = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getHostid()));
            for (String hostId : hostMap.keySet()) {
                List<ItemApplication> applications = hostMap.get(hostId);
                if(CollectionUtils.isEmpty(applications)){continue;}
                //分区名称设置
                applications.forEach(item->{
                    if(item.getName().contains("]")){
                        item.setTypeName(item.getName().substring(item.getName().indexOf("[")+1,item.getName().indexOf("]")));
                    }
                });
                //按照分区分组
                Map<String, List<ItemApplication>> typeNameMap = applications.stream().collect(Collectors.groupingBy(item -> item.getTypeName()));
                MwTangibleassetsDTO mwTangibleassetsDTO = mwTangibleassetsDTOMap.get(hostId);
                log.info("MwVisualizedZkRectityModule{} getZabbixItemInfo()  typeNameMap:"+typeNameMap);
                if(typeNameMap == null || typeNameMap.isEmpty() || mwTangibleassetsDTO == null){continue;}
                String assetsName = mwTangibleassetsDTO.getAssetsName() != null?mwTangibleassetsDTO.getAssetsName():mwTangibleassetsDTO.getInstanceName();
                List<MwVisuZkRectityModuleDto> zkRectityModuleDtos = getValue(typeNameMap, assetsName);
                rectityModuleMap.put(assetsName,zkRectityModuleDtos);
            }
        }
        return rectityModuleMap;
    }

    /**
     * 获取数据值
     * @param typeNameMap
     */
    private List<MwVisuZkRectityModuleDto> getValue(Map<String, List<ItemApplication>> typeNameMap,String assetsName) throws Exception {
        List<MwVisuZkRectityModuleDto> rectityModuleDtos = new ArrayList<>();
        for (String typeName : typeNameMap.keySet()) {
            List<ItemApplication> applications = typeNameMap.get(typeName);
            if(CollectionUtils.isEmpty(applications)){continue;}
            MwVisuZkRectityModuleDto rectityModuleDto = new MwVisuZkRectityModuleDto();
            rectityModuleDto.setDistributionName(assetsName);
            for (ItemApplication application : applications) {
                String name = application.getName();
                name = name.replace(typeName, "");
                String proPerty = VisualizedZkSoftWareItemEnum.getProPerty(name);
                if(StringUtils.isBlank(proPerty)){continue;}
                Field field = rectityModuleDto.getClass().getDeclaredField(proPerty);
                field.setAccessible(true);
                //数据单位转换
                if(!MwVisualizedUtil.checkStrIsNumber(application.getLastvalue())){
                    field.set(rectityModuleDto,application.getLastvalue()+application.getUnits());
                    continue;
                }
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(application.getLastvalue()), application.getUnits());
                field.set(rectityModuleDto,convertedValue.get("value")+convertedValue.get("units"));
            }
            rectityModuleDtos.add(rectityModuleDto);
        }
        return rectityModuleDtos;
    }

    /**
     * 获取配电柜实例
     * @return
     */
    private List<MwTangibleassetsDTO> getAssetsData() throws Exception {
        //获取IOT类型ID
        List<Integer> modelTypeId = mwModelViewCommonService.getModelGroupIdByName(VisualizedZkSoftWareEnum.TYPE_IOT.getName());
        if(CollectionUtils.isEmpty(modelTypeId)){return null;}
        List<MwTangibleassetsDTO>  mwTangibleassetsTables = new ArrayList<>();
        QueryModelAssetsParam queryTangAssetsParam = new QueryModelAssetsParam();
        queryTangAssetsParam.setAssetsTypeId(modelTypeId.get(0));
        queryTangAssetsParam.setIsQueryAssetsState(true);
        //根据资产类型ID查询实例数据
        mwTangibleassetsTables = mwModelViewCommonService.findModelAssets(MwTangibleassetsDTO.class,queryTangAssetsParam);
        if(CollectionUtils.isNotEmpty(mwTangibleassetsTables)){
            return mwTangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0 && StringUtils.isNotBlank(item.getAssetsId())
                    && item.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName())).collect(Collectors.toList());
        }
        return mwTangibleassetsTables;
    }
}
