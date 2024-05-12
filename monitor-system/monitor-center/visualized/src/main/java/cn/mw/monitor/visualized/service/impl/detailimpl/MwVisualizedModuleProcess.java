package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleProcessDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareItemEnum;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 可视化进程信息
 * @date 2023/9/14 15:46
 */
@Service
@Slf4j
public class MwVisualizedModuleProcess implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    private final String FW = "FW_";

    @Override
    public int[] getType() {
        return new int[]{102};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            //查询数据库缓存数据
            List<String> assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            List<MwVisualizedCacheDto> cacheDtoList = new ArrayList<>();
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            for (List<String> ids : partition) {
                cacheDtoList.addAll(visualizedManageDao.selectvisualizedCacheInfos(ids, moduleParam.getItemNames()));
            }
            if(CollectionUtils.isEmpty(cacheDtoList)){return null;}
            for (MwVisualizedCacheDto cacheDto : cacheDtoList) {
                String itemName = cacheDto.getItemName();
                if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
                cacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
                cacheDto.setItemName(itemName.split("]")[1]);
            }
            return handlerProcessInfo(cacheDtoList);
        }catch (Throwable e){
            log.error("MwVisualizedModuleProcess{} getData() error",e);
            return null;
        }
    }

    /**
     * 处理进程数据信息
     * @param cacheDtoList
     */
    private List<MwVisualizedModuleProcessDto> handlerProcessInfo(List<MwVisualizedCacheDto> cacheDtoList) throws Exception{
        List<MwVisualizedModuleProcessDto> processDtos = new ArrayList<>();
        Map<String, List<MwVisualizedCacheDto>> processNameMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getName()));
        for (Map.Entry<String, List<MwVisualizedCacheDto>> entry : processNameMap.entrySet()) {
            MwVisualizedModuleProcessDto moduleProcessDto = new MwVisualizedModuleProcessDto();
            String name = entry.getKey();
            if(name.contains(FW)){
                moduleProcessDto.setProcessName(name.replace(FW,""));
            }else{
                moduleProcessDto.setProcessName(name);
            }
            for (MwVisualizedCacheDto mwVisualizedCacheDto : entry.getValue()) {
                String proPerty = VisualizedZkSoftWareItemEnum.getProPerty(mwVisualizedCacheDto.getItemName());
                if(StringUtils.isBlank(proPerty)){continue;}
                Field field = moduleProcessDto.getClass().getDeclaredField(proPerty);
                field.setAccessible(true);
                if(StringUtils.isNotBlank(mwVisualizedCacheDto.getUnits())){
                    field.set(moduleProcessDto,mwVisualizedCacheDto.getValue()+mwVisualizedCacheDto.getUnits());
                }else{
                    field.set(moduleProcessDto,mwVisualizedCacheDto.getValue());
                }
            }
            processDtos.add(moduleProcessDto);
        }
        return processDtos;
    }
}
