package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.model.dto.rancher.MwModelRancherClusterCommonDTO;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherCommonDTO;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherNodesDTO;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherNodesRankingDTO;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParamList;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.visualized.dto.MwVisualizedHostGroupDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleRankingDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 可视化集群信息组件
 * @Author gengjb
 * @Date 2023/5/21 14:01
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedColonyInfo implements MwVisualizedModule {

    private static final String regex = "\\d+\\.+\\d+";

    private static final String state = "Provisioning";

    @Autowired
    private MwModelCommonService modelCommonService;

    @Override
    public int[] getType() {
        return new int[]{1000};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            QueryModelInstanceByPropertyIndexParamList indexParamList = new QueryModelInstanceByPropertyIndexParamList();
            indexParamList.setIsQueryAssetsState(false);
            QueryModelInstanceByPropertyIndexParam propertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
            propertyIndexParam.setPropertiesIndexId(moduleParam.getPropertiesIndexId());
            propertyIndexParam.setPropertiesValue(String.valueOf(moduleParam.getModelInstanceId()));
            indexParamList.setParamLists(Arrays.asList(propertyIndexParam));
            List<MwModelRancherCommonDTO> rancherInfoByModelAssets = modelCommonService.findRancherInfoByModelAssets(indexParamList);
            if(CollectionUtils.isEmpty(rancherInfoByModelAssets)){return null;}
            handlerColonyRankingInfo(rancherInfoByModelAssets);
            //处理集群信息
            return rancherInfoByModelAssets;
        }catch (Throwable e){
            log.error("MwVisualizedColonyInfo{} getData::",e);
            return null;
        }
    }

    /**
     * 处理集群排行信息
     * @param rancherInfoByModelAssets
     */
    private void handlerColonyRankingInfo(List<MwModelRancherCommonDTO> rancherInfoByModelAssets){
        for (MwModelRancherCommonDTO rancherInfoByModelAsset : rancherInfoByModelAssets) {
            List<MwModelRancherClusterCommonDTO> clusterList = rancherInfoByModelAsset.getClusterList();
            if(CollectionUtils.isEmpty(clusterList)){continue;}
            for (MwModelRancherClusterCommonDTO mwModelRancherClusterCommonDTO : clusterList) {
                if(StringUtils.isBlank(mwModelRancherClusterCommonDTO.getState()) || mwModelRancherClusterCommonDTO.getState().equals(state)){continue;}
                List<MwModelRancherNodesRankingDTO> cpuRankingDTOS = new ArrayList<>();
                List<MwModelRancherNodesRankingDTO> memoryRankingDTOS = new ArrayList<>();
                List<MwModelRancherNodesDTO> nodeList = mwModelRancherClusterCommonDTO.getNodeList();
                if(CollectionUtils.isEmpty(nodeList)){continue;}
                for (MwModelRancherNodesDTO mwModelRancherNodesDTO : nodeList) {
                    cpuRankingDTOS.add(digitExtract(mwModelRancherNodesDTO.getCpuUtilization(),mwModelRancherNodesDTO.getName()));
                    memoryRankingDTOS.add(digitExtract(mwModelRancherNodesDTO.getMemoryUtilization(),mwModelRancherNodesDTO.getName()));
                }
                //数据排序
                dataSort(cpuRankingDTOS);
                dataSort(memoryRankingDTOS);
                mwModelRancherClusterCommonDTO.setNodesCpus(cpuRankingDTOS);
                mwModelRancherClusterCommonDTO.setNodesMemorys(memoryRankingDTOS);
            }
        }
    }

    /**
     * 带单位的数字处理
     * @param valueWithUnits
     * @param name
     * @return
     */
    private MwModelRancherNodesRankingDTO digitExtract(String valueWithUnits,String name){
        MwModelRancherNodesRankingDTO nodesRankingDTO = new MwModelRancherNodesRankingDTO();
        if(StringUtils.isNotBlank(valueWithUnits)){
            String value = null;
            String units = null;
            //提取数字
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(valueWithUnits);
            while (matcher.find()){
                String group = matcher.group();
                value = new BigDecimal(group).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                units = valueWithUnits.replace(group,"");
            }
            nodesRankingDTO.extractFrom(name,value,units);
        }
        return nodesRankingDTO;
    }

    private void dataSort(List<MwModelRancherNodesRankingDTO> rankingDTOS){
        Collections.sort(rankingDTOS, new Comparator<MwModelRancherNodesRankingDTO>() {
            @Override
            public int compare(MwModelRancherNodesRankingDTO o1, MwModelRancherNodesRankingDTO o2) {
                if(Double.parseDouble(o1.getValue()) >Double.parseDouble(o2.getValue())){
                    return -1;
                }
                if(Double.parseDouble(o1.getValue()) < Double.parseDouble(o2.getValue())){
                    return 1;
                }
                return 0;
            }
        });
    }
}
