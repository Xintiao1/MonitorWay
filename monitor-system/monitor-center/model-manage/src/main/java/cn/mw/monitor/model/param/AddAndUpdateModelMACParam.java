package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/4/28
 */
@Data
public class AddAndUpdateModelMACParam extends BaseParam {
    @ApiModelProperty("mac地址前六位")
    private String mac;
    @ApiModelProperty("厂商/品牌全称")
    private String brand;
    @ApiModelProperty("厂商/品牌Id")
    private Integer brandId;
    @ApiModelProperty("简称")
    private String shortName;
    @ApiModelProperty("城市")
    private String country;
    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("修改之前的mac地址前六位")
    private String oldMac;

    @ApiModelProperty("批量删除使用")
    private List<String> macList;

    /**
     * 模糊查询所有字段的条件
     */
    private String value;
}
