package cn.mw.module.solarwind.service.impl;

import cn.mw.module.solarwind.dao.MWSolarReportDao;
import cn.mw.module.solarwind.dto.*;
import cn.mw.module.solarwind.param.ExportSolarParam;
import cn.mw.module.solarwind.param.InputParam;
import cn.mw.module.solarwind.param.QueryParam;
import cn.mw.module.solarwind.service.MWCreatePdf;
import cn.mw.module.solarwind.service.MWSolarReportService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.SolarTimeDto;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.solarwind.dao.MwMonitorSolarReportDao;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.NewUnits;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author xhy
 * @date 2020/6/22 16:13
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "solarwind", name = "enable", havingValue = "true")
public class MWSolarReportServiceImpl implements MWSolarReportService, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/solarReport");

    @Value("${solarwind.datesource.name}")
    private String dateSourceName;
    @Resource
    MWSolarReportDao mwSolarReportDao;
    @Resource
    MwMonitorSolarReportDao mwMonitorSolarReportDao;
    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Override
    public Reply selectSolarReportList(QueryParam param) {
        try {

            List<InterfaceReportDto> list = new ArrayList<>();
            PageInfo pageInfo=new PageInfo();
           if(param.getIsExport()){
               param.setPageNumber(-1);
               param.setPageSize(0);
           }
            /**
             * 非高级查询
             */
            if (!param.getSeniorchecked()) {
                String tableName = "";
                if (param.getDayType() == 0) {//全天(24小时）
                    tableName = "mw_solar_day_detail_allday";
                } else if (param.getDayType() == 1) { //全天(指定时间的段)
                    tableName = "mw_solar_day_detail_allday_worktime";
                } else if (param.getDayType() == 2) {//工作日（）
                    tableName = "mw_solar_day_detail_workday";
                } else if (param.getDayType() == 3) {//工作日(指定时间的段）
                    tableName = "mw_solar_day_detail_workday_worktime";
                } else {
                    return Reply.fail(ErrorConstant.SOLAR_REPORT_PARAMETER_CODE_306000, ErrorConstant.SOLAR_REPORT_PARAMETER_MSG_306000);
                }
                SolarReportDto solarReportDto = SolarReportDto.builder().tableName(tableName).build();
                if (null!=param.getFixedDate()&&param.getFixedDate().size() > 0) {
                    solarReportDto.setStartTime(param.getFixedDate().get(0));
                    solarReportDto.setEndTime(param.getFixedDate().get(1));
                }
                if (null != param.getCarrierName()) {
                    solarReportDto.setCarrierName(param.getCarrierName());
                }
                if (param.getInterfaceIds().size() > 0) {
                    solarReportDto.setInterfaceIDs(param.getInterfaceIds());
                }
                PageHelper.startPage(param.getPageNumber(), param.getPageSize());
                Map criteria = PropertyUtils.describe(solarReportDto);

                List<InterfaceReportDto> interfaceReportDtos = mwMonitorSolarReportDao.selectList(criteria);
                pageInfo=PageInfo.of(interfaceReportDtos);
                if (interfaceReportDtos.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(interfaceReportDtos.size());
                    List<Future<InterfaceReportDto>> futureList = new ArrayList<>();

                    for (InterfaceReportDto interfaceReportDto : interfaceReportDtos) {
                        MWInterfaceReportThread interFaceReportThread = new MWInterfaceReportThread() {
                            @Override
                            public InterfaceReportDto call() throws Exception {
                                InterfaceReportDto dto = InterfaceReportDto.builder()
                                        .interfaceID(interfaceReportDto.getInterfaceID())
                                        .caption(interfaceReportDto.getCaption())
                                        .inBandwidth(UnitsUtil.getValueWithUnits(interfaceReportDto.getInBandwidth(), NewUnits.BPS.getUnits()))
                                        .inMaxbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getInMaxbps(), NewUnits.BPS.getUnits()))
                                        .inMinbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getInMinbps(), NewUnits.BPS.getUnits()))
                                        .inAveragebps(UnitsUtil.getValueWithUnits(interfaceReportDto.getInAveragebps(), NewUnits.BPS.getUnits()))
                                        .outMaxbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutMaxbps(), NewUnits.BPS.getUnits()))
                                        .outMinbps(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutMinbps(), NewUnits.BPS.getUnits()))
                                        .outAveragebps(UnitsUtil.getValueWithUnits(interfaceReportDto.getOutAveragebps(), NewUnits.BPS.getUnits()))
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
                                return dto;
                            }
                        };
                        Future<InterfaceReportDto> f = executorService.submit(interFaceReportThread);
                        futureList.add(f);
                    }
                    futureList.forEach(f -> {
                        try {
                            InterfaceReportDto interfaceReportDto = f.get(30, TimeUnit.MINUTES);
                            list.add(interfaceReportDto);
                        } catch (Exception e) {
                            f.cancel(true);
                            executorService.shutdown();
                            log.error("fail to selectSolarReportList", e);
                        }
                    });
                    executorService.shutdown();
                }
                pageInfo.setList(list);

            } else {
                /**
                 * 高级查询
                 */
                String startTime = param.getChooseTime().get(0);
                String endTime = param.getChooseTime().get(1);
                List<String> tableNames = getMonthBetween(startTime, endTime);
                String[] tableNameArray = tableNames.toArray(new String[tableNames.size()]);
                SolarReportDto solarReportDto = SolarReportDto.builder()
                        .startTimeDay(param.getTimeValue().get(0))
                        .endTimeDay(param.getTimeValue().get(1))
                        .startTime(startTime + " " + param.getTimeValue().get(0))
                        .endTime(endTime + " " + param.getTimeValue().get(1))
                        .tableNames(tableNameArray)
                        .build();

                if (null != param.getCarrierName()) {
                    solarReportDto.setCarrierName(param.getCarrierName());
                }
                solarReportDto.setPeriodRadio(param.getPeriodRadio());

                PageHelper.startPage(param.getPageNumber(), param.getPageSize());
                if (param.getInterfaceIds().size() > 0) {
                    solarReportDto.setInterfaceIDs(param.getInterfaceIds());
                }
                Map criteria = PropertyUtils.describe(solarReportDto);
                List<InterfaceTable> interfaceTables = mwSolarReportDao.selectList(criteria);
                pageInfo=PageInfo.of(interfaceTables);
                if (interfaceTables.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(interfaceTables.size());
                    List<Future<InterfaceReportDto>> futureList = new ArrayList<>();

                    for (InterfaceTable interfaceTable : interfaceTables) {
                        MWInterfaceReportThread mwInterfaceReportThread = new MWInterfaceReportThread() {
                            @Override
                            public InterfaceReportDto call() throws Exception {

                                SolarReportDto solarDto=new SolarReportDto();
                                solarDto = SolarReportDto.builder()
                                        .startTimeDay(param.getTimeValue().get(0))
                                        .endTimeDay(param.getTimeValue().get(1))
                                        .startTime(startTime + " " + param.getTimeValue().get(0))
                                        .endTime(endTime + " " + param.getTimeValue().get(1))
                                        .tableNames(tableNameArray)
                                        .build();

                                if (null != param.getCarrierName()) {
                                    solarDto.setCarrierName(param.getCarrierName());
                                }
                                solarDto.setPeriodRadio(param.getPeriodRadio());
                                if (param.getInterfaceIds().size() > 0) {
                                    solarDto.setInterfaceIDs(param.getInterfaceIds());
                                }
                                solarDto.setInterfaceID(interfaceTable.getInterfaceID());
                                ProportionDto proportionDto = mwSolarReportDao.newSelectProportion(solarDto);
                                InterfaceReportDto dto = InterfaceReportDto.builder()
                                        .interfaceID(interfaceTable.getInterfaceID())
                                        .caption(interfaceTable.getCaption())
                                        .inBandwidth(UnitsUtil.getValueWithUnits(interfaceTable.getInBandwidth().toString(), NewUnits.bPS.getUnits()))
                                        .inMaxbps(UnitsUtil.getValueWithUnits(interfaceTable.getInMaxbps().toString(), NewUnits.BPS.getUnits()))
                                        .inMinbps(UnitsUtil.getValueWithUnits(interfaceTable.getInMinbps().toString(), NewUnits.BPS.getUnits()))
                                        .inAveragebps(UnitsUtil.getValueWithUnits(interfaceTable.getInAveragebps().toString(), NewUnits.BPS.getUnits()))
                                        .outMaxbps(UnitsUtil.getValueWithUnits(interfaceTable.getOutMaxbps().toString(), NewUnits.BPS.getUnits()))
                                        .outMinbps(UnitsUtil.getValueWithUnits(interfaceTable.getOutMinbps().toString(), NewUnits.BPS.getUnits()))
                                        .outAveragebps(UnitsUtil.getValueWithUnits(interfaceTable.getOutAveragebps().toString(), NewUnits.BPS.getUnits()))
                                        .inMaxUse(interfaceTable.getInMaxUse() + "%")
                                        .inAvgUse(interfaceTable.getInAvgUse() + "%")
                                        .outMaxUse(interfaceTable.getOutMaxUse() + "%")
                                        .outAvgUse(interfaceTable.getOutAvgUse() + "%")
                                        .inProportionTen(proportionDto.getInProportionTen() + "%")
                                        .inProportionFifty(proportionDto.getInProportionFifty() + "%")
                                        .inProportionEighty(proportionDto.getInProportionEighty() + "%")
                                        .inProportionHundred(proportionDto.getInProportionHundred() + "%")
                                        .outProportionTen(proportionDto.getOutProportionTen() + "%")
                                        .outProportionFifty(proportionDto.getOutProportionFifty() + "%")
                                        .outProportionEighty(proportionDto.getOutProportionEighty() + "%")
                                        .outProportionHundred(proportionDto.getOutProportionHundred() + "%")
                                        .build();
                                return dto;
                            }
                        };
                        Future<InterfaceReportDto> f = executorService.submit(mwInterfaceReportThread);
                        futureList.add(f);
                    }
                    futureList.forEach(f -> {
                        try {
                            InterfaceReportDto interfaceReportDto = f.get(30, TimeUnit.MINUTES);
                            if (null != interfaceReportDto) {
                                list.add(interfaceReportDto);

                            }
                        } catch (Exception e) {
                            f.cancel(true);
                            executorService.shutdown();
                            log.error("fail to selectSolarReportList", e);
                        }
                    });
                    executorService.shutdown();
                }
                pageInfo.setList(list);

            }
//            PageInfo pageInfo = new PageInfo<>(list);
//            pageInfo.setList(list);

            logger.info("SOLAR_REPORT_LOG[]report[]报表[]查询Solar报表条件[]{}", param);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectSolarReportList", e);
            return Reply.fail(ErrorConstant.SOLAR_REPORT_SELECT_CODE_306001, ErrorConstant.SOLAR_REPORT_SELECT_MSG_306001);
        }

    }

    private InterfaceReportDto getInterfaceReportDto(SolarDetailDto solarDetailDto) {
        return InterfaceReportDto.builder()
                .interfaceID(solarDetailDto.getInterfaceID())
                .caption(solarDetailDto.getCaption())
                .inBandwidth(UnitsUtil.getValueWithUnits(solarDetailDto.getInBandwidth().toString(), NewUnits.bPS.getUnits()))
                .inMaxbps(UnitsUtil.getValueWithUnits(solarDetailDto.getInMaxbps().toString(), NewUnits.BPS.getUnits()))
                .inMinbps(UnitsUtil.getValueWithUnits(solarDetailDto.getInMinbps().toString(), NewUnits.BPS.getUnits()))
                .inAveragebps(UnitsUtil.getValueWithUnits(solarDetailDto.getInAveragebps().toString(), NewUnits.BPS.getUnits()))
                .outMaxbps(UnitsUtil.getValueWithUnits(solarDetailDto.getOutMaxbps().toString(), NewUnits.BPS.getUnits()))
                .outMinbps(UnitsUtil.getValueWithUnits(solarDetailDto.getOutMinbps().toString(), NewUnits.BPS.getUnits()))
                .outAveragebps(UnitsUtil.getValueWithUnits(solarDetailDto.getOutAveragebps().toString(), NewUnits.BPS.getUnits()))
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

    @Override
    public Reply selectCarrierName() {

        try {
            List<String> carrierNameList = mwSolarReportDao.selectCarrierName();
            logger.info("SOLAR_REPORT_LOG[]report[]报表[]查询Solar报表CarrierName的下拉列表");
            return Reply.ok(carrierNameList);
        } catch (Exception e) {
            log.error("fail to selectCarrierName", e);
            return Reply.fail(ErrorConstant.SOLAR_CARRIERNAME_SELECT_CODE_306002, ErrorConstant.SOLAR_CARRIERNAME_SELECT_MSG_306002);
        }
    }

    @Override
    public Reply selectCaption(String carrierName) {
        try {
            List<Map> captionList = mwSolarReportDao.selectCaption(carrierName);
            logger.info("SOLAR_REPORT_LOG[]report[]报表[]查询Solar报表CarrierName的下拉列表");
            return Reply.ok(captionList);
        } catch (Exception e) {
            log.error("fail to selectCaption", e);
            return Reply.fail(ErrorConstant.SOLAR_CAPTION_SELECT_CODE_306007, ErrorConstant.SOLAR_CAPTION_SELECT_MSG_306007);
        }
    }


    @Override
    public Reply selectHistory(QueryParam param) {
        try {
            List<SolarHistoryDto> list = new ArrayList<>();
            /**
             * 非高级查询
             */
            if (!param.getSeniorchecked()) {
                String tableName = "";
                if (param.getDayType() == 0) {//全天(指定时间的段）
                    tableName = "mw_solar_day_detail_allday";
                } else if (param.getDayType() == 1) { //全天
                    tableName = "mw_solar_day_detail_allday_worktime";
                } else if (param.getDayType() == 2) {//工作日(指定时间的段）
                    tableName = "mw_solar_day_detail_workday";
                } else if (param.getDayType() == 3) {//工作日全天
                    tableName = "mw_solar_day_detail_workday_worktime";
                } else {
                    return Reply.fail(ErrorConstant.SOLAR_REPORT_PARAMETER_CODE_306000, ErrorConstant.SOLAR_REPORT_PARAMETER_MSG_306000);
                }
                SolarReportDto solarReportDto = SolarReportDto.builder().interfaceID(param.getInterfaceID()).tableName(tableName).build();
                if (param.getFixedDate()!=null&&param.getFixedDate().size() > 0) {
                    solarReportDto.setStartTime(param.getFixedDate().get(0));
                    solarReportDto.setEndTime(param.getFixedDate().get(1));
                }
                if (param.getValueType().equals("MAX")) {
                    solarReportDto.setInColumn("inMaxbps");
                    solarReportDto.setOutColumn("outMaxbps");
                } else if (param.getValueType().equals("MIN")) {
                    solarReportDto.setInColumn("inMinbps");
                    solarReportDto.setOutColumn("outMinbps");
                } else {
                    solarReportDto.setInColumn("inAveragebps");
                    solarReportDto.setOutColumn("outAveragebps");
                }

                List<MwHistoryDTO> inMwHistoryDTOS = mwMonitorSolarReportDao.selectInHistoryList(solarReportDto);
                List<MwHistoryDTO> outMwHistoryDTOS = mwMonitorSolarReportDao.selectOutHistoryList(solarReportDto);
                List<MwHistoryDTO> mwHistoryDTOS = new ArrayList<>();
                inMwHistoryDTOS.forEach(inMwHistoryDTO -> {
                    mwHistoryDTOS.add(inMwHistoryDTO);
                });

                Collections.sort(mwHistoryDTOS, new Comparator<MwHistoryDTO>() {
                    @Override
                    public int compare(MwHistoryDTO o1, MwHistoryDTO o2) {
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

                if(mwHistoryDTOS.size()>0&&null!=mwHistoryDTOS.get(0).getValue()&&StringUtils.isNotEmpty(mwHistoryDTOS.get(0).getValue())){
                    String units = UnitsUtil.getValueAndUnits(mwHistoryDTOS.get(0).getValue(), "bps").get("units");
                    List<MwHistoryDTO> newInMwHistoryDTOS = setHistoryValueUnits(inMwHistoryDTOS, units);
                    List<MwHistoryDTO> newOutMwHistoryDTOS = setHistoryValueUnits(outMwHistoryDTOS, units);

                    SolarHistoryDto inSolarHistoryDto = new SolarHistoryDto();
                    SolarHistoryDto outSolarHistoryDto = new SolarHistoryDto();

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
            } else {
                /**
                 * 高级查询
                 */
                String startTime = param.getChooseTime().get(0);
                String endTime = param.getChooseTime().get(1);
                List<String> tableNames = getMonthBetween(startTime, endTime);
                String[] tableNameArray = tableNames.toArray(new String[tableNames.size()]);
                SolarReportDto solarReportDto = SolarReportDto.builder()
                        .interfaceID(param.getInterfaceID())
                        .startTimeDay(param.getTimeValue().get(0))
                        .endTimeDay(param.getTimeValue().get(1))
                        .startTime(startTime + " " + param.getTimeValue().get(0))
                        .endTime(endTime + " " + param.getTimeValue().get(1))
                        .tableNames(tableNameArray)
                        .build();
                if (param.getValueType().equals("MAX")) {
                    solarReportDto.setInColumn("in_Maxbps");
                    solarReportDto.setOutColumn("out_Maxbps");
                } else if (param.getValueType().equals("MIN")) {
                    solarReportDto.setInColumn("in_Minbps");
                    solarReportDto.setOutColumn("out_Minbps");
                } else {
                    solarReportDto.setInColumn("in_Averagebps");
                    solarReportDto.setOutColumn("out_Averagebps");
                }
                if (null != param.getCarrierName()) {
                    solarReportDto.setCarrierName(param.getCarrierName());
                }
                solarReportDto.setPeriodRadio(param.getPeriodRadio());
                List<MwHistoryDTO> inMwHistoryDTOS = mwSolarReportDao.selectInHistory(solarReportDto);
                List<MwHistoryDTO> outMwHistoryDTOS = mwSolarReportDao.selectOutHistory(solarReportDto);
                if (null != inMwHistoryDTOS && inMwHistoryDTOS.size() > 0) {
                    logger.info("进入inMwHistoryDTOS", inMwHistoryDTOS);
                    List<MwHistoryDTO> mwHistoryDTOS = new ArrayList<>();
                    for (MwHistoryDTO inMwHistoryDTO : inMwHistoryDTOS) {
                        mwHistoryDTOS.add(inMwHistoryDTO);
                    }
                    if (null != outMwHistoryDTOS && outMwHistoryDTOS.size() > 0) {
                        logger.info("进入outMwHistoryDTOS{}");
                        for (MwHistoryDTO outMwHistoryDTO : outMwHistoryDTOS) {
                            mwHistoryDTOS.add(outMwHistoryDTO);
                        }
                    }
                    if (mwHistoryDTOS.size() > 0) {
                        logger.info("进入mwHistoryDTOS{}");

                        Collections.sort(mwHistoryDTOS, new Comparator<MwHistoryDTO>() {
                            @Override
                            public int compare(MwHistoryDTO o1, MwHistoryDTO o2) {
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
                        if(mwHistoryDTOS.size()>0&&null!=mwHistoryDTOS.get(0).getValue()&&StringUtils.isNotEmpty(mwHistoryDTOS.get(0).getValue())){
                            String units = UnitsUtil.getValueAndUnits(mwHistoryDTOS.get(0).getValue(), "bps").get("units");
                            logger.info("单位units{}", units);
                            List<MwHistoryDTO> newInMwHistoryDTOS = setHistoryValueUnits(inMwHistoryDTOS, units);
                            List<MwHistoryDTO> newOutMwHistoryDTOS = setHistoryValueUnits(outMwHistoryDTOS, units);
                            logger.info("newInMwHistoryDTOS{}");
                            logger.info("newOutMwHistoryDTOS{}");


                            SolarHistoryDto inSolarHistoryDto = new SolarHistoryDto();
                            SolarHistoryDto outSolarHistoryDto = new SolarHistoryDto();

                            //inSolarHistoryDto.setCaption(param.getCaption());

                            inSolarHistoryDto.setUnit(units);
                            if (inMwHistoryDTOS.size() > 0) {
                                logger.info("inSolarHistoryDto{}");
                                inSolarHistoryDto.setLastUpdateTime(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getDate());
                                inSolarHistoryDto.setLastUpdateValue(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getValue());
                                logger.info("inSolarHistoryDto1{}");
                            }
                            inSolarHistoryDto.setDataList(newInMwHistoryDTOS);
                            inSolarHistoryDto.setTitleName("IN");

                            outSolarHistoryDto.setUnit(units);
                            if (outMwHistoryDTOS.size() > 0) {
                                logger.info("outMwHistoryDTOS{}");
                                outSolarHistoryDto.setLastUpdateTime(outMwHistoryDTOS.get(outMwHistoryDTOS.size() - 1).getDate());
                                outSolarHistoryDto.setLastUpdateValue(outMwHistoryDTOS.get(outMwHistoryDTOS.size() - 1).getValue());
                                logger.info("outMwHistoryDTOS1{}");
                            }
                            outSolarHistoryDto.setDataList(newOutMwHistoryDTOS);
                            outSolarHistoryDto.setTitleName("OUT");
                            list.add(inSolarHistoryDto);
                            list.add(outSolarHistoryDto);
                        }
                    }
                }
            }
            logger.info("SOLAR_REPORT_LOG[]report[]报表[]查询Solar历史报表条件[]{}", param);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to selectHistory", e);
            return Reply.fail(ErrorConstant.SOLAR_REPORT_SELECT_CODE_306001, ErrorConstant.SOLAR_REPORT_SELECT_MSG_306001);
        }

    }


    @Override
    public Reply groupSelect(QueryParam param) {
        try {
            String startTime = "";
            String endTime = "";
            String tableName = "";
            String startTimeDay = "";
            String endTimeDay = "";
            if (!param.getSeniorchecked()) {
                if (param.getDayType() == 0) {//全天(指定时间的段）
                    tableName = "mw_solar_day_detail_allday";
                } else if (param.getDayType() == 1) { //全天
                    tableName = "mw_solar_day_detail_allday_worktime";
                } else if (param.getDayType() == 2) {//工作日(指定时间的段）
                    tableName = "mw_solar_day_detail_workday";
                } else if (param.getDayType() == 3) {//工作日全天
                    tableName = "mw_solar_day_detail_workday_worktime";
                } else {
                    return Reply.fail(ErrorConstant.SOLAR_REPORT_PARAMETER_CODE_306000, ErrorConstant.SOLAR_REPORT_PARAMETER_MSG_306000);
                }
                if(null!=param.getFixedDate()) {
                    startTime = param.getFixedDate().get(0);
                    endTime = param.getFixedDate().get(1);
                }
            } else {
                tableName = dateSourceName + ".dbo.InterfaceTraffic_Detail_" + param.getMouthTime();
                int year = Integer.parseInt(param.getMouthTime().substring(0, 4));
                int mouth = Integer.parseInt(param.getMouthTime().substring(4, 6));
                startTime = MWUtils.getFirstDayOfMonth(year, mouth) + " " + param.getTimeValue().get(0);
                endTime = MWUtils.getLastDayOfMonth(year, mouth) + " " + param.getTimeValue().get(1);
                startTimeDay = param.getTimeValue().get(0);
                endTimeDay = param.getTimeValue().get(1);
            }

            GroupDto groupDto0 = GroupDto.builder().carrierName(param.getCarrierName())
                    .tag(0)
                    .tableName(tableName)
                    .periodRadio(param.getPeriodRadio())
                    .inColumn("inMaxbps")
                    .outColumn("outMaxbps")
                    .percentFront(0f)
                    .percentBack(0.1f)
                    .startTimeDay(startTimeDay)
                    .endTimeDay(endTimeDay)
                    .startTime(startTime)
                    .endTime(endTime)
                    .interfaceIDs(param.getInterfaceIds())
                    .build();
            GroupDto groupDto1 = GroupDto.builder().carrierName(param.getCarrierName())
                    .tag(1)
                    .tableName(tableName)
                    .periodRadio(param.getPeriodRadio())
                    .inColumn("inMaxbps")
                    .outColumn("outMaxbps")
                    .percentFront(0.1f)
                    .percentBack(0.5f)
                    .startTimeDay(startTimeDay)
                    .endTimeDay(endTimeDay)
                    .startTime(startTime)
                    .endTime(endTime)
                    .interfaceIDs(param.getInterfaceIds())
                    .build();
            GroupDto groupDto2 = GroupDto.builder().carrierName(param.getCarrierName())
                    .tag(2)
                    .tableName(tableName)
                    .periodRadio(param.getPeriodRadio())
                    .inColumn("inMaxbps")
                    .outColumn("outMaxbps")
                    .percentFront(0.5f)
                    .percentBack(1f)
                    .startTimeDay(startTimeDay)
                    .endTimeDay(endTimeDay)
                    .startTime(startTime)
                    .endTime(endTime)
                    .interfaceIDs(param.getInterfaceIds())
                    .build();
            GroupDto groupDto3 = GroupDto.builder().carrierName(param.getCarrierName())
                    .tag(3)
                    .tableName(tableName)
                    .periodRadio(param.getPeriodRadio())
                    .inColumn("inAveragebps")
                    .outColumn("outAveragebps")
                    .percentFront(0f)
                    .percentBack(0.1f)
                    .startTimeDay(startTimeDay)
                    .endTimeDay(endTimeDay)
                    .startTime(startTime)
                    .endTime(endTime)
                    .interfaceIDs(param.getInterfaceIds())
                    .build();
            GroupDto groupDto4 = GroupDto.builder().carrierName(param.getCarrierName())
                    .tag(4)
                    .tableName(tableName)
                    .periodRadio(param.getPeriodRadio())
                    .inColumn("inAveragebps")
                    .outColumn("outAveragebps")
                    .percentFront(0.1f)
                    .percentBack(0.5f)
                    .startTimeDay(startTimeDay)
                    .endTimeDay(endTimeDay)
                    .startTime(startTime)
                    .endTime(endTime)
                    .interfaceIDs(param.getInterfaceIds())
                    .build();
            GroupDto groupDto5 = GroupDto.builder().carrierName(param.getCarrierName())
                    .tag(5)
                    .tableName(tableName)
                    .periodRadio(param.getPeriodRadio())
                    .inColumn("inAveragebps")
                    .outColumn("outAveragebps")
                    .percentFront(0.5f)
                    .percentBack(1f)
                    .startTimeDay(startTimeDay)
                    .endTimeDay(endTimeDay)
                    .startTime(startTime)
                    .endTime(endTime)
                    .interfaceIDs(param.getInterfaceIds())
                    .build();
            List<GroupDto> groupDtoList = new ArrayList<>();
            groupDtoList.add(groupDto0);
            groupDtoList.add(groupDto1);
            groupDtoList.add(groupDto2);
            groupDtoList.add(groupDto3);
            groupDtoList.add(groupDto4);
            groupDtoList.add(groupDto5);

            Map<String, String> map1 = new HashMap<>();
            Map<String, String> map2 = new HashMap<>();
            Map<String, String> map3 = new HashMap<>();
            Map<String, String> map4 = new HashMap<>();
            Map<String, String> map5 = new HashMap<>();
            Map<String, String> map6 = new HashMap<>();

            map1.put("caption", param.getCarrierName() + "最大值小于带宽利用率的10%");
            map2.put("caption", param.getCarrierName() + "最大值于带宽利用率的10%-50%");
            map3.put("caption", param.getCarrierName() + "最大值大于带宽利用率的50%");
            map4.put("caption", param.getCarrierName() + "平均值小于带宽利用率的10%");
            map5.put("caption", param.getCarrierName() + "平均值于带宽利用率的10%-50%");
            map6.put("caption", param.getCarrierName() + "平均值大于带宽利用率的50%");

            List<Map<String, String>> list = new ArrayList<>();

            ExecutorService executorService = Executors.newFixedThreadPool(6);
            List<Future<Map<Integer, List<SolarDetailDto>>>> futureList = new ArrayList<>();
            groupDtoList.forEach(groupDto -> {
                Callable<Map<Integer, List<SolarDetailDto>>> callable = new Callable<Map<Integer, List<SolarDetailDto>>>() {
                    @Override
                    public Map<Integer, List<SolarDetailDto>> call() {
                        List<SolarDetailDto> solarDetailDtos = new ArrayList<>();
                        if (param.getSeniorchecked()) {
                            solarDetailDtos = mwSolarReportDao.groupSelectList(groupDto);
                        } else {
                            solarDetailDtos = mwMonitorSolarReportDao.groupSelectList(groupDto);
                        }
                        Map<Integer, List<SolarDetailDto>> map = new HashMap<>();
                        map.put(groupDto.getTag(), solarDetailDtos);
                        return map;
                    }
                };
                Future<Map<Integer, List<SolarDetailDto>>> submit = executorService.submit(callable);
                futureList.add(submit);
            });
            if (futureList.size() > 0) {
                futureList.forEach(f -> {
                    try {
                        Map<Integer, List<SolarDetailDto>> solarDetailDtos = f.get(10, TimeUnit.MINUTES);
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
                                for (SolarDetailDto solarDetailDto : solarDetailDtos.get(integer)) {
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
                        executorService.shutdown();
                    }
                });
            }
            executorService.shutdown();
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to groupSelect", e);
            return Reply.fail(ErrorConstant.SOLAR_GROUP_SELECT_CODE_306006, ErrorConstant.SOLAR_GROUP_SELECT_MSG_306006);
        }
    }


    @Override
    public Reply getHistoryByList(QueryParam param) {
        try {
            List<List<SolarHistoryDto>> solarHistoryList = new ArrayList<>();
            List<Integer> interfaceIds = param.getInterfaceIds();
            if (interfaceIds.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(interfaceIds.size());
                List<Future<List<SolarHistoryDto>>> futureList = new ArrayList<>();
                /**
                 * 非高级查询
                 */
                if (!param.getSeniorchecked()) {
                    String tableName = "";
                    if (param.getDayType() == 0) {//全天 24小时
                        tableName = "mw_solar_day_detail_allday";
                    } else if (param.getDayType() == 1) { //全天(指定时间的段）
                        tableName = "mw_solar_day_detail_allday_worktime";
                    } else if (param.getDayType() == 2) {//工作日24小时
                        tableName = "mw_solar_day_detail_workday";
                    } else if (param.getDayType() == 3) {//工作日(指定时间的段）
                        tableName = "mw_solar_day_detail_workday_worktime";
                    } else {
                        return Reply.fail(ErrorConstant.SOLAR_REPORT_PARAMETER_CODE_306000, ErrorConstant.SOLAR_REPORT_PARAMETER_MSG_306000);
                    }
                    /**
                     * 查询每一个线路的历史记录并返回
                     */
                    for (Integer interfaceID : interfaceIds) {
                        String finalTableName = tableName;
                        MWInterfaceHistoryThread mwInterfaceHistoryThread = new MWInterfaceHistoryThread() {
                            @Override
                            public List<SolarHistoryDto> call() throws Exception {
                                List<SolarHistoryDto> list = new ArrayList<>();
                                SolarReportDto solarReportDto = SolarReportDto.builder().interfaceID(interfaceID).tableName(finalTableName).build();
                                if (param.getFixedDate().size() > 0) {
                                    solarReportDto.setStartTime(param.getFixedDate().get(0));
                                    solarReportDto.setEndTime(param.getFixedDate().get(1));
                                }
                                if (param.getValueType().equals("MAX")) {
                                    solarReportDto.setInColumn("inMaxbps");
                                    solarReportDto.setOutColumn("outMaxbps");
                                } else if (param.getValueType().equals("MIN")) {
                                    solarReportDto.setInColumn("inMinbps");
                                    solarReportDto.setOutColumn("outMinbps");
                                } else {
                                    solarReportDto.setInColumn("inAveragebps");
                                    solarReportDto.setOutColumn("outAveragebps");
                                }
                                List<MwHistoryDTO> inMwHistoryDTOS = mwMonitorSolarReportDao.selectInHistoryList(solarReportDto);
                                List<MwHistoryDTO> outMwHistoryDTOS = mwMonitorSolarReportDao.selectOutHistoryList(solarReportDto);
                                if (inMwHistoryDTOS.size() > 0 && outMwHistoryDTOS.size() > 0) {
                                    String caption = mwSolarReportDao.getCaption(interfaceID);
                                    List<MwHistoryDTO> mwHistoryDTOS = new ArrayList<>();
                                    inMwHistoryDTOS.forEach(inMwHistoryDTO -> {
                                        mwHistoryDTOS.add(inMwHistoryDTO);
                                    });
                                    outMwHistoryDTOS.forEach(outMwHistoryDTO -> {
                                        mwHistoryDTOS.add(outMwHistoryDTO);
                                    });
                                    Collections.sort(mwHistoryDTOS, new Comparator<MwHistoryDTO>() {
                                        @Override
                                        public int compare(MwHistoryDTO o1, MwHistoryDTO o2) {
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
                                        String units = UnitsUtil.getValueAndUnits(mwHistoryDTOS.get(0).getValue(), NewUnits.bPS.getUnits()).get("units");
                                        List<MwHistoryDTO> newInMwHistoryDTOS = setHistoryValueUnits(inMwHistoryDTOS, units);
                                        List<MwHistoryDTO> newOutMwHistoryDTOS = setHistoryValueUnits(outMwHistoryDTOS, units);

                                        SolarHistoryDto inSolarHistoryDto = new SolarHistoryDto();
                                        SolarHistoryDto outSolarHistoryDto = new SolarHistoryDto();
                                        inSolarHistoryDto.setCaption(caption);

                                        inSolarHistoryDto.setUnit(units);
                                        if (inMwHistoryDTOS.size() > 0) {
                                            inSolarHistoryDto.setLastUpdateTime(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getDate());
                                            inSolarHistoryDto.setLastUpdateValue(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getValue());
                                        }
                                        inSolarHistoryDto.setDataList(newInMwHistoryDTOS);
                                        inSolarHistoryDto.setTitleName("IN");

                                        outSolarHistoryDto.setUnit(units);
                                        if (outMwHistoryDTOS.size() > 0) {
                                            outSolarHistoryDto.setLastUpdateTime(outMwHistoryDTOS.get(outMwHistoryDTOS.size() - 1).getDate());
                                            outSolarHistoryDto.setLastUpdateValue(outMwHistoryDTOS.get(outMwHistoryDTOS.size() - 1).getValue());
                                        }
                                        outSolarHistoryDto.setDataList(newOutMwHistoryDTOS);
                                        outSolarHistoryDto.setTitleName("OUT");
                                        list.add(inSolarHistoryDto);
                                        list.add(outSolarHistoryDto);
                                        return list;
                                    }
                                }
                                return null;
                            }
                        };
                        if (null != mwInterfaceHistoryThread) {
                            Future<List<SolarHistoryDto>> submit = executorService.submit(mwInterfaceHistoryThread);
                            futureList.add(submit);
                        }
                    }
                } else {
                    /**
                     * 高级查询
                     */
                    String startTime = param.getChooseTime().get(0);
                    String endTime = param.getChooseTime().get(1);
                    List<String> tableNames = getMonthBetween(startTime, endTime);
                    String[] tableNameArray = tableNames.toArray(new String[tableNames.size()]);
                    // interfaceIds.forEach(interfaceId -> {
                    for (Integer interfaceId : interfaceIds) {
                        MWInterfaceHistoryThread mwInterfaceHistoryThread = new MWInterfaceHistoryThread() {
                            @Override
                            public List<SolarHistoryDto> call() throws Exception {
                                List<SolarHistoryDto> list = new ArrayList<>();
                                SolarReportDto solarReportDto = SolarReportDto.builder()
                                        .interfaceID(interfaceId)
                                        .startTimeDay(param.getTimeValue().get(0))
                                        .endTimeDay(param.getTimeValue().get(1))
                                        .startTime(startTime + " " + param.getTimeValue().get(0))
                                        .endTime(endTime + " " + param.getTimeValue().get(1))
                                        .tableNames(tableNameArray)
                                        .build();
                                if (param.getValueType().equals("MAX")) {
                                    solarReportDto.setInColumn("in_Maxbps");
                                    solarReportDto.setOutColumn("out_Maxbps");
                                } else if (param.getValueType().equals("MIN")) {
                                    solarReportDto.setInColumn("in_Minbps");
                                    solarReportDto.setOutColumn("out_Minbps");
                                } else {
                                    solarReportDto.setInColumn("in_Averagebps");
                                    solarReportDto.setOutColumn("out_Averagebps");
                                }
                                if (null != param.getCarrierName()) {
                                    solarReportDto.setCarrierName(param.getCarrierName());
                                }
                                solarReportDto.setPeriodRadio(param.getPeriodRadio());
                                List<MwHistoryDTO> inMwHistoryDTOS = mwSolarReportDao.selectInHistory(solarReportDto);
                                List<MwHistoryDTO> outMwHistoryDTOS = mwSolarReportDao.selectOutHistory(solarReportDto);
                                SolarHistoryDto inSolarHistoryDto = new SolarHistoryDto();
                                SolarHistoryDto outSolarHistoryDto = new SolarHistoryDto();
                                if (inMwHistoryDTOS.size() > 0 && outMwHistoryDTOS.size() > 0) {
                                    String caption = mwSolarReportDao.getCaption(interfaceId);
                                    List<MwHistoryDTO> mwHistoryDTOS = new ArrayList<>();
                                    if (null != inMwHistoryDTOS && inMwHistoryDTOS.size() > 0) {
                                        logger.info("inMwHistoryDTOS", inMwHistoryDTOS);
                                        inMwHistoryDTOS.forEach(inMwHistoryDTO -> {
                                            mwHistoryDTOS.add(inMwHistoryDTO);
                                        });
                                        if (null != outMwHistoryDTOS && outMwHistoryDTOS.size() > 0) {
                                            outMwHistoryDTOS.forEach(outMwHistoryDTO -> {
                                                mwHistoryDTOS.add(outMwHistoryDTO);
                                            });
                                        }
                                        Collections.sort(mwHistoryDTOS, new Comparator<MwHistoryDTO>() {
                                            @Override
                                            public int compare(MwHistoryDTO o1, MwHistoryDTO o2) {
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
                                            List<MwHistoryDTO> newInMwHistoryDTOS = setHistoryValueUnits(inMwHistoryDTOS, units);
                                            List<MwHistoryDTO> newOutMwHistoryDTOS = setHistoryValueUnits(outMwHistoryDTOS, units);
                                            inSolarHistoryDto.setCaption(caption);
                                            inSolarHistoryDto.setUnit(units);
                                            if (inMwHistoryDTOS.size() > 0) {
                                                inSolarHistoryDto.setLastUpdateTime(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getDate());
                                                inSolarHistoryDto.setLastUpdateValue(inMwHistoryDTOS.get(inMwHistoryDTOS.size() - 1).getValue());
                                            }
                                            inSolarHistoryDto.setDataList(newInMwHistoryDTOS);
                                            inSolarHistoryDto.setTitleName("IN");

                                            outSolarHistoryDto.setTitleName(param.getCaption());
                                            outSolarHistoryDto.setUnit(units);
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
                                }
                                return list;
                            }
                        };
                        if (null != mwInterfaceHistoryThread) {
                            Future<List<SolarHistoryDto>> submit = executorService.submit(mwInterfaceHistoryThread);
                            futureList.add(submit);
                        }
                        // });
                    }
                }
                futureList.forEach(f -> {
                    try {
                        List<SolarHistoryDto> solarHistoryDtos = f.get(30, TimeUnit.MINUTES);
                        if (null != solarHistoryDtos && solarHistoryDtos.size() > 0) {
                            solarHistoryList.add(solarHistoryDtos);
                        }
                    } catch (Exception e) {
                        f.cancel(true);
                        executorService.shutdown();
                        log.error("fail to selectSolarReportList", e);
                    }
                });
                executorService.shutdown();
            }
            logger.info("SOLAR_REPORT_LOG[]report[]报表[]查询Solar历史报表条件[]{}", param);
            return Reply.ok(solarHistoryList);
        } catch (Exception e) {
            log.error("fail to selectSolarReportList", e);
            return Reply.fail(ErrorConstant.SOLAR_REPORT_SELECT_CODE_306001, ErrorConstant.SOLAR_REPORT_SELECT_MSG_306001);
        }
    }

    @Resource
    private MwReportDao mwReportDao;

    @Override
    @Transactional
    public Reply inputSolarTime(InputParam inputParam) {
        try {
            Integer year = Integer.valueOf(inputParam.getExportDate().substring(0, 4));
            Integer mouth = Integer.valueOf(inputParam.getExportDate().substring(4, 6));
            Integer day = Integer.valueOf(inputParam.getExportDate().substring(6, 8));

            String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, year, mouth - 1, day);
            String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, year, mouth - 1, day);

            int timeDateCount = mwReportDao.getTimeDateCount(allDayStartTime, allDayEndTime);

            if (timeDateCount == 0) {//当天的数据还没有从solarWind的数据库里面计算存入monitor的数据库
                String newMouth = mouth > 9 ? String.valueOf(mouth) : "0" + mouth;

                List<Integer> interfaceIds = mwSolarReportDao.selectInterfaceIds();

                String tableName = dateSourceName + ".dbo.InterfaceTraffic_Detail_" + year + newMouth;
                SolarReportDto solarReportDto = SolarReportDto.builder().tableName(tableName).dateTime(MWUtils.strToDateLong(allDayStartTime))
                        .interfaceIDs(interfaceIds)
                        .build();
                solarReportDto.setStartTime(allDayStartTime);
                solarReportDto.setEndTime(allDayEndTime);

                logger.info(allDayStartTime);
                logger.info(allDayEndTime);

                List<SolarDetailDto> allDay = mwSolarReportDao.selectInterfaceDetail(solarReportDto);

                SolarTimeDto solarTimeDto = mwReportDao.selectTime(0);

                String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, year, mouth - 1, day);
                String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, year, mouth - 1, day);


                logger.info(workStartTime);
                logger.info(workEndTime);

                solarReportDto.setStartTime(workStartTime);
                solarReportDto.setEndTimeDay(workEndTime);

                List<SolarDetailDto> allDayWorkTime = mwSolarReportDao.selectInterfaceDetail(solarReportDto);
                String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, year, mouth - 1, day);//存入选中日期的参数
                int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);
                if (count == 0) {
                    /**
                     * 工作日
                     * 0-24小时的数据
                     * 8-17（自定义时间段）点的数据
                     */
                    mwMonitorSolarReportDao.insertWorkDay(allDay);
                    mwMonitorSolarReportDao.insertWorkDayWorkTIme(allDayWorkTime);
                }
                /**
                 * 全天
                 * 0-24小时的数据
                 * 8-17（自定义时间段）点的数据
                 */
                mwMonitorSolarReportDao.insertAllday(allDay);
                mwMonitorSolarReportDao.insertAlldayWorkTime(allDayWorkTime);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to inputSolarTime", e);
           // throw new RuntimeException();
            return Reply.fail(ErrorConstant.SOLAR_INPUT_CODE_306008, ErrorConstant.SOLAR_INPUT_MSG_306008);
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

    @Override
    public void export(ExportSolarParam uParam, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            //需要导出的数据
            List<InterfaceReportDto> list = uParam.getList();
            //将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            //初始化导出字段
            Set<String> includeColumnFiledNames = new HashSet<>();
            if (uParam.getFields() != null && uParam.getFields().size() > 0) {
                includeColumnFiledNames = uParam.getFields();
            } else {
//                includeColumnFiledNames.add("carrierName");
//                includeColumnFiledNames.add("interfaceID");
//                includeColumnFiledNames.add("_XID");
                includeColumnFiledNames.add("caption");
                includeColumnFiledNames.add("inBandwidth");
                includeColumnFiledNames.add("inAveragebps");
                includeColumnFiledNames.add("inMinbps");
                includeColumnFiledNames.add("inMaxbps");
                includeColumnFiledNames.add("inMaxUse");
                includeColumnFiledNames.add("inAvgUse");
                includeColumnFiledNames.add("outMinbps");
                includeColumnFiledNames.add("outAveragebps");
                includeColumnFiledNames.add("outMaxbps");
                includeColumnFiledNames.add("outAvgUse");
                includeColumnFiledNames.add("outMaxUse");
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
            // 背景设置为红色
            //  headWriteCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 11);
            headWriteCellStyle.setWriteFont(headWriteFont);
            // 内容的策略
            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
            // contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
            // 背景绿色
            //     contentWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
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

    @Override
    public void selectAll() {
        mwSolarReportDao.selectAll();
    }

    private List<MwHistoryDTO> setHistoryValueUnits(List<MwHistoryDTO> list, String lastUnits) {
        List<MwHistoryDTO> newList = new ArrayList<>();
        logger.info("setHistoryValueUnits list{},lastUnits{}", list, lastUnits);
        if (list.size() > 0) {
            for (MwHistoryDTO dto : list) {
                // list.forEach(dto -> {
                if (null != dto.getValue() && StringUtils.isNotEmpty(dto.getValue())) {
                    String value = UnitsUtil.getValueMap(dto.getValue(), lastUnits, NewUnits.bPS.getUnits()).get("value");
                    MwHistoryDTO build = MwHistoryDTO.builder().value(value).date(dto.getDate()).build();
                    newList.add(build);
                }
                //   });
            }
        }
        logger.info("setHistoryValueUnits newList{}", newList);
        return newList;
    }


    /**
     * @param minDate 最小时间  2015-01
     * @param maxDate 最大时间 2015-10
     * @return 日期集合 格式为 年-月
     * @throws Exception
     */
    private List<String> getMonthBetween(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat(MWDateConstant.NORM_DATE);//格式化为年月

        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        Date date = sdf.parse(minDate);
        min.setTime(date);
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
        max.setTime(sdf.parse(maxDate));
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

        Calendar curr = min;
        while (curr.before(max)) {
            SimpleDateFormat sdf1 = new SimpleDateFormat(MWDateConstant.MOUTH_DATE);//格式化为年月
            result.add(dateSourceName + ".dbo.InterfaceTraffic_Detail_" + sdf1.format(curr.getTime()));
            curr.add(Calendar.MONTH, 1);
        }

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(">>>>>>>MWSolarReportServiceImpl >>>>>>>>>>");
    }


    /**
     * 导出PDF
     * @param param
     * @param response
     */
    @Override
    public void exportPdf(QueryParam param,HttpServletResponse response,String filePath) {
        try {
            //根据查询条件查询线路数据
            param.setPageNumber(1);
            param.setPageSize(1000000);
            Reply reply = selectSolarReportList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return;
            }
            PageInfo pageInfo= (PageInfo) reply.getData();
            List<InterfaceReportDto> list = pageInfo.getList();
            //进行数据导出
            String[] head = {"线路名称","带宽","接入流量(入向)最大","接入流量(入向)最小","接入流量(入向)平均","接入流量(入向)最大利用率","接入流量(入向)平均利用率","接口流量时间占比(入向)<10%",  "接口流量时间占比(入向)10%-50%","接口流量时间占比(入向)50%-80%","接口流量时间占比(入向)>80%","接出流量(出向)最大","接出流量(出向)最小","接出流量(出向)平均",  "接出流量(出向)最大利用率","接出流量(出向)平均利用率","接口流量时间占比(出向)<10%","接口流量时间占比(出向)10%-50%","接口流量时间占比(出向)50%-80%","接口流量时间占比(出向)>80%"};
            new MWCreatePdf().generatePDFs(head,list,response,filePath);
        }catch (Exception e){

        }
    }

}
