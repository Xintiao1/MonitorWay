package cn.mw.monitor.model.service;

import cn.mw.monitor.service.model.param.MwModelAlertShowParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2023/8/16
 */
public interface MwModelDigitalTwinService {
    Reply getAlertShowInfo(MwModelAlertShowParam param);

    Reply getPageTypeInfoById(MwModelAlertShowParam param);

    Reply getLinkInfoById(MwModelAlertShowParam param);
}
