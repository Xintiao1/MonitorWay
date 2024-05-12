package cn.mw.monitor.netflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className ApplicationEntity
 * @description 应用基础信息
 * @date 2022/8/25
 */
@Data
@TableName(value = "mw_netflow_application_base")
public class ApplicationEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 应用名称
     */
    @TableField("application_name")
    private String applicationName;

    /**
     * 监控状态（0：未监控，1：监控中）
     */
    @TableField("monitor_state")
    private Boolean monitorState;

    /**
     * 协议类别（0:全部,1：TCP  ，2：UDP）
     */
    @TableField("protocol_type")
    private Integer protocolType;

    /**
     * IP组源ID（存储IP地址组基础信息表主键ID）
     */
    @TableField("source_ip_id")
    private Integer sourceIpId;

    /**
     * IP组目标ID（存储IP地址组基础信息表主键ID）
     */
    @TableField("dest_ip_id")
    private Integer destIpId;

    /**
     * 应用对应的树ID
     */
    @TableField("tree_id")
    private Integer treeId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 是否删除（0：未删除，1；已删除）
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    /**
     * 端口列表
     */
    @TableField(exist = false)
    private List<AppExpandPort> portList;
}
