package cn.mw.monitor.service.model.param;

import lombok.Data;

import java.util.List;

@Data
public class MwModeRommCommonParam {
    private String instanceName;
    private Integer modelInstanceId;
    private String modelIndex;
    private Integer rowNum;
    private Integer colNum;
    private List<List<QueryLayoutDataParam>> layoutData;
    private List<MwModeCabinetCommonParam> relationCabinetrList;
}
