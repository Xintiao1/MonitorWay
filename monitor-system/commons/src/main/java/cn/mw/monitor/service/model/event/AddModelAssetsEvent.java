package cn.mw.monitor.service.model.event;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddModelAssetsEvent extends Event {

    private AddUpdateTangAssetsParam addModelAssetsParam;
}
