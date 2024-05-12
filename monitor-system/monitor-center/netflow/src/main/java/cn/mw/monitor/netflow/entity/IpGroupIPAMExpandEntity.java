package cn.mw.monitor.netflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className IpGroupIPAMExpandEntity
 * @description IP地址端IPAM的拓展信息
 * @date 2022/8/24
 */
@Data
@TableName(value = "mw_netflow_ip_group_expand_ipam")
public class IpGroupIPAMExpandEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * IP地址组主键ID
     */
    @TableField("ip_group_id")
    private Integer IpGroupId;

    /**
     * 节点类别（grouping：文件夹，iPaddresses：IP地址段）
     */
    @TableField("item_type")
    private String itemType;

    /**
     * 节点ID，对应着IP地址管理的树节点ID
     */
    @TableField("item_id")
    private Integer itemId;

    /**
     * 节点的父ID
     */
    @TableField("item_pid")
    private Integer itemPid;


    /**
     * IP地址范围，如1.0.0.1-1.0.0.19
     */
    @TableField("item_label")
    private String itemLabel;


}
