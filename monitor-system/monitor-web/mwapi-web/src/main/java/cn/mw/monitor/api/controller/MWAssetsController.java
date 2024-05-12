package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.IpV6Util;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.api.controller.dto.MonitorDTO;
import cn.mw.monitor.api.dataview.AssetsDiscoverRuleView;
import cn.mw.monitor.api.dataview.AssetsResultFailView;
import cn.mw.monitor.api.dataview.AssetsResultSuccView;
import cn.mw.monitor.api.dataview.AssetsResultView;
import cn.mw.monitor.assets.api.param.assets.*;
import cn.mw.monitor.assets.service.MwIntangibleAssetsService;
import cn.mw.monitor.assetsSubType.model.MwAssetsGroupTable;
import cn.mw.monitor.assetsSubType.service.MwAssetsSubTypeService;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.engineManage.service.MwEngineManageService;
import cn.mw.monitor.model.service.MwModelManageService;
import cn.mw.monitor.scan.dataview.ScanProcView;
import cn.mw.monitor.scan.exception.ScanException;
import cn.mw.monitor.scanrule.api.param.scanrule.*;
import cn.mw.monitor.scanrule.dto.*;
import cn.mw.monitor.scanrule.model.ScanTaskExecuteStatus;
import cn.mw.monitor.scanrule.model.ScanTaskManage;
import cn.mw.monitor.scanrule.service.MwScanruleService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchAddAssetsEvent;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.service.assets.service.MwAssetsInterfaceService;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.model.service.MwModelAssetsByESService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.service.scan.model.ScanResultFail;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.model.SecurityLevel;
import cn.mw.monitor.service.scan.param.QueryScanResultParam;
import cn.mw.monitor.service.scan.param.RuleParam;
import cn.mw.monitor.service.user.dto.MwLoginUserDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.snmp.model.ExceuteInfo;
import cn.mw.monitor.snmp.param.scan.IPRangeParam;
import cn.mw.monitor.snmp.param.scan.InterruptParam;
import cn.mw.monitor.snmp.param.scan.ScanParam;
import cn.mw.monitor.snmp.param.scan.SubnetParam;
import cn.mw.monitor.snmp.service.IMonitor;
import cn.mw.monitor.snmp.service.MWScanService;
import cn.mw.monitor.snmp.service.scan.ScanManager;
import cn.mw.monitor.snmp.service.scan.ScanResult;
import cn.mw.monitor.snmp.service.scan.ScanResultType;
import cn.mw.monitor.snmp.utils.ProcesUtil;
import cn.mw.monitor.snmp.utils.ResovlerUtil;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.AssetsUtils;
import cn.mw.monitor.util.LicenseManagementEnum;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 资产api管理
 * @auth baochengbin
 * @desc
 * @date 2020/3/16
 */
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "资产列表接口列表")
public class MWAssetsController extends BaseApiService {
    private static final String BEAN_NAME = "MWAssetsController";
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwTangibleAssetsService mwTangService;

    @Autowired
    private LicenseManagementService licenseManagement;

    @Autowired
    private CheckCountService checkCountService;

    @Autowired
    private IMonitor monitor;

    @Autowired
    private MwIntangibleAssetsService mwIntangService;

    @Autowired
    private MWScanService mwScanService;

    @Autowired
    DataSource dataSource;

    @Autowired
    private MwScanruleService mwScanruleService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MwAssetsSubTypeService mwAssetsSubTypeService;

    @Autowired
    private MwAssetsInterfaceService mwAssetsInterfaceService;

    @Value("${asset.scan.sessionTime}")
    private int scanSessionTime;

    @Value("${scanqueue-enable}")
    private boolean scanqueueEnable;

    //启用模型资产管理
    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    //代理agent默认端口
    @Value("${asset.scan.proxy.port}")
    private Integer proxyPort;
    //资产推送是否开启
    @Value("${assets.push.enable}")
    private boolean assetsPush;

    @Autowired
    private MwModelManageService mwModelManageService;

    @Autowired
    private MwModelAssetsByESService mwModelAssetsByESService;

    @Autowired
    private ScanTaskManage scanTaskManage;

    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    /**
     * 资产扫描
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/scan/perform")
    @ResponseBody
    @ApiOperation(value = "资产扫描")
    public ResponseBase scan(@RequestBody AssetsScanParam assetsScanParam,
                             HttpServletRequest request, RedirectAttributesModelMap model) {
        String id = (String) SecurityUtils.getSubject().getSession().getId();
        log.info("scan session id:" + id + ";scanSessionTime:" + scanSessionTime);
        SecurityUtils.getSubject().getSession().setTimeout(scanSessionTime * 1000);
        ResponseBase responseBase = null;

        try {
            MWAssetsController controller = (MWAssetsController) SpringUtils.getBean(BEAN_NAME);
            if(assetsScanParam.getIsExecute()!=null && assetsScanParam.getIsExecute()){
                assetsScanParam.setExecuteNow(true);
            }
            if(!scanqueueEnable){
                responseBase = controller.scan(assetsScanParam, true, false);
            }else{
                //清空已完成的任务
                Queue<AssetsScanTaskRecord> queue = scanTaskManage.getQueue();
                if(CollectionUtils.isNotEmpty(queue)){
                    for (AssetsScanTaskRecord taskRecord : queue) {
                        if(taskRecord.getTaskStatus().equals(ScanTaskExecuteStatus.EXECUTE_COMPLETE.getCode())){
                            queue.remove(taskRecord);
                        }
                    }
                }
                if(CollectionUtils.isEmpty(queue)){
                    responseBase = controller.scan(assetsScanParam, true, false);
                }else{
                    //把扫描任务放入队列,kafka会监听队列,并执行任务
                    String taskId = createAssetsScanQueue(assetsScanParam, true, false, 1);
                    //获取扫描结果;
                    responseBase = getScanResult(taskId);
                }
            }
        } catch (ScanException scanException) {
            responseBase = scanException.getResponseBase();
        } catch (Exception e) {
            responseBase = setResultFail(e.getMessage(), assetsScanParam);
        }
        return responseBase;
    }

    /**
     * 修改资产扫描规则
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/scan/editor")
    @ResponseBody
    public ResponseBase scanEdit(@RequestBody AssetsScanParam assetsScanParam,
                                 HttpServletRequest request, RedirectAttributesModelMap model) {
        try {

            ScanParam scanParam = tranform(assetsScanParam);
            UpdateScanruleParam updateScanruleParam = (UpdateScanruleParam) tranform(scanParam);
            updateScanruleParam.setId(assetsScanParam.getRuleId());
            MWAssetsController controller = (MWAssetsController) SpringUtils.getBean(BEAN_NAME);
            ResponseBase responseBase = controller.scan(assetsScanParam, false, false);
            if (responseBase.getRtnCode() == 200) {
                updateScanruleParam.setEngineId(assetsScanParam.getEngineId());
                mwScanruleService.update(updateScanruleParam, true);
            }
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("资产扫描规则")
                    .objName(assetsScanParam.getName()).operateDes("修改资产扫描规则").build();
            logger.info(JSON.toJSONString(builder));
            return responseBase;
        } catch (Exception e) {
            log.error("scan edit param:" + assetsScanParam.toString(), e);
            return setResultFail("MWAssetsController{} scanEdit() error","");
        }
    }

    @Transactional
    public ResponseBase scan(AssetsScanParam assetsScanParam
            , boolean isSaveScanRuled, boolean isRescan) throws Exception {
        //格式校验和数据重组
        ScanParam scanParam = tranform(assetsScanParam);

        if (monitor.getThreads().size() > 0) {
            return setResultFail(ErrorConstant.SCANRULE_MSG_220108, assetsScanParam);
        }

        //如果返回错误信息，后面没必要执行了
        if (null != scanParam.getTipInfo()) {
            log.error("scan error param:" + scanParam.toString());
            return setResultFail(scanParam.getTipInfo(), scanParam);
        }

        //根据引擎id获取ip地址
        if(StringUtils.isNotEmpty(assetsScanParam.getEngineId()) && !MwEngineCommonsService.LOCALHOST_KEY.equals(assetsScanParam.getEngineId())){
            MwEngineManageDTO mwEngineManageDTO = mwEngineCommonsService.selectEngineByIdNoPerm(assetsScanParam.getEngineId());
            if(null != mwEngineManageDTO){
                scanParam.setMonitorServerId(mwEngineManageDTO.getMonitorServerId());
                List<ProxyInfo> proxyInfos = new ArrayList<>();
                ProxyInfo proxyInfo = new ProxyInfo(mwEngineManageDTO.getProxyAddress() ,proxyPort);
                proxyInfos.add(proxyInfo);
                scanParam.setProxyInfos(proxyInfos);
            }
        }

        ScanManager scanManager = SpringUtils.getBean(ScanManager.class);
        ScanResult scanResult = null;
        Reply ret = null;
        boolean hasError = false;

        Integer ruleId = 0;
        try {
            Date currentDate = new Date();

            Reply reply = null;
            //数据格式转换
            AddScanruleParam addScanruleParam = tranform(scanParam);
            if (isSaveScanRuled) {
                //保存扫描规则
                addScanruleParam.setCreateDate(currentDate);
                addScanruleParam.setScanStartDate(currentDate);
                addScanruleParam.setEngineId(assetsScanParam.getEngineId());

                //单个添加资产页面添加资产时,前端需要设置该参数,逻辑删除,保证该规则不可见
                Integer isdelete = assetsScanParam.getIsdelete();
                if (isdelete != null && isdelete == 1) {
                    addScanruleParam.setDeleteFlag(1);
                }
                log.info("mwScanruleService ScanStartDate:{}", df.format(currentDate));
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
                scanResult.setPollingEngine(assetsScanParam.getEngineId());
                synchronized (lockObject) {
                    lockObject.notify();
                }
            }

            if (null != scanResult) {
                //保存扫描结果
                //查询分组信息
                scanParam.setRuleId(addScanruleParam.getId());
                Reply stsReply = null;
                List<MwModelAssetsGroupTable> mwModelAssetsGroupTables = new ArrayList<>();
                //////////////////////
                //模型管理下的调用。
                if(assetsScanParam.getIsNewVersion()!=null && assetsScanParam.getIsNewVersion()){
                    ////  模型分组替代资产类型
                    stsReply = mwModelManageService.selectGroupServerMap(null);
                    mwModelAssetsGroupTables = (List<MwModelAssetsGroupTable>) stsReply.getData();
                }else{
                    stsReply = mwAssetsSubTypeService.selectGroupServerMapList();
                    //将原先的MwAssetsGroupTable 实体转为 MwModelAssetsGroupTable；
                    for(MwAssetsGroupTable table :  (List<MwAssetsGroupTable>)stsReply.getData()){
                        MwModelAssetsGroupTable mwModelAssetsGroupTable = new MwModelAssetsGroupTable();
                        BeanUtils.copyProperties(table,mwModelAssetsGroupTable);
                        mwModelAssetsGroupTables.add(mwModelAssetsGroupTable);
                    }
                }

                if (null != stsReply && stsReply.getRes() != PaasConstant.RES_SUCCESS) {
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

                reply = mwScanService.addScanResult(scanParam, scanResult, isRescan, groupMap,assetsScanParam.getIsNewVersion());

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
            throw new ScanException(setResultWarn(ErrorConstant.SCANRULE_MSG_220106));
        }
        return setResultSuccess(ret);
    }


    /**
     * 根据规则id扫描资产
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/scanbyid/perform")
    @ResponseBody
    public ResponseBase scan(@RequestBody AssetsScanIDParam assetsScanIDParam,
                             HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        MwScanruleDTO msDto = null;
        try {
            ProcesUtil.getInstance().setScanRuleId(assetsScanIDParam.getScanRuleId());
            //根据规则id获取规则信息
            reply = mwScanruleService.selectById(assetsScanIDParam.getScanRuleId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            msDto = (MwScanruleDTO) reply.getData();
            Date scanStartDate = new Date();
            UpdateScanruleParam updateScanruleParam = new UpdateScanruleParam();
            updateScanruleParam.setScanStartDate(scanStartDate);
            updateScanruleParam.setId(assetsScanIDParam.getScanRuleId());
            updateScanruleParam.setEngineId(msDto.getEngineId());
            mwScanruleService.update(updateScanruleParam, false);
            if (null != msDto) {
                //设置扫描ip范围, 扫描规则
                AssetsScanParam assetsScanParam = transform(msDto);
                assetsScanParam.setResulttype(assetsScanIDParam.getResulttype());
                if(assetsScanIDParam.getIsNewVersion()!=null && assetsScanIDParam.getIsNewVersion()){
                    assetsScanParam.setIsNewVersion(true);
                }
                MWAssetsController controller = (MWAssetsController) SpringUtils.getBean(BEAN_NAME);
                ResponseBase resp;
                if(!scanqueueEnable){
                    resp = controller.scan(assetsScanParam, false, false);
                }else{
                    String taskId = createAssetsScanQueue(assetsScanParam, false, false, 2);
                    //获取扫描结果
                    resp = getScanResult(taskId);
                }
                ProcesUtil.getInstance().setScanRuleId(0);
                return resp;
            }
        } catch (Exception e) {
            log.error("scanbyid", e);
            return setResultFail(ErrorConstant.SCANRULE_MSG_220107, assetsScanIDParam);
        }

        return setResultFail(ErrorConstant.SCANRULE_MSG_220107, assetsScanIDParam);
    }

    private AssetsScanParam transform(MwScanruleDTO msDto) {
        AssetsScanParam assetsScanParam = new AssetsScanParam();
        assetsScanParam.setRuleId(msDto.getScanruleId());
        assetsScanParam.setMonitorServerId(msDto.getMonitorServerId());
        assetsScanParam.setName(msDto.getScanruleName());
        assetsScanParam.setExecuteNow(true);
        assetsScanParam.setEngineId(msDto.getEngineId());

        List<MwIpRangDTO> ipRangDTOS = msDto.getIpRangDTO();
        if (null != ipRangDTOS && ipRangDTOS.size() > 0) {
            List<cn.mw.monitor.scanrule.api.param.scanrule.IPRangeParam> ipRangeParamList = new ArrayList<>();
            ipRangDTOS.forEach(value -> {
                cn.mw.monitor.scanrule.api.param.scanrule.IPRangeParam param = new cn.mw.monitor.scanrule.api.param.scanrule.IPRangeParam();
                param.setStartip(value.getIpRangStart());
                param.setEndip(value.getIpRangEnd());
                param.setIpv6checked(value.getIpType());
                ipRangeParamList.add(param);
            });
            assetsScanParam.setIpRange(ipRangeParamList);
        }

        List<MwIpAddressesDTO> ipsubnets = msDto.getIpAddressesDTO();
        if (null != ipsubnets && ipsubnets.size() > 0) {
            List<IPSubnetParam> ipSubnetParams = new ArrayList<>();
            ipsubnets.forEach(value -> {
                IPSubnetParam param = new IPSubnetParam();
                param.setSubnet(value.getIpAddresses());
                param.setIpv6checked(value.getIpType());
                ipSubnetParams.add(param);
            });
            assetsScanParam.setIpsubnets(ipSubnetParams);
        }

        List<MwIpAddressListDTO> ipAddressListDTO = msDto.getIpAddressListDTO();
        if (null != ipAddressListDTO && ipAddressListDTO.size() > 0) {
            assetsScanParam.setIpv6checked(ipAddressListDTO.get(0).getIpType());
            StringBuffer sb = new StringBuffer();
            ipAddressListDTO.forEach(value -> {
                if (null != value.getIpAddress()) {
                    sb.append(ResovlerUtil.IP_SEPERATOR).append(value.getIpAddress());
                }
            });
            if (sb.length() > ResovlerUtil.IP_SEPERATOR.length()) {
                assetsScanParam.setIplist(sb.toString().substring(ResovlerUtil.IP_SEPERATOR.length()));
            }
        }

        List<AssetsScanRuleParam> scanrules = new ArrayList<>();
        List<MwRulesnmpv1DTO> snmpv1v2 = msDto.getRulesnmpv1DTOs();
        if (null != snmpv1v2 && snmpv1v2.size() > 0) {
            snmpv1v2.forEach(value -> {
                AssetsScanRuleParam assetsScanRuleParam = new AssetsScanRuleParam();
                assetsScanRuleParam.setProtoType(RuleType.SNMP);
                assetsScanRuleParam.setVersion(RuleType.SNMPv1v2.getName());
                assetsScanRuleParam.setPort(value.getPort().toString());
                assetsScanRuleParam.setCommunity(value.getCommunity());
                scanrules.add(assetsScanRuleParam);
            });
        }

        List<MwRulesnmpDTO> snmpv3 = msDto.getRulesnmpDTOs();
        if (null != snmpv3 && snmpv3.size() > 0) {
            snmpv3.forEach(value -> {
                AssetsScanRuleParam assetsScanRuleParam = new AssetsScanRuleParam();
                assetsScanRuleParam.setProtoType("SNMP");
                assetsScanRuleParam.setVersion("SNMPv3");
                assetsScanRuleParam.setSecurityName(value.getSecName());
                assetsScanRuleParam.setContextName(value.getContextName());
                assetsScanRuleParam.setSecurityLevel(value.getSecLevel());
                assetsScanRuleParam.setAuthProtocol(value.getAuthAlg());
                assetsScanRuleParam.setAuthToken(value.getAuthValue());
                assetsScanRuleParam.setPrivProtocol(value.getPrivAlg());
                assetsScanRuleParam.setPrivToken(value.getPriValue());
                scanrules.add(assetsScanRuleParam);
            });
        }

        List<MwAgentruleDTO> agentruleDTOs = msDto.getAgentruleDTOs();
        if (null != agentruleDTOs && agentruleDTOs.size() > 0) {
            agentruleDTOs.forEach(value -> {
                AssetsScanRuleParam assetsScanRuleParam = new AssetsScanRuleParam();
                assetsScanRuleParam.setProtoType(RuleType.ZabbixAgent.getName());
                assetsScanRuleParam.setPort(value.getPort().toString());
                scanrules.add(assetsScanRuleParam);
            });
        }

        List<MwPortruleDTO> portruleDTOs = msDto.getPortruleDTOs();
        if (null != portruleDTOs && portruleDTOs.size() > 0) {
            portruleDTOs.forEach(value -> {
                AssetsScanRuleParam assetsScanRuleParam = new AssetsScanRuleParam();
                assetsScanRuleParam.setVersion(RuleType.Port.getName());
                if(null != value.getPort()){
                    assetsScanRuleParam.setPort(value.getPort().toString());
                }
                scanrules.add(assetsScanRuleParam);
            });
        }

        List<MwIcmpruleDTO> icmpruleDTOs = msDto.getIcmpruleDTOList();
        if (null != icmpruleDTOs && icmpruleDTOs.size() > 0) {
            icmpruleDTOs.forEach(value -> {
                AssetsScanRuleParam assetsScanRuleParam = new AssetsScanRuleParam();
                assetsScanRuleParam.setPort(value.getPort().toString());
                assetsScanRuleParam.setProtoType(RuleType.ICMP.getName());
                scanrules.add(assetsScanRuleParam);
            });
        }

        assetsScanParam.setScanrules(scanrules);
        return assetsScanParam;
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

    //当用户从资产发现列表中, 查看已经发现的主机
    //此时页面中显示成功结果和失败结果
    //如果在已有结果中点击立即执行,对未添加和扫描失败的主机重新扫描
    private Reply reScanResultSearch(QueryScanResultParam queryScanResultParam) throws Exception {
        //搜索找出未添加资产或扫描失败的主机
        queryScanResultParam.setPageSize(Integer.MAX_VALUE);
        Reply reply = mwScanService.scanResultSearch(queryScanResultParam);
        StringBuffer sbIps = new StringBuffer();
        if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
            return reply;
        }
        String resultType = queryScanResultParam.getResulttype();
        PageInfo pageInfo = (PageInfo) reply.getData();

        List<Integer> rescanIds = new ArrayList<Integer>();
        if (null != resultType) {
            if (resultType.equals(ScanResultType.SUCCESS.name())) {
                List<ScanResultSuccess> list = (List<ScanResultSuccess>) pageInfo.getList();
                if (null != list && list.size() > 0) {
                    list.forEach(value -> {
                        if (null != value && value.getScanSuccessIdInAssets() <= 0) {
                            sbIps.append(value.getIpAddress()).append(ResovlerUtil.IP_SEPERATOR);
                            rescanIds.add(value.getId());
                        }
                    });
                }
            }

            if (resultType.equals(ScanResultType.ERROR.name())) {
                List<ScanResultFail> list = (List<ScanResultFail>) pageInfo.getList();
                if (null != list && list.size() > 0) {
                    list.forEach(value -> {
                        sbIps.append(value.getIpAddress()).append(ResovlerUtil.IP_SEPERATOR);
                        rescanIds.add(value.getId());
                    });
                }
            }
        }

        //重置需要扫描ip, 重新扫描
        MwScanruleDTO msDto = null;
        try {
            reply = mwScanruleService.selectById(queryScanResultParam.getScanruleId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return reply;
            }
            msDto = (MwScanruleDTO) reply.getData();
        } catch (Exception e) {
            log.error("reScanResultSearch" ,e);
            return reply;
        }

        if (null != msDto) {
            AssetsScanParam assetsScanParam = transform(msDto);
            assetsScanParam.setResulttype(queryScanResultParam.getResulttype());
            assetsScanParam.setExecuteNow(true);
            assetsScanParam.setIpsubnets(null);
            assetsScanParam.setIpRange(null);
            assetsScanParam.setIplist(sbIps.toString());
            assetsScanParam.setIpv6checked(false);//先固定写死为ipv4
            assetsScanParam.setRescanIds(rescanIds);
            MWAssetsController controller = (MWAssetsController) SpringUtils.getBean(BEAN_NAME);
            ResponseBase resp = controller.scan(assetsScanParam, false, true);
        }

        return reply;
    }

    /**
     * 扫描结果列表
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/scan/result/browse")
    @ResponseBody
    public ResponseBase scanResultSearch(@RequestBody QueryScanResultParam queryScanResultParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (queryScanResultParam.isReScanResult()) {
                reScanResultSearch(queryScanResultParam);
            }

            if(queryScanResultParam.getIsNewVersion()!=null && queryScanResultParam.getIsNewVersion()){
                reply = mwModelAssetsByESService.scanResultSearch(queryScanResultParam);
            }else{
                reply = mwScanService.scanResultSearch(queryScanResultParam);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

            PageInfo pageInfo = (PageInfo) reply.getData();
            String resultType = queryScanResultParam.getResulttype();
            List<AssetsResultView> listview = new ArrayList<AssetsResultView>();
            List<AssetsResultSuccView> succListView = new ArrayList<AssetsResultSuccView>();
            List<AssetsResultFailView> failListView = new ArrayList<AssetsResultFailView>();

            if (null != resultType) {
                if (resultType.equals(ScanResultType.SUCCESS.name())) {
                    List<ScanResultSuccess> list = (List<ScanResultSuccess>) pageInfo.getList();
                    if (null != list && list.size() > 0) {
                        list.forEach(value -> {
                            AssetsResultSuccView view = new AssetsResultSuccView();
                            view.init(value);
                            listview.add(view);
                            succListView.add(view);
                        });
                    }
                }

                if (resultType.equals(ScanResultType.ERROR.name())) {
                    List<ScanResultFail> list = (List<ScanResultFail>) pageInfo.getList();
                    if (null != list && list.size() > 0) {
                        list.forEach(value -> {
                            AssetsResultFailView view = new AssetsResultFailView();
                            view.init(value);
                            listview.add(view);
                            failListView.add(view);
                        });
                    }
                }
            }

            int id = (queryScanResultParam.getPageNumber() - 1) * queryScanResultParam.getPageSize();
            if (listview.size() > 0) {
                for (AssetsResultView view : listview) {
                    id++;
                    view.setId(id);
                }
            }

            JSONObject result = new JSONObject();
            result.put("code", 0);
            result.put("msg", "");
            result.put("totalResult", pageInfo.getTotal());
            if (succListView.size() > 0) {
                result.put("data", succListView);
            } else if (failListView.size() > 0) {
                result.put("data", failListView);
            }

            return setResultSuccess(result);
        } catch (ScanException e) {
            log.error("error{}", e);
            return setResultFail(e.getResponseBase().getData()!=null?e.getResponseBase().getData().toString():"扫描失败", queryScanResultParam);
        } catch (Exception e) {
            log.error("error{}", e);
            return setResultFail("MWAssetsController{} scanEdit() error",queryScanResultParam);
        }
    }


    /**
     * 资产发现列表
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/discovery-rule/browse")
    @ResponseBody
    public ResponseBase assetsDiscoverySearch(@RequestBody QueryScanruleParam queryScanruleParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwScanruleService.selectList(queryScanruleParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

            PageInfo pageInfo = (PageInfo) reply.getData();

            List<MwScanruleDTO> mwScanruleDTOS = (List<MwScanruleDTO>) pageInfo.getList();
            List<AssetsDiscoverRuleView> adrvlist = new ArrayList<AssetsDiscoverRuleView>();
            if (null != mwScanruleDTOS) {
                mwScanruleDTOS.forEach(value -> {
                    AssetsDiscoverRuleView adrv = new AssetsDiscoverRuleView();
                    adrv.init(value);
                    adrvlist.add(adrv);
                });
            }
            pageInfo.setList(adrvlist);
            JSONObject result = new JSONObject();
            result.put("code", 0);
            result.put("msg", "");
            result.put("count", pageInfo.getTotal());
            result.put("data", pageInfo.getList());
            return setResultSuccess(result);
        } catch (Exception e) {
            log.error("assetsDiscoverySearch" ,e);
        }
        return setResultFail(ErrorConstant.SCANRULE_MSG_220101, queryScanruleParam);
    }

    /**
     * 根据规则id查看扫描进度
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/scan/procedure/browse")
    @ResponseBody
    public ResponseBase scanProcedureBrowse(@RequestBody AssetsScanIDParam assetsScanIDParam
            , HttpServletRequest request, RedirectAttributesModelMap model) {
        if (null == assetsScanIDParam.getScanRuleId()&& ProcesUtil.getInstance().getScanRuleId()==0) {
            return setResultFail(ErrorConstant.COMMON_MSG_200003, assetsScanIDParam);
        }
        assetsScanIDParam.setScanRuleId(ProcesUtil.getInstance().getScanRuleId());
        ScanProcView scanProcView = new ScanProcView();
        try {
            ExceuteInfo ei = monitor.getExceuteInfoById(assetsScanIDParam.getScanRuleId());
            Map<Integer, Thread> threads = monitor.getThreads();
            threads.forEach((key, value) -> {
                log.info("scan theads ruleId:" + key + ";thread:" + value.getName());
            });
            if (null != ei) {
                scanProcView.setExceuteInfo(ei);
            } else {
                scanProcView.setProcessCount(100);
                scanProcView.setFinish(true);
            }
            log.info(scanProcView.toString());
        } catch (Exception e) {
            log.error("scanProcedureBrowse:", e);
        }
        return setResultSuccess(scanProcView);
    }

    /**
     * 扫描线程查看
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/thread/browse")
    @ResponseBody
    public ResponseBase threadBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        List<MonitorDTO> executeList = new ArrayList<MonitorDTO>();
        Map<Integer, Thread> threads = monitor.getThreads();
        threads.forEach((ruleId, thread) -> {
            MonitorDTO monitorDTO = new MonitorDTO();
            ExceuteInfo ei = monitor.getExceuteInfo(thread.getName());
            monitorDTO.setRuleId(ruleId);
            monitorDTO.setThreadName(thread.getName());
            monitorDTO.setAllCount(ei.getAllCount());
            monitorDTO.setSuccessCount(ei.getSuccessCount().get());
            monitorDTO.setErrorCount(ei.getErrorCount().get());
            executeList.add(monitorDTO);
        });
        return setResultSuccess(executeList);
    }

    /**
     * 线程中断
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/thread/interrupt/perform")
    @ResponseBody
    public ResponseBase threadInterrupt(@RequestBody InterruptParam interruptParam, HttpServletRequest request, RedirectAttributesModelMap model) {

        log.info("interrupt ruleId:" + interruptParam.getScanRuleId());
        Map<Integer, Thread> threads = monitor.getThreads();
        if (null == threads || null == interruptParam.getScanRuleId()) {
            return setResultSuccess();
        }
        Thread thread = threads.get(interruptParam.getScanRuleId());
        if (null != thread) {
            log.info("interrupt thread:" + thread.getName());
            ExceuteInfo ei = monitor.getExceuteInfo(thread.getName());
            Object lockObject = ei.getLockObject();

            thread.interrupt();
            try {
                synchronized (lockObject) {
                    lockObject.wait(30000);
                }
                UpdateScanruleParam updateScanruleParam = new UpdateScanruleParam();
                updateScanruleParam.setId(interruptParam.getScanRuleId());
                updateScanruleParam.setScanEndDate(ei.getEndTime());
                mwScanruleService.update(updateScanruleParam, false);
                monitor.clean(interruptParam.getScanRuleId());
            } catch (Exception e) {
                log.error("threadInterrupt", e);
            }
        }
        return setResultSuccess();
    }


    /**
     * 非扫描资产查询模板接口
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/template/getList")
    @ResponseBody
    public ResponseBase getTemplateList(@RequestBody AddUpdateTangAssetsParam addUpdateTangAssetsParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTangService.getTemplateList(addUpdateTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTemplateList" ,e);
            return setResultFail("MWAssetsController{} getTemplateList() error",addUpdateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 非扫描资产查询模板接口，根据监控方式
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/templateByMode/getList")
    @ResponseBody
    public ResponseBase getTemplateByModeList(@RequestBody AddUpdateTangAssetsParam addUpdateTangAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTangService.getTemplateListByMode(addUpdateTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.TANGASSETS_MSG_210120, reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTemplateByModeList" ,e);
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210120, addUpdateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 非扫描资产查询模板接口，根据所选模板获取宏值
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/macros/getList")
    @ResponseBody
    public ResponseBase getMacrosList(@RequestBody AddUpdateTangAssetsParam param,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTangService.getTemplateMacrosByTemplateId(param.getMonitorServerId(), param.getTemplateId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.TANGASSETS_MSG_210119, reply.getData());
            }
        } catch (Throwable e) {
            log.error("getMacrosList" ,e);
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210119, param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产新增
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/create")
    @ResponseBody
    public ResponseBase addTangAssets(@RequestBody AddUpdateTangAssetsParam addUpdateTangAssetsParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;

        //设置为模型管理模式,则不能通过资产管理添加
        if(modelAssetEnable){
            return setResultFail(ErrorConstant.MODEL_INSTANCE_MSG_313009,addUpdateTangAssetsParam);
        }

        try {
            //判断是否是预添加资产，如果是，走队列添加
            if(addUpdateTangAssetsParam.getIspreAddAssets() != null && addUpdateTangAssetsParam.getIspreAddAssets()){
                scanTaskManage.setPreAddAssets(addUpdateTangAssetsParam);
                return setResultSuccess("正在等待资产自动添加，请稍后");
            }
            //除了Snmp资产，其他监控方式资产启动配置为false
            //snmpv1v2和snmpv3的monitorMode相同,只用了snmpv1v2来判断
            if(addUpdateTangAssetsParam.getMonitorMode() != null
            && addUpdateTangAssetsParam.getMonitorMode().intValue() != RuleType.SNMPv1v2.getMonitorMode()){
                addUpdateTangAssetsParam.setSettingFlag(false);
            }
            //许可校验
            Integer typeId = addUpdateTangAssetsParam.getAssetsTypeId();
            Integer monitorMode = addUpdateTangAssetsParam.getMonitorMode();
            List<Integer> assetTypeIds = new ArrayList<>();
            int lCount = 0;
            if (typeId != null) {
                List<Integer> netAssetsList = LicenseManagementEnum.ASSETS_MANAGE_NET.getTypeId();
                if (netAssetsList.contains(typeId)) {
                    assetTypeIds = netAssetsList;
                    lCount = checkCountService.selectTableCount("mw_network_link", true);
                }
                List<Integer> serverAssetsList = LicenseManagementEnum.ASSETS_MANAGE_SERVER.getTypeId();
                if (serverAssetsList.contains(typeId)) {
                    assetTypeIds = serverAssetsList;
                }
                List<Integer> storgeAssetsList = LicenseManagementEnum.ASSETS_MANAGE_STORAGE.getTypeId();
                if (storgeAssetsList.contains(typeId)) {
                    assetTypeIds = storgeAssetsList;
                }
            }

            //数量获取
            int aCount = checkCountService.selectAssetsCount(assetTypeIds, assetTypeIds);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengtAssetsByMonitorMode(typeId, monitorMode, aCount + lCount, 1);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("有形资产")
                    .objName(addUpdateTangAssetsParam.getAssetsName()).operateDes("有形资产新增").build();
            logger.info(JSON.toJSONString(builder));
            reply = mwTangService.insertAssets(addUpdateTangAssetsParam, false);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

            if(mwScanService instanceof ListenerService){
                AddTangibleassetsEvent event = AddTangibleassetsEvent.builder().addTangAssetsParam(addUpdateTangAssetsParam).build();
                ((ListenerService)mwScanService).publishFinishEvent(event);
            }
        } catch (Throwable e) {
            log.error("addTangAssets" ,e);
            return setResultFail("MWAssetsController{} addTangAssets() error",addUpdateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产批量新增
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/batch/create")
    @ResponseBody
    public ResponseBase batchAddTangAssets(@RequestBody BatchAddTangAssetsParam batchAddTangAssetsParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;

        //设置为模型管理模式,则不能通过资产管理添加
        if(modelAssetEnable){
            return setResultFail(ErrorConstant.MODEL_INSTANCE_MSG_313009,batchAddTangAssetsParam);
        }

        try {
            reply = mwScanService.batchInsertAssets(batchAddTangAssetsParam);
            if (null != reply) {
                if (reply.getRes() == PaasConstant.RES_WARN) {
                    return setResult(300, reply.getMsg(), null);
                }
                if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                    return setResultFail(reply.getMsg(), reply.getData());
                }

                if(mwScanService instanceof ListenerService){
                    BatchAddAssetsEvent batchAddAssetsEvent = (BatchAddAssetsEvent)reply.getData();
                    ((ListenerService)mwScanService).publishFinishEvent(batchAddAssetsEvent);

                    //覆盖原有的reply
                    reply = Reply.ok();
                }
            }
        } catch (Throwable e) {
            log.error("batchAddTangAssets" ,e);
            return setResultFail("MWAssetsController{} batchAddTangAssets() error",batchAddTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 无形资产新增
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/create")
    @ResponseBody
    public ResponseBase addIntangAssets(@RequestBody AddUpdateIntangAssetsParam aParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            //许可校验
            //数量获取
            int count = checkCountService.selectTableCount("mw_intangibleassets_table", true);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengt("assets_manage", count, 1);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            // 验证内容正确性
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("无形资产")
                    .objName(aParam.getAssetsName()).operateDes("无形资产新增").build();
            logger.info(JSON.toJSONString(builder));
            reply = mwIntangService.insert(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addIntangAssets" ,e);
            return setResultFail("MWAssetsController{} addIntangAssets() error",aParam);
        }

        return setResultSuccess(reply);
    }


    /**
     * 有形资产批量修改
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/editor")
    @ResponseBody
    public ResponseBase updateBatchTangAssets(@RequestBody UpdateTangAssetsParam updateTangAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            if(StringUtils.isNotBlank(updateTangAssetsParam.getAssetsName())){
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("资产列表")
                        .objName(updateTangAssetsParam.getAssetsName()).operateDes("资产修改").build();
                logger.info(JSON.toJSONString(builder));
            }
            // 验证内容正确性
            reply = mwTangService.updateAssets(updateTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateBatchTangAssets" ,e);
            return setResultFail("MWAssetsController{} updateBatchTangAssets() error",updateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 无形资产修改
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/editor")
    @ResponseBody
    public ResponseBase updateIntangAssets(@RequestBody AddUpdateIntangAssetsParam updateintangAssetsParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("无形资产")
                    .objName(updateintangAssetsParam.getAssetsName()).operateDes("无形资产修改").build();
            logger.info(JSON.toJSONString(builder));
            reply = mwIntangService.update(updateintangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateIntangAssets" ,e);
            return setResultFail("MWAssetsController{} updateIntangAssets() error","");
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产删除
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/delete")
    @ResponseBody
    public ResponseBase deleteTangAssets(@RequestBody DeleteTangAssetsParam deleteTangAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            List<DeleteTangAssetsID> idList = deleteTangAssetsParam.getIdList();
            //校验许可数量
            int lCount = checkCountService.selectTableCount("mw_network_link", true);
            for (DeleteTangAssetsID assetsID : idList) {
                Integer typeId = assetsID.getAssetsTypeId();
                Integer monitorMode = assetsID.getMonitorMode();
                List<Integer> assetTypeIds = new ArrayList<>();
                if (typeId != null) {
                    List<Integer> netAssetsList = LicenseManagementEnum.ASSETS_MANAGE_NET.getTypeId();
                    if (netAssetsList.contains(typeId)) {
                        assetTypeIds = netAssetsList;
                    }
                    List<Integer> serverAssetsList = LicenseManagementEnum.ASSETS_MANAGE_SERVER.getTypeId();
                    if (serverAssetsList.contains(typeId)) {
                        assetTypeIds = serverAssetsList;
                        lCount = 0;
                    }
                    List<Integer> storgeAssetsList = LicenseManagementEnum.ASSETS_MANAGE_STORAGE.getTypeId();
                    if (storgeAssetsList.contains(typeId)) {
                        assetTypeIds = storgeAssetsList;
                        lCount = 0;
                    }
                }
                //数量获取
                int aCount = checkCountService.selectAssetsCount(assetTypeIds, assetTypeIds);
                ResponseBase responseBase = licenseManagement.getLicenseManagemengtAssetsByMonitorMode(typeId, monitorMode, aCount + lCount, -1);
                if (responseBase.getRtnCode() != 200) {
                    return  setResultFail(responseBase.getMsg(), responseBase.getData());
                }
            }
            // 验证内容正确性
            reply = mwTangService.deleteAssets(idList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteTangAssets", e);
            return setResultFail("MWAssetsController{} deleteTangAssets() error","");
        }

        return setResultSuccess(reply);
    }

    /**
     * 无形资产删除
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/delete")
    @ResponseBody
    public ResponseBase deleteIntangAssets(@RequestBody DeleteInTangAssetsParam param,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            List<String> idList = param.getIdList();
            // 验证内容正确性
            reply = mwIntangService.delete(idList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteIntangAssets", e);
            return setResultFail("MWAssetsController{} deleteIntangAssets() error","");
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产下拉查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/dropdown")
    @ResponseBody
    public ResponseBase dropdownTangAssets(@RequestBody QueryTangAssetsParam browseTangAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model){
        ResponseBase response = browseTangAssets(browseTangAssetsParam ,request ,model);
        Reply reply = (Reply)response.getData();
        PageInfo pageInfo = (PageInfo)reply.getData();
        //转换显示
        List<MwTangibleassetsTableView> viewList = pageInfo.getList();
        List<MwTopoAssetsDropDown> list = new ArrayList<>();
        try{
            for(MwTangibleassetsTableView view : viewList){
                MwTopoAssetsDropDown dropDown = new MwTopoAssetsDropDown();
                dropDown.extractFrom(view);
                list.add(dropDown);
            }
            pageInfo.setList(list);
        }catch (Exception e){
            log.error("dropdownTangAssets" ,e);
            return setResultFail("查询异常", browseTangAssetsParam);
        }

        return setResultSuccess(reply);

    }

    /**
     * 有形资产查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/browse")
    @ResponseBody
    public ResponseBase browseTangAssets(@RequestBody QueryTangAssetsParam browseTangAssetsParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.selectList(browseTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

            //转换显示
            List<MwTangibleassetsTableView> viewList = new ArrayList<>();
            PageInfo pageInfo = (PageInfo)reply.getData();
            for(MwTangibleassetsTable mwTangibleassetsTable : (List<MwTangibleassetsTable>)pageInfo.getList()){
                MwTangibleassetsTableView mwTangibleassetsTableView = new MwTangibleassetsTableView();
                CopyUtils.copyObj(mwTangibleassetsTable ,mwTangibleassetsTableView);
                if(StringUtils.isNotBlank(mwTangibleassetsTableView.getHostName())){
                    String hostName = mwTangibleassetsTableView.getHostName()
                            .replaceAll("\\r\\n|\\n|\\r"," ");
                    mwTangibleassetsTableView.setHostName(hostName);
                }
                if(StringUtils.isNotBlank(mwTangibleassetsTableView.getDescription())){
                    String desc = mwTangibleassetsTableView.getDescription()
                            .replaceAll("\\r\\n|\\n|\\r"," ");

                    mwTangibleassetsTableView.setDescription(desc);
                }
                viewList.add(mwTangibleassetsTableView);
            }
            pageInfo.setList(viewList);
        } catch (Throwable e) {
            log.error("browseTangAssets" ,e);
            return setResultFail("MWAssetsController{} browseTangAssets() error",browseTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据资产类型查询所有标签
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/type/label/browse")
    @ResponseBody
    public ResponseBase browseAssetsTypeLabel(@RequestBody QueryTangAssetsParam browseTangAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.selectAllLabel(browseTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseAssetsTypeLabel" ,e);
            return setResultFail("MWAssetsController{} browseAssetsTypeLabel() error",browseTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 无形资产查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/browse")
    @ResponseBody
    public ResponseBase browseIntangAssets(@RequestBody QueryIntangAssetsParam browseTangAssetsParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIntangService.selectList(browseTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseIntangAssets" ,e);
            return setResultFail("MWAssetsController{} browseIntangAssets() error",browseTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 无形资产查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/popup/browse")
    @ResponseBody
    public ResponseBase browseIntangPopupAssets(@RequestBody QueryIntangAssetsParam browseTangAssetsParam,
                                                HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIntangService.selectById(browseTangAssetsParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseIntangPopupAssets" ,e);
            return setResultFail("MWAssetsController{} browseIntangPopupAssets() error",browseTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/popup/browse")
    @ResponseBody
    public ResponseBase browseTangPopupAssets(@RequestBody QueryTangAssetsParam browseTangAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.selectById(browseTangAssetsParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseTangPopupAssets" ,e);
            return setResultFail("MWAssetsController{} browseTangPopupAssets() error",browseTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产树状结构查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/treeList")
    @ResponseBody
    public ResponseBase getAssetsTypeTree(@RequestBody QueryAssetsTypeParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.getAssetsTypesTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAssetsTypeTree" ,e);
            return setResultFail("MWAssetsController{} getAssetsTypeTree() error","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 有形资产状态修改
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/perform")
    @ResponseBody
    public ResponseBase updateTangibleState(@RequestBody UpdateAssetsStateParam updateAssetsStateParam,
                                            HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            if(updateAssetsStateParam.getHostNames()!=null &&updateAssetsStateParam.getHostNames().size()>0 ){
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("资产")
                        .objName(updateAssetsStateParam.getHostNames().get(0)).operateDes("资产状态修改").build();

                logger.info(JSON.toJSONString(builder));
            }
            reply = mwTangService.updateState(updateAssetsStateParam);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.TANGASSETS_MSG_210118, reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateTangibleState" ,e);
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210118, updateAssetsStateParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 无形资产状态修改
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/perform")
    @ResponseBody
    public ResponseBase updateIntangibleStatue(@RequestBody UpdateAssetsStateParam updateAssetsStateParam,
                                               HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("无形资产")
                    .operateDes("无形资产状态修改").build();
            logger.info(JSON.toJSONString(builder));
            reply = mwIntangService.updateState(updateAssetsStateParam);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateIntangibleStatue" ,e);
            return setResultFail("MWAssetsController{} updateIntangibleStatue() error",updateAssetsStateParam);
        }

        return setResultSuccess(reply);
    }

//    //下拉框
//    @PostMapping("/assets/tangible/getAssetsDropdown")
//    @ResponseBody
//    public ResponseBase getAssetsDropdown(HttpServletRequest request, RedirectAttributesModelMap model) {
//
//        Reply reply;
//        try {
//            reply = mwIntangService.getDropdown();
//            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        } catch (Throwable e) {
//            log.error(e.getMessage());
//            return setResultFail(e.getMessage(), "");
//        }
//        return setResultSuccess(reply);
//    }

    //查看标签
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/getLabels")
    @ResponseBody
    public ResponseBase getLabels(@RequestBody QueryTangAssetsParam qparam, HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            List<MwAssetsLabelDTO> lists = mwLabelCommonServcie.getLabelBoard(qparam.getId(), qparam.getAssetsType());
            reply = Reply.ok(lists);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getLabels" ,e);
            return setResultFail("MWAssetsController{} getLabels() error","");
        }
        return setResultSuccess(reply);
    }

    //更新资产表中的templateId
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/updateTemplateIds")
    @ResponseBody
    public ResponseBase updateTemplateIds(HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            reply = mwTangService.updateAssetsTemplateIds();
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateTemplateIds" ,e);
            return setResultFail("MWAssetsController{} updateTemplateIds() error","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 无形资产查看标签
     *
     * @param qparam
     * @param request
     * @param model
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/intangible/getLabels")
    @ResponseBody
    public ResponseBase getIntangibleLabels(@RequestBody QueryTangAssetsParam qparam, HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
            List<MwAssetsLabelDTO> lists = mwLabelCommonServcie.getLabelBoard(qparam.getId(), DataType.INASSETS.getName());
            reply = Reply.ok(lists);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getIntangibleLabels" ,e);
            return setResultFail("MWAssetsController{} getIntangibleLabels() error","");
        }
        return setResultSuccess(reply);
    }

    /**
     * Ipv4地址分为ABCDE五大类，其中ABC类是普通ip地址，D类是组播地址，E类保留，作为研究之用。
     * A： 1.0.0.1 一127.255.255.255
     * 内网地址范围：10.0.0.0 一一10-255.255.255
     * B： 128.0.0.1 —191.255.255.255
     * 内网地址范围：172.16.0.0——172.31.255.255
     * C： 192.0.0.1 —223.255.255.255
     * 内网地址范围：192.168.0.0—一192.168.255.255
     * D： 224.0.0.1 —239.255.255.255
     * E： 240.0.0.1 —255.255.255.255
     * 我们的正则要求ip必须是ABC类地址。
     *
     * @param ipAddr
     * @return
     */
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

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/searchTerm/browse")
    @ResponseBody
    public ResponseBase browseAssetsPartAllData() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.selectAssetsSearchTermData();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseAssetsPartAllData" ,e);
            return setResultFail("MWAssetsController{} browseAssetsPartAllData() error","");
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/fuzzSeachAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSeachAllFiledData(@RequestBody AssetsSearchTermFuzzyParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.fuzzSeachAllFiledData(param.getValue(), param.isAssetsIOTFlag());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("fuzzSeachAllFiledData" ,e);
            return setResultFail("MWAssetsController{} fuzzSeachAllFiledData() error","");
        }

        return setResultSuccess(reply);
    }


    /**
     * 有形资产批量修改获取标签数据
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/batchEditor/label/browse")
    @ResponseBody
    public ResponseBase batchEditTangAssetsGetLabel(@RequestBody UpdateTangAssetsParam updateTangAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.batchEditAssetsGetLabel(updateTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("batchEditTangAssetsGetLabel" ,e);
            return setResultFail("MWAssetsController{} batchEditTangAssetsGetLabel() error",updateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 有形资产批量修改删除标签数据
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/tangible/batchEditor/label/delete")
    @ResponseBody
    public ResponseBase batchEditTangAssetsDeleteLabel(@RequestBody List<MwAssetsLabelDTO> labelDTO,
                                                    HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.batchEditAssetsDeleteLabel(labelDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("batchEditTangAssetsDeleteLabel" ,e);
            return setResultFail("MWAssetsController{} batchEditTangAssetsDeleteLabel() error",labelDTO);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/scanResult/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase scanResultfuzzSearch(@RequestBody QueryScanResultParam queryScanResultParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwScanService.fuzzSearchAllFiledData(queryScanResultParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("scanResultfuzzSearch" ,e);
            return setResultFail("MWAssetsController{} scanResultfuzzSearch() error",queryScanResultParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 更新接口表信息
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/refreshInterfaceInfo/perform")
    @ResponseBody
    public ResponseBase refreshInterfaceInfo(@RequestBody RefreshInterfaceParam refreshInterfaceParam
            ,HttpServletRequest request, RedirectAttributesModelMap model) {
        if(StringUtils.isEmpty(refreshInterfaceParam.getRefreshType())){
            return setResultWarn("未设置更新类型");
        }

        InterfaceRefreshType interfaceRefreshType = null;
        try {
            interfaceRefreshType = InterfaceRefreshType.valueOf(refreshInterfaceParam.getRefreshType());
        }catch (Exception e){
            return setResultWarn("更新类型类型应是All ,Cust");
        }

        if(interfaceRefreshType == InterfaceRefreshType.Cust
        && (null == refreshInterfaceParam.getAssetIds() || refreshInterfaceParam.getAssetIds().size() == 0)){
            return setResultWarn("未设置资产id");
        }

        mwAssetsInterfaceService.refreshInterfaceInfo(refreshInterfaceParam);
        return setResultSuccess(Reply.ok());
    }

    /**
     * 创建资产扫描队列并执行
     * @param assetsScanParam 扫描参数
     * @param isSaveScanRuled 是否保存规则
     * @param isRescan 是否生成批次号
     * @param executionMode 执行方式
     */
    public String createAssetsScanQueue(AssetsScanParam assetsScanParam
            , boolean isSaveScanRuled, boolean isRescan,Integer executionMode){
        //创建扫描上下文数据
        AssetsScanContext scanContext = new AssetsScanContext();
        scanContext.setScanParam(assetsScanParam);
        scanContext.setSaveScanRuled(isSaveScanRuled);
        scanContext.setRescan(isRescan);
        //设置加入队列参数
        String taskId = UUID.randomUUID().toString().replace("-","");
        AssetsScanTaskDto scanTaskDto = new AssetsScanTaskDto();
        scanTaskDto.setTaskId(taskId);//任务ID
        scanTaskDto.setExecutionName(assetsScanParam.getName());//执行名称
        scanTaskDto.setExecutionMode(executionMode);//执行方式
        scanTaskDto.setExecutionUser(iLoginCacheInfo.getLoginName());//执行用户
        scanTaskDto.setScanContext(scanContext);
        scanTaskDto.setDataPerm(iLoginCacheInfo.getRoleInfo().getDataPerm());
        scanTaskDto.setLoginName(iLoginCacheInfo.getLoginName());
        scanTaskDto.setRoleId(iLoginCacheInfo.getRoleInfo().getId());
        scanTaskDto.setUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
        scanTaskManage.createQueue(scanTaskDto);
        return taskId;
    }

    /**
     * 取出扫描结果
     * @param taskId
     */
    private ResponseBase getScanResult(String taskId) throws InterruptedException {
        Thread.sleep(2000);
        ResponseBase resp = null;
        Queue<AssetsScanTaskRecord> queue = scanTaskManage.getQueue();
        if(CollectionUtils.isNotEmpty(queue)){
            for (AssetsScanTaskRecord taskRecord : queue) {
                if(taskId.equals(taskRecord.getTaskId())){
                    Integer taskStatus = taskRecord.getTaskStatus();
                    if(ScanTaskExecuteStatus.TOBE_EXECUTE.getCode().equals(taskStatus)){
                        resp = setResultFail("扫描任务正在等待执行",taskId);
                    }
                    if(ScanTaskExecuteStatus.EXECUTE_IN.getCode().equals(taskStatus)){
                        resp = setResultFail("扫描任务正在执行中",taskId);
                    }
                    if(ScanTaskExecuteStatus.EXECUTE_COMPLETE.getCode().equals(taskStatus)){
                        resp = setResultSuccess("扫描任务执行成功");
                    }
                }
            }
        }
        return resp;
    }


    /**
     * 获取kafka推送消息状态
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/kafkaPush/status")
    @ResponseBody
    public ResponseBase getKafkaPushStatus() {
        try {
            return setResultSuccess(assetsPush);
        } catch (Exception e) {
            log.error("getKafkaPushStatus{}", e);
            return setResultFail("MWAssetsController{} getKafkaPushStatus() error","");
        }
    }

    /**
     * 资产推送
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assets/kafkaPush/sync")
    @ResponseBody
    public ResponseBase kafkaPushAssets() {
        Reply reply = null;
        MwScanruleDTO msDto = null;
        try {
            mwTangService.tangibleAssetsPushConvert(new ArrayList<>());
        } catch (Exception e) {
            log.error("kafkaPushAssets fail to::", e);
            return setResultFail("同步推送数据失败","");
        }
        return setResultSuccess("同步推送数据失败");
    }

}
