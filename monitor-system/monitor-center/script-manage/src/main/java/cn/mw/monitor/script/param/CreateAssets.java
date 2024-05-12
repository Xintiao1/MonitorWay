package cn.mw.monitor.script.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author lumingming
 * @createTime 2023501 21:28
 * @description
 */
@Data
public class CreateAssets {
    /**
     * 主键ID
     */
    @ApiModelProperty("id")
    private Integer id;
    /**
     * 资产名称
     */
    @ApiModelProperty("资产名称")
    @Size(max = 256, message = "账号名称最大长度不能超过256字符！")
    private String assetName;
    /**
     * ip访问地址
     */
    @ApiModelProperty("ip访问地址")
    @Size(max = 256, message = "账号名称最大长度不能超过256字符！")
    private String iPAddress;

    @ApiModelProperty("账号ID")
    private Integer accountId;

}
