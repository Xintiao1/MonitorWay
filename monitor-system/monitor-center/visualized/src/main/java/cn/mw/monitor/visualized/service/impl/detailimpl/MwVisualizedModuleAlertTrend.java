package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedAlertRecordDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleAlertLevelDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedModuleAlertClassifyTrend
 * @Description 查询告警趋势
 * @Author gengjb
 * @Date 2023/4/17 15:02
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleAlertTrend implements MwVisualizedModule {

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MWUserCommonService commonService;

    @Override
    public int[] getType() {
        return new int[]{48};
    }

    private final String ALERT_LEVEL = "未分类";


    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            List<Integer> instanceIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getModelInstanceId).collect(Collectors.toList());
            List<String> days = MwVisualizedDateUtil.getDays(moduleParam.getAlertTrendDays());
            List<MwVisualizedAlertRecordDto> alertRecordDtos = new ArrayList<>();
            log.info("MwVisualizedModuleAlertTrend{} getData() instanceIds:"+instanceIds.size());
            //根据日期和id查询数据库缓存告警信息
            if(instanceIds != null && instanceIds.size() > 500){
                List<List<Integer>> partition = Lists.partition(instanceIds, 500);
                for (List<Integer> ids : partition) {
                    List<String> collect = ids.stream().map(Object::toString).collect(Collectors.toList());
                    alertRecordDtos.addAll(visualizedManageDao.selectAlertCacheInfo(collect, days));
                }
            }else{
                List<String> collect = instanceIds.stream().map(Object::toString).collect(Collectors.toList());
                alertRecordDtos.addAll(visualizedManageDao.selectAlertCacheInfo(collect, days));
            }
            log.info("MwVisualizedModuleAlertTrend{} getData() alertRecordDtos:"+alertRecordDtos);
            //资产分组
            Map<Integer, List<String>> groupMap = tangibleassetsDTOS.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            AlertParam alertParam = new AlertParam();
            log.info("MwVisualizedModuleAlertTrend{} getData() assetsIds:"+instanceIds.size());
            for (Integer serverId : groupMap.keySet()) {
                List<String> hostIds = groupMap.get(serverId);
                alertParam.setPageSize(Integer.MAX_VALUE);
                alertParam.setUserId(commonService.getAdmin());
                alertParam.setStartTime(DateUtils.formatDate(new Date()));
                alertParam.setEndTime(DateUtils.formatDate(new Date()));
                alertParam.setQueryHostIds(hostIds);
                alertParam.setQueryMonitorServerId(serverId);
                Reply reply = mwalertService.getHistAlertPage(alertParam);
                if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS){continue;}
                PageInfo pageInfo = (PageInfo) reply.getData();
                List<ZbxAlertDto> zbxAlertDtos = pageInfo.getList();
                log.info("MwVisualizedModuleAlertTrend{} getData() zbxAlertDtos:"+zbxAlertDtos.size());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String time = format.format(new Date());
                if(alertRecordDtos == null){
                    alertRecordDtos = new ArrayList<>();
                }
                for (ZbxAlertDto zbxAlertDto : zbxAlertDtos) {
                    MwVisualizedAlertRecordDto recordDto = new MwVisualizedAlertRecordDto();
                    recordDto.extractFrom(zbxAlertDto,time);
                    alertRecordDtos.add(recordDto);
                }
            }
            fillData(alertRecordDtos,days);
            if(CollectionUtils.isEmpty(alertRecordDtos)){return null;}
            return alertDataHandle(alertRecordDtos);
        }catch (Throwable e){
            log.error("可视化组件区查询告警趋势失败",e);
            return null;
        }
    }

    /**
     * 告警数据处理
     */
    private  Map<String,List<MwVisualizedModuleAlertLevelDto>> alertDataHandle(List<MwVisualizedAlertRecordDto> alertRecordDtos) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<MwVisualizedAlertRecordDto>> listMap = alertRecordDtos.stream().collect(Collectors.groupingBy(item -> item.getAlertSeverity()));
        Map<String, List<MwVisualizedModuleAlertLevelDto>> levelMap = new HashMap<>();
        if(listMap == null || listMap.isEmpty()){return levelMap;}
        ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
        for (String level : listMap.keySet()) {
            List<MwVisualizedModuleAlertLevelDto> alertClassifyInfo = new ArrayList<>();
            List<MwVisualizedAlertRecordDto> recordDtos = listMap.get(level);
            if(CollectionUtils.isEmpty(recordDtos)){continue;}
            Map<String, List<MwVisualizedAlertRecordDto>> timeMap = recordDtos.stream().collect(Collectors.groupingBy(item -> item.getTime()));
            for (String time : timeMap.keySet()) {
                List<MwVisualizedAlertRecordDto> dtos = timeMap.get(time);
                MwVisualizedModuleAlertLevelDto alertLevelDto = new MwVisualizedModuleAlertLevelDto();
                alertLevelDto.setTime(time.substring(5,10));
                if(StringUtils.isBlank(dtos.get(0).getAssetsId())){
                    alertLevelDto.setAlertCount(0);
                }else{
                    alertLevelDto.setAlertCount(dtos.size());
                }
                alertLevelDto.setLevelName(level);
                alertLevelDto.setSortDate(format.parse(time));
                alertClassifyInfo.add(alertLevelDto);
            }
            //根据时间排序
            alertSort(alertClassifyInfo);
            for (String code : alertLevelMap.keySet()) {
                String name = alertLevelMap.get(code);
                if(name.equals(level)){
                    levelMap.put(code+"_"+level,alertClassifyInfo);
                    break;
                }
            }
        }
        Map<String, List<MwVisualizedModuleAlertLevelDto>> result = new LinkedHashMap<>();
        levelMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    /**
     * 告警按时间排序
     */
    private void alertSort( List<MwVisualizedModuleAlertLevelDto> alertClassifyInfo){
        Collections.sort(alertClassifyInfo, new Comparator<MwVisualizedModuleAlertLevelDto>() {
            @Override
            public int compare(MwVisualizedModuleAlertLevelDto o1, MwVisualizedModuleAlertLevelDto o2) {
                if(o1.getSortDate().compareTo(o2.getSortDate()) > 0){
                    return 1;
                }
                if(o1.getSortDate().compareTo(o2.getSortDate()) < 0){
                    return -1;
                }
                return 0;
            }
        });
    }



    private void fillData(List<MwVisualizedAlertRecordDto> alertRecordDtos,List<String> days){
        ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
        Collection<String> levels = alertLevelMap.values();
        //先把已有日期的告警趋势数据补全
        Map<String, List<MwVisualizedAlertRecordDto>> alertDateMap
                = alertRecordDtos.stream().collect(Collectors.groupingBy(item -> item.getTime()));
        if(alertDateMap != null){
            for (Map.Entry<String, List<MwVisualizedAlertRecordDto>> entry : alertDateMap.entrySet()) {
                List<MwVisualizedAlertRecordDto> recordDtos = entry.getValue();
                List<String> alertSeverity = recordDtos.stream().map(MwVisualizedAlertRecordDto::getAlertSeverity).collect(Collectors.toList());
                List<String> levelList = levels.stream()
                        .filter(element -> !alertSeverity.contains(element))
                        .collect(Collectors.toList());
                //增加数据
                for (String item : levelList) {
                    if(item.equals(ALERT_LEVEL)){continue;}
                    MwVisualizedAlertRecordDto recordDto = new MwVisualizedAlertRecordDto();
                    recordDto.setTime(entry.getKey());
                    recordDto.setAlertSeverity(item);
                    alertRecordDtos.add(recordDto);
                }
            }
        }
        List<String> dates = alertRecordDtos.stream().map(MwVisualizedAlertRecordDto::getTime).collect(Collectors.toList());
        List<String> list = days.stream()
                .filter(element -> !dates.contains(element))
                .collect(Collectors.toList());
        for (String date : list) {
            //根据日期创建空的告警趋势
            for (String level : levels) {
                if(level.equals(ALERT_LEVEL)){continue;}
                MwVisualizedAlertRecordDto recordDto = new MwVisualizedAlertRecordDto();
                recordDto.setTime(date);
                recordDto.setAlertSeverity(level);
                alertRecordDtos.add(recordDto);
            }
        }

    }
}
