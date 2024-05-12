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
public class MwModelDigitalTwinCabinetParam {
    //机柜Id
    private String id;
    //机柜名称
    private String name;
    //机柜位置
    private String position;
    //机柜下所有设备
    private List<MwModelDigitalTwinDeviceParam> devices;

}
