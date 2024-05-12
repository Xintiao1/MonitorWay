package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("测试属性")
public class Label {
    //主键
    @ApiModelProperty(value="标签名称")
    String labelName;


    @ApiModelProperty(value="下拉的对应对应下拉id")
    String labelDrop;

    @ApiModelProperty(value="0.标识基础属性 1.标识高级属性")
    boolean labelLevel;

    @ApiModelProperty(value="随机字符串标识")
    private Integer labelIpId;

    @ApiModelProperty(value="下拉选择id")
    private Integer labelId;

    @ApiModelProperty(value="选项值")
    private Integer labelDropId;

    @ApiModelProperty(value="ip种类 0.4 1.6")
    private Integer labelIpType;

    @ApiModelProperty(value="选项值内容")
    private String dropValue;

    @ApiModelProperty(value="下拉选项")
    private List<LabelCheck> labelChecks;
    @ApiModelProperty(value="是否新增")
    private Boolean chooseAdd;
    @ApiModelProperty(value="是否必填")
    private Boolean isRequired;
    @ApiModelProperty(value="下拉的对应对应下拉id")
    private String prop;
    @ApiModelProperty(value = "1:文本2:时间3:下拉框")
    private String inputFormat;
    @ApiModelProperty(value = "文本值")
    private String testValue;
    @ApiModelProperty(value = "文本标签值")
    private String tagboard;



    /**
     * 时间框标签值
     */
    @ApiModelProperty(value = "下拉框标签值;对应mw_dropdown_table的drop_id")
    private Integer dropTagboard;
    /**
     * 时间框标签值
     */
    @ApiModelProperty(value = "时间框标签值")
    private Date dateTagboard;


    public void setInitDropValue(){
        this.dropValue = this.testValue;
    }

}
