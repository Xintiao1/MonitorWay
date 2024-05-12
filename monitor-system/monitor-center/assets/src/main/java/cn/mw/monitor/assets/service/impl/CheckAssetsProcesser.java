package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.dto.DeviceCountDTO;
import cn.mw.monitor.assets.service.CheckTangibleAssetsListener;
import cn.mw.monitor.assets.service.IMWAssetsCheckProcesser;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.event.EventProcFunc;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchAddAssetsEvent;
import cn.mw.monitor.service.assets.event.UpdateTangibleassetsEvent;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.DeviceScanContext;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.scan.MWSnmpSearchService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.Md5Utils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author baochengbin
 * @date 2020/4/16
 */
@Service
@Slf4j
public class CheckAssetsProcesser implements CheckTangibleAssetsListener, IMWAssetsCheckProcesser, InitializingBean {

    @Value("${asset.scan.proxy.port}")
    private Integer proxyPort;

    private Map<String, EventProcFunc> eventProcFuncMap = new HashMap<String, EventProcFunc>();

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        //新建资产进行CHECK
        if (event instanceof AddTangibleassetsEvent) {
            AddTangibleassetsEvent addEvent = (AddTangibleassetsEvent) event;
            List<Reply> faillist = processCheckTangibleAssets(addEvent.getAddTangAssetsParam(), true);
            return faillist;
        } else if (event instanceof UpdateTangibleassetsEvent) {
            UpdateTangibleassetsEvent updateEvent = (UpdateTangibleassetsEvent) event;
            List<Reply> faillist = processCheckTangibleAssets(updateEvent.getUpdateTangAssetsParam(), false);
            return faillist;
        }

        EventProcFunc eventProcFunc = eventProcFuncMap.get(event.getClass().getSimpleName());
        if(null != eventProcFunc){
            List<Reply> faillist = eventProcFunc.process(event);
            return faillist;
        }
        return null;
    }

    @Override
    public List<Reply> processCheckTangibleAssets(AddUpdateTangAssetsParam aParam, boolean isAdd) {
        List<Reply> faillist = new ArrayList<>();
        List<String> errerMsg = new ArrayList<>();
        boolean ipIsChanged = false;

        RuleType ruleType = RuleType.getInfoByMonitorMode(aParam.getMonitorMode());

        //如果类型是网络设备,说明通过其他的方式,如vxlan的http,获取设备信息,此时不需要设置厂商,规格型号
        if(ruleType != RuleType.NetWorkDevice) {
            //校验厂商和规格型号不能为空
            if (StringUtils.isBlank(aParam.getManufacturer())) {
                errerMsg.add("厂商不能为空");
            }
            if (StringUtils.isBlank(aParam.getSpecifications())) {
                errerMsg.add("规格型号不能为空");
            }
        }
        //重复性校验
        //当监控方式是中间件，数据库，应用的时候不进行ip校验
        switch (ruleType){
            case SNMPv1v2:
            case SNMPv3:
            case ZabbixAgent:
                if (aParam.getInBandIp() != null && StringUtils.isNotEmpty(aParam.getInBandIp())) {
                    List<MwTangibleassetsDTO> ckeckDTO = mwTangibleAssetsDao.check(QueryTangAssetsParam.builder()
                            .inBandIp(aParam.getInBandIp()).monitorMode(aParam.getMonitorMode()).build());
                    if (ckeckDTO.size() > 0) {
                        if (isAdd) {
                            errerMsg.add("IP地址重复");
                            log.warn("processCheckTangibleAssets isAdd"+ isAdd +";IP地址重复:" + aParam.getInBandIp());
                        } else {
                            if (!ckeckDTO.get(0).getId().equals(aParam.getId())) {
                                errerMsg.add("IP地址重复");
                                log.warn("processCheckTangibleAssets isAdd"+ isAdd +";IP地址重复:" + aParam.getInBandIp());
                            }
                        }
                    } else {
                        if (!isAdd) {
                            ipIsChanged = true;
                        }
                    }
                }
                break;
            default:
        }

        if (aParam.getAssetsId() != null && StringUtils.isNotEmpty(aParam.getAssetsId())) {
            List<MwTangibleassetsDTO> ckeckDTO1 = mwTangibleAssetsDao.check(QueryTangAssetsParam.builder()
                    .monitorServerId(aParam.getMonitorServerId()).assetsId(aParam.getAssetsId()).build());
            if (ckeckDTO1.size() > 0) {
                if (isAdd) {
                    errerMsg.add("主机id重复");
                    log.warn("processCheckTangibleAssets 主机id重复 isAdd"+ isAdd +";aParam:" + aParam.toString());
                } else {
                    if (!ckeckDTO1.get(0).getId().equals(aParam.getId())) {
                        errerMsg.add("主机id重复");
                        log.warn("processCheckTangibleAssets 主机id重复 isAdd"+ isAdd +";aParam:" + aParam.toString());
                    }
                }

            }
        }

        if (ruleType == RuleType.SNMPv1v2 || ruleType == RuleType.SNMPv3) {
            if (isAdd || ipIsChanged) {
                String proxyIp = "";
                DeviceScanContext deviceScanContext = new DeviceScanContext();
                String proxyServerId = aParam.getPollingEngine();
                if(StringUtils.isNotEmpty(proxyServerId)
                && !MwEngineCommonsService.LOCALHOST_KEY.equals(proxyServerId)){
                    MwEngineManageDTO mwEngineManageDTO = mwEngineCommonsService.selectEngineByIdNoPerm(proxyServerId);
                    proxyIp = mwEngineManageDTO.getProxyAddress();
                    ProxyInfo proxyInfo = new ProxyInfo(mwEngineManageDTO.getProxyAddress() ,proxyPort);
                    deviceScanContext.setProxyInfo(proxyInfo);
                }

                MWSnmpSearchService mwSnmpSearchService = SpringUtils.getBean(MWSnmpSearchService.class);
                log.info("processCheckTangibleAssets ip:{} ,proxy:{}" ,aParam.getInBandIp() ,proxyIp);
                deviceScanContext.setAddUpdateTangAssetsParam(aParam);
                String deviceCode = mwSnmpSearchService.searchDeviceCode(deviceScanContext);
                //查询设备是否存在
                List<String> list = new ArrayList<>();
                String md5Code = Md5Utils.encode(deviceCode);
                list.add(md5Code);
                aParam.setDeviceCode(md5Code);
                List<DeviceCountDTO> deviceCountDTOS = mwTangibleAssetsDao.deviceCount(list);
                if (null != deviceCountDTOS && deviceCountDTOS.size() > 0) {
                    DeviceCountDTO deviceCountDTO = deviceCountDTOS.get(0);
                    if (deviceCountDTO.getCount() > 0) {
                        String ip = (StringUtils.isNotEmpty(aParam.getInBandIp()) ? aParam.getInBandIp() : aParam.getOutBandIp());
                        String msg = Reply.replaceMsg(ErrorConstant.TANGASSETS_MSG_210121, new String[]{ip});
                        log.info("monitorMode:[{}];exist ip:[{}];device code:[{}];new deviceCode:[{}]"
                                ,ruleType.getName() ,ip ,deviceCountDTO.getDeviceCode(), deviceCode);
                        errerMsg.add(msg);
                    }
                }
            }
        }

        if (errerMsg.size() > 0) {
            String msg = StringUtils.join(new String[]{ErrorConstant.TANGASSETS_MSG_210104 + ErrorConstant.TANGASSETS_MSG_210112, StringUtils.join(errerMsg, "、")});
            log.error(msg);
            faillist.add(Reply.fail(ErrorConstant.TANGASSETSCODE_210104, msg));
        }
        return faillist;
    }

    //找到重复的扫描结果信息并返回
    private List<Reply> checkBatchAdd(BatchAddAssetsEvent batchAddAssetsEvent){
        log.info("CheckAssetsProcesser checkBatchAdd!");
        List<ScanResultSuccess> scanResultSuccess = batchAddAssetsEvent.getScanResultSuccessList();
        List<ScanResultSuccess> snmpResults = new ArrayList<>();
        List<ScanResultSuccess> otherResults = new ArrayList<>();
        //区分snmp扫描结果和非snmp扫描结果
        for(ScanResultSuccess scanResultSuccess1:scanResultSuccess){
            RuleType rt = RuleType.valueOf(scanResultSuccess1.getMonitorMode());
            switch (rt){
                case SNMPv1v2:
                case SNMPv3:
                    snmpResults.add(scanResultSuccess1);
                    break;
                default:
                    otherResults.add(scanResultSuccess1);
                    break;
            }
        }


        //由于一个网络设备有多个接口,不同的ip可能对应同一个网络设备
        //因此通过网络设备上所有的接口来唯一识别一个网络设备
        //查询冗余的扫描结果,同时设置设备编码
        List<Reply> retlist = new ArrayList<>();
        if(snmpResults.size() > 0) {
            MWSnmpSearchService mwSnmpSearchService = (MWSnmpSearchService) SpringUtils.getBean(MWSnmpSearchService.class);
            List<ScanResultSuccess> redundantResults = mwSnmpSearchService.searchRedundantResult(snmpResults);
            List<ScanResultSuccess> filterResultSuccess = new ArrayList<ScanResultSuccess>();

            Set<Integer> idSet = new HashSet<>();
            for (ScanResultSuccess scanResultSuccess1 : redundantResults) {
                idSet.add(scanResultSuccess1.getId());
            }

            //过滤冗余结果
            List<String> deviceCodes = new ArrayList<String>();
            Map<String, ScanResultSuccess> assetIpMap = new HashMap<String, ScanResultSuccess>();
            for (ScanResultSuccess scanResultSuccess1 : scanResultSuccess) {
                if (!idSet.contains(scanResultSuccess1.getId())) {
                    filterResultSuccess.add(scanResultSuccess1);
                    deviceCodes.add(scanResultSuccess1.getDeviceCode());
                    assetIpMap.put(scanResultSuccess1.getDeviceCode(), scanResultSuccess1);
                }
            }

            //查询设备是否存在
            List<DeviceCountDTO> deviceCountDTOS = mwTangibleAssetsDao.deviceCount(deviceCodes);
            StringBuffer existDeviceIP = new StringBuffer();
            List<ScanResultSuccess> delResults = new ArrayList<>();
            for (DeviceCountDTO deviceCountDTO : deviceCountDTOS) {
                if (deviceCountDTO.getCount() > 0) {
                    ScanResultSuccess result = assetIpMap.get(deviceCountDTO.getDeviceCode());
                    String ip = result.getIpAddress();
                    existDeviceIP.append(",").append(ip);
                    log.info("exist ip:[{}]" + ip + ";new DeviceCode:[{}]"
                            ,ip ,deviceCountDTO.getDeviceCode());
                    for (ScanResultSuccess fiterResult : filterResultSuccess) {
                        if (fiterResult.getDeviceCode().equals(deviceCountDTO.getDeviceCode())) {
                            delResults.add(fiterResult);
                        }
                    }
                }
            }

            //删除已经存在扫描结果
            for (ScanResultSuccess delResult : delResults) {
                filterResultSuccess.remove(delResult);
            }

            //合并过滤后的扫描结果
            otherResults.addAll(filterResultSuccess);

            Reply reply = Reply.ok(otherResults);
            reply.setRes(PaasConstant.RES_FILTER);
            retlist.add(reply);

            if (StringUtils.isNotEmpty(existDeviceIP)) {
                String msg = Reply.replaceMsg(ErrorConstant.TANGASSETS_MSG_210121, new String[]{existDeviceIP.toString().substring(1)});
                retlist.add(Reply.fail(msg));
            }
        }
        return retlist;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventProcFunc batchAddEventFunc = (event) -> checkBatchAdd((BatchAddAssetsEvent)event);
        eventProcFuncMap.put(BatchAddAssetsEvent.class.getSimpleName(), batchAddEventFunc);
    }
}
