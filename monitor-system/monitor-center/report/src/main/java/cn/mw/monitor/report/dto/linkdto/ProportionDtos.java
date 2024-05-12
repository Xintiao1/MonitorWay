package cn.mw.monitor.report.dto.linkdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author xhy
 * @date 2020/6/28 15:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProportionDtos {
    @ApiModelProperty("接口流量入向流量时间占比例<10%")
    private Float inProportionTen;
    @ApiModelProperty("接口流量入向流量时间占比例10%~50%")
    private Float inProportionFifty;
    @ApiModelProperty("接口流量入向流量时间占比例50%~80%")
    private Float inProportionEighty;
    @ApiModelProperty("接口流量入向流量时间占比例>80%")
    private Float inProportionHundred;
    @ApiModelProperty("接口流量出向流量时间占比例<10%")
    private Float outProportionTen;
    @ApiModelProperty("接口流量出向流量时间占比例10%~50%")
    private Float outProportionFifty;
    @ApiModelProperty("接口流量出向峰值流量时间占比例50%~80%")
    private Float outProportionEighty;
    @ApiModelProperty("接口流量出向流量时间占比例>80%")
    private Float outProportionHundred;

}
