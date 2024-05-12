package cn.mw.monitor.screen.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/21 11:21
 */
@Data
@Builder
public class ScreenOrgMapper {
    private Integer orgId;
    private String typeId;
    private String type;
}
