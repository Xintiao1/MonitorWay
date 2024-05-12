package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className DetectExecLog
 * @description 合规检测扫描记录信息
 * @date 2021/12/31
 */
@Data
public class DetectExecLog {

    /**
     * logID
     */
    private int id;

    /**
     * 报告的UUID
     */
    private String reportUUID;

    /**
     * 资产ID
     */
    private String assetsId;

    /**
     * 规则ID
     */
    private int ruleId;

    /**
     * 策略ID
     */
    private int policyId;

    /**
     * 报告ID
     */
    private int reportId;

    /**
     * 处理状态（0：未处理，1：处理中  2：处理结束  3：处理失败）
     */
    private int handleState;

    /**
     * 匹配等级（0.一般 1.警告 2.严重）
     */
    private int ruleLevel;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 更新人
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
