package cn.mw.xiangtai.plugin.domain.dto;

import lombok.Data;

/**
 * 经纬度信息对象
 */
@Data
public class LongitudeLatitudeDTO {

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 国家
     */
    private String county;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

}
