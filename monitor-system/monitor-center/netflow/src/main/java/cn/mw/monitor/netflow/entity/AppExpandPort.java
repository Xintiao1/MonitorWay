package cn.mw.monitor.netflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className AppExpandPort
 * @description 应用拓展信息--端口信息
 * @date 2022/8/25
 */
@Data
@TableName(value = "mw_netflow_app_expand_port")
public class AppExpandPort {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 端口内容
     */
    @TableField("port_content")
    private String portContent;

    /**
     * 监控状态（0：未监控，1：监控中）
     */
    @TableField("monitor_state")
    private Boolean monitorState;

    /**
     * 协议类别（1：TCP  ，2：UDP）
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
     * 父ID（应用基础信息表的主键ID）
     */
    @TableField("parent_id")
    private Integer parentId;

}
