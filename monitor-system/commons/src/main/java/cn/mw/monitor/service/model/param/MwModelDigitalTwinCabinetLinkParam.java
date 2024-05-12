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
public class MwModelDigitalTwinCabinetLinkParam {
    //机柜Id
    private String cabinetId;
    //设备数据
    private List<MwModelDigitalTwinDeviceLinkParam> deviceList;


}
