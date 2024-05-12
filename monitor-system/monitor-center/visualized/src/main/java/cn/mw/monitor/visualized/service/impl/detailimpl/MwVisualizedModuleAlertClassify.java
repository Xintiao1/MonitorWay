package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleAlertClassifyDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedAlertClassify
 * @Description 大屏告警分类信息统计
 * @Author gengjb
 * @Date 2023/4/17 9:55
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleAlertClassify implements MwVisualizedModule {

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Autowired
    private MWUserCommonService userService;

    @Override
    public int[] getType() {
        return new int[]{47};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            moduleParam.setIsFilterMonitorFlag(true);
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            log.info("可视化查询告警分类数据资产数据"+tangibleassetsDTOS);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            Map<Integer, List<String>> groupMap = tangibleassetsDTOS.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            List<ZbxAlertDto> alertDtos = new ArrayList<>();
            for (Integer serverId : groupMap.keySet()){
                List<String> assetsIds = groupMap.get(serverId);
                log.info("MwVisualizedModuleAlertClassify{} getData() assetsIds::"+assetsIds+":::serverId"+serverId);
                //查询告警信息
                AlertParam alertParam = new AlertParam();
                alertParam.setPageSize(Integer.MAX_VALUE);
                alertParam.setQueryHostIds(assetsIds);
                alertParam.setQueryMonitorServerId(serverId);
                alertParam.setUserId(userService.getAdmin());
                Reply reply = mwalertService.getCurrAlertPage(alertParam);
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(CollectionUtils.isEmpty(pageInfo.getList())){continue;}
                alertDtos.addAll(pageInfo.getList());
            }
            log.info("MwVisualizedModuleAlertClassify{} getData() propertiesIndexId:::"+moduleParam.getPropertiesIndexId()+":::alertDtos::"+alertDtos);
            //进行数据组装
            return handleTypeClassiftData(tangibleassetsDTOS,alertDtos);
        }catch (Throwable e){
            log.error("可视化组件区查询告警分类信息失败",e);
            return null;
        }
    }

    /**
     * 处理资产类型分类数据
     */
    private List<MwVisualizedModuleAlertClassifyDto> handleTypeClassiftData(List<MwTangibleassetsDTO> tangibleassetsDTOS,List<ZbxAlertDto> alertDtos){
        List<MwVisualizedModuleAlertClassifyDto> alertClassifyDtos = new ArrayList<>();
        if(alertDtos == null){
            alertDtos = new ArrayList<>();
        }
        log.info("可视化告警分类查询告警数据"+alertDtos.size());
        //按照资产类型进行资产数据分组
        Map<String, List<MwTangibleassetsDTO>> classAssetsDtos = tangibleassetsDTOS.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeName()));
        Map<String, List<ZbxAlertDto>> alertMap = alertDtos.stream().collect(Collectors.groupingBy(item -> item.getAlertType()));
        List<String> levels = new ArrayList<>(MWAlertLevelParam.alertLevelMap.values());
        for (Map.Entry<String, List<MwTangibleassetsDTO>> entry : classAssetsDtos.entrySet()) {
            String typeName = entry.getKey();
            List<MwTangibleassetsDTO> dtoList = entry.getValue();
            MwVisualizedModuleAlertClassifyDto alertClassifyDto = new MwVisualizedModuleAlertClassifyDto();
            alertClassifyDto.setTypeName(typeName);
            alertClassifyDto.setTotalCount(dtoList.size());
            alertClassifyDto.setOnLineCount(dtoList.stream().filter(item->item.getMonitorFlag() != null && item.getMonitorFlag()).collect(Collectors.toList()).size());
            Map<String,Integer> alertCountMap = new HashMap<>();
            if(alertMap.containsKey(typeName)){
                List<ZbxAlertDto> zbxAlertDtos = alertMap.get(typeName);
                for (ZbxAlertDto zbxAlertDto : zbxAlertDtos) {
                    String severity = zbxAlertDto.getSeverity();//告警等级
                    if(alertCountMap.containsKey(severity)){
                        Integer count = alertCountMap.get(severity);
                        alertCountMap.put(severity,count+1);
                    }else{
                        alertCountMap.put(severity,1);
                    }
                }
            }
            for (String level : levels) {
                if(!alertCountMap.containsKey(level) && !level.equals("未分类")){
                    alertCountMap.put(level,0);
                }
            }
            log.info("可视化告警分类分类数据"+typeName+":::"+alertCountMap);
            alertClassifyDto.extractFrom(alertCountMap);
            alertClassifyDtos.add(alertClassifyDto);
        }
        return alertClassifyDtos;
    }
}
