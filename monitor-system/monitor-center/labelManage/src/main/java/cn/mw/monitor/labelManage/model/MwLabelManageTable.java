package cn.mw.monitor.labelManage.model;

import lombok.Data;

import java.util.Date;

@Data
public class MwLabelManageTable {

    // 标签Id
    private Integer labelId;
    // 标签名称
    private String labelName;
    // 格式（1.文本2.日期3.下拉4.其他）
    private Integer inputFormat;
    // 是否可添加下拉值
    private Boolean chooseAdd;
    // 是否告警
    private Boolean earlyWarning;
    // 是否筛选
    private Boolean screen;
    // 是否报表
    private Boolean report;
    // 是否必填
    private Boolean isRequired;
    // 下拉标签枚举值
    private String dropdownValue;
    // 标签编号
    private String labelCode;
    // 删除限制
    private Boolean deleteRestrict;
    // 标签状态
    private String enable;
    // 创建人
    private String creator;
    // 创建日期
    private Date createDate;
    // 修改人
    private String modifier;
    // 修改日期
    private Date modificatioDate;
    // 删除标识
    private Boolean deleteFlag;

}
