package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className RuleManage
 * @description 规则管理实体类
 * @date 2021/12/28
 */
@Data
public class RuleManage {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则所在文件夹地址ID
     */
    private Integer ruleTreeGroup;

    /**
     * 规则描述
     */
    private String ruleDescribe;

    /**
     * 规则匹配内容
     */
    private String ruleMatchContent;

    /**
     * 匹配内容类别 0：字符串  1：正则表达式
     */
    private Integer matchContentType;

    /**
     * 匹配方式 0：匹配1：不匹配
     */
    private Integer ruleMatchType;

    /**
     * 是否为高级搜索(0：普通检索  1：高级检索)
     */
    private Integer seniorType;


    /**
     * 规则评判等级(0：一般 1：警告 2：严重)
     */
    private Integer ruleLevel;

    /**
     * 修复脚本类别（暂无）
     */
    private Integer ruleRepairType;

    /**
     * 修复动作（暂无）
     */
    private String ruleRepairString;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    private Date modificationDate;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 创建人
     */
    private String creator;

}
