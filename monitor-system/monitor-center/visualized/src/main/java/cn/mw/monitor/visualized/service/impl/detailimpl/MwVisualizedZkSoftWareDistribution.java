package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dto.MwVisuZkSoftWareDistributionDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import cn.mw.monitor.visualized.util.MwVisualizedSetPropertyUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 中控配电柜数据
 * @Author gengjb
 * @Date 2023/3/16 15:11
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedZkSoftWareDistribution implements MwVisualizedZkSoftWare {

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public int[] getType() {
        return new int[]{45};
    }

    @Override
    public Object getData() {
        try {
            //获取机柜数据
            List<MwTangibleassetsDTO> assetsData = getAssetsData();
            log.info("中控配电柜资产数据"+assetsData);
            //获取配电柜的监控信息
            List<MwVisuZkSoftWareDistributionDto> distributionDtos = getDistributionItem(assetsData);
//            distributionDtos.add(MwVisuZkSoftWareDistributionDto.builder().distributionName("配电柜1").voltage("53.5v").current("25.3A").curtailment("21.2%").distributionPdb("1380h").distributionBattery1("0.00A").distributionRemainder("100%").distributionSinglephaseVoltage("221v").build());
//            distributionDtos.add(MwVisuZkSoftWareDistributionDto.builder().distributionName("配电柜2").voltage("63.5v").current("35.3A").curtailment("31.2%").distributionPdb("2380h").distributionBattery1("1.00A").distributionRemainder("80%").distributionSinglephaseVoltage("224v").build());
            return distributionDtos;
        }catch (Throwable e){
            log.error("可视化查询中控配电柜数据失败",e);
            return null;
        }
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
        return mwTangibleassetsTables;
    }

    /**
     * 获取配电柜监控项信息
     * @param assetsData
     */
    private List<MwVisuZkSoftWareDistributionDto> getDistributionItem(List<MwTangibleassetsDTO> assetsData) throws Exception {
        List<MwVisuZkSoftWareDistributionDto> realDatas = new ArrayList<>();
        //根据服务器进行分组
        if(CollectionUtils.isEmpty(assetsData)){return realDatas;}
        Map<String,MwVisuZkSoftWareDistributionDto> distributionDtoMap = new HashMap<>();
        Map<String, String> hostNameMap = new HashMap<>();
        for (MwTangibleassetsDTO assetsDatum : assetsData) {
            hostNameMap.put(assetsDatum.getAssetsId(),assetsDatum.getHostName());
        }
        Map<Integer, List<String>> hostMap = assetsData.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0 && StringUtils.isNotBlank(item.getAssetsId())
                && item.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName()))
                .collect(Collectors.groupingBy(MwTangibleassetsDTO::getMonitorServerId, Collectors.mapping(MwTangibleassetsDTO::getAssetsId, Collectors.toList())));
        log.info("中控配电柜资产数据2"+hostMap);
        if(hostMap == null || hostMap.isEmpty()){return realDatas;}
        for (Map.Entry<Integer, List<String>> entry : hostMap.entrySet()) {
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, RackZabbixItemConstant.DISTRIBUTION_ITEM, hostIds);
            log.info("中控配电柜资产数据3"+result);
            //数据处理
            if(result == null || result.getCode() != 0){continue;}
            //数据集
            JsonNode jsonNode = (JsonNode) result.getData();
            handleDataSet(jsonNode,hostNameMap,distributionDtoMap);
        }
        if(distributionDtoMap == null || distributionDtoMap.isEmpty()){return realDatas;}
        for (Map.Entry<String, MwVisuZkSoftWareDistributionDto> entry : distributionDtoMap.entrySet()) {
            realDatas.add(entry.getValue());
        }
        return realDatas;
    }

    /**
     * 处理zabbix数据集
     */
    private void handleDataSet(JsonNode jsonNode,Map<String, String> hostNameMap,Map<String,MwVisuZkSoftWareDistributionDto> distributionDtoMap) throws Exception {
        for (JsonNode node : jsonNode) {
            String hostid = node.get(RackZabbixItemConstant.HOST_ID).asText();
            String name = node.get(RackZabbixItemConstant.NAME).asText();
            String units = node.get(RackZabbixItemConstant.UNITS).asText();
            String lastValue = node.get(RackZabbixItemConstant.LASTVALUE).asText();
            if(distributionDtoMap.containsKey(hostid)){
                MwVisuZkSoftWareDistributionDto distributionDto = distributionDtoMap.get(hostid);
                distributionDto.setDistributionName(hostNameMap.get(hostid));
                MwVisualizedSetPropertyUtil.setProperty(distributionDto, name, lastValue, units);
                distributionDtoMap.put(hostid,distributionDto);
            }else{
                MwVisuZkSoftWareDistributionDto distributionDto = new MwVisuZkSoftWareDistributionDto();
                distributionDto.setDistributionName(hostNameMap.get(hostid));
                MwVisualizedSetPropertyUtil.setProperty(distributionDto,name,lastValue,units);
                distributionDtoMap.put(hostid,distributionDto);
            }
        }
        log.info("中控配电柜资产数据4"+distributionDtoMap);
    }
}
