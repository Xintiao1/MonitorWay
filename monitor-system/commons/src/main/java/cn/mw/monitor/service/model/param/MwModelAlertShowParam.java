package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class MwModelAlertShowParam {
    //Id（前端页面选中的Id）
    private String id;
    //类型（room：机房，cabinet：机柜，device：设备，building：楼宇）
    private String type;
}
