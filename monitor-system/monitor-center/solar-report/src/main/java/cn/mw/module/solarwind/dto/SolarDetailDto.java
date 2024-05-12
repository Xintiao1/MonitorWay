package cn.mw.module.solarwind.dto;

import io.swagger.annotations.ApiModel;
import lombok.*;

/**
 * @author xhy
 * @date 2020/6/23 9:29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ApiModel(value = "solar報表返回结果集")
public class SolarDetailDto extends InterfaceTable {
    private ProportionDto proportionDto;
    private Integer tag;

}
