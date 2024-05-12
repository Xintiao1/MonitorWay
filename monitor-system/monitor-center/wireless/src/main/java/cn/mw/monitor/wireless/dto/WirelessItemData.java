package cn.mw.monitor.wireless.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/29 14:19
 */
@Data
public class WirelessItemData {
    private String name;
    //数值
    private Long value;
    //带单位的数值
    private String unitsValue;

    private Long num;
}
