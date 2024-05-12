package cn.mw.monitor.dev.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.param.role.AddUpdateModuleParam;
import cn.mw.monitor.api.param.role.DeleteModuleParam;
import io.swagger.models.auth.In;

import java.util.List;

public interface MWModuleService {

    Reply insertRoleModule(AddUpdateModuleParam param);

    Reply deleteRoleModule(List<Integer> ids);

    Reply updateRoleModule(AddUpdateModuleParam param);

    Reply selectRoleModule(Integer id);
}
