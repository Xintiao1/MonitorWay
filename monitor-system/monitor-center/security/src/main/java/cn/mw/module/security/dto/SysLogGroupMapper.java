package cn.mw.module.security.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author lbq
 * @date 2022/3/31 12:10
 */
@Data
@Builder
public class SysLogGroupMapper {
    private String actionId;
    private Integer groupId;
}
