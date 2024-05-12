package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dto.MwVisualizedResourceAssetsSpreadDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 资产分布
 * @Author gengjb
 * @Date 2023/5/18 16:17
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedResourceAssetsSpread implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    public static final String ASSETS_TYPE = "资产类型分布";

    public static final String ASSETS_MANUFACTURER = "品牌分布";

    public static final String ASSETS_SYSTEM = "系统分布";

    public static final String SERVER_ASSETS = "服务器";

    @Value("${visualized.environment}")
    private String environment;

    @Override
    public int[] getType() {
        return new int[]{70};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            if(StringUtils.isNotBlank(environment) && !"MW".equals(environment)){
                return handlerAssetsSystemInfo(tangibleassetsDTOS);
            }
            return handlerAssetsSpreadInfo(tangibleassetsDTOS);
        }catch (Throwable e){
            log.error("MwVisualizedResourceAssetsSpread{} getData::",e);
            return null;
        }
    }

    /**
     * 处理资产分布信息
     * @param tangibleassetsDTOS
     * @return
     */
    private List<MwVisualizedResourceAssetsSpreadDto> handlerAssetsSpreadInfo(List<MwTangibleassetsDTO> tangibleassetsDTOS){
        List<MwVisualizedResourceAssetsSpreadDto> assetsSpreadDtos = new ArrayList<>();
        //按照类型分组
        Map<String, List<MwTangibleassetsDTO>> assetsTypeGroupMap = tangibleassetsDTOS.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeName()));
        MwVisualizedResourceAssetsSpreadDto assetsTypeSpreadDto = calculatedData(assetsTypeGroupMap, tangibleassetsDTOS.size());
        assetsTypeSpreadDto.setName(ASSETS_TYPE);
        assetsSpreadDtos.add(assetsTypeSpreadDto);
        //按照品牌分组
        Map<String, List<MwTangibleassetsDTO>> assetsManufacturerGroupMap = tangibleassetsDTOS.stream().filter(item->item.getManufacturer() != null).collect(Collectors.groupingBy(item -> item.getManufacturer()));
        MwVisualizedResourceAssetsSpreadDto assetsManufacturerSpreadDto = calculatedData(assetsManufacturerGroupMap, tangibleassetsDTOS.size());
        assetsManufacturerSpreadDto.setName(ASSETS_MANUFACTURER);
        assetsSpreadDtos.add(assetsManufacturerSpreadDto);
        return assetsSpreadDtos;
    }


    /**
     * 按照服务器类型分组
     * @param tangibleassetsDTOS
     * @return
     */
    private List<MwVisualizedResourceAssetsSpreadDto> handlerAssetsSystemInfo(List<MwTangibleassetsDTO> tangibleassetsDTOS){
        Map<String, List<MwTangibleassetsDTO>> assetsTypeGroupMap = tangibleassetsDTOS.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeName()));
        //获取服务器资产
        List<MwTangibleassetsDTO> mwTangibleassetsDTOS = assetsTypeGroupMap.get(SERVER_ASSETS);
        if(CollectionUtils.isEmpty(mwTangibleassetsDTOS)){return null;}
        //按照规格型号分组
        Map<String, List<MwTangibleassetsDTO>> listMap = mwTangibleassetsDTOS.stream().filter(item->StringUtils.isNotBlank(item.getSpecifications())).collect(Collectors.groupingBy(item -> item.getSpecifications()));
        MwVisualizedResourceAssetsSpreadDto assetsManufacturerSpreadDto = calculatedData(listMap, tangibleassetsDTOS.size());
        assetsManufacturerSpreadDto.setName(ASSETS_SYSTEM);
        return Arrays.asList(assetsManufacturerSpreadDto);
    }

    /**
     * 计算百分比
     * @param assetsGroupMap
     * @param sumNumber
     */
    private MwVisualizedResourceAssetsSpreadDto calculatedData(Map<String, List<MwTangibleassetsDTO>> assetsGroupMap,int sumNumber){
        MwVisualizedResourceAssetsSpreadDto assetsSpreadDto = new MwVisualizedResourceAssetsSpreadDto();
        if(assetsGroupMap == null || assetsGroupMap.isEmpty()){return assetsSpreadDto;}
        List<MwVisualizedResourceAssetsSpreadDto.SpreadDetailDto> detailDtos = new ArrayList<>();
        for (String name : assetsGroupMap.keySet()) {
            List<MwTangibleassetsDTO> tangibleassetsDTOS = assetsGroupMap.get(name);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){continue;}
            MwVisualizedResourceAssetsSpreadDto.SpreadDetailDto detailDto = assetsSpreadDto.new SpreadDetailDto();
            detailDto.setTypeName(name);
            detailDto.setValue(new BigDecimal(String.valueOf((Double.parseDouble(String.valueOf(tangibleassetsDTOS.size())) / Double.parseDouble(String.valueOf(sumNumber)))*100)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()+VisualizedConstant.PER_CENT);
            detailDto.setDValue(new BigDecimal(String.valueOf((Double.parseDouble(String.valueOf(tangibleassetsDTOS.size())) / Double.parseDouble(String.valueOf(sumNumber)))*100)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            detailDtos.add(detailDto);
        }
        assetsSpreadDto.setSpreadDetailDtos(detailDtos);
        return assetsSpreadDto;
    }
}
