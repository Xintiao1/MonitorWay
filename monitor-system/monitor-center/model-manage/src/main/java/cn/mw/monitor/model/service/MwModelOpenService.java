package cn.mw.monitor.model.service;

import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2023/8/3
 */
public interface MwModelOpenService {

    Reply getRoomInfoByDigitalTwin();

    Reply getLinkInfo();

    Reply getAllLinkInfo();
}
