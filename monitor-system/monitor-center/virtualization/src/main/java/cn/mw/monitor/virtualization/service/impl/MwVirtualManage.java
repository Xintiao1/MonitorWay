package cn.mw.monitor.virtualization.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.GroupHosts;
import cn.mw.monitor.common.util.VHostTreeDTO;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.virtualization.dto.GroupHostDTO;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2021/5/21 11:00
 * @Version 1.0
 */
@Component
@Slf4j(topic = "logfile")
public class MwVirtualManage {
    public static final String HOSTCOMPUTER = "宿主机";
    public static final String DATACENTER = "[DATACENTER]";
    public static final String CLUSTER = "[CLUSTER]";
    public static final Map<Integer, String> diskItemName = new HashMap<>();
    private static final Map<Integer, String> storeItemName = new HashMap<>();
    private static final Map<Integer, String> urlTree = new HashMap<>();
    private static final String STORE = "store";
    private static final String VHOST = "vHost";
    public static Map mapByAssestInfo = new HashMap();

    static {
        diskItemName.put(0, "MW_DISK_UTILIZATION");
        diskItemName.put(1, "MW_DISK_USED");
        diskItemName.put(2, "MW_DISK_FREE");
        diskItemName.put(3, "MW_DISK_TOTAL");

        storeItemName.put(0, "AVERAGE_DATASTORE_READ_LATENCY");
        storeItemName.put(1, "FREE_DATASTORE_UTILIZATION");
        storeItemName.put(2, "TOTAL_DATASTORE_SIZE");
        storeItemName.put(3, "AVERAGE_DATASTORE_WRITE_LATENCY");

        urlTree.put(0, "assetsHost");
        urlTree.put(1, "dataCenter");
        urlTree.put(2, "cluster");
        urlTree.put(3, "host");
        urlTree.put(4, "virtual");
        urlTree.put(5, "store");
    }

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWUserService userService;

    private Reply getHostTreeByNotAdmin() {
        return getHostTreeByNotAdmin(null);
    }

    /**
     * 权限控制版
     *
     * @return
     */
    private Reply getHostTreeByNotAdmin(Integer userId) {
        GlobalUserInfo globalUser = (userId == null ? userService.getGlobalUser() : userService.getGlobalUser(userId));
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.VIRTUAL);
        mapByAssestInfo = new HashMap();
        log.info("改版后的虚拟化树结构非管理员权限控制");
        try {
//        根据登录用户查询虚拟化资产的hostId,hostName ??
            List<VHostTreeDTO> assetsDtos = getVirtualAssetsInfo();
            List<VHostTreeDTO> assetsInfos = new ArrayList<>();
            log.info("虚拟化数设备：" + JSON.toJSONString(assetsDtos));
            Map<Integer, List<String>> mapHost = new HashMap();
            List<String> hostIdList = new ArrayList<>();
            if (assetsDtos.size() > 0) {
                for (VHostTreeDTO assetsDTO : assetsDtos) {
                    Integer serverId = assetsDTO.getMonitorServerId();
                    String assetHostId = assetsDTO.getAssetHostId();
                    // 二级根据自动发现规则将自动创建的主机进行分类，自动发现规则名称为宿主机而自动创建的主机群组名称带"宿主机"的作为二级
                    List<String> itemLikeHost = getItemIdList(serverId, Arrays.asList(assetHostId));
                    log.info("success to getHostTree ruleNameLikeHost:{}", itemLikeHost);
                    if (itemLikeHost != null && itemLikeHost.size() > 0) {
                        //获取所有parent_discoveryId为自动发现规则itemId的主机群组
                        List<String> groupIds = getHostsByItemId1(serverId, itemLikeHost);

                        log.info("获取所有parent_discoveryId为自动发现规则itemId的主机群组 groupIds:{}", groupIds);
                        if (groupIds.size() > 0) {
                            //获取所有主机群组对应的所有主机信息
                            MWZabbixAPIResult groupHosts = mwtpServerAPI.getGroupHostsByGroupIds(serverId, groupIds);
                            if (groupHosts.getCode() == 0) {
                                JsonNode data = (JsonNode) groupHosts.getData();
                                //将data分为两种，其中[DATACENTER]作为二级，[CLUSTER]作为三级
                                List<GroupHostDTO> dataCenterList = new ArrayList<>();
                                List<GroupHostDTO> clusterList = new ArrayList<>();
                                List<GroupHostDTO> groupHostDTOS = JSONArray.parseArray(data.toString(), GroupHostDTO.class);
                                log.info("success to getHostTree groupHostDTOS:{}", JSONObject.toJSONString(groupHostDTOS));
                                List<GroupHostDTO> clusterRealList = new ArrayList<>();
                                Map<String, List<GroupHosts>> dataCenterMaps = new HashMap<>();
                                Map<String, List<GroupHosts>> clusterMaps = new HashMap<>();
                                Map<String, String> dataCenterGroupIds = new HashMap<>();
                                Map<String, String> clusterGroupId = new HashMap<>();
                                groupHostDTOS.forEach(group -> {
                                    if (group.getName().indexOf(DATACENTER) != -1) {
                                        if (group.getName() != null) {
                                            String dataCenterName = "";
                                            if (group.getName().split(",").length > 1) {
                                                dataCenterName = group.getName().split(",")[0];
                                            }
                                            if (group.getName().split(",").length == 1) {
                                                dataCenterName = group.getName();
                                            }
                                            //将groupId存入map中，方便下面取用
                                            dataCenterGroupIds.put(dataCenterName, group.getGroupid());
                                            if (dataCenterMaps.containsKey(dataCenterName)) {
                                                //重复
                                                List<GroupHosts> lists = dataCenterMaps.get(dataCenterName);
                                                lists.addAll(group.getHosts());
                                                dataCenterMaps.put(dataCenterName, lists);
                                            } else {
                                                //不重复
                                                List<GroupHosts> lists = new ArrayList<>();
                                                lists.addAll(group.getHosts());
                                                dataCenterMaps.put(dataCenterName, lists);
                                            }
                                        }
                                        log.info("success to getHostTree dataCenterSet:{}",JSONObject.toJSONString(dataCenterMaps));
                                    } else if (group.getName().indexOf(CLUSTER) != -1) {
                                        clusterRealList.add(group);
                                        if (group.getName() != null) {
                                            String cluster = "";
                                            if (group.getName().split(",").length > 1) {
                                                cluster = group.getName().split(",")[0];
                                            }
                                            if (group.getName().split(",").length == 1) {
                                                cluster = group.getName();
                                            }
                                            //将groupId存入map中，方便下面取用
                                            clusterGroupId.put(cluster, group.getGroupid());
                                            if (clusterMaps.containsKey(cluster)) {
                                                //重复
                                                List<GroupHosts> lists = clusterMaps.get(cluster);
                                                lists.addAll(group.getHosts());
                                                clusterMaps.put(cluster, lists);
                                            } else {
                                                //不重复
                                                List<GroupHosts> lists = new ArrayList<>();
                                                lists.addAll(group.getHosts());
                                                clusterMaps.put(cluster, lists);
                                            }
                                        }
                                        log.info("success to getHostTree clusterSet:{}", JSONObject.toJSONString(clusterMaps));
                                    }
                                });

                                Map<List<GroupHostDTO>, List<GroupHostDTO>> dataMap = new HashMap();
                                dataCenterMaps.forEach((key, val) -> {
                                    List<GroupHostDTO> dataCenterLists = new ArrayList<>();
                                    GroupHostDTO groupHostDTO = new GroupHostDTO();
                                    groupHostDTO.setGroupid(dataCenterGroupIds != null ? dataCenterGroupIds.get(key) : "");
                                    groupHostDTO.setName(key);
                                    groupHostDTO.setHosts(val);
                                    dataCenterLists.add(groupHostDTO);
                                    List<GroupHostDTO> clusterLists = new ArrayList<>();
                                    clusterMaps.forEach((key1, val1) -> {
                                        val.containsAll(val1);
                                        //如果dataCenter和cluster的GroupHosts相同，则表示为同一资产的两个级别。
                                        List<GroupHosts> distinctList = val.stream().filter(item -> val1.contains(item)).collect(Collectors.toList());
                                        if (distinctList != null && distinctList.size() > 0) {
                                            GroupHostDTO groupHostDTOs = new GroupHostDTO();
                                            groupHostDTOs.setGroupid(clusterGroupId != null ? clusterGroupId.get(key1) : "");
                                            groupHostDTOs.setName(key1);
                                            groupHostDTOs.setHosts(distinctList);
                                            clusterLists.add(groupHostDTOs);
                                        }
                                        dataMap.put(dataCenterLists, clusterLists);
                                    });
                                    clusterList.addAll(clusterLists);
                                    log.info("success to getHostTree dataMap:{}", dataMap);
                                });
                                log.info("success to 2222 getHostTree dataMap2:{}", JSONObject.toJSONString(dataMap));

                                List<String> nameList = new ArrayList<>();
                                List<String> hostList = new ArrayList<>();
                                Map<String, String> maps = new HashMap<>();
                                log.info("List<GroupHostDTO> clusterList"+clusterList);
                                clusterList.forEach(cluster -> {
                                    if (cluster.getHosts() != null && cluster.getHosts().size() > 0) {
                                        cluster.getHosts().forEach(hostId -> {
                                            String firstName = "";
                                            if (cluster.getName() != null) {
                                                firstName = cluster.getName().split(",")[0];
                                            }
                                            int index = hostId.getName().indexOf("<");
                                            //判断主机名称是否带有“<>”
                                            String name = hostId.getName();
                                            if (index > 0) {
                                                name = hostId.getName().substring(0, index);
                                            }
                                            String hostIds = hostId.getHostid();
                                            nameList.add(firstName + "," + name);
                                            hostList.add(hostIds);
                                            maps.put(firstName + "," + name, hostIds);
                                        });
                                    }
                                });
                                //在多线程之前，将zabbix服务数据获取到。
                                Map<String, List<GroupHosts>> groupHostsByMap = new HashMap<>();
                                Map<String, VHostTreeDTO> hostsByMap = new HashMap<>();
                                Map<String, List<GroupHosts>> storeNamesByMap = new HashMap<>();
                                if (nameList != null && nameList.size() > 0) {
                                    log.info("获取 getHostsByMap参数:allTypeIdList" + allTypeIdList+";maps"+maps);
                                    hostsByMap = getHostsByMap(serverId, nameList, allTypeIdList, maps);
                                    log.info("有权限控制:hostsByMap：" + JSONObject.toJSONString(hostsByMap));
                                }
                                if (hostList != null && hostList.size() > 0) {
                                    groupHostsByMap = getGroupHostsByMap(serverId, hostList);
                                    log.info("有权限控制:groupHostsByMap：" + JSONObject.toJSONString(groupHostsByMap));
                                }

                                int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
                                coreSizePool = (coreSizePool < data.size()) ? coreSizePool : data.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
                                ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, data.size(), 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                                List<Future<VHostTreeDTO>> futureList = new ArrayList<>();
                                final Map<String, VHostTreeDTO> hostsByMaps = hostsByMap;
                                final Map<String, List<GroupHosts>> groupHostsByMaps = groupHostsByMap;
                                dataMap.forEach((keys, vals) -> {
                                    keys.forEach(groupHost -> {
                                        GetGroupListThread getGroupListThread = new GetGroupListThread() {
                                            @Override
                                            public VHostTreeDTO call() throws Exception {
                                                return getVHostTreeDTO(groupHost, vals, serverId, false, allTypeIdList, groupHostsByMaps, hostsByMaps);
                                            }
                                        };
                                        Future<VHostTreeDTO> f = executorService.submit(getGroupListThread);
                                        futureList.add(f);
                                    });
                                });
                                for (Future<VHostTreeDTO> f : futureList) {
                                    try {
                                        VHostTreeDTO vHostTreeDTO = f.get(300, TimeUnit.SECONDS);
                                        if (assetsDTO.getAssetHostId().equals(mapByAssestInfo.get(vHostTreeDTO.getId())) && vHostTreeDTO.getUrl().equals("dataCenter")) {
                                            assetsDTO.setMonitorServerId(serverId);
                                            log.info("success to getHostTree second:{}", JSONObject.toJSONString(vHostTreeDTO));
                                            assetsDTO.addChild(vHostTreeDTO);
                                            if (vHostTreeDTO.getHostList() != null) {
                                                //对HostList中的name带有“<>”的内容进行去除
                                                vHostTreeDTO.getHostList().stream().filter(h -> {
                                                    int index = h.getName().indexOf("<");
                                                    if (index > 0) {
                                                        h.setName(h.getName().substring(0, index));
                                                    }
                                                    return true;
                                                }).collect(Collectors.toList());
                                                assetsDTO.addHostList(vHostTreeDTO.getHostList());
                                                Vector<String> hostVector = new Vector<>();
                                                for (GroupHosts gh : vHostTreeDTO.getHostList()) {
                                                    hostVector.add(gh.getHostid());
                                                }
                                                assetsDTO.setHostIdStr(hostVector);
                                            }
                                            if (vHostTreeDTO.getVmList() != null) {
                                                //对VmList中的name带有“<>”的内容进行去除
                                                vHostTreeDTO.getVmList().stream().filter(h -> {
                                                    int index = h.getName().indexOf("<");
                                                    if (index > 0) {
                                                        h.setName(h.getName().substring(0, index));
                                                    }
                                                    return true;
                                                }).collect(Collectors.toList());
                                                assetsDTO.addVmList(vHostTreeDTO.getVmList());
                                                Vector<String> vMVector = new Vector<>();
                                                for (GroupHosts gh : vHostTreeDTO.getVmList()) {
                                                    vMVector.add(gh.getHostid());
                                                }
                                                assetsDTO.setVmIdStr(vMVector);
                                            }
                                            assetsDTO.addStoreList(vHostTreeDTO.getStoreList());
                                            boolean contains1 = false;
                                            boolean contains2 = false;
                                            for (String typeId : allTypeIdList) {
                                                String IdStr = "";
                                                //关联了主机的负责人
                                                if (typeId.indexOf("host") != -1) {
                                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                                    contains1 = assetsDTO.getHostIdStr().contains(IdStr);
                                                    if(contains1){
                                                        break;
                                                    }
                                                } else if (typeId.indexOf("vm") != -1) {//关联了虚拟机的负责人
                                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                                    if(assetsDTO.getVmIdStr().contains(IdStr)){
                                                        contains2 = true;
                                                        break;
                                                    }
                                                }

                                            }
                                            if (contains1 || contains2) {
                                                assetsInfos.add(assetsDTO);
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("fail to getHostTree futureList error:{}", e);
                                        f.cancel(true);
                                    }
                                }
                                executorService.shutdown();
                                log.info("关闭线程池");
                            }
                        }
                    }
                }

            }
            log.info("success to getHostTree return:{}", assetsInfos);
            return Reply.ok(assetsInfos);
        } catch (Exception e) {
            log.error("fail to getHostTree  cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TREE_CODE_307001, ErrorConstant.ASSETS_VCENTER_SELECT_TREE_MSG_307001);
        }
    }

    /**
     * 虚拟化树结构模板调整
     *
     * @return
     */
    public Reply getHostTreeByAdmin() {
        mapByAssestInfo = new HashMap();
        long time1 = System.currentTimeMillis();
        log.info("改版后的虚拟化树结构管理员无权限控制");
        try {
//        根据登录用户查询虚拟化资产的hostId,hostName ??
            List<VHostTreeDTO> assetsDtos = getVirtualAssetsInfo();
            log.info("虚拟化数设备：" + JSON.toJSONString(assetsDtos));
            if (assetsDtos.size() > 0) {
                for (VHostTreeDTO assets : assetsDtos) {
                    Integer serverId = assets.getMonitorServerId();
                    String assetHostId = assets.getAssetHostId();
                    // 二级根据自动发现规则将自动创建的主机进行分类，自动发现规则名称为宿主机而自动创建的主机群组名称带"宿主机"的作为二级
                    List<String> itemLikeHost = getItemIdList(serverId, Arrays.asList(assetHostId));
                    log.info("success to getHostTree ruleNameLikeHost:{}", itemLikeHost);
                    if (itemLikeHost != null && itemLikeHost.size() > 0) {
                        //获取所有parent_discoveryId为自动发现规则itemId的主机群组
                        List<String> groupIds = getHostsByItemId1(serverId, itemLikeHost);
                        if (groupIds.size() > 0) {
                            log.info("获取所有parent_discoveryId为自动发现规则itemId的主机群组 groupIds:{}", JSONObject.toJSONString(groupIds));
                            //获取所有主机群组对应的所有主机信息
                            MWZabbixAPIResult groupHosts = mwtpServerAPI.getGroupHostsByGroupIds(serverId, groupIds);
                            if (groupHosts != null && groupHosts.getCode() == 0) {
                                JsonNode data = (JsonNode) groupHosts.getData();
                                //将data分为两种，其中[DATACENTER]作为二级，[CLUSTER]作为三级
                                List<GroupHostDTO> groupHostDTOS = JSONArray.parseArray(data.toString(), GroupHostDTO.class);
                                log.info("success to getHostTree groupHostDTOS:{}", JSONObject.toJSONString(groupHostDTOS));
                                List<GroupHostDTO> clusterRealList = new ArrayList<>();
                                Map<String, List<GroupHosts>> dataCenterMaps = new HashMap<>();
                                Map<String, List<GroupHosts>> clusterMaps = new HashMap<>();
                                Map<String, String> dataCenterGroupIds = new HashMap<>();
                                Map<String, String> clusterGroupId = new HashMap<>();
                                groupHostDTOS.forEach(group -> {
                                    if (group.getName().indexOf(DATACENTER) != -1) {
                                        if (group.getName() != null) {
                                            String dataCenterName = "";
                                            if (group.getName().split(",").length > 1) {
                                                dataCenterName = group.getName().split(",")[0];
                                            }
                                            if (group.getName().split(",").length == 1) {
                                                dataCenterName = group.getName();
                                            }
                                            //将groupId存入map中，方便下面取用
                                            dataCenterGroupIds.put(dataCenterName, group.getGroupid());
                                            if (dataCenterMaps.containsKey(dataCenterName)) {
                                                //重复
                                                List<GroupHosts> lists = dataCenterMaps.get(dataCenterName);
                                                lists.addAll(group.getHosts());
                                                dataCenterMaps.put(dataCenterName, lists);
                                            } else {
                                                //不重复
                                                List<GroupHosts> lists = new ArrayList<>();
                                                lists.addAll(group.getHosts());
                                                dataCenterMaps.put(dataCenterName, lists);
                                            }
                                        }
                                        log.info("success to getHostTree dataCenterSet:{}", JSONObject.toJSONString(dataCenterMaps));
                                    } else if (group.getName().indexOf(CLUSTER) != -1) {
                                        clusterRealList.add(group);
                                        if (group.getName() != null) {
                                            String cluster = "";
                                            if (group.getName().split(",").length > 1) {
                                                cluster = group.getName().split(",")[0];
                                            }
                                            if (group.getName().split(",").length == 1) {
                                                cluster = group.getName();
                                            }
                                            //将groupId存入map中，方便下面取用
                                            clusterGroupId.put(cluster, group.getGroupid());
                                            if (clusterMaps.containsKey(cluster)) {
                                                //重复
                                                List<GroupHosts> lists = clusterMaps.get(cluster);
                                                lists.addAll(group.getHosts());
                                                clusterMaps.put(cluster, lists);
                                            } else {
                                                //不重复
                                                List<GroupHosts> lists = new ArrayList<>();
                                                lists.addAll(group.getHosts());
                                                clusterMaps.put(cluster, lists);
                                            }
                                        }
                                        log.info("success to getHostTree clusterSet:{}", JSONObject.toJSONString(clusterMaps));
                                    }
                                });
                                Map<List<GroupHostDTO>, List<GroupHostDTO>> dataMap = new HashMap();
                                dataCenterMaps.forEach((key, val) -> {
                                    List<GroupHostDTO> dataCenterLists = new ArrayList<>();
                                    GroupHostDTO groupHostDTO = new GroupHostDTO();
                                    groupHostDTO.setGroupid(dataCenterGroupIds != null ? dataCenterGroupIds.get(key) : "");
                                    groupHostDTO.setName(key);
                                    groupHostDTO.setHosts(val);
                                    dataCenterLists.add(groupHostDTO);
                                    List<GroupHostDTO> clusterLists = new ArrayList<>();
                                    clusterMaps.forEach((key1, val1) -> {
                                        val.containsAll(val1);
                                        //如果dataCenter和cluster的GroupHosts相同，则表示为同一资产的两个级别。
                                        List<GroupHosts> distinctList = val.stream().filter(item -> val1.contains(item)).collect(Collectors.toList());
                                        if (distinctList != null && distinctList.size() > 0) {
                                            GroupHostDTO groupHostDTOs = new GroupHostDTO();
                                            groupHostDTOs.setGroupid(clusterGroupId != null ? clusterGroupId.get(key1) : "");
                                            groupHostDTOs.setName(key1);
                                            groupHostDTOs.setHosts(distinctList);
                                            clusterLists.add(groupHostDTOs);
                                        }
                                        dataMap.put(dataCenterLists, clusterLists);
                                    });
                                    log.info("success to getHostTree dataMap:{}", dataMap);
                                });
                                List<String> nameList = new ArrayList<>();
                                List<String> hostList = new ArrayList<>();
                                clusterRealList.forEach(cluster -> {
                                    if (cluster.getHosts() != null && cluster.getHosts().size() > 0) {
                                        cluster.getHosts().forEach(hostId -> {
                                            String firstName = "";
                                            if (cluster.getName() != null) {
                                                firstName = cluster.getName().split(",")[0];
                                            }
                                            int index = hostId.getName().indexOf("<");
                                            //判断主机名称是否带有“<>”
                                            String name = hostId.getName();
                                            if (index > 0) {
                                                name = hostId.getName().substring(0, index);
                                            }
                                            String hostIds = hostId.getHostid();
                                            nameList.add(firstName + "," + name);
                                            hostList.add(hostIds);
                                        });
                                    }
                                });
                                log.info("success to 3333 getHostTree nameList:{}", nameList);
                                log.info("success to 4444 getHostTree nameList:{}", hostList);
                                //在多线程之前，将zabbix服务数据获取到。
                                Map<String, List<GroupHosts>> groupHostsByMap = new HashMap<>();
                                Map<String, VHostTreeDTO> hostsByMap = new HashMap<>();
                                Map<String, List<GroupHosts>> storeNamesByMap = new HashMap<>();
                                if (nameList != null && nameList.size() > 0) {
                                    hostsByMap = getHostsByMap(serverId, nameList);
                                    log.info("hostsByMap：" + JSONObject.toJSONString(hostsByMap));
                                }
                                if (hostList != null && hostList.size() > 0) {
                                    groupHostsByMap = getGroupHostsByMap(serverId, hostList);
                                    log.info("groupHostsByMap：" + JSONObject.toJSONString(groupHostsByMap));
                                }
                                int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
                                coreSizePool = (coreSizePool < data.size()) ? coreSizePool : data.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
                                ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, data.size(), 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                                List<Future<VHostTreeDTO>> futureList = new ArrayList<>();
                                final Map<String, VHostTreeDTO> hostsByMaps = hostsByMap;
                                final Map<String, List<GroupHosts>> groupHostsByMaps = groupHostsByMap;
                                dataMap.forEach((keys, vals) -> {
                                    keys.forEach(groupHost -> {
                                        GetGroupListThread getGroupListThread = new GetGroupListThread() {
                                            @Override
                                            public VHostTreeDTO call() throws Exception {
                                                //无权限控制版本
                                                return getVHostTreeDTO(groupHost, vals, serverId, false, groupHostsByMaps, hostsByMaps);
                                            }
                                        };
                                        Future<VHostTreeDTO> f = executorService.submit(getGroupListThread);
                                        futureList.add(f);
                                    });
                                });
                                for (Future<VHostTreeDTO> f : futureList) {
                                    try {
                                        VHostTreeDTO vHostTreeDTO = f.get(5, TimeUnit.MINUTES);
                                        if (assets.getAssetHostId().equals(mapByAssestInfo.get(vHostTreeDTO.getId())) && vHostTreeDTO.getUrl().equals("dataCenter")) {
                                            assets.setMonitorServerId(serverId);
                                            log.info("success to getHostTree second:{}", JSONObject.toJSONString(vHostTreeDTO));
                                            assets.addChild(vHostTreeDTO);
                                            if (vHostTreeDTO.getHostList() != null) {
                                                //对HostList中的name带有“<>”的内容进行去除
                                                vHostTreeDTO.getHostList().stream().filter(h -> {
                                                    int index = h.getName().indexOf("<");
                                                    if (index > 0) {
                                                        h.setName(h.getName().substring(0, index));
                                                    }
                                                    return true;
                                                }).collect(Collectors.toList());
                                                assets.addHostList(vHostTreeDTO.getHostList());
                                            }
                                            if (vHostTreeDTO.getVmList() != null) {
                                                //对VmList中的name带有“<>”的内容进行去除
                                                vHostTreeDTO.getVmList().stream().filter(h -> {
                                                    int index = h.getName().indexOf("<");
                                                    if (index > 0) {
                                                        h.setName(h.getName().substring(0, index));
                                                    }
                                                    return true;
                                                }).collect(Collectors.toList());
                                                assets.addVmList(vHostTreeDTO.getVmList());
                                            }
                                            assets.addStoreList(vHostTreeDTO.getStoreList());
                                        }
                                    } catch (Exception e) {
                                        log.error("fail to getHostTree futureList error:{}", e);
                                        f.cancel(true);
                                    }
                                }
                                executorService.shutdown();
                                log.info("关闭线程池");
                            }
                        }
                    }
                }
            }
            long time2 = System.currentTimeMillis();
            log.info("success to getHostTree return:{}", JSONObject.toJSONString(assetsDtos));
            return Reply.ok(assetsDtos);
        } catch (Exception e) {
            log.error("fail to getHostTree  cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TREE_CODE_307001, ErrorConstant.ASSETS_VCENTER_SELECT_TREE_MSG_307001);
        }
    }

    public Reply getHostTree() {
        Reply reply = new Reply();
        String roleId = iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName());
        if (!Strings.isNullOrEmpty(roleId) && !roleId.equals("0")) {
            //非管理员权限
            reply = getHostTreeByNotAdmin();
        } else {//管理员权限
            reply = getHostTreeByAdmin();
        }
        return reply;
    }


    public Reply getHostTree(String roleId, Integer userId) {
        Reply reply = new Reply();
        if (!Strings.isNullOrEmpty(roleId) && !roleId.equals("0")) {
            //非管理员权限
            reply = getHostTreeByNotAdmin(userId);
        } else {//管理员权限
            reply = getHostTreeByAdmin();
        }
        return reply;
    }

    public Reply getAdminHostTree() {
        Reply reply = new Reply();
        reply = getHostTreeByAdmin();
        return reply;
    }

    public Reply getStoreTree() {
        try {
            log.info("改版后的存储的树结构");
            //        根据登录用户查询虚拟化资产的hostId,hostName ??
            List<VHostTreeDTO> assetsDtos = getVirtualAssetsInfo();
            Map<Integer, List<String>> mapHost = new HashMap();
            List<String> hostIdList = new ArrayList<>();
            if (assetsDtos.size() > 0) {
                for (VHostTreeDTO assets : assetsDtos) {
                    Integer serverId = assets.getMonitorServerId();
                    String hostId = assets.getAssetHostId();
                    if (mapHost.containsKey(serverId)) {
                        List<String> hostIdList1 = mapHost.get(serverId);
                        hostIdList1.add(hostId);
                        mapHost.put(serverId, hostIdList);
                    } else {
                        hostIdList = new ArrayList<>();
                        hostIdList.add(hostId);
                        mapHost.put(assets.getMonitorServerId(), hostIdList);
                    }
                }
                mapHost.forEach((k, v) -> {
                    List<String> itemLikeHost = getItemIdList(k, v);
                    if (itemLikeHost != null && itemLikeHost.size() > 0) {
                        List<String> groupIds = getHostsByItemId1(k, itemLikeHost);
                        if (groupIds.size() > 0) {
                            MWZabbixAPIResult groupHosts = mwtpServerAPI.getGroupHostsByGroupIds(k, groupIds);
                            if (groupHosts.getCode() == 0) {
                                JsonNode data = (JsonNode) groupHosts.getData();
                                //将data分为两种，其中[DATACENTER]作为二级，[CLUSTER]作为三级
                                List<GroupHostDTO> dataCenterList = new ArrayList<>();
                                List<GroupHostDTO> clusterList = new ArrayList<>();
                                List<GroupHostDTO> groupHostDTOS = JSONArray.parseArray(data.toString(), GroupHostDTO.class);
                                groupHostDTOS.forEach(group -> {
                                    if (group.getName().indexOf(DATACENTER) != -1) {
                                        dataCenterList.add(group);
                                    } else if (group.getName().indexOf(CLUSTER) != -1) {
                                        clusterList.add(group);
                                    }
                                });

                                List<String> nameList = new ArrayList<>();
                                List<String> hostList = new ArrayList<>();
                                clusterList.forEach(cluster -> {
                                    if (cluster.getHosts() != null && cluster.getHosts().size() > 0) {
                                        cluster.getHosts().forEach(hostId -> {
                                            String firstName = "";
                                            if (cluster.getName() != null) {
                                                firstName = cluster.getName().split(",")[0];
                                            }
                                            int index = hostId.getName().indexOf("<");
                                            //判断主机名称是否带有“<>”
                                            String name = hostId.getName();
                                            if (index > 0) {
                                                name = hostId.getName().substring(0, index);
                                            }
                                            String hostIds = hostId.getHostid();
                                            nameList.add(firstName + "," + name);
                                            hostList.add(hostIds);
                                        });
                                    }
                                });
                                //在多线程之前，将zabbix服务数据获取到。
                                Map<String, List<GroupHosts>> groupHostsByMap = new HashMap<>();
                                Map<String, VHostTreeDTO> hostsByMap = new HashMap<>();
                                Map<String, List<GroupHosts>> storeNamesByMap = new HashMap<>();
                                if (nameList != null && nameList.size() > 0) {
                                    hostsByMap = getHostsByMap(k, nameList);
                                    String jsonNameList = JSONArray.toJSON(nameList).toString();
                                    log.info("jsonNameList：" + jsonNameList);
                                }
                                if (hostList != null && hostList.size() > 0) {
                                    String jsonHostList = JSONArray.toJSON(hostList).toString();
                                    log.info("jsonHostList：" + jsonHostList);
                                    groupHostsByMap = getGroupHostsByMap(k, hostList);
                                }

                                int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
                                coreSizePool = (coreSizePool < data.size()) ? coreSizePool : data.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
                                ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, data.size(), 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                                List<Future<VHostTreeDTO>> futureList = new ArrayList<>();
                                final Map<String, VHostTreeDTO> hostsByMaps = hostsByMap;
                                final Map<String, List<GroupHosts>> groupHostsByMaps = groupHostsByMap;
                                dataCenterList.forEach(groupHost -> {
                                    GetGroupListThread getGroupListThread = new GetGroupListThread() {
                                        @Override
                                        public VHostTreeDTO call() throws Exception {
                                            return getVHostTreeDTO(groupHost, clusterList, k, true, groupHostsByMaps, hostsByMaps);
                                        }
                                    };
                                    Future<VHostTreeDTO> f = executorService.submit(getGroupListThread);
                                    futureList.add(f);
                                });

                                for (Future<VHostTreeDTO> f : futureList) {
                                    try {
                                        VHostTreeDTO vHostTreeDTO = f.get(300, TimeUnit.SECONDS);
                                        for (VHostTreeDTO assets : assetsDtos) {
                                            if (assets.getAssetHostId().equals(mapByAssestInfo.get(vHostTreeDTO.getId())) && vHostTreeDTO.getUrl().equals("dataCenter")) {
                                                assets.addChild(vHostTreeDTO);
                                                assets.addHostList(vHostTreeDTO.getHostList());
                                                assets.addVmList(vHostTreeDTO.getVmList());
                                                assets.addStoreList(vHostTreeDTO.getStoreList());
                                            }
                                        }
                                    } catch (Exception e) {
                                        f.cancel(true);
                                        log.error("虚拟化查询缺少数据 cause:{}", e);
                                    }
                                }
                                executorService.shutdown();
                                log.info("关闭线程池");
                            }
                        }
                    }
                });
            }

            log.info("success to getStoreTree return:{}", assetsDtos);
            return Reply.ok(assetsDtos);
        } catch (Exception e) {
            log.error("fail to getStoreTree cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETS_STORE_SELECT_TREE_CODE_307003, ErrorConstant.ASSETS_STORE_SELECT_TREE_MSG_307003);
        }
    }

    //    根据登录用户查询虚拟化资产的hostId,hostName
    public List<VHostTreeDTO> getVirtualAssetsInfo() {
        List<VHostTreeDTO> hostDtos = new ArrayList<>();
        List<MwTangibleassetsDTO> assetsList = mwAssetsManager.getAssetsByAssetsTypeId(5);
        if (assetsList.size() > 0) {
            assetsList.forEach(mwTangAssets -> {
                VHostTreeDTO hostDto = new VHostTreeDTO();
                hostDto.setMonitorServerId(mwTangAssets.getMonitorServerId());
                hostDto.setId(mwTangAssets.getId());
                hostDto.setIp(mwTangAssets.getInBandIp());
                hostDto.setSelectIp(mwTangAssets.getInBandIp());
                hostDto.setLabel(mwTangAssets.getAssetsName());
                hostDto.setAssetHostId(mwTangAssets.getAssetsId());
                hostDto.setUrl(urlTree.get(0));
                hostDto.setMonitorServerName(mwTangAssets.getMonitorServerName());
                hostDtos.add(hostDto);

            });
        }
        return hostDtos;
    }

    /**
     * 根据主机自动发现规则查找规则名称中有宿主机的规则名称及信息
     *
     * @param monitorServerId
     * @param hostIds
     * @return
     */
    public List<String> getItemIdList(Integer monitorServerId, List<String> hostIds) {
        MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostIdList(monitorServerId, hostIds);
        List<String> itemIdList = new ArrayList<>();
        if (dRuleByHostId != null && dRuleByHostId.getCode() == 0) {
            JsonNode resultData = (JsonNode) dRuleByHostId.getData();
            log.info("根据主机自动发现规则查找规则 getItemIdList:{}", JSONObject.toJSONString(resultData));
            if (resultData.size() > 0) {
                for (JsonNode resultDatum : resultData) {
                    String name = resultDatum.get("name").asText();
                    if (null != name && StringUtils.isNotEmpty(name)) {
                        if (name.contains(HOSTCOMPUTER)) {
                            String itemId = resultDatum.get("itemid").asText();
                            String hostId = resultDatum.get("hostid").asText();
                            mapByAssestInfo.put(itemId, hostId);
                            itemIdList.add(itemId);
                        }
                    }
                }
            }
            log.info("根据主机自动发现规则查找规则 mapByAssestInfo:{}", JSONObject.toJSONString(mapByAssestInfo));
        }
        return itemIdList;
    }

    /**
     * 根据主机自动发现规则查找规则名称中有宿主机的规则名称及信息(原版)
     *
     * @param monitorServerId
     * @param hostId
     * @return
     */
    public JsonNode getDRuleNameLikeHost(Integer monitorServerId, String hostId) {
        MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostId);
        if (dRuleByHostId.getCode() == 0) {
            JsonNode resultData = (JsonNode) dRuleByHostId.getData();
            if (resultData.size() > 0) {
                for (JsonNode resultDatum : resultData) {
                    String name = resultDatum.get("name").asText();
                    if (null != name && StringUtils.isNotEmpty(name)) {
                        if (name.contains(HOSTCOMPUTER)) {
                            return resultDatum;
                        }
                    }
                }
            }
        }
        return null;
    }

    @ApiOperation(value = "通过itemId获取主机组Id")
    public List<String> getHostsByItemId1(int monitorServerId, List<String> itemIdList) {
        ArrayList<String> groupIds = new ArrayList<>();
        if (null != itemIdList && itemIdList.size() > 0) {
            MWZabbixAPIResult groups = mwtpServerAPI.getHostGroup(monitorServerId);
            if (groups != null && groups.getCode() == 0) {
                log.info("通过itemId获取主机组Id groups:{}", JSONObject.toJSONString(groups));
                JsonNode data = (JsonNode) groups.getData();
                if (data.size() > 0) {
                    data.forEach(group -> {
                        for (String itemId : itemIdList) {
                            if (group.get("discoveryRule").size() > 0) {
                                if (itemId.equals(group.get("discoveryRule").get("itemid").asText())) {
                                    groupIds.add(group.get("groupid").asText());
                                    mapByAssestInfo.put(group.get("groupid").asText(), mapByAssestInfo.get(itemId));
                                }
                            }
                        }
                    });
                }
                log.info("通过itemId获取主机组Id mapByAssestInfo:{}", JSONObject.toJSONString(mapByAssestInfo));
            }
        }
        return groupIds;
    }

    /**
     * 改版权限控制
     *
     * @param groupHost
     * @param clusterList
     * @param MonitorServerId
     * @param isStore
     * @return
     */
    @ApiOperation(value = "通过groupHost 及clusterList 以及一级数据 获取二级VHostTreeDTO")
    public VHostTreeDTO getVHostTreeDTO(GroupHostDTO groupHost, List<GroupHostDTO> clusterList, Integer MonitorServerId, boolean isStore, List<String> allTypeIdList, Map<String, List<GroupHosts>> groupHostsByMap, Map<String, VHostTreeDTO> hostsByMap) {
        VHostTreeDTO second = new VHostTreeDTO();
        try {
            second.setMonitorServerId(MonitorServerId);
            second.setId(groupHost.getGroupid());
            second.setLabel(groupHost.getName().indexOf(DATACENTER) != -1 ? groupHost.getName().substring(groupHost.getName().indexOf("]") + 1) : groupHost.getName());
            second.setUrl(urlTree.get(1));
            List<GroupHosts> hosts = groupHost.getHosts();
//            second.addHostList(hosts);
            List<GroupHosts> secondHostList = new ArrayList<>();
            List<String> hostIdList = new ArrayList<>();
            if (hosts.size() > 0) {//当主机组中还包含未被三级包含的主机时，直接显示在二级下面
                hosts.forEach(hostId -> {
                    hostIdList.add(hostId.getHostid());
                });
            }
            Map<String, List<GroupHosts>> storeNamesByMap = new HashMap<>();
            if (hostIdList != null && hostIdList.size() > 0) {
                storeNamesByMap = getStoreNamesByMapData(groupHostsByMap);
            }

            if (hosts != null && hosts.size() > 0) {
                final Map<String, VHostTreeDTO> hostsByMaps = hostsByMap;
                final Map<String, List<GroupHosts>> groupHostsByMaps = groupHostsByMap;
                final Map<String, List<GroupHosts>> storeNamesByMaps = storeNamesByMap;
                //三级 根据clusterList宿主机中的主机分组是否包含在当前dataCenter宿主机分组中，判断是否为三级
                clusterList.forEach(cluster -> {
                    if (cluster.getHosts() != null && cluster.getHosts().size() > 0) {
                        if (hosts.containsAll(cluster.getHosts())) {
                            //最好把匹配上的做删除？？
                            VHostTreeDTO third = new VHostTreeDTO();
                            third.setMonitorServerId(MonitorServerId);
                            third.setId(cluster.getGroupid());
                            third.setLabel(cluster.getName().indexOf(CLUSTER) != -1 ? cluster.getName().substring(cluster.getName().indexOf("]") + 1) : cluster.getName());
                            third.setUrl(urlTree.get(2));
                            //分类所含主机
                            third.addHostList(cluster.getHosts());
                            List<GroupHosts> fourthHostList = new ArrayList<>();
                            cluster.getHosts().forEach(hostId -> {
                                //根据cluster宿主机名称获取根据cluster宿主机名称创建的主机群组和主机群组下的所有主机，所有主机作为最底层展示
                                int index = hostId.getName().indexOf("<");
                                //判断主机名称是否带有“<>”
                                String name = hostId.getName();
                                if (index > 0) {
                                    name = hostId.getName().substring(0, index);
                                }
                                log.info("虚拟化没有数据的原因name:{}", name);
                                VHostTreeDTO fourth = new VHostTreeDTO();
                                boolean contains1 = false;
                                boolean contains2 = false;
                                if (hostsByMaps != null && hostsByMaps.get(name) != null) {
                                    fourth = hostsByMaps.get(name);
                                    log.info("有权限控制::虚拟化树结构fourth::{}", fourth);
                                    if (fourth.getVmList() != null && fourth.getVmList().size() > 0) {
                                        Vector<String> hostVector4 = new Vector<>();
                                        hostVector4.add(hostId.getHostid());
                                        if (fourth.getVmList().size() > 0) {
                                            fourthHostList.add(hostId);
                                        }
                                        fourth.setHostIdStr(hostVector4);
                                        if (!Strings.isNullOrEmpty(fourth.getLabel())) {
                                            if (index > 0) {
                                                //获取“<>”中以逗号分割得第一个字段，为Ip
                                                String lastStr = hostId.getName().substring(index + 1, hostId.getName().length() - 1);
                                                if (lastStr.split(",").length > 0) {
                                                    String ip = lastStr.split(",")[0];
                                                    fourth.setSelectIp(ip);
                                                }
                                            }
                                            //此等级下有存储数据
                                            if (groupHostsByMaps != null && groupHostsByMaps.get(hostId.getHostid()) != null) {
                                                fourth.setStoreList(groupHostsByMaps.get(hostId.getHostid()));
                                            } else {
                                                fourth.setStoreList(new ArrayList<>());
                                            }
                                            //此等级下有主机
                                            fourth.addHost(hostId);
                                            fourth.getHostList();
                                            log.info("有权限控制::虚拟化树结构fourth22::{}", fourth);
                                            for (String typeId : allTypeIdList) {
                                                String IdStr = "";
                                                if (typeId.indexOf("vm") != -1) {//关联了虚拟机的负责人
                                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                                    if (fourth.getVmIdStr().contains(IdStr)) {
                                                        contains2 = true;
                                                        break;
                                                    }
                                                }
                                                //关联了主机的负责人
                                                if (typeId.indexOf("host") != -1) {
                                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                                    contains1 = fourth.getHostIdStr().contains(IdStr);
                                                    if (contains1) {
                                                        break;
                                                    }
                                                }

                                            }
                                        }
                                        if (contains1 || contains2) {
                                            third.addChild(fourth);
                                            //此等级下存储数据
                                            third.addStoreList(fourth.getStoreList());
                                            //此等级下虚拟机数据
                                            third.addVmList(fourth.getVmList());
                                        }
                                    }

                                }
                            });
                            Vector<String> vector = new Vector<>();
                            if (third.getVmList() != null && third.getVmList().size() > 0) {
                                for (GroupHosts gh : third.getVmList()) {
                                    vector.add(gh.getHostid());
                                }
                                third.setVmIdStr(vector);
                            }
                            third.setHostList(fourthHostList);
                            Vector<String> hostVector = new Vector<>();
                            for (GroupHosts gh : fourthHostList) {
                                hostVector.add(gh.getHostid());
                            }
                            third.setHostIdStr(hostVector);
                            if (isStore) {
                                third.setChildren(getStoreEndChildren(MonitorServerId, cluster.getHosts()));
                            }
                            boolean contains1 = false;
                            boolean contains2 = false;
                            for (String typeId : allTypeIdList) {
                                String IdStr = "";
                                //关联了主机的负责人
                                if (typeId.indexOf("host") != -1) {
                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                    contains1 = third.getHostIdStr().contains(IdStr);
                                    if(contains1){
                                        break;
                                    }
                                } else if (typeId.indexOf("vm") != -1) {//关联了虚拟机的负责人
                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                    if(third.getVmIdStr().contains(IdStr)){
                                        contains2 = true;
                                        break;
                                    }
                                }
                            }
                            if (contains1 || contains2) {
                                second.addChild(third);
                                //此等级下存储数据
                                second.addStoreList(third.getStoreList());
                                //此等级下虚拟机数据
                                second.addVmList(third.getVmList());

                            }
                            secondHostList.addAll(third.getHostList());

                            //将三级及以下的都找到以后，将已经配上对的做清空
                            hosts.removeAll(cluster.getHosts());
                        }
                    }
                });

                Vector<String> vector = new Vector<>();
                for (GroupHosts gh : second.getVmList()) {
                    vector.add(gh.getHostid());
                }
                second.setHostList(secondHostList);
                second.setVmIdStr(vector);
                if (hosts.size() > 0) {//当主机组中还包含未被三级包含的主机时，直接显示在二级下面
                    hosts.forEach(hostId -> {
                        //主机群组下的所有主机，所有主机作为最底层展示
                        int index1 = hostId.getName().indexOf("<");
                        //判断主机名称是否带有“<>”
                        String name1 = hostId.getName();
                        if (index1 > 0) {
                            name1 = hostId.getName().substring(0, index1);
                        }
//                        VHostTreeDTO fourth = getHostsByGroupName(MonitorServerId, name1);
                        VHostTreeDTO fourth = new VHostTreeDTO();
                        if (hostsByMaps != null && hostsByMaps.get(name1) != null) {
                            fourth = hostsByMaps.get(name1);
                        }
                        if (index1 > 0) {
                            //获取“<>”中以逗号分割得第一个字段，为Ip
                            String lastStr = hostId.getName().substring(index1 + 1, hostId.getName().length() - 1);
                            if (lastStr.split(",").length > 0) {
                                String ip = lastStr.split(",")[0];
                                fourth.setSelectIp(ip);
                            }
                        }
                        if (isStore) {
                            List<VHostTreeDTO> storeEndChildren = getStoreEndChildren(MonitorServerId, Arrays.asList(hostId));
                            second.addChildren(storeEndChildren);
                            //此等级下存储数据
                            if (storeNamesByMaps != null && storeNamesByMaps.get(hostId.getHostid()) != null) {
                                second.addStoreList(storeNamesByMaps.get(hostId.getHostid()));
                            }
                        } else {
                            if (!Strings.isNullOrEmpty(fourth.getLabel())) {
                                //此等级下有存储数据
                                if (storeNamesByMaps != null && storeNamesByMaps.get(hostId.getHostid()) != null) {
                                    fourth.setStoreList(storeNamesByMaps.get(hostId.getHostid()));
                                } else {
                                    fourth.setStoreList(new ArrayList<>());
                                }
                                //此等级下有主机
                                fourth.addHost(hostId);
                                //此等级下存储数据
                                second.addStoreList(fourth.getStoreList());
                                second.addChild(fourth);
                            }
                        }
                        //此等级下虚拟机数据
                        second.addVmList(fourth.getVmList());
                    });
                }
                Vector<String> vector2 = new Vector<>();
                for (GroupHosts gh : second.getVmList()) {
                    vector2.add(gh.getHostid());
                }
                second.setVmIdStr(vector2);
            }
        } catch (Exception e) {
            log.error("fail to getVHostTreeDTO  groupHost:{}, clusterList:{}, assets:{}, isStore:{}  cause:{}", groupHost, clusterList, MonitorServerId, isStore, e);
        }
        log.info("有权限控制::success to getVHostTreeDTO second:{}", second);
        return second;
    }

    /**
     * 改版 无权限控制
     *
     * @param groupHost
     * @param clusterList
     * @param MonitorServerId
     * @param isStore
     * @return
     */
    @ApiOperation(value = "通过groupHost 及clusterList 以及一级数据 获取二级VHostTreeDTO")
    public VHostTreeDTO getVHostTreeDTO(GroupHostDTO groupHost, List<GroupHostDTO> clusterList, Integer MonitorServerId, boolean isStore, Map<String, List<GroupHosts>> groupHostsByMap, Map<String, VHostTreeDTO> hostsByMap) {
        VHostTreeDTO second = new VHostTreeDTO();
        try {
            second.setMonitorServerId(MonitorServerId);
            second.setId(groupHost.getGroupid());
            second.setLabel(groupHost.getName().indexOf(DATACENTER) != -1 ? groupHost.getName().substring(groupHost.getName().indexOf("]") + 1) : groupHost.getName());
            second.setUrl(urlTree.get(1));
            List<GroupHosts> hosts = groupHost.getHosts();
            second.addHostList(hosts);
            List<String> hostIdList = new ArrayList<>();
            if (hosts.size() > 0) {//当主机组中还包含未被三级包含的主机时，直接显示在二级下面
                hosts.forEach(hostId -> {
                    hostIdList.add(hostId.getHostid());
                });
            }
            Map<String, List<GroupHosts>> storeNamesByMap = new HashMap<>();
            if (hostIdList != null && hostIdList.size() > 0) {
                storeNamesByMap = getStoreNamesByMapData(groupHostsByMap);
            }

            if (hosts != null && hosts.size() > 0) {
                final Map<String, VHostTreeDTO> hostsByMaps = hostsByMap;
                final Map<String, List<GroupHosts>> groupHostsByMaps = groupHostsByMap;
                final Map<String, List<GroupHosts>> storeNamesByMaps = storeNamesByMap;
                //三级 根据clusterList宿主机中的主机分组是否包含在当前dataCenter宿主机分组中，判断是否为三级
                clusterList.forEach(cluster -> {
                    if (cluster.getHosts() != null && cluster.getHosts().size() > 0) {
                        if (hosts.containsAll(cluster.getHosts())) {
                            log.info("success to getVHostTreeDTO cluster:{}", cluster);
                            //最好把匹配上的做删除？？
                            VHostTreeDTO third = new VHostTreeDTO();
                            third.setMonitorServerId(MonitorServerId);
                            third.setId(cluster.getGroupid());
                            third.setLabel(cluster.getName().indexOf(CLUSTER) != -1 ? cluster.getName().substring(cluster.getName().indexOf("]") + 1) : cluster.getName());
                            third.setUrl(urlTree.get(2));
                            //分类所含主机
                            third.addHostList(cluster.getHosts());
                            cluster.getHosts().forEach(hostId -> {
                                //根据cluster宿主机名称获取根据cluster宿主机名称创建的主机群组和主机群组下的所有主机，所有主机作为最底层展示
                                int index = hostId.getName().indexOf("<");
                                //判断主机名称是否带有“<>”
                                String name = hostId.getName();
                                if (index > 0) {
                                    name = hostId.getName().substring(0, index);
                                }
                                log.info("虚拟化fourth-name:{}", name);
                                if (hostsByMaps != null && hostsByMaps.get(name) != null) {
                                    VHostTreeDTO fourth = hostsByMaps.get(name);
                                    log.info("无权限控制虚拟化fourth:{}", fourth);
                                    if (!Strings.isNullOrEmpty(fourth.getLabel())) {
                                        if (index > 0) {
                                            //获取“<>”中以逗号分割得第一个字段，为Ip
                                            String lastStr = hostId.getName().substring(index + 1, hostId.getName().length() - 1);
                                            if (lastStr.split(",").length > 0) {
                                                String ip = lastStr.split(",")[0];
                                                fourth.setSelectIp(ip);
                                            }
                                        }
                                        //此等级下有存储数据
                                        if (groupHostsByMaps != null && groupHostsByMaps.get(hostId.getHostid()) != null) {
                                            log.info("success to getVHostTreeDTO fourth.setStoreList:{}", groupHostsByMaps.get(hostId.getHostid()));
                                            fourth.setStoreList(groupHostsByMaps.get(hostId.getHostid()));
                                        } else {
                                            fourth.setStoreList(new ArrayList<>());
                                            log.info("fourth.setStoreList空数组： " + groupHostsByMaps + "；hostid：" + hostId.getHostid());
                                        }
                                        //此等级下有主机
                                        fourth.addHost(hostId);
                                        third.addChild(fourth);
                                        //此等级下存储数据
                                        third.addStoreList(fourth.getStoreList());
                                        //此等级下虚拟机数据
                                        third.addVmList(fourth.getVmList());
                                        log.info("success to getVHostTreeDTO third:{}", third);
                                    }
                                }
                            });
                            if (isStore) {
                                third.setChildren(getStoreEndChildrenByListData(MonitorServerId, cluster.getHosts(), storeNamesByMaps));
                            }
                            second.addChild(third);
                            //此等级下存储数据
                            second.addStoreList(third.getStoreList());
                            //此等级下虚拟机数据
                            second.addVmList(third.getVmList());
                            //将三级及以下的都找到以后，将已经配上对的做清空
                            hosts.removeAll(cluster.getHosts());
                        }
                    }
                });
                if (hosts.size() > 0) {//当主机组中还包含未被三级包含的主机时，直接显示在二级下面
                    hosts.forEach(hostId -> {
                        //主机群组下的所有主机，所有主机作为最底层展示
                        int index1 = hostId.getName().indexOf("<");
                        //判断主机名称是否带有“<>”
                        String name1 = hostId.getName();
                        if (index1 > 0) {
                            name1 = hostId.getName().substring(0, index1);
                        }
                        VHostTreeDTO fourth = new VHostTreeDTO();
                        if (hostsByMaps != null && hostsByMaps.get(name1) != null) {
                            fourth = hostsByMaps.get(name1);
                            if (index1 > 0) {
                                //获取“<>”中以逗号分割得第一个字段，为Ip
                                String lastStr = hostId.getName().substring(index1 + 1, hostId.getName().length() - 1);
                                if (lastStr.split(",").length > 0) {
                                    String ip = lastStr.split(",")[0];
                                    fourth.setSelectIp(ip);
                                }
                            }
                        }
                        if (isStore) {
                            List<VHostTreeDTO> storeEndChildren = getStoreEndChildren(MonitorServerId, Arrays.asList(hostId));
                            second.addChildren(storeEndChildren);
                            //此等级下存储数据
                            if (storeNamesByMaps != null && storeNamesByMaps.get(hostId.getHostid()) != null) {
                                second.addStoreList(storeNamesByMaps.get(hostId.getHostid()));
                            }
                        } else {
                            if (!Strings.isNullOrEmpty(fourth.getLabel())) {
                                //此等级下有存储数据
                                if (storeNamesByMaps != null && storeNamesByMaps.get(hostId.getHostid()) != null) {
                                    fourth.setStoreList(storeNamesByMaps.get(hostId.getHostid()));
                                } else {
                                    fourth.setStoreList(new ArrayList<>());
                                }
                                //此等级下有主机
                                fourth.addHost(hostId);
                                //此等级下存储数据
                                second.addStoreList(fourth.getStoreList());
                                second.addChild(fourth);
                            }
                        }
                        //此等级下虚拟机数据
                        second.addVmList(fourth.getVmList());
                    });
                }
                Vector<String> vector2 = new Vector<>();
                for (GroupHosts gh : second.getVmList()) {
                    vector2.add(gh.getHostid());
                }
                second.setVmIdStr(vector2);
            }
        } catch (Exception e) {
            log.error("fail to getVHostTreeDTO  groupHost:{}, clusterList:{}, assets:{}, isStore:{}  cause:{}", groupHost, clusterList, MonitorServerId, isStore, e);
        }
        log.info("success to getVHostTreeDTO second:{}", second);
        return second;
    }

    /**
     * 通过groupNames获取主机组Id   权限控制版
     *
     * @param monitorServerId
     * @param groupNameList
     * @param allTypeIdList
     * @param maps
     * @return
     */
    @ApiOperation(value = "通过groupNames获取主机组Id")
    public Map<String, VHostTreeDTO> getHostsByMap(int monitorServerId, List<String> groupNameList, List<String> allTypeIdList, Map<String, String> maps) {
        VHostTreeDTO vHostTreeDTO = new VHostTreeDTO();
        Map<String, VHostTreeDTO> map = new HashMap();
        if (null != groupNameList && groupNameList.size() > 0) {
            MWZabbixAPIResult groups = mwtpServerAPI.getGroupHostByNames(monitorServerId, groupNameList);
            if (groups != null && groups.getCode() == 0) {
                JsonNode data = (JsonNode) groups.getData();
                List<GroupHostDTO> groupHostDTOS = JSONArray.parseArray(data.toString(), GroupHostDTO.class);
                log.info("select to groupHostDTOS data:{}", JSONObject.toJSONString(groupHostDTOS));
                if (groupHostDTOS.size() > 0) {
                    for (GroupHostDTO gs : groupHostDTOS) {
                        vHostTreeDTO = new VHostTreeDTO();
                        boolean contains1 = false;
                        boolean contains2 = false;
                        boolean isFlagVm = false;
                        List<GroupHosts> vmList = new ArrayList<>();
                        for (GroupHosts host : gs.getHosts()) {
                            VHostTreeDTO hostDto = new VHostTreeDTO();
                            hostDto.setMonitorServerId(monitorServerId);
                            String name = "";
                            if (host != null && host.getName() != null) {
                                int index = host.getName().indexOf("<");
                                if (index > 0) {
                                    name = host.getName().substring(0, index);
                                    //获取“<>”中以逗号分割得第一个字段，为Ip
                                    String lastStr = host.getName().substring(index + 1, host.getName().length());
                                    if (lastStr.split(",").length > 0) {
                                        String ip = lastStr.split(",")[0];
                                        hostDto.setSelectIp(ip);
                                    }
                                } else {
                                    name = host.getName();
                                }
                            }
                            hostDto.setPId(maps.get(gs.getName()));
                            hostDto.setLabel(name);
                            hostDto.setId(host.getHostid());
                            hostDto.setUrl(urlTree.get(4));
                            Vector<String> vector = new Vector<>();
                            vector.add(host.getHostid());
                            hostDto.setVmIdStr(vector);
                            hostDto.addVm(host);//最后一层只有虚拟机
                            hostDto.setFlag(VHOST);
                            for (String typeId : allTypeIdList) {
                                if (StringUtils.isBlank(typeId)) continue;
                                String IdStr = "";
                                if (typeId.indexOf("vm") != -1) {//关联了虚拟机的负责人
                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                    contains2 = hostDto.getVmIdStr().contains(IdStr);
                                    if (contains2) {
                                        vHostTreeDTO.addChild(hostDto);
                                        vmList.add(host);
                                        isFlagVm = true;
                                    }
                                }
                            }
                            for (String typeId : allTypeIdList) {
                                if (StringUtils.isBlank(typeId)) continue;
                                String IdStr = "";
                                if (typeId.indexOf("host") != -1) {//关联了虚拟机的负责人
                                    IdStr = typeId.substring(typeId.indexOf("_") + 1, typeId.indexOf("_", typeId.indexOf("_") + 1));
                                    contains1 = IdStr.equals(maps.get(gs.getName()));
                                    //主机关联负责人 虚拟机没关联时，添加虚拟机
                                    if (contains1 && !isFlagVm) {
                                        vHostTreeDTO.addChild(hostDto);
                                        vmList.add(host);
                                    }
                                }
                            }

//                            //主机关联了负责人
//                            if (contains1 ) {
//                                //仅主机关联了负责人，默认显示主机下所有的虚拟机
//                                if(!isFlagVm){
//                                    vHostTreeDTO.addChild(hostDto);
//                                }
//                                vmList.add(host);
//                            }
                        }
                        vHostTreeDTO.setVmList(vmList);//此等级下需要有的虚拟机
                        Vector<String> vector = new Vector<>();
                        for (GroupHosts gh : vmList) {
                            vector.add(gh.getHostid());
                        }
                        vHostTreeDTO.setVmIdStr(vector);
                        vHostTreeDTO.setId(gs.getGroupid());
                        vHostTreeDTO.setMonitorServerId(monitorServerId);
                        String name1 = "";
                        if (!Strings.isNullOrEmpty(gs.getName())) {
                            int s = gs.getName().indexOf(",", 0);
                            String gsName = "";
                            if (s != -1) {
                                gsName = gs.getName().substring(s + 1);
                            } else {
                                gsName = gs.getName();
                            }
                            int index = gsName.indexOf("<");
                            if (index > 0) {
                                name1 = gsName.substring(0, index);
                            } else {
                                name1 = gsName;
                            }
                        }
                        vHostTreeDTO.setLabel(name1);
                        vHostTreeDTO.setUrl(urlTree.get(3));
                        if (vHostTreeDTO.getVmList() != null && vHostTreeDTO.getVmList().size() > 0) {
                            map.put(name1, vHostTreeDTO);
                        }
                    }
                }
            }
        }
        return map;
    }


    /**
     * 通过groupNames获取主机组Id   无权限控制版
     *
     * @param monitorServerId
     * @param groupNameList
     * @return
     */
    @ApiOperation(value = "通过groupNames获取主机组Id")
    public Map<String, VHostTreeDTO> getHostsByMap(int monitorServerId, List<String> groupNameList) {
        VHostTreeDTO vHostTreeDTO = new VHostTreeDTO();
        Map<String, VHostTreeDTO> map = new HashMap();
        if (null != groupNameList && groupNameList.size() > 0) {
            MWZabbixAPIResult groups = mwtpServerAPI.getGroupHostByNames(monitorServerId, groupNameList);
            if (!groups.isFail()) {
                JsonNode data = (JsonNode) groups.getData();
                List<GroupHostDTO> groupHostDTOS = JSONArray.parseArray(data.toString(), GroupHostDTO.class);
                if (groupHostDTOS.size() > 0) {
                    for (GroupHostDTO gs : groupHostDTOS) {
                        vHostTreeDTO = new VHostTreeDTO();
                        for (GroupHosts host : gs.getHosts()) {
                            VHostTreeDTO hostDto = new VHostTreeDTO();
                            hostDto.setMonitorServerId(monitorServerId);
                            String name = "";
                            if (host != null && host.getName() != null) {
                                int index = host.getName().indexOf("<");
                                if (index > 0) {
                                    name = host.getName().substring(0, index);
                                    //获取“<>”中以逗号分割得第一个字段，为Ip
                                    String lastStr = host.getName().substring(index + 1, host.getName().length());
                                    if (lastStr.split(",").length > 0) {
                                        String ip = lastStr.split(",")[0];
                                        hostDto.setSelectIp(ip);
                                    }
                                } else {
                                    name = host.getName();
                                }
                            }
                            hostDto.setLabel(name);
                            hostDto.setId(host.getHostid());
                            hostDto.setUrl(urlTree.get(4));
                            Vector<String> vector = new Vector<>();
                            vector.add(host.getHostid());
                            hostDto.setVmIdStr(vector);
                            hostDto.addVm(host);//最后一层只有虚拟机
                            hostDto.setFlag(VHOST);
                            vHostTreeDTO.addChild(hostDto);
                        }
                        vHostTreeDTO.addVmList(gs.getHosts());//此等级下需要有的虚拟机
                        Vector<String> vector = new Vector<>();
                        for (GroupHosts gh : gs.getHosts()) {
                            vector.add(gh.getHostid());
                        }
                        vHostTreeDTO.setVmIdStr(vector);
                        vHostTreeDTO.setId(gs.getGroupid());
                        vHostTreeDTO.setMonitorServerId(monitorServerId);
                        String name1 = "";
                        gs.setName("");
                        if (!Strings.isNullOrEmpty(gs.getName())) {
                            int s = gs.getName().indexOf(",", 0);
                            String gsName = "";
                            if (s != -1) {
                                gsName = gs.getName().substring(s + 1);
                            } else {
                                gsName = gs.getName();
                            }
                            int index = gsName.indexOf("<");
                            if (index > 0) {
                                name1 = gsName.substring(0, index);
                            } else {
                                name1 = gsName;
                            }
                        }
                        vHostTreeDTO.setLabel(name1);
                        vHostTreeDTO.setUrl(urlTree.get(3));
                        map.put(name1, vHostTreeDTO);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 改版后，和getGroupHostsByMap公用一个zabbix服务数据
     *
     * @param datas
     * @return
     */
    @ApiOperation(value = "根据数据重新组装结构")
    public Map<String, List<GroupHosts>> getStoreNamesByMapData(Map<String, List<GroupHosts>> datas) {
        Map<String, List<GroupHosts>> map = new HashMap();
        if (datas != null && datas.size() > 0) {
            datas.forEach((k, v) -> {
                for (GroupHosts g : v) {
                    List<GroupHosts> list = new ArrayList<>();
                    list.add(g);
                    map.put("[" + g.getName() + "]TOTAL_DATASTORE_SIZE", list);
                }
            });
        }
        log.info("根据数据重新组装结构:getStoreNamesByMapData" +JSONObject.toJSONString(map));
        return map;
    }

    public List<GroupHosts> getStoreNamesByHostIds(int monitorServerId, List<String> hostIds) {
        List<GroupHosts> storeNames = new ArrayList<>();
        Set<GroupHosts> set = new HashSet<>();
        if (hostIds.size() > 0) {
            MWZabbixAPIResult result = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostIds, "Datastore", "TOTAL_DATASTORE_SIZE");
            if (result.getCode() == 0) {
                JsonNode data = (JsonNode) result.getData();
                data.forEach(itemName -> {
                    int l = itemName.get("name").asText().indexOf("[");
                    int r = itemName.get("name").asText().indexOf("]");
                    String hostId = itemName.get("name").asText();
                    GroupHosts build = GroupHosts.builder().hostid(itemName.get("hostid").asText()).name(itemName.get("name").asText().substring(l + 1, r)).build();
                    set.add(build);
                });
            }
        }
        storeNames.addAll(set);
        return storeNames;
    }


    /**
     * 根据主机信息数组，获取主机数组中所有的存储数据
     *
     * @param hostList
     * @return
     */
    public List<VHostTreeDTO> getStoreEndChildrenByListData(int monitorServerId, List<GroupHosts> hostList, Map<String, List<GroupHosts>> maps) {
        List<VHostTreeDTO> resultList = new ArrayList<>();
        if (maps != null && maps.size() > 0) {
            maps.forEach((k, v) -> {
                List<GroupHosts> storeNames = v;
                if (storeNames.size() > 0) {
                    storeNames.forEach(storeName -> {
                        for (GroupHosts groupHosts : hostList) {
                            if (!Strings.isNullOrEmpty(groupHosts.getHostid()) && (groupHosts.getHostid()).equals(storeName.getHostid())) {
                                VHostTreeDTO end = new VHostTreeDTO();
                                end.setMonitorServerId(monitorServerId);
                                end.setLabel(storeName.getName());
                                end.setId(storeName.getHostid());
                                end.setUrl(urlTree.get(5));
                                end.setFlag(STORE);
                                end.addStore(storeName);
                                resultList.add(end);
                            }
                        }
                    });
                }
            });
        }
        return resultList;
    }


    /**
     * 根据主机信息数组，获取主机数组中所有的存储数据
     *
     * @param monitorServerId
     * @param hostList
     * @return
     */
    public List<VHostTreeDTO> getStoreEndChildren(int monitorServerId, List<GroupHosts> hostList) {
        List<VHostTreeDTO> resultList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        hostList.forEach(host -> {
            list.add(host.getHostid());
        });
        List<GroupHosts> storeNames = getStoreNamesByHostIds(monitorServerId, list);
        if (storeNames.size() > 0) {
            storeNames.forEach(storeName -> {
                VHostTreeDTO end = new VHostTreeDTO();
                end.setMonitorServerId(monitorServerId);
                end.setLabel(storeName.getName());
                end.setId(storeName.getHostid());
                end.setUrl(urlTree.get(5));
                end.setFlag(STORE);
                end.addStore(storeName);
                resultList.add(end);
            });
        }
        return resultList;
    }


    @ApiOperation(value = "通过hostId 获取DataStore的名字")
    public Map<String, List<GroupHosts>> getGroupHostsByMap(int monitorServerId, List<String> hostIdList) {
        Map<String, List<GroupHosts>> map = new HashMap();
        if (null != hostIdList && hostIdList.size() > 0) {
            MWZabbixAPIResult result = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostIdList, "Datastore", "TOTAL_DATASTORE_SIZE");
            if (result.getCode() == 0) {
                JsonNode data = (JsonNode) result.getData();
                data.forEach(itemName -> {
                    int l = itemName.get("name").asText().indexOf("[");
                    int r = itemName.get("name").asText().indexOf("]");
                    String hostId = itemName.get("hostid").asText();
                    GroupHosts build = GroupHosts.builder().hostid(hostId).name(itemName.get("name").asText().substring(l + 1, r)).build();
                    if (map.containsKey(hostId)) {
                        List<GroupHosts> groupHosts = map.get(hostId);
                        groupHosts.add(build);
                        map.put(hostId, groupHosts);
                    } else {
                        List<GroupHosts> groupHosts = new ArrayList<>();
                        groupHosts.add(build);
                        map.put(hostId, groupHosts);
                    }
                });
            }
        }
        return map;
    }

    /**
     * 原版
     *
     * @param monitorServerId
     * @param hostId
     * @return
     */
    @ApiOperation(value = "通过hostId 获取DataStore的名字")
    public List<GroupHosts> getStoreNamesByHostId(int monitorServerId, String hostId) {
        List<GroupHosts> storeNames = new ArrayList<>();
        if (null != hostId && StringUtils.isNotEmpty(hostId)) {
            MWZabbixAPIResult result = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostId, "Datastore", "TOTAL_DATASTORE_SIZE");
            if (result.getCode() == 0) {
                JsonNode data = (JsonNode) result.getData();
                data.forEach(itemName -> {
                    int l = itemName.get("name").asText().indexOf("[");
                    int r = itemName.get("name").asText().indexOf("]");
                    GroupHosts build = GroupHosts.builder().hostid(hostId).name(itemName.get("name").asText().substring(l + 1, r)).build();
                    storeNames.add(build);
                });
            }
        }
        return storeNames;
    }

    public String readTxt(String txtPath) {
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer sb = new StringBuffer();
                String text = null;
                while ((text = bufferedReader.readLine()) != null) {
                    sb.append(text);
                }
                return sb.toString();
            } catch (Exception e) {
                log.error("fail to readTxt case:{}", e);
            }
        }
        return null;
    }
}
