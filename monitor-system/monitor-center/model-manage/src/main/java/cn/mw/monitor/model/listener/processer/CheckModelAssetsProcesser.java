package cn.mw.monitor.model.listener.processer;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.dto.DeviceCountDTO;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.event.EventProcFunc;
import cn.mw.monitor.model.listener.ModelAssetsCheckProcesser;
import cn.mw.monitor.service.assets.event.BatchAddAssetsEvent;
import cn.mw.monitor.service.assets.event.UpdateTangibleassetsEvent;
import cn.mw.monitor.service.assets.model.MwSnmpAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.DeviceScanContext;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.model.event.AddModelAssetsEvent;
import cn.mw.monitor.service.model.listener.CheckMWModelAssetsListener;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.MWSnmpSearchService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.Md5Utils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

/**
 * @author baochengbin
 * @date 2020/4/16
 */
@Service
@Slf4j
public class CheckModelAssetsProcesser implements CheckMWModelAssetsListener, ModelAssetsCheckProcesser, InitializingBean {

    @Value("${asset.scan.proxy.port}")
    private Integer proxyPort;

    private Map<String, EventProcFunc> eventProcFuncMap = new HashMap<String, EventProcFunc>();

    private final static String specialRand="华智达";
    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Resource
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        //新建资产进行CHECK
        if (event instanceof AddModelAssetsEvent) {
            AddModelAssetsEvent addEvent = (AddModelAssetsEvent) event;
            List<Reply> faillist = processCheckModelAssets(addEvent.getAddModelAssetsParam(), true);
            return faillist;
        } else if (event instanceof UpdateTangibleassetsEvent) {
            UpdateTangibleassetsEvent updateEvent = (UpdateTangibleassetsEvent) event;
            List<Reply> faillist = processCheckModelAssets(updateEvent.getUpdateTangAssetsParam(), false);
            return faillist;
        }

        EventProcFunc eventProcFunc = eventProcFuncMap.get(event.getClass().getSimpleName());
        if (null != eventProcFunc) {
            List<Reply> faillist = eventProcFunc.process(event);
            return faillist;
        }
        return null;
    }

    @Override
    public List<Reply> processCheckModelAssets(AddUpdateTangAssetsParam aParam, boolean isAdd) {
        List<Reply> faillist = new ArrayList<>();
        List<String> errerMsg = new ArrayList<>();
        boolean ipIsChanged = false;

        RuleType ruleType = RuleType.getInfoByMonitorMode(aParam.getMonitorMode());

        //如果类型是网络设备,说明通过其他的方式,如vxlan的http,获取设备信息,此时不需要设置厂商,规格型号
        if (ruleType != RuleType.NetWorkDevice) {
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
        switch (ruleType) {
            case ZabbixAgent:
                log.info("checkModelAssets 1 :" + JSONObject.toJSONString(aParam));
                if (aParam.getInBandIp() != null && StringUtils.isNotEmpty(aParam.getInBandIp())) {
                    List<Map<String, Object>> ckeckDTO = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                            .inBandIp(aParam.getInBandIp()).monitorMode(aParam.getMonitorMode()).assetsTypeId(aParam.getAssetsTypeId()).filterQuery(true).build());
                    if (isAdd) {
                        //如果是新增，去除本身的数据
                        ckeckDTO = ckeckDTO.stream().filter(map -> intValueConvert(map.get("modelInstanceId")) != aParam.getInstanceId().intValue()).collect(Collectors.toList());
                    }
                    if (ckeckDTO.size() > 0) {
                        log.info("processCheckModelAssets:::ckeckDTO"+ckeckDTO+"QueryModelAssetsParam::"+aParam);
                        if (isAdd) {
                            errerMsg.add("IP地址重复");
                            log.warn("processCheckTangibleAssets isAdd" + isAdd + ";IP地址重复:" + aParam.getInBandIp());
                        } else {
                            if (!ckeckDTO.get(0).get(MwModelViewCommonService.INSTANCE_ID_KEY).equals(aParam.getInstanceId())) {
                                errerMsg.add("IP地址重复");
                                log.warn("processCheckTangibleAssets isAdd" + isAdd + ";IP地址重复:" + aParam.getInBandIp());
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
        if (ruleType == RuleType.SNMPv1v2 || ruleType == RuleType.SNMPv3) {
            ////是否忽略deviceCode校验，忽略设置后，不进行code校验
            //snmp扫描时，对特殊的厂商设备进行例外处理（不进行deviceCode校验）
            if(specialRand.contains(aParam.getManufacturer()) || aParam.isIgnoreCodeCheck()){
                if (aParam.getInBandIp() != null && StringUtils.isNotEmpty(aParam.getInBandIp())) {
                    List<Map<String, Object>> ckeckDTO = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                            .inBandIp(aParam.getInBandIp()).monitorMode(aParam.getMonitorMode()).assetsTypeId(aParam.getAssetsTypeId()).filterQuery(true).build());
                    if (isAdd) {
                        //如果是新增，去除本身的数据
                        ckeckDTO = ckeckDTO.stream().filter(map -> intValueConvert(map.get("modelInstanceId")) != aParam.getInstanceId().intValue()).collect(Collectors.toList());
                    }
                    if (ckeckDTO.size() > 0) {
                        log.info("processCheckModelAssets:::ckeckDTO"+ckeckDTO+"QueryModelAssetsParam::"+aParam);
                        if (isAdd) {
                            errerMsg.add("IP地址重复");
                            log.warn("processCheckTangibleAssets isAdd" + isAdd + ";IP地址重复:" + aParam.getInBandIp());
                        } else {
                            if (!ckeckDTO.get(0).get(MwModelViewCommonService.INSTANCE_ID_KEY).equals(aParam.getInstanceId())) {
                                errerMsg.add("IP地址重复");
                                log.warn("processCheckTangibleAssets isAdd" + isAdd + ";IP地址重复:" + aParam.getInBandIp());
                            }
                        }
                    }
                }

            }
            else{
                log.info("checkModelAssets 1 :" + JSONObject.toJSONString(aParam));
                MwSnmpv1AssetsDTO snmpV1AssetsDTO = aParam.getSnmpV1AssetsDTO();
                MwSnmpAssetsDTO mwSnmpAssetsDTO = aParam.getSnmpAssetsDTO();
                //进入SNMP校验，必须snmpV1V2DTO、SnmpV3DTO两者有一个有值
                //否则不需要校验（手动添加纳管资产时，选择snmp方式的模板，会导致扫描方式是SNMP，但V1V2 和 V3都没有值）
                if ((null != snmpV1AssetsDTO && (snmpV1AssetsDTO.getPort() != null)) || (null != mwSnmpAssetsDTO && ((mwSnmpAssetsDTO.getPort() != null)))) {
                    if (isAdd || ipIsChanged) {
                        DeviceScanContext deviceScanContext = new DeviceScanContext();
                        String proxyIp = "";
                        String proxyServerId = aParam.getPollingEngine();
                        if (StringUtils.isNotEmpty(proxyServerId)
                                && !MwEngineCommonsService.LOCALHOST_KEY.equals(proxyServerId)) {
                            MwEngineManageDTO mwEngineManageDTO = mwEngineCommonsService.selectEngineByIdNoPerm(proxyServerId);
                            proxyIp = mwEngineManageDTO.getProxyAddress();
                            ProxyInfo proxyInfo = new ProxyInfo(mwEngineManageDTO.getProxyAddress(), proxyPort);
                            deviceScanContext.setProxyInfo(proxyInfo);
                        }
                        MWSnmpSearchService mwSnmpSearchService = SpringUtils.getBean(MWSnmpSearchService.class);
                        log.info("processCheckTangibleAssets ip:{}, proxy:{}", aParam.getInBandIp(), proxyIp);
                        deviceScanContext.setAddUpdateTangAssetsParam(aParam);
                        String deviceCode = mwSnmpSearchService.searchDeviceCode(deviceScanContext);
                        log.info("checkModelAssets 222 :" + JSONObject.toJSONString(deviceCode));
                        //查询设备是否存在
                        String md5Code = "";
                        List<String> list = new ArrayList<>();
                        List<DeviceCountDTO> deviceCountDTOS = new ArrayList<>();
                        if (!Strings.isNullOrEmpty(deviceCode) && !"null".equals(deviceCode)) {
                            md5Code = Md5Utils.encode(deviceCode);
                            list.add(md5Code);
                            aParam.setDeviceCode(md5Code);
                            deviceCountDTOS = mwTangibleAssetsDao.deviceCount(list);
                        }
                        log.info("checkModelAssets 333333:" + JSONObject.toJSONString(deviceCountDTOS));
                        if (null != deviceCountDTOS && deviceCountDTOS.size() > 0) {
                            DeviceCountDTO deviceCountDTO = deviceCountDTOS.get(0);
                            if (deviceCountDTO.getCount() > 0) {
                                String ip = (StringUtils.isNotEmpty(aParam.getInBandIp()) ? aParam.getInBandIp() : aParam.getOutBandIp());
                                String msg = Reply.replaceMsg(ErrorConstant.TANGASSETS_MSG_210121, new String[]{ip});
                                log.info("monitorMode:[{}];exist ip:[{}];device code:[{}];new deviceCode:[{}]"
                                        , ruleType.getName(), ip, deviceCountDTO.getDeviceCode(), deviceCode);
                                errerMsg.add(msg);
                            }
                        }
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
    private List<Reply> checkBatchAdd(BatchAddAssetsEvent batchAddAssetsEvent) {
        log.info("CheckAssetsProcesser checkBatchAdd!");
        List<ScanResultSuccess> scanResultSuccess = batchAddAssetsEvent.getScanResultSuccessList();
        //是否忽略deviceCode校验，默认不忽略
       boolean isIgnoreCodeCheck = batchAddAssetsEvent.isIgnoreCodeCheck();
        List<ScanResultSuccess> snmpResults = new ArrayList<>();
        List<ScanResultSuccess> otherResults = new ArrayList<>();
        //区分snmp扫描结果和非snmp扫描结果
        for (ScanResultSuccess scanResultSuccess1 : scanResultSuccess) {
            RuleType rt = RuleType.valueOf(scanResultSuccess1.getMonitorMode());
            switch (rt) {
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
        if (snmpResults.size() > 0 && !isIgnoreCodeCheck) {
            Set<Integer> redundantIdSet = new HashSet<>();
            Set<String> deviceIDCodeSet = new HashSet<>();

            //检查是否含有没有deviceCode的结果,并检查是否有冗余的设备
            List<ScanResultSuccess> noDeviceCodeResult = new ArrayList<>();
            for(ScanResultSuccess result : snmpResults){
                if(StringUtils.isEmpty(result.getDeviceCode())){
                    noDeviceCodeResult.add(result);
                }else if(deviceIDCodeSet.contains(result.getDeviceCode())){
                    redundantIdSet.add(result.getId());
                }else{
                    deviceIDCodeSet.add(result.getDeviceCode());
                }
            }

            MWSnmpSearchService mwSnmpSearchService = (MWSnmpSearchService) SpringUtils.getBean(MWSnmpSearchService.class);
            List<ScanResultSuccess> redundantResults = mwSnmpSearchService.searchRedundantResult(noDeviceCodeResult);
            List<ScanResultSuccess> filterResultSuccess = new ArrayList<ScanResultSuccess>();

            for (ScanResultSuccess scanResultSuccess1 : redundantResults) {
                redundantIdSet.add(scanResultSuccess1.getId());
            }

            //过滤冗余结果
            List<String> deviceCodes = new ArrayList<String>();
            Map<String, ScanResultSuccess> assetIpMap = new HashMap<String, ScanResultSuccess>();
            for (ScanResultSuccess scanResultSuccess1 : snmpResults) {
                if (!redundantIdSet.contains(scanResultSuccess1.getId())) {
                    filterResultSuccess.add(scanResultSuccess1);
                    if (!Strings.isNullOrEmpty(scanResultSuccess1.getDeviceCode())) {
                        deviceCodes.add(scanResultSuccess1.getDeviceCode());
                        assetIpMap.put(scanResultSuccess1.getDeviceCode(), scanResultSuccess1);
                    }
                }
            }
            List<DeviceCountDTO> deviceCountDTOS = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(deviceCodes)){
                //查询设备是否存在
               deviceCountDTOS = mwTangibleAssetsDao.deviceCount(deviceCodes);
            }
            StringBuffer existDeviceIP = new StringBuffer();
            List<ScanResultSuccess> delResults = new ArrayList<>();
            for (DeviceCountDTO deviceCountDTO : deviceCountDTOS) {
                if (deviceCountDTO.getCount() > 0 && !Strings.isNullOrEmpty(deviceCountDTO.getDeviceCode())) {
                    ScanResultSuccess result = assetIpMap.get(deviceCountDTO.getDeviceCode());
                    String ip = "";
                    if (result != null && !Strings.isNullOrEmpty(result.getIpAddress())) {
                        ip = result.getIpAddress();
                        existDeviceIP.append(",").append(ip);
                    }
                    log.info("exist ip:[{}]" + ip + ";new DeviceCode:[{}]"
                            , ip, deviceCountDTO.getDeviceCode());
                    for (ScanResultSuccess fiterResult : filterResultSuccess) {
                        if (fiterResult != null && fiterResult.getDeviceCode() != null && fiterResult.getDeviceCode().equals(deviceCountDTO.getDeviceCode())) {
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

            log.info("checkBatchAdd 方法结束！");
            if (StringUtils.isNotEmpty(existDeviceIP)) {
                String msg = Reply.replaceMsg(ErrorConstant.TANGASSETS_MSG_210121, new String[]{existDeviceIP.toString().substring(1)});
                retlist.add(Reply.fail(msg));
            }
        }
        return retlist;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventProcFunc batchAddEventFunc = (event) -> checkBatchAdd((BatchAddAssetsEvent) event);
        eventProcFuncMap.put(BatchAddAssetsEvent.class.getSimpleName(), batchAddEventFunc);
    }
}
