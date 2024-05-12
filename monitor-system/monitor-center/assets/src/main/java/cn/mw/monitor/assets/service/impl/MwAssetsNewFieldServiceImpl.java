package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwAssetsNewFieldDao;
import cn.mw.monitor.assets.dto.MwAssetsCustomFieldDto;
import cn.mw.monitor.assets.service.MwAssetsNewFieldService;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwAssetsNewFieldServiceImpl
 * @Author gengjb
 * @Date 2022/7/5 10:41
 * @Version 1.0
 **/
@Service
@Slf4j
@Transactional
public class MwAssetsNewFieldServiceImpl implements MwAssetsNewFieldService {

    @Resource
    private MwAssetsNewFieldDao assetsNewFieldDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    /**
     * 资产新增自定义字段
     * @param customFieldDto
     * @return
     */
    @Override
    public Reply addAssetsCustomField(MwAssetsCustomFieldDto customFieldDto) {
        try {
            if(StringUtils.isBlank(customFieldDto.getLabel())){
                return Reply.fail("字段名称不可为空");
            }
            if(!onlyCheck(customFieldDto.getProp(),null)){
                return Reply.fail("该字段已经存在");
            }
            assetsNewFieldDao.insertAssetsCustomField(customFieldDto);
            return Reply.ok(customFieldDto);
        }catch (Throwable e){
            log.error("增加资产自定义字段失败", e);
            return Reply.fail("增加资产自定义字段失败,失败信息："+e.getMessage());
        }
    }

    /**
     * 资产删除自定义字段
     * @param customFieldDto
     * @return
     */
    @Override
    public Reply deleteAssetsCustomField(MwAssetsCustomFieldDto customFieldDto) {
        try {
            assetsNewFieldDao.deleteAssetsCustomField(customFieldDto.getIds());
            return Reply.ok("删除成功");
        }catch (Throwable e){
            log.error("删除资产自定义字段失败", e);
            return Reply.fail("删除资产自定义字段失败,失败信息："+e.getMessage());
        }
    }

    /**
     * 资产修改自定义字段
     * @param customFieldDto
     * @return
     */
    @Override
    public Reply updateAssetsCustomField(MwAssetsCustomFieldDto customFieldDto) {
        try {
            if(StringUtils.isBlank(customFieldDto.getLabel())){
                return Reply.fail("字段名称不可为空");
            }
            if(!onlyCheck(customFieldDto.getProp(),customFieldDto.getColId())){
                return Reply.fail("该字段已经存在");
            }
            assetsNewFieldDao.updateAssetsCustomField(customFieldDto);
            return Reply.ok("修改成功");
        }catch (Throwable e){
            log.error("修改资产自定义字段失败", e);
            return Reply.fail("修改资产自定义字段失败,失败信息："+e.getMessage());
        }
    }


    /**
     * 资产查询自定义字段
     * @param customFieldDto
     * @return
     */
    @Override
    public Reply selectAssetsCustomField(MwAssetsCustomFieldDto customFieldDto) {
        try {
            List<MwAssetsCustomFieldDto> mwAssetsCustomFieldDtos = assetsNewFieldDao.selectAssetsCustomField();
            return Reply.ok(mwAssetsCustomFieldDtos);
        }catch (Throwable e){
            log.error("查询资产自定义字段失败", e);
            return Reply.fail("查询资产自定义字段失败,失败信息："+e.getMessage());
        }
    }

    /**
     * 获取自定义字段的值
     * @param mwTangAssetses
     */
    @Override
    public void getAssetsCustomFieldValue(List<MwTangibleassetsTable> mwTangAssetses){
        log.info("有形资产查询自定义字段2"+new Date());
        //获取需要查询的字段
        List<MwAssetsCustomFieldDto> mwAssetsCustomFieldDtos = assetsNewFieldDao.selectAssetsCustomField();
        if(CollectionUtils.isEmpty(mwAssetsCustomFieldDtos) || CollectionUtils.isEmpty(mwTangAssetses))return;
        List<MwAssetsCustomFieldDto> labelAssetsCustomFieldDtos = new ArrayList<>();
        //数据分组
        Map<Integer, List<String>> groupMap = mwTangAssetses.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        List<String> itemNames = new ArrayList<>();
        Map<String,String> customFieldMap = new HashMap<>();
        //取出监控项名称与字段名称
        mwAssetsCustomFieldDtos.forEach(value ->{
            if(value.getType() == 1){
                itemNames.add(value.getProp());
                customFieldMap.put(value.getProp(),value.getLabel());
            }else{
                labelAssetsCustomFieldDtos.add(value);
            }
        });
        Map<String,BigDecimal> diskTotal = new HashMap<>();
        Map<String,List<String>> map = new HashMap<>();
        log.info("有形资产查询自定义字段监控项start"+new Date()+" 参数为:"+groupMap);
        if(groupMap != null && CollectionUtils.isNotEmpty(itemNames)){
            //根据分组查询zabbix接口获取数据
            for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                log.info("有形资产查询自定义字段监控项2"+new Date()+" 参数为:"+value.getKey()+"："+itemNames+":"+ value.getValue());
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(value.getKey(), itemNames, value.getValue());
                log.info("有形资产查询自定义字段监控项3"+new Date()+" 参数为:"+result);
                if (result!=null && !result.isFail()){
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0){
                        for (JsonNode node : jsonNode){
                            String lastvalue = node.get("lastvalue").asText();//最新值
                            String hostId = node.get("hostid").asText();//主机ID
                            String units = "";//单位
                            if(node.get("units") != null){
                                units = node.get("units").asText();
                            }
                            String name = node.get("name").asText();//监控名称
                            if(lastvalue == null || StringUtils.isBlank(hostId))continue;
                            if(name.contains("MW_DISK_TOTAL") && StringUtils.isNotBlank(lastvalue)){
                                if(diskTotal.containsKey(hostId)){
                                    BigDecimal bigDecimal = diskTotal.get(hostId);
                                    BigDecimal decimal = bigDecimal.add(new BigDecimal(lastvalue));
                                    diskTotal.put(hostId,decimal);
                                }else{
                                    diskTotal.put(hostId,new BigDecimal(lastvalue));
                                }
                            }
                            if(map != null && map.get(hostId) != null){
                                List<String> values = map.get(hostId);
                                values.add(lastvalue+units+","+name);
                                map.put(hostId,values);
                            }else{
                                List<String> values = new ArrayList<>();
                                values.add(lastvalue+units+","+name);
                                map.put(hostId,values);
                            }
                        }
                    }
                }
            }
        }
        log.info("有形资产查询自定义字段监控项end"+new Date());
        //设置资产自定义字段值
        if(map != null && !map.isEmpty()){
            for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
                Map<String,String> assetsMap = new HashMap<>();
                String assetsId = mwTangAssets.getAssetsId();
                List<String> values = map.get(assetsId);
                if(CollectionUtils.isEmpty(values))continue;
                values.forEach(value->{
                    for (String key : customFieldMap.keySet()) {
                        if((value.split(",")[1]).contains(key)){
                            if(key.equals("MW_DISK_TOTAL") && diskTotal.containsKey(assetsId)){
                                BigDecimal bigDecimal = diskTotal.get(assetsId);
                                assetsMap.put(key,bigDecimal.toString()+"B");
                            }else{
                                assetsMap.put(key,value.split(",")[0]);
                            }
                            continue;
                        }
                    }
                });
                //进行单位转换
                if(!assetsMap.isEmpty()){
                    for (String name : assetsMap.keySet()) {
                        String value = assetsMap.get(name);
                        Map<String, String> changeUnitMap = null;
                        if(StringUtils.isNotBlank(value) && value.endsWith("B")){
                            changeUnitMap = changeUnit("B", value.split("B")[0]);
                        }
                        if(StringUtils.isNotBlank(value) && value.endsWith("bps")){
                            changeUnitMap = changeUnit("bps",value.split("bps")[0]);
                        }
                        if(changeUnitMap != null){
                            assetsMap.put(name,changeUnitMap.get("value")+changeUnitMap.get("units"));
                        }
                    }
                }
                mwTangAssets.setCustomFieldValue(assetsMap);
            }
        }
        log.info("有形资产查询自定义字段4"+new Date());
        //处理标签自定义字段
        handleLabelCustomField(labelAssetsCustomFieldDtos,mwTangAssetses);
        log.info("有形资产查询自定义字段5"+new Date());
    }

    /**
     * 处理标签自定义字段
     * @param labelAssetsCustomFieldDtos
     */
    private void handleLabelCustomField(List<MwAssetsCustomFieldDto> labelAssetsCustomFieldDtos,List<MwTangibleassetsTable> mwTangAssetses){
        if(CollectionUtils.isEmpty(labelAssetsCustomFieldDtos) || CollectionUtils.isEmpty(mwTangAssetses))return;
        //获取标签名称集合
        List<String> labelNames = new ArrayList<>();
        labelAssetsCustomFieldDtos.forEach(value->{
            labelNames.add(value.getProp());
        });
        if(CollectionUtils.isEmpty(labelNames))return;
        //根据标签名称查询资产对应标签信息
        log.info("有形资产查询自定义字段6"+new Date());
        List<Map<String, Object>> labelMaps = assetsNewFieldDao.selectAssetsLabelByLabelName(labelNames);
        log.info("有形资产查询自定义字段7"+new Date());
        if(CollectionUtils.isEmpty(labelMaps))return;
        for (Map<String, Object> map : labelMaps){
            Object typeId = map.get("typeId");//资产ID
            Object labelName = map.get("labelName");//标签名称
            Object labelValue = map.get("labelValue");//标签值
            for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
                if(typeId == null || labelName == null || labelValue == null || !mwTangAssets.getId().equals(typeId.toString()))continue;
                Map<String, String> customFieldValue = mwTangAssets.getCustomFieldValue();
                if(customFieldValue == null){
                    customFieldValue = new HashMap<>();
                }
                customFieldValue.put(labelName.toString(),labelValue.toString());
                mwTangAssets.setCustomFieldValue(customFieldValue);
            }
        }
        log.info("有形资产查询自定义字段8"+new Date());
    }

    /**
     * 获取资产的所有标签
     */
    @Override
    public Map<String,String> getAssetsAllLabel() {
        List<Map<String, Object>> labelMap = assetsNewFieldDao.selectAllAssetsLabel();//所有资产标签
        Map<String,String> assetsLabel = new HashMap<>();
        if(CollectionUtils.isEmpty(labelMap))return assetsLabel;
        for (Map<String, Object> map : labelMap) {
            Object typeId = map.get("typeId");//资产ID
            Object labelName = map.get("labelName");//标签名称
            Object labelValue = map.get("labelValue");//标签值
            if(typeId == null || labelName == null || labelValue == null)continue;
            //将同资产的标签进行拼接
            if(assetsLabel != null && assetsLabel.get(typeId.toString()) != null){
                //已经存在的数据，将新标签与原来的标签拼接，以"\"分割
                String label = assetsLabel.get(typeId.toString());
                assetsLabel.put(typeId.toString(),label+"/"+labelName+":"+labelValue);
            }else{
                assetsLabel.put(typeId.toString(),labelName+":"+labelValue);
            }
        }
        return assetsLabel;
    }

    /**
     * 资产字段排序
     * @param customColDTO
     * @return
     */
    @Override
    public Reply assetsFieldSort(List<UpdateCustomColDTO> customColDTO) {
        if(CollectionUtils.isEmpty(customColDTO))return null;
        List<MwCustomColDTO> sortDto = new ArrayList<>();//系统字段数据
        List<MwAssetsCustomFieldDto> fieldDtos = new ArrayList<>();//自定义字段数据
        Iterator<UpdateCustomColDTO> iterator = customColDTO.iterator();
        while(iterator.hasNext()){
            UpdateCustomColDTO mwCustomColDTO = iterator.next();
            Integer customFieldType = mwCustomColDTO.getCustomFieldType();
            if(customFieldType != null && customFieldType == 1){
                MwAssetsCustomFieldDto dto = new MwAssetsCustomFieldDto();
                dto.setOrderNumber(mwCustomColDTO.getOrderNumber());
                dto.setColId(mwCustomColDTO.getColId());
                fieldDtos.add(dto);
                iterator.remove();
            }
        }
        //进行字段顺序修改
        if(CollectionUtils.isNotEmpty(customColDTO)){//修改系统字段顺序
            assetsNewFieldDao.updateAssetsSysFieldOrder(customColDTO);
        }
        if(CollectionUtils.isNotEmpty(fieldDtos)){//修改自定义字段顺序
            assetsNewFieldDao.updateAssetsCustomFieldOrder(fieldDtos);
        }
        return Reply.ok();
    }

    /**
     * 自定义自定唯一性校验
     * @param itemName
     * @return
     */
    private boolean onlyCheck(String itemName,Integer id){
        int count = assetsNewFieldDao.selectCustomFieldCount(itemName, id);
        if(count == 0)return true;
        return false;
    }

    /**
     * 转换单位
     * @param unit 原来的单位
     * @param value 需转换的值
     * @return 返回新单位与新值
     */
    private Map<String,String> changeUnit(String unit, String value){
        if("B".equals(unit)){
            if(StringUtils.isNotBlank(value) && Double.parseDouble(value) > 10000){
                return UnitsUtil.getValueMap(value,"GB",unit);
            }else{
                return UnitsUtil.getValueMap(value,"MB",unit);
            }
        }
        if("bps".equals(unit)){
            if(StringUtils.isNotBlank(value) &&  Double.parseDouble(value) > 10000){
                return UnitsUtil.getValueMap(value,"Gbps",unit);
            }else{
                return UnitsUtil.getValueMap(value,"Mbps",unit);
            }
        }
        return null;
    }
}
