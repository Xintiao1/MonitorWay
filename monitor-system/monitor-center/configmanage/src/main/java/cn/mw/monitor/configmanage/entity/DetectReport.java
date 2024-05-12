package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className DetectReport
 * @description 合规检测——检测报告
 * @date 2021/12/17
 */
@Data
public class DetectReport {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 报告的UUID
     */
    private String reportUUID;

    /**
     * 报告名称
     */
    private String reportName;

    /**
     * 报告对应的文件夹ID
     */
    private Integer reportTreeGroup;

    /**
     * 报告描述
     */
    private String reportDesc;

    /**
     * 状态
     */
    private Boolean reportState;

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
