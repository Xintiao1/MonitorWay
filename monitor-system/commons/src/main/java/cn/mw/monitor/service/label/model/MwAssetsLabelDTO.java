package cn.mw.monitor.service.label.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/4/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ApiModel(value = "标签结果集")
public class MwAssetsLabelDTO {
    @ApiModelProperty(value = "下拉框id")
    private Integer id;

    @ApiModelProperty(value = "下拉框prop")
    private String prop;

    @ApiModelProperty(value = "下拉框url")
    private String url;
    /**
     * 标签Id
     */
    @ApiModelProperty(value = "标签Id")
    private Integer labelId;

    /**
     * 资产Id
     */
    @ApiModelProperty(value = "资产Id")
    private String assetsId;

    /**
     * 标签名称
     */
    @ApiModelProperty(value = "标签名称")
    private String labelName;

    /**
     * 标签名称
     */
    @ApiModelProperty(value = "标签值")
    private String labelValue;

    /**
     * 文本标签值
     */
    @ApiModelProperty(value = "文本标签值")
    private String tagboard;

    /**
     * 下拉框标签值
     */
    @ApiModelProperty(value = "下拉框标签ID")
    private Integer dropId;

    /**
     * 下拉框标签值
     */
    @ApiModelProperty(value = "下拉框标签值")
    private String dropValue;
    /**
     * 时间框标签值
     */
    @ApiModelProperty(value = "时间框标签值")
    private Date dateTagboard;


    /**
     * 时间框标签值
     */
    @ApiModelProperty(value = "下拉框标签值;对应mw_dropdown_table的drop_id")
    private Integer dropTagboard;
    /**
     * 是否可添加下拉值
     */
    @ApiModelProperty(value = "是否可添加下拉值")
    private Boolean chooseAdd;

    /**
     * 是否可添加下拉值
     */
    @ApiModelProperty(value = "输入框类型")
    private String inputFormat;

    @ApiModelProperty(value = "模块id")
    private Integer moduleId;
}
