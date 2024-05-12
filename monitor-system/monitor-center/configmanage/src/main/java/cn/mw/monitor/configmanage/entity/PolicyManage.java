package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className PolicyManage
 * @description 策略管理实体类
 * @date 2021/12/21
 */
@Data
public class PolicyManage {

    /**
     * 报告ID
     */
    private Integer id;

    /**
     * 报告名称
     */
    private String policyName;

    /**
     * 文件夹地址ID
     */
    private Integer policyTreeGroup;

    /**
     * 策略描述
     */
    private String policyDesc;

    /**
     * 策略配置类别
     */
    private Integer configType;

    /**
     * 检测类别（0：厂商  1：自定义）
     */
    private Integer detectAssetsType;

    /**
     * 判断条件（0：等于  1：不等于）
     */
    private Integer detectCondition;

    /**
     * 厂商ID
     */
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

}
