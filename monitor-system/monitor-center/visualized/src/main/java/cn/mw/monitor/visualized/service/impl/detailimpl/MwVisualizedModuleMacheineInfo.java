package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleMacheineDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName
 * @Description 获取机器信息
 * @Author gengjb
 * @Date 2023/5/19 14:52
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleMacheineInfo implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    private final String USE_CAPACITY_TOTAL_HADOOP = "USE_CAPACITY_TOTAL_HADOOP";


    @Override
    public int[] getType() {
        return new int[]{75,76};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = visualizedManageDao.selectvisualizedCacheInfos(Arrays.asList(moduleParam.getAssetsId()),moduleParam.getItemNames());
            if(CollectionUtils.isEmpty(mwVisualizedCacheDtos)){return null;}
            List<MwVisualizedModuleMacheineDto> macheineDtos = handlerMacheineInfo(mwVisualizedCacheDtos);
            dataHandler(macheineDtos);
            return macheineDtos;
        }catch (Throwable e){
            log.error("MwVisualizedModuleMacheineInfo{} getData::",e);
            return null;
        }
    }

    /**
     * 处理机器信息
     * @param mwVisualizedCacheDtos
     */
    private List<MwVisualizedModuleMacheineDto> handlerMacheineInfo(List<MwVisualizedCacheDto> mwVisualizedCacheDtos){
        List<MwVisualizedModuleMacheineDto> macheineDtos = new ArrayList<>();
        String useCapacityPercentagehadoop = null;
        String useCapacityhadoop = null;
        String useCapacityhadoopUnits = null;
        for (MwVisualizedCacheDto mwVisualizedCacheDto : mwVisualizedCacheDtos) {
            MwVisualizedModuleMacheineDto macheineDto = new MwVisualizedModuleMacheineDto();
            if(StringUtils.isNotBlank(mwVisualizedCacheDto.getItemName()) && mwVisualizedCacheDto.getItemName().contains(RackZabbixItemConstant.USE_CAPACITY_PERCENTAGE_HADOOP)){
                useCapacityPercentagehadoop = mwVisualizedCacheDto.getValue();
            }
            if(StringUtils.isNotBlank(mwVisualizedCacheDto.getItemName()) && mwVisualizedCacheDto.getItemName().contains(RackZabbixItemConstant.USE_CAPACITY_HADOOP)){
                useCapacityhadoop = mwVisualizedCacheDto.getValue();
                useCapacityhadoopUnits = mwVisualizedCacheDto.getUnits();
            }
            macheineDto.setValue(mwVisualizedCacheDto.getValue());
            macheineDto.setName(mwVisualizedCacheDto.getItemName());
            macheineDto.setUnits(mwVisualizedCacheDto.getUnits());
            macheineDtos.add(macheineDto);
        }
        if(StringUtils.isBlank(useCapacityPercentagehadoop) || StringUtils.isBlank(useCapacityhadoop)){return macheineDtos;}
        MwVisualizedModuleMacheineDto macheineDto = new MwVisualizedModuleMacheineDto();
        macheineDto.setValue(new BigDecimal(Double.parseDouble(useCapacityhadoop) / (Double.parseDouble(useCapacityPercentagehadoop) / 100)).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        macheineDto.setUnits(useCapacityhadoopUnits);
        macheineDto.setName(USE_CAPACITY_TOTAL_HADOOP);
        macheineDtos.clear();
        macheineDtos.add(macheineDto);
        return macheineDtos;
    }


    private void dataHandler(List<MwVisualizedModuleMacheineDto> macheineDtos){
        if(CollectionUtils.isEmpty(macheineDtos)){return;}
        for (MwVisualizedModuleMacheineDto macheineDto : macheineDtos) {
            String value = macheineDto.getValue();
            if(!MwVisualizedUtil.checkStrIsNumber(value)){continue;}
            int ceil = (int)Math.ceil(Double.parseDouble(value));
            macheineDto.setValue(String.valueOf(ceil));
        }
    }
}
