package cn.mw.monitor.dropDown.model;

import lombok.Data;

import java.util.Date;

@Data
public class MwDropdownTable {

    // 下拉框id
    private Integer dropId;
    // 下拉框Code
    private String dropCode;
    // 下拉框Key
    private Integer dropKey;
    // 下拉框Value
    private String dropValue;
    // 更新时间
    private Date updateTime;
    // 删除标识
    private Boolean deleteFlag;

}