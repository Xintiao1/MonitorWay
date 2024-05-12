package cn.mw.monitor.netflow.param;

import lombok.Data;

import java.util.List;

@Data
public class NetflowParam {
    boolean delHistory;
    private List<AssetParam> paramList;
}
