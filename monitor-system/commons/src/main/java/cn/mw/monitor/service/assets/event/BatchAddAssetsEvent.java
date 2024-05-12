package cn.mw.monitor.service.assets.event;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import lombok.Data;

import java.util.List;

@Data
public class BatchAddAssetsEvent extends Event{
    private List<ScanResultSuccess> scanResultSuccessList;

    private boolean ignoreCodeCheck;
}
