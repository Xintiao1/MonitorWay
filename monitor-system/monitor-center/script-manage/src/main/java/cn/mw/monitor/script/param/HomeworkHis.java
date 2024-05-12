package cn.mw.monitor.script.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * @author lumingming
 * @createTime 2023615 10:46
 * @description
 */
@Data
@ApiModel(value = "历史查询参数")
public class HomeworkHis extends DataPermissionParam {

    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty("作业ID")
    private Integer homeworkId;

    @ApiModelProperty("步骤ID")
    private Integer homeworkVersionId;


    @ApiModelProperty("作业ID")
    private String fatherId;

    @ApiModelProperty("步骤ID")
    private String childId;

    @ApiModelProperty("查询执行开始时间")
    private Date createStarttime;

    @ApiModelProperty("查询执行时间")
    private Date createEndtime;

    @Override
    public DataType getBaseDataType() {
        return null;
    }

    @Override
    public String getBaseTypeId() {
        return null;
    }
}
