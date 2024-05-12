package cn.mw.monitor.prometheus.dto;

import lombok.Data;

import java.util.List;

@Data
public class PanelConfigDto {

    /**
     * 面板id
     */
    private int id;

    /**
     * 面板名称
     */
    private String panelName;

    /**
     * 面板类型
     */
    private String panelType;

    /**
     * 查询条件
     */
    private Integer queryId;

    /**
     * 创建人
     */
    private Integer creator;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 删除标记
     */
    private boolean deleteFlag;

    /**
     * 布局id
     */
    private Integer layoutId;

    /**
     * 坐标信息
     */
    private Integer x;
    private Integer y;
    private Integer z;
    private Integer w;
    private Integer h;

    private List<PanelQueryRelDto> columnList;
}
