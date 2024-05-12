package cn.mw.monitor.scanrule.dto;

import lombok.Data;

/**
 * @ClassName AssetsScanTaskDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/9/21 10:37
 * @Version 1.0
 **/
@Data
public class AssetsScanTaskDto {

    private AssetsScanContext scanContext;

    //任务ID，用于获取执行任务
    private String taskId;

    //执行方式 1：单个资产扫描  2：批量扫描IP信息
    private Integer executionMode;

    //任务执行名称
    private String executionName;

    //任务开始时间
    private String startTime;

    //任务结束时间
    private String endTime;

    //执行用户
    private String executionUser;

    //用户ID
    private Integer userId;

    //登录名称
    private String loginName;

    //角色ID
    private Integer roleId;

    //权限
    private String dataPerm;


}
