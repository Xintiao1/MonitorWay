package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2022/4/28
 */
@Data
public class AddAndUpdateModelSpecificationParam extends BaseParam {
    @ApiModelProperty("主键id")
    private Integer id;
    @ApiModelProperty("主键ListIds")
    private List<Integer> ids;
    @ApiModelProperty("厂商/品牌")
    private String brand;
    @ApiModelProperty("厂商/品牌Id")
    private Integer brandId;
    @ApiModelProperty("规格型号")
    private String specification;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设备高度")
    private int deviceHeight;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    private Date modificationDateStart;
    private Date modificationDateEnd;
    private Date createDateEnd;
    private Date createDateStart;
}
