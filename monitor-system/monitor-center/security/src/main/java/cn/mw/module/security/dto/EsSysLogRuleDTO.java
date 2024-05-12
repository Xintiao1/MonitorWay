package cn.mw.module.security.dto;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2021/12/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsSysLogRuleDTO extends BaseParam {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("ids")
    private List<Integer> ids;
    @ApiModelProperty("规则名称")
    private String ruleName;
    @ApiModelProperty("启用状态")
    private Boolean state;
    @ApiModelProperty("标签Ids")
    private List<Integer> tagIds;
    @ApiModelProperty("筛选规则Id")
    private String ruleId;
    @ApiModelProperty("规则List")
    List<MwRuleSelectParam> mwRuleSelectListParam;
    @ApiModelProperty("标签List")
    List<EsSysLogTagDTO> tagDTOList;
    private String creator;//'创建人'
    private Date createDate;//'创建时间'
    private String updater;//'修改人'
    private Date updateDate;//'修改时间'
    private List<String> ruleIds;//綁定的通知規則
    private List<Integer> actionUserIds;
    private List<Integer> actionGroupIds;
    private List<String> actions;//1:标记；2：通知
    private String action;


}
