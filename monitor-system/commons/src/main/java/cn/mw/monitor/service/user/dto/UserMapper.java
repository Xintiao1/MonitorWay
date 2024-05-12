package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/1 9:25
 */
@Data
@Builder
public class UserMapper {
    private String typeId;
    private Integer userId;
    private String type;
}
