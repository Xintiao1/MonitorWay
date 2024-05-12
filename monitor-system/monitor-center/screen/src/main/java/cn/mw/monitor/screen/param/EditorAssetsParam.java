package cn.mw.monitor.screen.param;

import lombok.Data;


/**
 * @author xhy
 * @date 2020/7/28 11:50
 */
@Data
public class EditorAssetsParam {

    private String inBandIp;
    /**
     * 资产名称
     */
    private String assetsName;
    /**
     * 带外IP
     */
    private String outBandIp;

    /**
     * 资产类型
     */
    private Integer assetsTypeId;

    /**
     * 资产子类型
     */
    private Integer assetsTypeSubId;

    /**
     * 轮训引擎
     */
    private String pollingEngine;

    /**
     * 监控方式
     */
    private Integer monitorMode;

    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 规格型号
     */
    private String specifications;

    /**
     * 描述
     */
    private String description;


    /**
     * 首页是否默认查询
     */
    private Boolean isDefault;

}
