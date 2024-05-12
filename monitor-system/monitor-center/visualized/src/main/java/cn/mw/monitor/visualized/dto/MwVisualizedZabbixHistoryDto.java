package cn.mw.monitor.visualized.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName MwVisualizedZabbixHistoryDto
 * @Author gengjb
 * @Date 2022/4/26 15:59
 * @Version 1.0
 **/
@Data
@Builder
public class MwVisualizedZabbixHistoryDto {
    private Double value;
    private Date clock;
}
