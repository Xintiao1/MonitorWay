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
public class MwModelDigitalTwinDeviceLinkParam {
    //设备Id
    private String deviceId;
    //链路数据
    private List<MwModelDigitalTwinLinkParam> linkList;


}
