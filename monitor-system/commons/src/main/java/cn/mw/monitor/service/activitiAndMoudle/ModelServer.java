package cn.mw.monitor.service.activitiAndMoudle;

import cn.mw.monitor.service.model.param.MwModelInstanceCommonParam;
import cn.mw.monitor.service.model.param.QueryCustomModelCommonParam;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

/**
 * @author lumingming
 * @createTime 29 15:05
 * @description
 */
public interface ModelServer {
    Reply creatModelInstance(Object instanceParam,Integer type);

    Reply updateModelInstance(Object instanceParam,Integer type);

    Reply deleteModelInstance(Object instanceParam,Integer type);

    Reply selectModelNameById(Integer modelId);

    List<Map<String, Object>> getInstanceInfoByModelId(QueryInstanceModelParam param);

    List<MwModelInstanceCommonParam> selectModelInstanceInfoById(Integer modelId);

    Reply batchCreatModelInstance(Object batchInstanceList, Integer types);

    Reply selectModelInstanceFiledList(QueryCustomModelCommonParam queryCustomModelParam);
}
