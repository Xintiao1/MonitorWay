package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName
 * @Description 容器使用情况
 * @Author gengjb
 * @Date 2023/6/7 16:18
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedContaineUsage implements MwVisualizedModule {

    //使用率
    private final String USAGE_RATE = "USAGERATE";

    //已使用
    private final String USAGE = "USAGE";

    //已预留
    private final String RESERVED = "RESERVED";

    @Override
    public int[] getType() {
        return new int[]{73};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwVisualizedPrometheusDropDto> prometheusParam = moduleParam.getPrometheusParam();
            if(CollectionUtils.isEmpty(prometheusParam)){return null;}
            MwVisualizedContaineUsageDto usageDto = new MwVisualizedContaineUsageDto();
            for (MwVisualizedPrometheusDropDto mwVisualizedPrometheusDropDto : prometheusParam) {
                MwPrometheusResult prometheusResult = MwVisualizedUtil.getHttpPrometheusGet(mwVisualizedPrometheusDropDto);
                handleDataInfo(prometheusResult,mwVisualizedPrometheusDropDto.getItemName(),usageDto,mwVisualizedPrometheusDropDto.getUnits());
            }
            return usageDto;
        }catch (Throwable e){
            log.error("MwVisualizedContaineRanking{}  getData()",e);
            return null;
        }
    }

    private void handleDataInfo(MwPrometheusResult results,String itemName,MwVisualizedContaineUsageDto usageDto,String uints){
        MwPromDataInfo data = results.getData();
        List<MwPromMetric> metrics = data.getResult();
        if(CollectionUtils.isEmpty(metrics)){return;}
        MwPromMetric mwPromMetric = metrics.get(0);
        List<MwPromValueDto> promValue = mwPromMetric.getPromValue();
        if(CollectionUtils.isEmpty(promValue)){return;}
        String value = promValue.get(0).getValue();
        setContainerUsageInfo(itemName,value,usageDto,uints);
    }

    /**
     * 设置集群使用信息
     */
    private void setContainerUsageInfo(String itemName,String value,MwVisualizedContaineUsageDto usageDto,String uints){
        switch (itemName){
            case USAGE_RATE:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    usageDto.setUsageRate(new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                    usageDto.setUsageRateUnits(uints);
                }
                break;
            case USAGE:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    usageDto.setUsage(new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                    usageDto.setUsageUnits(uints);
                }
                break;
            case RESERVED:
                if(MwVisualizedUtil.checkStrIsNumber(value)){
                    usageDto.setReserved(new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                    usageDto.setReservedUnits(uints);
                }
                break;
            default:
                break;
        }
    }
}
