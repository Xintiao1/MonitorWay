package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MwMplsReportDto
 * @Author gengjb
 * @Date 2021/10/26 14:17
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwMplsReportDto {

    private String lineName;

    private String vendor;

    private String nodeName;

    private String ipAddress;

    private String averageAvailability;

    private String nodeAssetsId;

    private String dateRegion;


}
