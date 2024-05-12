package cn.mw.monitor.report.dto.linkdto;

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
public class LinkDetailDto extends LinkReportTable {
    private ProportionDtos proportionDto;
    private Integer tag;

}
