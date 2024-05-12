package cn.mw.monitor.user.model;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author swy
 * @since 2023-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@KeySequence(value = "mw_user_session_seq",clazz = Integer.class)
@TableName("mw_user_session")
public class MwUserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("user_name")
    private String userName;

    @TableField("login_name")
    private String loginName;

    @TableField("org_id")
    private String orgId;

    @TableField("org_name")
    private String orgName;

    @TableField("login_time")
    private Date loginTime;

    @TableField("logout_time")
    private Date logoutTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("online_time")
    private Long onlineTime;

}
