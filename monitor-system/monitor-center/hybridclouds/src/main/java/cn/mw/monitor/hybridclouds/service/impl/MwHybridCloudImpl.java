package cn.mw.monitor.hybridclouds.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.hybridclouds.dao.MwHybridCloudDao;
import cn.mw.monitor.hybridclouds.dto.*;
import cn.mw.monitor.hybridclouds.service.MwHybridCloudService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import joptsimple.internal.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2021/6/8
 */
@Service
public class MwHybridCloudImpl implements MwHybridCloudService {
    private static final Logger logger = LoggerFactory.getLogger("MwHybridCloudImpl");
    public static final Map<Integer, String> diskItemName = new HashMap<>();
    private static final Map<Integer, String> urlTree = new HashMap<>();

    @Autowired
    private MwHybridCloudDao mwHybridCloudDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    static {
        diskItemName.put(0, "MW_DISK_UTILIZATION");
        diskItemName.put(1, "MW_DISK_USED");
        diskItemName.put(2, "MW_DISK_FREE");
        diskItemName.put(3, "MW_DISK_TOTAL");

        urlTree.put(0, "assetsHost");
        urlTree.put(1, "group");
        urlTree.put(2, "hybridCloud");
    }

    @Override
    public Reply getAllTree() {
        StringBuilder stringBuilder = new StringBuilder();
        //将hybridcloud::vHost作为key，获取存入redis中值。
        stringBuilder.append("hybridcloud");
        stringBuilder.append("::vHost");
        String key = stringBuilder.toString();

        String hString = redisTemplate.opsForValue().get(key);
        logger.info("saveVirtualTree 查看信息  key:{} hString:{}", key, hString);
        if (null != hString && StringUtils.isNotEmpty(hString) && !"null".equals(hString)) {
            //TODO 从Redis中取值
            return Reply.ok();
        } else {
            logger.info("get data from zabbix");
            return getHybridCloudTree();
        }
    }

    @Override
    public Reply getBasic(QueryNewHostParam qParam) {
        try {
            List<BasicDto> list = new ArrayList<>();
            if (null != qParam.getAssetHostId() && StringUtils.isNotEmpty(qParam.getAssetHostId())) {
//                如果qParam.getAssetsHostId()不为空，说明是最高级
                List<MwTangibleassetsTable> assetsInfoList = mwHybridCloudDao.getAssetsIdById(qParam);
                if (assetsInfoList != null && assetsInfoList.size() > 0) {
                    MwTangibleassetsTable assetsInfo = assetsInfoList.get(0);
                    BasicDto basicDto = new BasicDto();
                    basicDto.setName("主机名称");
                    basicDto.setValue(assetsInfo.getAssetsName());
                    BasicDto basicDto1 = new BasicDto();
                    basicDto1.setName("IP地址");
                    basicDto1.setValue(assetsInfo.getInBandIp());
                    BasicDto basicDto2 = new BasicDto();
                    basicDto2.setName("资产类型");
                    basicDto2.setValue(assetsInfo.getAssetsTypeName());
                    BasicDto basicDto3 = new BasicDto();
                    basicDto3.setName("资产子类型");
                    basicDto3.setValue(assetsInfo.getAssetsTypeSubName());
                    BasicDto basicDto4 = new BasicDto();
                    basicDto4.setName("服务器");
                    basicDto4.setValue(assetsInfo.getMonitorServerName());
                    BasicDto basicDto5 = new BasicDto();
                    basicDto5.setName("厂商");
                    basicDto5.setValue(assetsInfo.getManufacturer());
                    BasicDto basicDto6 = new BasicDto();
                    basicDto6.setName("主机群组");
                    basicDto6.setValue("0");
                    if (qParam.getGroupList() != null) {
                        basicDto6.setValue(qParam.getGroupList().size() + "");
                    }
                    BasicDto basicDto7 = new BasicDto();
                    basicDto7.setName("混合云");
                    basicDto7.setValue("0");
                    if (qParam.getHcList() != null) {
                        basicDto7.setValue(qParam.getHcList().size() + "");
                    }
                    list.add(basicDto);
                    list.add(basicDto1);
                    list.add(basicDto2);
                    list.add(basicDto3);
                    list.add(basicDto4);
                    list.add(basicDto5);
                    list.add(basicDto6);
                    list.add(basicDto7);
                }
            } else if (!Strings.isNullOrEmpty(qParam.getFlag()) && "group".equals(qParam.getFlag())) {
                //根据flag判断点击树的层级。1级：assetsHost   2级：group   3级：hybridCloud
                BasicDto basicDto = new BasicDto();
                basicDto.setName("群组名称");
                basicDto.setValue(qParam.getGroupName());
                BasicDto basicDto1 = new BasicDto();
                basicDto1.setName("混合云");
                basicDto1.setValue(qParam.getHcList().size() + "");
                list.add(basicDto);
                list.add(basicDto1);
            } else {//三级 混合云
                if ("hybridCloud".equals(qParam.getFlag())) {
                    List<BasicDto> itemBasicByHostId = getItemBasicByHostId(qParam.getMonitorServerId(), qParam.getHostId());
                    list.addAll(itemBasicByHostId);
                }
            }
            logger.info("VIRTUAL_LOG[]getBasic[]qParam获取不同的table所有数据[]{}[]", qParam.getHostId());
            logger.info("getBasic 获取基本数据,运行成功结束");
            return Reply.ok(list);
        } catch (Exception e) {
            logger.error("fail to getHostTable QueryHostParam:{} cause:{}", qParam.getHostId(), e);
            return Reply.fail(ErrorConstant.ASSETS_HYBRIDCLOUD_SELECT_BASE_CODE_318003, ErrorConstant.ASSETS_HYBRIDCLOUD_SELECT_BASE_MSG_318003);
        }
    }

    @Override
    public Reply getHhyTable(QueryNewHostParam qParam) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            List<GroupHosts> list = qParam.getHcList();
            List<VirtualTableDto> virtualTableDtos = new ArrayList<>();
            pageInfo.setTotal(list.size());
            if (qParam.getSortField() == null || StringUtils.isEmpty(qParam.getSortField())) {//如果不排序的话正常查询分页的内容
                list = pageList.getList(list, qParam.getPageNumber(), qParam.getPageSize());
            }
            if (list.size() > 0) {
                //获取list对象中属性为hostid 的重新组成一个数组
                List<String> collect = list.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                virtualTableDtos = getVirtualTableDtoByHostId(qParam.getMonitorServerId(), collect, qParam.getItemNames());
            }
            if (qParam.getSortField() != null && StringUtils.isNotEmpty(qParam.getSortField())) {
                ListSortUtil<VirtualTableDto> finalHostTableDtos = new ListSortUtil<>();
                String sort = "sort" + qParam.getSortField().substring(0, 1).toUpperCase() + qParam.getSortField().substring(1);
                //查看当前属性名称是否在对象中
                try {
                    Field field = VirtualTableDto.class.getDeclaredField(sort);
                    finalHostTableDtos.sort(virtualTableDtos, sort, qParam.getSortMode());
                } catch (NoSuchFieldException e) {
                    logger.info("has no field", e);
                    finalHostTableDtos.sort(virtualTableDtos, qParam.getSortField(), qParam.getSortMode());
                }

                virtualTableDtos = pageList.getList(virtualTableDtos, qParam.getPageNumber(), qParam.getPageSize());

            }
            pageInfo.setList(virtualTableDtos);
            logger.info("VIRTUAL_LOG[]getVMsTable[]qParam获取VMstable所有数据[]{}[]", qParam.getSortField());
            logger.info("getVMsTable 获取VMstable数据,运行成功结束");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            logger.error("fail to getVMsTable QueryHostParam:{} cause:{}", qParam.getSortField(), e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_CODE_307002, ErrorConstant.ASSETS_VCENTER_SELECT_TABLE_MSG_307002);
        }
    }

    @ApiOperation(value = "通过hostId获取VirtualTableDto")
    public List<VirtualTableDto> getVirtualTableDtoByHostId(int monitorServerId, Object hostIds, List<String> itemNames) {
        List<ItemApplication> itemInfos = new ArrayList<>();
        MWZabbixAPIResult itemsbyHostIds = mwtpServerAPI.getItemsbyHostIds(monitorServerId, hostIds);
        List<VirtualTableDto> list = new ArrayList<>();
        if (!itemsbyHostIds.isFail()) {
            itemInfos = JSONArray.parseArray(itemsbyHostIds.getData().toString(), ItemApplication.class);
            for (ItemApplication item : itemInfos) {
                if ("MW_ALIYUN_ASSETS_RAW".equals(item.getName())) {
                    VirtualTableDto itemInfo = JSONObject.parseObject(item.getStringValue().toString(), VirtualTableDto.class);
                    itemInfo.setHostId(item.getHostid());
                    if (itemInfo != null && itemInfo.getVpcAttributes() != null && itemInfo.getVpcAttributes().getPrivateIpAddress() != null && itemInfo.getVpcAttributes().getPrivateIpAddress().getIpAddress() != null) {
                        itemInfo.setVpcIp((JSONArray.parseArray(itemInfo.getVpcAttributes().getPrivateIpAddress().getIpAddress()).get(0)) + "");
                    }
                    itemInfo.setMemory(itemInfo.getMemory() + "MB");
                    list.add(itemInfo);
                }
            }
        }
        return list;
    }


    // 根据hostId,获取所有需要展示的item名称和值
    private List<BasicDto> getItemBasicByHostId(int monitorServerId, String hostId) {
        List<ItemApplication> itemInfos = new ArrayList<>();
        List<BasicDto> list = new ArrayList<>();
        MWZabbixAPIResult itemsbyHostId = mwtpServerAPI.getItemsbyHostId(monitorServerId, hostId);
        if (!itemsbyHostId.isFail()) {
            itemInfos = JSONArray.parseArray(itemsbyHostId.getData().toString(), ItemApplication.class);
            for (ItemApplication item : itemInfos) {
                if ("MW_ALIYUN_ASSETS_RAW".equals(item.getName())) {
                    VirtualTableDto itemInfo = JSONObject.parseObject(item.getStringValue().toString(), VirtualTableDto.class);
                    itemInfo.setHostId(item.getHostid());
                    if (itemInfo != null && itemInfo.getVpcAttributes() != null && itemInfo.getVpcAttributes().getPrivateIpAddress() != null && itemInfo.getVpcAttributes().getPrivateIpAddress().getIpAddress() != null) {
                        itemInfo.setVpcIp((JSONArray.parseArray(itemInfo.getVpcAttributes().getPrivateIpAddress().getIpAddress()).get(0)) + "");
                    }
                    BasicDto basicDto = new BasicDto();
                    basicDto.setName("网络连接类型");
                    basicDto.setValue(itemInfo.getInstanceNetworkType());
                    BasicDto basicDto1 = new BasicDto();
                    basicDto1.setName("vpc地址");
                    basicDto1.setValue((JSONArray.parseArray(itemInfo.getVpcAttributes().getPrivateIpAddress().getIpAddress()).get(0)) + "");
                    BasicDto basicDto2 = new BasicDto();
                    basicDto2.setName("内存");
                    basicDto2.setValue(itemInfo.getMemory() + "MB");
                    BasicDto basicDto8 = new BasicDto();
                    basicDto8.setName("操作系统类型");
                    basicDto8.setValue(itemInfo.getOSType());
                    BasicDto basicDto3 = new BasicDto();
                    basicDto3.setName("操作系统名称");
                    basicDto3.setValue(itemInfo.getOSName());
                    BasicDto basicDto4 = new BasicDto();
                    basicDto4.setName("区域");
                    basicDto4.setValue(itemInfo.getRegionId());
                    BasicDto basicDto5 = new BasicDto();
                    basicDto5.setName("开始时间");
                    basicDto5.setValue(DateUtils.formatDateTime(DateUtils.parse(itemInfo.getStartTime(), "yyyy-MM-dd'T'HH:mm'Z'")));
                    BasicDto basicDto6 = new BasicDto();
                    basicDto6.setName("过期时间");
                    basicDto6.setValue(DateUtils.formatDateTime(DateUtils.parse(itemInfo.getExpiredTime(), "yyyy-MM-dd'T'HH:mm'Z'")));

                    list.add(basicDto);
                    list.add(basicDto1);
                    list.add(basicDto2);
                    list.add(basicDto3);
                    list.add(basicDto4);
                    list.add(basicDto5);
                    list.add(basicDto6);
                    list.add(basicDto8);
                }
            }
        }

        return list;
    }


    public Reply getHybridCloudTree() {
        try {
//        根据登录用户查询混合云资产的hostId,hostName
            List<HybridCloudTreeDTO> assetsDtos = getVirtualAssetsInfo();
            if (assetsDtos.size() > 0) {
                assetsDtos.forEach(assets -> {
                    //根据hostId查询自动发现规则的 itemId
                    JsonNode ruleNameLikeHost = getDRuleNameLikeHost(assets.getMonitorServerId(), assets.getAssetHostId());
                    logger.info("success to getHostTree ruleNameLikeHost:{}", ruleNameLikeHost);
                    if (ruleNameLikeHost != null) {
                        //获取所有parent_discoveryId为自动发现规则itemId的  主机群组
                        List<String> groupIds = getHostsByItemId(assets.getMonitorServerId(), ruleNameLikeHost.get("itemid").asText());
                        logger.info("获取所有parent_discoveryId为自动发现规则itemId的主机群组 groupIds:{}", groupIds);
                        if (groupIds.size() > 0) {
                            //获取所有主机群组对应的所有主机信息
                            MWZabbixAPIResult groupHosts = mwtpServerAPI.getGroupHostsByGroupIds(assets.getMonitorServerId(), groupIds);

                            logger.info("success to getHostTree groupHosts:{}", groupHosts);
                            JsonNode data = (JsonNode) groupHosts.getData();
                            //第二级数据list
                            List<GroupHostDTO> hostList = new ArrayList<>();
                            List<GroupHost> groupList = new ArrayList<>();
                            List<GroupHostDTO> clusterList = new ArrayList<>();
                            List<GroupHostDTO> groupHostDTOS = JSONArray.parseArray(data.toString(), GroupHostDTO.class);
                            groupHostDTOS.forEach(group -> {
                                GroupHost groupHost = new GroupHost();
                                hostList.add(group);
                                groupHost.setGroupid(group.getGroupid());
                                groupHost.setName(group.getName());
                                groupList.add(groupHost);
                            });

                            int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
                            coreSizePool = (coreSizePool < data.size()) ? coreSizePool : data.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
                            ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, data.size(), 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                            List<Future<HybridCloudTreeDTO>> futureList = new ArrayList<>();

                            groupHostDTOS.forEach(groupHost -> {
                                GetGroupListThread getGroupListThread = new GetGroupListThread() {
                                    @Override
                                    public HybridCloudTreeDTO call() throws Exception {
                                        return getHybridCloudTreeDTO(groupHost, hostList, assets, false);
                                    }
                                };
                                Future<HybridCloudTreeDTO> f = executorService.submit(getGroupListThread);
                                futureList.add(f);
                            });

                            for (Future<HybridCloudTreeDTO> f : futureList) {
                                try {
                                    HybridCloudTreeDTO hybridCloudTreeDTO = f.get(10, TimeUnit.SECONDS);
                                    for (HybridCloudTreeDTO h : hybridCloudTreeDTO.getChildren()) {
                                        logger.info("success to getHostTree second:{}", hybridCloudTreeDTO.getChildren());
                                        HybridCloudTreeDTO hybridCloudTreeDTOs = hybridCloudTreeDTO.getChildren().get(0);
                                        assets.addChild(hybridCloudTreeDTOs);
                                        assets.addGroupList(groupList);
                                        assets.addHcList(hybridCloudTreeDTOs.getHcList());
                                    }
                                } catch (Exception e) {
                                    logger.error("success to getHostTree error:{}", e);
                                    f.cancel(true);
                                }
                            }
                            executorService.shutdown();
                            logger.info("关闭线程池");
                        }
                    }
                });
            }
            logger.info("success to getHostTree return:{}", assetsDtos);
            return Reply.ok(assetsDtos);
        } catch (Exception e) {
            logger.error("fail to getHostTree  cause:{}", e);
            return Reply.fail(ErrorConstant.ASSETS_VCENTER_SELECT_TREE_CODE_307001, ErrorConstant.ASSETS_VCENTER_SELECT_TREE_MSG_307001);
        }
    }

    /**
     * 根据主机自动发现规则查找规则名称中有宿主机的规则名称及信息
     *
     * @param monitorServerId
     * @param hostId
     * @return
     */
    public JsonNode getDRuleNameLikeHost(Integer monitorServerId, String hostId) {
        MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostId);
        JsonNode resultData = (JsonNode) dRuleByHostId.getData();
        if (resultData.size() > 0) {
            for (JsonNode resultDatum : resultData) {
                String name = resultDatum.get("name").asText();
                if (null != name && StringUtils.isNotEmpty(name)) {
                    return resultDatum;
                }
            }
        }
        return null;
    }

    @ApiOperation(value = "通过itemId获取主机组Id")
    public List<String> getHostsByItemId(int monitorServerId, String itemId) {
        ArrayList<String> groupIds = new ArrayList<>();
        if (null != itemId && StringUtils.isNotEmpty(itemId)) {
            MWZabbixAPIResult groups = mwtpServerAPI.getHostGroup(monitorServerId);
            JsonNode data = (JsonNode) groups.getData();
            if (data.size() > 0) {
                data.forEach(group -> {
                    if (group.get("discoveryRule").size() > 0) {
                        if (itemId.equals(group.get("discoveryRule").get("itemid").asText())) {
                            groupIds.add(group.get("groupid").asText());
                        }
                    }
                });
            }
        }
        return groupIds;
    }

    @ApiOperation(value = "通过groupHost 及clusterList 以及一级数据 获取二级HybridCloudTreeDTO")
    public HybridCloudTreeDTO getHybridCloudTreeDTO(GroupHostDTO groupHost, List<GroupHostDTO> hostList, HybridCloudTreeDTO assets, boolean isStore) {
        HybridCloudTreeDTO one = new HybridCloudTreeDTO();
        List<String> hostIds = new ArrayList<>();
        if (hostList.get(0).getHosts() != null && hostList.get(0).getHosts().size() > 0) {
            //三级 根据clusterList宿主机中的主机分组是否包含在当前dataCenter宿主机分组中，判断是否为三级
            hostList.forEach(host -> {
                HybridCloudTreeDTO second = new HybridCloudTreeDTO();
                second.setMonitorServerId(assets.getMonitorServerId());
                second.setId(groupHost.getGroupid());
                second.setLabel(host.getName());
                second.setUrl(urlTree.get(1));
                second.setFlag("group");

                host.getHosts().forEach(h -> {
                    hostIds.add(h.getHostid());
                });
                MWZabbixAPIResult hostNameList = mwtpServerAPI.getItemDataByAppName(assets.getMonitorServerId(), hostIds, "ASSETS", "InstanceName");
                Map<String,String> map = new HashMap();
                if (!hostNameList.isFail()) {
                    JsonNode datas = (JsonNode) hostNameList.getData();
                    for (JsonNode data : datas) {
                        String hostName = data.get("lastvalue").asText();
                        String hostId = data.get("hostid").asText();
                        map.put(hostId,hostName);
                    }
                }
                host.getHosts().forEach(h -> {
                    HybridCloudTreeDTO third = new HybridCloudTreeDTO();
                    third.setMonitorServerId(assets.getMonitorServerId());
                    third.setId(h.getHostid());
                    h.setName(map.get(h.getHostid()));
                    third.setLabel(map.get(h.getHostid()));
                    third.setUrl(urlTree.get(2));
                    third.setFlag("hybridCloud");
                    GroupHosts g = new GroupHosts();
                    g.setHostid(h.getHostid());
                    g.setName(h.getName());
                    List<GroupHosts> listGroiup = new ArrayList<>();
                    listGroiup.add(g);
                    third.addHcList(listGroiup);
                    second.addChild(third);
                });
                second.addHcList(host.getHosts());
                one.addChild(second);
            });
        }
        logger.info("success to getVHostTreeDTO second:{}", one);
        return one;
    }

    //    根据登录用户查询混合云资产的hostId,hostName
    public List<HybridCloudTreeDTO> getVirtualAssetsInfo() {
        List<HybridCloudTreeDTO> hostDtos = new ArrayList<>();
        //混合云类型为10
        List<MwTangibleassetsDTO> assetsList = mwHybridCloudDao.selectAssetsByAssetsTypeId(10);
        if (assetsList.size() > 0) {
            assetsList.forEach(mwTangAssets -> {
                HybridCloudTreeDTO hostDto = new HybridCloudTreeDTO();
                hostDto.setMonitorServerId(mwTangAssets.getMonitorServerId());
                hostDto.setId(mwTangAssets.getId());
                hostDto.setIp(mwTangAssets.getInBandIp());
                hostDto.setLabel(mwTangAssets.getHostName());
                hostDto.setAssetHostId(mwTangAssets.getAssetsId());
                hostDto.setUrl(urlTree.get(0));
                hostDto.setFlag("assetsHost");
                hostDtos.add(hostDto);
            });
        }
        return hostDtos;
    }
}
