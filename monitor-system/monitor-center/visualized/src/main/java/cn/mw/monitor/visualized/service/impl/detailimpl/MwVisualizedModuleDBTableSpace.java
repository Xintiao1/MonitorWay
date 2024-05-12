package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.DBTableSpaceDetailedDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleDBTableSpaceDto;
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
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedModuleDBTableSpace
 * @Description 数据库表空间使用情况
 * @Author gengjb
 * @Date 2023/4/18 15:31
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleDBTableSpace  implements MwVisualizedModule {

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

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Override
    public int[] getType() {
        return new int[]{57};
    }

    @Override
    public Object getData(Object data) {
        try {
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            log.info("可视化查询表空间数据资产数据"+moduleParam.getAssetsIds());
            List<String> assetsIds = new ArrayList<>();
            //判断是否需要根据业务系统查询资产
            if(StringUtils.isNotBlank(moduleParam.getAssetsId())){
                assetsIds.add(moduleParam.getAssetsId());
            }
            if(CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())){
                assetsIds.addAll(moduleParam.getAssetsIds());
            }
            log.info("可视化查询表空间数据资产数据2"+assetsIds);
            if(CollectionUtils.isNotEmpty(tangibleassetsDTOS)){
                List<String> ids = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
                Iterator<String> iterator = assetsIds.iterator();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    if(!ids.contains(next)){
                        iterator.remove();
                    }
                }
            }
            if(StringUtils.isBlank(moduleParam.getAssetsId()) && CollectionUtils.isEmpty(moduleParam.getAssetsIds())){
                assetsIds = tangibleassetsDTOS.stream().map(MwTangibleassetsDTO::getId).collect(Collectors.toList());
            }
            if((StringUtils.isNotBlank(moduleParam.getAssetsId()) || CollectionUtils.isNotEmpty(moduleParam.getAssetsIds())) && CollectionUtils.isEmpty(assetsIds)){
                return null;
            }
            List<MwVisualizedCacheDto> cacheDtoList = new ArrayList<>();
            List<List<String>> partition = Lists.partition(assetsIds, 500);
            for (List<String> ids : partition) {
                cacheDtoList.addAll(visualizedManageDao.selectvisualizedCacheInfos(ids, Arrays.asList(RackZabbixItemConstant.MW_ORACLE_TBS_USED_EXTPCT)));
            }
            log.info("可视化查询表空间数据"+cacheDtoList);
            if(CollectionUtils.isEmpty(cacheDtoList)){return null;}
            //根据监控服务器ID进行数据分组
            List<MwVisualizedModuleDBTableSpaceDto> dbMonitorInfo = getDBMonitorInfo(cacheDtoList);
            log.info("可视化组件区查询表空间最终数据"+dbMonitorInfo);
            return dbMonitorInfo;
        }catch (Throwable e){
            log.error("可视化组件区查询数据库表空间排行失败",e);
            return null;
        }
    }

    /**
     * 查询数据库表空间监测情况
     * @param
     */
    private List<MwVisualizedModuleDBTableSpaceDto> getDBMonitorInfo(List<MwVisualizedCacheDto> cacheDtoList){
        List<MwVisualizedModuleDBTableSpaceDto> moduleDBTableSpaceDtos = new ArrayList<>();
        //按资产分组
        Map<String, List<MwVisualizedCacheDto>> assetsGroupMap = cacheDtoList.stream().collect(Collectors.groupingBy(item -> item.getAssetsId()));
        for (String assetsId : assetsGroupMap.keySet()) {
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = assetsGroupMap.get(assetsId);
            if(CollectionUtils.isEmpty(mwVisualizedCacheDtos)){continue;}
            MwVisualizedModuleDBTableSpaceDto dbTableSpaceDto = new MwVisualizedModuleDBTableSpaceDto();
            dbTableSpaceDto.setHostName(mwVisualizedCacheDtos.get(0).getAssetsName());
            List<DBTableSpaceDetailedDto> tbsSpaces = new ArrayList<>();
            for (MwVisualizedCacheDto cacheDto : mwVisualizedCacheDtos) {
                DBTableSpaceDetailedDto dbTableSpaceDetailedDto = new DBTableSpaceDetailedDto();
                String itemName = cacheDto.getItemName();
                dbTableSpaceDetailedDto.setTbsSpaceName(getTbsSpaceName(itemName));
                dbTableSpaceDetailedDto.setValue(cacheDto.getValue()+cacheDto.getUnits());
                dbTableSpaceDetailedDto.setSortValue(Double.parseDouble(cacheDto.getValue()));
                tbsSpaces.add(dbTableSpaceDetailedDto);
            }
            tbsSpaceSort(tbsSpaces);
            if(CollectionUtils.isNotEmpty(tbsSpaces) && tbsSpaces.size() > 5){
                dbTableSpaceDto.setTbsSpaces(tbsSpaces.subList(0,5));
            }else{
                dbTableSpaceDto.setTbsSpaces(tbsSpaces);
            }

            moduleDBTableSpaceDtos.add(dbTableSpaceDto);
        }
        return moduleDBTableSpaceDtos;
    }

    /**
     * 获取表空间名称
     * @return
     */
    private String getTbsSpaceName(String itemName){
        if(StringUtils.isBlank(itemName) || !itemName.contains("[") || !itemName.contains("]")){return null;}
        return itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]"));
    }


    private void tbsSpaceSort(List<DBTableSpaceDetailedDto> tbsSpaces){
        Collections.sort(tbsSpaces, new Comparator<DBTableSpaceDetailedDto>() {
            @Override
            public int compare(DBTableSpaceDetailedDto o1, DBTableSpaceDetailedDto o2) {
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
