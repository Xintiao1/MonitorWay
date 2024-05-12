package cn.mw.monitor.customPage.model;

import lombok.Data;

@Data
public class MwPagefieldTable {

    // id
    private Integer id;
    // 页面ID
    private Integer pageId;
    // 字段代码
    private String prop;
    // 字段名称
    private String label;
    // 是否重要字段
    private Boolean importance;
    private Integer orderNum;

    //字段显示的类型  img ope txt 三种 用,分割
    private String type;
    //是否下拉
    private Boolean isTree =false;
}
