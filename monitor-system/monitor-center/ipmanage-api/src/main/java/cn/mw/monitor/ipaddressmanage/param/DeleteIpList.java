package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 20220915 14:33
 * @description 15
 */
@Data
public class DeleteIpList extends BaseParam {
    @ApiModelProperty(value="获取所在分布")
    String applicantId;

    @ApiModelProperty(value="回收的id集合")
    List<Integer> ids;

    @ApiModelProperty(value="回收的id集合")
    Integer id = 0;
}
