package cn.mw.monitor.model.param;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
public class MwModelUpdateWebMonitorStateParam {

    private Integer id;
    private String assetsId;
    private String assetsName;
    private String httpTestId;
    private boolean enable;
    private Integer monitorServerId;
    private String modelIndex;
    private Integer modelInstanceId;
}
