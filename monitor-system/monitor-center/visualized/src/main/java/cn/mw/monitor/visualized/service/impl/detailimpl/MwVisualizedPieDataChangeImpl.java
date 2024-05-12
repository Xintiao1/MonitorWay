package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dto.MwVisualizedZabbixDataDto;
import cn.mw.monitor.visualized.service.MwVisualizedDataChange;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedPieDataChangeImpl
 * @Description 可视化饼状环形图数据转换接口
 * @Author gengjb
 * @Date 2022/6/1 10:05
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedPieDataChangeImpl implements MwVisualizedDataChange {
    @Override
    public int[] getType() {
        return new int[]{7,8};
    }

    @Override
    public Object getData(Object data) {
        //将数据转换为集合
        List<MwVisualizedZabbixDataDto> zabbixDataDtos = (List<MwVisualizedZabbixDataDto>) data;
        if(CollectionUtils.isEmpty(zabbixDataDtos))return zabbixDataDtos;
        List<Map<String,Object>> realDatas = new ArrayList<>();
        for (MwVisualizedZabbixDataDto zabbixDataDto : zabbixDataDtos) {
            Map<String,Object> map = new HashMap<>();
            //设置饼状环形图的名称与值
            String units = zabbixDataDto.getUnits();
            Double avgValue = zabbixDataDto.getAvgValue();
            String assetsName = zabbixDataDto.getAssetsName();
            String interfaceName = zabbixDataDto.getInterfaceName();
            String fieldName = zabbixDataDto.getFieldName();
            if(avgValue == null){
                avgValue = 0.00;
            }
            if(StringUtils.isNotBlank(interfaceName)){
                map.put("name",assetsName+"["+interfaceName+"]"+fieldName);
                map.put("value",avgValue);
            }else{
                map.put("name",assetsName+"["+fieldName+"]");
                map.put("value",avgValue);
            }
            realDatas.add(map);
        }
        if(zabbixDataDtos.get(0).getIsExport() == 1){
            return exportExcelDataChange(realDatas);
        }
        return realDatas;
    }

    /**
     * 可视化导出数据
     * @param realDatas
     * @return
     */
    private List<Map<String,Object>> exportExcelDataChange( List<Map<String,Object>> realDatas){
        List<Map<String,Object>> mapList = new ArrayList<>();
        if(CollectionUtils.isEmpty(realDatas))return mapList;
        for (Map<String, Object> realData : realDatas) {
            Object name = realData.get("name");
            Object value = realData.get("value");
            Map<String,Object> map = new HashMap<>();
            if(name == null)continue;
            map.put(name.toString(),value);
            mapList.add(map);
        }
        return mapList;
    }
}
