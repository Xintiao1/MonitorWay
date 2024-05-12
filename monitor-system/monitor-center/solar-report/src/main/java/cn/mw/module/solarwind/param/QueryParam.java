package cn.mw.module.solarwind.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/22 16:38
 */
@Data
public class QueryParam extends BaseParam {
    @ApiModelProperty(value="接口线路分支名称")
    private String carrierName;

    private Integer interfaceID;

    private String caption;

    @ApiModelProperty("1全天(自定义时间段）  2 全天(24小时) 3工作日（自定义时间段） 4 工作日（24小时）")
    private Integer dayType;

    @ApiModelProperty("是否高级查询 true 是")
    private Boolean seniorchecked;

    @ApiModelProperty("非高级查询选择的日期")
    private List<String> fixedDate;

    //高级查询对应的字段
    @ApiModelProperty("选择的日期")
    private List<String> chooseTime;

    @ApiModelProperty("选择的时间")
    private List<String> timeValue;

    @ApiModelProperty("工作日 全天")
    private String periodRadio;

    @ApiModelProperty("最大值，最小值，平均值")
    private String valueType;

    private String mouthTime;

    private  List<Integer> interfaceIds;
    //判断是否为导出前查询
    private Boolean isExport;
}
