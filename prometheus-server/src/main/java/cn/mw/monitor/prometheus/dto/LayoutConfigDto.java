package cn.mw.monitor.prometheus.dto;

import cn.mw.monitor.prometheus.dao.PanelConfigDao;
import lombok.Data;

import java.util.List;

@Data
public class LayoutConfigDto {

    /**
     * id
     */
    private Integer id;

    /**
     * 布局名称
     */
    private String layoutName;

    /**
     * 是否默认
     */
    private Boolean isDefault;

    /**
     * 创建人
     */
    private Integer creator;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 布局包含面板组件
     */
    private List<PanelConfigDto> panelConfigDtoList;
}
