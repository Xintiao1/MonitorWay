package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName
 * @Description 可视化获取容器信息
 * @Author gengjb
 * @Date 2023/6/7 10:52
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedGetContainer implements MwVisualizedModule {

    //节点总数
    private final String kube_node_info = "kube_node_info";

    //不正常节点数
    private final String kube_node_spec_unschedulable = "kube_node_spec_unschedulable";

    //pod总数
    private final String kube_pod_status_phase = "kube_pod_status_phase";

    //pod异常
    private final String kube_pod_status_abnormal = "kube_pod_status_abnormal";


    @Override
    public int[] getType() {
        return new int[]{72};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwVisualizedPrometheusDropDto> prometheusParam = moduleParam.getPrometheusParam();
            if(CollectionUtils.isEmpty(prometheusParam)){return null;}
            MwVisualizedContainerOverViewDto containerOverViewDto = new MwVisualizedContainerOverViewDto();
            for (MwVisualizedPrometheusDropDto mwVisualizedPrometheusDropDto : prometheusParam) {
                MwPrometheusResult prometheusResult = MwVisualizedUtil.getHttpPrometheusGet(mwVisualizedPrometheusDropDto);
                handleDataInfo(prometheusResult,mwVisualizedPrometheusDropDto.getItemName(),containerOverViewDto);
            }
            containerOverViewDto.setNormalNodeNumber(containerOverViewDto.getNodeCount()-containerOverViewDto.getAbNormalNodeNumber());
            containerOverViewDto.setNormalPodNumber(containerOverViewDto.getPodCount()-containerOverViewDto.getAbNormalPodNumber());
            return containerOverViewDto;
        }catch (Throwable e){
            log.error("MwVisualizedGetContainer{}  getData()",e);
            return null;
        }
    }


    private void handleDataInfo(MwPrometheusResult results,String itemName,MwVisualizedContainerOverViewDto overViewDto){
        MwPromDataInfo data = results.getData();
        List<MwPromMetric> metrics = data.getResult();
        if(CollectionUtils.isEmpty(metrics)){return;}
        MwPromMetric mwPromMetric = metrics.get(0);
        List<MwPromValueDto> promValue = mwPromMetric.getPromValue();
        if(CollectionUtils.isEmpty(promValue)){return;}
        String value = promValue.get(0).getValue();
        setContainerOverViewInfo(itemName,value,overViewDto);
    }


    /**
     * 设置集群概览信息
     */
    private void setContainerOverViewInfo(String itemName,String value,MwVisualizedContainerOverViewDto overViewDto){
        switch (itemName){
            case kube_node_info:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    overViewDto.setNodeCount(Integer.parseInt(value));
                }
                break;
            case kube_node_spec_unschedulable:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    overViewDto.setAbNormalNodeNumber(Integer.parseInt(value));
                }
                break;
            case kube_pod_status_phase:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    overViewDto.setPodCount(Integer.parseInt(value));
                }
                break;
            case kube_pod_status_abnormal:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    overViewDto.setAbNormalPodNumber(Integer.parseInt(value));
                }
                break;
            default:
                break;
        }
    }
}
