package cn.mw.monitor.customPage.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.api.param.UpdateCustomPageParam;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;

import java.util.List;

public interface MwCustomcolService {

    /**
     *
     */
    Reply selectById(QueryCustomPageParam queryCustomPageParam);

    List<MwCustomColDTO> getCustom(QueryCustomPageParam queryCustomPageParam);
    /**
     *
     */
    Reply update(List<UpdateCustomColDTO> models);
    /**
     *
     */
    Reply selectByMultiPageId(QueryCustomMultiPageParam queryCustomMultiPageParam);
    /**
     *
     */
    Reply insert(List<MwCustomcolTable> models);


    Reply reset(UpdateCustomPageParam uParam);
}
