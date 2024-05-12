package cn.mw.monitor.service.assets.event;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import lombok.Builder;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/23
 */
@Data
@Builder
public class UpdateTangibleassetsEvent extends Event{
    public AddUpdateTangAssetsParam updateTangAssetsParam;
}
