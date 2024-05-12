package cn.mw.monitor.screen.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateAddress {
    @ApiModelProperty(value = "经纬度")
    private String coordinate;
    @ApiModelProperty("地址")
    private String address;
    @ApiModelProperty("机构id")
    private Integer orgId;
    @ApiModelProperty("机构下关联资产统计")
    private Map<String,Integer> orgAsset;
    @ApiModelProperty("0:蓝色，1：绿色；2：橙色；大于等于3：红色")
    private int isAlert;
}
