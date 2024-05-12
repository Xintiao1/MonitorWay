package cn.mw.monitor.customPage.model;

import lombok.Data;

@Data
public class MwPageselectTable {

    // id
    private Integer id;
    // 页面ID
    private Integer pageId;
    // 字段代码
    private String prop;
    // 字段名称
    private String label;
    // 输入格式(1.文本2.日期3.下拉框)
    private String inputFormat;
    // 下拉框请求url
    private String url;

    private Integer typeof;
    // 是否展开
    private Integer isTree;

}
