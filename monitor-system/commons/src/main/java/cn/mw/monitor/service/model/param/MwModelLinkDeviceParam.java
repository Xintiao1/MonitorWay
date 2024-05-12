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
public class MwModelLinkDeviceParam {
    private Integer modelInstanceId;//链路Id
    private Integer ownLinkDeviceId;//	本端设备Id
    private String ownLinkDeviceName;//	本端设备名称
    private Integer ownLinkCabinetId;//	本端机柜Id
    private String ownLinkCabinetName;//本端机柜名称
    private List<String> multiNode;//	路径节点
    private Integer oppositeLinkDeviceId;//	对端设备Id
    private String oppositeLinkDeviceName;//对端设备名称
    private Integer oppositeLinkCabinetId;//	对端机柜Id
    private String oppositeLinkCabinetName;//	对端机柜名称
    private String ownLinkInterfaceName;//	本端接口名称
    private String oppositeLinkInterfaceName;//	对端接口名称
    private String ownInterfaceStatus;//	本端接口状态
    private String oppositeInterfaceStatus;//	对端接口状态
    private String linkMedium;//线缆介质
    private String linkType;//	线缆类型
    private String linkSpeed;//	线缆速率
    private String ownCabinetPosition;//本端机柜位置
    private String oppositeCabinetPosition;//对端机柜位置

}
