package cn.mw.monitor.report.timer;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.constant.ReportConstant;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.MwReportDiskDto;
import cn.mw.monitor.report.enums.MwReportDiskEnum;
import cn.mw.monitor.report.param.ReportMessageMapperParam;
import cn.mw.monitor.report.service.impl.MwReportTypeEnum;
import cn.mw.monitor.report.util.MwReportSendEmailUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.user.dto.UserOrgDTO;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.util.entity.CustomJavaMailSenderImpl;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 磁盘报表发送今日数据邮件
 * @date 2024/1/26 15:39
 */
@Component
@Slf4j
public class MwDiskReportSendEmailTime {

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Resource
    private MwReportDao mwReportDao;

    @Autowired
    private CustomJavaMailSenderImpl customJavaMailSender;

    @Autowired
    private MWOrgService orgService;

    @Value("${report.history.group}")
    private Integer groupCount;

    @Value("${report.email.filter.value}")
    private Integer filterValue;

    @Value("${report.excelPath}")
    private String path;

    private final String unit = "%";

    private final String REPORT_NAME = "磁盘使用情况报表";

    private final Integer DISTINCT_TYPEID = 72;

//    @Scheduled(cron = "0 0/2 * * * ?")
    public TimeTaskRresult sendDiskReport(){
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //获取资产
            List<MwTangibleassetsTable> assetsInfo = getAssetsInfo();
            Map<String, MwTangibleassetsTable> assetsMap = assetsInfo.stream().filter(item->StringUtils.isNotBlank(item.getAssetsId()) && item.getMonitorServerId() != null && !item.getAssetsTypeSubId().equals(DISTINCT_TYPEID))
                    .collect(Collectors.toMap(s -> s.getMonitorServerId() + s.getAssetsId(), s -> s));
            //获取机构分组的资产
            Map<Integer, List<String>> orgGroupAssets = getOrgGroupAssets(assetsInfo);
            //查询资产的磁盘信息
            Map<Integer, List<String>> groupMap = assetsInfo.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != null)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            List<MwReportDiskDto> diskDtos = new ArrayList<>();
            for (Map.Entry<Integer, List<String>> entry : groupMap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> hostIds = entry.getValue();
                List<List<String>> hosts = Lists.partition(hostIds, groupCount);
                List<ItemApplication> itemApplications = new ArrayList<>();
                for (List<String> host : hosts) {
                    MWZabbixAPIResult zabbixAPIResult = mwtpServerAPI.itemGetbySearch(serverId, ReportConstant.DISK_ITEMS, host);
                    if(zabbixAPIResult == null || zabbixAPIResult.isFail()){continue;}
                    itemApplications.addAll(JSONArray.parseArray(String.valueOf(zabbixAPIResult.getData()), ItemApplication.class));
                }
                //处理数据
                if(CollectionUtils.isEmpty(itemApplications)){continue;}
                diskDtos.addAll(handlerItemData(itemApplications, assetsMap, serverId));
            }
            //将数据按照机构分组
            Map<Integer, List<MwReportDiskDto>> diskOrgMap = diskDataGroupOrg(diskDtos, orgGroupAssets);
            if(diskOrgMap == null || diskOrgMap.isEmpty()){return null;}
            //查询机构下的用户
            Set<Integer> orgIds = diskOrgMap.keySet();
            Map<Integer, List<UserOrgDTO>> userOrgMap = new HashMap<>();
            Reply userListByOrgIds = orgService.getUserListByOrgIds(new ArrayList<>(orgIds));
            if(userListByOrgIds != null){
                userOrgMap = (Map<Integer, List<UserOrgDTO>>) userListByOrgIds.getData();
            }
            //获取通知规则信息
            List<ReportMessageMapperParam> fromMappers = mwReportDao.selectMessageMapperReport(MwReportTypeEnum.DISK_REPORT.getName());
            if(CollectionUtils.isEmpty(fromMappers)){return null;}
            //进行邮件发送
            emailDataHanlder(fromMappers,diskOrgMap,userOrgMap);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("邮件发送磁盘报表:成功");
        }catch (Throwable e){
            log.error("MwDiskReportSendEmail{} sendDiskReport() ERROR::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("邮件发送磁盘报表:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }


    private void emailDataHanlder(List<ReportMessageMapperParam> fromMappers,Map<Integer, List<MwReportDiskDto>> diskOrgMap,Map<Integer, List<UserOrgDTO>> userOrgMap){
        for (ReportMessageMapperParam fromMapper : fromMappers) {
            EmailFrom from = mwReportDao.selectEmailFromReport(fromMapper.getRuleId());
            for (Map.Entry<Integer, List<MwReportDiskDto>> orgEntry : diskOrgMap.entrySet()) {
                List<MwReportDiskDto> reportDiskDtos = orgEntry.getValue();
                if(CollectionUtils.isEmpty(reportDiskDtos)){continue;}
                String pathName = dataExcel(reportDiskDtos);
                //获取机构下用户
                List<UserOrgDTO> userOrgDTOS = userOrgMap.get(orgEntry.getKey());
                //取用户的邮箱信息
                List<String> toEmails = userOrgDTOS.stream().filter(item -> StringUtils.isNotBlank(item.getEmail())).map(UserOrgDTO::getEmail).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(reportDiskDtos) || CollectionUtils.isEmpty(toEmails)){continue;}
                String message = MwReportSendEmailUtil.sendReportEmail(toEmails.toArray(new String[0]), REPORT_NAME, from, Arrays.asList(pathName), customJavaMailSender);
                log.info("MwDiskReportSendEmail{} emailDataHanlder() message:"+message);
            }
        }
    }


    /**
     * 数据转成excel格式
     */
    private String dataExcel(List<MwReportDiskDto> diskDtos){
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("assetsName");
        includeColumnFiledNames.add("assetsIp");
        includeColumnFiledNames.add("diskName");
        includeColumnFiledNames.add("typeName");
        includeColumnFiledNames.add("diskTotal");
        includeColumnFiledNames.add("diskUse");
        includeColumnFiledNames.add("diskUtilization");
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, MwReportDiskDto.class).build();
        WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                .includeColumnFiledNames(includeColumnFiledNames)
                .build();
        excelWriter.write(diskDtos, sheet);
        //6导出
        excelWriter.finish();
        return pathName;
    }


    /**
     * 磁盘数据按机构分组
     */
    private Map<Integer,List<MwReportDiskDto>> diskDataGroupOrg(List<MwReportDiskDto> diskDtos,Map<Integer, List<String>> orgGroupAssets){
        Map<Integer,List<MwReportDiskDto>> diskOrgMap = new HashMap<>();
        for (Map.Entry<Integer, List<String>> orgEntry : orgGroupAssets.entrySet()) {
            List<String> assetsIds = orgEntry.getValue();
            List<MwReportDiskDto> dtos = diskDtos.stream().filter(item -> assetsIds.contains(item.getAssetsId())).collect(Collectors.toList());
            diskOrgMap.put(orgEntry.getKey(),dtos);
        }
        return diskOrgMap;
    }

    /**
     * 处理监控项数据
     * @param itemApplications
     */
    private List<MwReportDiskDto> handlerItemData(List<ItemApplication> itemApplications,Map<String,MwTangibleassetsTable> assetsMap,Integer serverId){
        List<MwReportDiskDto> reportDiskDtos = new ArrayList<>();
        for (ItemApplication itemApplication : itemApplications) {
            String name = itemApplication.getName();
            if(name.contains("[") && name.contains("]")){
                itemApplication.setName(name.split("]")[1]);
                itemApplication.setChName(name.substring(name.indexOf("[")+1,name.indexOf("]")));
            }
        }
        //按照主机和名称分组
        Map<String, List<ItemApplication>> map = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getHostid() + item.getChName()));
        for (Map.Entry<String, List<ItemApplication>> diskEntry : map.entrySet()) {
            List<ItemApplication> applications = diskEntry.getValue();
            MwReportDiskDto diskDto = new MwReportDiskDto();
            MwTangibleassetsTable mwTangibleassetsTable = assetsMap.get(serverId + applications.get(0).getHostid());
            for (ItemApplication application : applications) {
                diskDto.extractFrom(application,mwTangibleassetsTable);
                MwReportDiskEnum diskEnum = MwReportDiskEnum.getDiskEnum(application.getName());
                try {
                    Field declaredField = diskDto.getClass().getDeclaredField(diskEnum.getField());
                    declaredField.setAccessible(true);
                    //单位转换
                    Map<String, String> valueMap = UnitsUtil.getConvertedValue(new BigDecimal(application.getLastvalue()), application.getUnits());
                    if(valueMap != null){
                        declaredField.set(diskDto,valueMap.get("value")+valueMap.get("units"));
                    }else{
                        declaredField.set(diskDto,application.getLastvalue()+application.getUnits());
                    }
                }catch (Throwable e){

                }
            }
            //数据是否需要过滤
            String diskUtilization = diskDto.getDiskUtilization();
            if(StringUtils.isNotBlank(diskUtilization)){
                String value = diskUtilization.replace(unit, "");
                Double filter = filterValue.doubleValue();
                int compareTo = filter.compareTo(Double.parseDouble(value));
                if(compareTo > 0){continue;}
            }
            reportDiskDtos.add(diskDto);
        }
        return reportDiskDtos;
    }



    private Map<Integer,List<String>> getOrgGroupAssets(List<MwTangibleassetsTable> assetsInfo){
        Map<Integer,List<String>> orgAssetsMap = new HashMap<>();
        for (MwTangibleassetsTable tangibleassetsTable : assetsInfo) {
            List<List<Integer>> modelViewOrgIds = tangibleassetsTable.getModelViewOrgIds();
            if(CollectionUtils.isEmpty(modelViewOrgIds)){continue;}
            for (List<Integer> modelViewOrgId : modelViewOrgIds) {
                for (Integer id : modelViewOrgId) {
                    if(orgAssetsMap.containsKey(id)){
                        List<String> assetsIds = orgAssetsMap.get(id);
                        assetsIds.add(tangibleassetsTable.getId());
                        orgAssetsMap.put(id,assetsIds);
                    }else{
                        List<String> assetsIds = new ArrayList<>();
                        assetsIds.add(tangibleassetsTable.getId());
                        orgAssetsMap.put(id, assetsIds);
                    }
                }
            }
        }
        return orgAssetsMap;
    }

    private List<MwTangibleassetsTable> getAssetsInfo(){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(userService.getAdmin());
        return mwAssetsManager.getAssetsTable(assetsParam);
    }

}
