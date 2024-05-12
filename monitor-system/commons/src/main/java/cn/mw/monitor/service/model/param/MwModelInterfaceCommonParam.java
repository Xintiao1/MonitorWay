package cn.mw.monitor.service.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/6/6
 */
@Data
public class MwModelInterfaceCommonParam {
    private String hostId;
    private String hostIp;
    private String interfaceName;
    private Boolean alertTag;
    //接口描述
    private String interfaceDesc;

    private List<String> interfaceDescs;
}
