package cn.mw.monitor.service.assets.model;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/3/27
 */
@Data
public class OrgDTO {
    private Integer orgId;

    private String orgName;

    private String nodes;

    private String coordinate;
}
