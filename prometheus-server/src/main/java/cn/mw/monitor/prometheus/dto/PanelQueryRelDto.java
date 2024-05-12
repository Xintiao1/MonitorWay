package cn.mw.monitor.prometheus.dto;

import lombok.Data;

@Data
public class PanelQueryRelDto {

    /**
     * 面板id
     */
    private int panelId;

    /**
     * 查询id
     */
    private Integer queryId;

    /**
     * 属性名
     */
    private String propertyName;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 排序号
     */
    private int sortNo;
}
