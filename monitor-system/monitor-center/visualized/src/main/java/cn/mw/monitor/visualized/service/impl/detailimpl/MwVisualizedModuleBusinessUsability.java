package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleBusinessUsabilityDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 业务系统可用性统计
 * @Author gengjb
 * @Date 2023/4/17 16:07
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleBusinessUsability implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MWAlertService mwalertService;

    private String status = "宕机";

    @Override
    public int[] getType() {
        return new int[]{50};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,true);
            if(CollectionUtils.isEmpty(tangibleassetsDTOS)){return null;}
            //按照业务分类进行分组
            Map<String, List<MwTangibleassetsDTO>> classAssetsDtos = tangibleassetsDTOS.stream().filter(item->item.getModelClassify() != null &&  !item.getModelClassify().equals("")).collect(Collectors.groupingBy(item -> item.getModelClassify()));
            if(CollectionUtils.isEmpty(classAssetsDtos)){return null;}
            List<MwVisualizedModuleBusinessUsabilityDto> businessUsabilityDtos = new ArrayList<>();
            Map<String, String> alertInfo = getAlertInfo();
            for (Map.Entry<String, List<MwTangibleassetsDTO>> entry : classAssetsDtos.entrySet()) {
                String name = entry.getKey();
                List<MwTangibleassetsDTO> dtos = entry.getValue();
                //取资产状态
                String status = getStatus(dtos,alertInfo);
                MwVisualizedModuleBusinessUsabilityDto businessUsabilityDto = new MwVisualizedModuleBusinessUsabilityDto();
                businessUsabilityDto.setName(name);
                businessUsabilityDto.setStatus(status);
                businessUsabilityDtos.add(businessUsabilityDto);
            }
            return businessUsabilityDtos;
        }catch (Throwable e){
            log.error("可视化组件区查询业务系统可用性失败",e);

            return null;
        }
    }

    /**
     * 获取业务分类状态
     * @param dtos
     * @return
     */
    private String getStatus(List<MwTangibleassetsDTO> dtos,Map<String, String> alertInfo){
        if(CollectionUtils.isEmpty(dtos)){return null;}
        for (MwTangibleassetsDTO dto : dtos) {
            String alertMessage = alertInfo.get(dto.getMonitorServerId() + dto.getAssetsId());
            if(StringUtils.isNotBlank(alertMessage) && alertMessage.contains(status)){
                return VisualizedConstant.ABNORMAL;
            }
        }
        return VisualizedConstant.NORMAL;
    }



    /**
     * 获取当前的告警信息
     */
    private Map<String,String> getAlertInfo(){
        Map<String,String> alertMap = new HashMap<>();
        AlertParam alertParam = new AlertParam();
        alertParam.setPageSize(Integer.MAX_VALUE);
        alertParam.setUserId(userService.getAdmin());
        Reply reply = mwalertService.getCurrAlertPage(alertParam);
        PageInfo pageInfo = (PageInfo) reply.getData();
        if(pageInfo == null || pageInfo.getList() == null){return alertMap;}
        List<ZbxAlertDto> alertLists = pageInfo.getList();
        for (ZbxAlertDto alertList : alertLists) {
            if(alertMap.containsKey(alertList.getMonitorServerId()+alertList.getHostid())){
                String alertTitle = alertMap.get(alertList.getMonitorServerId() + alertList.getHostid());
                alertMap.put(alertList.getMonitorServerId() + alertList.getHostid(),alertTitle+alertList.getName());
            }else{
                alertMap.put(alertList.getMonitorServerId() + alertList.getHostid(),alertList.getName());
            }
        }
        return alertMap;
    }
}
