package cn.mw.monitor.visualized.time;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedAeestsCountDto;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 可视化统计分区资产数量
 * @Author gengjb
 * @Date 2023/6/12 9:13
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedAeestsCountTime {

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private ModuleIDManager idManager;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult getPartitionAssetsInfo(){
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //获取所有资产
            List<MwTangibleassetsTable> tangibleassetsTables = getAssetsInfo();
            if(CollectionUtils.isEmpty(tangibleassetsTables)){return null;}
            //按照业务系统进行分组
            Map<String, List<MwTangibleassetsTable>> listMap = tangibleassetsTables.stream().filter(item -> item.getModelSystem() != null && !"".equals(item.getModelSystem())).collect(Collectors.groupingBy(item -> item.getModelSystem()));
            log.info("MwVisualizedAeestsCountTime{} getPartitionAssetsInfo()::"+listMap.size());
            //设置数据
            if(listMap == null || listMap.isEmpty()){return null;}
            List<MwVisualizedAeestsCountDto> aeestsCountDtos = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            for (String modelSystemName : listMap.keySet()) {
                log.info("MwVisualizedAeestsCountTime{} getPartitionAssetsInfo()::modelSystemName"+modelSystemName+":::count"+listMap.get(modelSystemName).size());
                int count = listMap.get(modelSystemName).size();
                MwVisualizedAeestsCountDto countDto = new MwVisualizedAeestsCountDto();
                countDto.extractFrom(modelSystemName,count,format.format(new Date()));
                countDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                aeestsCountDtos.add(countDto);
            }
            log.info("MwVisualizedAeestsCountTime{} getPartitionAssetsInfo()::aeestsCountDtos"+aeestsCountDtos);
            //数据添加
            visualizedManageDao.insertVisualizedPartitionAssets(aeestsCountDtos);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存分区资产统计:成功");
        }catch (Throwable e){
            log.error("MwVisualizedAeestsCountTime{} getPartitionAssetsInfo::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存分区资产统计:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }

    /**
     * 获取资产信息
     */
    private List<MwTangibleassetsTable> getAssetsInfo(){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(userService.getAdmin());
        assetsParam.setAlertQuery(true);
        return mwAssetsManager.getAssetsTable(assetsParam);
    }
}
