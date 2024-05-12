package cn.mw.monitor.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author swy
 * @since 2023-11-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("mw_user_group_mapper")
public class MwUserGroupMapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增序列
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户组ID
     */
    @TableField("group_id")
    private Integer groupId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 删除标识
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;


}
