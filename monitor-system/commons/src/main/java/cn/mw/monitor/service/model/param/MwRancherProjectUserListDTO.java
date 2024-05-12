package cn.mw.monitor.service.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwRancherProjectUserListDTO {
    private String id;
    private Integer modelInstanceId;
    private Integer modelId;
    private String instanceName;
    private String projectId;
    private List<Integer> userIds;
    private List<Integer> groupIds;
    //名称
    private String relationModelSystem;
    private String relationModelClassify;
    private String relationName;
    private String relationIp;

    private String inBandIp;

}
