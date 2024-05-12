package cn.mw.monitor.service.server.api.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author syt
 * @Date 2021/6/10 16:31
 * @Version 1.0
 */
@Data
@Slf4j
public class MWHistoryDTO {

    private String itemid;
    private String clock;
    private String value;
    private String ns;
    private Double lastValue;
    private Double valueAvg;
    private Double valueMin;
    private Double valueMax;

    public void setValue(String value) {
        this.value = value;
        try {
            Double aDouble = Double.parseDouble(value);//当历史数据可以正常转换时，赋值给lastValue
            this.lastValue = aDouble;
        } catch (Exception e) {
            log.error("fail to Double.parseDouble(value) value:{} cause:{}", value, e);
        }
    }
}
