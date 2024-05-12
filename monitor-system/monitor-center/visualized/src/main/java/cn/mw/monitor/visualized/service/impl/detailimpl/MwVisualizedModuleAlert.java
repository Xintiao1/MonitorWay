package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 查询告警数据
 * @Author gengjb
 * @Date 2023/4/17 15:27
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleAlert implements MwVisualizedModule {

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Autowired
    private MWUserCommonService userService;

    @Override
    public int[] getType() {
        return new int[]{49};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            log.info("可视化组件区查询告警数据资产数据::"+moduleParam.getServerName()+":::"+tangibleassetsDTOS);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            //按照serverId分组
            Map<Integer, List<String>> groupMap = tangibleassetsDTOS.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            List realDate = new ArrayList();
            if(moduleParam.getIsToDayAlert() != null && moduleParam.getIsToDayAlert()){
                 getToDayAllAlert(realDate,groupMap);
                 return realDate;
            }
            for (Integer serverId : groupMap.keySet()) {
                List<String> assetsIds = groupMap.get(serverId);
                AlertParam alertParam = new AlertParam();
                alertParam.setPageSize(Integer.MAX_VALUE);
                alertParam.setQueryHostIds(assetsIds);
                alertParam.setQueryMonitorServerId(serverId);
                alertParam.setUserId(userService.getAdmin());
                Reply reply = mwalertService.getCurrAlertPage(alertParam);
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(pageInfo == null || pageInfo.getList() == null){continue;}
                realDate.addAll(pageInfo.getList());
            }
            return realDate;
        }catch (Throwable e){
            log.error("可视化组件区查询告警数据失败",e);
            return null;
        }
    }


    /**
     * 获取当天所有的告警
     */
    private void getToDayAllAlert(List realDate, Map<Integer, List<String>> groupMap){
        for (Integer serverId : groupMap.keySet()) {
            List<String> assetsIds = groupMap.get(serverId);
            AlertParam alertParam = new AlertParam();
            alertParam.setPageSize(Integer.MAX_VALUE);
            alertParam.setQueryHostIds(assetsIds);
            alertParam.setQueryMonitorServerId(serverId);
            alertParam.setUserId(userService.getAdmin());
            alertParam.setStartTime(DateUtils.formatDate(new Date()));
            alertParam.setEndTime(DateUtils.formatDate(new Date()));
            Reply reply = mwalertService.getHistAlertPage(alertParam);
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(pageInfo == null || pageInfo.getList() == null){continue;}
            realDate.addAll(pageInfo.getList());
        }
    }
}
