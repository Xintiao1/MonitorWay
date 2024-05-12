package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.service.server.api.CoreValueInterface;
import lombok.Data;

@Data
public class OrgDTO implements CoreValueInterface {

    private Integer orgId;

    private String orgName;

    private String nodes;

    @Override
    public String getCoreValue() {
        return this.orgName;
    }
}
