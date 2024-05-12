package cn.mw.monitor.model.param;

import lombok.Data;

import java.util.List;

@Data
public class BatchUpdatePropertiesStatusParam {
    private Integer modelId;
    private String indexId;
    private Boolean isShow;
    private Integer sort;
}
