package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dto.MwVisualizedAssetsDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gengjb
 * @description 或者资产状态信息
 * @date 2023/9/11 10:17
 */
@Service
@Slf4j
public class MwVisualizedAssetsStatus implements MwVisualizedModule {


    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Override
    public int[] getType() {
        return new int[]{105};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,true);
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            List<MwVisualizedAssetsDto> assetsDtos = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(assetsIds)){
                for (MwTangibleassetsDTO tangibleassetsDTO : tangibleassetsDTOS) {
                    String id = tangibleassetsDTO.getId()==null?String.valueOf(tangibleassetsDTO.getModelInstanceId()):tangibleassetsDTO.getId();
                    if(assetsIds.contains(id)){
                        MwVisualizedAssetsDto assetsDto = new MwVisualizedAssetsDto();
                        assetsDto.setAssetsName(tangibleassetsDTO.getAssetsName()==null?tangibleassetsDTO.getInstanceName():tangibleassetsDTO.getAssetsName());
                        assetsDto.setAssetsStatus(tangibleassetsDTO.getItemAssetsStatus());
                        assetsDtos.add(assetsDto);
                    }
                }
                return assetsDtos;
            }
            for (MwTangibleassetsDTO tangibleassetsDTO : tangibleassetsDTOS) {
                MwVisualizedAssetsDto assetsDto = new MwVisualizedAssetsDto();
                assetsDto.setAssetsName(tangibleassetsDTO.getAssetsName()==null?tangibleassetsDTO.getInstanceName():tangibleassetsDTO.getAssetsName());
                assetsDto.setAssetsStatus(tangibleassetsDTO.getItemAssetsStatus());
                assetsDtos.add(assetsDto);
            }
            return assetsDtos;
        }catch (Throwable e){
            log.error("MwVisualizedAssetsStatus{} getData() ERROR::",e);
            return null;
        }

    }
}
