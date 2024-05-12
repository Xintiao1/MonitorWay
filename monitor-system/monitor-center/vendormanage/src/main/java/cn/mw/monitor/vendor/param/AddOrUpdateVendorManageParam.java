package cn.mw.monitor.vendor.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2021/1/20 9:46
 * @Version 1.0
 */
@Data
public class AddOrUpdateVendorManageParam {
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
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("自定义厂商名称")
    private String baseVendor;
    @ApiModelProperty("厂商图标")
    private String vendorSmallIcon;
    @ApiModelProperty("mac厂商全称")
    private String macVendor;
    @ApiModelProperty("mac厂商简称")
    private String shortName;
    @ApiModelProperty("mac前六位")
    private String mac;
    @ApiModelProperty("自定义厂商标识")
    private Boolean customBrand;
    private Integer customFlag;
}
