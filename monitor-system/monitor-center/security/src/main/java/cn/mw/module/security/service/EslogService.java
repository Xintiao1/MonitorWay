package cn.mw.module.security.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.module.security.dto.AddEsLogParam;
import cn.mw.module.security.dto.EslogParam;
import cn.mw.module.security.dto.EslogUpdateParam;
import cn.mw.module.security.dto.MessageParam;

/**
 * @author xhy
 * @date 2020/9/7 15:24
 */
public interface EslogService {
    Reply getLogList(EslogParam param);

    Reply updateLogList(EslogUpdateParam param);

    Reply creatLog(AddEsLogParam param);

    Reply getMessageList(MessageParam param);
}
