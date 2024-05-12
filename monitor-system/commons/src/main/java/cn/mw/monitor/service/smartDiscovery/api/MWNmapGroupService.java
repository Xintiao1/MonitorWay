package cn.mw.monitor.service.smartDiscovery.api;

import cn.mwpaas.common.model.Reply;
import org.springframework.stereotype.Service;

public interface MWNmapGroupService {

    Reply getDropDownFingerNodeGroup();

    Reply getDropDownExceptionNodeGroup();

    Reply getDropDownPortGroup();

    Reply getDropDownLiveNodeGroup();

    Reply getDropDownNodeGroup();

}
