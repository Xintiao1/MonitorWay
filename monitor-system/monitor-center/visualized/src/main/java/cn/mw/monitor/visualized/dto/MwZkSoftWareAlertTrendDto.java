package cn.mw.monitor.visualized.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName
 * @Description 告警趋势DTO
 * @Author gengjb
 * @Date 2023/3/17 11:03
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwZkSoftWareAlertTrendDto {

    private List<String> times;

    private List<Integer> datas;
}
