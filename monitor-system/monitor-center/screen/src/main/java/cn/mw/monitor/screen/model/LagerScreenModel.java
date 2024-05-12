package cn.mw.monitor.screen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xhy
 * @date 2020/4/10 10:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LagerScreenModel implements Serializable {
    private static final long serialVersionUID = 101670063201451330L;
    private String id;
    private String modelName;
    private String modelDesc;
    private String modelContent;
    private String modelType;
}
