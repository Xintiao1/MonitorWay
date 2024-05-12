package cn.mw.xiangtai.plugin.domain.vo;

import cn.mw.xiangtai.plugin.domain.dto.AttackDataDTO;
import cn.mw.xiangtai.plugin.domain.dto.AttackTypeDTO;
import cn.mw.xiangtai.plugin.domain.dto.LogPointDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("威胁态势数据对象")
public class ThreatSituationDataVO {

    @ApiModelProperty("攻击源TOP N")
    private ExternalAttackSourceVO attackSourceTopN;

    @ApiModelProperty("攻击类型TOP N")
    private List<AttackTypeDTO> attackTypeTopN;

    @ApiModelProperty("日志总事件(5年)")
    private List<LogPointDTO> logEventCount;

    @ApiModelProperty("攻击事件总数(5年)")
    private List<LogPointDTO> attackEventCount;

    @ApiModelProperty("攻击数据分析")
    private List<AttackDataDTO> attackDataAnalysis;

    @ApiModelProperty("近一周攻击事件总数")
    private List<LogPointDTO> attackEventCountByWeek;

}
