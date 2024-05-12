package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleDBSessionStatDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 数据库Session会话数统计
 * @Author gengjb
 * @Date 2023/4/24 15:37
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleDBSessionStat implements MwVisualizedModule {


    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Override
    public int[] getType() {
        return new int[]{59};
    }

    @Override
    public Object getData(Object data) {
        try {
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            if(CollectionUtils.isNotEmpty(tangibleassetsDTOS) && CollectionUtils.isNotEmpty(assetsIds)){
                Iterator<MwTangibleassetsDTO> iterator = tangibleassetsDTOS.iterator();
                while (iterator.hasNext()){
                    MwTangibleassetsDTO next = iterator.next();
                    if(!assetsIds.contains(next.getId())){
                        iterator.remove();
                    }
                }
            }
            //查询数据库资产信息
            List<MwTangibleassetsDTO> mwTangAssetses = tangibleassetsDTOS;
            log.info("可视化查询数据库会话数资产数据"+mwTangAssetses);
            if(CollectionUtils.isEmpty(mwTangAssetses)){return null;}
            //根据监控服务器ID进行数据分组
            Map<Integer, List<String>> groupMap = mwTangAssetses.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,String> assetsMap = new HashMap<>();
            mwTangAssetses.forEach(item->{
                assetsMap.put(item.getAssetsId(),item.getInstanceName()!=null?item.getInstanceName():item.getHostName());
            });
            List<MwVisualizedModuleDBSessionStatDto> monitorInfo = getDBSessionMonitorInfo(groupMap, assetsMap);
            //数据排序
            sessionCountSort(monitorInfo);
            log.info("可视化查询数据库会话数最终数据"+monitorInfo);
            //取前十
            if(CollectionUtils.isEmpty(monitorInfo) || monitorInfo.size() <= 10){return monitorInfo;}
            return monitorInfo.subList(0, 10);
        }catch (Throwable e){
            log.error("可视化组件区查询数据库表Session会话数失败",e);
            return null;
        }
    }


    /**
     * 查询数据库session会话数监测情况
     * @param groupMap
     */
    private List<MwVisualizedModuleDBSessionStatDto> getDBSessionMonitorInfo(Map<Integer, List<String>> groupMap, Map<String,String> assetsMap){
        List<MwVisualizedModuleDBSessionStatDto> dbSessionStatDtos = new ArrayList<>();
        if(groupMap == null || groupMap.isEmpty()){return dbSessionStatDtos;}
        List<ItemApplication> itemApplicationList = new ArrayList<>();
        for (Integer serverId : groupMap.keySet()) {
            List<String> hostIds = groupMap.get(serverId);
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, RackZabbixItemConstant.MW_ORACLE_SESSION_COUNT, hostIds);
            //数据转换为实体
            itemApplicationList.addAll(JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class));
        }
        if(CollectionUtils.isEmpty(itemApplicationList)){return dbSessionStatDtos;}
        for (ItemApplication itemApplication : itemApplicationList) {
            String units = itemApplication.getUnits();
            String lastValue = itemApplication.getLastvalue();
            String hostid = itemApplication.getHostid();
            MwVisualizedModuleDBSessionStatDto dbSessionStatDto = new MwVisualizedModuleDBSessionStatDto();
            dbSessionStatDto.setName(assetsMap.get(hostid));
            dbSessionStatDto.setSessionCount(new BigDecimal(lastValue).setScale(2,BigDecimal.ROUND_HALF_UP).intValue()+units);
            dbSessionStatDto.setSortValue(new BigDecimal(lastValue).setScale(2,BigDecimal.ROUND_HALF_UP).intValue());
            dbSessionStatDtos.add(dbSessionStatDto);
        }
        return dbSessionStatDtos;
    }

    /**
     * 查询实例为数据库的资产
     * @return
     * @throws Exception
     */
    private List<MwTangibleassetsDTO> getAssetsData() throws Exception {
        //获取IOT类型ID
        List<Integer> modelTypeId = mwModelViewCommonService.getModelGroupIdByName(VisualizedZkSoftWareEnum.TYPE_DB.getName());
        if(CollectionUtils.isEmpty(modelTypeId)){return null;}
        List<MwTangibleassetsDTO>  tangibleassetsDTOS = new ArrayList<>();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setAssetsTypeId(modelTypeId.get(0));
        assetsParam.setAssetsTypeName(VisualizedZkSoftWareEnum.TYPE_DB.getName());
        assetsParam.setUserId(userService.getAdmin());
        List<MwTangibleassetsTable> mwTangibleassetsTables = mwAssetsManager.getAssetsTable(assetsParam);
        if(CollectionUtils.isNotEmpty(mwTangibleassetsTables)){
            for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
                MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
                BeansUtils.copyProperties(mwTangibleassetsTable,mwTangibleassetsDTO);
                tangibleassetsDTOS.add(mwTangibleassetsDTO);
            }
        }
        return tangibleassetsDTOS;
    }

    private void sessionCountSort(List<MwVisualizedModuleDBSessionStatDto> sessionStatDtos){
        Collections.sort(sessionStatDtos, new Comparator<MwVisualizedModuleDBSessionStatDto>() {
            @Override
            public int compare(MwVisualizedModuleDBSessionStatDto o1, MwVisualizedModuleDBSessionStatDto o2) {
                if(o1.getSortValue() > o2.getSortValue()){
                    return -1;
                }
                if(o1.getSortValue() < o2.getSortValue()){
                    return 1;
                }
                return 0;
            }
        });
    }
}
