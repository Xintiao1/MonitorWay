package cn.mw.monitor.api.param.aduser;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by zy.quaee on 2021/8/30 10:26.
 **/
@Data
@ApiModel(value = "删除AD映射配置中用户")
public class DeleteADUserParam {


    @ApiModelProperty("用户id集合")
    private List<Integer> userIdList;

    /**
     * 用作删除config 表中user
     */
    @ApiModelProperty("id集合")
    private List<Integer> idList;
}
