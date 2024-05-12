package cn.mw.monitor.virtualization.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author syt
 * @Date 2021/8/20 19:09
 * @Version 1.0
 */
@Data
@Slf4j
public class ItemValue {
    private String hostid;
    private Double lastvalue;
    public void setLastvalue(String lastvalue) {
        try {
            double d = Double.parseDouble(lastvalue);
            if (d == 0) {
                d = 10;
            }
            this.lastvalue = d;
        } catch (Exception e) {
            log.warn("String to Double error lastvalue:{}", lastvalue, e);
            this.lastvalue = null;
        }
    }
}
