package cn.joinhealth.monitor.assets.dto;

import lombok.Data;

/**
 * 监控项
 * Created by jiangwenjiang on 2019/6/30.
 */
@Data
public class ItemDTO {
    private String itemid;
    private String name;
    private String key_;
    private String history;
    private String hostid;
    private String app;
    private String trends;
    private String lastvalue;
    private String delay;//时间间隔
    private Long lastclock;
    private String times;
    private String units;
    private String valueType;
    private String valuemapid;
    private String valuemapdesc;
}
