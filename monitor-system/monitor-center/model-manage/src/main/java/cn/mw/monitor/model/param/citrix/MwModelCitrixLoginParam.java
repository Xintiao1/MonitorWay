package cn.mw.monitor.model.param.citrix;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/8
 */
@Data
public class MwModelCitrixLoginParam {
    private String url;
    private String userName;
    private String password;
    private String port;
    private String type;
    private Boolean isRelationQuery;
    private Integer modelId;
    private Integer instanceId;
}
