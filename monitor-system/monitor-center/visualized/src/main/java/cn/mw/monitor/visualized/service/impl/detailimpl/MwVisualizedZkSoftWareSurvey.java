package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dto.MwVisuZkSoftWareSurveyDto;
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

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description MwVisualizedZkSoftWareSurvey 获取机柜环境监测数据
 * @Author gengjb
 * @Date 2023/3/15 20:26
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedZkSoftWareSurvey implements MwVisualizedZkSoftWare {

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public int[] getType() {
        return new int[]{40};
    }

    @Override
    public Object getData() {
        try {
            //获取机柜数据
            List<MwTangibleassetsDTO> assetsData = getAssetsData();
            log.info("中控机柜监测资产数据"+assetsData);
            //获取机柜的监控信息
            List<MwVisuZkSoftWareSurveyDto> zkSoftWareSurveyDtos = getServerItem(assetsData);
            //按名称排序
            if(CollectionUtils.isEmpty(zkSoftWareSurveyDtos)){return zkSoftWareSurveyDtos;}
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            List<MwVisuZkSoftWareSurveyDto> softWareSurveyDtos = zkSoftWareSurveyDtos.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getServerRoomName()), pinyin4jUtil.getStringPinYin(o2.getServerRoomName()))).collect(Collectors.toList());
            return softWareSurveyDtos;
        }catch (Throwable e){
            log.error("可视化查询中控机柜监测数据失败",e);
            return null;
        }
    }

    /**
     * 获取机柜实例
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
     * 获取机柜的监控信息
     * @param assetsData
     */
    private List<MwVisuZkSoftWareSurveyDto> getServerItem(List<MwTangibleassetsDTO> assetsData) throws Exception{
        List<MwVisuZkSoftWareSurveyDto> realDatas = new ArrayList<>();
        //根据服务器进行分组
        if(CollectionUtils.isEmpty(assetsData)){return realDatas;}
        Map<String,MwVisuZkSoftWareSurveyDto> surveyDtoMap = new HashMap<>();
        Map<String, String> hostNameMap = new HashMap<>();
        for (MwTangibleassetsDTO assetsDatum : assetsData) {
            if(!assetsDatum.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName())){
                hostNameMap.put(assetsDatum.getAssetsId(),assetsDatum.getHostName());
            }
        }
        Map<Integer, List<String>> hostMap = assetsData.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0 && StringUtils.isNotBlank(item.getAssetsId())
                && !item.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName()))
                .collect(Collectors.groupingBy(MwTangibleassetsDTO::getMonitorServerId, Collectors.mapping(MwTangibleassetsDTO::getAssetsId, Collectors.toList())));
        log.info("中控机柜监测资产数据2"+hostMap);
        if(hostMap == null || hostMap.isEmpty()){return realDatas;}
        for (Map.Entry<Integer, List<String>> entry : hostMap.entrySet()) {
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, RackZabbixItemConstant.RACKITEM, hostIds);
            log.info("中控机柜监测资产数据3"+result);
            //数据处理
            if(result == null || result.getCode() != 0){continue;}
            //数据集
            JsonNode jsonNode = (JsonNode) result.getData();
            handleDataSet(jsonNode,hostNameMap,surveyDtoMap);
        }
        if(surveyDtoMap == null || surveyDtoMap.isEmpty()){return realDatas;}
        for (Map.Entry<String, MwVisuZkSoftWareSurveyDto> entry : surveyDtoMap.entrySet()) {
            realDatas.add(entry.getValue());
        }
        if(CollectionUtils.isNotEmpty(realDatas)){
            for (MwVisuZkSoftWareSurveyDto realData : realDatas) {
                String frontDoor = realData.getFrontDoor();//前门
                String behindDoor = realData.getBehindDoor();//后门
                if(StringUtils.isNotBlank(frontDoor)){
                    if(Double.parseDouble(frontDoor) == 0){
                        realData.setFrontDoor(RackZabbixItemConstant.OPEN);
                    }
                    if(Double.parseDouble(frontDoor) == 1){
                        realData.setFrontDoor(RackZabbixItemConstant.CLOSE);
                    }
                }
                if(StringUtils.isNotBlank(behindDoor)){
                    if(Double.parseDouble(behindDoor) == 0){
                        realData.setBehindDoor(RackZabbixItemConstant.OPEN);
                    }
                    if(Double.parseDouble(behindDoor) == 1){
                        realData.setBehindDoor(RackZabbixItemConstant.CLOSE);
                    }
                }
            }
        }
        return realDatas;
    }

    /**
     * 处理zabbix数据集
     */
    private void handleDataSet(JsonNode jsonNode,Map<String, String> hostNameMap,Map<String,MwVisuZkSoftWareSurveyDto> surveyDtoMap) throws Exception {
        for (JsonNode node : jsonNode) {
            String hostid = node.get(RackZabbixItemConstant.HOST_ID).asText();
            String name = node.get(RackZabbixItemConstant.NAME).asText();
            String units = node.get(RackZabbixItemConstant.UNITS).asText();
            String lastValue = node.get(RackZabbixItemConstant.LASTVALUE).asText();
            if(surveyDtoMap.containsKey(hostid)){
                MwVisuZkSoftWareSurveyDto mwVisuZkSoftWareSurveyDto = surveyDtoMap.get(hostid);
                mwVisuZkSoftWareSurveyDto.setServerRoomName(hostNameMap.get(hostid));
                MwVisualizedSetPropertyUtil.setProperty(mwVisuZkSoftWareSurveyDto, name, lastValue, units);
                surveyDtoMap.put(hostid,mwVisuZkSoftWareSurveyDto);
            }else{
                MwVisuZkSoftWareSurveyDto mwVisuZkSoftWareSurveyDto = new MwVisuZkSoftWareSurveyDto();
                mwVisuZkSoftWareSurveyDto.setServerRoomName(hostNameMap.get(hostid));
                MwVisualizedSetPropertyUtil.setProperty(mwVisuZkSoftWareSurveyDto, name, lastValue, units);
                surveyDtoMap.put(hostid,mwVisuZkSoftWareSurveyDto);
            }
        }
        log.info("中控机柜监测资产数据4"+surveyDtoMap);
    }
}
