package cn.mw.monitor.service.assets.param;

import lombok.Data;

/**
 * @ClassName MWMainTainHostParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/8/9 11:34
 * @Version 1.0
 **/
@Data
public class MWMainTainHostParam {

    private String hostName;

    private String hostId;

    private Integer serverId;

    private Integer maintenanceid;

    private Integer typeId;

    //模型ID
    private String modelInstanceId;


}
