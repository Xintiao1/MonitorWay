package cn.mw.monitor.service.assets.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author syt
 * @Date 2020/5/29 11:16
 * @Version 1.0
 */
@Data
public class RedisItemHistoryDto implements Serializable {
    private String minValue;
    private String maxValue;
    private String avgValue;
    private String updateTime;
//    页面显示的value
    private String value;
    private Double minMax;
    private Double maxMax;
    private Double avgMax;
}
