package cn.mw.monitor.credential.api.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by zy.quaee on 2021/5/31 16:10.
 **/
@Data
@ApiModel(value="系统凭据删除参数")
public class DeleteCredentialParam {

    /**
     * 凭据id集合
     */
    @ApiModelProperty(value="凭据id集合")
    private List<Integer> ids;

    /**
     * 凭据类型
     */
    @ApiModelProperty(value = "凭据类型")
    private String credType;
}
