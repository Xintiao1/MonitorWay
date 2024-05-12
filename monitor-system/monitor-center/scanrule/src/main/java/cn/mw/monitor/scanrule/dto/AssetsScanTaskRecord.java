package cn.mw.monitor.scanrule.dto;

import lombok.Data;

/**
 * @ClassName AssetsScanTaskRecord
 * @Description 记录任务
 * @Author gengjb
 * @Date 2022/9/28 10:17
 * @Version 1.0
 **/
@Data
public class AssetsScanTaskRecord {

    //任务ID
    private String taskId;

    //任务状态
    private Integer taskStatus;
}
