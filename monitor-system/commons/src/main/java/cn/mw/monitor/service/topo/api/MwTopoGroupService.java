package cn.mw.monitor.service.topo.api;

import cn.mw.monitor.service.topo.model.TopoGroupView;
import cn.mw.monitor.service.topo.param.TopoGroupAddParam;
import cn.mw.monitor.service.topo.param.TopoGroupDelParam;
import cn.mw.monitor.service.topo.param.TopoGroupDragParam;
import cn.mwpaas.common.model.Reply;

public interface MwTopoGroupService {
    static final String PATH_SEP = "-";

    static final String LEFT_BRACKET = "(";

    static final String RIGHT_BRACKET = ")";

    Reply addTopoGroup(TopoGroupAddParam topoGroupAddParam);
    Reply dragTopoGroup(TopoGroupDragParam topoGroupDragParam);
    Reply deleteTopoGroup(TopoGroupDelParam topoGroupAddParam);
    Reply getTopoGroupView(boolean order);

    /**
     * 获取标签分组数据
     * @param order 排序
     * @return
     */
    Reply getTopoLabelGroupView(boolean order);

    /**
     * 获取部门机构分组数据
     * @param order 排序
     * @return
     */
    Reply getTopoDeptGroupView(boolean order);
}
