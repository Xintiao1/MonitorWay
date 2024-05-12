package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.constant.ReportConstant;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.param.PatrolInspectionParam;
import cn.mw.monitor.report.service.MwPatrolInspectionService;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.report.service.manager.PatrolInspectionAreaType;
import cn.mw.monitor.report.service.manager.PatrolInspectionManage;
import cn.mw.monitor.report.util.OfficeNetWorkReportWordUtil;
import cn.mw.monitor.report.util.PatrolInspectionWordUtil;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.server.serverdto.ValueMappingDto;
import cn.mw.monitor.server.serverdto.ValuemapDto;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.param.AssetsStatusQueryParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.MwModelInterfaceCommonParam;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.timetask.api.MwBaseLineValueService;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwPatrolInspectionServiceImpl
 * @Author gengjb
 * @Date 2022/11/1 10:24
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwPatrolInspectionServiceImpl implements MwPatrolInspectionService {


    @Autowired
    private MwReportDao reportDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Value("${report.excelPath}")
    private String path;

    @Value("${report.history.group}")
    private Integer hisToryGroup;

    //模板上传目录
    static final String MODULE = "report-upload";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MwBaseLineValueService baseLineValueService;

    @Autowired
    private MWAlertService alertService;

    //需要查询的监控项
    private List<String> itemName = Arrays.asList("MW_HOST_AVAILABLE","CPU_UTILIZATION","MEMORY_UTILIZATION","PowerSupply_STATUS","FAN_SPEED_SENSOR_STATUS",
            "MW_INTERFACE_STATUS","BGP_PEER_STATUS","OSPF_NBR_STATUS","MW_FRAME_STATUS","IRF_STATUS","DRNI_KEEPALIVE_LINK_STATUS",
            "DRNI_PORT_PORT_STATUS","DRNI_ROLE","VRRP_STATUS","MW_FRAME_STATUS","INTERFACE_IN_UTILIZATION","INTERFACE_OUT_UTILIZATION");

    /**
     * 查询巡检报告
     * @return
     */
    @Override
    public Reply selectPatrolInspection(PatrolInspectionParam patrolInspectionParam) {
        try {
            String labelName = getAreaName(Integer.parseInt(patrolInspectionParam.getReportId()));//获取需要查询的标签
            List<Long> longs = calculitionTime(patrolInspectionParam.getDateType(), patrolInspectionParam.getChooseTime());
            Map<String,Object> param = new HashMap<>();
            //查询以区域标签分类数据
            Map<String,String> hostMaps = new HashMap<>();
            Map<String, Map<Integer, List<String>>> assetsDataMap = getAssetsData(hostMaps,labelName);
            if(CollectionUtils.isEmpty(assetsDataMap))return Reply.ok(param);
            Map<String,List<ValueMappingDto>> valuemapDtoMap = new HashMap<>();//存状态对应数据
            Map<String, List<String>> zabbixValueMap = new HashMap<>();
            //先从redis取值，取不到再查询数据
            String patrolZabbixData = redisTemplate.opsForValue().get("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
            String patrolZabbixValuemapDto = redisTemplate.opsForValue().get("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
            if(StringUtils.isBlank(patrolZabbixData) && StringUtils.isBlank(patrolZabbixValuemapDto)){
                //根据数据查询zabbix
                zabbixValueMap = queryZabbixData(assetsDataMap,valuemapDtoMap,longs);
                if(patrolInspectionParam.getDateType() != 11){
                    //将数据存入redis
                    redisTemplate.opsForValue().set("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(zabbixValueMap), 30, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(valuemapDtoMap), 30, TimeUnit.MINUTES);
                }else{
                    //将数据存入redis
                    redisTemplate.opsForValue().set("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(zabbixValueMap), 2, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(valuemapDtoMap), 2, TimeUnit.MINUTES);
                }
            }else{
                zabbixValueMap = JSONObject.parseObject(patrolZabbixData,Map.class);
                analysisZbbixValueMapping(valuemapDtoMap,patrolZabbixValuemapDto);
            }
            //设置各区域状态
            Map<String,Set<String>> abnormalMap = new HashMap<>();
            List<String> assetsAlert = getAssetsAlert(longs);
            setVariousAreaStatus(param,zabbixValueMap,hostMaps,valuemapDtoMap,abnormalMap,patrolInspectionParam.getDateType(),Integer.parseInt(patrolInspectionParam.getReportId()),assetsAlert);
            //组合运行状态数据
            makeUpRunStatusData(param,abnormalMap,assetsDataMap);
            return Reply.ok(param);
        }catch (Throwable e){
            log.error("查询巡检报告数据失败,失败信息",e);
            return Reply.fail("查询巡检报告数据失败,失败信息"+e.getMessage());
        }
    }

    /**
     * 导出word文件
     * @param request
     * @param response
     */
    @Override
    public void exportWord(HttpServletRequest request, HttpServletResponse response,PatrolInspectionParam patrolInspectionParam) {
        try {
            String labelName = getAreaName(Integer.parseInt(patrolInspectionParam.getReportId()));//获取需要查询的标签
            List<Long> longs = calculitionTime(patrolInspectionParam.getDateType(), patrolInspectionParam.getChooseTime());
            //查询以区域标签分类数据
            Map<String,String> hostMaps = new HashMap<>();
            Map<String, Map<Integer, List<String>>> assetsDataMap = getAssetsData(hostMaps,labelName);
            if(CollectionUtils.isEmpty(assetsDataMap))return;
            Map<String,List<ValueMappingDto>> valuemapDtoMap = new HashMap<>();//存状态对应数据
            //根据数据查询zabbix
            Map<String, List<String>> zabbixValueMap = new HashMap<>();
            //先从redis取值，取不到再查询数据
            String patrolZabbixData = redisTemplate.opsForValue().get("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
            String patrolZabbixValuemapDto = redisTemplate.opsForValue().get("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
            if(StringUtils.isBlank(patrolZabbixData) && StringUtils.isBlank(patrolZabbixValuemapDto)){
                //根据数据查询zabbix
                zabbixValueMap = queryZabbixData(assetsDataMap,valuemapDtoMap,longs);
                //将数据存入redis
                if(patrolInspectionParam.getDateType() != 11){
                    redisTemplate.opsForValue().set("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(zabbixValueMap), 30, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(valuemapDtoMap), 30, TimeUnit.MINUTES);
                }else{
                    redisTemplate.opsForValue().set("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(zabbixValueMap), 2, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().set("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(valuemapDtoMap), 2, TimeUnit.MINUTES);
                }
            }else{
                zabbixValueMap = JSONObject.parseObject(patrolZabbixData,Map.class);
                analysisZbbixValueMapping(valuemapDtoMap,patrolZabbixValuemapDto);
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            //模板填充参数
            Map<String,Object> param = new HashMap<>();
            //设置巡检报告时间
            param.put("${time}",format.format(new Date(longs.get(0)*1000))+"~"+format.format(new Date(longs.get(1)*1000)));
            //设置各区域状态
            Map<String,Set<String>> abnormalMap = new HashMap<>();
            List<String> assetsAlert = getAssetsAlert(longs);
            setVariousAreaStatus(param,zabbixValueMap,hostMaps,valuemapDtoMap,abnormalMap,patrolInspectionParam.getDateType(),Integer.parseInt(patrolInspectionParam.getReportId()),assetsAlert);
            //组合运行状态数据
            makeUpRunStatusData(param,abnormalMap,assetsDataMap);
            //获取模板名称
            String wordTemplateName = getWordTemplateName(Integer.parseInt(patrolInspectionParam.getReportId()));
            //获取模板文件路劲
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator+wordTemplateName;
            log.info("巡检报告word模板路径"+temPath);
            //所有参数设置好，填充word模板
            XWPFDocument document;
            if(Integer.parseInt(patrolInspectionParam.getReportId()) == PatrolInspectionAreaType.PATROL_INSPECTION_AREA.getType()){
                document = PatrolInspectionWordUtil.generateWord(param, temPath);
            }else{
                document = OfficeNetWorkReportWordUtil.generateWord(param, temPath);
            }
            getTempLateRoute(response,document);
        }catch (Throwable e){
            log.error("导出巡检报告Word数据失败,失败信息",e);
        }
    }

    /**
     * 导出excel文件
     * @param request
     * @param response
     */
    @Override
    public void exportExcel(PatrolInspectionParam patrolInspectionParam,HttpServletRequest request, HttpServletResponse response) {
        try {
            //获取excel展示内容
            Map<String, List<PatrolInspectionExcelDto>> listMap = getPatrolInspectionDetailed(patrolInspectionParam);
            //进行excel导出
            export(listMap,response,patrolInspectionParam.getDateType(),Integer.parseInt(patrolInspectionParam.getReportId()));
        }catch (Throwable e){
            log.error("导出巡检报告excel数据失败,失败信息",e);
        }
    }

    @Override
    public void exportInterfaceExcel(PatrolInspectionParam patrolInspectionParam, HttpServletRequest request, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        //获取接口利用率明细信息
        String patrolInterfaceDetail = redisTemplate.opsForValue().get("patrolInterfaceDetail"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
        if(StringUtils.isNotBlank(patrolInterfaceDetail)){
            Map map = JSONObject.parseObject(patrolInterfaceDetail, Map.class);
            try {
                excelWriter = exportReportSetNews(response,PatrolInspectionLinkDto.class);
                int sheetIndex = 0;
                for (Object key : map.keySet()) {
                    JSONArray array = (JSONArray) map.get(key.toString());
                    if(CollectionUtils.isEmpty(array))continue;
                    List<PatrolInspectionLinkDto> dtos = new ArrayList<>();
                    for (Object o : array) {
                        JSONObject object = (JSONObject) o;
                        PatrolInspectionLinkDto inspectionLinkDto = JSONObject.parseObject(object.toJSONString(), PatrolInspectionLinkDto.class);
                        dtos.add(inspectionLinkDto);
                    }
                    //进行数据导出
                    String sheetName = key.toString();//页签名称
                    //数据按照IP地址排序
                    Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                    Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                    List<PatrolInspectionLinkDto> value = dtos.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getIpAddress()), pinyin4jUtil.getStringPinYin(o2.getIpAddress()))).collect(Collectors.toList());
                    HashSet<String> includeColumnFiledNames = new HashSet<>();
                    includeColumnFiledNames.add("assetsName");
                    includeColumnFiledNames.add("ipAddress");
                    includeColumnFiledNames.add("interfaceName");
                    includeColumnFiledNames.add("maxInterfaceInUtilization");
                    includeColumnFiledNames.add("avgInterfaceInUtilization");
                    includeColumnFiledNames.add("minInterfaceInUtilization");
                    includeColumnFiledNames.add("maxInterfaceOutUtilization");
                    includeColumnFiledNames.add("avgInterfaceOutUtilization");
                    includeColumnFiledNames.add("minInterfaceOutUtilization");
                    WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex, sheetName)
                            .includeColumnFiledNames(includeColumnFiledNames)
                            .build();
                    excelWriter.write(value, writeSheet);
                    sheetIndex++;
                }
            }catch (Exception e){
                log.error("导出接口利用率数据失败",e);
            }finally {
                if (excelWriter != null) {
                    excelWriter.finish();
                }
            }
        }
    }

    @Override
    public String getExportWordPath(PatrolInspectionParam patrolInspectionParam) {
        String pathName = "";
        try {
            String labelName = getAreaName(Integer.parseInt(patrolInspectionParam.getReportId()));//获取需要查询的标签
            List<Long> longs = calculitionTime(patrolInspectionParam.getDateType(), patrolInspectionParam.getChooseTime());
            //查询以区域标签分类数据
            Map<String,String> hostMaps = new HashMap<>();
            Map<String, Map<Integer, List<String>>> assetsDataMap = getAssetsData(hostMaps,labelName);
            if(CollectionUtils.isEmpty(assetsDataMap))return "";
            Map<String,List<ValueMappingDto>> valuemapDtoMap = new HashMap<>();//存状态对应数据
            //根据数据查询zabbix
            Map<String, List<String>> zabbixValueMap = queryZabbixData(assetsDataMap,valuemapDtoMap,longs);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            //模板填充参数
            Map<String,Object> param = new HashMap<>();
            //设置巡检报告时间
            param.put("${time}",format.format(new Date(longs.get(0)*1000))+"~"+format.format(new Date(longs.get(1)*1000)));
            //设置各区域状态
            Map<String,Set<String>> abnormalMap = new HashMap<>();
            List<String> assetsAlert = getAssetsAlert(longs);
            setVariousAreaStatus(param,zabbixValueMap,hostMaps,valuemapDtoMap,abnormalMap,patrolInspectionParam.getDateType(),Integer.parseInt(patrolInspectionParam.getReportId()),assetsAlert);
            //组合运行状态数据
            makeUpRunStatusData(param,abnormalMap,assetsDataMap);
            //获取模板文件路劲
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator+"patrolTemplate.docx";
            //所有参数设置好，填充word模板
            XWPFDocument document = PatrolInspectionWordUtil.generateWord(param, temPath);
            //1文件地址+名称
            String name = UUIDUtils.getUUID() + ".docx";
            Date now = new Date();
            String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
            File f = new File(paths);
            if (!f.exists()) {
                f.mkdirs();
            }
            pathName = paths +"/"+ name;
            FileOutputStream outputStream = new FileOutputStream(pathName);
            document.write(outputStream);
            outputStream.close();
        }catch (Throwable e){
            log.error("导出巡检报告Word数据失败,失败信息",e);
        }
        return pathName;
    }


    @Override
    public String getExportExcelPath(PatrolInspectionParam patrolInspectionParam) {
        String pathName = "";
        try {
            //获取excel展示内容
            Map<String, List<PatrolInspectionExcelDto>> listMap = getPatrolInspectionDetailed(patrolInspectionParam);
            //进行excel导出
            //进行数据导出为excel
            //1文件地址+名称
            String name = UUIDUtils.getUUID() + ".xlsx";
            Date now = new Date();
            String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
            File f = new File(paths);
            if (!f.exists()) {
                f.mkdirs();
            }
            pathName = paths +"/"+ name;
            //4创建easyExcel写出对象
            ExcelWriter excelWriter = EasyExcel.write(pathName, PatrolInspectionExcelDto.class).build();
            try {
                int sheetIndex = 0;
                for (Map.Entry<String, List<PatrolInspectionExcelDto>> entry : listMap.entrySet()) {
                    String sheetName = entry.getKey();//页签名称
                    List<PatrolInspectionExcelDto> value = entry.getValue();//导出数据
                    HashSet<String> includeColumnFiledNames = new HashSet<>();
                    includeColumnFiledNames.add("assetsName");
                    includeColumnFiledNames.add("ipAddress");
                    if(sheetName.contains("广域网")){
                        includeColumnFiledNames.add("deviceStatus");
                        includeColumnFiledNames.add("cpuUtilzation");
                        includeColumnFiledNames.add("memoryUtilzation");
                        includeColumnFiledNames.add("powerSupplyStatus");
                        includeColumnFiledNames.add("fanStatus");
                        includeColumnFiledNames.add("interfaceStatus");
                        includeColumnFiledNames.add("bgpStatus");
                        includeColumnFiledNames.add("ospfStatus");
                        includeColumnFiledNames.add("systemStability");
                    }
                    if(sheetName.contains("核心区")){
                        includeColumnFiledNames.add("deviceStatus");
                        includeColumnFiledNames.add("cpuUtilzation");
                        includeColumnFiledNames.add("memoryUtilzation");
                        includeColumnFiledNames.add("powerSupplyStatus");
                        includeColumnFiledNames.add("fanStatus");
                        includeColumnFiledNames.add("interfaceStatus");
                        includeColumnFiledNames.add("ospfStatus");
                        includeColumnFiledNames.add("systemStability");
                    }
                    if(sheetName.contains("总行外网") || sheetName.contains("总行内网")){
                        includeColumnFiledNames.add("deviceStatus");
                        includeColumnFiledNames.add("cpuUtilzation");
                        includeColumnFiledNames.add("memoryUtilzation");
                        includeColumnFiledNames.add("powerSupplyStatus");
                        includeColumnFiledNames.add("fanStatus");
                        includeColumnFiledNames.add("irfStatus");
                        includeColumnFiledNames.add("bgpStatus");
                        includeColumnFiledNames.add("drniLinkStatus");
                        includeColumnFiledNames.add("ippStatus");
                        includeColumnFiledNames.add("drniRole");
                    }
                    if(sheetName.contains("互联网区")){
                        includeColumnFiledNames.add("deviceStatus");
                        includeColumnFiledNames.add("cpuUtilzation");
                        includeColumnFiledNames.add("memoryUtilzation");
                        includeColumnFiledNames.add("powerSupplyStatus");
                        includeColumnFiledNames.add("fanStatus");
                        includeColumnFiledNames.add("irfStatus");
                        includeColumnFiledNames.add("interfaceStatus");
                        includeColumnFiledNames.add("vrrpStatus");
                        includeColumnFiledNames.add("routerStability");
                    }
                    if(sheetName.contains("外联区")){
                        includeColumnFiledNames.add("deviceStatus");
                        includeColumnFiledNames.add("cpuUtilzation");
                        includeColumnFiledNames.add("memoryUtilzation");
                        includeColumnFiledNames.add("powerSupplyStatus");
                        includeColumnFiledNames.add("fanStatus");
                        includeColumnFiledNames.add("irfStatus");
                        includeColumnFiledNames.add("ospfStatus");
                        includeColumnFiledNames.add("vrrpStatus");
                        includeColumnFiledNames.add("systemStability");
                    }
                    if(sheetName.contains("总行综合") || sheetName.contains("总行带外")){
                        includeColumnFiledNames.add("deviceStatus");
                        includeColumnFiledNames.add("cpuUtilzation");
                        includeColumnFiledNames.add("memoryUtilzation");
                        includeColumnFiledNames.add("powerSupplyStatus");
                        includeColumnFiledNames.add("fanStatus");
                        includeColumnFiledNames.add("interfaceStatus");
                    }
                    WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex, sheetName)
                            .includeColumnFiledNames(includeColumnFiledNames)
                            .build();
                    excelWriter.write(value, writeSheet);
                    sheetIndex++;
                }
            }catch (Exception e){
                log.error("导出巡检报告明细数据失败"+e);
            }finally {
                if (excelWriter != null) {
                    excelWriter.finish();
                }
            }
        }catch (Throwable e){
            log.error("导出巡检报告excel数据失败,失败信息",e);
        }
        return pathName;
    }

    /**
     * 获取巡检报告excel明细数据
     * @return
     */
    public Map<String, List<PatrolInspectionExcelDto>> getPatrolInspectionDetailed(PatrolInspectionParam patrolInspectionParam){
        if(StringUtils.isEmpty(patrolInspectionParam.getReportId())){
            return new HashMap<>();
        }

        String labelName = getAreaName(Integer.parseInt(patrolInspectionParam.getReportId()));//获取需要查询的标签
        List<Long> longs = calculitionTime(patrolInspectionParam.getDateType(), patrolInspectionParam.getChooseTime());
        //查询以区域标签分类数据
        Map<String,String> hostMaps = new HashMap<>();
        Map<String, Map<Integer, List<String>>> assetsDataMap = getAssetsData(hostMaps,labelName);
        if(CollectionUtils.isEmpty(assetsDataMap))return null;
        Map<String,List<ValueMappingDto>> valuemapDtoMap = new HashMap<>();//存状态对应数据
        //根据数据查询zabbix
        Map<String, List<String>> zabbixValueMap = new HashMap<>();
        //先从redis取值，取不到再查询数据
        String patrolZabbixData = redisTemplate.opsForValue().get("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
        String patrolZabbixValuemapDto = redisTemplate.opsForValue().get("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId());
        if(StringUtils.isBlank(patrolZabbixData) && StringUtils.isBlank(patrolZabbixValuemapDto)){
            //根据数据查询zabbix
            zabbixValueMap = queryZabbixData(assetsDataMap,valuemapDtoMap,longs);
            //将数据存入redis
            if(patrolInspectionParam.getDateType() != 11){
                redisTemplate.opsForValue().set("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(zabbixValueMap), 30, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(valuemapDtoMap), 30, TimeUnit.MINUTES);
            }else{
                redisTemplate.opsForValue().set("patrolZabbixData"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(zabbixValueMap), 2, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set("patrolZabbixValuemapDto"+patrolInspectionParam.getDateType()+":"+patrolInspectionParam.getReportId(), JSONObject.toJSONString(valuemapDtoMap), 2, TimeUnit.MINUTES);
            }
        }else{
            zabbixValueMap = JSONObject.parseObject(patrolZabbixData,Map.class);
            analysisZbbixValueMapping(valuemapDtoMap,patrolZabbixValuemapDto);
        }
        return setExportDetailed(zabbixValueMap, hostMaps, valuemapDtoMap);
    }

    /**
     * 解析redis中存的zabbix状态映射关系
     * @param valuemapDtoMap
     * @param patrolZabbixValuemapDto
     */
    private void analysisZbbixValueMapping(Map<String,List<ValueMappingDto>> valuemapDtoMap,String patrolZabbixValuemapDto){
        Map map = JSONObject.parseObject(patrolZabbixValuemapDto, Map.class);
        for (Object key : map.keySet()) {
            List<ValueMappingDto> mappingDtos = new ArrayList<>();
            JSONArray array = (JSONArray) map.get(key.toString());
            if(CollectionUtils.isEmpty(array))continue;
            for (Object o : array) {
                JSONObject object = (JSONObject) o;
                ValueMappingDto dto = JSONObject.parseObject(object.toJSONString(), ValueMappingDto.class);
                mappingDtos.add(dto);
            }
            valuemapDtoMap.put(key.toString(),mappingDtos);
        }
    }


    /**
     * 导出word
     * @return
     */
    private void getTempLateRoute(HttpServletResponse response,XWPFDocument document){
        try {
            String fileName = "巡检报告"+System.currentTimeMillis();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8")));
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
        }catch (Throwable e){
            log.error("导出word模板失败，失败信息：",e);
        }
    }

    /**
     * 查询zabbix数据
     * @param assetsDataMap 根据标签值分类的数据
     */
    private Map<String,List<String>> queryZabbixData(Map<String, Map<Integer, List<String>>> assetsDataMap,Map<String,List<ValueMappingDto>> valuemapDtoMap,List<Long> times){
        List<String> interfaceinfoByDesc = getInterfaceinfoByDesc();
        log.info("巡检报告查询分区数据"+assetsDataMap);
        Map<String,List<String>> labelMap = new HashMap<>();
        for (Map.Entry<String, Map<Integer, List<String>>> entry : assetsDataMap.entrySet()){
            String dropValue = entry.getKey();//标签值名称
            log.info("巡检报告开始查询zabbix数据标签"+dropValue);
            Map<Integer, List<String>> serverIdAndHostIds = entry.getValue();
            List<String> zabbixResults = new ArrayList<>();
            for (Integer serverId : serverIdAndHostIds.keySet()) {
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbySearch(serverId,itemName,serverIdAndHostIds.get(serverId));
                //处理返回数据
                zabbixResults.addAll(handleZabbixResult(mwZabbixAPIResult,serverId,valuemapDtoMap,serverIdAndHostIds.get(serverId),times,dropValue,interfaceinfoByDesc));
            }
            labelMap.put(dropValue,zabbixResults);
        }
        log.info("巡检报告查询标签数据结束"+labelMap);
        return labelMap;
    }

    /**
     * 处理zabbix返回结果
     */
    private List<String> handleZabbixResult(MWZabbixAPIResult mwZabbixAPIResult,Integer serverId,Map<String,List<ValueMappingDto>> valuemapDtoMap,List<String> hostIds,List<Long> times,String dropValue,List<String> interfaceinfoByDesc){
        List<String> zabbixNews = new ArrayList<>();//存放zabbix结果信息
        List<String> newhost = new ArrayList<>();
        for (String hostId : hostIds){
            for (String name : itemName){
                newhost.add(hostId+","+name+", "+","+" , ");
            }
        }
        if(mwZabbixAPIResult == null || mwZabbixAPIResult.isFail()){
            zabbixNews.addAll(newhost);
            return zabbixNews;
        }
        List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(mwZabbixAPIResult.getData()), ItemApplication.class);
        log.info("巡检报告最新数据记录"+dropValue+":::"+itemApplications.size());
        List<String> valueMapIds = new ArrayList<>();
        itemApplications.forEach(item->{
            valueMapIds.add(item.getValuemapid());
        });
        if(CollectionUtils.isNotEmpty(valueMapIds)){
            MWZabbixAPIResult valueMapIdResult = mwtpServerAPI.getValueMapById(serverId, valueMapIds);
            if(valueMapIdResult != null && !valueMapIdResult.isFail()){
                List<ValuemapDto> valuemapDtos = JSONArray.parseArray(String.valueOf(valueMapIdResult.getData()), ValuemapDto.class);
                if(CollectionUtils.isNotEmpty(valuemapDtos)){
                    for (ValuemapDto valuemapDto : valuemapDtos) {
                        valuemapDtoMap.put(valuemapDto.getValuemapid(),valuemapDto.getMappings());
                    }
                }
            }
        }
        getHistoryInfo(itemApplications,serverId,times,zabbixNews,valuemapDtoMap,newhost,interfaceinfoByDesc);
        zabbixNews.addAll(newhost);
        log.info("巡检白改历史数据查询返回"+zabbixNews);
        return zabbixNews;
    }


    private List<String> interfaceUtilizationSetValue(String hostId,String name,String units, List<HistoryValueDto> valueTimeData){
        List<String> interfaceUtilizationValues = new ArrayList<>();
        Double max = new Double(0);
        Double total = new Double(0);
        Double min = new Double(100);
        for (HistoryValueDto valueTimeDatum : valueTimeData) {
            Double value = valueTimeDatum.getValue();
            Long clock = valueTimeDatum.getClock();
            if(value > max){
                max = value;
            }
            if(value < min){
                min = value;
            }
            total += value;
        }
        interfaceUtilizationValues.add(hostId+","+name+","+max+",max,"+units);
        interfaceUtilizationValues.add(hostId+","+name+","+(total/valueTimeData.size())+",avg,"+units);
        interfaceUtilizationValues.add(hostId+","+name+","+min+",min,"+units);
        return interfaceUtilizationValues;
    }

    /**
     * 处理历史数据
     * @param valueTimeData
     */
    private void handleHistoryData(List<HistoryValueDto> valueTimeData,String name,Map<String,List<ValueMappingDto>> valuemapDtoMap){
        if(CollectionUtils.isEmpty(valueTimeData))return;
        for (HistoryValueDto valueTimeDatum : valueTimeData) {
            Double value = valueTimeDatum.getValue();
            Long clock = valueTimeDatum.getClock();

        }
    }

    /**
     * 查询区域标签分类的数据
     * @return
     */
    private Map<String,Map<Integer,List<String>>> getAssetsData(Map<String,String> host,String labelName){
        Map<String,Map<Integer,List<String>>> labelClassifyMap = new HashMap<>();
        //查询所有资产，以区域标签分类
        List<PatrolInspectionDto> inspectionDtos = reportDao.selectAssetsByLabel(labelName);
        if(CollectionUtils.isEmpty(inspectionDtos))return labelClassifyMap;
        //进行数据分类
        for (PatrolInspectionDto inspectionDto : inspectionDtos) {
            String assetsId = inspectionDto.getAssetsId();
            String dropValue = inspectionDto.getDropValue();
            Integer serverId = inspectionDto.getServerId();
            if(StringUtils.isBlank(assetsId) || StringUtils.isBlank(dropValue))continue;
            if(labelClassifyMap.containsKey(dropValue)){
                Map<Integer, List<String>> hostMaps = labelClassifyMap.get(dropValue);
                if(hostMaps.containsKey(serverId)){
                    List<String> values = hostMaps.get(serverId);
                    values.add(assetsId);
                    hostMaps.put(serverId,values);
                }else{
                    List<String> values = new ArrayList<>();
                    values.add(assetsId);
                    hostMaps.put(serverId,values);
                }
                labelClassifyMap.put(dropValue,hostMaps);
            }else{
                Map<Integer,List<String>> hostMaps = new HashMap<>();
                List<String> values = new ArrayList<>();
                values.add(assetsId);
                hostMaps.put(serverId,values);
                labelClassifyMap.put(dropValue,hostMaps);
            }
            host.put(assetsId,inspectionDto.getAssetsName()+","+inspectionDto.getIp());
        }
        log.info("巡检报告获取标签资产数据"+labelClassifyMap);
        return labelClassifyMap;
    }

    /**
     * 组织运行状态数据
     * @param param 模板参数
     */
    private void makeUpRunStatusData(Map<String,Object> param,Map<String,Set<String>> abnormalMap,Map<String, Map<Integer, List<String>>> assetsDataMap){
        log.info("查询巡检报告运行状态数据"+abnormalMap);
        log.info("查询巡检报告运行状态数据222"+assetsDataMap);
        List<PatrolInspectionRunStatusDto> runStatusDtos = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, List<String>>> entry : assetsDataMap.entrySet()) {
            PatrolInspectionRunStatusDto runStatusDto = new PatrolInspectionRunStatusDto();
            String key = entry.getKey();
            int count = 0;
            Map<Integer, List<String>> value = entry.getValue();
            if(value == null || value.isEmpty())continue;
            for (Map.Entry<Integer, List<String>> listEntry : value.entrySet()) {
                List<String> listEntryValue = listEntry.getValue();
                if(listEntryValue != null){
                    count = listEntryValue.size();
                }
            }
            Set<String> set = abnormalMap.get(key);
            log.info("获取巡检报告运行状态数据"+set+":::"+key);
            int normalAssetsCount = 0;
            int abnormalAssetsCount = 0;
            if(set == null){
                normalAssetsCount = count;
                abnormalAssetsCount = 0;
            }else{
                normalAssetsCount = count - set.size();
                abnormalAssetsCount = set.size();
            }

            runStatusDto.setName(key);
            runStatusDto.setNormalAssetsCount(normalAssetsCount);
            runStatusDto.setAbnormalAssetsCount(abnormalAssetsCount);
            runStatusDto.setAssetsTotal(abnormalAssetsCount+normalAssetsCount);
            runStatusDtos.add(runStatusDto);
        }
        param.put("runStatus",runStatusDtos);
        log.info("巡检报告运行状态数据记录2222："+param);
    }

    /**
     * 设置各区域状态
     * @param param
     * @param zabbixValueMap
     */
    private void setVariousAreaStatus(Map<String,Object> param,Map<String, List<String>> zabbixValueMap,Map<String,String> hostMaps,Map<String,List<ValueMappingDto>> valuemapDtoMap,
                                      Map<String,Set<String>> abnormalMap,Integer dateType,Integer reportId,List<String> assetsAlert){
        Map<String, MwReportSafeValueDto> safeValueInfoMap = getSafeValueInfo();
        List<Map<String, Object>> baseLineValue = getBaseLineValue();
        log.info("巡检报告zabbix映射关系"+valuemapDtoMap);
        Map<String,List<PatrolInspectionLinkDto>> LinkDtoMaps = new HashMap<>();//记录接口利用率明细
        Map<String, List<Double>> linkMaps = new HashMap<>();
        Set<String> abnormalRecord = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : zabbixValueMap.entrySet()) {
            String key = entry.getKey();//区域信息
            List<String> value = entry.getValue();//主机信息
            Map<String,List<String>> valueMap = new HashMap<>();
            Map<String,List<String>> interfaceMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(value)){
                for (String host : value) {
                    String[] split = host.split(",");
                    if(split.length < 3)continue;
                    String hostId = split[0];
                    String itemNameStr = split[1];
                    String lastValue = split[2];
                    //如果是接口利用率数据，做单独处理
                    if(itemNameStr.contains("INTERFACE_IN_UTILIZATION") || itemNameStr.contains("INTERFACE_OUT_UTILIZATION")){
                        if(interfaceMap.containsKey(key)){
                            List<String> strings = interfaceMap.get(key);
                            strings.add(host);
                            interfaceMap.put(key,strings);
                        }else{
                            List<String> strings = new ArrayList<>();
                            strings.add(host);
                            interfaceMap.put(key,strings);
                        }
                    }
                    if(itemNameStr.contains("[") && itemNameStr.contains("]")){
                        itemNameStr = itemNameStr.split("]")[1];
                    }
                    if(valueMap.containsKey(itemNameStr)){
                        List<String> strings = valueMap.get(itemNameStr);
                        strings.add(hostId+"_"+lastValue+"_"+split[4]);
                        valueMap.put(itemNameStr,strings);
                    }else{
                        List<String> strings = new ArrayList<>();
                        strings.add(hostId+"_"+lastValue+"_"+split[4]);
                        valueMap.put(itemNameStr,strings);
                    }
                }
            }
            List<PatrolInspectionDeviceDto> deviceDtos = new ArrayList<>();
            MwReportSafeValueDto mwReportSafeValueDto = safeValueInfoMap.get(key);
            //解析map数据
            Set<String> abnormalSet = new HashSet<>();
            for (Map.Entry<String, List<String>> listEntry : valueMap.entrySet()) {
                String itemNameStr = listEntry.getKey();//监控名称
                List<String> hostAndValues = listEntry.getValue();//主机和值
                PatrolInspectionDeviceDto deviceDto = PatrolInspectionManage.handleDeviceStatus(itemNameStr, hostAndValues, hostMaps, valuemapDtoMap,abnormalSet,baseLineValue,mwReportSafeValueDto,assetsAlert);
                deviceDtos.add(deviceDto);
            }
            //获取接口利用率的数据
            Map<String,PatrolInspectionDeviceDto> deviceDtoMap = new HashMap<>();
            List<PatrolInspectionLinkDto> patrolInspectionLinkDtos = new ArrayList<>();
            //linkMaps 记录接口利用率信息
            log.info("巡检报告接口区域数据"+interfaceMap);
            linkMaps.putAll(PatrolInspectionManage.handleInterfaceUtilization(interfaceMap, hostMaps, abnormalSet, deviceDtoMap,patrolInspectionLinkDtos,baseLineValue,mwReportSafeValueDto));
            PatrolInspectionManage.handleRunStatusData(abnormalMap,key,abnormalSet);
            abnormalRecord.addAll(abnormalSet);
            LinkDtoMaps.put(key,patrolInspectionLinkDtos);
            log.info("巡检报告word分区值设置"+key+":::"+deviceDtos);
            PatrolInspectionDeviceDto deviceDto = deviceDtoMap.get(key);
            deviceDtos.add(deviceDto);
            //处理接口数据
            PatrolInspectionManage.setPatrolInspectionAreaData(key,deviceDtos,param);
        }
        log.info("巡检报告异常数据记录"+abnormalMap);
        log.info("巡检报告流量明细长度"+LinkDtoMaps.size());
        for (String key : LinkDtoMaps.keySet()) {
            List<PatrolInspectionLinkDto> patrolInspectionLinkDtos = LinkDtoMaps.get(key);
            log.info("巡检报告流量明细数据长度"+key+"::"+patrolInspectionLinkDtos.size());
        }
        if(dateType != 11){
            redisTemplate.opsForValue().set("patrolLinks"+dateType+":"+reportId, JSONObject.toJSONString(linkMaps), 30, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("patrolInterfaceDetail"+dateType+":"+reportId, JSONObject.toJSONString(LinkDtoMaps), 30, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("patroAbnormalIp"+dateType+":"+reportId, JSONObject.toJSONString(abnormalMap), 30, TimeUnit.MINUTES);
        }else{
            redisTemplate.opsForValue().set("patrolLinks"+dateType+":"+reportId, JSONObject.toJSONString(linkMaps), 2, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("patrolInterfaceDetail"+dateType+":"+reportId, JSONObject.toJSONString(LinkDtoMaps), 2, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("patroAbnormalIp"+dateType+":"+reportId, JSONObject.toJSONString(abnormalMap), 2, TimeUnit.MINUTES);
        }
    }


    /**
     * 获取基线设置中存在的值
     */
    private List<Map<String, Object>> getBaseLineValue(){
        List<Map<String, Object>> baseLineVlaue = baseLineValueService.getBaseLineAllData();
        return baseLineVlaue;
    }

    /**
     * 获取状态数字对应翻译数据
     * @param value
     * @param valueMapId
     * @param valuemapDtoMap
     */
    private String getStatusCorrespondingData(String value,String valueMapId,Map<String,List<ValueMappingDto>> valuemapDtoMap){
        if(!valuemapDtoMap.containsKey(valueMapId))return "";
        List<ValueMappingDto> valueMappingDtos = valuemapDtoMap.get(valueMapId);
        for (ValueMappingDto valueMappingDto : valueMappingDtos) {
            if(Double.parseDouble(value) == Double.parseDouble(valueMappingDto.getValue())){
                return valueMappingDto.getNewvalue();
            }
        }
        return "";
    }

    /**
     * 导出excel明细数据设置
     * @param zabbixValueMap
     * @param hostMaps
     * @param valuemapDtoMap
     */
    private  Map<String,List<PatrolInspectionExcelDto>> setExportDetailed(Map<String, List<String>> zabbixValueMap,Map<String,String> hostMaps,Map<String,List<ValueMappingDto>> valuemapDtoMap){
        Map<String,List<PatrolInspectionExcelDto>> excelMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : zabbixValueMap.entrySet()) {
            String key = entry.getKey();//区域信息
            List<String> value = entry.getValue();//主机信息
            Map<String,PatrolInspectionExcelDto> map = new HashMap<>();
            List<PatrolInspectionExcelDto> excelDtos = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(value)){
                for (String host : value) {
                    //先去除字符串中的[]
                    if(host.contains("[")  && host.contains("]")){
                        String[] split = host.split("\\[");
                        String[] split1 = host.split("]");
                        host = split[0]+split1[1];
                    }
                    String[] split = host.split(",");
                    if(split.length < 3)continue;
                    String hostId = split[0];
                    String itemNameStr = split[1];
                    String lastValue = split[2];
                    String assetsName = hostMaps.get(hostId);
                    if(key.contains("总行外网") && assetsName.split(",")[1].equals("11.1.1.128")){
                        log.info("查询报告导出excel总行外网数据"+itemNameStr,split);
                    }
                    if(map.containsKey(hostId)){
                        PatrolInspectionExcelDto excelDto = map.get(hostId);
                        excelDto.setAssetsName(assetsName.split(",")[0]);
                        excelDto.setIpAddress(assetsName.split(",")[1]);
                        nameMatchSetValue(itemNameStr,excelDto,split,valuemapDtoMap);
                        map.put(hostId,excelDto);
                    }else{
                        PatrolInspectionExcelDto excelDto = new PatrolInspectionExcelDto();
                        excelDto.setAssetsName(assetsName.split(",")[0]);
                        excelDto.setIpAddress(assetsName.split(",")[1]);
                        nameMatchSetValue(itemNameStr,excelDto,split,valuemapDtoMap);
                        map.put(hostId,excelDto);
                    }
                }
            }
            if(map != null && map.size() > 0){
                for (String host : map.keySet()) {
                    PatrolInspectionExcelDto excelDto = map.get(host);
                    excelDtos.add(excelDto);
                }
            }
            if(key.contains("总行外网")){
                log.info("查询报告导出excel总行外网数据最终数据"+excelDtos);
            }
            excelMap.put(key,excelDtos);
        }
        return excelMap;
    }

    /**
     * 根据itemName设置导出字段值
     * @param itemNameStr
     * @param excelDto
     * @param split
     * hostId+","+name+","+lastValue+","+units+","+valueMapId
     */
    private void nameMatchSetValue(String itemNameStr,PatrolInspectionExcelDto excelDto,String[] split,Map<String,List<ValueMappingDto>> valueMap){
        String status = getStatusCorrespondingData(split[2], split[4], valueMap);
        if(itemNameStr.contains("MW_HOST_AVAILABLE")){//设备状态
            log.info("巡检报告excel导出状态"+split[2]);
            if(StringUtils.isNotBlank(split[2])){
                if(Double.parseDouble(split[2]) == 1){
                    excelDto.setDeviceStatus("normal");
                }else{
                    excelDto.setDeviceStatus("abnormal");
                }
            }else{
                excelDto.setDeviceStatus("N/A");
            }
        }
        if(itemNameStr.contains("CPU_UTILIZATION")){//CPU利用率
            if(StringUtils.isNotBlank(split[2])){
                double value = new BigDecimal(split[2]).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                excelDto.setCpuUtilzation(value+split[3]);
            }else{
                excelDto.setCpuUtilzation("N/A");
            }
        }
        if(itemNameStr.contains("MEMORY_UTILIZATION")){//内存利用率
            if(StringUtils.isNotBlank(split[2])){
                double value = new BigDecimal(split[2]).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                excelDto.setMemoryUtilzation(value+split[3]);
            }else{
                excelDto.setMemoryUtilzation("N/A");
            }
        }
        if(itemNameStr.contains("PowerSupply_STATUS")){//电源状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setPowerSupplyStatus(status);
            }else{
                excelDto.setPowerSupplyStatus("N/A");
            }
        }
        if(itemNameStr.contains("FAN_SPEED_SENSOR_STATUS")){//风扇状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setFanStatus(status);
            }else{
                excelDto.setFanStatus("N/A");
            }
        }
        if(itemNameStr.contains("MW_INTERFACE_STATUS")){//接口状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setInterfaceStatus(status);
            }else{
                excelDto.setInterfaceStatus("N/A");
            }
        }
        if(itemNameStr.contains("BGP_PEER_STATU")){//BGP状态
            String bgpStatus = excelDto.getBgpStatus();
            if(StringUtils.isNotBlank(status)){
                if(!status.equals("established")){
                    excelDto.setOspfStatus(status);
                    return;
                }
                if(StringUtils.isNotBlank(bgpStatus) && !bgpStatus.equals("established")){return;}
                excelDto.setBgpStatus(status);
            }else{
                excelDto.setBgpStatus("N/A");
            }
        }
        if(itemNameStr.contains("OSPF_NBR_STATUS")){//OSPF状态
            String ospfStatus = excelDto.getOspfStatus();
            if(StringUtils.isNotBlank(status)){
                if(!status.equals("full")){
                    excelDto.setOspfStatus(status);
                    return;
                }
                if(StringUtils.isNotBlank(ospfStatus) && !ospfStatus.equals("full")){return;}
                excelDto.setOspfStatus(status);
            }else{
                excelDto.setOspfStatus("N/A");
            }
        }
        if(itemNameStr.contains("MW_FRAME_STATUS")){//系统稳定性
            if(StringUtils.isNotBlank(status)){
                excelDto.setRouterStability(status);
                excelDto.setSystemStability(status);
            }else{
                excelDto.setRouterStability("N/A");
                excelDto.setSystemStability("N/A");
            }
        }
        if(itemNameStr.contains("IRF_STATUS")){//防火墙状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setIrfStatus(status);
            }else{
                excelDto.setIrfStatus("N/A");
            }
        }
        if(itemNameStr.contains("DRNI_KEEPALIVE_LINK_STATUS")){//链路状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setDrniLinkStatus(status);
            }else{
                excelDto.setDrniLinkStatus("N/A");
            }
        }
        if(itemNameStr.contains("DRNI_PORT_PORT_STATUS")){//rni的IPP口的状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setIppStatus(status);
            }else{
                excelDto.setIppStatus("N/A");
            }
        }
        if(itemNameStr.contains("DRNI_ROLE")){//drni组角色状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setDrniRole(status);
            }else{
                excelDto.setDrniRole("N/A");
            }
        }
        if(itemNameStr.contains("VRRP_STATUS")){//互联网汇聚交换机vrrp状态
            if(StringUtils.isNotBlank(status)){
                excelDto.setVrrpStatus(status);
            }else{
                excelDto.setVrrpStatus("N/A");
            }
        }
    }

    /**
     * 进行导出
     * @param listMap
     * @param response
     */
    private void export(Map<String, List<PatrolInspectionExcelDto>> listMap,HttpServletResponse response,Integer dateType,Integer reportId) throws IOException {
        ExcelWriter excelWriter = null;
        try {
            if(listMap == null || listMap.size() == 0)return;
            excelWriter = exportReportSetNews(response,PatrolInspectionExcelDto.class);
            int sheetIndex = 0;
            for (Map.Entry<String, List<PatrolInspectionExcelDto>> entry : listMap.entrySet()) {
                String sheetName = entry.getKey();//页签名称
                List<PatrolInspectionExcelDto> patrolInspectionExcelDtos = entry.getValue();//导出数据
                //数据按照IP地址排序
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                List<PatrolInspectionExcelDto> value = patrolInspectionExcelDtos.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getIpAddress()), pinyin4jUtil.getStringPinYin(o2.getIpAddress()))).collect(Collectors.toList());
                HashSet<String> includeColumnFiledNames = new HashSet<>();
                includeColumnFiledNames.add("assetsName");
                includeColumnFiledNames.add("ipAddress");
                if(sheetName.contains("广域网")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("interfaceStatus");
                    includeColumnFiledNames.add("bgpStatus");
                    includeColumnFiledNames.add("ospfStatus");
                    includeColumnFiledNames.add("systemStability");
                }
                if(sheetName.contains("核心区")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("interfaceStatus");
                    includeColumnFiledNames.add("ospfStatus");
                    includeColumnFiledNames.add("systemStability");
                }
                if(sheetName.contains("外网") || sheetName.contains("内网")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("irfStatus");
                    includeColumnFiledNames.add("bgpStatus");
                    includeColumnFiledNames.add("drniLinkStatus");
                    includeColumnFiledNames.add("ippStatus");
                    includeColumnFiledNames.add("drniRole");
                }
                if(sheetName.contains("互联网")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("irfStatus");
                    includeColumnFiledNames.add("interfaceStatus");
                    includeColumnFiledNames.add("vrrpStatus");
                    includeColumnFiledNames.add("routerStability");
                }
                if(sheetName.contains("外联")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("irfStatus");
                    includeColumnFiledNames.add("ospfStatus");
                    includeColumnFiledNames.add("vrrpStatus");
                    includeColumnFiledNames.add("systemStability");
                }
                if(sheetName.contains("综合管理") || sheetName.contains("带外管理")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("interfaceStatus");
                }
                if(sheetName.contains("佛山分行") || sheetName.contains("卡中心") || sheetName.equals("总行办公网")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("bgpStatus");
                    includeColumnFiledNames.add("ospfStatus");
                    includeColumnFiledNames.add("interfaceStatus");
                }
                if(sheetName.equals("总行办公网核心")){
                    includeColumnFiledNames.add("deviceStatus");
                    includeColumnFiledNames.add("cpuUtilzation");
                    includeColumnFiledNames.add("memoryUtilzation");
                    includeColumnFiledNames.add("powerSupplyStatus");
                    includeColumnFiledNames.add("fanStatus");
                    includeColumnFiledNames.add("bgpStatus");
                    includeColumnFiledNames.add("ospfStatus");
                    includeColumnFiledNames.add("irfStatus");
                    includeColumnFiledNames.add("interfaceStatus");
                }
                includeColumnFiledNames.add("interfaceInUtilization");
                includeColumnFiledNames.add("interfaceOutUtilization");
                includeColumnFiledNames.add("testingResult");
                //设置接口信息
                setExportExcelIntefaceData(value,sheetName,dateType,reportId);
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex, sheetName)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(value, writeSheet);
                sheetIndex++;
            }
        }catch (Exception e){
            log.error("导出巡检报告明细数据失败"+e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 设置导出excel的接口利用率信息
     * @param value
     */
    private void setExportExcelIntefaceData(List<PatrolInspectionExcelDto> value,String key,Integer dateType,Integer reportId){
        String patrolLinks = redisTemplate.opsForValue().get("patrolLinks"+dateType+":"+reportId);
        String patroAbnormalIps = redisTemplate.opsForValue().get("patroAbnormalIp"+dateType+":"+reportId);//异常数据
        if(StringUtils.isNotBlank(patrolLinks)){
            Map<String, List> linkMaps = JSONObject.parseObject(patrolLinks,Map.class);
            for (PatrolInspectionExcelDto excelDto : value) {
                String assetsName = excelDto.getAssetsName();
                String ipAddress = excelDto.getIpAddress();
                List inUtilization = linkMaps.get(assetsName + ipAddress + "INTERFACE_IN_UTILIZATION");
                List outUtilization = linkMaps.get(assetsName + ipAddress + "INTERFACE_OUT_UTILIZATION");
                //设置最大接口利用率
                BigDecimal inMax =new BigDecimal(0);
                BigDecimal outMax =new BigDecimal(0);
                if(CollectionUtils.isNotEmpty(inUtilization)){
                    for (Object o : inUtilization) {
                        BigDecimal decimal = (BigDecimal) o;
                        if(decimal.compareTo(inMax) == 1){
                            inMax = decimal;
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(outUtilization)){
                    for (Object o : outUtilization) {
                        BigDecimal decimal = (BigDecimal) o;
                        if(decimal.compareTo(outMax) == 1){
                            outMax = decimal;
                        }
                    }
                }
                excelDto.setInterfaceInUtilization(inMax.setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                excelDto.setInterfaceOutUtilization(outMax.setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                if(StringUtils.isNotBlank(patroAbnormalIps)){
                    Map abnormalMap = JSONObject.parseObject(patroAbnormalIps, Map.class);
                    JSONArray array = (JSONArray) abnormalMap.get(key);
                    Set<String> ipSets = new HashSet<>();
                    if(CollectionUtils.isNotEmpty(array)){
                        for (Object o : array) {
                            ipSets.add(o.toString());
                        }
                    }
                    if(ipSets.contains(ipAddress)){
                        excelDto.setTestingResult("异常");
                    }else{
                        excelDto.setTestingResult("正常");
                    }
                }
            }
        }
    }

    /**
     * 设置导出信息
     * @param response
     * @param dtoclass
     * @return
     * @throws IOException
     */
    private ExcelWriter exportReportSetNews(HttpServletResponse response, Class dtoclass) throws IOException {
        String fileName = "巡检报告"+System.currentTimeMillis()+""; //导出文件名
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
        log.info("巡检报告报表时间转换正常"+start+":"+end);
        return times;
    }

    /**
     * 获取需要查询的区域
     * @param reportId
     * @return
     */
    private String getAreaName(Integer reportId){
        if(reportId == PatrolInspectionAreaType.PATROL_INSPECTION_AREA.getType()){
            return PatrolInspectionAreaType.PATROL_INSPECTION_AREA.getName();
        }
        if(reportId == PatrolInspectionAreaType.WORK_AREA.getType()){
            return PatrolInspectionAreaType.WORK_AREA.getName();
        }
        return "";
    }

    /**
     * 获取word模板名称
     * @param reportId
     * @return
     */
    private String getWordTemplateName(Integer reportId){
        if(reportId == PatrolInspectionAreaType.PATROL_INSPECTION_AREA.getType()){
            return "patrolTemplate.docx";
        }
        if(reportId == PatrolInspectionAreaType.WORK_AREA.getType()){
            return "patrolTemplateLanOA.docx";
        }
        return "";
    }

    private void getHistoryInfo(List<ItemApplication> itemApplications,Integer serverId,List<Long> times,List<String> zabbixNews,Map<String,List<ValueMappingDto>> valuemapDtoMap, List<String> newhost,List<String> interfaceNames){
        Map<String,ItemApplication> applicationMap = new HashMap<>();
        itemApplications.forEach(item->{
            applicationMap.put(item.getItemid(),item);
        });
        Map<String, List<ItemApplication>> listMap = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getValue_type()));
        boolean dateDay = getDateDay(times.get(0), times.get(1));
        for (String valueType : listMap.keySet()) {
            List<ItemApplication> applications = listMap.get(valueType);
            //获取所有ItemId
            List<String> itemIds = applications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(itemIds)){continue;}
            List<HistoryValueDto> valueTimeData = new ArrayList<>();
            List<List<String>> lists = Lists.partition(itemIds, hisToryGroup);
            for (List<String> list : lists) {//分组查询历史记录
                if(!dateDay){
                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, list, times.get(0), times.get(1), Integer.parseInt(valueType));
                    if(historyRsult == null || historyRsult.isFail()){continue;}
                    JsonNode node = (JsonNode) historyRsult.getData();
                    log.info("巡检报告历史数据长度"+node.size());
                    node.forEach(data -> {
                        valueTimeData.add(HistoryValueDto.builder().value(data.get("value").asDouble()).clock(data.get("clock").asLong()).itemid(data.get("itemid").asText()).build());
                    });
                    log.info("巡检报告查询zabbix历史数据"+valueTimeData.size()+"::"+historyRsult.getCode());
                    continue;
                }
                MWZabbixAPIResult result = mwtpServerAPI.trendBatchGet(serverId, list, times.get(0), times.get(1));
                if(result == null || result.isFail()){continue;}
                JsonNode node = (JsonNode) result.getData();
                log.info("巡检报告历史数据长度"+node.size());
                node.forEach(data -> {
                    ItemApplication application = applicationMap.get(data.get("itemid").asText());
                    if(application != null && application.getUnits().contains("%")){
                        valueTimeData.add(HistoryValueDto.builder().value(data.get("value_max").asDouble()).clock(data.get("clock").asLong()).itemid(data.get("itemid").asText()).build());
                    }else{
                        valueTimeData.add(HistoryValueDto.builder().value(data.get("value_avg").asDouble()).clock(data.get("clock").asLong()).itemid(data.get("itemid").asText()).build());
                    }
                });
                log.info("巡检报告查询zabbix历史数据"+valueTimeData.size()+"::"+result.getCode());
            }
            if(CollectionUtils.isNotEmpty(valueTimeData)){
                Map<String, List<HistoryValueDto>> collect = valueTimeData.stream().collect(Collectors.groupingBy(item -> item.getItemid()));
                for (String itemId : collect.keySet()) {
                    List<HistoryValueDto> historyValueDtos = collect.get(itemId);
                    ItemApplication application = applicationMap.get(itemId);
                    String lastValue = application.getLastvalue();//监控项值
                    String time = "";
                    if(application.getName().contains("INTERFACE_IN_UTILIZATION") || application.getName().contains("INTERFACE_OUT_UTILIZATION")){
                        String name = application.getName();
                        if(StringUtils.isNotBlank(name) && name.contains("[") && name.contains("]")){
                            name = name.substring(name.indexOf("[")+1,name.indexOf("]"));
                        }
                        if(CollectionUtils.isEmpty(valueTimeData) || interfaceNames.contains(name)){continue;}
                        zabbixNews.addAll(interfaceUtilizationSetValue(application.getHostid(), application.getName(), application.getUnits(), historyValueDtos));
                        continue;
                    }
                    //日志记录 临时
                    if("MW_HOST_AVAILABLE".equals(application.getName())){
                        log.info("可用性状态记录"+valuemapDtoMap);
                    }
                    String value = PatrolInspectionManage.handlePatrolInspectionHistory(application.getName(), historyValueDtos, valuemapDtoMap, application.getValuemapid());
                    if("MW_HOST_AVAILABLE".equals(application.getName())){
                        log.info("可用性状态记录2"+value);
                    }
                    if(StringUtils.isNotBlank(value)){
                        String[] split = value.split(",");
                        lastValue = split[0];
                        time = split[1];
                    }
                    Iterator<String> iterator = newhost.iterator();
                    while (iterator.hasNext()){
                        String next = iterator.next();
                        if(next.contains(application.getHostid()) && application.getName().contains(next.split(",")[1])){
                            iterator.remove();
                        }
                    }
                    zabbixNews.add(application.getHostid()+","+application.getName()+","+lastValue+","+application.getUnits()+","+application.getValuemapid()+","+time);
                }
            }
        }
    }

    /**
     * 获取日期相隔天数
     * @param startTime
     * @param endTime
     * @return
     */
    private boolean getDateDay(Long startTime,Long endTime){
        long day = 1000*60*60;
        if(Math.abs((int)(((endTime * 1000) - (startTime * 1000)) / day)) > 1){return true;}
        return false;
    }

    /**
     * 查询安全门限
     * @return
     */
    private Map<String,MwReportSafeValueDto> getSafeValueInfo(){
        List<MwReportSafeValueDto> mwReportSafeValueDtos = reportDao.selectSafeValueByType(1);
        if(CollectionUtils.isEmpty(mwReportSafeValueDtos)){new HashMap<>();}
        return mwReportSafeValueDtos.stream().collect(Collectors.toMap(MwReportSafeValueDto::getName, a -> a, (k1, K2) -> k1));
    }

    /**
     * 获取定义的资产告警
     */
    private  List<String> getAssetsAlert(List<Long> longs){
        AssetsStatusQueryParam queryParam = new AssetsStatusQueryParam();
        queryParam.setStartTime(new Date(longs.get(0) * 1000));
        queryParam.setEndTime(new Date(longs.get(1) * 1000));
        queryParam.setHostids(ReportConstant.alertAssets);
        List<String> alertAssets = alertService.getAssetsStatusByHisAlert(queryParam);
        if(alertAssets == null){return new ArrayList<>();}
        return alertAssets;
    }

    @Autowired
    private MwModelCommonService mwModelCommonService;

    /**
     * 根据描述信息获取接口信息
     */
    private List<String> getInterfaceinfoByDesc(){
        Map<String, MwReportSafeValueDto> safeValueInfo = getSafeValueInfo();
        if(safeValueInfo.isEmpty()){return new ArrayList<>();}
        List<String> descs = new ArrayList<>();
        for (String areaName : safeValueInfo.keySet()) {
            String interFaceDesc = safeValueInfo.get(areaName).getInterFaceDesc();
            if(StringUtils.isBlank(interFaceDesc)){continue;}
            descs.addAll(Arrays.asList(interFaceDesc.split(",")));
        }
        if(CollectionUtils.isEmpty(descs)){return new ArrayList<>();}
        MwModelInterfaceCommonParam modelInterfaceCommonParam = new MwModelInterfaceCommonParam();
        modelInterfaceCommonParam.setInterfaceDescs(descs);
        List<MwModelInterfaceCommonParam> commonParams = mwModelCommonService.queryInterfaceInfoAlertTag(modelInterfaceCommonParam);
        if(CollectionUtils.isEmpty(commonParams)){return new ArrayList<>();}
        return commonParams.stream().map(MwModelInterfaceCommonParam::getInterfaceDesc).collect(Collectors.toList());
    }
}
