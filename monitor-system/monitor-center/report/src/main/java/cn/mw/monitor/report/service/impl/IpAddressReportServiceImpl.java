package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.param.IpAddressReportParam;
import cn.mw.monitor.report.service.IpAddressReportService;
import cn.mw.monitor.report.util.MwReportDateUtil;
import cn.mw.monitor.service.ipmanage.IpManageService;
import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName
 * @Description IP地址报表接口实现
 * @Author gengjb
 * @Date 2023/3/7 10:08
 * @Version 1.0
 **/
@Service
@Slf4j
public class IpAddressReportServiceImpl implements IpAddressReportService {

    @Autowired
    private IpManageService ipManageService;

    public static final String ALL_IPADDRESS_SEGMENT = "全部";

    /**
     * 获取IP地址段使用率统计
     * @return
     */
    @Override
    public Reply getIpAddressReportData(IpAddressReportParam addressReportParam) {
        try {
            IpAddressReportDto addressReportDto = new IpAddressReportDto();
            List<Long> times = MwReportDateUtil.calculitionTime(addressReportParam.getDateType(), addressReportParam.getChooseTime());
            Date startDate = new Date();
            startDate.setTime(times.get(0)*1000);
            Date endDate = new Date();
            endDate.setTime(times.get(1)*1000);
            List<IpManageTree> treeFrist = ipManageService.getTreeFrist(startDate, endDate, 1, Integer.MAX_VALUE,0);
            PageInfo<IpManageTree> ipManageTreePageInfo = ipManageService.countFenOrHui(startDate, endDate, 1, Integer.MAX_VALUE);
            List<IpManageTree> ipManageTrees = ipManageTreePageInfo.getList();
            //进行报表数据处理
            addressReportDto.setUtilizationTopNMap(handleUtilizationTop(ipManageTrees));
            addressReportDto.setClassifyDtoMap(handleOperateClassify(ipManageTrees));
            addressReportDto.setUpdateNumberMap(handleUpdateNumber(ipManageTrees));
            IpAddressDto addressDto = IpAddressDto.builder().addressDataHandleDtos(handleData(addressReportDto)).utilizationDtos(handleAllUtilization(treeFrist)).build();
            return Reply.ok(addressDto);
        }catch (Throwable e){
            log.error("IP报表查询IP地址段使用率统计失败",e);
        }
        return null;
    }

    @Override
    public Reply getIpAddressExportData(IpAddressReportParam addressReportParam) {
        try {
            IpAddressReportDto addressReportDto = new IpAddressReportDto();
            List<Long> times = MwReportDateUtil.calculitionTime(addressReportParam.getDateType(), addressReportParam.getChooseTime());
            Date startDate = new Date();
            startDate.setTime(times.get(0)*1000);
            Date endDate = new Date();
            endDate.setTime(times.get(1)*1000);
            List<IpManageTree> treeFrist = ipManageService.getTreeFrist(startDate, endDate, 1, Integer.MAX_VALUE,0);
            PageInfo<IpManageTree> ipManageTreePageInfo = ipManageService.countFenOrHui(startDate, endDate, 1, Integer.MAX_VALUE);
            List<IpManageTree> ipManageTrees = ipManageTreePageInfo.getList();
            //进行报表数据处理
            addressReportDto.setIpAddressSegmentDto(handleAllUtilization(treeFrist));
            addressReportDto.setUtilizationTopNMap(handleUtilizationTop(ipManageTrees));
            addressReportDto.setClassifyDtoMap(handleOperateClassify(ipManageTrees));
            addressReportDto.setUpdateNumberMap(handleUpdateNumber(ipManageTrees));
            return Reply.ok(addressReportDto);
        }catch (Throwable e){
            log.error("IP报表查询IP地址段使用率统计失败",e);
        }
        return null;
    }

    @Override
    public Reply getIpAddressUtilizationDto(IpAddressReportParam param) {
        List<Long> times = MwReportDateUtil.calculitionTime(param.getDateType(), param.getChooseTime());
        Date startDate = new Date();
        startDate.setTime(times.get(0)*1000);
        Date endDate = new Date();
        endDate.setTime(times.get(1)*1000);
        List<IpManageTree> treeFrist = ipManageService.getTreeFrist(startDate, endDate, 1, Integer.MAX_VALUE,param.getId());
        List<IpAddressUtilizationDto> list = handleAllUtilization(treeFrist);
        return Reply.ok(list);
    }


    private List<IpAddressDataHandleDto> handleData(IpAddressReportDto addressReportDto){
        List<IpAddressDataHandleDto> addressDataDtos = new ArrayList<>();
        Map<String, List<IpAddressUtilizationTopNDto>> utilizationTopNMap = addressReportDto.getUtilizationTopNMap();
        for (Map.Entry<String, List<IpAddressUtilizationTopNDto>> entry : utilizationTopNMap.entrySet()) {
            List<IpAddressUtilizationTopNDto> value = entry.getValue();
            IpAddressOperateClassifyDto ipAddressOperateClassifyDto = addressReportDto.getClassifyDtoMap().get(entry.getKey());
            List<IpAddressUpdateNumberDto> ipAddressUpdateNumberDtos = addressReportDto.getUpdateNumberMap().get(entry.getKey());
            addressDataDtos.add(IpAddressDataHandleDto.builder().name(entry.getKey()).utilizationTopNDtos(value).ipAddressOperateClassifyDto(ipAddressOperateClassifyDto).ipAddressUpdateNumberDtos(ipAddressUpdateNumberDtos).build());
        }
        return addressDataDtos;
    }

    /**
     * 处理IP地址使用率统计
     * @return
     */
    private List<IpAddressUtilizationDto> handleAllUtilization(List<IpManageTree> treeFrist){
        List<IpAddressUtilizationDto> realData = new ArrayList<>();
        if(CollectionUtils.isEmpty(treeFrist)){
            return realData;
        }
        //做数据分组
        Map<String,IpAddressUtilizationDto> map = new HashMap<>();
        IpAddressUtilizationDto allDto = new IpAddressUtilizationDto();
        for (IpManageTree ipManageTree : treeFrist){
            String key = ipManageTree.getLabel();//一级分类名称
            if(map.containsKey(key)){
                IpAddressUtilizationDto utilizationDto = map.get(key);
                utilizationDto.setIpAddressSegmentAmount(utilizationDto.getIpAddressSegmentAmount()+ipManageTree.getGtEight()+ipManageTree.getLtFri()+ipManageTree.getGtFri());
                utilizationDto.setLtEqualToFiftyAmount(utilizationDto.getLtEqualToFiftyAmount()+ipManageTree.getLtFri());
                utilizationDto.setFiftyToEightyAmount(utilizationDto.getFiftyToEightyAmount()+ipManageTree.getGtFri());
                utilizationDto.setGtEqualToEightyAmount(utilizationDto.getGtEqualToEightyAmount()+ipManageTree.getGtEight());
                utilizationDto.setGroupName(key);
                utilizationDto.setId(ipManageTree.getId());
                map.put(key,utilizationDto);
            }else{
                IpAddressUtilizationDto utilizationDto = new IpAddressUtilizationDto();
                utilizationDto.setIpAddressSegmentAmount(utilizationDto.getIpAddressSegmentAmount()+ipManageTree.getGtEight()+ipManageTree.getLtFri()+ipManageTree.getGtFri());
                utilizationDto.setLtEqualToFiftyAmount(utilizationDto.getLtEqualToFiftyAmount()+ipManageTree.getLtFri());
                utilizationDto.setFiftyToEightyAmount(utilizationDto.getFiftyToEightyAmount()+ipManageTree.getGtFri());
                utilizationDto.setGtEqualToEightyAmount(utilizationDto.getGtEqualToEightyAmount()+ipManageTree.getGtEight());
                utilizationDto.setGroupName(key);
                utilizationDto.setId(ipManageTree.getId());
                map.put(key,utilizationDto);
            }
            allDto.setIpAddressSegmentAmount(allDto.getIpAddressSegmentAmount()+ipManageTree.getGtEight()+ipManageTree.getLtFri()+ipManageTree.getGtFri());
            allDto.setLtEqualToFiftyAmount(allDto.getLtEqualToFiftyAmount()+ipManageTree.getLtFri());
            allDto.setFiftyToEightyAmount(allDto.getFiftyToEightyAmount()+ipManageTree.getGtFri());
            allDto.setGtEqualToEightyAmount(allDto.getGtEqualToEightyAmount()+ipManageTree.getGtEight());
        }
        allDto.setGroupName(ALL_IPADDRESS_SEGMENT);
        realData.add(allDto);
        for (Map.Entry<String, IpAddressUtilizationDto> entry : map.entrySet()) {
            realData.add(entry.getValue());
        }
        return realData;
    }

    /**
     * 处理分配/变更次数TopN
     * @param ipManageTrees
     */
    private  Map<String,List<IpAddressUpdateNumberDto>> handleUpdateNumber(List<IpManageTree> ipManageTrees){
        Map<String,List<IpAddressUpdateNumberDto>> realDataMap = new LinkedHashMap<>();
        List<IpAddressUpdateNumberDto> addressUpdateNumberDtos = new ArrayList<>();
        realDataMap.put(ALL_IPADDRESS_SEGMENT,addressUpdateNumberDtos);
        if(CollectionUtils.isEmpty(ipManageTrees)){
            return realDataMap;
        }
        //做数据分组
        for (IpManageTree ipManageTree : ipManageTrees) {
            List<String> labels = ipManageTree.getLabels();
            if(CollectionUtils.isEmpty(labels)){
                continue;
            }
            IpAddressUpdateNumberDto dto = new IpAddressUpdateNumberDto();
            StringBuilder builder = new StringBuilder();
            for (String label : labels) {
                builder.append(label+"/");
            }
            if(builder != null && builder.length() > 0){
                builder.deleteCharAt(builder.length()-1);
                dto.setGroupName(builder.toString());
            }
            if(ipManageTree.getCountCha() == null){
                ipManageTree.setCountCha(0);
            }
            if(ipManageTree.getCountCle() == null){
                ipManageTree.setCountCle(0);
            }
            if(ipManageTree.getCountDis() == null){
                ipManageTree.setCountDis(0);
            }
            dto.setUpdateNumber(ipManageTree.getCountCha()+ipManageTree.getCountCle()+ipManageTree.getCountDis());
            String key = labels.get(0);//一级分类名称
            if(realDataMap.containsKey(key)){
                List<IpAddressUpdateNumberDto> ipAddressUpdateNumberDtos = realDataMap.get(key);
                ipAddressUpdateNumberDtos.add(dto);
                realDataMap.put(key,ipAddressUpdateNumberDtos);
            }else{
                List<IpAddressUpdateNumberDto> ipAddressUpdateNumberDtos = new ArrayList<>();
                ipAddressUpdateNumberDtos.add(dto);
                realDataMap.put(key,ipAddressUpdateNumberDtos);
            }
            addressUpdateNumberDtos.add(dto);
        }
        realDataMap.put(ALL_IPADDRESS_SEGMENT,addressUpdateNumberDtos);
        //排序
        for (Map.Entry<String, List<IpAddressUpdateNumberDto>> listEntry : realDataMap.entrySet()) {
            String name = listEntry.getKey();
            List<IpAddressUpdateNumberDto> value = listEntry.getValue();
            if(CollectionUtils.isEmpty(value)){
                continue;
            }
            Collections.sort(value, new Comparator<IpAddressUpdateNumberDto>() {
                @Override
                public int compare(IpAddressUpdateNumberDto o1, IpAddressUpdateNumberDto o2) {
                    return (o2.getUpdateNumber()).compareTo(o1.getUpdateNumber());
                }
            });
            realDataMap.put(name,value);
        }
        return realDataMap;
    }

    /**
     * 处理网段IP利用率TopN数据
     * @param ipManageTrees
     */
     private  Map<String,List<IpAddressUtilizationTopNDto>> handleUtilizationTop(List<IpManageTree> ipManageTrees){
         Map<String,List<IpAddressUtilizationTopNDto>> realDataMap = new LinkedHashMap<>();
         List<IpAddressUtilizationTopNDto> allUtilizationTopNDtos = new ArrayList<>();
         realDataMap.put(ALL_IPADDRESS_SEGMENT,allUtilizationTopNDtos);
         if(CollectionUtils.isEmpty(ipManageTrees)){
             return realDataMap;
         }
         //做数据分组
         for (IpManageTree ipManageTree : ipManageTrees) {
             List<String> labels = ipManageTree.getLabels();
             Double statusPrecent = ipManageTree.getStatusPrecent();//地址段使用率
             if(CollectionUtils.isEmpty(labels)){
                 continue;
             }
             IpAddressUtilizationTopNDto dto = new IpAddressUtilizationTopNDto();
             StringBuilder builder = new StringBuilder();
             for (String label : labels) {
                 builder.append(label+"/");
             }
             if(builder != null && builder.length() > 0){
                 builder.deleteCharAt(builder.length()-1);
                 dto.setNetWorksName(builder.toString());
             }
             if(statusPrecent == null){
                 statusPrecent = new Double(0);
             }
             dto.setCurrUtilization(statusPrecent+"%");
             dto.setSortValue(statusPrecent);
             String key = labels.get(0);//一级分类名称
             if(realDataMap.containsKey(key)){
                 List<IpAddressUtilizationTopNDto> ipAddressUtilizationTopNDtos = realDataMap.get(key);
                 ipAddressUtilizationTopNDtos.add(dto);
                 realDataMap.put(key,ipAddressUtilizationTopNDtos);
             }else{
                 List<IpAddressUtilizationTopNDto> ipAddressUtilizationTopNDtos = new ArrayList<>();
                 ipAddressUtilizationTopNDtos.add(dto);
                 realDataMap.put(key,ipAddressUtilizationTopNDtos);
             }
             allUtilizationTopNDtos.add(dto);
         }
         realDataMap.put(ALL_IPADDRESS_SEGMENT,allUtilizationTopNDtos);
         Map<String,List<IpAddressUtilizationTopNDto>> topNMap = new LinkedHashMap<>();
         //排序
         for (Map.Entry<String, List<IpAddressUtilizationTopNDto>> listEntry : realDataMap.entrySet()) {
             String name = listEntry.getKey();
             List<IpAddressUtilizationTopNDto> value = listEntry.getValue();
             if(CollectionUtils.isEmpty(value)){
                 continue;
             }
             Collections.sort(value, new Comparator<IpAddressUtilizationTopNDto>() {
                 @Override
                 public int compare(IpAddressUtilizationTopNDto o1, IpAddressUtilizationTopNDto o2) {
                     return (o2.getSortValue()).compareTo(o1.getSortValue());
                 }
             });
             if(value.size() > 15){
                 topNMap.put(name,value.subList(0,15));
             }else{
                 topNMap.put(name,value);
             }
         }
         return topNMap;
     }


    /**
     * 处理分类数据
     */
    private Map<String,IpAddressOperateClassifyDto> handleOperateClassify(List<IpManageTree> ipManageTrees){
        Map<String,IpAddressOperateClassifyDto> realDataMap = new LinkedHashMap<>();
        if(CollectionUtils.isEmpty(ipManageTrees)){
            return realDataMap;
        }
        int disOperateNumber = 0;
        int updateOperateNumber = 0;
        int retrieveOperateNumber = 0;
        realDataMap.put(ALL_IPADDRESS_SEGMENT,null);
        //做数据分组
        for (IpManageTree ipManageTree : ipManageTrees) {
            List<String> labels = ipManageTree.getLabels();
            Double statusPrecent = ipManageTree.getStatusPrecent();//地址段使用率
            if(CollectionUtils.isEmpty(labels)){
                continue;
            }
            String key = labels.get(0);//一级分类名称
            if(realDataMap.containsKey(key)){
                IpAddressOperateClassifyDto ipAddressOperateClassifyDto = realDataMap.get(key);
                ipAddressOperateClassifyDto.setDisOperateNumber(ipManageTree.getCountDis()==null?ipAddressOperateClassifyDto.getDisOperateNumber():ipAddressOperateClassifyDto.getDisOperateNumber()+ipManageTree.getCountDis());
                ipAddressOperateClassifyDto.setUpdateOperateNumber(ipManageTree.getCountCha()==null?ipAddressOperateClassifyDto.getUpdateOperateNumber():ipAddressOperateClassifyDto.getUpdateOperateNumber()+ipManageTree.getCountCha());
                ipAddressOperateClassifyDto.setRetrieveOperateNumber(ipManageTree.getCountCle()==null?ipAddressOperateClassifyDto.getRetrieveOperateNumber():ipAddressOperateClassifyDto.getRetrieveOperateNumber()+ipManageTree.getCountCle());
                realDataMap.put(key,ipAddressOperateClassifyDto);
            }else{
                IpAddressOperateClassifyDto ipAddressOperateClassifyDto = new IpAddressOperateClassifyDto();
                ipAddressOperateClassifyDto.setDisOperateNumber(ipManageTree.getCountDis()==null?0:ipManageTree.getCountDis());
                ipAddressOperateClassifyDto.setUpdateOperateNumber(ipManageTree.getCountCha()==null?0:ipManageTree.getCountCha());
                ipAddressOperateClassifyDto.setRetrieveOperateNumber(ipManageTree.getCountCle()==null?0:ipManageTree.getCountCle());
                realDataMap.put(key,ipAddressOperateClassifyDto);
            }
            disOperateNumber = disOperateNumber + (ipManageTree.getCountDis()==null?0:ipManageTree.getCountDis());
            updateOperateNumber = updateOperateNumber + (ipManageTree.getCountCha()==null?0:ipManageTree.getCountCha());
            retrieveOperateNumber = retrieveOperateNumber + (ipManageTree.getCountCle()==null?0:ipManageTree.getCountCle());
        }
        realDataMap.put(ALL_IPADDRESS_SEGMENT,IpAddressOperateClassifyDto.builder().disOperateNumber(disOperateNumber).updateOperateNumber(updateOperateNumber).retrieveOperateNumber(retrieveOperateNumber).build());
        return realDataMap;
     }
}
