package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.labelManage.dao.MwLabelManageTableDao;
import cn.mw.monitor.labelManage.dto.MwLabelManageDTO;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.constant.ReportConstant;
import cn.mw.monitor.report.dao.MwReportTerraceManageDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.IpReportSreach;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.*;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.report.service.manager.MWReportCpuMemoryRealTimeManage;
import cn.mw.monitor.report.service.manager.RunTimeReportManager;
import cn.mw.monitor.report.timer.MwManualTimeTaskRun;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.server.serverdto.AssetsAvailableDTO;
import cn.mw.monitor.server.serverdto.AvailableInfoDTO;
import cn.mw.monitor.server.serverdto.InterfaceInfoEnum;
import cn.mw.monitor.server.serverdto.ItemGetDTO;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryAssetsTypeParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.service.server.param.QueryAssetsAvailableParam;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.webmonitor.model.MwHistoryDTO;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.*;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName MwReportTerraceManageServiceImpl
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/13 10:59
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwReportTerraceManageServiceImpl implements MwReportTerraceManageService {

    @Value("${report.diskHostGroupSize}")
    private int diskHostGroupSize;

    @Value("${report.diskUseCacheTime}")
    private long diskUseCacheTime;

    @Value("${report.getRedisRunTime.open}")
    private boolean getRedisRunTime;

    @Value("${report.getAssetsAbblie.open}")
    private boolean getAssetsAbblie;

    @Value("${report.history.group}")
    private Integer hisToryGroup;

    private SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");
    @Resource
    private MwReportTerraceManageDao terraceManageDao;
    @Autowired
    private MwReportTerraceManageService terraceManageService;
    @Autowired
    private MWUserDao userDao;

    @Autowired
    private MWOrgService orgService;

    @Autowired
    private MwLabelManageTableDao labelManageTableDao;

    @Autowired
    private MWNetWorkLinkService workLinkService;

    @Autowired
    private MwServerManager mwServerManager;

    private Boolean waitFor = false;

    @Resource
    private MwReportService mwReportService;

    @Autowired
    private MwTangibleAssetsService tangibleAssetsService;

    @Autowired
    private MwReportTerraceManageDao reportTerraceManageDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private RedisUtils redisUtils;


    @Autowired
    private MWWebZabbixManger zabbixManger;

    @Autowired
    private MwServerService serverService;

    @Autowired
    private RunTimeReportManager runTimeReportManager;


    @Autowired
    private MwReportService mwreportService;

    @Autowired
    private MWReportCpuMemoryRealTimeManage realTimeManage;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userCommonService;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    @Override
    @Transactional
    public Reply selectAssetsNews(QueryTangAssetsParam browseTangAssetsParam) {
        try {
            log.info("查询资产信息报表开始"+new Date());
            Integer dateType = browseTangAssetsParam.getDateType();
            if(dateType != null){
                List<Long> times = calculitionTime(dateType, null);
                Long startTime = times.get(0);
                Long endTime = times.get(1);
                List<String> chooseTimes = new ArrayList<>();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                date.setTime(startTime*1000);
                chooseTimes.add(format.format(date));
                Date date2 = new Date();
                date2.setTime(endTime*1000);
                browseTangAssetsParam.setModificationDateStart(date);
                browseTangAssetsParam.setModificationDateEnd(date2);
            }
            List<MwTangibleassetsTable> mwTangAssetses = null;
            //根据查询类型判断是否有资产过滤条件
            Integer treeType = browseTangAssetsParam.getTreeType();
            if(treeType != null){
                List<String> assetsids = getAssetsFilter(treeType);
                if(!CollectionUtils.isEmpty(assetsids)){
                    browseTangAssetsParam.setAssetsIds(assetsids);
                }
            }
            log.info("查询资产数据开始"+new Date());
            //获取资产数据信息
            mwTangAssetses = selectList(browseTangAssetsParam);
            if(CollectionUtils.isEmpty(mwTangAssetses)){
                PageInfo pageInfo = new PageInfo<>();
                return Reply.ok(pageInfo);
            }
            log.info("查询资产数据结束,长度为"+mwTangAssetses.size()+new Date());
            List<String> assetsIds = new ArrayList<>();
            //根据资产查询机构
            for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
                assetsIds.add(mwTangAssets.getId());
            }
            //资产对应的机构数据
            log.info("查询资产对应机构数据"+new Date());
            List<Map<String, String>> assetsOrgData = reportTerraceManageDao.getAssetsOrgData("ASSETS", assetsIds);
            log.info("查询资产对应机构数据成功，长度为"+assetsOrgData.size()+new Date());
            if(CollectionUtils.isEmpty(assetsOrgData)){
                PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                return Reply.ok(pageInfo);
            }
            //资产和机构数据组合
            List<MwTangibleassetsTable> assets = new ArrayList<>();
            for (Map<String, String> assetsOrgDatum : assetsOrgData) {
                String orgName = assetsOrgDatum.get("orgName");
                String typeId = assetsOrgDatum.get("typeId");
                if(StringUtils.isBlank(orgName) || StringUtils.isBlank(typeId)){
                    continue;
                }
                for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
                    if(typeId.equals(mwTangAssets.getId())){
                        MwTangibleassetsTable tangibleassetsTable = new MwTangibleassetsTable();
                        BeanUtils.copyProperties(mwTangAssets,tangibleassetsTable);
                        tangibleassetsTable.setOrgName(orgName);
                        assets.add(tangibleassetsTable);
                    }
                }
            }
            log.info("查询资产对数据报表结束，数据长度为"+assets.size()+new Date());
            Integer pageNumber = browseTangAssetsParam.getPageNumber();
            Integer pageSize = browseTangAssetsParam.getPageSize();
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > assets.size()){
                toIndex = assets.size();
            }
            List<MwTangibleassetsTable> mwTangibleassetsTables = assets.subList(fromIndex, toIndex);
            PageInfo pageInfo = new PageInfo<>(mwTangibleassetsTables);
            pageInfo.setTotal(assets.size());
            pageInfo.setList(mwTangibleassetsTables);
            return Reply.ok(pageInfo);
        }catch (Exception e){
            log.error("fail to selectAssetsNew with d={}, cause:{}", e);
            return Reply.fail("查询资产信息报表失败");
        }

    }

    /**
     * 获取过滤之后的资产ID数据
     * @param treeType
     * @return
     */
    private List<String> getAssetsFilter( Integer treeType){
        List<String> ids = new ArrayList<>();
        if(treeType != null){
            QueryAssetsTypeParam param = new QueryAssetsTypeParam();
            param.setTableType(1);
            param.setAssetsTypeId(treeType);
            Reply assetsTypesTree = tangibleAssetsService.getAssetsTypesTree(param);
            if(assetsTypesTree != null){
                List<AssetsTreeDTO> treeDTOS = (List<AssetsTreeDTO>) assetsTypesTree.getData();
                if(!CollectionUtils.isEmpty(treeDTOS)){
                    for (AssetsTreeDTO treeDTO : treeDTOS) {
                        List<AssetsDTO> assetsList = treeDTO.getAssetsList();
                        if(!CollectionUtils.isEmpty(assetsList)){
                            for (AssetsDTO assetsDTO : assetsList) {
                                ids.add(assetsDTO.getId());
                            }
                        }
                    }
                }
            }
        }
        return ids;
    }


    /**
     * 查询CPU历史信息
     * @return
     */
    @Override
    public Reply selectReportCPUNews(RunTimeQueryParam param) {
        try {
            if(param.getReportType() == 3){//失去CPU内存实时数据
                return Reply.ok(realTimeManage.getCpuAndMemoryRealTimeData(param,selectList(new QueryTangAssetsParam())));
            }
            Integer pageNumber = param.getPageNumber();
            Integer pageSize = param.getPageSize();
            //获取时间范围
            Integer dateType = param.getDateType();
            //根据时间判断取数来源
            PageInfo retData = getCpuAndMemoryReportCacheData(dateType, param.getTimingType(),param);
            if(retData != null && dateType != null && dateType != 2 && (param.getTimingType() == null || !param.getTimingType())){
                return Reply.ok(retData);
            }
            log.info("Cpu查询今日数据"+new Date());
            List<String> chooseTime = param.getChooseTime();
            List<Long> times = calculitionTime(dateType, chooseTime);
            Long startTime = times.get(0);
            Long endTime = times.get(1);
            Integer userId = null;
            UserDTO admin = userDao.selectByLoginName("admin");
            if(admin != null){
                userId = admin.getUserId();
            }
            List<RunTimeItemValue> runTimeItemValues = runTimeReportManager.getrunTimeCpuAndMemory(userId, startTime, endTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(!CollectionUtils.isEmpty(runTimeItemValues)){
                for (RunTimeItemValue runTimeItemValue : runTimeItemValues) {
                    String diskTotal = runTimeItemValue.getDiskTotal();
                    String diskUser = runTimeItemValue.getDiskUser();
                    String diskUserRate = runTimeItemValue.getDiskUserRate();
                    if(StringUtils.isBlank(diskTotal)){
                        runTimeItemValue.setDiskTotal("0.00GB");
                    }
                    if(StringUtils.isBlank(diskUser)){
                        runTimeItemValue.setDiskUser("0.00GB");
                    }
                    if(StringUtils.isBlank(diskUserRate)){
                        runTimeItemValue.setDiskUserRate("0.00%");
                    }
                    if(StringUtils.isBlank(runTimeItemValue.getMinValue())){
                        runTimeItemValue.setMinValue("0.00%");
                        runTimeItemValue.setMinValueTime(dateFormat.format(new Date(startTime*1000)));
                    }
                    if(StringUtils.isBlank(runTimeItemValue.getMaxValue())){
                        runTimeItemValue.setMaxValue("0.00%");
                        runTimeItemValue.setMaxValueTime(dateFormat.format(new Date(startTime*1000)));
                    }
                    if(StringUtils.isBlank(runTimeItemValue.getAvgValue())){
                        runTimeItemValue.setAvgValue("0.00%");
                    }
                    if(StringUtils.isBlank(runTimeItemValue.getMaxMemoryUtilizationRate())){
                        runTimeItemValue.setMaxMemoryUtilizationRate("0.00%");
                        runTimeItemValue.setMemoryMaxValueTime(dateFormat.format(new Date(startTime*1000)));
                    }
                    if(StringUtils.isBlank(runTimeItemValue.getMinMemoryUtilizationRate())){
                        runTimeItemValue.setMinMemoryUtilizationRate("0.00%");
                        runTimeItemValue.setMemoryMinValueTime(dateFormat.format(new Date(startTime*1000)));
                    }
                    String avgValue = runTimeItemValue.getAvgValue();
                    if(StringUtils.isNotBlank(avgValue)){
                        String replace = avgValue.replace("%", "");
                        if(Double.parseDouble(replace) > Double.parseDouble("70")){
                            runTimeItemValue.setIsCpuColor(true);
                        }else{
                            runTimeItemValue.setIsCpuColor(false);
                        }
                    }
                    if(StringUtils.isNotBlank(runTimeItemValue.getDiskUserRate())){
                        String replace = runTimeItemValue.getDiskUserRate().replace("%", "");
                        if(Double.parseDouble(replace) > Double.parseDouble("70")){
                            runTimeItemValue.setIsMemoryColor(true);
                        }else{
                            runTimeItemValue.setIsMemoryColor(false);
                        }
                    }
                }
            }
            log.info("查询CPU信息数据条数"+runTimeItemValues.size()+"数据信息："+runTimeItemValues);
            List<RunTimeItemValue> ret = runTimeItemValues.parallelStream()
                    .filter(data -> StringUtils.isNotEmpty(data.getDiskTotal())
                            && StringUtils.isNotEmpty(data.getDiskUser())
                            && StringUtils.isNotEmpty(data.getDiskUserRate())
                    ).collect(Collectors.toList());
            log.info("Cpu查询今日数据成功"+new Date()+ret.size()+ret);
            if(!CollectionUtils.isEmpty(ret)){
                for (RunTimeItemValue runTimeItemValue : ret) {
                    if(StringUtils.isBlank(runTimeItemValue.getLocation())){
                        runTimeItemValue.setLocation("");
                    }
                }
            }
            if(param.getTimingType() != null && param.getTimingType()){
                //重新计算CPU数据
//                getMemoryUtilization(param.getReportType(),ret,chooseTime,startTime);
//                getCpuDate(ret,startTime);
                List<RunTimeItemValue> realDatas = new ArrayList<>();
                //进行数据排序
                if(CollectionUtils.isNotEmpty(ret)){
                    //按照接口排序
                    Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                    Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                    realDatas = ret.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getLocation()), pinyin4jUtil.getStringPinYin(o2.getLocation()))).collect(Collectors.toList());

                }
                return Reply.ok(realDatas);
            }
//            getMemoryUtilization(param.getReportType(),ret,chooseTime,startTime);
            //过滤资产条件数据
            if(!CollectionUtils.isEmpty(param.getIds()) && !CollectionUtils.isEmpty(ret)){
                List<String> ids = param.getIds();
                Iterator<RunTimeItemValue> iterator = ret.iterator();
                while(iterator.hasNext()){
                    RunTimeItemValue next = iterator.next();
                    if(!ids.contains(next.getAssetsId())){
                        iterator.remove();
                    }
                }
            }
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > ret.size()){
                toIndex = ret.size();
            }
            getAssetsBrandAndLocation(ret);
            List<RunTimeItemValue> realDatas = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(ret)){
                //按照接口排序
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                realDatas = ret.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getLocation()), pinyin4jUtil.getStringPinYin(o2.getLocation()))).collect(Collectors.toList());

            }
            List<RunTimeItemValue> runTimeItemValueList = realDatas.subList(fromIndex, toIndex);
            String dateRegion = getDateRegion(param.getDateType(), param.getChooseTime());
            if(!CollectionUtils.isEmpty(runTimeItemValueList) && StringUtils.isNotBlank(dateRegion)){
                runTimeItemValueList.forEach(data->{
                    data.setTime(dateRegion);
                });
            }
            getCpuIcmpAndPing(runTimeItemValueList);
            PageInfo pageInfo = new PageInfo<>(runTimeItemValueList);
            pageInfo.setTotal(ret.size());
            pageInfo.setList(runTimeItemValueList);
            log.info("Cpu查询今日数据成返回"+new Date()+runTimeItemValueList);
            return Reply.ok(pageInfo);
        }catch (Exception e){
            log.error("fail to selectReportCPUNews with d={}, cause:{}", e);
            return Reply.fail("查询CPU报表失败");
        }
    }

    /**
     * 获取redis中的内存利用率
     * @param reportType 报表类型
     * @param runTimeItemValues 数据值
     */
    private void getMemoryUtilization(Integer reportType,List<RunTimeItemValue> runTimeItemValues,List<String> chooseTime,long startTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(CollectionUtils.isEmpty(runTimeItemValues))return;
        //获取内存利用率
        RunTimeQueryParam timeQueryParam = new RunTimeQueryParam();
        timeQueryParam.setDataSize(100000);
        timeQueryParam.setPageNumber(1);
        timeQueryParam.setPageSize(100000);
        if(CollectionUtils.isNotEmpty(chooseTime)){
            timeQueryParam.setChooseTime(chooseTime);
            timeQueryParam.setTimingType(true);
            timeQueryParam.setDateType(6);
        }else{
            timeQueryParam.setDateType(0);
        }
        timeQueryParam.setItemName("MEMORY_UTILIZATION");
        timeQueryParam.setReportItemType(0);
        Reply reply = mwreportService.getRunTimeItemUtilization(timeQueryParam);
        log.info("查询CPU报表内存使用数据"+reply);
        if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS)return;
        List<RunTimeItemValue> list = (List<RunTimeItemValue>) reply.getData();
        if(CollectionUtils.isEmpty(list))return;
        for (RunTimeItemValue runTimeItemValue : runTimeItemValues) {
            String assetsId = runTimeItemValue.getAssetsId();
            runTimeItemValue.setMemoryMaxValueTime(format.format(new Date(startTime*1000)));
            runTimeItemValue.setMemoryMinValueTime(format.format(new Date(startTime*1000)));
            for (RunTimeItemValue timeItemValue : list) {
                String id = timeItemValue.getAssetsId();
                if(StringUtils.isNotBlank(assetsId) && StringUtils.isNotBlank(id) && assetsId.equals(id)){
                    if(reportType == 1){
                        runTimeItemValue.setDiskUserRate(timeItemValue.getAvgValue()+"%");
                    }
                    String maxValue = timeItemValue.getMaxValue();
                    String minValue = timeItemValue.getMinValue();
                    if(StringUtils.isNotBlank(maxValue)){
                        runTimeItemValue.setMaxMemoryUtilizationRate(new BigDecimal(maxValue).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
                        runTimeItemValue.setMemoryMaxValueTime(timeItemValue.getMaxValueTime());
                    }
                    if(StringUtils.isNotBlank(minValue)){
                        runTimeItemValue.setMinMemoryUtilizationRate(new BigDecimal(minValue).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
                        runTimeItemValue.setMemoryMinValueTime(timeItemValue.getMinValueTime());
                    }
                    String avgValue = timeItemValue.getAvgValue();
                    if(StringUtils.isNotBlank(avgValue)){
                        runTimeItemValue.setDiskUserRate(new BigDecimal(timeItemValue.getAvgValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
                    }
                }
            }
        }

        RunTimeQueryParam timeQueryParam2 = new RunTimeQueryParam();
        timeQueryParam2.setDataSize(100000);
        timeQueryParam2.setPageNumber(1);
        timeQueryParam2.setPageSize(100000);
        if(CollectionUtils.isNotEmpty(chooseTime)){
            timeQueryParam2.setChooseTime(chooseTime);
            timeQueryParam2.setDateType(6);
            timeQueryParam2.setTimingType(true);
        }else{
            timeQueryParam2.setDateType(0);
        }
        timeQueryParam2.setItemName("CPU_UTILIZATION");
        timeQueryParam2.setReportItemType(0);
        Reply reply2 = mwreportService.getRunTimeItemUtilization(timeQueryParam2);
        if(reply2 == null || reply2.getRes() != PaasConstant.RES_SUCCESS)return;
        List<RunTimeItemValue> list2 = (List<RunTimeItemValue>) reply2.getData();
        log.info("查询CPUredis数据长度"+list2.size());
        if(CollectionUtils.isEmpty(list2))return;
        for (RunTimeItemValue runTimeItemValue : runTimeItemValues) {
            String assetsId = runTimeItemValue.getAssetsId();
            for (RunTimeItemValue timeItemValue : list2) {
                String id = timeItemValue.getAssetsId();
                if(StringUtils.isNotBlank(assetsId) && StringUtils.isNotBlank(id) && assetsId.equals(id)){
                    runTimeItemValue.setMaxValue(new BigDecimal(timeItemValue.getMaxValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN)+"%");
                    runTimeItemValue.setAvgValue(new BigDecimal(timeItemValue.getAvgValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN)+"%");
                    runTimeItemValue.setMinValue(new BigDecimal(timeItemValue.getMinValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN)+"%");
                }
            }
        }
    }

    /**
     * 获取数据的品牌与位置
     * @param ret
     */
    private void getAssetsBrandAndLocation(List<RunTimeItemValue> ret){
        if(CollectionUtils.isEmpty(ret))return;
        List<String> ids = new ArrayList<>();
        for (RunTimeItemValue runTimeItemValue : ret) {
            String assetsId = runTimeItemValue.getAssetsId();
            ids.add(assetsId);
        }
        if(CollectionUtils.isEmpty(ids))return;
        //查询资产的品牌
        List<Map<String, Object>> assetsBrand = reportTerraceManageDao.getAssetsBrand(ids);
        if(CollectionUtils.isNotEmpty(assetsBrand)){
            for (Map<String, Object> map : assetsBrand) {
                String id = (String) map.get("id");
                String vendor = (String) map.get("vendor");
                String icon = (String) map.get("icon");
                Integer urlType = (Integer) map.get("urlType");
                for (RunTimeItemValue runTimeItemValue : ret) {
                    String assetsId = runTimeItemValue.getAssetsId();
                    if(StringUtils.isNotBlank(id) && id.equals(assetsId)){
                        runTimeItemValue.setBrand(vendor);
                        runTimeItemValue.setUrl(icon);
                        runTimeItemValue.setUrlType(urlType);
                    }
                }
            }
        }
        //查询资产标签位置信息
        List<Map<String, String>> assetsLabelSeat = reportTerraceManageDao.getAssetsLabelLocation(ids);
        if(CollectionUtils.isNotEmpty(assetsLabelSeat)){
            for (Map<String, String> map : assetsLabelSeat) {
                String typeId = map.get("typeId");
                String dropValue = map.get("dropValue");
                for (RunTimeItemValue runTimeItemValue : ret) {
                    String assetsId = runTimeItemValue.getAssetsId();
                    if(StringUtils.isNotBlank(typeId) && typeId.equals(assetsId)){
                        runTimeItemValue.setLocation(dropValue);
                    }
                }
            }
        }
    }


    /**
     * 获取CPU延迟信息和响应信息
     * @param ret
     */
    private void getCpuIcmpAndPing(List<RunTimeItemValue> ret){
        if(CollectionUtils.isEmpty(ret))return;
        //zabbix服务器ID及资产主机存储
        Map<Integer,List<String>> serverMap = new HashMap<>();
        for (RunTimeItemValue runTimeItemValue : ret) {
            String assetsId = runTimeItemValue.getHostId();
            Integer serverId = runTimeItemValue.getServerId();
            if(serverId == null)continue;
            if(serverMap.containsKey(serverId)){
                List<String> hostIds = serverMap.get(serverId);
                hostIds.add(assetsId);
                serverMap.put(serverId,hostIds);
            }else{
                List<String> hostIds = new ArrayList<>();
                hostIds.add(assetsId);
                serverMap.put(serverId,hostIds);
            }
        }
        if(serverMap.isEmpty())return;
        //查询zabbix中item
        for (Integer serverId : serverMap.keySet()) {
            List<String> hostIds = serverMap.get(serverId);
            MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(serverId, "ICMP_PING", hostIds, false);
            if (result0.getCode() == 0){
                JsonNode jsonNode = (JsonNode) result0.getData();
                if (jsonNode.size() > 0){
                    for (int i = 0; i < jsonNode.size(); i++){
                        if (null == jsonNode.get(i)
                                || null == jsonNode.get(i).get("hostid")
                                || null == jsonNode.get(i).get("itemid")) {
                            continue;
                        }
                        String hostid =jsonNode.get(i).get("hostid").asText();
                        String lastValue =jsonNode.get(i).get("lastvalue").asText();
                        for (RunTimeItemValue runTimeItemValue : ret) {
                            String assetsId = runTimeItemValue.getHostId();
                            if(hostid.equals(assetsId) && StringUtils.isNotBlank(lastValue)){
                                int pingValue = Integer.parseInt(lastValue);
                                if(pingValue == 0){
                                    runTimeItemValue.setIcmpPing("Down");
                                }
                                if(pingValue == 1){
                                    runTimeItemValue.setIcmpPing("Up");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void getCpuDate(List<RunTimeItemValue> runTimeItemValueList,Long time){
        //根据数据加时间查询数据库运行状态数据
        Date date = new Date();
        date.setTime(time*1000);
        //根据时间查询数据库
        List<RunTimeItemValue> runTimeItemValues = reportTerraceManageDao.selectRunStateDailyData(date, date);
        if(!CollectionUtils.isEmpty(runTimeItemValueList) && !CollectionUtils.isEmpty(runTimeItemValues)){
            runTimeItemValueList.forEach(data->{
                String assetName = data.getAssetName();
                String ip = data.getIp();
                for (RunTimeItemValue runTimeItemValue : runTimeItemValues) {
                    String cpuAssetName = runTimeItemValue.getAssetName();
                    String cpuIp = runTimeItemValue.getIp();
                    if((assetName+ip).equals(cpuAssetName+cpuIp) && "CPU_UTILIZATION".equals(runTimeItemValue.getItemName())){
                        if(StringUtils.isNotBlank(runTimeItemValue.getMaxValue())){
                            data.setMaxValue( new BigDecimal(runTimeItemValue.getMaxValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
                        }
                        if(StringUtils.isNotBlank(runTimeItemValue.getMinValue())){
                            data.setMaxValue( new BigDecimal(runTimeItemValue.getMinValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
                        }
                        if(StringUtils.isNotBlank(runTimeItemValue.getAvgValue())){
                            data.setMaxValue( new BigDecimal(runTimeItemValue.getAvgValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
                        }
                    }
                }
            });
        }
    }


    /**
     * 根据数据判断是否从数据库取缓存数据
     * @param dateType
     * @param timingType
     */
    private PageInfo getCpuAndMemoryReportCacheData(Integer dateType,Boolean timingType,RunTimeQueryParam param) throws ParseException {
        List<RunTimeItemValue> dataList = new ArrayList<>();
        List<String> ids = param.getIds();
        if(dateType != null && dateType !=2 &&  (timingType == null || !timingType)){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String time = "";
            Long startTime = 0l;
            Long endTime = 0l;
            log.info("查询CPU报表数据库数据"+new Date());
            String dateRegion = getDateRegion(param.getDateType(), param.getChooseTime());
            log.info("查询CPU数据库数据2"+param.getDateType()+":::"+param.getChooseTime());
            //判断选择条件是昨天，上周，还是上月
            if(dateType != null && dateType == 1){//昨天
                log.info("查询CPU数据库数据昨天"+param.getDateType()+":::"+param.getChooseTime());
                //获取昨天的日期
                List<Date> yesterday = ReportDateUtil.getYesterday();
                startTime = yesterday.get(0).getTime();
                endTime = yesterday.get(1).getTime();
                time = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
                dataList = reportTerraceManageDao.selectCpuAndMemoryReportDailyData(yesterday.get(0), yesterday.get(1),ids);
            }
            if(dateType != null && dateType == 5){//上周
                log.info("查询CPU数据库数据上周"+param.getDateType()+":::"+param.getChooseTime());
                List<Date> lastWeek = ReportDateUtil.getLastWeek();
                startTime = lastWeek.get(0).getTime();
                endTime = lastWeek.get(1).getTime();
                time = format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1));
                dataList = reportTerraceManageDao.selectCpuAndMemoryReportWeellyData(time,ids);
            }
            if(dateType != null && dateType == 8){//上月
                log.info("查询CPU数据库数据上月"+param.getDateType()+":::"+param.getChooseTime());
                List<Date> lastMonth = ReportDateUtil.getLastMonth();
                startTime = lastMonth.get(0).getTime();
                endTime = lastMonth.get(1).getTime();
                time = format.format(lastMonth.get(0))+"~"+format.format(lastMonth.get(1));
                dataList = reportTerraceManageDao.selectCpuAndMemoryReportMonthlyData(time,ids);
            }
            if(!CollectionUtils.isEmpty(param.getChooseTime())){
                log.info("查询CPU数据库数据3"+param.getDateType()+":::"+param.getChooseTime());
                List<String> chooseTime = param.getChooseTime();
                startTime = format.parse(chooseTime.get(0)).getTime();
                endTime = format.parse(chooseTime.get(1)).getTime();
                time = chooseTime.get(0).substring(0,10)+"~"+ chooseTime.get(1).substring(0,10);
                List<RunTimeItemValue> runTimeItemValues = reportTerraceManageDao.selectCpuAndMemoryReportDailyData(format.parse(chooseTime.get(0).substring(0, 10)), format.parse(chooseTime.get(1).substring(0, 10)), ids);
                log.info("查询CPU数据库数据4"+format.parse(chooseTime.get(0).substring(0, 10))+":::"+format.parse(chooseTime.get(1).substring(0, 10))+":::"+runTimeItemValues);
                dataList = MWReportHandlerDataLogic.handleCpuAndMomeryReportData(runTimeItemValues, startTime, endTime);
                log.info("查询CPU数据库数据5"+dataList);
            }
            //进行数据计算组合
            if(CollectionUtils.isEmpty(dataList)){
                return null;
            }
            List<RunTimeItemValue> list = new ArrayList<>();
            dataList.stream().filter(distinctByKey(p -> p.getAssetName()+p.getAssetsId()))  //filter保留true的值
                    .forEach(list::add);
            for (RunTimeItemValue runTimeItemValue : list) {
                if(StringUtils.isBlank(runTimeItemValue.getLocation())){
                    runTimeItemValue.setLocation("");
                }
                String avgValue = runTimeItemValue.getAvgValue();
                if(StringUtils.isNotBlank(avgValue)){
                    String replace = avgValue.replace("%", "");
                    if(Double.parseDouble(replace) > Double.parseDouble("70")){
                        runTimeItemValue.setIsCpuColor(true);
                    }else{
                        runTimeItemValue.setIsCpuColor(false);
                    }
                }
                String diskUserRate = runTimeItemValue.getDiskUserRate();
                if(StringUtils.isNotBlank(diskUserRate)){
                    String replace = diskUserRate.replace("%", "");
                    if(Double.parseDouble(replace) > Double.parseDouble("70")){
                        runTimeItemValue.setIsMemoryColor(true);
                    }else{
                        runTimeItemValue.setIsMemoryColor(false);
                    }
                }
            }
            getAssetsBrandAndLocation(list);
            List<RunTimeItemValue> realData = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(list)){
                //按照接口排序
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                realData = list.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getLocation()), pinyin4jUtil.getStringPinYin(o2.getLocation()))).collect(Collectors.toList());

            }
            Integer pageNumber = param.getPageNumber();
            Integer pageSize = param.getPageSize();
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > realData.size()){
                toIndex = realData.size();
            }
            List<RunTimeItemValue> runTimeItemValueList = realData.subList(fromIndex, toIndex);
            getHostId(runTimeItemValueList);
            getCpuIcmpAndPing(runTimeItemValueList);

            if(!CollectionUtils.isEmpty(runTimeItemValueList) && StringUtils.isNotBlank(dateRegion)){
                runTimeItemValueList.forEach(data->{
                    data.setTime(dateRegion);
                });
            }
            PageInfo pageInfo = new PageInfo<>(runTimeItemValueList);
            pageInfo.setTotal(realData.size());
            pageInfo.setList(runTimeItemValueList);
            return pageInfo;
        }
        return null;
    }

    /**
     * 获取数据的hostID
     * @param list
     */
    private void getHostId(List<RunTimeItemValue> list){
        if(CollectionUtils.isEmpty(list))return;
        List<String> ids = new ArrayList<>();
        for (RunTimeItemValue runTimeItemValue : list) {
            ids.add(runTimeItemValue.getAssetsId());
        }
        List<Map<String, Object>> assetsHostId = reportTerraceManageDao.getAssetsHostId(ids);
        if(assetsHostId.isEmpty())return;
        for (Map<String, Object> map : assetsHostId) {
            Object id = map.get("id");
            Object hostId =  map.get("hostId");
            Object serverId = map.get("serverId");
            for (RunTimeItemValue runTimeItemValue : list) {
                if(id != null && id.toString().equals(runTimeItemValue.getAssetsId())){
                    runTimeItemValue.setHostId(hostId.toString());
                    if(serverId != null){
                        runTimeItemValue.setServerId(Integer.parseInt(serverId.toString()));
                    }
                }
            }
        }
    }


    public static Integer getInteger(String str) {
        int i = 0;
        String reg = "[^0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(str);
        String trim = m.replaceAll("").trim();
        String units = str.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
        if ("h".equals(units)) {
            i = Integer.valueOf(trim) * 60 * 60;
        }
        if ("m".equals(units)) {
            i = Integer.valueOf(trim) * 60;
        }
        if ("s".equals(units)) {
            i = Integer.valueOf(trim);
        }
        return i;
    }

    public static Integer getIntegerNew(String str) {
        int i = 0;
        String reg = "[^0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(str);
        String trim = m.replaceAll("").trim();
        String units = str.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
        if ("h".equals(units)) {
            i = (Integer.valueOf(trim) * 60 * 60) + 1;
        }
        if ("m".equals(units)) {
            i = (Integer.valueOf(trim) * 60) + 1;
        }
        if ("s".equals(units)) {
            i = Integer.valueOf(trim);
        }
        return i;
    }

    private List<Long> calculitionTime(Integer dateType, List<String> chooseTime){
        DateTypeEnum dateTypeEnum = DateTypeEnum.getDateTypeEnum(dateType);
        if(dateTypeEnum == null && CollectionUtils.isEmpty(chooseTime)){
            dateTypeEnum = DateTypeEnum.YESTERDAY;
        }
        return calculitionTime(dateTypeEnum, chooseTime);
    }

    private List<Long> calculitionTime(DateTypeEnum dateTypeEnum, List<String> chooseTime){
        long start = 0;
        long end = 0;
        log.info("报表定时任务开始进行时间转换");
        if(CollectionUtils.isEmpty(chooseTime)||dateTypeEnum.getType()<11){
            switch (dateTypeEnum){
                case YESTERDAY:
                    //昨天
                    List<Date> yesterday = ReportDateUtil.getYesterday();
                    start = yesterday.get(0).getTime() / 1000;
                    end = yesterday.get(1).getTime() / 1000;
                    break;
                case TODAY:
                    //本日
                    List<Date> today = ReportDateUtil.getToday();
                    start = today.get(0).getTime() / 1000;
                    end = today.get(1).getTime() / 1000;
                    break;
                case LATEST_7DAY:
                    //最近7天
                    Long curr = System.currentTimeMillis();
                    end = System.currentTimeMillis() / 1000;
                    long l2 = 7*24*60*60;
                    start = (curr-(l2*1000)) / 1000;
                    break;
                case THIS_WEEK:
                    //本周
                    List<Date> week = ReportDateUtil.getWeek();
                    start = week.get(0).getTime() / 1000;
                    end = week.get(1).getTime() / 1000;
                    break;
                case LAST_WEEK:
                    //上周
                    List<Date> lastWeek = ReportDateUtil.getLastWeek();
                    start = lastWeek.get(0).getTime() / 1000;
                    end = lastWeek.get(1).getTime() / 1000;
                    break;
                case LATEST_30DAY:
                    //最近30天
                    Long currDate = System.currentTimeMillis();
                    end = System.currentTimeMillis() / 1000;
                    long l3 = 30*24*60*60;
                    start = (currDate-(l3*1000)) / 1000;
                    break;
                case THIS_MONTH:
                    //本月
                    List<Date> month = ReportDateUtil.getMonth();
                    start = month.get(0).getTime() / 1000;
                    end = month.get(1).getTime() / 1000;
                    break;
                case LAST_MONTH:
                    //上月
                    List<Date> lastMonth = ReportDateUtil.getLastMonth();
                    start = lastMonth.get(0).getTime() / 1000;
                    end = lastMonth.get(1).getTime() / 1000;
                    break;
                case LAST_12MONTH:
                    //近12个月
                    List<Date> front12Month = ReportDateUtil.getFront12Month();
                    start = front12Month.get(0).getTime() / 1000;
                    end = front12Month.get(1).getTime() / 1000;
                    break;
                case THIS_YEAR:
                    //今年
                    List<Date> year = ReportDateUtil.getYear();
                    start = year.get(0).getTime() / 1000;
                    end = year.get(1).getTime() / 1000;
                    break;
                default:
                    break;
            }
        }
        log.info("报表定时任务开始进行时间转换结束");
        if(!CollectionUtils.isEmpty(chooseTime)||dateTypeEnum.getType()==DateTypeEnum.SET_DATE.getType()){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if (dateTypeEnum.getType()==DateTypeEnum.SET_DATE.getType()){
                    start = format.parse(chooseTime.get(0)).getTime() / 1000;
                    end = format.parse(chooseTime.get(1)).getTime() / 1000;
                }
            } catch (Exception e) {
                try{
                    SimpleDateFormat formatTwo = new SimpleDateFormat("yyyy-MM-dd");
                    start = formatTwo.parse(chooseTime.get(0)).getTime() / 1000;
                    end = formatTwo.parse(chooseTime.get(1)).getTime() / 1000;
                }catch (Exception f){
                    log.error("时间转换失败1",f);
                }
                log.error("时间转换失败2",e);
            }
        }
        List<Long> times = new ArrayList<>();
        times.add(start);
        times.add(end);
        log.info("报表时间转换正常"+start+":"+end);
        return times;
    }


    /**
     * 查询资产信息
     * @param qParam
     * @return
     */
    public  List<MwTangibleassetsTable> selectList(QueryTangAssetsParam qParam) {
        try {
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setPageNumber(1);
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setIsQueryAssetsState(false);
            assetsParam.setUserId(userCommonService.getAdmin());
            List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
            return mwTangAssetses;
        } catch (Exception e) {
            log.error("fail to selectList with mtaDTO={}, cause:{}", qParam, e);
        }
        return null;
    }

    @Override
    public Reply selectReportLinkNews(TrendParam trendParam) {
        try{
            Integer pageNumber = trendParam.getPageNumber();
            Integer pageSize = trendParam.getPageSize();
            List<LineFlowReportParam> datas = new ArrayList<>();
            //获取日期区间
            Integer dateType = trendParam.getDateType();
            log.info("进行线路流量数据查询222");
            if(trendParam.getTimingType() == null || !trendParam.getTimingType()){
                log.info("进行线路流量数据查询333");
                //根据时间判断取数来源
                List<LineFlowReportParam> dataBaseList = distinctList(getLinkFlowCensusReportCacheData(dateType, trendParam.getTimingType(),trendParam));
                if(!CollectionUtils.isEmpty(dataBaseList)){
                    int fromIndex = pageSize * (pageNumber -1);
                    int toIndex = pageSize * pageNumber;
                    if(toIndex > dataBaseList.size()){
                        toIndex = dataBaseList.size();
                    }
                    List<LineFlowReportParam> list = dataBaseList.subList(fromIndex, toIndex);
                    PageInfo pageInfo = new PageInfo<>(list);
                    pageInfo.setTotal(dataBaseList.size());
                    pageInfo.setList(list);
                    return Reply.ok(pageInfo);
                }else{
                    PageInfo pageInfo = new PageInfo<>();
                    return Reply.ok(pageInfo);
                }
            }
            log.info("进行线路流量数据查询444");
            Long startTime = 0l;
            Long endTime = 0l;
            log.info("线路流量执行666"+trendParam.getTimingType()+dateType+":"+trendParam.getChooseTime());
            if(trendParam.getTimingType() != null && trendParam.getTimingType()){
                List<String> chooseTime = trendParam.getChooseTime();
                List<Long> times = calculitionTime(dateType, chooseTime);
                startTime = times.get(0);
                endTime = times.get(1);
            }
            log.info("线路流量执行777"+startTime+":"+endTime);
            List<MwTangibleassetsTable> mwTangibleassetsTables = selectList(new QueryTangAssetsParam());
            /**
             * 去除ICMP类型的资产
             */
            if(CollectionUtils.isNotEmpty(mwTangibleassetsTables)){
                Iterator<MwTangibleassetsTable> iterator = mwTangibleassetsTables.iterator();
                while(iterator.hasNext()){
                    MwTangibleassetsTable next = iterator.next();
                    String assetsTypeName = next.getAssetsTypeName();
                    if(StringUtils.isNotBlank(assetsTypeName) && "ICMP".equals(assetsTypeName)){
                        iterator.remove();
                    }
                }
            }
            log.info("线路流量执行888"+mwTangibleassetsTables.size());
            if(CollectionUtils.isEmpty(mwTangibleassetsTables)){
                if(trendParam.getLineType() != null && trendParam.getLineType() == 2){
                    return null;
                }
                PageInfo pageInfo = new PageInfo<>(datas);
                pageInfo.setTotal(datas.size());
                pageInfo.setList(datas);
                return Reply.ok(pageInfo);
            }
            List<String> itemNames = Arrays.asList(InterfaceInfoEnum.MW_INTERFACE_OUT_TRAFFIC.getName(),
                    InterfaceInfoEnum.MW_INTERFACE_IN_TRAFFIC.getName());
            if(trendParam.getLineType() == null){
                trendParam.setLineType(10);
            }
            List<LineFlowReportParam> zabbixLineFlowReportParams = getLineFlowCensus(itemNames, mwTangibleassetsTables, datas, startTime, endTime, trendParam.getLineType());
            if(trendParam.getTimingType() != null && trendParam.getTimingType()){
                return Reply.ok(zabbixLineFlowReportParams);
            }
            if(trendParam.getLineType() != null && trendParam.getLineType() == 2){
                return Reply.ok(zabbixLineFlowReportParams);
            }
            return Reply.ok(null);
        }catch (Exception e){
            log.error("fail to selectList with selectReportLinkNews={}, cause:{}"+trendParam, e);
            return Reply.fail("fail to selectList with selectReportLinkNews={}, cause:{}");
        }

    }

    /**
     * 集合去重
     */
    private  List<LineFlowReportParam> distinctList(List<LineFlowReportParam> dataBaseList){
        List<LineFlowReportParam> newList = new ArrayList<>();
        if(CollectionUtils.isEmpty(dataBaseList)){return newList;}
        dataBaseList.stream().filter(distinctByKey(p -> p.getAssetsName()+p.getInterfaceName()))  //filter保留true的值
                .forEach(newList::add);
        return newList;
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    private List changeData(String strData){
        List list = new ArrayList<>();
        double data = 0;
        String accepAvgUnit = "";
        if(StringUtils.isNotBlank(strData)){
            String str = "";
            for (int i = 0; i < strData.length(); i++) {
                if((strData.charAt(i) >= 48 && strData.charAt(i) <= 57) || strData.charAt(i) == '.'){
                    str +=strData.charAt(i);
                }else{
                    accepAvgUnit +=  strData.charAt(i);
                }
            }
            if(StringUtils.isNotBlank(str)){
                data = Double.parseDouble(str);
            }
        }
        if(StringUtils.isNotBlank(accepAvgUnit)){
            list.add(data);
            list.add(accepAvgUnit);
        }
        return list;
    }

    private List<LineFlowReportParam> getlineFlowData( List<MwTangibleassetsTable> mwTangibleassetsTables,List<String> itemNames, List<LineFlowReportParam> lineFlowReportParams,Long date,Long date2,Integer lineType){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<LineFlowReportParam> datas = new ArrayList<>();
        Map<Integer,List<String>> assetsMap = new HashMap<>();
        Map<String,MwTangibleassetsTable> dtoMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsTables) {
            Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
            String assetsId = mwTangibleassetsDTO.getAssetsId();
            if(monitorServerId == null || monitorServerId == 0 || StringUtils.isBlank(assetsId)){continue;}
            dtoMap.put(assetsId,mwTangibleassetsDTO);
            if(assetsMap.isEmpty() || assetsMap.get(monitorServerId) == null){
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
                continue;
            }
            if(!assetsMap.isEmpty() && assetsMap.get(monitorServerId) != null){
                List<String> assetsIds = assetsMap.get(monitorServerId);
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
            }
        }
        log.info("线路流量执行9999"+assetsMap);
        if(assetsMap.isEmpty()){
            return datas;
        }
        for (Map.Entry<Integer, List<String>> entry : assetsMap.entrySet()){
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            log.info("线路流量查询zabbix最新数据信息："+hostIds.size());
            List<List<String>> partition = Lists.partition(hostIds, hisToryGroup);
            List<ItemGetDTO> itemGetDTOS = new ArrayList<>();
            for (List<String> hosts : partition) {
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, hosts);
                if (!result.isFail() && result.getData() != null){
                    itemGetDTOS.addAll(JSONObject.parseArray(result.getData().toString(), ItemGetDTO.class));
                }
            }
            log.info("线路流量查询zabbix最新数据信息结束："+itemGetDTOS.size());
            Map<String,List<String>> hostItems = new HashMap<>();
            //按照houstID+Name分组
            for (ItemGetDTO itemGetDTO : itemGetDTOS) {
                if(hostItems.containsKey(itemGetDTO.getHostid()+","+itemGetDTO.getName()+","+itemGetDTO.getValue_type()+","+itemGetDTO.getUnits()+","+itemGetDTO.getOriginalType())){
                    List<String> list = hostItems.get(itemGetDTO.getHostid() +","+ itemGetDTO.getName()+","+itemGetDTO.getValue_type()+","+itemGetDTO.getUnits()+","+itemGetDTO.getOriginalType());
                    list.add(itemGetDTO.getItemid());
                    hostItems.put(itemGetDTO.getHostid() +","+ itemGetDTO.getName()+","+itemGetDTO.getValue_type()+","+itemGetDTO.getUnits()+","+itemGetDTO.getOriginalType(),list);
                }
                if(!hostItems.containsKey(itemGetDTO.getHostid()+","+itemGetDTO.getName()+","+itemGetDTO.getValue_type()+","+itemGetDTO.getUnits()+","+itemGetDTO.getOriginalType())){
                    List<String> list = new ArrayList<>();
                    list.add(itemGetDTO.getItemid());
                    hostItems.put(itemGetDTO.getHostid() +","+ itemGetDTO.getName()+","+itemGetDTO.getValue_type()+","+itemGetDTO.getUnits()+","+itemGetDTO.getOriginalType(),list);
                }
            }
            Map<String,List<String>> valueMap = new HashMap<>();
            for (String key : hostItems.keySet()) {
                List<String> vs = new ArrayList<>();
                List<String> items = hostItems.get(key);
                String[] value = key.split(",");
                log.info("查询线路流量历史记录"+key+new Date());
                List<List<String>> itemss = Lists.partition(items, hisToryGroup);
                log.info("线路流量查询zabbix历史数据信息："+items.size());
                for (List<String> itemIds : itemss) {
                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemIds, date, date2,Integer.parseInt(value[2]));
                    if (!mwZabbixAPIResult.isFail() && mwZabbixAPIResult != null) {
                        JsonNode jsonNode2 = (JsonNode) mwZabbixAPIResult.getData();
                        if (jsonNode2 != null && jsonNode2.size() > 0) {
                            for (JsonNode node : jsonNode2) {
                                Double v = node.get("value").asDouble();
                                long clock = node.get("clock").asLong();
                                vs.add(v+"_"+clock);
                            }
                        }
                    }
                }
                log.info("查询线路流量历史记录结束"+key+new Date());
                valueMap.put(key,vs);
            }
            //计算数据
            if(valueMap.isEmpty())continue;
            for (String key : valueMap.keySet()) {
                String[] value = key.split(",");
                List<String> doubles = valueMap.get(key);
                if(CollectionUtils.isEmpty(doubles))continue;
                //计算最大，最小，平均
                BigDecimal max = new BigDecimal(0);
                BigDecimal sum = new BigDecimal(0);
                BigDecimal min = new BigDecimal(0);
                //最大值最小值时间
                String maxValueTime = "";
                String minValueTime = "";
                min = new BigDecimal(Integer.MAX_VALUE);
                for (String valueAndTime : doubles) {
                    String[] split = valueAndTime.split("_");
                    Double aDouble = Double.parseDouble(split[0]);
                    if(max.doubleValue() < aDouble){
                        max = new BigDecimal(aDouble);
                        maxValueTime = format.format(new Date(Long.parseLong(split[1]) * 1000));
                    }
                    if(min.doubleValue() > aDouble){
                        min = new BigDecimal(aDouble);
                        minValueTime = format.format(new Date(Long.parseLong(split[1]) * 1000));
                    }
                    sum = sum.add(new BigDecimal(aDouble));
                }
                BigDecimal divide = sum.divide(new BigDecimal(doubles.size()),BigDecimal.ROUND_HALF_UP);
                //单位全部转换为MBPS
                Map<String, String> maxValues = UnitsUtil.getValueMap(max.toString(),"Mbps",value[3]);
                Map<String, String> dicideValues = UnitsUtil.getValueMap(divide.toString(),"Mbps",value[3]);
                Map<String, String> sumValues = UnitsUtil.getValueMap(sum.toString(),"Gbps",value[3]);
                Map<String, String> minValues = UnitsUtil.getValueMap(min.toString(),"Mbps",value[3]);
                LineFlowReportParam  param = new LineFlowReportParam();
                param.setInterfaceName(value[4].replace("[","").replace("]",""));
                MwTangibleassetsTable tangibleassetsTable = dtoMap.get(value[0]);
                param.setAssetsId(tangibleassetsTable.getId());
                param.setAssetsName(tangibleassetsTable.getAssetsName());
                if(value[1].contains("MW_INTERFACE_IN_TRAFFIC")){
                    param.setAcceptFlowAvg(dicideValues.get("value")+"Mbps");
                    param.setAcceptFlowMax(maxValues.get("value")+"Mbps");
                    param.setAcceptFlowMin(minValues.get("value")+"Mbps");
                    param.setAcceptTotalFlow(sumValues.get("value")+"Gbps");
                    param.setAcceptMaxValueTime(maxValueTime);
                    param.setAcceptMinValueTime(minValueTime);
                }
                if(value[1].contains("MW_INTERFACE_OUT_TRAFFIC")){
                    param.setSendingFlowAvg(dicideValues.get("value")+"Mbps");
                    param.setSendingFlowMax(maxValues.get("value")+"Mbps");
                    param.setSendingFlowMin(minValues.get("value")+"Mbps");
                    param.setSendTotalFlow(sumValues.get("value")+"Gbps");
                    param.setSendMaxValueTime(maxValueTime);
                    param.setSendMinValueTime(minValueTime);
                }
                datas.add(param);
            }
        }
        Map<String,LineFlowReportParam> map = new HashMap<>();
        List<LineFlowReportParam> realData = new ArrayList<>();
        //进行同资产同接口数据合并
        if(!CollectionUtils.isEmpty(datas)){
            for (LineFlowReportParam data : datas) {
                String assetsId = data.getAssetsId();
                String interfaceName = data.getInterfaceName();
                if(map.containsKey(assetsId+interfaceName)){
                    LineFlowReportParam param = map.get(assetsId + interfaceName);
                    String acceptFlowAvg = data.getAcceptFlowAvg();
                    String acceptFlowMax = data.getAcceptFlowMax();
                    if(StringUtils.isNotBlank(acceptFlowAvg) && StringUtils.isNotBlank(acceptFlowMax)){
                        param.setAcceptFlowAvg(acceptFlowAvg);
                        param.setAcceptFlowMax(acceptFlowMax);
                        param.setAcceptFlowMin(data.getAcceptFlowMin());
                        param.setAcceptTotalFlow(data.getAcceptTotalFlow());
                        param.setAcceptMaxValueTime(data.getAcceptMaxValueTime());
                        param.setAcceptMinValueTime(data.getAcceptMinValueTime());
                    }
                    String sendingFlowAvg = data.getSendingFlowAvg();
                    String sendingFlowMax = data.getSendingFlowMax();
                    if(StringUtils.isNotBlank(sendingFlowAvg) && StringUtils.isNotBlank(sendingFlowMax)){
                        param.setSendingFlowAvg(sendingFlowAvg);
                        param.setSendingFlowMax(sendingFlowMax);
                        param.setSendingFlowMin(data.getSendingFlowMin());
                        param.setSendTotalFlow(data.getSendTotalFlow());
                        param.setSendMaxValueTime(data.getSendMaxValueTime());
                        param.setSendMinValueTime(data.getSendMinValueTime());
                    }
                    realData.add(param);
                }else{
                    map.put(assetsId + interfaceName,data);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(realData)){
            for (LineFlowReportParam realDatum : realData) {
                if(StringUtils.isBlank(realDatum.getAcceptMaxValueTime())){
                    realDatum.setAcceptMaxValueTime(format.format(new Date(date*1000)));
                }
                if(StringUtils.isBlank(realDatum.getAcceptMinValueTime())){
                    realDatum.setAcceptMinValueTime(format.format(new Date(date*1000)));
                }
                if(StringUtils.isBlank(realDatum.getSendMaxValueTime())){
                    realDatum.setSendMaxValueTime(format.format(new Date(date*1000)));
                }
                if(StringUtils.isBlank(realDatum.getSendMinValueTime())){
                    realDatum.setSendMinValueTime(format.format(new Date(date*1000)));
                }
            }
        }
        return realData;
    }


    /**
     * 磁盘使用率
     * @param
     * @return
     */
    @Override
    public Reply selectReportDiskUse(TrendParam trendParam) {
        try {
            List<TrendDiskDto> diskTrend = new ArrayList<>();
            //获取资产数据信息
            List<MwTangibleassetsTable> mwTangibleassetsTables = selectList(new QueryTangAssetsParam());
            if(CollectionUtils.isEmpty(mwTangibleassetsTables)){
                PageInfo pageInfo = new PageInfo<>(diskTrend);
                pageInfo.setTotal(diskTrend.size());
                pageInfo.setList(diskTrend);
                return Reply.ok(pageInfo);
            }

            CalculitionTimeCallBack calculitionTimeCallBack = (dateType, chooseTime) -> calculitionTime(dateType, chooseTime);
            DiskUseReportCacheDataFunc diskUseReportCacheDataFunc = (dateType ,param) -> getDiskUseReportCacheData(dateType ,param);
            DiskNewsCallBack diskNewsCallBack = (param) -> getDiskNews(param);
            ReportHandler<PageInfo> reportHandler = new DiskUseHandler(trendParam
                    ,calculitionTimeCallBack
                    ,mwTangibleassetsTables
                    ,redisUtils
                    ,diskUseCacheTime
                    ,diskUseReportCacheDataFunc
                    ,diskNewsCallBack
            );

            PageInfo pageInfo = reportHandler.handle();
            return Reply.ok(pageInfo);
        }catch (Exception e){
            log.error("fail to selectReportDiskUse with d={}, cause:{}", e);
            return Reply.fail("查询磁盘使用率报表失败");
        }
    }


    /**
     * 资产可用性报表查询
     * @param param
     * @return
     */
    @Transactional
    @Override
    public Reply selectReportAssetsUsability(RunTimeQueryParam param,boolean addNew) {
        try {
            Integer pageNumber = param.getPageNumber();
            Integer pageSize = param.getPageSize();
            List<MwAssetsUsabilityParam> paramsAll = new ArrayList<>();

            List<MwTangibleassetsTable> mwTangibleassetsTables = selectList(new QueryTangAssetsParam());
            if(CollectionUtils.isEmpty(mwTangibleassetsTables)){
                PageInfo pageInfo = new PageInfo<>(paramsAll);
                pageInfo.setList(paramsAll);
                return Reply.ok(pageInfo);
            }
            if (param.getDateType()!=DateTypeEnum.TODAY.getType()&&!addNew){
                return Reply.ok(getAssetsUsabilityReportCacheData(param.getDateType(),false,param));
            }
            Integer dateType = param.getDateType();
            List<String> chooseTime = param.getChooseTime();
            List<Long> times = calculitionTime(dateType, chooseTime);
            Long startTime = times.get(0);
            Long endTime = times.get(1);
            List<QueryAssetsAvailableParam> mwTangibleassetsTables1 = new ArrayList<>();

            for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
                if(mwTangibleassetsTable.getMonitorServerId() == null || mwTangibleassetsTable.getMonitorServerId() == 0
                        || StringUtils.isBlank(mwTangibleassetsTable.getAssetsId())){continue;}
                QueryAssetsAvailableParam queryAssetsAvailableParam = new QueryAssetsAvailableParam();
                queryAssetsAvailableParam.setInBandIp(mwTangibleassetsTable.getInBandIp());
                queryAssetsAvailableParam.setAssetsId(mwTangibleassetsTable.getAssetsId());
                queryAssetsAvailableParam.setId(mwTangibleassetsTable.getId());
                queryAssetsAvailableParam.setMonitorServerId(mwTangibleassetsTable.getMonitorServerId());
                mwTangibleassetsTables1.add(queryAssetsAvailableParam);
//                AvailableInfoDTO availableByHostId = getAvailableByHostId(queryAssetsAvailableParam, startTime, endTime);
//                if(availableByHostId == null || availableByHostId.getAvailablePer() == null){
//                    continue;
//                }
                MwAssetsUsabilityParam usabilityParam = new MwAssetsUsabilityParam();
                usabilityParam.setAssetsName(mwTangibleassetsTable.getAssetsName());
                usabilityParam.setIp(mwTangibleassetsTable.getInBandIp());
                usabilityParam.setAssetsId(mwTangibleassetsTable.getId());
//                usabilityParam.setAssetsUsability(availableByHostId.getAvailablePer());
                paramsAll.add(usabilityParam);
            }
            Map<String,AvailableInfoDTO> list =new HashMap<>();
            RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
            Map<String,AvailableInfoDTO> listset = (Map<String, AvailableInfoDTO>) redisUtils.get("Report:asstetsAble");
            if (listset!=null&&!addNew){
                try{
                    if (listset.keySet().size()>0){
                        list.putAll(listset);
                        redisUtils.set("Report:asstetsAble",list,600);
                    }
                }catch (Exception e){
                    Map<String,AvailableInfoDTO> listone = getIPHost(mwTangibleassetsTables1, startTime, endTime);
                    list.putAll(listone);
                    redisUtils.set("Report:asstetsAble",list,600);
                }

            }else {
                Map<String,AvailableInfoDTO> listone = getIPHost(mwTangibleassetsTables1, startTime, endTime);
                list.putAll(listone);
                if (!addNew){
                    redisUtils.set("Report:asstetsAble",list,600);
                }
            }


            List<MwAssetsUsabilityParam> params = new ArrayList<>();
            for (MwTangibleassetsTable assetsUsabilityParam:mwTangibleassetsTables) {
                if(list.get(assetsUsabilityParam.getAssetsId())!=null){
                    MwAssetsUsabilityParam usabilityParam = new MwAssetsUsabilityParam();
                    usabilityParam.setAssetsName(assetsUsabilityParam.getAssetsName());
                    usabilityParam.setIp(assetsUsabilityParam.getInBandIp());
                    usabilityParam.setAssetsId(assetsUsabilityParam.getId());
                    usabilityParam.setAssetsUsability(list.get(assetsUsabilityParam.getAssetsId()).getAvailablePer());
                    params.add(usabilityParam);
                }
            }


            //根据资产过滤数据
            if(!CollectionUtils.isEmpty(param.getIds()) && !CollectionUtils.isEmpty(params)){
                List<String> ids = param.getIds();
                Iterator<MwAssetsUsabilityParam> iterator = params.iterator();
                while(iterator.hasNext()){
                    MwAssetsUsabilityParam next = iterator.next();
                    if(!ids.contains(next.getAssetsId())){
                        iterator.remove();
                    }
                }

            }
            try{
                //排序
                List<MwAssetsUsabilityParam> dtos = null;
                if(params != null && params.size() > 0){
                    Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                    Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                    dtos = params.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getAssetsUsability()), pinyin4jUtil.getStringPinYin(o2.getAssetsUsability()))).collect(Collectors.toList());
                }
                String dateRegion = getDateRegion(param.getDateType(), param.getChooseTime());
                if(!CollectionUtils.isEmpty(dtos) && StringUtils.isNotBlank(dateRegion)){
                    dtos.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                int fromIndex = pageSize * (pageNumber -1);
                int toIndex = pageSize * pageNumber;
                if(toIndex > dtos.size()){
                    toIndex = dtos.size();
                }
//                List<MwAssetsUsabilityParam> usabilityParams = dtos.subList(fromIndex, toIndex);
                PageList pageList = new PageList();
//                List<MwAssetsUsabilityParam> set = pageList.getList(usabilityParams, param.getPageNumber(), param.getPageSize());
                int pagenaum = (int)Math.ceil((double) params.size() /(double)  param.getPageSize());
                List<Integer> pahe = new ArrayList<>();
                int [] pageNum = new int[pagenaum];
                for (int i =0; i <pagenaum; i++) {
                    pageNum[i] = i +1;
                }

                PageHelper.startPage(pageNumber, pageSize);
                PageInfo pageInfo = new PageInfo<>(  pageList.getList(params, pageNumber, pageSize));
                pageInfo.setList(  pageList.getList(params, pageNumber, pageSize));
                pageInfo.setHasNextPage(params.size() > param.getPageNumber() * param.getPageSize());
                pageInfo.setNavigatepageNums(pageNum);
                pageInfo.setIsLastPage(params.size() <= param.getPageNumber() * param.getPageSize());
                pageInfo.setTotal(params.size());
                pageInfo.setSize(params.size());
                return Reply.ok(pageInfo);
            }catch (Exception e){
                return  Reply.ok(null);
            }

        }catch (Exception e){
            log.error("fail to selectReportAssetsUsability with d={}, cause:{}", e.getMessage());
            return Reply.fail("查询资产可用性报表失败");
        }

    }

    private  Map<String,AvailableInfoDTO>  getIPHost(List<QueryAssetsAvailableParam> mwTangibleassetsTables, Long startTime, Long endTime) {
        Map<String,List<QueryAssetsAvailableParam>> paramMap = new HashMap<>();
//        Map<Integer,Map<Integer,List<String>>> fenzu = new HashMap<>();
        Map<String,List<String>> map = new HashMap<>();
//        Map<String,List<QueryAssetsAvailableParam>> stringListMap = new HashMap<>();
        //map全局参数
        Map<Integer,Map<Integer,List<String>>> fenzu = mwServerManager.getListitemsByassetId(mwTangibleassetsTables,map);
        Date countTime = new Date();
//        for (QueryAssetsAvailableParam q:mwTangibleassetsTables) {
//            QueryAssetsAvailableParam newParam = new QueryAssetsAvailableParam();
//            try {
//                newParam = mwServerManager.getItemIdByAvailableItem(q);
//            }catch (Exception e){
//                continue;
//            }
//
//            if (map.get(newParam.getAssetsId())!=null){
//                List<String> k = map.get(newParam.getAssetsId());
//                k.add(newParam.getItemId());
//                map.put(newParam.getAssetsId(),k);
//            }
//            else {
//                List<String> k = new ArrayList<>();
//                if (newParam.getItemId()!=null&&newParam.getItemId().trim()!="") {
//                    k.add(newParam.getItemId());
//                    map.put(newParam.getAssetsId(), k);
//                }
//            }
//            Map<Integer, List<String>> k = fenzu.get(newParam.getMonitorServerId());
//
//            if (k != null) {
//                List<String> strings = fenzu.get(newParam.getMonitorServerId()).get(newParam.getValue_type());
//                if (strings!=null) {
//                    strings.add(newParam.getItemId());
//                    fenzu.get(newParam.getMonitorServerId()).put(newParam.getValue_type(), strings);
//                } else {
//                    List<String> stringList = new ArrayList<>();
//                    if (newParam.getItemId()!=null&&newParam.getItemId().trim()!="") {
//                        stringList.add(newParam.getItemId());
//                        fenzu.get(newParam.getMonitorServerId()).put(newParam.getValue_type(), stringList);
//                    }
//                }
//            } else {
//                Map<Integer, List<String>> listMap = new HashMap<>();
//                List<String> strings = new ArrayList<>();
//                strings.add(newParam.getItemId());
//                listMap.put(newParam.getValue_type(), strings);
//                fenzu.put(newParam.getMonitorServerId(), listMap);
//            }
//        }
        fenzu.remove(0);
        Long s = (countTime.getTime() -new Date().getTime())/1000;
//        mwMessageService.createMessage("组装数据结束，耗时"+s.toString(),1,0);

        Map<String, List<MWItemHistoryDto>> reslut = new HashMap<>();
        Map<String,List<MWItemHistoryDto>> hashMap = new HashMap<>();
        //分类求数据据
        for (Integer i :fenzu.keySet()){
            for (Integer j:fenzu.get(i).keySet()) {
                List<String> k = fenzu.get(i).get(j);
                Map<Integer,List<String>> fenK = getFenK(k,hisToryGroup);
                for (Integer v:fenK.keySet()) {
                    List<String> x = fenK.get(v);
                    List<MWItemHistoryDto> MWItemHistoryDto = zabbixManger.HistoryGetByTimeAndHistoryListByitem(i, x, startTime, endTime, j);

                    for (MWItemHistoryDto a: MWItemHistoryDto) {
                        if (hashMap.get(a.getItemid())==null){
                            List<MWItemHistoryDto> newMWItemHistoryDto = new ArrayList<>();
                            newMWItemHistoryDto.add(a);
                            hashMap.put(a.getItemid(),newMWItemHistoryDto);
                        }else{
                            List<MWItemHistoryDto> newMWItemHistoryDto = hashMap.get(a.getItemid());
                            newMWItemHistoryDto.add(a);
                            hashMap.put(a.getItemid(),newMWItemHistoryDto);
                        }
                    }
                }
            }
        }

        for (String l :map.keySet()){
            if(reslut.get(l)==null){
                List<String> iterds =   map.get(l);
                List<MWItemHistoryDto> newMWItemHistoryDto = new ArrayList<>();
                try{
                    for (int m = 0; m < iterds.size(); m++) {
                        newMWItemHistoryDto.addAll(hashMap.get(iterds.get(m)));
                    }
                    reslut.put(l,newMWItemHistoryDto);
                }catch (Exception e){
                    log.info("当前报错报表"+iterds.toString()+"hashMap:"+hashMap.toString());
                }
            }
        }


        //计算值
        Map<String,AvailableInfoDTO> res  = new HashMap<>();
        for (String l : reslut.keySet()) {
            AvailableInfoDTO infoDTO = new AvailableInfoDTO();
            //获取数据信息
            List<MwHistoryDTO> historyDTOList = new ArrayList<>();
            List<AssetsAvailableDTO> colorData = new ArrayList<>();
            AssetsAvailableDTO availableDTO;
            Long lastValue = -2L;
            int count = 0;
            Double sum = 0.0;
            int startIndex = 0;

            List<MWItemHistoryDto> lastHisDTO = new ArrayList<>();
            List<MWItemHistoryDto> historyDtos = reslut.get(l);


            if (historyDtos != null && historyDtos.size() > 0) {
                int floor = 0;
                Long time_start = Long.valueOf(historyDtos.get(0).getClock());
                // 查看当前时间是否是最开始的时间，如果不是，这段时间为未管理的阶段
                if ((startTime + 60 * 60) < time_start) {
                    floor = (int) Math.floor((time_start - startTime) / 60);

                    MWItemHistoryDto itemHistoryDto = new MWItemHistoryDto();
                    itemHistoryDto.setLastValue(-1L);
                    itemHistoryDto.setClock(startTime.toString());

                    lastHisDTO.add(itemHistoryDto);

                    lastHisDTO.addAll(Arrays.asList(new MWItemHistoryDto[floor]));
                    lastHisDTO.addAll(historyDtos);
                } else {
                    lastHisDTO.addAll(historyDtos);
                }

                for (int i = 0; i < lastHisDTO.size(); i++) {
                    historyDTOList.add(MwHistoryDTO.builder()
                            .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(lastHisDTO.get(i).getLastValue()))
                            .value("8")
                            .dateTime(new Date(Long.valueOf(lastHisDTO.get(i).getClock()) * 1000L)).build());
                    if (lastHisDTO.get(i).getLastValue() == 1L || lastHisDTO.get(i).getLastValue() == 2L) {
                        sum++;//为了计算可用率
                    }
                    if (lastValue != lastHisDTO.get(i).getLastValue()) {
                        if ((count - 1) == 0) {
                            availableDTO = new AssetsAvailableDTO();
                            availableDTO.setGt(startIndex);
                            availableDTO.setLte(i);
                            availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                            colorData.add(availableDTO);
                            count--;
                        }
                        lastValue = lastHisDTO.get(i).getLastValue();
                        startIndex = i;
                        count++;
                    }
                    if (i == lastHisDTO.size() - 1) {//最后收尾的值
                        availableDTO = new AssetsAvailableDTO();
                        availableDTO.setGt(startIndex);
                        availableDTO.setLte(i);
                        availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                        colorData.add(availableDTO);
                    }
                    if (i == 0) {
                        i = floor;
                        for (int j = 0; j < floor; j++) {
                            startTime = startTime + 60;
                            historyDTOList.add(MwHistoryDTO.builder()
                                    .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(-1L))
                                    .value("8")
                                    .dateTime(new Date(startTime * 1000L)).build());
                        }
                    }
                }
                int size = historyDtos.size();
                String per = new BigDecimal(sum * 100 / size).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%";
                infoDTO.setAvailablePer(per);
                infoDTO.setColorData(colorData);
                infoDTO.setHistoryDTOList(historyDTOList);
                res.put(l,infoDTO);
            } else if (mwTangibleassetsTables.get(0).getDateType() != 3) {
                res.put(l,null);
            }
        }

        return res;
    }

    private  Map<Integer, List<String>> getFenK(List<String> k, int size) {
        Map<Integer, List<String>> fen = new HashMap<>();
        if (k.size()<size){
            fen.put(0,k);
        }else {
            Integer kill = k.size()/size;
            Integer yushu = k.size()%size;
            for (int i = 0; i < kill; i++) {
                fen.put(i+1,k.subList((i)*size,(i+1)*size));
            }
            if(yushu>0){
                fen.put(kill+1,k.subList(kill*size,kill*size+yushu));
            }
        }
        return fen;
    }

    @Transactional
    public AvailableInfoDTO getAvailableByHostId(QueryAssetsAvailableParam param,  Long startTime,Long endTime) {
        try {
            //先获取可用性监控项id信息
            QueryAssetsAvailableParam newParam = mwServerManager.getItemIdByAvailableItem(param);
            if (newParam.getItemId() != null && StringUtils.isNotEmpty(newParam.getItemId())) {
                AvailableInfoDTO infoDTO = new AvailableInfoDTO();
                //获取数据信息
                List<MwHistoryDTO> historyDTOList = new ArrayList<>();
                List<AssetsAvailableDTO> colorData = new ArrayList<>();
                AssetsAvailableDTO availableDTO;
                Long lastValue = -2L;
                int count = 0;
                Double sum = 0.0;
                int startIndex = 0;

                List<MWItemHistoryDto> lastHisDTO = new ArrayList<>();
                List<MWItemHistoryDto> historyDtos = zabbixManger.HistoryGetByTimeAndHistory(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());


                if (historyDtos != null && historyDtos.size() > 0) {
                    int floor = 0;
                    Long time_start = Long.valueOf(historyDtos.get(0).getClock());
                    // 查看当前时间是否是最开始的时间，如果不是，这段时间为未管理的阶段
                    if ((startTime + 60 * 60) < time_start) {
                        floor = (int) Math.floor((time_start - startTime) / 60);

                        MWItemHistoryDto itemHistoryDto = new MWItemHistoryDto();
                        itemHistoryDto.setLastValue(-1L);
                        itemHistoryDto.setClock(startTime.toString());

                        lastHisDTO.add(itemHistoryDto);

                        lastHisDTO.addAll(Arrays.asList(new MWItemHistoryDto[floor]));
                        lastHisDTO.addAll(historyDtos);
                    } else {
                        lastHisDTO.addAll(historyDtos);
                    }

                    for (int i = 0; i < lastHisDTO.size(); i++) {
                        historyDTOList.add(MwHistoryDTO.builder()
                                .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(lastHisDTO.get(i).getLastValue()))
                                .value("8")
                                .dateTime(new Date(Long.valueOf(lastHisDTO.get(i).getClock()) * 1000L)).build());
                        if (lastHisDTO.get(i).getLastValue() == 1L || lastHisDTO.get(i).getLastValue() == 2L) {
                            sum++;//为了计算可用率
                        }
                        if (lastValue != lastHisDTO.get(i).getLastValue()) {
                            if ((count - 1) == 0) {
                                availableDTO = new AssetsAvailableDTO();
                                availableDTO.setGt(startIndex);
                                availableDTO.setLte(i);
                                availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                                colorData.add(availableDTO);
                                count--;
                            }
                            lastValue = lastHisDTO.get(i).getLastValue();
                            startIndex = i;
                            count++;
                        }
                        if (i == lastHisDTO.size() - 1) {//最后收尾的值
                            availableDTO = new AssetsAvailableDTO();
                            availableDTO.setGt(startIndex);
                            availableDTO.setLte(i);
                            availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                            colorData.add(availableDTO);
                        }
                        if (i == 0) {
                            i = floor;
                            for (int j = 0; j < floor; j++) {
                                startTime = startTime + 60;
                                historyDTOList.add(MwHistoryDTO.builder()
                                        .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(-1L))
                                        .value("8")
                                        .dateTime(new Date(startTime * 1000L)).build());
                            }
                        }
                    }
                    int size = historyDtos.size();
                    String per = new BigDecimal(sum * 100 / size).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%";
                    infoDTO.setAvailablePer(per);
                    infoDTO.setColorData(colorData);
                    infoDTO.setHistoryDTOList(historyDTOList);
                } else if (param.getDateType() != 3) {
                    return null;
                }
                log.info("success to getAvailableByHostId result:{}", infoDTO);
                return infoDTO;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("fail to getAvailableByHostId errorInfo:{}", e);
            return null;
        }
    }


    /**
     * 资产信息报表导出
     * @param assets
     * @param response
     */
    @Transactional
    @Override
    public void assetsNewsReportExport(List<AssetsNewsReportExportParam> assets, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("资产信息", response, AssetsNewsReportExportParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("orgName");
            includeColumnFiledNames.add("assetsTypeName");
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("inBandIp");
            includeColumnFiledNames.add("manufacturer");
            includeColumnFiledNames.add("specifications");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(assets, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 导出CPU信息报表
     * @param response
     */
    @Transactional
    @Override
    public void cpuNewsReportExport(List<CpuNewsReportExportParam> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("CPU信息", response, CpuNewsReportExportParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetName");
            includeColumnFiledNames.add("ip");
            includeColumnFiledNames.add("diskUserRate");
            includeColumnFiledNames.add("diskUser");
            includeColumnFiledNames.add("diskTotal");
            includeColumnFiledNames.add("maxValue");
            includeColumnFiledNames.add("avgValue");
            includeColumnFiledNames.add("minValue");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(params, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Transactional
    @Override
    public void diskUseReportExport(List<DiskUseReportExportParam> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("磁盘使用率", response, DiskUseReportExportParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("typeName");
            includeColumnFiledNames.add("diskTotal");
            includeColumnFiledNames.add("diskAvgValue");
            includeColumnFiledNames.add("diskUse");
            includeColumnFiledNames.add("diskFree");
            includeColumnFiledNames.add("diskUsable");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(params, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Transactional
    @Override
    public void diskUseAbleReportExport(List<DiskUseAbleReportExportParam> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("磁盘可用率", response, DiskUseAbleReportExportParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("typeName");
            includeColumnFiledNames.add("diskTotal");
            includeColumnFiledNames.add("diskUsable");
            includeColumnFiledNames.add("diskFree");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(params, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Transactional
    @Override
    public void assetsUsabilityReportExport(List<MwAssetsUsabilityParam> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("资产可用性", response, MwAssetsUsabilityParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ip");
            includeColumnFiledNames.add("assetsUsability");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(params, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Transactional
    @Override
    public void lineFlowReportExport(List<LineFlowReportParam> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("线路流量", response, LineFlowReportParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("time");
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("interfaceName");
            includeColumnFiledNames.add("acceptFlowMax");
            includeColumnFiledNames.add("acceptFlowAvg");
            includeColumnFiledNames.add("sendingFlowMax");
            includeColumnFiledNames.add("sendingFlowAvg");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(params, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 查询资产树状结构数据
     * @return
     */
    @Transactional
    @Override
    public Reply selectAssetsReportTree(TrendParam tparam) {
        if(modelAssetEnable){return Reply.ok(getNewAssetsTreeInfo());}
        log.info("查询资产树状结构数据"+new Date());
        Integer[] arr = {1,2,3,6};
        Map<String,List<AssetsTreeDTO>> treeMap = new HashMap<>();
        String key = "assetTree";
        for (Integer integer : arr) {
            log.info("查询资产树状结构数据类型为"+integer+"的数据开始"+new Date());
            QueryAssetsTypeParam param = new QueryAssetsTypeParam();
            param.setTableType(1);
            param.setAssetsTypeId(integer);
            Reply reply = tangibleAssetsService.getAssetsTypesTree(param);
            log.info("查询资产树状结构数据类型为"+integer+"的数据结束"+new Date());
            if(reply == null){
                continue;
            }
            List<AssetsTreeDTO> treeDTOS = (List<AssetsTreeDTO>) reply.getData();
            //删除类型为未知的数据
            if(!CollectionUtils.isEmpty(treeDTOS)){
                Iterator<AssetsTreeDTO> iterator = treeDTOS.iterator();
                while(iterator.hasNext()){
                    AssetsTreeDTO next = iterator.next();
                    if("未知".equals(next.getTypeName()) || "虚拟化".equals(next.getTypeName())){
                        iterator.remove();
                    }
                }
            }
            //将最终数据存入集合
            if(integer == 1){
                treeMap.put("品牌",treeDTOS);
            }
            if(integer == 2){
                treeMap.put("资产类型",treeDTOS);
            }
            if(integer == 3){
                treeMap.put("标签",treeDTOS);
            }
            if(integer == 6){
                treeMap.put("机构",treeDTOS);
            }
        }
        if(tparam.getReportType() == 1){
            List<AssetsTreeDTO> assetsTreeDTOList = selectAseestsInterface();
            //按照资产名称排序
            Collections.sort(assetsTreeDTOList, new Comparator<AssetsTreeDTO>() {
                @Override
                public int compare(AssetsTreeDTO o1, AssetsTreeDTO o2) {
                    return o1.getTypeName().compareTo(o2.getTypeName());
                }
            });
            treeMap.put("资产",assetsTreeDTOList);
        }
        return Reply.ok(treeMap);
    }

    /**
     * 设置导出信息
     * @param name
     * @param response
     * @param dtoclass
     * @return
     * @throws IOException
     */
    private ExcelWriter exportReportSetNews(String name,HttpServletResponse response,Class dtoclass) throws IOException {
        String fileName = System.currentTimeMillis()+""; //导出文件名
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 11);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        //创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), dtoclass).registerWriteHandler(horizontalCellStyleStrategy).build();
        return excelWriter;
    }


    private List<TrendDiskDto> getDiskNews(TrendParam trendParam){
        List<TrendDiskDto> diskDtos = new ArrayList<>();
        if (null != trendParam.getChooseTime() && trendParam.getChooseTime().size() > 0){
            List<String> chooseTime = trendParam.getChooseTime();
            Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
            Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
            //根据资产服务ID分组
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = trendParam.getMwTangibleassetsDTOS();
            Map<Integer, List<String>> groupMap = mwTangibleassetsDTOS.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            mwTangibleassetsDTOS.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            Map<String,TrendDiskDto> diskDtoMap = new HashMap<>();
            for (Integer serverId : groupMap.keySet()) {
                List<String> hostIds = groupMap.get(serverId);
                //查询监控项数据
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, ReportConstant.DISK_ITEMS, hostIds);
                if(result == null || result.isFail()){continue;}
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                if(CollectionUtils.isEmpty(itemApplications)){continue;}
                Map<String,ItemApplication> itemApplicationMap = new HashMap<>();
                itemApplications.forEach(item->{
                    itemApplicationMap.put(item.getItemid(),item);
                });
                List<String> itemIds = itemApplications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
                Map<String, List<ItemApplication>> typeMap = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getValue_type()));
                Map<String, List<HistoryValueDto>> zabbixTrendInfo = getZabbixTrendInfo(serverId, itemIds, startTime, endTime);
                if(zabbixTrendInfo == null){
                    zabbixTrendInfo = new HashMap<>();
                    log.info("磁盘报表查询历史记录");
                    //查询历史数据
                    for (String vaueType : typeMap.keySet()) {
                        MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemIds, startTime, endTime, Integer.parseInt(vaueType));
                        if(historyRsult == null || historyRsult.isFail()){continue;}
                        Map<String, List<HistoryValueDto>> dataMap = ReportUtil.getValueDataMap(historyRsult);
                        zabbixTrendInfo.putAll(dataMap);
                    }
                }
                log.info("磁盘报表查询数据"+zabbixTrendInfo);
                handleDiskHisToryInfo(zabbixTrendInfo,itemApplicationMap,assetsMap,diskDtoMap,serverId);
            }
            log.info("磁盘报表数据处理"+diskDtoMap);
            //取出Map数据
            if(diskDtoMap != null && !diskDtoMap.isEmpty()){
                for (String key : diskDtoMap.keySet()) {
                    TrendDiskDto diskDto = diskDtoMap.get(key);
                    if(diskDto == null){continue;}
                    diskDtos.add(diskDto);
                }
            }
        }
        log.info("磁盘报表返回数据"+diskDtos);
        return diskDtos;
    }

    private void handleDiskHisToryInfo(Map<String, List<HistoryValueDto>> zabbixTrendInfo,Map<String,ItemApplication> itemApplicationMap,
                                       Map<String,MwTangibleassetsTable> assetsMap,Map<String,TrendDiskDto> diskDtoMap,Integer serverId){
        if(zabbixTrendInfo == null || zabbixTrendInfo.isEmpty()){return;}
        for (String itemId : zabbixTrendInfo.keySet()) {
            List<HistoryValueDto> historyValueDtos = zabbixTrendInfo.get(itemId);
            ItemApplication application = itemApplicationMap.get(itemId);
            MwTangibleassetsTable tangibleassetsTable = assetsMap.get(serverId+application.getHostid());
            String name = application.getName();
            String typeName = "";
            if(name.contains("]")){
                typeName = name.substring(name.indexOf("[")+1,name.indexOf("]"));
            }
            TrendDiskDto diskDto = diskDtoMap.get(tangibleassetsTable.getAssetsId() + typeName);
            if(diskDto == null){
                diskDto = new TrendDiskDto();
            }
            //求集合的平均值
            double value = historyValueDtos.stream().filter(item -> item.getValue() != null).mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
            DiskInfoManage diskInfoManage = new DiskInfoManage();
            if (diskInfoManage.matchDiskUtilization(name)) {
                diskDto.setDiskAvgValue(new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString()+application.getUnits());
                diskDto.setDiskUsable(new BigDecimal(100-value).setScale(2,BigDecimal.ROUND_HALF_UP).toString()+application.getUnits());
            }
            if (diskInfoManage.matchDiskFree(name)) {
                //单位转换
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), application.getUnits());
                diskDto.setDiskFree(convertedValue.get("value")+convertedValue.get("units"));
            }
            if (diskInfoManage.matchDiskTotal(name)) {
                //单位转换
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), application.getUnits());
                diskDto.setDiskTotal(convertedValue.get("value")+convertedValue.get("units"));
            }
            if (diskInfoManage.matchDiskUsed(name)) {
                //单位转换
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), application.getUnits());
                diskDto.setDiskUse(convertedValue.get("value")+convertedValue.get("units"));
            }
            diskDto.setAssetsName(tangibleassetsTable.getAssetsName());
            diskDto.setTypeName(typeName);
            diskDto.setIpAddress(tangibleassetsTable.getInBandIp());
            diskDto.setAssetsId(tangibleassetsTable.getId()==null?String.valueOf(tangibleassetsTable.getModelInstanceId()):tangibleassetsTable.getId());
            diskDtoMap.put(tangibleassetsTable.getAssetsId()+typeName,diskDto);
        }
    }



    private List<TrendDiskDto> getDiskNews2(TrendParam trendParam){
        log.info("开始查询磁盘信息"+new Date());
        List<TrendDiskDto> dtos = new ArrayList<>();
        log.info("开始将资产信息分组"+new Date());
        if (null != trendParam.getChooseTime() && trendParam.getChooseTime().size() > 0) {
            List<String> chooseTime = trendParam.getChooseTime();
            Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
            Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
            //根据资产服务ID分组
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = trendParam.getMwTangibleassetsDTOS();
            Map<Integer,List<String>> assetsMap = new HashMap<>();
            Map<String,MwTangibleassetsTable> dtoMap = new HashMap<>();
            for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
                String assetsId = mwTangibleassetsDTO.getAssetsId();
                dtoMap.put(assetsId,mwTangibleassetsDTO);
                List<String> assetsIds = assetsMap.get(monitorServerId);
                if(null == assetsIds){
                    assetsIds = new ArrayList<>();
                    assetsMap.put(monitorServerId,assetsIds);
                }
                assetsIds.add(assetsId);
            }
            if(assetsMap.isEmpty()){
                return dtos;
            }
            log.info("资产信息分组完成"+new Date());
            log.info("开始查询资产磁盘信息diskHostGroupSize:{}:time:{}",diskHostGroupSize, new Date());
            Pattern typePattern = Pattern.compile("^\\[.+\\]");

            for (Map.Entry<Integer, List<String>> integerListEntry : assetsMap.entrySet()) {
                Integer monitorServerId = integerListEntry.getKey();
                List<String> assetsIds = integerListEntry.getValue();


                //避免查询数据量太大,先按个数分组
                List<List<String>> idGroups = new ArrayList<>();
                List<String> idGroup = null;
                for(int i=0;i< assetsIds.size(); i++){
                    if(i % diskHostGroupSize == 0){
                        idGroup = new ArrayList<>();
                        idGroups.add(idGroup);
                    }
                    idGroup.add(assetsIds.get(i));
                }
                int index = 0;

                //并不是所有hostid都有磁盘信息,需要过滤出有效的hostid
                Set<String> activeIds = new HashSet<>();
                Set<String> types = new HashSet<>();
                for(List<String> group : idGroups) {
                    log.info("server:{};group index:{}", monitorServerId, index);
                    index++;
                    MWZabbixAPIResult result = mwtpServerAPI.itemgetbyhostid(monitorServerId, group, "DISK_INFO", true);
                    if (result.getCode() == 0) {
                        JsonNode resultData = (JsonNode) result.getData();

                        if (resultData.size() > 0) {
                            for (JsonNode resultDatum : resultData) {
                                String name = resultDatum.get("name").asText();
                                if (StringUtils.isBlank(name) || name.length() < 1 || name.indexOf("]") < 1 || !name.contains("DISK")) {
                                    continue;
                                }

                                Matcher matcher = typePattern.matcher(name);
                                if(matcher.find()){
                                    String type = matcher.group(0) + "MW_";
                                    types.add(type);
                                    activeIds.add(resultDatum.get("hostid").asText());
                                }
                            }
                        }
                    }
                }

                //遍历有效的hostid,获取磁盘信息
                List<List<String>> activeIdGroups = new ArrayList<>();
                List<String> activeIdGroup = null;
                List<String> activeIdList = new ArrayList<>(activeIds);
                for(int i=0;i< activeIdList.size(); i++){
                    if(i % diskHostGroupSize == 0){
                        activeIdGroup = new ArrayList<>();
                        activeIdGroups.add(activeIdGroup);
                    }
                    activeIdGroup.add(activeIdList.get(i));
                }
                List<String> names = new ArrayList<>(types);
                log.info("磁盘使用情况报表查询names"+names);
                for(List<String> group : activeIdGroups) {
                    List<TrendDiskDto> ret = getDiskData(names, monitorServerId, group, dtoMap, startTime, endTime);
                    dtos.addAll(ret);
                }
            }
        }
        log.info("返回数据"+new Date());
        return dtos;
    }

    /**
     * 山鹰MPLS报告接口
     * @param param
     * @return
     */
    @Transactional
    @Override
    public Reply selectReportLineMpls(MwLineMplsParam param) {
        try {
            if(CollectionUtils.isEmpty(param.getLineName())){
                List<MwLinkMplsReportDto> dtos = new ArrayList<>();
                return Reply.ok(dtos);
            }
            List<String> chooseTime = param.getChooseTime();
            Integer dateType = param.getDateType();
            List<Long> times = calculitionTime(dateType, chooseTime);
            Long startTime = times.get(0);
            Long endTime = times.get(1);
            LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
            List<String> lineNames = param.getLineName();
            log.info("开始查询线路基础数据"+new Date());
            List<NetWorkLinkDto> netWorkLinkDtos = workLinkService.seleAllLink();;
            if(!CollectionUtils.isEmpty(netWorkLinkDtos) && !CollectionUtils.isEmpty(lineNames)){
                Iterator<NetWorkLinkDto> iterator = netWorkLinkDtos.iterator();
                while(iterator.hasNext()){
                    NetWorkLinkDto next = iterator.next();
                    if(!lineNames.contains(next.getLinkName())){
                        iterator.remove();
                    }
                }
            }
            log.info("结束查询线路基础数据"+new Date());
            Map<Integer, List<String>> hostIdAndServerId = getHostIdAndServerId(netWorkLinkDtos);
            log.info("开始查询线路zabbix数据"+new Date());
            Map<String, List<String>> hostAndValueMap = getZabbixLinkPingData(hostIdAndServerId, startTime, endTime);
            log.info("结束查询线路zabbix数据"+new Date());
            log.info("开始计算线路MPLS数据"+new Date());
            List<MwMplsReportDto> mwMplsReportDtos = reckonMplsReportNetWorkUse(netWorkLinkDtos,hostAndValueMap);
            log.info("结束计算线路MPLS数据"+new Date());
            //判断是否根据线路名称过滤
            if(!CollectionUtils.isEmpty(param.getLineName()) && !CollectionUtils.isEmpty(mwMplsReportDtos)){
                Iterator<MwMplsReportDto> iterator = mwMplsReportDtos.iterator();
                while(iterator.hasNext()){
                    String lineName = iterator.next().getLineName();
                    if(StringUtils.isNotBlank(lineName) && !param.getLineName().contains(lineName)){
                        iterator.remove();
                    }
                }
            }
            if(param.getType() == 1){
                return Reply.ok(mwMplsReportDtos);
            }
            List<MwLinkMplsReportDto> mwLinkMplsReportDtos = new ArrayList<>();
            List<Map<String,String>> data = new ArrayList<>();
            if(!CollectionUtils.isEmpty(mwMplsReportDtos)){
                for (MwMplsReportDto dto : mwMplsReportDtos) {
                    Map<String,String> map = new HashMap<>();
                    String lineName = dto.getLineName();
                    String averageAvailability = dto.getAverageAvailability();
                    map.put("name",lineName);
                    map.put("value",averageAvailability);
                    data.add(map);
                }
            }
            MwLinkMplsReportDto dto = new MwLinkMplsReportDto();
            dto.setTitleName("可用性");
            dto.setUnit("%");
            dto.setData(data);
            Date date = new Date();
            date.setTime(startTime*1000);
            Date date2 = new Date();
            date2.setTime(endTime*1000);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dto.setDateRegion(format.format(date)+"~"+format.format(date2));
            mwLinkMplsReportDtos.add(dto);
            return Reply.ok(mwLinkMplsReportDtos);
        }catch (Exception e){
            log.error("查询线路网络可用率数据失败"+e.getMessage());
            return Reply.fail("查询线路网络可用率数据失败");
        }
    }




    /**
     * 获取hostID与ServerID查询zabbix
     */
    private Map<Integer,List<String>> getHostIdAndServerId(List<NetWorkLinkDto> netWorkLinkDtos){
        log.info("开始进行线路数据服务ID分组"+new Date());
        Map<Integer,List<String>> serverMap = new HashMap<>();
        if(CollectionUtils.isEmpty(netWorkLinkDtos)){
            return serverMap;
        }
        for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
            //原设备资产id
            String assetsId = netWorkLinkDto.getRootAssetsParam().getAssetsId();
            //原设备服务ID
            Integer monitorServerId = netWorkLinkDto.getRootAssetsParam().getMonitorServerId();
            //目标设备资产ID
            String targerAssetsId = netWorkLinkDto.getTargetAssetsParam().getAssetsId();
            //目标设备服务ID
            Integer targerMonitorServerId = netWorkLinkDto.getTargetAssetsParam().getMonitorServerId();
            if(serverMap.isEmpty() || serverMap.get(monitorServerId) == null){
                if(StringUtils.isNotBlank(assetsId)){
                    List<String> assetss = new ArrayList<>();
                    assetss.add(assetsId);
                    serverMap.put(monitorServerId,assetss);
                }
                if(serverMap.isEmpty() || serverMap.get(targerMonitorServerId) == null){
                    if(StringUtils.isNotBlank(targerAssetsId)){
                        List<String> targerAssetss = new ArrayList<>();
                        targerAssetss.add(targerAssetsId);
                        serverMap.put(targerMonitorServerId,targerAssetss);
                        continue;
                    }
                }
                if(!serverMap.isEmpty() && serverMap.get(targerMonitorServerId) != null){
                    if(StringUtils.isNotBlank(targerAssetsId)){
                        List<String> targerAssetss = serverMap.get(targerMonitorServerId);
                        targerAssetss.add(targerAssetsId);
                        serverMap.put(targerMonitorServerId,targerAssetss);
                        continue;
                    }
                }
            }
            if(!serverMap.isEmpty() && serverMap.get(monitorServerId) != null){
                if(StringUtils.isNotBlank(assetsId)){
                    List<String> assetss = serverMap.get(monitorServerId);
                    assetss.add(assetsId);
                    serverMap.put(monitorServerId,assetss);
                }
                if(serverMap.isEmpty() || serverMap.get(targerMonitorServerId) == null){
                    if(StringUtils.isNotBlank(targerAssetsId)){
                        List<String> targerAssetss = new ArrayList<>();
                        targerAssetss.add(targerAssetsId);
                        serverMap.put(targerMonitorServerId,targerAssetss);
                        continue;
                    }
                }
                if(!serverMap.isEmpty() && serverMap.get(targerMonitorServerId) != null){
                    if(StringUtils.isNotBlank(targerAssetsId)){
                        List<String> targerAssetss = serverMap.get(targerMonitorServerId);
                        targerAssetss.add(targerAssetsId);
                        serverMap.put(targerMonitorServerId,targerAssetss);
                    }
                }
            }
        }
        log.info("结束进行线路数据服务ID分组"+new Date());
        return serverMap;
    }

    /**
     * 获取zabbix中状态历史数据
     * @param hostIdAndServerId
     */
    private Map<String,List<String>> getZabbixLinkPingData(Map<Integer, List<String>> hostIdAndServerId,Long startTime,Long endTime){
        Map<String,List<String>> valueMap = new HashMap<>();
        log.info("开始查询zabbix上历史状态数据"+new Date());
        if(hostIdAndServerId.isEmpty()){
            return valueMap;
        }
        List<String> itemNames = new ArrayList<>();
        Map<String,List<String>> itemMap = new HashMap<>();
        itemNames.add("ICMP_PING");
        itemNames.add("MW_INTERFACE_STATUS");
        for (Map.Entry<Integer, List<String>> entry : hostIdAndServerId.entrySet()) {
            List<String> itemIds = new ArrayList<>();
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(serverId, itemNames, hostIds);
            if (result.getCode() == 0){
                JsonNode jsonNode = (JsonNode) result.getData();
                if (jsonNode.size() > 0){
                    for (JsonNode node : jsonNode){
                        String itemid = node.get("itemid").asText();
                        String hostid = node.get("hostid").asText();
                        if(StringUtils.isNotBlank(itemid) && !itemIds.contains(itemid)){
                            itemIds.add(itemid);
                        }
                        if(itemMap.isEmpty() || itemMap.get(hostid) == null){
                            List<String> items = new ArrayList<>();
                            items.add(itemid);
                            itemMap.put(hostid,items);
                            continue;
                        }
                        if(!itemMap.isEmpty() && itemMap.get(hostid) != null){
                            List<String> items = itemMap.get(hostid);
                            items.add(itemid);
                            itemMap.put(hostid,items);
                        }
                    }
                }
            }
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemIds, startTime, endTime, 3);
            if (mwZabbixAPIResult.getCode() == 0){
                JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                if (jsonNode.size() > 0){
                    for (JsonNode node : jsonNode){
                        String itemid = node.get("itemid").asText();
                        String value = node.get("value").asText();
                        for (Map.Entry<String, List<String>> listEntry : itemMap.entrySet()) {
                            String key = listEntry.getKey();
                            List<String> items = listEntry.getValue();
                            if(items.contains(itemid) && valueMap.containsKey(key)){
                                List<String> list = valueMap.get(key);
                                list.add(value);
                                valueMap.put(key,list);
                                continue;
                            }
                            if(items.contains(itemid) && !valueMap.containsKey(key)){
                                List<String> list = new ArrayList<>();
                                list.add(value);
                                valueMap.put(key,list);
                            }
                        }
                    }
                }
            }
        }
        return valueMap;
    }

    /**
     * 计算MPLS报表网络可用率
     * @param netWorkLinkDtos
     * @param hostAndValueMap
     */
    private  List<MwMplsReportDto> reckonMplsReportNetWorkUse(List<NetWorkLinkDto> netWorkLinkDtos,Map<String, List<String>> hostAndValueMap){
        List<MwMplsReportDto> mwMplsReportDtos = new ArrayList<>();
        if(hostAndValueMap.isEmpty()){
            return mwMplsReportDtos;
        }
        for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
            Set<String> assetsIds = new HashSet<>();
            if(StringUtils.isNotBlank(netWorkLinkDto.getRootAssetsParam().getAssetsId())){
                assetsIds.add(netWorkLinkDto.getRootAssetsParam().getAssetsId());
            }
            if(StringUtils.isNotBlank(netWorkLinkDto.getTargetAssetsParam().getAssetsId())){
                assetsIds.add(netWorkLinkDto.getTargetAssetsParam().getAssetsId());
            }
            if(CollectionUtils.isEmpty(assetsIds)){
                continue;
            }
            List<String> values = new ArrayList<>();
            for (String assetsId : assetsIds) {
                if(!CollectionUtils.isEmpty(hostAndValueMap.get(assetsId))){
                    values.addAll(hostAndValueMap.get(assetsId));
                }
            }
            if(CollectionUtils.isEmpty(values)){
                continue;
            }
            List<String> abnormalValue = new ArrayList<>();
            for (String value : values) {
                if(!"1".equals(value)){
                    abnormalValue.add(value);
                }
            }
            MwMplsReportDto dto = new MwMplsReportDto();
            dto.setLineName(netWorkLinkDto.getLinkName());
            dto.setNodeName(netWorkLinkDto.getTargetAssetsParam().getAssetsName());
            dto.setNodeAssetsId(netWorkLinkDto.getTargetAssetsParam().getAssetsId());
            dto.setIpAddress(netWorkLinkDto.getTargetIpAddress());
            if(CollectionUtils.isEmpty(abnormalValue)){
                dto.setAverageAvailability("100.00");
            }else{
                double d = (values.size() - abnormalValue.size()) / values.size();
                BigDecimal bg = new BigDecimal(d);
                dto.setAverageAvailability(String.valueOf(bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()));
            }
            mwMplsReportDtos.add(dto);
        }
        return mwMplsReportDtos;
    }

    @Transactional
    @Override
    public Reply selectLineMplsReportLineName() {
        LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
        linkDropDownParam.setIsAdvancedQuery(false);
        linkDropDownParam.setPageNumber(1);
        linkDropDownParam.setPageNumber(9999);
        Reply reply = workLinkService.selectList(linkDropDownParam);
        return reply;
    }



    /**
     * 线路MPLS报表汇总数据查询
     * @param param
     * @return
     */
    @Transactional
    @Override
    public Reply selectReportLineMplsPool(List<ServerHistoryDto> param) {
        try {
            List<MwMplsPoolReportDto> lineDataInAndOutFlow = new ArrayList<>();
            List<MwLinkMplsReportDto> mwLinkMplsReportDtos = new ArrayList<>();
            if(CollectionUtils.isEmpty(param)){
                return Reply.ok(mwLinkMplsReportDtos);
            }
            Integer dateType = null;
            for (ServerHistoryDto serverHistoryDto : param) {
                dateType = serverHistoryDto.getDateType();
                Reply reply = selectLinkHistoryFlow(serverHistoryDto);
                MwMplsPoolReportDto dto = reckonFlowPool(reply, serverHistoryDto.getLineName());
                lineDataInAndOutFlow.add(dto);
            }
            List<Map<String,String>> data = new ArrayList<>();
            List<Map<String,String>> data2 = new ArrayList<>();
            List<Map<String,String>> data3 = new ArrayList<>();
            List<Map<String,String>> data4 = new ArrayList<>();
            if(!CollectionUtils.isEmpty(lineDataInAndOutFlow)){
                for (MwMplsPoolReportDto dto : lineDataInAndOutFlow) {
                    Map<String,String> map = new HashMap<>();
                    Map<String,String> map2 = new HashMap<>();
                    Map<String,String> map3 = new HashMap<>();
                    Map<String,String> map4 = new HashMap<>();
                    String lineName = dto.getLineName();
                    String acceptFlowMax = dto.getAcceptFlowMax();
                    String acceptFlowAvg = dto.getAcceptFlowAvg();
                    String sendingFlowAvg = dto.getSendingFlowAvg();
                    String sendingFlowMax = dto.getSendingFlowMax();
                    map.put("name",lineName);
                    map.put("value",getNumber(acceptFlowMax));
                    map2.put("name",lineName);
                    map2.put("value",getNumber(acceptFlowAvg));
                    map3.put("name",lineName);
                    map3.put("value",getNumber(sendingFlowMax));
                    map4.put("name",lineName);
                    map4.put("value",getNumber(sendingFlowAvg));
                    data.add(map);
                    data2.add(map2);
                    data3.add(map3);
                    data4.add(map4);
                }
            }
            //设置查询日期区间
            String dateRegion = "";
            List<String> dates = new ArrayList<>();
            dates.add(param.get(0).getDateStart());
            dates.add(param.get(0).getDateEnd());
            List<Long> times = calculitionTime(dateType,dates);
            if(CollectionUtils.isEmpty(times)){
                dateRegion = param.get(0).getDateStart()+"~"+param.get(0).getDateEnd();
            }else{
                Long startTime = times.get(0);
                Long endTime = times.get(1);
                Date date = new Date();
                date.setTime(startTime*1000);
                Date date2 = new Date();
                date2.setTime(endTime*1000);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateRegion = format.format(date)+"~"+format.format(date2);
            }
            MwLinkMplsReportDto dto = new MwLinkMplsReportDto();
            dto.setTitleName("入最大");
            dto.setUnit("Kbps");
            dto.setData(data);
            dto.setDateRegion(dateRegion);
            mwLinkMplsReportDtos.add(dto);
            MwLinkMplsReportDto dto2 = new MwLinkMplsReportDto();
            dto2.setTitleName("入平均");
            dto2.setUnit("Kbps");
            dto2.setDateRegion(dateRegion);
            dto2.setData(data2);
            mwLinkMplsReportDtos.add(dto2);
            MwLinkMplsReportDto dto3 = new MwLinkMplsReportDto();
            dto3.setTitleName("出最大");
            dto3.setUnit("Kbps");
            dto3.setData(data3);
            dto3.setDateRegion(dateRegion);
            mwLinkMplsReportDtos.add(dto3);
            MwLinkMplsReportDto dto4 = new MwLinkMplsReportDto();
            dto4.setTitleName("出平均");
            dto4.setUnit("Kbps");
            dto4.setData(data4);
            dto4.setDateRegion(dateRegion);
            mwLinkMplsReportDtos.add(dto4);
            return Reply.ok(mwLinkMplsReportDtos);
        }catch (Exception e){
            log.error("查询线路汇总数据失败"+e.getMessage());
            return Reply.fail("查询线路汇总数据失败");
        }
    }





    public  String getNumber(String str) {
        String ni = str;
        // 需要取整数和小数的字符串
        // 控制正则表达式的匹配行为的参数(小数)
        Pattern p = Pattern.compile("(\\d+\\.\\d+)");
        //Matcher类的构造方法也是私有的,不能随意创建,只能通过Pattern.matcher(CharSequence input)方法得到该类的实例.
        if (str!=null&&str.length()>0){
            Matcher m = p.matcher(str);
            //m.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
            if (m.find()) {
                //如果有相匹配的,则判断是否为null操作
                //group()中的参数：0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则,在这只有一个括号,即1和0是一样的
                str = m.group(1) == null ? "" : m.group(1);
            } else {
                //如果匹配不到小数，就进行整数匹配
                p = Pattern.compile("(\\d+)");
                m = p.matcher(str);
                if (m.find()) {
                    //如果有整数相匹配
                    str = m.group(1) == null ? "" : m.group(1);
                } else {
                    //如果没有小数和整数相匹配,即字符串中没有整数和小数，就设为空
                    str = "";
                }
            }

            if (ni.contains("Kbps")){
                str =str;
            }
            if (ni.contains("Mbps")){
                Double c  =Double.valueOf(str)*(double)1024;
                str =c.toString();
            }

        }


        return str;
    }
    /**
     * 获取最终线路汇总数据
     * @param netWorkLinkDtos
     * @param lineFlowReportParams
     */
    private  List<MwMplsPoolReportDto> getLineDataInAndOutFlow(List<NetWorkLinkDto> netWorkLinkDtos,List<LineFlowReportParam> lineFlowReportParams){
        List<MwMplsPoolReportDto> dtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(netWorkLinkDtos) || CollectionUtils.isEmpty(lineFlowReportParams)){
            return dtos;
        }
        for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
            MwMplsPoolReportDto dto = new MwMplsPoolReportDto();
            dto.setLineName(netWorkLinkDto.getLinkName());
            //线路设备名称
            String assetsName = netWorkLinkDto.getRootAssetsParam().getAssetsName();
            //线路接口
            String rootPort = netWorkLinkDto.getRootPort().equals("")?netWorkLinkDto.getTargetPort():netWorkLinkDto.getRootPort();
            for (LineFlowReportParam lineFlowReportParam : lineFlowReportParams) {
                //资产名称
                String name = lineFlowReportParam.getAssetsName();

                //资产对应接口
                String interfaceName = lineFlowReportParam.getInterfaceName();

                if(rootPort.equals(interfaceName)){
                    dto.setAcceptFlowAvg(lineFlowReportParam.getAcceptFlowAvg());
                    dto.setAcceptFlowMax(lineFlowReportParam.getAcceptFlowMax());
                    dto.setSendingFlowAvg(lineFlowReportParam.getSendingFlowAvg());
                    dto.setSendingFlowMax(lineFlowReportParam.getSendingFlowMax());
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    @Override
    public void lineMplsReportExport(List<MwLineMplsParam> params, HttpServletResponse response) {
        try {
            if(CollectionUtils.isEmpty(params)){
                return;
            }
            List<MwMplsReportDto> mwMplsReportDtos = new ArrayList<>();
            List<MwMplsPoolReportDto> lineDataInAndOutFlow = new ArrayList<>();
            for (MwLineMplsParam param : params) {
                if(param.getType() == 1){
                    Reply reply = selectReportLineMpls(param);
                    if(reply != null){
                        mwMplsReportDtos = (List<MwMplsReportDto>) reply.getData();
                    }
                }
                if(param.getType() == 2){
                    Reply reply = selectReportLineMplsPool(null);
                    if(reply != null){
                        lineDataInAndOutFlow = (List<MwMplsPoolReportDto>) reply.getData();
                    }
                }
            }
            List<MwLineReportExportParam> reportExportParams = params.get(0).getParams();
            FileInputStream is = new FileInputStream("D:\\mpls报表导出模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            //导出第一个页签数据
            exportMplsData(mwMplsReportDtos,response,is,workbook);
            //导出第二个页签数据
            if(!CollectionUtils.isEmpty(reportExportParams) && reportExportParams.size() > 0){
                exportSheet2(reportExportParams.get(0),response,is,workbook);
            }
            if(!CollectionUtils.isEmpty(reportExportParams) && reportExportParams.size() > 1){
                exportSheet3(reportExportParams.get(1),response,is,workbook);
            }
            if(!CollectionUtils.isEmpty(reportExportParams) && reportExportParams.size() > 2){
                exportSheet4(reportExportParams.get(2),response,is,workbook);
            }
            if(!CollectionUtils.isEmpty(reportExportParams) && reportExportParams.size() > 3){
                exportSheet5(reportExportParams.get(3),response,is,workbook);
            }
            //导出第六个页签数据
            exportMplsPoolData(lineDataInAndOutFlow,response,is,workbook);
            OutputStream os = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition","attachment; filename="+System.currentTimeMillis()+".xlsx");
            response.setContentType("application/msexcel");
            workbook.write(os);
            os.close();
        }catch (Exception e){
            log.error("线路MPLS报表导出失败"+e.getMessage());
        }

    }

    @Transactional
    @Override
    public Reply selectReportIpNews(IpReportSreach param) {
        String loginName = iLoginCacheInfo.getLoginName();
        List <String> idlist = new ArrayList<>();
        if (param.getStatus()==0){
            List<String> ids = param.getIds();
            if(!CollectionUtils.isEmpty(ids)){
                for (String id : ids) {
                    String[] s = id.split("_");
                    if("orgId".equals(s[0])){
                        idlist.add(s[1]);
                    }
                }
            }
        }else {
            idlist.addAll(param.getIds());
        }
        String dateRegion = getDateRegion(param.getDateType(), param.getChooseTime());
        //获取时间范围
        Integer dateType = param.getDateType();
        List<String> chooseTime = param.getChooseTime();
        List<Long> times = calculitionTime(dateType, chooseTime);
        Date startTime =new Date(times.get(0)*1000) ;
        Date endTime =new Date(times.get(1)*1000) ;
        /*List<Map<String,Object>> mapList = reportTerraceManageDao.getIpAddress(idlist,param.getStatus());*/
        List<IpAddressReport> ipAddressNew = reportTerraceManageDao.getIpAddressNew(idlist, param.getStatus());
        List<String> strings = new ArrayList<>();
        List<Integer> str = new ArrayList<>();
        Map<Integer,String> mapkey = new HashMap<>();
        Map<String,String> mapput = new HashMap<>();
        for (IpAddressReport ipAddressReport : ipAddressNew) {
            String ipaddress = ipAddressReport.getIpAddress();
            Integer linkid = ipAddressReport.getLinkId();
            strings.add(ipaddress);
            if (!str.contains(linkid)){
                str.add(linkid);
                String src = getallByLinkid(linkid,"");
                mapkey.put(linkid,src);
                mapput.put(ipaddress,src);
            }else {
                mapput.put(ipaddress,mapkey.get(linkid));
            }
        }
        List<IpAddressReport> ipList1 = reportTerraceManageDao.selectEatAssets("测试", param.getRadio(), strings, startTime, endTime, param.getOrder(), param.getProperty());
        for (IpAddressReport ipAddressReport : ipList1) {
            String ipAddress = ipAddressReport.getIpAddress();
            Integer linkId = ipAddressReport.getLinkId();
            for (IpAddressReport addressReport : ipAddressNew) {
                String ipAddress1 = addressReport.getIpAddress();
                Integer linkId1 = addressReport.getId();
                if((ipAddress+linkId).equals(ipAddress1+linkId1)){
                    addressReport.setStatus(ipAddressReport.getStatus());
                    addressReport.setStringStatus(ipAddressReport.getStringStatus());
                    addressReport.setOnLineType(ipAddressReport.getType());
                    addressReport.setColorStatus(1);
                }
            }
        }
        List<IpAddressReport> ipList2 = reportTerraceManageDao.selectIPaddresshis("测试",param.getRadio(),strings,startTime,endTime,param.getOrder(),param.getProperty());
        for (IpAddressReport ipAddressReport : ipList2) {
            String ipAddress = ipAddressReport.getIpAddress();
            Integer linkId = ipAddressReport.getLinkId();
            for (IpAddressReport addressReport : ipAddressNew) {
                String ipAddress1 = addressReport.getIpAddress();
                Integer linkId1 = addressReport.getId();
                if((ipAddress+linkId).equals(ipAddress1+linkId1)){
                    addressReport.setStatus(ipAddressReport.getStatus());
                    addressReport.setStringStatus(ipAddressReport.getStringStatus());
                    addressReport.setUseStatus(ipAddressReport.getType());
                    if(addressReport.getColorStatus() == 1){
                        addressReport.setColorStatus(3);
                    }else{
                        addressReport.setColorStatus(2);
                    }

                }
            }
        }
        List<String> ips = new ArrayList<>();
        for (IpAddressReport s:ipAddressNew) {
            s.setLabel(mapput.get(s.getIpAddress()));
            s.setTime(dateRegion);
            ips.add(s.getIpAddress());
        }
        //根据IP地址查询是否以生成资产数据
        List<Map<String, Object>> mapList = reportTerraceManageDao.selectAssetsNanoTube(ips);
        if(!CollectionUtils.isEmpty(mapList)){
            for (Map<String, Object> map : mapList) {
                String ip = (String) map.get("ip");
                Date createDate = (Date) map.get("createDate");
                if(StringUtils.isBlank(ip) || createDate == null)continue;
                ipAddressNew.forEach(item->{
                    String ipAddress = item.getIpAddress();
                    if(ip.equals(ipAddress)){
                        item.setNanoTubeStatus("已纳管");
                        item.setUpdateDate(createDate);
                        //判断是否在选择区间内进行纳管
                        if((createDate.getTime() >= startTime.getTime()) && (createDate.getTime() <= endTime.getTime())){
                            item.setNanoTubeColor(1);
                        }
                    }
                });
            }
        }
        ipAddressNew.forEach(item->{
            if(StringUtils.isBlank(item.getNanoTubeStatus())){
                item.setNanoTubeStatus("未纳管");
            }
        });
        Comparator<Object> com = Collator.getInstance(Locale.CHINA);
        Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
        List<IpAddressReport> realData = ipAddressNew.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getIpAddress()), pinyin4jUtil.getStringPinYin(o2.getIpAddress()))).collect(Collectors.toList());
        //排序
        Collections.sort(realData, new Comparator<IpAddressReport>() {
            @Override
            public int compare(IpAddressReport o1, IpAddressReport o2) {
                if( o2.getColorStatus() > o1.getColorStatus()){
                    return 1;
                }
                if( o2.getColorStatus() < o1.getColorStatus()){
                    return -1;
                }
                return 0;
            }
        });
        Collections.sort(realData, new Comparator<IpAddressReport>() {
            @Override
            public int compare(IpAddressReport o1, IpAddressReport o2) {
                if( o2.getNanoTubeColor() > o1.getNanoTubeColor()){
                    return 1;
                }
                if( o2.getNanoTubeColor() < o1.getNanoTubeColor()){
                    return -1;
                }
                return 0;
            }
        });
       /* PageHelper.startPage(param.getPageNumber() , param.getPageSize());
        List<IpAddressReport> list = new ArrayList<>();
        if (strings.size()>0){
            if(param.getRadio()==0){
                list=reportTerraceManageDao.selectEatAssets("测试",param.getRadio(),strings,startTime,endTime,param.getOrder(),param.getProperty());
            }else {
                list=reportTerraceManageDao.selectIPaddresshis("测试",param.getRadio(),strings,startTime,endTime,param.getOrder(),param.getProperty());;
            }

        }*/
        Integer pageNumber = param.getPageNumber();
        Integer pageSize = param.getPageSize();
        int fromIndex = pageSize * (pageNumber -1);
        int toIndex = pageSize * pageNumber;
        if(toIndex > realData.size()){
            toIndex = realData.size();
        }
        List<IpAddressReport> ipAddressReports = realData.subList(fromIndex, toIndex);
        PageInfo pageInfo = new PageInfo<>(ipAddressReports);
        pageInfo.setTotal(ipAddressNew.size());
        pageInfo.setList(ipAddressReports);
        return Reply.ok(pageInfo);
    }

    private String getallByLinkid(Integer linkid,String s) {
        if (linkid!=0){
            Map<String,Object> map = reportTerraceManageDao.getlinkid(linkid);
            s=getallByLinkid((Integer) map.get("parent_id"),s);
            s=s+"/"+map.get("label");
        }
        else {
            return s;
        }
        return s;
    }

    //    @Override
    public void ipReportExport1(List<IpAddressReport> ipReportSreaches, HttpServletResponse response, Integer radio) {
        ExcelWriter excelWriter = null;
        try {
            String tittle = "IP报表数据";
            excelWriter = exportReportSetNews(tittle, response, IpAddressReport.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("time");
            includeColumnFiledNames.add("label");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("nanoTubeStatus");
            includeColumnFiledNames.add("strOnLineType");
            includeColumnFiledNames.add("strUseStatus");
            includeColumnFiledNames.add("updateDate");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(ipReportSreaches, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出失败", e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Override
    public void ipReportExport(List<IpAddressReport> ipReportSreaches, HttpServletResponse response, Integer radio){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("IP地址报表");
        XSSFRow hssfRow = sheet.createRow(0);
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        //添加表头内容
        XSSFCell headCell = hssfRow.createCell(0);
        headCell.setCellValue("时间区域");
        headCell.setCellStyle(cellStyle);
        headCell = hssfRow.createCell(1);
        headCell.setCellValue("站点名称");
        headCell.setCellStyle(cellStyle);
        headCell = hssfRow.createCell(2);
        headCell.setCellValue("ip地址");
        headCell.setCellStyle(cellStyle);
        headCell = hssfRow.createCell(3);
        headCell.setCellValue("纳管状态");
        headCell.setCellStyle(cellStyle);
        headCell = hssfRow.createCell(4);
        headCell.setCellValue("在线状态");
        headCell.setCellStyle(cellStyle);
        headCell = hssfRow.createCell(5);
        headCell.setCellValue("使用状态");
        headCell.setCellStyle(cellStyle);
        headCell = hssfRow.createCell(6);
        headCell.setCellValue("时间");
        headCell.setCellStyle(cellStyle);
        //添加数据内容
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < ipReportSreaches.size(); i++) {
            XSSFRow row = sheet.createRow(i + 1);
            IpAddressReport ipAddressReport = ipReportSreaches.get(i);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(ipAddressReport.getTime());
            cell.setCellStyle(cellStyle);
            cell = row.createCell(1);
            cell.setCellValue(ipAddressReport.getLabel());
            cell.setCellStyle(cellStyle);
            cell = row.createCell(2);
            cell.setCellValue(ipAddressReport.getIpAddress());
            cell.setCellStyle(cellStyle);
            cell = row.createCell(3);
            if(ipAddressReport.getNanoTubeColor() == 1){
                cell.setCellValue(ipAddressReport.getNanoTubeStatus());
                XSSFCellStyle style = workbook.createCellStyle();
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(IndexedColors.RED.index);
                cell.setCellStyle(style);
            }else{
                cell.setCellValue(ipAddressReport.getNanoTubeStatus());
                cell.setCellStyle(cellStyle);
            }
            cell = row.createCell(4);
            if(ipAddressReport.getColorStatus() == 1 || ipAddressReport.getColorStatus() == 3){
                cell.setCellValue(ipAddressReport.getStrOnLineType());
                XSSFCellStyle style = workbook.createCellStyle();
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(IndexedColors.RED.index);
                cell.setCellStyle(style);
            }else{
                cell.setCellValue(ipAddressReport.getStrOnLineType());
                cell.setCellStyle(cellStyle);
            }
            cell = row.createCell(5);
            if(ipAddressReport.getColorStatus() == 2 || ipAddressReport.getColorStatus() == 3){
                cell.setCellValue(ipAddressReport.getStrUseStatus());
                XSSFCellStyle style = workbook.createCellStyle();
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(IndexedColors.RED.index);
                cell.setCellStyle(style);
            }else{
                cell.setCellValue(ipAddressReport.getStrUseStatus());
                cell.setCellStyle(cellStyle);
            }
            cell = row.createCell(6);
            if(ipAddressReport.getUpdateDate() != null){
                cell.setCellValue(format.format(ipAddressReport.getUpdateDate()));
            }else{
                cell.setCellValue("");
            }
            cell.setCellStyle(cellStyle);
        }

        try {
            OutputStream outputStream = response.getOutputStream();

            String fileName = System.currentTimeMillis() +".xlsx";
//            response.setContentType("application/vnd.ms-excel");
//            response.setCharacterEncoding("utf-8");
//            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            response.setHeader("Content-Length", String.valueOf(baos.size()));
            outputStream.write( baos.toByteArray() );
            outputStream.close();
        }catch (Exception e){

        }

    }

    @Transactional
    @Override
    public Reply seleAllLink() {
        List<NetWorkLinkDto> netWorkLinkDtos = workLinkService.seleAllLink();
        PageInfo pageInfo = new PageInfo<>(netWorkLinkDtos);
        pageInfo.setList(netWorkLinkDtos);
        return Reply.ok(pageInfo);
    }


    private void exportMplsData(List<MwMplsReportDto> mwMplsReportDtos,HttpServletResponse response,FileInputStream is,XSSFWorkbook workbook) throws Exception {
        if(CollectionUtils.isEmpty(mwMplsReportDtos)){

            return;
        }
        //获取第一个页签模板
        XSSFSheet sheet = workbook.getSheetAt(0);
        sheet.setForceFormulaRecalculation(true);
        workbook.setSheetName(0,"MPLS报告");
        for (int i = 1; i < mwMplsReportDtos.size(); i++) {
            MwMplsReportDto dto = mwMplsReportDtos.get(i);
            XSSFRow row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            row.createCell(0).setCellValue(dto.getLineName());
            row.createCell(1).setCellValue(dto.getVendor());
            row.createCell(2).setCellValue(dto.getNodeName());
            row.createCell(3).setCellValue(dto.getIpAddress());
            XSSFCell cell = row.createCell(4);
            cell.setCellValue(Double.parseDouble(dto.getAverageAvailability()) / 100);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            XSSFDataFormat dataFormat = workbook.createDataFormat();
            cellStyle.setDataFormat(dataFormat.getFormat("0.00%"));
            cell.setCellStyle(cellStyle);
        }

    }

    private void exportSheet2(MwLineReportExportParam param,HttpServletResponse response,FileInputStream is,XSSFWorkbook workbook){
        //获取第为、、er个页签模板
        XSSFSheet sheet = workbook.getSheetAt(1);
        sheet.setForceFormulaRecalculation(true);
        workbook.setSheetName(1,param.getName());
        List<Map<String, String>> receiveMaxData = param.getReceiveMaxData();
        List<Map<String, String>> sendMaxData = param.getSendMaxData();
        for (int i = 1; i < receiveMaxData.size(); i++) {
            Map<String, String> map = receiveMaxData.get(i);
            Map<String, String> sendMap = sendMaxData.get(i);
            XSSFRow row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            String dateTime = map.get("dateTime");
            String doubleValue = map.get("doubleValue");
            String sendDoubleValue = sendMap.get("doubleValue");
            row.createCell(0).setCellValue(dateTime.split(" ")[0]);
            row.createCell(1).setCellValue(dateTime.split(" ")[1]);
            row.createCell(2).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(3).setCellValue(Double.parseDouble(sendDoubleValue));
            row.createCell(4).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(5).setCellValue(Double.parseDouble(sendDoubleValue));
        }
    }

    private void exportSheet3(MwLineReportExportParam param,HttpServletResponse response,FileInputStream is,XSSFWorkbook workbook){
        //获取第为、、er个页签模板
        XSSFSheet sheet = workbook.getSheetAt(1);
        sheet.setForceFormulaRecalculation(true);
        workbook.setSheetName(1,param.getName());
        List<Map<String, String>> receiveMaxData = param.getReceiveMaxData();
        List<Map<String, String>> sendMaxData = param.getSendMaxData();
        for (int i = 1; i < receiveMaxData.size(); i++) {
            Map<String, String> map = receiveMaxData.get(i);
            Map<String, String> sendMap = sendMaxData.get(i);
            XSSFRow row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            String dateTime = map.get("dateTime");
            String doubleValue = map.get("doubleValue");
            String sendDoubleValue = sendMap.get("doubleValue");
            row.createCell(0).setCellValue(dateTime.split(" ")[0]);
            row.createCell(1).setCellValue(dateTime.split(" ")[1]);
            row.createCell(2).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(3).setCellValue(Double.parseDouble(sendDoubleValue));
            row.createCell(4).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(5).setCellValue(Double.parseDouble(sendDoubleValue));
        }
    }

    private void exportSheet4(MwLineReportExportParam param,HttpServletResponse response,FileInputStream is,XSSFWorkbook workbook){
        //获取第为、、er个页签模板
        XSSFSheet sheet = workbook.getSheetAt(1);
        sheet.setForceFormulaRecalculation(true);
        workbook.setSheetName(1,param.getName());
        List<Map<String, String>> receiveMaxData = param.getReceiveMaxData();
        List<Map<String, String>> sendMaxData = param.getSendMaxData();
        for (int i = 1; i < receiveMaxData.size(); i++) {
            Map<String, String> map = receiveMaxData.get(i);
            Map<String, String> sendMap = sendMaxData.get(i);
            XSSFRow row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            String dateTime = map.get("dateTime");
            String doubleValue = map.get("doubleValue");
            String sendDoubleValue = sendMap.get("doubleValue");
            row.createCell(0).setCellValue(dateTime.split(" ")[0]);
            row.createCell(1).setCellValue(dateTime.split(" ")[1]);
            row.createCell(2).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(3).setCellValue(Double.parseDouble(sendDoubleValue));
            row.createCell(4).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(5).setCellValue(Double.parseDouble(sendDoubleValue));
        }
    }


    private void exportSheet5(MwLineReportExportParam param,HttpServletResponse response,FileInputStream is,XSSFWorkbook workbook){
        //获取第为、、er个页签模板
        XSSFSheet sheet = workbook.getSheetAt(1);
        sheet.setForceFormulaRecalculation(true);
        workbook.setSheetName(1,param.getName());
        List<Map<String, String>> receiveMaxData = param.getReceiveMaxData();
        List<Map<String, String>> sendMaxData = param.getSendMaxData();
        for (int i = 1; i < receiveMaxData.size(); i++) {
            Map<String, String> map = receiveMaxData.get(i);
            Map<String, String> sendMap = sendMaxData.get(i);
            XSSFRow row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            String dateTime = map.get("dateTime");
            String doubleValue = map.get("doubleValue");
            String sendDoubleValue = sendMap.get("doubleValue");
            row.createCell(0).setCellValue(dateTime.split(" ")[0]);
            row.createCell(1).setCellValue(dateTime.split(" ")[1]);
            row.createCell(2).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(3).setCellValue(Double.parseDouble(sendDoubleValue));
            row.createCell(4).setCellValue(Double.parseDouble(doubleValue));
            row.createCell(5).setCellValue(Double.parseDouble(sendDoubleValue));
        }
    }

    private void exportMplsPoolData(List<MwMplsPoolReportDto> lineDataInAndOutFlow,HttpServletResponse response,FileInputStream is,XSSFWorkbook workbook) throws Exception{
        if(CollectionUtils.isEmpty(lineDataInAndOutFlow)){
            return;
        }
        //获取第一个页签模板
        XSSFSheet sheet = workbook.getSheetAt(5);
        sheet.setForceFormulaRecalculation(true);
        workbook.setSheetName(5,"MPLS平均带宽汇总");
        for (int i = 1; i < lineDataInAndOutFlow.size(); i++) {
            MwMplsPoolReportDto dto = lineDataInAndOutFlow.get(i);
            XSSFRow row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            row.createCell(0).setCellValue(dto.getLineName());
            row.createCell(1).setCellValue(Double.parseDouble(dto.getAcceptFlowAvg()));
            row.createCell(2).setCellValue(Double.parseDouble(dto.getAcceptFlowMax()));
            row.createCell(3).setCellValue(Double.parseDouble(dto.getSendingFlowAvg()));
            XSSFCell cell = row.createCell(4);
            cell.setCellValue(Double.parseDouble(dto.getSendingFlowMax()));
//            XSSFCellStyle cellStyle = workbook.createCellStyle();
//            XSSFDataFormat dataFormat = workbook.createDataFormat();
//            cellStyle.setDataFormat(dataFormat.getFormat("0.00"));
//            cell.setCellStyle(cellStyle);
        }
    }



    /**
     * 线路分级查询
     * @return
     */
    @Transactional
    @Override
    public Reply selectReportLinkGrade() {
        List<MwReportLinkGradeDto> alldtos = new ArrayList<>();
        //查询所有机构数据
        List<MWOrgDTO> allOrgList = orgService.getAllOrgList();
        List<MwReportLinkGradeDto> dtos = new ArrayList<>();
        MwReportLinkGradeDto dto = new MwReportLinkGradeDto();
        setOrgValue(allOrgList,dtos);
        dto.setValue("orgId_parent"+1);
        dto.setLabel("机构");
        dto.setChildren(dtos);
        alldtos.add(dto);
        //查询标签数据
        List<MwLabelManageDTO> mwLabelManageDTOS = labelManageTableDao.selectList(new HashMap());
        List<MwReportLinkGradeDto> labelDtos = new ArrayList<>();
        if(!CollectionUtils.isEmpty(mwLabelManageDTOS)){
            for (MwLabelManageDTO mwLabelManageDTO : mwLabelManageDTOS) {
                MwReportLinkGradeDto label = new MwReportLinkGradeDto();
                label.setValue("labelId_"+mwLabelManageDTO.getLabelId());
                label.setLabel(mwLabelManageDTO.getLabelName());
                label.setChildren(new ArrayList<>());
                labelDtos.add(label);
            }
        }
        MwReportLinkGradeDto dto2 = new MwReportLinkGradeDto();
        dto2.setValue("labelId_parent"+2);
        dto2.setLabel("标签");
        dto2.setChildren(labelDtos);
        alldtos.add(dto2);
        return Reply.ok(alldtos);
    }


    /**
     * 设置机构数据
     * @param allOrgList
     * @param dtos
     */
    private void setOrgValue(List<MWOrgDTO> allOrgList,List<MwReportLinkGradeDto> dtos){
        for (MWOrgDTO mwOrgDTO : allOrgList) {
            MwReportLinkGradeDto dto = new MwReportLinkGradeDto();
            dto.setValue("orgId_"+mwOrgDTO.getOrgId());
            dto.setLabel(mwOrgDTO.getOrgName());
            List<MWOrgDTO> childs = mwOrgDTO.getChilds();
            List<MwReportLinkGradeDto> dtos2 = new ArrayList<>();
            if(!CollectionUtils.isEmpty(childs)){
                setOrgValue(childs,dtos2);
            }
            dto.setChildren(dtos2);
            dtos.add(dto);
        }
    }


    @Transactional
    @Override
    public Reply selectReportLinkGradeData(MwLinkGradeParam param) {
        try {
            //查询所有线路数据
            LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
            log.info("开始查询线路基础数据"+new Date());
            List<NetWorkLinkDto> netWorkLinkDtos = workLinkService.getNetWorkLinkDtos(linkDropDownParam);
            log.info("结束查询线路基础数据"+new Date());
            List<Integer> orgIds = new ArrayList<>();
            List<Integer> labelIds = new ArrayList<>();
            List<String> ids = param.getIds();
            if(!CollectionUtils.isEmpty(ids)){
                for (String id : ids) {
                    String[] s = id.split("_");
                    if("orgId".equals(s[0])){
                        orgIds.add(Integer.parseInt(s[1]));
                    }
                    if("labelId".equals(s[0])){
                        labelIds.add(Integer.parseInt(s[1]));
                    }
                }
            }
            if(!CollectionUtils.isEmpty(netWorkLinkDtos) && !CollectionUtils.isEmpty(orgIds)){
                Iterator<NetWorkLinkDto> iterator = netWorkLinkDtos.iterator();
                while(iterator.hasNext()){
                    NetWorkLinkDto next = iterator.next();
                    List<cn.mw.monitor.service.user.dto.OrgDTO> department = next.getDepartment();
                    if(!CollectionUtils.isEmpty(department)){
                        boolean flag = true;
                        for (cn.mw.monitor.service.user.dto.OrgDTO orgDTO : department) {
                            if(orgIds.contains(orgDTO.getOrgId())){
                                flag = false;
                            }
                        }
                        if(flag){
                            iterator.remove();
                        }
                    }
                }
            }
            if(!CollectionUtils.isEmpty(netWorkLinkDtos) && !CollectionUtils.isEmpty(labelIds)){
                //根据ID查询对应标签
                Iterator<NetWorkLinkDto> iterator = netWorkLinkDtos.iterator();
                while(iterator.hasNext()){
                    NetWorkLinkDto next = iterator.next();
                    String linkId = next.getLinkId();
                    List<Integer> labelData = reportTerraceManageDao.getLabelData(linkId);
                    if(CollectionUtils.isEmpty(labelData)){
                        boolean flag = true;
                        for (Integer labelDatum : labelData) {
                            if(labelIds.contains(labelDatum)){
                                flag = false;
                            }
                        }
                        if(flag){
                            iterator.remove();
                        }
                    }
                }
            }
            return Reply.ok(netWorkLinkDtos);
        }catch (Exception e){
            log.error("分级过滤失败"+e.getMessage());
            return Reply.fail("分级过滤失败");
        }
    }




    private List<RunTimeItemValue> getThreadValue(List<MwTangibleassetsTable> mwTangibleassetsDTOS,String name, Long startTime, Long endTime,Integer type, Set<String> ids) {
        log.info("topN类型为0,开始执行zabbix查询操作"+new Date());
        List<RunTimeItemValue> list = new ArrayList<>();
        //根据资产服务ID分组
        Map<Integer,List<String>> assetsMap = new HashMap<>();
        Map<String,MwTangibleassetsTable> dtoMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
            Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
            String assetsId = mwTangibleassetsDTO.getAssetsId();
            dtoMap.put(assetsId,mwTangibleassetsDTO);
            if(assetsMap.isEmpty() || assetsMap.get(monitorServerId) == null){
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
                continue;
            }
            if(!assetsMap.isEmpty() && assetsMap.get(monitorServerId) != null){
                List<String> assetsIds = assetsMap.get(monitorServerId);
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
            }
        }
        if(assetsMap.isEmpty()){
            return null;
        }
        Set<String> assetss = new HashSet<>();
        List<String> assetsIds = new ArrayList<>();
        log.info("topN类型0开始查询zabbix"+new Date());
        for (Map.Entry<Integer, List<String>> integerListEntry : assetsMap.entrySet()) {
            Integer monitorServerId = integerListEntry.getKey();
            List<String> hostIds = integerListEntry.getValue();
            log.info("名称"+name+"开始查询zabbix"+new Date());
            long getZabbixStart = System.currentTimeMillis();
            MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostIds, false);
            long getZabbixEnd = System.currentTimeMillis();
            log.info("名称"+name+"开始查询zabbix结束"+new Date());
            log.info("执行mwtpServerAPI.itemGetbyType方法,hostIds长度"+hostIds.size()+"查询名称"+name+"耗时"+(getZabbixEnd-getZabbixStart));
            List<String> itemIds = new ArrayList<>();
            if (result0.getCode() == 0) {
                JsonNode itemData = (JsonNode) result0.getData();
                if (itemData != null && itemData.size() > 0) {
                    List<String> hostItemIds = new ArrayList<>();
                    Integer vlaueType = itemData.get(0).get("value_type").asInt();
                    for (int i = 0; i < itemData.size(); i++) {
                        String itemid = itemData.get(i).get("itemid").asText();
                        String hostid = itemData.get(i).get("hostid").asText();
                        hostItemIds.add(hostid+itemid);
                        assetsIds.add(hostid);
                        itemIds.add(itemid);
                        if(ids == null){
                            continue;
                        }
                        ids.add(itemid);
                    }
                    if(type == 2){
                        continue;
                    }
                    log.info("名称"+name+"开始查询zabbix"+new Date());
                    getZabbixStart = System.currentTimeMillis();
                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                    getZabbixEnd = System.currentTimeMillis();
                    log.info("名称"+name+"开始查询zabbix结束"+new Date());
                    log.info("执行mwtpServerAPI.HistoryGetByTimeAndType方法,itemIds长度"+itemIds.size()+"查询名称"+name+"耗时"+(getZabbixEnd-getZabbixStart));
                    if (historyRsult.getCode() == 0){
                        JsonNode itemData2 = (JsonNode) historyRsult.getData();
                        Map<String,List<Double>> avgValueMap = new HashMap<>();
                        if (itemData2 != null && itemData2.size() > 0){
                            for (int i = 0; i < itemData2.size(); i++){
                                String itemid = itemData2.get(i).get("itemid").asText();
                                Double value = itemData2.get(i).get("value").asDouble();
                                if(StringUtils.isBlank(itemid)){
                                    continue;
                                }
                                if(avgValueMap.isEmpty() || avgValueMap.get(itemid) == null){
                                    List<Double> avgs = new ArrayList<>();
                                    avgs.add(value);
                                    avgValueMap.put(itemid,avgs);
                                    continue;
                                }
                                if(!avgValueMap.isEmpty() && avgValueMap.get(itemid) != null){
                                    List<Double> avgs = avgValueMap.get(itemid);
                                    avgs.add(value);
                                    avgValueMap.put(itemid,avgs);
                                }
                                for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                                    String assetsId = mwTangibleassetsDTO.getAssetsId();
                                    if(hostItemIds.contains(assetsId+itemid)){
                                        assetss.add(assetsId);
                                        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                                        runTimeItemValue.setItemName(name);
                                        runTimeItemValue.setItemId(itemid);
                                        runTimeItemValue.setHostId(mwTangibleassetsDTO.getAssetsId());
                                        runTimeItemValue.setAssetName(mwTangibleassetsDTO.getAssetsName());
                                        runTimeItemValue.setServerId(mwTangibleassetsDTO.getMonitorServerId());
                                        runTimeItemValue.setIp(mwTangibleassetsDTO.getInBandIp());
                                        list.add(runTimeItemValue);
                                    }
                                }
                            }
                        }else{
                            for (MwTangibleassetsTable tangibleassetsTable : mwTangibleassetsDTOS) {
                                if(assetsIds.contains(tangibleassetsTable.getAssetsId())){
                                    assetss.add(tangibleassetsTable.getAssetsId());
                                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                                    runTimeItemValue.setItemName(name);
                                    runTimeItemValue.setAvgValue("0.00%");
                                    runTimeItemValue.setHostId(tangibleassetsTable.getAssetsId());
                                    runTimeItemValue.setSortLastAvgValue(Double.valueOf("0"));
                                    runTimeItemValue.setMaxValue("0.00%");
                                    runTimeItemValue.setMinValue("0.00%");
                                    runTimeItemValue.setAssetName(tangibleassetsTable.getAssetsName());
                                    runTimeItemValue.setServerId(tangibleassetsTable.getMonitorServerId());
                                    runTimeItemValue.setIp(tangibleassetsTable.getInBandIp());
                                    list.add(runTimeItemValue);
                                }
                            }

                        }
                        if(!CollectionUtils.isEmpty(list)){
                            for (RunTimeItemValue runTimeItemValue : list) {
                                if(StringUtils.isNotBlank(runTimeItemValue.getItemId()) && avgValueMap.get(runTimeItemValue.getItemId()) != null){
                                    List<Double> agvgs = avgValueMap.get(runTimeItemValue.getItemId());
                                    String valueavg = String.valueOf(agvgs.stream().mapToDouble(Double::valueOf).average().getAsDouble());
                                    String values = new BigDecimal(valueavg).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    runTimeItemValue.setAvgValue(values+"%");
                                    runTimeItemValue.setMaxValue(String.valueOf(agvgs.stream().mapToDouble(Double::valueOf).max().getAsDouble())+"%");
                                    runTimeItemValue.setMinValue(String.valueOf(agvgs.stream().mapToDouble(Double::valueOf).min().getAsDouble())+"%");
                                    runTimeItemValue.setSortLastAvgValue(Double.valueOf(valueavg));
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("topN类型0查询zabbix结束"+new Date());
        Set<String> onlyId = new HashSet<>();
        if(!CollectionUtils.isEmpty(list)){
            Iterator<RunTimeItemValue> iterator = list.iterator();
            while(iterator.hasNext()){
                RunTimeItemValue next = iterator.next();
                if(next.getAvgValue() == null || onlyId.contains(next.getHostId())){
                    iterator.remove();
                }
                onlyId.add(next.getHostId());
            }
        }
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS){
            String assetsId = mwTangibleassetsDTO.getAssetsId();
            if(assetss.contains(assetsId) || !assetsIds.contains(assetsId)){
                continue;
            }
            RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
            runTimeItemValue.setItemName(name);
            runTimeItemValue.setHostId(mwTangibleassetsDTO.getAssetsId());
            runTimeItemValue.setAvgValue("0.00%");
            runTimeItemValue.setSortLastAvgValue(Double.valueOf("0"));
            runTimeItemValue.setMaxValue("0.00%");
            runTimeItemValue.setMinValue("0.00%");
            runTimeItemValue.setAssetName(mwTangibleassetsDTO.getAssetsName());
            runTimeItemValue.setServerId(mwTangibleassetsDTO.getMonitorServerId());
            runTimeItemValue.setIp(mwTangibleassetsDTO.getInBandIp());
            list.add(runTimeItemValue);
        }
        log.info("topN类型为0,执行zabbix查询操作完成"+new Date());
        return list;
    }

    /**
     * 查询线路历史流量数据
     * @param param
     * @return
     */
    @Transactional
    @Override
    public Reply selectLinkHistoryFlow(ServerHistoryDto param) {
        try {
            Integer dateType = param.getDateType();
            if(dateType != null && dateType != 2){
                List mplsLineHistoryCacheData = getMplsLineHistoryCacheData(param);
                return Reply.ok(mplsLineHistoryCacheData);
            }
            if(dateType != 5){
                List<Long> times = calculitionTime(dateType, null);
                Long startTime = times.get(0);
                Long endTime = times.get(1);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                date.setTime(startTime*1000);
                Date date2 = new Date();
                date2.setTime(endTime*1000);
                if(StringUtils.isBlank(param.getDateStart())){
                    param.setDateStart(format.format(date));
                }
                if(StringUtils.isBlank(param.getDateEnd())){
                    param.setDateEnd(format.format(date2));
                }
                param.setDateType(5);
            }
            Reply historyData = serverService.getHistoryData(param);
            return historyData;
        }catch (Exception e){
            log.error("fail to selectLinkHistoryFlow errorInfo:{}", e);
            return Reply.fail("fail to selectLinkHistoryFlow errorInfo:{}");
        }
    }

    private  List getMplsLineHistoryCacheData(ServerHistoryDto param) throws ParseException {
        Integer dateType = param.getDateType();
        String lineName = param.getLineName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Long startTime = 0l;
        Long endTime = 0l;
        Long lday = 86400000l;
        List<Map<String,String>> list = null;
        List<MWMplsCacheDataDto> mwMplsCacheDataDtos = new ArrayList<>();
        if(dateType != null && dateType != 2 && dateType == 1){//昨日数据，查询数据库
            List<Date> yesterday = ReportDateUtil.getYesterday();
            list = reportTerraceManageDao.selectMplsLineHistoryDailyDataToo(yesterday.get(0),yesterday.get(1), lineName);
        }
        if(dateType != null && dateType != 2 && dateType == 5){//上周数据，查询数据库
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            mwMplsCacheDataDtos = reportTerraceManageDao.selectMplsLineHistoryWeeklyData(format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1)), lineName);

        }
        if(dateType != null && dateType != 2 && dateType == 8){//上月数据，查询数据库
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            mwMplsCacheDataDtos = reportTerraceManageDao.selectMplsLineHistoryMonthlyData(format.format(lastMonth.get(0)) + "~" + format.format(lastMonth.get(1)), lineName);
        }
        if(StringUtils.isNotBlank(param.getDateStart()) && StringUtils.isNotBlank(param.getDateEnd()) && (dateType == null || dateType == 11)){//自定义时间
            startTime = format.parse(param.getDateStart()).getTime();
            endTime = format.parse(param.getDateEnd()).getTime();
            int day = (int) ((endTime - startTime) / lday);
            if(day > 3){
                list = reportTerraceManageDao.selectMplsLineHistoryDailyDataToo(format.parse(param.getDateStart()),format.parse(param.getDateEnd()), lineName);
            }else{
                list = reportTerraceManageDao.selectMplsLineHistoryDailyData(format.parse(param.getDateStart()),format.parse(param.getDateEnd()), lineName);
            }
        }
        List<Object> realData = MWReportHandlerDataLogic.handleMplsHistoryReportData(mwMplsCacheDataDtos, list, param);
        return realData;
    }

    private MwMplsPoolReportDto reckonFlowPool(Reply historyData,String lineName){
        MwMplsPoolReportDto dto = new MwMplsPoolReportDto();
        //计算线路流量汇总情况
        if(historyData == null){
            return dto;
        }
        List list = (List) historyData.getData();
        if(CollectionUtils.isEmpty(list)){
            return dto;
        }
        dto.setLineName(lineName);
        for (Object obj : list) {
            if(obj != null){
                List listMap = (List) obj;
                log.info("查询MPLS报表汇总数据计算开始1"+listMap);
                if(CollectionUtils.isEmpty(listMap)){
                    continue;
                }
                for (Object o : listMap) {
                    Map map = (Map) o;
                    log.info("查询MPLS报表汇总数据计算开始2"+map);
                    //获取流量走向
                    Object titleName = map.get("titleName");
                    String unit = (String) map.get("unitByReal");
                    if(StringUtils.isBlank(unit)){
                        continue;
                    }
                    dto.setUnit("kbps");
                    if(titleName != null && titleName.toString().contains("发送")){
                        if(map.get("realData") != null){
                            List realData = (List) map.get("realData");
                            double sendMax = 0;
                            double sendAvg;
                            double count = 0;
                            for (Object realDatum : realData) {
                                if(realDatum != null){
                                    MWItemHistoryDto historyDto = (MWItemHistoryDto) realDatum;
                                    String value = historyDto.getValue();
                                    if(StringUtils.isNotBlank(value)){
                                        double doubleValue = Double.parseDouble(value);
                                        if(sendMax < doubleValue){
                                            sendMax = doubleValue;
                                        }
                                        count += doubleValue;
                                    }
                                }
                            }
                            if(count == 0){
                                sendAvg = 0.00;
                            }else{
                                sendAvg = new BigDecimal(count / realData.size()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            }
                            Map<String, String> sendMaxValueMap = UnitsUtil.getValueMap(String.valueOf(sendMax), "kbps", unit);
                            dto.setSendingFlowMax(sendMaxValueMap.get("value"));
                            Map<String, String> sendAvgValueMap = UnitsUtil.getValueMap(String.valueOf(sendAvg), "kbps", unit);
                            dto.setSendingFlowAvg(sendAvgValueMap.get("value"));
                        }
                    }
                    if(titleName != null && titleName.toString().contains("接收")){
                        if(map.get("realData") != null){
                            List realData = (List) map.get("realData");
                            double acceptMax = 0;
                            double acceptAvg;
                            double count = 0;
                            for (Object realDatum : realData) {
                                if(realDatum != null){
                                    MWItemHistoryDto historyDto = (MWItemHistoryDto) realDatum;
                                    String value = historyDto.getValue();
                                    if(StringUtils.isNotBlank(value)){
                                        double doubleValue = Double.parseDouble(value);
                                        if(acceptMax < doubleValue){
                                            acceptMax = doubleValue;
                                        }
                                        count += doubleValue;
                                    }
                                }
                            }
                            if(count == 0){
                                acceptAvg = 0.00;
                            }else{
                                acceptAvg = new BigDecimal(count / realData.size()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            }
                            Map<String, String> asseptMaxValueMap = UnitsUtil.getValueMap(String.valueOf(acceptMax), "kbps", unit);
                            dto.setAcceptFlowMax(asseptMaxValueMap.get("value"));
                            Map<String, String> asseptAvgValueMap = UnitsUtil.getValueMap(String.valueOf(acceptAvg), "kbps", unit);
                            dto.setAcceptFlowAvg(asseptAvgValueMap.get("value"));
                        }
                    }
                }
            }
        }
        log.info("查询MPLS报表汇总数据计算开始3"+dto);
        return dto;
    }

    /**
     * 分线程执行线路流量报表查询
     * @param itemNames
     */
    private List<LineFlowReportParam> getLineFlowCensus(List<String> itemNames,List<MwTangibleassetsTable> mwTangibleassetsTables, List<LineFlowReportParam> lineFlowReportParams, Long startTime,Long endTime,Integer lineType){
        log.info("线路流量执行999"+mwTangibleassetsTables.size()+itemNames);
        return getlineFlowData(mwTangibleassetsTables, itemNames, new ArrayList<>(), startTime, endTime, lineType);
//        //将同资产同接口的数据进行合并
//        Set<String> idAndNames = new HashSet<>();
//        List<LineFlowReportParam> rets = new ArrayList<>();
//        if(!CollectionUtils.isEmpty(lineFlowReportParams)){
//            for (LineFlowReportParam lineFlowReportParam : lineFlowReportParams) {
//                String assetsId = lineFlowReportParam.getAssetsId();
//                String interfaceName = lineFlowReportParam.getInterfaceName();
//                if(idAndNames.contains(assetsId+interfaceName)){
//                    continue;
//                }
//                for (LineFlowReportParam flowReportParam : lineFlowReportParams) {
//                    String assetsId2 = flowReportParam.getAssetsId();
//                    String interfaceName2 = flowReportParam.getInterfaceName();
//                    if(assetsId.equals(assetsId2) && interfaceName.equals(interfaceName2)){
//                        if(StringUtils.isNotBlank(flowReportParam.getAcceptFlowAvg())){
//                            lineFlowReportParam.setAcceptFlowAvg(flowReportParam.getAcceptFlowAvg());
//                        }
//                        if(StringUtils.isNotBlank(flowReportParam.getAcceptFlowMax())){
//                            lineFlowReportParam.setAcceptFlowMax(flowReportParam.getAcceptFlowMax());
//                        }
//                        if(StringUtils.isNotBlank(flowReportParam.getSendingFlowAvg())){
//                            lineFlowReportParam.setSendingFlowAvg(flowReportParam.getSendingFlowAvg());
//                        }
//                        if(StringUtils.isNotBlank(flowReportParam.getSendingFlowMax())){
//                            lineFlowReportParam.setSendingFlowMax(flowReportParam.getSendingFlowMax());
//                        }
//                    }
//                }
//                rets.add(lineFlowReportParam);
//                idAndNames.add(assetsId+interfaceName);
//            }
//        }
//        return rets;
    }

    private List<TrendDiskDto> getDiskData(List<String> types,Integer monitorServerId, List<String> assetsIds
            , Map<String,MwTangibleassetsTable> dtoMap, Long startTime, Long endTime) {

        MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppNameList(monitorServerId, assetsIds, "DISK", types, true);
        log.info("开始进行磁盘信息分组" + new Date());
        Map<String, String> itemIdHostIdMap = new HashMap<>();
        Map<String, String> itemIdNameMap = new HashMap<>();
        Map<String, Double> hostIdDiskTotal = new HashMap<>();

        List<String> valueTypeItemIds = new ArrayList<>();
        List<String> diskFreeValueTypeItemIds = new ArrayList<>();

        DiskInfoManage diskInfoManage = new DiskInfoManage();

        Integer valueType = 0;
        Integer diskFreeValueType = 0;
        if (result1.getCode() == 0) {
            JsonNode resultData1 = (JsonNode) result1.getData();
            if (resultData1.size() > 0) {
                for (JsonNode item : resultData1) {
                    String hostid = item.get("hostid").asText();
                    String itemId = item.get("itemid").asText();
                    String itemName = item.get("name").asText();

                    if (diskInfoManage.matchDiskUtilization(itemName)) {
                        valueTypeItemIds.add(itemId);
                        valueType = item.get("value_type").asInt();
                    }

                    if (diskInfoManage.matchDiskFree(itemName)) {
                        diskFreeValueTypeItemIds.add(itemId);
                        diskFreeValueType = item.get("value_type").asInt();
                    }

                    String diskName = diskInfoManage.extractDiskName(itemName);
                    if (diskInfoManage.matchDiskTotal(itemName)) {
                        String key = hostid + diskName;
                        hostIdDiskTotal.put(key, item.get("lastvalue").asDouble());
                        log.info("磁盘使用情况报表4：key为:"+key+"值为:"+item.get("lastvalue").asDouble());
                    }
                    log.info("磁盘使用情况报表5：key为:"+hostid+":"+itemName+"值为:"+item.get("lastvalue").asDouble());
                    itemIdHostIdMap.put(itemId, hostid);
                    itemIdNameMap.put(itemId, diskName);
                }
            }
        }

        Map<String, TrendDiskDto> hostIdTrendDiskDtoMap = new HashMap<>();
        for (String hostId : dtoMap.keySet()) {
            MwTangibleassetsTable mwTangibleassetsTable = dtoMap.get(hostId);
            if(null != mwTangibleassetsTable){
                TrendDiskDto trendDiskDto = TrendDiskDto.builder()
                        .assetsName(mwTangibleassetsTable.getAssetsName())
                        .ipAddress(mwTangibleassetsTable.getInBandIp())
                        .assetsId(mwTangibleassetsTable.getId())
                        .build();
                hostIdTrendDiskDtoMap.put(hostId, trendDiskDto);
            }
        }

        Map<String, TrendDiskDto> resultTrendDiskDtoMap = new HashMap<>();
        List<TrendDiskDto> dtos = new ArrayList<>();
        log.info("开始进行获取历史信息" + new Date());
        Map<String, List<HistoryValueDto>> valueDataMap = new HashMap<>();
        valueDataMap = getZabbixTrendInfo(monitorServerId, valueTypeItemIds, startTime, endTime);
        if(valueDataMap == null){
            log.info("磁盘报表查询历史数据"+valueDataMap);
            MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, valueTypeItemIds, startTime, endTime, valueType);
            valueDataMap = ReportUtil.getValueDataMap(historyRsult);
        }
        for (Map.Entry<String, List<HistoryValueDto>> valueDatatEntry : valueDataMap.entrySet()){
            List<HistoryValueDto> valueData = valueDatatEntry.getValue();
            String itemId = valueDatatEntry.getKey();
            String hostId = itemIdHostIdMap.get(itemId);
            String type = itemIdNameMap.get(itemId);

            if(valueData == null || valueData.size() == 0 || StringUtils.isEmpty(hostId)){
                continue;
            }
            log.info("磁盘使用情况报表，hostID"+hostId+",host值为"+valueData);
            double max = valueData.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
            String maxStr = new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "%";

            double min = valueData.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
            String minStr = new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "%";

            double avg = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
            String avgStr = new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "%";

            String key = hostId + type;
            Double total = hostIdDiskTotal.get(key);
            log.info("磁盘6key为:"+key+"总数为:"+total);
            TrendDiskDto data = hostIdTrendDiskDtoMap.get(hostId);
            TrendDiskDto trendDiskDto = new TrendDiskDto();
            BeanUtils.copyProperties(data, trendDiskDto);
            trendDiskDto.setDiskMaxValue(maxStr);
            trendDiskDto.setDiskMinValue(minStr);
            trendDiskDto.setDiskAvgValue(avgStr);
            if(null != total) {
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(String.valueOf(total)), Units.B.getUnits());
                String valueWithUnits = convertedValue.get("value")+convertedValue.get("units");
                log.info("磁盘使用情况报表7，hostID"+hostId+",host值为"+valueWithUnits);
                if(convertedValue != null){
                    trendDiskDto.setDiskTotal(convertedValue.get("value")+convertedValue.get("units"));
                }else{
                    trendDiskDto.setDiskTotal(UnitsUtil.getValueWithUnits(String.valueOf(total), Units.B.getUnits()));
                }
            }
            trendDiskDto.setTypeName(type);
            dtos.add(trendDiskDto);
            resultTrendDiskDtoMap.put(key, trendDiskDto);
        }
        Map<String,List<HistoryValueDto>> valueData1Map = new HashMap<>();
        valueData1Map = getZabbixTrendInfo(monitorServerId, diskFreeValueTypeItemIds, startTime, endTime);
        if(valueData1Map == null){
            log.info("处理历史信息结束" + new Date());
            MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskFreeValueTypeItemIds, startTime, endTime, diskFreeValueType);
            log.info("获取历史信息结束1" + new Date());
           valueData1Map = ReportUtil.getValueDataMap(historyRsult1);
        }
        for (Map.Entry<String, List<HistoryValueDto>> valueDatatEntry : valueData1Map.entrySet()) {
            List<HistoryValueDto> valueData = valueDatatEntry.getValue();
            String itemId = valueDatatEntry.getKey();
            String hostId = itemIdHostIdMap.get(itemId);
            String type = itemIdNameMap.get(itemId);
            log.info("磁盘使用情况报表2，hostID"+hostId+",host值为"+valueData);
            if (valueData == null || valueData.size() == 0 || StringUtils.isEmpty(hostId)) {
                continue;
            }
            double avgFree = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
            double avgFreeConvert = new BigDecimal(avgFree).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String key = hostId + type;
            TrendDiskDto trendDiskDto = resultTrendDiskDtoMap.get(key);
            if(trendDiskDto == null){continue;}
            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(String.valueOf(avgFreeConvert)), Units.B.getUnits());
            if(convertedValue != null){
                trendDiskDto.setDiskFree(convertedValue.get("value")+convertedValue.get("units"));
            }else{
                trendDiskDto.setDiskFree(UnitsUtil.getValueWithUnits(String.valueOf(avgFreeConvert), Units.B.getUnits()));
            }
        }
        log.info("处理历史信息结束1" + new Date());

        return dtos;
    }

    /**
     * 获取趋势数据信息
     */
    private Map<String, List<HistoryValueDto>> getZabbixTrendInfo(Integer monitorServerId, List<String> itemIds,Long startTime, Long endTime){
        log.info("磁盘报表趋势查询::"+startTime+":::"+endTime);
        log.info("磁盘报表趋势查询items::"+itemIds);
        //判断时间是否大于12小时
        if((endTime - startTime) < (3600*5)){return null;}
        //取趋势数据
        MWZabbixAPIResult result = mwtpServerAPI.trendBatchGet(monitorServerId, itemIds, startTime, endTime);
        log.info("磁盘报表趋势查询2::"+result);
        if(result == null || result.isFail()){return null;}
        Map<String, List<HistoryValueDto>> trendValueDataMap = ReportUtil.getTrendValueDataMap(result);
        log.info("磁盘报表趋势查询3::"+trendValueDataMap);
        return trendValueDataMap;
    }

    /**
     * 根据数据判断是否从数据库取缓存数据
     * @param dateType
     * @param timingType
     */
    private List<LineFlowReportParam> getLinkFlowCensusReportCacheData(Integer dateType,Boolean timingType,TrendParam param) throws ParseException {
        List<LineFlowReportParam> list = new ArrayList<>();
        List<String> ids = param.getIds();
        if(dateType != null && (timingType == null || !timingType)){
            if(dateType != null && dateType == 2){
                //获取最近时间的数据
                list = reportTerraceManageDao.selectLinkFlowData(dateType,ids);
                return list;
            }else{
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String time = "";
                Long startTime = 0l;
                Long endTime = 0l;
                log.info("查询线路流量统计数据库数据"+new Date());
                //判断选择条件是昨天，上周，还是上月
                if(dateType != null && dateType == 1){//昨天
                    //获取昨天的日期
                    List<Date> yesterday = ReportDateUtil.getYesterday();
                    time = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
                    list = reportTerraceManageDao.selectLinkReportDailyData(yesterday.get(0),yesterday.get(1),ids);
                    if(CollectionUtils.isEmpty(list)){
                        return list;
                    }
                    for (LineFlowReportParam lineFlowReportParam : list) {
                        lineFlowReportParam.setTime(time);
                    }
                    return list;
                }
                if(dateType != null && dateType == 5){//上周
                    List<Date> lastWeek = ReportDateUtil.getLastWeek();
                    time = format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1));
                    list = reportTerraceManageDao.selectLinkReportWeeklyData(time,ids);
                    if(CollectionUtils.isEmpty(list)){
                        return list;
                    }
                    for (LineFlowReportParam lineFlowReportParam : list) {
                        lineFlowReportParam.setTime(time);
                    }
                    return list;
                }
                if(dateType != null && dateType == 8){//上月
                    List<Date> lastMonth = ReportDateUtil.getLastMonth();
                    time = format.format(lastMonth.get(0))+"~"+format.format(lastMonth.get(1));
                    list = reportTerraceManageDao.selectLinkReportMonthlyData(time,ids);
                    if(CollectionUtils.isEmpty(list)){
                        return list;
                    }
                    for (LineFlowReportParam lineFlowReportParam : list) {
                        lineFlowReportParam.setTime(time);
                    }
                    return list;
                }
                if(!CollectionUtils.isEmpty(param.getChooseTime())){
                    List<String> chooseTime = param.getChooseTime();
                    startTime = format.parse(chooseTime.get(0)).getTime();
                    endTime = format.parse(chooseTime.get(1)).getTime();
                    time = chooseTime.get(0).substring(0,10)+"~"+ chooseTime.get(1).substring(0,10);
                    list = reportTerraceManageDao.selectLinkReportDailyData(format.parse(chooseTime.get(0).substring(0,10)),format.parse(chooseTime.get(1).substring(0,10)),ids);
                }
                log.info("查询线路流量统计数据库数据结束"+new Date());
                //进行数据计算组合
                if(CollectionUtils.isEmpty(list)){
                    return list;
                }
                List<LineFlowReportParam> realData = MWReportHandlerDataLogic.handleLinkReportData(list, startTime, endTime,time);
                if(CollectionUtils.isEmpty(realData)){
                    return realData;
                }
                for (LineFlowReportParam lineFlowReportParam : realData) {
                    lineFlowReportParam.setTime(time);
                }
                return realData;
            }
        }
        return null;
    }

    /**
     * 根据数据判断是否从数据库取缓存数据
     * @param dateType
     */
//    @SneakyThrows
    private PageInfo getDiskUseReportCacheData(DateTypeEnum dateType, TrendParam param) {
        //查询数据库数据
        Integer pageNumber = param.getPageNumber();
        Integer pageSize = param.getPageSize();
        List<String> ids = param.getIds();
        String time = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<TrendDiskDto> list = new ArrayList<>();
        PageInfo pageInfo = null;
        Long startTime = 0l;
        Long endTime = 0l;
        String dateRegion = getDateRegion(param.getDateType(), param.getChooseTime());
        switch (dateType){
            case YESTERDAY://昨天
                List<Date> yesterday = ReportDateUtil.getYesterday();
                PageHelper.startPage(pageNumber, pageSize);
                list = reportTerraceManageDao.selectDiskUseReportDailyData(yesterday.get(0), yesterday.get(1), ids);
                if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(dateRegion)){
                    list.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                return pageInfo;
            case LAST_WEEK://上周
                List<Date> lastWeek = ReportDateUtil.getLastWeek();
                time = format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1));
                PageHelper.startPage(pageNumber, pageSize);
                list = reportTerraceManageDao.selectDiskUseReportWeeklyData(time, ids);
                if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(dateRegion)){
                    list.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                return pageInfo;
            case LAST_MONTH://上月
                List<Date> lastMonth = ReportDateUtil.getLastMonth();
                time = format.format(lastMonth.get(0))+"~"+format.format(lastMonth.get(1));
                PageHelper.startPage(pageNumber, pageSize);
                list = reportTerraceManageDao.selectDiskUseReportMonthlyData(time, ids);
                if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(dateRegion)){
                    list.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                return pageInfo;
            case SET_DATE://自定义
                try {
                    List<String> chooseTime = param.getChooseTime();
                    startTime = format.parse(chooseTime.get(0)).getTime();
                    endTime = format.parse(chooseTime.get(1)).getTime();
                    list = reportTerraceManageDao.selectDiskUseReportDailyData(format.parse(chooseTime.get(0).substring(0,10)),format.parse(chooseTime.get(1).substring(0,10)), ids);
                    break;
                }catch (Exception e){
                    log.error("磁盘使用情况报表查询自定义失败"+e);
                    break;
                }
            default:
                pageInfo.setList(list);
                return pageInfo;
        }
        if(CollectionUtils.isEmpty(list)){
            pageInfo = new PageInfo<>(list);
            pageInfo.setPageSize(list.size());
            pageInfo.setList(list);
            return pageInfo;
        }
        //代码走到这里，说明是自定义查询，需要处理数据逻辑
        List<TrendDiskDto> trendDiskDtos = MWReportHandlerDataLogic.handleDiskUseReportData(list, startTime, endTime);
        //进行数据分页处理
        if(CollectionUtils.isEmpty(trendDiskDtos)){
            pageInfo.setList(trendDiskDtos);
            return pageInfo;
        }
        int fromIndex = pageSize * (pageNumber -1);
        int toIndex = pageSize * pageNumber;
        if(toIndex > trendDiskDtos.size()){
            toIndex = trendDiskDtos.size();
        }
        List<TrendDiskDto> realData = trendDiskDtos.subList(fromIndex, toIndex);
        if(!CollectionUtils.isEmpty(realData) && StringUtils.isNotBlank(dateRegion)){
            realData.forEach(data->{
                data.setTime(dateRegion);
            });
        }
        PageInfo info = new PageInfo<>(realData);
        info.setTotal(trendDiskDtos.size());
        info.setList(realData);
        return info;
    }


    /**
     * 根据数据判断是否从数据库取缓存数据
     * @param dataType
     * @param timingType
     */
    private PageInfo getAssetsUsabilityReportCacheData(Integer dataType,Boolean timingType,RunTimeQueryParam param) throws ParseException {
        List<MwAssetsUsabilityParam> list = new ArrayList<>();
        DateTypeEnum dateTypeEnum = DateTypeEnum.getDateTypeEnum(dataType);
        Integer pageNumber = param.getPageNumber();
        Integer pageSize = param.getPageSize();
        List<String> ids = param.getIds();
        String time = "";
        Long startTime = 0l;
        Long endTime = 0l;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PageInfo pageInfo = null;
        String dateRegion = getDateRegion(param.getDateType(), param.getChooseTime());
        switch (dateTypeEnum){
            case YESTERDAY://昨天
                List<Date> yesterday = ReportDateUtil.getYesterday();
                time = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
                PageHelper.startPage(pageNumber,pageSize);
                list = reportTerraceManageDao.selectAssetsUsabilityDailyData(yesterday.get(0),yesterday.get(1),ids);
                if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(dateRegion)){
                    list.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                pageInfo = new PageInfo<>(list);
                pageInfo.setPageSize(list.size());
                pageInfo.setList(list);
                return pageInfo;
            case LAST_WEEK://上周
                List<Date> lastWeek = ReportDateUtil.getLastWeek();
                time = format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1));
                PageHelper.startPage(pageNumber,pageSize);
                list = reportTerraceManageDao.selectAssetsUsabilityWeeklyData(time,ids);
                if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(dateRegion)){
                    list.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                return pageInfo;
            case LAST_MONTH://上月
                List<Date> lastMonth = ReportDateUtil.getLastMonth();
                time = format.format(lastMonth.get(0))+"~"+format.format(lastMonth.get(1));
                PageHelper.startPage(pageNumber,pageSize);
                list = reportTerraceManageDao.selectAssetsUsabilityMonthlyData(time,ids);
                if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(dateRegion)){
                    list.forEach(data->{
                        data.setTime(dateRegion);
                    });
                }
                pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                return pageInfo;
            case SET_DATE://自定义
                List<String> chooseTime = param.getChooseTime();
                startTime = format.parse(chooseTime.get(0)).getTime();
                endTime = format.parse(chooseTime.get(1)).getTime();
                list = reportTerraceManageDao.selectAssetsUsabilityDailyData(format.parse(chooseTime.get(0).substring(0,10)),format.parse(chooseTime.get(1).substring(0,10)),ids);
        }
        if(CollectionUtils.isEmpty(list)){
            pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return pageInfo;
        }
        List<MwAssetsUsabilityParam> assetsUsabilityParamList = MWReportHandlerDataLogic.handleAssetUsabilityReportData(list, startTime, endTime);
        if(!CollectionUtils.isEmpty(assetsUsabilityParamList) && StringUtils.isNotBlank(dateRegion)){
            assetsUsabilityParamList.forEach(data->{
                data.setTime(dateRegion);
            });
        }
        int fromIndex = pageSize * (pageNumber -1);
        int toIndex = pageSize * pageNumber;
        if(toIndex > assetsUsabilityParamList.size()){
            toIndex = assetsUsabilityParamList.size();
        }
        List<MwAssetsUsabilityParam> realData = assetsUsabilityParamList.subList(fromIndex, toIndex);
        PageInfo info = new PageInfo<>(realData);
        info.setTotal(assetsUsabilityParamList.size());
        info.setList(realData);
        return info;
    }


    private String getDateRegion(Integer dateType,List<String> choosTime){
        if(!CollectionUtils.isEmpty(choosTime)){
            return choosTime.get(0).substring(0,10)+"~"+choosTime.get(1).substring(0,10);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if(dateType != null && dateType == 2){//今天
            List<Date> today = ReportDateUtil.getToday();
            return format.format(today.get(0))+"~"+format.format(today.get(1));
        }
        if(dateType != null && dateType == 1){//昨天
            List<Date> yesterday = ReportDateUtil.getYesterday();
            return format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
        }
        if(dateType != null && dateType == 5){//上周
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            return format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1));
        }
        if(dateType != null && dateType == 8){//上月
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            return format.format(lastMonth.get(0))+"~"+format.format(lastMonth.get(1));
        }
        return "";
    }

    /**
     * 查询定时任务下拉框
     * @return
     */
    @Override
    public Reply selectReportDown() {
        List<Map<String, Object>> maps = reportTerraceManageDao.selectReportDropDown();
        return Reply.ok(maps);
    }

    @Autowired
    private MwManualTimeTaskRun timeToo;

    @Override
    public Reply manualRunTimeTask(Integer reportId) {
        try {
            if(reportId == null)Reply.fail("请选择需要执行的定时任务");
            switch (reportId){
                case 1://流量统计报表每天
                    timeToo.manualLinkReportDailyDataCache();
                    break;
                case 2://流量统计报表上周
                    timeToo.manualLinkReportWeeklyDataCache();
                    break;
                case 3://流量统计报表上月
                    timeToo.manualLinkReportMonthlyDataCache();
                    break;
                case 4://磁盘使用情况报表每天
                    timeToo.manualDiskUseReportDailyDataCache();
                    break;
                case 5://磁盘使用情况报表上周
                    timeToo.manualDiskUseReportWeeklyDataCache();
                    break;
                case 6://磁盘使用情况报表上月
                    timeToo.manualDiskUseReportMonthlyDataCache();
                    break;
                case 7://资产可用性报表每天
                    timeToo.manualAssetUsabilityReportDailyDataCache();
                    break;
                case 8://资产可用性报表上周
                    timeToo.manualAssetUsabilityReportWeeklyDataCache();
                    break;
                case 9://资产可用性报表上月
                    timeToo.manualAssetUsabilityReportMonthlyDataCache();
                    break;
                case 10://MPLS报表每天
                    timeToo.manualMplsHistoryReportDailyDataCache();
                    break;
                case 11://MPLS报表上周
                    timeToo.manualMplsHistoryReportWeeklyDataCache();
                    break;
                case 12://MPLS报表上月
                    timeToo.manualMplsHistoryReportMonthlyDataCache();
                    break;
                case 13://运行状态报表每天
                    timeToo.manualRunStateReportDailyDataCache();
                    break;
                case 14://运行状态报表上周
                    timeToo.manualRunStateReportWeeklyDataCache();
                    break;
                case 15://运行状态报表上月
                    timeToo.manualRunStateReportMonthlyDataCache();
                    break;
                case 16://CPU报表每天
                    timeToo.manualCpuAndMemoryReportDailyDataCache();
                    break;
                case 17://CPU报表上周
                    timeToo.manualCpuAndMemoryReportWeeklyDataCache();
                    break;
                case 18://CPU报表上月
                    timeToo.manualCpuAndMemoryReportMonthlyDataCache();
                    break;
            }
            return Reply.ok("手动执行定时任务成功");
        } catch (Throwable e) {
            log.error("手动执行定时任务失败", e);
            return Reply.fail("手动执行定时任务失败");
        }
    }

    /**
     * 查询资产接口数据
     * @return
     */
    public List<AssetsTreeDTO> selectAseestsInterface() {
        List<AssetsTreeDTO> interfaceDtos = new ArrayList<>();
        //查询所有资产
        QueryTangAssetsParam browseTangAssetsParam = new QueryTangAssetsParam();
        List<MwTangibleassetsTable> mwTangibleassetsTables = selectList(browseTangAssetsParam);
        if(CollectionUtils.isEmpty(mwTangibleassetsTables))return interfaceDtos;
        Map<Integer,List<String>> serverMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
            Integer monitorServerId = mwTangibleassetsTable.getMonitorServerId();
            String assetsId = mwTangibleassetsTable.getAssetsId();
            if(serverMap.containsKey(monitorServerId)){
                List<String> hostIds = serverMap.get(monitorServerId);
                hostIds.add(assetsId);
                serverMap.put(monitorServerId,hostIds);
            }else{
                List<String> hostIds = new ArrayList<>();
                hostIds.add(assetsId);
                serverMap.put(monitorServerId,hostIds);
            }
        }
        if(serverMap.isEmpty())return interfaceDtos;
        Map<String,List<String>> assetsMap = new HashMap<>();
        for (Integer serverId : serverMap.keySet()) {
            List<String> hostIds = serverMap.get(serverId);
            List<String> types = new ArrayList<>();
            types.add("INTERFACE_DESCR");
            MWZabbixAPIResult result = mwtpServerAPI.getItemDataByAppNameList(serverId, hostIds, "INTERFACES", types);
            JsonNode resultData = (JsonNode) result.getData();
            if (result.getCode() == 0 && resultData.size() > 0){
                resultData.forEach(item -> {
                    String name = item.get("name").asText();
                    String hostid = item.get("hostid").asText();
                    if (name.indexOf("[") != -1) {
                        name = name.substring(name.indexOf("[") + 1, name.indexOf("]"));
                        if(StringUtils.isNotBlank(hostid) && StringUtils.isNotBlank(name)){
                            if(assetsMap.containsKey(hostid)){
                                List<String> names = assetsMap.get(hostid);
                                names.add(name);
                                assetsMap.put(hostid,names);
                            }else{
                                List<String> names = new ArrayList<>();
                                names.add(name);
                                assetsMap.put(hostid,names);
                            }
                        }
                    }
                });
            }
        }
        if(assetsMap.isEmpty())return interfaceDtos;
        for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
            String assetsName = mwTangibleassetsTable.getAssetsName();
            String assetsId = mwTangibleassetsTable.getAssetsId();
            String id = mwTangibleassetsTable.getId();
            if(assetsMap.get(assetsId) != null){
                List<String> names = assetsMap.get(assetsId);
                AssetsTreeDTO dto = new AssetsTreeDTO();
                dto.setTypeName(assetsName);
                List<AssetsTreeDTO> cdtos = new ArrayList<>();
                for (String name : names) {
                    AssetsTreeDTO cdto = new AssetsTreeDTO();
                    cdto.setTypeName(name);
                    AssetsDTO ao = new AssetsDTO();
                    ao.setId(id);
                    ao.setAssetsName(assetsName);
                    ao.setInterFaceName(name);
                    List<AssetsDTO> list = new ArrayList<>();
                    list.add(ao);
                    cdto.setAssetsList(list);
                    cdtos.add(cdto);
                }
                dto.setChildren(cdtos);
                AssetsDTO ao = new AssetsDTO();
                ao.setId(id);
                ao.setAssetsName(assetsName);
                List<AssetsDTO> list = new ArrayList<>();
                list.add(ao);
                dto.setAssetsList(list);
                interfaceDtos.add(dto);
            }
        }
        return interfaceDtos;
    }


    /**
     *查询蓝月亮流量数据
     * @param
     * @return
     */
    @Override
    public Reply selectLylLinkFlowData(TrendParam trendParam) {
        try {
            Integer pageNumber = trendParam.getPageNumber();
            Integer pageSize = trendParam.getPageSize();
            List<LineFlowReportParam> datas = new ArrayList<>();
            //获取日期区间
            Integer dateType = trendParam.getDateType();
            if(trendParam.getTimingType() == null || !trendParam.getTimingType()){
                //根据时间判断取数来源
                List<String> ids = trendParam.getIds();
                List<String> interFaceNames = trendParam.getInterFaceNames();
                List<String> names = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(interFaceNames)){
                    for (String interFaceName : interFaceNames) {
                        if(StringUtils.isNotBlank(interFaceName)){
                            names.add(interFaceName);
                        }
                    }
                }
                List<LineFlowReportParam> dataBaseList = new ArrayList<>();
                if(dateType != null && dateType == 2){
                    //获取最近时间的数据
                    dataBaseList = reportTerraceManageDao.selectLylLinkFlowData(dateType,ids,names);
                    if(CollectionUtils.isNotEmpty(dataBaseList)){
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        for (LineFlowReportParam param : dataBaseList) {
                            param.setTime(format.format(new Date()));
                        }
                    }
                }else{
                    List<Long> longs = calculitionTime(trendParam.getDateType(), trendParam.getChooseTime());
                    Long startTime = longs.get(0);
                    Long endTime = longs.get(1);
                    Date d1 = new Date();
                    d1.setTime(startTime*1000);
                    Date d2 = new Date();
                    d2.setTime(endTime*1000);
                    dataBaseList = reportTerraceManageDao.selectLylLinkReportDailyData(d1,d2,ids,names);
                }
                List<LineFlowReportParam> realDatas = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(dataBaseList)){
                    //按照接口排序
                    Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                    Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                    realDatas = dataBaseList.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getAssetsName()+o1.getInterfaceName()+o1.getTime()), pinyin4jUtil.getStringPinYin(o2.getAssetsName()+o2.getInterfaceName()+o2.getTime()))).collect(Collectors.toList());

                }
                if(!CollectionUtils.isEmpty(realDatas)){
                    int fromIndex = pageSize * (pageNumber -1);
                    int toIndex = pageSize * pageNumber;
                    if(toIndex > dataBaseList.size()){
                        toIndex = dataBaseList.size();
                    }
                    List<LineFlowReportParam> list = realDatas.subList(fromIndex, toIndex);
                    PageInfo pageInfo = new PageInfo<>(list);
                    pageInfo.setTotal(realDatas.size());
                    pageInfo.setList(list);
                    return Reply.ok(pageInfo);
                }else{
                    PageInfo pageInfo = new PageInfo<>();
                    return Reply.ok(pageInfo);
                }
            }
        }catch (Exception e){
            log.error("查询蓝月亮流量报表数据"+e.getMessage());
        }
        return null;
    }

    @Override
    public TimeTaskRresult manualRunTimeTaskTwo(String id) {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try {
            Integer reportId = Integer.parseInt(id);
            if(reportId == null)Reply.fail("请选择需要执行的定时任务");
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            switch (reportId){
                case 13://流量统计报表每天
                    timeToo.manualLinkReportDailyDataCache();
                    timeToo.manualLinkReportWeeklyDataCache();
                    timeToo.manualLinkReportMonthlyDataCache();
                    taskRresult.setResultContext("流量统计报表每天:成功");
                    break;
                case 10://磁盘使用情况报表每天
                    timeToo.manualDiskUseReportDailyDataCache();
                    timeToo.manualDiskUseReportWeeklyDataCache();
                    timeToo.manualDiskUseReportMonthlyDataCache();
                    taskRresult.setResultContext("磁盘使用情况报表每天:成功");
                    break;
                case 12://资产可用性报表每天
                    timeToo.manualAssetUsabilityReportDailyDataCache();
                    timeToo.manualAssetUsabilityReportWeeklyDataCache();
                    timeToo.manualAssetUsabilityReportMonthlyDataCache();
                    taskRresult.setResultContext("资产可用性报表每天:成功");
                case 21://MPLS报表每天
                    timeToo.manualMplsHistoryReportDailyDataCache();
                    timeToo.manualMplsHistoryReportWeeklyDataCache();
                    timeToo.manualMplsHistoryReportMonthlyDataCache();
                    taskRresult.setResultContext("MPLS报表每天:成功");
                    break;
                case 7://运行状态报表每天
                    timeToo.manualRunStateReportDailyDataCache();
                    timeToo.manualRunStateReportWeeklyDataCache();
                    timeToo.manualRunStateReportMonthlyDataCache();
                    taskRresult.setResultContext("运行状态报表每天:成功");
                    break;
                case 9://CPU报表每天
                    timeToo.manualCpuAndMemoryReportDailyDataCache();
                    timeToo.manualCpuAndMemoryReportWeeklyDataCache();
                    timeToo.manualCpuAndMemoryReportMonthlyDataCache();
                    taskRresult.setResultContext("CPU报表每天:成功");
                    break;
                case 23://CPU报表每天（蓝月亮）
                    timeToo.manualCpuAndMemoryReportDailyDataCache();
                    timeToo.manualCpuAndMemoryReportWeeklyDataCache();
                    timeToo.manualCpuAndMemoryReportMonthlyDataCache();
                    taskRresult.setResultContext("CPU报表每天（蓝月亮）:成功");
                    break;
                case 24://流量统计报表每天（蓝月亮）
                    timeToo.manualLinkReportDailyDataCache();
                    timeToo.manualLinkReportWeeklyDataCache();
                    timeToo.manualLinkReportMonthlyDataCache();
                    taskRresult.setResultContext("流量统计报表每天（蓝月亮）:成功");
                    break;
                default:
                    taskRresult.setSuccess(false);
                    taskRresult.setResultContext("此类报表未收录定时任务:失败");
                    break;
            }
            return taskRresult;
        } catch (Throwable e) {
            log.error("手动执行定时任务失败", e);
            taskRresult.setResultContext(e.toString());
            taskRresult.setSuccess(false);
            return taskRresult;
        }
    }

    @Override
    public TimeTaskRresult manualRunTimeTaskThere(String id) {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try {
            Integer reportId = Integer.parseInt(id);
            if(reportId == null)Reply.fail("请选择需要执行的定时任务");
            taskRresult.setSuccess(true);
            switch (reportId){
                case 13://流量统计报表每天
                    getToDayLInkFlowReportDataCache();
                    taskRresult.setResultContext("流量统计报表每天:成功");
                    break;
                case 12://资产可用性报表每天
                    getAssetsAbblie();
                    taskRresult.setResultContext("流量统计报表每天:成功");
                case 7://运行状态报表每天
                    getRedisRunTime();
                    taskRresult.setResultContext("流量统计报表每天:成功");
                    break;
                default:
                    taskRresult.setSuccess(false);
                    taskRresult.setResultContext("此类报表未收录定时任务:失败");
                    break;
            }
            return taskRresult;
        } catch (Throwable e) {
            log.error("手动执行定时任务失败", e);
            taskRresult.setResultContext(e.toString());
            taskRresult.setSuccess(false);
            return taskRresult;
        }
    }

    public void getRedisRunTime(){
        log.info("运行报表存放缓存"+new Date());
        try {
            if (!getRedisRunTime){
                return;
            }

            do {
                if (waitFor){
                    Thread.currentThread().sleep(60000);
                }
            }while (waitFor);
            waitFor = true;
            RunTimeQueryParam param = new RunTimeQueryParam();
            param.setDateType(DateTimeTypeEnum.TODAY.getCode());
            param.setDataSize(5);
            mwReportService.getRunTimeItemOptimizeUtilization(param, false, false);
            waitFor = false;
        }catch (Exception e){
            waitFor = false;
            log.error("运行报表存放缓存"+e.getMessage());
        }
    }

    public void getAssetsAbblie(){

        try {
            if (!getAssetsAbblie){
                return;
            }
            RunTimeQueryParam param = new RunTimeQueryParam();
            param.setDateType(2);
            Reply reply = terraceManageService.selectReportAssetsUsability(param,false);
        }catch (Exception e){
            log.error("资产可用性定时任务报表错误"+e.getMessage());
        }
    }

    //@Scheduled(cron = "0 */60 * * * ?")
    public void getToDayLInkFlowReportDataCache() {
        //数据进行数据库缓存
        log.info("开始进行线路流量统计报表定时缓存数据" + new Date());
        try {
            log.info("删除原有线路流量缓存数据" + new Date());
            terraceManageDao.deleteLinkFlowCacheData(2);
            List<LineFlowReportParam> cacheDatas = new ArrayList<>();
            TrendParam param = new TrendParam();
            param.setDateType(2);
            param.setTimingType(true);
            param.setPageNumber(0);
            param.setPageSize(100000);
            //获取线路流量今天的数据
            log.info("查询zabbix服务器中线路流量数据,类型：" + param.getDayType() + new Date());
            Reply reply = terraceManageService.selectReportLinkNews(param);
            if (reply != null && reply.getData() != null) {
                //线路流量数据
                List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
                if (!CollectionUtils.isEmpty(retDatas)) {
                    for (LineFlowReportParam retData : retDatas) {
                        retData.setType(param.getDateType());
                        retData.setTime(format.format(new Date())+"~"+format.format(new Date()));

                    }
                }
                if (!CollectionUtils.isEmpty(retDatas)) {
                    cacheDatas.addAll(retDatas);
                }
            }
            log.info("查询zabbix服务器中线路流量数据结束,类型：" + param.getDateType() + new Date());
            if (!CollectionUtils.isEmpty(cacheDatas)) {
                //数据进行数据库缓存
                if(!CollectionUtils.isEmpty(cacheDatas)){
                    Iterator<LineFlowReportParam> iterator = cacheDatas.iterator();
                    List<LineFlowReportParam> newList = new ArrayList<>();
                    while(iterator.hasNext()){
                        LineFlowReportParam next = iterator.next();
                        newList.add(next);
                        if(newList.size() == 100){
                            terraceManageDao.saveLinkFlowCacheData(newList);
                            newList.clear();
                        }
                    }
                    if(!CollectionUtils.isEmpty(newList)){
                        terraceManageDao.saveLinkFlowCacheData(newList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("定时缓存线路流量统计报表数据失败" + e.getMessage());
        }
    }

    /**
     * 获取资源中心树结构信息
     */
    private Map<String,List<AssetsTreeDTO>> getNewAssetsTreeInfo(){
        Map<String,List<AssetsTreeDTO>> treeMap = new HashMap<>();
        List<MwTangibleassetsTable> tangibleassetsTables = selectList(new QueryTangAssetsParam());
        if(CollectionUtils.isEmpty(tangibleassetsTables)){return treeMap;}
        //品牌分组
        manufacturerGroup(tangibleassetsTables,treeMap);
        //资产类型分组
        assetsTypeGroup(tangibleassetsTables,treeMap);
        //机构分组
        orgGroup(tangibleassetsTables,treeMap);
        //业务系统分组
        modelSystemGroup(tangibleassetsTables,treeMap);
        return treeMap;
    }

    private void modelSystemGroup(List<MwTangibleassetsTable> tangibleassetsTables, Map<String,List<AssetsTreeDTO>> treeMap){
        Map<String, List<MwTangibleassetsTable>> modelSystemMap = tangibleassetsTables.stream().filter(item -> StringUtils.isNotBlank(item.getModelSystem())).collect(Collectors.groupingBy(item -> item.getModelSystem()));
        List<AssetsTreeDTO> treeDTOS = new ArrayList<>();
        for (Map.Entry<String, List<MwTangibleassetsTable>> entry : modelSystemMap.entrySet()) {
            List<MwTangibleassetsTable> value = entry.getValue();
            //业务分类分组
            Map<String, List<MwTangibleassetsTable>> modelClassifyMap = value.stream().filter(item -> StringUtils.isNotBlank(item.getModelClassify())).collect(Collectors.groupingBy(item -> item.getModelClassify()));
            List<AssetsTreeDTO> childrenTreeDtos = new ArrayList<>();
            for (Map.Entry<String, List<MwTangibleassetsTable>> classifyEntry : modelClassifyMap.entrySet()) {
                List<MwTangibleassetsTable> classifyDtos = classifyEntry.getValue();
                AssetsTreeDTO treeDTO = new AssetsTreeDTO();
                treeDTO.setTypeName(classifyEntry.getKey());
                treeDTO.setUuid(UUID.randomUUID().toString().replace("-",""));
                List<AssetsDTO> assetsList = new ArrayList<>();
                for (MwTangibleassetsTable table : classifyDtos) {
                    AssetsDTO assetsDTO = new AssetsDTO();
                    assetsDTO.extractFrom(table);
                    assetsList.add(assetsDTO);
                }
                treeDTO.setAssetsList(assetsList);
                childrenTreeDtos.add(treeDTO);
            }
            AssetsTreeDTO treeDTO = new AssetsTreeDTO();
            treeDTO.setTypeName(entry.getKey());
            treeDTO.setUuid(UUID.randomUUID().toString().replace("-",""));
            List<AssetsDTO> assetsList = new ArrayList<>();
            for (MwTangibleassetsTable table : value) {
                AssetsDTO assetsDTO = new AssetsDTO();
                assetsDTO.extractFrom(table);
                assetsList.add(assetsDTO);
            }
            treeDTO.setAssetsList(assetsList);
            treeDTO.setChildren(childrenTreeDtos);
            treeDTOS.add(treeDTO);
        }
        treeMap.put(ReportConstant.assetsBusinessSystem,treeDTOS);
    }


    private void orgGroup(List<MwTangibleassetsTable> tangibleassetsTables, Map<String,List<AssetsTreeDTO>> treeMap){
        Map<Integer,List<AssetsDTO>> assetsMap = new HashMap<>();
        for (MwTangibleassetsTable tangibleassetsTable : tangibleassetsTables) {
            List<List<Integer>> modelViewOrgIds = tangibleassetsTable.getModelViewOrgIds();
            if(CollectionUtils.isEmpty(modelViewOrgIds)){continue;}
            for (List<Integer> modelViewOrgId : modelViewOrgIds) {
                for (Integer orgId : modelViewOrgId) {
                    List<AssetsDTO> assetsDTOS = assetsMap.get(orgId);
                    if(assetsDTOS == null){
                        assetsDTOS = new ArrayList<>();
                        AssetsDTO assetsDTO = new AssetsDTO();
                        assetsDTO.extractFrom(tangibleassetsTable);
                        assetsDTOS.add(assetsDTO);
                        assetsMap.put(orgId,assetsDTOS);
                        continue;
                    }
                    AssetsDTO assetsDTO = new AssetsDTO();
                    assetsDTO.extractFrom(tangibleassetsTable);
                    assetsDTOS.add(assetsDTO);
                }
            }
        }
        //查询机构
        List<MWOrgDTO> allOrgList = orgService.getAllOrgList();
        List<AssetsTreeDTO> treeDTOList = new ArrayList<>();
        for (MWOrgDTO mwOrgDTO : allOrgList) {
            AssetsTreeDTO assetsTreeDTO = new AssetsTreeDTO();
            List<AssetsDTO> assetsDTOS = assetsMap.get(mwOrgDTO.getOrgId());
            if(CollectionUtils.isEmpty(assetsDTOS)){continue;}
            assetsTreeDTO.setAssetsList(assetsDTOS);
            assetsTreeDTO.setTypeName(mwOrgDTO.getOrgName());
            assetsTreeDTO.setUuid(UUID.randomUUID().toString().replace("-",""));
            List<MWOrgDTO> childs = mwOrgDTO.getChilds();
            List<AssetsTreeDTO> childrens = new ArrayList<>();
            getChildren(childrens,assetsMap,childs);
            assetsTreeDTO.setChildren(childrens);
            treeDTOList.add(assetsTreeDTO);
        }
        treeMap.put(ReportConstant.assetsOrg,treeDTOList);
    }



    private void getChildren(List<AssetsTreeDTO> assetsTreeDTOs,Map<Integer,List<AssetsDTO>> assetsMap, List<MWOrgDTO> orgList){
        for (MWOrgDTO mwOrgDTO : orgList) {
            AssetsTreeDTO childrenDto = new AssetsTreeDTO();
            List<AssetsDTO> assetsDTOS = assetsMap.get(mwOrgDTO.getOrgId());
            if(CollectionUtils.isEmpty(assetsDTOS)){continue;}
            childrenDto.setAssetsList(assetsDTOS);
            childrenDto.setTypeName(mwOrgDTO.getOrgName());
            childrenDto.setUuid(UUID.randomUUID().toString().replace("-",""));
            assetsTreeDTOs.add(childrenDto);
        }
    }

    private void manufacturerGroup(List<MwTangibleassetsTable> tangibleassetsTables, Map<String,List<AssetsTreeDTO>> treeMap){
        Map<String, List<MwTangibleassetsTable>> listMap = tangibleassetsTables.stream().filter(item -> item.getManufacturer() != null && !"".equals(item.getManufacturer())).collect(Collectors.groupingBy(item -> item.getManufacturer()));
        List<AssetsTreeDTO> treeDTOList = new ArrayList<>();
        for (String manufacturer : listMap.keySet()) {
            AssetsTreeDTO treeDTO = new AssetsTreeDTO();
            treeDTO.setTypeName(manufacturer);
            treeDTO.setUuid(UUID.randomUUID().toString().replace("-",""));
            List<MwTangibleassetsTable> tables = listMap.get(manufacturer);
            List<AssetsDTO> assetsList = new ArrayList<>();
            for (MwTangibleassetsTable table : tables) {
                AssetsDTO assetsDTO = new AssetsDTO();
                assetsDTO.extractFrom(table);
                assetsList.add(assetsDTO);
            }
            treeDTO.setAssetsList(assetsList);
            treeDTOList.add(treeDTO);
        }
        treeMap.put(ReportConstant.manufacturer,treeDTOList);
    }

    private void assetsTypeGroup(List<MwTangibleassetsTable> tangibleassetsTables, Map<String,List<AssetsTreeDTO>> treeMap){
        Map<String, List<MwTangibleassetsTable>> listMap = tangibleassetsTables.stream().filter(item -> item.getAssetsTypeName() != null && !"".equals(item.getAssetsTypeName())).collect(Collectors.groupingBy(item -> item.getAssetsTypeName()));
        List<AssetsTreeDTO> treeDTOList = new ArrayList<>();
        for (String typeName : listMap.keySet()) {
            AssetsTreeDTO treeDTO = new AssetsTreeDTO();
            treeDTO.setTypeName(typeName);
            treeDTO.setUuid(UUID.randomUUID().toString().replace("-",""));
            List<MwTangibleassetsTable> tables = listMap.get(typeName);
            Map<String, List<MwTangibleassetsTable>> subTypeMap = tangibleassetsTables.stream().filter(item -> item.getAssetsTypeSubName() != null && !"".equals(item.getAssetsTypeSubName())).collect(Collectors.groupingBy(item -> item.getAssetsTypeSubName()));
            List<AssetsTreeDTO> childrens = new ArrayList<>();
            for (String subType : subTypeMap.keySet()) {
                AssetsTreeDTO childrenTreeDto = new AssetsTreeDTO();
                childrenTreeDto.setTypeName(subType);
                childrenTreeDto.setUuid(UUID.randomUUID().toString().replace("-",""));
                List<MwTangibleassetsTable> subDtos = subTypeMap.get(subType);
                List<AssetsDTO> assetsList = new ArrayList<>();
                for (MwTangibleassetsTable table : subDtos) {
                    AssetsDTO assetsDTO = new AssetsDTO();
                    assetsDTO.extractFrom(table);
                    assetsList.add(assetsDTO);
                }
                childrenTreeDto.setAssetsList(assetsList);
                childrens.add(childrenTreeDto);
            }
            List<AssetsDTO> assetsList = new ArrayList<>();
            for (MwTangibleassetsTable table : tables) {
                AssetsDTO assetsDTO = new AssetsDTO();
                assetsDTO.extractFrom(table);
                assetsList.add(assetsDTO);
            }
            treeDTO.setAssetsList(assetsList);
            treeDTO.setChildren(childrens);
            treeDTOList.add(treeDTO);
        }
        treeMap.put(ReportConstant.assetsType,treeDTOList);
    }
}
