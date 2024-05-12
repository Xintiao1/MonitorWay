package cn.mw.monitor.screen.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/21 9:49
 */
@Data
@Builder
public class ScreenGroupMapper {
    private Integer groupId;
    private String type;
    private String typeId;

}
