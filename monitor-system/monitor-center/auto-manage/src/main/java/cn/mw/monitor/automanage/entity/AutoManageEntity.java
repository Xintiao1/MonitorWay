package cn.mw.monitor.automanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className AutoManageEntity
 * @description 自动化运维实体类
 * @date 2022/4/4
 */
@Data
@TableName("mw_child_server_table")
public class AutoManageEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     * 服务名称
     */
    @TableField("server_name")
    private String serverName;

    /**
     * 服务IP
     */
    @TableField("server_ip")
    private String serverIp;

    /**
     * 服务端口
     */
    @TableField("server_port")
    private String serverPort;

    /**
     * 版本
     */
    @TableField("server_version")
    private String version;

    /**
     * 服务是否有效
     */
    @TableField("server_enable")
    private Boolean serverEnable;

    /**
     * 是否删除
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

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


}
