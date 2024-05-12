package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class MwModelAlertShowRoomParam {
    //机房Id
    private String id;
    //机房名称
    private String name;
    //楼宇Id
    private String buildId;
    //机房下机柜列表数据
    private List<MwModelAlertShowCabinetParam> cabinets;

}
