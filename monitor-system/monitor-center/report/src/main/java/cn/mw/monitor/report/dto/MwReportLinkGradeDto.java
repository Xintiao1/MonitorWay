package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MwReportLinkGradeDto
 * @Author gengjb
 * @Date 2021/10/28 14:15
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwReportLinkGradeDto {

    private String value;

    private String label;

    private List<MwReportLinkGradeDto> children;
}
