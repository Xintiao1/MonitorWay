package cn.mw.monitor.visualized.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName MwVisualizedChartDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/14 10:11
 * @Version 1.0
 **/
@Data
public class MwVisualizedChartDto {
    /**
     * 可视化图形ID
     */
    private Integer id;

    /**
     * 可视化图形父ID
     */
    private Integer parentId;

    /**
     * 可视化图形父英文名称
     */
    private String parentPartition;

    /**
     * 可视化图形父名称
     */
    private String parentPartitionName;

    /**
     * 分区类型名称
     */
    private String partitionName;

    /**
     * 分区类型英文
     */
    private String partition;

    /**
     * 图标图片
     */
    private String iconUrl;

    /**
     * 拖拽图片
     */
    private String dragUrl;


    /**
     * 前端标识
     */
    private String sign;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;


    //创建人
    private String creator;

    //创建时间
    private Date createDate;

    //修改人
    private String modifier;

    //修改时间
    private Date modificationDate;

    private List<MwVisualizedChartDto> children;
}
