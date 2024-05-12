package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedZabbixDataDto;
import cn.mw.monitor.visualized.service.MwVisualizedDataChange;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName MwVisualizedTableDataChangeImpl
 * @Author gengjb
 * @Date 2022/4/26 9:56
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedTableDataChangeImpl implements MwVisualizedDataChange {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Override
    public int[] getType() {
        return new int[]{5};
    }

    /**
     * 处理表格返回数据
     * @param data
     * @return
     */
    @Override
    public Object getData(Object data) {
        List<MwVisualizedZabbixDataDto> zabbixDataDtos = (List<MwVisualizedZabbixDataDto>) data;
        Map<String,List<String>> map = new HashMap<>();
        Map<String,List<MwVisualizedZabbixDataDto>> dtoMap = new HashMap<>();
        Map<String,String> hostNameMap = new HashMap<>();
        Set<String> itemNames = new HashSet<>();
        for (MwVisualizedZabbixDataDto zabbixDataDto : zabbixDataDtos) {
            String hostId = zabbixDataDto.getHostId();
            String interfaceName = zabbixDataDto.getInterfaceName();
            itemNames.add(zabbixDataDto.getName());
            hostNameMap.put(hostId,zabbixDataDto.getAssetsName()+","+zabbixDataDto.getIp());
            if(zabbixDataDto.getAvgValue() == null){
                zabbixDataDto.setAvgValue(0.0);
            }
            if(StringUtils.isBlank(interfaceName)){
                if(!map.isEmpty() && map.get(hostId) != null){
                    List<String> str = map.get(hostId);
                    str.add(zabbixDataDto.getName()+","+zabbixDataDto.getAvgValue()+zabbixDataDto.getUnits());
                    map.put(hostId,str);
                }else{
                    List<String> str =new ArrayList<>();
                    str.add(zabbixDataDto.getName()+","+zabbixDataDto.getAvgValue()+zabbixDataDto.getUnits());
                    map.put(hostId,str);
                }
            }else{
                if(!dtoMap.isEmpty() && dtoMap.get(hostId+","+interfaceName) != null){
                    List<MwVisualizedZabbixDataDto> dataDtos = dtoMap.get(hostId+","+interfaceName);
                    dataDtos.add(zabbixDataDto);
                    dtoMap.put(hostId+","+interfaceName,dataDtos);
                }else{
                    List<MwVisualizedZabbixDataDto> dataDtos = new ArrayList<>();
                    dataDtos.add(zabbixDataDto);
                    dtoMap.put(hostId+","+interfaceName,dataDtos);
                }
            }
        }
        Map<String,String> interfaceMap = new HashMap<>();
        List<Map<String,String>> realDatas = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (String hostid : dtoMap.keySet()) {
            List<MwVisualizedZabbixDataDto> zabbixDataDtoList = dtoMap.get(hostid);
            List<String> item = map.get(hostid.split(",")[0]);
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("assetsName",hostNameMap.get(hostid.split(",")[0]).split(",")[0]);
            dataMap.put("ip",hostNameMap.get(hostid.split(",")[0]).split(",")[1]);
            for (MwVisualizedZabbixDataDto zabbixDataDto : zabbixDataDtoList) {
                if(zabbixDataDto.getAvgValue() == null){
                    zabbixDataDto.setAvgValue(0.0);
                }
                dataMap.put(zabbixDataDto.getName(),zabbixDataDto.getAvgValue()+zabbixDataDto.getUnits());
            }
            dataMap.put("interfaceName",hostid.split(",")[1]);
            interfaceMap.put("prop","interfaceName");
            interfaceMap.put("name","接口名称");
            if(CollectionUtils.isNotEmpty(item)){
                ids.add(hostid.split(",")[0]);
                for (String str : item) {
                    dataMap.put(str.split(",")[0],str.split(",")[1]);
                }
            }
            realDatas.add(dataMap);
        }
        //删除已经匹配过的数据
        for (String hostId : map.keySet()) {
            if(ids.contains(hostId))continue;
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("assetsName",hostNameMap.get(hostId).split(",")[0]);
            dataMap.put("ip",hostNameMap.get(hostId).split(",")[1]);
            List<String> itemValues = map.get(hostId);
            for (String itemValue : itemValues) {
                dataMap.put(itemValue.split(",")[0],itemValue.split(",")[1]);
            }
            realDatas.add(dataMap);
        }

        Map<String,String> assetsMap = new HashMap<>();
        assetsMap.put("prop","assetsName");
        assetsMap.put("name","资产名称");
        Map<String,String> ipMap = new HashMap<>();
        ipMap.put("prop","ip");
        ipMap.put("name","IP地址");
        //查询字段名
        List<Map<String, String>> itemNameMaps = visualizedManageDao.selectItemName(Arrays.asList(itemNames.toArray(new String[0])));
        if(interfaceMap != null && !interfaceMap.isEmpty()){
            itemNameMaps.add(interfaceMap);
        }
        if(assetsMap != null && !assetsMap.isEmpty()){
            itemNameMaps.add(assetsMap);
        }
        if(ipMap != null && !ipMap.isEmpty()){
            itemNameMaps.add(ipMap);
        }
        Map<String,Object> realMap = new HashMap<>();
        realMap.put("data",realDatas);
        realMap.put("field",itemNameMaps);
        if(zabbixDataDtos.get(0).getIsExport() == 1){
            return exportExcelDataChange(realMap);
        }
        return realMap;
    }

    /**
     * 可视化导出数据
     * @param realMap
     * @return
     */
    private List<Map<String,Object>> exportExcelDataChange(Map<String,Object> realMap){
        List<Map<String,Object>> mapList = new ArrayList<>();
        if(realMap == null || realMap.isEmpty())return mapList;
        List<Map<String, String>> itemNameMaps = (List<Map<String, String>>) realMap.get("field");//字段名称与字段
        List<Map<String,String>> data = (List<Map<String, String>>) realMap.get("data");//数据
        for (Map<String, String> datum : data) {
            Map<String,Object> map = new HashMap<>();
            for (Map<String, String> itemNameMap : itemNameMaps) {
                String prop = itemNameMap.get("prop");
                String name = itemNameMap.get("name");
                String s = datum.get(prop);
                map.put(name,s);
            }
            mapList.add(map);
        }
        return mapList;
    }
}
