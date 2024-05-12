package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.dto.TargetAssetsIdDto;
import cn.mw.monitor.screen.model.MapAlert;
import cn.mw.monitor.screen.model.MapAlertConfig;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.OrgMapperDTO;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.user.api.OrgModuleType;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class MessageProcessTask implements Runnable{
    private static final Logger log = LoggerFactory.getLogger("MWAlertController");
    private Queue<String> messageQueue;
    private int maxMessageNum;
    private FinishProcessCallBack finishProcessCallBack;
    private String hostipStartKey;
    private MapAlertConfig mapAlertConfig;
    private MWLagerScreenDao dao;
    private MWOrgService mwOrgService;
    private boolean debug;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public MessageProcessTask(BlockingQueue<String> messageQueue , int maxMessageNum
    , FinishProcessCallBack finishProcessCallBack , String hostipStartKey , MapAlertConfig mapAlertConfig
    , MWLagerScreenDao dao , MWOrgService mwOrgService){
        this.messageQueue = messageQueue;
        this.maxMessageNum = maxMessageNum;
        this.finishProcessCallBack = finishProcessCallBack;
        this.hostipStartKey = hostipStartKey;
        this.mapAlertConfig = mapAlertConfig;
        this.dao = dao;
        this.mwOrgService = mwOrgService;
    }

    @Override
    public void run() {
        log.info("MessageProcessTask start process queue size:{}" ,messageQueue.size());
        int count = 0;
        List<String> messages = new ArrayList<>();
        try {
            while (count < maxMessageNum) {
                String message = messageQueue.remove();
                messages.add(message);
                count++;
            }
        }catch (NoSuchElementException e){
        }

        if(debug){
            for(String message: messages){
                log.info("MessageProcessTask before:{}" ,message);
            }
        }

        List<MapAlert> list = process(messages);

        if(null != list) {
            if (debug) {
                for (MapAlert mapAlert : list) {
                    log.info("MessageProcessTask after:{}", mapAlert.toString());
                }
            }
        }

        finishProcessCallBack.finish(list);
    }

    public List<MapAlert>  process(List<String> messages){
        Set<String> ipSet = new HashSet<>();
        Map<String ,String> ipAlertMap = new HashMap<>();
        for(String message : messages){
            int ipStart = message.indexOf(hostipStartKey);
            if(ipStart > 0){
                int ipEnd = message.indexOf( "," ,ipStart);
                String ip = message.substring(ipStart + hostipStartKey.length() ,ipEnd).trim();
                ipSet.add(ip);

                if(message.indexOf(mapAlertConfig.getNormalKey()) >= 0){
                    ipAlertMap.put(ip ,mapAlertConfig.getLinkNormalColor());
                }

                if(message.indexOf(mapAlertConfig.getAlertKey()) >= 0){
                    ipAlertMap.put(ip ,mapAlertConfig.getLinkErrorColor());
                }
            }
        }

        List<MapAlert> list = null;
        try {
            if(ipSet.size() > 0){
                list = doProcess(new ArrayList<>(ipSet));
            }
        }catch (Exception e){
            log.error("MessageProcessTask" ,e);
        }

        if(null != list) {
            for (MapAlert mapAlert : list) {
                String color = ipAlertMap.get(mapAlert.getLinkTargetIp());
                mapAlert.setColor(color);
            }
        }

        return list;
    }

    private List<MapAlert> doProcess(List<String> ipList){
        List<MapAlert> ret = new ArrayList<>();

        //根据ip找到链路
        List<TargetAssetsIdDto> linkInfo = dao.getIcmpLinkAssetIdsByIp(ipList);

        List<String> assetsIds = new ArrayList<>();
        for(TargetAssetsIdDto targetAssetsIdDto : linkInfo){
            assetsIds.add(targetAssetsIdDto.getId());
            assetsIds.add(targetAssetsIdDto.getTargetAssetsId());

            MapAlert mapAlert = new MapAlert();
            mapAlert.setLinkTargetIp(targetAssetsIdDto.getLinkTargetIp());
            mapAlert.setIcmpId(targetAssetsIdDto.getId());
            mapAlert.setTargetId(targetAssetsIdDto.getTargetAssetsId());
            ret.add(mapAlert);
        }

        //获取机构坐标信息
        Map<String ,String> orgMap = new HashMap<>();
        Map map = new HashMap();

        if(assetsIds.size() > 0) {
            map.put("ids", assetsIds);
            map.put("moduleType", OrgModuleType.ASSETS.name());
            Reply reply = mwOrgService.selectOrgByParamsAndIds(map);
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                List<OrgMapperDTO> orgDTOS = (List<OrgMapperDTO>) reply.getData();
                for (OrgMapperDTO orgMapperDTO : orgDTOS) {
                    orgMap.put(orgMapperDTO.getTypeId(), orgMapperDTO.getCoordinate());
                }
            }
        }

        for(MapAlert mapAlert : ret){
            String icmpInfo = orgMap.get(mapAlert.getIcmpId());
            String targetInfo = orgMap.get(mapAlert.getTargetId());

            if(StringUtils.isEmpty(icmpInfo) || StringUtils.isEmpty(targetInfo)){
                log.info("icmpId:{},{},targetId:{},{}" ,mapAlert.getIcmpId() ,icmpInfo
                        ,mapAlert.getTargetId() ,targetInfo);
                continue;
            }
            mapAlert.setIcmp(icmpInfo);
            mapAlert.setTarget(targetInfo);
        }

        return ret;
    }
}
