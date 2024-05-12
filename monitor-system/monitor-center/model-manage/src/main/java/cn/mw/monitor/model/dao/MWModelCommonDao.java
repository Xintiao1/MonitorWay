package cn.mw.monitor.model.dao;

import cn.mw.monitor.service.model.param.MwModelInstanceCommonParam;

import java.util.List;

/**
 * @author qzg
 * @date 2022/4/28
 */
public interface MWModelCommonDao {
    List<MwModelInstanceCommonParam> selectModelInfoByRoomAndCabinet();

    List<MwModelInstanceCommonParam> selectInstanceInfoByCabinet();

    List<MwModelInstanceCommonParam> selectModelInstanceInfo(String fromUserModelId);


}
