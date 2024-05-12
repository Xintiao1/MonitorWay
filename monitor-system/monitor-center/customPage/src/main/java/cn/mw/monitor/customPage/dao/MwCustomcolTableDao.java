package cn.mw.monitor.customPage.dao;

import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;

import java.util.List;

public interface MwCustomcolTableDao {

    int insert(List<MwCustomcolTable> records);

    int insertByModel(List<MwCustomcolTable> records);

    int updateBatch(List<UpdateCustomColDTO> records);

    @Deprecated
    int updateBatchByModel(List<UpdateCustomColDTO> records);

    int reset(List<UpdateCustomColDTO> models);

}