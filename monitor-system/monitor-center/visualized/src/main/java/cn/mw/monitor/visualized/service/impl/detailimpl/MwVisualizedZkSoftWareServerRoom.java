package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.dto.MwVisuZkSoftWareServerRoomDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
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
 * @Description 获取中控机柜模型数据
 * @Author gengjb
 * @Date 2023/3/15 15:01
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedZkSoftWareServerRoom implements MwVisualizedZkSoftWare {

    @Autowired
    private MWAlertService alertService;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Override
    public int[] getType() {
        return new int[]{37};
    }

    @Override
    public Object getData() {
        try {
            //查询机柜资产
            List<MwTangibleassetsDTO> assetsData = getAssetsData();
            log.info("中控机柜模型数据"+assetsData);
            //调取异常数据信息
            Map<String, String> abNormalAssetsMessage = getAbNormalAssetsMessage(assetsData);
            List<MwVisuZkSoftWareServerRoomDto> zkSoftWareServerRoomDtos = getRealData(assetsData, abNormalAssetsMessage);
//            zkSoftWareServerRoomDtos.add(MwVisuZkSoftWareServerRoomDto.builder().serverRoomName("机柜C1").serverRoomStatus(VisualizedZkSoftWareEnum.NORMAL.getName()).build());
//            zkSoftWareServerRoomDtos.add(MwVisuZkSoftWareServerRoomDto.builder().serverRoomName("机柜C2").serverRoomStatus(VisualizedZkSoftWareEnum.ABNORMAL.getName()).abNormalMessage("C2机柜温度异常").build());
            return zkSoftWareServerRoomDtos;
        }catch (Throwable e){
            log.error("可视化查询中控机柜模型数据失败",e);
        }
        return null;
    }

    /**
     * 组合数据
     * @param assetsData
     * @param abNormalAssetsMessage
     */
    private  List<MwVisuZkSoftWareServerRoomDto> getRealData(List<MwTangibleassetsDTO> assetsData,Map<String, String> abNormalAssetsMessage){
        List<MwVisuZkSoftWareServerRoomDto> zkSoftWareServerRoomDtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(assetsData)){return zkSoftWareServerRoomDtos;}
        for (MwTangibleassetsDTO assetsDatum : assetsData) {
            if(StringUtils.isBlank(assetsDatum.getAssetsTypeSubName()) || assetsDatum.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName())){continue;}
            MwVisuZkSoftWareServerRoomDto softWareServerRoomDto = new MwVisuZkSoftWareServerRoomDto();
            softWareServerRoomDto.setServerRoomName(assetsDatum.getHostName());
            softWareServerRoomDto.setServerRoomStatus(assetsDatum.getItemAssetsStatus());
            if(StringUtils.isNotBlank(assetsDatum.getItemAssetsStatus()) && assetsDatum.getItemAssetsStatus().equals(VisualizedZkSoftWareEnum.NORMAL.getName()) && abNormalAssetsMessage.get(assetsDatum.getAssetsId()) != null){
                softWareServerRoomDto.setServerRoomStatus(VisualizedZkSoftWareEnum.WARNING.getName());
            }else{
                softWareServerRoomDto.setServerRoomStatus(assetsDatum.getItemAssetsStatus());
            }
            softWareServerRoomDto.setAbNormalMessage(abNormalAssetsMessage.get(assetsDatum.getAssetsId()));
            zkSoftWareServerRoomDtos.add(softWareServerRoomDto);
        }
        return zkSoftWareServerRoomDtos;
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
     * 获取异常资产的错误信息
     * @param assetsData
     */
    private  Map<String,String> getAbNormalAssetsMessage(List<MwTangibleassetsDTO> assetsData){
        Map<String,String> alertMessageMap = new HashMap<>();
        if(CollectionUtils.isEmpty(assetsData)){return alertMessageMap;}
//        Map<Integer, List<String>> hostMap = assetsData.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0 && StringUtils.isNotBlank(item.getAssetsId())
//                                                                            && StringUtils.isNotBlank(item.getItemAssetsStatus()) && !item.getItemAssetsStatus().equals(VisualizedZkSoftWareEnum.NORMAL.getName())
//                                                                            && !item.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName()))
//                .collect(Collectors.groupingBy(MwTangibleassetsDTO::getMonitorServerId, Collectors.mapping(MwTangibleassetsDTO::getAssetsId, Collectors.toList())));
        Map<Integer, List<String>> hostMap = assetsData.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0 && StringUtils.isNotBlank(item.getAssetsId())
                && !item.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName()))
                .collect(Collectors.groupingBy(MwTangibleassetsDTO::getMonitorServerId, Collectors.mapping(MwTangibleassetsDTO::getAssetsId, Collectors.toList())));
        log.info("中控机柜模型数据2"+hostMap);
        if(hostMap == null || hostMap.isEmpty()){return alertMessageMap;}
        AlertParam alertParam = new AlertParam();
        for (Integer serverId : hostMap.keySet()) {
            alertParam.setMonitorServerId(serverId);
            alertParam.setHostids(hostMap.get(serverId));
            List<ZbxAlertDto> currentAltertList = alertService.getCurrentAltertList(alertParam);
            log.info("中控机柜模型数据3"+currentAltertList);
            if(CollectionUtils.isEmpty(currentAltertList)){continue;}
            for (ZbxAlertDto zbxAlertDto : currentAltertList) {
                String hostid = zbxAlertDto.getHostid();
                if(alertMessageMap.containsKey(hostid)){
                    String message = alertMessageMap.get(hostid);
                    alertMessageMap.put(hostid,message + "," + zbxAlertDto.getName());
                }else{
                    alertMessageMap.put(hostid,zbxAlertDto.getName());
                }
            }
        }

        log.info("中控机柜模型数据4"+alertMessageMap);
        return alertMessageMap;
    }
}
