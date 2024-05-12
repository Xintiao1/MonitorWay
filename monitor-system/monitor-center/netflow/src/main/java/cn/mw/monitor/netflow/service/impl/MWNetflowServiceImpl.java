package cn.mw.monitor.netflow.service.impl;

import cn.mw.monitor.agent.api.AgentManage;
import cn.mw.monitor.agent.api.FilterRuleChange;
import cn.mw.monitor.agent.model.AgentView;
import cn.mw.monitor.agent.param.NetFlowConfigParam;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.es.action.EsDataOperation;
import cn.mw.monitor.es.action.EsIndexOperation;
import cn.mw.monitor.es.action.EsQueryOperation;
import cn.mw.monitor.netflow.dao.*;
import cn.mw.monitor.netflow.dto.MWNetFlowConfigDTO;
import cn.mw.monitor.netflow.entity.*;
import cn.mw.monitor.netflow.enums.*;
import cn.mw.monitor.netflow.exception.DateNotSelectedException;
import cn.mw.monitor.netflow.param.*;
import cn.mw.monitor.netflow.service.MWNetflowService;
import cn.mw.monitor.netflow.service.MwNetflowDetailService;
import cn.mw.monitor.netflow.service.NetflowStatService;
import cn.mw.monitor.netflow.service.OperationType;
import cn.mw.monitor.netflow.view.ConfigurationView;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.rule.*;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.MathUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.util.RamUsageEstimator;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.ParsedComposite;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
@Slf4j
public class MWNetflowServiceImpl implements MWNetflowService  {
    private static final String RULE_PROCESSOR = "netflow";
    private static final String NETFLOW_SEP = "-";
    private static final String NETFLOW_ITEM_SEP = ",";
    private static final String RULE_MANAGER_ID = "global";
    private static final String INDEX_ALIAS = "netflow";
    private static final String CAPTCP_INDEX_ALIAS = "captcp";

    private final static String ES_NETFLOW_ALL_INDEX = RULE_PROCESSOR+"*";
    private static final int ONE_HOUR = 60 * 60;

    private final static int ES_MAX_SIZE = 10000;

    private final static int MAX_IN_SIZE = 500;

    private static final int ZERO = 0;

    /**
     * 默认的统一速率单位
     */
    private static final String DEFAULT_RATE_UNIT = "KBps";

    /**
     * redis前缀
     */
    private static final String REDIS_PREFIX = "redis-netflow";

    /**
     * es前缀
     */
    private static final String ES_PREFIX = "es-netflow";

    @Value("#{'${netflow.filterKey}'.split(';')}")
    private String[] filterKey;

    @Value("${cap.storage.type}")
    private int storageType;

    @Value("${netflow.debug}")
    private boolean isDebug;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RuleManageFactory ruleManageFactory;

    @Autowired
    private AgentManage agentManage;

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    @Resource
    private MWNetflowDao mwNetflowDao;

    @Resource
    private NetflowTreeManageDao treeManageDao;

    @Resource
    private IpGroupManageDao ipGroupManageDao;

    @Resource
    private IpGroupNFAExpandManageDao nfaExpandManageDao;

    @Resource
    private IpGroupIPAMExpandManageDao ipamExpandManageDao;

    @Resource
    private ApplicationManageDao applicationManageDao;

    @Resource
    private AppExpandPortManageDao portManageDao;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWUserService mwUserService;

    @Autowired
    private MwNetflowDetailService netflowDetailService;

    @Autowired
    private NetflowStatService netflowStatService;

    /**
     * 缓存数据方式（1：redis  2:ES ）
     */
    @Value("${data.cache.switch}")
    private int cacheSwitch;

    public Reply doInterfaces(NetflowParam netflowParam, OperationType type) throws Exception {
        Reply reply = null;
        List<AssetParam> paramList = netflowParam.getParamList();
        if (CollectionUtils.isEmpty(paramList)) {
            return Reply.fail("资产列表不能为空");
        }
        //判断资产是否已经存在，若已经存在。则无法添加
        String name = checkAssetsExist(netflowParam.getParamList());
        if (StringUtils.isNotEmpty(name) && OperationType.add == type) {
            return Reply.fail("资产" + name + "已经添加");
        }
        switch (type) {
            case add:
                addOrDelAssets(netflowParam, OperationType.add);
                //新增监控接口
                reply = performInterfaces(paramList, OperationType.start);
                break;
            case delete:
                addOrDelAssets(netflowParam, OperationType.delete);
                //删除监控接口
                reply = performInterfaces(paramList, OperationType.stop);
                if (netflowParam.isDelHistory()) {
                    //删除历史数据
                    EsDataOperation esDataOperation = new EsDataOperation(restHighLevelClient);
                    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

                    BoolQueryBuilder ipQueryBuilder = QueryBuilders.boolQuery();
                    BoolQueryBuilder interfaceQueryBuilder = QueryBuilders.boolQuery();

                    List<String> ipList = new ArrayList<>();
                    Set<Integer> interfaceSet = new HashSet<>();
                    for (AssetParam assetParam : paramList) {
                        ipList.add(assetParam.getIp());
                        for (InterfaceParam interfaceParam : assetParam.getInterfaceParamList()) {
                            interfaceSet.add(interfaceParam.getIfIndex());
                        }
                    }

                    ipQueryBuilder.should(QueryBuilders.termsQuery("ipv4SrcAddr", ipList))
                            .should(QueryBuilders.termsQuery("ipv4DstAddr", ipList));

                    interfaceQueryBuilder.should(QueryBuilders.termsQuery("inputInterface", interfaceSet))
                            .should(QueryBuilders.termsQuery("outputInterface", interfaceSet));

                    queryBuilder.must(ipQueryBuilder).must(interfaceQueryBuilder);
                    boolean ret = esDataOperation.deleteFromAliasByQuery(INDEX_ALIAS, queryBuilder);
                    reply.setData(ret);
                }
                break;
            case edit:
                editAssets(netflowParam);
                try {
                    performInterfaces(netflowParam.getParamList(), OperationType.start);
                } catch (Exception e) {
                    log.error("更新节点失败", e);
                }
                break;
            default:
                break;
        }
        return reply;
    }

    /**
     * 判断资产已经添加到左侧树状图
     *
     * @param paramList
     * @return
     */
    private String checkAssetsExist(List<AssetParam> paramList) {
        QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        wrapper.eq("item_type", 0);
        for (AssetParam param : paramList) {
            wrapper.eq("item_assets_id", param.getAssetId());
            NetflowTreeEntity tree = treeManageDao.selectOne(wrapper);
            if (tree != null) {
                return tree.getItemName();
            }
        }
        return null;
    }

    /**
     * 编辑资产数据
     *
     * @param netflowParam 流量监控参数
     */
    private void editAssets(NetflowParam netflowParam) {
        for (AssetParam assets : netflowParam.getParamList()) {
            QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("item_pid", assets.getId());
            wrapper.eq("item_type", 1);
            List<NetflowTreeEntity> childList = treeManageDao.selectList(wrapper);
            //待删除的接口index
            Set<Integer> indexSet = new HashSet<>();
            for (NetflowTreeEntity entity : childList) {
                indexSet.add(entity.getItemIndex());
            }

            //先删除原有端口数据
            Iterator iterator = assets.getInterfaceParamList().iterator();
            InterfaceParam interfaceParam;
            while (iterator.hasNext()) {
                interfaceParam = (InterfaceParam) iterator.next();
                //如果现有接口包含已有的，则先删除
                if (indexSet.contains(interfaceParam.getIfIndex())) {
                    indexSet.remove(interfaceParam.getIfIndex());
                    iterator.remove();
                }
            }

            if (CollectionUtils.isNotEmpty(indexSet)) {
                UpdateWrapper<NetflowTreeEntity> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("delete_flag", false)
                        .in("item_index", indexSet)
                        .eq("item_pid", assets.getId());
                updateWrapper.set("delete_flag", true);
                updateWrapper.set("item_state", 0);
                treeManageDao.update(null, updateWrapper);
            }

            //如果还存在未增加的数据，则先判断是否为未增加的
            if (CollectionUtils.isNotEmpty(assets.getInterfaceParamList())) {
                NetflowTreeEntity childTree;
                for (InterfaceParam param : assets.getInterfaceParamList()) {
                    String itemIp = treeManageDao.getIpByAssetsIdAndIfIndex(assets.getAssetId(), param.getIfIndex());
                    childTree = geneNetFlowTree();
                    childTree.setItemType(1);
                    childTree.setItemPid(assets.getId());
                    childTree.setItemIp(itemIp);
                    childTree.setItemAssetsId(assets.getAssetId());
                    childTree.setItemIndex(param.getIfIndex());
                    childTree.setItemName(param.getIfName());
                    treeManageDao.insert(childTree);
                }
                //如果子节点信息全删除，则将资产节点也删除
            } else {
                QueryWrapper<NetflowTreeEntity> treeWrapper = new QueryWrapper<>();
                treeWrapper.eq("delete_flag", false);
                treeWrapper.eq("item_pid", assets.getId());
                int count = treeManageDao.selectCount(treeWrapper);
                if (count == 0) {
                    //删除资产节点信息
                    UpdateWrapper<NetflowTreeEntity> updateTreeWrapper = new UpdateWrapper<>();
                    updateTreeWrapper.eq("delete_flag", false)
                            .eq("id", assets.getId());
                    updateTreeWrapper.set("delete_flag", true);
                    updateTreeWrapper.set("item_state", 0);
                    treeManageDao.update(null, updateTreeWrapper);
                }
            }
        }
    }

    @Override
    public Reply performInterfaces(List<AssetParam> paramList, OperationType type) throws Exception {
        boolean ret = false;
        //更新节点状态
        switchNetFlow(paramList, type);

        ret = agentManage.updateFilterRule(new FilterRuleChange() {
            @Override
            public String getChangeRule(String ruleStr) throws Exception {
                String ruleJson = null;
                if (OperationType.start == type) {
                    ruleJson = addInterfaceRule(paramList, ruleStr);
                } else {
                    ruleJson = delInterfaces(paramList, ruleStr);
                }
                return ruleJson;
            }
        });

        return Reply.ok(ret);
    }

    @Override
    public TimeTaskRresult cleanData() {
        //查询清理日期配置
        TimeTaskRresult timeTaskRresult = new TimeTaskRresult();
        timeTaskRresult.setObjectName("MWNetflowTimer");
        timeTaskRresult.setActionName("cleanData");
        timeTaskRresult.setActionModel("netflow");

        try {
            Map<String, String> configMap = getConfigMap();
            String saveDays = configMap.get(MWNetflowService.NETFLOW_SAVE_DAYS);
            if (StringUtils.isNotEmpty(saveDays)) {
                log.info("cleanData start save days {}", saveDays);
                int cleanDays = Integer.parseInt(saveDays);
                long interval = cleanDays * 86400000;
                Date curDate = new Date();
                long delTime = curDate.getTime() - interval;
                //删除历史数据
                EsDataOperation esDataOperation = new EsDataOperation(restHighLevelClient);
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                queryBuilder.must(QueryBuilders.rangeQuery("createTime").lt(delTime));
                esDataOperation.deleteFromAliasByQuery(INDEX_ALIAS, queryBuilder);
                log.info("cleanData finish");
            }
            timeTaskRresult.setSuccess(true);
        } catch (Exception e) {
            timeTaskRresult.setSuccess(false);
            timeTaskRresult.setFailReason(e.getMessage());
        }

        return timeTaskRresult;
    }

    @Override
    public Reply netflowConfig(NetFlowConfigParam netFlowConfigParam) {

        if (null != netFlowConfigParam.getSaveDays()) {
            MWNetFlowConfigDTO mwNetFlowConfigDTO = new MWNetFlowConfigDTO();
            mwNetFlowConfigDTO.setName(MWNetflowService.NETFLOW_SAVE_DAYS);
            mwNetFlowConfigDTO.setValue(String.valueOf(netFlowConfigParam.getSaveDays()));
            mwNetflowDao.updateNetflowConigInfo(mwNetFlowConfigDTO);
        }

        if (null != netFlowConfigParam.getAgentParam()) {
            //更新nacos
            boolean ret = agentManage.netflowConfig(netFlowConfigParam);
            if (!ret) {
                Reply.fail("配置更新异常");
            }
        }

        return Reply.ok();
    }

    @Override
    public Reply netflowConfigList(NetflowAgentParam netflowAgentParam) {
        ConfigurationView configurationView = new ConfigurationView();
        Map<String, String> map = getConfigMap();
        configurationView.setConfigMap(map);

        List<AgentView> agentViews = getAgentViews();

        if (StringUtils.isNotEmpty(netflowAgentParam.getIp())) {
            List<AgentView> filterList = new ArrayList<>();
            for (AgentView agentView : agentViews) {
                if (agentView.getIp().equals(netflowAgentParam.getIp())) {
                    filterList.add(agentView);
                }
            }
            configurationView.setAgentViewList(filterList);
        } else {
            configurationView.setAgentViewList(agentViews);
        }

        return Reply.ok(configurationView);
    }


    private Map<String, String> getConfigMap() {
        List<MWNetFlowConfigDTO> netFlowConfigDTOS = mwNetflowDao.selectNetflowConigInfo();
        Map<String, String> map = netFlowConfigDTOS.stream().collect(Collectors.toMap(MWNetFlowConfigDTO::getName, MWNetFlowConfigDTO::getValue));
        return map;
    }

    private List<AgentView> getAgentViews() {
        List<AgentView> list = agentManage.getAgentViews();
        return list;
    }

    /**
     * 获取流量监控树状图
     *
     * @return 树状图
     */
    @Override
    public Reply browseTree() {
        QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        wrapper.eq("item_pid", 0);
        wrapper.eq("item_type", 0);
        List<NetflowTreeEntity> treeList = treeManageDao.selectList(wrapper);
        for (NetflowTreeEntity item : treeList) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("item_pid", item.getId());
            wrapper.eq("item_type", 1);
            List<NetflowTreeEntity> childList = treeManageDao.selectList(wrapper);
            item.setChildList(childList);
        }
        //先获取redis缓存数据
        Map<Integer, String> rateMap = new HashMap<>();
        if (redisTemplate.hasKey(getRedisKey(1))) {
            rateMap = JSON.parseObject(redisTemplate.opsForValue().get(getRedisKey(1)), HashMap.class);
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    cacheResultToRedis();
                }
            });
            thread.start();
        }
        //更新流量监控的速率数据
        for (NetflowTreeEntity parent : treeList) {
            updateNetFlowRate(parent, rateMap);
        }
        return Reply.ok(treeList);
    }

    /**
     * 更新流量监控速率（以最近一小时为基准）
     *
     * @param parent  接口数据
     * @param rateMap redis缓存数据
     * @
     */
    private void updateNetFlowRate(NetflowTreeEntity parent, Map<Integer, String> rateMap) {
        //开始时间
        Date startTime = new Date();
        //结束时间
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        startTime = calendar.getTime();
        //根据资产ID获取对应的IP信息
        List<String> ipList = treeManageDao.getAssetsIpList(parent.getItemAssetsId());
        long inSumData = updateNetFlowRate(parent.getChildList(), ipList, startTime, endTime, NetFlowType.IN, rateMap);
        long outSumData = updateNetFlowRate(parent.getChildList(), ipList, startTime, endTime, NetFlowType.OUT, rateMap);
        if (rateMap.containsKey(parent.getId())) {
            String rateInfo = rateMap.get(parent.getId());
            String[] arr = rateInfo.split("\\|");
            String[] inArr = arr[0].split(":");
            String[] outArr = arr[1].split(":");
            parent.setInRateValue(Double.valueOf(inArr[0]));
            parent.setInRateUnit(inArr[1]);
            parent.setOutRateValue(Double.valueOf(outArr[0]));
            parent.setOutRateUnit(outArr[1]);
        } else {
            double rate = inSumData / ONE_HOUR;
            Map<String, String> valueMap = UnitsUtil.getConvertedValue(new BigDecimal(rate), "Bps");
            double rateValue = Double.valueOf(valueMap.get("value"));
            String unit = valueMap.get("units");
            parent.setInRateValue(rateValue);
            parent.setInRateUnit(unit);
            rate = outSumData / ONE_HOUR;
            valueMap = UnitsUtil.getConvertedValue(new BigDecimal(rate), "Bps");
            rateValue = Double.valueOf(valueMap.get("value"));
            unit = valueMap.get("units");
            parent.setOutRateValue(rateValue);
            parent.setOutRateUnit(unit);
        }
    }

    /**
     * 更新接口实时速率
     *
     * @param childList   接口列表
     * @param ipList      IP列表
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param netFlowType 流量监控类别
     * @param rateMap     redis缓存数据
     * @return 统计流量总数
     */
    private long updateNetFlowRate(List<NetflowTreeEntity> childList, List<String> ipList,
                                   Date startTime, Date endTime, NetFlowType netFlowType,
                                   Map<Integer, String> rateMap) {
        long sumData = 0L;
        double rate;
        String rateInfo;
        String[] arr;
        String[] inArr;
        String[] outArr;
        String[] sumArr;
        for (NetflowTreeEntity child : childList) {
            try {
                if (child.getItemState() == 0) {
                    child.setInRateValue(0);
                    child.setOutRateValue(0);
                    child.setInRateUnit("Bps");
                    child.setOutRateUnit("Bps");
                    continue;
                }
                if (rateMap.containsKey(child.getId())) {
                    rateInfo = rateMap.get(child.getId());
                    arr = rateInfo.split("\\|");
                    inArr = arr[0].split(":");
                    outArr = arr[1].split(":");
                    sumArr = arr[2].split(":");
                    switch (netFlowType) {
                        //入流量
                        case IN:
                            child.setInRateValue(Double.valueOf(inArr[0]));
                            child.setInRateUnit(inArr[1]);
                            sumData += Long.parseLong(sumArr[0]);
                            break;
                        //出流量
                        case OUT:
                            child.setOutRateValue(Double.valueOf(outArr[0]));
                            child.setOutRateUnit(outArr[1]);
                            sumData += Long.parseLong(sumArr[1]);
                            break;
                    }
                } else {
                    SearchRequest searchRequest = new SearchRequest();
                    searchRequest.indices(getEsIndex(startTime,endTime));
                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                            .gte(startTime.getTime())
                            .lte(endTime.getTime()));
                    //根据资产ID获取对应的IP信息
                    BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
                    for (String ip : ipList) {
                        ipShouldQueryBuilder = ipShouldQueryBuilder.should(QueryBuilders.termQuery("sender", ip));
                    }
                    boolQueryBuilder.must(ipShouldQueryBuilder);
                    switch (netFlowType) {
                        //入流量
                        case IN:
                            boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", child.getItemIndex()));
                            break;
                        //出流量
                        case OUT:
                            boolQueryBuilder.must(QueryBuilders.termQuery("outputInterface", child.getItemIndex()));
                            break;
                        //入流量
                        default:
                            boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", child.getItemIndex()));
                            break;
                    }
                    searchSourceBuilder.query(boolQueryBuilder);
                    //求和
                    SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sumBytes").field("inBytes");
                    searchSourceBuilder.aggregation(sumAggregationBuilder);
                    searchSourceBuilder.size(ES_MAX_SIZE);
                    searchRequest.source(searchSourceBuilder);
                    SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                    Sum sum = searchResponse.getAggregations().get("sumBytes");
                    rate = sum.getValue() / ONE_HOUR;
                    Map<String, String> valueMap = UnitsUtil.getConvertedValue(new BigDecimal(rate), "Bps");
                    double rateValue = Double.valueOf(valueMap.get("value"));
                    String unit = valueMap.get("units");
                    switch (netFlowType) {
                        //入流量
                        case IN:
                            child.setInRateValue(rateValue);
                            child.setInRateUnit(unit);
                            child.setInSumData((long) sum.getValue());
                            break;
                        //出流量
                        case OUT:
                            child.setOutRateValue(rateValue);
                            child.setOutRateUnit(unit);
                            child.setOutSumData((long) sum.getValue());
                            break;
                        //入流量
                        default:
                            break;
                    }
                    sumData += (long) sum.getValue();
                }
            } catch (Exception e) {
                log.error("获取ES数据失败-->流量统计,统计最近的接口上行/下行流量失败", e);
            }
        }
        return sumData;
    }

    /**
     * 批量开启/关闭
     *
     * @param paramList 参数列表
     * @param type      状态
     */
    @Override
    public void switchNetFlow(List<AssetParam> paramList, OperationType type) {
        for (AssetParam assets : paramList) {
            if (CollectionUtils.isEmpty(assets.getInterfaceParamList())){
                continue;
            }
            List<Integer> idList = new ArrayList<>();
            for (InterfaceParam interfaceParam : assets.getInterfaceParamList()) {
                idList.add(interfaceParam.getId());
            }
            switchNetFlow(type, idList);
            //如果是关闭功能，则子节点全部关闭，父节点才会关闭
            if (OperationType.stop == type) {
                QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("delete_flag", false);
                wrapper.eq("item_state", 1);
                wrapper.eq("item_pid", assets.getId());
                int count = treeManageDao.selectCount(wrapper);
                if (count == 0) {
                    switchNetFlow(assets.getId(), type);
                }
            } else {
                switchNetFlow(assets.getId(), type);
            }
        }
    }

    /**
     * 获取流量监控结果
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply browseResult(NetFlowRequestParam requestParam) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (NetflowStatService.DATABASE_CLICKHOUSE.equals(requestParam.getDatabaseType())) {
                return netflowStatService.browseResult(requestParam);
            }
            if (isDebug){
                log.info("netflow start browseResult");
            }
            resultMap = new HashMap<>();
            //先获取节点信息
            NetflowTreeEntity treeInfo = treeManageDao.selectById(requestParam.getId());
            if (treeInfo == null || treeInfo.getDeleteFlag()) {
                return Reply.fail("节点不存在或已删除");
            }
            //判断是否有最近一小时缓存
            if (requestParam.getDateType() == 1) {
                if (cacheSwitch == 2) {
                    String esKey = getStatESKey(treeInfo.getId(), NetFlowType.getType(requestParam.getNetFlowType()));
                    EsQueryOperation esQueryOperation = new EsQueryOperation(restHighLevelClient);
                    Map<String, Object> resMap = esQueryOperation.getOne(getESIndex(), esKey);
                    if (resMap != null) {
                        resultMap = JSON.parseObject(uncompress(resMap.get("cacheValue").toString()), HashMap.class);
                        return Reply.ok(resultMap);
                    }
                } else {
                    String statKey = getStatRedisKey(treeInfo.getId(), NetFlowType.getType(requestParam.getNetFlowType()));
                    if (redisTemplate.hasKey(statKey)) {
                        resultMap = JSON.parseObject(uncompress(redisTemplate.opsForValue().get(statKey)), HashMap.class);
                        return Reply.ok(resultMap);
                    }
                }
            }
            //如果是自定义时间，需要先校验时间
            if (5 == requestParam.getDateType()) {
                //先判断时间是否为1个月内
                if (requestParam.getStartTime() == null
                        || requestParam.getEndTime() == null
                        || requestParam.getStartTime().after(requestParam.getEndTime())) {
                    return Reply.fail("时间格式不正确，开始时间需要在结束时间之前");
                }
                Calendar c = Calendar.getInstance();
                c.setTime(requestParam.getStartTime());
                c.add(Calendar.MONTH, 1);
                if (c.getTime().before(requestParam.getEndTime())) {
                    return Reply.fail("最大时间仅支持一个月");
                }
                c.setTime(requestParam.getStartTime());
                c.add(Calendar.MINUTE, 5);
                if (c.getTime().after(requestParam.getEndTime())) {
                    return Reply.fail("最小时间仅支持5分钟");
                }
            }
            //开始时间
            Date startTime;
            //结束时间
            Date endTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            switch (requestParam.getDateType()) {
                //最近一小时
                case 1:
                    calendar.add(Calendar.HOUR_OF_DAY, -1);
                    break;
                //最近一天
                case 2:
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    break;
                //最近一周
                case 3:
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                //最近一月
                case 4:
                    calendar.add(Calendar.MONTH, -1);
                    break;
                //自定义
                case 5:
                    endTime = requestParam.getEndTime();
                    calendar.setTime(requestParam.getStartTime());
                    break;
                //最近5分钟
                case 6:
                    calendar.add(Calendar.MINUTE, -5);
                    break;
                default:
                    calendar.add(Calendar.HOUR_OF_DAY, -1);
                    break;
            }
            startTime = calendar.getTime();
            if (isDebug){
                log.info("netflow start flush ES");
            }
            //刷新ES
            long flushEsStart = System.currentTimeMillis();
            flushES();
            if (isDebug){
                log.info("netflow end flush ES");
            }
            long flushEsEnd = System.currentTimeMillis();
            log.info("[1] 刷新ES耗时：{}ms", flushEsEnd - flushEsStart);


            Map<String, String> assetsNameMap = getAssetsNameMap();

            long interfaceDateStart = System.currentTimeMillis();
            //先计算初始统计数据
            List<NetFlowTopData> initialStatList = new ArrayList<>();
            //如果节点是资产数据,则先统计下属所有接口数据(且统计为BOTH方式)
            if (treeInfo.getItemType() == 0 && requestParam.getNetFlowType() == 3) {
                QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
                wrapper = new QueryWrapper<>();
                wrapper.eq("delete_flag", false);
                wrapper.eq("item_state", true);
                wrapper.eq("item_pid", treeInfo.getId());
                wrapper.eq("item_type", 1);
                List<NetflowTreeEntity> childList = treeManageDao.selectList(wrapper);
                for (NetflowTreeEntity child : childList) {
                    initialStatList.addAll(statNetFlowData(requestParam.getNetFlowType(), child, startTime, endTime));
                }
            } else {
                initialStatList = statNetFlowData(requestParam.getNetFlowType(), treeInfo, startTime, endTime);
            }
            if (isDebug){
                log.info("initialStatList size is " + (CollectionUtils.isNotEmpty(initialStatList) ? initialStatList.size() : "null"));
            }
            long interfaceDateEnd = System.currentTimeMillis();
            log.info("[2] 接口流量计算耗时：{}ms", interfaceDateEnd - interfaceDateStart);

            //再根据出入流量计算
            long inOutNetStart = System.currentTimeMillis();
            List<NetFlowTopData> statList = statNetFlowData(requestParam.getNetFlowType(), initialStatList);
            long inOutNetEnd = System.currentTimeMillis();
            log.info("[3] 出入流量计算耗时：{}ms", inOutNetEnd - inOutNetStart);

            if (isDebug){
                log.info("statList size is " + (CollectionUtils.isNotEmpty(statList) ? statList.size() : "null"));
            }
            //获取TOP5的折线图数据
            long topFiveStart = System.currentTimeMillis();
            List<NetFlowTopData> netFlowTopList;
            if (statList.size() > 5) {
                netFlowTopList = statList.subList(0, 5);
            } else {
                netFlowTopList = new ArrayList<>(statList);
            }
            List<Map<String, Object>> netFlowChartList = new ArrayList<>();
            for (NetFlowTopData topData : netFlowTopList) {
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> chartResultList = getTopLineChart(topData, requestParam.getNetFlowType(),
                        treeInfo, startTime, endTime);
                updateChartList(chartResultList, startTime, endTime);
                map.put("avgData", new ArrayList<>());
                map.put("maxData", new ArrayList<>());
                map.put("minData", new ArrayList<>());
                map.put("titleName", getAssetsName(topData.getSourceIp(), assetsNameMap)
                        + " to " + getAssetsName(topData.getDstIp(), assetsNameMap));
                map.put("realData", chartResultList);
                map.put("unit", DEFAULT_RATE_UNIT);
                netFlowChartList.add(map);
            }
            long topFiveEnd = System.currentTimeMillis();
            log.info("[4] top5折线数据获取耗时：{}ms", topFiveEnd - topFiveStart);


            //统计主机排序和TOP5折线图
            long statsHostAndTopStart= System.currentTimeMillis();
            List<NetFlowTopData> hostStatList = statHostData(statList);
            if (isDebug){
                log.info("hostStatList size is " + (CollectionUtils.isNotEmpty(hostStatList) ? hostStatList.size() : "null"));
            }
            List<NetFlowTopData> hostTopList;
            if (hostStatList.size() > 5) {
                hostTopList = hostStatList.subList(0, 5);
            } else {
                hostTopList = new ArrayList<>(hostStatList);
            }
            List<Map<String, Object>> hostChartList = new ArrayList<>();
            for (NetFlowTopData topData : hostTopList) {
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> chartResultList = getTopHostLineChart(topData,
                        requestParam.getNetFlowType(), treeInfo, startTime, endTime);
                updateChartList(chartResultList, startTime, endTime);
                map.put("avgData", new ArrayList<>());
                map.put("maxData", new ArrayList<>());
                map.put("minData", new ArrayList<>());
                map.put("titleName", getAssetsName(topData.getSourceIp(), assetsNameMap));
                map.put("realData", chartResultList);
                map.put("unit", DEFAULT_RATE_UNIT);
                hostChartList.add(map);
            }
            long statsHostAndTopEnd = System.currentTimeMillis();
            log.info("[5] 统计主机排序和TOP5折线图耗时：{}ms", statsHostAndTopEnd - statsHostAndTopStart);

            //统计TOP5应用的数据
            long statsAllAppStart = System.currentTimeMillis();
            LinkedHashMap<Integer, List<AppTopData>> recordAppMap = new LinkedHashMap<>();
            List<AppTopData> recordList;
            List<ApplicationEntity> appList = getAllAppList();
            if (isDebug) {
                log.info("appList size is " + (CollectionUtils.isNotEmpty(appList) ? appList.size() : "null"));
            }
            long statsAllAppEnd = System.currentTimeMillis();
            log.info("[6] 统计TOP5应用的数据耗时：{}ms", statsAllAppEnd - statsAllAppStart);

            long statAppTopStart = System.currentTimeMillis();
            List<AppTopData> appTopList = statAppTop(requestParam.getNetFlowType(), initialStatList, recordAppMap, appList);
            if (isDebug) {
                log.info("appTopList size is " + (CollectionUtils.isNotEmpty(appTopList) ? appTopList.size() : "null"));
            }
            long statAppTopEnd = System.currentTimeMillis();
            log.info("[7] 统计TOP应用耗时：{}ms", statAppTopEnd - statAppTopStart);

            List<Map<String, Object>> appChartList = new ArrayList<>();
            ApplicationEntity app;
            log.info("[8] 应用记录数量：{}", recordAppMap.keySet().size());

            long recordAppStart = System.currentTimeMillis();
            for (Integer id : recordAppMap.keySet()) {
                recordList = recordAppMap.get(id);
                if (id == 0) {
                    app = new ApplicationEntity();
                    app.setApplicationName("未知");
                    app.setId(0);
                } else {
                    app = applicationManageDao.selectById(id);
                }
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> chartResultList = getTopAppLineChart(recordList,
                        requestParam.getNetFlowType(), treeInfo, startTime, endTime);
                updateChartList(chartResultList, startTime, endTime);
                map.put("avgData", new ArrayList<>());
                map.put("maxData", new ArrayList<>());
                map.put("minData", new ArrayList<>());
                map.put("titleName", app.getApplicationName());
                map.put("realData", chartResultList);
                map.put("unit", DEFAULT_RATE_UNIT);
                appChartList.add(map);
            }
            long recordAppEnd = System.currentTimeMillis();
            log.info("[9] 获取前五折线图耗时：{}ms", recordAppEnd - recordAppStart);

            updateAssetsName(statList, assetsNameMap);
            resultMap.put("netFlowChartList", netFlowChartList);
            resultMap.put("netFlowStatList", statList);
            updateAssetsName(hostStatList, assetsNameMap);
            resultMap.put("hostChartList", hostChartList);
            resultMap.put("hostStatList", hostStatList);
            resultMap.put("appStatList", appTopList);
            resultMap.put("appChartList", appChartList);
            resultMap.put("startTime", DateUtils.formatDateTime(startTime));
            resultMap.put("endTime", DateUtils.formatDateTime(endTime));
            if (isDebug){
                log.info("netflow end browseResult");
            }
        } catch (Exception e) {
            log.error("netflow browseResult error",e);
        }
        return Reply.ok(resultMap);
    }

    /**
     * 更新IP成资产名称
     *
     * @param statList      统计数据
     * @param assetsNameMap 资产名称对应IP关系
     */
    private void updateAssetsName(List<NetFlowTopData> statList, Map<String, String> assetsNameMap) {
        for (NetFlowTopData data : statList) {
            data.setSourceIp(getAssetsName(data.getSourceIp(), assetsNameMap));
            data.setDstIp(getAssetsName(data.getDstIp(), assetsNameMap));
        }
    }

    /**
     * 获取资产和IP对应的映射关系
     *
     * @return
     */
    private Map<String, String> getAssetsNameMap() {
        Map<String, String> map = new HashMap<>();
        List<Map<String, String>> list = mwNetflowDao.getAssetsNameMap();
        for (Map<String, String> nameMap : list) {
            map.put(nameMap.get("assetsIp"), nameMap.get("assetsName"));
        }
        return map;
    }

    /**
     * 根据IP获取资产名称
     *
     * @param ip  IP
     * @param map 资产名称map
     * @return
     */
    private String getAssetsName(String ip, Map<String, String> map) {
        if (map.containsKey(ip)) {
            return map.get(ip);
        }
        return ip;
    }

    /**
     * 获取所有应用信息数据
     *
     * @return
     */
    private List<ApplicationEntity> getAllAppList() {
        //获取应用基础信息
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        List<ApplicationEntity> appList = applicationManageDao.selectList(wrapper);
        //获取端口列表
        QueryWrapper<AppExpandPort> portWrapper;
        List<Integer> parentIdList = new ArrayList<>();
        List<AppExpandPort> allPortList = new ArrayList<>();
        Map<Integer, List<AppExpandPort>> appPortMap = new HashMap<>();
        for (ApplicationEntity application : appList) {
            parentIdList.add(application.getId());
        }
        List<List<Integer>> parentIdLists = Lists.partition(parentIdList, MAX_IN_SIZE);
        long portStart = System.currentTimeMillis();
        for (List<Integer> lists : parentIdLists) {
            portWrapper = new QueryWrapper<>();
            portWrapper.in("parent_id", lists);
            List<AppExpandPort> portList = portManageDao.selectList(portWrapper);
            allPortList.addAll(portList);
        }
        long portEnd = System.currentTimeMillis();
        log.info("[5.1] 获取端口信息耗时：{}ms", portEnd - portStart);

        int parentId;
        List<AppExpandPort> appPortList;
        for (AppExpandPort port : allPortList) {
            parentId = port.getParentId();
            if (appPortMap.containsKey(parentId)) {
                appPortList = appPortMap.get(parentId);
                appPortList.add(port);
            } else {
                appPortList = new ArrayList<>();
                appPortList.add(port);
                appPortMap.put(parentId, appPortList);
            }
        }
        for (ApplicationEntity app : appList) {
            app.setPortList(appPortMap.get(app.getId()));
        }
        return appList;
    }

    /**
     * 统计应用数据
     *
     * @param netFlowType     流量监控类别（1：入  2：出  3：出+入）
     * @param initialStatList 初始数据列表
     * @param recordAppMap    记录列表
     */
    private List<AppTopData> statAppTop(Integer netFlowType,
                                        List<NetFlowTopData> initialStatList,
                                        LinkedHashMap<Integer, List<AppTopData>> recordAppMap,
                                        List<ApplicationEntity> appList) {
        List<AppTopData> appTopList = new ArrayList<>();
        Map<String, Integer> keyMap = new HashMap<>();
        Map<String, Integer> childKeyMap = new HashMap<>();
        Map<Integer, List<AppTopData>> recordTopDataMap = new HashMap<>();
        List<AppTopData> recordList;
        NetFlowType type = NetFlowType.getType(netFlowType);
        for (ApplicationEntity app : appList) {
            //增加记录信息
            recordTopDataMap.put(app.getId(), new ArrayList<>());
        }
        //未知应用数据
        AppTopData unknownApp = geneUnknown(type);
        recordTopDataMap.put(unknownApp.getAppId(), new ArrayList<>());
        //是否是未知流量
        boolean isUnknown;
        AppTopData topData;
        AppTopData childTopData;
        AppTopData parentTopData;
        String key;
        ProtocolType protocolType;
        ProtocolType appProtocolType;
        //获取对应的IP
        long topDataForStart = System.currentTimeMillis();
        // 这两个for循环对顺序有要求，不能使用多线程进程处理
        for (NetFlowTopData data : initialStatList) {
            AppTopData record = new AppTopData();
            record.setSourceIp(data.getSourceIp());
            record.setDstIp(data.getDstIp());
            record.setSourcePort(data.getSourcePort());
            record.setDstPort(data.getDstPort());
            isUnknown = true;
            protocolType = ProtocolType.getByName(data.getProtocol());
            for (ApplicationEntity app : appList) {
                appProtocolType = ProtocolType.getByType(app.getProtocolType());
                if (appProtocolType == null || protocolType == null) {
                    continue;
                }
                if ((appProtocolType == ProtocolType.TCP && protocolType != ProtocolType.TCP) ||
                        (appProtocolType == ProtocolType.UDP && protocolType != ProtocolType.UDP)) {
                    continue;
                }
                recordList = recordTopDataMap.get(app.getId());
                //判断源端口是否命中应用端口列表
                if (checkPort(data.getSourcePort(), app.getPortList())) {
                    for (AppExpandPort expandPort : app.getPortList()) {
                        //判断流量入端口和流量出端口是否命中端口列表,
                        if ((checkPort(data.getSourcePort(), expandPort) ||
                                checkPort(data.getDstPort(), expandPort)) &&
                                checkAppIp(data.getSourceIp(), app, true) &&
                                checkAppIp(data.getDstIp(), app, false)) {
                            //判断应用统计数据是否已经增加
                            key = app.getId() + "";
                            if (keyMap.containsKey(key)) {
                                topData = appTopList.get(keyMap.get(key));
                                //增加流量
                                switch (type) {
                                    case IN:
                                        topData.setInData(topData.getInData() + data.getCompareData());
                                        topData.setInPackage(topData.getInPackage() + data.getComparePackage());
                                        break;
                                    case OUT:
                                        topData.setOutData(topData.getOutData() + data.getCompareData());
                                        topData.setOutPackage(topData.getOutPackage() + data.getComparePackage());
                                        break;
                                    case BOTH:
                                        topData.setInData(topData.getInData() + data.getInData());
                                        topData.setInPackage(topData.getInPackage() + data.getInPackage());
                                        topData.setOutData(topData.getOutData() + data.getOutData());
                                        topData.setOutPackage(topData.getOutPackage() + data.getOutPackage());
                                        topData.setSumData(topData.getSumData() + data.getCompareData());
                                        topData.setSumPackage(topData.getSumPackage() + data.getComparePackage());
                                        break;
                                    default:
                                        break;
                                }
                                topData.setCompareData(topData.getCompareData() + data.getCompareData());
                                topData.setComparePackage(topData.getComparePackage() + data.getComparePackage());
                                parentTopData = topData;
                            } else {
                                topData = new AppTopData();
                                topData.setAppName(app.getApplicationName());
                                topData.setAppId(app.getId());
                                //增加流量
                                switch (type) {
                                    case IN:
                                        topData.setInData(data.getCompareData());
                                        topData.setInPackage(data.getComparePackage());
                                        break;
                                    case OUT:
                                        topData.setOutData(data.getCompareData());
                                        topData.setOutPackage(data.getComparePackage());
                                        break;
                                    case BOTH:
                                        topData.setInData(data.getInData());
                                        topData.setInPackage(data.getInPackage());
                                        topData.setOutData(data.getOutData());
                                        topData.setOutPackage(data.getOutPackage());
                                        topData.setSumData(data.getCompareData());
                                        topData.setSumPackage(data.getComparePackage());
                                        break;
                                    default:
                                        break;
                                }
                                topData.setCompareData(data.getCompareData());
                                topData.setComparePackage(data.getComparePackage());
                                topData.setChildList(new ArrayList<>());
                                appTopList.add(topData);
                                keyMap.put(key, appTopList.size() - 1);
                                parentTopData = topData;
                            }

                            //判断端口统计信息是否增加
                            key = app.getId() + "+" + expandPort.getId();
                            if (childKeyMap.containsKey(key)) {
                                childTopData = parentTopData.getChildList().get(childKeyMap.get(key));
                                //增加流量
                                switch (type) {
                                    case IN:
                                        childTopData.setInData(childTopData.getInData() + data.getCompareData());
                                        childTopData.setInPackage(childTopData.getInPackage() + data.getComparePackage());
                                        break;
                                    case OUT:
                                        childTopData.setOutData(childTopData.getOutData() + data.getCompareData());
                                        childTopData.setOutPackage(childTopData.getOutPackage() + data.getComparePackage());
                                        break;
                                    case BOTH:
                                        childTopData.setInData(childTopData.getInData() + data.getInData());
                                        childTopData.setInPackage(childTopData.getInPackage() + data.getInPackage());
                                        childTopData.setOutData(childTopData.getOutData() + data.getOutData());
                                        childTopData.setOutPackage(childTopData.getOutPackage() + data.getOutPackage());
                                        childTopData.setSumData(childTopData.getSumData() + data.getCompareData());
                                        childTopData.setSumPackage(childTopData.getSumPackage() + data.getComparePackage());
                                        break;
                                    default:
                                        break;
                                }
                                childTopData.setCompareData(childTopData.getCompareData() + data.getCompareData());
                                childTopData.setComparePackage(childTopData.getComparePackage() + data.getComparePackage());
                            } else {
                                childTopData = new AppTopData();
                                childTopData.setAppName(expandPort.getPortContent());
                                //增加流量
                                switch (type) {
                                    case IN:
                                        childTopData.setInData(data.getCompareData());
                                        childTopData.setInPackage(data.getComparePackage());
                                        break;
                                    case OUT:
                                        childTopData.setOutData(data.getCompareData());
                                        childTopData.setOutPackage(data.getComparePackage());
                                        break;
                                    case BOTH:
                                        childTopData.setInData(data.getInData());
                                        childTopData.setInPackage(data.getInPackage());
                                        childTopData.setOutData(data.getOutData());
                                        childTopData.setOutPackage(data.getOutPackage());
                                        childTopData.setSumData(data.getCompareData());
                                        childTopData.setSumPackage(data.getComparePackage());
                                        break;
                                    default:
                                        break;
                                }
                                childTopData.setCompareData(data.getCompareData());
                                childTopData.setComparePackage(data.getComparePackage());
                                childTopData.setChildList(new ArrayList<>());
                                parentTopData.getChildList().add(childTopData);
                                childKeyMap.put(key, parentTopData.getChildList().size() - 1);
                            }
                            isUnknown = false;
                            recordList.add(record);
                        }
                    }
                }
            }
            if (isUnknown) {
                recordList = recordTopDataMap.get(unknownApp.getAppId());
                recordList.add(record);
                //数据归纳到未知数据栏里
                switch (type) {
                    case IN:
                        unknownApp.setInData(unknownApp.getInData() + data.getCompareData());
                        unknownApp.setInPackage(unknownApp.getInPackage() + data.getComparePackage());
                        break;
                    case OUT:
                        unknownApp.setOutData(unknownApp.getOutData() + data.getCompareData());
                        unknownApp.setOutPackage(unknownApp.getOutPackage() + data.getComparePackage());
                        break;
                    case BOTH:
                        unknownApp.setInData(unknownApp.getInData() + data.getInData());
                        unknownApp.setInPackage(unknownApp.getInPackage() + data.getInPackage());
                        unknownApp.setOutData(unknownApp.getOutData() + data.getOutData());
                        unknownApp.setOutPackage(unknownApp.getOutPackage() + data.getOutPackage());
                        unknownApp.setSumData(unknownApp.getSumData() + data.getCompareData());
                        unknownApp.setSumPackage(unknownApp.getSumPackage() + data.getComparePackage());
                        break;
                    default:
                        break;
                }
                unknownApp.setCompareData(unknownApp.getCompareData() + data.getCompareData());
                unknownApp.setComparePackage(unknownApp.getComparePackage() + data.getComparePackage());
            }
        }


        long topDataForEnd = System.currentTimeMillis();
        log.info("[6.1] top data for time: {} ms", topDataForEnd - topDataForStart);

        if (unknownApp.getCompareData() > 0) {
            appTopList.add(unknownApp);
        }
        //排序
        long sortStart = System.currentTimeMillis();
        sortTopList(appTopList);
        for (AppTopData appTopData : appTopList) {
            if (CollectionUtils.isNotEmpty(appTopData.getChildList())) {
                sortTopList(appTopData.getChildList());
            }
        }
        long sortEnd = System.currentTimeMillis();
        log.info("[6.2] sortTopList: {} ms", sortEnd - sortStart);

        //将数据进行计算
        long calcNetFlowUnitStart = System.currentTimeMillis();
        calcNetFlowUnit(netFlowType, appTopList);
        for (AppTopData appTopData : appTopList) {
            if (CollectionUtils.isNotEmpty(appTopData.getChildList())) {
                calcNetFlowUnit(netFlowType, appTopData.getChildList());
            }
        }
        //计算百分比
        calcAppNetFlowPercent(appTopList);
        //获取前五的数据
        List<AppTopData> appTopFiveList;
        if (appTopList.size() > 5) {
            appTopFiveList = appTopList.subList(0, 5);
        } else {
            appTopFiveList = new ArrayList<>(appTopList);
        }
        for (AppTopData data : appTopFiveList) {
            List<AppTopData> list = recordTopDataMap.get(data.getAppId());
            recordAppMap.put(data.getAppId(), list);
        }
        long calcNetFlowUnitEnd = System.currentTimeMillis();
        log.info("[6.3] 数据计算: {} ms", calcNetFlowUnitEnd - calcNetFlowUnitStart);
        log.info("[6.4] appTopList 大小： {}", appTopList.size());

        return appTopList;
    }

    /**
     * 创建未知应用
     *
     * @param type 流量监控类别
     * @return
     */
    private AppTopData geneUnknown(NetFlowType type) {
        AppTopData unknownApp = new AppTopData();
        unknownApp.setAppName("未知应用");
        unknownApp.setAppId(0);
        //增加流量
        switch (type) {
            case IN:
                unknownApp.setInData(0D);
                unknownApp.setInPackage(0);
                break;
            case OUT:
                unknownApp.setOutData(0D);
                unknownApp.setOutPackage(0);
                break;
            case BOTH:
                unknownApp.setInData(0D);
                unknownApp.setInPackage(0);
                unknownApp.setOutData(0D);
                unknownApp.setOutPackage(0);
                unknownApp.setSumData(0D);
                unknownApp.setSumPackage(0);
                break;
            default:
                break;
        }
        unknownApp.setCompareData(0D);
        unknownApp.setComparePackage(0);
        return unknownApp;
    }

    /**
     * 检查ip是否命中应用的IP地址组
     *
     * @param ip   IP
     * @param app  应用信息
     * @param isIn true：入IP  false:出IP
     * @return
     */
    private boolean checkAppIp(String ip, ApplicationEntity app, boolean isIn) {
        List<String> list = new ArrayList<>();
        IpGroupEntity ipGroup;
        boolean result = false;
        if (isIn) {
            if (app.getSourceIpId() == 0) {
                return true;
            }
            ipGroup = ipGroupManageDao.selectById(app.getSourceIpId());
            if (ipGroup == null || ipGroup.getDeleteFlag()) {
                return false;
            }
        } else {
            if (app.getDestIpId() == 0) {
                return true;
            }
            ipGroup = ipGroupManageDao.selectById(app.getDestIpId());
            if (ipGroup == null || ipGroup.getDeleteFlag()) {
                return false;
            }
        }
        if (ipGroup.getAddType() == 1) {
            list = nfaExpandManageDao.getIpGroupList(ipGroup.getId());
            for (String ips : list) {
                if (ips.contains("/")) {
                    if (isInRange(ip, ips)) {
                        return true;
                    }
                } else if (ips.contains("-")) {
                    if (isInPhase(ip, ips)) {
                        return true;
                    }

                } else {
                    if (isInList(ip, Arrays.asList(ips.split(",")))) {
                        return true;
                    }
                }
            }
        } else if (ipGroup.getAddType() == 2) {
            list = ipamExpandManageDao.getIpGroupList(ipGroup.getId());
            for (String ips : list) {
                if (isInPhase(ip, ips)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断端口列表是否包含该端口
     *
     * @param port     端口
     * @param portList 端口列表
     * @return
     */
    private boolean checkPort(String port, List<AppExpandPort> portList) {
        String[] portArr;
        try {
            for (AppExpandPort expandPort : portList) {
                if (expandPort.getPortContent().contains("-")) {
                    portArr = expandPort.getPortContent().split("-");
                    if (StringUtils.isNotEmpty(portArr[0]) && StringUtils.isNotEmpty(portArr[1]) &&
                            (Integer.parseInt(portArr[0]) <= Integer.parseInt(port)) &&
                            (Integer.parseInt(portArr[1]) >= Integer.parseInt(port))) {
                        return true;
                    }
                } else {
                    if (expandPort.getPortContent().equals(port)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("判断端口列表是否包含该端口出现错误", e);
        }
        return false;
    }

    /**
     * 判断端口列表是否包含该端口
     *
     * @param port       端口
     * @param expandPort 端口信息
     * @return
     */
    private boolean checkPort(String port, AppExpandPort expandPort) {
        String[] portArr;
        try {
            if (expandPort.getPortContent().contains("-")) {
                portArr = expandPort.getPortContent().split("-");
                if (StringUtils.isNotEmpty(portArr[0]) && StringUtils.isNotEmpty(portArr[1]) &&
                        (Integer.parseInt(portArr[0]) <= Integer.parseInt(port)) &&
                        (Integer.parseInt(portArr[1]) >= Integer.parseInt(port))) {
                    return true;
                }
            } else {
                if (expandPort.getPortContent().equals(port)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("判断端口是否包含该端口出现错误", e);
        }
        return false;
    }


    /**
     * 统计数据（将初始化数据统计成交互流量和主机流量需求的）
     *
     * @param netFlowType     流量监控类别（1：入  2：出  3：出+入）
     * @param initialStatList 初始化数据
     * @return
     */
    private List<NetFlowTopData> statNetFlowData(Integer netFlowType, List<NetFlowTopData> initialStatList) {
        List<NetFlowTopData> statList = new ArrayList<>();
        Map<String, Integer> keyMap = new HashMap<>();
        NetFlowType type = NetFlowType.getType(netFlowType);

        NetFlowTopData topData;
        String key;
        for (NetFlowTopData data : initialStatList) {
            key = data.getSourceIp() + "+" + data.getDstIp();
            if (keyMap.containsKey(key)) {
                topData = statList.get(keyMap.get(key));
                switch (type) {
                    case IN:
                        topData.setInData(topData.getInData() + data.getCompareData());
                        topData.setInPackage(topData.getInPackage() + data.getComparePackage());
                        break;
                    case OUT:
                        topData.setOutData(topData.getOutData() + data.getCompareData());
                        topData.setOutPackage(topData.getOutPackage() + data.getComparePackage());
                        break;
                    case BOTH:
                        topData.setInData(topData.getInData() + data.getInData());
                        topData.setInPackage(topData.getInPackage() + data.getInPackage());
                        topData.setOutData(topData.getOutData() + data.getOutData());
                        topData.setOutPackage(topData.getOutPackage() + data.getOutPackage());
                        topData.setSumData(topData.getSumData() + data.getCompareData());
                        topData.setSumPackage(topData.getSumPackage() + data.getComparePackage());
                        break;
                    default:
                        break;
                }
                topData.setCompareData(topData.getCompareData() + data.getCompareData());
                topData.setComparePackage(topData.getComparePackage() + data.getComparePackage());
            } else {
                topData = new NetFlowTopData();
                topData.setSourceIp(data.getSourceIp());
                topData.setDstIp(data.getDstIp());
                switch (type) {
                    case IN:
                        topData.setInData(data.getCompareData());
                        topData.setInPackage(data.getComparePackage());
                        break;
                    case OUT:
                        topData.setOutData(data.getCompareData());
                        topData.setOutPackage(data.getComparePackage());
                        break;
                    case BOTH:
                        topData.setInData(data.getInData());
                        topData.setInPackage(data.getInPackage());
                        topData.setOutData(data.getOutData());
                        topData.setOutPackage(data.getOutPackage());
                        topData.setSumData(data.getCompareData());
                        topData.setSumPackage(data.getComparePackage());
                        break;
                    default:
                        break;
                }
                topData.setCompareData(data.getCompareData());
                topData.setComparePackage(data.getComparePackage());
                statList.add(topData);
                keyMap.put(key, statList.size() - 1);
            }
        }
        //先排序
        sortTopList(statList);
        //再计算占比
        calcNetFlowPercent(statList);
        //将单位转换成合适的单位
        calcNetFlowUnit(netFlowType, statList);
        return statList;
    }

    /**
     * 获取IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply browseIpGroup(IpGroupRequestParam requestParam) {
        try {
            //先获取IP地址组基础信息
            QueryWrapper<IpGroupEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            if (requestParam.getAddType() != 9) {
                wrapper.eq("add_type", requestParam.getAddType());
            }
            if (requestParam.getVisibleType() != 9) {
                wrapper.eq("visible_flag", requestParam.getVisibleType());
            }
            wrapper.orderByDesc("create_time");
            //分页数据
            PageHelper.startPage(requestParam.getPageNumber(), requestParam.getPageSize());
            List<IpGroupEntity> ipGroupList = ipGroupManageDao.selectList(wrapper);
            for (IpGroupEntity ipGroup : ipGroupList) {
                List<String> list = new ArrayList<>();
                if (ipGroup.getAddType() == 1) {
                    list = nfaExpandManageDao.getIpGroupList(ipGroup.getId());
                } else if (ipGroup.getAddType() == 2) {
                    list = ipamExpandManageDao.getIpGroupList(ipGroup.getId());
                }
                ipGroup.setIpGroupList(list);
            }
            PageInfo pageInfo = new PageInfo<>(ipGroupList);
            pageInfo.setList(ipGroupList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取IP地址组数据失败", e);
            return Reply.fail("获取失败");
        }
    }

    /**
     * 获取IP地址组数据下拉数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply dropBrowseIpGroup(IpGroupRequestParam requestParam) {
        try {
            //先获取IP地址组基础信息
            QueryWrapper<IpGroupEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("visible_flag", true);
            wrapper.orderByDesc("create_time");
            //分页数据
            List<IpGroupEntity> ipGroupList = ipGroupManageDao.selectList(wrapper);
            //给列表增加全部数据
            IpGroupEntity all = new IpGroupEntity();
            all.setId(0);
            all.setGroupName("ANY");
            ipGroupList.add(0, all);
            return Reply.ok(ipGroupList);
        } catch (Exception e) {
            log.error("获取IP地址组数据下拉数据失败", e);
            return Reply.fail("获取失败");
        }
    }

    /**
     * 获取单个IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply browseOneIpGroup(IpGroupRequestParam requestParam) {
        try {
            IpGroupEntity ipGroup = ipGroupManageDao.selectById(requestParam.getId());
            if (ipGroup == null || ipGroup.getDeleteFlag() || ipGroup.getAddType() != 1) {
                return Reply.fail("获取失败，无对应IP地址组");
            }
            //获取具体的参数
            ipGroup.setIpRange(getIpObjectList(ipGroup.getId(), IpObjectType.IP_RANGE.getType()));
            ipGroup.setIpPhase(getIpObjectList(ipGroup.getId(), IpObjectType.IP_PHASE.getType()));
            ipGroup.setIpList(getIpObjectList(ipGroup.getId(), IpObjectType.IP_LIST.getType()));
            return Reply.ok(ipGroup);
        } catch (Exception e) {
            log.error("获取单个IP地址组数据失败", e);
            return Reply.fail("获取失败");
        }
    }

    /**
     * 获取NFA导入IP地址信息
     *
     * @param ipGroupId  IP地址组ID
     * @param objectType IP对象类别（1：ip范围，2：ip地址段：3：ip地址清单）
     * @return
     */
    private List<IpGroupNFAExpandEntity> getIpObjectList(int ipGroupId, int objectType) {
        //获取具体的参数
        QueryWrapper<IpGroupNFAExpandEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("ip_group_id", ipGroupId);
        wrapper.eq("ip_object_type", objectType);
        List<IpGroupNFAExpandEntity> list = nfaExpandManageDao.selectList(wrapper);
        return list;
    }

    /**
     * 添加IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply addIpGroup(IpGroupRequestParam requestParam) {
        try {
            //先校验数据
            if (StringUtils.isEmpty(requestParam.getGroupName())) {
                return Reply.fail("IP地址组不能为空");
            }
            if (requestParam.getAddType() != 1 && requestParam.getAddType() != 2) {
                return Reply.fail("添加仅支持NFA和IPAM导入");
            }
            //先保存基础数据
            IpGroupEntity ipGroup = new IpGroupEntity();
            ipGroup.setId(getMaxIpGroupId());
            ipGroup.setAddType(requestParam.getAddType());
            ipGroup.setCreateTime(new Date());
            ipGroup.setDeleteFlag(false);
            ipGroup.setVisibleFlag(true);
            ipGroup.setGroupName(requestParam.getGroupName());
            ipGroupManageDao.insert(ipGroup);
            //再保存拓展数据
            switch (requestParam.getAddType()) {
                //NFA
                case 1:
                    saveNFA(IpObjectType.IP_RANGE, requestParam.getIpRange(), ipGroup);
                    saveNFA(IpObjectType.IP_PHASE, requestParam.getIpPhase(), ipGroup);
                    saveNFA(IpObjectType.IP_LIST, requestParam.getIpList(), ipGroup);
                    break;
                //IPAM
                case 2:
                    saveIPAM(requestParam.getIpamList(), ipGroup);
                    break;
                default:
                    break;
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("添加IP地址组数据失败", e);
            return Reply.fail("添加失败");
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxIpGroupId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<IpGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        IpGroupEntity maxEntity = ipGroupManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 保存IPAM拓展数据
     *
     * @param ipamList IPAM拓展数据列表
     * @param ipGroup  IP组基础信息
     */
    private void saveIPAM(List<IpGroupIPAMRequestParam> ipamList, IpGroupEntity ipGroup) {
        IpGroupIPAMExpandEntity ipamEntity;
        for (IpGroupIPAMRequestParam ipam : ipamList) {
            //保存IPAM拓展信息
            ipamEntity = new IpGroupIPAMExpandEntity();
            ipamEntity.setId(getMaxIpamExpandId());
            ipamEntity.setIpGroupId(ipGroup.getId());
            ipamEntity.setItemId(ipam.getItemId());
            ipamEntity.setItemPid(ipam.getItemPid());
            ipamEntity.setItemLabel(ipam.getItemLabel());
            ipamEntity.setItemType(ipam.getItemType());
            ipamExpandManageDao.insert(ipamEntity);
            if (CollectionUtils.isNotEmpty(ipam.getChildList())) {
                saveIPAM(ipam.getChildList(), ipGroup);
            }
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxIpamExpandId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<IpGroupIPAMExpandEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        IpGroupIPAMExpandEntity maxEntity = ipamExpandManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 保存NFA拓展数据
     *
     * @param objectType IP对象类别（1：ip范围，2：ip地址段：3：ip地址清单）
     * @param nfaList    NFA拓展数据列表
     * @param ipGroup    IP组基础信息
     */
    private void saveNFA(IpObjectType objectType, List<IpGroupNFARequestParam> nfaList, IpGroupEntity ipGroup) {
        IpGroupNFAExpandEntity nfaEntity;
        for (IpGroupNFARequestParam nfaParam : nfaList) {
            //保存NFA拓展信息
            nfaEntity = new IpGroupNFAExpandEntity();
            nfaEntity.setId(getMaxNfaExpandId());
            nfaEntity.setIpGroupId(ipGroup.getId());
            nfaEntity.setIpType(nfaParam.getIpType());
            nfaEntity.setObjectType(objectType.getType());
            nfaEntity.setIpRange(nfaParam.getIpRange());
            nfaEntity.setIpPhase(nfaParam.getIpPhase());
            nfaEntity.setIpList(nfaParam.getIpList());
            nfaExpandManageDao.insert(nfaEntity);
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxNfaExpandId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<IpGroupNFAExpandEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        IpGroupNFAExpandEntity maxEntity = nfaExpandManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;

    }

    /**
     * 编辑IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply editIpGroup(IpGroupRequestParam requestParam) {
        try {
            IpGroupEntity ipGroup = ipGroupManageDao.selectById(requestParam.getId());
            if (ipGroup == null || ipGroup.getDeleteFlag()) {
                return Reply.fail("更新失败，无对应IP地址组");
            }
            //更新IP地址组数据
            if (requestParam.getAddType() == 1) {
                //先校验数据
                if (StringUtils.isEmpty(requestParam.getGroupName())) {
                    return Reply.fail("IP地址组不能为空");
                }
                if (CollectionUtils.isEmpty(requestParam.getIpList()) &&
                        CollectionUtils.isEmpty(requestParam.getIpPhase()) &&
                        CollectionUtils.isEmpty(requestParam.getIpRange())) {
                    return Reply.fail("IP对象不能为空");
                }
                ipGroup.setGroupName(requestParam.getGroupName());
                ipGroup.setVisibleFlag(requestParam.getVisibleFlag());
                ipGroupManageDao.updateById(ipGroup);
                //先删除原有的拓展数据
                QueryWrapper<IpGroupNFAExpandEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("ip_group_id", ipGroup.getId());
                nfaExpandManageDao.delete(wrapper);
                //批量添加
                saveNFA(IpObjectType.IP_RANGE, requestParam.getIpRange(), ipGroup);
                saveNFA(IpObjectType.IP_PHASE, requestParam.getIpPhase(), ipGroup);
                saveNFA(IpObjectType.IP_LIST, requestParam.getIpList(), ipGroup);
            } else if (requestParam.getAddType() == 2) {
                ipGroup.setVisibleFlag(requestParam.getVisibleFlag());
                ipGroupManageDao.updateById(ipGroup);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("编辑IP地址组数据失败", e);
            return Reply.fail("编辑失败");
        }
    }

    /**
     * 删除IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply deleteIpGroup(IpGroupRequestParam requestParam) {
        try {
            UpdateWrapper<IpGroupEntity> updateWrapper;
            updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", requestParam.getIds());
            updateWrapper.set("delete_flag", true);
            ipGroupManageDao.update(null, updateWrapper);
            //删除原有的拓展数据
            QueryWrapper<IpGroupNFAExpandEntity> wrapper = new QueryWrapper<>();
            wrapper.in("ip_group_id", requestParam.getIds());
            nfaExpandManageDao.delete(wrapper);
            QueryWrapper<IpGroupIPAMExpandEntity> ipamWrapper = new QueryWrapper<>();
            ipamWrapper.in("ip_group_id", requestParam.getIds());
            ipamExpandManageDao.delete(ipamWrapper);
            return Reply.ok();
        } catch (Exception e) {
            log.error("删除IP地址组数据失败", e);
            return Reply.fail("删除失败");
        }
    }

    /**
     * 更新IP地址组数据状态
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply updateIpGroupState(IpGroupRequestParam requestParam) {
        try {
            IpGroupEntity ipGroup = ipGroupManageDao.selectById(requestParam.getId());
            if (ipGroup == null || ipGroup.getDeleteFlag()) {
                return Reply.fail("更新失败，无对应IP地址组");
            }
            ipGroup.setVisibleFlag(requestParam.getVisibleFlag());
            ipGroupManageDao.updateById(ipGroup);
            return Reply.ok();
        } catch (Exception e) {
            log.error("更新IP地址组数据状态失败", e);
            return Reply.fail("更新失败");
        }
    }

    /**
     * 查看应用列表数据
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply browseApp(ApplicationRequestParam requestParam) {
        try {
            //获取应用基础信息
            QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            if (requestParam.getTreeId() != null && requestParam.getTreeId() != 0) {
                wrapper.eq("tree_id", requestParam.getTreeId());
            }
            if (StringUtils.isNotEmpty(requestParam.getKeyWord())) {
                wrapper.like("application_name", requestParam.getKeyWord());
            }
            wrapper.orderByDesc("create_time");
            PageHelper.startPage(requestParam.getPageNumber(), requestParam.getPageSize());
            List<ApplicationEntity> appList = applicationManageDao.selectList(wrapper);
            //获取端口列表
            QueryWrapper<AppExpandPort> portWrapper;
            for (ApplicationEntity app : appList) {
                portWrapper = new QueryWrapper<>();
                portWrapper.eq("parent_id", app.getId());
                List<AppExpandPort> portList = portManageDao.selectList(portWrapper);
                app.setPortList(portList);
            }
            PageInfo pageInfo = new PageInfo<>(appList);
            pageInfo.setList(appList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("查看应用列表数据失败", e);
            return Reply.fail("获取失败");
        }
    }

    /**
     * 查看单个应用
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply browseOneApp(ApplicationRequestParam requestParam) {
        return null;
    }

    /**
     * 添加应用
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply addApp(ApplicationRequestParam requestParam) {
        try {
            //先校验数据
            if (StringUtils.isEmpty(requestParam.getApplicationName())) {
                return Reply.fail("应用名称不能为空");
            }
            if (StringUtils.isEmpty(requestParam.getPortContent())) {
                return Reply.fail("端口不能为空");
            }
            if (requestParam.getTreeId() == 0) {
                return Reply.fail("请选择文件夹");
            }
            //先添加基础信息
            ApplicationEntity app = new ApplicationEntity();
            app.setId(getMaxAppId());
            app.setApplicationName(requestParam.getApplicationName());
            app.setCreateTime(new Date());
            app.setDeleteFlag(false);
            app.setTreeId(requestParam.getTreeId());
            app.setMonitorState(true);
            app.setProtocolType(requestParam.getProtocolType());
            app.setSourceIpId(requestParam.getSourceIpId());
            app.setDestIpId(requestParam.getDestIpId());
            applicationManageDao.insert(app);
            //根据端口添加拓展信息
            saveExpandPort(app, requestParam.getPortContent());
            return Reply.ok();
        } catch (Exception e) {
            log.error("添加应用失败", e);
            return Reply.fail("添加应用失败");
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxAppId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        ApplicationEntity maxEntity = applicationManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 保存端口拓展数据
     *
     * @param app   应用数据
     * @param ports 端口数据
     */
    private void saveExpandPort(ApplicationEntity app, String ports) {
        //中英文逗号分隔
        String[] portArr = ports.split("[,，]");
        AppExpandPort expandPort;
        for (String port : portArr) {
            expandPort = new AppExpandPort();
            expandPort.setId(getMaxExpandPortId());
            expandPort.setParentId(app.getId());
            expandPort.setMonitorState(true);
            expandPort.setPortContent(port);
            expandPort.setProtocolType(app.getProtocolType());
            expandPort.setSourceIpId(app.getSourceIpId());
            expandPort.setDestIpId(app.getDestIpId());
            portManageDao.insert(expandPort);
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxExpandPortId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<AppExpandPort> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        AppExpandPort maxEntity = portManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 编辑应用
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply editApp(ApplicationRequestParam requestParam) {
        try {
            //先校验数据
            if (StringUtils.isEmpty(requestParam.getApplicationName())) {
                return Reply.fail("应用名称不能为空");
            }
            if (StringUtils.isEmpty(requestParam.getPortContent())) {
                return Reply.fail("端口不能为空");
            }
            if (requestParam.getTreeId() == 0) {
                return Reply.fail("请选择文件夹");
            }
            //获取应用数据
            ApplicationEntity app = applicationManageDao.selectById(requestParam.getId());
            if (app == null || app.getDeleteFlag()) {
                return Reply.fail("应用不存在");
            }
            //更新数据
            app.setMonitorState(requestParam.getMonitorState());
            app.setDestIpId(requestParam.getDestIpId());
            app.setSourceIpId(requestParam.getSourceIpId());
            app.setApplicationName(requestParam.getApplicationName());
            app.setTreeId(requestParam.getTreeId());
            app.setProtocolType(requestParam.getProtocolType());
            applicationManageDao.updateById(app);
            //先删除原有的拓展数据
            QueryWrapper<AppExpandPort> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id", app.getId());
            portManageDao.delete(wrapper);
            //批量添加
            saveExpandPort(app, requestParam.getPortContent());
            return Reply.ok();
        } catch (Exception e) {
            log.error("编辑应用失败", e);
            return Reply.fail("编辑应用失败");
        }
    }

    /**
     * 删除应用
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply deleteApp(ApplicationRequestParam requestParam) {
        try {
            UpdateWrapper<ApplicationEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", requestParam.getIds());
            updateWrapper.set("delete_flag", true);
            applicationManageDao.update(null, updateWrapper);
            //删除原有的拓展数据
            QueryWrapper<AppExpandPort> wrapper = new QueryWrapper<>();
            wrapper.in("parent_id", requestParam.getIds());
            portManageDao.delete(wrapper);
            return Reply.ok();
        } catch (Exception e) {
            log.error("删除应用失败", e);
            return Reply.fail("删除应用失败");
        }
    }

    /**
     * 更新应用数据状态
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply updateAppState(ApplicationRequestParam requestParam) {
        try {
            if (requestParam.getId() != 0) {
                //获取应用数据
                ApplicationEntity app = applicationManageDao.selectById(requestParam.getId());
                if (app == null || app.getDeleteFlag()) {
                    return Reply.fail("应用不存在");
                }
                app.setMonitorState(requestParam.getMonitorState());
                applicationManageDao.updateById(app);
                //批量更新子数据
                //删除节点信息
                UpdateWrapper<AppExpandPort> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("parent_id", app.getId());
                updateWrapper.set("monitor_state", requestParam.getMonitorState());
                portManageDao.update(null, updateWrapper);
            } else if (requestParam.getPortId() != 0) {
                //获取端口ID
                AppExpandPort port = portManageDao.selectById(requestParam.getPortId());
                if (port == null) {
                    return Reply.fail("端口不存在");
                }
                //获取应用数据
                ApplicationEntity app = applicationManageDao.selectById(port.getParentId());
                if (app == null || app.getDeleteFlag()) {
                    return Reply.fail("应用不存在");
                }
                //更新端口
                port.setMonitorState(requestParam.getMonitorState());
                portManageDao.updateById(port);
                //逻辑运算：是否父节点需要同步更新
                if (requestParam.getMonitorState()) {
                    if (!app.getMonitorState()) {
                        app.setMonitorState(true);
                        applicationManageDao.updateById(app);
                    }
                } else {
                    //如果子节点信息全关闭，则将应用节点也关闭
                    QueryWrapper<AppExpandPort> wrapper = new QueryWrapper<>();
                    wrapper.eq("monitor_state", true);
                    wrapper.eq("parent_id", app.getId());
                    int count = portManageDao.selectCount(wrapper);
                    if (count == 0) {
                        app.setMonitorState(false);
                        applicationManageDao.updateById(app);
                    }
                }
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("更新应用数据状态失败", e);
            return Reply.fail("更新失败");
        }
    }

    /**
     * 缓存数据到REDIS
     *
     * @return
     */
    @Override
    public TimeTaskRresult cacheResultToRedis() {
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //先更新流量监控左侧树状速率数据
            updateTreeNetFlowRate();
            //更新流量监控初始数据
            cacheInitialData();
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("定时任务执行失败", e);
            result.setSuccess(false);
            result.setFailReason("执行失败" + e.toString());
        }
        return result;
    }

    /**
     * 获取资产及端口数据
     *
     * @param assetParam
     * @return
     */
    @Override
    public Reply popupInterfaces(AssetParam assetParam) {
        NetflowTreeEntity tree = treeManageDao.selectById(assetParam.getId());
        if (tree == null || tree.getDeleteFlag()) {
            return Reply.fail("资产不存在或已删除");
        }
        AssetsInfo assets;
        Reply reply = mwModelViewCommonService.selectById(tree.getItemAssetsId());
        if (null != reply && PaasConstant.RES_SUCCESS.equals(reply.getRes())) {
            MwTangibleassetsTable data = (MwTangibleassetsTable) reply.getData();
            if (data == null || (data.getDeleteFlag() != null && data.getDeleteFlag())) {
                return Reply.fail("资产不存在或已删除");
            } else {
                assets = CopyUtils.copy(AssetsInfo.class, data);
            }
        } else {
            return Reply.fail("资产不存在或已删除");
        }
        QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        wrapper.eq("item_pid", tree.getId());
        wrapper.eq("item_type", 1);
        List<NetflowTreeEntity> childList = treeManageDao.selectList(wrapper);
        Set<Integer> indexSet = new HashSet<>();
        for (NetflowTreeEntity entity : childList) {
            indexSet.add(entity.getItemIndex());
        }

        List<QueryAssetsInterfaceParam> interfaceList = treeManageDao.getAllInterface(tree.getItemAssetsId(), null, false);
        List<QueryAssetsInterfaceParam> selectedInterfaceList = new ArrayList<>();
        for (QueryAssetsInterfaceParam inf : interfaceList) {
            if (indexSet.contains(inf.getIfIndex())) {
                selectedInterfaceList.add(inf);
            }
        }
        assets.setInterfaceList(interfaceList);
        assets.setSelectedInterfaceList(selectedInterfaceList);

        return Reply.ok(assets);
    }

    /**
     * 更新资产的端口数据
     *
     * @param assetParam
     * @return
     */
    @Override
    public Reply editorInterfaces(AssetParam assetParam) {
        NetflowParam netflowParam = new NetflowParam();
        try {
            netflowParam.setDelHistory(false);
            netflowParam.setParamList(Arrays.asList(assetParam));
            this.doInterfaces(netflowParam, OperationType.edit);
        } catch (Exception e) {
            log.error("更新资产的端口数据失败", e);
            return Reply.fail("更新资产的端口数据失败");
        }
        return Reply.ok();
    }

    /**
     * 获取流量明细
     *
     * @return
     */
    @Override
    public Reply getNetFlowDetail(NetFlowDetailParam param) {
        if (MwNetflowDetailService.STORAGE_CLICKHOUSE == storageType) {
            return netflowDetailService.getNetFlowDetail(param);
        }
        //处理时间
        PageInfo pageInfo;
        try {
            updateDetailDate(param);
            //获取表格数据
            List<NetFlowCapEntity> list = getNetFlowDetailList(param);
            //获取查询到的总数
            int totalCount = getAllCount(param);
            pageInfo = new PageInfo(list);
            pageInfo.setPageSize(param.getPageSize());
            pageInfo.setPageNum(param.getPageNumber());
            //现支持前10000条查询,高于10000条的数据，无法查询
            if (totalCount > ES_MAX_SIZE) {
                pageInfo.setTotal(ES_MAX_SIZE);
            } else {
                pageInfo.setTotal(totalCount);
            }
            return Reply.ok(pageInfo);
        } catch (DateNotSelectedException e) {
            return Reply.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取流量明细列表数据失败", e);
            return Reply.fail("获取流量明细列表数据失败");
        }
    }

    /**
     * 获取流量明细图表数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getNetFlowDetailChart(NetFlowDetailParam param) {
        if (MwNetflowDetailService.STORAGE_CLICKHOUSE == storageType) {
            return netflowDetailService.getNetFlowDetailChart(param);
        }
        try {
            //先保存数据
            saveCacheInfo(param);
            //处理时间
            updateDetailDate(param);
            //获取表格数据
            List<NetFlowDetailChart> chartList = getNetFlowChart(param);
            updateDetailChartList(chartList, param.getStartDateTime(), param.getEndDateTime());
            return Reply.ok(chartList);
        } catch (DateNotSelectedException e) {
            return Reply.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取流量明细图表数据失败", e);
            return Reply.fail("获取流量明细图表数据失败");
        }
    }

    /**
     * 保存流量明细缓存数据
     *
     * @param param
     */
    private void saveCacheInfo(NetFlowDetailParam param) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            NetflowDetailCacheInfo cacheInfo;
            String oldCache = mwNetflowDao.getNetflowCacheInfo(userInfo.getUserId());
            if (StringUtils.isNotEmpty(oldCache)) {
                cacheInfo = JSON.parseObject(oldCache, NetflowDetailCacheInfo.class);
            } else {
                cacheInfo = new NetflowDetailCacheInfo();
                cacheInfo.setSelectedColumns(new ArrayList<>());
            }
            if (CollectionUtils.isNotEmpty(param.getKibanaList())) {
                StringBuffer stringBuffer = new StringBuffer();
                for (KibanaPageParam page : param.getKibanaList()) {
                    stringBuffer.append(page.getValue());
                }
                cacheInfo.setKibanaInfo(stringBuffer.toString());
            } else {
                cacheInfo.setKibanaInfo("");
            }
            cacheInfo.setStartTime(param.getStartTime());
            cacheInfo.setEndTime(param.getEndTime());
            //先删除
            mwNetflowDao.deleteNetflowCacheInfo(userInfo.getUserId());
            //再增加
            mwNetflowDao.saveNetlowCacheInfo(userInfo.getUserId(), JSON.toJSONString(cacheInfo));
        } catch (Exception e) {
            log.error("保存流量明细缓存数据失败", e);
        }
    }

    /**
     * 更新时间字段
     *
     * @param param
     */
    private void updateDetailDate(NetFlowDetailParam param) {
        if (param.dateParamIsNotEmpty()){
            param.setStartDateTime(getDateTime(param.getStartTime()));
            param.setEndDateTime(getDateTime(param.getEndTime()));
        }else {
            throw new DateNotSelectedException("请选择时间段");
        }
    }

    /**
     *  获取时间数据
     * @param timeParam
     * @return
     */
    private Date getDateTime(TimeParam timeParam) {
        Date dateTime;
        switch (timeParam.getType()) {
            case TimeParam.DATE_TYPE_ABSOLUTE:
                dateTime = MWUtils.strToDateLong(timeParam.getValue());
                break;
            case TimeParam.DATE_TYPE_NOW:
                dateTime = new Date();
                break;
            case TimeParam.DATE_TYPE_RELATIVE:
                dateTime = addTime(timeParam);
                break;
            default:
                dateTime = new Date();
                break;
        }
        return dateTime;
    }

    /**
     * relative时，处理时间数据
     *
     * @param timeParam
     * @return
     */
    private Date addTime(TimeParam timeParam) {
        Date returnDate;
        Date nowDate = new Date();
        TimeType timeType = TimeType.getTimeType(timeParam.getUnit());
        int amount = -Integer.parseInt(timeParam.getValue());
        switch (timeType) {
            case SECOND:
                returnDate = DateUtils.addSeconds(nowDate, amount);
            case MINUTE:
                returnDate = DateUtils.addMinutes(nowDate, amount);
            case HOUR:
                returnDate = DateUtils.addHours(nowDate, amount);
            case DAY:
                returnDate = DateUtils.addDays(nowDate, amount);
            case WEEK:
                returnDate = DateUtils.addWeeks(nowDate, amount);
            case MONTH:
                returnDate = DateUtils.addMonths(nowDate, amount);
            case YEAR:
                returnDate = DateUtils.addYears(nowDate, amount);
            default:
                returnDate = nowDate;
        }
        if (timeParam.isRound()) {
            return roundDate(timeType, nowDate);
        }
        return returnDate;
    }

    /**
     * 获取抹零后的时间
     *
     * @param timeType 时间类别
     * @param nowDate  待修改的时间
     * @return
     */
    private Date roundDate(TimeType timeType, Date nowDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        switch (timeType) {
            case SECOND:
                break;
            case MINUTE:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                break;
            case HOUR:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                break;
            case DAY:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case WEEK:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case MONTH:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case YEAR:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                break;
        }
        return calendar.getTime();
    }

    /**
     * 获取ES总数
     *
     * @param param
     * @return
     */
    private int getAllCount(NetFlowDetailParam param) {
        Date startTime;
        Date endTime;
        int totalCount = 0;
        CountRequest countRequest = new CountRequest();
        try {
            startTime = param.getStartDateTime();
            endTime = param.getEndDateTime();
            countRequest.indices(getCapIndex(startTime,endTime));

            BoolQueryBuilder boolQueryBuilder = geneNetFlowDetailQueryBuilder(param);
            countRequest.query(boolQueryBuilder);
            CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
            totalCount = Integer.parseInt(String.valueOf(countResponse.getCount()));
        } catch (Exception e) {
            log.error("获取ES总数", e);
        }
        return totalCount;
    }

    /**
     * 获取流量明细数据
     *
     * @param param
     * @return
     */
    private List<NetFlowCapEntity> getNetFlowDetailList(NetFlowDetailParam param) {
        List<NetFlowCapEntity> resultList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest();

        try {
            searchRequest.indices(getCapIndex(param.getStartDateTime(), param.getEndDateTime()));

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.sort("capTime", SortOrder.DESC);
            searchSourceBuilder.size(param.getPageSize());
            searchSourceBuilder.from(param.getPageSize() * (param.getPageNumber() - 1));

            BoolQueryBuilder boolQueryBuilder = geneNetFlowDetailQueryBuilder(param);
            searchSourceBuilder.query(boolQueryBuilder);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : searchResponse.getHits().getHits()) {
                NetFlowCapEntity capInfo = JSON.parseObject(JSON.toJSONString(searchHit.getSourceAsMap()), NetFlowCapEntity.class);
                capInfo.setCreateTimeString(DateUtils.formatDateTime(capInfo.getCreateTime()));
                resultList.add(capInfo);
            }
            //进行数据排序
            resultList  = sortDetailList(param,resultList);
            return resultList;
        } catch (Exception e) {
            log.error("获取流量明细数据失败", e);
        }
        return new ArrayList<>();
    }

    /**
     * 进行数据排序
     *
     * @param param      查询参数
     * @param resultList 结果
     */
    private List<NetFlowCapEntity> sortDetailList(NetFlowDetailParam param, List<NetFlowCapEntity> resultList) {
        List<NetFlowCapEntity> newList = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(param.getSortColumn()) || StringUtils.isEmpty(param.getSortType())) {
                return resultList;
            }
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            Field field = NetFlowCapEntity.class.getDeclaredField(param.getSortColumn());
            Method method = NetFlowCapEntity.class.getMethod(getMethodName(field));
            field.setAccessible(true);
            if (SortType.desc.name().equalsIgnoreCase(param.getSortType())){
                newList = resultList.stream().sorted((o1, o2) -> {
                    try {
                        return ((Collator) com).compare(pinyin4jUtil.getStringPinYin(String.valueOf(method.invoke(o2))),
                                pinyin4jUtil.getStringPinYin(String.valueOf(method.invoke(o1))));
                    } catch (Exception e) {
                        log.error("sortDetailList---->数据比较错误", e);
                        return 0;
                    }
                }).collect(Collectors.toList());
            }else {
                newList = resultList.stream().sorted((o1, o2) -> {
                    try {
                        return ((Collator) com).compare(pinyin4jUtil.getStringPinYin(String.valueOf(method.invoke(o1))),
                                pinyin4jUtil.getStringPinYin(String.valueOf(method.invoke(o2))));
                    } catch (Exception e) {
                        log.error("sortDetailList---->数据比较错误", e);
                        return 0;
                    }
                }).collect(Collectors.toList());
            }
            return newList;
        } catch (Exception e) {
            log.error("sortDetailList---->数据排序错误", e);
        }
        return resultList;
    }

    private String getMethodName(Field field) {
        char[] nameArr = field.getName().toCharArray();
        char first = nameArr[0];
        if (first >= 97 && first <= 122){
            first ^= 32;
        }
        nameArr[0] = first;
        return "get"+String.valueOf(nameArr);
    }

    /**
     * 获取流量详情分段数据信息
     *
     * @param param
     * @return
     */
    private List<NetFlowDetailChart> getNetFlowChart(NetFlowDetailParam param) {
        List<NetFlowDetailChart> chartList = new ArrayList<>();
        Date startTime;
        Date endTime;
        NetFlowDetailChart chart;
        SearchRequest searchRequest = new SearchRequest();
        try {
            startTime = param.getStartDateTime();
            endTime = param.getEndDateTime();
            searchRequest.indices(getCapIndex(startTime, endTime));

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = geneNetFlowDetailQueryBuilder(param);
            //时间分割
            DateHistogramInterval dateHistogramInterval = getDateHistogramInterval(startTime, endTime);
            AggregationBuilder timeAggregation = AggregationBuilders.dateHistogram("agg").field("capTime").fixedInterval(dateHistogramInterval);
            searchSourceBuilder.query(boolQueryBuilder);
            //分段计数
            TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sumCount").field("length").size(ES_MAX_SIZE);
            timeAggregation.subAggregation(termsAggregationBuilder);
            searchSourceBuilder.aggregation(timeAggregation);
            searchSourceBuilder.size(ES_MAX_SIZE);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Histogram histogram = aggregations.get("agg");
            Calendar calendar = Calendar.getInstance();
            for (Histogram.Bucket bucket : histogram.getBuckets()) {
                ZonedDateTime time = (ZonedDateTime) bucket.getKey();
                //修改时间
                calendar.set(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth(),
                        time.getHour(), time.getMinute(), time.getSecond());
                chart = new NetFlowDetailChart();
                chart.setCount(Integer.parseInt(String.valueOf(bucket.getDocCount())));
                chart.setColumnName(DateUtils.formatDateTime(calendar.getTime()));
                chartList.add(chart);
            }
        } catch (Exception e) {
            log.error("获取流量详情分段数据信息失败", e);
        }
        return chartList;
    }

    /**
     * 构建时间查询条件以及KIBANA语句的查询条件
     *
     * @param param
     * @return
     */
    private BoolQueryBuilder geneNetFlowDetailQueryBuilder(NetFlowDetailParam param) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("capTime")
                .gte(DateUtils.addHours(param.getStartDateTime(),8).getTime())
                .lte(DateUtils.addHours(param.getEndDateTime(),8).getTime()));
        if (CollectionUtils.isNotEmpty(param.getKibanaList())) {
            BoolQueryBuilder kibanaQueryBuilder = new BoolQueryBuilder();
            KibanaType kibanaType;
            String key = null;
            OperateType operateType = null;
            String value = null;
            RelationType relationType;
            for (KibanaPageParam page : param.getKibanaList()) {
                kibanaType = KibanaType.getKibanaType(page.getType());
                switch (kibanaType) {
                    case KEY:
                        key = page.getValue();
                        break;
                    case OPERATE:
                        operateType = OperateType.getOperateType(page.getValue());
                        break;
                    case VALUE:
                        value = page.getValue();
                        break;
                    case RELATION:
                        relationType = RelationType.valueOf(page.getValue());
                        kibanaQueryBuilder = geneKibanaQueryBuild(key, operateType, value);
                        switch (relationType) {
                            case and:
                                kibanaQueryBuilder.must(kibanaQueryBuilder);
                                break;
                            case or:
                                kibanaQueryBuilder.should(kibanaQueryBuilder);
                                break;
                        }
                        //clear
                        key = null;
                        value = null;
                        operateType = null;
                        break;
                    default:
                        break;
                }
            }
            kibanaQueryBuilder = geneKibanaQueryBuild(key, operateType, value);
            boolQueryBuilder.must(kibanaQueryBuilder);
        }
        return boolQueryBuilder;
    }

    /**
     * 构建KIBANA查询条件
     *
     * @param key         查询ES的column
     * @param operateType 条件符
     * @param value       值
     * @return
     */
    private BoolQueryBuilder geneKibanaQueryBuild(String key, OperateType operateType, String value) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (StringUtils.isBlank(key) || StringUtils.isEmpty(value) || operateType == null) {
            return boolQueryBuilder;
        }
        switch (operateType) {
            case EQUAL:
                boolQueryBuilder.must(QueryBuilders.matchQuery(key, value));
                break;
            case EQUAL_ALL:
                break;
            case LESS_THAN:
                boolQueryBuilder.must(QueryBuilders.rangeQuery(key).lt(value));
                break;
            case LESS_THAN_EQUAL_TO:
                boolQueryBuilder.must(QueryBuilders.rangeQuery(key).lte(value));
                break;
            case GREATER_THAN:
                boolQueryBuilder.must(QueryBuilders.rangeQuery(key).gt(value));
                break;
            case GREATER_THAN_EQUAL_TO:
                boolQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(value));
                break;
            default:
                break;
        }
        return boolQueryBuilder;
    }

    /**
     * 获取索引属性
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private String[] getCapIndex(Date startTime, Date endTime) {
        long betweenDays = 0;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startTime);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endTime);
        if (startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR)) {
            betweenDays = endCalendar.get(Calendar.DAY_OF_YEAR) - startCalendar.get(Calendar.DAY_OF_YEAR);
        } else {
            betweenDays = DateUtils.between(startTime, endTime, DateUnitEnum.DAY);
        }
        String[] indexArray = new String[Integer.parseInt(String.valueOf(betweenDays + 1))];
        for (int i = 0; i <= betweenDays; i++) {
            indexArray[i] = CAPTCP_INDEX_ALIAS + DateUtils.format(DateUtils.addDays(startTime, i), DateConstant.PURE_DATE);
        }
        return indexArray;
    }

    /**
     * 获取ES中索引的字段
     *
     * @return
     */
    @Override
    public Reply getNetFlowColumns() {
        if (MwNetflowDetailService.STORAGE_CLICKHOUSE == storageType) {
            return netflowDetailService.getNetFlowColumns();
        }
        try {
            List<Map<String,Object>> mapList = getOperateMap();
            return Reply.ok(mapList);
        } catch (Exception e) {
            log.error("获取ES中索引的字段fail", e);
        }
        return Reply.fail("获取ES中索引的字段失败");
    }

    /**
     * 获取流量明细缓存数据
     *
     * @return
     */
    @Override
    public Reply getNetFlowDetailCacheInfo() {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            NetflowDetailCacheInfo cache;
            if (userInfo.getUserId() != null) {
                String saveInfo = mwNetflowDao.getNetflowCacheInfo(userInfo.getUserId());
                if (StringUtils.isEmpty(saveInfo)) {
                    cache = new NetflowDetailCacheInfo();
                } else {
                    cache = JSON.parseObject(saveInfo, NetflowDetailCacheInfo.class);
                }
                return Reply.ok(cache);
            }
        } catch (Exception e) {
            log.error("获取流量明细缓存数据失败", e);
        }
        return Reply.fail("获取流量明细缓存数据失败");
    }

    /**
     * 保存流量分析已选择的数据
     *
     * @param cacheInfo 缓存数据
     * @return
     */
    @Override
    public Reply saveNetFlowDetailSelectedColumns(NetflowDetailCacheInfo cacheInfo) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            NetflowDetailCacheInfo cache;
            if (userInfo.getUserId() != null){
                //先获取原有数据
                String saveInfo = mwNetflowDao.getNetflowCacheInfo(userInfo.getUserId());
                if (StringUtils.isEmpty(saveInfo)){
                    cache = new NetflowDetailCacheInfo();
                }else {
                    cache = JSON.parseObject(saveInfo,NetflowDetailCacheInfo.class);
                }
                cache.setSelectedColumns(cacheInfo.getSelectedColumns());
                //先删除
                mwNetflowDao.deleteNetflowCacheInfo(userInfo.getUserId());
                //再增加
                mwNetflowDao.saveNetlowCacheInfo(userInfo.getUserId(),JSON.toJSONString(cache));
            }
        } catch (Exception e) {
            log.error("保存流量分析已选择的数据失败",e);
            return Reply.fail("保存失败");
        }
        return Reply.ok();
    }

    /**
     * 获取操作MAP
     *
     * @return
     */
    private List<Map<String, Object>> getOperateMap() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> opMap;
        Class netFlowCap = NetFlowCapEntity.class;
        Field[] fields = netFlowCap.getDeclaredFields();
        IconType iconType;
        for (Field field : fields) {
            opMap = new HashMap<>();
            List<OperateType> operateList = OperateType.getOperateValueList(FieldType.getField(field.getType()));
            List<String> opList = listToString(operateList);
            opMap.put(field.getName(), opList);
            iconType = IconType.getIconType(field.getType());
            opMap.put("icon", iconType.getColumnCode());
            resultList.add(opMap);
        }
        return resultList;
    }

    /**
     * 将操作列表转换成字符串列表
     *
     * @param operateList
     * @return
     */
    private List<String> listToString(List<OperateType> operateList) {
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(operateList)) {
            return list;
        }
        for (OperateType type : operateList) {
            list.add(type.getOperateValue());
        }
        return list;
    }

    /**
     * 补全柱状图时间数据
     *
     * @param chartList 折线图数据
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private void updateDetailChartList(List<NetFlowDetailChart> chartList, Date startTime, Date endTime) {
        List<String> dateList = getDateArray(startTime, endTime);
        Set<String> dateSet = new HashSet<>();
        for (NetFlowDetailChart chart : chartList) {
            dateSet.add(chart.getColumnName());
        }
        for (String date : dateList) {
            if (!dateSet.contains(date)) {
                NetFlowDetailChart zeroChart = new NetFlowDetailChart();
                zeroChart.setCount(0);
                zeroChart.setColumnName(date);
                chartList.add(zeroChart);
            }
        }
        Collections.sort(chartList, new Comparator<NetFlowDetailChart>() {
            @Override
            public int compare(NetFlowDetailChart o1, NetFlowDetailChart o2) {
                Date date1 = DateUtils.parse(o1.getColumnName(), DateConstant.NORM_DATETIME);
                Date date2 = DateUtils.parse(o2.getColumnName(), DateConstant.NORM_DATETIME);
                return date1.compareTo(date2);
            }
        });
    }

    /**
     * 保存初始化数据
     */
    private void cacheInitialData() {
        //获取当前所有节点
        QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        wrapper.eq("item_state", true);
        wrapper.eq("item_pid", 0);
        wrapper.eq("item_type", 0);
        List<NetflowTreeEntity> treeList = treeManageDao.selectList(wrapper);
        for (NetflowTreeEntity item : treeList) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("item_state", true);
            wrapper.eq("item_pid", item.getId());
            wrapper.eq("item_type", 1);
            List<NetflowTreeEntity> childList = treeManageDao.selectList(wrapper);
            item.setChildList(childList);
        }
        //刷新ES
        flushES();
        //开始时间
        Date startTime;
        //结束时间
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        //最近一小时
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        startTime = calendar.getTime();
        //获取全部应用数据
        List<ApplicationEntity> appList = getAllAppList();
        //缓存数据
        List<NetFlowTopData> parentInitialStatList;
        List<NetFlowTopData> childInitialStatList;
        for (NetFlowType netFlowType : NetFlowType.values()) {
            for (NetflowTreeEntity parent : treeList) {
                parentInitialStatList = new ArrayList<>();
                for (NetflowTreeEntity child : parent.getChildList()) {
                    //缓存子节点（接口）的最近一小时数据
                    childInitialStatList = statNetFlowData(netFlowType.getType(), child, startTime, endTime);
                    //缓存最近一小时数据
                    cacheLastData(child, netFlowType, childInitialStatList, startTime, endTime, appList);
                    parentInitialStatList.addAll(childInitialStatList);
                }
                cacheLastData(parent, netFlowType, parentInitialStatList, startTime, endTime, appList);
            }
        }
    }

    /**
     * 缓存最近一小时的数据
     *
     * @param treeInfo        节点信息
     * @param netFlowType     请求方式
     * @param initialStatList 原始统计数据
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param appList         应用列表
     */
    private void cacheLastData(NetflowTreeEntity treeInfo, NetFlowType netFlowType,
                               List<NetFlowTopData> initialStatList, Date startTime,
                               Date endTime, List<ApplicationEntity> appList) {
        try {
            if (CollectionUtils.isEmpty(initialStatList)) {
                return;
            }
            String key = getStatRedisKey(treeInfo.getId(), netFlowType);
            String esKey = getStatESKey(treeInfo.getId(), netFlowType);
            //判断缓存是否存在，存在则延时不处理
            if (redisTemplate.hasKey(key)) {
                redisTemplate.expire(key, ONE_HOUR, TimeUnit.SECONDS);
            }
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, String> assetsNameMap = getAssetsNameMap();
            //再根据出入流量计算
            List<NetFlowTopData> statList = statNetFlowData(netFlowType.getType(), initialStatList);
            //获取TOP5的折线图数据
            List<NetFlowTopData> netFlowTopList;
            if (statList.size() > 5) {
                netFlowTopList = statList.subList(0, 5);
            } else {
                netFlowTopList = new ArrayList<>(statList);
            }
            List<Map<String, Object>> netFlowChartList = new ArrayList<>();
            for (NetFlowTopData topData : netFlowTopList) {
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> chartResultList = getTopLineChart(topData, netFlowType.getType(),
                        treeInfo, startTime, endTime);
                updateChartList(chartResultList, startTime, endTime);
                map.put("avgData", new ArrayList<>());
                map.put("maxData", new ArrayList<>());
                map.put("minData", new ArrayList<>());
                map.put("titleName", getAssetsName(topData.getSourceIp(), assetsNameMap)
                        + " to " + getAssetsName(topData.getDstIp(), assetsNameMap));
                map.put("realData", chartResultList);
                map.put("unit", DEFAULT_RATE_UNIT);
                netFlowChartList.add(map);
            }
            //统计主机排序和TOP5折线图
            List<NetFlowTopData> hostStatList = statHostData(statList);
            List<NetFlowTopData> hostTopList;
            if (hostStatList.size() > 5) {
                hostTopList = hostStatList.subList(0, 5);
            } else {
                hostTopList = new ArrayList<>(hostStatList);
            }
            List<Map<String, Object>> hostChartList = new ArrayList<>();
            for (NetFlowTopData topData : hostTopList) {
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> chartResultList = getTopHostLineChart(topData,
                        netFlowType.getType(), treeInfo, startTime, endTime);
                updateChartList(chartResultList, startTime, endTime);
                map.put("avgData", new ArrayList<>());
                map.put("maxData", new ArrayList<>());
                map.put("minData", new ArrayList<>());
                map.put("titleName", getAssetsName(topData.getSourceIp(), assetsNameMap));
                map.put("realData", chartResultList);
                map.put("unit", DEFAULT_RATE_UNIT);
                hostChartList.add(map);
            }
            //统计TOP5应用的数据
            LinkedHashMap<Integer, List<AppTopData>> recordAppMap = new LinkedHashMap<>();
            List<AppTopData> recordList;
            List<AppTopData> appTopList = statAppTop(netFlowType.getType(), initialStatList, recordAppMap, appList);
            List<Map<String, Object>> appChartList = new ArrayList<>();
            ApplicationEntity app;
            for (Integer id : recordAppMap.keySet()) {
                recordList = recordAppMap.get(id);
                if (id == 0) {
                    app = new ApplicationEntity();
                    app.setApplicationName("未知");
                    app.setId(0);
                } else {
                    app = applicationManageDao.selectById(id);
                }
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> chartResultList = getTopAppLineChart(recordList,
                        netFlowType.getType(), treeInfo, startTime, endTime);
                updateChartList(chartResultList, startTime, endTime);
                map.put("avgData", new ArrayList<>());
                map.put("maxData", new ArrayList<>());
                map.put("minData", new ArrayList<>());
                map.put("titleName", app.getApplicationName());
                map.put("realData", chartResultList);
                map.put("unit", DEFAULT_RATE_UNIT);
                appChartList.add(map);
            }
            updateAssetsName(statList, assetsNameMap);
            resultMap.put("netFlowChartList", netFlowChartList);
            resultMap.put("netFlowStatList", statList);
            updateAssetsName(hostStatList, assetsNameMap);
            resultMap.put("hostChartList", hostChartList);
            resultMap.put("hostStatList", hostStatList);
            resultMap.put("appStatList", appTopList);
            resultMap.put("appChartList", appChartList);
            resultMap.put("startTime", DateUtils.formatDateTime(startTime));
            resultMap.put("endTime", DateUtils.formatDateTime(endTime));
            //缓存数据到redis
            log.error("计算缓存数据大小》》》》    " + RamUsageEstimator.sizeOfMap(resultMap));
            redisTemplate.opsForValue().set(key, compress(JSON.toJSONString(resultMap)), ONE_HOUR, TimeUnit.SECONDS);
            cacheToES(esKey, compress(JSON.toJSONString(resultMap)));
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    /**
     * 缓存数据至ES
     *
     * @param esKey ES的KEY
     * @param data  数据
     */
    private void cacheToES(String esKey, String data) {
        EsIndexOperation esIndexOperation = new EsIndexOperation(restHighLevelClient);
        EsDataOperation esDataOperation = new EsDataOperation(restHighLevelClient);
        Map<String, Object> dataMap = new HashMap<>();
        if (esIndexOperation.checkIndex(getESIndex())) {
            //塞数据
            dataMap.put("cacheValue", data);
            dataMap.put("id", esKey);
            esDataOperation.insert(getESIndex(), dataMap);
        } else {
            //先创建索引
            esIndexOperation.createIndex(getESIndex(), new HashMap<>());
            //塞数据
            dataMap.put("cacheValue", data);
            dataMap.put("id", esKey);
            esDataOperation.insert(getESIndex(), dataMap);
        }
    }


    /**
     * 获取缓存的key
     *
     * @param treeId 节点ID
     * @param type   类别
     * @return
     */
    private String getStatRedisKey(int treeId, NetFlowType type) {
        return REDIS_PREFIX + "-stat-cache" + "-" + treeId + "-" + type.name();
    }

    /**
     * 获取缓存至ES的key
     *
     * @param treeId 节点ID
     * @param type   类别
     * @return
     */
    private String getStatESKey(int treeId, NetFlowType type) {
        return ES_PREFIX + "-stat-cache" + "-" + treeId + "-"
                + type.name() + DateUtils.format(new Date(), DateConstant.PURE_DATE_HOUR);
    }

    /**
     * 获取ES缓存索引
     *
     * @return
     */
    private String getESIndex() {
        return ES_PREFIX + "-stat-cache";
    }

    /**
     * 更新流量监控树的流量速率
     */
    private void updateTreeNetFlowRate() {
        //更新流量监控树的流量速率
        QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        wrapper.eq("item_pid", 0);
        wrapper.eq("item_type", 0);
        List<NetflowTreeEntity> treeList = treeManageDao.selectList(wrapper);
        for (NetflowTreeEntity item : treeList) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("item_pid", item.getId());
            wrapper.eq("item_type", 1);
            List<NetflowTreeEntity> childList = treeManageDao.selectList(wrapper);
            item.setChildList(childList);
        }
        //更新流量监控的速率数据
        for (NetflowTreeEntity parent : treeList) {
            updateNetFlowRate(parent, new HashMap<>());
        }
        //将速率更新到map里
        Map<Integer, String> rateMap = new HashMap<>();
        for (NetflowTreeEntity parent : treeList) {
            rateMap.put(parent.getId(), parent.getInRateValue() + ":" + parent.getInRateUnit() + "|" + parent.getOutRateValue() + ":" + parent.getOutRateUnit());
            if (CollectionUtils.isNotEmpty(parent.getChildList())) {
                for (NetflowTreeEntity child : parent.getChildList()) {
                    rateMap.put(child.getId(), child.getInRateValue() + ":" + child.getInRateUnit() + "|" + child.getOutRateValue() + ":" + child.getOutRateUnit() + "|" + child.getInSumData() + ":" + child.getOutSumData());
                }
            }
        }
        redisTemplate.opsForValue().set(getRedisKey(1), JSON.toJSONString(rateMap), ONE_HOUR, TimeUnit.SECONDS);
    }

    private String getRedisKey(int type) {
        String key = REDIS_PREFIX;
        switch (type) {
            //左侧树状图速率缓存数据
            case 1:
                key += "_tree_rate_map";
                break;
            //
            case 2:
                break;
            default:
                break;
        }
        return key;
    }

    /**
     * 获取前五的折线图
     *
     * @param topData   Top数据
     * @param type      流量监控类别（1：入  2：出  3：出+入）
     * @param treeInfo  节点信息
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private List<Map<String, Object>> getTopHostLineChart(NetFlowTopData topData, Integer type, NetflowTreeEntity treeInfo, Date startTime, Date endTime) {
        NetFlowType netFlowType = NetFlowType.getType(type);
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(getEsIndex(startTime,endTime));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                    .gte(startTime.getTime())
                    .lte(endTime.getTime()));
            //时间分割
            DateHistogramInterval dateHistogramInterval = getDateHistogramInterval(startTime, endTime);
            int divideTime = getDivideTime(startTime, endTime);
            //根据资产ID获取对应的IP信息
            List<String> ipList = treeManageDao.getAssetsIpList(treeInfo.getItemAssetsId());
            BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
            for (String ip : ipList) {
                ipShouldQueryBuilder = ipShouldQueryBuilder.should(QueryBuilders.termQuery("sender", ip));
            }
            boolQueryBuilder.must(ipShouldQueryBuilder);
            //如果是接口，则需要增加筛选条件
            if (treeInfo.getItemType() == 1) {
                switch (netFlowType) {
                    //入流量
                    case IN:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量
                    case OUT:
                        boolQueryBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量+入流量
                    case BOTH:
                        BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder inBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder outBuilder = new BoolQueryBuilder();
                        inBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        outBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        shouldQueryBuilder.should(inBuilder).should(outBuilder);
                        boolQueryBuilder.must(shouldQueryBuilder);
                        break;
                    //入流量
                    default:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                }
                //如果是资产数据，则需要先查询出所有的IP数据
            } else {
                if (netFlowType == NetFlowType.IN || netFlowType == NetFlowType.OUT) {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder indexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        if (netFlowType == NetFlowType.IN) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        } else if (netFlowType == NetFlowType.OUT) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                        }
                    }
                    boolQueryBuilder.must(indexShouldQueryBuilder);
                } else {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder inIndexShouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder outIndexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        inIndexShouldQueryBuilder = inIndexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        outIndexShouldQueryBuilder = outIndexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                    }
                    shouldQueryBuilder.should(inIndexShouldQueryBuilder);
                    shouldQueryBuilder.should(outIndexShouldQueryBuilder);
                    boolQueryBuilder.must(shouldQueryBuilder);
                }
            }
            //设置源IP和目标IP
            BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
            BoolQueryBuilder inBuilder = new BoolQueryBuilder();
            BoolQueryBuilder outBuilder = new BoolQueryBuilder();
            inBuilder.must(QueryBuilders.termQuery("ipv4SrcAddr", topData.getSourceIp()));
            outBuilder.must(QueryBuilders.termQuery("ipv4DstAddr", topData.getSourceIp()));
            shouldQueryBuilder.should(inBuilder).should(outBuilder);
            boolQueryBuilder.must(shouldQueryBuilder);

            DateHistogramAggregationBuilder timeAggregation = AggregationBuilders.dateHistogram("agg").field("createTime");
            timeAggregation.fixedInterval(dateHistogramInterval);

            searchSourceBuilder.query(boolQueryBuilder);
            //求和
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sumBytes").field("inBytes");
            timeAggregation.subAggregation(sumAggregationBuilder);
            searchSourceBuilder.aggregation(timeAggregation);
            searchSourceBuilder.size(ZERO);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Histogram histogram = aggregations.get("agg");
            Map<String, Object> chartMap;
            Map<String, String> valueMap;
            Calendar calendar = Calendar.getInstance();
            for (Histogram.Bucket bucket : histogram.getBuckets()) {
                chartMap = new HashMap<>();
                ZonedDateTime time = (ZonedDateTime) bucket.getKey();
                calendar.set(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth(),
                        time.getHour() + 8, time.getMinute(), time.getSecond());
                Sum sum = bucket.getAggregations().get("sumBytes");
                double value = sum.getValue() / divideTime;
                log.info("主机IP = " + topData.getSourceIp() + "dateKey is " + calendar.getTime() + " \t sumBytes is " + sum.getValue());
                chartMap.put("dateTime", DateUtils.formatDateTime(calendar.getTime()));
                valueMap = UnitsUtil.getValueMap(String.valueOf(value), DEFAULT_RATE_UNIT, "Bps");
                chartMap.put("value", Double.parseDouble(valueMap.get("value")));
                chartMap.put("unitByReal", valueMap.get("units"));
                list.add(chartMap);
            }
            if (isDebug) {
                log.info(treeInfo.getItemName() + " id is " + treeInfo.getId() + " and getTopHostLineChart " + netFlowType.name() + " list size is " + (CollectionUtils.isNotEmpty(list) ? list.size() : "null"));
            }
        } catch (Exception e) {
            log.error("流量统计，按时间间隔获取TOP数据-->获取ES数据失败", e);
        }
        return list;
    }


    /**
     * 获取前五的折线图
     *
     * @param topDataList Top数据
     * @param type        流量监控类别（1：入  2：出  3：出+入）
     * @param treeInfo    节点信息
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    private List<Map<String, Object>> getTopAppLineChart(List<AppTopData> topDataList, Integer type,
                                                         NetflowTreeEntity treeInfo, Date startTime, Date endTime) {
        NetFlowType netFlowType = NetFlowType.getType(type);
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(getEsIndex(startTime,endTime));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                    .gte(startTime.getTime())
                    .lte(endTime.getTime()));
            //时间分割
            DateHistogramInterval dateHistogramInterval = getDateHistogramInterval(startTime, endTime);
            int divideTime = getDivideTime(startTime, endTime);
            //根据资产ID获取对应的IP信息
            List<String> ipList = treeManageDao.getAssetsIpList(treeInfo.getItemAssetsId());
            BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
            for (String ip : ipList) {
                ipShouldQueryBuilder = ipShouldQueryBuilder.should(QueryBuilders.termQuery("sender", ip));
            }
            boolQueryBuilder.must(ipShouldQueryBuilder);
            //如果是接口，则需要增加筛选条件
            if (treeInfo.getItemType() == 1) {
                switch (netFlowType) {
                    //入流量
                    case IN:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量
                    case OUT:
                        boolQueryBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量+入流量
                    case BOTH:
                        BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder inBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder outBuilder = new BoolQueryBuilder();
                        inBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        outBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        shouldQueryBuilder.should(inBuilder).should(outBuilder);
                        boolQueryBuilder.must(shouldQueryBuilder);
                        break;
                    //入流量
                    default:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                }
                //如果是资产数据，则需要先查询出所有的IP数据
            } else {
                if (netFlowType == NetFlowType.IN || netFlowType == NetFlowType.OUT) {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder indexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        if (netFlowType == NetFlowType.IN) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        } else if (netFlowType == NetFlowType.OUT) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                        }
                    }
                    boolQueryBuilder.must(indexShouldQueryBuilder);
                } else {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder inIndexShouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder outIndexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        inIndexShouldQueryBuilder = inIndexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        outIndexShouldQueryBuilder = outIndexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                    }
                    shouldQueryBuilder.should(inIndexShouldQueryBuilder);
                    shouldQueryBuilder.should(outIndexShouldQueryBuilder);
                    boolQueryBuilder.must(shouldQueryBuilder);
                }
            }
            //设置源IP和目标IP
            BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
            BoolQueryBuilder ipBuilder;
            Map<String, Set<String>> queryConditionMap = topDataList.stream()
                    .flatMap(topData -> Stream.of(
                            new AbstractMap.SimpleEntry<>("ipv4SrcAddr", String.valueOf(topData.getSourceIp())),
                            new AbstractMap.SimpleEntry<>("ipv4DstAddr", String.valueOf(topData.getDstIp())),
                            new AbstractMap.SimpleEntry<>("l4SrcPort", String.valueOf(topData.getSourcePort())),
                            new AbstractMap.SimpleEntry<>("l4DstPort", String.valueOf(topData.getDstPort()))
                    ))
                    .filter(entity -> !"null".equals(entity.getValue()))
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                    ));

            ipBuilder = new BoolQueryBuilder();
            for (Map.Entry<String, Set<String>> entry : queryConditionMap.entrySet()) {
                ipBuilder.must(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
            }
            shouldQueryBuilder.should(ipBuilder);

//            for (AppTopData topData : topDataList) {
//                ipBuilder = new BoolQueryBuilder();
//                ipBuilder.must(QueryBuilders.termQuery("ipv4SrcAddr", topData.getSourceIp()));
//                ipBuilder.must(QueryBuilders.termQuery("ipv4DstAddr", topData.getDstIp()));
//                ipBuilder.must(QueryBuilders.termQuery("l4SrcPort", topData.getSourcePort()));
//                ipBuilder.must(QueryBuilders.termQuery("l4DstPort", topData.getDstPort()));
//                shouldQueryBuilder.should(ipBuilder);
//            }
            boolQueryBuilder.must(shouldQueryBuilder);

            AggregationBuilder timeAggregation = AggregationBuilders.dateHistogram("agg").field("createTime").dateHistogramInterval(dateHistogramInterval);

            searchSourceBuilder.query(boolQueryBuilder);
            //求和
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sumBytes").field("inBytes");
            timeAggregation.subAggregation(sumAggregationBuilder);
            searchSourceBuilder.aggregation(timeAggregation);
            // 这里给0的原因是因为用不到hits里面的值，只需要sum即可。避免内存消耗
            searchSourceBuilder.size(ZERO);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Histogram histogram = aggregations.get("agg");
            Map<String, Object> chartMap;
            Map<String, String> valueMap;
            Calendar calendar = Calendar.getInstance();
            for (Histogram.Bucket bucket : histogram.getBuckets()) {
                chartMap = new HashMap<>();
                ZonedDateTime time = (ZonedDateTime) bucket.getKey();
                calendar.set(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth(),
                        time.getHour() + 8, time.getMinute(), time.getSecond());
                Sum sum = bucket.getAggregations().get("sumBytes");
                double value = sum.getValue() / divideTime;
                chartMap.put("dateTime", DateUtils.formatDateTime(calendar.getTime()));
                valueMap = UnitsUtil.getValueMap(String.valueOf(value), DEFAULT_RATE_UNIT, "Bps");
                chartMap.put("value", Double.parseDouble(valueMap.get("value")));
                chartMap.put("unitByReal", valueMap.get("units"));
                list.add(chartMap);
            }
            if (isDebug) {
                log.info(treeInfo.getItemName() + " id is " + treeInfo.getId() + " and getTopAppLineChart "
                        + netFlowType.name() + " list size is " + (CollectionUtils.isNotEmpty(list) ? list.size() : "null"));
            }
        } catch (Exception e) {
            log.error("流量统计，按时间间隔获取应用TOP折线图数据-->获取ES数据失败", e);
        }
        return list;
    }

    /**
     * 针对输出数据进行统计主机IP列表
     *
     * @param statList
     * @return
     */
    private List<NetFlowTopData> statHostData(List<NetFlowTopData> statList) {
        List<NetFlowTopData> hostList = new ArrayList<>();
        Map<String, Integer> hostMap = new HashMap<>();
        int index;
        String ip;
        NetFlowTopData hostData;
        for (NetFlowTopData data : statList) {
            //计算源IP
            ip = data.getSourceIp();
            if (hostMap.containsKey(ip)) {
                index = hostMap.get(ip);
                hostData = hostList.get(index);
                hostData.setSumData(hostData.getSumData() + data.getCompareData());
                hostData.setCompareData(hostData.getCompareData() + data.getCompareData());
                hostData.setSumPackage(hostData.getSumPackage() + data.getComparePackage());
            } else {
                hostData = new NetFlowTopData();
                hostData.setSourceIp(ip);
                hostData.setSumData(data.getCompareData());
                hostData.setCompareData(data.getCompareData());
                hostData.setSumPackage(data.getComparePackage());
                hostList.add(hostData);
                hostMap.put(ip, hostList.size() - 1);
            }
            //计算目标IP
            ip = data.getDstIp();
            if (hostMap.containsKey(ip)) {
                index = hostMap.get(ip);
                hostData = hostList.get(index);
                hostData.setSumData(hostData.getSumData() + data.getCompareData());
                hostData.setCompareData(hostData.getCompareData() + data.getCompareData());
                hostData.setSumPackage(hostData.getSumPackage() + data.getComparePackage());
            } else {
                hostData = new NetFlowTopData();
                hostData.setSourceIp(ip);
                hostData.setSumData(data.getCompareData());
                hostData.setCompareData(data.getCompareData());
                hostData.setSumPackage(data.getComparePackage());
                hostList.add(hostData);
                hostMap.put(ip, hostList.size() - 1);
            }
        }
        //排序
        sortTopList(hostList);
        //将数据进行计算
        Map<String, String> valueMap;
        for (NetFlowTopData data : hostList) {
            valueMap = UnitsUtil.getConvertedValue(new BigDecimal(data.getSumData()), "B");
            data.setUnit(valueMap.get("units"));
            data.setSumData(Double.valueOf(valueMap.get("value")));
        }
        return hostList;
    }

    /**
     * 获取前五的折线图
     *
     * @param topData   Top数据
     * @param type      流量监控类别（1：入  2：出  3：出+入）
     * @param treeInfo  节点信息
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private List<Map<String, Object>> getTopLineChart(NetFlowTopData topData, Integer type,
                                                      NetflowTreeEntity treeInfo, Date startTime, Date endTime) {
        NetFlowType netFlowType = NetFlowType.getType(type);
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(getEsIndex(startTime,endTime));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                    .gte(startTime.getTime())
                    .lte(endTime.getTime()));
            //时间分割
            DateHistogramInterval dateHistogramInterval = getDateHistogramInterval(startTime, endTime);
            int divideTime = getDivideTime(startTime, endTime);
            //根据资产ID获取对应的IP信息
            List<String> ipList = treeManageDao.getAssetsIpList(treeInfo.getItemAssetsId());
            BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
            for (String ip : ipList) {
                ipShouldQueryBuilder = ipShouldQueryBuilder.should(QueryBuilders.termQuery("sender", ip));
            }
            boolQueryBuilder.must(ipShouldQueryBuilder);
            //如果是接口，则需要增加筛选条件
            if (treeInfo.getItemType() == 1) {
                switch (netFlowType) {
                    //入流量
                    case IN:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量
                    case OUT:
                        boolQueryBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量+入流量
                    case BOTH:
                        BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder inBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder outBuilder = new BoolQueryBuilder();
                        inBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        outBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        shouldQueryBuilder.should(inBuilder).should(outBuilder);
                        boolQueryBuilder.must(shouldQueryBuilder);
                        break;
                    //入流量
                    default:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                }
                //如果是资产数据，则需要先查询出所有的IP数据
            } else {
                if (netFlowType == NetFlowType.IN || netFlowType == NetFlowType.OUT) {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder indexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        if (netFlowType == NetFlowType.IN) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        } else if (netFlowType == NetFlowType.OUT) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                        }
                    }
                    boolQueryBuilder.must(indexShouldQueryBuilder);
                } else {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder inIndexShouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder outIndexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        inIndexShouldQueryBuilder = inIndexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        outIndexShouldQueryBuilder = outIndexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                    }
                    shouldQueryBuilder.should(inIndexShouldQueryBuilder);
                    shouldQueryBuilder.should(outIndexShouldQueryBuilder);
                    boolQueryBuilder.must(shouldQueryBuilder);
                }
            }
            //设置源IP和目标IP
            boolQueryBuilder.must(QueryBuilders.termQuery("ipv4SrcAddr", topData.getSourceIp()));
            boolQueryBuilder.must(QueryBuilders.termQuery("ipv4DstAddr", topData.getDstIp()));
            DateHistogramAggregationBuilder timeAggregation = AggregationBuilders.dateHistogram("agg").field("createTime");
            // dateHistogramInterval官方已经标识弃用，并且查询效率低
            timeAggregation.fixedInterval(dateHistogramInterval);

            searchSourceBuilder.query(boolQueryBuilder);
            //求和
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sumBytes").field("inBytes");
            timeAggregation.subAggregation(sumAggregationBuilder);
            searchSourceBuilder.aggregation(timeAggregation);
            // 没有用到hits的数据，直接为0
            searchSourceBuilder.size(ZERO);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Histogram histogram = aggregations.get("agg");
            Map<String, Object> chartMap;
            Map<String, String> valueMap;
            Calendar calendar = Calendar.getInstance();
            for (Histogram.Bucket bucket : histogram.getBuckets()) {
                chartMap = new HashMap<>();
                ZonedDateTime time = (ZonedDateTime) bucket.getKey();
                calendar.set(time.getYear(), time.getMonthValue() - 1, time.getDayOfMonth(),
                        time.getHour() + 8, time.getMinute(), time.getSecond());
                Sum sum = bucket.getAggregations().get("sumBytes");
                double value = sum.getValue() / divideTime;
                chartMap.put("dateTime", DateUtils.formatDateTime(calendar.getTime()));
                valueMap = UnitsUtil.getValueMap(String.valueOf(value), DEFAULT_RATE_UNIT, "Bps");
                chartMap.put("value", Double.parseDouble(valueMap.get("value")));
                chartMap.put("unitByReal", valueMap.get("units"));
                list.add(chartMap);
            }
            if (isDebug) {
                log.info(treeInfo.getItemName() + " id is " + treeInfo.getId() + " and getTopLineChart " + netFlowType.name() + " list size is " + (CollectionUtils.isNotEmpty(list) ? list.size() : "null"));
            }
        } catch (Exception e) {
            log.error("流量统计，按时间间隔获取TOP数据-->获取ES数据失败", e);
        }
        return list;
    }

    /**
     * 补全折线图时间数据
     *
     * @param chartList 折线图数据
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private void updateChartList(List<Map<String, Object>> chartList, Date startTime, Date endTime) {
        List<String> dateList = getDateArray(startTime, endTime);
        Set<String> dateSet = new HashSet<>();
        for (Map<String, Object> map : chartList) {
            dateSet.add((String) map.get("dateTime"));
        }
        for (String date : dateList) {
            if (!dateSet.contains(date)) {
                Map<String, Object> zeroMap = new HashMap();
                zeroMap.put("dateTime", date);
                zeroMap.put("value", 0);
                zeroMap.put("unitByReal", DEFAULT_RATE_UNIT);
                chartList.add(zeroMap);
            }
        }
        Collections.sort(chartList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Date date1 = DateUtils.parse((String) o1.get("dateTime"), DateConstant.NORM_DATETIME);
                Date date2 = DateUtils.parse((String) o2.get("dateTime"), DateConstant.NORM_DATETIME);
                return date1.compareTo(date2);
            }
        });
    }

    /**
     * 根据开始时间和结束时间分割时间段
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private List<String> getDateArray(Date startTime, Date endTime) {
        List<String> dateList = new ArrayList<>();
        int divide = getDivideTime(startTime, endTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String dateStr;
        date:
        while (true) {
            calendar.add(Calendar.SECOND, divide);
            if (startTime.after(calendar.getTime())) {
                continue date;
            }
            if (endTime.before(calendar.getTime())) {
                break date;
            }
            dateStr = DateUtils.format(calendar.getTime(), DateConstant.NORM_DATETIME);
            dateList.add(dateStr);
        }
        return dateList;
    }

    /**
     * 获取时间差
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private int getDivideTime(Date startTime, Date endTime) {
        long intervalTime = endTime.getTime() - startTime.getTime();
        //如果开始时间和结束时间间隔小于等于1分钟，间隔1秒
        if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.MINUTE)) {
            return DateUnitEnum.getSeconds(1, DateUnitEnum.SECOND);
            //大于1分钟，小于等于30分钟，间隔30秒
        } else if (intervalTime <= DateUnitEnum.getMillis(30, DateUnitEnum.MINUTE)) {
            return DateUnitEnum.getSeconds(30, DateUnitEnum.SECOND);
            //大于30分钟，小于等于一个小时，间隔1分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.HOUR)) {
            return DateUnitEnum.getSeconds(1, DateUnitEnum.MINUTE);
            //大于一个小时，小于等于6个小时，间隔5分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(6, DateUnitEnum.HOUR)) {
            return DateUnitEnum.getSeconds(5, DateUnitEnum.MINUTE);
            //大于6个小时，小于等于12个小时，间隔十分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(12, DateUnitEnum.HOUR)) {
            return DateUnitEnum.getSeconds(10, DateUnitEnum.MINUTE);
            //大于12个小时，小于等于1天，间隔三十分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.DAY)) {
            return DateUnitEnum.getSeconds(30, DateUnitEnum.MINUTE);
            //大于一天，小于等于4天，间隔1小时
        } else if (intervalTime <= DateUnitEnum.getMillis(4, DateUnitEnum.DAY)) {
            return DateUnitEnum.getSeconds(1, DateUnitEnum.HOUR);
            //大于4天，小于等于一个月，间隔12个小时
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.MONTH)) {
            return DateUnitEnum.getSeconds(12, DateUnitEnum.HOUR);
            //大于一个月，小于等于一年，间隔一天
        } else if (intervalTime <= DateUnitEnum.getMillis(12, DateUnitEnum.MONTH)) {
            return DateUnitEnum.getSeconds(1, DateUnitEnum.DAY);
            //大于一年，间隔1周
        } else {
            return DateUnitEnum.getSeconds(1, DateUnitEnum.WEEK);
        }
    }

    /**
     * 根据开始时间和结束时间获取时间聚合
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private DateHistogramInterval getDateHistogramInterval(Date startTime, Date endTime) {
        long intervalTime = endTime.getTime() - startTime.getTime();
        //如果开始时间和结束时间间隔小于等于1分钟，间隔1秒
        if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.MINUTE)) {
            return DateHistogramInterval.seconds(1);
            //大于1分钟，小于等于30分钟，间隔30秒
        } else if (intervalTime <= DateUnitEnum.getMillis(30, DateUnitEnum.MINUTE)) {
            return DateHistogramInterval.seconds(30);
            //大于30分钟，小于等于一个小时，间隔1分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.HOUR)) {
            return DateHistogramInterval.minutes(1);
            //大于一个小时，小于等于6个小时，间隔5分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(6, DateUnitEnum.HOUR)) {
            return DateHistogramInterval.minutes(5);
            //大于6个小时，小于等于12个小时，间隔十分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(12, DateUnitEnum.HOUR)) {
            return DateHistogramInterval.minutes(10);
            //大于12个小时，小于等于1天，间隔三十分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.DAY)) {
            return DateHistogramInterval.minutes(30);
            //大于一天，小于等于4天，间隔1小时
        } else if (intervalTime <= DateUnitEnum.getMillis(4, DateUnitEnum.DAY)) {
            return DateHistogramInterval.hours(1);
            //大于4天，小于等于一个月，间隔12个小时
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.MONTH)) {
            return DateHistogramInterval.hours(12);
            //大于一个月，小于等于一年，间隔一天
        } else if (intervalTime <= DateUnitEnum.getMillis(12, DateUnitEnum.MONTH)) {
            return DateHistogramInterval.days(1);
            //大于一年，间隔1周
        } else {
            return DateHistogramInterval.weeks(1);
        }
    }

    /**
     * 统计流量监控数据
     *
     * @param netFlowType 流量监控类别（1：入  2：出  3：出+入）
     * @param treeInfo    节点信息
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    private List<NetFlowTopData> statNetFlowData(Integer netFlowType, NetflowTreeEntity treeInfo, Date startTime, Date endTime) {
        List<NetFlowTopData> statTopList = new ArrayList<>();
        //TOP数据集合,入流量和出流量集合
        List<NetFlowTopData> inTopList;
        List<NetFlowTopData> outTopList;
        List<NetFlowTopData> allTopList;
        Map<String, Integer> inIndexMap;
        Map<String, Integer> outIndexMap;
        NetFlowType type = NetFlowType.getType(netFlowType);
        try {
            switch (type) {
                case IN:
                    inTopList = getNetFlowList(type, treeInfo, startTime, endTime);
                    statTopList = new ArrayList<>(inTopList);
                    break;
                case OUT:
                    outTopList = getNetFlowList(type, treeInfo, startTime, endTime);
                    statTopList = new ArrayList<>(outTopList);
                    break;
                case BOTH:
                    inTopList = getNetFlowList(NetFlowType.IN, treeInfo, startTime, endTime);
                    outTopList = getNetFlowList(NetFlowType.OUT, treeInfo, startTime, endTime);
                    inIndexMap = getIndexMap(inTopList);
                    outIndexMap = getIndexMap(outTopList);
                    allTopList = getNetFlowList(type, treeInfo, startTime, endTime);
                    //给集合数据填充
                    fillTopList(allTopList, inTopList, outTopList, inIndexMap, outIndexMap);
                    statTopList = new ArrayList<>(allTopList);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("统计流量信息失败", e);
        }
        return statTopList;
    }

    /**
     * 计算流量数据占比
     *
     * @param statTopList 流量监控数据
     */
    private void calcNetFlowPercent(List<NetFlowTopData> statTopList) {
        double allData = 0;
        long allPackage = 0;
        for (NetFlowTopData data : statTopList) {
            allData += data.getCompareData();
            allPackage += data.getComparePackage();
        }
        for (NetFlowTopData data : statTopList) {
            data.setNetFlowPercent(MathUtils.getPercentValue(data.getCompareData(), allData));
            data.setPackagePercent(MathUtils.getPercentValue(data.getComparePackage(), allPackage));
            data.setNetFlowPercentString(MathUtils.getPercent(data.getCompareData(), allData));
            data.setPackagePercentString(MathUtils.getPercent(data.getComparePackage(), allPackage));
        }
    }

    /**
     * 计算流量数据占比
     *
     * @param statTopList 流量监控数据
     */
    private void calcAppNetFlowPercent(List<AppTopData> statTopList) {
        double allData = 0;
        long allPackage = 0;
        for (AppTopData data : statTopList) {
            allData += data.getCompareData();
            allPackage += data.getComparePackage();
        }
        for (AppTopData data : statTopList) {
            data.setNetFlowPercent(MathUtils.getPercentValue(data.getCompareData(), allData));
            data.setPackagePercent(MathUtils.getPercentValue(data.getComparePackage(), allPackage));
            data.setNetFlowPercentString(MathUtils.getPercent(data.getCompareData(), allData));
            data.setPackagePercentString(MathUtils.getPercent(data.getComparePackage(), allPackage));
            if (CollectionUtils.isNotEmpty(data.getChildList())) {
                for (AppTopData childData : data.getChildList()) {
                    childData.setNetFlowPercent(MathUtils.getPercentValue(childData.getCompareData(), allData));
                    childData.setPackagePercent(MathUtils.getPercentValue(childData.getComparePackage(), allPackage));
                    childData.setNetFlowPercentString(MathUtils.getPercent(childData.getCompareData(), allData));
                    childData.setPackagePercentString(MathUtils.getPercent(childData.getComparePackage(), allPackage));
                }
            }
        }
    }


    /**
     * 计算数据单位
     *
     * @param netFlowType 流量监控类别（1：入  2：出  3：出+入）
     * @param list        数据
     */
    private void calcNetFlowUnit(Integer netFlowType, List<? extends NetFlowTopData> list) {
        NetFlowType type = NetFlowType.getType(netFlowType);
        Map<String, String> valueMap;
        for (NetFlowTopData data : list) {
            switch (type) {
                case IN:
                    valueMap = UnitsUtil.getConvertedValue(new BigDecimal(data.getInData()), "B");
                    data.setInUnit(valueMap.get("units"));
                    data.setInData(Double.valueOf(valueMap.get("value")));
                    break;
                case OUT:
                    valueMap = UnitsUtil.getConvertedValue(new BigDecimal(data.getOutData()), "B");
                    data.setOutUnit(valueMap.get("units"));
                    data.setOutData(Double.valueOf(valueMap.get("value")));
                    break;
                case BOTH:
                    valueMap = UnitsUtil.getConvertedValue(new BigDecimal(data.getSumData()), "B");
                    data.setUnit(valueMap.get("units"));
                    data.setSumData(Double.valueOf(valueMap.get("value")));
                    valueMap = UnitsUtil.getConvertedValue(new BigDecimal(data.getInData()), "B");
                    data.setInUnit(valueMap.get("units"));
                    data.setInData(Double.valueOf(valueMap.get("value")));
                    valueMap = UnitsUtil.getConvertedValue(new BigDecimal(data.getOutData()), "B");
                    data.setOutUnit(valueMap.get("units"));
                    data.setOutData(Double.valueOf(valueMap.get("value")));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 填充数据（补充单项的出入量）
     *
     * @param allTopList  入+出集合
     * @param inTopList   入集合
     * @param outTopList  出集合
     * @param inIndexMap  入索引集合
     * @param outIndexMap 出索引集合
     */
    private void fillTopList(List<NetFlowTopData> allTopList,
                             List<NetFlowTopData> inTopList,
                             List<NetFlowTopData> outTopList,
                             Map<String, Integer> inIndexMap,
                             Map<String, Integer> outIndexMap) {
        //入流量监控数据
        NetFlowTopData inData;
        //出流量监控数据
        NetFlowTopData outData;
        //入流量监控索引
        int inIndex;
        //出流量监控索引
        int outIndex;
        String key;
        for (NetFlowTopData topData : allTopList) {
            //获取入和出的数据
            key = getIndexKey(topData);
            if (inIndexMap.containsKey(key)) {
                inIndex = inIndexMap.get(key);
                inData = inTopList.get(inIndex);
                topData.setInData(inData.getCompareData());
                topData.setInPackage(inData.getComparePackage());
            } else {
                topData.setInData(0D);
                topData.setInPackage(0);
            }
            if (outIndexMap.containsKey(key)) {
                outIndex = outIndexMap.get(key);
                outData = outTopList.get(outIndex);
                topData.setOutData(outData.getCompareData());
                topData.setOutPackage(outData.getComparePackage());
            } else {
                topData.setOutData(0D);
                topData.setOutPackage(0);
            }
        }
    }

    /**
     * 通过列表获取对应数据的索引集合
     *
     * @param list 集合数据
     * @return
     */
    private Map<String, Integer> getIndexMap(List<NetFlowTopData> list) {
        Map<String, Integer> indexMap = new HashMap<>();
        NetFlowTopData data;
        for (int i = 0; i < list.size(); i++) {
            data = list.get(i);
            indexMap.put(getIndexKey(data), i);
        }
        return indexMap;
    }

    /**
     * 获取KEY
     *
     * @param data
     * @return
     */
    private String getIndexKey(NetFlowTopData data) {
        return data.getSourceIp() + ":" + data.getSourcePort() + "to" + data.getDstIp() +
                ":" + data.getDstPort() + "protocol=" + data.getProtocol();
    }

    /**
     * 给流量监控集合机型排序(从大到小排)
     *
     * @param topList
     */
    private void sortTopList(List<? extends NetFlowTopData> topList) {
        Collections.sort(topList, new Comparator<NetFlowTopData>() {
            @Override
            public int compare(NetFlowTopData o1, NetFlowTopData o2) {
                if (o1.getCompareData() - o2.getCompareData() > 0) {
                    return -1;
                } else if (o1.getCompareData() - o2.getCompareData() < 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * 获取流量监控数据列表（从ES获取）
     *
     * @param netFlowType 流量监控类别（1：入  2：出  3：出+入）
     * @param treeInfo    节点信息
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return
     */
    private List<NetFlowTopData> getNetFlowList(NetFlowType netFlowType, NetflowTreeEntity treeInfo, Date startTime, Date endTime) {
        List<NetFlowTopData> list = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(getEsIndex(startTime,endTime));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                    .gte(startTime.getTime())
                    .lte(endTime.getTime()));
            //根据资产ID获取对应的IP信息
            List<String> ipList = treeManageDao.getAssetsIpList(treeInfo.getItemAssetsId());
            BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
            for (String ip : ipList) {
                ipShouldQueryBuilder = ipShouldQueryBuilder.should(QueryBuilders.termQuery("sender", ip));
            }
            boolQueryBuilder.must(ipShouldQueryBuilder);
            //如果是接口，则需要增加筛选条件
            if (treeInfo.getItemType() == 1) {
                switch (netFlowType) {
                    //入流量
                    case IN:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量
                    case OUT:
                        boolQueryBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        break;
                    //出流量+入流量
                    case BOTH:
                        BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder inBuilder = new BoolQueryBuilder();
                        BoolQueryBuilder outBuilder = new BoolQueryBuilder();
                        inBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        outBuilder.must(QueryBuilders.termQuery("outputInterface", treeInfo.getItemIndex()));
                        shouldQueryBuilder.should(inBuilder).should(outBuilder);
                        boolQueryBuilder.must(shouldQueryBuilder);
                        break;
                    //入流量
                    default:
                        boolQueryBuilder.must(QueryBuilders.termQuery("inputInterface", treeInfo.getItemIndex()));
                        break;
                }
                //如果是资产数据，则需要先查询出所有的IP数据
            } else {
                if (netFlowType == NetFlowType.IN || netFlowType == NetFlowType.OUT) {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder indexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        if (netFlowType == NetFlowType.IN) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        } else if (netFlowType == NetFlowType.OUT) {
                            indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                        }
                    }
                    boolQueryBuilder.must(indexShouldQueryBuilder);
                } else {
                    //获取所有接口索引
                    List<Integer> indexList = treeManageDao.getIfIndexList(treeInfo.getItemAssetsId());
                    BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder inIndexShouldQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder outIndexShouldQueryBuilder = new BoolQueryBuilder();
                    for (Integer ifIndex : indexList) {
                        inIndexShouldQueryBuilder = inIndexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                        outIndexShouldQueryBuilder = outIndexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                    }
                    shouldQueryBuilder.should(inIndexShouldQueryBuilder);
                    shouldQueryBuilder.should(outIndexShouldQueryBuilder);
                    boolQueryBuilder.must(shouldQueryBuilder);
                }
            }

            searchSourceBuilder.query(boolQueryBuilder);
            //分组
            List<CompositeValuesSourceBuilder<?>> compositeValuesSourceBuilders = new ArrayList<>();
            // 设置聚合字段，采用的复合聚合方式（没有term精准），数据量大后，terms导致查询效率非常低
            TermsValuesSourceBuilder srcAddrBuilder = new TermsValuesSourceBuilder("ipv4SrcAddr").field("ipv4SrcAddr").missingBucket(true);
            TermsValuesSourceBuilder dstAddrBuilder = new TermsValuesSourceBuilder("ipv4DstAddr").field("ipv4DstAddr").missingBucket(true);
            TermsValuesSourceBuilder srcPortBuilder = new TermsValuesSourceBuilder("l4SrcPort").field("l4SrcPort").missingBucket(true);
            TermsValuesSourceBuilder dstPortBuilder = new TermsValuesSourceBuilder("l4DstPort").field("l4DstPort").missingBucket(true);
            TermsValuesSourceBuilder protocolBuilder = new TermsValuesSourceBuilder("protocol").field("protocol").missingBucket(true);
            compositeValuesSourceBuilders.add(srcAddrBuilder);
            compositeValuesSourceBuilders.add(dstAddrBuilder);
            compositeValuesSourceBuilders.add(srcPortBuilder);
            compositeValuesSourceBuilders.add(dstPortBuilder);
            compositeValuesSourceBuilders.add(protocolBuilder);
            CompositeAggregationBuilder compositeAgg = AggregationBuilders.composite("composite_agg", compositeValuesSourceBuilders).size(1000);
            //求和
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sumBytes").field("inBytes");
            SumAggregationBuilder sumPackageAggregationBuilder = AggregationBuilders.sum("sumPackage").field("inPkts");
            compositeAgg.subAggregation(sumAggregationBuilder);
            compositeAgg.subAggregation(sumPackageAggregationBuilder);

            searchSourceBuilder.aggregation(compositeAgg);

            // 只用到了聚合数据,设置0，避免网络传输中的内存消耗
            searchSourceBuilder.size(ZERO);
            searchRequest.source(searchSourceBuilder);
            // TODO 10G数据量的时候，在ES不存在缓存的时候，查询需要花费100S的时间，计算方式（10G数据）: 资产数量（20） * 6 = 120
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            ParsedComposite aggregation = searchResponse.getAggregations().get("composite_agg");
            for (ParsedComposite.ParsedBucket bucket : aggregation.getBuckets()) {
                Map<String, Object> objectMap = bucket.getKey();
                String ipv4SrcAddr = (String) objectMap.get("ipv4SrcAddr");
                String ipv4DstAddr = (String) objectMap.get("ipv4DstAddr");
                String srcPortTerms = String.valueOf(objectMap.get("l4SrcPort"));
                String dstPortTerms = String.valueOf(objectMap.get("l4DstPort"));
                String protocolTerms = (String) objectMap.get("protocol");
                if (StringUtils.isNotEmpty(ipv4SrcAddr) && StringUtils.isNotEmpty(ipv4DstAddr) && StringUtils.isNotEmpty(srcPortTerms) &&
                        StringUtils.isNotEmpty(dstPortTerms) && StringUtils.isNotEmpty(protocolTerms)) {
                    Sum sum = bucket.getAggregations().get("sumBytes");
                    Sum sumPackage = bucket.getAggregations().get("sumPackage");
                    NetFlowTopData topData = new NetFlowTopData();
                    topData.setSourceIp(ipv4SrcAddr);
                    topData.setDstIp(ipv4DstAddr);
                    topData.setSourcePort(srcPortTerms);
                    topData.setDstPort(dstPortTerms);
                    topData.setProtocol(protocolTerms);
                    topData.setDataType(1);
                    topData.setCompareData(sum.getValue());
                    topData.setComparePackage((int) sumPackage.getValue());
                    list.add(topData);
                }
            }
            if (isDebug) {
                log.info(treeInfo.getItemName() + " id is " + treeInfo.getId() + " and getNetFlowList " + netFlowType.name() + " list size is " + (CollectionUtils.isNotEmpty(list) ? list.size() : "null"));
            }
        } catch (Exception e) {
            log.error("流量统计-->获取ES数据失败", e);
        }
        return list;
    }


    private String addInterfaceRule(List<AssetParam> assetParams, String ruleJson) {
        RuleManager ruleManager = null;

        String value = null;
        StringBuffer stringBuffer = new StringBuffer();
        for (AssetParam assetParam : assetParams) {
            for (InterfaceParam interfaceParam : assetParam.getInterfaceParamList()) {
                stringBuffer.append(NETFLOW_ITEM_SEP);
                stringBuffer.append(assetParam.getIp()).append(NETFLOW_SEP).append(interfaceParam.getIfIndex());
            }
        }

        if (StringUtils.isNotEmpty(ruleJson)) {
            ruleManager = ruleManageFactory.getRuleManager(ruleJson);
        } else {
            ruleManager = newRuleManager();
            value = stringBuffer.substring(1);
        }

        RuleProcessor ruleProcessor = ruleManager.getRuleProcessor(RULE_PROCESSOR);
        RuleGroupSet ruleGroupSet = (RuleGroupSet) ruleProcessor.getMessageRule();

        if (null == value) {
            value = ruleGroupSet.getValue() + stringBuffer.toString();
        }

        ruleGroupSet.setValue(value);

        Gson gson = new GsonBuilder().create();
        String content = gson.toJson(ruleManager);

        return content;
    }

    private RuleManager newRuleManager() {
        RuleManager ruleManager = new RuleManager();
        ruleManager.setId(RULE_MANAGER_ID);
        RuleProcessor ruleProcessor = new RuleProcessor();
        ruleManager.addRuleProcessor(ruleProcessor);
        ruleProcessor.setRuleName(RULE_PROCESSOR);
        ruleManager.initRuleProcessorMap();

        RuleOrSet ruleOrSet = new RuleOrSet();
        ruleProcessor.setMessageRule(ruleOrSet);

        for (int i = 0; i < filterKey.length; i++) {
            RuleSetContain ruleSetContain = new RuleSetContain();
            ruleSetContain.setKey(filterKey[i]);
            ruleOrSet.addMessageRule(ruleSetContain);
        }

        return ruleManager;
    }

    private String delInterfaces(List<AssetParam> assetParams, String ruleJson) throws Exception {
        Set<String> deleteSet = new HashSet<>();
        if (null != assetParams) {
            for (AssetParam assetParam : assetParams) {
                for (InterfaceParam interfaceParam : assetParam.getInterfaceParamList()) {
                    String deleteKey = assetParam.getIp() + NETFLOW_SEP + interfaceParam.getIfIndex();
                    deleteSet.add(deleteKey);
                }
            }
        }

        if (StringUtils.isNotEmpty(ruleJson)) {
            RuleManager ruleManager = ruleManageFactory.getRuleManager(ruleJson);
            RuleProcessor ruleProcessor = ruleManager.getRuleProcessor(RULE_PROCESSOR);
            AbstractMessageRule messageRule = (AbstractMessageRule) ruleProcessor.getMessageRule();

            String value = messageRule.getValue();
            Set<String> set = new HashSet<>();
            if (StringUtils.isNotEmpty(value)) {
                String[] items = value.split(NETFLOW_ITEM_SEP);
                Collections.addAll(set, items);
            }

            set.removeAll(deleteSet);

            StringBuffer stringBuffer = new StringBuffer();
            for (String item : set) {
                stringBuffer.append(NETFLOW_ITEM_SEP).append(item);
            }

            messageRule.setValue(stringBuffer.substring(1));

            Gson gson = new GsonBuilder().create();
            String content = gson.toJson(ruleManager);

            return content;
        }
        return null;
    }

    private Map getValue(String searchKey, Map cacheYamlMap) {
        String[] keys = searchKey.split("\\.");
        Map ret = cacheYamlMap;
        for (String key : keys) {
            if (null != ret) {
                ret = (Map) ret.get(key);
            }
        }
        return ret;
    }

    /**
     * 增加/删除资产树数据
     *
     * @param netflowParam 流量监控数据
     * @param type         操作类别
     */
    private void addOrDelAssets(NetflowParam netflowParam, OperationType type) {
        NetflowTreeEntity childTree;
        switch (type) {
            case add:
                for (AssetParam assets : netflowParam.getParamList()) {
                    NetflowTreeEntity tree = geneNetFlowTree();
                    tree.setItemIp(assets.getIp());
                    tree.setItemAssetsId(assets.getAssetId());
                    tree.setItemType(0);
                    tree.setItemName(assets.getAssetsName());
                    //增加资产数据
                    treeManageDao.insert(tree);
                    //增加接口数据
                    for (InterfaceParam interfaceParam : assets.getInterfaceParamList()) {
                        String itemIp = treeManageDao.getIpByAssetsIdAndIfIndex(assets.getAssetId(), interfaceParam.getIfIndex());
                        childTree = geneNetFlowTree();
                        childTree.setItemType(1);
                        childTree.setItemPid(tree.getId());
                        childTree.setItemIp(itemIp);
                        childTree.setItemAssetsId(assets.getAssetId());
                        childTree.setItemIndex(interfaceParam.getIfIndex());
                        childTree.setItemName(interfaceParam.getIfName());
                        treeManageDao.insert(childTree);
                    }
                }
                break;
            case delete:
                for (AssetParam assets : netflowParam.getParamList()) {
                    List<Integer> idList = new ArrayList<>();
                    for (InterfaceParam interfaceParam : assets.getInterfaceParamList()) {
                        idList.add(interfaceParam.getId());
                    }
                    //删除节点信息
                    UpdateWrapper<NetflowTreeEntity> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("delete_flag", false)
                            .in("id", idList);
                    updateWrapper.set("delete_flag", true);
                    updateWrapper.set("item_state", 0);
                    treeManageDao.update(null, updateWrapper);
                    //如果子节点信息全删除，则将资产节点也删除
                    QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
                    wrapper.eq("delete_flag", false);
                    wrapper.eq("item_pid", assets.getId());
                    int count = treeManageDao.selectCount(wrapper);
                    if (count == 0) {
                        //删除资产节点信息
                        UpdateWrapper<NetflowTreeEntity> assetsUpdateWrapper = new UpdateWrapper<>();
                        assetsUpdateWrapper.eq("delete_flag", false)
                                .eq("id", assets.getId());
                        assetsUpdateWrapper.set("delete_flag", true);
                        assetsUpdateWrapper.set("item_state", 0);
                        treeManageDao.update(null, assetsUpdateWrapper);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 开启/关闭流量监控
     *
     * @param id   对应ID
     * @param type 操作类别
     */
    private void switchNetFlow(Integer id, OperationType type) {
        UpdateWrapper<NetflowTreeEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("delete_flag", false)
                .eq("id", id);
        switch (type) {
            case start:
                updateWrapper.set("item_state", 1);
                break;
            case stop:
                updateWrapper.set("item_state", 0);
                break;
            default:
                break;
        }
        treeManageDao.update(null, updateWrapper);
    }

    /**
     * 开启/关闭流量监控
     *
     * @param id   对应ID
     * @param type 操作类别
     */
    private void switchNetFlow(OperationType type, List<Integer> id) {
        if (CollectionUtils.isEmpty(id)) {
            return;
        }
        UpdateWrapper<NetflowTreeEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("delete_flag", false)
                .in("id", id);
        switch (type) {
            case start:
                updateWrapper.set("item_state", 1);
                break;
            case stop:
                updateWrapper.set("item_state", 0);
                break;
            default:
                break;
        }
        treeManageDao.update(null, updateWrapper);
    }

    /**
     * 构建一个空白tree
     *
     * @return
     */
    private NetflowTreeEntity geneNetFlowTree() {
        NetflowTreeEntity tree = new NetflowTreeEntity();
        tree.setId(getMaxNetFlowTreeId());
        tree.setDeleteFlag(false);
        tree.setItemState(1);
        tree.setItemIp("");
        tree.setItemAssetsId("");
        tree.setItemIndex(0);
        tree.setItemPid(0);
        return tree;
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxNetFlowTreeId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<NetflowTreeEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        NetflowTreeEntity maxEntity = treeManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 更新ES数据,同时开启聚合功能
     */
    private void flushES() {
        PutMappingRequest request = new PutMappingRequest(ES_NETFLOW_ALL_INDEX);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        properties.put("properties", field);
        field.put("ipv4SrcAddr", value);
        value.put("type", "text");
        value.put("fielddata", true);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            AcknowledgedResponse response = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("开启ES聚合搜索失败", e);
        }


        request = new PutMappingRequest(ES_NETFLOW_ALL_INDEX);
        request.type("_doc");
        properties = new HashMap();
        field = new HashMap();
        value = new HashMap();
        properties.put("properties", field);
        field.put("ipv4DstAddr", value);
        value.put("type", "text");
        value.put("fielddata", true);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            AcknowledgedResponse response = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("开启ES聚合搜索失败", e);
        }

        request = new PutMappingRequest(ES_NETFLOW_ALL_INDEX);
        request.type("_doc");
        properties = new HashMap();
        field = new HashMap();
        value = new HashMap();
        properties.put("properties", field);
        field.put("protocol", value);
        value.put("type", "text");
        value.put("fielddata", true);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            AcknowledgedResponse response = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("开启ES聚合搜索失败", e);
        }
    }

    /**
     * 判断IP是否在指定网段内
     *
     * @param ip   ip
     * @param cidr 指定网段
     * @return
     */
    private boolean isInPhase(String ip, String cidr) {
        try {
            String[] ips = ip.split("\\.");
            int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16) |
                    (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
            int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
            int mask = 0xFFFFFFFF << (32 - type);
            String cidrIp = cidr.replaceAll("/.*", "");
            String[] cidrIps = cidrIp.split("\\.");
            int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16) |
                    (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);
            return (ipAddr & mask) == (cidrIpAddr & mask);
        } catch (Exception e) {
            log.error("判断IP是否在指定网段内失败", e);
            return false;
        }
    }

    /**
     * 判断IP是否命中IP列表
     *
     * @param ip     ip
     * @param ipList IP列表
     * @return
     */
    private boolean isInList(String ip, List<String> ipList) {
        Set<String> set = new HashSet<>(ipList);
        return set.contains(ip);
    }

    /**
     * 判断IP是否命中IP地址段
     *
     * @param ip      ip
     * @param ipPhase IP地址段
     * @return
     */
    private boolean isInRange(String ip, String ipPhase) {
        try {
            ipPhase = ipPhase.trim();
            ip = ip.trim();
            final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
            final String REGX_IPB = REGX_IP + "\\-" + REGX_IP;
            if (!ipPhase.matches(REGX_IPB) || !ip.matches(REGX_IP)) {
                return false;
            }

            int idx = ipPhase.indexOf('-');
            String[] sips = ipPhase.substring(0, idx).split("\\.");
            String[] sipe = ipPhase.substring(idx + 1).split("\\.");
            String[] sipt = ip.split("\\.");
            long ips = 0L, ipe = 0L, ipt = 0L;
            for (int i = 0; i < 4; ++i) {
                ips = ips << 8 | Integer.parseInt(sips[i]);
                ipe = ipe << 8 | Integer.parseInt(sipe[i]);
                ipt = ipt << 8 | Integer.parseInt(sipt[i]);
            }
            if (ips > ipe) {
                long t = ips;
                ips = ipe;
                ipe = t;
            }
            return ips <= ipt && ipt <= ipe;
        } catch (Exception e) {
            log.error("判断IP是否命中IP地址段失败", e);
            return false;
        }
    }

    /**
     * 使用gzip压缩字符串
     *
     * @param str 要压缩的字符串
     * @return
     */
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
        } catch (IOException e) {
            log.error("压缩失败", e);
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                }
            }
        }
        return new sun.misc.BASE64Encoder().encode(out.toByteArray());
    }

    /**
     * 使用gzip解压缩
     *
     * @param compressedStr 压缩字符串
     * @return
     */
    public static String uncompress(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            log.error("解压失败", e);
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return decompressed;
    }


    /**
     * 获取流量监控数据列表（从ES获取）
     *
     * @return
     */
//    private List<NetFlowCapResult> getNetFlowDetailList(NetFlowDetailParam param) {
//        List<NetFlowCapResult> list = new ArrayList<>();
//        Map<String, NetFlowCapResult> netFlowMap = new HashMap<>();
//        try {
//            SearchRequest searchRequest = new SearchRequest();
//            searchRequest.indices(CAPTCP_INDEX_ALIAS + getCurrentWeek());
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            searchSourceBuilder.sort("capTime", SortOrder.DESC);
//            searchSourceBuilder.size(100);
//            searchRequest.source(searchSourceBuilder);
//            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//            netFlowMap = analysisResult(searchResponse, param);
//            list.addAll(netFlowMap.values());
//        } catch (Exception e) {
//            log.error("失败", e);
//        }
//        return list;
//    }

    /**
     * 分析ES返回的数据
     *
     * @param searchResponse ES响应数据
     * @param param          筛选参数
     * @return
     */
//    private Map<String, NetFlowCapResult> analysisResult(SearchResponse searchResponse, NetFlowDetailParam param) {
//        Map<String, NetFlowCapResult> netFlowMap = new HashMap<>();
//        String key;
//        NetFlowCapResult capResult;
//        Calendar calendar = Calendar.getInstance();
//        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
//            NetFlowCapEntity capInfo = JSON.parseObject(JSON.toJSONString(searchHit.getSourceAsMap()), NetFlowCapEntity.class);
//            key = generateKey(capInfo);
//            if (netFlowMap.containsKey(key)) {
//                capResult = netFlowMap.get(key);
//                if (capResult.getMaxTime() < capInfo.getCapTime().getTime()) {
//                    capResult.setMaxTime(capInfo.getCapTime().getTime());
//                }
//                if (capResult.getMinTime() > capInfo.getCapTime().getTime()) {
//                    capResult.setMinTime(capInfo.getCapTime().getTime());
//                }
//                capResult.addSumBytes(capInfo.getLength());
//                capResult.addPackage();
//                capResult.getHeaderList().add(capInfo.getHeader());
//                capResult.getUriList().add(capInfo.getUri());
//            } else {
//                capResult = new NetFlowCapResult();
//                capResult.setSourceIp(capInfo.getSrcIp());
//                capResult.setSourcePort(capInfo.getSrcPort());
//                capResult.setDestIp(capInfo.getDestIp());
//                capResult.setDestPort(capInfo.getDestPort());
//                capResult.setSumBytes(capInfo.getLength());
//                capResult.setMaxTime(capInfo.getCapTime().getTime());
//                capResult.setMinTime(capInfo.getCapTime().getTime());
//                capResult.setSumPackage(1);
//                List<String> headers = new ArrayList<>();
//                headers.add(capInfo.getHeader());
//                capResult.setHeaderList(headers);
//                List<String> uris = new ArrayList<>();
//                uris.add(capInfo.getUri());
//                capResult.setUriList(uris);
//                netFlowMap.put(key, capResult);
//            }
//        }
//        for (NetFlowCapResult result : netFlowMap.values()) {
//            calendar.setTimeInMillis(result.getMaxTime());
//            result.setMaxTimeString(DateUtils.formatDateTime(calendar.getTime()));
//            calendar.setTimeInMillis(result.getMinTime());
//            result.setMinTimeString(DateUtils.formatDateTime(calendar.getTime()));
//        }
//        //条件筛选过滤
//        List<MwRuleSelectParam> rootList = getRootList(param.getSeniorMatchRuleList());
//        Iterator iterator = netFlowMap.keySet().iterator();
//        while (iterator.hasNext()) {
//            key = (String) iterator.next();
//            capResult = netFlowMap.get(key);
//            if (StringUtils.isNotEmpty(param.getFuzzyQuery()) && !key.contains(param.getFuzzyQuery())) {
//                iterator.remove();
//            }
//            if (StringUtils.isNotEmpty(param.getSourceIp()) && !capResult.getSourceIp().contains(param.getSourceIp())) {
//                iterator.remove();
//            }
//            if (param.getSourcePort() != null && param.getSourcePort() > 0 &&
//                    capResult.getSourcePort() != param.getSourcePort()) {
//                iterator.remove();
//            }
//            if (StringUtils.isNotEmpty(param.getDestIp()) && !capResult.getDestIp().contains(param.getDestIp())) {
//                iterator.remove();
//            }
//            if (param.getDestPort() != null && param.getDestPort() > 0 &&
//                    capResult.getDestPort() != param.getDestPort()) {
//                iterator.remove();
//            }
//            //高级查询
//            if (CollectionUtils.isNotEmpty(rootList)) {
//                if (!seniorMatch(capResult,rootList)) {
//                    iterator.remove();
//                }
//            }
//        }
//        return netFlowMap;
//    }

    /**
     * 高级匹配
     *
     * @param capResult      抓取数据
     * @param ruleSelectList 匹配规则
     * @return true:匹配成功  false :匹配失败
     */
    private boolean seniorMatch(NetFlowCapResult capResult, List<MwRuleSelectParam> ruleSelectList) {
        boolean result = false;
        HashMap<String, Object> assetsMap = new HashMap<>();
        assetsMap.put("sourceIp", capResult.getSourceIp());
        assetsMap.put("sourcePort", capResult.getSourcePort());
        assetsMap.put("destIp", capResult.getDestIp());
        assetsMap.put("destPort", capResult.getDestPort());
        cn.mw.monitor.weixinapi.MessageContext messageContext = new MessageContext();
        messageContext.setKey(assetsMap);
        result = DelFilter.delFilter(ruleSelectList, messageContext, ruleSelectList);
        return result;
    }

    private List<MwRuleSelectParam> getRootList(List<MwRuleSelectParam> ruleSelectList) {
        List<MwRuleSelectParam> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(ruleSelectList)) {
            return list;
        }
        for (MwRuleSelectParam s : ruleSelectList) {
            if (s.getParentKey().equals("root")) {
                list.add(s);
                return list;
            } else {
                list.addAll(getRootList(s.getConstituentElements()));
            }
        }
        return list;
    }

    /**
     * 获取KEY
     *
     * @param capInfo
     * @return
     */
    private String generateKey(NetFlowCapEntity capInfo) {
        return capInfo.getSrcIp() + ":" + capInfo.getSrcPort() +
                "to" + capInfo.getDestIp() + ":" + capInfo.getDestPort();
    }

    /**
     * 获取当前月份+星期
     *
     * @return
     */
    private String getCurrentWeek() {
        Date curDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(curDate);
    }

    private String getEsIndex(Date startTime, Date endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        String yearMonth = dateFormat.format(startTime);
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        String startIndex = new StringBuffer(INDEX_ALIAS).append(yearMonth).append("-").append(week).toString();

        calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        yearMonth = dateFormat.format(endTime);
        String endIndex = new StringBuffer(INDEX_ALIAS).append(yearMonth).append("-").append(week).toString();

        if (startIndex.equals(endIndex)) {
            return startIndex;
        } else {
            return ES_NETFLOW_ALL_INDEX;
        }
    }
}
