package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.param.MwModeRommCommonParam;
import cn.mw.monitor.service.model.param.QueryLayoutDataParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParamList;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedResourceRoomDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 机房机柜信息
 * @Author gengjb
 * @Date 2023/5/18 14:56
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedResourceRoomCabinet implements MwVisualizedModule {

    @Autowired
    private MwModelCommonService mwModelCommonService;

    @Override
    public int[] getType() {
        return new int[]{68};
    }


    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //查询分区的机房机柜信息
            QueryModelInstanceByPropertyIndexParamList indexParamList = new QueryModelInstanceByPropertyIndexParamList();
            indexParamList.setIsQueryAssetsState(false);
            QueryModelInstanceByPropertyIndexParam propertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
            propertyIndexParam.setPropertiesIndexId(moduleParam.getPropertiesIndexId());
            propertyIndexParam.setPropertiesValue(String.valueOf(moduleParam.getModelInstanceId()));
            indexParamList.setParamLists(Arrays.asList(propertyIndexParam));
            List<MwModeRommCommonParam> modeRommCommonParams = mwModelCommonService.getAllRoomAndCabinetInfo(indexParamList);
            //处理机房数据
            return handlerRoomInfo(modeRommCommonParams);
        }catch (Throwable e){
            log.error("MwVisualizedResourceHostAgentStatus{} getData::",e);
            return null;
        }
    }

    /**
     * 处理机房信息
     */
    private List<MwVisualizedResourceRoomDto> handlerRoomInfo(List<MwModeRommCommonParam> modeRommCommonParams){
        List<MwVisualizedResourceRoomDto> resourceRoomDtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(modeRommCommonParams)){return resourceRoomDtos;}
        DecimalFormat format = new DecimalFormat("#.00");
        for (MwModeRommCommonParam modeRommCommonParam : modeRommCommonParams) {
            MwVisualizedResourceRoomDto roomDto = new MwVisualizedResourceRoomDto();
            int sumCount = 0;
            int groundingCount = 0;
            int idleCount =0;
            roomDto.setRoomName(modeRommCommonParam.getInstanceName());
            List<List<QueryLayoutDataParam>> layoutData = modeRommCommonParam.getLayoutData();
            for (List<QueryLayoutDataParam> layoutDatum : layoutData) {
                if(CollectionUtils.isEmpty(layoutDatum)){continue;}
                for (QueryLayoutDataParam queryLayoutDataParam : layoutDatum) {
                    Boolean isBan = queryLayoutDataParam.getIsBan();
                    Boolean isSelected = queryLayoutDataParam.getIsSelected();
                    if(isBan == null || isBan || isSelected == null || isSelected){
                        groundingCount++;
                    }else{
                        idleCount++;
                    }
                    sumCount++;
                }
            }
            roomDto.setSumCount(sumCount);
            roomDto.setGroundingCount(new BigDecimal(((double)groundingCount/sumCount)*100).setScale(2,BigDecimal.ROUND_HALF_UP) + VisualizedConstant.PER_CENT);
            roomDto.setIdleCount(new BigDecimal(((double)idleCount/sumCount)*100).setScale(2,BigDecimal.ROUND_HALF_UP)+ VisualizedConstant.PER_CENT);
            resourceRoomDtos.add(roomDto);
        }
        return resourceRoomDtos;
    }
}
