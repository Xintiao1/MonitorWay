package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/7/29 15:18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterAssetsParam {

    private Integer id;

    private String bulkDataId;

    private Integer modelId;

    private String modelDataId;

    private Integer userId;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("带内ip")
    private String inBandIp;

    @ApiModelProperty("资产类型")
    private Integer assetsTypeId;

    @ApiModelProperty("资产子类型")
    private Integer assetsTypeSubId;

    @ApiModelProperty("轮训引擎")
    private String pollingEngine;

    @ApiModelProperty("监控方式")
    private Integer monitorMode;

    @ApiModelProperty("厂商")
    private String manufacturer;

    @ApiModelProperty("规格型号")
    private String specifications;

    @ApiModelProperty("类型 首页 INDEX  大屏 SCREEN")
    private String type;

    @ApiModelProperty("时间")
    private Integer timeLag;


}
