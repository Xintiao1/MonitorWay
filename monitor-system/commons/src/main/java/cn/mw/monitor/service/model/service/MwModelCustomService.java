package cn.mw.monitor.service.model.service;

import cn.mw.monitor.service.model.param.QueryModelCustomPageParam;
import cn.mw.monitor.service.model.param.QueryModelGroupParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author xhy
 * @date 2021/2/20 9:03
 */
public interface MwModelCustomService {
    Reply selectModelAllCustom(QueryModelCustomPageParam pageParam);

    Reply selectModelPropertiesAllCustom(QueryModelInstanceParam param) ;

    Reply selectModelAllIcon();

    Reply selectModelGroupList(QueryModelGroupParam param);

    Reply selectPropertiesList();
}
