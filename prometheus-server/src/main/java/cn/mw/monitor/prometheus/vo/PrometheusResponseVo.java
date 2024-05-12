package cn.mw.monitor.prometheus.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrometheusResponseVo {
    
    private PrometheusResponseDataVo data;

    private String status;
}
