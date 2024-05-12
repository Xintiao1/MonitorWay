package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.link.dto.MwLinkInterfaceDto;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.model.param.QueryEsParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedLinkFlowDto;
import cn.mw.monitor.visualized.enums.VisualizedScoreItemEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
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
 * @author gengjb
 * @description 线路流量信息
 * @date 2023/9/11 10:56
 */
@Service
@Slf4j
public class MwVisualizedLinkFlow implements MwVisualizedModule {

    @Autowired
    private MWNetWorkLinkCommonService linkCommonService;

    @Autowired
    private MwModelCommonService mwModelCommonService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;


    private String SERVER_ID = "monitorServerId";

    private String ASSETS_ID = "assetsId";

    private final String MW_INTERFACE_IN_TRAFFIC = "MW_INTERFACE_IN_TRAFFIC";

    private final String MW_INTERFACE_OUT_TRAFFIC = "MW_INTERFACE_OUT_TRAFFIC";
    private final String INTERFACE_IN_UTILIZATION = "INTERFACE_IN_UTILIZATION";

    private final String INTERFACE_OUT_UTILIZATION = "INTERFACE_OUT_UTILIZATION";


    @Override
    public int[] getType() {
        return new int[]{104,106};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<String> linkIds = moduleParam.getLinkIds();
            log.info("MwVisualizedLinkFlow{} getData() linkIds::"+linkIds);
            //根据线路ID获取线路数据
            List<MwLinkInterfaceDto> linkInterfaceInfo = linkCommonService.getLinkInterfaceInfo(linkIds);
            log.info("MwVisualizedLinkFlow{} getData() linkInterfaceInfo::"+linkInterfaceInfo);
            Map<Integer, List<String>> groupMap = linkInterfaceInfo.stream().filter(item->item.getServerId() != null &&  item.getServerId() != 0)
                    .collect(Collectors.groupingBy(MwLinkInterfaceDto::getServerId, Collectors.mapping(MwLinkInterfaceDto::getHostId, Collectors.toList())));
            List<MwTangibleassetsDTO> assetsInfo = getAssetsInfo(groupMap);
            if(CollectionUtils.isEmpty(assetsInfo)){return null;}
            List<String> assetsIds = assetsInfo.stream().map(item -> String.valueOf(item.getModelInstanceId())).collect(Collectors.toList());
            log.info("MwVisualizedLinkFlow{} getData() linkIds::"+assetsIds);
            List<String> names = linkInterfaceInfo.stream().map(MwLinkInterfaceDto::getInterfaceName).collect(Collectors.toList());
            List<String> itemNames = new ArrayList<>();
            for (String name : names) {
                itemNames.add("["+name+"]"+MW_INTERFACE_IN_TRAFFIC);
                itemNames.add("["+name+"]"+MW_INTERFACE_OUT_TRAFFIC);
            }
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            List<MwVisualizedCacheDto> cacheDtoList = new ArrayList<>();
            for (List<String> ids : partition) {
                cacheDtoList.addAll(visualizedManageDao.selectvisualizedCacheInfos(ids,itemNames));
            }
            Map<String, MwLinkInterfaceDto> interfaceDtoMap = linkInterfaceInfo.stream()
                    .collect(Collectors.toMap(
                            item -> item.getHostId() + "-" + item.getServerId()+"-"+item.getInterfaceName(),
                            item -> item));
            Map<String, MwTangibleassetsDTO> tangibleassetsDTOMap = assetsInfo.stream().collect(Collectors.toMap(MwTangibleassetsDTO::getId, item -> item));
            List<MwVisualizedCacheDto> cacheDtos = new ArrayList<>();
            //名称替换
            for (MwVisualizedCacheDto mwVisualizedCacheDto : cacheDtoList) {
                String itemName = mwVisualizedCacheDto.getItemName();
                if(StringUtils.isNotBlank(itemName) && itemName.contains("]")){
                    mwVisualizedCacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
                    mwVisualizedCacheDto.setItemName(itemName.split("]")[1]);
                }
                MwTangibleassetsDTO mwTangibleassetsDTO = tangibleassetsDTOMap.get(mwVisualizedCacheDto.getAssetsId());
                MwLinkInterfaceDto interfaceDto = interfaceDtoMap.get(mwTangibleassetsDTO.getAssetsId() + "-" + mwTangibleassetsDTO.getMonitorServerId()+"-"+mwVisualizedCacheDto.getName());
                if(interfaceDto == null || !mwVisualizedCacheDto.getName().equals(interfaceDto.getInterfaceName())){continue;}
                mwVisualizedCacheDto.setAssetsName(interfaceDto.getLinkName());
                //单位转换
                Map<String, String> valueMap = UnitsUtil.getValueMap(mwVisualizedCacheDto.getValue(), moduleParam.getUnits(), mwVisualizedCacheDto.getUnits());
                if(valueMap != null){
                    mwVisualizedCacheDto.setValue(valueMap.get("value"));
                    mwVisualizedCacheDto.setUnits(valueMap.get("units"));
                }
                cacheDtos.add(mwVisualizedCacheDto);
            }
            Map<String, List<MwVisualizedCacheDto>> collected = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId() + item.getName()));
            List<MwVisualizedCacheDto> newCacheDtos = new ArrayList<>();
            for (Map.Entry<String, List<MwVisualizedCacheDto>> entry : collected.entrySet()) {
                List<MwVisualizedCacheDto> value = entry.getValue();
                //求和
                double sum = value.stream()
                        .mapToDouble(obj -> Double.parseDouble(obj.getValue())).sum();
                for (MwVisualizedCacheDto mwVisualizedCacheDto : value) {
                    mwVisualizedCacheDto.setSortValue(sum);
                    newCacheDtos.add(mwVisualizedCacheDto);
                }
            }
            getLinkFlowBandWithUtilization(linkInterfaceInfo,newCacheDtos);
            //判断是否列表展示
            if(moduleParam.getChartType() == 106){
                return handlerLinkTableInfo(newCacheDtos);
            }
            //按照监控项分组
            Map<String, List<MwVisualizedCacheDto>> listMap = newCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            Map<String,List<MwVisualizedCacheDto>> realMap = new HashMap<>();
            //数据排序，只取前10
            for (Map.Entry<String, List<MwVisualizedCacheDto>> entry : listMap.entrySet()) {
                List<MwVisualizedCacheDto> value = entry.getValue();
                dataSort(value);
                if(value.size() > 10){
                    realMap.put(entry.getKey(),value.subList(0,10));
                    continue;
                }
                realMap.put(entry.getKey(),value);
            }
            return realMap;
        }catch (Throwable e){
            log.error("MwVisualizedLinkFlow{} getData() ERROR",e);
            return null;
        }
    }


    /**
     * 展示线路列表
     * @param newCacheDtos
     */
    private List<MwVisualizedLinkFlowDto> handlerLinkTableInfo( List<MwVisualizedCacheDto> newCacheDtos){
        List<MwVisualizedLinkFlowDto> linkFlowDtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(newCacheDtos)){return linkFlowDtos;}
        //按照线路名称分组
        Map<String, List<MwVisualizedCacheDto>> listMap = newCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsName()));
        for (Map.Entry<String, List<MwVisualizedCacheDto>> entry : listMap.entrySet()) {
            String linkName = entry.getKey();
            List<MwVisualizedCacheDto> cacheDtos = entry.getValue();
            MwVisualizedLinkFlowDto mwVisualizedLinkFlowDto = setTableInfo(cacheDtos);
            mwVisualizedLinkFlowDto.setLinkName(linkName);
            linkFlowDtos.add(mwVisualizedLinkFlowDto);
        }
        return linkFlowDtos;
    }


    private MwVisualizedLinkFlowDto setTableInfo( List<MwVisualizedCacheDto> cacheDtos){
        MwVisualizedLinkFlowDto linkFlowDto = new MwVisualizedLinkFlowDto();
        for (MwVisualizedCacheDto cacheDto : cacheDtos) {
            String itemName = cacheDto.getItemName();
            VisualizedScoreItemEnum name = VisualizedScoreItemEnum.getByItemName(itemName);
            switch (name){
                case MW_INTERFACE_IN_TRAFFIC:
                    linkFlowDto.setFlowIn(cacheDto.getValue()+cacheDto.getUnits());
                    break;
                case MW_INTERFACE_OUT_TRAFFIC:
                    linkFlowDto.setFlowOut(cacheDto.getValue()+cacheDto.getUnits());
                    break;
                case INTERFACE_IN_UTILIZATION:
                    linkFlowDto.setFlowPercentageIn(cacheDto.getValue()+"%");
                    break;
                case INTERFACE_OUT_UTILIZATION:
                    linkFlowDto.setFlowPercentageOut(cacheDto.getValue()+"%");
                    break;
            }
        }
        return linkFlowDto;
    }

    private void getLinkFlowBandWithUtilization(List<MwLinkInterfaceDto> linkInterfaceInfo,List<MwVisualizedCacheDto> newCacheDtos){
        for (MwLinkInterfaceDto interfaceDto : linkInterfaceInfo) {
            MwVisualizedCacheDto inCacheDto = new MwVisualizedCacheDto();
            inCacheDto.setAssetsName(interfaceDto.getLinkName());
            inCacheDto.setValue(interfaceDto.getUpBnadWithUtilization());
            inCacheDto.setSortValue(Double.parseDouble(interfaceDto.getUpBnadWithUtilization()));
            inCacheDto.setItemName(VisualizedScoreItemEnum.INTERFACE_IN_UTILIZATION.getItemName());
            inCacheDto.setUnits("%");
            newCacheDtos.add(inCacheDto);
            MwVisualizedCacheDto outCacheDto = new MwVisualizedCacheDto();
            outCacheDto.setAssetsName(interfaceDto.getLinkName());
            outCacheDto.setValue(interfaceDto.getUpBnadWithUtilization());
            outCacheDto.setSortValue(Double.parseDouble(interfaceDto.getUpBnadWithUtilization()));
            outCacheDto.setItemName(VisualizedScoreItemEnum.INTERFACE_OUT_UTILIZATION.getItemName());
            outCacheDto.setUnits("%");
            newCacheDtos.add(outCacheDto);
        }
    }

    private void dataSort(List<MwVisualizedCacheDto> cacheDtoList){
        Collections.sort(cacheDtoList, new Comparator<MwVisualizedCacheDto>() {
            @Override
            public int compare(MwVisualizedCacheDto o1, MwVisualizedCacheDto o2) {
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

    private List<MwTangibleassetsDTO> getAssetsInfo( Map<Integer, List<String>> groupMap) throws Exception {
        List<MwTangibleassetsDTO> tangibleassetsDTOS = new ArrayList<>();
        for (Integer serverId : groupMap.keySet()) {
            QueryEsParam param = new QueryEsParam();
            QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(SERVER_ID);
            qParam.setPropertiesValue(String.valueOf(serverId));
            QueryModelInstanceByPropertyIndexParam qParam2 = new QueryModelInstanceByPropertyIndexParam();
            qParam2.setPropertiesIndexId(ASSETS_ID);
            qParam2.setPropertiesValueList(groupMap.get(serverId));
            List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
            paramLists.add(qParam);
            paramLists.add(qParam2);
            param.setParamLists(paramLists);
            param.setPageSize(Integer.MAX_VALUE);
            log.info("MwVisualizedLinkFlow{} getAssetsInfo() param::"+param);
            List<Map<String, Object>> allInstanceInfoByQueryParam = mwModelCommonService.getAllInstanceInfoByQueryParam(param);
            log.info("MwVisualizedLinkFlow{} getAssetsInfo() allInstanceInfoByQueryParam::"+allInstanceInfoByQueryParam);
            if(CollectionUtils.isNotEmpty(allInstanceInfoByQueryParam)){
                tangibleassetsDTOS.addAll(MwModelUtils.convertEsData(MwTangibleassetsDTO.class, allInstanceInfoByQueryParam));
            }
        }
        log.info("MwVisualizedLinkFlow{} getAssetsInfo() tangibleassetsDTOS::"+tangibleassetsDTOS);
        return tangibleassetsDTOS;
    }
}
