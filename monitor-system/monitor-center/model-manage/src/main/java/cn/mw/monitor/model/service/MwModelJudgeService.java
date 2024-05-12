package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.MwModelIdListParam;
import cn.mw.monitor.model.param.MwModelJudgeParam;
import cn.mwpaas.common.model.Reply;

public interface MwModelJudgeService {

    Reply insertJudgeMessage(MwModelJudgeParam param);

    Reply selectJudgeMessage(MwModelJudgeParam param);

    Reply checkJudgeCycle(MwModelJudgeParam param);

    Reply deleteJudgeMessage(MwModelIdListParam param);
}
