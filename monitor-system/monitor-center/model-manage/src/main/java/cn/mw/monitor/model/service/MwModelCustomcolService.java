package cn.mw.monitor.model.service;

import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.api.param.UpdateCustomPageParam;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.model.dto.MwCustomcolByModelTable;
import cn.mw.monitor.model.dto.UpdateCustomPageByModelParam;
import cn.mw.monitor.model.param.QueryCustomModelparam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface MwModelCustomcolService {

    /**
     *
     */
    Reply selectById(QueryCustomModelparam queryCustomPageParam);

    List<MwCustomColDTO> getCustom(QueryCustomPageParam queryCustomPageParam);

    /**
     *
     */
    Reply update(List<MwCustomcolByModelTable> models);

    /**
     *
     */
    Reply selectByMultiPageId(QueryCustomMultiPageParam queryCustomMultiPageParam);

    /**
     *
     */
    Reply insert(List<MwCustomcolTable> models);


    Reply reset(UpdateCustomPageByModelParam uParam);
}
