package cn.mw.monitor.customPage.dao;

import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.dto.MwCustomMultiColDTO;
import cn.mw.monitor.service.model.param.MwPagefieldByModelTable;
import cn.mw.monitor.customPage.model.MwPagefieldTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwPagefieldTableDao {

    List<MwCustomColDTO> selectByUserId(QueryCustomPageParam queryCustomPageParam);

    List<MwCustomMultiColDTO> selectByMutilPageId(QueryCustomMultiPageParam queryCustomMultiPageParam);

    List<MwPagefieldTable> seletctAll();

    List<MwPagefieldByModelTable> seletctAllByModel();

    int deleteByUserId(@Param("userIds") List<Integer> userIds);

    int deleteByModelUserId(@Param("userIds") List<Integer> userIds);

    List<MwCustomColDTO> selectResetById(QueryCustomPageParam queryCustomPageParam);
}