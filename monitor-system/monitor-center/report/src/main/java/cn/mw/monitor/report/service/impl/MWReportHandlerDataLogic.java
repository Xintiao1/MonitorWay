package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.dto.MWMplsCacheDataDto;
import cn.mw.monitor.report.dto.TrendDiskDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.param.LineFlowReportParam;
import cn.mw.monitor.report.param.MwAssetsUsabilityParam;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MWReportHandlerDataLogic
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/7 16:08
 * @Version 1.0
 **/
@Slf4j
public class MWReportHandlerDataLogic {

    public static List<MwAssetsUsabilityParam> handleAssetUsabilityReportData(List<MwAssetsUsabilityParam> list, Long startTime, Long endTime){
        List<MwAssetsUsabilityParam> realData = new ArrayList<>();
        if(CollectionUtils.isEmpty(list)){
            return realData;
        }
        //数据分组
        Map<String,List<MwAssetsUsabilityParam>> usabilityParamMap = new HashMap<>();
        for (MwAssetsUsabilityParam usabilityParam : list) {
            String assetsId = usabilityParam.getAssetsId();
            String ip = usabilityParam.getIp();
            if(usabilityParamMap.containsKey(assetsId+ip)){
                List<MwAssetsUsabilityParam> assetsUsabilityParams = usabilityParamMap.get(assetsId+ip);
                assetsUsabilityParams.add(usabilityParam);
                usabilityParamMap.put(assetsId+ip,assetsUsabilityParams);
            }else{
                List<MwAssetsUsabilityParam> assetsUsabilityParams = new ArrayList<>();
                assetsUsabilityParams.add(usabilityParam);
                usabilityParamMap.put(assetsId+ip,assetsUsabilityParams);
            }
        }
        if(usabilityParamMap.isEmpty()){
            return realData;
        }
        //计算数据
        for (String key : usabilityParamMap.keySet()) {
            List<MwAssetsUsabilityParam> params = usabilityParamMap.get(key);
            if(CollectionUtils.isEmpty(params)){
                continue;
            }
            MwAssetsUsabilityParam newParam = new MwAssetsUsabilityParam();
            newParam.setAssetsId(params.get(0).getAssetsId());
            newParam.setAssetsName(params.get(0).getAssetsName());
            newParam.setIp(params.get(0).getIp());
            newParam.setUpdateSuccess(true);
            double usabilityTotal = 0;
            for (MwAssetsUsabilityParam param : params) {
                String assetsUsability = param.getAssetsUsability();
                if(StringUtils.isNotBlank(assetsUsability)){
                    usabilityTotal += Double.parseDouble(assetsUsability.replace("%",""));
                }
            }
            //计算天数
            Long k = 86400000l;
            int day = (int) ((endTime - startTime) / k+1);
            newParam.setAssetsUsability(new BigDecimal(usabilityTotal / params.size()).setScale(4, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            realData.add(newParam);
        }

        return realData;
    }

    /**
     * CPU与内存报表数据处理合并
     * @param list
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<RunTimeItemValue> handleCpuAndMomeryReportData(List<RunTimeItemValue> list, Long startTime, Long endTime){
        log.info("进行CPU报表数据处理");
        //进行数据计算组合
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        Map<String,List<RunTimeItemValue>> itemValueMap = new HashMap<>();
        for (RunTimeItemValue runTimeItemValue : list) {
            String assetsId = runTimeItemValue.getAssetsId();
            String ip = runTimeItemValue.getIp();
            if(itemValueMap.containsKey(assetsId+ip)){
                List<RunTimeItemValue> itemValues = itemValueMap.get(assetsId+ip);
                itemValues.add(runTimeItemValue);
                itemValueMap.put(assetsId+ip,itemValues);
            }else{
                List<RunTimeItemValue> itemValues = new ArrayList<>();
                itemValues.add(runTimeItemValue);
                itemValueMap.put(assetsId+ip,itemValues);
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<RunTimeItemValue> realData = new ArrayList<>();
        for (String assetsIdAndIp : itemValueMap.keySet()){
            List<RunTimeItemValue> runTimeItemValues = itemValueMap.get(assetsIdAndIp);
            if(CollectionUtils.isEmpty(runTimeItemValues)){
                continue;
            }
            RunTimeItemValue newItemValue = new RunTimeItemValue();
            newItemValue.setAssetsId(runTimeItemValues.get(0).getAssetsId());
            newItemValue.setAssetName(runTimeItemValues.get(0).getAssetName());
            newItemValue.setIp(runTimeItemValues.get(0).getIp());
            double dMaxValue = 0;
            double dMinValue = 200;
            double dSumValue = 0;
            double dDiskTotal = 0;
            double diskUserSum = 0;
            double diskUserRateSum = 0;
            double memoryMax = 0;
            double memoryMin = 100;
            double icmpResponseTimeSum = 0;
            log.info("CPU报表数据处理条数"+assetsIdAndIp+":::"+runTimeItemValues.size());
            for (RunTimeItemValue runTimeItemValue : runTimeItemValues) {
                String maxValue = runTimeItemValue.getMaxValue();
                String minValue = runTimeItemValue.getMinValue();
                String avgValue = runTimeItemValue.getAvgValue();
                String diskTotal = runTimeItemValue.getDiskTotal();
                String diskUser = runTimeItemValue.getDiskUser();
                String diskUserRate = runTimeItemValue.getDiskUserRate();
                String icmpResponseTime = runTimeItemValue.getIcmpResponseTime();
                String maxMemoryUtilizationRate = runTimeItemValue.getMaxMemoryUtilizationRate();
                String minMemoryUtilizationRate = runTimeItemValue.getMinMemoryUtilizationRate();
                //计算CPU最大值
                if(StringUtils.isNotBlank(maxValue) && dMaxValue < Double.parseDouble(maxValue.replace("%",""))){
                    dMaxValue = Double.parseDouble(maxValue.replace("%",""));
                    newItemValue.setMaxValueTime(runTimeItemValue.getMaxValueTime());
                }
                //计算CPU最小值
                if(StringUtils.isNotBlank(minValue)){
                    if(dMinValue == 0){
                        dMinValue = Double.parseDouble(minValue.replace("%",""));
                    }else if(dMinValue > Double.parseDouble(minValue.replace("%",""))){
                        dMinValue = Double.parseDouble(minValue.replace("%",""));
                    }
                    newItemValue.setMinValueTime(runTimeItemValue.getMinValueTime());
                }else{
                    if(dMinValue == 200){
                        dMinValue = 0;
                    }
                    newItemValue.setMinValueTime(runTimeItemValue.getMinValueTime());
                }
                //计算CPU平均值
                if(StringUtils.isNotBlank(avgValue)){
                    dSumValue += Double.parseDouble(avgValue.replace("%",""));
                }

                //计算总内存
                if(StringUtils.isNotBlank(diskTotal)){
                    String str = "";
                    String unit = "";
                    for (int i = 0; i < diskTotal.length(); i++) {
                        if((diskTotal.charAt(i) >= 48 && diskTotal.charAt(i) <= 57) || diskTotal.charAt(i) == '.'){
                            str += diskTotal.charAt(i);
                        }else{
                            unit +=  diskTotal.charAt(i);
                        }
                    }
                    Map<String, String> acceptMaxValueMap = UnitsUtil.getValueMap(str, "MB", unit);
                    if(dDiskTotal < Double.parseDouble(acceptMaxValueMap.get("value"))){
                        dDiskTotal = Double.parseDouble(acceptMaxValueMap.get("value"));
                    }
                }
                //计算已使用内存
                if(StringUtils.isNotBlank(diskUser)){
                    String str = "";
                    String unit = "";
                    for (int i = 0; i < diskUser.length(); i++) {
                        if((diskUser.charAt(i) >= 48 && diskUser.charAt(i) <= 57) || diskUser.charAt(i) == '.'){
                            str += diskUser.charAt(i);
                        }else{
                            unit +=  diskUser.charAt(i);
                        }
                    }
                    Map<String, String> acceptMaxValueMap = UnitsUtil.getValueMap(str, "MB", unit);
                    diskUserSum += Double.parseDouble(acceptMaxValueMap.get("value"));
                }
                //计算内存利用率
                if(StringUtils.isNotBlank(diskUserRate)){
                    diskUserRateSum += Double.parseDouble(diskUserRate.replace("%",""));
                }
                if(StringUtils.isNotBlank(icmpResponseTime)){
                    String str = "";
                    for (int i = 0; i < icmpResponseTime.length(); i++) {
                        if((icmpResponseTime.charAt(i) >= 48 && icmpResponseTime.charAt(i) <= 57) || icmpResponseTime.charAt(i) == '.'){
                            str += icmpResponseTime.charAt(i);
                        }
                    }
                    if(StringUtils.isNotBlank(str)){
                        icmpResponseTimeSum += Double.parseDouble(str);
                    }
                }

                //计算最大内存利用率
                if(StringUtils.isNotBlank(maxMemoryUtilizationRate)){
                    String str = "";
                    for (int i = 0; i < maxMemoryUtilizationRate.length(); i++) {
                        if((maxMemoryUtilizationRate.charAt(i) >= 48 && maxMemoryUtilizationRate.charAt(i) <= 57) || maxMemoryUtilizationRate.charAt(i) == '.'){
                            str += maxMemoryUtilizationRate.charAt(i);
                        }
                    }
                    if(StringUtils.isNotBlank(str) && memoryMax < Double.parseDouble(str)){
                        memoryMax = Double.parseDouble(str);
                        newItemValue.setMemoryMaxValueTime(runTimeItemValue.getMemoryMaxValueTime());
                    }
                }

                //计算最小内存利用率
                if(StringUtils.isNotBlank(minMemoryUtilizationRate)){
                    String str = "";
                    for (int i = 0; i < minMemoryUtilizationRate.length(); i++) {
                        if((minMemoryUtilizationRate.charAt(i) >= 48 && minMemoryUtilizationRate.charAt(i) <= 57) || minMemoryUtilizationRate.charAt(i) == '.'){
                            str += minMemoryUtilizationRate.charAt(i);
                        }
                    }
                    if(StringUtils.isNotBlank(str) && memoryMin > Double.parseDouble(str)){
                        memoryMin = Double.parseDouble(str);
                        newItemValue.setMemoryMinValueTime(runTimeItemValue.getMemoryMinValueTime());
                    }
                }
            }
            //计算天数
            Long k = 86400000l;
            int day = (int) ((endTime - startTime) / k+1);
            newItemValue.setMaxValue(dMaxValue+"%");
            newItemValue.setMinValue(dMinValue+"%");
            if(StringUtils.isBlank(newItemValue.getMaxValueTime())){
                newItemValue.setMaxValueTime(format.format(new Date(startTime)));
            }
            if(StringUtils.isBlank(newItemValue.getMinValueTime())){
                newItemValue.setMinValueTime(format.format(new Date(startTime)));
            }
            newItemValue.setAvgValue(new BigDecimal(dSumValue / day).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            newItemValue.setIcmpResponseTime(new BigDecimal(icmpResponseTimeSum / day).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"ms");
            if(dDiskTotal >= 1024){
                Map<String, String> values = UnitsUtil.getValueMap(dDiskTotal+"", "GB", "MB");
                newItemValue.setDiskTotal(values.get("value")+"GB");
                Map<String, String> useValues = UnitsUtil.getValueMap(diskUserSum / day+"", "GB", "MB");
                newItemValue.setDiskUser(new BigDecimal(useValues.get("value")).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"GB");
            }else{
                newItemValue.setDiskTotal(dDiskTotal+"MB");
                newItemValue.setDiskUser(new BigDecimal(diskUserSum / day).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"MB");
            }
            if(dDiskTotal == 0){
                newItemValue.setDiskUser(newItemValue.getDiskTotal());
                newItemValue.setDiskUserRate("0%");
            }
            newItemValue.setDiskUserRate(new BigDecimal(diskUserRateSum / runTimeItemValues.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            newItemValue.setUpdateSuccess(true);
            newItemValue.setMaxMemoryUtilizationRate(new BigDecimal(memoryMax).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            if(memoryMin == 100){
                newItemValue.setMinMemoryUtilizationRate(0+"%");
            }else{
                newItemValue.setMinMemoryUtilizationRate(new BigDecimal(memoryMin).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            }
            if(StringUtils.isBlank(newItemValue.getMemoryMaxValueTime())){
                newItemValue.setMemoryMaxValueTime(format.format(new Date(startTime)));
            }
            if(StringUtils.isBlank(newItemValue.getMemoryMinValueTime())){
                newItemValue.setMemoryMinValueTime(format.format(new Date(startTime)));
            }
            checkMemoryInfo(newItemValue);
            log.info("CPU报表处理数据计算内存"+newItemValue);
            checkCpuInfo(newItemValue);
            log.info("CPU报表处理数据计算CPU"+newItemValue);
            realData.add(newItemValue);
        }
        return realData;
    }


    /**
     * 校验内存信息
     */
    public static void checkMemoryInfo(RunTimeItemValue runTimeItemValue){
        String diskUserRate = runTimeItemValue.getDiskUserRate();
        String maxMemoryUtilizationRate = runTimeItemValue.getMaxMemoryUtilizationRate();
        String minMemoryUtilizationRate = runTimeItemValue.getMinMemoryUtilizationRate();
        if(StringUtils.isBlank(diskUserRate) || StringUtils.isBlank(maxMemoryUtilizationRate) || StringUtils.isBlank(minMemoryUtilizationRate)){return;}
        if(Double.parseDouble(diskUserRate.replace("%","")) > Double.parseDouble(maxMemoryUtilizationRate.replace("%",""))){
            String value = new BigDecimal((Double.parseDouble(maxMemoryUtilizationRate.replace("%","")) + Double.parseDouble(minMemoryUtilizationRate.replace("%",""))) / 2)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            runTimeItemValue.setDiskUserRate(value+"%");
        }
    }

    /**
     * 校验内存信息
     */
    public static void checkCpuInfo(RunTimeItemValue runTimeItemValue){
        String avgValue = runTimeItemValue.getAvgValue();
        String maxValue = runTimeItemValue.getMaxValue();
        String minValue = runTimeItemValue.getMinValue();
        if(StringUtils.isBlank(avgValue) || StringUtils.isBlank(maxValue) || StringUtils.isBlank(minValue)){return;}
        if(Double.parseDouble(avgValue.replace("%","")) > Double.parseDouble(maxValue.replace("%",""))){
            String value = new BigDecimal((Double.parseDouble(maxValue.replace("%","")) + Double.parseDouble(minValue.replace("%",""))) / 2)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            runTimeItemValue.setAvgValue(value+"%");
        }
    }

    /**
     * 流量统计报表数据处理合并
     * @param list
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<LineFlowReportParam> handleLinkReportData(List<LineFlowReportParam> list, Long startTime, Long endTime,String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String,List<LineFlowReportParam>> lineFlowMap = new HashMap<>();
        for (LineFlowReportParam lineFlowReportParam : list) {
            String assetsId = lineFlowReportParam.getAssetsId();
            String interfaceName = lineFlowReportParam.getInterfaceName();
            if(lineFlowMap.containsKey(assetsId+interfaceName)){
                List<LineFlowReportParam> lineFlowReportParams = lineFlowMap.get(assetsId+interfaceName);
                lineFlowReportParams.add(lineFlowReportParam);
                lineFlowMap.put(assetsId+interfaceName,lineFlowReportParams);
            }else{
                List<LineFlowReportParam> lineFlowReportParams = new ArrayList<>();
                lineFlowReportParams.add(lineFlowReportParam);
                lineFlowMap.put(assetsId+interfaceName,lineFlowReportParams);
            }
        }
        List<LineFlowReportParam> realData = new ArrayList<>();
        for (String assetsIdAndName : lineFlowMap.keySet()) {
            List<LineFlowReportParam> lineFlowReportParams = lineFlowMap.get(assetsIdAndName);
            if(!CollectionUtils.isEmpty(lineFlowReportParams)){
                LineFlowReportParam reportParam = new LineFlowReportParam();
                double acceptMax = 0;
                double acceptSum = 0;
                double sendMax = 0;
                double sendSum = 0;
                double acceptTotal = 0;
                double sendTotal = 0;
                double sendMin = 10000;
                double acceptMin = 10000;
                String sendMaxValueTime = "";
                String sendMinValueTime = "";
                String acceptMaxValueTime = "";
                String acceptMinValueTime = "";
                for (LineFlowReportParam lineFlowReportParam : lineFlowReportParams) {
                    reportParam.setAssetsName(lineFlowReportParam.getAssetsName());
                    reportParam.setInterfaceName(lineFlowReportParam.getInterfaceName());
                    reportParam.setAssetsId(lineFlowReportParam.getAssetsId());
                    String acceptFlowMax = lineFlowReportParam.getAcceptFlowMax();
                    String acceptFlowAvg = lineFlowReportParam.getAcceptFlowAvg();
                    String sendingFlowMax = lineFlowReportParam.getSendingFlowMax();
                    String sendingFlowAvg = lineFlowReportParam.getSendingFlowAvg();
                    String sendTotalFlow = lineFlowReportParam.getSendTotalFlow();
                    String acceptTotalFlow = lineFlowReportParam.getAcceptTotalFlow();
                    String acceptFlowMin = lineFlowReportParam.getAcceptFlowMin();
                    String sendingFlowMin = lineFlowReportParam.getSendingFlowMin();
                    if(StringUtils.isNotBlank(acceptFlowMin)){
                        String str = "";
                        String unit = "";
                        for (int i = 0; i < acceptFlowMin.length(); i++) {
                            if((acceptFlowMin.charAt(i) >= 48 && acceptFlowMin.charAt(i) <= 57) || acceptFlowMin.charAt(i) == '.'){
                                str += acceptFlowMin.charAt(i);
                            }else{
                                unit +=  acceptFlowMin.charAt(i);
                            }
                            if(acceptMin > Double.parseDouble(str)){
                                acceptMin = Double.parseDouble(str);
                                acceptMinValueTime = lineFlowReportParam.getAcceptMinValueTime();
                            }
                        }
                    }
                    if(StringUtils.isNotBlank(sendingFlowMin)){
                        String str = "";
                        String unit = "";
                        for (int i = 0; i < sendingFlowMin.length(); i++) {
                            if((sendingFlowMin.charAt(i) >= 48 && sendingFlowMin.charAt(i) <= 57) || sendingFlowMin.charAt(i) == '.'){
                                str += sendingFlowMin.charAt(i);
                            }else{
                                unit +=  sendingFlowMin.charAt(i);
                            }
                            if(sendMin > Double.parseDouble(str)){
                                sendMin = Double.parseDouble(str);
                                sendMinValueTime = lineFlowReportParam.getSendMinValueTime();
                            }
                        }
                    }

                    if(StringUtils.isNotBlank(sendTotalFlow)){
                        String str = "";
                        for (int i = 0; i < sendTotalFlow.length(); i++) {
                            if((sendTotalFlow.charAt(i) >= 48 && sendTotalFlow.charAt(i) <= 57) || sendTotalFlow.charAt(i) == '.'){
                                str += sendTotalFlow.charAt(i);
                            }
                        }
                        sendTotal += Double.parseDouble(str);
                    }
                    if(StringUtils.isNotBlank(acceptTotalFlow)){
                        String str = "";
                        for (int i = 0; i < acceptTotalFlow.length(); i++) {
                            if((acceptTotalFlow.charAt(i) >= 48 && acceptTotalFlow.charAt(i) <= 57) || acceptTotalFlow.charAt(i) == '.'){
                                str += acceptTotalFlow.charAt(i);
                            }
                        }
                        acceptTotal += Double.parseDouble(str);
                    }
                    if(StringUtils.isNotBlank(acceptFlowMax)){
                        String str = "";
                        String unit = "";
                        for (int i = 0; i < acceptFlowMax.length(); i++) {
                            if((acceptFlowMax.charAt(i) >= 48 && acceptFlowMax.charAt(i) <= 57) || acceptFlowMax.charAt(i) == '.'){
                                str += acceptFlowMax.charAt(i);
                            }else{
                                unit +=  acceptFlowMax.charAt(i);
                            }
                        }
                        Map<String, String> acceptMaxValueMap = UnitsUtil.getValueMap(str, "Kbps", unit);
                        if(acceptMax < Double.parseDouble(acceptMaxValueMap.get("value"))){
                            acceptMax = Double.parseDouble(acceptMaxValueMap.get("value"));
                            acceptMaxValueTime = lineFlowReportParam.getAcceptMaxValueTime();
                        }
                    }

                    if(StringUtils.isNotBlank(acceptFlowAvg)){
                        String str = "";
                        String unit = "";
                        for (int i = 0; i < acceptFlowAvg.length(); i++) {
                            if((acceptFlowAvg.charAt(i) >= 48 && acceptFlowAvg.charAt(i) <= 57) || acceptFlowAvg.charAt(i) == '.'){
                                str += acceptFlowAvg.charAt(i);
                            }else{
                                unit +=  acceptFlowAvg.charAt(i);
                            }
                        }
                        Map<String, String> acceptAvgValueMap = UnitsUtil.getValueMap(str, "Kbps", unit);
                        acceptSum += Double.parseDouble(acceptAvgValueMap.get("value"));
                    }

                    if(StringUtils.isNotBlank(sendingFlowMax)){
                        String str = "";
                        String unit = "";
                        for (int i = 0; i < sendingFlowMax.length(); i++) {
                            if((sendingFlowMax.charAt(i) >= 48 && sendingFlowMax.charAt(i) <= 57) || sendingFlowMax.charAt(i) == '.'){
                                str += sendingFlowMax.charAt(i);
                            }else{
                                unit +=  sendingFlowMax.charAt(i);
                            }
                        }
                        Map<String, String> sendMaxValueMap = UnitsUtil.getValueMap(str, "Kbps", unit);
                        if(sendMax < Double.parseDouble(sendMaxValueMap.get("value"))){
                            sendMax = Double.parseDouble(sendMaxValueMap.get("value"));
                            sendMaxValueTime = lineFlowReportParam.getSendMaxValueTime();
                        }
                    }

                    if(StringUtils.isNotBlank(sendingFlowAvg)){
                        String str = "";
                        String unit = "";
                        for (int i = 0; i < sendingFlowAvg.length(); i++) {
                            if((sendingFlowAvg.charAt(i) >= 48 && sendingFlowAvg.charAt(i) <= 57) || sendingFlowAvg.charAt(i) == '.'){
                                str += sendingFlowAvg.charAt(i);
                            }else{
                                unit +=  sendingFlowAvg.charAt(i);
                            }
                        }
                        Map<String, String> sendAvgValueMap = UnitsUtil.getValueMap(str, "Kbps", unit);
                        sendSum += Double.parseDouble(sendAvgValueMap.get("value"));
                    }
                }
                Map<String, String> acceptMaxValueMap = UnitsUtil.getValueMap(acceptMax+"", "Mbps", "Kbps");
                reportParam.setAcceptFlowMax(acceptMaxValueMap.get("value")+"Mbps");
                if(StringUtils.isBlank(acceptMaxValueTime)){
                    reportParam.setAcceptMaxValueTime(format.format(new Date(startTime)));
                }else{
                    reportParam.setAcceptMaxValueTime(acceptMaxValueTime);
                }
                Map<String, String> sendMaxValueMap = UnitsUtil.getValueMap(sendMax+"", "Mbps", "Kbps");
                reportParam.setSendingFlowMax(sendMaxValueMap.get("value")+"Mbps");
                if(StringUtils.isBlank(sendMaxValueTime)){
                    reportParam.setSendMaxValueTime(format.format(new Date(startTime)));
                }else{
                    reportParam.setSendMaxValueTime(sendMaxValueTime);
                }
                //计算天数
                Long k = 86400000l;
                int day = (int) ((endTime - startTime) / k+1);
                double value = new BigDecimal(acceptSum / day).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
                Map<String, String> acceptFlowAvgValueMap = UnitsUtil.getValueMap(value+"", "Mbps", "Kbps");
                reportParam.setAcceptFlowAvg(acceptFlowAvgValueMap.get("value")+"Mbps");
                double v = new BigDecimal(sendSum / day).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
                Map<String, String> sendingFlowAvgValueMap = UnitsUtil.getValueMap(v+"", "Mbps", "Kbps");
                reportParam.setSendingFlowAvg(sendingFlowAvgValueMap.get("value")+"Mbps");
                reportParam.setSendingFlowMin(new BigDecimal(sendMin).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"Mbps");
                if(StringUtils.isBlank(sendMinValueTime)){
                    reportParam.setSendMinValueTime(format.format(new Date(startTime)));
                }else{
                    reportParam.setSendMinValueTime(sendMinValueTime);
                }
                reportParam.setSendTotalFlow(new BigDecimal(sendTotal).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"Gbps");
                reportParam.setAcceptTotalFlow(new BigDecimal(acceptTotal).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"Gbps");
                reportParam.setAcceptFlowMin(new BigDecimal(acceptMin).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"Mbps");
                if(StringUtils.isBlank(acceptMinValueTime)){
                    reportParam.setAcceptMinValueTime(format.format(new Date(startTime)));
                }else{
                    reportParam.setAcceptMinValueTime(acceptMinValueTime);
                }
                reportParam.setTime(time);
                reportParam.setUpdateSuccess(true);
                realData.add(reportParam);
            }
        }
        return realData;
    }

    /**
     * 处理磁盘使用情况数据
     * @param diskDtos
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<TrendDiskDto> handleDiskUseReportData(List<TrendDiskDto> diskDtos, Long startTime, Long endTime){
        List<TrendDiskDto> weekDtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(diskDtos)){
            return weekDtos;
        }
        //数据分组
        Map<String,List<TrendDiskDto>> diskDtoMap = new HashMap<>();
        for (TrendDiskDto dto : diskDtos) {
            String assetsId = dto.getAssetsId();
            String typeName = dto.getTypeName();
            if(diskDtoMap.containsKey(assetsId+typeName)){
                List<TrendDiskDto> trendDiskDtos = diskDtoMap.get(assetsId+typeName);
                trendDiskDtos.add(dto);
                diskDtoMap.put(assetsId+typeName,trendDiskDtos);
            }else{
                List<TrendDiskDto> trendDiskDtos = new ArrayList<>();
                trendDiskDtos.add(dto);
                diskDtoMap.put(assetsId+typeName,trendDiskDtos);
            }
        }
        if(diskDtoMap.isEmpty()){
            return weekDtos;
        }
        //进行磁盘使用情况数据计算
        for (String key : diskDtoMap.keySet()) {
            TrendDiskDto newDto = new TrendDiskDto();
            List<TrendDiskDto> trendDiskDtos = diskDtoMap.get(key);
            if(CollectionUtils.isEmpty(trendDiskDtos)){
                continue;
            }
            newDto.setAssetsId(trendDiskDtos.get(0).getAssetsId());
            newDto.setIpAddress(trendDiskDtos.get(0).getIpAddress());
            newDto.setTypeName(trendDiskDtos.get(0).getTypeName());
            newDto.setAssetsName(trendDiskDtos.get(0).getAssetsName());
            double doubleDiskTotal = 0;
            double doubleDiskFree = 0;
            double doubleDiskMaxValue = 0;
            double doubleDiskMinValue = 200;
            double doubleDiskAvgValue = 0;
            for (TrendDiskDto trendDiskDto : trendDiskDtos) {
                String diskTotal = trendDiskDto.getDiskTotal();//总内存
                String diskFree = trendDiskDto.getDiskFree();//剩余磁盘容量
                String diskMaxValue = trendDiskDto.getDiskMaxValue();//最大使用率
                String diskMinValue = trendDiskDto.getDiskMinValue();//最小使用率
                String diskAvgValue = trendDiskDto.getDiskAvgValue();//平均使用率
                if(StringUtils.isNotBlank(diskTotal)){
                    String str = "";
                    String unit = "";
                    for (int i = 0; i < diskTotal.length(); i++) {
                        if((diskTotal.charAt(i) >= 48 && diskTotal.charAt(i) <= 57) || diskTotal.charAt(i) == '.'){
                            str += diskTotal.charAt(i);
                        }else{
                            unit +=  diskTotal.charAt(i);
                        }
                    }
                    Map<String, String> diskTotalMap = UnitsUtil.getValueMap(str, "GB", unit);
                    doubleDiskTotal += Double.parseDouble(diskTotalMap.get("value"));
                }
                if(StringUtils.isNotBlank(diskFree)){
                    String str = "";
                    String unit = "";
                    for (int i = 0; i < diskFree.length(); i++) {
                        if((diskFree.charAt(i) >= 48 && diskFree.charAt(i) <= 57) || diskFree.charAt(i) == '.'){
                            str += diskFree.charAt(i);
                        }else{
                            unit +=  diskFree.charAt(i);
                        }
                    }
                    Map<String, String> diskFreeMap = UnitsUtil.getValueMap(str, "GB", unit);
                    doubleDiskFree += Double.parseDouble(diskFreeMap.get("value"));
                }
                if(StringUtils.isNotBlank(diskMaxValue)){
                    if(Double.parseDouble(diskMaxValue.replace("%","")) > doubleDiskMaxValue){
                        doubleDiskMaxValue = Double.parseDouble(diskMaxValue.replace("%",""));
                    }
                }
                if(StringUtils.isNotBlank(diskMinValue)){
                    if(doubleDiskMinValue == 200){
                        doubleDiskMinValue = Double.parseDouble(diskMinValue.replace("%",""));
                    }else if(doubleDiskMinValue > Double.parseDouble(diskMinValue.replace("%",""))){
                        doubleDiskMinValue = Double.parseDouble(diskMinValue.replace("%",""));
                    }
                }
                if(StringUtils.isNotBlank(diskAvgValue)){
                    doubleDiskAvgValue += Double.parseDouble(diskAvgValue.replace("%",""));
                }
            }
            //计算天数
            Long k = 86400000l;
            int day = (int) ((endTime - startTime) / k+1);
            double v = new BigDecimal(doubleDiskTotal / trendDiskDtos.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
            newDto.setDiskTotal(String.valueOf(v)+"GB");
            double v2 = new BigDecimal(doubleDiskFree /  trendDiskDtos.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
            newDto.setDiskFree(String.valueOf(v2)+"GB");
            newDto.setDiskMaxValue(doubleDiskMaxValue+"%");
            newDto.setDiskMinValue(doubleDiskMinValue+"%");
            newDto.setDiskAvgValue(new BigDecimal(doubleDiskAvgValue /  trendDiskDtos.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            //计算已使用数据
            double v3 = new BigDecimal(v-v2).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
            newDto.setDiskUse(v3+"GB");
            if(v2 == 0 || v == 0){
                newDto.setDiskUsable("0.00%");
            }else{
                newDto.setDiskUsable(new BigDecimal((v2/v)*100).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"%");
            }
            newDto.setUpdateSuccess(true);
            weekDtos.add(newDto);
        }
        return weekDtos;
    }


    public static List<RunTimeItemValue> handleRunStateReportData(List<RunTimeItemValue> itemValues){
        List<RunTimeItemValue> realData = new ArrayList<>();
        if(CollectionUtils.isEmpty(itemValues)){
            return realData;
        }
        //按照ItemName进行名称分组
        Map<String,List<RunTimeItemValue>> itemValueMap = new HashMap<>();
        itemValues.forEach(item->{
            String itemName = item.getItemName();
            if(itemValueMap.containsKey(itemName)){
                List<RunTimeItemValue> valueList = itemValueMap.get(itemName);
                valueList.add(item);
                itemValueMap.put(itemName,valueList);
            }else{
                List<RunTimeItemValue> valueList = new ArrayList<>();
                valueList.add(item);
                itemValueMap.put(itemName,valueList);
            }
        });
        if(itemValueMap.isEmpty()){
            return realData;
        }
        for (String key : itemValueMap.keySet()) {
            List<RunTimeItemValue> runTimeItemValues = itemValueMap.get(key);
            List<RunTimeItemValue> runTimeItemValueList = new ArrayList<>();
            if("DISK_UTILIZATION".equals(key)){
                for (int i = 0; i < runTimeItemValues.size(); i++) {
                    Boolean add = true;
                    for (int j = 0; j < runTimeItemValueList.size(); j++) {
                        if (runTimeItemValues.get(i).getAssetsId().equals(runTimeItemValueList.get(j).getAssetsId()) && runTimeItemValues.get(i).getDiskName().equals(runTimeItemValueList.get(j).getDiskName())) {
                            try {
                                String avg = String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getAvgValue()) + Double.parseDouble(runTimeItemValues.get(i).getAvgValue())) / 2).setScale(2, BigDecimal.ROUND_HALF_UP));
                                runTimeItemValueList.get(j).setAvgValue(avg);
                            } catch (Exception e) {
                            }
                            add = false;
                        }
                    }
                    if (add) {
                        runTimeItemValueList.add(runTimeItemValues.get(i));
                    }
                }
                realData.addAll(runTimeItemValueList);
            }
            if("INTERFACE_IN_UTILIZATION".equals(key)){
                for (int i = 0; i < runTimeItemValues.size(); i++) {
                    Boolean add = true;
                    for (int j = 0; j < runTimeItemValueList.size(); j++) {
                        if (runTimeItemValues.get(i).getAssetsId().equals(runTimeItemValueList.get(j).getAssetsId()) && runTimeItemValues.get(i).getInterfaceName().equals(runTimeItemValueList.get(j).getInterfaceName())) {
                            try {
                                String outInterfaceAvgValue = String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getOutInterfaceAvgValue()) + Double.parseDouble(runTimeItemValues.get(i).getOutInterfaceAvgValue())) / 2).setScale(2, BigDecimal.ROUND_HALF_UP));
                                String avg = String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getAvgValue()) + Double.parseDouble(runTimeItemValues.get(i).getAvgValue())) / 2).setScale(2, BigDecimal.ROUND_HALF_UP));
                                runTimeItemValueList.get(j).setAvgValue(avg);
                                runTimeItemValueList.get(j).setOutInterfaceAvgValue(outInterfaceAvgValue);
                            } catch (Exception e) {
                            }
                            add = false;
                        }
                    }
                    if (add) {
                        runTimeItemValueList.add(runTimeItemValues.get(i));
                    }
                }
                realData.addAll(runTimeItemValueList);
            }
            if(!"INTERFACE_IN_UTILIZATION".equals(key) && !"DISK_UTILIZATION".equals(key)){
                for (int i = 0; i <runTimeItemValues.size() ; i++) {
                    Boolean add = true ;
                    for (int j = 0; j <runTimeItemValueList.size() ; j++) {
                        if (runTimeItemValues.get(i).getAssetsId().equals(runTimeItemValueList.get(j).getAssetsId())){
                            try {
                                String avg =String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getAvgValue())+Double.parseDouble(runTimeItemValues.get(i).getAvgValue()))/2).setScale(2,BigDecimal.ROUND_HALF_UP));
                                runTimeItemValueList.get(j).setAvgValue(avg);
                            }catch (Exception e){

                            }
                            add = false ;
                        }
                    }
                    if (add){
                        runTimeItemValueList.add(runTimeItemValues.get(i));
                    }
                }
                realData.addAll(runTimeItemValueList);
            }
        }
        return realData;
    }

    /**
     * 处理MPLS报表数据
     * @param mwMplsCacheDataDtos 上周上月数据
     * @param list 昨天自定义数据
     * @param param 请求参数
     */
    public static  List<Object> handleMplsHistoryReportData(List<MWMplsCacheDataDto> mwMplsCacheDataDtos, List<Map<String,String>> list, ServerHistoryDto param){
        Integer dateType = param.getDateType();
        Map<String,Object> sendMap = new HashMap<>();
        Map<String,Object> acceptMap = new HashMap<>();
        List<MWItemHistoryDto> sendDtos = new ArrayList<>();
        List<MWItemHistoryDto> acceptDtos = new ArrayList<>();
        Set<String> sendSet = new HashSet<>();
        Set<String> acceptSet = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if((dateType != null && dateType != 2 && dateType == 1) || (StringUtils.isNotBlank(param.getDateStart()) && StringUtils.isNotBlank(param.getDateEnd()))){//昨日或者是自定义数据
            //获取发送数据及接收数据
            if(!CollectionUtils.isEmpty(list)){
                for (Map<String, String> map : list) {
                    String sendData = map.get("sendData");
                    String acceptData = map.get("acceptData");
                    if(StringUtils.isNotBlank(sendData)){
                        List<String> sends = JSON.parseObject(sendData, List.class);
                        List<MWItemHistoryDto> sortDtos = new ArrayList<>();
                        if(!CollectionUtils.isEmpty(sends)){
                            for (String send : sends) {
                                String[] split = send.split(",");
                                MWItemHistoryDto dto = new MWItemHistoryDto();
                                dto.setValue(split[1]);
                                try {
                                    dto.setDateTime(dateFormat.parse(split[0]));
                                }catch (Exception e){

                                }
                                sendDtos.add(dto);
                                sendMap.put("unitByReal",split[2]);
                            }
                        }
                    }
                    if(StringUtils.isNotBlank(acceptData)){
                        List<String> accepts = JSON.parseObject(acceptData, List.class);
                        if(!CollectionUtils.isEmpty(accepts)){
                            for (String accept : accepts) {
                                String[] split = accept.split(",");
                                MWItemHistoryDto dto = new MWItemHistoryDto();
                                dto.setValue(split[1]);
                                try {
                                    dto.setDateTime(dateFormat.parse(split[0]));
                                }catch (Exception e){

                                }
                                acceptDtos.add(dto);
                                acceptMap.put("unitByReal",split[2]);
                            }
                        }
                    }
                }
            }
            Collections.sort(sendDtos, new Comparator<MWItemHistoryDto>() {
                @Override
                public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
            sendMap.put("realData",sendDtos);
            sendMap.put("titleName","数据发送流量");
            Collections.sort(acceptDtos, new Comparator<MWItemHistoryDto>() {
                @Override
                public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
            acceptMap.put("realData",acceptDtos);
            acceptMap.put("titleName","数据接收流量");
            List<Object> list2 = new ArrayList<>();
            list2.add(sendMap);
            list2.add(acceptMap);
            List<Object> list3 = new ArrayList<>();
            list3.add(list2);
            return list3;
        }
        if(dateType != null && dateType != 2 && (dateType == 8 || dateType == 5) && !CollectionUtils.isEmpty(mwMplsCacheDataDtos)){//上周或者上月数据
            List<String> sends = new ArrayList<>();
            List<String> accepts = new ArrayList<>();
            mwMplsCacheDataDtos.forEach(dataDto->{
                String accept = dataDto.getAccept();
                String send = dataDto.getSend();
                if(StringUtils.isNotBlank(accept) && StringUtils.isNotBlank(send)){
                    sends.add(send);
                    accepts.add(accept);
                }
            });
            List<MWItemHistoryDto> sortDtos = new ArrayList<>();
            if(!CollectionUtils.isEmpty(sends)){
                for (String send : sends) {
                    String[] split = send.split(",");
                    MWItemHistoryDto dto = new MWItemHistoryDto();
                    dto.setValue(split[1]);
                    try {
                        dto.setDateTime(dateFormat.parse(split[0]));
                    }catch (Exception e){

                    }
                    sendDtos.add(dto);
                    sendMap.put("unitByReal",split[2]);
                }
            }
            if(!CollectionUtils.isEmpty(accepts)){
                for (String accept : accepts) {
                    String[] split = accept.split(",");
                    MWItemHistoryDto dto = new MWItemHistoryDto();
                    dto.setValue(split[1]);
                    try {
                        dto.setDateTime(dateFormat.parse(split[0]));
                    }catch (Exception e){

                    }
                    acceptDtos.add(dto);
                    acceptMap.put("unitByReal",split[2]);
                }
            }
            Collections.sort(sendDtos, new Comparator<MWItemHistoryDto>() {
                @Override
                public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
            sendMap.put("realData",sendDtos);
            sendMap.put("titleName","数据发送流量");
            Collections.sort(acceptDtos, new Comparator<MWItemHistoryDto>() {
                @Override
                public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
            acceptMap.put("realData",acceptDtos);
            acceptMap.put("titleName","数据接收流量");
            List<Object> list2 = new ArrayList<>();
            list2.add(sendMap);
            list2.add(acceptMap);
            List<Object> list3 = new ArrayList<>();
            list3.add(list2);
            return list3;
        }
        return null;
    }


    public static String getDateRegion(Integer dateType,List<String> choosTime){
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
}
