package cn.mw.monitor.model.param.rancher;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class RancherInstanceParam {
    private Integer modelId;
    private String modelIndex;
    private Integer modelInstanceId;
    private String instanceName;
    private String IPAdress;
    private String tokens;
    private String id;
    private String type;
    private String ip;
}
