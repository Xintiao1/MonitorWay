package cn.mw.monitor.labelManage.api.param;

import cn.mw.monitor.dropDown.api.param.AddDropDownParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data

public class AddUpdateLabelManageParam {

    // 标签id
    private Integer labelId;
    // 标签名称
    private String labelName;
    // 格式（1.文本;2.日期;3.下拉;4.其他）
    private Integer inputFormat;
    // 限制值列表
    private List<AddDropDownParam> dropdownTable;
    // 是否允许添加新值
    private Boolean chooseAdd;
    // 资产类型
    private List<Integer> assetsTypeIdList;
    // 模块类型
    private List<Integer> modeList;
    // 告警
    private Boolean earlyWarning;
    // 是否筛选
    private Boolean screen;
    // 是否报表
    private Boolean report;
    // 是否必填
    private Boolean isRequired;
    // 系统创建的下拉框名称
    private String dropdownValue;
    // 标签代码
    private String labelCode;
    // 删除限制
    private Boolean deleteRestrict;
    // 标签状态(1:AVTIVE 2:DISACTIVE)
    private String enable;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;

    private List<AddDropDownParam> operateDropDowns;

}
