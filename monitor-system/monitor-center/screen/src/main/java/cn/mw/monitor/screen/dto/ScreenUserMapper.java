package cn.mw.monitor.screen.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/21 9:48
 */
@Data
@Builder
public class ScreenUserMapper {
    private Integer userId;
    private String userName;
    private String loginName;
    private String typeId;
    private String type;
}
