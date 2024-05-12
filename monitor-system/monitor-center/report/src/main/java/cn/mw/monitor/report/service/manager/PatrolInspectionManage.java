package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.constant.ReportConstant;
import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.MwReportSafeValueDto;
import cn.mw.monitor.report.dto.PatrolInspectionDeviceDto;
import cn.mw.monitor.report.dto.PatrolInspectionLinkDto;
import cn.mw.monitor.server.serverdto.ValueMappingDto;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName PatrolInspectionManage
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/11/4 9:30
 * @Version 1.0
 **/
@Slf4j
public class PatrolInspectionManage {


    /**
     * 处理巡检报告历史数据
     */
    public static String handlePatrolInspectionHistory(String itemName, List<HistoryValueDto> valueTimeData, Map<String,List<ValueMappingDto>> valuemapDtoMap,String valueMapId){
        if(itemName.contains("MW_HOST_AVAILABLE")){//设备状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                Long clock = valueTimeDatum.getClock();
                if(value != null){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.equals("CPU_UTILIZATION")){//CPU利用率
            String data;
            Double max = new Double(0);
            Long time = null;
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                Long clock = valueTimeDatum.getClock();
                if(value > max){
                    max = value;
                    time = clock;
                }
            }
            return max+","+time;
        }
        if(itemName.equals("MEMORY_UTILIZATION")){//内存利用率
            String data;
            Double max = new Double(0);
            Long time = null;
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                Long clock = valueTimeDatum.getClock();
                if(value > max){
                    max = value;
                    time = clock;
                }
            }
            return max+","+time;
        }
        if(itemName.contains("PowerSupply_STATUS")){//电源状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("FAN_SPEED_SENSOR_STATUS")){//风扇状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("MW_INTERFACE_STATUS")){//接口状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("BGP_PEER_STATU")){//BGP状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("OSPF_NBR_STATUS")){//OSPF状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("MW_FRAME_STATUS")){//OSPF状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("IRF_STATUS")){//防火墙状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("DRNI_KEEPALIVE_LINK_STATUS")){//链路状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("DRNI_PORT_PORT_STATUS")){//rni的IPP口的状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("DRNI_ROLE")){//drni组角色状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        if(itemName.contains("VRRP_STATUS")){//互联网汇聚交换机vrrp状态
            String data = "";
            for (HistoryValueDto valueTimeDatum : valueTimeData) {
                Double value = valueTimeDatum.getValue();
                if(value == null)continue;
                String status = getStatusCorrespondingData(value.toString(), valueMapId, valuemapDtoMap);
                Long clock = valueTimeDatum.getClock();
                if(StringUtils.isNotBlank(status)){
                    data = value+","+clock;
                    break;
                }
                data = value+","+clock;
            }
            return data;
        }
        return null;
    }


    public static PatrolInspectionDeviceDto handleDeviceStatus(String itemName, List<String> hostAndValues, Map<String,String> hostMaps, Map<String,List<ValueMappingDto>> valuemapDtoMap,
                                                               Set<String> abnormalSet, List<Map<String, Object>> baseLineValue, MwReportSafeValueDto mwReportSafeValueDto,List<String> assetsAlert){
        PatrolInspectionDeviceDto deviceDto = new PatrolInspectionDeviceDto();
        if(itemName.contains("MW_HOST_AVAILABLE")){//设备状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                if(StringUtils.isNotBlank(split[0]) && ReportConstant.alertAssets.contains(split[0]) && assetsAlert.contains(split[0])){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",显示设备信息状态");
                    flag = false;
                    continue;
                }
                if(StringUtils.isNotBlank(split[1]) && Double.parseDouble(split[1]) == 0){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",显示设备信息状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("显示设备信息状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("显示设备信息状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.equals("CPU_UTILIZATION")){//CPU利用率
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                Integer safeValue = null;
                if(mwReportSafeValueDto != null && mwReportSafeValueDto.getCpuSafeValue() != null){
                    safeValue = mwReportSafeValueDto.getCpuSafeValue();
                }
                if(safeValue != null){
                    if(StringUtils.isNotBlank(split[1]) && Double.parseDouble(split[1]) > safeValue){
                        String assetsName = hostMaps.get(split[0]).split(",")[0];
                        String ip = hostMaps.get(split[0]).split(",")[1];
                        message.append(assetsName+"("+ip+")、");
                        abnormalSet.add(ip+",CPU的利用率");
                        flag = false;
                    }
                }else{
                    if(StringUtils.isNotBlank(split[1]) && Double.parseDouble(split[1]) > 85){
                        String assetsName = hostMaps.get(split[0]).split(",")[0];
                        String ip = hostMaps.get(split[0]).split(",")[1];
                        message.append(assetsName+"("+ip+")、");
                        abnormalSet.add(ip+",CPU的利用率");
                        flag = false;
                    }
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("CPU利用率高于健康值");
            }
            if(flag){
                deviceDto.setInspectionContent("CPU的利用率");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("CPU的利用率");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.equals("MEMORY_UTILIZATION")){//内存利用率
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                Integer safeValue = null;
                if(mwReportSafeValueDto != null && mwReportSafeValueDto.getMemorySafeValue() != null){
                    safeValue = mwReportSafeValueDto.getMemorySafeValue();
                }
                if(safeValue != null){
                    if(StringUtils.isNotBlank(split[1]) && Double.parseDouble(split[1]) > safeValue){
                        String assetsName = hostMaps.get(split[0]).split(",")[0];
                        String ip = hostMaps.get(split[0]).split(",")[1];
                        message.append(assetsName+"("+ip+")、");
                        abnormalSet.add(ip+",内存的利用率");
                        flag = false;
                    }
                }else{
                    if(StringUtils.isNotBlank(split[1]) && Double.parseDouble(split[1]) > 85){
                        String assetsName = hostMaps.get(split[0]).split(",")[0];
                        String ip = hostMaps.get(split[0]).split(",")[1];
                        message.append(assetsName+"("+ip+")、");
                        abnormalSet.add(ip+",内存的利用率");
                        flag = false;
                    }
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("内存利用率高于健康值");
            }
            if(flag){
                deviceDto.setInspectionContent("内存的利用率");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("内存的利用率");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("PowerSupply_STATUS")){//电源状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                log.info("巡检报告电源状态映射"+split[1]+"::"+split[2]+":::"+status+"::"+hostMaps.get(split[0]).split(",")[1]);
                if(StringUtils.isNotBlank(split[1]) && (!"normal".equals(status) && !"absent".equals(status))){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",显示设备电源的工作状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("电源状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("显示设备电源的工作状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("显示设备电源的工作状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("FAN_SPEED_SENSOR_STATUS")){//风扇状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                log.info("巡检报告风扇状态映射"+split[1]+"::"+split[2]+":::"+status+"::"+hostMaps.get(split[0]).split(",")[1]);
                if(StringUtils.isNotBlank(split[1]) && !"normal".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",显示设备风扇的工作状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("风扇状态状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("显示设备风扇的工作状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("显示设备风扇的工作状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("MW_INTERFACE_STATUS")){//接口状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"up".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(assetsName+"("+ip+")"))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看与其他设备互联接口状态或查看互联网接入路由器状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("接口状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看与其他设备互联接口状态或查看互联网接入路由器状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看与其他设备互联接口状态或查看互联网接入路由器状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("BGP_PEER_STATU")){//BGP状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"established".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看BGP的邻居状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("BGP状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看BGP的邻居状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看BGP的邻居状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("OSPF_NBR_STATUS")){//OSPF状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"full".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看OSPF的邻居状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("OSPF状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看OSPF的邻居状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看OSPF的邻居状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("MW_FRAME_STATUS")){//OSPF状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"normal".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看框试设备系统稳定性或查看互联网接入路由器的稳定性");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("稳定性状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看框试设备系统稳定性或查看互联网接入路由器的稳定性");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看框试设备系统稳定性或查看互联网接入路由器的稳定性");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("IRF_STATUS")){//防火墙状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"Master".equalsIgnoreCase(status) && !"Standby".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看防火墙堆叠状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("防火墙状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看防火墙堆叠状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看防火墙堆叠状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("DRNI_KEEPALIVE_LINK_STATUS")){//链路状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"up".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看drni的keepalive链路状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("drni的keepalive链路状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看drni的keepalive链路状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看drni的keepalive链路状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("DRNI_PORT_PORT_STATUS")){//rni的IPP口的状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"up".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看drni的IPP口的状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("drni的IPP口状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看drni的IPP口的状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看drni的IPP口的状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("DRNI_ROLE")){//drni组角色状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"Primary".equalsIgnoreCase(status) && !"Secondary".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看drni组角色");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("drni组角色状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看drni组角色");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看drni组角色");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        if(itemName.contains("VRRP_STATUS")){//互联网汇聚交换机vrrp状态
            boolean flag = true;
            StringBuilder message = new StringBuilder();
            for (String hostAndValue : hostAndValues) {
                String[] split = hostAndValue.split("_");
                String status = getStatusCorrespondingData(split[1], split[2], valuemapDtoMap);
                if(StringUtils.isNotBlank(split[1]) && !"master".equalsIgnoreCase(status) && !"Backup".equalsIgnoreCase(status)){
                    String assetsName = hostMaps.get(split[0]).split(",")[0];
                    String ip = hostMaps.get(split[0]).split(",")[1];
                    if(message != null && message.length() > 0 && message.toString().contains(ip))continue;
                    message.append(assetsName+"("+ip+")、");
                    abnormalSet.add(ip+",查看汇聚交换机vrrp状态");
                    flag = false;
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("互联网汇聚交换机vrrp状态异常");
            }
            if(flag){
                deviceDto.setInspectionContent("查看汇聚交换机vrrp状态");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }else{
                deviceDto.setInspectionContent("查看汇聚交换机vrrp状态");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }
        }
        return deviceDto;
    }


    /**
     * 获取状态数字对应翻译数据
     * @param value
     * @param valueMapId
     * @param valuemapDtoMap
     */
    public static String getStatusCorrespondingData(String value,String valueMapId,Map<String,List<ValueMappingDto>> valuemapDtoMap){
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
     * 处理运行状态异常数据
     * @param abnormalMap
     * @param label
     * @param abnormalSet
     */
    public static void handleRunStatusData(Map<String,Set<String>> abnormalMap,String label,Set<String> abnormalSet){
        if(CollectionUtils.isEmpty(abnormalSet)){
            abnormalMap.put(label,abnormalSet);
            return;
        }
        log.info("巡检报告状态"+label+"::"+abnormalSet);
        Iterator<String> iterator = abnormalSet.iterator();
        Set<String> ipSet = new HashSet<>();
        if(label.contains("广域网")){
            while (iterator.hasNext()){
                String next = iterator.next();
                if(next.contains("防火墙堆叠") || next.contains("drni") || next.contains("汇聚交换机") || next.contains("互联接口状态")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        if(label.contains("核心区")){
            while (iterator.hasNext()){
                String next = iterator.next();
                log.info("巡检报告状态核心区"+next);
                if(next.contains("防火墙堆叠") || next.contains("drni") || next.contains("汇聚交换机") || next.contains("互联接口状态")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        if(label.contains("外网") || label.contains("内网")){
            while (iterator.hasNext()){
                String next = iterator.next();
                if(next.contains("互联接口状态") || next.contains("OSPF的邻居") || next.contains("汇聚交换机") || next.contains("系统稳定性") || next.contains("内存")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        if(label.contains("互联网")){
            log.info("过滤异常数据互联网接入333"+abnormalSet);
            while (iterator.hasNext()){
                String next = iterator.next();
                log.info("过滤异常数据互联网接入222"+next);
                if(next.contains("BGP的邻居") || next.contains("OSPF的邻居") || next.contains("查看drni") || next.contains("互联接口状态") || !next.contains(",") || !next.contains(",")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
            log.info("过滤异常数据互联网接入"+ipSet);
        }
        if(label.contains("外联")){
            while (iterator.hasNext()){
                String next = iterator.next();
                if(next.contains("BGP的邻居") || next.contains("互联接口状态") || next.contains("查看drni") || !next.contains(",")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        if(label.contains("综合管理") || label.contains("带外")){
            while (iterator.hasNext()){
                String next = iterator.next();
                if(next.contains("防火墙堆叠") || next.contains("查看drni") || next.contains("汇聚交换机") || next.contains("BGP的邻居") || next.contains("OSPF的邻居") || next.contains("系统稳定性") || next.contains("互联接口状态") || !next.contains(",")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        if(label.contains("佛山分行") || label.contains("卡中心") || label.equals("总行办公网")){
            while (iterator.hasNext()){
                String next = iterator.next();
                if(next.contains("防火墙堆叠") || next.contains("查看drni") || next.contains("汇聚交换机") || next.contains("系统稳定性") || !next.contains(",")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        if(label.equals("总行办公网核心")){
            while (iterator.hasNext()){
                String next = iterator.next();
                if(next.contains("查看drni") || next.contains("汇聚交换机") || next.contains("系统稳定性") || !next.contains(",")){
                    iterator.remove();
                }else{
                    if(next.split(",").length < 2){continue;}
                    ipSet.add(next.split(",")[0]);
                }
            }
        }
        log.info("巡检报告状态222"+label+"::"+ipSet);
        abnormalMap.put(label,ipSet);
    }

    /**
     * 处理查询到的接口利用率信息
     * @param interfaceMap
     */
    public static Map<String,List<Double>> handleInterfaceUtilization(Map<String,List<String>> interfaceMap,Map<String,String> hostMaps,Set<String> abnormalSet,Map<String,PatrolInspectionDeviceDto> deviceDtoMap,
                                                                      List<PatrolInspectionLinkDto> patrolInspectionLinkDtos,List<Map<String, Object>> baseLineValue,MwReportSafeValueDto mwReportSafeValueDto){
        Map<String,List<Double>> linkMaps = new HashMap<>();
        if(interfaceMap == null || interfaceMap.isEmpty())return linkMaps;
        Map<String,List<String>> interfaceValueMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : interfaceMap.entrySet()) {
            StringBuilder message = new StringBuilder();
            PatrolInspectionDeviceDto deviceDto = new PatrolInspectionDeviceDto();
            String key = entry.getKey();//区域信息
            List<String> value = entry.getValue();//该区域所有资产的接口信息
            log.info("巡检报告接口数据利用处理"+key+"::"+value);
            log.info("巡检报告接口数据利用处理222"+hostMaps);
            if(CollectionUtils.isEmpty(value))continue;
            for (String hoststr : value) {
                String[] split = hoststr.split(",");
                String hostId = split[0];
                String itemNameStr = split[1];
                String lastValue = split[2];
                if(StringUtils.isBlank(lastValue))continue;
                double v = new BigDecimal(lastValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                String hostNameAndIp = hostMaps.get(hostId);
                Integer safeValue = null;
                if(mwReportSafeValueDto != null && mwReportSafeValueDto.getInterfaceSafeValue() != null){
                    safeValue = mwReportSafeValueDto.getInterfaceSafeValue();
                }
                if(safeValue != null){
                    if(v > safeValue){
                        if(message != null && message.length() > 0 && message.toString().contains(hostNameAndIp.split(",")[1]))continue;
                        abnormalSet.add(hostNameAndIp.split(",")[1]+",接口利用率异常");
                        message.append(hostNameAndIp.split(",")[0]+"("+hostNameAndIp.split(",")[1]+")、");
                    }
                }else{
                    if(v > 85){
                        if(message != null && message.length() > 0 && message.toString().contains(hostNameAndIp.split(",")[1]))continue;
                        abnormalSet.add(hostNameAndIp.split(",")[1]+",接口利用率异常");
                        message.append(hostNameAndIp.split(",")[0]+"("+hostNameAndIp.split(",")[1]+")、");
                    }
                }
                String ip = hostNameAndIp.split(",")[1];
                if(interfaceValueMap.containsKey(hostNameAndIp.split(",")[0]+","+itemNameStr+","+ip)){
                    List<String> strings = interfaceValueMap.get(hostNameAndIp.split(",")[0] + "," + itemNameStr+","+ip);
                    strings.add(lastValue+","+split[3]);
                    interfaceValueMap.put(hostNameAndIp.split(",")[0] + "," + itemNameStr+","+ip,strings);
                }else{
                    List<String> strings = new ArrayList<>();
                    strings.add(lastValue+","+split[3]);
                    interfaceValueMap.put(hostNameAndIp.split(",")[0] + "," + itemNameStr+","+ip,strings);
                }
                String linkKey = null;
                if(itemNameStr.contains("INTERFACE_IN_UTILIZATION")){
                    linkKey = hostNameAndIp.split(",")[0] + ip+"INTERFACE_IN_UTILIZATION";
                }
                if(itemNameStr.contains("INTERFACE_OUT_UTILIZATION")){
                    linkKey = hostNameAndIp.split(",")[0] + ip+"INTERFACE_OUT_UTILIZATION";
                }
                //存储接口利用率信息
                if(linkMaps.containsKey(linkKey)){
                    List<Double> doubles = linkMaps.get(linkKey);
                    doubles.add(v);
                    linkMaps.put(linkKey,doubles);
                }else{
                    List<Double> doubles = new ArrayList<>();
                    doubles.add(v);
                    linkMaps.put(linkKey,doubles);
                }
            }
            if(message.length() > 0){
                message.insert(0,"资产名称为");
                message.deleteCharAt(message.length() - 1);
                message.append("接口利用率异常");
                deviceDto.setInspectionContent("查看接口利用率");
                deviceDto.setInspectionResult("异常");
                deviceDto.setMessage(message.toString());
            }else{
                deviceDto.setInspectionContent("查看接口利用率");
                deviceDto.setInspectionResult("正常");
                deviceDto.setMessage("");
            }
            deviceDtoMap.put(key,deviceDto);
            //获取接口利用率明细数据
            patrolInspectionLinkDtos.addAll(setInterfaceUtilizationDetail(interfaceValueMap));
        }
        return linkMaps;
    }

    /**
     * 设置接口利用率明细
     */
    public static List<PatrolInspectionLinkDto> setInterfaceUtilizationDetail( Map<String,List<String>> interfaceValueMap){
        List<PatrolInspectionLinkDto> patrolInspectionLinkDtos = new ArrayList<>();
        if(interfaceValueMap == null || interfaceValueMap.isEmpty())return patrolInspectionLinkDtos;
        Map<String,PatrolInspectionLinkDto> listMap = new HashMap<>();
        for (String key : interfaceValueMap.keySet()) {
            List<String> values = interfaceValueMap.get(key);
            String[] split = key.split(",");
            Double max = new Double(0);
            Double avg = new Double(0);
            Double min = new Double(0);
            for (String value : values) {
                String[] split1 = value.split(",");
                if(split1[1].equals("max")){
                    max = Double.parseDouble(split1[0]);
                }
                if(split1[1].equals("min")){
                    min = Double.parseDouble(split1[0]);
                }
                if(split1[1].equals("avg")){
                    avg = Double.parseDouble(split1[0]);
                }
            }
            //取出接口名称
            int fromSize = split[1].indexOf("[");
            int toSize = split[1].indexOf("]");
            String interfaceName = split[1].substring(fromSize+1, toSize);
            if(listMap.containsKey(split[0]+interfaceName)){
                PatrolInspectionLinkDto linkDto = listMap.get(split[0] + interfaceName);
                linkDto.setAssetsName(split[0]);
                linkDto.setInterfaceName(interfaceName);
                linkDto.setIpAddress(split[2]);
                if(split[1].contains("INTERFACE_IN_UTILIZATION")){
                    linkDto.setMaxInterfaceInUtilization(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setMinInterfaceInUtilization(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setAvgInterfaceInUtilization(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                }
                if(split[1].contains("INTERFACE_OUT_UTILIZATION")){
                    linkDto.setMaxInterfaceOutUtilization(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setMinInterfaceOutUtilization(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setAvgInterfaceOutUtilization(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                }
                listMap.put(split[0] + interfaceName,linkDto);
            }else{
                PatrolInspectionLinkDto linkDto = new PatrolInspectionLinkDto();
                linkDto.setAssetsName(split[0]);
                linkDto.setInterfaceName(interfaceName);
                linkDto.setIpAddress(split[2]);
                if(split[1].contains("INTERFACE_IN_UTILIZATION")){
                    linkDto.setMaxInterfaceInUtilization(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setMinInterfaceInUtilization(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setAvgInterfaceInUtilization(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                }
                if(split[1].contains("INTERFACE_OUT_UTILIZATION")){
                    linkDto.setMaxInterfaceOutUtilization(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setMinInterfaceOutUtilization(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                    linkDto.setAvgInterfaceOutUtilization(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                }
                listMap.put(split[0] + interfaceName,linkDto);
            }
        }
        if(listMap != null && !listMap.isEmpty()){
            for (String key : listMap.keySet()) {
                patrolInspectionLinkDtos.add(listMap.get(key));
            }
        }
        return patrolInspectionLinkDtos;
    }

    /**
     * 设置巡检报告各区域数据
     * @param deviceDtos
     * @param param
     */
    public static void setPatrolInspectionAreaData(String area,List<PatrolInspectionDeviceDto> deviceDtos,Map<String,Object> param){
        if(area.contains("广域网")){
            param.put("wideareaNetWork",deviceDtos);
        }
        if(area.contains("核心区")){
            param.put("coreArea",deviceDtos);
        }
        if(area.contains("外网")){
            param.put("totalLineOuterNet",deviceDtos);
        }
        if(area.contains("内网")){
            param.put("totalLineIntraNet",deviceDtos);
        }
        if(area.contains("互联网")){
            param.put("interNetArea",deviceDtos);
        }
        if(area.contains("外联")){
            param.put("outreachArea",deviceDtos);
        }
        if(area.contains("综合")){
            param.put("totalLineSynthesize",deviceDtos);
        }
        if(area.contains("带外")){
            param.put("totalLineOutBand",deviceDtos);
        }
        if(area.contains("佛山分行")){
            param.put("foShanBranch",deviceDtos);
        }
        if(area.contains("卡中心")){
            param.put("cardCore",deviceDtos);
        }
        if(area.equals("总行办公网")){
            param.put("headOfficeNetWork",deviceDtos);
        }
        if(area.equals("总行办公网核心")){
            param.put("headOfficeNetWorkCore",deviceDtos);
        }
    }

    public static Double getHealthValue(List<Map<String, Object>> baseLineValue,String hostId,String itemName){
        Double healthValue = null;
        if(CollectionUtils.isEmpty(baseLineValue) || StringUtils.isBlank(hostId) || StringUtils.isBlank(itemName))return healthValue;
        for (Map<String, Object> map : baseLineValue) {
            Object assetsId = map.get("assetsId");
            Object baseLineItemName = map.get("itemName");
            Object value = map.get("value");
            if(assetsId == null || baseLineItemName == null || value == null)continue;
            if(hostId.equals(assetsId.toString()) && (itemName.contains(baseLineItemName.toString()) || baseLineItemName.toString().contains(itemName))){
                //取出value中的数字
                healthValue = Double.parseDouble(value.toString().split("%")[0]);
                break;
            }
        }
        return healthValue;
    }


}
