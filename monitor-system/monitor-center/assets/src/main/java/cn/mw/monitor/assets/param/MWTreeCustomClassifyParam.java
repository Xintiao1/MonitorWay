package cn.mw.monitor.assets.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName MWTreeCustomClassifyParam
 * @Author gengjb
 * @Date 2021/9/9 14:04
 * @Version 1.0
 **/
@Data
public class MWTreeCustomClassifyParam extends BaseParam {

    @ApiModelProperty("自定义分类ID")
    private Integer id;

    /**
     * 自定义名称
     */
    @ApiModelProperty("自定义名称")
    private String customName;

    /**
     * 一级分类ID
     */
    @ApiModelProperty("一级分类ID")
    private Integer oneLevelClassifyId;

    /**
     * 一级分类名称
     */
    @ApiModelProperty("一级分类名称")
    private String oneLevelClassifyName;

    /**
     * 二级分类ID
     */
    @ApiModelProperty("二级分类ID")
    private Integer twoLevelClassifyId;

    /**
     * 二级分类名称
     */
    @ApiModelProperty("二级分类名称")
    private String twoLevelClassifyName;

    /**
     * 三级分类ID
     */
    @ApiModelProperty("三级分类ID")
    private Integer threeLevelClassifyId;

    /**
     * 三级分类名称
     */
    @ApiModelProperty("三级分类名称")
    private String threeLevelClassifyName;

    /**
     * 分类类型（属于哪个模块）
     */
    @ApiModelProperty("分类类型")
    private String classifyType;

    /**
     * 自定义分类ID的集合
     */
    private List<Integer> customIds;

    /**
     * 对应无形资产（2），有形资产（1），带外资产（3）
     */
    private Integer tableType;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;
}
