package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dto.MwVisualizedResourceClassifyDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
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
 * @ClassName
 * @Description 可视化资源分类
 * @Author gengjb
 * @Date 2023/5/17 9:58
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedResourceClassify implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Override
    public int[] getType() {
        return new int[]{64,89};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            //按照硬件进行资产分组
            return assetsHardWareGroup(tangibleassetsDTOS,moduleParam.getChartType());
        }catch (Throwable e){
            log.error("MwVisualizedResourceClassify{} getData::",e);
            return null;
        }
    }

    /**
     * 按照硬件进行数据分组
     */
    private List<MwVisualizedResourceClassifyDto> assetsHardWareGroup( List<MwTangibleassetsDTO> tangibleassetsDTOS,Integer chartType){
        List<MwVisualizedResourceClassifyDto> classifyDtos = new ArrayList<>();
        Map<String, List<MwTangibleassetsDTO>> assetsMap = new HashMap<>();
        if(chartType.equals(64)){
            assetsMap = tangibleassetsDTOS.stream().filter(item -> VisualizedConstant.HARD_WARE_TYPES.contains(item.getAssetsTypeName())).collect(Collectors.groupingBy(item -> item.getAssetsTypeName()));
        }else{
            assetsMap = tangibleassetsDTOS.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeName()));
        }
        if(assetsMap == null || assetsMap.isEmpty()){return classifyDtos;}
        for (Map.Entry<String, List<MwTangibleassetsDTO>> entry : assetsMap.entrySet()) {
            MwVisualizedResourceClassifyDto resourceClassifyDto = new MwVisualizedResourceClassifyDto();
            String typeName = entry.getKey();//名称
            List<MwTangibleassetsDTO> dtos = entry.getValue();
            resourceClassifyDto.setName(typeName);
            resourceClassifyDto.setValue(dtos.size());
            resourceClassifyDto.setUnits("台");
            classifyDtos.add(resourceClassifyDto);
        }
        return classifyDtos;
    }
}
