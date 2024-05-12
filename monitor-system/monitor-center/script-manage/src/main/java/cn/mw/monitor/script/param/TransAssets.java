package cn.mw.monitor.script.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className TransAssets
 * @description 资产数据信息, 用于脚本执行和文件下发和作业管理
 * @date 2022/5/22
 */
@ApiModel("资产数据信息,用于脚本执行和文件下发和作业管理")
@Data
public class TransAssets {

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ApiModelProperty("账户ID")
    private Integer accountId;

    @ApiModelProperty("执行Ansible地址")
    private String ansibleIpaddress;
}
