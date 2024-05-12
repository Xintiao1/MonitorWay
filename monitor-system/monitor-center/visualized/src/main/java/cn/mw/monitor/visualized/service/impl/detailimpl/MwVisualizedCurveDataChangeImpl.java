package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dto.MwVisualizedZabbixDataDto;
import cn.mw.monitor.visualized.dto.MwVisualizedZabbixHistoryDto;
import cn.mw.monitor.visualized.service.MwVisualizedDataChange;
import cn.mw.monitor.visualized.util.MwVisualizedUnitChangeUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MwVisualizedCurveDataChangeImpl
 * @Description 曲线数据转换
 * @Author gengjb
 * @Date 2022/5/25 19:03
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedCurveDataChangeImpl implements MwVisualizedDataChange {
    @Override
    public int[] getType() {
        return new int[]{3,4};
    }

    @Override
    public Object getData(Object data) {
        //将数据转换为线的格式结构
        List<MwVisualizedZabbixDataDto> zabbixDataDtos = (List<MwVisualizedZabbixDataDto>) data;
        if(CollectionUtils.isEmpty(zabbixDataDtos))return zabbixDataDtos;
        List<Map<String,Object>> realData = new ArrayList<>();
        Set<Date> dateSets = new HashSet<>();
        for (MwVisualizedZabbixDataDto zabbixDataDto : zabbixDataDtos) {
            Map<String,Object> dataMap = new HashMap<>();
            String name = zabbixDataDto.getName();//监控项名称
            String fieldName = zabbixDataDto.getFieldName();//字段名称
            String assetsName = zabbixDataDto.getAssetsName();//资产名称
            List<MwVisualizedZabbixHistoryDto> values = zabbixDataDto.getValues();//折线图数据
            String units = zabbixDataDto.getOriginUtits();
            dataMap.put("units",units);
            Map<Date,Object> map = new HashMap<>();
            //进行单位转换
            if(CollectionUtils.isNotEmpty(values) && StringUtils.isNotBlank(units)){
                for (MwVisualizedZabbixHistoryDto value : values) {
                    dateSets.add(value.getClock());
                    Map<String, String> unitMap = MwVisualizedUnitChangeUtil.changeUnit(units, value.getValue().toString());
                    if(unitMap == null){
                        map.put(value.getClock(),new BigDecimal(value.getValue()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue());
                    }else{
                        value.setValue(Double.parseDouble(unitMap.get("value")));
                        dataMap.put("units",unitMap.get("units"));
                        map.put(value.getClock(),new BigDecimal(Double.parseDouble(unitMap.get("value"))).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue());
                    }
                }
            }
            List<String> colors = new ArrayList<>();
            colors.add("#"+String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10,5))));
            colors.add("#"+String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10,5))));
            dataMap.put("itemName",name);
            dataMap.put("realData",map);
            dataMap.put("interfaceName",zabbixDataDto.getInterfaceName());
            dataMap.put("name",fieldName);
            dataMap.put("assetsName",assetsName);
            dataMap.put("data",values);
            dataMap.put("color",colors);
            realData.add(dataMap);
        }
        List<Date> sortDates = Arrays.asList(dateSets.toArray(new Date[0]));
        //将时间集合排序
        Collections.sort(sortDates);
        if(realData != null){
            for (Map<String, Object> realDatum : realData) {
                realDatum.put("dates",sortDates);
            }
        }
        if(zabbixDataDtos.get(0).getIsExport() == 1){
            return exportExcelDataChange(realData);
        }
        return realData;
    }

    /**
     * 可视化导出数据
     * @param realDatas
     * @return
     */
    private List<Map<String,Object>> exportExcelDataChange( List<Map<String,Object>> realDatas){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String,Object>> mapList = new ArrayList<>();
        if(CollectionUtils.isEmpty(realDatas))return mapList;
        for (Map<String, Object> realData : realDatas) {
            Object assetsName = realData.get("assetsName");
            Object name = realData.get("name");
            Object data = realData.get("realData");
            Object interfaceName = realData.get("interfaceName");
            Object units = realData.get("units");
            if(data == null)continue;
            Map<Date,Object> oldValueMap = (Map<Date, Object>) data;
            for (Date date : oldValueMap.keySet()) {
                Map<String,Object> map = new HashMap<>();
                Object value = oldValueMap.get(date);
                map.put("资产名称",assetsName);
                map.put("监控项名称",name);
                if(interfaceName != null && StringUtils.isNotBlank(interfaceName.toString())){
                    map.put("接口名称",interfaceName);
                }
                map.put("日期",format.format(date));
                map.put("值",value.toString()+units.toString());
                mapList.add(map);
            }
        }
        return mapList;
    }
}
