package cn.mw.monitor.prometheus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class PrometheusResultVo {

    private Map<String, String> metric;

    private List<Object> value;

    private List<List<Object>> values;
}
