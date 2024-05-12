package cn.mw.monitor.netflow.param;

import lombok.Data;

import java.util.List;

@Data
public class PerformNetflowParam {
    private boolean start;
    private List<AssetParam> paramList;
}
