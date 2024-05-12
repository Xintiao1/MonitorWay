package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 20211209 15:21
 * @description 规则类的基础规则
 */
@Data
@ApiModel("配置管理基础规则管理")
public class mwConfigManageBasicsRule {
    @ApiModelProperty(value="主键")
    private Integer id;
    @ApiModelProperty(value="规则String")
    private String rule;

    @ApiModelProperty(value="匹配方式（0.匹配 1.不匹配）")
    private Integer ruleRankType;

    @ApiModelProperty(value="规则描述")
    private String ruleType;

    @ApiModelProperty(value="0.一般 1.警告 2.严重")
    private Integer ruleLevel;

    @ApiModelProperty(value="修复脚本的类型 0.命令行 1.配置模板更改")
    private String ruleRepairType;

    @ApiModelProperty(value="0.一般 1.警告 2.严重")
    private Integer rule_repair_string;

    @ApiModelProperty(value="基础规则")
    private List<mwConfigManageBasicsRule> mwConfigManageBasicsRules;
}
