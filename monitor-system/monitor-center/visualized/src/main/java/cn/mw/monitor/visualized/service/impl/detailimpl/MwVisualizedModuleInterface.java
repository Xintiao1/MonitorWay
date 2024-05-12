package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualioduleInterfaceDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
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
 * @ClassName
 * @Description 接口信息组件
 * @Author gengjb
 * @Date 2023/5/19 9:46
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleInterface implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;


    @Override
    public int[] getType() {
        return new int[]{0};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            //获取ID集合
            List<String> assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = new ArrayList<>();
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            for (List<String> ids : partition) {
                mwVisualizedCacheDtos.addAll(visualizedManageDao.selectvisualizedCacheInfos(ids,moduleParam.getItemNames()));
            }
            if(CollectionUtils.isEmpty(mwVisualizedCacheDtos)){return null;}
            return handlerInterfaceInfo(mwVisualizedCacheDtos);
        }catch (Throwable e){
            log.error("MwVisualizedResourceClassify{} getData::",e);
            return null;
        }
    }

    /**
     * 处理接口信息
     * @param mwVisualizedCacheDtos
     */
    private List<MwVisualioduleInterfaceDto> handlerInterfaceInfo(List<MwVisualizedCacheDto> mwVisualizedCacheDtos){
        List<MwVisualioduleInterfaceDto> interfaceDtos = new ArrayList<>();
        //按照资产数据进行分组
        Map<String, List<MwVisualizedCacheDto>> interfaceMap = mwVisualizedCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId()));
        if(interfaceMap == null || interfaceMap.isEmpty()){return interfaceDtos;}
        for (Map.Entry<String, List<MwVisualizedCacheDto>> entry : interfaceMap.entrySet()) {
            List<MwVisualizedCacheDto> cacheDtos = entry.getValue();
            if(CollectionUtils.isEmpty(cacheDtos)){continue;}
            //将itemName中包含的中括号去除
            for (MwVisualizedCacheDto cacheDto : cacheDtos) {
                String itemName = cacheDto.getItemName();
                if(StringUtils.isBlank(itemName) || !itemName.contains("[")){continue;}
                cacheDto.setItemName(itemName.split("]")[1]);
            }
            //按照名称分组
            Map<String, List<MwVisualizedCacheDto>> listMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            if(listMap == null || listMap.isEmpty()){continue;}
            MwVisualioduleInterfaceDto interfaceDto = new MwVisualioduleInterfaceDto();
            interfaceDto.setName(cacheDtos.get(0).getAssetsName());
            for (String itemName : listMap.keySet()) {
                List<MwVisualizedCacheDto> dtos = listMap.get(itemName);
                double value = dtos.stream().map(MwVisualizedCacheDto::getValue).collect(Collectors.toList()).stream().mapToDouble(item -> Double.parseDouble(item)).reduce((a, b) -> a + b).getAsDouble();
                if(itemName.contains("IN")){
                    interfaceDto.setAcceptValue(String.valueOf(value));
                    interfaceDto.setAcceptUnits(dtos.get(0).getUnits());
                }
                if(itemName.contains("OUT")){
                    interfaceDto.setSendValue(String.valueOf(value));
                    interfaceDto.setSendUnits(dtos.get(0).getUnits());
                }
            }
            interfaceDtos.add(interfaceDto);
        }
    return interfaceDtos;
    }
}
