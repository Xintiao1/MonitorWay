package cn.mw.monitor.script.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className HomeworkRelationEntity
 * @description 作业关联数据实体类
 * @date 2022/5/20
 */
@Data
@TableName("mw_homework_relation_table")
public class HomeworkRelationEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 作业ID
     */
    @TableField("homework_id")
    private Integer homeworkId;

    /**
     * 执行ID
     */
    @TableField("exec_id")
    private Integer execId;

    /**
     * 作业版本ID,用于记录作业信息
     */
    @TableField("homework_version_id")
    private Integer homeworkVersionId;

    /**
     * 作业执行排序
     */
    @TableField("homework_sort")
    private Integer homeworkSort;
}
