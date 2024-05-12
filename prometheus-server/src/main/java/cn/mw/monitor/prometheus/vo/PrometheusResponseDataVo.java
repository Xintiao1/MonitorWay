package cn.mw.monitor.prometheus.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrometheusResponseDataVo {

    private List<PrometheusResultVo> result;

    private String resultType;
}
