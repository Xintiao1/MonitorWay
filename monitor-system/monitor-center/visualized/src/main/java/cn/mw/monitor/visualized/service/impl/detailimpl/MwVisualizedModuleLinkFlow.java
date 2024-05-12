package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleLinkFlowDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 可视化组件流量数据
 * @Author gengjb
 * @Date 2023/6/20 15:59
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleLinkFlow implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Override
    public int[] getType() {
        return new int[]{92};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            if(CollectionUtils.isEmpty(assetsIds)){
                List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
                assetsIds.addAll(tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList()));
            }
            //获取监控信息并返回
            List<MwVisualizedCacheDto> cacheDtos = visualizedManageDao.selectvisualizedCacheInfos(assetsIds, moduleParam.getItemNames());
            if(CollectionUtils.isEmpty(cacheDtos)){return null;}
            for (MwVisualizedCacheDto cacheDto : cacheDtos) {
                String itemName = cacheDto.getItemName();
                if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
                cacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
                cacheDto.setItemName(itemName.split("]")[1]);
            }
            //按资产加接口数据分组
            Map<String, List<MwVisualizedCacheDto>> itemMap = cacheDtos.stream().collect(Collectors.groupingBy(item -> item.getName() + item.getAssetsId()));
            if(itemMap == null){return  null;}
            List<MwVisualizedModuleLinkFlowDto> mwVisualizedModuleLinkFlowDtos = new ArrayList<>();
            for (String itemName : itemMap.keySet()) {
                List<MwVisualizedCacheDto> dtos = itemMap.get(itemName);
                if(CollectionUtils.isEmpty(dtos)){continue;}
                MwVisualizedModuleLinkFlowDto linkFlowDto = new MwVisualizedModuleLinkFlowDto();
                for (MwVisualizedCacheDto dto : dtos) {
                    linkFlowDto.extractFrom(dto);
                }
                //设置排序值
                linkFlowDto.setSortValue((linkFlowDto.getFlowIn()==null?0:Double.parseDouble(linkFlowDto.getFlowIn())) +  (linkFlowDto.getFlowOut()==null?0:Double.parseDouble(linkFlowDto.getFlowOut())));
                mwVisualizedModuleLinkFlowDtos.add(linkFlowDto);
            }
            //排序
            valueSort(mwVisualizedModuleLinkFlowDtos);
            if(CollectionUtils.isEmpty(mwVisualizedModuleLinkFlowDtos) || mwVisualizedModuleLinkFlowDtos.size() <= 10){return mwVisualizedModuleLinkFlowDtos;}
            return mwVisualizedModuleLinkFlowDtos.subList(0, 10);
        }catch (Throwable e){
            log.error("MwVisualizedModuleLinkFlow{}",e);
            return null;
        }
    }


    private void valueSort(List<MwVisualizedModuleLinkFlowDto> mwVisualizedModuleLinkFlowDtos){
        Collections.sort(mwVisualizedModuleLinkFlowDtos, new Comparator<MwVisualizedModuleLinkFlowDto>() {
            @Override
            public int compare(MwVisualizedModuleLinkFlowDto o1, MwVisualizedModuleLinkFlowDto o2) {
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
}
