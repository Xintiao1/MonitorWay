package cn.mw.monitor.script.entity;

import cn.mw.monitor.script.param.HomeworkParam;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className HomeworkEntity
 * @description 作业实体类
 * @date 2022/5/19
 */
@Data
@TableName("mw_homework_manage_table")
public class HomeworkEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 作业名称
     */
    @TableField("homework_name")
    private String homeworkName;

    /**
     * 作业所在树ID
     */
    @TableField("homework_tree_id")
    private Integer homeworkTreeId;

    /**
     * 作业描述
     */
    @TableField("homework_desc")
    private String homeworkDesc;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 更新人
     */
    @TableField("updater")
    private String updater;

    /**
     * 删除标志位
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    /**
     * 作业版本ID
     */
    @TableField("version_id")
    private Integer homeworkVersionId;


    /**
     * 更新人
     */
    @TableField("variable_ids")
    private String variableIds;
    /**
     * 作业步骤列表
     */
    @TableField(exist = false)
    private List<HomeworkParam.HomeworkChildParam> stepList;
}
