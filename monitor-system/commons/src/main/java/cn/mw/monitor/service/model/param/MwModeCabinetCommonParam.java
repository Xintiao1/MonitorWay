package cn.mw.monitor.service.model.param;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MwModeCabinetCommonParam {
    private String instanceName;
    private Integer modelInstanceId;
    private String modelIndex;
    private Integer relationSiteRoom;
    private List<CabinetLayoutDataParam> layoutData;
    private Integer UNum;
}
