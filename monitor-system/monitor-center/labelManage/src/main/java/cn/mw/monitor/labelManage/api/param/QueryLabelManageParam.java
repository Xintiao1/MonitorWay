package cn.mw.monitor.labelManage.api.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

@Data
public class QueryLabelManageParam extends BaseParam {

    // 标签Id
    private Integer labelId;
    // 标签类型
    private String labelName;
    private String assetsTypeId;
    // 格式（1.文本2.日期3.下拉4.其他）
    private Integer inputFormat;
    // 标签状态
    private String enable;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间开始
    private Date createDateStart;
    // 创建时间结束
    private Date createDateEnd;
    // 修改时间开始
    private Date modificationDateStart;
    // 修改时间结束
    private Date modificationDateEnd;

    private Boolean isRequired;

    private String fuzzyQuery;
}
