package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/1 9:26
 */
@Data
@Builder
public class OrgMapper {
    private String typeId;
    private Integer orgId;
    private String type;
}
