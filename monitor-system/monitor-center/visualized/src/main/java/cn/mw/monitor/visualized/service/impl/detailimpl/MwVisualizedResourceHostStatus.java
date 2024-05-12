package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dto.MwVisualizedResourceHostStatusDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedResourceHostStatus
 * @Description 主机状态统计
 * @Author gengjb
 * @Date 2023/5/17 11:21
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedResourceHostStatus implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Override
    public int[] getType() {
        return new int[]{65};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,true);
            //主机状态统计
            return getHostStatusInfo(tangibleassetsDTOS);
        }catch (Throwable e){
            log.error("MwVisualizedResourceHostStatus{} getData::",e);
            return null;
        }
    }

    /**
     * 获取主机状态统计数据
     * @param tangibleassetsDTOS
     */
    private MwVisualizedResourceHostStatusDto getHostStatusInfo(List<MwTangibleassetsDTO> tangibleassetsDTOS){
        //正常数量
        int normalCount = tangibleassetsDTOS.stream().filter(item->item.getItemAssetsStatus().equals(VisualizedConstant.NORMAL)).collect(Collectors.toList()).size();
        //异常数量
        int abnormalCount = tangibleassetsDTOS.stream().filter(item->!item.getItemAssetsStatus().equals(VisualizedConstant.NORMAL)).collect(Collectors.toList()).size();
        MwVisualizedResourceHostStatusDto resourceHostStatusDto = new MwVisualizedResourceHostStatusDto();
        resourceHostStatusDto.setNormalValue(new BigDecimal(((double)normalCount/(normalCount + abnormalCount))*100).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        resourceHostStatusDto.setNormalValueUnits(VisualizedConstant.PER_CENT);
        resourceHostStatusDto.setAbnormalValue(new BigDecimal(((double)abnormalCount/(normalCount + abnormalCount))*100).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        resourceHostStatusDto.setAbnormalValueUnits(VisualizedConstant.PER_CENT);
        return resourceHostStatusDto;
    }
}
