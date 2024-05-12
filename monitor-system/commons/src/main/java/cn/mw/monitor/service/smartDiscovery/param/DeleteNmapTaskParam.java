package cn.mw.monitor.service.smartDiscovery.param;

import lombok.Data;

import java.util.List;

@Data
public class DeleteNmapTaskParam {
    private List<Integer> nmapTaskIdList;
}
