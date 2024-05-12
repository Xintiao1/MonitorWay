package cn.mw.monitor.visualized.time;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ServerHostGroupDto;
import cn.mw.monitor.service.tpserver.api.MwCommonsTPServer;
import cn.mw.monitor.service.tpserver.dto.MwTpServerCommonsDto;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedAlertDto;
import cn.mw.monitor.visualized.dto.MwVisualizedHostGroupDto;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 获取zabbix中主机信息
 * @Author gengjb
 * @Date 2023/5/19 19:27
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedObtainDataTime {

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Value("${visualized.monitor.server.ip}")
    private String serverIp;

    @Value("${visualized.group.count}")
    private Integer groupCount;

    @Autowired
    private MwCommonsTPServer commonsTPServer;

    @Autowired
    private ModuleIDManager idManager;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;
    /**
     * 获取zabbix所有主机信息g
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult getZabbixHostInfoCache(){
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //查询对应配置IP的server信息
            if(StringUtils.isBlank(serverIp)){return null;}
            List<MwTpServerCommonsDto> mwTpServerCommonsDtos = commonsTPServer.selectServerIdInfoByIp(Arrays.asList(serverIp.split(",")));
            log.info("MwVisualizedObtainDataTime{} getZabbixHostInfo::mwTpServerCommonsDtos::"+mwTpServerCommonsDtos);
            if(CollectionUtils.isEmpty(mwTpServerCommonsDtos)){return null;}
            List<MwVisualizedHostGroupDto> visualizedHostGroupDtos = new ArrayList<>();
            for (MwTpServerCommonsDto mwTpServerCommonsDto : mwTpServerCommonsDtos) {
                MWZabbixAPIResult apiResult = mwtpServerAPI.getHostGroup(mwTpServerCommonsDto.getServerId());
                List<MwVisualizedHostGroupDto> hostGroupDtos = handlerHostGroupInfo(apiResult,mwTpServerCommonsDto);
                if(CollectionUtils.isEmpty(hostGroupDtos)){return null;}
                //获取告警信息
                getAlertInfo(hostGroupDtos,mwTpServerCommonsDto.getServerId());
                visualizedHostGroupDtos.addAll(hostGroupDtos);
            }
            //先删除原先缓存数据
            visualizedManageDao.deleteHostAndGroupCache();
            //添加主机和主机组到数据库
            List<List<MwVisualizedHostGroupDto>> lists = Lists.partition(visualizedHostGroupDtos, groupCount);
            for (List<MwVisualizedHostGroupDto> hostGroupDtos : lists) {
                visualizedManageDao.insertHostAndGroupCache(hostGroupDtos);
            }
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存zabbix资产数据:成功");
        }catch (Throwable e){
            log.error("MwVisualizedObtainDataTime{} getZabbixHostInfo::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存zabbix资产数据:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }

    /**
     * 处理主机组信息
     */
    private List<MwVisualizedHostGroupDto> handlerHostGroupInfo(MWZabbixAPIResult result,MwTpServerCommonsDto mwTpServerCommonsDto){
        List<MwVisualizedHostGroupDto> visualizedHostGroupDtos = new ArrayList<>();
        if(result == null || result.isFail()){return visualizedHostGroupDtos;}
        List<ServerHostGroupDto> hostGroupDtos = JSONArray.parseArray(String.valueOf(result.getData()), ServerHostGroupDto.class);
        //数据转换为可视化的实体
        for (ServerHostGroupDto hostGroupDto : hostGroupDtos) {
            List<ItemApplication> hosts = hostGroupDto.getHosts();
            if(CollectionUtils.isEmpty(hosts)){continue;}
            for (ItemApplication application : hosts) {
                MwVisualizedHostGroupDto visualizedHostGroupDto = new MwVisualizedHostGroupDto();
                visualizedHostGroupDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                visualizedHostGroupDto.setCreator("admin");
                visualizedHostGroupDto.extractFrom(hostGroupDto.getGroupid(),hostGroupDto.getName(),application,mwTpServerCommonsDto.getServerId(),mwTpServerCommonsDto.getMonitoringServerName());
                visualizedHostGroupDtos.add(visualizedHostGroupDto);
            }
        }
        return visualizedHostGroupDtos;
    }

    /**
     * 获取告警信息
     * @param visualizedHostGroupDtos
     */
    private void getAlertInfo(List<MwVisualizedHostGroupDto> visualizedHostGroupDtos,Integer serverId){
        MWZabbixAPIResult result = mwtpServerAPI.problemget(serverId, null);
        //获取当前告警所有的eventID
        if(result == null || result.isFail()){return;}
        List<MwVisualizedAlertDto> mwVisualizedAlertDtos = JSONArray.parseArray(String.valueOf(result.getData()), MwVisualizedAlertDto.class);
        if(CollectionUtils.isEmpty(mwVisualizedAlertDtos)){//没有告警，说明所有资产状态正常
            visualizedHostGroupDtos.forEach(item->{
                item.setHostStatus(VisualizedConstant.NORMAL);
            });
        }
        Map<String, MwVisualizedAlertDto> collect = mwVisualizedAlertDtos.stream().collect(Collectors.toMap(MwVisualizedAlertDto::getEventid, option -> option, (oldOption, newOption) -> newOption));
        //根据eventid查询告警的主机
        List<String> eventIds = mwVisualizedAlertDtos.stream().map(MwVisualizedAlertDto::getEventid).collect(Collectors.toList());
        MWZabbixAPIResult eventSeverity = mwtpServerAPI.getEventSeverity(serverId, eventIds);
        if(eventSeverity == null || eventSeverity.isFail()){return;}
        List<MwVisualizedAlertDto> hostAlertDtos = JSONArray.parseArray(String.valueOf(eventSeverity.getData()), MwVisualizedAlertDto.class);
        //拿到所有的主机ID
        Set<String> hostIds = new HashSet<>();
        Map<String,String> alertLevel = new HashMap<>();
        ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
        for (MwVisualizedAlertDto hostAlertDto : hostAlertDtos) {
            List<ItemApplication> hosts = hostAlertDto.getHosts();
            if(CollectionUtils.isEmpty(hosts)){continue;}
            String eventid = hostAlertDto.getEventid();
            MwVisualizedAlertDto mwVisualizedAlertDto = collect.get(eventid);
            hostIds.addAll(hosts.stream().map(ItemApplication::getHostid).collect(Collectors.toList()));
            for (ItemApplication host : hosts) {
                alertLevel.put(host.getHostid(),alertLevelMap.get(hostAlertDto.getSeverity())+"_"+mwVisualizedAlertDto.getName());
            }
        }
        for (MwVisualizedHostGroupDto visualizedHostGroupDto : visualizedHostGroupDtos) {
            if(hostIds.contains(visualizedHostGroupDto.getHostId())){
                visualizedHostGroupDto.setHostStatus(alertLevel.get(visualizedHostGroupDto.getHostId()).split("_")[0]);
                visualizedHostGroupDto.setAlertTitle(alertLevel.get(visualizedHostGroupDto.getHostId()).split("_")[1]);
                continue;
            }
            visualizedHostGroupDto.setHostStatus(VisualizedConstant.NORMAL);
        }
    }
}
