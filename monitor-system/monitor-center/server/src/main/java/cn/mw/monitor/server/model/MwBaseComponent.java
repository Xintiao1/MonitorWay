package cn.mw.monitor.server.model;

import lombok.Data;

/**
 * @author syt
 * @Date 2021/2/3 14:53
 * @Version 1.0
 */
@Data
public class MwBaseComponent {
    private int baseComId;
    private String componentName;
    private String componentUrl;
    private String componentParam;
    private String componentSelfParam;
    private String componentType;
}
