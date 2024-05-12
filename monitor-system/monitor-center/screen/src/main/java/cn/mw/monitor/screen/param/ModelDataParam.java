package cn.mw.monitor.screen.param;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/19 12:01
 */
@Data
public class ModelDataParam {
    private String bulkDataId;
    private String layoutDataId;
    private Integer modelId;
    /**
     * 是否打开地图名称
     */
    private Boolean isOpenMapName;
}
