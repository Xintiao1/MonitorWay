package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/4/28
 */
@Data
public class AddAndUpdateModelFirmParam {
    @ApiModelProperty("主键id")
    private Integer id;
    @ApiModelProperty("厂商/品牌")
    private String brand;
    @ApiModelProperty("厂商小图标")
    private String vendorSmallIcon;
    @ApiModelProperty("厂商大图标")
    private String vendorLargeIcon;
    @ApiModelProperty("图标类型： 0-系统定义，1-用户上传")
    private Integer customFlag;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("规格型号")
    private String specification;
    @ApiModelProperty("规格型号设备高度")
    private int deviceHeight;
    @ApiModelProperty("厂商的规格型号数量")
    private Integer specificationNum;
}
