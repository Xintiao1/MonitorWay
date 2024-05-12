package cn.mw.monitor.vendor.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2021/1/20 10:02
 * @Version 1.0
 */
@Data
public class MwVendorManageTable {
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

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;
}
