package cn.mw.monitor.timetask.timer;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.timetask.dao.MwBaseLineDao;
import cn.mw.monitor.timetask.entity.MwBaseLineHealthValueDto;
import cn.mw.monitor.timetask.entity.MwBaseLineItemNameDto;
import cn.mw.monitor.timetask.entity.MwBaseLineManageDto;
import cn.mw.monitor.timetask.service.MwBaseLineService;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwBaseLineTime
 * @Description 定时统计基线健康数据
 * @Author gengjb
 * @Date 2022/4/6 14:18
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j(topic = "timerController")
public class MwBaseLineTime {

    @Autowired
    private MwBaseLineService service;

    @Resource
    private MwBaseLineDao baseLineDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    private final String filterItem = "INTERFACE";

    @Autowired
    private MwAssetsManager assetsManager;

    @Autowired
    private MWUserCommonService userService;

    private Integer groupCount = 200;

    /**
     * 定时统计基线健康数据
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult baseLineHealthValue(){
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try {
            //查询所有的基线数据
            List<MwBaseLineManageDto> list = new ArrayList<>();
            MwBaseLineManageDto dto = new MwBaseLineManageDto();
            dto.setPageNumber(0);
            dto.setPageSize(10000);
            Reply reply = service.selectBaseLineData(dto);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(pageInfo != null){
                    list = pageInfo.getList();
                }
            }
            //查询资产及监控数据
            List<MwBaseLineHealthValueDto> healthValueDtos = getItemAndAseets(list);
            if(CollectionUtils.isNotEmpty(healthValueDtos)){
                //将原来的数据删除
                baseLineDao.deleteBaseLineHealthData();
                //添加新的数据
                baseLineDao.insertBaseLineHealthData(healthValueDtos);
            }
            //将基线数据同步到zabbix宏值
            syncZabbixHostMacro(healthValueDtos);
            //进行数据添加
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("基线健康值统计:成功");
            return taskRresult;
        }catch (Exception e){
            log.error("基线健康值统计失败，统计日期"+new Date(),e);
            taskRresult.setSuccess(false);
            taskRresult.setResultType(1);
            taskRresult.setResultContext("基线健康值统计:失败");
            return taskRresult;
        }
    }


    /**
     * 获取资产及需要查询的监控项
     */
    private List<MwBaseLineHealthValueDto> getItemAndAseets(List<MwBaseLineManageDto> list){
        List<MwBaseLineHealthValueDto> healthValueDtos = new ArrayList<>();
        //获取所有资产
        QueryTangAssetsParam qParam = new QueryTangAssetsParam();
        qParam.setPageNumber(1);
        qParam.setPageSize(Integer.MAX_VALUE);
        qParam.setUserId(userService.getAdmin());
        List<MwTangibleassetsTable> assetsTable = assetsManager.getAssetsTable(qParam);
        if(CollectionUtils.isEmpty(assetsTable)){return healthValueDtos;}
        //分组
        Map<Integer, List<String>> groupMap = assetsTable.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        log.info("MwBaseLineTime{} getItemAndAseets() groupMap::"+groupMap);
        for (MwBaseLineManageDto dto : list) {
            Integer dateType = dto.getDateType();
            List<Long> time = new ArrayList<>();
            switch (dateType){
                case 1:
                    List<Date> yesterday = getYesterday();
                    time.add(yesterday.get(0).getTime()/1000);
                    time.add(yesterday.get(1).getTime()/1000);
                    break;
                case 2:
                    List<Date> lastWeek = getLastWeek();
                    time.add(lastWeek.get(0).getTime()/1000);
                    time.add(lastWeek.get(1).getTime()/1000);
                    break;
                case 3:
                    List<Date> lastMonth = getLastMonth();
                    time.add(lastMonth.get(0).getTime()/1000);
                    time.add(lastMonth.get(1).getTime()/1000);
                    break;
                case 4:
                    List<Date> lastYear = getLastYear();
                    time.add(lastYear.get(0).getTime()/1000);
                    time.add(lastYear.get(1).getTime()/1000);
                    break;
            }
            log.info("MwBaseLineTime{} getItemAndAseets() time::"+time);
            //获取监控项
            List<MwBaseLineItemNameDto> itemNameDtos = dto.getItemNameDtos();
            getItemInfo(itemNameDtos,groupMap,time,healthValueDtos);
        }
        return healthValueDtos;
    }

    private void getItemInfo(List<MwBaseLineItemNameDto> itemNameDtos,Map<Integer, List<String>> groupMap,List<Long> time,List<MwBaseLineHealthValueDto> healthValueDtos){
        List<String> itemNames = itemNameDtos.stream().map(MwBaseLineItemNameDto::getItemName).collect(Collectors.toList());
        log.info("MwBaseLineTime{} getItemInfo() itemNames::"+itemNames);
        for (Integer serverId : groupMap.keySet()) {
            List<String> hostIds = groupMap.get(serverId);
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, hostIds);
            if(result == null || result.isFail()){continue;}
            List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            setItemNameInfo(itemApplications);
            //按照类型分组
            Map<String, List<String>> itemMap = itemApplications.stream().collect(Collectors.groupingBy(ItemApplication::getValue_type, Collectors.mapping(ItemApplication::getItemid, Collectors.toList())));
            //查询历史记录
            List<MWItemHistoryDto> historyDtos = getZabbixHistoryInfo(serverId, itemMap, time);
            if(CollectionUtils.isEmpty(historyDtos)){continue;}
            Map<String, List<ItemApplication>> hostMap = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getHostid()));
            Map<String, List<MWItemHistoryDto>> historyItemMap = historyDtos.stream().collect(Collectors.groupingBy(item -> item.getItemid()));
            log.info("MwBaseLineTime{} getItemInfo() hostMap::"+hostMap);
            zabbixDataHandler(hostMap,historyItemMap,healthValueDtos);
        }
    }


    private void zabbixDataHandler(Map<String, List<ItemApplication>> hostMap,Map<String, List<MWItemHistoryDto>> historyItemMap,List<MwBaseLineHealthValueDto> healthValueDtos){
        for (String hostId : hostMap.keySet()) {
            List<ItemApplication> applications = hostMap.get(hostId);
            if(CollectionUtils.isEmpty(applications)){continue;}
            //按照监控名称分组
            Map<String, List<ItemApplication>> itemNameMap = applications.stream().collect(Collectors.groupingBy(item -> item.getName()));
            for (String itemName : itemNameMap.keySet()) {
                List<ItemApplication> nameItems = itemNameMap.get(itemName);
                if(CollectionUtils.isEmpty(nameItems)){continue;}
                MwBaseLineHealthValueDto healthValueDto = new MwBaseLineHealthValueDto();
                List<MWItemHistoryDto> itemNameHistory = new ArrayList<>();
                for (ItemApplication nameItem : nameItems) {
                    if(CollectionUtils.isNotEmpty(historyItemMap.get(nameItem.getItemid()))){
                        itemNameHistory.addAll(historyItemMap.get(nameItem.getItemid()));
                    }
                }
                if(CollectionUtils.isEmpty(itemNameHistory)){continue;}
                //求历史数据的平均值，并保留两位小数
                double asDouble = itemNameHistory.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getValue())).average().getAsDouble();
                log.info("MwBaseLineTime{} zabbixDataHandler() asDouble::"+asDouble);
                //单位转换
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(asDouble), nameItems.get(0).getUnits());
                healthValueDto.setValue(convertedValue.get("value")+convertedValue.get("units"));
                healthValueDto.setAssetsId(hostId);
                healthValueDto.setItemName(itemName);
                healthValueDto.setDValue(Double.parseDouble(convertedValue.get("value")));
                log.info("MwBaseLineTime{} zabbixDataHandler() healthValueDto::"+healthValueDto);
                healthValueDtos.add(healthValueDto);
            }
        }
    }


    private List<MWItemHistoryDto> getZabbixHistoryInfo(Integer serverId,Map<String, List<String>> itemMap,List<Long> time){
        List<MWItemHistoryDto> historyDtos = new ArrayList<>();
        for (String valueType : itemMap.keySet()) {
            //分组查询
            List<List<String>> partition = Lists.partition(itemMap.get(valueType), groupCount);
            for (List<String> itemIds : partition) {
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemIds, time.get(0), time.get(1),Integer.parseInt(valueType));
                if(mwZabbixAPIResult == null || mwZabbixAPIResult.isFail()){continue;}
                historyDtos.addAll(JSONArray.parseArray(String.valueOf(mwZabbixAPIResult.getData()), MWItemHistoryDto.class));
            }
        }
        return historyDtos;
    }

    private void setItemNameInfo(List<ItemApplication> itemApplications){
        for (ItemApplication itemApplication : itemApplications) {
            String name = itemApplication.getName();
            if(name.contains(filterItem) || (!name.contains("[") && !name.contains("]"))){continue;}
            itemApplication.setName(name.split("]")[1]);
        }
    }

    //昨天
    public static List<Date> getYesterday(){
        Date dateStart;
        Date dateEnd;
        Calendar cal2=new GregorianCalendar();
        cal2.setTime(DateUtils.getTimesMorning());
        cal2.add(Calendar.DAY_OF_MONTH,-1);
        dateStart=cal2.getTime();
        Calendar cal3=new GregorianCalendar();
        cal3.setTime(DateUtils.getTimesNight());
        cal3.add(Calendar.DAY_OF_MONTH,-1);
        dateEnd=cal3.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dateStart);
        list.add(dateEnd);
        return list;
    }

    //上周
    public static List<Date> getLastWeek(){
        LocalDate localDate=LocalDate.now();
        int value = localDate.getDayOfWeek().getValue();
        LocalDate s1 = localDate.minus(value+6, ChronoUnit.DAYS);
        LocalDateTime localDateTime=s1.atTime(0,0,0);
        LocalDate s2 = localDate.minus(value, ChronoUnit.DAYS);
        LocalDateTime localDateTime1 = s2.atTime(23, 59, 59);
        List<Date> list=new ArrayList<>();
        list.add(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        list.add(Date.from(localDateTime1.atZone(ZoneId.systemDefault()).toInstant()));
        return list;
    }

    //上月
    public static List<Date> getLastMonth(){
        SimpleDateFormat stf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.MONTH, -1);
        int lastMonthMaxDay=calendar1.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar1.set(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH), lastMonthMaxDay, 23, 59, 59);
        List<Date> list=new ArrayList<>();
        list.add(calendar.getTime());
        list.add(calendar1.getTime());

        String format = stf.format(calendar.getTime());
        String format1 = stf.format(calendar1.getTime());
        return list;
    }

    //去年
    public static List<Date> getLastYear(){
        SimpleDateFormat stf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,0);
        calendar.set(Calendar.DAY_OF_MONTH+1,calendar.getActualMinimum(Calendar.YEAR));
        calendar.add(Calendar.YEAR,-1);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(calendar1.get(Calendar.YEAR),calendar1.get(Calendar.MONTH),calendar1.get(Calendar.DAY_OF_MONTH),0,0,0);
        calendar1.set(Calendar.DAY_OF_MONTH+1,calendar1.getActualMinimum(Calendar.YEAR));
        calendar1.add(Calendar.YEAR,0);
        List<Date> list=new ArrayList<>();
        list.add(calendar.getTime());
        list.add(calendar1.getTime());

        String format = stf.format(calendar.getTime());
        String format1 = stf.format(calendar1.getTime());
        return list;
    }

    /**
     * 同步zabbix主机宏值
     * @param healthValueDtos
     */
    private void syncZabbixHostMacro(List<MwBaseLineHealthValueDto> healthValueDtos){
        if(CollectionUtils.isEmpty(healthValueDtos))return;
        Set<String> assetsIds = new HashSet<>();
        healthValueDtos.forEach(value->{
            assetsIds.add(value.getAssetsId());
        });
        //查询监控项数据
        List<MwBaseLineItemNameDto> itemNames = baseLineDao.getItemNames();
        //设置宏信息
        for (MwBaseLineHealthValueDto healthValueDto : healthValueDtos) {
            String itemName = healthValueDto.getItemName();
            for (MwBaseLineItemNameDto itemNameDto : itemNames) {
                String name = itemNameDto.getItemName();
                String macro = itemNameDto.getMacro();
                if(StringUtils.isNotBlank(itemName) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(macro) && itemName.equals(name)){
                    healthValueDto.setMacro(macro);
                    continue;
                }
            }
        }
        //查询serverid
        List<String> list = new ArrayList<>(assetsIds);
        //获取所有资产
        QueryTangAssetsParam qParam = new QueryTangAssetsParam();
        qParam.setPageNumber(1);
        qParam.setPageSize(Integer.MAX_VALUE);
        qParam.setAssetsIds(list);
        List<MwTangibleassetsTable> allAssets = assetsManager.getAssetsTable(qParam);
        if(CollectionUtils.isEmpty(allAssets))return;
        for (MwTangibleassetsTable allAsset : allAssets) {
            Object assetsId = allAsset.getAssetsId();//资产主机ID
            Object serverId = allAsset.getMonitorServerId();//zabbix服务器ID
            for (MwBaseLineHealthValueDto healthValueDto : healthValueDtos){
                String healthValueDtoAssetsId = healthValueDto.getAssetsId();
                if(assetsId != null && serverId != null && StringUtils.isNotBlank(healthValueDtoAssetsId) && healthValueDtoAssetsId.equals(assetsId.toString())){
                    healthValueDto.setServerId(Integer.parseInt(serverId.toString()));
                }
            }
        }
        //调用接口修改zabbix宏值
        for (MwBaseLineHealthValueDto healthValueDto : healthValueDtos) {
            Integer serverId = healthValueDto.getServerId();//zabbix服务器ID
            String assetsId = healthValueDto.getAssetsId();//主机ID
            String macro = healthValueDto.getMacro();//宏
            Double dValue = healthValueDto.getDValue();//宏值
            MWZabbixAPIResult result = mwtpServerAPI.hostCreateMacro(serverId, assetsId, macro, dValue);
            if (result != null && !result.isFail()){
                log.info("主机"+assetsId+"创建主机宏成功");
            }else{
                log.info("主机"+assetsId+"创建主机宏失败");
            }
        }
    }
}
