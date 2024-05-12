package cn.mw.xiangtai.plugin.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttackFrequencyDTO {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer count;
}
