package cn.mw.monitor.netflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className IpGroupExpandEntity
 * @description IP地址组拓展信息
 * @date 2022/8/24
 */
@Data
@TableName(value = "mw_netflow_ip_group_expand_nfa")
public class IpGroupNFAExpandEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * IP地址组主键ID
     */
    @TableField("ip_group_id")
    private Integer ipGroupId;

    /**
     * IP类别（1：ipv4，2：ipv6）
     */
    @TableField("ip_type")
    private Integer ipType;

    /**
     * IP对象类别（1：ip范围，2：ip地址段：3：ip地址清单）
     */
    @TableField("ip_object_type")
    private Integer objectType;

    /**
     * IP地址范围，如1.0.0.1-1.0.0.19
     */
    @TableField("ip_range")
    private String ipRange;

    /**
     * IP地址段
     */
    @TableField("ip_phase")
    private String ipPhase;

    /**
     * ip地址清单，多个用,拼接
     */
    @TableField("ip_list")
    private String ipList;

}
