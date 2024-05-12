package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.dto.MwVisuZkSoftWareServerRoomDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import cn.mwpaas.common.model.Reply;
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
 * @author gengjb
 * @description TODO
 * @date 2023/7/26 14:13
 */
@Service
@Slf4j
public class MwVisualized3DCabinet  implements MwVisualizedZkSoftWare {

    @Autowired
    private MWAlertService alertService;

    @Autowired
    private MwModelCommonService commonService;

    @Override
    public int[] getType() {
        return new int[]{94};
    }

    @Override
    public Object getData() {
        try {
            //查询机柜资产
            Map<String, List<MwTangibleassetsDTO>> assetsMap = getAssetsData();
            log.info("MwVisualized3DCabinet{} getData():"+assetsMap);
            //获取所有资产数据
            if(assetsMap == null || assetsMap.isEmpty()){return null;}
            List<MwTangibleassetsDTO> assetsData = new ArrayList<>();
            for (String key : assetsMap.keySet()) {
                assetsData.addAll(assetsMap.get(key));
            }
            //调取异常数据信息
            Map<String, String> abNormalAssetsMessage = getAbNormalAssetsMessage(assetsData);
            List<MwVisuZkSoftWareServerRoomDto> zkSoftWareServerRoomDtos = getRealData(assetsMap, abNormalAssetsMessage);
            return zkSoftWareServerRoomDtos;
        }catch (Throwable e){
            log.error("可视化查询3D机柜模型数据失败",e);
        }
        return null;
    }


    /**
     * 组合数据
     * @param assetsMap
     * @param abNormalAssetsMessage
     */
    private  List<MwVisuZkSoftWareServerRoomDto> getRealData(Map<String, List<MwTangibleassetsDTO>> assetsMap,Map<String, String> abNormalAssetsMessage){
        List<MwVisuZkSoftWareServerRoomDto> zkSoftWareServerRoomDtos = new ArrayList<>();
        for (Map.Entry<String, List<MwTangibleassetsDTO>> entry : assetsMap.entrySet()) {
            MwVisuZkSoftWareServerRoomDto softWareServerRoomDto = new MwVisuZkSoftWareServerRoomDto();
            String cabinetName = entry.getKey();
            List<MwTangibleassetsDTO> tangibleassetsDTOS = entry.getValue();
            String[] split = cabinetName.split("_");
            if(split != null && split.length >= 2){//取机柜名称
                softWareServerRoomDto.setServerRoomName(cabinetName.split("_")[1]);
            }
            if(split != null && split.length >= 3){//取机组名称
                softWareServerRoomDto.setMachineSet(cabinetName.split("_")[2]);
            }
            softWareServerRoomDto.setTangibleassetsDTOList(tangibleassetsDTOS);
            StringBuffer buffer = new StringBuffer();
            for (MwTangibleassetsDTO tangibleassetsDTO : tangibleassetsDTOS) {
                String message = abNormalAssetsMessage.get(tangibleassetsDTO.getAssetsId());
                if(StringUtils.isNotBlank(message)){
                    String assetsName = tangibleassetsDTO.getAssetsName()==null?tangibleassetsDTO.getInstanceName():tangibleassetsDTO.getAssetsName();
                    buffer.append("资产："+assetsName+message+"\r\n");
                }
            }
            if(buffer != null && buffer.length() > 0){
                softWareServerRoomDto.setServerRoomStatus(VisualizedZkSoftWareEnum.ABNORMAL.getName());
                softWareServerRoomDto.setAbNormalMessage(buffer.toString());
                zkSoftWareServerRoomDtos.add(softWareServerRoomDto);
                continue;
            }
            softWareServerRoomDto.setServerRoomStatus(VisualizedZkSoftWareEnum.NORMAL.getName());
            zkSoftWareServerRoomDtos.add(softWareServerRoomDto);
        }
        return zkSoftWareServerRoomDtos;
    }

    /**
     * 获取机柜实例
     * @return
     */
    private Map<String, List<MwTangibleassetsDTO>> getAssetsData() throws Exception {
        Reply allInstanceInfoByCabinet = commonService.getAllInstanceInfoByCabinet();
        if(allInstanceInfoByCabinet == null){return null;}
        Map<String, List<MwTangibleassetsDTO>> assetsMap = (Map<String, List<MwTangibleassetsDTO>>) allInstanceInfoByCabinet.getData();
        return assetsMap;
    }


    /**
     * 获取异常资产的错误信息
     * @param assetsData
     */
    private  Map<String,String> getAbNormalAssetsMessage(List<MwTangibleassetsDTO> assetsData){
        Map<String,String> alertMessageMap = new HashMap<>();
        if(CollectionUtils.isEmpty(assetsData)){return alertMessageMap;}
        Map<Integer, List<String>> hostMap = assetsData.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0 && StringUtils.isNotBlank(item.getAssetsId()))
                .collect(Collectors.groupingBy(MwTangibleassetsDTO::getMonitorServerId, Collectors.mapping(MwTangibleassetsDTO::getAssetsId, Collectors.toList())));
        if(hostMap == null || hostMap.isEmpty()){return alertMessageMap;}
        AlertParam alertParam = new AlertParam();
        for (Integer serverId : hostMap.keySet()) {
            alertParam.setMonitorServerId(serverId);
            alertParam.setHostids(hostMap.get(serverId));
            List<ZbxAlertDto> currentAltertList = alertService.getCurrentAltertList(alertParam);
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
        return alertMessageMap;
    }
}
