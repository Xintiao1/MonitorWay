package cn.mw.monitor.visualized.service.impl;


import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.visualized.api.MwVisualizedCommonService;
import cn.mw.monitor.service.visualized.dto.MwDigitalTwinAlertDto;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.service.visualized.dto.MwDigitalTwinInterfaceDto;
import cn.mw.monitor.service.visualized.dto.MwDigitalTwinItemDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.enums.VisualizedDigitalTwinEnum;
import cn.mw.monitor.service.visualized.param.MwDigitalTwinItemParam;
import cn.mw.monitor.visualized.service.MwDigitalTwinService;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 数字孪生逻辑处理
 * @date 2023/8/2 9:43
 */
@Service
@Slf4j
public class MwDigitalTwinServiceImpl implements MwDigitalTwinService, MwVisualizedCommonService {


    @Autowired
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private MWUserCommonService userService;

    private final List<String> itemNames = Arrays.asList("CPU_UTILIZATION","TEMPERTURE_SENSOR_VALUE","MEMORY_TOTAL","MEMORY_UTILIZATION","MW_DISK_TOTAL","MW_DISK_USED",
            "DISK_FREE_PERCENTAGE","INTERFACE_IPADDRESS","INTERFACE_MACADDR","MW_INTERFACE_SPEED","INTERFACE_IN_DROP_RATE",
            "INTERFACE_OUT_DROP_RATE","INTERFACE_IN_ERRORS","INTERFACE_OUT_ERRORS","INTERFACE_NAME","MW_INTERFACE_IN_TRAFFIC","MW_INTERFACE_OUT_TRAFFIC");

    private String interfaceName = "INTERFACE";

    private String diskName = "DISK";

    private String memoryName = "MEMORY";

    private String MEMORY_TOTAL = "MEMORY_TOTAL";

    private String diskUnits = "B";

    @Autowired
    private MWNetWorkLinkCommonService netWorkLinkCommonService;

    private String bandWidthUnits = "Mbps";

    /**
     * 查询资产对应的zabbix监控项
     * @param itemParam
     * @return
     */
    @Override
    public Reply getAssetsItemInfo(MwDigitalTwinItemParam itemParam) {
        try {
            if(CollectionUtils.isEmpty(itemParam.getAssetsIds())){return Reply.fail("参数为空");}
            //根据资产ID集合查询数据库缓存数据
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = visualizedManageDao.selectvisualizedCacheInfos(itemParam.getAssetsIds(), itemNames);
            if(CollectionUtils.isEmpty(mwVisualizedCacheDtos)){return Reply.ok();}
            log.info("MwDigitalTwinServiceImpl{} getAssetsItemInfo() mwVisualizedCacheDtos::"+mwVisualizedCacheDtos.size());
            for (MwVisualizedCacheDto mwVisualizedCacheDto : mwVisualizedCacheDtos) {
                String value = mwVisualizedCacheDto.getValue();
                if(StringUtils.isNotBlank(value) && (value.equals("-1") || value.equals("-1.00"))){
                    mwVisualizedCacheDto.setValue("0.00");
                }
            }
            //按资产进行数据分组
            Map<String, List<MwVisualizedCacheDto>> assetsGroupMap = mwVisualizedCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId()));
            List<MwDigitalTwinItemDto> digitalTwinItemDtos = new ArrayList<>();
            //设置资产的监控项信息
            for (String assetsId : assetsGroupMap.keySet()) {
                MwDigitalTwinItemDto digitalTwinItemDto = new MwDigitalTwinItemDto();
                List<MwVisualizedCacheDto> cacheDtos = assetsGroupMap.get(assetsId);
                if(CollectionUtils.isEmpty(cacheDtos)){continue;}
                //过滤接口数据
                List<MwVisualizedCacheDto> dtos = cacheDtos.stream().filter(item -> !item.getItemName().contains(interfaceName)).collect(Collectors.toList());
                handlerObjectMappingInfo(dtos,digitalTwinItemDto);
                //处理接口信息
                List<MwDigitalTwinInterfaceDto> digitalTwinInterfaceDtos = handlerInterfaceInfo(cacheDtos);
                digitalTwinItemDto.setInterfaceConut(digitalTwinInterfaceDtos.size());
                digitalTwinItemDto.setInterfaceDtos(digitalTwinInterfaceDtos);
                digitalTwinItemDtos.add(digitalTwinItemDto);
                //内存数据汇总
                handlerMemoryInfo(cacheDtos,digitalTwinItemDto);
                //磁盘数据汇总
                handlerDiskInfo(cacheDtos,digitalTwinItemDto);
            }
            return Reply.ok(digitalTwinItemDtos);
        }catch (Throwable e){
            log.error("MwDigitalTwinServiceImpl {} getAssetsItemInfo()",e);
            return Reply.fail("MwDigitalTwinServiceImpl {} getAssetsItemInfo()"+e.getMessage());
        }
    }

    /**
     * 处理内存数据
     */
    private void handlerMemoryInfo(List<MwVisualizedCacheDto> cacheDtos,MwDigitalTwinItemDto digitalTwinItemDto){
        List<MwVisualizedCacheDto> memoryCacheDtos = cacheDtos.stream().filter(item -> item.getItemName().contains(memoryName)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(memoryCacheDtos)){return;}
        for (MwVisualizedCacheDto memoryCacheDto : memoryCacheDtos) {
            String itemName = memoryCacheDto.getItemName();
            if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
            memoryCacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
            memoryCacheDto.setItemName(itemName.split("]")[1]);
        }
        Map<String, List<MwVisualizedCacheDto>> memoryMap = memoryCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
        for (String itemName : memoryMap.keySet()) {
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = memoryMap.get(itemName);
            if(itemName.equals(MEMORY_TOTAL)){
                //汇总数据
                double sum = mwVisualizedCacheDtos.stream().mapToDouble(cacheDto -> Double.valueOf(cacheDto.getValue())).sum();
                //单位转换
                Map<String, String> valueMap = UnitsUtil.getConvertedValue(new BigDecimal(sum),mwVisualizedCacheDtos.get(0).getUnits());
                digitalTwinItemDto.setMemoryTotal(valueMap.get("value")+valueMap.get("units"));
                continue;
            }
            double avgValue = mwVisualizedCacheDtos.stream().mapToDouble(item -> Double.valueOf(item.getValue())).average().getAsDouble();
            digitalTwinItemDto.setMemoryUtilization(new BigDecimal(avgValue).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()+"%");
            digitalTwinItemDto.setMemoyFreeUtilization(new BigDecimal(100 - avgValue).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()+"%");
        }
    }

    /**
     * 磁盘信息单独处理，需要汇总计算
     * @param cacheDtos
     */
    private void handlerDiskInfo(List<MwVisualizedCacheDto> cacheDtos,MwDigitalTwinItemDto digitalTwinItemDto){
        List<MwVisualizedCacheDto> diskCacheDtos = cacheDtos.stream().filter(item -> item.getItemName().contains(diskName)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(diskCacheDtos)){return;}
        for (MwVisualizedCacheDto diskCacheDto : diskCacheDtos) {
            String itemName = diskCacheDto.getItemName();
            if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
            diskCacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
            diskCacheDto.setItemName(itemName.split("]")[1]);
            //单位进行一致转换
            Map<String, String> valueMap = UnitsUtil.getValueMap(diskCacheDto.getValue(), diskUnits, diskCacheDto.getUnits());
            diskCacheDto.setValue(valueMap.get("value"));
            diskCacheDto.setUnits(diskUnits);
        }
        //按照监控名称分组
        Map<String, List<MwVisualizedCacheDto>> diskMap = diskCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
        if(diskMap == null || diskMap.isEmpty()){return;}
        Double diskTotal = new Double(0);
        Double diskUsed = new Double(0);
        for (String itemName : diskMap.keySet()) {
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = diskMap.get(itemName);
            if(itemName.contains(VisualizedDigitalTwinEnum.MW_DISK_TOTAL.getName())){
                diskTotal += mwVisualizedCacheDtos.stream().mapToDouble(cacheDto -> Double.valueOf(cacheDto.getValue())).sum();
            }
            if(itemName.contains(VisualizedDigitalTwinEnum.MW_DISK_USED.getName())){
                diskUsed += mwVisualizedCacheDtos.stream().mapToDouble(cacheDto -> Double.valueOf(cacheDto.getValue())).sum();
            }
        }
        //计算百分比
        double value = new BigDecimal((diskUsed / diskTotal) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        digitalTwinItemDto.setDiskUtilization(String.valueOf(value)+"%");
        digitalTwinItemDto.setDiskFreeUtilization(String.valueOf(100-value)+"%");
        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(diskTotal), diskUnits);
        digitalTwinItemDto.setDiskTotal(convertedValue.get("value")+convertedValue.get("units"));
    }

    /**
     * 处理接口信息
     * @param cacheDtos
     */
    private  List<MwDigitalTwinInterfaceDto> handlerInterfaceInfo(List<MwVisualizedCacheDto> cacheDtos) throws Exception {
        List<MwDigitalTwinInterfaceDto> digitalTwinInterfaceDtos = new ArrayList<>();
        List<MwVisualizedCacheDto> interfaceCacheDtos = cacheDtos.stream().filter(item -> item.getItemName().contains(interfaceName)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(interfaceCacheDtos)){return digitalTwinInterfaceDtos;}
        for (MwVisualizedCacheDto interfaceCacheDto : interfaceCacheDtos) {
            String itemName = interfaceCacheDto.getItemName();
            if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
            interfaceCacheDto.setName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
            interfaceCacheDto.setItemName(itemName.split("]")[1]);
        }
        //按照接口名称分组
        Map<String, List<MwVisualizedCacheDto>> interfaceNameMap = interfaceCacheDtos.stream().filter(item->StringUtils.isNotBlank(item.getName())).collect(Collectors.groupingBy(item -> item.getName()));
        if(interfaceNameMap == null || interfaceNameMap.isEmpty()){return digitalTwinInterfaceDtos;}
        for (String itemName : interfaceNameMap.keySet()) {
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = interfaceNameMap.get(itemName);
            if(CollectionUtils.isEmpty(mwVisualizedCacheDtos)){continue;}
            MwDigitalTwinInterfaceDto interfaceDto = new MwDigitalTwinInterfaceDto();
            handlerObjectMappingInfo(mwVisualizedCacheDtos,interfaceDto);
            digitalTwinInterfaceDtos.add(interfaceDto);
        }
        return digitalTwinInterfaceDtos;
    }


    /**
     * 处理对象映射信息，使用反射设置属性
     * @param cacheDtos
     */
    private void handlerObjectMappingInfo(List<MwVisualizedCacheDto> cacheDtos,Object obj) throws Exception {
        //根据监控项获取实体对象属性
        for (MwVisualizedCacheDto cacheDto : cacheDtos) {
            String itemName = cacheDto.getItemName();
            if(itemName.contains("[") && itemName.contains("]")){
                itemName = itemName.split("]")[1];
            }
            String proPerty = VisualizedDigitalTwinEnum.getProPerty(itemName);
            if(StringUtils.isEmpty(proPerty)){continue;}
            Field field = obj.getClass().getDeclaredField(proPerty);
            field.setAccessible(true);
            field.set(obj,cacheDto.getValue()+cacheDto.getUnits());
        }
    }


    /**
     * 获取告警信息
     * @param paramList
     * @return
     */
    @Override
    public MwDigitalTwinAlertDto getAssetsAlertInfo(List<MwDigitalTwinItemParam> paramList) {
        try {
            MwDigitalTwinAlertDto digitalTwinAlertDto = new MwDigitalTwinAlertDto();
            //获取线路信息
            getLinkInfo(digitalTwinAlertDto);
            List<ZbxAlertDto> realDate = new ArrayList();
            for (MwDigitalTwinItemParam mwDigitalTwinItemParam : paramList) {
                if(mwDigitalTwinItemParam.getServerId() == null || mwDigitalTwinItemParam.getServerId() == 0 || CollectionUtils.isEmpty(mwDigitalTwinItemParam.getHostIds())){Reply.fail("参数为空");}
                //查询告警
                AlertParam alertParam = new AlertParam();
                alertParam.setPageSize(Integer.MAX_VALUE);
                alertParam.setQueryHostIds(mwDigitalTwinItemParam.getHostIds());
                alertParam.setQueryMonitorServerId(mwDigitalTwinItemParam.getServerId());
//                alertParam.setAcknowledged("未确认");
                alertParam.setUserId(userService.getAdmin());
                Reply reply = mwalertService.getCurrAlertPage(alertParam);
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(pageInfo == null || pageInfo.getList() == null){continue;}
                realDate.addAll(pageInfo.getList());
            }
            Map<String, Integer> alertClassifyMap = handlerAlertLevelInfo(realDate);
            if(CollectionUtils.isEmpty(realDate)){
                digitalTwinAlertDto.setAlertCount(0);
                digitalTwinAlertDto.setAlertClassift(alertClassifyMap);
                digitalTwinAlertDto.setAlertInfos(realDate);
                return digitalTwinAlertDto;

            }
            //取最近一次时间的告警
            realDate.forEach(item->{
                item.setAlertDate(DateUtils.parse(item.getClock(), DateConstant.NORM_DATETIME));
            });
            ZbxAlertDto zbxAlertDto = realDate.stream().max(Comparator.comparing(ZbxAlertDto::getAlertDate)).get();
            //警设备数量统计
            Map<String, List<ZbxAlertDto>> alertDeviceCountCollect = realDate.stream().filter(s-> !Strings.isNullOrEmpty(s.getHostid())).collect(Collectors.groupingBy(s -> s.getHostid()));
            int alertDeviceCount = 0;
            if(alertDeviceCountCollect!=null){
                alertDeviceCount = alertDeviceCountCollect.size();
            }
            digitalTwinAlertDto.setAlertDeviceCount(alertDeviceCount);
            //处理告警设备类型
            Map<Integer, List<ZbxAlertDto>> alertDeviceTypeCollect = realDate.stream().filter(s->s.getAssetsTypeSubId()!=null).collect(Collectors.groupingBy(s -> s.getAssetsTypeSubId()));
            int alertDeviceTypeCount = 0;
            if(alertDeviceTypeCollect!=null){
                alertDeviceTypeCount = alertDeviceTypeCollect.size();
            }
            digitalTwinAlertDto.setAlertDeviceTypeCount(alertDeviceTypeCount);
            digitalTwinAlertDto.setAlertInfos(realDate);
            digitalTwinAlertDto.setAlertCount(realDate.size());
            digitalTwinAlertDto.setAlertClassift(alertClassifyMap);
            digitalTwinAlertDto.setLastTime(zbxAlertDto.getClock());
            return digitalTwinAlertDto;
        }catch (Throwable e){
            log.error("MwDigitalTwinServiceImpl {} getAssetsAlertInfo()",e);
            return new MwDigitalTwinAlertDto();
        }
    }



    private void getLinkInfo(MwDigitalTwinAlertDto digitalTwinAlertDto){
        List<AddAndUpdateParam> allLinkInfo = netWorkLinkCommonService.getAllLinkInfo();
        if(CollectionUtils.isEmpty(allLinkInfo)){
            digitalTwinAlertDto.setLinkCount(0);
            digitalTwinAlertDto.setTotalBandWidth(0.0);
            digitalTwinAlertDto.setBandWidthUnit(bandWidthUnits);
            return;
        }
        digitalTwinAlertDto.setLinkCount(allLinkInfo.size());
        //带宽单位转换并相加
        Double bandWidth = new Double(0);
        for (AddAndUpdateParam updateParam : allLinkInfo) {
            String upLinkBandwidth = updateParam.getUpLinkBandwidth();
            String bandUnit = updateParam.getBandUnit();
            //单位转换
            Map<String, String> valueMap = UnitsUtil.getValueMap(upLinkBandwidth, bandWidthUnits, bandUnit);
            bandWidth += Double.parseDouble(valueMap.get("value"));
        }
        //进行合适的单位转换
        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(bandWidth), bandWidthUnits);
        digitalTwinAlertDto.setTotalBandWidth(Double.parseDouble(convertedValue.get("value")));
        digitalTwinAlertDto.setBandWidthUnit(convertedValue.get("units"));
    }

    /**
     * 处理告警等级
     * @param realDate
     */
    private Map<String,Integer> handlerAlertLevelInfo(List<ZbxAlertDto> realDate){
        Map<String,Integer> alertClassifyMap = new HashMap<>();
        ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
        if(CollectionUtils.isEmpty(realDate)){
            for (String id : alertLevelMap.keySet()) {
                String level = alertLevelMap.get(id);
                alertClassifyMap.put(level,0);
            }
            return alertClassifyMap;
        }
        //按照等级分类
        Map<String, List<ZbxAlertDto>> listMap = realDate.stream().collect(Collectors.groupingBy(item -> item.getSeverity()));
        for (String id : alertLevelMap.keySet()) {
            String level = alertLevelMap.get(id);
            List<ZbxAlertDto> zbxAlertDtos = listMap.get(level);
            if(CollectionUtils.isEmpty(zbxAlertDtos)){
                alertClassifyMap.put(level,0);
                continue;
            }
            alertClassifyMap.put(level,zbxAlertDtos.size());
        }
        return alertClassifyMap;
    }

}
