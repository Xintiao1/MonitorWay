package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/1 9:26
 */
@Data
@Builder
public class GroupMapper {
    private String typeId;
    private Integer groupId;
    private String type;
}
