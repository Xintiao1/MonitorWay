package cn.mw.monitor.service.impl;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.link.dao.MWNetWorkLinkDao;
import cn.mw.monitor.link.dto.LinkDirectoryDetailDto;
import cn.mw.monitor.link.dto.MwLinkDropDowmDto;
import cn.mw.monitor.link.dto.MwLinkTreeDto;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.DeleteLinkParam;
import cn.mw.monitor.link.param.DropDownParam;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.label.model.MWCommonLabel;
import cn.mw.monitor.service.link.api.LinkLifeCycleListener;
import cn.mw.monitor.service.link.constant.LinkConstant;
import cn.mw.monitor.service.link.dto.MwLinkDirectoryDto;
import cn.mw.monitor.service.link.dto.MwLinkInterfaceDto;
import cn.mw.monitor.service.link.dto.MwLinkLineChartDto;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mw.monitor.service.link.param.AssetsParam;
import cn.mw.monitor.service.link.param.MwLinkCommonParam;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.tpserver.api.MwCommonsTPServer;
import cn.mw.monitor.service.user.api.*;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/7/20 11:23
 */
@Service
@Slf4j
@Transactional
public class MWNetWorkLinkServiceImpl implements MWNetWorkLinkService, MWNetWorkLinkCommonService {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/link");

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Resource
    MWNetWorkLinkDao mwNetWorkLinkDao;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MwAssetsManager mwAssetsManager;
    @Autowired
    private MWLinkZabbixManger mwLinkZabbixManger;
    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Resource
    MwLabelManageService mwLabelManageService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWUserService userService;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwCommonsTPServer mwCommonsTPServer;

    @Autowired
    private List<LinkLifeCycleListener> linkLifeCycleListeners;

    @Autowired
    private MWCommonService commonService;

    @Autowired
    private MwEngineCommonsService commonsService;

    @Autowired
    private MwTangibleAssetsService assetsService;

    @Override
    public Reply selectList(LinkDropDownParam param) {
        try {
            List<NetWorkLinkDto> newNetWorkLinkDtos = new ArrayList<>();
            if (param.getIsAdvancedQuery()) {
                MWCommonLabel commonLabel = MWCommonLabel.builder().columnName(LinkConstant.T2_LINK_ID)
                        .mapperTableName(LinkConstant.MW_LABEL_LINK_MAPPER)
                        .tableName(LinkConstant.MW_NETWORK_LINK)
                        .dropKey(param.getDropKey())
                        .labelValue(param.getLabelValue())
                        .InputFormat(param.getInputFormat()).build();
                List<String> linkIds = mwLabelManageService.getIdListByLabel(commonLabel);
                if (null != linkIds && linkIds.size() > 0) {
                    param.setLinkIds(linkIds);
                } else {
                    logger.info("LINK_LOG[]link[]链路[]查询链路{}高级查询，根据标签筛选查询，没有查到任何数据}", param);
                    PageInfo pageInfo = new PageInfo<>(newNetWorkLinkDtos);
                    pageInfo.setList(newNetWorkLinkDtos);
                    return Reply.ok(pageInfo);
                }
            }
            String parentId = param.getParentId();
            List<NetWorkLinkDto> netWorkLinkDtos;
            if(StringUtils.isNotBlank(parentId) && CollectionUtils.isEmpty(param.getLinkIds())){
                netWorkLinkDtos = new ArrayList<>();
            }else{
                netWorkLinkDtos = getNetWorkLinkDtos(param);
            }
            getLinkZabbixData(netWorkLinkDtos);
            if (netWorkLinkDtos.size() > 0) {
                /**
                 * 将所有数据整理
                 */
                long s1 = System.currentTimeMillis();
                Set<String> itemNames = new HashSet<>();
                List<String> hostIds = new ArrayList<>();
                Map mapval = new HashMap();
                Map<Integer, List<String>> serverMap = new HashMap<>();
                Map<Integer, Set<String>> serverItemsMap = new HashMap<>();
                for (NetWorkLinkDto link : netWorkLinkDtos) {
                    String scanType = link.getScanType();
                    String valuePort = link.getValuePort();
                    String baseLinkHostId = "";
                    Integer baseLinkServerId = 0;
                    String enableLinkHostId = "";
                    Integer enableLinkServerId = 0;
                    String port = "";
                    if (valuePort.equals(LinkConstant.ROOT)) {
                        baseLinkHostId = link.getRootAssetsParam().getAssetsId();
                        baseLinkServerId = link.getRootAssetsParam().getMonitorServerId() == null ? 0 : link.getRootAssetsParam().getMonitorServerId();
                        port = link.getRootPort();
                    } else {
                        baseLinkHostId = link.getTargetAssetsParam().getAssetsId();
                        baseLinkServerId = link.getTargetAssetsParam().getMonitorServerId() == null ? 0 : link.getTargetAssetsParam().getMonitorServerId();
                        port = link.getTargetPort();
                    }
                    if (link.getEnable().equals(LinkConstant.ACTIVE)) {//启用了线路探测
                        String linkTargetIp = link.getLinkTargetIp();
                        if (LinkConstant.ICMP.equals(scanType)) {//ICMP 使用目标IP/目标设备
                            MwAssetsIdsDTO mwAssetsIdsDTO = mwAssetsManager.selectAssetsByIp(linkTargetIp);
                            if (null != mwAssetsIdsDTO && null != mwAssetsIdsDTO.getHostId()) {
                                enableLinkHostId = mwAssetsIdsDTO.getHostId();
                                enableLinkServerId = mwAssetsIdsDTO.getMonitorServerId();
                            } else {
                                enableLinkHostId = link.getTargetAssetsParam().getAssetsId();
                                enableLinkServerId = link.getTargetAssetsParam().getMonitorServerId();
                            }
                        } else if (LinkConstant.NQA.equals(scanType)) {//其他使用源设备
                            enableLinkHostId = link.getRootAssetsParam().getAssetsId();
                            enableLinkServerId = link.getRootAssetsParam().getMonitorServerId();
                        } else if (LinkConstant.NQA.equals(scanType)) {//其他使用源设备
                            enableLinkHostId = link.getRootAssetsParam().getAssetsId();
                            enableLinkServerId = link.getRootAssetsParam().getMonitorServerId();
                        }
                    }
                    if (baseLinkServerId != 0 && !Strings.isNullOrEmpty(port) && !Strings.isNullOrEmpty(baseLinkHostId)) {
                        if (serverMap.containsKey(baseLinkServerId)) {
                            hostIds = serverMap.get(baseLinkServerId);
                            if (!mapval.containsKey(baseLinkHostId)) {
                                hostIds.add(baseLinkHostId);
                            }
                            serverMap.put(baseLinkServerId, hostIds);
                        } else {
                            hostIds = new ArrayList<>();
                            hostIds.add(baseLinkHostId);
                            serverMap.put(baseLinkServerId, hostIds);
                        }

                        if (serverItemsMap.containsKey(baseLinkServerId)) {
                            itemNames = serverItemsMap.get(baseLinkServerId);
                        } else {
                            itemNames = new HashSet<>();
                        }
                        itemNames.add("[" + port + "]" + LinkConstant.INTERFACE_BANDWIDTH);// 上行/下行带宽使用一个值
//                        itemNames.add("[" + port + "]" + "MW_INTERFACE_STATUS");
                        itemNames.add("[" + port + "]" + LinkConstant.MW_INTERFACE_IN_TRAFFIC); // in/out带宽利用率
                        itemNames.add("[" + port + "]" + LinkConstant.MW_INTERFACE_OUT_TRAFFIC);
                        serverItemsMap.put(baseLinkServerId, itemNames);
                    }
                    if (enableLinkServerId != 0 && !Strings.isNullOrEmpty(enableLinkHostId)) {
                        if (serverMap.containsKey(enableLinkServerId)) {
                            hostIds = serverMap.get(enableLinkServerId);
                            if (!mapval.containsKey(enableLinkHostId)) {
                                hostIds.add(enableLinkHostId);
                            }
                            serverMap.put(enableLinkServerId, hostIds);
                        } else {
                            hostIds = new ArrayList<>();
                            hostIds.add(enableLinkHostId);
                            serverMap.put(enableLinkServerId, hostIds);
                        }

                        if (serverItemsMap.containsKey(enableLinkServerId)) {
                            itemNames = serverItemsMap.get(enableLinkServerId);
                        } else {
                            itemNames = new HashSet<>();
                        }
                        serverItemsMap.put(enableLinkServerId, itemNames);
                    }
                    mapval.put(baseLinkHostId, baseLinkHostId);
                    mapval.put(enableLinkHostId, enableLinkHostId);
                    link.setBaseLinkHostId(baseLinkHostId);
                    link.setBaseLinkServerId(baseLinkServerId);
                    link.setBasePort(port);
                    link.setEnableLinkHostId(enableLinkHostId);
                    link.setEnableLinkServerId(enableLinkServerId);
                }
                long time_e = System.currentTimeMillis();
                Map<String, Map<String, Object>> linkValues = new HashMap<>();
                List<Future<Map<String, Map<String, Object>>>> lists = new ArrayList<>();
                ThreadPoolExecutor executorService = new ThreadPoolExecutor(serverMap.entrySet().size(), serverMap.entrySet().size() + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                for (Map.Entry<Integer, List<String>> entrys : serverMap.entrySet()) {
                    final Map.Entry<Integer, List<String>> entry = entrys;
                    Callable<Map<String, Map<String, Object>>> callable = new Callable<Map<String, Map<String, Object>>>() {
                        @Override
                        public Map<String, Map<String, Object>> call() throws Exception {
                            Integer k = entry.getKey();
                            List v = entry.getValue();
                            Integer serverId = k;
                            List<String> hostIdList = v;
                            Map<String, Map<String, Object>> linkValue = mwLinkZabbixManger.getLinkValue(serverId, hostIdList, new ArrayList<>(serverItemsMap.get(k)));
                            return linkValue;
                        }
                    };
                    Future<Map<String, Map<String, Object>>> submit = executorService.submit(callable);
                    lists.add(submit);
                }
                if (lists.size() > 0) {
                    lists.forEach(f -> {
                        try {
                            Map<String, Map<String, Object>> map = f.get(20, TimeUnit.SECONDS);
                            linkValues.putAll(map);
                        } catch (Exception e) {
                            log.error("查询线路数据失败，失败信息"+e.getMessage());
                        }
                    });
                }
                executorService.shutdown();
                logger.info("关闭线程池");
                long time_s = System.currentTimeMillis();
//                ////System.out.println("测试时间：" + (time_s - time_e));
                /**
                 * 查询zabbix中的数据
                 * 根据item查出上下带宽和上下带宽利用率 是根据取值端口
                 * ping的状态 时间和丢包率是根据 协议  ICMP 取得是目标hostid  其他协议取的是源hostid
                 * 线路的状态   如果启用了探测，线路状态取决于探测的结果
                 *              未启用探测，线路状态取决于选择的取数端口状态
                 */
                long time1 = System.currentTimeMillis();
                for (NetWorkLinkDto link : netWorkLinkDtos) {
                    Map<String, Object> baseMap = linkValues.get(link.getBaseLinkHostId() + "_" + link.getBaseLinkServerId());
                    String bandUnit = link.getBandUnit();//带宽单位
                    String downLinkBandwidth = link.getDownLinkBandwidth();//自定义的下行带宽
                    String upLinkBandwidth = link.getUpLinkBandwidth();//自定义的上行带宽
                    link.setInLinkBandwidthUtilization(0.0);
                    link.setOutLinkBandwidthUtilization(0.0);
                    if (null != baseMap) {
//                        if ("DISACTIVE".equals(link.getEnable())) {
//                            link.setStatus(null != baseMap.get(link.getBasePort() + "MW_INTERFACE_STATUS") ? baseMap.get(link.getBasePort() + "MW_INTERFACE_STATUS").toString() : "");
//                        }
                        //计算带宽，根据流量进行计算
                        Object inLinkFlow = baseMap.get(link.getBasePort() + LinkConstant.MW_INTERFACE_IN_TRAFFIC);
                        Object outLinkFlow = baseMap.get(link.getBasePort() + LinkConstant.MW_INTERFACE_OUT_TRAFFIC);
                        if(inLinkFlow != null){
                            String[] valueAndUnits = inLinkFlow.toString().split("_");//值与单位
                            //进行单位统一
                            String value = valueAndUnits[0];
                            String unit = valueAndUnits[1];
                            Map<String, String> valueMap = UnitsUtil.getValueMap(upLinkBandwidth, unit, bandUnit);
                            if(valueMap != null){
                                String changeValue = valueMap.get(LinkConstant.VALUE);
                                double inLinkBandwidthUtilization = 0;
                                try {
                                    inLinkBandwidthUtilization = new BigDecimal(Double.parseDouble(value) / Double.parseDouble(changeValue) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                }catch (Exception e){
                                    log.warn("selectList {}" ,e.getMessage());
                                }
                                if(inLinkBandwidthUtilization > 100){
                                    link.setInLinkBandwidthUtilization(100d);
                                }else{
                                    link.setInLinkBandwidthUtilization(inLinkBandwidthUtilization);
                                }
                            }
                        }
                        if(outLinkFlow != null){
                            String[] valueAndUnits = outLinkFlow.toString().split("_");//值与单位
                            //进行单位统一
                            String value = valueAndUnits[0];
                            String unit = valueAndUnits[1];
                            Map<String, String> valueMap = UnitsUtil.getValueMap(downLinkBandwidth, unit, bandUnit);
                            if(valueMap != null){
                                String changeValue = valueMap.get(LinkConstant.VALUE);
                                double outLinkBandwidthUtilization = 0;
                                try {
                                    outLinkBandwidthUtilization = new BigDecimal(Double.parseDouble(value) / Double.parseDouble(changeValue) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                }catch (Exception e){
                                    log.warn("selectList {}" ,e.getMessage());
                                }
                                if(outLinkBandwidthUtilization > 100){
                                    link.setOutLinkBandwidthUtilization(100d);
                                }else{
                                    link.setOutLinkBandwidthUtilization(outLinkBandwidthUtilization);
                                }
                            }
                        }
//                        link.setInLinkBandwidthUtilization(null != baseMap.get(link.getBasePort() + "IN_BANDWIDTH_UTILIZATION") ? Double.valueOf(baseMap.get(link.getBasePort() + "IN_BANDWIDTH_UTILIZATION").toString()) : 0);
//                        link.setOutLinkBandwidthUtilization(null != baseMap.get(link.getBasePort() + "OUT_BANDWIDTH_UTILIZATION") ? Double.valueOf(baseMap.get(link.getBasePort() + "OUT_BANDWIDTH_UTILIZATION").toString()) : 0);
                    }
                    Map<String, Object> enableMap = linkValues.get(link.getEnableLinkHostId() + "_" + link.getEnableLinkServerId());
                }
                long time2 = System.currentTimeMillis();
            }

            if(param.getLinkStatus() != null && param.getLinkStatus() == 1){
                List<NetWorkLinkDto> linkDtos = netWorkLinkDtos.stream().filter(netWorkLinkDto -> StringUtils.isNotBlank(netWorkLinkDto.getStatus()) && netWorkLinkDto.getStatus().equals(LinkConstant.NORMAL)).collect(Collectors.toList());
                return Reply.ok(linkPageHelper(linkDtos,param));
            }
            if(param.getLinkStatus() != null && param.getLinkStatus() == 0){
                List<NetWorkLinkDto> linkDtos = netWorkLinkDtos.stream().filter(netWorkLinkDto -> StringUtils.isNotBlank(netWorkLinkDto.getStatus()) && netWorkLinkDto.getStatus().equals(LinkConstant.ABNORMAL)).collect(Collectors.toList());
                return Reply.ok(linkPageHelper(linkDtos,param));
            }
            logger.info("LINK_LOG[]link[]链路[]查询链路{}}", param);
            return Reply.ok(linkPageHelper(netWorkLinkDtos,param));
        } catch (Exception e) {
            log.error("fail to select with selectList={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.NETWORK_LINK_SELECT_CODE_308001, ErrorConstant.NETWORK_LINK_SELECT_MSG_308001);
        }
    }


    private PageInfo linkPageHelper(List<NetWorkLinkDto> netWorkLinkDtos, LinkDropDownParam param){
        if(CollectionUtils.isNotEmpty(netWorkLinkDtos)){
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            List<NetWorkLinkDto> linkDtos = netWorkLinkDtos.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getLinkName()), pinyin4jUtil.getStringPinYin(o2.getLinkName()))).collect(Collectors.toList());
            //根据分页信息分割数据
            Integer pageNumber = param.getPageNumber();
            Integer pageSize = param.getPageSize();
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > linkDtos.size()){
                toIndex = linkDtos.size();
            }
            if(fromIndex > toIndex){
                fromIndex = 0;
            }
            List<NetWorkLinkDto> newLinkDtos = linkDtos.subList(fromIndex, toIndex);
            PageInfo pageInfo = new PageInfo<>(linkDtos);
            pageInfo.setPageSize(linkDtos.size());
            pageInfo.setList(newLinkDtos);
            pageInfo.setPageNum(linkDtos.size());
            return pageInfo;
        }
        return new PageInfo();
    }

    @Override
    public List<NetWorkLinkDto> getNetWorkLinkDtos(LinkDropDownParam param) {
        //当前用户信息
        GlobalUserInfo userInfo;
        //链路数据信息
        List<NetWorkLinkDto> netWorkLinkDtos = new ArrayList<>();
        //多线程确保session一致
        if (param.getUserId() != null) {
            userInfo = userService.getGlobalUser(param.getUserId());
        } else {
            userInfo = userService.getGlobalUser();
        }
        try {
            List<String> typeIds = userService.getAllTypeIdList(userInfo, DataType.LINK);
//            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            Map criteria = PropertyUtils.describe(param);
            criteria.put(LinkConstant.LISTSET, Joiner.on(",").join(typeIds));
            criteria.put(LinkConstant.ISSYSTEM, userInfo.isSystemUser());
            netWorkLinkDtos = mwNetWorkLinkDao.getAllLinkList(criteria);

            //获取数据权限
            List<String> idList = new ArrayList<>();
            Map<String, DataPermission> permissionMap = new HashMap<>();
            for (NetWorkLinkDto link : netWorkLinkDtos) {
                idList.add(link.getLinkId());
            }
            List<DataPermission> permissionList = commonService.getDataAuthByIds(DataType.LINK, idList);
            for (DataPermission permission : permissionList) {
                permissionMap.put(permission.getId(), permission);
            }
            for (NetWorkLinkDto link : netWorkLinkDtos) {
                DataPermission permission = permissionMap.get(link.getLinkId());
                if (permission != null){
                    link.setPrincipal(permission.getPrincipal());
                    link.setDepartment(permission.getDepartment());
                    link.setGroups(permission.getGroups());
                }
            }
        } catch (Exception e) {
            log.error("getNetWorkLinkDtos", e);
        }
        return netWorkLinkDtos;
    }

    @Override
    public Reply getLinkList() {
        List<Map<String, String>> map = mwNetWorkLinkDao.getLinkListIds();
        //获取当前登录用户信息
        GlobalUserInfo globalUser = userService.getGlobalUser();
        //根据当前登录用户查询属于该用户权限的资产ID集合
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.LINK);
        //增加当前登录用户权限过滤,去除当前登录用户没有权限的数据
        if(CollectionUtils.isNotEmpty(map) && CollectionUtils.isNotEmpty(allTypeIdList)){
            Iterator<Map<String, String>> iterator = map.iterator();
            while(iterator.hasNext()){
                Map<String, String> next = iterator.next();
                String interfaceid = next.get(LinkConstant.INTERFACEID);
                if(StringUtils.isNotBlank(interfaceid) && !allTypeIdList.contains(interfaceid)){
                    iterator.remove();
                }
            }
        }
        return Reply.ok(map);
    }

    @Override
    public Reply getBandwidth(DropDownParam param) {
        Map<String, Object> map = new HashMap<>();
        if (param.getMonitorServerId() != null) {
            map = mwLinkZabbixManger.getInterfaceBandwidth(param.getMonitorServerId(), param.getHostid(), param.getValuePort());
        }
        //设置以Mbps为单位
        if(!map.isEmpty()){
            Object upInterfaceBandwidth = map.get(LinkConstant.UP_INTERFACE_BANDWIDTH);
            Object downInterfaceBandwidth = map.get(LinkConstant.DOWN_INTERFACE_BANDWIDTH);
            Object units = map.get(LinkConstant.UNITS);
            if(upInterfaceBandwidth != null && units != null){
                String value = UnitsUtil.getValueMap(upInterfaceBandwidth.toString(), LinkConstant.UNITS_MBPS, units.toString()).get(LinkConstant.VALUE);
                map.put(LinkConstant.UP_INTERFACE_BANDWIDTH,Double.parseDouble(value));
            }else{
                map.put(LinkConstant.UP_INTERFACE_BANDWIDTH,0);
            }
            if(downInterfaceBandwidth != null && units != null){
                String value = UnitsUtil.getValueMap(downInterfaceBandwidth.toString(), LinkConstant.UNITS_MBPS, units.toString()).get(LinkConstant.VALUE);
                map.put(LinkConstant.DOWN_INTERFACE_BANDWIDTH,Double.parseDouble(value));
            }else{
                map.put(LinkConstant.DOWN_INTERFACE_BANDWIDTH,0);
            }
            map.put(LinkConstant.UNITS,LinkConstant.UNITS_MBPS);
        }
        return Reply.ok(map);
    }


    @Override
    @Transactional
    public Reply insertNetWorkLink(AddAndUpdateParam addAndUpdateParam) {

        //如果是icmp监控方式，目标设备必填
//        if (addAndUpdateParam.getEnable().equals("ACTIVE")) {
//            if (addAndUpdateParam.getScanType().equals("ICMP")) {
//                if (StringUtils.isEmpty(addAndUpdateParam.getTargetAssetsParam().getAssetsId())
//                        || StringUtils.isEmpty(addAndUpdateParam.getTargetIpAddress())
//                        || StringUtils.isEmpty(addAndUpdateParam.getTargetAssetsParam().getAssetsName())
//                        || StringUtils.isEmpty(addAndUpdateParam.getTargetPort())) {
//                    return Reply.fail(ErrorConstant.NETWORK_LINK_INSERT_CODE_308002, "ICMP监控方式目标设备信息必填");
//                }
//            }
//        }
        /**
         * 添加数据到zabbix中
         * 添加前要先查询这个模板是不是已经关联上hostid 如果关联上就不在添加
         */

//        Boolean aBoolean = true;
//        aBoolean =
        addAndUpdateParam = getaBoolean(addAndUpdateParam, true);
        if (addAndUpdateParam.isABoolean()) {

            /**
             * 添加数据到数据库表中
             */
            addAndUpdateParam.setLinkId(UuidUtil.getUid());
            addAndUpdateParam.setCreator(iLoginCacheInfo.getLoginName());
            addAndUpdateParam.setModifier(iLoginCacheInfo.getLoginName());
            mwNetWorkLinkDao.insert(addAndUpdateParam);

            //插入线路目录数据
            Integer contentsId = addAndUpdateParam.getContentsId();//目录ID
            if(contentsId != null && contentsId != 0){
                List<String> linkIds = new ArrayList<>();
                linkIds.add(addAndUpdateParam.getLinkId());
                mwNetWorkLinkDao.insertLinkIdAndTreeId(contentsId,linkIds);
            }else{
                contentsId = 0;
                //该线路自己作为根目录
                MwLinkTreeDto dto = new MwLinkTreeDto();
                dto.setLinkId(addAndUpdateParam.getLinkId());
                dto.setParentId(0);
                List<String> ids = new ArrayList<>();
                ids.add(addAndUpdateParam.getLinkId());
                dto.setLinkIds(ids);
                dto.setContentsName(addAndUpdateParam.getLinkName());
                createLinkContents(dto);
            }

            /*添加链路标签*/
            if (null != addAndUpdateParam.getAssetsLabel() && addAndUpdateParam.getAssetsLabel().size() > 0 && null != addAndUpdateParam.getAssetsLabel()) {
                mwLabelCommonServcie.insertLabelboardMapper(addAndUpdateParam.getAssetsLabel(), addAndUpdateParam.getLinkId(), DataType.LINK.getName());
            }

            /**
             * 添加权限
             */
            InsertDto insertDto = InsertDto.builder()
                    .groupIds(addAndUpdateParam.getGroupIds())
                    .userIds(addAndUpdateParam.getUserIds())
                    .orgIds(addAndUpdateParam.getOrgIds())
                    .typeId(addAndUpdateParam.getLinkId())
                    .type(DataType.LINK.getName())
                    .desc(DataType.LINK.getDesc()).build();
            //添加负责人
            mwCommonService.addMapperAndPerm(insertDto);

            for(LinkLifeCycleListener listener : linkLifeCycleListeners){
                listener.add(addAndUpdateParam);
            }
            return Reply.ok("添加线路成功");
        }
        logger.info("LINK_LOG[]link[]链路[]创建链路[]{}}", addAndUpdateParam);
        return Reply.fail(ErrorConstant.NETWORK_LINK_INSERT_CODE_308002, ErrorConstant.NETWORK_LINK_INSERT_MSG_308002);

    }

    private AddAndUpdateParam getaBoolean(AddAndUpdateParam addAndUpdateParam, Boolean aBoolean) {
        try {
            String hostid = "";
            Integer monitorServerId = 0;
            if (addAndUpdateParam.getEnable().equals(LinkConstant.ACTIVE)) {
                if (addAndUpdateParam.getScanType().equals(LinkConstant.NQA) || addAndUpdateParam.getScanType().equals(LinkConstant.IPSLA)) {//NOA绑定的是原设备的模板  ICMP绑定目标设备的模板
                    hostid = addAndUpdateParam.getRootAssetsParam().getAssetsId();
                    monitorServerId = addAndUpdateParam.getRootAssetsParam().getMonitorServerId();
                } else if (addAndUpdateParam.getScanType().equals(LinkConstant.ICMP)) {
                    String linkTargetIp = addAndUpdateParam.getLinkTargetIp();
                    MwAssetsIdsDTO mwAssetsIdsDTO = mwAssetsManager.selectAssetsByIp(linkTargetIp);
                    if (null == mwAssetsIdsDTO || null == mwAssetsIdsDTO.getHostId()) {
                        Reply reply = createICMPAssets(addAndUpdateParam);

                        Thread.sleep(1000);
                        if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                            mwAssetsIdsDTO = mwAssetsManager.selectAssetsByIp(linkTargetIp);

//                        addAndUpdateParam.setTargetAssetsName("ICMP_" + linkTargetIp);
//                        addAndUpdateParam.setTargetIpAddress(linkTargetIp);
                            hostid = mwAssetsIdsDTO.getHostId();
                            monitorServerId = mwAssetsIdsDTO.getMonitorServerId();
                        } else {
                            throw new RuntimeException("目标IP地址对应资产不存在或创建不成功 reason:" + reply.getMsg());
                        }
                    }
//                AssetsParam targetAssetsParam = addAndUpdateParam.getTargetAssetsParam();
//                targetAssetsParam.setAssetsId(mwAssetsIdsDTO.getHostId());
//                targetAssetsParam.setAssetsName("ICMP_" + linkTargetIp);
//                targetAssetsParam.setMonitorServerId(mwAssetsIdsDTO.getMonitorServerId());
//                addAndUpdateParam.setTargetAssetsId(mwAssetsIdsDTO.getHostId());
//                addAndUpdateParam.setTargetAssetsParam(targetAssetsParam);
                    hostid = mwAssetsIdsDTO.getHostId();
                    monitorServerId = mwAssetsIdsDTO.getMonitorServerId();
                    //修改轮询引擎
                    updateAssetsPollingEngine(monitorServerId,hostid,addAndUpdateParam.getPollingEngine());
                }
                aBoolean = mwLinkZabbixManger.hostMassUpdate(monitorServerId, hostid, addAndUpdateParam.getScanType());
            }
            addAndUpdateParam.setABoolean(aBoolean);
            return addAndUpdateParam;
        }catch (Throwable e){
            log.error("线路线路添加资产失败",e);
        }
       return addAndUpdateParam;
    }

    private void updateAssetsPollingEngine(Integer monitorServerId,String hostid,String pollId){
        try {
            if (pollId != null && !"".equals(pollId)) {
                MwEngineManageDTO proxyEntity = commonsService.selectEngineByIdNoPerm(pollId);
                MWZabbixAPIResult result = mwtpServerAPI.hostProxyUpdate(monitorServerId, hostid, proxyEntity.getProxyId());
                if (result.isFail()) {
                    log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                }
            } else {
                if (monitorServerId != null && monitorServerId != 0) {
                    MWZabbixAPIResult result = mwtpServerAPI.hostProxyUpdate(monitorServerId, hostid, null);
                    if (result.isFail()) {
                        log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                    }
                }
            }
            assetsService.updateAssetsPollingEngineInfo(pollId,monitorServerId,hostid);
        }catch (Throwable e){
            log.error("updateAssetsPollingEngine() error",e);
        }

    }

    /**
     * 根据ip 在主第三方监控服务器上添加ICMP 资产
     *
     * @param addAndUpdateParam
     */
    private Reply createICMPAssets(AddAndUpdateParam addAndUpdateParam) {
        AddUpdateTangAssetsParam param = new AddUpdateTangAssetsParam();
        param.setAssetsName(LinkConstant.ICMP+"_" + addAndUpdateParam.getLinkTargetIp());
        param.setHostName(param.getAssetsName());
        param.setMonitorModeName(RuleType.ICMP.getName());
        param.setMonitorMode(RuleType.ICMP.getMonitorMode());
        Integer serverId = addAndUpdateParam.getMonitorServerId();
        if (serverId == null || serverId == 0) {
            Reply reply = mwCommonsTPServer.selectByMainServer();
            if (reply.getData() != null && (int) reply.getData() != 0) {
                param.setMonitorServerId((int) reply.getData());
            } else {
                return Reply.fail("无监控服务器，请先创建监控服务器");
            }
        } else { //用户选的
            param.setMonitorServerId(serverId);
            param.setPollingEngine(addAndUpdateParam.getPollingEngine());
        }

        Reply templateListByMode = mwModelViewCommonService.getTemplateListByMode(param);
        if (null != templateListByMode && templateListByMode.getRes() == PaasConstant.RES_SUCCESS) {
            List<MwAssetsTemplateDTO> templateList = (List<MwAssetsTemplateDTO>) templateListByMode.getData();
            if (templateList != null && templateList.size() > 0) {
                //取第一个作为模板
                MwAssetsTemplateDTO template = templateList.get(0);
                param.setAssetsTypeId(template.getAssetsTypeId());
                param.setAssetsTypeSubId(template.getSubAssetsTypeId());
                param.setManufacturer(template.getBrand());
                param.setHostGroupId(template.getGroupId());
                param.setSpecifications(template.getSpecification());
                param.setDescription(template.getDescription());
                param.setTemplateId(template.getTemplateId());
                param.setMonitorFlag(true);
                param.setSettingFlag(false);
            } else {
                return Reply.fail("无ICMP相关模板，请先创建相关模板");
            }
        }

        param.setInBandIp(addAndUpdateParam.getLinkTargetIp());
        try {
            return mwModelViewCommonService.insertAssets(param, false);
        } catch (Throwable throwable) {
            logger.error("createICMPAssets" ,throwable);
            return Reply.fail("创建ICMP资产失败 reason:{}", throwable.getMessage());
        }
    }


    @Override
    public Reply getAssetsList(DropDownParam dropDownParam) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
            mwCommonAssetsDto.setUserId(userId);
            Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
            List<Map<String, Object>> assetsNameList = new ArrayList<>();
            List<MwTangibleassetsTable> list = new ArrayList<>();
            if (null != assets) {
                Object assetsList = assets.get(LinkConstant.ASSETSLIST);
                if (null != assetsList) {
                    list = (List<MwTangibleassetsTable>) assetsList;
                    list.forEach(data -> {
                        if (data.getAssetsId() != null && StringUtils.isNotEmpty(data.getAssetsId())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(LinkConstant.ASSETSID, data.getAssetsId());
                            map.put(LinkConstant.ASSETSNAME, data.getAssetsName());
                            map.put(LinkConstant.MONITOR_SERVER_ID, data.getMonitorServerId());
                            assetsNameList.add(map);
                        }
                    });
                }
            }
            logger.info("LINK_LOG[]link[]链路[]查询可以添加的资产{}}", dropDownParam);
            return Reply.ok(assetsNameList);
        } catch (Exception e) {
            log.error("fail to select with getAssetsList={}, cause:{}", dropDownParam, e);
            return Reply.fail(ErrorConstant.NETWORK_LINK_SELECT_ASSETS_CODE_308003, ErrorConstant.NETWORK_LINK_SELECT_ASSETS_MSG_308003);
        }
    }


    @Override
    public Reply getIpAddressList(DropDownParam dropDownParam) {
        try {
            /**
             * 从zabbix 查询ip地址数据
             */
            List<Object> ipAddressList = new ArrayList<>();
            if (dropDownParam.getIsGetByZabbix()) {
                if (null != dropDownParam.getMonitorServerId() && null != dropDownParam.getItemName() && StringUtils.isNotEmpty(dropDownParam.getHostid())) {
                    ipAddressList = mwLinkZabbixManger.getIpAddressList(dropDownParam.getMonitorServerId(), dropDownParam.getItemName(), dropDownParam.getHostid());
                }
            } else {
                boolean isICMP = false;
                // 从数据中查询ip地址
                Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
                MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
                //多zabbixServer assetsId不唯一
                if (dropDownParam.getMonitorModeName() != null && RuleType.valueOf(dropDownParam.getMonitorModeName()) == RuleType.ICMP) {//查询对应监控方式ICMP 的ip地址
                    mwCommonAssetsDto.setMonitorMode(RuleType.ICMP.getMonitorMode());
                    isICMP = true;
                } else {
                    mwCommonAssetsDto.setAssetsId(dropDownParam.getHostid());
                    mwCommonAssetsDto.setId(dropDownParam.getId());
                }
                mwCommonAssetsDto.setUserId(userId);
                Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
                List<MwTangibleassetsTable> list = new ArrayList<>();
                if (null != assets) {
                    Object assetsList = assets.get(LinkConstant.ASSETSLIST);
                    if (null != assetsList) {

                        if (isICMP) {
                            ipAddressList = (List<Object>) assetsList;
                        } else {
                            list = (List<MwTangibleassetsTable>) assetsList;
                            for (MwTangibleassetsTable data : list) {
                                ipAddressList.add(data.getInBandIp());
                            }
                        }
                    }
                }
            }

            logger.info("LINK_LOG[]link[]链路[]查询可以添加的ip地址{}}", dropDownParam);
            return Reply.ok(ipAddressList);
        } catch (Exception e) {
            log.error("fail to select with getIpAddressList={}, cause:{}", dropDownParam, e);
            return Reply.fail(ErrorConstant.NETWORK_LINK_SELECT_IP_ADDRESS_CODE_308034, ErrorConstant.NETWORK_LINK_SELECT_IP_ADDRESS_MSG_308004);
        }
    }

    @Override
    @Transactional
    public Reply editorNetWorkLink(AddAndUpdateParam addAndUpdateParam) {

        //如果是icmp监控方式，目标设备必填
//        if (addAndUpdateParam.getEnable().equals("ACTIVE")) {
//            if (addAndUpdateParam.getScanType().equals("ICMP")) {
//                if (StringUtils.isEmpty(addAndUpdateParam.getTargetAssetsParam().getAssetsId())
//                        || StringUtils.isEmpty(addAndUpdateParam.getTargetIpAddress())
//                        || StringUtils.isEmpty(addAndUpdateParam.getTargetAssetsParam().getAssetsName())
//                        || StringUtils.isEmpty(addAndUpdateParam.getTargetPort())) {
//                    return Reply.fail(ErrorConstant.NETWORK_LINK_INSERT_CODE_308002, "ICMP监控方式目标设备信息必填");
//                }
//            }
//        }

        /**
         * 修改zabbix
         * 判斷
         * 这个新资产是否已经关联了模板，如果关联了就不在关联，没有关联就需要关联
         */


//            NetWorkLinkDto dto = mwNetWorkLinkDao.selectNetWorkLinkDto(addAndUpdateParam.getLinkId());
//            String oldHostid = "";
//            Integer oldmonitorServerId = 0;
//            if (dto.getValuePort().equals("ROOT")) {
//                oldHostid = dto.getRootAssetsId();
//                oldmonitorServerId = dto.getRootServerId();
//            } else {
//                oldHostid = dto.getTargetAssetsId();
//                oldmonitorServerId = dto.getTargetServerId();
//
//            }
//            if (null != addAndUpdateParam.getScanType()) {
//                Boolean oldBoolean = getBoolean(oldmonitorServerId, oldHostid, addAndUpdateParam.getScanType());
//                if (!oldBoolean) {
//                    return Reply.fail(ErrorConstant.NETWORK_LINK_EDITOR_CODE_308005, ErrorConstant.NETWORK_LINK_EDITOR_MSG_308005);
//                }
//            }

//        Boolean aBoolean = true;
//        aBoolean
        addAndUpdateParam = getaBoolean(addAndUpdateParam, true);
        if (addAndUpdateParam.isABoolean()) {
            addAndUpdateParam.setModifier(iLoginCacheInfo.getLoginName());
            mwNetWorkLinkDao.update(addAndUpdateParam);

            /**
             * 修改标签
             * 删除后添加
             */

            //修改线路目录数据
            Integer contentsId = addAndUpdateParam.getContentsId();
            String linkId = addAndUpdateParam.getLinkId();
            mwNetWorkLinkDao.deleteLinkId(linkId);
            List<String> ids = new ArrayList<>();
            ids.add(linkId);
            mwNetWorkLinkDao.insertLinkIdAndTreeId(contentsId,ids);


            //删除标签参数
            mwLabelCommonServcie.deleteLabelBoard(addAndUpdateParam.getLinkId(), DataType.LINK.getName());

            /*添加链路标签*/
            if (null != addAndUpdateParam.getAssetsLabel() && addAndUpdateParam.getAssetsLabel().size() > 0) {
                mwLabelCommonServcie.insertLabelboardMapper(addAndUpdateParam.getAssetsLabel(), addAndUpdateParam.getLinkId(), DataType.LINK.getName());
            }

            /**
             * 修改权限
             */
            DeleteDto deleteDto = DeleteDto.builder().typeId(addAndUpdateParam.getLinkId()).type(DataType.LINK.getName()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);
            InsertDto insertDto = InsertDto.builder()
                    .groupIds(addAndUpdateParam.getGroupIds())
                    .userIds(addAndUpdateParam.getUserIds())
                    .orgIds(addAndUpdateParam.getOrgIds())
                    .typeId(addAndUpdateParam.getLinkId())
                    .type(DataType.LINK.getName())
                    .desc(DataType.LINK.getDesc()).build();
            //添加负责人
            mwCommonService.addMapperAndPerm(insertDto);

            for(LinkLifeCycleListener listener : linkLifeCycleListeners){
                listener.modify(addAndUpdateParam);
            }

            logger.info("LINK_LOG[]link[]链路[]修改链路{}}", addAndUpdateParam);
            return Reply.ok("编辑线路成功");
        }
        log.error("fail to update with editorNetWorkLink={}, cause:{}", addAndUpdateParam);
        return Reply.fail(ErrorConstant.NETWORK_LINK_EDITOR_CODE_308005, ErrorConstant.NETWORK_LINK_EDITOR_MSG_308005);
    }

    @Override
    public Reply deleteNetWorkLink(DeleteLinkParam dParam) {
        try {
            List<String> linkIds = new ArrayList<>();
            List<AddAndUpdateParam> addAndUpdateParams = dParam.getAddAndUpdateParams();

            for(AddAndUpdateParam addAndUpdateParam:addAndUpdateParams){
                linkIds.add(addAndUpdateParam.getLinkId());


                if (dParam.isDeleteICMPFlag() && LinkConstant.ICMP.equals(addAndUpdateParam.getScanType())) {//做删除ICMP资产操作
                    AssetsParam targetAssetsParam = addAndUpdateParam.getTargetAssetsParam();
                    if (targetAssetsParam != null) {
                        Object ret = mwModelViewCommonService.selectByAssetsIdAndServerId(targetAssetsParam.getAssetsId(), targetAssetsParam.getMonitorServerId());
                        if (null != ret) {
                            mwModelViewCommonService.deleteNetworkLinkAsset(ret ,targetAssetsParam);
                        }
                    }
                }
            }

            deleteMappers(linkIds);
            //删除线路时删除目录数据
            if(CollectionUtils.isNotEmpty(linkIds)){
                mwNetWorkLinkDao.deleteTreeLinkIds(linkIds);

                for(LinkLifeCycleListener listener : linkLifeCycleListeners){
                    listener.delete(linkIds);
                }
            }
            logger.info("LINK_LOG[]link[]链路[]删除链路{}}", addAndUpdateParams);
            return Reply.ok("删除线路成功");
        } catch (Exception e) {
            log.error("fail to update with editorNetWorkLink={}, cause:{}", dParam, e);
            return Reply.fail(ErrorConstant.NETWORK_LINK_DELETE_CODE_308006, ErrorConstant.NETWORK_LINK_DELETE_MSG_308006);
        }
    }

    @Override
    public void deleteMappers(List<String> linkIds) {
        mwNetWorkLinkDao.delete(linkIds);
        DeleteDto deleteDto = DeleteDto.builder().typeIds(linkIds).type(DataType.LINK.getName()).build();
        mwCommonService.deleteMapperAndPerms(deleteDto);
        //批量删除标签
        mwLabelCommonServcie.deleteLabelBoards(linkIds, DataType.LINK.getName());
    }

    @Override
    public Reply selectLink(String linkId) {
        try {
            NetWorkLinkDto netWorkLinkDto = mwNetWorkLinkDao.selectById(linkId);
            //获取数据权限
            DataPermission dataPermission = commonService.getDataPermissionDetail(DataType.LINK, linkId);
            netWorkLinkDto.setPrincipal(dataPermission.getPrincipal());
            netWorkLinkDto.setDepartment(dataPermission.getDepartment());
            netWorkLinkDto.setGroups(dataPermission.getGroups());
            //根据linkId查询目录ID
            Integer treeId = mwNetWorkLinkDao.selectTreeId(linkId);
            if(treeId != null){
                netWorkLinkDto.setContentsId(treeId);
            }
            //查询资产的标签
            List<MwAssetsLabelDTO> labelBoard = mwLabelCommonServcie.getLabelBoard(linkId, DataType.LINK.getName());
            netWorkLinkDto.setAssetsLabel(labelBoard);

            // usergroup重新赋值使页面可以显示
            List<Integer> groupIds = new ArrayList<>();
            netWorkLinkDto.getGroups().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            netWorkLinkDto.setGroupIds(groupIds);
            // user重新赋值
            List<Integer> userIds = new ArrayList<>();
            netWorkLinkDto.getPrincipal().forEach(
                    userDTO -> userIds.add(userDTO.getUserId())
            );
            netWorkLinkDto.setUserIds(userIds);
            // 机构重新赋值使页面可以显示

            List<List<Integer>> orgNodes = new ArrayList<>();
            if (null != netWorkLinkDto.getDepartment() && netWorkLinkDto.getDepartment().size() > 0) {
                netWorkLinkDto.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                netWorkLinkDto.setOrgIds(orgNodes);
            }
            logger.info("LINK_LOG[]link[]链路[]编辑链路前查询{}}", linkId);

            if (netWorkLinkDto.getLinkTargetIp() != null && StringUtils.isNotEmpty(netWorkLinkDto.getLinkTargetIp())) {
                MwAssetsIdsDTO mwAssetsIdsDTO = mwAssetsManager.selectAssetsByIp(netWorkLinkDto.getLinkTargetIp());
                if (mwAssetsIdsDTO != null) {
                    netWorkLinkDto.setMonitorServerId(mwAssetsIdsDTO.getMonitorServerId());
                    netWorkLinkDto.setPollingEngine(mwAssetsIdsDTO.getPollingEngine());
                }
            }
            return Reply.ok(netWorkLinkDto);
        } catch (Exception e) {
            log.error("fail to selectById with d={}, cause:{}", linkId, e);
            return Reply.fail(ErrorConstant.NETWORK_LINK_SELECT_IP_ADDRESS_CODE_308034, ErrorConstant.NETWORK_LINK_SELECT_IP_ADDRESS_MSG_308004);
        }
    }

    @Override
    public Reply enableActive(String enable, String linkId) {
        try {
            NetWorkLinkDto dto = mwNetWorkLinkDao.selectNetWorkLinkDto(linkId);
            if (null != dto.getScanType() && StringUtils.isNotEmpty(dto.getScanType()) && null != dto.getLinkTargetIp() && StringUtils.isNotEmpty(dto.getLinkTargetIp())) {
                mwNetWorkLinkDao.enableActive(enable, linkId);
                return Reply.ok("修改成功");
            } else {
                //如果是icmp监控方式，目标设备必填
                if (LinkConstant.ACTIVE.equals(dto.getEnable())) {
                    if (LinkConstant.ICMP.equals(dto.getScanType())) {

                        if (StringUtils.isEmpty(dto.getTargetAssetsId())
//                                || StringUtils.isEmpty(dto.getTargetIpAddress())
//                                || StringUtils.isEmpty(dto.getTargetAssetsName())
                                || StringUtils.isEmpty(dto.getTargetPort())) {
                            return Reply.fail(ErrorConstant.NETWORK_ENABLE_ACTIVE_CODE_308007, "ICMP监控方式目标设备信息必填");
                        }
                    }
                }
                return Reply.fail(ErrorConstant.NETWORK_ENABLE_ACTIVE_CODE_308007, "当前线路未选择监控方式,请先在编辑中选择");
            }
        } catch (Exception e) {
            log.error("fail to enableActive with d={}, cause:{}", enable, e);
            return Reply.fail(ErrorConstant.NETWORK_ENABLE_ACTIVE_CODE_308007, ErrorConstant.NETWORK_ENABLE_ACTIVE_MSG_308007);
        }
    }


    private Boolean getBoolean(Integer monitorServerId, String hostid, String scanType) {
        Boolean aBoolean = true;
        /**
         * 判断链路中是否还有其它的资产关联了这个模板 ==0则没有，可以取消该模板
         */
        int count = mwNetWorkLinkDao.selectHostIdCount(hostid, scanType);
        if (count == 0) {
            aBoolean = mwLinkZabbixManger.hostMassRemove(monitorServerId, hostid, scanType);
        }
        return aBoolean;

    }

    /**
     * web监测模糊搜索所有字段联想
     *
     * @param value
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(String value) {

        //根据值模糊查询数据
//        List<Map<String, String>> fuzzSeachAllFileds = mwNetWorkLinkDao.fuzzSearchAllFiled(value);
        List<NetWorkLinkDto> workLinkDtos = mwNetWorkLinkDao.linkFuzzSearchAllFiled(value);
        Set<String> fuzzSeachData = new HashSet<>();
        if (!cn.mwpaas.common.utils.CollectionUtils.isEmpty(workLinkDtos)) {
            for (NetWorkLinkDto linkDto : workLinkDtos) {

                String link_name = linkDto.getLinkName();
                String root_ip_address = linkDto.getRootIpAddress();
                String root_port = linkDto.getRootPort();
                String target_ip_address = linkDto.getTargetIpAddress();
                String target_port = linkDto.getTargetPort();
                String link_target_ip = linkDto.getLinkTargetIp();
                String root_assets_name = linkDto.getRootAssetsName();
                String target_assets_name = linkDto.getTargetAssetsName();
                String creator = linkDto.getCreator();
                String modifier = linkDto.getModifier();

                if (!Strings.isNullOrEmpty(link_name) && link_name.contains(value)) {
                    fuzzSeachData.add(link_name);
                }
                if (!Strings.isNullOrEmpty(root_ip_address) && root_ip_address.contains(value)) {
                    fuzzSeachData.add(root_ip_address);
                }

                if (!Strings.isNullOrEmpty(root_port) && root_ip_address.contains(root_port)) {
                    fuzzSeachData.add(root_port);
                }
                if (!Strings.isNullOrEmpty(target_ip_address) && target_ip_address.contains(value)) {
                    fuzzSeachData.add(target_ip_address);
                }
                if (!Strings.isNullOrEmpty(target_port) && target_port.contains(value)) {
                    fuzzSeachData.add(target_port);
                }
                if (!Strings.isNullOrEmpty(link_target_ip) && link_target_ip.contains(value)) {
                    fuzzSeachData.add(link_target_ip);
                }
                if (!Strings.isNullOrEmpty(root_assets_name) && root_assets_name.contains(value)) {
                    fuzzSeachData.add(root_assets_name);
                }
                if (!Strings.isNullOrEmpty(target_assets_name) && target_assets_name.contains(value)) {
                    fuzzSeachData.add(target_assets_name);
                }
                if (!Strings.isNullOrEmpty(creator) && creator.contains(value)) {
                    fuzzSeachData.add(creator);
                }
                if (!Strings.isNullOrEmpty(modifier) && modifier.contains(value)) {
                    fuzzSeachData.add(modifier);
                }

            }
        }
        Map<String, Set<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put(LinkConstant.FUZZY_QUERY, fuzzSeachData);
        return Reply.ok(fuzzyQuery);
    }

    @Override
    public List<NetWorkLinkDto>  seleAllLink() {
        List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkDao.getAllLink(); ;

        return netWorkLinkDtos;
    }

    /**
     * 查询树状结构数据
     * @return
     */
    @Override
    public Reply selectLinkTree() {
        try {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.LINK);
            Map map = new HashMap();
            map.put("isSystem",globalUser.isSystemUser());
            map.put("listSet",Joiner.on(",").join(allTypeIdList));
            List<MwLinkTreeDto> mwLinkTreeDtos = mwNetWorkLinkDao.selectLinkTree(map);
            List<Map<String, Object>> idMaps = mwNetWorkLinkDao.selectLinkIdAndTreeId();
            if(CollectionUtils.isEmpty(mwLinkTreeDtos))return Reply.ok(mwLinkTreeDtos);
            //处理id数据
            handleLinkIdAndTreeId(mwLinkTreeDtos,idMaps);
            //查询线路数据，增加到树结构中
//            LinkDropDownParam param = new LinkDropDownParam();
//            param.setIsAdvancedQuery(false);
//            param.setPageNumber(1);
//            param.setPageSize(100000);
//            param.setUserId(globalUser.getUserId());
//            Reply reply = selectList(param);
//            //处理线路数据，将数据加入MAP
//            Map<String,NetWorkLinkDto> linkMap = new HashMap<>();
//            if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS)return Reply.ok(mwLinkTreeDtos);
//
//            PageInfo pageInfo = (PageInfo) reply.getData();
//            if(pageInfo == null)return Reply.ok(mwLinkTreeDtos);
//
//
//            if(CollectionUtils.isNotEmpty(netWorkLinkDtos)){
//                for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
//                    String linkId = netWorkLinkDto.getLinkId();
//                    linkMap.put(linkId,netWorkLinkDto);
//                }
//            }
            Map<String,NetWorkLinkDto> linkMap = new HashMap<>();
            recursionHandleTree(mwLinkTreeDtos,linkMap);
            if(CollectionUtils.isNotEmpty(mwLinkTreeDtos)){
                for (MwLinkTreeDto mwLinkTreeDto : mwLinkTreeDtos) {
                    List<String> linkIds = mwLinkTreeDto.getLinkIds();
                    Set<String> set = new HashSet<>();
                    setLinkIds(mwLinkTreeDto,set);
                    List<String> clinkIds = new ArrayList<>(set);
                    if(CollectionUtils.isEmpty(linkIds)){
                        mwLinkTreeDto.setLinkIds(clinkIds);
                    }else{
                        linkIds.addAll(clinkIds);
                        mwLinkTreeDto.setLinkIds(linkIds);
                    }
                    mwLinkTreeDto.setCount(mwLinkTreeDto.getLinkIds().size());
                }
            }
            return Reply.ok(mwLinkTreeDtos);
        }catch (Exception e){
            log.error("查询线路树状结构失败，失败信息："+e);
            return Reply.fail("查询线路树状结构失败，失败信息："+e.getMessage());
        }
    }

    /**
     * 创建线路目录
     * @param mwLinkTreeDto 目录数据
     * @return
     */
    @Override
    public Reply createLinkContents(MwLinkTreeDto mwLinkTreeDto) {
        try {
            List<Integer> userGroupIds = mwLinkTreeDto.getGroupIds();
            List<Integer> userIds = mwLinkTreeDto.getUserIds();
            List<List<Integer>> orgIds = mwLinkTreeDto.getOrgIds();
            if(CollectionUtils.isNotEmpty(userGroupIds)){
                mwLinkTreeDto.setUserGroupIdStr(JSON.toJSON(userGroupIds).toString());
            }
            if(CollectionUtils.isNotEmpty(userIds)){
                mwLinkTreeDto.setUserIdStr(JSON.toJSON(userIds).toString());
            }
            if(CollectionUtils.isNotEmpty(orgIds)){
                mwLinkTreeDto.setOrgIdStr(JSON.toJSON(orgIds).toString());
            }
            List<String> linkIds = mwLinkTreeDto.getLinkIds();
            //添加目录数据
            mwNetWorkLinkDao.insertLinkTree(mwLinkTreeDto);
            //添加目录下的线路数据
            Integer id = mwLinkTreeDto.getId();
            addMapperAndPerm(mwLinkTreeDto);
            if(CollectionUtils.isEmpty(linkIds) || mwLinkTreeDto.getId() == null) return Reply.ok("添加成功");
            mwNetWorkLinkDao.insertLinkIdAndTreeId(id,linkIds);
            addMapperAndPerm(mwLinkTreeDto);
            return Reply.ok("添加成功");
        }catch (Exception e){
            log.error("添加线路树状结构目录失败，失败信息："+e);
            return Reply.fail("添加线路树状结构目录失败，失败信息："+e.getMessage());
        }
    }


    private void addMapperAndPerm(MwLinkTreeDto mwLinkTreeDto){
        InsertDto insertDto = InsertDto.builder()
                .groupIds(mwLinkTreeDto.getGroupIds())  //用户组
                .userIds(mwLinkTreeDto.getUserIds())  //责任人
                .orgIds(mwLinkTreeDto.getOrgIds())      //机构
                .typeId(String.valueOf(mwLinkTreeDto.getId())) //数据主键
                .type(DataType.LINK.getName())        //链路
                .desc(DataType.LINK.getDesc()).build(); //链路
        mwCommonService.addMapperAndPerm(insertDto);
    }


    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param auParam
     */
    private void deleteMapperAndPerm(MwLinkTreeDto auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.LINK.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 修改线路目录数据
     * @param mwLinkTreeDtos 目录数据
     * @return
     */
    @Override
    public Reply updateLinkContents(List<MwLinkTreeDto> mwLinkTreeDtos) {
        try {
            for (MwLinkTreeDto mwLinkTreeDto : mwLinkTreeDtos) {
                List<Integer> userGroupId = mwLinkTreeDto.getGroupIds();
                List<Integer> userId = mwLinkTreeDto.getUserIds();
                List<List<Integer>> orgIds = mwLinkTreeDto.getOrgIds();
                if(CollectionUtils.isNotEmpty(userGroupId)){
                    mwLinkTreeDto.setUserGroupIdStr(JSON.toJSON(userGroupId).toString());
                }
                if(CollectionUtils.isNotEmpty(userId)){
                    mwLinkTreeDto.setUserIdStr(JSON.toJSON(userId).toString());
                }
                if(CollectionUtils.isNotEmpty(orgIds)){
                    mwLinkTreeDto.setOrgIdStr(JSON.toJSON(orgIds).toString());
                }
                List<String> linkIds = mwLinkTreeDto.getLinkIds();
                //添加目录数据
                mwNetWorkLinkDao.updateLinkTree(mwLinkTreeDto);
                if(CollectionUtils.isEmpty(linkIds) || mwLinkTreeDto.getId() == null) continue;
                //添加目录下的线路数据
                Integer id = mwLinkTreeDto.getId();
                //先删除原有关联关系，在添加新的关联关系
                mwNetWorkLinkDao.deleteLinkIdAndTreeId(id);
                mwNetWorkLinkDao.insertLinkIdAndTreeId(id,linkIds);
                deleteMapperAndPerm(mwLinkTreeDto);
                addMapperAndPerm(mwLinkTreeDto);
            }
            return Reply.ok("修改成功");
        }catch (Exception e){
            log.error("修改线路树状结构目录失败，失败信息："+e);
            return Reply.fail("修改线路树状结构目录失败，失败信息："+e.getMessage());
        }
    }

    /**
     * 删除线路目录数据
     * @param mwLinkTreeDto 目录数据
     * @return
     */
    @Override
    public Reply deleteLinkContents(MwLinkTreeDto mwLinkTreeDto) {
        try {
            //目录ID
            Integer id = mwLinkTreeDto.getId();
            if(id != null){
                //先查询删除目录下的子目录ID
                List<Integer> ids = mwNetWorkLinkDao.selectLinkTreeId(id);
                if(CollectionUtils.isEmpty(ids)){
                    ids = new ArrayList<>();
                    ids.add(id);
                }else{
                    ids.add(id);
                }
                mwNetWorkLinkDao.deleteLinkTree(id);
                mwNetWorkLinkDao.deleteLinkTreeIds(ids);
                deleteMapperAndPerm(mwLinkTreeDto);
            }
            return Reply.ok("删除成功");
        }catch (Exception e){
            log.error("删除线路树状结构目录失败，失败信息："+e);
            return Reply.fail("删除线路树状结构目录失败，失败信息："+e.getMessage());
        }
    }

    /**
     * 查询线路下拉数据
     * @return
     */
    @Override
    public Reply selectLinkDropDown() {
        try {
            List<Map<String, String>> maps = mwNetWorkLinkDao.selectLinkIdAndName();
            return Reply.ok(maps);
        }catch (Exception e){
            log.error("查询线路下拉数据失败，失败信息："+e);
            return Reply.fail("查询线路下拉数据失败，失败信息："+e.getMessage());
        }
    }

    @Override
    public Reply dragLinkContents(MwLinkTreeDto mwLinkTreeDto) {
        try {
            int type = mwLinkTreeDto.getType();
            String originId = mwLinkTreeDto.getOriginId();
            String targetId = mwLinkTreeDto.getTargetId();
            if(type == 0){//拖动的是目录
                //更改目录结构
                if(StringUtils.isBlank(targetId)){
                    targetId = LinkConstant.TARGETID;
                }
                mwNetWorkLinkDao.updateLinkTreeParentId(Integer.parseInt(originId),Integer.parseInt(targetId));
            }else if(StringUtils.isNotBlank(originId) && StringUtils.isNotBlank(targetId) && type == 1){//拖动的是线路
                //删除原有的线路关系
                mwNetWorkLinkDao.deleteLinkId(originId);
                List<String> list = new ArrayList<>();
                list.add(originId);
                mwNetWorkLinkDao.insertLinkIdAndTreeId(Integer.parseInt(targetId),list);
            }else{
                //添加根目录数据
                GlobalUserInfo globalUser = userService.getGlobalUser();
                MwLinkTreeDto dto = new MwLinkTreeDto();
                dto.setLinkId(originId);
                dto.setParentId(0);
                List<String> ids = new ArrayList<>();
                ids.add(originId);
                dto.setLinkIds(ids);
                //查询线路数据
                LinkDropDownParam param = new LinkDropDownParam();
                param.setIsAdvancedQuery(false);
                param.setPageNumber(1);
                param.setPageSize(Integer.MAX_VALUE);
                param.setUserId(globalUser.getUserId());
                param.setLinkId(originId);
                Reply reply = selectList(param);
                //处理线路数据，将数据加入MAP
                Map<String,NetWorkLinkDto> linkMap = new HashMap<>();
                PageInfo pageInfo = (PageInfo) reply.getData();
                List<NetWorkLinkDto> netWorkLinkDtos = pageInfo.getList();
                dto.setContentsName(netWorkLinkDtos.get(0).getLinkName());
                createLinkContents(dto);
            }
            return Reply.ok("成功");
        }catch (Exception e){
            log.error("拖动线路目录失败，失败信息："+e);
            return Reply.fail("拖动线路目录失败，失败信息："+e.getMessage());
        }
    }

    /**
     * 查询目录下拉数据
     * @return
     */
    @Override
    public Reply getLinkTreeDropDown() {
        try {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.LINK);
            List<Map<String, Object>> maps = mwNetWorkLinkDao.selectLinkTreeDropDown(allTypeIdList);
            return Reply.ok(maps);
        }catch (Exception e){
            log.error("查询线路目录下拉数据失败，失败信息："+e);
            return Reply.fail("查询线路目录下拉数据失败，失败信息："+e.getMessage());
        }
    }



    /**
     * 查询线路趋势
     * @param linkDto
     * @return
     */
    @Override
    public Reply getLinkTrendData(NetWorkLinkDto linkDto) {
        try {
            List realData = new ArrayList();
            //探测类型
            String hostId = "";
            Integer serverId = null;
            List<String> itemNames = new ArrayList<>();
            Map<String, Object> itemAndHostId = getItemAndHostId(linkDto, hostId, serverId, itemNames);
            log.info("线路折线图查询"+itemAndHostId);
            if((itemAndHostId.isEmpty() || itemAndHostId.get(LinkConstant.HOST_ID) == null || itemAndHostId.get(LinkConstant.SERVER_ID) == null) && linkDto.getScanType().equals(LinkConstant.ICMP))return Reply.ok(realData);
            hostId = itemAndHostId.get(LinkConstant.HOST_ID).toString();
            serverId = Integer.parseInt(itemAndHostId.get(LinkConstant.SERVER_ID).toString());
            //进行时间转换
            List<Long> time = getTime(linkDto);
            //先查询zabbix上历史记录的itemId
            Map<String,String> itemMap = new HashMap<>();
            Map<Integer,List<String>> itemIdMaps = new HashMap<>();
            Map<String,String> unitsMap = new HashMap<>();
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(serverId, itemNames, hostId);
            log.info("查询最新数据监控项"+itemNames+":hostID为"+hostId+":"+serverId+"，返回结果为"+mwZabbixAPIResult);
            if (!mwZabbixAPIResult.isFail()) {
                JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                if (jsonNode != null && jsonNode.size() > 0){
                    for (JsonNode node : jsonNode) {
                        String itemid = node.get(LinkConstant.ITEM_ID).asText();
                        String name = node.get(LinkConstant.NAME).asText();
                        int valueType = node.get(LinkConstant.VALUE_TYPE).asInt();
                        String units = node.get(LinkConstant.UNITS).asText();
                        if(StringUtils.isBlank(itemid) || StringUtils.isBlank(name) || StringUtils.isBlank(units))continue;
                        if(itemIdMaps.containsKey(valueType)){
                            List<String> itemIds = itemIdMaps.get(valueType);
                            itemIds.add(itemid);
                            itemIdMaps.put(valueType,itemIds);
                        }else{
                            List<String> itemIds = new ArrayList<>();
                            itemIds.add(itemid);
                            itemIdMaps.put(valueType,itemIds);
                        }
                        itemMap.put(itemid,name);
                        unitsMap.put(itemid,units);
                    }
                }
            }
            //查询历史记录
            Map<String, List<MWItemHistoryDto>> historyData = getHistoryData(serverId, itemIdMaps, time);
            //数据处理并计算
            realData = handleHistoryData(historyData, itemMap, linkDto, unitsMap);
            //进行历史数据处理
            return Reply.ok(realData);
        }catch (Exception e){
            log.error("查询线路趋势数据失败，失败信息："+e);
            return Reply.fail("查询线路趋势数据失败，失败信息："+e.getMessage());
        }
    }


    /**
     * 进行历史数据处理
     * @param historyData 历史数据
     * @param itemMap itemid与itemName对应关系
     */
    private List handleHistoryData(Map<String, List<MWItemHistoryDto>> historyData,Map<String,String> itemMap,NetWorkLinkDto link,Map<String,String> unitsMap){
        if(historyData.isEmpty() || itemMap.isEmpty())return new ArrayList();
        String valuePort = link.getValuePort();
        //1:延迟数据  2：丢包率数据
        int trendType = link.getTrendType();
        String scanType = link.getScanType();
        String linkTargetIp = link.getLinkTargetIp();
        List<MWItemHistoryDto> realData = new ArrayList<>();
        log.info("计算线路趋势图"+trendType+":"+scanType+":"+linkTargetIp+"，返回结果为"+historyData);
        if(StringUtils.isNotBlank(scanType) && LinkConstant.ICMP.equals(scanType)){
            //如果是ICMP数据，不需要进行计算，直接进行数据处理返回,延迟数据，以MS为单位
            for (String itemId : historyData.keySet()) {
                String units = unitsMap.get(itemId);//单位
                List<MWItemHistoryDto> datas = historyData.get(itemId);
                if(CollectionUtils.isEmpty(datas))continue;
                for (MWItemHistoryDto data : datas) {
                    //进行单位转换
                    if(trendType == 1){
                        String strValue = UnitsUtil.getValueMap(data.getDoubleValue().toString(), LinkConstant.UNITS_MS, units).get(LinkConstant.VALUE);
                        data.setValue(strValue);
                    }else if(trendType == 2){
                        data.setValue(new BigDecimal(data.getDoubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    realData.add(data);
                }
            }
        }
        if(StringUtils.isNotBlank(scanType) && (LinkConstant.NQA.equals(scanType) || LinkConstant.IPSLA.equals(scanType))){
            //如果是NQA或者IPSLA数据，丢包率需要进行计算
            if(trendType == 1){
                for (String itemId : historyData.keySet()) {
                    String units = unitsMap.get(itemId);//单位
                    List<MWItemHistoryDto> datas = historyData.get(itemId);
                    if(CollectionUtils.isEmpty(datas))continue;
                    for (MWItemHistoryDto data : datas) {
                        //进行单位转换
                        String strValue = UnitsUtil.getValueMap(data.getDoubleValue().toString(), LinkConstant.UNITS_MS, units).get(LinkConstant.VALUE);
                        data.setValue(strValue);
                        realData.add(data);
                    }
                }
            }else if(trendType == 2){
                for (String itemId : historyData.keySet()) {
                    String units = unitsMap.get(itemId);//单位
                    List<MWItemHistoryDto> datas = historyData.get(itemId);
                    if(CollectionUtils.isEmpty(datas))continue;
                    for (MWItemHistoryDto data : datas) {
                        //进行单位转换
                        String strValue = UnitsUtil.getConvertedValue(new BigDecimal(data.getDoubleValue().toString()),units).get(LinkConstant.VALUE);
                        data.setValue(strValue);
                        realData.add(data);
                    }
                }
//                reckonNqaOrIpslaLossData(historyData,itemMap,linkTargetIp,realData);
            }
        }
        if(CollectionUtils.isNotEmpty(realData)){
            Collections.sort(realData, new Comparator<MWItemHistoryDto>() {
                @Override
                public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
        }
        log.info("计算线路趋势图数据"+realData);
        List<MWItemHistoryDto> dtos = dataHandle(realData, link.getDateType());
        log.info("计算线路趋势图数据2"+dtos);
        //进行数据排序
        if(CollectionUtils.isNotEmpty(dtos)){
            Collections.sort(dtos, new Comparator<MWItemHistoryDto>() {
                @Override
                public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
        }
        //组返回数据
        MwLinkLineChartDto linkLineChartDto = new MwLinkLineChartDto();
        linkLineChartDto.setLastUpdateTime(new Date());
        linkLineChartDto.setRealData(dtos);
        linkLineChartDto.setMaxData(dtos);
        linkLineChartDto.setAvgData(dtos);
        linkLineChartDto.setMinData(dtos);
        if(trendType == 1){
            linkLineChartDto.setUnitByReal(LinkConstant.UNITS_MS);
            linkLineChartDto.setUnitByAvg(LinkConstant.UNITS_MS);
            linkLineChartDto.setUnitByMax(LinkConstant.UNITS_MS);
            linkLineChartDto.setUnitByMin(LinkConstant.UNITS_MS);
            linkLineChartDto.setTitleName(link.getLinkName()+LinkConstant.DELAYED_DATA);
        }else if(trendType == 2){
            linkLineChartDto.setUnitByReal(LinkConstant.UNITS_PER_CENT);
            linkLineChartDto.setUnitByAvg(LinkConstant.UNITS_PER_CENT);
            linkLineChartDto.setUnitByMax(LinkConstant.UNITS_PER_CENT);
            linkLineChartDto.setUnitByMin(LinkConstant.UNITS_PER_CENT);
            linkLineChartDto.setTitleName(link.getLinkName()+LinkConstant.LOSS_RATE_DATA);
        }
        List list = new ArrayList();
        list.add(linkLineChartDto);
        List list2 = new ArrayList();
        list2.add(list);
        return list2;
    }

    /**
     * 处理折线图数据，一周和一月数据太多，需要处理
     * @param realData 所有数据
     * @param dateType 时间类型
     */
    private List<MWItemHistoryDto> dataHandle(List<MWItemHistoryDto> realData,int dateType){
        if(CollectionUtils.isEmpty(realData) || (dateType != 3 && dateType != 4))return realData;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<MWItemHistoryDto> dtoList = new ArrayList<>();
        List<MWItemHistoryDto> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        if(dateType == 3){
            //一周数据一天保留100条
            for (MWItemHistoryDto realDatum : realData) {
                if(set.contains(format.format(realDatum.getDateTime()))){
                    list.add(realDatum);
                }else{
                    set.add(format.format(realDatum.getDateTime()));
                    if(list.size() > 100){
                        int i = list.size() / 100;
                        for (int j = 0; j < 100; j++) {
                            MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) list.get(j*i);
                            dtoList.add(mwItemHistoryDto);
                        }
                    }else{
                        dtoList.addAll(list);
                    }
                    list.clear();
                }
            }
        }
        if(dateType == 4){
            //一月数据一天保留40条
            for (MWItemHistoryDto realDatum : realData) {
                if(set.contains(format.format(realDatum.getDateTime()))){
                    list.add(realDatum);
                }else{
                    set.add(format.format(realDatum.getDateTime()));
                    if(list.size() > 40){
                        int i = list.size() / 40;
                        for (int j = 0; j < 40; j++) {
                            MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) list.get(j*i);
                            dtoList.add(mwItemHistoryDto);
                        }
                    }else{
                        dtoList.addAll(list);
                    }
                    list.clear();
                }
            }
        }
        return dtoList;
    }

    /**
     * 计算NQA与IPSLA丢包率历史数据
     * @param historyData 历史数据集合
     * @param itemMap itemID与itemName集合
     * @param linkTargetIp 目标IP
     * @param realData 返回数据
     */
    private void reckonNqaOrIpslaLossData(Map<String, List<MWItemHistoryDto>> historyData,Map<String,String> itemMap,String linkTargetIp,List<MWItemHistoryDto> realData){
        Map<String,List<MWItemHistoryDto>> nameDataMap = new HashMap<>();
        //获取不同name的历史记录
        for (String itemId : historyData.keySet()) {
            String name = itemMap.get(itemId);
            List<MWItemHistoryDto> datas = historyData.get(itemId);
            if(("["+linkTargetIp+"]"+LinkConstant.NQA_SENT).equals(name)){
                nameDataMap.put(LinkConstant.NQA_SENT,datas);
            }
            if(("["+linkTargetIp+"]"+LinkConstant.NQA_SUCESS).equals(name)){
                nameDataMap.put(LinkConstant.NQA_SUCESS,datas);
            }
        }
        if(nameDataMap.isEmpty())return;
        List<MWItemHistoryDto> nqaSentDtos = nameDataMap.get(LinkConstant.NQA_SENT);
        List<MWItemHistoryDto> nqaSucessDtos = nameDataMap.get(LinkConstant.NQA_SUCESS);
        if(CollectionUtils.isEmpty(nqaSentDtos))return;
        //进行丢包率的计算NQA_SENT - NQA_SUCESS / NQA_SENT
        for (int i = 0; i < nqaSentDtos.size(); i++) {
            MWItemHistoryDto historyDto = nqaSentDtos.get(i);
            Double doubleNqaSentValue = historyDto.getDoubleValue();
            Double doubleNqaSucessValue = 0.00;
            Double loss = 0.00;
            if(CollectionUtils.isNotEmpty(nqaSucessDtos) && nqaSucessDtos.size() > i){
                doubleNqaSucessValue = nqaSucessDtos.get(i).getDoubleValue();
            }
            loss = (doubleNqaSentValue - doubleNqaSucessValue)/doubleNqaSentValue;
            historyDto.setDoubleValue(loss);
            historyDto.setValue(new BigDecimal(loss).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
            realData.add(historyDto);
        }
    }

    /**
     * 查询历史记录数据
     * @param serverId 服务器ID
     * @param itemIdMaps 类型和item集合
     * @param time 时间
     */
    private Map<String,List<MWItemHistoryDto>> getHistoryData(Integer serverId,Map<Integer,List<String>> itemIdMaps,List<Long> time){
        if(itemIdMaps.isEmpty() || serverId == null || CollectionUtils.isEmpty(time))return new HashMap<>();
        Map<String,List<MWItemHistoryDto>> map = new HashMap<>();
        for (Integer valueType : itemIdMaps.keySet()) {
            List<String> itemIds = itemIdMaps.get(valueType);
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemIds, time.get(0), time.get(1),valueType);
            log.info("查询历史数据监控项"+valueType+":itemID为"+itemIds+":"+serverId+"，返回结果为"+mwZabbixAPIResult);
            if (!mwZabbixAPIResult.isFail()) {
                JsonNode jsonNode2 = (JsonNode) mwZabbixAPIResult.getData();
                if (jsonNode2 != null && jsonNode2.size() > 0) {
                    for (JsonNode node : jsonNode2) {
                        double v = node.get(LinkConstant.VALUE).asDouble();
                        String itemid = node.get(LinkConstant.ITEM_ID).asText();
                        String clock = node.get(LinkConstant.CLOCK).asText();
                        String ns = node.get(LinkConstant.NS).asText();
                        MWItemHistoryDto dto = new MWItemHistoryDto();
                        dto.setNs(ns);
                        dto.setItemid(itemid);
                        dto.setClock(clock);
                        dto.setDateTime(new Date(Long.parseLong(clock)*1000));
                        dto.setDoubleValue(v);
                        if(map.containsKey(itemid)){
                            List<MWItemHistoryDto> doubles = map.get(itemid);
                            doubles.add(dto);
                            map.put(itemid,doubles);
                        }else{
                            List<MWItemHistoryDto> doubles = new ArrayList<>();
                            doubles.add(dto);
                            map.put(itemid,doubles);
                        }
                    }
                }
            }
        }
        log.info("查询历史数据监控项最终返回数据"+map);
        return map;
    }

    /**
     * 获取查询日期区间
     * @param link 日期数据
     * @return
     */
    private List<Long> getTime(NetWorkLinkDto link){
        List<Long> times = new ArrayList<>();
        Date date = new Date();
        long endTime = date.getTime() / 1000;
        //开始时间，默认是一小时前
        long startTime = DateUtils.addHours(date, -1).getTime() / 1000;
        Date startDate = DateUtils.addHours(date, -1);
        Date endDate = date;

        //为了兼容更多接口的参数，DateStart、DateEnd也作为开始时间结束时间传入。
        if (link.getDateType() == 5 && link.getDateStart() != null) {
            startDate = DateUtils.parse(link.getDateStart());
        }
        if (link.getDateType() == 5 && link.getDateEnd() != null) {
            endDate = DateUtils.parse(link.getDateEnd());
        }
        if (link.getDateType() != null) {
            switch (link.getDateType()) {//1：hour 2:day 3:week 4:month
                case 1:
                    startTime = DateUtils.addHours(date, -1).getTime() / 1000;
                    break;
                case 2:
                    startTime = DateUtils.addDays(date, -1).getTime() / 1000;
                    break;
                case 3:
                    startTime = DateUtils.addWeeks(date, -1).getTime() / 1000;
                    break;
                case 4:
                    startTime = DateUtils.addMonths(date, -1).getTime() / 1000;
                    break;
                case 5:
                    startTime = (startDate.getTime()) / 1000;
                    endTime = (endDate.getTime()) / 1000;
                    break;
                default:
                    break;
            }
        }
        times.add(startTime);
        times.add(endTime);
        return times;
    }

    /**
     * 获取差zabbix服务器条件
     * @param link 线路数据
     * @param hostId 主机ID
     * @param serverId 服务器ID
     * @param itemNames 监控项名称
     */
    private Map<String,Object> getItemAndHostId(NetWorkLinkDto link,String hostId,Integer serverId,List<String> itemNames){
        Map<String,Object> realData = new HashMap<>();
        String valuePort = link.getValuePort();
        //1:延迟数据  2：丢包率数据
        int trendType = link.getTrendType();
        String scanType = link.getScanType();
        String linkTargetIp = link.getLinkTargetIp();
        //查询目标IP资产信息
        MwAssetsIdsDTO mwAssetsIdsDTO = mwAssetsManager.selectAssetsByIp(linkTargetIp);
        if(mwAssetsIdsDTO != null){
            hostId = mwAssetsIdsDTO.getHostId();
            serverId = mwAssetsIdsDTO.getMonitorServerId();
        }
        if(StringUtils.isNotBlank(scanType) && LinkConstant.ICMP.equals(scanType)){
            //取ICMP的item
            if(trendType == 1){//取延迟数据
                itemNames.add(LinkConstant.ICMP_RESPONSE_TIME);
            }else if(trendType == 2){//取丢包率数据
                itemNames.add(LinkConstant.ICMP_LOSS);
            }
        }
        if(StringUtils.isNotBlank(scanType) && (LinkConstant.NQA.equals(scanType) || LinkConstant.IPSLA.equals(scanType))){
            if (valuePort.equals(LinkConstant.ROOT)) {
                serverId = link.getRootAssetsParam().getMonitorServerId() == null ? 0 : link.getRootAssetsParam().getMonitorServerId();
            } else {
                serverId = link.getTargetAssetsParam().getMonitorServerId() == null ? 0 : link.getTargetAssetsParam().getMonitorServerId();
            }
            //取NQA的item
            if(trendType == 1){//取延迟数据
                itemNames.add("["+linkTargetIp+"]"+LinkConstant.PING_AVGRTT);
            }else if(trendType == 2){//取丢包率数据
                itemNames.add("["+linkTargetIp+"]"+LinkConstant.NQA_LOSS);
            }
        }
        realData.put(LinkConstant.HOST_ID,hostId);
        realData.put(LinkConstant.SERVER_ID,serverId);
        return realData;
    }

    /**
     * 换脸树结构与线路之间联系
     * @param mwLinkTreeDtos 树结构数据
     * @param idMaps 树结构ID与线路ID数据
     */
    private void handleLinkIdAndTreeId(List<MwLinkTreeDto> mwLinkTreeDtos,List<Map<String, Object>> idMaps){
        if(CollectionUtils.isEmpty(idMaps))return;
        Map<Integer,List<String>> realData = new HashMap<>();
        for (Map<String, Object> idMap : idMaps) {
            Object linkId = idMap.get(LinkConstant.LINK_ID);
            Object treeId = idMap.get(LinkConstant.TREE_ID);
            if(linkId == null || treeId == null)continue;
            if(realData.get(treeId) != null){
                List<String> linkIds = realData.get(treeId);
                linkIds.add(linkId.toString());
                realData.put(Integer.parseInt(treeId.toString()),linkIds);
            }else{
                List<String> linkIds = new ArrayList<>();
                linkIds.add(linkId.toString());
                realData.put(Integer.parseInt(treeId.toString()),linkIds);
            }
        }
        for (MwLinkTreeDto mwLinkTreeDto : mwLinkTreeDtos) {
            Integer id = mwLinkTreeDto.getId();
            mwLinkTreeDto.setStrId(id.toString());
            List<String> linkIds = realData.get(id);
            if(CollectionUtils.isNotEmpty(linkIds)){
                mwLinkTreeDto.setLinkIds(linkIds);
            }
            String userGroupIdStr = mwLinkTreeDto.getUserGroupIdStr();
            String orgIdStr = mwLinkTreeDto.getOrgIdStr();
            String userIdStr = mwLinkTreeDto.getUserIdStr();
            if(StringUtils.isNotBlank(userGroupIdStr)){
                List<String> list = JSON.parseArray(userGroupIdStr, String.class);
                List<Integer> groups = new ArrayList<>();
                for (String s : list) {
                    groups.add(Integer.parseInt(s));
                }
                mwLinkTreeDto.setGroupIds(groups);
            }
            if(StringUtils.isNotBlank(userIdStr)){
                List<String> list = JSON.parseArray(userIdStr, String.class);
                List<Integer> userIds = new ArrayList<>();
                for (String s : list) {
                    userIds.add(Integer.parseInt(s));
                }
                mwLinkTreeDto.setUserIds(userIds);
            }
            if(StringUtils.isNotBlank(orgIdStr)){
                List<List> list = JSON.parseArray(orgIdStr, List.class);
                List<List<Integer>> orgIds = new ArrayList<>();
                for (List l : list) {
                    List<Integer> us = new ArrayList<>();
                    if(CollectionUtils.isNotEmpty(l)){
                        for (Object s : l) {
                            if(s != null){
                                us.add(Integer.parseInt(s.toString()));
                            }
                        }
                    }
                    orgIds.add(us);
                }
                mwLinkTreeDto.setOrgIds(orgIds);
            }
        }
    }

    private void setLinkIds(MwLinkTreeDto mwLinkTreeDtos,Set<String> linkIds){
        List<MwLinkTreeDto> children = mwLinkTreeDtos.getChildren();
        if(CollectionUtils.isNotEmpty(children)){
            for (MwLinkTreeDto child : children) {
                List<String> linkIds2 = child.getLinkIds();
                if(CollectionUtils.isNotEmpty(linkIds2)){
                    child.setCount(linkIds2.size());
                    linkIds.addAll(linkIds2);
                }
                setLinkIds(child,linkIds);
            }
        }
    }

    /**
     * 组合线路树状结构数据
     * @param mwLinkTreeDtos
     */
    private void recursionHandleTree(List<MwLinkTreeDto> mwLinkTreeDtos,Map<String,NetWorkLinkDto> linkMap){
        for (MwLinkTreeDto mwLinkTreeDto : mwLinkTreeDtos) {
            Integer id = mwLinkTreeDto.getId();
            String linkId = mwLinkTreeDto.getLinkId();
//            if(StringUtils.isNotBlank(linkId)){
//                continue;
//            }
//            setLinkData(mwLinkTreeDto,linkMap);
            for (MwLinkTreeDto linkTreeDto : mwLinkTreeDtos) {
                Integer prentId = linkTreeDto.getParentId();
                if(id == prentId){
                    List<MwLinkTreeDto> children = mwLinkTreeDto.getChildren();
                    if(children == null){
                        children = new ArrayList<>();
                    }
                    children.add(linkTreeDto);
                    mwLinkTreeDto.setChildren(children);
                }
            }
        }
        Iterator<MwLinkTreeDto> iterator = mwLinkTreeDtos.iterator();
        while(iterator.hasNext()){
            MwLinkTreeDto next = iterator.next();
            if(next.getParentId() != 0){
                iterator.remove();
            }
        }
    }

    /**
     * 设置树状结构中线路数据
     * @param mwLinkTreeDto 线路树状结构数据
     * @param linkMap 线路ID与数据
     */
    private void setLinkData(MwLinkTreeDto mwLinkTreeDto,Map<String,NetWorkLinkDto> linkMap){
        List<String> linkIds = mwLinkTreeDto.getLinkIds();
        List<NetWorkLinkDto> links = new ArrayList<>();
        List<MwLinkTreeDto> childrens = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(linkIds)){
            for (String linkId : linkIds) {
                if(!linkMap.isEmpty() && linkMap.get(linkId) != null){
                    NetWorkLinkDto netWorkLinkDto = linkMap.get(linkId);
                    MwLinkTreeDto dto = new MwLinkTreeDto();
                    dto.setContentsName(netWorkLinkDto.getLinkName());
                    dto.setType(1);
                    dto.setStrId(netWorkLinkDto.getLinkId());
                    dto.setParentId(mwLinkTreeDto.getId());
                    dto.setUserIds(netWorkLinkDto.getUserIds());
                    dto.setOrgIds(netWorkLinkDto.getOrgIds());
                    dto.setGroupIds(netWorkLinkDto.getGroupIds());
                    List<String> ids = new ArrayList<>();
                    ids.add(netWorkLinkDto.getLinkId());
                    dto.setLinkIds(ids);
                    childrens.add(dto);

                }
            }
        }
        mwLinkTreeDto.setChildren(childrens);
    }

    private void getLinkZabbixData(List<NetWorkLinkDto> netWorkLinkDtos){
        if(CollectionUtils.isEmpty(netWorkLinkDtos))return;
        /**
         * 将所有数据整理
         */
        Map<Integer,List<String>> enableIcmpProbemap = new HashMap<>();//启用ICMP线路探测Map
        Map<Integer,List<String>> notEnableProbemap = new HashMap<>();//未启用线路探测Map
        Map<Integer,List<String>> enableNqaProbemap = new HashMap<>();//启用Nqa线路探测Map
        Map<Integer,List<String>> enableIpslaProbemap = new HashMap<>();//启用Ipsla线路探测Map
        Set<String> notEnableProbeItemNames = new HashSet<>();
        Set<String> enableicmpProbeItemNames = new HashSet<>();
        Set<String> enableNqaProbeItemNames = new HashSet<>();
        Set<String> enableIpslaProbeItemNames = new HashSet<>();
        Map<String,List<NetWorkLinkDto>> workDtoMap = new HashMap<>();
        for (NetWorkLinkDto link : netWorkLinkDtos) {
            String scanType = link.getScanType();
            String valuePort = link.getValuePort();
            String baseLinkHostId = "";
            Integer baseLinkServerId = 0;
            String port = "";
            if (valuePort.equals(LinkConstant.ROOT)) {
                baseLinkHostId = link.getRootAssetsParam().getAssetsId();
                baseLinkServerId = link.getRootAssetsParam().getMonitorServerId() == null ? 0 : link.getRootAssetsParam().getMonitorServerId();
                port = link.getRootPort();
            } else {
                baseLinkHostId = link.getTargetAssetsParam().getAssetsId();
                baseLinkServerId = link.getTargetAssetsParam().getMonitorServerId() == null ? 0 : link.getTargetAssetsParam().getMonitorServerId();
                port = link.getTargetPort();
            }
            if(StringUtils.isBlank(scanType)){
                if(workDtoMap != null && workDtoMap.get(baseLinkHostId) != null){
                    List<NetWorkLinkDto> dtos = workDtoMap.get(baseLinkHostId);
                    dtos.add(link);
                    workDtoMap.put(baseLinkHostId,dtos);
                }else{
                    List<NetWorkLinkDto> dtos = new ArrayList<>();
                    dtos.add(link);
                    workDtoMap.put(baseLinkHostId,dtos);
                }
            }else{
                if(!LinkConstant.ICMP.equals(scanType)){
                    if(workDtoMap != null && workDtoMap.get(baseLinkHostId+scanType) != null){
                        List<NetWorkLinkDto> dtos = workDtoMap.get(baseLinkHostId+scanType);
                        dtos.add(link);
                        workDtoMap.put(baseLinkHostId+scanType,dtos);
                    }else{
                        List<NetWorkLinkDto> dtos = new ArrayList<>();
                        dtos.add(link);
                        workDtoMap.put(baseLinkHostId+scanType,dtos);
                    }
                }
            }

            if (link.getEnable().equals(LinkConstant.ACTIVE)) {//启用了线路探测
                String linkTargetIp = link.getLinkTargetIp();
                if (LinkConstant.ICMP.equals(scanType)) {//ICMP 使用目标IP/目标设备
                    MwAssetsIdsDTO mwAssetsIdsDTO = mwAssetsManager.selectAssetsByIp(linkTargetIp);
                    if(mwAssetsIdsDTO == null){continue;}
                    Object assetsId = mwAssetsIdsDTO.getHostId();
                    Object serverId = mwAssetsIdsDTO.getMonitorServerId();
                    if(!workDtoMap.isEmpty() && workDtoMap.get(assetsId.toString()+scanType) != null){
                        List<NetWorkLinkDto> dtos = workDtoMap.get(assetsId.toString()+scanType);
                        dtos.add(link);
                        workDtoMap.put(assetsId.toString()+scanType,dtos);
                    }else{
                        List<NetWorkLinkDto> dtos = new ArrayList<>();
                        dtos.add(link);
                        workDtoMap.put(assetsId.toString()+scanType,dtos);
                    }
                    if(!enableIcmpProbemap.isEmpty() && enableIcmpProbemap.get(Integer.parseInt(serverId.toString())) != null){
                        List<String> enableHostIds = enableIcmpProbemap.get(Integer.parseInt(serverId.toString()));
                        enableHostIds.add(assetsId.toString());
                        enableIcmpProbemap.put(Integer.parseInt(serverId.toString()),enableHostIds);
                    }else{
                        List<String> enableHostIds = new ArrayList<>();
                        enableHostIds.add(assetsId.toString());
                        enableIcmpProbemap.put(Integer.parseInt(serverId.toString()),enableHostIds);
                    }
                    enableicmpProbeItemNames.add(LinkConstant.ICMP_RESPONSE_TIME);
                    enableicmpProbeItemNames.add(LinkConstant.ICMP_PING);
                    enableicmpProbeItemNames.add(LinkConstant.ICMP_LOSS);
                } else if (LinkConstant.NQA.equals(scanType)) {
                    if(!enableNqaProbemap.isEmpty() && enableNqaProbemap.get(baseLinkServerId) != null){
                        List<String> enableHostIds = enableNqaProbemap.get(baseLinkServerId);
                        enableHostIds.add(baseLinkHostId);
                        enableNqaProbemap.put(baseLinkServerId,enableHostIds);
                    }else{
                        List<String> enableHostIds = new ArrayList<>();
                        enableHostIds.add(baseLinkHostId);
                        enableNqaProbemap.put(baseLinkServerId,enableHostIds);
                    }
                    enableNqaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.NQA_SUCESS);
                    enableNqaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.PING_AVGRTT);
                    enableNqaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.NQA_SENT);
                } else if (LinkConstant.IPSLA.equals(scanType)) {
                    if(!enableIpslaProbemap.isEmpty() && enableIpslaProbemap.get(baseLinkServerId) != null){
                        List<String> enableHostIds = enableIpslaProbemap.get(baseLinkServerId);
                        enableHostIds.add(baseLinkHostId);
                        enableIpslaProbemap.put(baseLinkServerId,enableHostIds);
                    }else{
                        List<String> enableHostIds = new ArrayList<>();
                        enableHostIds.add(baseLinkHostId);
                        enableIpslaProbemap.put(baseLinkServerId,enableHostIds);
                    }
                    enableIpslaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.IPSLA_TIMEOUT);
                    enableIpslaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.PING_AVGRTT);
                    enableIpslaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.NQA_SENT);
                    enableIpslaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.NQA_SUCESS);
                    enableIpslaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.IPSLA_COMPLETION_TIME);
                    enableIpslaProbeItemNames.add("["+linkTargetIp+"]"+LinkConstant.IPSLA_STATUS);
                }
            }else{
                link.setResponseTime(LinkConstant.NA);
                //未启用线路探测
                if(baseLinkServerId == 0 || StringUtils.isBlank(baseLinkHostId))continue;
                if(!notEnableProbemap.isEmpty() && notEnableProbemap.get(baseLinkServerId) != null){
                    List<String> enableHostIds = notEnableProbemap.get(baseLinkServerId);
                    enableHostIds.add(baseLinkHostId);
                    notEnableProbemap.put(baseLinkServerId,enableHostIds);
                }else{
                    List<String> enableHostIds = new ArrayList<>();
                    enableHostIds.add(baseLinkHostId);
                    notEnableProbemap.put(baseLinkServerId,enableHostIds);
                }
                notEnableProbeItemNames.add("["+port+"]"+LinkConstant.MW_INTERFACE_STATUS);
                notEnableProbeItemNames.add("["+port+"]"+LinkConstant.INTERFACE_IN_DROP_RATE);
                notEnableProbeItemNames.add("["+port+"]"+LinkConstant.INTERFACE_OUT_DROP_RATE);
            }
        }
        //查询数据
        getnotEnableProbeData(notEnableProbemap,workDtoMap,netWorkLinkDtos,notEnableProbeItemNames);

        //查询启用ICMP线路探测数据
        enableIcmpProbeData(enableIcmpProbemap,enableicmpProbeItemNames,workDtoMap);

        //查询启用NQA线路探测数据
        enableNqaProbeData(enableNqaProbemap,enableNqaProbeItemNames,workDtoMap);

        //查询启用IPSLA线路探测数据
        enableIpslaProbeData(enableIpslaProbemap,enableIpslaProbeItemNames,workDtoMap);
        log.info("MWNetWorkLinkServiceImpl{} getLinkZabbixData() workDtoMap::"+workDtoMap);
        log.info("MWNetWorkLinkServiceImpl{} getLinkZabbixData() enableIpslaProbeItemNames::"+enableIpslaProbeItemNames);
        //计算NQA与IPSLA的丢包率
        List<NetWorkLinkDto> workLinkDtos = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        for (String key : workDtoMap.keySet()) {
            List<NetWorkLinkDto> dtos = workDtoMap.get(key);
            if(CollectionUtils.isEmpty(dtos))continue;
            for (NetWorkLinkDto dto : dtos) {
                String scanType = dto.getScanType();
                if(StringUtils.isNotBlank(scanType) && (LinkConstant.NQA.equals(scanType) || LinkConstant.IPSLA.equals(scanType))){
                    Double nqaSent = dto.getNqaSent();
                    Double nqaSucess = dto.getNqaSucess();
                    if(nqaSent != null && nqaSucess != null && nqaSent != 0){
                        double v = (nqaSent - nqaSucess) / nqaSent;
                        double value = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setLossRate(decimalFormat.format(value));
                    }
                }
                workLinkDtos.add(dto);
            }
        }
        //设置线路状态，响应时间，丢包率数据
        if(CollectionUtils.isEmpty(netWorkLinkDtos) || CollectionUtils.isEmpty(workLinkDtos))return;
        for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
            String linkId = netWorkLinkDto.getLinkId();
            for (NetWorkLinkDto workLinkDto : workLinkDtos) {
                String linkId2 = workLinkDto.getLinkId();
                if(linkId.equals(linkId2)){
                    if(netWorkLinkDto.getEnable() != null && LinkConstant.ACTIVE.equals(netWorkLinkDto.getEnable())){
                        netWorkLinkDto.setLossRate(workLinkDto.getLossRate());
                    }
                    netWorkLinkDto.setStatus(workLinkDto.getStatus());
                    netWorkLinkDto.setResponseTime(workLinkDto.getResponseTime());
                }
            }
        }
    }

    /**
     * 查询带宽
     * @param bandWidthmap 服务器ID与hostID
     * @param bandWidthItemNames item名称
     * @param netWorkLinkDtos 线路数据
     */
    private Map<String, Map<String, Object>> getBandWidthData( Map<Integer,List<String>> bandWidthmap, Set<String> bandWidthItemNames,List<NetWorkLinkDto> netWorkLinkDtos) {
        Map<String, Map<String, Object>> linkValue = new HashMap<>();
        if (!bandWidthmap.isEmpty()) {
            for (Map.Entry<Integer, List<String>> entry : bandWidthmap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> value = entry.getValue();
                List<String> names = new ArrayList<>(bandWidthItemNames);
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(serverId, names, value);
                Map<String, Object> map = new HashMap<>();
                if (result.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0) {
                        for (JsonNode node : jsonNode) {
                            String typePort = "";
                            String name = node.get(LinkConstant.NAME).asText();
                            String units = node.get(LinkConstant.UNITS).asText();
                            if (name.indexOf("]") != -1) {
                                typePort = name.substring(1, name.indexOf("]"));
                                name = name.substring(name.indexOf("]") + 1);
                            }
                            String lastValue = node.get(LinkConstant.LAST_VALUE).asText();
                            String hostId = node.get(LinkConstant.ZABBIX_HOST_ID).asText();
                            if (linkValue.containsKey(hostId + "_" + serverId)) {
                                map = linkValue.get(hostId + "_" + serverId);
                            } else {
                                map = new HashMap<>();
                            }

//                            switch (name) {
//                                case INTERFACE_BANDWIDTH:
//                                    String valueWithUnits = UnitsUtil.getValueWithUnits(lastValue, units);
//                                    map.put(typePort + UP_INTERFACE_BANDWIDTH, valueWithUnits);
//                                    map.put(typePort + DOWN_INTERFACE_BANDWIDTH, valueWithUnits);
//                                    break;
//                                case INTERFACE_IN_UTILIZATION:
//                                    map.put(typePort + IN_BANDWIDTH_UTILIZATION, (lastValue != null && StringUtils.isNotEmpty(lastValue)) ? Double.valueOf(lastValue) : 0.0);
//                                    break;
//                                case INTERFACE_OUT_UTILIZATION:
//                                    map.put(typePort + OUT_BANDWIDTH_UTILIZATION, (lastValue != null && StringUtils.isNotEmpty(lastValue)) ? Double.valueOf(lastValue) : 0.0);
//                                    break;
//                                default:
//                                    break;
//                            }
                            linkValue.put(hostId + "_" + serverId, map);
                        }
                    }
                }
            }
        }
       return linkValue;
    }

    /**
     * 查询未启用探测的状态数据
     * @param notEnableProbemap 未启用探测的线路数据
     * @param workDtoMap 线路ID与对应数据
     * @param netWorkLinkDtos 查询的线路数据
     * @param notEnableProbeItemNames 查询的ITEM名称
     */
    private void getnotEnableProbeData(Map<Integer,List<String>> notEnableProbemap,Map<String,List<NetWorkLinkDto>> workDtoMap,List<NetWorkLinkDto> netWorkLinkDtos, Set<String> notEnableProbeItemNames){
        //查询未启用线路探测数据
        if(!notEnableProbemap.isEmpty()){
            for (Map.Entry<Integer, List<String>> entry : notEnableProbemap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> value = entry.getValue();
                List<String> names = new ArrayList<>(notEnableProbeItemNames);
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(serverId, names, value);
                Map<String,List<Double>> rateMap = new HashMap<>();
                if (mwZabbixAPIResult != null && mwZabbixAPIResult.getCode() == 0){
                    JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                    if (jsonNode.size() > 0){
                        for (JsonNode node : jsonNode){
                            String name = node.get(LinkConstant.NAME).asText();
                            String units = node.get(LinkConstant.UNITS).asText();
                            Double lastvalue = node.get(LinkConstant.LAST_VALUE).asDouble();
                            String hostid = node.get(LinkConstant.ZABBIX_HOST_ID).asText();
                            List<NetWorkLinkDto> dtos = workDtoMap.get(hostid);
                            for (NetWorkLinkDto dto : dtos) {
                                String linkId = dto.getLinkId();
                                String valuePort = dto.getValuePort();
                                String baseLinkHostId = "";
                                String port = "";
                                if (valuePort.equals(LinkConstant.ROOT)) {
                                    baseLinkHostId = dto.getRootAssetsParam().getAssetsId();
                                    port = dto.getRootPort();
                                } else {
                                    baseLinkHostId = dto.getTargetAssetsParam().getAssetsId();
                                    port = dto.getTargetPort();
                                }
                                if(!hostid.equals(baseLinkHostId))continue;
                                if(name.equals("["+port+"]"+LinkConstant.MW_INTERFACE_STATUS)){//设置状态
                                    if(lastvalue == 1){
                                        dto.setStatus(LinkConstant.NORMAL);
                                    }else{
                                        dto.setStatus(LinkConstant.ABNORMAL);
                                    }
                                }
                                dto.setResponseTime(LinkConstant.NA);
                                if(name.equals("["+port+"]"+LinkConstant.INTERFACE_OUT_DROP_RATE) || name.equals("["+port+"]"+LinkConstant.INTERFACE_IN_DROP_RATE)){
                                    if(rateMap.containsKey(linkId)){
                                        List<Double> doubles = rateMap.get(linkId);
                                        doubles.add(lastvalue);
                                        rateMap.put(linkId,doubles);
                                    }else{
                                        List<Double> doubles = new ArrayList<>();
                                        doubles.add(lastvalue);
                                        rateMap.put(linkId,doubles);
                                    }
                                }
                            }
                        }
                    }
                }
                if(!rateMap.isEmpty()){
                    for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
                        String linkId = netWorkLinkDto.getLinkId();
                        if(rateMap.get(linkId) == null)continue;
                        List<Double> doubles = rateMap.get(linkId);
                        double v = (doubles.get(0) + doubles.get(1)) / 2;
                        netWorkLinkDto.setLossRate(new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+LinkConstant.UNITS_PER_CENT);
                    }
                }
            }
        }
    }

    /**
     * 查询ICMP类型的线路状态
     * @param enableIcmpProbemap serverID与hostID
     * @param enableicmpProbeItemNames zabbix上ITEM名称
     * @param workDtoMap 线路主键对应数据
     */
    private void enableIcmpProbeData(Map<Integer,List<String>> enableIcmpProbemap,Set<String> enableicmpProbeItemNames,Map<String,List<NetWorkLinkDto>> workDtoMap){
        if(!enableIcmpProbemap.isEmpty()){
            for (Map.Entry<Integer, List<String>> entry : enableIcmpProbemap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> value = entry.getValue();
                List<String> names = new ArrayList<>(enableicmpProbeItemNames);
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(serverId, names, value);
                Map<String,List<Double>> rateMap = new HashMap<>();
                if (mwZabbixAPIResult != null && mwZabbixAPIResult.getCode() == 0){
                    JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                    if (jsonNode.size() > 0){
                        for (JsonNode node : jsonNode){
                            String name = node.get(LinkConstant.NAME).asText();
                            String units = node.get(LinkConstant.UNITS).asText();
                            Double lastvalue = node.get(LinkConstant.LAST_VALUE).asDouble();
                            String hostid = node.get(LinkConstant.ZABBIX_HOST_ID).asText();
                            List<NetWorkLinkDto> dtos = workDtoMap.get(hostid+LinkConstant.ICMP);
                            if(dtos == null)continue;
                            for (NetWorkLinkDto dto : dtos) {
                                if(name.equals(LinkConstant.ICMP_PING)){//设置状态
                                    if(lastvalue == 1){
                                        dto.setStatus(LinkConstant.NORMAL);
                                    }else{
                                        dto.setStatus(LinkConstant.ABNORMAL);
                                    }
                                }
                                if(name.equals(LinkConstant.ICMP_RESPONSE_TIME)  && lastvalue != null){//设置状态
                                    String strValue = UnitsUtil.getValueMap(lastvalue.toString(), LinkConstant.UNITS_MS, units).get(LinkConstant.VALUE);
                                    dto.setResponseTime(strValue+LinkConstant.UNITS_MS);
                                }

                                if(name.equals(LinkConstant.ICMP_LOSS)){
                                    dto.setLossRate(new BigDecimal(lastvalue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 查询NQA类型线路状态数据
     * @param enableNqaProbemap serverID与hostID
     * @param enableNqaProbeItemNames zabbix上ITEM名称
     * @param workDtoMap 线路主键对应数据
     */
    private void enableNqaProbeData(Map<Integer,List<String>> enableNqaProbemap,Set<String> enableNqaProbeItemNames,Map<String,List<NetWorkLinkDto>> workDtoMap){
        if(!enableNqaProbemap.isEmpty()){
            for (Map.Entry<Integer, List<String>> entry : enableNqaProbemap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> value = entry.getValue();
                List<String> names = new ArrayList<>(enableNqaProbeItemNames);
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(serverId, names, value);
                if (mwZabbixAPIResult != null && mwZabbixAPIResult.getCode() == 0){
                    JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                    if (jsonNode.size() > 0){
                        for (JsonNode node : jsonNode){
                            String name = node.get(LinkConstant.NAME).asText();
                            String units = node.get(LinkConstant.UNITS).asText();
                            Double lastvalue = node.get(LinkConstant.LAST_VALUE).asDouble();
                            String hostid = node.get(LinkConstant.ZABBIX_HOST_ID).asText();
                            List<NetWorkLinkDto> dtos = workDtoMap.get(hostid+LinkConstant.NQA);
                            for (NetWorkLinkDto dto : dtos) {
                                String linkTargetIp = dto.getLinkTargetIp();
                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.NQA_SUCESS)){//设置状态
                                    if(lastvalue != 0){
                                        dto.setStatus(LinkConstant.NORMAL);
                                    }else{
                                        dto.setStatus(LinkConstant.ABNORMAL);
                                    }
                                    dto.setNqaSucess(lastvalue);
                                }
                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.PING_AVGRTT) && lastvalue != null){
                                    String strValue = UnitsUtil.getValueMap(lastvalue.toString(), LinkConstant.UNITS_MS, units).get(LinkConstant.VALUE);
                                    dto.setResponseTime(strValue+LinkConstant.UNITS_MS);
                                }

                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.NQA_SENT)){
                                    dto.setNqaSent(lastvalue);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 查询IPSLA类型线路状态数据
     * @param enableIpslaProbemap serverID与hostID
     * @param enableIpslaProbeItemNames zabbix上ITEM名称
     * @param workDtoMap 线路主键对应数据
     */
    private void enableIpslaProbeData(Map<Integer,List<String>> enableIpslaProbemap,Set<String> enableIpslaProbeItemNames,Map<String,List<NetWorkLinkDto>> workDtoMap){
        if(!enableIpslaProbemap.isEmpty()){
            for (Map.Entry<Integer, List<String>> entry : enableIpslaProbemap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> value = entry.getValue();
                List<String> names = new ArrayList<>(enableIpslaProbeItemNames);
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(serverId, names, value);
                if (mwZabbixAPIResult != null && mwZabbixAPIResult.getCode() == 0){
                    JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                    if (jsonNode.size() > 0){
                        for (JsonNode node : jsonNode){
                            String name = node.get(LinkConstant.NAME).asText();
                            String units = node.get(LinkConstant.UNITS).asText();
                            Double lastvalue = node.get(LinkConstant.LAST_VALUE).asDouble();
                            String hostid = node.get(LinkConstant.ZABBIX_HOST_ID).asText();
                            List<NetWorkLinkDto> dtos = workDtoMap.get(hostid+LinkConstant.IPSLA);
                            log.info("MWNetWorkLinkServiceImpl{} enableIpslaProbeData() node::"+node);
                            for (NetWorkLinkDto dto : dtos) {
                                String linkTargetIp = dto.getLinkTargetIp();
                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.IPSLA_STATUS)){//设置状态
                                    if(lastvalue != 0){
                                        dto.setStatus(LinkConstant.NORMAL);
                                    }else{
                                        dto.setStatus(LinkConstant.ABNORMAL);
                                    }
                                }
                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.IPSLA_COMPLETION_TIME) && lastvalue != null){
                                    String strValue = UnitsUtil.getValueMap(lastvalue.toString(), LinkConstant.UNITS_MS, units).get(LinkConstant.VALUE);
                                    dto.setResponseTime(strValue+LinkConstant.UNITS_MS);
                                }

                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.NQA_SENT)){
                                    dto.setNqaSent(lastvalue);
                                }
                                if(name.equals("["+linkTargetIp+"]"+LinkConstant.NQA_SUCESS)){
                                    dto.setNqaSucess(lastvalue);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据assetsId和IP地址查询对应设备线路
     * @param param
     * @return
     */
    @Override
    public List<AddAndUpdateParam> getLinkByAssetsIdAndIp(AddAndUpdateParam param) {
        List<AddAndUpdateParam> linkDtos = new ArrayList<>();
        if(StringUtils.isBlank(param.getLinkTargetIp()) && CollectionUtils.isEmpty(param.getLinkNames()))return linkDtos;
        linkDtos = mwNetWorkLinkDao.getLinkListByAssetsIdAndIp(param.getLinkNames(), param.getLinkTargetIp());
        if(CollectionUtils.isEmpty(linkDtos))return linkDtos;
        //获取标签信息
        List<String> linkIds = linkDtos.stream().map(AddAndUpdateParam::getLinkId).collect(Collectors.toList());
        Map<String, List<MwAssetsLabelDTO>> labelMaps = mwLabelCommonServcie.getLabelBoards(linkIds, DataType.LINK.getName());
        if(labelMaps == null || labelMaps.isEmpty())return linkDtos;
        for (AddAndUpdateParam linkDto : linkDtos) {
            List<MwAssetsLabelDTO> labelDTOS = labelMaps.get(linkDto.getLinkId());
            linkDto.setAssetsLabel(labelDTOS);
        }
        return linkDtos;
    }


    @Override
    public Reply getLinkStatusDropDown() {
        //查询数据下拉值
        List<MwLinkDropDowmDto> mwLinkDropDowmDtos = mwNetWorkLinkDao.selectLinkDropDown();
        return Reply.ok(mwLinkDropDowmDtos);
    }

    @Override
    public Reply getLinkDirectoryDetail() {
        try {
            List<LinkDirectoryDetailDto> realData = new ArrayList<>();
            //查询所有线路
            LinkDropDownParam dropDownParam = new LinkDropDownParam();
            dropDownParam.setPageSize(Integer.MAX_VALUE);
            List<NetWorkLinkDto> netWorkLinkDtos = getNetWorkLinkDtos(dropDownParam);
            if(CollectionUtils.isEmpty(netWorkLinkDtos)){return Reply.ok(realData);}
            List<String> linkIds = netWorkLinkDtos.stream().map(NetWorkLinkDto::getLinkId).collect(Collectors.toList());
            Map<String, NetWorkLinkDto> netWorkLinkDtoMap = netWorkLinkDtos.stream().collect(Collectors.toMap(NetWorkLinkDto::getLinkId, option -> option, (oldOption, newOption) -> newOption));
            //查询线路目录信息
            List<LinkDirectoryDetailDto> linkDirectoryDetailDtos = mwNetWorkLinkDao.selectLinkDirectoryByLinkId(linkIds);
            if(CollectionUtils.isEmpty(linkDirectoryDetailDtos)){return Reply.ok(realData);}
            Map<String, List<LinkDirectoryDetailDto>> contentsNameMap = linkDirectoryDetailDtos.stream().collect(Collectors.groupingBy(item -> item.getContentsName()));
            for (String contentsName : contentsNameMap.keySet()) {
                List<LinkDirectoryDetailDto> directoryDetailDtos = contentsNameMap.get(contentsName);
                LinkDirectoryDetailDto directoryDetailDto = new LinkDirectoryDetailDto();
                directoryDetailDto = directoryDetailDtos.get(0);
                List<NetWorkLinkDto> workLinkDtos = new ArrayList<>();
                for (LinkDirectoryDetailDto dto : directoryDetailDtos) {
                    workLinkDtos.add(netWorkLinkDtoMap.get(dto.getLinkId()));
                }
                directoryDetailDto.setWorkLinkDtos(workLinkDtos);
                realData.add(directoryDetailDto);
            }
            return Reply.ok(realData);
        }catch (Throwable e){
            log.error("MWNetWorkLinkServiceImpl{} getLinkDirectoryDetail() error:",e);
            return Reply.fail("MWNetWorkLinkServiceImpl{} getLinkDirectoryDetail() error:",e);
        }
    }

    /**
     * 查询所有线路
     * @return
     */
    @Override
    public List<AddAndUpdateParam> getAllLinkInfo() {
        try {
            List<AddAndUpdateParam> addAndUpdateParams = new ArrayList<>();
            LinkDropDownParam dropDownParam = new LinkDropDownParam();
            dropDownParam.setPageSize(Integer.MAX_VALUE);
            dropDownParam.setIsAdvancedQuery(false);
            Reply reply = selectList(dropDownParam);
            if(reply == null){return addAndUpdateParams;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(pageInfo == null || pageInfo.getList() == null){return addAndUpdateParams;}
            List<NetWorkLinkDto> newLinkDtos = pageInfo.getList();
            for (NetWorkLinkDto newLinkDto : newLinkDtos) {
                AddAndUpdateParam updateParam = new AddAndUpdateParam();
                PropertyUtils.copyProperties(updateParam,newLinkDto);
                addAndUpdateParams.add(updateParam);
            }
            return addAndUpdateParams;
        }catch (Throwable e){
            log.error("MWNetWorkLinkServiceImpl{} getAllLinkInfo()",e);
            return null;
        }
    }

    /**
     * 根据线路ID获取线路接口信息
     * @param linkIds
     * @return
     */
    @Override
    public List<MwLinkInterfaceDto> getLinkInterfaceInfo(List<String> linkIds) {
        try {
            List<MwLinkInterfaceDto> interfaceDtos = new ArrayList<>();
            LinkDropDownParam dropDownParam = new LinkDropDownParam();
            dropDownParam.setPageSize(Integer.MAX_VALUE);
            dropDownParam.setIsAdvancedQuery(false);
            dropDownParam.setLinkIds(linkIds);
            Reply reply = selectList(dropDownParam);
            if(reply == null){return interfaceDtos;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(pageInfo == null || pageInfo.getList() == null){return interfaceDtos;}
            List<NetWorkLinkDto> netWorkLinkDtos = pageInfo.getList();
            log.info("MWNetWorkLinkServiceImpl{} getLinkInterfaceInfo()  netWorkLinkDtos::"+netWorkLinkDtos);
            if(CollectionUtils.isEmpty(netWorkLinkDtos)){return interfaceDtos;}
            for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
                MwLinkInterfaceDto interfaceDto = new MwLinkInterfaceDto();
                String valuePort = netWorkLinkDto.getValuePort();
                if(valuePort.equals(LinkConstant.ROOT)){
                    AssetsParam rootAssetsParam = netWorkLinkDto.getRootAssetsParam();
                    interfaceDto.extractFrom(rootAssetsParam,netWorkLinkDto.getRootPort());
                }else{
                    AssetsParam targetAssetsParam = netWorkLinkDto.getTargetAssetsParam();
                    interfaceDto.extractFrom(targetAssetsParam,netWorkLinkDto.getTargetPort());
                }
                interfaceDto.setLinkId(netWorkLinkDto.getLinkId());
                interfaceDto.setLinkName(netWorkLinkDto.getLinkName());
                interfaceDto.setUpBnadWithUtilization(String.valueOf(netWorkLinkDto.getInLinkBandwidthUtilization()));
                interfaceDto.setDownBnadWithUtilization(String.valueOf(netWorkLinkDto.getOutLinkBandwidthUtilization()));
                interfaceDtos.add(interfaceDto);
            }
            return interfaceDtos;
        }catch (Throwable e){
            log.info("MWNetWorkLinkServiceImpl{} getLinkInterfaceInfo() ERROR:",e);
            return null;
        }
    }



    @Override
    public Reply getLinkInfo(MwLinkCommonParam linkCommonParam) {
        LinkDropDownParam param = new LinkDropDownParam();
        param.setPageNumber(linkCommonParam.getPageNumber());
        param.setPageSize(linkCommonParam.getPageSize());
        param.setLinkName(linkCommonParam.getLinkName());
        param.setLinkStatus(linkCommonParam.getLinkStatus());
        param.setIsAdvancedQuery(false);
        Reply reply = selectList(param);
        return reply;
    }

    @Override
    public Reply getLinkDirectoryDropDown() {
        List<MwLinkDirectoryDto> directoryDtos = new ArrayList<>();
        Reply linkDirectoryDetail = getLinkDirectoryDetail();
        List<LinkDirectoryDetailDto> realData = (List<LinkDirectoryDetailDto>) linkDirectoryDetail.getData();
        if(CollectionUtils.isEmpty(realData)){return Reply.ok(directoryDtos);}
        for (LinkDirectoryDetailDto realDatum : realData) {
            List<NetWorkLinkDto> workLinkDtos = realDatum.getWorkLinkDtos();
            if(CollectionUtils.isEmpty(workLinkDtos)){continue;}
            MwLinkDirectoryDto dto = new MwLinkDirectoryDto();
            List<String> linkIds = workLinkDtos.stream().map(NetWorkLinkDto::getLinkId).collect(Collectors.toList());
            dto.setDirectoryName(realDatum.getContentsName());
            dto.setLinkIds(linkIds);
            directoryDtos.add(dto);
        }
        return Reply.ok(directoryDtos);
    }
}
