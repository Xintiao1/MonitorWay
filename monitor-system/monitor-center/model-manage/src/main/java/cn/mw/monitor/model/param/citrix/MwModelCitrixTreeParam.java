package cn.mw.monitor.model.param.citrix;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/10/14
 */
@Data
public class MwModelCitrixTreeParam {
    private Integer id;
    private Integer modelId;
    private String modelIndex;
    private String label;
    //"LB" "GSLB"
    private String type;
    private List children;
}
