package cn.mw.monitor.logManage.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "日志分析请求参数")
@Data
public class LogAnalysisParam {
    /**
     * 查询的表名
     */
    @ApiModelProperty(name = "查询的表名")
    private String tableName;

    /**
     * 条件
     */
    @ApiModelProperty(name = "查询条件，直接传递输入框中的语句即可")
    private String condition;

    /**
     * 需要返回的字段,以逗号分割
     */
    @ApiModelProperty(name = "需要返回显示的字段名")
    private String showColumn;

    /**
     * 1：SPL搜索； 2：KQL搜索
     */
    @ApiModelProperty(name = "搜索类型：1 SPL搜索 2 KQL搜索", value = "1")
    private Integer searchType;

    /**
     * 开始时间
     */
    private TimeParam startTime;

    /**
     * 结束时间
     */
    private TimeParam endTime;

    /**
     * 排序字段
     */
    @ApiModelProperty("排序字段名")
    private String sortColumn;

    /**
     * 排序方式（desc 或者 asc）
     */
    @ApiModelProperty("排序方式（desc 或者 asc）")
    private String sortType;

    @ApiModelProperty("后端自用的参数传递，前端无需理会")
    private String userIdOrName;

    // 第几页
    @ApiModelProperty("第几页")
    @TableField(exist = false)
    private Integer pageNum = 1;
    // 每页显示行数
    @ApiModelProperty("每页显示行数")
    @TableField(exist = false)
    private Integer pageSize = 20;

}
