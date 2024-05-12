package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MWNewScreenAssetsDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/29 10:53
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenAssetsDto {

    //总资产数量
    private int totalAseetsAmount;

    //有形资产数量
    private int tangibleAssetsAmount;

    //带外资产数量
    private int outAeestsAmount;

    //正常资产数量
    private int normalAseetsAmount;

    //异常资产数量
    private int unusualAseetsAmount;

    //宕机资产数量
    private int downTimeAseetsAmount;

    private List<String> normalAssets;

    private List<String> unusualAssets;

    private List<String> downTimeAssets;

    private List<MWNewScreenAssetsClassifyDto> normalAssetsMap;

    private List<MWNewScreenAssetsClassifyDto> unusualAssetsMap;

    private List<MWNewScreenAssetsClassifyDto> downTimeAssetsMap;

    //带外资产正常资产数量
    private int normalOutBandAseetsAmount;

    //带外资产异常资产数量
    private int unusualOutBandAseetsAmount;

    //带外资产宕机资产数量
    private int downTimeOutBandAseetsAmount;

    //是否是模型资产
    private Boolean isModel;

}
