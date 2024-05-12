package cn.mw.monitor.configmanage.entity;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author gui.quanwang
 * @className PolicyManageDTO
 * @description 策略管理类
 * @date 2021/12/21
 */
@Data
@ApiModel("配置管理--策略管理管理")
public class PolicyManageDTO extends DataPermissionParam {

    /**
     * 批量删除时的主键ID
     */
    private List<Integer> ids;

    /**
     * 报告ID
     */
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "策略管理名称")
    private String policyName;

    /**
     * 文件夹地址ID
     */
    @ApiModelProperty(value = "策略对应的文件夹地址ID")
    private Integer policyTreeGroup;

    /**
     * 策略描述
     */
    @ApiModelProperty(value = "策略描述")
    private String policyDesc;

    /**
     * 策略配置类别
     */
    @ApiModelProperty(value = "策略配置类别")
    private Integer configType;

    /**
     * 检测类别（0：厂商  1：自定义）
     */
    @ApiModelProperty(value = "检测类别")
    private Integer detectAssetsType;

    /**
     * 判断条件（0：等于  1：不等于）
     */
    @ApiModelProperty(value = "判断条件")
    private Integer detectCondition;

    /**
     * 厂商ID
     */
    @ApiModelProperty(value = "厂商ID")
    private Integer vendorId;

    /**
     * 厂商名称
     */
    private String vendorName;

    /**
     * 规则列表
     */
    private List<String> ruleList;

    /**
     * 查询资产ID列表
     */
    private List<String> assetsIdList;

    /**
     * 查询资产ID列表
     */
    private List<HashMap<String, String>> assetsList;

    /**
     * 创建用户
     */
    private String creator;

    /**
     * 更新用户
     */
    private String updater;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 全局模糊搜索字段
     */
    @ApiModelProperty(value = "全局模糊搜索字段")
    private String searchAll;

    /**
     * 查询集合（内部使用。当用户为非系统管理员时，值生效）
     */
    private String findInSet;

    /**
     * 当前用户是否为系统管理员（内部使用）
     */
    private boolean systemUser;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        return DataType.DETECT_POLICY_MANAGE;
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
