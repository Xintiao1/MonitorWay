package cn.mw.xiangtai.plugin.domain.vo;

import cn.mw.xiangtai.plugin.domain.dto.AttackAddressDataDTO;
import cn.mw.xiangtai.plugin.domain.dto.AttackFrequencyDTO;
import cn.mw.xiangtai.plugin.domain.entity.SyslogAlertEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel("综合态势")
public class ComprehensiveSituationDataVO {

    @ApiModelProperty("当日攻击情况")
    private Integer toDayAttackCount;

    @ApiModelProperty("威胁等级分析")
    private List<Map<String, Integer>> threatLevel;

    @ApiModelProperty("攻击地址数据集合")
    private List<AttackAddressDataDTO> attackAddressDataList;

    @ApiModelProperty("威胁事件列表")
    private List<SyslogAlertEntity> threatEventList;

    @ApiModelProperty("攻击频率")
    private List<AttackFrequencyDTO> attackFrequencyDTOList;
}
