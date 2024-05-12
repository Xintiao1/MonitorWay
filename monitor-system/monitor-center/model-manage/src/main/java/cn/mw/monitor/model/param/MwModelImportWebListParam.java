package cn.mw.monitor.model.param;

import lombok.Data;

import java.util.List;


@Data
public class MwModelImportWebListParam {
    private List<MwModelImportWebMonitorParam> successList;

    private List<MwModelImportWebMonitorParam> errorList;
}
