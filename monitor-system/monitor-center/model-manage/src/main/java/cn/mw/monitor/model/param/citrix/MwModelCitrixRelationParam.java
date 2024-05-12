package cn.mw.monitor.model.param.citrix;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/8
 */
@Data
public class MwModelCitrixRelationParam {
    //
    private String name;
    private Integer nameInstanceId;
    private Integer nameModeleId;
    private String servicename;
    private Integer serviceNameInstanceId;
    private Integer serviceNameModelId;

}
