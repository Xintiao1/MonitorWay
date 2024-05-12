package cn.mw.xiangtai.plugin.service.impl;

import cn.mw.xiangtai.plugin.constants.XiangtaiConstants;
import cn.mw.xiangtai.plugin.domain.dto.*;
import cn.mw.xiangtai.plugin.domain.entity.SyslogAlertEntity;
import cn.mw.xiangtai.plugin.domain.param.EventParam;
import cn.mw.xiangtai.plugin.domain.vo.ComprehensiveSituationDataVO;
import cn.mw.xiangtai.plugin.domain.vo.ExternalAttackSourceVO;
import cn.mw.xiangtai.plugin.domain.vo.ThreatSituationDataVO;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional("mclickhouseTransactionManager")
public class XiangtaiVisualizedServiceImpl implements XiangtaiVisualizedService {

    private final SyslogAlertService syslogAlertService;

    @Autowired
    public XiangtaiVisualizedServiceImpl(SyslogAlertService syslogAlertService) {
        this.syslogAlertService = syslogAlertService;
    }

    @Override
    public ExternalAttackSourceVO attackSourceTopN(Integer topN) {
        if (topN == null || XiangtaiConstants.ZERO.equals(topN)) {
            topN = XiangtaiConstants.FIVE;
        }
        ExternalAttackSourceVO vo = new ExternalAttackSourceVO();
        List<AttackSourceDTO> attackSourceList = this.syslogAlertService.attackSourceTopN(topN);
        if (CollectionUtils.isEmpty(attackSourceList)) {
            return vo;
        }
        vo.setAttackSourceList(attackSourceList);
        int sum = attackSourceList.stream().mapToInt(AttackSourceDTO::getValue).sum();
        vo.setExAttackSourceCount((long) sum);
        return vo;
    }

    @Override
    public List<AttackTypeDTO> attackTypeTopN(Integer topN) {
        return this.syslogAlertService.attackTypeTopN(topN);
    }

    @Override
    public List<LogPointDTO> logEventCount(EventParam param) {
        if (ObjectUtils.isEmpty(param)) {
            param = new EventParam();
        }

        return this.syslogAlertService.logEventCount(param);
    }

    @Override
    public List<LogPointDTO> attackEventCount(EventParam param) {
        if (ObjectUtils.isEmpty(param)) {
            param = new EventParam();
        }
        return this.syslogAlertService.attackEventCount(param);
    }

    @Override
    public List<AttackDataDTO> attackDataAnalysis() {
        return this.syslogAlertService.attackData();
    }

    @Override
    public List<LogPointDTO> attackEventCountByWeek() {
        return this.syslogAlertService.attackEventCountByWeek();
    }

    @Override
    public ThreatSituationDataVO getThreatSituationData() {
        ThreatSituationDataVO vo = new ThreatSituationDataVO();
        vo.setAttackSourceTopN(this.attackSourceTopN(XiangtaiConstants.FIVE));
        vo.setAttackTypeTopN(this.attackTypeTopN(XiangtaiConstants.FIVE));
        vo.setLogEventCount(this.logEventCount(null));
        vo.setAttackEventCount(this.attackEventCount(null));
        vo.setAttackDataAnalysis(this.attackDataAnalysis());
        vo.setAttackEventCountByWeek(this.attackEventCountByWeek());
        return vo;
    }

    @Override
    public ComprehensiveSituationDataVO getComprehensiveSituationData() {
        // 当日攻击情况；攻击事件总数
        Integer toDayAttackCount = this.syslogAlertService.lambdaQuery().last("where create_time >= toStartOfDay(now()) and create_time <= now()").count();

        // 威胁等级分析
        List<Map<String, Integer>> threatLevelList = this.syslogAlertService.getThreatLevel();

        // 当日攻击情况
        List<AttackAddressDataDTO> toDayAttackDataList = this.syslogAlertService.getAttackDataForTheDay();

        // 威胁事件列表
        List<SyslogAlertEntity> list = this.syslogAlertService.query().orderByDesc("create_time").last("LIMIT 20").list();

        // 攻击频率  一天按4小时每段统计
        List<AttackFrequencyDTO> attackFrequencyDTOList = this.syslogAlertService.getAttackFrequency();

        ComprehensiveSituationDataVO vo = new ComprehensiveSituationDataVO();
        vo.setToDayAttackCount(toDayAttackCount);
        vo.setThreatLevel(threatLevelList);
        vo.setAttackAddressDataList(toDayAttackDataList);
        vo.setThreatEventList(list);
        vo.setAttackFrequencyDTOList(attackFrequencyDTOList);

        return vo;
    }
}
