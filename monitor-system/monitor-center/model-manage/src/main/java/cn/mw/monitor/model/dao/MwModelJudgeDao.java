package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.MwModelJudgeDTO;
import cn.mw.monitor.model.param.MwModelJudgeParam;

import java.util.List;

/**
 * @author qzg
 * @date 2023/8/31
 */
public interface MwModelJudgeDao {
    List<MwModelJudgeDTO> selectModelJudgeInfo(MwModelJudgeParam param);

    void insertModelJudgeInfo(MwModelJudgeParam param);

    MwModelJudgeDTO checkModelJudgeTime(MwModelJudgeParam param);

    void batchDeleteModelJudge(List<Integer> ids);
}
