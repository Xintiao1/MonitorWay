package cn.mw.monitor.screen.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/12 15:56
 */
@Data
public class ModelDto {
    private String bulkDataId;
    private Integer modelId;
    private String modelName;
    private String modelDesc;
    private String modelDataId;
    private String modelType;
    private String screenId;
    /**
     * 是否打开地图名称
     */
    private Boolean isOpenMapName;
}
