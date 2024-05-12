package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.ModelRelationInstanceUserListParam;
import cn.mw.monitor.model.param.ModelRelationInstanceUserParam;
import cn.mw.monitor.model.param.rancher.QueryRancherInstanceParam;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author qzg
 * @date 2023/4/17
 */
public interface MwModelRancherService {
    Reply getAllRancherDataInfo(RancherInstanceParam param);
    Reply getRancherDeviceTree(QueryRancherInstanceParam param);
    Reply getRancherList(QueryRancherInstanceParam param);
    void setModelInstancePerUser(ModelRelationInstanceUserListParam params);
    List<ModelRelationInstanceUserParam> getModelInstancePerUser(ModelRelationInstanceUserParam param);
    Reply setRancherUser(ModelRelationInstanceUserListParam params);
    Reply getRancherUser(ModelRelationInstanceUserParam param);
}
