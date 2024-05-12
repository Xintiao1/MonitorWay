package cn.mw.monitor.screen.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/25 13:56
 */
@Data
@Builder
public class LayoutDataDto {
    private String layoutDataId;
    private String screenId;
    private Integer layoutId;

}
