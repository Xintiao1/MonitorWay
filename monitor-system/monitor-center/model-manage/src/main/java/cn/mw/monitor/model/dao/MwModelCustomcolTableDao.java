package cn.mw.monitor.model.dao;

import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.model.dto.MwCustomcolByModelTable;

import java.util.List;

public interface MwModelCustomcolTableDao {

    @Deprecated
    int insert(List<MwCustomcolTable> records);

    @Deprecated
    int insertByModel(List<MwCustomcolTable> records);

    int updateBatch(List<MwCustomcolByModelTable> records);

    @Deprecated
    int updateBatchByModel(List<MwCustomcolByModelTable> records);

    int reset(List<MwCustomcolByModelTable> models);

}