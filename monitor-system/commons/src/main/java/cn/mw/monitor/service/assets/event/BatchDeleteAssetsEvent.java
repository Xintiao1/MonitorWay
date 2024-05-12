package cn.mw.monitor.service.assets.event;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteAssetsEvent extends Event {
    private List<DeleteTangAssetsID> deleteTangAssetsIDList;
}
