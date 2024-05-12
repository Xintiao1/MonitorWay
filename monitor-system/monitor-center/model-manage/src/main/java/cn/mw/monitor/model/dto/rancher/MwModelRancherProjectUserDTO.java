package cn.mw.monitor.model.dto.rancher;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherProjectUserDTO {
    private String id;
    private String projectId;
    private String userName;
    private String userId;
    private String type;
    private Integer mwUserId;
}
