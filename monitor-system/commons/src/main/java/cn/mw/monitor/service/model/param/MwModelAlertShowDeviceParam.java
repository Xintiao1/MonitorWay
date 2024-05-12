package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class MwModelAlertShowDeviceParam {
    //设备Id
    private String id;
    //设备名称
    private String name;
    //设备子类型名称
    private String type;
    //设备类型Id
    private Integer typeId;
    //设备Ip
    private String ip;
    //制造商
    private String maker;
    //设备型号
    private String model;
    //设备序列号
    private String serialNum;
    //U位数
    private int high;
    //所属机柜id
    private String cabinetId;
    //设备U位
    private int position;
    //设备状态 0-未运行，1-正常，2-故障
    private int state;
    private Integer monitorServerId;
    private String assetsId;

}
