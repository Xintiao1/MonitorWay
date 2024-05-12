package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedResourceHostStatusDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedResourceHostAgentStatus
 * @Description 主机agent状态
 * @Author gengjb
 * @Date 2023/5/18 14:10
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedResourceHostAgentStatus implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    private final String units = "台";

    @Override
    public int[] getType() {
        return new int[]{67};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,true);
            log.info("MwVisualizedResourceHostAgentStatus{} getData()::tangibleassetsDTOS::"+tangibleassetsDTOS.size());
            //查询资产主机状态
            List<String> assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            List<MwVisualizedCacheDto> visualizedCacheDtos = new ArrayList<>();
            for (List<String> ids : partition) {
                visualizedCacheDtos.addAll(visualizedManageDao.selectvisualizedCacheInfos(ids, RackZabbixItemConstant.ITEM_HOST_STATUS));
            }
            log.info("MwVisualizedResourceHostAgentStatus{} getData()::visualizedCacheDtos::"+visualizedCacheDtos.size());
            //主机状态统计
            return hostStatusGroup(visualizedCacheDtos,tangibleassetsDTOS);
        }catch (Throwable e){
            log.error("MwVisualizedResourceHostAgentStatus{} getData::",e);
            return null;
        }
    }

    /**
     * 主机agent状态分组
     * @param cacheDtos
     */
    private MwVisualizedResourceHostStatusDto hostStatusGroup(List<MwVisualizedCacheDto> cacheDtos,List<MwTangibleassetsDTO> tangibleassetsDTOS){
        MwVisualizedResourceHostStatusDto hostStatusDto = new MwVisualizedResourceHostStatusDto();
        if(CollectionUtils.isEmpty(cacheDtos)){return hostStatusDto;}
        Map<String, List<MwVisualizedCacheDto>> listMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId()));
        List<MwVisualizedCacheDto> visualizedCacheDtos = handlerHostStatus(listMap);
        hostStatusDto.setSumValue(Double.parseDouble(String.valueOf(tangibleassetsDTOS.size())));
        hostStatusDto.setSumUnits(units);
        int normalCount = 0;
        for (MwVisualizedCacheDto visualizedCacheDto : visualizedCacheDtos) {
            String value = visualizedCacheDto.getValue();
            if(StringUtils.isNotBlank(value) && Double.parseDouble(value) == 1){
                normalCount++;
            }
        }
        hostStatusDto.setNormalValue(Double.parseDouble(String.valueOf(normalCount)));
        hostStatusDto.setNormalValueUnits(units);
        hostStatusDto.setAbnormalValue(Double.parseDouble(String.valueOf(tangibleassetsDTOS.size() - normalCount)));
        hostStatusDto.setAbnormalValueUnits(units);
        return hostStatusDto;
    }

    /**
     * 如果主机有AGENT_PING_STATUS和ICMP_PING,优先取AGENT_PING_STATUS
     * @param listMap
     */
    private List<MwVisualizedCacheDto> handlerHostStatus(Map<String, List<MwVisualizedCacheDto>> listMap){
        List<MwVisualizedCacheDto> newCacheDtos = new ArrayList<>();
        for (String assetsId : listMap.keySet()) {
            List<MwVisualizedCacheDto> cacheDtos = listMap.get(assetsId);
            if(CollectionUtils.isEmpty(cacheDtos)){continue;}
            if(cacheDtos.size() > 1){
                //按监控名称分组
                Map<String, List<MwVisualizedCacheDto>> itemNameMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
                List<MwVisualizedCacheDto> dtos = itemNameMap.get(RackZabbixItemConstant.MW_HOST_AVAILABLE);
                if(CollectionUtils.isNotEmpty(dtos)){
                    newCacheDtos.add(dtos.get(0));
                    continue;
                }
                List<MwVisualizedCacheDto> agentDtos = itemNameMap.get(RackZabbixItemConstant.AGENT_PING_STATUS);
                if(CollectionUtils.isNotEmpty(agentDtos)){
                    newCacheDtos.add(agentDtos.get(0));
                    continue;
                }
                newCacheDtos.add(cacheDtos.get(0));
            }else{
                newCacheDtos.add(cacheDtos.get(0));
            }
        }
        return newCacheDtos;
    }
}
