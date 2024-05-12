package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.virtual.dto.VirtualizationMonitorInfo;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleVDIResourseLoadDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description VDI资源负载组件
 * @Author gengjb
 * @Date 2023/4/25 9:42
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleVDIResourseLoad implements MwVisualizedModule {

    @Autowired
    private MwModelViewCommonService modelViewCommonService;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    private final String rediskey = "VISUALIZED_VDI";

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Override
    public int[] getType() {
        return new int[]{58};
    }

    @Override
    public Object getData(Object data) {
        try {
            //查询虚拟化信息
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            if(CollectionUtils.isNotEmpty(tangibleassetsDTOS)){
                List<String> ids = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
                Iterator<String> iterator = assetsIds.iterator();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    if(!ids.contains(next)){
                        iterator.remove();
                    }
                }
            }
            if(StringUtils.isBlank(moduleParam.getAssetsId()) && CollectionUtils.isEmpty(moduleParam.getAssetsIds())){
                assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            }
            log.info("可视化VDI数据查询22"+data);
            String redislist = redisTemplate.opsForValue().get(rediskey);
            List<VirtualizationMonitorInfo> allVirtualInfoByMonitorData = new ArrayList<>();
            if(StringUtils.isNotBlank(redislist)){
                allVirtualInfoByMonitorData = JSONObject.parseArray(redislist, VirtualizationMonitorInfo.class);
            }else{
                allVirtualInfoByMonitorData = modelViewCommonService.getAllVirtualInfoByMonitorData();
            }
            log.info("可视化VDI数据查询"+allVirtualInfoByMonitorData);
            if(CollectionUtils.isEmpty(allVirtualInfoByMonitorData)){return null;}
            List<MwVisualizedModuleVDIResourseLoadDto> vdiResourseLoadDtos = new ArrayList<>();
            for (VirtualizationMonitorInfo allVirtualInfoByMonitorDatum : allVirtualInfoByMonitorData) {
                MwVisualizedModuleVDIResourseLoadDto resourseLoadDto = new MwVisualizedModuleVDIResourseLoadDto();
                resourseLoadDto.setName(allVirtualInfoByMonitorDatum.getInstanceName());
                resourseLoadDto.setCpuCapacity(allVirtualInfoByMonitorDatum.getTotalCPU());
                resourseLoadDto.setMemoryCapacity(allVirtualInfoByMonitorDatum.getTotalMemory());
                resourseLoadDto.setStorageCapacity(allVirtualInfoByMonitorDatum.getTotalStorage());
                if(!assetsIds.contains(String.valueOf(allVirtualInfoByMonitorDatum.getInstanceId()))){continue;}
                vdiResourseLoadDtos.add(resourseLoadDto);
            }
            return vdiResourseLoadDtos;
        }catch (Throwable e){
            log.error("可视化组件区查询VDI资源负载失败",e);
            return null;
        }
    }
}
