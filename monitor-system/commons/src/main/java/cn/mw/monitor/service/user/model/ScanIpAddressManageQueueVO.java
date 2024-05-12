package cn.mw.monitor.service.user.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 202211-1616 10:41
 * @description 扫描队列
 */
@Data
public class ScanIpAddressManageQueueVO {
    @ApiModelProperty(value="队列序号")
    Integer id;
    @ApiModelProperty(value="关联ip地址段")
    Integer linkId;
    @ApiModelProperty(value="用户id")
    Integer userId;
    @ApiModelProperty(value="参数内容")
    String param;
}
