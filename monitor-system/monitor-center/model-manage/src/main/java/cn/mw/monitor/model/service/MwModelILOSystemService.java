package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.ilosystem.ILOInstanceParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2023/5/9
 */
public interface MwModelILOSystemService {
    Reply getAllILODataInfo(ILOInstanceParam param);
}
