package cn.mw.monitor.scanrule.model;

import cn.mw.monitor.api.common.IpV6Util;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.scanrule.api.param.scanrule.AddScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.AssetsScanParam;
import cn.mw.monitor.scanrule.api.param.scanrule.AssetsScanRuleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.UpdateScanruleParam;
import cn.mw.monitor.scanrule.dto.*;
import cn.mw.monitor.scanrule.service.MwScanruleService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.model.SecurityLevel;
import cn.mw.monitor.service.scan.param.QueryScanResultParam;
import cn.mw.monitor.service.scan.param.RuleParam;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.dto.MwLoginUserDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.snmp.model.ExceuteInfo;
import cn.mw.monitor.snmp.param.scan.IPRangeParam;
import cn.mw.monitor.snmp.param.scan.ScanParam;
import cn.mw.monitor.snmp.param.scan.SubnetParam;
import cn.mw.monitor.snmp.service.IMonitor;
import cn.mw.monitor.snmp.service.MWScanService;
import cn.mw.monitor.snmp.service.scan.ScanManager;
import cn.mw.monitor.snmp.service.scan.ScanResult;
import cn.mw.monitor.snmp.utils.ResovlerUtil;
import cn.mw.monitor.util.AssetsUtils;
import cn.mw.monitor.util.KafkaProducerUtil;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName ScanTaskManage
 * @Description 扫描队列操作
 * @Author gengjb
 * @Date 2022/9/21 10:54
 * @Version 1.0
 **/
@Component
@Slf4j
public class ScanTaskManage {

    //返回结果数据
    ConcurrentHashMap<String,AddUpdateTangAssetsParam> preAddAssetsMap = new ConcurrentHashMap();

    //返回结果数据
    ConcurrentHashMap scanResult = new ConcurrentHashMap();

    Queue<AssetsScanTaskRecord> queue = new ArrayBlockingQueue(100);

    private final String topic = "scan-queue";

    @Autowired
    private MwScanruleService mwScanruleService;

    @Autowired
    private IMonitor monitor;

    @Autowired
    private MWScanService mwScanService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private KafkaProducerUtil kafkaProducerUtil;



    /**
     * 创建扫描队列
     * @param scanTaskDto 创建信息
     */

    public void createQueue(AssetsScanTaskDto scanTaskDto){
        try {
            //将参数转为集合
            String jsonString = JSONObject.toJSONString(scanTaskDto);
            List<String> value = new ArrayList<>();
            value.add(jsonString);
            //删除执行完成的队列数据
            for (AssetsScanTaskRecord taskRecord : queue) {
                Integer taskStatus = taskRecord.getTaskStatus();
                if(ScanTaskExecuteStatus.EXECUTE_COMPLETE.getCode().equals(taskStatus)){
                    queue.remove(taskRecord);
                }
            }
            if(CollectionUtils.isEmpty(queue)){
                AssetsScanTaskRecord taskRecord = new AssetsScanTaskRecord();
                taskRecord.setTaskId(scanTaskDto.getTaskId());
                taskRecord.setTaskStatus(ScanTaskExecuteStatus.EXECUTE_IN.getCode());
                queue.add(taskRecord);
            }else{
                AssetsScanTaskRecord taskRecord = new AssetsScanTaskRecord();
                taskRecord.setTaskId(scanTaskDto.getTaskId());
                taskRecord.setTaskStatus(ScanTaskExecuteStatus.TOBE_EXECUTE.getCode());
                queue.add(taskRecord);
            }
            //将任务放入kafuka队列中
            kafkasend(value);
        }catch (Throwable e){
            log.error("创建扫描队列失败，失败信息:"+e.getMessage());
        }
    }

    /**
     * Kafka发送消息
     */
    public void kafkasend(List<String> value){
        kafkaProducerUtil.send(topic,value);
        log.info("资产扫描发送消息成功");
    }


    /**
     * 获取扫描结果
     */
    public  Queue<AssetsScanTaskRecord> getQueue(){
        try {
            return queue;
        }catch (Throwable e){
            log.error("获取队列扫描结果失败，失败信息:"+e.getMessage());
        }
        return null;
    }

    @Autowired
    public MWMessageService mwMessageService;

    /**
     * 执行扫描任务
     */
    @KafkaListener(idIsGroup = false,groupId ="scan",topics = {topic},containerFactory = "KafkaConsumerConfig")
    public void runQueue(ConsumerRecord<?, ?> record){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            if (!kafkaMessage.isPresent())return;
            String message = kafkaMessage.get().toString();
            if(StringUtils.isBlank(message))return;
            //将数据转换为实体类
            AssetsScanTaskDto assetsScanTaskDto = JSON.parseObject(message, AssetsScanTaskDto.class);
            log.info("扫描队列消费中"+assetsScanTaskDto);
            //设置本地线程用户信息
            setUserNewsLocalThread(assetsScanTaskDto);
            if(CollectionUtils.isNotEmpty(queue)){
                for (AssetsScanTaskRecord taskRecord : queue) {
                    if(taskRecord.getTaskId().equals(assetsScanTaskDto.getTaskId())){
                        taskRecord.setTaskStatus(ScanTaskExecuteStatus.EXECUTE_IN.getCode());
                    }
                }
            }
            assetsScanTaskDto.setStartTime(format.format(new Date()));
            //进行资产扫描操作
            Reply reply = scanData(assetsScanTaskDto.getScanContext());
            log.info("扫描操作结束"+reply);
            scanResult.put(assetsScanTaskDto.getTaskId(),reply);
            assetsScanTaskDto.setEndTime(format.format(new Date()));
            if(CollectionUtils.isNotEmpty(queue)){
                for (AssetsScanTaskRecord taskRecord : queue) {
                    if(taskRecord.getTaskId().equals(assetsScanTaskDto.getTaskId())){
                        taskRecord.setTaskStatus(ScanTaskExecuteStatus.EXECUTE_COMPLETE.getCode());
                    }
                }
            }
            List<MWUser> mwUsers = new ArrayList<>();
            mwUsers.add(MWUser.builder().userId(assetsScanTaskDto.getUserId()).userName(assetsScanTaskDto.getExecutionUser()).loginName(assetsScanTaskDto.getExecutionUser()).build());
            Integer executionMode = assetsScanTaskDto.getExecutionMode();
            log.info("开始发送消息"+executionMode);
            //如果是单个添加资产，需要在扫描完成后自动进行资产添加
            if(executionMode == 1){
                autoMaticCreateAssets(assetsScanTaskDto,reply,mwUsers);
                mwMessageService.sendAssetsScanCompleteMessage("资产添加扫描任务完成，扫描IP:"+assetsScanTaskDto.getScanContext().getScanParam().getIplist()+" 扫描结果:"+reply,mwUsers,"单个资产扫描完成");
            }
            if(executionMode == 2){
                mwMessageService.sendAssetsScanCompleteMessage("资产扫描任务完成，扫描规则名称:"+assetsScanTaskDto.getScanContext().getScanParam().getName()+" 扫描结果:"+reply,mwUsers,"资产扫描任务完成");
            }
            scanResult.remove(assetsScanTaskDto.getTaskId());
        }catch (Throwable e){
             log.error("执行扫描任务失败，失败信息:"+e.getMessage());
        }finally {
            iLoginCacheInfo.removeLocalTread();
            MwLoginUserDto localTread = iLoginCacheInfo.getLocalTread();
            if(localTread != null){
                iLoginCacheInfo.removeLocalTread();
            }
        }
    }

    /**
     * 删除队列中的任务
     * @param scanTaskDto 队列信息
     */
    public void deleteQueue(AssetsScanTaskDto scanTaskDto){
        try {

        }catch (Throwable e){
            log.error("删除队列任务失败，失败信息:"+e.getMessage());
        }
    }


    public Reply scanData(AssetsScanContext scanContext){
        AssetsScanParam assetsScanParam = scanContext.getScanParam();
        //格式校验和数据重组
        ScanParam scanParam = tranform(assetsScanParam);
        ScanManager scanManager = (ScanManager) SpringUtils.getBean("scanManager");
        ScanResult scanResult = null;
        Reply ret = null;
        boolean hasError = false;
        Integer ruleId = 0;
        try {
            Date currentDate = new Date();

            Reply reply = null;
            //数据格式转换
            AddScanruleParam addScanruleParam = tranform(scanParam);
            if (scanContext.isSaveScanRuled()) {
                //保存扫描规则
                addScanruleParam.setCreateDate(currentDate);
                addScanruleParam.setScanStartDate(currentDate);

                //单个添加资产时,物理删除,保证//  @MwPermit(moduleName = "ip_manage")模块中,该规则不可见
                Integer isdelete = assetsScanParam.getIsdelete();
                if (isdelete != null && isdelete == 1) {
                    addScanruleParam.setDeleteFlag(1);
                }
                //扫描规则保存入库
                reply = mwScanruleService.insert(addScanruleParam);
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    addScanruleParam = (AddScanruleParam) reply.getData();
                }
            } else {
                addScanruleParam.setId(assetsScanParam.getRuleId());
            }

            ruleId = addScanruleParam.getId();

            //判断是否立即执行, 新增规则时默认不执行
            if (assetsScanParam.isExecuteNow()) {
                monitor.register(ruleId, Thread.currentThread());
                ExceuteInfo ei = monitor.getExceuteInfo(Thread.currentThread().getName());
                scanManager.setThreadName(Thread.currentThread().getName());
                monitor.setStartTime(ruleId, currentDate);
                Object lockObject = new Object();
                ei.setLockObject(lockObject);
                scanResult = scanManager.scan(scanParam);
                scanResult.setScanTime(currentDate);
            }
            if (null != scanResult) {
                //保存扫描结果
                //查询分组信息
                scanParam.setRuleId(addScanruleParam.getId());
                Reply stsReply = null;
                List<MwModelAssetsGroupTable> mwModelAssetsGroupTables = new ArrayList<>();
                //模型管理下的调用。
                if(assetsScanParam.getIsNewVersion()!=null && assetsScanParam.getIsNewVersion()){
                    ////  模型分组替代资产类型
                    mwModelAssetsGroupTables = new ArrayList<>();
                    stsReply = mwScanruleService.selectGroupServerMap(null);
                    mwModelAssetsGroupTables = (List<MwModelAssetsGroupTable>) stsReply.getData();
                }else{
                    stsReply = mwScanruleService.selectGroupServerMapList();
                    //将原先的MwAssetsGroupTable 实体转为 MwModelAssetsGroupTable；
                    for(MwAssetsScanGroupTable table :  (List<MwAssetsScanGroupTable>)stsReply.getData()){
                        MwModelAssetsGroupTable mwModelAssetsGroupTable = new MwModelAssetsGroupTable();
                        BeanUtils.copyProperties(table,mwModelAssetsGroupTable);
                        mwModelAssetsGroupTables.add(mwModelAssetsGroupTable);
                    }
                }
                if (null != stsReply && stsReply.getRes() != PaasConstant.RES_SUCCESS) {
                    scanParam = (ScanParam) stsReply.getData();
                    throw new Exception(" select group mapper fail!");
                }
                Map<String, String> groupMap = new HashMap<String, String>();
                if (null != mwModelAssetsGroupTables && mwModelAssetsGroupTables.size() > 0) {
                    mwModelAssetsGroupTables.forEach(value -> {
                        log.info("assetsTypeId:" + value.getAssetsSubtypeId() + ";getMonitorServerId:" + assetsScanParam.getMonitorServerId());
                        String key = AssetsUtils.genGroupKey(value.getAssetsSubtypeId(), value.getMonitorServerId());
                        groupMap.put(key, value.getGroupId());
                    });
                }
                reply = mwScanService.addScanResult(scanParam, scanResult, scanContext.isRescan(), groupMap,assetsScanParam.getIsNewVersion());
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    scanParam = (ScanParam) reply.getData();
                    hasError = true;
                }
                UpdateScanruleParam updateScanruleParam = new UpdateScanruleParam();
                updateScanruleParam.setId(addScanruleParam.getId());
                Date scanEndDate = new Date();
                updateScanruleParam.setModificationDate(scanEndDate);
                updateScanruleParam.setScanEndDate(scanEndDate);
                mwScanruleService.update(updateScanruleParam, false);
                monitor.clean(ruleId);
                //返回扫描结果
                QueryScanResultParam queryScanResultParam = new QueryScanResultParam();
                queryScanResultParam.setScanruleId(scanParam.getRuleId());
                queryScanResultParam.setScanBatch(scanParam.getBatchNo());
                queryScanResultParam.setPageSize(assetsScanParam.isNmapFlag() ? Integer.MAX_VALUE : queryScanResultParam.getPageSize());
                ret = mwScanService.scanResultSearch(queryScanResultParam);
            }
        } catch (Exception e) {
            hasError = true;
            monitor.clean(ruleId);
            log.error("scan error", e);
        } finally {
            ExceuteInfo ei = monitor.getExceuteInfo(Thread.currentThread().getName());
            if (null != ei) {
                ei.isScanDoned(true);
            }
        }
        log.info("scan finished!");
        if (hasError) {
            return Reply.fail(ErrorConstant.SCANRULE_MSG_220106);
        }
        return ret;
    }


    private ScanParam tranform(AssetsScanParam assetsScanParam) {
        ScanParam scanParam = new ScanParam();
        scanParam.setResulttype(assetsScanParam.getResulttype());
        scanParam.setMonitorServerId(assetsScanParam.getMonitorServerId());
        scanParam.setRuleName(assetsScanParam.getName());
        scanParam.setRescanIds(assetsScanParam.getRescanIds());
        scanParam.setIpv6checked(assetsScanParam.isIpv6checked());
        //对ip地址清单中格式进行校验，以免之后格式不正确程序直接出错
        if (StringUtils.isNotEmpty(assetsScanParam.getIplist())) {
            String iplist = assetsScanParam.getIplist();
            String[] strs = iplist.trim().split(ResovlerUtil.IP_SEPERATOR);
            boolean isTrue = true;//假设ip地址清单中所有格式都是正确的
            String errorIp = "";
            if (strs != null && strs.length > 0) {
                for (String s : strs) {
                    boolean flag = assetsScanParam.isIpv6checked() ? IpV6Util.isIP(s) : isValidIpv4Addr(s);
                    errorIp = s;
                    if (!flag) {
                        isTrue = false;
                        break;
                    }
                }
            }
            if (!isTrue) {
                log.info("error ip format:" + errorIp);
                scanParam.setTipInfo("ip地址清单中存在错误格式");
                return scanParam;
            }
//            }
        }

        if (StringUtils.isNotEmpty(assetsScanParam.getIplist())) {
            scanParam.setIps(assetsScanParam.getIplist().trim());
        }

        List<IPRangeParam> ipRangeParamList = new ArrayList<IPRangeParam>();
        List<SubnetParam> subnetParamList = new ArrayList<SubnetParam>();
        if (null != assetsScanParam.getIpRange() && assetsScanParam.getIpRange().size() > 0) {

            //对ip段开始和结束 格式判断
            List<cn.mw.monitor.scanrule.api.param.scanrule.IPRangeParam> ipRanges = assetsScanParam.getIpRange();
            if (null != ipRanges && ipRanges.size() > 0) {
                for (cn.mw.monitor.scanrule.api.param.scanrule.IPRangeParam value : ipRanges) {
                    if (StringUtils.isNotEmpty(value.getStartip()) && StringUtils.isNotEmpty(value.getEndip())) {
                        String startip = value.getStartip();//开始ip
                        String endip = value.getEndip();//结束ip
                        boolean match_start = value.isIpv6checked() ? IpV6Util.isIP(startip) : isValidIpv4Addr(startip);
                        boolean mathc_end =  value.isIpv6checked() ? IpV6Util.isIP(startip) : isValidIpv4Addr(endip);
                        if (match_start && mathc_end) {
                        } else {
                            scanParam.setTipInfo("ip范围段开始或结尾格式错误");
                            return scanParam;
                        }
//                        }
                    } else {
                        scanParam.setTipInfo("ip范围段开始或结尾不能为空");
                        return scanParam;
                    }
                }
            }

            assetsScanParam.getIpRange().forEach(value -> {
                if (StringUtils.isEmpty(value.getStartip().trim()) || StringUtils.isEmpty(value.getEndip().trim())) {
                    return;
                }

                IPRangeParam ipRangeParam = new IPRangeParam();
                BeanUtils.copyProperties(value, ipRangeParam);
                ipRangeParamList.add(ipRangeParam);
            });
        }

        if (ipRangeParamList.size() > 0) {
            scanParam.setIpranges(ipRangeParamList);
        }

        if (null != assetsScanParam.getIpsubnets() && assetsScanParam.getIpsubnets().size() > 0) {
            assetsScanParam.getIpsubnets().forEach(value -> {
                if (StringUtils.isEmpty(value.getSubnet())) {
                    return;
                }
                SubnetParam subnetParam = new SubnetParam();
                BeanUtils.copyProperties(value, subnetParam);
                subnetParamList.add(subnetParam);
            });
        }

        if (subnetParamList.size() > 0) {
            scanParam.setSubnets(subnetParamList);
        }


        List<AssetsScanRuleParam> list = assetsScanParam.getScanrules();
        List<RuleParam> ruleParamList = new ArrayList<RuleParam>();

        if (null != list && list.size() > 0) {
            list.forEach(value -> {
                RuleParam ruleParam = new RuleParam();
                RuleType rt = RuleType.valueOf("Port");
                if(value.getProtoType()!=null){
                    rt = RuleType.SNMP.equals(value.getProtoType())
                            ? RuleType.valueOf(value.getVersion()) : RuleType.valueOf(value.getProtoType());
                }
                ruleParam.setRuleType(rt.getName());
                switch (rt) {
                    case Port:
                    case ZabbixAgent:
                        ruleParam.setPort(value.getPort());
                        break;
                    case SNMPv1v2:
                        ruleParam.setCommunity(value.getCommunity());
                        ruleParam.setPort(value.getPort());
                        break;
                    case SNMPv3:
                        SecurityLevel sl = SecurityLevel.valueOf(value.getSecurityLevel());
                        ruleParam.setSecurityName(value.getSecurityName());
                        ruleParam.setContextName(value.getContextName());
                        ruleParam.setSecurityLevel(sl.name());
                        switch (sl) {
                            case noAuthNoPriv:
                                break;
                            case authNoPriv:
                                ruleParam.setAuthProtocol(value.getAuthProtocol());
                                ruleParam.setAuthToken(value.getAuthToken());
                                break;
                            case authPriv:
                                ruleParam.setAuthProtocol(value.getAuthProtocol());
                                ruleParam.setAuthToken(value.getAuthToken());
                                ruleParam.setPrivProtocol(value.getPrivProtocol());
                                ruleParam.setPrivToken(value.getPrivToken());
                                break;
                            default:
                        }
                    case ICMP:
                    default:
                }
                ruleParamList.add(ruleParam);
            });
        }
        scanParam.setRuleParams(ruleParamList);

        return scanParam;
    }


    public boolean isValidIpv4Addr(String ipAddr) {
        String regex = "(^((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|([0-9]){1,2})"
                + "([.](25[0-5]|2[0-4][0-9]|[0-1][0-9][0-9]|([0-9]){1,2})){3})$)";

        if (ipAddr == null) {
            log.info("ip address is null ");
            return false;
        }
        ipAddr = Normalizer.normalize(ipAddr, Normalizer.Form.NFKC);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ipAddr);
        boolean match = matcher.matches();
        return match;
    }

    private AddScanruleParam tranform(ScanParam scanParam) {
        AddScanruleParam addScanruleParam = new UpdateScanruleParam();
        String ruleName = scanParam.getRuleName();
        if (StringUtils.isEmpty(ruleName)) {
            LocalDateTime localDateTime = LocalDateTime.now();
            ruleName = localDateTime.toString();
        }
        addScanruleParam.setScanruleName(ruleName);
        addScanruleParam.setMonitorServerId(scanParam.getMonitorServerId());
        //转换ip范围
        List<MwIpRangDTO> mwIpRangDTOlist = new ArrayList<MwIpRangDTO>();
        List<IPRangeParam> ipRangeParamList = scanParam.getIpranges();
        if (null != ipRangeParamList) {
            ipRangeParamList.forEach(value -> {
                MwIpRangDTO mwIpRangDTO = new MwIpRangDTO();
                mwIpRangDTO.setIpRangStart(value.getStartip());
                mwIpRangDTO.setIpRangEnd(value.getEndip());
                mwIpRangDTO.setIpType(value.isIpv6checked());
                mwIpRangDTOlist.add(mwIpRangDTO);
            });
            addScanruleParam.setMwIpRangDTO(mwIpRangDTOlist);
        }

        //转换ip地址段
        List<MwIpAddressesDTO> ipAddressesDTOList = new ArrayList<MwIpAddressesDTO>();
        List<SubnetParam> subnets = scanParam.getSubnets();
        if (null != subnets) {
            subnets.forEach(value -> {
                MwIpAddressesDTO mwIpAddressesDTO = new MwIpAddressesDTO();
                mwIpAddressesDTO.setIpAddresses(value.getSubnet());
                mwIpAddressesDTO.setIpType(value.isIpv6checked());
                ipAddressesDTOList.add(mwIpAddressesDTO);
            });
            addScanruleParam.setIpAddressesDTO(ipAddressesDTOList);
        }

        //转换ip列表
        String ips = scanParam.getIps();
        List<MwIpAddressListDTO> ipAddressList = new ArrayList<MwIpAddressListDTO>();
        if (null != ips) {
            String[] ipArray = ips.split(ResovlerUtil.IP_SEPERATOR);
            for (String ip : ipArray) {
                MwIpAddressListDTO mwIpAddressListDTO = new MwIpAddressListDTO();
                mwIpAddressListDTO.setIpAddress(ip);
                mwIpAddressListDTO.setIpType(scanParam.isIpv6checked());
                ipAddressList.add(mwIpAddressListDTO);
            }
            addScanruleParam.setIpAddressListDTO(ipAddressList);
        }

        List<RuleParam> ruleParamList = scanParam.getRuleParams();
        List<MwRulesnmpv1DTO> rulesnmpv1DTOList = new ArrayList<MwRulesnmpv1DTO>();
        addScanruleParam.setRulesnmpv1DTOList(rulesnmpv1DTOList);

        List<MwRulesnmpDTO> rulesnmpDTOList = new ArrayList<MwRulesnmpDTO>();
        addScanruleParam.setRulesnmpDTOList(rulesnmpDTOList);

        List<MwAgentruleDTO> agentruleDTOList = new ArrayList<MwAgentruleDTO>();
        addScanruleParam.setAgentruleDTOList(agentruleDTOList);

        List<MwPortruleDTO> portruleDTOList = new ArrayList<MwPortruleDTO>();
        addScanruleParam.setPortruleDTOList(portruleDTOList);

        List<MwIcmpruleDTO> icmpruleDTOList = new ArrayList<MwIcmpruleDTO>();
        addScanruleParam.setIcmpruleDTOList(icmpruleDTOList);

        if (null != ruleParamList) {
            ruleParamList.forEach(value -> {
                String port;
                try {
                    RuleType rt = RuleType.valueOf(value.getRuleType());
                    switch (rt) {
                        case SNMPv1v2:
                            MwRulesnmpv1DTO mwRulesnmpv1DTO = new MwRulesnmpv1DTO();
                            port = (null == value.getPort() ? rt.getPort() : value.getPort());
                            mwRulesnmpv1DTO.setPort(Integer.parseInt(port));
                            mwRulesnmpv1DTO.setCommunity(value.getCommunity());
                            rulesnmpv1DTOList.add(mwRulesnmpv1DTO);
                            break;
                        case SNMPv3:
                            MwRulesnmpDTO mwRulesnmpDTO = new MwRulesnmpDTO();
                            port = (null == value.getPort() ? rt.getPort() : value.getPort());
                            mwRulesnmpDTO.setPort(Integer.parseInt(port));
                            mwRulesnmpDTO.setCommunity(value.getCommunity());
                            mwRulesnmpDTO.setSecName(value.getSecurityName());
                            mwRulesnmpDTO.setSecLevel(value.getSecurityLevel());
                            mwRulesnmpDTO.setContextName(value.getContextName());
                            mwRulesnmpDTO.setAuthAlg(value.getAuthProtocol());
                            mwRulesnmpDTO.setAuthValue(value.getAuthToken());
                            mwRulesnmpDTO.setPrivAlg(value.getPrivProtocol());
                            mwRulesnmpDTO.setPriValue(value.getPrivToken());
                            rulesnmpDTOList.add(mwRulesnmpDTO);
                            break;
                        case ZabbixAgent:
                            MwAgentruleDTO mwAgentruleDTO = new MwAgentruleDTO();
                            port = (null == value.getPort() ? rt.getPort() : value.getPort());
                            mwAgentruleDTO.setPort(Integer.parseInt(port));
                            agentruleDTOList.add(mwAgentruleDTO);
                            break;
                        case ICMP:
                            MwIcmpruleDTO mwIcmpruleDTO = new MwIcmpruleDTO();
                            port = (null == value.getPort() ? rt.getPort() : value.getPort());
                            mwIcmpruleDTO.setPort(Integer.parseInt(port));
                            icmpruleDTOList.add(mwIcmpruleDTO);
                            break;
                        case Port:
                            MwPortruleDTO mwPortruleDTO1 = new MwPortruleDTO();
                            mwPortruleDTO1.setProtocolType(rt.getName());
                            port = (null == value.getPort() ? rt.getPort() : value.getPort());
                            mwPortruleDTO1.setPort(Integer.parseInt(port));
                            portruleDTOList.add(mwPortruleDTO1);
                            break;
                        default:
                    }
                } catch (Exception e) {
                    log.error("tranform(ScanParam scanParam)", e);
                }
            });
        }
        return addScanruleParam;
    }

    @Autowired
    private MwTangibleAssetsService mwTangService;

    /**
     * 设置预添加资产信息
     * @param addUpdateTangAssetsParam
     */
    public void setPreAddAssets(AddUpdateTangAssetsParam addUpdateTangAssetsParam){
        preAddAssetsMap.put(addUpdateTangAssetsParam.getTaskId(),addUpdateTangAssetsParam);
    }

    /**
     * 队列单个资产扫描任务结束后，需要进行单个资产自动添加
     */
    private void autoMaticCreateAssets(AssetsScanTaskDto assetsScanTaskDto,Reply reply,List<MWUser> mwUsers){
        try {
            AddUpdateTangAssetsParam addUpdateTangAssetsParam = preAddAssetsMap.get(assetsScanTaskDto.getTaskId());
            preAddAssetsMap.remove(assetsScanTaskDto.getTaskId());
            if(addUpdateTangAssetsParam == null)return;
            if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS){
                mwMessageService.sendAssetsScanCompleteMessage("资产自动添加失败，扫描IP:"+assetsScanTaskDto.getScanContext().getScanParam().getIplist()+" 扫描失败信息:"+reply.getMsg(),mwUsers,"资产自动添加失败");
                return;
            }
            //取出扫描数据
            PageInfo pageInfo  = (PageInfo) reply.getData();
            //扫描成功的数据
            List<ScanResultSuccess> successes = pageInfo.getList();
            if(CollectionUtils.isEmpty(successes)){
                mwMessageService.sendAssetsScanCompleteMessage("资产自动添加失败，扫描IP:"+assetsScanTaskDto.getScanContext().getScanParam().getIplist()+" 扫描失败信息:"+reply.getMsg(),mwUsers,"资产自动添加失败");
                return;
            }
            //单个资产添加扫描只会存在一条数据，取第一条就可以
            ScanResultSuccess scanResultSuccess = successes.get(0);
            //组资产添加参数
            composeAssetsAddParam(addUpdateTangAssetsParam,scanResultSuccess,assetsScanTaskDto.getExecutionUser());
            //进行资产数据添加
            Reply result = mwTangService.insertAssets(addUpdateTangAssetsParam, false);
            if(result == null || result.getRes() != PaasConstant.RES_SUCCESS){
                mwMessageService.sendAssetsScanCompleteMessage("资产自动添加失败，添加IP:"+assetsScanTaskDto.getScanContext().getScanParam().getIplist()+":添加资产名称:"+addUpdateTangAssetsParam.getAssetsName()+":添加失败信息"+reply.getMsg(),mwUsers,"资产自动添加失败");
                return;
            }
            mwMessageService.sendAssetsScanCompleteMessage("资产自动添加成功，添加资产名称:"+addUpdateTangAssetsParam.getAssetsName()+": 添加IP:"+assetsScanTaskDto.getScanContext().getScanParam().getIplist(),mwUsers,"资产自动添加成功");
        }catch (Throwable e){
            log.error("资产自动添加失败,失败信息"+e.getMessage());
            mwMessageService.sendAssetsScanCompleteMessage("资产自动添加失败，添加IP:"+assetsScanTaskDto.getScanContext().getScanParam().getIplist()+":添加失败信息"+reply.getMsg(),mwUsers,"资产自动添加失败");
        }
    }

    /**
     * 组合资产添加参数
     * @param addUpdateTangAssetsParam
     * @param scanResultSuccess
     */
    private void composeAssetsAddParam(AddUpdateTangAssetsParam addUpdateTangAssetsParam,ScanResultSuccess scanResultSuccess,String userName){
        addUpdateTangAssetsParam.setAssetsName(scanResultSuccess.getHostName());
        addUpdateTangAssetsParam.setAssetsTypeId(scanResultSuccess.getAssetsTypeId());
        addUpdateTangAssetsParam.setAssetsTypeSubId(scanResultSuccess.getAssetsSubTypeId());
        addUpdateTangAssetsParam.setDescription(scanResultSuccess.getDescription());
        addUpdateTangAssetsParam.setHostGroupId(scanResultSuccess.getGroupId());
        addUpdateTangAssetsParam.setHostName(scanResultSuccess.getHostName());
        addUpdateTangAssetsParam.setInBandIp(scanResultSuccess.getIpAddress());
        addUpdateTangAssetsParam.setInterfacesType(0);
        addUpdateTangAssetsParam.setManufacturer(scanResultSuccess.getBrand());
        addUpdateTangAssetsParam.setMonitorPort(Integer.parseInt(scanResultSuccess.getPort()));
        addUpdateTangAssetsParam.setMonitorServerId(scanResultSuccess.getMonitorServerId());
        addUpdateTangAssetsParam.setScanSuccessId(scanResultSuccess.getId());
        addUpdateTangAssetsParam.setSnmpV1AssetsDTO(MwSnmpv1AssetsDTO.builder().community(scanResultSuccess.getCommunity()).port(Integer.parseInt(scanResultSuccess.getPort())).build());
        addUpdateTangAssetsParam.setSpecifications(scanResultSuccess.getSpecifications());
        addUpdateTangAssetsParam.setTemplateId(scanResultSuccess.getTemplateId());
        addUpdateTangAssetsParam.setCreator(userName);
        addUpdateTangAssetsParam.setCreateDate(new Date());
        addUpdateTangAssetsParam.setModifier(userName);
    }

    /**
     * 设置本地用户线程信息
     * @param assetsScanTaskDto
     */
    private void setUserNewsLocalThread(AssetsScanTaskDto assetsScanTaskDto){
        MwLoginUserDto userDto = new MwLoginUserDto();
        userDto.setDataPerm(assetsScanTaskDto.getDataPerm());
        userDto.setLoginName(assetsScanTaskDto.getLoginName());
        userDto.setRoleId(assetsScanTaskDto.getRoleId());
        userDto.setUserId(assetsScanTaskDto.getUserId());
        iLoginCacheInfo.createLocalTread(userDto);
    }
}
