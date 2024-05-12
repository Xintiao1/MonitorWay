package cn.mw.monitor.screen.param;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/21 14:00
 */
@Data
public class UpdateModelDataParam {
    private String modelDataId;
    private Integer modelId;
    /**
     * 是否打开地图名称
     */
    private boolean isOpenMapName;
}
