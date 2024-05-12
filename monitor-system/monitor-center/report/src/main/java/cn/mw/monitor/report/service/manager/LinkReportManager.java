package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.constant.ReportConstant;
import cn.mw.monitor.report.util.MwReportDateUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.link.dao.MWNetWorkLinkDao;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.linkdto.*;
import cn.mw.monitor.report.param.ReportBase;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/12/28 9:38
 */
@Component
@Slf4j
public class LinkReportManager {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/report/LinkReportManager");

    @Resource
    private MwReportDao mwReportDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Resource
    private MWNetWorkLinkDao mwNetWorkLinkDao;

    @Autowired
    private MWNetWorkLinkService mwNetWorkLinkService;

    @Value("${linkreport.group.count}")
    private int groupCount;

    public List<LinkHistoryDto> selectLinkHistory(TrendParam param) {
        List<LinkHistoryDto> list = new ArrayList<>();
        List<MwHistoryDto> inMwHistoryDTOS = new ArrayList<>();
        List<MwHistoryDto> outMwHistoryDTOS = new ArrayList<>();
        /**
         * 非高级查询
         */
        if (!param.getSeniorchecked()) {
            String tableName = "";
            if (null != param.getDayType()) {
                tableName = setTableName(param, tableName);
                LinkHistoryParam linkHistoryParam = LinkHistoryParam.builder().interfaceID(param.getInterfaceID()).tableName(tableName).build();
                if (param.getFixedDate().size() > 0) {
                    linkHistoryParam.setStartTime(param.getFixedDate().get(0));
                    linkHistoryParam.setEndTime(param.getFixedDate().get(1));
                }
                if (param.getValueType().equals("MAX")) {
                    linkHistoryParam.setInColumn("in_max_bps");
                    linkHistoryParam.setOutColumn("out_max_bps");
                } else if (param.getValueType().equals("MIN")) {
                    linkHistoryParam.setInColumn("in_min_bps");
                    linkHistoryParam.setOutColumn("out_min_bps");
                } else {
                    linkHistoryParam.setInColumn("in_average_bps");
                    linkHistoryParam.setOutColumn("out_average_bps");
                }

                inMwHistoryDTOS = mwReportDao.selectInHistoryList(linkHistoryParam);
                outMwHistoryDTOS = mwReportDao.selectOutHistoryList(linkHistoryParam);
            }
        } else {
            if (null != param.getChooseTime() && param.getChooseTime().size() > 0) {
                List<String> chooseTime = param.getChooseTime();
                Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
                Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
                String port = "";
                String bandHostid = "";
                Integer bandServerId = 0;
                NetWorkLinkDto netWorkLinkDto = mwNetWorkLinkDao.selectById(param.getInterfaceID());
                if (StringUtils.isNotBlank(param.getRootPort())) {
                    bandHostid = netWorkLinkDto.getRootAssetsParam().getAssetsId();
                    bandServerId = netWorkLinkDto.getRootAssetsParam().getMonitorServerId();
                    port = param.getRootPort();
                } else if(StringUtils.isNotBlank(param.getTargetPort())){
                    bandHostid = netWorkLinkDto.getTargetAssetsParam().getAssetsId();
                    bandServerId = netWorkLinkDto.getTargetAssetsParam().getMonitorServerId();
                    port = param.getTargetPort();
                }else if(StringUtils.isBlank(param.getRootPort())&&StringUtils.isBlank(param.getTargetPort())
                        &&"ROOT".equals(netWorkLinkDto.getValuePort())){
                    bandHostid = netWorkLinkDto.getRootAssetsParam().getAssetsId();
                    bandServerId = netWorkLinkDto.getRootAssetsParam().getMonitorServerId();
                    port=netWorkLinkDto.getRootPort();
                }else if(StringUtils.isBlank(param.getRootPort())&&StringUtils.isBlank(param.getTargetPort())
                        &&"TARGET".equals(netWorkLinkDto.getValuePort())){
                    bandHostid = netWorkLinkDto.getTargetAssetsParam().getAssetsId();
                    bandServerId = netWorkLinkDto.getTargetAssetsParam().getMonitorServerId();
                    port=netWorkLinkDto.getTargetPort();
                }
                List<String> bandNameList = new ArrayList<>();
                bandNameList.add("[" + port + "]" + "MW_INTERFACE_IN_TRAFFIC");
                bandNameList.add("[" + port + "]" + "MW_INTERFACE_OUT_TRAFFIC");
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(bandServerId, bandNameList, bandHostid);
                if (result != null && result.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    // Map<String, Object> map = new HashMap<>();
                    if (jsonNode.size() > 0) {
                        for (JsonNode node : jsonNode) {
                            String name = node.get("name").asText();
                            name = name.substring(name.indexOf("]") + 1, name.length());
                            String lastValue = node.get("lastvalue").asText();
                            switch (name) {
                                case "MW_INTERFACE_IN_TRAFFIC":
                                    log.info("lastValue_INTERFACE_IN_TOTALTRAFFIC{}", lastValue);
                                    String itemid = node.get("itemid").asText();
                                    Integer valueType = node.get("value_type").asInt();
                                    //通过itemd查询历史数据
                                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(bandServerId, itemid, startTime, endTime, valueType);
                                    inMwHistoryDTOS = LinkReportUtil.getMwHistoryDto(historyRsult);
                                    break;
                                case "MW_INTERFACE_OUT_TRAFFIC":
                                    log.info("lastValue_INTERFACE_OUT_TOTALTRAFFIC{}", lastValue);
                                    String outitemid = node.get("itemid").asText();
                                    Integer outvalueType = node.get("value_type").asInt();
                                    //通过itemd查询历史数据
                                    MWZabbixAPIResult outhistoryRsult = mwtpServerAPI.HistoryGetByTimeAndType(bandServerId, outitemid, startTime, endTime, outvalueType);
                                    outMwHistoryDTOS = LinkReportUtil.getMwHistoryDto(outhistoryRsult);
                            }
                        }
                    }
                }
            }
        }
        list = convertLinkHistory(param, list, inMwHistoryDTOS, outMwHistoryDTOS);
        return list;
    }

    /**
     * 对于流入流量和流出流量处理转换成前端可以展示的数据
     *
     * @param param
     * @param list
     * @param inMwHistoryDTOS
     * @param outMwHistoryDTOS
     * @return
     */
    private List<LinkHistoryDto> convertLinkHistory(TrendParam param, List<LinkHistoryDto> list, List<MwHistoryDto> inMwHistoryDTOS, List<MwHistoryDto> outMwHistoryDTOS) {
        List<MwHistoryDto> mwHistoryDTOS = new ArrayList<>();
        if (inMwHistoryDTOS.size() > 0) {
            inMwHistoryDTOS.forEach(inMwHistoryDTO -> {
                mwHistoryDTOS.add(inMwHistoryDTO);
            });
        }
        if (outMwHistoryDTOS.size() > 0) {
            outMwHistoryDTOS.forEach(outMwHistoryDTO -> {
                mwHistoryDTOS.add(outMwHistoryDTO);
            });
        }
        /**
         * 对历史数据降序排序获得最大值，通过最大值确定统一转化的单位
         */
        if (mwHistoryDTOS.size() > 0) {
            Collections.sort(mwHistoryDTOS, new Comparator<MwHistoryDto>() {
                @Override
                public int compare(MwHistoryDto o1, MwHistoryDto o2) {
                    if (null == o1.getValue()) {
                        return (null == o2.getValue()) ? 0 : 1;
                    }
                    if (null == o2.getValue()) {
                        return -1;
                    }
                    if (Double.parseDouble(o1.getValue()) - Double.parseDouble(o2.getValue()) > 0) {
                        return -1;
                    } else if (Double.parseDouble(o1.getValue()) - Double.parseDouble(o2.getValue()) < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            if (null != mwHistoryDTOS.get(0).getValue() && StringUtils.isNotEmpty(mwHistoryDTOS.get(0).getValue())) {
                String units = UnitsUtil.getValueAndUnits(mwHistoryDTOS.get(0).getValue(), "bps").get("units");
                List<MwHistoryDto> newInMwHistoryDTOS = LinkReportUtil.setHistoryValueUnits(inMwHistoryDTOS, units);
                List<MwHistoryDto> newOutMwHistoryDTOS = LinkReportUtil.setHistoryValueUnits(outMwHistoryDTOS, units);

                LinkHistoryDto inSolarHistoryDto = new LinkHistoryDto();
                LinkHistoryDto outSolarHistoryDto = new LinkHistoryDto();

                inSolarHistoryDto.setCaption(param.getCaption());

                inSolarHistoryDto.setUnit("bps");
                if (inMwHistoryDTOS.size() > 0) {
                    inSolarHistoryDto.setLastUpdateTime(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getDate());
                    inSolarHistoryDto.setLastUpdateValue(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getValue());
                }
                inSolarHistoryDto.setDataList(newInMwHistoryDTOS);
                inSolarHistoryDto.setTitleName("IN");


                outSolarHistoryDto.setUnit("bps");
                if (outMwHistoryDTOS.size() > 0) {
                    outSolarHistoryDto.setLastUpdateTime(outMwHistoryDTOS.get(outMwHistoryDTOS.size() - 1).getDate());
                    outSolarHistoryDto.setLastUpdateValue(outMwHistoryDTOS.get(outMwHistoryDTOS.size() - 1).getValue());
                }
                outSolarHistoryDto.setDataList(newOutMwHistoryDTOS);
                outSolarHistoryDto.setTitleName("OUT");
                list.add(inSolarHistoryDto);
                list.add(outSolarHistoryDto);
            }
        }
        return list;
    }

    /**
     * 查询多条线路历史
     *
     * @param param
     * @return
     */
    public List<List<LinkHistoryDto>> getHistoryByList(TrendParam param) {
        List<List<LinkHistoryDto>> newList = new ArrayList<>();
        List<String> interfaceIds = param.getInterfaceIds();
        if (interfaceIds.size() > 0) {
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 50, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            List<Future<List<LinkHistoryDto>>> futures = new ArrayList<>();
            for (String interfaceId : interfaceIds) {
                Callable<List<LinkHistoryDto>> callable = new Callable<List<LinkHistoryDto>>() {
                    @Override
                    public List<LinkHistoryDto> call() throws Exception {
                        param.setInterfaceID(interfaceId);
                        List<LinkHistoryDto> linkHistoryDtos = selectLinkHistory(param);
                        return linkHistoryDtos;
                    }
                };
                Future<List<LinkHistoryDto>> future = executorService.submit(callable);
                futures.add(future);


            }
            futures.forEach(f -> {
                List<LinkHistoryDto> linkHistoryDtos = null;
                try {
                    linkHistoryDtos = f.get(15, TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.error("{getHistoryByList{}}", e);
                }
                if (null != linkHistoryDtos) {
                    newList.add(linkHistoryDtos);
                }
            });
        }
        return newList;
    }


    /**
     * 分组查询线路月报
     *
     * @param param
     * @return
     */
    public List<Map<String, String>> groupSelect(TrendParam param) {
        String startTime = "";
        String endTime = "";
        String tableName = "";
        String startTimeDay = "";
        String endTimeDay = "";
        if (!param.getSeniorchecked()) {
            tableName = setTableName(param, tableName);
            if(null!=param.getFixedDate()) {
                startTime = param.getFixedDate().get(0);
                endTime = param.getFixedDate().get(1);
            }
        }

        List<MwGroupDto> mwGroupDtoLIst = getMwGroupDtoLIst(param, startTime, endTime, tableName, startTimeDay, endTimeDay);

        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        Map<String, String> map3 = new HashMap<>();
        Map<String, String> map4 = new HashMap<>();
        Map<String, String> map5 = new HashMap<>();
        Map<String, String> map6 = new HashMap<>();

        map1.put("caption", "最大值小于带宽利用率的10%");
        map2.put("caption", "最大值于带宽利用率的10%-50%");
        map3.put("caption", "最大值大于带宽利用率的50%");
        map4.put("caption", "平均值小于带宽利用率的10%");
        map5.put("caption", "平均值于带宽利用率的10%-50%");
        map6.put("caption", "平均值大于带宽利用率的50%");

        List<Map<String, String>> list = new ArrayList<>();

        ThreadPoolExecutor executorService = new ThreadPoolExecutor(6, 10, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<Map<Integer, List<LinkDetailDto>>>> futureList = new ArrayList<>();
        mwGroupDtoLIst.forEach(groupDto -> {
            Callable<Map<Integer, List<LinkDetailDto>>> callable = new Callable<Map<Integer, List<LinkDetailDto>>>() {
                @Override
                public Map<Integer, List<LinkDetailDto>> call() {
                    List<LinkDetailDto> solarDetailDtos = new ArrayList<>();
                    solarDetailDtos = mwReportDao.groupSelectList(groupDto);
                    Map<Integer, List<LinkDetailDto>> map = new HashMap<>();
                    map.put(groupDto.getTag(), solarDetailDtos);
                    return map;
                }
            };
            Future<Map<Integer, List<LinkDetailDto>>> submit = executorService.submit(callable);
            futureList.add(submit);
        });
        if (futureList.size() > 0) {
            futureList.forEach(f -> {
                try {
                    Map<Integer, List<LinkDetailDto>> solarDetailDtos = f.get(10, TimeUnit.MINUTES);
                    Set<Integer> integers = solarDetailDtos.keySet();
                    for (Integer integer : integers) {
                        switch (integer) {
                            case 0:
                                list.add(map1);
                                break;
                            case 1:
                                list.add(map2);
                                break;
                            case 2:
                                list.add(map3);
                                break;
                            case 3:
                                list.add(map4);
                                break;
                            case 4:
                                list.add(map5);
                                break;
                            case 5:
                                list.add(map6);
                                break;
                        }
                        if (solarDetailDtos.get(integer).size() > 0) {
                            for (LinkDetailDto solarDetailDto : solarDetailDtos.get(integer)) {
                                InterfaceReportDto dto = getInterfaceReportDto(solarDetailDto);
                                Map<String, String> map = JSONObject.parseObject(JSON.toJSONString(dto), new TypeReference<Map<String, String>>() {
                                });
                                list.add(map);
                            }
                        }
                    }
                } catch (Exception e) {
                    f.cancel(true);
                    log.error("fail to groupSelect", e);
                }
            });
        }
        return list;
    }

    private InterfaceReportDto getInterfaceReportDto(LinkDetailDto solarDetailDto) {
        return InterfaceReportDto.builder()
                .interfaceID(solarDetailDto.getInterfaceID())
                .caption(solarDetailDto.getCaption())
                .inBandwidth(UnitsUtil.getValueWithUnits(solarDetailDto.getInBandwidth().toString(), solarDetailDto.getBandUnit()))
                .outBandwidth(UnitsUtil.getValueWithUnits(solarDetailDto.getOutBandwidth().toString(), solarDetailDto.getBandUnit()))
                .inMaxbps(UnitsUtil.getValueWithUnits(solarDetailDto.getInMaxbps().toString(), solarDetailDto.getBandUnit()))
                .inMinbps(UnitsUtil.getValueWithUnits(solarDetailDto.getInMinbps().toString(), solarDetailDto.getBandUnit()))
                .inAveragebps(UnitsUtil.getValueWithUnits(solarDetailDto.getInAveragebps().toString(), solarDetailDto.getBandUnit()))
                .outMaxbps(UnitsUtil.getValueWithUnits(solarDetailDto.getOutMaxbps().toString(), solarDetailDto.getBandUnit()))
                .outMinbps(UnitsUtil.getValueWithUnits(solarDetailDto.getOutMinbps().toString(), solarDetailDto.getBandUnit()))
                .outAveragebps(UnitsUtil.getValueWithUnits(solarDetailDto.getOutAveragebps().toString(), solarDetailDto.getBandUnit()))
                .inMaxUse(solarDetailDto.getInMaxUse() + "%")
                .inAvgUse(solarDetailDto.getInAvgUse() + "%")
                .outMaxUse(solarDetailDto.getOutMaxUse() + "%")
                .outAvgUse(solarDetailDto.getOutAvgUse() + "%")
                .inProportionTen(solarDetailDto.getProportionDto().getInProportionTen() + "%")
                .inProportionFifty(solarDetailDto.getProportionDto().getInProportionFifty() + "%")
                .inProportionEighty(solarDetailDto.getProportionDto().getInProportionEighty() + "%")
                .inProportionHundred(solarDetailDto.getProportionDto().getInProportionHundred() + "%")
                .outProportionTen(solarDetailDto.getProportionDto().getOutProportionTen() + "%")
                .outProportionFifty(solarDetailDto.getProportionDto().getOutProportionFifty() + "%")
                .outProportionEighty(solarDetailDto.getProportionDto().getOutProportionEighty() + "%")
                .outProportionHundred(solarDetailDto.getProportionDto().getOutProportionHundred() + "%")
                .build();
    }

    private List<MwGroupDto> getMwGroupDtoLIst(TrendParam param, String startTime, String endTime, String tableName, String startTimeDay, String endTimeDay) {
        MwGroupDto MwGroupDto0 = MwGroupDto.builder().caption(param.getCaption())
                .tag(0)
                .tableName(tableName)
                .periodRadio(param.getPeriodRadio())
                .inColumn("in_max_bps")
                .outColumn("out_max_bps")
                .percentFront(0f)
                .percentBack(0.1f)
                .startTimeDay(startTimeDay)
                .endTimeDay(endTimeDay)
                .startTime(startTime)
                .endTime(endTime)
                .interfaceIDs(param.getInterfaceIds())
                .build();
        MwGroupDto MwGroupDto1 = MwGroupDto.builder().caption(param.getCaption())
                .tag(1)
                .tableName(tableName)
                .periodRadio(param.getPeriodRadio())
                .inColumn("in_max_bps")
                .outColumn("out_max_bps")
                .percentFront(0.1f)
                .percentBack(0.5f)
                .startTimeDay(startTimeDay)
                .endTimeDay(endTimeDay)
                .startTime(startTime)
                .endTime(endTime)
                .interfaceIDs(param.getInterfaceIds())
                .build();
        MwGroupDto MwGroupDto2 = MwGroupDto.builder().caption(param.getCaption())
                .tag(2)
                .tableName(tableName)
                .periodRadio(param.getPeriodRadio())
                .inColumn("in_max_bps")
                .outColumn("out_max_bps")
                .percentFront(0.5f)
                .percentBack(1f)
                .startTimeDay(startTimeDay)
                .endTimeDay(endTimeDay)
                .startTime(startTime)
                .endTime(endTime)
                .interfaceIDs(param.getInterfaceIds())
                .build();
        MwGroupDto MwGroupDto3 = MwGroupDto.builder().caption(param.getCaption())
                .tag(3)
                .tableName(tableName)
                .periodRadio(param.getPeriodRadio())
                .inColumn("in_average_bps")
                .outColumn("out_average_bps")
                .percentFront(0f)
                .percentBack(0.1f)
                .startTimeDay(startTimeDay)
                .endTimeDay(endTimeDay)
                .startTime(startTime)
                .endTime(endTime)
                .interfaceIDs(param.getInterfaceIds())
                .build();
        MwGroupDto MwGroupDto4 = MwGroupDto.builder().caption(param.getCaption())
                .tag(4)
                .tableName(tableName)
                .periodRadio(param.getPeriodRadio())
                .inColumn("in_average_bps")
                .outColumn("out_average_bps")
                .percentFront(0.1f)
                .percentBack(0.5f)
                .startTimeDay(startTimeDay)
                .endTimeDay(endTimeDay)
                .startTime(startTime)
                .endTime(endTime)
                .interfaceIDs(param.getInterfaceIds())
                .build();
        MwGroupDto MwGroupDto5 = MwGroupDto.builder().caption(param.getCaption())
                .tag(5)
                .tableName(tableName)
                .periodRadio(param.getPeriodRadio())
                .inColumn("in_average_bps")
                .outColumn("out_average_bps")
                .percentFront(0.5f)
                .percentBack(1f)
                .startTimeDay(startTimeDay)
                .endTimeDay(endTimeDay)
                .startTime(startTime)
                .endTime(endTime)
                .interfaceIDs(param.getInterfaceIds())
                .build();
        List<MwGroupDto> MwGroupDtoList = new ArrayList<>();
        MwGroupDtoList.add(MwGroupDto0);
        MwGroupDtoList.add(MwGroupDto1);
        MwGroupDtoList.add(MwGroupDto2);
        MwGroupDtoList.add(MwGroupDto3);
        MwGroupDtoList.add(MwGroupDto4);
        MwGroupDtoList.add(MwGroupDto5);
        return MwGroupDtoList;
    }

    public void exportLink(ExportLinkParam uParam, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            List<InterfaceReportDto> list = uParam.getList();
            Set<String> includeColumnFiledNames = new HashSet<>();
            if (uParam.getFields() != null && uParam.getFields().size() > 0) {
                includeColumnFiledNames = uParam.getFields();
            } else {
                includeColumnFiledNames.add("caption");
                includeColumnFiledNames.add("inBandwidth");
                includeColumnFiledNames.add("outBandwidth");
                includeColumnFiledNames.add("inAveragebps");
                includeColumnFiledNames.add("inMinbps");
                includeColumnFiledNames.add("inMaxbps");
                includeColumnFiledNames.add("outMinbps");
                includeColumnFiledNames.add("outAveragebps");
                includeColumnFiledNames.add("outMaxbps");
                includeColumnFiledNames.add("inAvgUse");
                includeColumnFiledNames.add("inProportionTen");
                includeColumnFiledNames.add("inProportionFifty");
                includeColumnFiledNames.add("inProportionEighty");
                includeColumnFiledNames.add("inProportionHundred");
                includeColumnFiledNames.add("outProportionTen");
                includeColumnFiledNames.add("outProportionFifty");
                includeColumnFiledNames.add("outProportionEighty");
                includeColumnFiledNames.add("outProportionHundred");
            }
            //设置回复头一些信息
            String fileName = null; //导出文件名
            if (uParam.getName() != null && !uParam.getName().equals("")) {
                fileName = uParam.getName();
            } else {
                fileName = System.currentTimeMillis() + "";
            }
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            logger.info("fileName: {}", fileName);
            List<List<InterfaceReportDto>> li = getSubLists(
                    list, 50000);

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
            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            //创建easyExcel写出对象
            excelWriter = EasyExcel.write(response.getOutputStream(), InterfaceReportDto.class).registerWriteHandler(horizontalCellStyleStrategy).build();

            //计算sheet分页
            Integer sheetNum = list.size() % 50000 == 0 ? list.size() / 50000 : list.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(li.get(i), sheet);
            }
            logger.info("导出成功");
        } catch (Exception e) {
            logger.error("导出失败", e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    //将list集合数据按照指定大小分成好几个小的list
    public List<List<InterfaceReportDto>> getSubLists(List<InterfaceReportDto> allData, int size) {
        List<List<InterfaceReportDto>> result = new ArrayList();
        for (int begin = 0; begin < allData.size(); begin = begin + size) {
            int end = (begin + size > allData.size() ? allData.size() : begin + size);
            result.add(allData.subList(begin, end));
        }
        return result;
    }

    public List<InterfaceReportDto> getLink(TrendParam trendParam) {
        log.info("LinkReportManager{} getLink() trendParam::"+trendParam);
        List<InterfaceReportDto> list = new ArrayList<>();
        LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
        linkDropDownParam.setPageNumber(trendParam.getPageNumber());
        linkDropDownParam.setPageSize(trendParam.getPageSize());
        List<String> faceList=trendParam.getInterfaceIds();
        if(faceList.size()==1){
            linkDropDownParam.setLinkId(faceList.get(0));
        }else if(faceList.size()>1){
            List<String> ilist=new ArrayList<>();
            linkDropDownParam.setIsAdvancedQuery(true);
            for (String item:faceList) {
                ilist.add(item);
            }
            linkDropDownParam.setLinkIds(ilist);
        }
        List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkService.getNetWorkLinkDtos(linkDropDownParam);
        log.info("LinkReportManager{} getLink() netWorkLinkDtos::"+netWorkLinkDtos);
        if(null==netWorkLinkDtos){
            return list;
        }
        log.info("LinkReportManager{} getLink() getSeniorchecked::"+trendParam.getSeniorchecked());
        if (!trendParam.getSeniorchecked()) {//非高级查询
            String tableName = "";
            tableName = setTableName(trendParam, tableName);
            log.info("LinkReportManager{} getLink() tableName::"+tableName);
            List<String> interfaceIds = new ArrayList<>();
            netWorkLinkDtos.forEach(linkDto -> {
                interfaceIds.add(linkDto.getLinkId());
            });
            if(null!=trendParam.getFixedDate()&&trendParam.getFixedDate().size()>0&&StringUtils.isNotEmpty(trendParam.getFixedDate().get(0))) {
                QueryDto queryDto = QueryDto.builder()
                        .tableName(tableName)
                        .objectIds(interfaceIds)
                        .startTime(trendParam.getFixedDate().get(0))
                        .endTime(trendParam.getFixedDate().get(1)).
                                build();
                log.info("LinkReportManager{} getLink() queryDto::"+queryDto);
                List<InterfaceReportDtos> interfaceReportDtos = mwReportDao.selectLinkList(queryDto);
                log.info("LinkReportManager{} getLink() interfaceReportDtos::"+interfaceReportDtos.size());
                if (null != interfaceReportDtos && interfaceReportDtos.size() > 0) {
                    interfaceReportDtos.forEach(interfaceReportDto -> {
                        InterfaceReportDto dto = InterfaceReportDto.builder()
                                .interfaceID(interfaceReportDto.getInterfaceID())
                                .caption(interfaceReportDto.getCaption())
                                .inBandwidth(UnitsUtil.getValueWithUnits(interfaceReportDto.getInBandwidth().toString(), interfaceReportDto.getBandUnit()))
                                .outBandwidth(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutBandwidth().toString(), interfaceReportDto.getBandUnit()))
                                .inMaxbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getInMaxbps().toString(), interfaceReportDto.getBandUnit()))
                                .inMinbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getInMinbps().toString(), interfaceReportDto.getBandUnit()))
                                .inAveragebps(UnitsUtil.getValueWithUnits(interfaceReportDto.getInAveragebps().toString(), interfaceReportDto.getBandUnit()))
                                .outMaxbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutMaxbps().toString(), interfaceReportDto.getBandUnit()))
                                .outMinbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutMinbps().toString(), interfaceReportDto.getBandUnit()))
                                .outAveragebps(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutAveragebps().toString(), interfaceReportDto.getBandUnit()))
                                .inMaxUse(interfaceReportDto.getInMaxUse() + "%")
                                .inAvgUse(interfaceReportDto.getInAvgUse() + "%")
                                .outMaxUse(interfaceReportDto.getOutMaxUse() + "%")
                                .outAvgUse(interfaceReportDto.getOutAvgUse() + "%")
                                .inProportionTen(interfaceReportDto.getInProportionTen() + "%")
                                .inProportionFifty(interfaceReportDto.getInProportionFifty() + "%")
                                .inProportionEighty(interfaceReportDto.getInProportionEighty() + "%")
                                .inProportionHundred(interfaceReportDto.getInProportionHundred() + "%")
                                .outProportionTen(interfaceReportDto.getOutProportionTen() + "%")
                                .outProportionFifty(interfaceReportDto.getOutProportionFifty() + "%")
                                .outProportionEighty(interfaceReportDto.getOutProportionEighty() + "%")
                                .outProportionHundred(interfaceReportDto.getOutProportionHundred() + "%")
                                .build();
                        list.add(dto);
                    });
                }
            }
        } else {//高级查询
            list.addAll(getLinkInfo(trendParam));
        }
        return list;

    }

    private InterfaceReportDto getInterfaceReportDto(NetWorkLinkDto dto, TrendParam trendParam) {
        InterfaceReportDto interfaceReportDto = new InterfaceReportDto();
        interfaceReportDto.setCaption(dto.getLinkName());
        interfaceReportDto.setInterfaceID(dto.getLinkId());
        String port = "";
        String bandHostid = "";
        Integer bandServerId = 0;
        if (dto.getValuePort().equals("ROOT")) {
            bandHostid = dto.getRootAssetsParam().getAssetsId();
            bandServerId = dto.getRootAssetsParam().getMonitorServerId();
            port = dto.getRootPort();
        } else {
            bandHostid = dto.getTargetAssetsParam().getAssetsId();
            bandServerId = dto.getTargetAssetsParam().getMonitorServerId();
            port = dto.getTargetPort();
        }
        List<String> bandNameList = new ArrayList<>();
        // bandNameList.add("[" + port + "]" + "INTERFACE_BANDWIDTH");
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_IN_TRAFFIC");
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_OUT_TRAFFIC");
        //查询zabbix获得流入流出流量的itemid
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(bandServerId, bandNameList, bandHostid);
        List<String> chooseTime = trendParam.getChooseTime();
        Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
        Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
        Map<String, Object> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            String bandUnit = dto.getBandUnit();

            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String name = node.get("name").asText();
                    name = name.substring(name.indexOf("]") + 1, name.length());
                    String lastValue = node.get("lastvalue").asText();
                    switch (name) {
                        case "MW_INTERFACE_IN_TRAFFIC":
                            log.info("lastValue_INTERFACE_IN_TOTALTRAFFIC{}", lastValue);
                            map.put("INTERFACE_IN_TOTALTRAFFIC", lastValue);
                            String units = node.get("units").asText();
                            map.put("IN_UNITS", units);
                            String itemid = node.get("itemid").asText();
                            Integer valueType = node.get("value_type").asInt();
                            map.put("IN_ITEMID", itemid);
                            map.put("IN_VALUE_TYPE", valueType);
                            //通过itemd查询历史数据
                            MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(bandServerId, itemid, startTime, endTime, valueType);
                            String upLinkBandwidth = dto.getUpLinkBandwidth();
                            if (!units.equals(bandUnit)) {
                                Map<String, String> valueMap = UnitsUtil.getValueMap(upLinkBandwidth, units, bandUnit);
                                upLinkBandwidth = valueMap.get("value");
                            }
                            interfaceReportDto.setInBandwidth(UnitsUtil.getValueWithUnits(dto.getUpLinkBandwidth(), dto.getBandUnit()));
                            //对历史数据进行处理，获取最大值最小值平均值
                            List<HistoryValueDto> inData = ReportUtil.getValueData(historyRsult);
                            TrendDto inTrendDto = ReportUtil.getTrendDto(inData, units);
                            interfaceReportDto.setInMaxbps(inTrendDto.getValueMax());
                            interfaceReportDto.setInMinbps(inTrendDto.getValueMin());
                            interfaceReportDto.setInAveragebps(inTrendDto.getValueAvg());

                            TrendDto intrendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(inData);
                            if (Double.valueOf(upLinkBandwidth) != 0) {
                                interfaceReportDto.setInAvgUse(RoundOff(intrendDtoNotUnit.getValueAvg(), upLinkBandwidth) + "%");
                                interfaceReportDto.setInMaxUse(RoundOff(intrendDtoNotUnit.getValueMax(), upLinkBandwidth) + "%");
                            }
                            //获取不同的时间占比
                            ProportionDto inproportion = getProportion(inData, upLinkBandwidth);
                            interfaceReportDto.setInProportionTen(inproportion.getProportionTen() + "%");
                            interfaceReportDto.setInProportionFifty(inproportion.getProportionFifty() + "%");
                            interfaceReportDto.setInProportionEighty(inproportion.getProportionEighty() + "%");
                            interfaceReportDto.setInProportionHundred(inproportion.getProportionHundred() + "%");
                            break;
                        case "MW_INTERFACE_OUT_TRAFFIC":
                            log.info("lastValue_INTERFACE_OUT_TOTALTRAFFIC{}", lastValue);
                            map.put("INTERFACE_OUT_TOTALTRAFFIC", lastValue);
                            String ounits = node.get("units").asText();
                            String outitemid = node.get("itemid").asText();
                            Integer outvalueType = node.get("value_type").asInt();
                            map.put("OUT_UNITS", ounits);
                            map.put("OUT_ITEMID", outitemid);
                            map.put("OUT_VALUE_TYPE", outvalueType);
                            //通过itemd查询历史数据
                            MWZabbixAPIResult outhistoryRsult = mwtpServerAPI.HistoryGetByTimeAndType(bandServerId, outitemid, startTime, endTime, outvalueType);
                            String downLinkBandwidth = dto.getDownLinkBandwidth();
                            if (!ounits.equals(bandUnit)) {
                                Map<String, String> valueMap = UnitsUtil.getValueMap(downLinkBandwidth, ounits, bandUnit);
                                downLinkBandwidth = valueMap.get("value");
                            }
                            interfaceReportDto.setOutBandwidth(UnitsUtil.getValueWithUnits(dto.getDownLinkBandwidth(), dto.getBandUnit()));
                            //对历史数据进行处理，获取最大值，最小值，平均值
                            List<HistoryValueDto> outData = ReportUtil.getValueData(outhistoryRsult);
                            TrendDto outTrendDto = ReportUtil.getTrendDto(outData, ounits);
                            interfaceReportDto.setOutMaxbps(outTrendDto.getValueMax());
                            interfaceReportDto.setOutMinbps(outTrendDto.getValueMin());
                            interfaceReportDto.setOutAveragebps(outTrendDto.getValueAvg());
                            //对历史数据进行处理，获取最大，最小，平均利用率
                            TrendDto outtrendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(outData);
                            if (Double.valueOf(downLinkBandwidth) != 0) {
                                interfaceReportDto.setOutAvgUse(RoundOff(outtrendDtoNotUnit.getValueAvg(), downLinkBandwidth) + "%");
                                interfaceReportDto.setOutMaxUse(RoundOff(outtrendDtoNotUnit.getValueMax(), downLinkBandwidth) + "%");
                            }
                            //获取不同的时间占比
                            ProportionDto outproportion = getProportion(outData, downLinkBandwidth);
                            interfaceReportDto.setOutProportionTen(outproportion.getProportionTen() + "%");
                            interfaceReportDto.setOutProportionFifty(outproportion.getProportionFifty() + "%");
                            interfaceReportDto.setOutProportionEighty(outproportion.getProportionEighty() + "%");
                            interfaceReportDto.setOutProportionHundred(outproportion.getProportionHundred() + "%");
                            break;
                    }
                }
            }
        }
        return interfaceReportDto;
    }

    /**
     * 查询线路占比
     * @param list
     * @param bandWidth
     * @return
     */
    private ProportionDto getProportion(List<HistoryValueDto> list, String bandWidth) {
        ProportionDto proportionDto = new ProportionDto();
        Double band = Double.valueOf(bandWidth);
        Double inProportionTen = band * 0.1;
        Double inProportionFifty = band * 0.5;
        Double inProportionEighty = band * 0.8;
        Integer countTen = 0;
        Integer countFifty = 0;
        Integer countEighty = 0;
        Integer count = 0;
        for (HistoryValueDto dto : list) {
            if (dto.getValue() < inProportionTen) {
                countTen++;
            } else if (dto.getValue() < inProportionFifty) {
                countFifty++;
            } else if (dto.getValue() < inProportionEighty) {
                countEighty++;
            } else {
                count++;
            }
        }
        if (countTen != 0) {
            Double ten1 = new BigDecimal((double) countTen / list.size() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            proportionDto.setProportionTen(ten1);
        } else {
            proportionDto.setProportionTen(0.00);
        }
        if (countFifty != 0) {
            Double ten1 = new BigDecimal((double) countFifty / list.size() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            proportionDto.setProportionFifty(ten1);
        } else {
            proportionDto.setProportionFifty(0.00);
        }
        if (countEighty != 0) {
            Double ten1 = new BigDecimal((double) countEighty / list.size() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            proportionDto.setProportionEighty(ten1);
        } else {
            proportionDto.setProportionEighty(0.00);
        }
        if (count != 0) {
            Double ten1 = new BigDecimal((double) count / list.size() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            proportionDto.setProportionHundred(ten1);
        } else {
            proportionDto.setProportionHundred(0.00);
        }
        return proportionDto;

    }

    private Double RoundOff(String value, String bandWidth) {
        Double outBandWidthUtilization = Double.valueOf(value) / Double.valueOf(bandWidth) * 100;
        BigDecimal outDisk = new BigDecimal(outBandWidthUtilization.toString());
        outDisk = outDisk.setScale(2, BigDecimal.ROUND_HALF_UP);//四舍五入保留两位小数
        return outDisk.doubleValue();

    }

    /**
     * 导入数据库的查询
     * @param netWorkLinkDtos
     * @param startTime
     * @param endTime
     * @return
     */
    public List<InterfaceReportDtos> getLinks(List<NetWorkLinkDto> netWorkLinkDtos, Long startTime, Long endTime) {
        List<InterfaceReportDtos> list = new ArrayList<>();
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 50, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<InterfaceReportDtos>> futures = new ArrayList<>();
        netWorkLinkDtos.forEach(dto -> {
            Callable<InterfaceReportDtos> callable = new Callable<InterfaceReportDtos>() {
                @Override
                public InterfaceReportDtos call() throws Exception {
                    return getInterfaceReportDtos(dto, startTime, endTime);
                }
            };
            Future<InterfaceReportDtos> submit = executorService.submit(callable);
            futures.add(submit);

        });
        futures.forEach(f -> {
            try {
                InterfaceReportDtos interfaceReportDto = f.get(15, TimeUnit.SECONDS);
                list.add(interfaceReportDto);
            } catch (Exception e) {
                logger.info("getLink", e);
            }
        });
        return list;
    }

    private InterfaceReportDtos getInterfaceReportDtos(NetWorkLinkDto dto, Long startTime, Long endTime) {
        InterfaceReportDtos interfaceReportDto = new InterfaceReportDtos();
        interfaceReportDto.setInterfaceID(dto.getLinkId());
        interfaceReportDto.setCaption(dto.getLinkName());
        String port = "";
        String bandHostid = "";
        Integer bandServerId = 0;
        if (dto.getValuePort().equals("ROOT")) {
            bandHostid = dto.getRootAssetsParam().getAssetsId();
            bandServerId = dto.getRootAssetsParam().getMonitorServerId();
            port = dto.getRootPort();
        } else {
            bandHostid = dto.getTargetAssetsParam().getAssetsId();
            bandServerId = dto.getTargetAssetsParam().getMonitorServerId();
            port = dto.getTargetPort();
        }
        List<String> bandNameList = new ArrayList<>();
        // bandNameList.add("[" + port + "]" + "INTERFACE_BANDWIDTH");
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_IN_TRAFFIC");
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_OUT_TRAFFIC");
        //查询zabbix获得流入流出流量的itemid
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(bandServerId, bandNameList, bandHostid);
        Map<String, Object> map = new HashMap<>();
        if (result != null && result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            String bandUnit = dto.getBandUnit();

            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String name = node.get("name").asText();
                    name = name.substring(name.indexOf("]") + 1, name.length());
                    String lastValue = node.get("lastvalue").asText();
                    boolean isTrend = MwReportDateUtil.getDateDay(startTime, endTime);
                    switch (name) {
                        case "MW_INTERFACE_IN_TRAFFIC":
                            log.info("lastValue_INTERFACE_IN_TOTALTRAFFIC{}", lastValue);
                            map.put("INTERFACE_IN_TOTALTRAFFIC", lastValue);
                            String units = node.get("units").asText();
                            map.put("IN_UNITS", units);
                            String itemid = node.get("itemid").asText();
                            Integer valueType = node.get("value_type").asInt();
                            map.put("IN_ITEMID", itemid);
                            map.put("IN_VALUE_TYPE", valueType);
                            //通过itemd查询历史数据
                            //判断是否查询趋势
                            List<HistoryValueDto> inData = new ArrayList<>();
                            if(isTrend){
                                MWZabbixAPIResult trendResult = mwtpServerAPI.trendBatchGet(bandServerId, Arrays.asList(itemid), startTime, endTime);
                                inData = ReportUtil.getTrendValueData(trendResult);
                            }else{
                                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(bandServerId, itemid, startTime, endTime, valueType);
                                inData = ReportUtil.getValueData(historyRsult);
                            }
                            for (HistoryValueDto inDatum : inData) {
                                Double value = inDatum.getValue();
                                Map<String, String> valueMap = UnitsUtil.getValueMap(String.valueOf(value), dto.getBandUnit(), units);
                                inDatum.setValue(Double.parseDouble(valueMap.get("value")));
                            }
                            String upLinkBandwidth = dto.getUpLinkBandwidth();
//                            if (!units.equals(bandUnit)) {
//                                Map<String, String> valueMap = UnitsUtil.getValueMap(upLinkBandwidth, units, bandUnit);
//                                upLinkBandwidth = valueMap.get("value");
//                            }
                            interfaceReportDto.setInBandwidth(Double.valueOf(dto.getUpLinkBandwidth()));
                            interfaceReportDto.setBandUnit(dto.getBandUnit());
                            //对历史数据进行处理，获取最大值最小值平均值
                            TrendDto intrendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(inData);
                            interfaceReportDto.setInMaxbps(Double.valueOf(intrendDtoNotUnit.getValueMax()));
                            interfaceReportDto.setInMinbps(Double.valueOf(intrendDtoNotUnit.getValueMin()));
                            interfaceReportDto.setInAveragebps(Double.valueOf(intrendDtoNotUnit.getValueAvg()));
                            //对历史数据进行处理，获取最大值最小值平均值利用率
                            if (Double.valueOf(upLinkBandwidth) != 0) {
                                interfaceReportDto.setInAvgUse(RoundOff(intrendDtoNotUnit.getValueAvg(), upLinkBandwidth));
                                interfaceReportDto.setInMaxUse(RoundOff(intrendDtoNotUnit.getValueMax(), upLinkBandwidth));
                            }else{
                                interfaceReportDto.setInAvgUse(0.0);
                                interfaceReportDto.setInMaxUse(0.0);
                            }
                            //获取不同的时间占比
                            ProportionDto inproportion = getProportion(inData, upLinkBandwidth);
                            interfaceReportDto.setInProportionTen(inproportion.getProportionTen());
                            interfaceReportDto.setInProportionFifty(inproportion.getProportionFifty());
                            interfaceReportDto.setInProportionEighty(inproportion.getProportionEighty());
                            interfaceReportDto.setInProportionHundred(inproportion.getProportionHundred());
                            break;
                        case "MW_INTERFACE_OUT_TRAFFIC":
                            log.info("lastValue_INTERFACE_OUT_TOTALTRAFFIC{}", lastValue);
                            map.put("INTERFACE_OUT_TOTALTRAFFIC", lastValue);
                            String ounits = node.get("units").asText();
                            String outitemid = node.get("itemid").asText();
                            Integer outvalueType = node.get("value_type").asInt();
                            map.put("OUT_UNITS", ounits);
                            map.put("OUT_ITEMID", outitemid);
                            map.put("OUT_VALUE_TYPE", outvalueType);
                            //通过itemd查询历史数据
                            //判断是否查询趋势
                            List<HistoryValueDto> outData = new ArrayList<>();
                            if(isTrend){
                                MWZabbixAPIResult trendResult = mwtpServerAPI.trendBatchGet(bandServerId, Arrays.asList(outitemid), startTime, endTime);
                                if(trendResult == null || trendResult.isFail()){continue;}
                                outData = ReportUtil.getTrendValueData(trendResult);
                            }else{
                                MWZabbixAPIResult outhistoryRsult = mwtpServerAPI.HistoryGetByTimeAndType(bandServerId, outitemid, startTime, endTime, outvalueType);
                                if(outhistoryRsult == null || outhistoryRsult.isFail()){continue;}
                                outData = ReportUtil.getValueData(outhistoryRsult);
                            }
                            for (HistoryValueDto outDatum : outData) {
                                Double value = outDatum.getValue();
                                Map<String, String> valueMap = UnitsUtil.getValueMap(String.valueOf(value), dto.getBandUnit(), ounits);
                                outDatum.setValue(Double.parseDouble(valueMap.get("value")));
                            }
                            String downLinkBandwidth = dto.getDownLinkBandwidth();
//                            if (!ounits.equals(bandUnit)) {
//                                Map<String, String> valueMap = UnitsUtil.getValueMap(downLinkBandwidth, ounits, bandUnit);
//                                downLinkBandwidth = valueMap.get("value");
//                            }
                            //对历史数据进行处理，获取最大值最小值平均值
                            TrendDto outTrendDto = ReportUtil.getTrendDtoNotUnit(outData);
                            interfaceReportDto.setOutBandwidth(Double.valueOf(dto.getDownLinkBandwidth()));
                            interfaceReportDto.setOutMaxbps(Double.valueOf(outTrendDto.getValueMax()));
                            interfaceReportDto.setOutMinbps(Double.valueOf(outTrendDto.getValueMin()));
                            interfaceReportDto.setOutAveragebps(Double.valueOf(outTrendDto.getValueAvg()));
                            //对历史数据进行处理，获取最大值最小值平均值
                            if (Double.valueOf(downLinkBandwidth) != 0) {
                                interfaceReportDto.setOutAvgUse(RoundOff(outTrendDto.getValueAvg(), downLinkBandwidth));
                                interfaceReportDto.setOutMaxUse(RoundOff(outTrendDto.getValueMax(), downLinkBandwidth));
                            }else{
                                interfaceReportDto.setOutAvgUse(0.0);
                                interfaceReportDto.setOutMaxUse(0.0);
                            }
                            //获取不同的时间占比
                            ProportionDto outproportion = getProportion(outData, downLinkBandwidth);
                            interfaceReportDto.setOutProportionTen(outproportion.getProportionTen());
                            interfaceReportDto.setOutProportionFifty(outproportion.getProportionFifty());
                            interfaceReportDto.setOutProportionEighty(outproportion.getProportionEighty());
                            interfaceReportDto.setOutProportionHundred(outproportion.getProportionHundred());
                            break;
                    }
                }
            }
        }
        return interfaceReportDto;
    }

    private String setTableName(TrendParam trendParam, String tableName) {
        if (trendParam.getDayType() == 0) {//全天(24小时)
            tableName = "mw_report_link_allday";
        } else if (trendParam.getDayType() == 1) { //全天(指定时间的段）
            tableName = "mw_report_link_allday_worktime";
        } else if (trendParam.getDayType() == 2) {// 工作日(24小时)
            tableName = "mw_report_link_workday";
        } else if (trendParam.getDayType() == 3) {// 工作日(指定时间的段）
            tableName = "mw_report_link_workday_worktime";
        }
        return tableName;
    }

    public void inputLink(Long startFrom, Long endTill, Date dateTime) {
        List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkDao.getLinkList();
        List<InterfaceReportDtos> allDayLink = getLinks(netWorkLinkDtos, startFrom, endTill);
        if (allDayLink.size() > 0) {
            SolarTimeDto solarTimeDto = mwReportDao.selectTime(ReportBase.LINK.getId());
            String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
            Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);

            List<InterfaceReportDtos> workLink = getLinks(netWorkLinkDtos, workStartFrom, workEndTill);

            String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
            int count = mwReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日

            if (count == 0) {
                /**
                 * 工作日
                 * 0-24小时的数据
                 * 8-17（自定义时间段）点的数据
                 */
                String workDayTableName = "mw_report_link_workday";
                String workDayWorkTimeTableName = "mw_report_link_workday_worktime";
                mwReportDao.insertReportLink(workDayTableName, dateTime, allDayLink);
                mwReportDao.insertReportLink(workDayWorkTimeTableName, dateTime, workLink);
            }
            String allDayTableName = "mw_report_link_allday";
            String allDayWorkTimeTableName = "mw_report_link_allday_worktime";
            mwReportDao.insertReportLink(allDayTableName, dateTime, allDayLink);
            mwReportDao.insertReportLink(allDayWorkTimeTableName, dateTime, workLink);
        }
    }

    //查询线路流量zabbix
    private List<InterfaceReportDto> getInterfaceReportBatch(List<NetWorkLinkDto> netWorkLinkDtos,TrendParam trendParam){
        List<InterfaceReportDto> interfaceDtoInfos = getInterfaceDtoInfo(netWorkLinkDtos);
        if(CollectionUtils.isEmpty(interfaceDtoInfos)){return interfaceDtoInfos;}
        //按照serverId进行数据分组
        Map<Integer, List<String>> serverGroupMap = interfaceDtoInfos.stream().filter(item->item.getServerId() != null &&  item.getServerId() != 0)
                .collect(Collectors.groupingBy(InterfaceReportDto::getServerId, Collectors.mapping(InterfaceReportDto::getHostId, Collectors.toList())));
        if(CollectionUtils.isEmpty(serverGroupMap)){return interfaceDtoInfos;}
        List<String> itemNames = getItemNamesInfo(interfaceDtoInfos);//所有需要查询的监控项
        List<InterfaceReportDto> realDatas = new ArrayList<>();
        for (Integer serverId : serverGroupMap.keySet()) {
            List<String> hostIds = serverGroupMap.get(serverId);
            //查询zabbix获得流入流出流量的itemid
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(serverId,itemNames,hostIds,true);
            if(result == null || result.isFail()){continue;}
            List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            realDatas.addAll(getHisToryInfo(itemApplications,trendParam,serverId,interfaceDtoInfos,netWorkLinkDtos));
        }
        return realDatas;
    }


    private List<InterfaceReportDto> getHisToryInfo(List<ItemApplication> itemApplications,TrendParam trendParam,Integer serverId,List<InterfaceReportDto> interfaceDtoInfos,List<NetWorkLinkDto> netWorkLinkDtos){
        List<InterfaceReportDto> realDatas = new ArrayList<>();
        HashMap<String, ItemApplication> applicationMap = itemApplications.stream().collect(HashMap::new, (m, v) -> m.put(v.getItemid(), v), HashMap::putAll);
        List<String> chooseTime = trendParam.getChooseTime();
        Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
        Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
        //取出所有的itemID
        List<String> itemIds = itemApplications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
        //判断是否查询趋势
        boolean isTrend = MwReportDateUtil.getDateDay(startTime, endTime);
        List<HistoryValueDto> historyData = getHistoryData(isTrend, itemIds, startTime, endTime, serverId);
        if(CollectionUtils.isEmpty(historyData)){return realDatas;}
        HashMap<String, InterfaceReportDto> reportDtosHashMap = interfaceDtoInfos.stream().collect(HashMap::new, (m, v) -> m.put(v.getHostId() + v.getInterfaceName(), v), HashMap::putAll);
        HashMap<String, NetWorkLinkDto> linkDtoHashMap = netWorkLinkDtos.stream().collect(HashMap::new, (m, v) -> m.put(v.getLinkId(), v), HashMap::putAll);
        //按照itemid分组
        Map<String, List<HistoryValueDto>> listMap = historyData.stream().collect(Collectors.groupingBy(item -> item.getItemid()));
        for (String itemId : listMap.keySet()) {
            ItemApplication application = applicationMap.get(itemId);
            List<HistoryValueDto> historyValueDtos = listMap.get(itemId);
            String name = application.getName();
            String port = "";
            log.info("LinkReportManager{} getHisToryInfo() name:"+name);
            if(StringUtils.isNotBlank(name) && name.contains("]")){
                port = name.substring(name.indexOf("[")+1,name.indexOf("]"));
                name = name.split("]")[1];
            }
            //获取对应线路数据
            InterfaceReportDto interfaceReportDto = reportDtosHashMap.get(application.getHostid() + port);
            if(interfaceReportDto == null){continue;}
            NetWorkLinkDto workLinkDto = linkDtoHashMap.get(interfaceReportDto.getInterfaceID());
            setInterfaceInfo(name,workLinkDto,application,historyValueDtos,interfaceReportDto);
        }
        Collection<InterfaceReportDto> values = reportDtosHashMap.values();
        return new ArrayList<InterfaceReportDto>(values);
    }

    private void setInterfaceInfo(String name,NetWorkLinkDto dto,ItemApplication application,List<HistoryValueDto> valueDtos,InterfaceReportDto interfaceReportDto){
        String bandUnit = dto.getBandUnit();
        String upLinkBandwidth = dto.getUpLinkBandwidth();
        switch (name) {
            case "MW_INTERFACE_IN_TRAFFIC":
                if (!application.getUnits().equals(bandUnit)) {
                    Map<String, String> valueMap = UnitsUtil.getValueMap(upLinkBandwidth, application.getUnits(), bandUnit);
                    upLinkBandwidth = valueMap.get("value");
                }
                interfaceReportDto.setInBandwidth(UnitsUtil.getValueWithUnits(dto.getUpLinkBandwidth(), dto.getBandUnit()));
                //对历史数据进行处理，获取最大值最小值平均值
                TrendDto inTrendDto = ReportUtil.getTrendDto(valueDtos, application.getUnits());
                interfaceReportDto.setInMaxbps(inTrendDto.getValueMax());
                interfaceReportDto.setInMinbps(inTrendDto.getValueMin());
                interfaceReportDto.setInAveragebps(inTrendDto.getValueAvg());

                TrendDto intrendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(valueDtos);
                if (Double.valueOf(upLinkBandwidth) != 0) {
                    interfaceReportDto.setInAvgUse(RoundOff(intrendDtoNotUnit.getValueAvg(), upLinkBandwidth) + "%");
                    interfaceReportDto.setInMaxUse(RoundOff(intrendDtoNotUnit.getValueMax(), upLinkBandwidth) + "%");
                }
                //获取不同的时间占比
                ProportionDto inproportion = getProportion(valueDtos, upLinkBandwidth);
                interfaceReportDto.setInProportionTen(inproportion.getProportionTen() + "%");
                interfaceReportDto.setInProportionFifty(inproportion.getProportionFifty() + "%");
                interfaceReportDto.setInProportionEighty(inproportion.getProportionEighty() + "%");
                interfaceReportDto.setInProportionHundred(inproportion.getProportionHundred() + "%");
                break;
            case "MW_INTERFACE_OUT_TRAFFIC":
                String downLinkBandwidth = dto.getDownLinkBandwidth();
                if (!application.getUnits().equals(bandUnit)) {
                    Map<String, String> valueMap = UnitsUtil.getValueMap(downLinkBandwidth, application.getUnits(), bandUnit);
                    downLinkBandwidth = valueMap.get("value");
                }
                interfaceReportDto.setOutBandwidth(UnitsUtil.getValueWithUnits(dto.getDownLinkBandwidth(), dto.getBandUnit()));
                //对历史数据进行处理，获取最大值，最小值，平均值
                TrendDto outTrendDto = ReportUtil.getTrendDto(valueDtos, application.getUnits());
                interfaceReportDto.setOutMaxbps(outTrendDto.getValueMax());
                interfaceReportDto.setOutMinbps(outTrendDto.getValueMin());
                interfaceReportDto.setOutAveragebps(outTrendDto.getValueAvg());
                //对历史数据进行处理，获取最大，最小，平均利用率
                TrendDto outtrendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(valueDtos);
                if (Double.valueOf(downLinkBandwidth) != 0) {
                    interfaceReportDto.setOutAvgUse(RoundOff(outtrendDtoNotUnit.getValueAvg(), downLinkBandwidth) + "%");
                    interfaceReportDto.setOutMaxUse(RoundOff(outtrendDtoNotUnit.getValueMax(), downLinkBandwidth) + "%");
                }
                //获取不同的时间占比
                ProportionDto outproportion = getProportion(valueDtos, downLinkBandwidth);
                interfaceReportDto.setOutProportionTen(outproportion.getProportionTen() + "%");
                interfaceReportDto.setOutProportionFifty(outproportion.getProportionFifty() + "%");
                interfaceReportDto.setOutProportionEighty(outproportion.getProportionEighty() + "%");
                interfaceReportDto.setOutProportionHundred(outproportion.getProportionHundred() + "%");
                break;
        }
    }

    //判断查询趋势或者历史
    private List<HistoryValueDto> getHistoryData(boolean isTrend,List<String> itemIds,Long startTime,Long endTime,Integer serverId){
        List<HistoryValueDto> valueTimeData = new ArrayList<>();
        if(!isTrend){
            MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemIds, startTime, endTime, 3);
            if(historyRsult == null || historyRsult.isFail()){return valueTimeData;}
            JsonNode node = (JsonNode) historyRsult.getData();
            node.forEach(data -> {
                valueTimeData.add(HistoryValueDto.builder().value(data.get("value").asDouble()).clock(data.get("clock").asLong()).itemid(data.get("itemid").asText()).build());
            });
            return valueTimeData;
        }
        //避免查询量太大，分组查询
        List<List<String>> lists = Lists.partition(itemIds, groupCount);
        for (List<String> list : lists) {
            MWZabbixAPIResult result = mwtpServerAPI.trendBatchGet(serverId, list, startTime, endTime);
            if(result == null || result.isFail()){return valueTimeData;}
            JsonNode node = (JsonNode) result.getData();
            node.forEach(data -> {
                valueTimeData.add(HistoryValueDto.builder().value(data.get("value_avg").asDouble()).clock(data.get("clock").asLong()).itemid(data.get("itemid").asText()).build());
            });
        }
        return valueTimeData;
    }

    private List<InterfaceReportDto> getInterfaceDtoInfo(List<NetWorkLinkDto> netWorkLinkDtos){
        List<InterfaceReportDto> reportDtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(netWorkLinkDtos)){return reportDtos;}
        for (NetWorkLinkDto dto : netWorkLinkDtos) {
            //取线路信息
            InterfaceReportDto interfaceReportDto = new InterfaceReportDto();
            interfaceReportDto.setInterfaceID(dto.getLinkId());
            interfaceReportDto.setCaption(dto.getLinkName());
            String port = "";
            String bandHostid = "";
            Integer bandServerId = 0;
            if (dto.getValuePort().equals("ROOT")) {
                bandHostid = dto.getRootAssetsParam().getAssetsId();
                bandServerId = dto.getRootAssetsParam().getMonitorServerId();
                port = dto.getRootPort();
            } else {
                bandHostid = dto.getTargetAssetsParam().getAssetsId();
                bandServerId = dto.getTargetAssetsParam().getMonitorServerId();
                port = dto.getTargetPort();
            }
            interfaceReportDto.extractFrom(bandServerId,bandHostid,port);
            reportDtos.add(interfaceReportDto);
        }
        return reportDtos;
    }

    /**
     * 获取监控项信息
     * @return
     */
    private List<String> getItemNamesInfo(List<InterfaceReportDto> interfaceDtoInfos){
        List<String> itemNames = new ArrayList<>();
        for (InterfaceReportDto interfaceDtoInfo : interfaceDtoInfos) {
            itemNames.add("[" + interfaceDtoInfo.getInterfaceName() + "]" + ReportConstant.MW_INTERFACE_IN_TRAFFIC);
            itemNames.add("[" + interfaceDtoInfo.getInterfaceName() + "]" + ReportConstant.MW_INTERFACE_OUT_TRAFFIC);
        }
        return itemNames;
    }

    public List<InterfaceReportDto> getLinkInfo(TrendParam trendParam) {
        List<InterfaceReportDto> list = new ArrayList<>();
        LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
        linkDropDownParam.setPageNumber(trendParam.getPageNumber());
        linkDropDownParam.setPageSize(trendParam.getPageSize());
        List<String> faceList=trendParam.getInterfaceIds();
        if(faceList.size()==1){
            linkDropDownParam.setLinkId(faceList.get(0));
        }else if(faceList.size()>1){
            List<String> ilist=new ArrayList<>();
            linkDropDownParam.setIsAdvancedQuery(true);
            for (String item:faceList) {
                ilist.add(item);
            }
            linkDropDownParam.setLinkIds(ilist);
        }
        List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkService.getNetWorkLinkDtos(linkDropDownParam);
        if(null==netWorkLinkDtos){
            return list;
        }
        list = getInterfaceReportBatch(netWorkLinkDtos,trendParam);
        return list;

    }
}
