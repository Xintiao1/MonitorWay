package cn.mw.monitor.screen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xhy
 * @date 2020/4/12 13:36
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Model implements Serializable {
    private Integer modelId;
    private String modelName;
    private String modelDesc;
    private String modelContent;
    private String modelType;

}
