package cn.mw.monitor.service.topo.model;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className TopoLabelInfo
 * @description 拓扑标签信息
 * @date 2022/7/21
 */
@Data
public class TopoLabelInfo {

    /**
     * 标签ID
     */
    private Integer labelId;

    /**
     * 标签名称
     */
    private String labelName;

    /**
     * 标签类别  1.文本 2.日期 3.下拉
     */
    private Integer labelType;

    /**
     * 拓扑ID
     */
    private String topoId;

    /**
     * 二级标签记录ID
     */
    private Integer secondTypeId;

    /**
     * 二级标签标签具体内容
     */
    private String secondTypeName;

}
