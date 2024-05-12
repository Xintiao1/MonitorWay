package cn.mw.monitor.logManage.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class BaseParam {
    // 第几页
    @ApiModelProperty("第几页")
    @TableField(exist = false)
    private Integer pageNum = 1;
    // 每页显示行数
    @ApiModelProperty("每页显示行数")
    @TableField(exist = false)
    private Integer pageSize = 20;
}
