package cn.mw.monitor.service.label.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


/**
 * @author xhy
 * @date 2020/11/25 16:17
 */
@Data
public class LogicalQueryLabelParam {
    //@ApiModelProperty("逻辑运算符 and / or")
   // private String logicalOperator;
 //   @ApiModelProperty("条件选择  like  eq  lt  ge le  ")
    /**
     * 如果inputFormat==1 文本格式  只能是like
     * 如果inputFormat==2 时间格式  eq= lt< gt>  ge>= le<=
     * 如果inputFormat==3 下拉格式  只能是eq
     */
  //  private String conditionOption;
    @ApiModelProperty("标签ID")
    private String labelId;
    @ApiModelProperty("标签名称")
    private String labelName;
    @ApiModelProperty("标签格式")
    private String inputFormat;

    @ApiModelProperty("文本标签的值")
    private String tagboard;

    private List<Date> time;

    @ApiModelProperty("时间标签的值")
    private Date startdateTagboard;

    private Date enddateTagboard;

    @ApiModelProperty("下拉标签的值")
    private String dropTagboard;

    @ApiModelProperty("模块类型 例如ASSETS")
    private String modelType;

    @ApiModelProperty("数据库映射表名称")
    private String tableName;


}
