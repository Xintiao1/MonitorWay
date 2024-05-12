package cn.mw.monitor.vendor.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2021/1/20 10:18
 * @Version 1.0
 */
@Data
public class QueryVendorManageParam extends BaseParam {
    @ApiModelProperty("主键id")
    private Integer id;
    @ApiModelProperty("厂商/品牌")
    private String brand;
    @ApiModelProperty("规格型号")
    private String specification;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("关联的厂商图标表id")
    private Integer vendorId;
    /**
     * 批量删除
     */
    @ApiModelProperty("批量删除ids")
    private List<Integer> ids;

    private String creator;

    private String modifier;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private List<Integer> vendorIds;
}
