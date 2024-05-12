package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.api.common.IpV6Util;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageListTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpv6ManageListDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpv6ManageTableDao;
import cn.mw.monitor.ipaddressmanage.ipconflict.IpConflictManage;
import cn.mw.monitor.ipaddressmanage.model.IPOnline;
import cn.mw.monitor.ipaddressmanage.model.IPState;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTableParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageScanService;
import cn.mw.monitor.service.assets.model.IpAssetsNameDTO;
import cn.mw.monitor.service.ipmanage.exception.IpScanInterruptException;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.api.MWScanCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.user.model.ScanIpAddressManageQueueVO;
import cn.mw.monitor.snmp.mib.MibIfEntry;
import cn.mw.monitor.snmp.service.MWTopologyService;
import cn.mw.monitor.topology.IpInfoProcess;
import cn.mw.monitor.topology.model.DeviceInfo;
import cn.mw.monitor.topology.model.DeviceTypeInfo;
import cn.mw.monitor.topology.model.PortInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.SpringContextUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.snmp4j.smi.OctetString;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MwIpAddressManageServiceScanImpl implements MwIpAddressManageScanService, MWScanCommonService {
    private static final int MaxIpNum = 255;

    @Value("${ipmanage.ipConfictDetect}")
    private boolean ipConfictEnable;

    @Value("${ipmanage.ip.icmpDetect}")
    private String icmpDetect;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Resource
    private MwIpAddressManageListTableDao mwIpAddressManageListTableDao;

    @Resource
    private MwIpAddressManageTableDao mwIpAddressManageTableDao;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWTopologyService mwTopologyService;

    @Resource
    private MwIpv6ManageListDao ipv6ManageListDao;

    @Resource
    private MwIpv6ManageTableDao ipv6ManageTableDao;

    @Autowired
    private IpConflictManage ipConflictManage;

    @Autowired
    private NotificationManage notificationManage;

    @Override
    public Reply getHisList(AddUpdateIpAddressManageListParam parm) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PageHelper.startPage(parm.getPageNumber(), parm.getPageSize());
        Map priCriteria = PropertyUtils.describe(parm);
        List<AddUpdateIpAddressManageListHisParam> list = mwIpAddressManageListTableDao.getHisList(priCriteria);
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    @Override
    public void updateAssetsType(Integer linkid) {
        //增加资产类型字段 在线状态 资产列表存在 已知 在线状态 资产列表不存在 未知
        mwIpAddressManageListTableDao.updateAssetsTypeKnow(linkid);
        mwIpAddressManageListTableDao.updateAssetsTypeUnKnow(linkid);
        mwIpAddressManageListTableDao.updateAssetsTypeRollback(linkid);
    }

    @Override
    public Reply canleBatchScan(Integer linkId) {
        mwMessageService.createBrokenPonint(linkId.toString());
        try {
            String key = IpInfoProcess.getShutdownKey(linkId);
            IpInfoProcess ipInfoProcess = IpInfoProcess.getIpInfoProcess(key);
            if(null != ipInfoProcess){
                ipInfoProcess.shutdown(key);
            }
        }catch (Exception e){
            log.error("canleBatchScan" ,e);
        }

        return Reply.ok("终止IP扫描");
    }

    @Override
    public void addScanQueue(String parm, Integer userId, Integer linkId) {
        mwIpAddressManageListTableDao.addScanQueue(parm,iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId(),linkId);
    }

    @Override
    public ScanIpAddressManageQueueVO selectqueue() {
        List<ScanIpAddressManageQueueVO> scanIpAddressManageQueueVOS = selectqueueList(0);
        if (scanIpAddressManageQueueVOS!=null&&scanIpAddressManageQueueVOS.size()>0){
            return scanIpAddressManageQueueVOS.get(0);
        }
        return null;
    }



    @Override
    public void deleteQueue(Integer linkId, Integer id) {
        mwIpAddressManageListTableDao.deleteQueue(id,linkId);

    }

    @Override
    public Reply batchScanIp(List<AddUpdateIpAddressManageListParam> uParam){
        return batchScanIp(uParam, null,null);
    }

    @Override
    public Reply batchScanIp(List<AddUpdateIpAddressManageListParam> uParam, Integer linkId, MWUser userInfo) {
        try {
            int ipSize = uParam.size();
            if (uParam != null && ipSize > 0) {
                //检查icmp检测模式
                if (StringUtils.isNotEmpty(icmpDetect) && icmpDetect.equals(IpIcmpType.OnlyIcmpDetect.name())
                        && ipSize < MaxIpNum) {
                    ScanStrategy scanStrategy = SpringUtils.getBean(IcmpScan.class);
                    Set<Integer> inverseTree = new HashSet<>();
                    for (AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam : uParam) {
                        inverseTree.add(addUpdateIpAddressManageListParam.getLinkId());
                    }

                    Reply reply = scanStrategy.ipScan(uParam, linkId, userInfo, new IcmpScanCallback() {
                        @Override
                        public void callback(Set<String> onLineIps) {
                            recursiveIp(inverseTree);
                        }
                    });
                    return reply;
                }

                //1 处理参数
                List<String> ips = new ArrayList<>();
                uParam.forEach(item -> {
                    ips.add(item.getIpAddress());
                });
                //初始化消息记录
                mwMessageService.createMessage("扫描开始", 1, 3, userInfo);

                mwMessageService.createMessage(linkId.toString(), 0, 3, userInfo);
                mwMessageService.brokenPonint(linkId.toString());
                //2 调用接口获取ip状态
                Map<String, List<IPInfoDTO>> scanRes = new HashMap<>();

                List<String> subnetList = null;
                if (null != linkId) {
                    String subnet = mwIpAddressManageListTableDao.selectIpAddresses(linkId);
                    subnetList = new ArrayList<>();
                    subnetList.add(subnet);
                }

                Reply reply = mwTopologyService.getIPInfoList(ips, subnetList, linkId, userInfo, ipConfictEnable);
                if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                    List<IPInfoDTO> sacnReturn = (List) reply.getData();
                    sacnReturn.forEach(item -> {
                        String key = genIPInfoDTOKey(item);
                        List<IPInfoDTO> ipInfoDTOS = scanRes.get(key);
                        if (null == ipInfoDTOS) {
                            ipInfoDTOS = new ArrayList<>();
                            scanRes.put(key, ipInfoDTOS);
                        }
                        ipInfoDTOS.add(item);
                    });
                } else {
                    mwMessageService.createMessage("IP扫描接口出错：" + uParam.toString(), 1, 100, userInfo);
                    mwMessageService.createMessage(linkId.toString(), 0, 100, userInfo);
                    return Reply.fail("IP扫描接口出错", uParam);
                }

                log.info("=======batchScanIp result======");
                Iterator<Map.Entry<String, List<IPInfoDTO>>> it = scanRes.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<IPInfoDTO>> entry = it.next();
                    log.info("key:" + entry.getKey());
                    for (IPInfoDTO ipInfoDTO : entry.getValue()) {
                        log.info("Value:" + ipInfoDTO.toString());
                    }
                }

                mwMessageService.brokenPonint(linkId.toString());

                //由于updateIplistData方法会更新uParam,所以需要先备份数据
                //在检查变化中使用
                List<NotifyParam> copyParams = new ArrayList<>();
                for(AddUpdateIpAddressManageListParam param : uParam){
                    NotifyParam copyParam = new NotifyParam();
                    BeanUtils.copyProperties(param ,copyParam);
                    copyParams.add(copyParam);
                }

                String result = doExecute(() -> {
                    String ret = "";
                    try {
                        ret = updateIplistData(uParam, scanRes);
                    }catch (Exception e){
                        log.error("updateIplistData" ,e);
                        return MwIpAddressManageScanService.SCAN_ERROR;
                    }
                    return ret;
                });

                if(result.equals(MwIpAddressManageScanService.SCAN_SUCCESS)){
                    doExecute(() -> {
                        String ret = MwIpAddressManageScanService.SCAN_SUCCESS;
                        try {
                            notificationManage.checkIPInfoAndNofity(copyParams ,scanRes);
                        }catch (Exception e){
                            log.error("checkIPInfoAndNofity" ,e);
                            return MwIpAddressManageScanService.SCAN_ERROR;
                        }
                        return ret;
                    });
                }

                mwMessageService.createMessage(linkId.toString(), 0, 80, userInfo);
                mwMessageService.brokenPonint(linkId.toString());

                mwMessageService.brokenPonint(linkId.toString());
                mwMessageService.createMessage(linkId.toString(), 0, 100, userInfo);
                mwMessageService.createMessage("IP扫描结束", 1, 0, userInfo);
                return Reply.ok(result);
            }
        }catch (IpScanInterruptException ipScanInterruptException){
            mwMessageService.createMessage("IP扫描被用户终止",1,0,userInfo);
            mwMessageService.createMessage(linkId.toString(),0,100,userInfo);
            return Reply.ok("IP扫描被用户终止");
        }catch (Exception e){
            log.error("batchScanIp", e);
            mwMessageService.createMessage(linkId.toString(),0,100,userInfo);
            mwMessageService.createMessage("IP扫描报错 报错原因："+e.toString(),1,100,userInfo);
            return Reply.fail("IP扫描接口出错",e);
        }
        return null;
    }

    private String doExecute(Callable<String> callable) throws Exception{
        ExecutorService newFixedThreadPool = Executors.newSingleThreadExecutor();
        Future<String> submit = newFixedThreadPool.submit(callable);
        String result = submit.get();
        log.info(result);
        newFixedThreadPool.shutdown();
        return result;
    }

    public void cronBatchScanIpv6(){
        Map<String, DeviceInfo> deviceInfoMap = mwTopologyService.scanIpv6Detail();
        //ipv6资产信息
        Map<String, DeviceInfo> deviceInfoIPV6=new HashMap<>();
        for (String key:deviceInfoMap.keySet()
        ) {
            boolean flag = IpV6Util.isIP(key);
            if(flag){
                deviceInfoIPV6.put(key,deviceInfoMap.get(key));
            }
        }
        List<IPInfoDTO> ret = new ArrayList<IPInfoDTO>();
        deviceInfoIPV6.forEach((key,value)->{
            IPInfoDTO ipInfoDTO = new IPInfoDTO();
            ipInfoDTO.setPort(PortInfo.NOPORT);
            ret.add(ipInfoDTO);
            ipInfoDTO.setIp(key);
            DeviceInfo deviceInfo = deviceInfoMap.get(key);
            if(null == deviceInfo){
                return;
            }
            String mac = deviceInfo.getDeviceMAC();
            ipInfoDTO.setMac(mac);
            Map<Integer,PortInfo> portInfos = deviceInfo.getPortInfoMap();
            if(null == portInfos || portInfos.size() <= 0){
                return;
            }
            DeviceInfo linkDeviceInfo = getLinkDevice(portInfos);
            if(null != linkDeviceInfo) {
                ipInfoDTO.setLinkDeviceName(linkDeviceInfo.getDeviceName());
                ipInfoDTO.setLinkDeviceDesc(linkDeviceInfo.getDeviceDesc());
                PortInfo linkPortInfo = linkDeviceInfo.getMacPortInfoMap().get(mac);
                if (null != linkPortInfo) {
                    ipInfoDTO.setPort(linkPortInfo.port);
                    //获取端口对应的名称
                    MibIfEntry mibIfEntry = linkDeviceInfo.getPortIfMap().get(linkPortInfo.port);
                    if(null != mibIfEntry){
                        ipInfoDTO.setPortName(mibIfEntry.getIfDescr());
                    }
                }
            }
        });
        //ipv6 地址段
        List<QueryIpAddressManageListParam> insetList = new ArrayList<>();
        List<QueryIpAddressManageListParam> updateList = new ArrayList<>();
        List<AddUpdateIpAddressManageListHisParam> createHis = new ArrayList<>();
        List<AddUpdateIpAddressManageListHisParam> updateHis = new ArrayList<>();
        List<Ipv6ManageTableParam> ipv6ManageTableParams = ipv6ManageTableDao.selectIPv6IpAddress();
        for (int i = 0; i <ipv6ManageTableParams.size() ; i++) {
            Ipv6ManageTableParam valIpv6 = ipv6ManageTableParams.get(i);
            ret.stream().forEach(val->{
                String ip = val.getIp();
                String ipAddresses = valIpv6.getIpAddresses();
                //根据ipv6代码段解析出该ipv6是否属于这个IP地址段
                IPv6Network iPv6Addresses = IPv6Network.fromString(ipAddresses);
                IPv6AddressRange range= IPv6AddressRange.fromFirstAndLast(
                        IPv6Address.fromString(iPv6Addresses.getFirst().toString()),
                        IPv6Address.fromString(iPv6Addresses.getLast().toString()));
                boolean contains = range.contains(IPv6Network.fromString(ip));
                if(contains){
                    AddUpdateIpAddressManageListParam ipv6param=new AddUpdateIpAddressManageListParam();
                    ipv6param.setIpAddress(ip);
                    List<AddUpdateIpAddressManageListParam> queryIpAddressManageListParams=null;
                    try {
                        Map describe = PropertyUtils.describe(ipv6param);
                        queryIpAddressManageListParams = ipv6ManageListDao.selectSonList(describe);
                    }catch (Exception e){
                        log.error("查询失败", e);
                    }
                    //判断该ip之前是否存在 不存在添加 存在则修改 有mac地址的是在线 没有的是离线
                    String hexMac = val.getMac();

                    if(hexMac!=null&& !hexMac.equals("")){
                        //历史清单更新
                        createOrUpdateHis(valIpv6,val,createHis,updateHis);
                        //ipv6地址清单
                        scanOnIPv6List(queryIpAddressManageListParams,valIpv6,val,insetList,updateList);
                    }else {
                        //历史清单更新
                        createOrUpdateHis(valIpv6,val,createHis,updateHis);
                        //ipv6地址清单
                        scanOffIPv6List(queryIpAddressManageListParams,valIpv6,val,insetList,updateList);
                    }
                }
            });
        }
        if(insetList.size()>0){
            ipv6ManageListDao.insertIpv6List(insetList);
        }
        if(updateList.size()>0){
            ipv6ManageListDao.insertIpv6List(updateList);
        }
        if(createHis.size()>0){
            ipv6ManageListDao.batchCreateHis(createHis);
        }
        if(updateHis.size()>0){
            ipv6ManageListDao.batchUpdateHis(updateHis);
        }

    }
    private DeviceInfo getLinkDevice(Map<Integer, PortInfo> portInfos){
        for(PortInfo portInfo: portInfos.values()){
            for(DeviceInfo deviceInfo: portInfo.subDeviceList){
                if(DeviceTypeInfo.checkNetworkDevice(deviceInfo)){
                    return deviceInfo;
                }
            }
        }
        return null;
    }
    //新增的时候应该将历史清单也新增上 修改的时候再去比较是否需要新增还是比较
    public void scanOnIPv6List(List<AddUpdateIpAddressManageListParam> queryIpAddressManageListParams,Ipv6ManageTableParam valIpv6
    ,IPInfoDTO val,List<QueryIpAddressManageListParam> insetList,List<QueryIpAddressManageListParam> updateList){
        if(queryIpAddressManageListParams==null){
            //新增
            QueryIpAddressManageListParam insertParam=new QueryIpAddressManageListParam();
            Date date = new Date();
            insertParam.setLinkId(valIpv6.getId());
            insertParam.setIpAddress(val.getIp());
            insertParam.setIpState(1);
            insertParam.setCreator(valIpv6.getCreator());
            insertParam.setCreateDate(date);
            insertParam.setModifier(valIpv6.getCreator());
            insertParam.setModificationDate(date);
            insertParam.setOnline(1);
            insertParam.setMac(val.getMac());
            insertParam.setVendor(getVendorByMac(val.getMac()));
            insertParam.setAccessEquip(val.getLinkDeviceName()==null?"":val.getLinkDeviceName());
            insertParam.setAccessPort(val.getPortName());
            List<String> result = mwModelViewCommonService.getAssetsNameByIp(val.getIp());
            if(result.size()>0) {
                String assets = result.stream().collect(Collectors.joining(","));
                if (assets != null) {
                    insertParam.setAssetsName(assets);
                }
            }
            insetList.add(insertParam);
        }else{
            //做更新
            QueryIpAddressManageListParam insertParam=new QueryIpAddressManageListParam();
            Date date = new Date();
            insertParam.setUpdateDate(date);
            insertParam.setIpState(1);
            insertParam.setOnline(1);
            insertParam.setMac(val.getMac());
            insertParam.setVendor(getVendorByMac(val.getMac()));
            insertParam.setAccessEquip(val.getLinkDeviceName()==null?"":val.getLinkDeviceName());
            insertParam.setAccessPort(val.getPortName());
            List<String> result = mwModelViewCommonService.getAssetsNameByIp(val.getIp());
            if(result.size()>0) {
                String assets = result.stream().collect(Collectors.joining(","));
                if (assets != null) {
                    insertParam.setAssetsName(assets);
                }
            }
            updateList.add(insertParam);
        }
    }

    public void scanOffIPv6List(List<AddUpdateIpAddressManageListParam> queryIpAddressManageListParams,Ipv6ManageTableParam valIpv6
            ,IPInfoDTO val,List<QueryIpAddressManageListParam> insetList,List<QueryIpAddressManageListParam> updateList){
        if(queryIpAddressManageListParams==null){
            //新增
            QueryIpAddressManageListParam insertParam=new QueryIpAddressManageListParam();
            Date date = new Date();
            insertParam.setLinkId(valIpv6.getId());
            insertParam.setIpAddress(val.getIp());
            insertParam.setIpState(1);
            insertParam.setCreator(valIpv6.getCreator());
            insertParam.setCreateDate(date);
            insertParam.setModifier(valIpv6.getCreator());
            insertParam.setModificationDate(date);
            insertParam.setOnline(0);
            insertParam.setAccessEquip(val.getLinkDeviceName()==null?"":val.getLinkDeviceName());
            insertParam.setAccessPort(val.getPortName());
            List<String> result = mwModelViewCommonService.getAssetsNameByIp(val.getIp());
            if(result.size()>0) {
                String assets = result.stream().collect(Collectors.joining(","));
                if (assets != null) {
                    insertParam.setAssetsName(assets);
                }
            }
            insetList.add(insertParam);
        }else{
            QueryIpAddressManageListParam insertParam=new QueryIpAddressManageListParam();
            Date date = new Date();
            insertParam.setUpdateDate(date);
            insertParam.setIpState(1);
            insertParam.setOnline(0);
            insertParam.setAccessEquip(val.getLinkDeviceName()==null?"":val.getLinkDeviceName());
            insertParam.setAccessPort(val.getPortName());
            List<String> result = mwModelViewCommonService.getAssetsNameByIp(val.getIp());
            if(result.size()>0) {
                String assets = result.stream().collect(Collectors.joining(","));
                if (assets != null) {
                    insertParam.setAssetsName(assets);
                }
            }
            updateList.add(insertParam);
        }
    }

    public void createOrUpdateHis(Ipv6ManageTableParam valIpv6
            ,IPInfoDTO val,List<AddUpdateIpAddressManageListHisParam> createHis,List<AddUpdateIpAddressManageListHisParam> updateHis){
        AddUpdateIpAddressManageListHisParam hisListForOne = ipv6ManageListDao.getHisListForOne(val.getIp());
        Boolean same = isSame(hisListForOne, val);
        if(hisListForOne==null||same==false){
            //新增或者mac地址有变化时
            AddUpdateIpAddressManageListHisParam a = new AddUpdateIpAddressManageListHisParam();
            a.setIpAddress(val.getIp());
            a.setLinkId(valIpv6.getId());
            a.setUpdateDate(new Date());
            a.setAccessPort(String.valueOf(val.getPort()));
            a.setAccessPortName(val.getPortName());
            String mac = val.getMac() ==null? "" : OctetString.fromHexString(val.getMac()).toString();
            a.setMac(mac);
            a.setVendor(getVendorByMac(a.getMac()));
            a.setAccessEquip(val.getLinkDeviceName()==null?"":val.getLinkDeviceName());
            createHis.add(a);
        }else{
            //更新 查看设备和mac地址是否有变化 无变化修改更新时间即可
            hisListForOne.setUpdateDate(new Date());
            updateHis.add(hisListForOne);
        }
    }

    //定期扫描
    //定期扫描
    public TimeTaskRresult cronBatchScanIp(String id) {
        //判断定时任务传来的参数  根据参数得知改定时任务是三小时 六小时 八小时的定时
        log.info("cronBatchScanIp start {}" ,id);
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //ip地址段
        List<Integer> idList = new ArrayList<>();
        idList.add(Integer.valueOf(id));
        idList.stream().forEach(linkId -> {;
            List<AddUpdateIpAddressManageListParam> uParam = mwIpAddressManageListTableDao.selectListByLinkId(linkId);
            String subnet = mwIpAddressManageListTableDao.selectIpAddresses(linkId);
            List<String> subnetList = null;
            if(StringUtils.isNotEmpty(subnet)){
                subnetList = new ArrayList<>();
                subnetList.add(subnet);
            }
            try{
                if(uParam!=null && uParam.size()>0){
                    //1 处理参数
                    List<String> ips = new ArrayList<>();
                    uParam.forEach(item -> {
                        ips.add(item.getIpAddress());
                    });

                    //2 调用接口获取ip状态
                    List<IPInfoDTO> sacnReturn = new ArrayList<>();
                    Map<String, List<IPInfoDTO>> scanRes = new HashMap<>();
                    Reply reply =  mwTopologyService.getIPInfoList(ips ,subnetList ,ipConfictEnable);
                    if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                        sacnReturn = (List)reply.getData();
                        sacnReturn.forEach(item -> {
                            String key = genIPInfoDTOKey(item);
                            List<IPInfoDTO> ipInfoDTOS = scanRes.get(key);
                            if(null == ipInfoDTOS){
                                ipInfoDTOS = new ArrayList<>();
                                scanRes.put(key,ipInfoDTOS);
                            }
                            ipInfoDTOS.add(item);
                        });
                    }else{
                        throw new Exception("没有对应的ip信息");
                    }
                    //3 更新ip清单状态及历史
                    ExecutorService newFixedThreadPool = Executors.newSingleThreadExecutor();
                    Future<String> submit = newFixedThreadPool.submit(new Callable<String>() {
                        @Override
                        public String call(){
                            String ret = "";
                            try {
                                ret = updateIplistData(uParam, scanRes);
                            }catch (Exception e){
                                log.error("updateIplistData" ,e);
                                return MwIpAddressManageScanService.SCAN_ERROR;
                            }
                            return ret;
                        }
                    });

                    String ret = submit.get();
                    log.info(ret);

                }
                mwIpAddressManageListTableDao.updateScanTime(0,Integer.parseInt(id),null);
                taskRresult.setSuccess(true).setResultContext("IP扫描成功");
            }catch (Exception e){
                log.error("cronBatchScanIp",e);
                taskRresult.setSuccess(false).setFailReason(e.toString());
            }
        });
        return taskRresult;
    }



    private String updateIplistData(List<AddUpdateIpAddressManageListParam> uParam, final Map<String, List<IPInfoDTO>> scanRes) {

        List<AddUpdateIpAddressManageListHisParam> createHiss = new ArrayList<>();
        List<AddUpdateIpAddressManageListParam> update = new ArrayList<>();
        Map<String, AddUpdateIpAddressManageListParam> ipParamMap = new HashMap<>();

        Set<Integer> inverseTree = new HashSet<>();//存储页子节点主键

        for (int i = 0; i < uParam.size(); i++) {
            ipParamMap.put(uParam.get(i).getIpAddress(), uParam.get(i));
        }

        Date start = new Date();

        //设置接入设备及端口信息
        Map<Integer, AddUpdateIpAddressManageListParam> idMap = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        for (AddUpdateIpAddressManageListParam param : uParam){
            if (StringUtils.isNotEmpty(param.getAccessPortName()) && param.getAccessPortName().equals(IPInfoDTO.MORE_DATA)) {
                idMap.put(param.getId(), param);
                ids.add(param.getId());
            }
        }
        if(ids.size() > 0) {
            List<AddUpdatePortInfoParam> portInfoParams = mwIpAddressManageListTableDao.selectPortInfos(ids);
            for (AddUpdatePortInfoParam portInfoParam : portInfoParams) {
                AddUpdateIpAddressManageListParam ipInfo = idMap.get(portInfoParam.getIpManageListId());
                List<AddUpdatePortInfoParam> portInfoParamList = ipInfo.getPortInfos();
                if (null == portInfoParamList) {
                    portInfoParamList = new ArrayList<>();
                    ipInfo.setPortInfos(portInfoParamList);
                }
                portInfoParamList.add(portInfoParam);
            }
        }

        Date end = new Date();
        long interval = DateUtils.between(start ,end , DateUnitEnum.SECOND);
        log.info("设置接入设备及端口信息:{}s" ,interval);

        for (AddUpdateIpAddressManageListParam addIp : uParam) {
            //更新ip地址清单信息及历史
            String key = genAddUpdateIpAddressManageListParamKey(addIp);
            List<IPInfoDTO> ipInfoDTOList = scanRes.get(key);
            if(null == ipInfoDTOList){
                log.info("ipInfoDTOList is null key:" + key);
                continue;
            }

            IPInfoDTO newAddip = ipInfoDTOList.get(0);

            //更新使用状态处理
            //如果ip已经关联资产,且设置为保留状态,则不变

            //1更新历史表
            updateOrCreateHis(addIp, ipInfoDTOList, createHiss);

            if (newAddip.isOnline()) {
                //2更新清单表
                updateIpListScanOn(addIp, newAddip, update, inverseTree);
            } else {//扫描没有结果
                //2更新清单表
                updateIpListScanOff(addIp, newAddip, update, inverseTree);
            }
        }

        //检查更新ip地址状态,以及资产名称
        updateIpStateAndAssetInfo(uParam ,scanRes);

        //检查需要新增的数据
        checkAddPortInfo(scanRes, ipParamMap);

        Date end3 = new Date();
        interval = DateUtils.between(end ,end3 , DateUnitEnum.SECOND);
        log.info("检查需要新增的数据:{}s" ,interval);

        //保存更新数据
        if(createHiss.size()>0){
            String batchId = MwIpAddressManageScanService.PREFIX + UUIDUtils.getUUID();
            for(AddUpdateIpAddressManageListHisParam param:createHiss){
                param.setBatchId(batchId);
            }

            //追加历史记录
            mwIpAddressManageListTableDao.batchCreateHis(createHiss);

            //根据批次id返回历史记录
            Map criteria = new HashMap();
            criteria.put("batchId",batchId);
            List<AddUpdateIpAddressManageListHisParam> params = mwIpAddressManageListTableDao.selectHis(criteria);
            Map<String, AddUpdateIpAddressManageListHisParam> hisIpMap = new HashMap<>();
            for(AddUpdateIpAddressManageListHisParam param:params){
                hisIpMap.put(param.getIpAddress(), param);
            }

            //设置历史端口信息关联id
            List<AddUpdatePortInfoHisParam> hisParams = new ArrayList<>();
            for(AddUpdateIpAddressManageListHisParam param:createHiss){
                if(null != param.getPortInfos()){
                    for(AddUpdatePortInfoParam portInfoParam:param.getPortInfos()){
                        AddUpdatePortInfoHisParam hisParam = new AddUpdatePortInfoHisParam();
                        BeanUtils.copyProperties(portInfoParam, hisParam);

                        Integer hisId = hisIpMap.get(param.getIpAddress()).getId();
                        hisParam.setIpManageListHisId(hisId);

                        hisParams.add(hisParam);
                    }

                    if(hisParams.size() > 0){
                        mwIpAddressManageListTableDao.insertHisPortInfos(hisParams);
                    }
                }
            }
        }

        Date end4 = new Date();
        interval = DateUtils.between(end3 ,end4 , DateUnitEnum.SECOND);
        log.info("追加历史记录:{}s" ,interval);

        List<AddUpdateIpAddressManageListHisParam> listHisParams = new ArrayList<>();
        if (update.size()>0){
            List<AddUpdateIpAddressManageListParam> list = mwIpAddressManageListTableDao.selectListByLinkId(update.get(0).getLinkId());

            for (int p = 0; p <update.size() ; p++) {
                for (int k = 0; k < list.size(); k++) {
                    if (list.get(k).getIpAddress().equals(update.get(p).getIpAddress())){
                        AddUpdateIpAddressManageListHisParam addUpdateIpAddressManageListHisParam = new AddUpdateIpAddressManageListHisParam();
                        addUpdateIpAddressManageListHisParam.setBatchId(MwIpAddressManageScanService.PREFIX + UUIDUtils.getUUID());
                        addUpdateIpAddressManageListHisParam.setIpAddress(list.get(k).getIpAddress());
                        addUpdateIpAddressManageListHisParam.setLinkId(list.get(k).getId());
                        addUpdateIpAddressManageListHisParam.setUpdateDate(new Date());
                        if (list.get(k).getIpState()!=update.get(p).getIpState()&&list.get(k).getOnline()!=update.get(p).getOnline()){
                            addUpdateIpAddressManageListHisParam.setChangeIpStatus(3);
                            listHisParams.add(addUpdateIpAddressManageListHisParam);
                        }
                        if (list.get(k).getIpState()!=update.get(p).getIpState()&&list.get(k).getOnline()==update.get(p).getOnline()){
                            addUpdateIpAddressManageListHisParam.setChangeIpStatus(2);
                            listHisParams.add(addUpdateIpAddressManageListHisParam);
                        }
                        if (list.get(k).getIpState()==update.get(p).getIpState()&&list.get(k).getOnline()!=update.get(p).getOnline()){
                            addUpdateIpAddressManageListHisParam.setChangeIpStatus(1);
                            listHisParams.add(addUpdateIpAddressManageListHisParam);
                        }
                    }
                }
            }
        }


        //状态记录历史
        if (listHisParams.size()>0){
            mwIpAddressManageListTableDao.batchCreateHis(listHisParams);
        }

        if(update.size()>0){
            mwIpAddressManageListTableDao.batchUpdateList(update);
        }

        Date end5 = new Date();
        interval = DateUtils.between(end4 ,end5 , DateUnitEnum.SECOND);
        log.info("状态记录历史:{}s" ,interval);

        //更新统计信息
        if(inverseTree.size()>0){
            recursiveIp(inverseTree);
        }

        //更新ip冲突信息
        if(ipConfictEnable){
            updateConfictHis(scanRes);
        }

        Date end6 = new Date();
        interval = DateUtils.between(end5 ,end6 , DateUnitEnum.SECOND);
        log.info("初始化页子节点数量:{}s" ,interval);

        return MwIpAddressManageScanService.SCAN_SUCCESS;
    }

    public void updateIpStateAndAssetInfo(List<AddUpdateIpAddressManageListParam> uParam ,Map<String, List<IPInfoDTO>> scanRes){

        List<List<Integer>> ob = new ArrayList<>();
        List<List<String>> ipGroups = new ArrayList<>();
        //固定分组盒子 最大 260
        int groupSize = 260;
        List<Integer> pi = new ArrayList<>();
        List<String> uParamIP = new ArrayList<String>();
        int j = 0;
        for (int i = 0; i < uParam.size(); i++) {
            //固定拆分数组
            if (j == groupSize) {
                ob.add(pi);
                ipGroups.add(uParamIP);
                j = 0;
                pi = new ArrayList<>();
                ;
                uParamIP = new ArrayList<>();
            } else {
                AddUpdateIpAddressManageListParam param = uParam.get(i);
                pi.add(param.getId());
                uParamIP.add(param.getIpAddress());
            }
            j++;
        }
        if (pi.size() > 0) {
            ob.add(pi);
            ipGroups.add(uParamIP);
        }
        if (ob.size() == 1) {
            pi = new ArrayList<>();
            pi.add(-1);
            ob.add(pi);
        }

        Map<String, List<IpAssetsNameDTO>> ipAssetNameMap = new HashMap<String, List<IpAssetsNameDTO>>();
        for (List<String> list : ipGroups) {
            //获取ip关联的资产信息
            List<IpAssetsNameDTO> ipAssetsNameDTOS = mwModelViewCommonService.getAssetsNameByIps(list);
            for (IpAssetsNameDTO ipAssetsNameDTO : ipAssetsNameDTOS) {

                if (StringUtils.isNotEmpty(ipAssetsNameDTO.getInBandIp())){
                    addIpAssetNameMap(ipAssetsNameDTO.getInBandIp() ,ipAssetsNameDTO ,ipAssetNameMap);
                    continue;
                }

                if(StringUtils.isNotEmpty(ipAssetsNameDTO.getOutBandIp())) {
                    addIpAssetNameMap(ipAssetsNameDTO.getOutBandIp() ,ipAssetsNameDTO ,ipAssetNameMap);
                }
            }
        }

        //设置ip状态,已使用：资产表内有/PING通/ARP\MAC地址表内有过
        for(AddUpdateIpAddressManageListParam param: uParam) {
            //更新资产名称
            //检查资产表是否有数据
            String assetName = null;
            List<IpAssetsNameDTO> ipAssetsNameDTOS = ipAssetNameMap.get(param.getIpAddress());
            if(null != ipAssetsNameDTOS && ipAssetsNameDTOS.size() > 0){
                StringBuffer sb = new StringBuffer();

                //加上带外资产 所有IP对应的资产名称均加上 以逗号分隔
                for(IpAssetsNameDTO ipAssetsNameDTO : ipAssetsNameDTOS){
                    if(StringUtils.isEmpty(param.getAssetsId())){
                        param.setAssetsId(ipAssetsNameDTO.getAssetId());
                    }
                    sb.append(",").append(ipAssetsNameDTO.getAssetsName());
                }
                assetName = sb.substring(1);
                param.setAssetsName(assetName);
            }

            if(null == param.getIpState()){
                continue;
            }

            //当状态未使用时,检查并更新状态,处于已使用状态的,不再更新,需要通过用户手动修改
            int state = param.getIpState().intValue();
            if(state == IPState.NotUsed.getCode()){
                boolean checkUpdate = false;

                if(StringUtils.isNotEmpty(assetName)){
                    checkUpdate = true;
                }

                List<IPInfoDTO> ipInfoDTOList = scanRes.get(param.getIpAddress());
                if(null != ipInfoDTOList && ipInfoDTOList.size() > 0){
                    IPInfoDTO ipInfoDTO = ipInfoDTOList.get(0);
                    if(ipInfoDTO.isOnline() || StringUtils.isNotEmpty(ipInfoDTO.getMac())){
                        checkUpdate = true;
                    }
                }

                if(checkUpdate){
                    param.setIpState(IPState.Used.getCode());
                }
            }
        }
    }

    private void addIpAssetNameMap(String ip ,IpAssetsNameDTO ipAssetsNameDTO ,Map<String, List<IpAssetsNameDTO>> ipAssetNameMap){
        List<IpAssetsNameDTO> dataList = ipAssetNameMap.get(ip);
        if (null == dataList) {
            dataList = new ArrayList<>();
            ipAssetNameMap.put(ip, dataList);
        }
        dataList.add(ipAssetsNameDTO);
    }

    private void updateConfictHis(Map<String, List<IPInfoDTO>> scanRes){
        List<IPInfoDTO> ipInfoDTOS = new ArrayList<>();
        for(List<IPInfoDTO> ipInfoDTOList : scanRes.values()) {
            for (IPInfoDTO ipInfoDTO : ipInfoDTOList) {
                if (null != ipInfoDTO.getConflictsIp() && ipInfoDTO.getConflictsIp().size() > 0) {
                    ipInfoDTOS.add(ipInfoDTO);
                }
            }
        }

        if(ipInfoDTOS.size() > 0){
            ipConflictManage.batchInsert(ipInfoDTOS);
        }
    }

    private void checkAddPortInfo(Map<String, List<IPInfoDTO>> scanRes, Map<String, AddUpdateIpAddressManageListParam> ipParamMap){

        List<AddUpdatePortInfoParam> insertlist = new ArrayList<>();
        List<AddUpdatePortInfoParam> deletelist = new ArrayList<>();
        for (Map.Entry<String, List<IPInfoDTO>> entry : scanRes.entrySet()) {
            AddUpdateIpAddressManageListParam ipAddressManageListParam = ipParamMap.get(entry.getKey());
            if(entry.getValue().size() > 1){
                //当存在2个以上的上联设备时,需要更新显示方式
                setMoreData(ipAddressManageListParam);

                AddUpdatePortInfoParam deleteParam = new AddUpdatePortInfoParam();
                deleteParam.setIpManageListId(ipAddressManageListParam.getId());
                deletelist.add(deleteParam);

                for(IPInfoDTO ipInfoDTO: entry.getValue()){
                    AddUpdatePortInfoParam param = new AddUpdatePortInfoParam();
                    param.setAccessEquip(ipInfoDTO.getLinkDeviceName());
                    param.setAccessPort(String.valueOf(ipInfoDTO.getPort()));
                    param.setAccessPortName(ipInfoDTO.getPortName());
                    param.setIpManageListId(ipAddressManageListParam.getId());
                    insertlist.add(param);
                }
            }
        }

        if(insertlist.size() > 0){
            mwIpAddressManageListTableDao.deletePortInfos(deletelist);
            mwIpAddressManageListTableDao.insertPortInfos(insertlist);
        }

    }

    private String genIPInfoDTOKey(IPInfoDTO ipInfoDTO){
        return ipInfoDTO.getIp();
    }

    private String genAddUpdateIpAddressManageListParamKey(AddUpdateIpAddressManageListParam param){
        return param.getIpAddress();
    }

    //初始化页子节点数量，递归的开始
    public void recursiveIp(Set<Integer> ids) {
        List<IpAddressManageTableParam> updateList = new ArrayList<>();
        for (Integer id : ids) {
            int online = 0;//在线数量
            int offline = 0;//离线数据
            int useCount = 0;//使用数量
            int notUserCount = 0 ;//未使用数量
            int reservedCount = 0 ;//预留数量
            List<AddUpdateIpAddressManageListParam> list = mwIpAddressManageListTableDao.selectListByLinkId(id);
            for (AddUpdateIpAddressManageListParam parm : list) {
                int isOnline = parm.getOnline()==null||parm.getOnline().equals("")? 0:parm.getOnline();
                if(isOnline==0){
                    offline++;
                }else if(isOnline==1){
                    online++;
                }

                int ipState = parm.getIpState()==null||parm.getIpState().equals("")? IPState.NotUsed.getCode():parm.getIpState();
                if(ipState==IPState.NotUsed.getCode()){
                    notUserCount++;
                }else if(ipState==IPState.Used.getCode()){
                    useCount++;
                }else if(ipState==IPState.Reserved.getCode()){
                    reservedCount++;
                }
            }
            IpAddressManageTableParam data = IpAddressManageTableParam.builder().id(id)
                    .offline(offline).online(online)
                    .useCount(useCount).notuseCount(notUserCount)
                    .reservedCount(reservedCount).build();
            updateList.add(data);
        }
        mwIpAddressManageListTableDao.updateBatch(updateList);
        Set<Integer> parentIds = mwIpAddressManageListTableDao.getIdsByIds(ids);
        if(parentIds!=null && parentIds.size()>0){
            recursive(parentIds);
        }
    }

    private void recursive(Set<Integer> ids) {
        List<IpAddressManageTableParam> updateList = new ArrayList<>();
        for (Integer id : ids) {
            int online = 0;//在线数量
            int offline = 0;//离线数据
            int useCount = 0;//使用数量
            int notUserCount = 0 ;//未使用数量
            int reservedCount = 0 ;//预留数量
            List<IpAddressManageTableParam> list = mwIpAddressManageTableDao.selectListById(id);
            for (IpAddressManageTableParam parm : list) {
                online += parm.getOnline();
                offline += parm.getOffline();
                useCount += parm.getUseCount();
                notUserCount += parm.getNotuseCount();
                reservedCount += parm.getReservedCount();
            }
            IpAddressManageTableParam data = IpAddressManageTableParam.builder().id(id)
                    .offline(offline).online(online)
                    .useCount(useCount).notuseCount(notUserCount)
                    .reservedCount(reservedCount).build();
            updateList.add(data);
        }
        mwIpAddressManageListTableDao.updateBatch(updateList);
        Set<Integer> parentIds = mwIpAddressManageListTableDao.getIdsByIds(ids);
        if(parentIds!=null && parentIds.size()>0){
            recursive(parentIds);
        }
    }

    public void updateIpListScanOn(AddUpdateIpAddressManageListParam addIp,IPInfoDTO newAddip,
                                   List<AddUpdateIpAddressManageListParam> update,Set<Integer> inverseTree){


        Integer onLineNow = addIp.getOnline();
        AddUpdateIpAddressManageListParam addressManageListParam =  addIp;
        if(onLineNow==null || onLineNow== IPOnline.OFFLINE.getCode()){
            //之前是离线，现在是在线
            //更新 更新时间 在线状态 使用状态 mac 厂商 接入设备 接入端口
            //逆树结构修改使用状态和离线状态数量
            addIp.setOnline(IPOnline.ONLINE.getCode());
        }

        extractIPInfoDTO(addIp, newAddip);

        if (addIp.getIsRewrite()==1){
            Integer type = changeType(addIp,addressManageListParam);
            addIp.setIsUpdate(type);
        }

        if(newAddip.getConflictsIp().size() > 0){
            addIp.setConflict(true);
        }

        update.add(addIp);

        inverseTree.add(addIp.getLinkId());
    }


    private void extractIPInfoDTO(BaseIpAddressManageListParam addIp, IPInfoDTO newAddip){
        String mac = newAddip.getMac() ==null? "" : OctetString.fromHexString(newAddip.getMac()).toString();
        addIp.setMac(mac);
        String vendor = getVendorByMac(mac);
        addIp.setVendor(vendor);
        addIp.setAccessEquip(newAddip.getLinkDeviceName()==null?"":newAddip.getLinkDeviceName());
        addIp.setAccessPort(String.valueOf(newAddip.getPort()));
        addIp.setAccessPortName(newAddip.getPortName());
        addIp.setUpdateDate(new Date());
    }

    private Integer changeType(AddUpdateIpAddressManageListParam addIp, AddUpdateIpAddressManageListParam addressManageListParam) {
        Integer type  = 0;
        if (!addIp.getMac().equals(addressManageListParam.getMac())||!addIp.getAssetsName().equals(addressManageListParam.getAssetsName())||addIp.getAccessPort()!=addressManageListParam.getAccessPort()
                ||!addIp.getAccessPortName().equals(addressManageListParam.getAccessPortName())||!addIp.getAccessPortName().equals(addressManageListParam.getAccessPortName())
                ||!addIp.getAccessEquip().equals(addressManageListParam.getAccessEquip())||!addIp.getVendor().equals(addressManageListParam.getVendor())){

            type++;
        }
        return type;
    }

    public void updateIpListScanOff(AddUpdateIpAddressManageListParam addIp,IPInfoDTO newAddip,
                                    List<AddUpdateIpAddressManageListParam> update,Set<Integer> inverseTree){


        Integer onLineNow = addIp.getOnline();
        AddUpdateIpAddressManageListParam addressManageListParam =  addIp;
        if(onLineNow==null || onLineNow==IPOnline.OFFLINE.getCode()){//之前是离线，现在是离线
            addIp.setUpdateDate(new Date());
        }else {//之前是在线，现在是离线
            //更新 更新时间 在线状态 使用状态 mac 厂商 接入设备 接入端口
            //逆树结构修改使用状态和离线状态数量
            addIp.setOnline(IPOnline.OFFLINE.getCode());
        }

        extractIPInfoDTO(addIp, newAddip);

        if (addIp.getIsRewrite()==1){
            Integer type = changeType(addIp,addressManageListParam);
            addIp.setIsUpdate(type);
        }

        if(newAddip.getConflictsIp().size() > 0){
            addIp.setConflict(true);
        }

        update.add(addIp);

        inverseTree.add(addIp.getLinkId());
    }


    public int extractIPState(AddUpdateIpAddressManageListParam addIp){
        int c = addIp.getIpState()==null?IPState.NotUsed.getCode():addIp.getIpState();
        return c==IPState.Reserved.getCode()? c:IPState.Used.getCode();
    }

    public void updateOrCreateHis(AddUpdateIpAddressManageListParam addIp,List<IPInfoDTO> ipInfoDTOList,
                                  List<AddUpdateIpAddressManageListHisParam> createHiss){

        //判断是否mac等信息发生改变
        Map<String, AddUpdateIpAddressManageListParam> paramMap = new HashMap<>();
        Map<String, AddUpdateIpAddressManageListParam> ipMap = new HashMap<>();
        if(StringUtils.isNotEmpty(addIp.getAccessPortName()) && IPInfoDTO.MORE_DATA.equals(addIp.getAccessPortName())){
            for(AddUpdatePortInfoParam portInfoParam : addIp.getPortInfos()){
                AddUpdateIpAddressManageListParam param = new AddUpdateIpAddressManageListParam();
                BeanUtils.copyProperties(portInfoParam, param);
                param.setMac(addIp.getMac());
                paramMap.put(param.getAccessPortName(), param);
            }
        }
        ipMap.put(addIp.getIpAddress(), addIp);

        for(IPInfoDTO newAddip: ipInfoDTOList) {
            String key = StringUtils.isEmpty(newAddip.getPortName())?"null":newAddip.getPortName();
            AddUpdateIpAddressManageListParam compare = paramMap.get(key);
            if(null == compare) {
                compare = ipMap.get(newAddip.getIp());
            }
            Boolean isSame = isSame(compare, newAddip);
            if (!isSame) {
                AddUpdateIpAddressManageListHisParam his = new AddUpdateIpAddressManageListHisParam();
                his.setIpAddress(addIp.getIpAddress());
                his.setLinkId(addIp.getId());

                BeanUtils.copyProperties(compare, his);
                his.setUpdateDate(new Date());
                his.setLinkId(compare.getId());

                if(null != addIp.getPortInfos() && addIp.getPortInfos().size() > 1) {
                    his.setPortInfos(addIp.getPortInfos());
                    setMoreData(his);
                }
                createHiss.add(his);
                break;
            }
        }
    }

    private void setMoreData(BaseIpAddressManageListParam param){
        param.setAccessEquip(IPInfoDTO.MORE_DATA);
        param.setAccessPort(IPInfoDTO.MORE_DATA);
        param.setAccessPortName(IPInfoDTO.MORE_DATA);
    }

    //根据mac前六位查询vendor
    private String getVendorByMac(String mac){
        if(mac==null || mac.equals("")){
            return "";
        }

        String mac6 = mac.replace(":","").substring(0,6);
        MwOUIParam oui = mwIpAddressManageListTableDao.selectOUIByMac(mac6);
        if(oui != null){
            return  oui.getShortName()==null||oui.getShortName().equals("")? "未知":oui.getShortName();
        }else {
            return "";
        }
    }

    private Boolean isSame(BaseIpAddressManageListParam c, IPInfoDTO newAddip) {

        if(null == c && null != newAddip){
            return false;
        }
        String mac1 = c.getMac();
        String mac2 = newAddip.getMac();
        if(StringUtils.isEmpty(mac1)){
            if(StringUtils.isNotEmpty(mac2)){
                return false;
            }
        }else {
            if(StringUtils.isEmpty(mac2)){
                return false;
            }else{
                String mac = OctetString.fromHexString(mac2).toString();
                if(!mac1.equals(mac)){
                    return false;
                }
            }
        }

        String accessEquip1 = c.getAccessEquip();
        String accessEquip2 = newAddip.getLinkDeviceName();
        if(StringUtils.isEmpty(accessEquip1)){
            if(StringUtils.isNotEmpty(accessEquip2)){
                return false;
            }
        }else {
            if(StringUtils.isEmpty(accessEquip2)){
                return false;
            }else{
                if(!accessEquip1.equals(accessEquip2)){
                    return false;
                }
            }
        }

        int accessPort1 = (StringUtils.isEmpty(c.getAccessPort())?-1:Integer.valueOf(c.getAccessPort()));
        int accessPort2 = newAddip.getPort();
        if(accessPort1 <= 0){
            if(accessPort2 > 0){
                return false;
            }
        }else {
            if(accessPort2 <= 0){
                return false;
            }else{
                if(accessPort1 != accessPort2){
                    return false;
                }
            }
        }

        String accessPortName1 = c.getAccessPortName();
        String accessPortName2 = newAddip.getPortName();
        if(StringUtils.isEmpty(accessPortName1)){
            if(StringUtils.isNotEmpty(accessPortName2)){
                return false;
            }
        }else {
            if(StringUtils.isEmpty(accessPortName2)){
                return false;
            }else{
                if(!accessPortName1.equals(accessPortName2)){
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public List<ScanIpAddressManageQueueVO> selectqueueList(Integer integer) {
        return mwIpAddressManageListTableDao.selectqueue(integer);
    }
}
