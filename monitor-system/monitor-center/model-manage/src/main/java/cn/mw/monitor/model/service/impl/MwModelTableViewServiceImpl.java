package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.model.dao.MwModelSnmpInfoDao;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.ModelTableViewParam;
import cn.mw.monitor.model.service.MwModelTableViewService;
import cn.mw.monitor.model.type.ARPType;
import cn.mw.monitor.model.type.InterfaceState;
import cn.mw.monitor.model.type.MACState;
import cn.mw.monitor.model.type.TableViewEnum;
import cn.mw.monitor.service.assets.model.MwSnmpAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.param.RuleParam;
import cn.mw.monitor.service.scan.param.SearchParam;
import cn.mw.monitor.service.scan.param.SnmpSearchAction;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.dto.MwLoginUserDto;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.snmp.mib.Dot1dTpFdbEntry;
import cn.mw.monitor.snmp.mib.Dot1qVlanStaticEntry;
import cn.mw.monitor.snmp.mib.MibIfEntry;
import cn.mw.monitor.snmp.mib.MibMacIP;
import cn.mw.monitor.topology.SnmpSearchService;
import cn.mw.monitor.topology.model.DeviceInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.DBUtils;
import cn.mw.monitor.util.ExcelUtils;
import cn.mw.monitor.util.GzipTool;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author guiquanwnag
 * @datetime 2023/7/2
 * @Description 实现类
 */
@Service
@Slf4j
public class MwModelTableViewServiceImpl implements MwModelTableViewService {

    @Value("#{'${scan.networkDevice.type}'.split(',')}")
    private Set<Integer> networkDeviceType;


    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    @Autowired
    private MwModelSnmpInfoDao mwModelSnmpInfoDao;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Override
    public Reply getTableView(ModelTableViewParam param) {
        TableViewEnum tableViewEnum = TableViewEnum.getByTypeId(param.getViewType());
        if (tableViewEnum == null) {
            return Reply.fail("获取失败，请选择视图类型");
        }
        List<ModelTableViewInfo> resultList;
        String id = String.valueOf(param.getAssetsId());
        ModelSnmpInfoDTO modelSnmpInfoDTO = mwModelSnmpInfoDao.selectById(id);
        if (null != modelSnmpInfoDTO) {
            if (!modelSnmpInfoDTO.isDataEmpty()) {
                resultList = modelSnmpInfoDTO.getSnmpInfo(tableViewEnum);
            } else {
                resultList = getListFromScan(param,id);
            }
        } else {
            resultList = getListFromScan(param ,id);
        }
        resultList = filterByParams(param, resultList);
        PageInfo pageInfo = pageList(resultList, param);
        return Reply.ok(pageInfo);
    }

    private List<ModelTableViewInfo> getListFromScan(ModelTableViewParam param ,String id) {
        List<ModelTableViewInfo> resultList = new ArrayList<>();
        List<DeviceInfo> deviceList = getDeviceList(param);
        if (CollectionUtils.isEmpty(deviceList)) {
            return resultList;
        }

        for (DeviceInfo device : deviceList) {
            List<List<ModelTableViewInfo>> ret = doGetListFromScan(device);
            TableViewEnum tableViewEnum = TableViewEnum.getByTypeId(param.getViewType());
            resultList = ret.get(tableViewEnum.getIndex());
            saveDB(id, ret);
        }
        return resultList;
    }

    private List<List<ModelTableViewInfo>> doGetListFromScan(DeviceInfo device){
        List<List<ModelTableViewInfo>> resultList = new ArrayList<>();

        for(TableViewEnum tableViewEnum: TableViewEnum.values()) {
            List<ModelTableViewInfo> tableViewInfos = new ArrayList<>();
            switch (tableViewEnum) {
                case ARP:
                    for (MibMacIP arp : device.getMibMacIPList()) {
                        ModelTableViewInfo view = new ModelTableViewInfo();
                        log.info("mac is " + arp.getIpNetToMediaPhysAddress());
                        view.setMacAddress(arp.getIpNetToMediaPhysAddress());
                        //macAddress为空则去除
                        if (StringUtils.isEmpty(view.getMacAddress())) {
                            continue;
                        }
                        view.setType(String.valueOf(arp.getIpNetToMediaType()));
                        view.setType(ARPType.getNameByType(arp.getIpNetToMediaType()));
                        view.setIpAddress(arp.getIpNetToMediaNetAddress());
                        MibIfEntry mibIfEntry = device.getPortIfMap().get(arp.getIpNetToMediaIfIndex());
                        if (mibIfEntry != null) {
                            view.setInterfaceName(mibIfEntry.getIfDescr());
                            List<Dot1qVlanStaticEntry> entries = device.getIfVlanMap().get(mibIfEntry.getIfIndex());
                            if (CollectionUtils.isNotEmpty(entries)) {
                                String vlanName = entries.stream().map(Dot1qVlanStaticEntry::getDot1qVlanStaticName).collect(Collectors.joining(","));
                                view.setVlanName(vlanName);
                            }
                        }
                        tableViewInfos.add(view);
                    }
                    break;
                case MAC:
                    for (Dot1dTpFdbEntry mac : device.getdTpFdbTableList()) {
                        ModelTableViewInfo view = new ModelTableViewInfo();
                        MibIfEntry mibIfEntry = device.getIfEntryByBasePort(mac.getDot1dTpFdbPort());
                        log.info("mac is " + mac.getDot1dTpFdbAddress());
                        view.setMacAddress(mac.getDot1dTpFdbAddress());
                        //macAddress为空则去除
                        if (StringUtils.isEmpty(view.getMacAddress())) {
                            continue;
                        }
                        view.setStatus(MACState.getNameByState(mac.getDot1dTpFdbStatus()));
                        if (mibIfEntry != null) {
                            view.setInterfaceName(mibIfEntry.getIfDescr());
                            List<Dot1qVlanStaticEntry> entries = device.getIfVlanMap().get(mibIfEntry.getIfIndex());
                            if (CollectionUtils.isNotEmpty(entries)) {
                                String vlanName = entries.stream().map(Dot1qVlanStaticEntry::getDot1qVlanStaticName).collect(Collectors.joining(","));
                                view.setVlanName(vlanName);
                            }
                        }
                        tableViewInfos.add(view);
                    }
                    break;
                case INTERFACE:
                    for (MibIfEntry ip : device.getPortIfMap().values()) {
                        ModelTableViewInfo view = new ModelTableViewInfo();
                        log.info("mac is " + ip.getIfPhysAddress());
                        view.setMacAddress(ip.getIfPhysAddress());
                        //macAddress为空则去除
                        if (StringUtils.isEmpty(view.getMacAddress())) {
                            continue;
                        }
                        view.setStatus(InterfaceState.getNameByState(ip.getIfOperStatus()));
                        view.setInterfaceName(ip.getIfDescr());
                        List<Dot1qVlanStaticEntry> entries = device.getIfVlanMap().get(ip.getIfIndex());
                        if (CollectionUtils.isNotEmpty(entries)) {
                            String vlanName = entries.stream().map(Dot1qVlanStaticEntry::getDot1qVlanStaticName).collect(Collectors.joining(","));
                            view.setVlanName(vlanName);
                        }
                        tableViewInfos.add(view);
                    }
                    break;
                default:
                    break;
            }

            generateUUID(tableViewInfos);
            resultList.add(tableViewInfos);
        }
        return resultList;
    }

    private List<DeviceInfo> getDeviceList(ModelTableViewParam param) {
        //获取资产信息
        MwTangibleassetsDTO assetInfo = getAssetInfo(param.getAssetsId());
        if (assetInfo == null) {
            log.error("获取失败，无资产信息");
            return null;
        }
        //网络设备,安全设备类型的资产
        if(!networkDeviceType.contains(assetInfo.getAssetsTypeId())){
            log.info("transformSearchParam skip device:{}" ,assetInfo);
            return null;
        }
        //获取设备信息
        List<MwTangibleassetsDTO> assetInfos = new ArrayList<>();
        assetInfos.add(assetInfo);
        return doGetDeviceList(assetInfos);
    }

    private List<DeviceInfo> doGetDeviceList(List<MwTangibleassetsDTO> assetInfos){
        List<SearchParam> searchParams = new ArrayList<>();
        List<DeviceInfo> deviceList = null;
        try {
            for (MwTangibleassetsDTO assetInfo : assetInfos) {
                SearchParam searchParam = transformSearchParam(assetInfo);
                searchParams.add(searchParam);
            }

            SnmpSearchService snmpSearchService = SpringUtils.getBean(SnmpSearchService.class);
            SnmpSearchAction ipSnmpSearchAction = new SnmpSearchAction();
            ipSnmpSearchAction.enableIfArpMapVlan();
            deviceList = snmpSearchService.searchDeviceList(searchParams, false, ipSnmpSearchAction, true);
            if (CollectionUtils.isEmpty(deviceList)) {
                log.error("获取设备信息失败,devicelist is empty searchParam" + JSON.toJSONString(searchParams));
                return null;
            }
        } catch (Exception e) {
            log.error("getDeviceList error",e);
            return null;
        }
        return deviceList;
    }

    private List<ModelTableViewInfo> fakeList() {

        List<ModelTableViewInfo> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            ModelTableViewInfo view = new ModelTableViewInfo();
            view.setIpAddress(new Random().nextInt(255) + "." + new Random().nextInt(255) + "." + new Random().nextInt(255) + "." + new Random().nextInt(255));
            view.setMacAddress("30:30:3a:30:30:3a:30:30:3a:30:30:3a:30:30:3a:30:30");
            view.setInterfaceName("GE1/0/1");
            view.setInterfaceDesc("中文描述、英文描述");
            view.setType(String.valueOf(new Random().nextInt(5) + 1));
            view.setStatus(String.valueOf(new Random().nextInt(1) + 1));
            list.add(view);
        }
        return list;
    }

    private void generateUUID(List<ModelTableViewInfo> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        for (ModelTableViewInfo viewInfo : resultList) {
            viewInfo.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
    }

    private List<ModelTableViewInfo> filterByParams(ModelTableViewParam param, List<ModelTableViewInfo> list) {
        List<ModelTableViewInfo> resultList = new ArrayList<>();
        //优先模糊查询
        if (StringUtils.isNotEmpty(param.getFuzzyQuery())) {
            String alias = param.getFuzzyQuery();
            for (ModelTableViewInfo viewInfo : list) {
                if ((StringUtils.isNotEmpty(viewInfo.getInterfaceName()) && viewInfo.getInterfaceName().contains(alias)) ||
                        (StringUtils.isNotEmpty(viewInfo.getMacAddress()) && viewInfo.getMacAddress().contains(alias)) ||
                        (StringUtils.isNotEmpty(viewInfo.getIpAddress()) && viewInfo.getIpAddress().contains(alias)) ||
                        (StringUtils.isNotEmpty(viewInfo.getVlanName()) && viewInfo.getVlanName().contains(alias))) {
                    resultList.add(viewInfo);
                }
            }
            return resultList;
        }
        if (StringUtils.isNotEmpty(param.getMacAddress())) {
            String macAddress = param.getMacAddress();
            for (ModelTableViewInfo viewInfo : list) {
                if (StringUtils.isNotEmpty(viewInfo.getMacAddress()) && viewInfo.getMacAddress().contains(macAddress)) {
                    resultList.add(viewInfo);
                }
            }
            return resultList;
        }
        if (StringUtils.isNotEmpty(param.getVlanName())) {
            String vlanName = param.getVlanName();
            for (ModelTableViewInfo viewInfo : list) {
                if (StringUtils.isNotEmpty(viewInfo.getVlanName()) && viewInfo.getVlanName().contains(vlanName)) {
                    resultList.add(viewInfo);
                }
            }
            return resultList;
        }
        if (StringUtils.isNotEmpty(param.getInterfaceName())) {
            String interfaceName = param.getInterfaceName();
            for (ModelTableViewInfo viewInfo : list) {
                if (StringUtils.isNotEmpty(viewInfo.getInterfaceName()) && viewInfo.getInterfaceName().contains(interfaceName)) {
                    resultList.add(viewInfo);
                }
            }
            return resultList;
        }
        return list;
    }

    private void saveDB(String id ,List<List<ModelTableViewInfo>> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        ModelSnmpInfoDTO modelSnmpInfoDTO = new ModelSnmpInfoDTO();
        modelSnmpInfoDTO.setId(id);
        modelSnmpInfoDTO.extractFrom(list);
        modelSnmpInfoDTO.setCreateTime(new Date());
        mwModelSnmpInfoDao.insert(modelSnmpInfoDTO);
    }

    /**
     * 导出excel模板
     *
     * @param response 导出数据
     * @param param    数据
     */
    @Override
    public void exportResultExcel(HttpServletResponse response, ModelTableViewParam param) {
        TableViewEnum tableViewEnum = TableViewEnum.getByTypeId(param.getViewType());
        if (tableViewEnum == null) {
            log.error("获取失败，无资产信息");
            return;
        }
        List<ModelTableViewInfo> resultList = getResultList(param, tableViewEnum);
        if (CollectionUtils.isEmpty(resultList)) {
            log.error("获取失败，无设备信息" + JSON.toJSONString(param));
            return;
        }
        Set<String> includeColumnFiledNames = new HashSet<>();
        switch (tableViewEnum) {
            case ARP:
                includeColumnFiledNames.add("macAddress");
                includeColumnFiledNames.add("ipAddress");
                includeColumnFiledNames.add("type");
                includeColumnFiledNames.add("interfaceName");
                includeColumnFiledNames.add("vlanName");
                List<ModelTableViewARP> arpList = (List<ModelTableViewARP>) getList(resultList, tableViewEnum);
                exportExcel(response, includeColumnFiledNames, arpList, ModelTableViewARP.class);
                break;
            case MAC:
                includeColumnFiledNames.add("macAddress");
                includeColumnFiledNames.add("interfaceName");
                includeColumnFiledNames.add("status");
                includeColumnFiledNames.add("vlanName");
                List<ModelTableViewMAC> macList = (List<ModelTableViewMAC>) getList(resultList, tableViewEnum);
                exportExcel(response, includeColumnFiledNames, macList, ModelTableViewMAC.class);
                break;
            case INTERFACE:
                includeColumnFiledNames.add("macAddress");
                includeColumnFiledNames.add("interfaceName");
                includeColumnFiledNames.add("status");
                includeColumnFiledNames.add("vlanName");
                List<ModelTableViewIP> ipList = (List<ModelTableViewIP>) getList(resultList, tableViewEnum);
                exportExcel(response, includeColumnFiledNames, ipList, ModelTableViewIP.class);
                break;
            default:
                break;
        }


    }

    private static void exportExcel(HttpServletResponse response, Set<String> includeColumnFiledNames, List<?> list, Class dtoclass) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = ExcelUtils.getExcelWriter("exportMacInfo", response, dtoclass);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
        } catch (IOException e) {
            log.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
    }

    private List<?> getList(List<ModelTableViewInfo> resultList, TableViewEnum tableViewEnum) {
        switch (tableViewEnum) {
            case ARP:
                List<ModelTableViewARP> arpList = new ArrayList<>();
                for (ModelTableViewInfo viewInfo : resultList) {
                    ModelTableViewARP viewARP = new ModelTableViewARP();
                    viewARP.setMacAddress(viewInfo.getMacAddress());
                    viewARP.setIpAddress(viewInfo.getIpAddress());
                    viewARP.setType(viewInfo.getType());
                    viewARP.setInterfaceName(viewInfo.getInterfaceName());
                    viewARP.setVlanName(viewInfo.getVlanName());
                    arpList.add(viewARP);
                }
                return arpList;
            case MAC:
                List<ModelTableViewMAC> macList = new ArrayList<>();
                for (ModelTableViewInfo viewInfo : resultList) {
                    ModelTableViewMAC viewMAC = new ModelTableViewMAC();
                    viewMAC.setMacAddress(viewInfo.getMacAddress());
                    viewMAC.setInterfaceName(viewInfo.getInterfaceName());
                    viewMAC.setStatus(viewInfo.getStatus());
                    viewMAC.setVlanName(viewInfo.getVlanName());
                    macList.add(viewMAC);
                }
                return macList;
            case INTERFACE:
                List<ModelTableViewIP> ipList = new ArrayList<>();
                for (ModelTableViewInfo viewInfo : resultList) {
                    ModelTableViewIP viewIP = new ModelTableViewIP();
                    viewIP.setMacAddress(viewInfo.getMacAddress());
                    viewIP.setStatus(viewInfo.getStatus());
                    viewIP.setInterfaceName(viewInfo.getInterfaceName());
                    viewIP.setVlanName(viewInfo.getVlanName());
                    ipList.add(viewIP);
                }
                return ipList;
            default:
                break;
        }
        return null;
    }

    private List<ModelTableViewInfo> getResultList(ModelTableViewParam param, TableViewEnum tableViewEnum) {
        List<ModelTableViewInfo> resultList;
        String id = String.valueOf(param.getAssetsId());
        ModelSnmpInfoDTO modelSnmpInfoDTO = mwModelSnmpInfoDao.selectById(id);
        if (null != modelSnmpInfoDTO) {
            if (!modelSnmpInfoDTO.isDataEmpty()) {
                resultList = modelSnmpInfoDTO.getSnmpInfo(tableViewEnum);
            } else {
                resultList = getListFromScan(param ,id);
            }
        } else {
            resultList = getListFromScan(param ,id);
        }
        if (CollectionUtils.isNotEmpty(param.getIdList())) {
            Set<String> set = new HashSet<>(param.getIdList());
            Iterator iterator = resultList.iterator();
            while (iterator.hasNext()) {
                ModelTableViewInfo view = (ModelTableViewInfo) iterator.next();
                if (!set.contains(view.getId())) {
                    iterator.remove();
                }
            }
        }
        return resultList;
    }

    private PageInfo pageList(List resultList, ModelTableViewParam param) {
        PageInfo pageInfo = new PageInfo();
        int fromIndex = param.getPageSize() * (param.getPageNumber() - 1);
        int toIndex = param.getPageSize() * param.getPageNumber();
        if (fromIndex < 0 || fromIndex > resultList.size()) {
            fromIndex = 0;
            toIndex = param.getPageSize();
        }
        if (toIndex > resultList.size()) {
            toIndex = resultList.size();
        }
        List newList = resultList.subList(fromIndex, toIndex);
        pageInfo.setTotal(resultList.size());
        pageInfo.setList(newList);
        return pageInfo;
    }

    private SearchParam transformSearchParam(MwTangibleassetsDTO assetInfo) {

        //生成引擎和代理服务器ip映射信息
        Map<String, String> proxyIpMap = mwEngineCommonsService.genProxyIpMap(Arrays.asList(assetInfo));

        SearchParam searchParam = new SearchParam();
        searchParam.setIp(assetInfo.getInBandIp());
        String proxyServerIp = proxyIpMap.get(assetInfo.getPollingEngine());
        searchParam.setProxyServerIp(proxyServerIp);
        try {
            MwSnmpv1AssetsDTO snmpv1v2 = assetInfo.getSnmpv1AssetsDTO();
            RuleParam ruleParam = new RuleParam();
            if (null != snmpv1v2 && StringUtils.isNotEmpty(snmpv1v2.getCommunity())) {
                ruleParam.extractFromMwSnmpv1AssetsDTO(snmpv1v2);
            }

            MwSnmpAssetsDTO snmpv3 = assetInfo.getSnmpAssetsDTO();
            if (null != snmpv3 && StringUtils.isNotEmpty(snmpv3.getSecName())) {
                ruleParam.extractFromMwSnmpAssetsDTO(snmpv3);
            }

            if (null == ruleParam.getRuleType()) {
                log.error("transformSearchParam  rule type is null !!");
                return null;
            }

            searchParam.setRuleParam(ruleParam);
        } catch (Exception e) {
            log.error("transformSearchParam assetId:{},{}", assetInfo.getId(), e);
        }
        return searchParam;
    }

    private MwTangibleassetsDTO getAssetInfo(int assetsId) {
        Map map = new HashedMap();
        map.put("ids", Arrays.asList(assetsId));
        Reply reply = mwModelViewCommonService.findTopoModelAssets(MwTangibleassetsDTO.class, map);
        if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
            List<MwTangibleassetsDTO> list = (List) reply.getData();
            if (CollectionUtils.isNotEmpty(list)) {
                return list.get(0);
            }
        }
        return null;
    }

    //定时器调用方法
    public TimeTaskRresult timeTask(){
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        MwLoginUserDto mwLoginUserDto = new MwLoginUserDto();
        mwLoginUserDto.setUserId(mwUserCommonService.getAdmin());
        iLoginCacheInfo.createTimeTaskUser(mwLoginUserDto);

        //获取需要同步的所有snmp设备信息
        try {
            Reply reply = mwModelViewCommonService.findTopoModelAssetsBySNMP();
            if (null != reply && PaasConstant.RES_SUCCESS.equals(reply.getRes())) {
                List<MwTangibleassetsDTO> list = (List<MwTangibleassetsDTO>) reply.getData();
                int size = null == list?0: list.size();
                log.info("timeTask assets size:{}" ,size);
                Map<String ,String> ipIdMap = list.stream().collect(Collectors.toMap(MwTangibleassetsDTO::getInBandIp ,MwTangibleassetsDTO::getId));
                List<DeviceInfo> deviceInfos = doGetDeviceList(list);
                List<ModelSnmpInfoDTO> modelSnmpInfoDTOS = new ArrayList<>();

                for (DeviceInfo device : deviceInfos) {
                    List<List<ModelTableViewInfo>> data = doGetListFromScan(device);
                    ModelSnmpInfoDTO modelSnmpInfoDTO = new ModelSnmpInfoDTO();
                    String id = ipIdMap.get(device.getDeviceIP());
                    modelSnmpInfoDTO.extractFrom(data);
                    modelSnmpInfoDTO.setId(id);
                    modelSnmpInfoDTO.setCreateTime(new Date());
                    modelSnmpInfoDTOS.add(modelSnmpInfoDTO);
                }

                DBUtils dbUtils = new DBUtils();
                DBUtils.BatchInsert<MwModelSnmpInfoDao, ModelSnmpInfoDTO> action = (dao, data) -> {
                    dao.insert(data);
                };

                mwModelSnmpInfoDao.cleanTable();
                dbUtils.doBatchInsert(sqlSessionFactory, modelSnmpInfoDTOS, ModelSnmpInfoDTO.class, MwModelSnmpInfoDao.class
                        , 300, action);
                taskRresult.setSuccess(true);
            }
        }finally {
            iLoginCacheInfo.removeTimeTaskUser();
        }

        return taskRresult;
    }
}
