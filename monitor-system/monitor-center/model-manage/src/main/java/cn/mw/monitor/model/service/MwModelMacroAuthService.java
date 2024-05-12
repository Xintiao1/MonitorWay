package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.DeleteModelMacrosParam;
import cn.mw.monitor.model.param.MwModelMacrosManageParam;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author qzg
 * @date 2023/6/12
 */
public interface MwModelMacroAuthService {
    Reply getMacroAuthList(MwModelMacrosManageParam param);

    Reply getAllMacroField();

    Reply queryMacroFieldByModelId(MwModelMacrosManageParam param);

    Reply addMacroAuthInfo(List<MwModelMacrosManageParam> paramList);

    Reply updateMacroAuthNameInfo(List<MwModelMacrosManageParam> paramList);

    Reply deleteMarcoInfoByModel(List<DeleteModelMacrosParam> param);

    Reply selectInfoPopup(MwModelMacrosManageParam param);
}
