package cn.mw.monitor.model.param.ilosystem;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class ILOInstanceParam {
    private Integer modelId;
    private String modelIndex;
    private Integer modelInstanceId;
    private String instanceName;
    private String IPAdress;
    private String userName;
    private String passWord;
    private String type;
    private String url;
}
