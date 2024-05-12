package cn.mw.monitor.service.smartDiscovery.api;

import cn.mw.monitor.service.smartDiscovery.param.AddUpdateNmapTaskParam;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapResultParam;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapTaskParam;
import cn.mwpaas.common.model.Reply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

public interface MWNmapTaskService {


      Reply insert(AddUpdateNmapTaskParam param);

      Reply selectResult(QueryNmapResultParam param);

      Reply pageUser(QueryNmapTaskParam qParam);

      Reply selectNmapTaskDetails(QueryNmapResultParam param);

      Reply updateNmapTask(AddUpdateNmapTaskParam param);

      Reply runNmapTask(QueryNmapResultParam param);

       Reply delete(List<Integer> idList);
}
