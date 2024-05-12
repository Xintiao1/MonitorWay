package cn.mw.monitor.service.alert.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/29 14:19
 */
@Data
public class ItemData {
    private String name;
    private String value;
    private String chName;
    private String valuemapId;
    private String itemId;
    private Double newValue;
}
