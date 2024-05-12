package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.visualized.dto.MwVisuZkSoftWareStatisticsDto;
import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareEnum;
import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName
 * @Description 中控统计信息
 * @Author gengjb
 * @Date 2023/3/16 15:55
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedZkSoftWareStatistics implements MwVisualizedZkSoftWare {

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Override
    public int[] getType() {
        return new int[]{39};
    }

    @Override
    public Object getData() {
        try {
            //获取机柜数据
            List<MwTangibleassetsDTO> assetsData = getAssetsData();
            MwVisuZkSoftWareStatisticsDto softWareStatisticsDto = getAssetsStatus(assetsData);
//            return MwVisuZkSoftWareStatisticsDto.builder().monitorNumber(100).normalNumber(88).abNormalNumber(12).build();
            return softWareStatisticsDto;
        }catch (Throwable e){
            log.error("可视化查询中控统计数据数据失败",e);
            return null;
        }
    }

    /**
     * 获取实例
     * @return
     */
    private List<MwTangibleassetsDTO> getAssetsData() throws Exception {
        //获取IOT类型ID
        List<Integer> modelTypeId = mwModelViewCommonService.getModelGroupIdByName(VisualizedZkSoftWareEnum.TYPE_IOT.getName());
        if(CollectionUtils.isEmpty(modelTypeId)){return null;}
        List<MwTangibleassetsDTO>  mwTangibleassetsTables = new ArrayList<>();
        QueryModelAssetsParam queryTangAssetsParam = new QueryModelAssetsParam();
        queryTangAssetsParam.setAssetsTypeId(modelTypeId.get(0));
        queryTangAssetsParam.setIsQueryAssetsState(true);
        //根据资产类型ID查询实例数据
        mwTangibleassetsTables = mwModelViewCommonService.findModelAssets(MwTangibleassetsDTO.class,queryTangAssetsParam);
        return mwTangibleassetsTables;
    }

    /**
     * 获取实例的状态
     * @param assetsData
     */
    private MwVisuZkSoftWareStatisticsDto getAssetsStatus(List<MwTangibleassetsDTO> assetsData){
        MwVisuZkSoftWareStatisticsDto softWareStatisticsDto = new MwVisuZkSoftWareStatisticsDto();
        if(CollectionUtils.isEmpty(assetsData)){return softWareStatisticsDto;}
        int normalNumber = 0;
        int abNormalNumber = 0;
        for (MwTangibleassetsDTO assetsDatum : assetsData) {
            if(StringUtils.isBlank(assetsDatum.getAssetsTypeSubName()) || assetsDatum.getAssetsTypeSubName().equals(VisualizedZkSoftWareEnum.SUB_TYPE_DISTRIBUTION.getName())){continue;}
            String itemAssetsStatus = assetsDatum.getItemAssetsStatus();
            if(StringUtils.isNotBlank(itemAssetsStatus) && itemAssetsStatus.equals(VisualizedZkSoftWareEnum.NORMAL.getName())){
                normalNumber++;
                continue;
            }
            abNormalNumber++;
        }
        softWareStatisticsDto.setMonitorNumber(normalNumber+abNormalNumber);
        softWareStatisticsDto.setNormalNumber(normalNumber);
        softWareStatisticsDto.setAbNormalNumber(abNormalNumber);
        return softWareStatisticsDto;
    }
}
