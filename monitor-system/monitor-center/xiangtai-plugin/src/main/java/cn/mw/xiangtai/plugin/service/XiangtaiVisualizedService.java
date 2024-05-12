package cn.mw.xiangtai.plugin.service;

import cn.mw.xiangtai.plugin.domain.dto.AttackDataDTO;
import cn.mw.xiangtai.plugin.domain.dto.AttackTypeDTO;
import cn.mw.xiangtai.plugin.domain.dto.LogPointDTO;
import cn.mw.xiangtai.plugin.domain.param.EventParam;
import cn.mw.xiangtai.plugin.domain.vo.ComprehensiveSituationDataVO;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.domain.vo.ExternalAttackSourceVO;
import cn.mw.xiangtai.plugin.domain.vo.ThreatSituationDataVO;
import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface XiangtaiVisualizedService {


    /**
     * 攻击源top n
     *
     * @param topN top几
     * @return 攻击源topN对象
     */
    ExternalAttackSourceVO attackSourceTopN(Integer topN);

    /**
     * 攻击类型top n
     *
     * @param topN top几
     * @return 攻击类型topN对象
     */
    List<AttackTypeDTO> attackTypeTopN(Integer topN);

    /**
     * 日志总事件 （事件参数）
     *
     * @return 日志总事件对象
     */
    List<LogPointDTO> logEventCount(EventParam param);

    /**
     * 攻击事件总数
     *
     * @return 攻击事件总数对象
     */
    List<LogPointDTO> attackEventCount(EventParam param);

    /**
     * 攻击数据分析
     * @return list
     */
    List<AttackDataDTO> attackDataAnalysis();

    /**
     * 一周内攻击事件总数
     *
     * @return 攻击事件总数对象
     */
    List<LogPointDTO> attackEventCountByWeek();

    ThreatSituationDataVO getThreatSituationData();

    /**
     * 后去综合态势数据
     * @return data
     */
    ComprehensiveSituationDataVO getComprehensiveSituationData();
}
