package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageListTableDao;
import cn.mw.monitor.ipaddressmanage.model.BatchOnLineUpdate;
import cn.mw.monitor.ipaddressmanage.model.IPOnline;
import cn.mw.monitor.ipaddressmanage.param.AddUpdateIpAddressManageListParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageScanService;
import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.snmp.service.MWTopologyService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
public class IcmpScan implements ScanStrategy {

    @Resource
    private MwIpAddressManageListTableDao mwIpAddressManageListTableDao;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private MWTopologyService mwTopologyService;

    @Autowired
    private MwIpAddressManageScanService mwIpAddressManageScanService;

    @Override
    public Reply ipScan(List<AddUpdateIpAddressManageListParam> uParam, Integer linkId, MWUser userInfo
    , IcmpScanCallback icpmScanCallback) throws Exception {

        //初始化消息记录
        mwMessageService.createMessage("扫描开始",1,3,userInfo);
        mwMessageService.createMessage(linkId.toString(),0,3,userInfo);
        mwMessageService.brokenPonint(linkId.toString());

        Map<String ,AddUpdateIpAddressManageListParam> map = new HashMap<>();
        List<String> ipList = new ArrayList<>();
        for(AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam : uParam){
            if(StringUtils.isNotEmpty(addUpdateIpAddressManageListParam.getIpAddress())){
                ipList.add(addUpdateIpAddressManageListParam.getIpAddress());
                map.put(addUpdateIpAddressManageListParam.getIpAddress() ,addUpdateIpAddressManageListParam);
            }
        }

        Map<String, List<IPInfoDTO>> scanRes = new HashMap<>();

        //2 调用接口获取ip状态
        Set<String> onLineIps = mwTopologyService.icmpCheck(ipList ,ipList.size());
        Set<String> offLineIps = new HashSet<>();
        for(String ip : ipList){
            List<IPInfoDTO> list = new ArrayList<>();
            IPInfoDTO ipInfoDTO = new IPInfoDTO();
            ipInfoDTO.setIp(ip);
            list.add(ipInfoDTO);
            scanRes.put(ip ,list);

            if(!onLineIps.contains(ip)){
                offLineIps.add(ip);
                ipInfoDTO.setOnline(false);
            }else{
                ipInfoDTO.setOnline(true);
            }
        }

        mwMessageService.createMessage(linkId.toString(),0,50,userInfo);
        mwMessageService.brokenPonint(linkId.toString());

        mwIpAddressManageScanService.updateIpStateAndAssetInfo(uParam ,scanRes);

        Map<Integer ,BatchOnLineUpdate> updateMap = new HashMap<>();
        if(null != onLineIps && onLineIps.size() > 0){
            for(String onLineIp : onLineIps){
                AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam = map.get(onLineIp);
                if(null != addUpdateIpAddressManageListParam){
                    addUpdateIpAddressManageListParam.setOnline(IPOnline.ONLINE.getCode());
                    addBatchOnLineUpdate(addUpdateIpAddressManageListParam ,updateMap);
                }
            }

        }

        if(offLineIps.size() > 0){
            for(String offLineIp : offLineIps) {
                AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam = map.get(offLineIp);
                if (null != addUpdateIpAddressManageListParam) {
                    addUpdateIpAddressManageListParam.setOnline(IPOnline.OFFLINE.getCode());
                    addBatchOnLineUpdate(addUpdateIpAddressManageListParam ,updateMap);
                }
            }
        }

        ExecutorService newFixedThreadPool = Executors.newSingleThreadExecutor();
        Future<String> submit = newFixedThreadPool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                updateIpInfo(updateMap);
                if(null != icpmScanCallback){
                    icpmScanCallback.callback(onLineIps);
                }
                return MwIpAddressManageScanService.SCAN_SUCCESS;
            }
        });

        mwMessageService.createMessage(linkId.toString(),0,80,userInfo);
        mwMessageService.brokenPonint(linkId.toString());
        String result = submit.get();
        log.info(result);
        newFixedThreadPool.shutdown();
        mwMessageService.brokenPonint(linkId.toString());
        mwMessageService.createMessage(linkId.toString(),0,100,userInfo);
        mwMessageService.createMessage("IP扫描结束",1,0,userInfo);

        return Reply.ok(result);
    }

    private void updateIpInfo(Map<Integer ,BatchOnLineUpdate> updateMap){
        Set<Integer> keys = updateMap.keySet();
        for(Integer key : keys){
            BatchOnLineUpdate batchOnLineUpdate = updateMap.get(key);
            mwIpAddressManageListTableDao.batchUpdateOnLineList(batchOnLineUpdate);
        }
    }

    private void addBatchOnLineUpdate(AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam
            ,Map<Integer ,BatchOnLineUpdate> updateMap){
        BatchOnLineUpdate batchOnLineUpdate = new BatchOnLineUpdate(addUpdateIpAddressManageListParam.getIpState()
                ,addUpdateIpAddressManageListParam.getOnline() ,addUpdateIpAddressManageListParam.getId());

        BatchOnLineUpdate data = updateMap.get(batchOnLineUpdate.hashCode());
        Date now = new Date();
        if(null != data){
            data.add(addUpdateIpAddressManageListParam.getId());
            data.setUpdateDate(now);
        }else{
            updateMap.put(batchOnLineUpdate.hashCode() ,batchOnLineUpdate);
            batchOnLineUpdate.setUpdateDate(now);
        }
    }
}
