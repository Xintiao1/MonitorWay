package cn.mw.monitor.configmanage.entity;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 20211209 15:16
 * @description 配置规则管理
 */
@Data
@ApiModel("配置管理规则管理")
public class MwConfigManageRuleManage extends DataPermissionParam {

    /**
     * 批量删除时的主键ID
     */
    private List<Integer> ids;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    /**
     * 规则所在文件夹地址ID
     */
    @ApiModelProperty(value = "规则所在树状图哪个组")
    private Integer ruleTreeGroup;

    /**
     * 规则描述
     */
    @ApiModelProperty(value = "规则描述")
    private String ruleDescribe;

    /**
     * 规则匹配内容
     */
    @ApiModelProperty(value = "规则匹配内容")
    private String ruleMatchContent;

    /**
     * 是否为高级搜索(0：普通检索  1：高级检索)
     */
    @ApiModelProperty(value = "是否为高级搜索(0：普通检索  1：高级检索)")
    private Integer seniorType;

    /**
     * 匹配方式 0：匹配1：不匹配
     */
    @ApiModelProperty(value = "匹配方式 0：匹配1：不匹配")
    private Integer ruleMatchType;

    /**
     * 匹配内容类别 0：字符串  1：正则表达式
     */
    @ApiModelProperty(value = "匹配内容类别 0：字符串  1：正则表达式")
    private Integer matchContentType;

    /**
     * 规则评判等级(0：一般 1：警告 2：严重)
     */
    @ApiModelProperty(value = "0.一般 1.警告 2.严重")
    private Integer ruleLevel;

    /**
     * 修复脚本类别（暂无）
     */
    @ApiModelProperty(value = "修复脚本的类型 0.命令行 1.配置模板更改")
    private Integer ruleRepairType;

    /**
     * 修复动作（暂无）
     */
    @ApiModelProperty(value = "修复的代码")
    private String ruleRepairString;

    /**
     * 高级匹配规则
     */
    @ApiModelProperty(value = "高级匹配规则列表")
    private List<MwRuleSelectParam> seniorMatchRuleList;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间查询开始")
    private Date createDate;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间查询开始")
    private Date modificationDate;

    /**
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    private String modifier;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String creator;


    /**
     * 全局模糊搜索字段
     */
    @ApiModelProperty(value = "全局模糊搜索字段")
    private String searchAll;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        return DataType.DETECT_RULE_MANAGE;
    }

    /**
     * 获取绑定的数据ID
     *
     * @return
     */
    @Override
    public String getBaseTypeId() {
        return id + "";
    }
}
