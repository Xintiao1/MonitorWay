package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/6/29 10:24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolarTimeDto {
    private Integer id;
    private Integer userId;

    private String creator;
    private String modifier;

   private Integer startHourTime;
   private Integer endHourTime;
   private Integer startMinuteTime;
   private Integer endMinuteTime;

   private Integer type;
}
