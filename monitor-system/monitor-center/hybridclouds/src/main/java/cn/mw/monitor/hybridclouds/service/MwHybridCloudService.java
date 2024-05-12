package cn.mw.monitor.hybridclouds.service;

import cn.mw.monitor.hybridclouds.dto.QueryNewHostParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/6/8
 */
public interface MwHybridCloudService {

    Reply getAllTree();

    Reply getBasic(QueryNewHostParam qParam);

    Reply getHhyTable(QueryNewHostParam qParam);
}
