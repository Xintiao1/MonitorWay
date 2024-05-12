package cn.mw.monitor.report.dto;

import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/12/9 16:45
 *
 * {
 *     value: 1,
 *     label: '历史数据',
 *     count: 4,
 *     icon: require('@/assets/image/png/history.png')
 *   },
 *   {
 *     value: 2,
 *     label: '趋势分析',
 *     count: 1,
 *     icon: require('@/assets/image/png/qushi.png')
 *   },
 *   {
 *     value: 3,
 *     label: 'TOP  N',
 *     count: 1,
 *     icon: require('@/assets/image/png/paihang.png')
 *   }
 */
@Data
public class ReportCountDto {
    private Integer reportTypeId;
    private String reportTypeName;
    private Integer count;
    private String icon;
    private List<ReportCountDto> children;

}
