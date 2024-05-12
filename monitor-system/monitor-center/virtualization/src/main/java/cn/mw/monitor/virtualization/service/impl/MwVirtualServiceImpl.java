package cn.mw.monitor.virtualization.service.impl;

import cn.mw.monitor.assets.utils.AttributesExtractUtils;
import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.*;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.service.MwAssetsVirtualService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.virtualization.dao.MwVirtualDao;
import cn.mw.monitor.virtualization.dto.*;
import cn.mw.monitor.virtualization.service.MwVirtualService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/6/28 11:54
 * @Version 1.0
 */
@Service
@Slf4j(topic = "logfile")
@Transactional
public class MwVirtualServiceImpl implements MwVirtualService, MwAssetsVirtualService {
    private static final Logger logger = LoggerFactory.getLogger("MwVirtualServiceImpl");
    @Autowired
    private MwServerManager mwServerManager;
    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MwVirtualManage mwVirtualManage;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwVirtualDao mwVirtualDao;

    @Autowired
    private MWUserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Reply getHostTable(QueryHostParam qParam) {
        log.info("改版后的主机数据列表排序111");
        try {
            long time1 = System.currentTimeMillis();
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            List<GroupHosts> list = qParam.getHostList();
            List<HostTableDto> hostTableDtos = new ArrayList<>();
            pageInfo.setTotal(list.size());
            //将list 先按状态排序
            List<String> collect = new ArrayList<>();
            if (list.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
                collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(qParam.getMonitorServerId(), Arrays.asList(VmHostEnum.OVERALL_STATUS.getItemName()), collect);
                if (!filterResult.isFail()) {
                    List<ItemValue> itemApplications = JSONArray.parseArray(filterResult.getData().toString(), ItemValue.class);
                    ListSortUtil<ItemValue> lists = new ListSortUtil<>();
                    lists.sort(itemApplications, "lastvalue", 0);
                    collect = itemApplications.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                }
            }
            long time2 = System.currentTimeMillis();
            if (qParam.getSortField() == null || StringUtils.isEmpty(qParam.getSortField())) {//如果不排序的话正常查询分页的内容
                collect = pageList.getList(collect, qParam.getPageNumber(), qParam.getPageSize());
            }

            if (collect.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
//                List<String> collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                hostTableDtos = getHostTableDtoByHost(qParam.getMonitorServerId(), collect);

            }
            long time3 = System.currentTimeMillis();
            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<HostTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = HostTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(hostTableDtos, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    log.info("has no field", e);
                    finalHostTableDtos.sort(hostTableDtos, qParam.getSortField(), qParam.getSortMode());
                }

                hostTableDtos = pageList.getList(hostTableDtos, qParam.getPageNumber(), qParam.getPageSize());
            }
            long time4 = System.currentTimeMillis();

            List<Map> powerList = mwVirtualDao.getVirtualPowerList();
            if (powerList != null && powerList.size() > 0) {
                for (HostTableDto dto : hostTableDtos) {
                    for (Map m : powerList) {
                        if (m.get("typeId").toString().indexOf(dto.getHostId()) != -1) {
                            List<String> userIds = Arrays.asList(m.get("userIds").toString().split(","));
                            List<String> groupIds = Arrays.asList(m.get("groupIds").toString().split(","));
                            List<String> orgIds = Arrays.asList(m.get("orgIds").toString().split(","));
                            dto.setUserIds(userIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
                            dto.setGroupIds(groupIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
                            dto.setOrgIds(Arrays.asList(orgIds.stream().map(Integer::parseInt).collect(Collectors.toList())));
                        }

                    }
                }
            }
            pageInfo.setList(hostTableDtos);
            log.info("VIRTUAL_LOG[]getHostTable[]qParam获取不同的table所有数据[]{}[]", qParam.getSortField());
            log.info("getHostTable 获取所有table数据,运行成功结束");
            log.info("HostTable表数据,数据整理时间：" + (time2 - time1) + "ms；获取DTO信息时间：" + (time3 - time2) + "ms；最后排序时间：" + (time4 - time3) + "ms");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getHostTable QueryHostParam:{} cause:{}", qParam.getSortField(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    @Override
    public Reply getVMsTable(QueryHostParam qParam) {
        log.info("改版后的虚拟化数据列表排序");
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            long time1 = System.currentTimeMillis();
            List<GroupHosts> list = qParam.getVmList();
            List<VirtualTableDto> virtualTableDtos = new ArrayList<>();
            List<VirtualTableDto> virtualTableFilter = new ArrayList<>();

            //将list 先按状态排序
            List<String> collect = new ArrayList<>();
            if (list.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
                collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(qParam.getMonitorServerId(), Arrays.asList(ZabbixItemConstant.ITEMNAME.get(8)), collect);
                if (!filterResult.isFail()) {
                    List<ItemValue> itemApplications = JSONArray.parseArray(filterResult.getData().toString(), ItemValue.class);
                    ListSortUtil<ItemValue> lists = new ListSortUtil<>();
                    lists.sort(itemApplications, "lastvalue", 0);
                    collect = itemApplications.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                }
            }


//            if (qParam.getSortField() == null || StringUtils.isEmpty(qParam.getSortField())) {//如果不排序的话正常查询分页的内容
//                collect = pageList.getList(collect, qParam.getPageNumber(), qParam.getPageSize());
//            }
            long time2 = System.currentTimeMillis();
            if (collect.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
//                List<String> collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                virtualTableDtos = getVirtualTableDtoByHostId(qParam.getMonitorServerId(), collect);
            }
            long time3 = System.currentTimeMillis();
            virtualTableFilter = virtualTableDtos;
            if (qParam.getIsManage() != null) {
                //数据过滤（纳管状态）
                virtualTableFilter = new ArrayList<>();
                if (qParam.getIsManage()) {
                    virtualTableFilter = virtualTableDtos.stream().filter(s -> s.getIsConnect() == 1).collect(Collectors.toList());
                } else {
                    virtualTableFilter = virtualTableDtos.stream().filter(s -> s.getIsConnect() == 0).collect(Collectors.toList());
                }
            }

            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<VirtualTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = VirtualTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(virtualTableFilter, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    log.info("has no field", e);
                    finalHostTableDtos.sort(virtualTableFilter, qParam.getSortField(), qParam.getSortMode());
                }
            }
            pageInfo.setTotal(virtualTableFilter.size());
            virtualTableFilter = pageList.getList(virtualTableFilter, qParam.getPageNumber(), qParam.getPageSize());
            long time4 = System.currentTimeMillis();
            //list列表显示负责人、机构
            List<Map> powerList = mwVirtualDao.getVirtualPowerList();
            if (powerList != null && powerList.size() > 0) {
                for (VirtualTableDto dto : virtualTableFilter) {
                    for (Map m : powerList) {
                        if (m.get("typeId").toString().indexOf(dto.getHostId()) != -1) {
                            List<String> userIds = Arrays.asList(m.get("userIds").toString().split(","));
                            List<String> groupIds = Arrays.asList(m.get("groupIds").toString().split(","));
                            List<String> orgIds = Arrays.asList(m.get("orgIds").toString().split(","));
                            dto.setUserIds(userIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
                            dto.setGroupIds(groupIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
                            dto.setOrgIds(Arrays.asList(orgIds.stream().map(Integer::parseInt).collect(Collectors.toList())));
                        }

                    }
                }
            }
            pageInfo.setList(virtualTableFilter);
            log.info("VIRTUAL_LOG[]getVMsTable[]qParam获取VMstable所有数据[]{}[]", qParam.getSortField());
            log.info("getVMsTable表数据,数据整理时间：" + (time2 - time1) + "ms；获取DTO信息时间：" + (time3 - time2) + "ms；最后排序时间：" + (time4 - time3) + "ms");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getVMsTable QueryHostParam:{} cause:{}", qParam.getSortField(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    /**
     * 批量获取虚拟机数据
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply getVMsInfoList(QueryHostParam qParam) {
        log.info("批量获取虚拟机数据");
        try {
            List<GroupHosts> list = new ArrayList<>(qParam.getVmList());
            Integer monitorServerId = qParam.getMonitorServerId();
            List<VirtualTableDto> virtualTableDtos = new ArrayList<>();
            //将list 先按状态排序
            List<String> collect = new ArrayList<>();
            if (list.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
                collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyVm(monitorServerId, Arrays.asList(ZabbixItemConstant.ITEMNAME.get(8)), collect);
                if (!filterResult.isFail()) {
                    JsonNode jsonNode = (JsonNode) filterResult.getData();
                    if (jsonNode.size() > 0) {
                        for (JsonNode node : jsonNode) {
                            List<GroupHosts> vmList = JSONArray.parseArray(node.get("hosts").toString(), GroupHosts.class);
                            if (vmList != null && vmList.size() > 0) {
                                for (GroupHosts host : vmList) {
                                    VirtualTableDto dto = new VirtualTableDto();
                                    dto.setHostId(host.getHostid());
                                    int index = host.getName().indexOf("<");
                                    //判断主机名称是否带有“<>”
                                    //例：host.getName()为 ES_MASTER<10.18.5.80,linuxGuest,CentOS 7 (64 位)>
                                    String name = host.getName();
                                    String lastStr = "";
                                    String ip = "";
                                    if (index > 0) {
                                        //获取“<>”中以逗号分割得第一个字段，为Ip
                                        lastStr = host.getName().substring(index + 1, host.getName().length());
                                        if (lastStr.split(",").length > 0) {
                                            ip = lastStr.split(",")[0];
                                        }
                                        //获取"<>"之前的数据作为name
                                        name = host.getName().substring(0, index);
                                    }
                                    dto.setHostName(name);
                                    dto.setIpAddress(ip);
                                    dto.setMonitorServerId(monitorServerId);
                                    virtualTableDtos.add(dto);
                                }
                            }
                        }
                    }
                }
            }
            return Reply.ok(virtualTableDtos);
        } catch (Exception e) {
            log.error("fail to getVMsTable  cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }


    @Override
    public Reply exportVMsTableData(QueryHostParam qParam, HttpServletResponse response) {
        log.info("虚拟化数据导出");
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            long time1 = System.currentTimeMillis();
            List<GroupHosts> list = qParam.getVmList();
            qParam.getHostList();
            List<VirtualTableDto> virtualTableDtos = new ArrayList<>();
            List<VirtualTableDto> virtualTableFilter = new ArrayList<>();

            //将list 先按状态排序
            List<String> collect = new ArrayList<>();
            if (list.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
                collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(qParam.getMonitorServerId(), Arrays.asList(ZabbixItemConstant.ITEMNAME.get(8)), collect);
                if (!filterResult.isFail()) {
                    List<ItemValue> itemApplications = JSONArray.parseArray(filterResult.getData().toString(), ItemValue.class);
                    ListSortUtil<ItemValue> lists = new ListSortUtil<>();
                    lists.sort(itemApplications, "lastvalue", 0);
                    collect = itemApplications.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                }
            }

            long time2 = System.currentTimeMillis();
            if (collect.size() > 0) {
                virtualTableDtos = getVirtualTableDtoByHostId(qParam.getMonitorServerId(), collect);
            }
            long time3 = System.currentTimeMillis();
            virtualTableFilter = virtualTableDtos;
            if (qParam.getIsManage() != null) {
                //数据过滤（纳管状态）
                virtualTableFilter = new ArrayList<>();
                if (qParam.getIsManage()) {
                    virtualTableFilter = virtualTableDtos.stream().filter(s -> s.getIsConnect() == 1).collect(Collectors.toList());
                } else {
                    virtualTableFilter = virtualTableDtos.stream().filter(s -> s.getIsConnect() == 0).collect(Collectors.toList());
                }
            }

            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<VirtualTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = VirtualTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(virtualTableFilter, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    log.info("has no field", e);
                    finalHostTableDtos.sort(virtualTableFilter, qParam.getSortField(), qParam.getSortMode());
                }
            }

            List<Map> mapList = new ArrayList<>();
            List<String> lable = Arrays.asList("hostName", "ipAddress", "status", "diskSpace", "diskUtilization", "cpuUsage", "memoryUsed", "vMTools");
            List<String> lableName = Arrays.asList("主机名称", "IP地址", "状态", "磁盘空间", "磁盘使用率(%)", "已用CPU", "已用内存", "VMTools");
            for (VirtualTableDto dto : virtualTableFilter) {
                Map<String, Object> map = AttributesExtractUtils.extract(dto, lable);
                mapList.add(map);
            }
            try {
                ExportExcel.exportExcel("虚拟机资产导出", "虚拟机资产导出表", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
            } catch (IOException e) {
                logger.error("fail to exportVMsTableData:虚拟机资产导出失败, case by {}", e);
            }
            return Reply.ok("导出成功");

        } catch (Exception e) {
            log.error("fail to exportVMsTableData param:{} cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.SET_VIRTUAL_EXPORT_CODE_307006, ErrorConstant.SET_VIRTUAL_EXPORT_MSG_307006);
        }
    }

    @Override
    public Reply exportHostTableData(QueryHostParam qParam, HttpServletResponse response) {
        try {
            long time1 = System.currentTimeMillis();
            List<GroupHosts> list = qParam.getHostList();
            List<HostTableDto> hostTableDtos = new ArrayList<>();
            //将list 先按状态排序
            List<String> collect = new ArrayList<>();
            if (list.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
                collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(qParam.getMonitorServerId(), Arrays.asList(VmHostEnum.OVERALL_STATUS.getItemName()), collect);
                if (!filterResult.isFail()) {
                    List<ItemValue> itemApplications = JSONArray.parseArray(filterResult.getData().toString(), ItemValue.class);
                    ListSortUtil<ItemValue> lists = new ListSortUtil<>();
                    lists.sort(itemApplications, "lastvalue", 0);
                    collect = itemApplications.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                }
            }
            long time2 = System.currentTimeMillis();
            if (collect.size() > 0) {
                hostTableDtos = getHostTableDtoByHost(qParam.getMonitorServerId(), collect);
            }
            long time3 = System.currentTimeMillis();
            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<HostTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = HostTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(hostTableDtos, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    log.info("has no field", e);
                    finalHostTableDtos.sort(hostTableDtos, qParam.getSortField(), qParam.getSortMode());
                }
            }
            long time4 = System.currentTimeMillis();
            List<Map> mapList = new ArrayList<>();
            List<String> lable = Arrays.asList("hostName", "ipAddress", "status", "memoryTotal", "cluster", "cpuUtilization", "memoryUsed", "model", "vendor", "memoryUtilization", "duration");
            List<String> lableName = Arrays.asList("主机名称", "IP地址", "状态", "总内存", "群集", "CPU使用情况", "已用内存", "型号", "厂商", "内存使用情况", "正常运行时间");
            for (HostTableDto dto : hostTableDtos) {
                Map<String, Object> map = AttributesExtractUtils.extract(dto, lable);
                mapList.add(map);
            }
            try {
                ExportExcel.exportExcel("主机资产导出", "主机资产导出表", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
            } catch (IOException e) {
                log.error("fail to exportHostTableData:主机资产导出 param:{} cause:{}", qParam, e);
            }
            return Reply.ok("导出成功");
        } catch (Exception e) {
            log.error("fail to exportHostTableData param:{},cause:{}", qParam.getSortField(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    @Override
    public Reply exportStoreTableData(QueryHostParam qParam, HttpServletResponse response) {
        try {
            long time1 = System.currentTimeMillis();
//            list存所有数据存储名
            List<GroupHosts> lists = qParam.getStoreList();
            //根据name去重。
            List<GroupHosts> list = lists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getHostid() + ";" + s.getName()))), ArrayList::new));
            List<DataStoreTableDto> dataStoreTableDtos = new ArrayList<>();
            log.info("distinct before list:{}", list);
            list = list.stream().distinct().collect(Collectors.toList());
            log.info("distinct before list:{}", list);
            long time2 = System.currentTimeMillis();
            if (list.size() > 0) {
                List<String> hostIdList = new ArrayList<>();
                List<String> nameList = new ArrayList<>();
                Map maps = new HashMap();
                for (GroupHosts store : list) {
                    if (!maps.containsKey(store.getHostid())) {
                        hostIdList.add(store.getHostid());
                    }
                    maps.put(store.getHostid(), store.getHostid());
                    nameList.add(store.getName());
                }
                MWZabbixAPIResult resultData = mwtpServerAPI.getItemDataByAppNameList(qParam.getMonitorServerId(), hostIdList, "Datastore", null);
                for (String name : nameList) {
                    DataStoreTableDto dataStoreTableDto = new DataStoreTableDto();
                    if (resultData.getCode() == 0) {
                        dataStoreTableDto.setStoreName(name);
                        JsonNode itemData = (JsonNode) resultData.getData();
                        Double total = 0.0;
                        Double utilization = 0.0;
                        String units = "";
                        for (JsonNode item : itemData) {
                            String itemName = item.get("name").asText();
                            String lastValue = item.get("lastvalue").asText();
                            Double sortLastValue = item.get("lastvalue").asDouble();
                            if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(0))) {
                                dataStoreTableDto.setReadLatency(lastValue);
                                dataStoreTableDto.setSortReadLatency(sortLastValue);
                            } else if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(1))) {
                                utilization = sortLastValue;
                            } else if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(2))) {
                                total = sortLastValue;
                                units = item.get("units").asText();
                                String dataUnits = UnitsUtil.getValueWithUnits(lastValue, units);
                                dataStoreTableDto.setTotalCapacity(dataUnits);
                                dataStoreTableDto.setSortTotalCapacity(total);
                            } else if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(3))) {
                                dataStoreTableDto.setWriteLatency(lastValue);
                                dataStoreTableDto.setSortWriteLatency(sortLastValue);
                            }
                        }
                        Double free = (total * utilization) / 100;
                        String dataUnits = UnitsUtil.getValueWithUnits(free.toString(), units);
                        dataStoreTableDto.setAvailableCapacity(dataUnits);
                        dataStoreTableDto.setSortAvailableCapacity(free);
                        if (total != 0) {
                            BigDecimal bg = new BigDecimal(((total - free) / total) * 100);
                            Double store = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            dataStoreTableDto.setStoreUtilization(store);
                        } else {
                            dataStoreTableDto.setStoreUtilization(0.0);
                        }
                    }
                    dataStoreTableDtos.add(dataStoreTableDto);
                }
            }
            long time3 = System.currentTimeMillis();
            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<DataStoreTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = DataStoreTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(dataStoreTableDtos, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    log.info("has no field", e);
                    finalHostTableDtos.sort(dataStoreTableDtos, qParam.getSortField(), qParam.getSortMode());
                }
            }
            long time4 = System.currentTimeMillis();
            log.info("storeTable表数据,导出处理");
            List<Map> mapList = new ArrayList<>();
            List<String> lable = Arrays.asList("storeName", "readLatency", "writeLatency", "totalCapacity", "availableCapacity", "storeUtilization");
            List<String> lableName = Arrays.asList("存储名称", "读延迟", "写延迟", "总容量", "可用容量", "存储使用率(%)");
            for (DataStoreTableDto dto : dataStoreTableDtos) {
                Map<String, Object> map = AttributesExtractUtils.extract(dto, lable);
                mapList.add(map);
            }
            try {
                ExportExcel.exportExcel("数据存储资产导出", "数据存储资产导出表", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
            } catch (IOException e) {
                log.error("fail to exportStoreTableData param:{},cause:{}", qParam, e);
            }
            return Reply.ok("导出成功");
        } catch (Exception e) {
            log.error("fail to exportStoreTableData Param:{} cause:{}", qParam.getSortField(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    @Override
    public Reply getStoreTable(QueryHostParam qParam) {
        log.info("改版后的存储数据列表排序");
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            long time1 = System.currentTimeMillis();
//            list存所有数据存储名
            List<GroupHosts> lists = qParam.getStoreList();
            //根据name去重。
            List<GroupHosts> list = lists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getHostid() + ";" + s.getName()))), ArrayList::new));
            List<DataStoreTableDto> dataStoreTableDtos = new ArrayList<>();
            log.info("distinct before list:{}", list);
            list = list.stream().distinct().collect(Collectors.toList());
            log.info("distinct before list:{}", list);
            pageInfo.setTotal(list.size());
            if (qParam.getSortField() == null || StringUtils.isEmpty(qParam.getSortField())) {//如果不排序的话正常查询分页的内容
                list = pageList.getList(list, qParam.getPageNumber(), qParam.getPageSize());
            }
            long time2 = System.currentTimeMillis();
            if (list.size() > 0) {
                List<String> hostIdList = new ArrayList<>();
                List<String> nameList = new ArrayList<>();
                Map maps = new HashMap();
                for (GroupHosts store : list) {
                    if (!maps.containsKey(store.getHostid())) {
                        hostIdList.add(store.getHostid());
                    }
                    maps.put(store.getHostid(), store.getHostid());
                    nameList.add(store.getName());
                }
                MWZabbixAPIResult resultData = mwtpServerAPI.getItemDataByAppNameList(qParam.getMonitorServerId(), hostIdList, "Datastore", null);
                for (String name : nameList) {
                    DataStoreTableDto dataStoreTableDto = new DataStoreTableDto();
                    if (resultData.getCode() == 0) {
                        dataStoreTableDto.setStoreName(name);
                        JsonNode itemData = (JsonNode) resultData.getData();
                        Double total = 0.0;
                        Double utilization = 0.0;
                        String units = "";
                        for (JsonNode item : itemData) {
                            String itemName = item.get("name").asText();
                            String lastValue = item.get("lastvalue").asText();
                            Double sortLastValue = item.get("lastvalue").asDouble();
                            if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(0))) {
                                dataStoreTableDto.setReadLatency(lastValue);
                                dataStoreTableDto.setSortReadLatency(sortLastValue);
                            } else if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(1))) {
                                utilization = sortLastValue;
                            } else if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(2))) {
                                total = sortLastValue;
                                units = item.get("units").asText();
                                String dataUnits = UnitsUtil.getValueWithUnits(lastValue, units);
                                dataStoreTableDto.setTotalCapacity(dataUnits);
                                dataStoreTableDto.setSortTotalCapacity(total);
                            } else if (itemName.equals("[" + name + "]" + ZabbixItemConstant.storeItemName.get(3))) {
                                dataStoreTableDto.setWriteLatency(lastValue);
                                dataStoreTableDto.setSortWriteLatency(sortLastValue);
                            }
                        }
                        Double free = (total * utilization) / 100;
                        String dataUnits = UnitsUtil.getValueWithUnits(free.toString(), units);
                        dataStoreTableDto.setAvailableCapacity(dataUnits);
                        dataStoreTableDto.setSortAvailableCapacity(free);
                        if (total != 0) {
                            BigDecimal bg = new BigDecimal(((total - free) / total) * 100);
                            Double store = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            dataStoreTableDto.setStoreUtilization(store);
                        } else {
                            dataStoreTableDto.setStoreUtilization(0.0);
                        }
                    }
                    dataStoreTableDtos.add(dataStoreTableDto);
                }
//                for (GroupHosts store : list) {
//                    GetStoreListThread getStoreListThread = new GetStoreListThread() {
//                        @Override
//                        public DataStoreTableDto call() throws Exception {
//                            DataStoreTableDto dataStoreTableDto1 = getStorageTableDto(qParam.getMonitorServerId(), store.getHostid(), store.getName());
//                            return dataStoreTableDto1;
//                        }
//                    };
//                    Future<DataStoreTableDto> f = executorService.submit(getStoreListThread);
//                    futureList.add(f);
//                }
//                for (Future<DataStoreTableDto> f : futureList) {
//                    try {
//                        DataStoreTableDto dataStoreTableDto1 = f.get(10, TimeUnit.SECONDS);
//                        dataStoreTableDtos.add(dataStoreTableDto1);
//                    } catch (Exception e) {
//                        f.cancel(true);
//                    }
//                }
//                executorService.shutdown();
//                logger.info("关闭线程池");
            }
            long time3 = System.currentTimeMillis();

            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<DataStoreTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = DataStoreTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(dataStoreTableDtos, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    log.info("has no field", e);
                    finalHostTableDtos.sort(dataStoreTableDtos, qParam.getSortField(), qParam.getSortMode());
                }

                dataStoreTableDtos = pageList.getList(dataStoreTableDtos, qParam.getPageNumber(), qParam.getPageSize());
            }
            long time4 = System.currentTimeMillis();
            pageInfo.setList(dataStoreTableDtos);
            log.info("VIRTUAL_LOG[]getStoreTable[]qParam获取storeTable所有数据[]{}[]", qParam.getSortField());
            log.info("getStoreTable 获取storeTable数据,运行成功结束");
            log.info("storeTable表数据,数据整理时间：" + (time2 - time1) + "ms；获取DTO信息时间：" + (time3 - time2) + "ms；最后排序时间：" + (time4 - time3) + "ms");

            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getStoreTable QueryHostParam:{} cause:{}", qParam.getSortField(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    @Override
    public Reply getTableTitle(QueryHostParam qParam) {
        try {
            List<String> titleList = new ArrayList<>();
            if (null != qParam.getAssetHostId() && StringUtils.isNotEmpty(qParam.getAssetHostId())) {
                titleList = Arrays.asList("主机", "虚拟机", "数据储存");
            } else if (null != qParam.getGroupId() && StringUtils.isNotEmpty(qParam.getGroupId())) {
                titleList = Arrays.asList("主机", "虚拟机", "数据储存");
            } else {
                titleList = Arrays.asList((ZabbixItemConstant.STORE.equals(qParam.getFlag())) ? "数据储存" : "虚拟机");
            }
            log.info("VIRTUAL_LOG[]getTableTitle[]qParam获取TableTitle数据[]{}[]", qParam);
            log.info("getStoreTable 获取TableTitle数据,运行成功结束");
            return Reply.ok(titleList);
        } catch (Exception e) {
            log.error("fail to getTableTitle QueryHostParam:{} cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    @Override
    public Reply getBasic(QueryHostParam qParam) {
        try {
            List<BasicDto> list = new ArrayList<>();
            if (qParam.getHostList() != null) {
                Integer hostSize = qParam.getHostList().size();
                Integer vmSize = qParam.getVmList().size();
                if (null != qParam.getAssetHostId() && StringUtils.isNotEmpty(qParam.getAssetHostId())) {
//                如果qParam.getAssetsHostId()不为空，说明是最高级
                    MWZabbixAPIResult hosts = mwtpServerAPI.getHostByHostId(qParam.getMonitorServerId(), qParam.getAssetHostId(), null);
                    if (hosts.getCode() == 0) {
                        JsonNode data = (JsonNode) hosts.getData();
                        if (data.size() > 0) {
                            data.forEach(host -> {
                                if (host.get("interfaces").size() > 0) {
                                    BasicDto basicDto = new BasicDto();
                                    basicDto.setName("IP地址");
                                    basicDto.setValue(host.get("interfaces").get(0).get("ip").asText());
                                    list.add(basicDto);
                                }
                            });
                        }
                    }

                    //主机对应的自动发现的规则名
                    MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(qParam.getMonitorServerId(), qParam.getAssetHostId());
                    if (dRuleByHostId != null && dRuleByHostId.getCode() == 0) {
                        JsonNode resultData = (JsonNode) dRuleByHostId.getData();
                        if (resultData.size() > 0) {
                            for (JsonNode resultDatum : resultData) {
                                String name = resultDatum.get("name").asText();
//                                    根据规则名获取主机组信息
                                MWZabbixAPIResult groupHostByName = mwtpServerAPI.getGroupHostByName(qParam.getMonitorServerId(), name);
                                if (groupHostByName != null && groupHostByName.getCode() == 0) {
                                    JsonNode groupHost = (JsonNode) groupHostByName.getData();
                                    if (groupHost.size() > 0) {
                                        groupHost.forEach(group -> {
                                            Integer size = group.get("hosts").size();
                                            BasicDto basicDto = new BasicDto();
                                            if (name.indexOf(ZabbixItemConstant.HOSTCOMPUTER) != -1) {
//                                    HostComputerDto hostComputerDto = getHostComputerDtoByHosts(getGroupHostIds(group.get("name").asText()));
                                                basicDto.setName("宿主机数量");
                                                basicDto.setValue(qParam.getHostList().size() + "");
                                            } else if (name.indexOf(ZabbixItemConstant.VMWARE) != -1) {
                                                basicDto.setName("虚拟机数量");
                                                basicDto.setValue(qParam.getVmList().size() + "");
                                            } else {
                                                basicDto.setName("集群数量");
                                                basicDto.setValue(size.toString());
                                            }
                                            list.add(basicDto);
                                        });
                                    }
                                }
                            }
                        }
                    }

                    List<BasicDto> itemBasicByHostId = getItemBasicByHostId(qParam.getMonitorServerId(), qParam.getAssetHostId(), hostSize, vmSize);
                    list.addAll(itemBasicByHostId);
                } else if (null != qParam.getGroupId() && StringUtils.isNotEmpty(qParam.getGroupId())) {
                    MWZabbixAPIResult groups = mwtpServerAPI.getGroupHosts(qParam.getMonitorServerId(), qParam.getGroupId());
                    if (groups != null && groups.getCode() == 0) {
                        JsonNode data = (JsonNode) groups.getData();
                        if (data.size() > 0) {
                            for (JsonNode group : data) {
                                if (group.get("discoveryRule").size() > 0) {
                                    String s = group.get("discoveryRule").get("name").asText();
                                    if (group.get("hosts").size() > 0) {
                                        if (s.indexOf(ZabbixItemConstant.VMWARE) != -1) {
                                            String hostId = "";
                                            MWZabbixAPIResult hostByHostId = mwtpServerAPI.getHostByHostIdByFuzzy(qParam.getMonitorServerId(), null, group.get("name").asText());
                                            if (hostByHostId != null && hostByHostId.getCode() == 0) {
                                                JsonNode HostData = (JsonNode) hostByHostId.getData();
                                                for (JsonNode host : HostData) {
                                                    hostId = host.get("hostid").asText();
                                                }
                                            }
                                            List<BasicDto> itemBasicByHostId = getItemBasicByHostId(qParam.getMonitorServerId(), hostId, hostSize, vmSize);
                                            list.addAll(itemBasicByHostId);
                                        } else if (s.indexOf(ZabbixItemConstant.HOSTCOMPUTER) != -1) {
                                            BasicDto basicDto = new BasicDto();
                                            basicDto.setName("宿主机数量");
                                            basicDto.setValue(qParam.getHostList().size() + "");
                                            list.add(basicDto);
                                            BasicDto basicDto1 = new BasicDto();
                                            basicDto1.setName("虚拟机数量");
                                            basicDto1.setValue(qParam.getVmList().size() + "");
                                            list.add(basicDto1);
                                            BasicDto basicDto2 = new BasicDto();
                                            basicDto2.setName("数据存储");
                                            List<String> hostIds = getGroupHostIds(qParam.getMonitorServerId(), group.get("name").asText());
                                            List<GroupHosts> storeNamesByHostIds = mwVirtualManage.getStoreNamesByHostIds(qParam.getMonitorServerId(), hostIds);
                                            basicDto2.setValue(String.valueOf(storeNamesByHostIds.size()));
                                            list.add(basicDto2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (ZabbixItemConstant.VHOST.equals(qParam.getFlag())) {
                        List<BasicDto> itemBasicByHostId = getItemBasicByHostId(qParam.getMonitorServerId(), qParam.getHostId(), hostSize, vmSize);
                        list.addAll(itemBasicByHostId);
                    }
                }
            }

            log.info("VIRTUAL_LOG[]getBasic[]qParam获取不同的table所有数据[]{}[]", qParam.getHostId());
            log.info("getBasic 获取基本数据,运行成功结束");
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getHostTable QueryHostParam:{} cause:{}", qParam.getHostId(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }


    @Override
    public TimeTaskRresult saveVirtualTree() {
        Reply r = mwVirtualManage.getHostTreeByAdmin();
        Reply reply = mwVirtualManage.getStoreTree();
        StringBuilder stringBuilder = new StringBuilder();
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        stringBuilder.append("virtualization");
        stringBuilder.append("::");
        log.info("saveVirtualTree 查看信息 cause:{}", stringBuilder);
        if (r.getRes() == 0 && r.getData() != null) {
            List<VHostTreeDTO> hList = (List<VHostTreeDTO>) r.getData();
            String hKey = stringBuilder.append(ZabbixItemConstant.VHOST).toString();
            redisTemplate.opsForValue().set(hKey, JSONObject.toJSONString(hList), 15, TimeUnit.MINUTES);
            log.info("saveVirtualTree 查看信息 value:{} cause:{} ", hList, hKey);
        }
        if (reply.getRes() == 0 && reply.getData() != null) {
            List<VHostTreeDTO> sList = (List<VHostTreeDTO>) reply.getData();
            String sKey = stringBuilder.append(ZabbixItemConstant.STORE).toString();
            redisTemplate.opsForValue().set(sKey, JSONObject.toJSONString(sList), 15, TimeUnit.MINUTES);
            log.info("saveVirtualTree 查看信息 value:{} cause:{}", sList, sKey);
        }
        taskRresult.setSuccess(true).setResultContext("虚拟化缓存成功");
        return taskRresult;
    }

    @Override
    public TimeTaskRresult saveAllVirtualListByAlert() {
        Reply r = mwVirtualManage.getHostTreeByAdmin();
        StringBuilder stringBuilder = new StringBuilder();
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        stringBuilder.append("virtualAssetsList");
        stringBuilder.append("::");
        log.info("savevirtualAssetsList 查看信息 cause:{}", stringBuilder);
        if (r.getRes() == 0 && r.getData() != null) {
            List<VHostTreeDTO> hList = (List<VHostTreeDTO>) r.getData();
            //为节省redis空间，将Children数据去重，只保留host、vm、store数据
            for (VHostTreeDTO dto : hList) {
                dto.setChildren(new ArrayList<>());
            }
            String hKey = stringBuilder.append(ZabbixItemConstant.VHOST).toString();
            redisTemplate.opsForValue().set(hKey, JSONObject.toJSONString(hList), 15, TimeUnit.MINUTES);
            log.info("savevirtualAssetsList 查看信息 hList:{} hKey:{} ", JSONObject.toJSONString(hList), hKey);
        }
        taskRresult.setSuccess(true).setResultContext("获取所有虚拟化资产缓存成功");
        return taskRresult;
    }

    //定时任务存储虚拟化资产
    public void getAllTreeByTimeTask(String type) {
        Reply r = mwVirtualManage.getHostTreeByAdmin();
        StringBuilder stringBuilder = new StringBuilder();
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        stringBuilder.append("virtualization");
        stringBuilder.append("::");
        log.info("saveVirtualTree 查看信息 cause:{}", stringBuilder);
        if (r.getRes() == 0 && r.getData() != null) {
            List<VHostTreeDTO> hList = (List<VHostTreeDTO>) r.getData();
            saveData(hList);

        }
    }

    private void saveData(List<VHostTreeDTO> list) {
        for (VHostTreeDTO dto : list) {
            dto.getIp();
        }
    }


    @Override
    public Reply getAllTree(String type) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("virtualization");
        stringBuilder.append("::");
        String key = stringBuilder.append(type).toString();

        String hString = redisTemplate.opsForValue().get(key);
        if (null != hString && StringUtils.isNotEmpty(hString) && !"null".equals(hString) && !"[]".equals(hString)) {
            log.info("saveVirtualTree 查看信息  key:{} hString:{}", key, hString);
            return Reply.ok(JSONObject.parseArray(hString, VHostTreeDTO.class));
        } else {
            log.info("get data from zabbix ");
            return ZabbixItemConstant.VHOST.equals(type) ? mwVirtualManage.getHostTree() : mwVirtualManage.getStoreTree();
        }
    }


    @Override
    public Reply getAllInventedAssets(String type, String roleId, Integer userId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("virtualAssetsList");
        stringBuilder.append("::");
        String key = stringBuilder.append(type).toString();
        Reply r = new Reply();
        String hString = redisTemplate.opsForValue().get(key);

        List<VHostTreeDTO> hostTreeDTOList = new ArrayList<>();
        if (null != hString && StringUtils.isNotEmpty(hString) && !"null".equals(hString) && !"[]".equals(hString)) {
            log.info("saveVirtualTree 查看信息  key:{} hString:{}", key, hString);
            hostTreeDTOList = JSONObject.parseArray(hString, VHostTreeDTO.class);
        } else {
            log.info("get data from zabbix ");
            if (ZabbixItemConstant.VHOST.equals(type)) {
                r = mwVirtualManage.getHostTreeByAdmin();
            } else {
                r = mwVirtualManage.getStoreTree();
            }
            if (r.getRes() == 0 && r.getData() != null) {
                hostTreeDTOList = (List<VHostTreeDTO>) r.getData();
                //为节省redis空间，将Children数据去重，只保留host、vm、store数据
                for (VHostTreeDTO dto : hostTreeDTOList) {
                    dto.setChildren(new ArrayList<>());
                }
                redisTemplate.opsForValue().set(key, JSONObject.toJSONString(hostTreeDTOList), 15, TimeUnit.MINUTES);
                log.info("savevirtualAssetsList 查看信息 hList:{} hKey:{} ", JSONObject.toJSONString(hostTreeDTOList), key);
            }
        }

        GlobalUserInfo globalUser = (userId == null ? userService.getGlobalUser() : userService.getGlobalUser(userId));
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.VIRTUAL);
        //非管理员用户，进行权限控制
        if(!globalUser.isSystemUser()){
            for (VHostTreeDTO dto : hostTreeDTOList) {
                Integer monitorServerId = dto.getMonitorServerId();
                List<GroupHosts>  vmList = dto.getVmList();
                List<GroupHosts> disVmList = new ArrayList<>();
                //对虚拟机设备过滤
                for(GroupHosts vmInfo : vmList){
                    if(!Strings.isNullOrEmpty(vmInfo.getHostid())){
                        String vmId = vmInfo.getHostid();
                        for(String idStr : allTypeIdList){
                            if(idStr.startsWith("vm_"+vmId+"_") && idStr.endsWith("_"+monitorServerId)){
                                disVmList.add(vmInfo);
                            }
                        }
                    }

                }
                dto.setVmList(disVmList);

                List<GroupHosts>  hostsList = dto.getHostList();
                List<GroupHosts> disHostsList = new ArrayList<>();
                //对宿主机设备过滤
                for(GroupHosts hostInfo : hostsList){
                    if(!Strings.isNullOrEmpty(hostInfo.getHostid())){
                        String hostId = hostInfo.getHostid();
                        for(String idStr : allTypeIdList){
                            if(idStr.startsWith("host_"+hostId+"_") && idStr.endsWith("_"+monitorServerId)){
                                disHostsList.add(hostInfo);
                            }
                        }
                    }
                }
                dto.setHostList(disHostsList);
            }
        }
        return Reply.ok(hostTreeDTOList);
    }

    @Override
    public Reply getAllTree(String type, String roleId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("virtualAssetsList");
        stringBuilder.append("::");
        String key = stringBuilder.append(type).toString();
        Reply r = new Reply();
        String hString = redisTemplate.opsForValue().get(key);
        if (null != hString && StringUtils.isNotEmpty(hString) && !"null".equals(hString) && !"[]".equals(hString)) {
            log.info("saveVirtualTree 查看信息  key:{} hString:{}", key, hString);
            return Reply.ok(JSONObject.parseArray(hString, VHostTreeDTO.class));
        } else {
            log.info("get data from zabbix ");
            r = mwVirtualManage.getAdminHostTree();
            if (r.getRes() == 0 && r.getData() != null) {
                List<VHostTreeDTO> hList = (List<VHostTreeDTO>) r.getData();
                //为节省redis空间，将Children数据去重，只保留host、vm、store数据
                for (VHostTreeDTO dto : hList) {
                    dto.setChildren(new ArrayList<>());
                }
                redisTemplate.opsForValue().set(key, JSONObject.toJSONString(hList), 15, TimeUnit.MINUTES);
                log.info("savevirtualAssetsList 查看信息 hList:{} hKey:{} ", JSONObject.toJSONString(hList), key);
            }
            return r;
        }
    }

    @ApiOperation(value = "通过hostIds 获取HostTableDto数组")
    public List<HostTableDto> getHostTableDtoByHost(int monitorServerId, Object hostIds) {
        log.info("改版后的主机数据列表排序");
        List<HostTableDto> list = new ArrayList<>();
        List<ItemApplication> itemList = new ArrayList<>();
        List<String> stringList = Arrays.asList(
                VmHostEnum.MWVM_CLUSTER_NAME.getItemName(),
                VmHostEnum.MWVM_UPTIME.getItemName(),
                VmHostEnum.MWVM_CPU_CORES.getItemName(),
                VmHostEnum.MWVM_CPU_FREQUENCY.getItemName(),
                VmHostEnum.MWVM_CPU_USAGE.getItemName(),
                VmHostEnum.MWVM_MEMORY_TOTAL.getItemName(),
                VmHostEnum.MWVM_MEMORY_USED.getItemName(),
                VmHostEnum.HOST_MODEL.getItemName(),
                VmHostEnum.HOST_VENDOR.getItemName(),
                VmHostEnum.OVERALL_STATUS.getItemName());
        MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(monitorServerId, stringList, hostIds);

        if (!filterResult.isFail()) {
            itemList.addAll(JSONArray.parseArray(filterResult.getData().toString(), ItemApplication.class));
        }
        Map<String, List<ItemApplication>> hostCollect = itemList.stream().collect(Collectors.groupingBy(ItemApplication::getHostid));
        List<String> lists = (List<String>) hostIds;

        List<String> valuemapId = new ArrayList<>();
        for (String hostId : lists) {
            List<ItemApplication> items = hostCollect.get(hostId);
            if (items != null && items.size() > 0) {
                Map<String, List<ItemApplication>> collect = items.stream().collect(Collectors.groupingBy(ItemApplication::getName));
                for (String key : collect.keySet()) {
                    List<ItemApplication> itemApplications = collect.get(key);
                    if (itemApplications != null && itemApplications.size() > 0) {
                        ItemApplication item = itemApplications.get(0);
                        if ("Overall status".equals(key)) {
                            valuemapId.add(item.getValuemapid());
                        }
                    }
                }
            }
        }
        Map<String, Map> valueMapByIdList = mwServerManager.getValueMapByIdList(monitorServerId, valuemapId);

        for (String hostId : lists) {
            List<ItemApplication> items = hostCollect.get(hostId);
            HostTableDto hostTableDto = new HostTableDto();
            Double cores = 0.0;
            Double freQuency = 0.0;
            Double usage = 0.0;
            Double mTotal = 0.0;
            Double mUsed = 0.0;
            if (items != null && items.size() > 0) {
                Map<String, List<ItemApplication>> collect = items.stream().collect(Collectors.groupingBy(ItemApplication::getName));
                for (String key : collect.keySet()) {
                    List<ItemApplication> itemApplications = collect.get(key);
                    if (itemApplications != null && itemApplications.size() > 0) {
                        ItemApplication item = itemApplications.get(0);

                        Double lastvalue = item.getLastvalue();
                        String lastValue = item.getStringValue();
                        try {
                            if ("Overall status".equals(key)) {
                                key = "OVERALL_STATUS";
                            }
                            VmHostEnum value = VmHostEnum.valueOf(key);
                            String newValue = "";
                            switch (value) {
                                case MWVM_CLUSTER_NAME:
                                    hostTableDto.setCluster(lastValue);
                                    break;
                                case MWVM_UPTIME:
                                    if (item.getHosts() != null && item.getHosts().size() > 0) {
                                        GroupHosts host = item.getHosts().get(0);
                                        hostTableDto.setHostId(host.getHostid());
                                        int index = host.getName().indexOf("<");
                                        //判断主机名称是否带有“<>”
                                        //例：host.getName()为 10.18.5.11<10.18.5.11>
                                        String name = host.getName();
                                        String lastStr = "";
                                        String ip = "";
                                        if (index > 0) {
                                            //获取“<>”中以逗号分割得第一个字段，为Ip
                                            lastStr = host.getName().substring(index + 1, host.getName().length() - 1);
                                            if (lastStr.split(",").length > 0) {
                                                ip = lastStr.split(",")[0];
                                            }
                                            //获取"<>"之前的数据作为name
                                            name = host.getName().substring(0, index);
                                        }
                                        hostTableDto.setHostName(name);

                                        hostTableDto.setIpAddress(ip);
                                    }
//                                    if (item.getInterfaces() != null && item.getInterfaces().size() > 0) {
//                                        hostTableDto.setIpAddress(item.getInterfaces().get(0).getIp());
//                                    }
                                    hostTableDto.setDuration(SeverityUtils.getLastTime(lastvalue.longValue()));
                                    hostTableDto.setSortDuration(lastvalue);
                                    break;
                                case MWVM_CPU_CORES:
                                    cores = lastvalue;
                                    break;
                                case MWVM_CPU_FREQUENCY:
                                    freQuency = lastvalue;
                                    break;
                                case MWVM_CPU_USAGE:
                                    usage = lastvalue;
                                    break;
                                case MWVM_MEMORY_TOTAL:
                                    mTotal = lastvalue;
                                    hostTableDto.setSortMemoryTotal(lastvalue);
                                    hostTableDto.setMemoryTotal(UnitsUtil.getValueWithUnits(lastvalue.toString(), item.getUnits()));
                                    break;
                                case MWVM_MEMORY_USED:
                                    mUsed = lastvalue;
                                    hostTableDto.setSortMemoryUsed(lastvalue);
                                    hostTableDto.setMemoryUsed(UnitsUtil.getValueWithUnits(lastvalue.toString(), item.getUnits()));
                                    break;
                                case HOST_MODEL:
                                    hostTableDto.setModel(lastValue);
                                    break;
                                case HOST_VENDOR:
                                    hostTableDto.setVendor(lastValue);
                                    break;
                                case OVERALL_STATUS:
//                                    newValue = mwServerManager.getValueMapById(monitorServerId, item.getValuemapid(), String.valueOf(item.getLastvalue().intValue()));
                                    String val = String.valueOf(item.getLastvalue().intValue());
                                    newValue = (String) valueMapByIdList.get(item.getValuemapid()).get(val);
                                    if ("gray".equals(newValue)) {
                                        hostTableDto.setStatus("离线");
                                    } else if ("green".equals(newValue)) {
                                        hostTableDto.setStatus("正常");
                                    } else if ("yellow".equals(newValue)) {
                                        hostTableDto.setStatus("告警");
                                    } else if ("red".equals(newValue)) {
                                        hostTableDto.setStatus("异常");
                                    } else {
                                        hostTableDto.setStatus("未知");
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            log.info("没有对应枚举，不做处理");
                            continue;
                        }
                    }
                }
                if (usage != 0 && (cores * freQuency) != 0) {
                    BigDecimal bigDecimal = new BigDecimal((usage / (cores * freQuency)) * 100);
                    double cpu = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    hostTableDto.setCpuUtilization(cpu);
                } else {
                    hostTableDto.setCpuUtilization(0.0);
                }
                hostTableDto.setMonitorServerId(monitorServerId);
                if (mUsed != 0 && mTotal != 0) {
                    BigDecimal bg = new BigDecimal((mUsed / mTotal) * 100);
                    double memory = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    hostTableDto.setMemoryUtilization(memory);
                } else {
                    hostTableDto.setMemoryUtilization(0.0);
                }
                list.add(hostTableDto);
            }
        }
//        long end = System.currentTimeMillis();
//        ////System.out.println("共耗时" + (end - start));
        return list;
    }

    @ApiOperation(value = "通过hostId获取VirtualTableDto")
    public List<VirtualTableDto> getVirtualTableDtoByHostId(int monitorServerId, Object hostIds) {
//        long start = System.currentTimeMillis();
        List<VirtualTableDto> list = new ArrayList<>();
        List<ItemApplication> itemList = new ArrayList<>();
        long timet1 = System.currentTimeMillis();
//        MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(monitorServerId, Arrays.asList(ZabbixItemConstant.ITEMNAME.get(2), ZabbixItemConstant.ITEMNAME.get(7), ZabbixItemConstant.ITEMNAME.get(8)), hostIds);
//        MWZabbixAPIResult searchResult = mwtpServerAPI.itemGetbySearch(monitorServerId, Arrays.asList(ZabbixItemConstant.diskItemName.get(3), ZabbixItemConstant.diskItemName.get(1)), hostIds);


        List<List> listAll = splitList((List) hostIds, 100);
        int coreSizePool = 10;
        coreSizePool = (coreSizePool > listAll.size()) ? listAll.size() : coreSizePool;
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, coreSizePool + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<MWZabbixAPIResult>> futureList = new ArrayList<>();
        List<MWZabbixAPIResult> filterResultList = new ArrayList<>();

        for (List<String> subList : listAll) {
            Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                @Override
                public MWZabbixAPIResult call() throws Exception {
                    log.info("进入线程1时间：" + System.currentTimeMillis());
                    MWZabbixAPIResult filterResult = mwtpServerAPI.itemGetbyFilter(monitorServerId, Arrays.asList(ZabbixItemConstant.ITEMNAME.get(2), ZabbixItemConstant.ITEMNAME.get(7), ZabbixItemConstant.ITEMNAME.get(8)), subList);
                    return filterResult;
                }
            };
            Future<MWZabbixAPIResult> submit = executorService.submit(callable);
            futureList.add(submit);
        }

        ThreadPoolExecutor executorService2 = new ThreadPoolExecutor(coreSizePool, coreSizePool + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<MWZabbixAPIResult>> searchList = new ArrayList<>();
        List<MWZabbixAPIResult> searchResultList = new ArrayList<>();

        for (List<String> subList2 : listAll) {
            Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                @Override
                public MWZabbixAPIResult call() throws Exception {
                    log.info("进入线程2时间：" + System.currentTimeMillis());
                    MWZabbixAPIResult searchResult = mwtpServerAPI.itemGetbySearch(monitorServerId, Arrays.asList(ZabbixItemConstant.diskItemName.get(3), ZabbixItemConstant.diskItemName.get(1)), subList2);
                    return searchResult;
                }

            };
            Future<MWZabbixAPIResult> submit = executorService2.submit(callable);
            searchList.add(submit);
        }
        if (futureList.size() > 0) {
            futureList.forEach(f -> {
                try {
                    MWZabbixAPIResult result = f.get(300, TimeUnit.SECONDS);
                    filterResultList.add(result);
                } catch (Exception e) {
                    log.error("fail to getVirtualTableDtoByHostId:多线程等待数据返回失败 param:{},cause:{}", hostIds, e);
                }
            });
        }

        executorService.shutdown();
        log.info("关闭线程池");
        if (searchList.size() > 0) {
            searchList.forEach(f -> {
                try {
                    MWZabbixAPIResult result = f.get(300, TimeUnit.SECONDS);
                    searchResultList.add(result);
                } catch (Exception e) {
                    log.error("fail to getVirtualTableDtoByHostId:多线程等待数据返回失败 param:{},cause:{}", hostIds, e);
                }
            });
        }

        executorService2.shutdown();
        log.info("关闭线程池");


        long timet11 = System.currentTimeMillis();
        for (MWZabbixAPIResult filterResult : filterResultList) {
            if (!filterResult.isFail()) {
                itemList.addAll(JSONArray.parseArray(filterResult.getData().toString(), ItemApplication.class));
            }
        }
        for (MWZabbixAPIResult searchResult : searchResultList) {
            if (!searchResult.isFail()) {
                itemList.addAll(JSONArray.parseArray(searchResult.getData().toString(), ItemApplication.class));
            }
        }

        long timet12 = System.currentTimeMillis();
        Map<String, List<ItemApplication>> hostCollect = itemList.stream().collect(Collectors.groupingBy(ItemApplication::getHostid));
        List<String> lists = (List<String>) hostIds;
        long timet2 = System.currentTimeMillis();
//        for (String hostId : lists) {
//            List<ItemApplication> items = hostCollect.get(hostId);
//            if (items != null && items.size() > 0) {
//                Map<String, List<ItemApplication>> collect = items.stream().collect(Collectors.groupingBy(ItemApplication::getName));
//                for (String key : collect.keySet()) {
//                    List<ItemApplication> itemApplications = collect.get(key);
//                    VmHostEnum value = VmHostEnum.valueOf(key);
//                    if (itemApplications != null && itemApplications.size() > 0) {
//                        String dataUnits = "";
//                        ItemApplication item = itemApplications.get(0);
//                        switch (value) {
//                            case MWVM_CPU_USAGE:
//                                if (item.getHosts() != null && item.getHosts().size() > 0) {
//                                    GroupHosts host = item.getHosts().get(0);
//                                    String ip = "";
//                                    String lastStr = "";
//                                    int index = host.getName().indexOf("<");
//                                    if (index > 0) {
//                                        //获取“<>”中以逗号分割得第一个字段，为Ip
//                                        lastStr = host.getName().substring(index + 1, host.getName().length());
//                                        if (lastStr.split(",").length > 0) {
//                                            ip = lastStr.split(",")[0];
//                                        }
//                                    }
//                                    QueryHostParam qParam = new QueryHostParam();
//                                    qParam.setIp(ip);
//                                    qParam.setMonitorServerId(monitorServerId);
//                                    Reply assetsInfoByIp = getAssetsIdByIp(qParam);
//                                    if (assetsInfoByIp.getData() != null) {//有数据，可以跳转
//                                        List<MwTangibleassetsTable> assets = (List<MwTangibleassetsTable>) assetsInfoByIp.getData();
//                                    }
//                                }
//
//                        }
//                    }
//                }
//            }
//        }
        Map<String, ItemApplication> vmtoolByMap = new HashMap();
        List<String> assetsIds = new ArrayList<>();
        QueryHostParam qParams = new QueryHostParam();
        Map<String, MwTangibleassetsTable> assectByConnectMap = new HashMap<>();
        Reply assetsInfoByIps = getAssetsIdByIp(qParams);
        List<String> assetsNames = new ArrayList<>();
        if (assetsInfoByIps.getData() != null) {//有数据，可以跳转
            List<MwTangibleassetsTable> assetsList = (List<MwTangibleassetsTable>) assetsInfoByIps.getData();
            for (MwTangibleassetsTable assets : assetsList) {
                assectByConnectMap.put(assets.getInBandIp() + "_" + assets.getMonitorServerId(), assets);
                assetsNames.add(assets.getAssetsName());
                assetsIds.add(assets.getAssetsId());
            }
            MWZabbixAPIResult results = mwtpServerAPI.itemGetbyFilter(monitorServerId, Arrays.asList(ZabbixItemConstant.VMTOOLS_VERSION), assetsIds);
            if (results.getCode() == 0) {
                List<ItemApplication> vmtoolList = JSONArray.parseArray(results.getData().toString(), ItemApplication.class);
                for (ItemApplication vmtool : vmtoolList) {
                    vmtoolByMap.put(vmtool.getInterfaces().get(0).getIp() + "_" + monitorServerId, vmtool);
                }
            }
        }
        long timet3 = System.currentTimeMillis();

        for (String hostId : lists) {
            List<ItemApplication> items = hostCollect.get(hostId);
            VirtualTableDto virtualTableDto = new VirtualTableDto();
            Double diskTotal = 0.0;
            String diskTotalUnits = "";
            String status = "";
            Double diskUsed = 0.0;
            if (items != null && items.size() > 0) {
                Map<String, List<ItemApplication>> collect = items.stream().collect(Collectors.groupingBy(ItemApplication::getName));
                for (String key : collect.keySet()) {
                    List<ItemApplication> itemApplications = collect.get(key);
                    if (itemApplications != null && itemApplications.size() > 0) {
                        String dataUnits = "";
                        ItemApplication item = itemApplications.get(0);

                        Double lastvalue = item.getLastvalue();

                        try {
                            VmHostEnum value = VmHostEnum.valueOf(key);
                            switch (value) {
                                case MWVM_CPU_USAGE:
                                    dataUnits = UnitsUtil.getValueWithUnits(lastvalue.toString(), item.getUnits());
                                    virtualTableDto.setCpuUsage(dataUnits);
                                    virtualTableDto.setSortCpuUsage(lastvalue);
                                    if (item.getHosts() != null && item.getHosts().size() > 0) {
                                        GroupHosts host = item.getHosts().get(0);
                                        virtualTableDto.setHostId(host.getHostid());
                                        int index = host.getName().indexOf("<");
                                        //判断主机名称是否带有“<>”
                                        //例：host.getName()为 ES_MASTER<10.18.5.80,linuxGuest,CentOS 7 (64 位)>
                                        String name = host.getName();
                                        String lastStr = "";
                                        String ip = "";
                                        if (index > 0) {
                                            //获取“<>”中以逗号分割得第一个字段，为Ip
                                            lastStr = host.getName().substring(index + 1, host.getName().length());
                                            if (lastStr.split(",").length > 0) {
                                                ip = lastStr.split(",")[0];
                                            }
                                            //获取"<>"之前的数据作为name
                                            name = host.getName().substring(0, index);
                                        }
                                        virtualTableDto.setHostName(name);
                                        virtualTableDto.setIpAddress(ip);
                                        boolean isTrue = false;
                                        if (assectByConnectMap != null && assectByConnectMap.get(ip + "_" + monitorServerId) != null) {
                                            virtualTableDto.setIsConnect(1);
                                        } else {
                                            //再次判断多Ip的设备，如果名称前缀重复，也表示已纳管。
                                            for (String assetsName : assetsNames) {
                                                //名称重复（资产名称包含虚拟化名称时），表示可以跳转
                                                if (assetsName.indexOf(name) != -1) {
                                                    isTrue = true;
                                                }
                                            }
                                            if (isTrue) {
                                                virtualTableDto.setIsConnect(1);
                                            } else {
                                                virtualTableDto.setIsConnect(0);
                                            }
                                        }
                                        if (vmtoolByMap != null && vmtoolByMap.get(ip + "_" + monitorServerId) != null) {
                                            ItemApplication vmtools = vmtoolByMap.get(ip + "_" + monitorServerId);
                                            //有数据，可以跳转
                                            virtualTableDto.setVMTools(vmtools.getStringValue());
                                        }
                                    }
                                    break;
                                case MWVM_MEMORY_USAGE:
                                    dataUnits = UnitsUtil.getValueWithUnits(lastvalue.toString(), item.getUnits());
                                    virtualTableDto.setMemoryUsed(dataUnits);
                                    virtualTableDto.setSortMemoryUsed(lastvalue);
                                    break;
                                case MW_DISK_USED:
                                    diskUsed = collect.get(key).stream().collect(Collectors.summingDouble(ItemApplication::getLastvalue));
                                    break;
                                case MW_DISK_TOTAL:
                                    diskTotalUnits = item.getUnits();
                                    diskTotal = collect.get(key).stream().collect(Collectors.summingDouble(ItemApplication::getLastvalue));
                                    break;
                                case VM_POWER_STATE:
                                    status = String.valueOf(item.getLastvalue().longValue());
                                    if ("0".equals(status)) {//未启用
                                        virtualTableDto.setStatus("已关闭");
                                    } else if ("1".equals(status)) {//启用
                                        virtualTableDto.setStatus("运行中");
                                    } else if ("2".equals(status)) {//暂停
                                        virtualTableDto.setStatus("暂停");
                                    } else {//未知
                                        virtualTableDto.setStatus("未知");
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            log.info("没有对应枚举，不做处理");
                            continue;
                        }
                    }
                }
            }
            if (diskTotal != 0) {
                BigDecimal bg = new BigDecimal((diskUsed / diskTotal) * 100);
                double disk = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                virtualTableDto.setDiskUtilization(disk);
            } else {
                virtualTableDto.setDiskUtilization(0.0);
            }
            virtualTableDto.setMonitorServerId(monitorServerId);
            String dataUnits = UnitsUtil.getValueWithUnits(diskTotal.toString(), diskTotalUnits);
            virtualTableDto.setDiskSpace(dataUnits);
            virtualTableDto.setSortDiskSpace(diskTotal);
            list.add(virtualTableDto);
        }
//        long end = System.currentTimeMillis();
//        ////System.out.println("共耗时" + (end - start));
        long timet4 = System.currentTimeMillis();
        log.info("数据整理：" + ((timet11 - timet1) + "、" + (timet12 - timet11) + "、" + (timet2 - timet12)) + "ms；zabbix服务请求：" + (timet3 - timet2) + "ms；最后循环：" + (timet4 - timet3) + "ms");
        return list;

    }

    @ApiOperation(value = "通过storeName  获取DataStorageTableDto")
    public DataStoreTableDto getStorageTableDto(int monitorServerId, String hostId, String storeName) {
        DataStoreTableDto dataStoreTableDto = new DataStoreTableDto();
        if (null != storeName && StringUtils.isNotEmpty(storeName) && null != hostId && StringUtils.isNotEmpty(hostId)) {
            MWZabbixAPIResult resultData = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostId, "Datastore", "[" + storeName + "]");
            if (resultData.getCode() == 0) {
                dataStoreTableDto.setStoreName(storeName);
                JsonNode itemData = (JsonNode) resultData.getData();
                Double total = 0.0;
                Double utilization = 0.0;
                String units = "";
                for (JsonNode item : itemData) {
                    String itemName = item.get("name").asText();
                    String lastValue = item.get("lastvalue").asText();
                    Double sortLastValue = item.get("lastvalue").asDouble();
                    if (itemName.equals("[" + storeName + "]" + ZabbixItemConstant.storeItemName.get(0))) {
                        dataStoreTableDto.setReadLatency(lastValue);
                        dataStoreTableDto.setSortReadLatency(sortLastValue);
                    } else if (itemName.equals("[" + storeName + "]" + ZabbixItemConstant.storeItemName.get(1))) {
                        utilization = sortLastValue;
                    } else if (itemName.equals("[" + storeName + "]" + ZabbixItemConstant.storeItemName.get(2))) {
                        total = sortLastValue;
                        units = item.get("units").asText();
                        String dataUnits = UnitsUtil.getValueWithUnits(lastValue, units);
                        dataStoreTableDto.setTotalCapacity(dataUnits);
                        dataStoreTableDto.setSortTotalCapacity(total);
                    } else if (itemName.equals("[" + storeName + "]" + ZabbixItemConstant.storeItemName.get(3))) {
                        dataStoreTableDto.setWriteLatency(lastValue);
                        dataStoreTableDto.setSortWriteLatency(sortLastValue);
                    }
                }
                Double free = (total * utilization) / 100;
                String dataUnits = UnitsUtil.getValueWithUnits(free.toString(), units);
                dataStoreTableDto.setAvailableCapacity(dataUnits);
                dataStoreTableDto.setSortAvailableCapacity(free);
                if (total != 0) {
                    BigDecimal bg = new BigDecimal(((total - free) / total) * 100);
                    Double store = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    dataStoreTableDto.setStoreUtilization(store);
                } else {
                    dataStoreTableDto.setStoreUtilization(0.0);
                }
            }
        }
        return dataStoreTableDto;
    }

    //根据宿主机组计算所有宿主机之和 （CPU容量、已用CPU、可用CPU、内存容量、已用内存、可用内存、存储容量、已用存储、可用存储）
    private HostComputerDto getHostComputerDtoByHosts(int monitorServerId, List<String> hostIds) {
        Double cUsed = 0.0;
        Double cTotal = 0.0;
        Double mTotal = 0.0;
        Double mUsed = 0.0;
        Double sTotal = 0.0;
        Double sFree = 0.0;
        String cUnits = "";
        String mUnits = "";
        String sUnits = "";
        for (String hostId : hostIds) {
            Double CPUcores = 0.0;
            Double CPUfreQuency = 0.0;
            //先查cpu,memory的数据
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(monitorServerId, ZabbixItemConstant.ITEMNAME.subList(0, 5), hostId);
            if (result.getCode() == 0) {
                JsonNode items = (JsonNode) result.getData();
                if (items.size() > 0) {
                    for (JsonNode item : items) {
                        String itemName = item.get("name").asText();
                        if (ZabbixItemConstant.ITEMNAME.get(0).equals(itemName)) {
                            CPUcores = item.get("lastvalue").asDouble();
                        } else if (ZabbixItemConstant.ITEMNAME.get(1).equals(itemName)) {
                            cUnits = item.get("units").asText();
                            CPUfreQuency = item.get("lastvalue").asDouble();
                        } else if (ZabbixItemConstant.ITEMNAME.get(2).equals(itemName)) {
                            cUsed += item.get("lastvalue").asDouble();
                        } else if (ZabbixItemConstant.ITEMNAME.get(3).equals(itemName)) {
                            mUnits = item.get("units").asText();
                            mTotal += item.get("lastvalue").asDouble();
                        } else if (ZabbixItemConstant.ITEMNAME.get(4).equals(itemName)) {
                            mUsed += item.get("lastvalue").asDouble();
                        }
                    }
                }
                cTotal = cTotal + (CPUcores * CPUfreQuency);
            }
//                获取数据存储名
            List<GroupHosts> storeNames = mwVirtualManage.getStoreNamesByHostId(monitorServerId, hostId);
            MWZabbixAPIResult itemResult = mwtpServerAPI.itemGetbyFilter(monitorServerId, ZabbixItemConstant.ITEMNAME.subList(5, ZabbixItemConstant.ITEMNAME.size()), hostId);
            if (itemResult.getCode() == 0) {
                JsonNode itemResults = (JsonNode) itemResult.getData();
//                根据数据存储名遍历item,找到对应的item，计算他们的和
                for (GroupHosts storeName : storeNames) {
                    Double sSize = 0.0;
                    Double sUtilization = 0.0;
                    if (itemResults.size() > 0) {
                        for (JsonNode storeU : itemResults) {
                            String sName = storeU.get("name").asText();
                            if (sName.equals("[" + storeName.getName() + "]" + ZabbixItemConstant.ITEMNAME.get(5))) {
                                sTotal += storeU.get("lastvalue").asDouble();
                                sSize = storeU.get("lastvalue").asDouble();
                                sUnits = storeU.get("units").asText();
                            } else if (sName.equals("[" + storeName.getName() + "]" + ZabbixItemConstant.ITEMNAME.get(6))) {
                                sUtilization = storeU.get("lastvalue").asDouble();
                            }
                        }
                    }
                    sFree = sFree + ((sSize * sUtilization) / 100);
                }
            }
        }
        HostComputerDto hostComputerDto = new HostComputerDto();

        hostComputerDto.setCPUTotal(UnitsUtil.getValueWithUnits(cTotal.toString(), cUnits));
        hostComputerDto.setCPUUsed(UnitsUtil.getValueWithUnits(cUsed.toString(), cUnits));
        Double cFree = cTotal - cUsed;
        hostComputerDto.setCPUFree(UnitsUtil.getValueWithUnits(cFree.toString(), cUnits));
        hostComputerDto.setMemoryTotal(UnitsUtil.getValueWithUnits(mTotal.toString(), mUnits));
        hostComputerDto.setMemoryUsed(UnitsUtil.getValueWithUnits(mUsed.toString(), mUnits));
        Double mFree = mTotal - mUsed;
        hostComputerDto.setMemoryFree(UnitsUtil.getValueWithUnits(mFree.toString(), mUnits));
        Double sUsed = sTotal - sFree;
        hostComputerDto.setStoreTotal(UnitsUtil.getValueWithUnits(sTotal.toString(), sUnits));
        hostComputerDto.setStoreUsed(UnitsUtil.getValueWithUnits(sUsed.toString(), sUnits));
        hostComputerDto.setStoreFree(UnitsUtil.getValueWithUnits(sFree.toString(), sUnits));
        return hostComputerDto;
    }

    // 根据hostId,获取所有需要展示的item名称和值
    private List<BasicDto> getItemBasicByHostId(int monitorServerId, String hostId, Integer hostSize, Integer vmSize) {
        List<BasicDto> list = new ArrayList<>();
        MWZabbixAPIResult itemsbyHostId = mwtpServerAPI.getItemsbyHostId(monitorServerId, hostId);
        if (itemsbyHostId.getCode() == 0) {
            JsonNode items = (JsonNode) itemsbyHostId.getData();
            if (items.size() > 0) {
                for (BasicItemEnum basicItemEnum : BasicItemEnum.values()) {
                    for (JsonNode item : items) {
                        String name = item.get("name").asText();
                        if (basicItemEnum.getName().equals(name)) {
                            BasicDto basicDto = new BasicDto();
                            basicDto.setName(basicItemEnum.getChName());
                            String newValue = item.get("lastvalue").asText();
                            if (!"0".equals(item.get("valuemapid").asText())) {
                                newValue = mwServerManager.getValueMapById(monitorServerId, item.get("valuemapid").asText(), item.get("lastvalue").asText());
                            } else {
                                if (null != item.get("units").asText() && StringUtils.isNotEmpty(item.get("units").asText())) {
                                    newValue = UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText());
                                }
                            }
                            if ("uptime".equals(item.get("units").asText())) {
                                newValue = SeverityUtils.getLastTime(item.get("lastvalue").asLong());
                            }
                            if ("虚拟机".equals(basicDto.getName())) {
                                basicDto.setValue(vmSize + "");
                            } else {
                                basicDto.setValue(newValue);
                            }

                            list.add(basicDto);
                        }
                    }
                }
            }
        }
        return list;
    }

    //根据主机组名称，模糊或精准查询主机组中的hosts
    public List<GroupHosts> getGroupHosts(int monitorServerId, List<String> names) {
        List<GroupHosts> list = new ArrayList<>();
        MWZabbixAPIResult hostGroupData = mwtpServerAPI.getGroupHostByNames(monitorServerId, names);
        if (hostGroupData.getCode() == 0) {
            JsonNode hostGroups = (JsonNode) hostGroupData.getData();
            if (hostGroups.size() > 0) {
                for (JsonNode hostGroup : hostGroups) {
                    if (hostGroup.get("hosts").size() > 0) {
                        String hosts = String.valueOf(hostGroup.get("hosts"));
                        list.addAll(JSONArray.parseArray(hosts, GroupHosts.class));
                    }
                }
            }
        }
        return list;
    }

    //根据主机组名称，模糊或精准查询主机组中的hostsIds
    private List<String> getGroupHostIds(int monitorServerId, String name) {
        List<String> list = new ArrayList<>();
        MWZabbixAPIResult hostGroupData = mwtpServerAPI.getGroupHostByName(monitorServerId, name);
        if (hostGroupData.getCode() == 0) {
            JsonNode hostGroups = (JsonNode) hostGroupData.getData();
            if (hostGroups.size() > 0) {
                for (JsonNode hostGroup : hostGroups) {
                    if (hostGroup.get("hosts").size() > 0) {
                        hostGroup.get("hosts").forEach(host -> {
                            list.add(host.get("hostid").asText());
                        });
                    }
                }
            }
        }
        return list;
    }


    /**
     * 根据Ip查询assetsid和主键Id
     */
    @Override
    public Reply getAssetsIdByIp(QueryHostParam qParam) {
        List<MwTangibleassetsTable> assetsList = mwVirtualDao.getAssetsIdByIp(qParam);
        //加资产健康状态
        if (assetsList != null && assetsList.size() > 0) {
            Map<Integer, List<String>> groupMap = assetsList.stream()
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String, String> statusMap = new HashMap<>();
            for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                if (value.getKey() != null && value.getKey() > 0) {
                    //有改动-zabbi
                    MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.ASSETS_STATUS, value.getValue());
                    if (!statusData.isFail()) {
                        JsonNode jsonNode = (JsonNode) statusData.getData();
                        if (jsonNode.size() > 0) {
                            for (JsonNode node : jsonNode) {
                                Integer lastvalue = node.get("lastvalue").asInt();
                                String hostId = node.get("hostid").asText();
                                String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                statusMap.put(value.getKey() + ":" + hostId, status);
                            }
                        }
                    }
                    /*statusMap.put(value.getKey() + ":" + value.getValue(), "ABNORMAL");*/
                }
            }
            String status = "";
            for (MwTangibleassetsTable asset : assetsList) {
                if (asset.getMonitorFlag()) {
                    String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
                    if (s != null && StringUtils.isNotEmpty(s)) {
                        status = s;
                    } else {
                        status = "UNKNOWN";
                    }
                } else {
                    status = "SHUTDOWN";
                }
                asset.setItemAssetsStatus(status);
            }
        }
        if (assetsList != null && assetsList.size() > 0) {
            return Reply.ok(assetsList);
        }
        return Reply.fail(280102, "该虚拟机暂无服务器信息！");
    }

    /**
     * 设置虚拟化资产负责人
     */
    @Override
    public Reply setVirtualUser(VirtualUserListPerm params) {
        try {
            //绑定机构
            List<OrgMapper> orgMapper = new ArrayList<>();
            List<GroupMapper> groupMapper = new ArrayList<>();
            List<UserMapper> userMapper = new ArrayList<>();
            List<DataPermissionDto> permissionMapper = new ArrayList<>();
            List<String> typeIds = new ArrayList<>();
            String type = DataType.VIRTUAL.getName();
            for (VirtualUserPerm qParam : params.getPermList()) {
                String typeId = qParam.getTypeId();
                typeIds.add(typeId);
                DataPermissionDto dto = new DataPermissionDto();
                dto.setType(type);     //类型
                dto.setTypeId(typeId);  //数据主键
                dto.setDescription(DataType.valueOf(type).getDesc()); //描述
                List<Integer> userIdList = qParam.getUserIds();
                List<List<Integer>> orgIdList = qParam.getOrgIds();
                List<Integer> groupIdList = qParam.getGroupIds();

                orgIdList.forEach(
                        orgId -> orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(orgId.get(orgId.size() - 1)).type(type).build())
                );
                if (CollectionUtils.isNotEmpty(groupIdList)) {
                    dto.setIsGroup(1);
                } else {
                    dto.setIsGroup(0);
                }
                groupIdList.forEach(
                        groupId -> groupMapper.add(GroupMapper.builder().typeId(typeId).groupId(groupId).type(type).build())
                );
                if (CollectionUtils.isNotEmpty(userIdList)) {
                    dto.setIsUser(1);
                } else {
                    dto.setIsUser(0);
                }
                userIdList.forEach(userIds -> {
                            log.info("userMapper.add,userid:{}", userIds);
                            userMapper.add(UserMapper.builder().typeId(typeId).userId(userIds).type(type).build());
                        }
                );
                permissionMapper.add(dto);
            }
            DeleteDto deleteDto = DeleteDto.builder()
                    .typeIds(typeIds)
                    .type(type)
                    .build();
            if (CollectionUtils.isNotEmpty(deleteDto.getTypeIds())) {
                mwCommonService.deleteMapperAndPerms(deleteDto);
            }
            if (CollectionUtils.isNotEmpty(groupMapper)) {
                mwCommonService.insertGroupMapper(groupMapper);
            }
            if (CollectionUtils.isNotEmpty(userMapper)) {
                mwCommonService.insertUserMapper(userMapper);
            }
            if (CollectionUtils.isNotEmpty(orgMapper)) {
                mwCommonService.insertOrgMapper(orgMapper);
            }
            if (CollectionUtils.isNotEmpty(permissionMapper)) {
                mwCommonService.insertPermissionMapper(permissionMapper);
            }

            return Reply.ok("新增关联负责人成功！");
        } catch (Exception e) {
            return Reply.fail(ErrorConstant.SET_VIRTUAL_USER_CODE_307004, ErrorConstant.SET_VIRTUAL_USER_MSG_307004);
        }
    }


    /*
     * 获取虚拟化资产负责人
     */
    @Override
    public Reply getVirtualUser(VirtualUser qParam) {
        try {
            List<VirtualUserPerm> list = mwVirtualDao.selectVirtualUserList(qParam.getTypeId());
            for (VirtualUserPerm dto : list) {
                // usergroup重新赋值使页面可以显示
                List<Integer> groupIds = new ArrayList<>();
                dto.getGroups().forEach(
                        groupDTO -> groupIds.add(groupDTO.getGroupId())
                );
                dto.setGroupIds(groupIds);
                // user重新赋值
                List<Integer> userIds = new ArrayList<>();
                dto.getPrincipal().forEach(
                        userDTO -> userIds.add(userDTO.getUserId())
                );
                dto.setUserIds(userIds);
                // 机构重新赋值使页面可以显示
                List<List<Integer>> orgNodes = new ArrayList<>();
                if (null != dto.getDepartment() && dto.getDepartment().size() > 0) {
                    dto.getDepartment().forEach(department -> {
                                List<Integer> orgIds = new ArrayList<>();
                                List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                                nodes.forEach(node -> {
                                    if (!"".equals(node))
                                        orgIds.add(Integer.valueOf(node));
                                });
                                orgNodes.add(orgIds);
                            }
                    );
                    dto.setOrgIds(orgNodes);
                }
            }
            return Reply.ok(list);
        } catch (Exception e) {
            return Reply.fail(ErrorConstant.SET_VIRTUAL_USER_CODE_307005, ErrorConstant.SET_VIRTUAL_USER_MSG_307005);
        }
    }


    private List<List> splitList(List messagesList, int groupSize) {
        int length = messagesList.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize; // TODO
        List<List> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(messagesList.subList(fromIndex, toIndex));
        }
        return newList;
    }


    protected void addMapperAndPerm(VirtualPermControlParam param) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(param.getGroupIds())  //用户组
                .userIds(param.getUserIds())  //责任人
                .orgIds(param.getOrgIds())      //机构
                .typeId(String.valueOf(param.getId())) //数据主键
                .type(param.getType())        //链路
                .desc(param.getDesc()).build(); //链路
        mwCommonService.addMapperAndPerm(insertDto);
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param param
     */
    protected void deleteMapperAndPerm(VirtualPermControlParam param) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(param.getId()))
                .type(param.getType())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

}
